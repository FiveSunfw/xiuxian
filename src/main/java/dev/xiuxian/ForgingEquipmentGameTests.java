package dev.xiuxian;

import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Xiuxian.ID)
@PrefixGameTestTemplate(false)
public class ForgingEquipmentGameTests {
    private static TagKey<Item> tag(String path) {
        return TagKey.create(Registries.ITEM, new ResourceLocation(Xiuxian.ID, path));
    }

    @GameTest(template = "empty")
    public static void swordsAndArmorUseRealEquipmentSlotsAndTags(GameTestHelper helper) {
        for (var entry : ForgingContent.SWORDS.entrySet()) {
            ItemStack stack = new ItemStack(entry.getValue().get());
            helper.assertTrue(stack.getItem() instanceof SwordItem, "Named sword must be a SwordItem: " + entry.getKey());
            helper.assertTrue(stack.is(ItemTags.SWORDS) && stack.is(tag("forging_swords")), "Sword tags: " + entry.getKey());
            helper.assertTrue(Mob.getEquipmentSlotForItem(stack) == EquipmentSlot.MAINHAND, "Held sword slot: " + entry.getKey());
        }
        ItemStack blankSword = new ItemStack(ForgingContent.BLANKS.get("spirit_gold_sword").get());
        helper.assertTrue(blankSword.getItem() instanceof SwordItem && blankSword.is(ItemTags.SWORDS), "Blank sword is usable");

        for (var entry : ForgingContent.ARMOR.entrySet()) verifyArmor(helper, entry.getKey(), entry.getValue().get());
        for (var entry : ForgingContent.BLANKS.entrySet()) {
            if (!entry.getKey().endsWith("_sword")) verifyArmor(helper, entry.getKey(), entry.getValue().get());
        }
        helper.succeed();
    }

    private static void verifyArmor(GameTestHelper helper, String id, Item item) {
        ItemStack stack = new ItemStack(item);
        EquipmentSlot slot = id.endsWith("_boots") ? EquipmentSlot.FEET
            : id.endsWith("_leggings") ? EquipmentSlot.LEGS
            : id.endsWith("_chestplate") || id.endsWith("_armor") ? EquipmentSlot.CHEST : EquipmentSlot.HEAD;
        String tagPath = switch (slot) {
            case HEAD -> "forging_helmets";
            case CHEST -> "forging_chestplates";
            case LEGS -> "forging_leggings";
            case FEET -> "forging_boots";
            default -> throw new IllegalStateException("Unexpected armor slot");
        };
        helper.assertTrue(item instanceof ArmorItem, "Wearable armor: " + id);
        helper.assertTrue(Mob.getEquipmentSlotForItem(stack) == slot, "Armor slot: " + id);
        helper.assertTrue(stack.is(tag(tagPath)) && stack.is(TagKey.create(Registries.ITEM,
            new ResourceLocation("minecraft", "trimmable_armor"))), "Armor tags: " + id);
    }
}
