package dev.xiuxian;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.RegistryObject;
import java.util.EnumMap;
import java.util.Map;

public final class SpiritualFarming {
    public static final RegistryObject<Block> SOIL = Xiuxian.BLOCKS.register("spirit_farmland", () -> new Block(BlockBehaviour.Properties.copy(Blocks.DIRT).strength(.8F)));
    public static final RegistryObject<Item> SOIL_ITEM = Xiuxian.ITEMS.register("spirit_farmland", () -> new BlockItem(SOIL.get(), new Item.Properties()));
    public static final RegistryObject<Block> KITCHEN = Xiuxian.BLOCKS.register("spirit_kitchen", SpiritKitchenBlock::new);
    public static final RegistryObject<Item> KITCHEN_ITEM = Xiuxian.ITEMS.register("spirit_kitchen", () -> new BlockItem(KITCHEN.get(), new Item.Properties()));
    public static final RegistryObject<BlockEntityType<SpiritKitchenEntity>> ENTITY = Xiuxian.ENTITIES.register("spirit_kitchen", () -> BlockEntityType.Builder.of(SpiritKitchenEntity::new, KITCHEN.get()).build(null));
    public static final RegistryObject<MenuType<SpiritKitchenMenu>> MENU = Xiuxian.MENUS.register("spirit_kitchen", () -> IForgeMenuType.create(SpiritKitchenMenu::new));
    public static final Map<FoodPlantKind, RegistryObject<Block>> CROPS = new EnumMap<>(FoodPlantKind.class);
    public static final Map<FoodPlantKind, RegistryObject<Item>> PLANTS = new EnumMap<>(FoodPlantKind.class);
    public static final Map<FoodPlantKind, RegistryObject<Feature<NoneFeatureConfiguration>>> FEATURES = new EnumMap<>(FoodPlantKind.class);
    public static final Map<SpiritFoodKind, RegistryObject<Item>> FOODS = new EnumMap<>(SpiritFoodKind.class);

    public static final RegistryObject<MobEffect> MANA_REGEN = CultivationResources.EFFECTS.register("spirit_food_mana_regen", ManaRegen::new);
    public static final RegistryObject<MobEffect> ATTACK = CultivationResources.EFFECTS.register("spirit_food_attack", () ->
        new Marker(0xc7472d).addAttributeModifier(Attributes.ATTACK_DAMAGE, "49b4a751-59d7-47ec-9ed6-69dfb517b011", 3, AttributeModifier.Operation.ADDITION));
    public static final RegistryObject<MobEffect> VITALITY = CultivationResources.EFFECTS.register("spirit_food_vitality", () ->
        new Marker(0xc67b41).addAttributeModifier(Attributes.MAX_HEALTH, "49b4a751-59d7-47ec-9ed6-69dfb517b012", 6, AttributeModifier.Operation.ADDITION)
            .addAttributeModifier(Attributes.KNOCKBACK_RESISTANCE, "49b4a751-59d7-47ec-9ed6-69dfb517b013", .2, AttributeModifier.Operation.ADDITION));
    public static final RegistryObject<MobEffect> DEFENSE = CultivationResources.EFFECTS.register("spirit_food_defense", () ->
        new Marker(0x9d8a5c).addAttributeModifier(Attributes.ARMOR, "49b4a751-59d7-47ec-9ed6-69dfb517b014", 4, AttributeModifier.Operation.ADDITION));
    public static final RegistryObject<MobEffect> SPEED = CultivationResources.EFFECTS.register("spirit_food_speed", () ->
        new Marker(0x70b5a5).addAttributeModifier(Attributes.MOVEMENT_SPEED, "49b4a751-59d7-47ec-9ed6-69dfb517b015", .2, AttributeModifier.Operation.MULTIPLY_TOTAL));
    public static final RegistryObject<MobEffect> STARLIGHT = CultivationResources.EFFECTS.register("spirit_food_starlight", () -> new Marker(0x8a9af1));

    static {
        for (FoodPlantKind kind : FoodPlantKind.values()) {
            RegistryObject<Block> block = Xiuxian.BLOCKS.register(kind.id, () -> new FoodPlantBlock(kind));
            CROPS.put(kind, block);
            PLANTS.put(kind, Xiuxian.ITEMS.register(kind.id, () -> new BlockItem(block.get(), new Item.Properties())));
            FEATURES.put(kind, CultivationResources.FEATURES.register("wild_" + kind.id, () -> new WildFoodPlantFeature(kind)));
        }
        for (SpiritFoodKind kind : SpiritFoodKind.values())
            FOODS.put(kind, Xiuxian.ITEMS.register(kind.id, () -> new SpiritFoodItem(kind)));
    }

    private SpiritualFarming() {}
    public static void register() {}
    private static class Marker extends MobEffect { Marker(int color) { super(MobEffectCategory.BENEFICIAL, color); } }
    private static final class ManaRegen extends Marker {
        ManaRegen() { super(0x65d3d1); }
        @Override public boolean isDurationEffectTick(int duration, int amplifier) { return duration % 20 == 0; }
        @Override public void applyEffectTick(LivingEntity entity, int amplifier) {
            if (entity instanceof ServerPlayer player) CultivationFoundation.restoreMana(player, 2);
        }
    }
}
