package dev.xiuxian;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Registry entries for the first tier of forging content. Gameplay is added separately. */
public final class ForgingContent {
    public static final RegistryObject<Item> SPIRIT_GOLD_INGOT = item("spirit_gold_ingot");
    public static final Map<String, RegistryObject<Item>> BLANKS = new LinkedHashMap<>();
    public static final Map<String, RegistryObject<Item>> ARMOR = new LinkedHashMap<>();
    public static final Map<String, RegistryObject<Item>> SWORDS = new LinkedHashMap<>();

    public static final RegistryObject<Block> BASIC_FORGING_TABLE = Xiuxian.BLOCKS.register(
        "basic_forging_table",
        ForgingTableBlock::new
    );
    public static final RegistryObject<Item> BASIC_FORGING_TABLE_ITEM = Xiuxian.ITEMS.register(
        "basic_forging_table",
        () -> new BlockItem(BASIC_FORGING_TABLE.get(), new Item.Properties())
    );

    static {
        for (String id : List.of(
            "spirit_gold_sword", "spirit_gold_helmet", "spirit_gold_chestplate",
            "spirit_gold_leggings", "spirit_gold_boots"
        )) {
            BLANKS.put(id, id.endsWith("_sword") ? sword(id, Tiers.IRON) : armor(id, ArmorMaterials.IRON));
        }

        for (String id : List.of(
            "spirit_horn_crown", "nightwing_hood", "frost_silk_headband", "thunder_pattern_headband",
            "frost_horn_helmet", "wolf_hide_hunting_chestplate", "rock_armor_chestplate",
            "sand_scorpion_chestplate", "king_armor", "jade_scale_water_armor", "wolf_pursuit_leggings",
            "marsh_walking_leggings", "vine_leggings", "tide_scale_leggings", "python_scale_leggings",
            "spirit_horn_light_boots", "sand_walking_boots", "rock_step_boots", "redtail_swift_boots",
            "falling_cloud_boots"
        )) {
            ArmorMaterials material = switch (id) {
                case "spirit_horn_crown", "nightwing_hood", "wolf_hide_hunting_chestplate",
                     "wolf_pursuit_leggings", "spirit_horn_light_boots", "sand_walking_boots" -> ArmorMaterials.CHAIN;
                case "thunder_pattern_headband", "sand_scorpion_chestplate", "king_armor",
                     "jade_scale_water_armor", "python_scale_leggings" -> ArmorMaterials.DIAMOND;
                default -> ArmorMaterials.IRON;
            };
            ARMOR.put(id, armor(id, material));
        }

        for (String id : List.of(
            "spirit_horn_light_sword", "azure_fang_hunting_sword", "rock_fang_armor_piercing_sword",
            "frost_silk_binding_sword", "windfeather_returning_sword", "viper_fang_poison_sword",
            "crystal_horn_rockbreaker_sword", "thunder_spine_shock_sword", "king_tusk_greatsword",
            "jade_python_binding_sword", "redtail_flame_sword"
        )) {
            Tier tier = switch (id) {
                case "thunder_spine_shock_sword", "king_tusk_greatsword", "jade_python_binding_sword" -> Tiers.DIAMOND;
                default -> Tiers.IRON;
            };
            SWORDS.put(id, sword(id, tier));
        }
    }

    private ForgingContent() {}

    private static RegistryObject<Item> item(String id) {
        return Xiuxian.ITEMS.register(id, () -> new Item(new Item.Properties()));
    }

    private static RegistryObject<Item> sword(String id, Tier tier) {
        return Xiuxian.ITEMS.register(id, () -> new SwordItem(tier, 3, -2.4F, new Item.Properties()));
    }

    private static RegistryObject<Item> armor(String id, ArmorMaterials material) {
        ArmorItem.Type type;
        if (id.endsWith("_leggings")) type = ArmorItem.Type.LEGGINGS;
        else if (id.endsWith("_boots")) type = ArmorItem.Type.BOOTS;
        else if (id.endsWith("_chestplate") || id.endsWith("_armor")) type = ArmorItem.Type.CHESTPLATE;
        else type = ArmorItem.Type.HELMET;
        return Xiuxian.ITEMS.register(id, () -> new ArmorItem(material, type, new Item.Properties()));
    }

    /** Forces this class's deferred entries to be declared before the mod registers its event bus. */
    public static void register() {}
}
