package dev.xiuxian;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;

import java.util.*;

/** Single source of truth for material compatibility, quality and preview text. */
public final class ForgingRules {
    public enum Slot { SWORD, HEAD, CHEST, LEGS, FEET }
    public record Preview(String error, int tier, List<String> existing, String added, Slot slot) {
        public boolean valid() { return error.isEmpty(); }
    }

    private static final Map<String, Integer> LAYERS = new LinkedHashMap<>();
    private static final Map<Slot, Map<String, String>> EFFECTS = new EnumMap<>(Slot.class);

    static {
        for (Slot slot : Slot.values()) EFFECTS.put(slot, new LinkedHashMap<>());
        layer(1, "spirit_horn");
        layer(2, "wolf_pelt", "azure_fang");
        layer(3, "rockhide_plate", "curved_tusk");
        layer(4, "viper_fang", "venom_sac");
        layer(5, "frost_silk", "frost_gland");
        layer(6, "wind_feather", "eagle_talon");
        layer(7, "thunder_scale", "charged_spine");
        layer(8, "thick_rock_plate", "king_tusk");
        layer(9, "jade_scale", "python_sinew", "beast_marrow");

        effects(Slot.SWORD,
            "spirit_horn", "飞剑速度+12%，命中伤害−8%", "wolf_pelt", "普通挥剑攻速+8%，飞剑伤害−5%",
            "azure_fang", "追击同一目标时飞剑速度+10%", "rockhide_plate", "普通剑击伤害+8%，飞剑速度−10%",
            "curved_tusk", "飞剑命中破甲2点，持续3秒", "viper_fang", "飞剑命中造成流血，总计2点伤害，同目标12秒冷却",
            "venom_sac", "飞剑命中附加中毒Ⅰ，持续3秒，同目标12秒冷却", "frost_silk", "飞剑返程速度+10%",
            "frost_gland", "飞剑命中施加缓速Ⅱ，持续2秒，同目标10秒冷却", "wind_feather", "飞剑出击速度+15%",
            "eagle_talon", "飞剑返程速度+25%，归剑耗灵−1", "thunder_scale", "对雷纹蜥伤害+8%",
            "charged_spine", "同一目标连续命中3次追加2点雷伤，每8秒至多一次", "thick_rock_plate", "近战减伤8%，飞剑速度−20%",
            "king_tusk", "飞剑命中伤害+12%", "jade_scale", "水中飞剑速度+12%",
            "python_sinew", "飞剑命中施加缓速Ⅰ，持续2秒，同目标12秒冷却", "beast_marrow", "灵力上限+20");
        effects(Slot.HEAD,
            "spirit_horn", "跳跃高度小幅提升", "wolf_pelt", "潜行移动速度+8%",
            "rockhide_plate", "护甲+1，移动速度−3%", "frost_silk", "普通减速持续时间−40%",
            "thunder_scale", "雷纹放电伤害−25%", "thick_rock_plate", "护甲+1，移动速度−4%",
            "jade_scale", "水下视野改善", "beast_marrow", "灵力上限+20");
        effects(Slot.CHEST,
            "wolf_pelt", "冲刺移动速度+8%", "rockhide_plate", "护甲+2，移动速度−5%",
            "venom_sac", "中毒持续时间−35%", "thick_rock_plate", "护甲+2，移动速度−12%",
            "king_tusk", "受到冲撞伤害−20%", "jade_scale", "水中移动速度+15%",
            "python_sinew", "水下氧气消耗放慢", "beast_marrow", "灵力上限+20");
        effects(Slot.LEGS,
            "wolf_pelt", "靠近目标时移动速度+6%", "rockhide_plate", "护甲+1，移动速度−3%",
            "venom_sac", "中毒持续时间−25%", "frost_silk", "蛛网移动惩罚−40%",
            "jade_scale", "氧气消耗速度−30%", "python_sinew", "水中受到击退−20%",
            "beast_marrow", "灵力上限+20");
        effects(Slot.FEET,
            "spirit_horn", "跳跃高度小幅提升",
            "azure_fang", "朝目标前进时移动速度+6%", "wind_feather", "摔落伤害−40%",
            "king_tusk", "受到击退距离−20%，移动速度−4%",
            "beast_marrow", "灵力上限+20");
    }

    private ForgingRules() {}

    private static void layer(int value, String... ids) {
        for (String id : ids) LAYERS.put(id, value);
    }

    private static void effects(Slot slot, String... pairs) {
        for (int i = 0; i < pairs.length; i += 2) EFFECTS.get(slot).put(pairs[i], pairs[i + 1]);
    }

    public static Slot slot(ItemStack equipment) {
        if (equipment.getItem() instanceof SwordItem) return Slot.SWORD;
        if (equipment.getItem() instanceof ArmorItem armor) {
            return switch (armor.getType()) {
                case HELMET -> Slot.HEAD;
                case CHESTPLATE -> Slot.CHEST;
                case LEGGINGS -> Slot.LEGS;
                case BOOTS -> Slot.FEET;
            };
        }
        return null;
    }

    public static boolean isEquipmentBlank(ItemStack stack) {
        if (stack.isEmpty()) return false;
        var key = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(stack.getItem());
        return key != null && key.getNamespace().equals(Xiuxian.ID) && key.getPath().startsWith("spirit_gold_") && slot(stack) != null;
    }

    public static String materialId(ItemStack stack) {
        if (stack.isEmpty()) return "";
        var key = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(stack.getItem());
        return key != null && key.getNamespace().equals(Xiuxian.ID) && LAYERS.containsKey(key.getPath()) ? key.getPath() : "";
    }

    public static int materialLayer(String id) { return LAYERS.getOrDefault(id, 0); }
    public static int qualityTier(int layer) { return layer <= 2 ? 1 : layer <= 6 ? 2 : 3; }
    public static String qualityName(int tier) { return switch (tier) { case 1 -> "一阶下品"; case 2 -> "一阶中品"; default -> "一阶上品"; }; }
    public static String effect(String id, Slot slot) { return EFFECTS.get(slot).getOrDefault(id, ""); }
    public static String effectKey(String id, Slot slot) { return "forging.xiuxian.affix." + slot.name().toLowerCase(Locale.ROOT) + "." + id; }
    public static boolean compatible(String id, Slot slot) { return !effect(id, slot).isEmpty(); }
    public static List<String> materials() { return List.copyOf(LAYERS.keySet()); }

    public static Preview preview(ItemStack equipment, ItemStack materialStack) {
        Slot slot = slot(equipment);
        if (equipment.isEmpty() || slot == null || !(isEquipmentBlank(equipment) || ForgedItemData.isInfused(equipment)))
            return new Preview("equipment", 0, List.of(), "", slot);
        String added = materialId(materialStack);
        if (added.isEmpty()) return new Preview("material", 0, List.of(), "", slot);
        List<String> existing = ForgedItemData.materials(equipment);
        if (existing.size() >= 2) return new Preview("two_materials", 0, existing, "", slot);
        if (existing.contains(added)) return new Preview("duplicate", 0, existing, "", slot);
        if (!compatible(added, slot)) return new Preview("incompatible", 0, existing, "", slot);
        int layer = Math.max(materialLayer(added), existing.stream().mapToInt(ForgingRules::materialLayer).max().orElse(0));
        return new Preview("", qualityTier(layer), existing, effect(added, slot), slot);
    }

    public static List<String> compatibleMaterials(Slot slot) {
        return LAYERS.keySet().stream().filter(id -> compatible(id, slot)).toList();
    }
}
