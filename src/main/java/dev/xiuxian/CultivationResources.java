package dev.xiuxian;

import net.minecraft.world.item.*;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.feature.*;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.registries.*;

public final class CultivationResources {
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, Xiuxian.ID);
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, Xiuxian.ID);
    public static final RegistryObject<MobEffect> NOURISHMENT = EFFECTS.register("spiritual_nourishment", () -> new Nourishment());
    public static final RegistryObject<Block> ORE = Xiuxian.BLOCKS.register("spirit_stone_ore", () -> new VeinBlocks.Ore(false));
    public static final RegistryObject<Block> DEEP_ORE = Xiuxian.BLOCKS.register("deepslate_spirit_stone_ore", () -> new VeinBlocks.Ore(true));
    public static final RegistryObject<Item> ORE_ITEM = Xiuxian.ITEMS.register("spirit_stone_ore", () -> new BlockItem(ORE.get(), new Item.Properties()));
    public static final RegistryObject<Item> DEEP_ORE_ITEM = Xiuxian.ITEMS.register("deepslate_spirit_stone_ore", () -> new BlockItem(DEEP_ORE.get(), new Item.Properties()));
    public static final RegistryObject<Item> SPIRIT_COPPER = Xiuxian.ITEMS.register("spirit_copper_ingot", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> STONE = Xiuxian.ITEMS.register("spirit_stone", () -> new CultivationResourceItem(CultivationResourceItem.Kind.STONE));
    public static final RegistryObject<Item> STONE_2 = Xiuxian.ITEMS.register("spirit_stone_tier_2", () -> new CultivationResourceItem(CultivationResourceItem.Kind.STONE_2));
    public static final RegistryObject<Item> PILL = Xiuxian.ITEMS.register("qi_gathering_pill", () -> new CultivationResourceItem(CultivationResourceItem.Kind.PILL));
    public static final RegistryObject<Item> MEAL = Xiuxian.ITEMS.register("spirit_meal", SpiritMeal::new);
    public static final RegistryObject<Item> MANUAL_1 = Xiuxian.ITEMS.register("breathing_manual_1", () -> new TechniqueItem(1));
    public static final RegistryObject<Item> MANUAL_2 = Xiuxian.ITEMS.register("breathing_manual_2", () -> new TechniqueItem(2));
    public static final RegistryObject<Item> MANUAL_3 = Xiuxian.ITEMS.register("breathing_manual_3", () -> new TechniqueItem(3));
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> SCATTERED = FEATURES.register("scattered_spirit_stones", () -> new SpiritVeinFeature(1, 2));
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> SMALL = FEATURES.register("small_spirit_vein", () -> new SpiritVeinFeature(12, 16));
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> MEDIUM = FEATURES.register("medium_spirit_vein", () -> new SpiritVeinFeature(32, 48));
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> LARGE = FEATURES.register("large_spirit_vein", () -> new SpiritVeinFeature(64, 96));
    private CultivationResources() {}
    public static void register(net.minecraftforge.eventbus.api.IEventBus bus) { FoundationItems.register(); Alchemy.register(); VeinBlocks.register(); EFFECTS.register(bus); FEATURES.register(bus); }
    private static class Nourishment extends MobEffect { Nourishment() { super(MobEffectCategory.BENEFICIAL, 0xc4d998); } }
    private static class SpiritMeal extends BowlFoodItem {
        SpiritMeal() { super(new Item.Properties().stacksTo(1).food(new FoodProperties.Builder().nutrition(6).saturationMod(0.6f).alwaysEat().build())); }
        @Override public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
            ItemStack result = super.finishUsingItem(stack, level, entity);
            if (!level.isClientSide) entity.addEffect(new MobEffectInstance(NOURISHMENT.get(), Cultivation.FOOD_SECONDS.get() * 20));
            return result;
        }
    }
}
