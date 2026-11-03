package dev.xiuxian;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

/** Saved infusion data stays on the equipment stack, so trading and restarting preserve it. */
@Mod.EventBusSubscriber(modid = Xiuxian.ID)
public final class ForgedItemData {
    private static final String KEY = "XiuxianForged";

    private ForgedItemData() {}

    public static boolean isInfused(ItemStack stack) { return !materials(stack).isEmpty(); }

    public static List<String> materials(ItemStack stack) {
        CompoundTag data = stack.getTagElement(KEY);
        if (data == null) return List.of();
        List<String> result = new ArrayList<>();
        for (var tag : data.getList("Materials", 8)) {
            String id = tag.getAsString();
            if (ForgingRules.materialLayer(id) > 0 && !result.contains(id)) result.add(id);
        }
        return result.stream().sorted().toList();
    }

    public static int tier(ItemStack stack) {
        CompoundTag data = stack.getTagElement(KEY);
        return data == null ? 0 : Math.max(0, Math.min(3, data.getInt("Tier")));
    }

    public static ItemStack infuse(ItemStack original, String materialId) {
        ItemStack result = original.copyWithCount(1);
        CompoundTag data = result.getOrCreateTagElement(KEY);
        if (data.getBoolean("GeneratedName") && !data.getString("GeneratedNameJson").equals(Component.Serializer.toJson(result.getHoverName())))
            data.putBoolean("GeneratedName", false);
        List<String> materials = new ArrayList<>(materials(result));
        if (materials.contains(materialId) || materials.size() >= 2) return ItemStack.EMPTY;
        materials.add(materialId);
        materials.sort(String::compareTo);
        int maxLayer = materials.stream().mapToInt(ForgingRules::materialLayer).max().orElse(1);
        ListTag list = new ListTag();
        materials.forEach(id -> list.add(StringTag.valueOf(id)));
        data.put("Materials", list);
        data.putInt("Tier", ForgingRules.qualityTier(maxLayer));
        if (!data.getBoolean("GeneratedName") && !result.hasCustomHoverName()) data.putBoolean("GeneratedName", true);
        if (data.getBoolean("GeneratedName")) updateGeneratedName(result, materials);
        return result;
    }

    private static void updateGeneratedName(ItemStack stack, List<String> materials) {
        var key = ForgeRegistries.ITEMS.getKey(stack.getItem());
        String path = key == null ? "" : key.getPath();
        String preset = presetName(path, materials);
        if (!preset.isEmpty()) {
            stack.setHoverName(Component.translatable("forging.xiuxian.name." + preset));
            stack.getOrCreateTagElement(KEY).putString("GeneratedNameJson", Component.Serializer.toJson(stack.getHoverName()));
            return;
        }
        Component base = Component.translatable(key == null ? stack.getDescriptionId() : "item." + key.getNamespace() + "." + key.getPath());
        Component first = materialName(materials.get(0));
        Component name = materials.size() == 1
            ? Component.translatable("forging.xiuxian.forged_name_one", base, first)
            : Component.translatable("forging.xiuxian.forged_name_two", base, first, materialName(materials.get(1)));
        stack.setHoverName(name);
        stack.getOrCreateTagElement(KEY).putString("GeneratedNameJson", Component.Serializer.toJson(name));
    }

    private static String presetName(String equipment, List<String> materials) {
        if (equipment.equals("spirit_gold_sword") && materials.contains("curved_tusk"))
            return materials.contains("rockhide_plate") ? "heavy_rock_fang_sword" : "rock_fang_sword";
        if (equipment.equals("spirit_gold_sword") && materials.containsAll(List.of("wind_feather", "eagle_talon")))
            return "windfeather_returning_sword";
        if (equipment.equals("spirit_gold_boots") && materials.contains("wind_feather")) return "falling_cloud_boots";
        if (equipment.equals("spirit_gold_chestplate") && materials.contains("rockhide_plate")) return "rockhide_chestplate";
        return "";
    }

    public static Component materialName(String id) {
        var item = ForgeRegistries.ITEMS.getValue(new net.minecraft.resources.ResourceLocation(Xiuxian.ID, id));
        return item == null ? Component.literal(id) : Component.translatable(item.getDescriptionId());
    }

    @SubscribeEvent public static void tooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        List<String> materials = materials(stack);
        if (materials.isEmpty()) return;
        event.getToolTip().add(Component.translatable("forging.xiuxian.quality", ForgingRules.qualityName(tier(stack))));
        var slot = ForgingRules.slot(stack);
        for (String id : materials) {
            event.getToolTip().add(Component.translatable("forging.xiuxian.infused_material", materialName(id)));
            if (slot != null) event.getToolTip().add(Component.translatable(ForgingRules.effectKey(id, slot)));
        }
    }
}
