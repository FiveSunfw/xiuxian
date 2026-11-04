package dev.xiuxian;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Xiuxian.ID)
@PrefixGameTestTemplate(false)
public class SpiritualFarmingGameTests {
    @GameTest(template = "empty")
    public static void soilAndGrowthConditions(GameTestHelper h) {
        BlockPos pos = new BlockPos(1, 2, 1);
        h.setBlock(pos.below(), SpiritualFarming.SOIL.get());
        for (FoodPlantKind kind : FoodPlantKind.values()) {
            var crop = (FoodPlantBlock) SpiritualFarming.CROPS.get(kind).get();
            h.setBlock(pos, crop.defaultBlockState());
            var state = h.getBlockState(pos);
            h.assertTrue(state.canSurvive(h.getLevel(), h.absolutePos(pos)), kind + " grows on spirit soil");
            h.assertTrue(crop.isValidBonemealTarget(h.getLevel(), h.absolutePos(pos), state, false), kind + " accepts bonemeal");
            crop.performBonemeal(h.getLevel(), h.getLevel().random, h.absolutePos(pos), state);
            h.assertTrue(h.getBlockState(pos).getValue(FoodPlantBlock.AGE) == 1, kind + " bonemeal advances one stage");
        }
        h.assertTrue(!FoodPlantBlock.growthAllowed(FoodPlantKind.JADE_RICE, 8, false, true), "Rice needs light 9");
        h.assertTrue(FoodPlantBlock.growthAllowed(FoodPlantKind.JADE_RICE, 9, false, true), "Rice grows at light 9");
        h.assertTrue(!FoodPlantBlock.growthAllowed(FoodPlantKind.STAR_FLOWER, 15, false, true), "Star flower requires night");
        h.assertTrue(!FoodPlantBlock.growthAllowed(FoodPlantKind.STAR_FLOWER, 0, true, false), "Star flower requires sky");
        h.assertTrue(FoodPlantBlock.growthAllowed(FoodPlantKind.STAR_FLOWER, 0, true, true), "Star flower grows under night sky");
        h.assertTrue(FoodPlantBlock.growthAllowed(FoodPlantKind.EARTH_GINGER, 7, false, false), "Ginger grows at light 7");
        h.assertTrue(!FoodPlantBlock.growthAllowed(FoodPlantKind.EARTH_GINGER, 8, false, false), "Ginger requires darkness");
        h.setBlock(pos.below(), Blocks.DIRT);
        var rice = (FoodPlantBlock) SpiritualFarming.CROPS.get(FoodPlantKind.JADE_RICE).get();
        h.setBlock(pos, rice.defaultBlockState());
        h.assertTrue(!rice.isValidBonemealTarget(h.getLevel(), h.absolutePos(pos), h.getBlockState(pos), false), "Ordinary soil rejects bonemeal");
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void kitchenRecipesAndPersistence(GameTestHelper h) {
        BlockPos pos = new BlockPos(1, 1, 1);
        for (var recipe : SpiritKitchenRecipes.all()) {
            h.setBlock(pos, Blocks.AIR);
            h.setBlock(pos, SpiritualFarming.KITCHEN.get());
            var stove = (SpiritKitchenEntity) h.getBlockEntity(pos);
            stove.setItem(0, new ItemStack(recipe.mains().get(0)));
            stove.setItem(1, new ItemStack(SpiritualFarming.PLANTS.get(recipe.plant()).get(), recipe.plantCount()));
            if (recipe.container() != null) stove.setItem(2, new ItemStack(recipe.container()));
            stove.setItem(3, new ItemStack(Items.COAL));
            for (int i = 0; i < 199; i++) SpiritKitchenEntity.tick(h.getLevel(), stove.getBlockPos(), h.getLevel().getBlockState(stove.getBlockPos()), stove);
            h.assertTrue(stove.getItem(4).isEmpty(), recipe.food() + " does not finish early");
            var saved = stove.saveWithoutMetadata();
            var restored = new SpiritKitchenEntity(stove.getBlockPos(), stove.getBlockState());
            restored.load(saved);
            h.assertTrue(restored.saveWithoutMetadata().getInt("Progress") == saved.getInt("Progress"), "Progress persists");
            SpiritKitchenEntity.tick(h.getLevel(), stove.getBlockPos(), h.getLevel().getBlockState(stove.getBlockPos()), stove);
            h.assertTrue(stove.getItem(4).is(recipe.output().getItem()), recipe.food() + " produces correct meal");
            h.assertTrue(stove.getItem(1).isEmpty(), recipe.food() + " consumes plant");
            if (recipe.mains().get(0) == Items.HONEY_BOTTLE) {
                h.assertTrue(stove.getItem(2).is(Items.GLASS_BOTTLE), "Honey bottle returned");
                h.assertTrue(stove.canTakeItemThroughFace(2, stove.getItem(2), Direction.DOWN), "Hopper can extract returned bottle");
            }
        }
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void spiritFuelAndBlockedOutput(GameTestHelper h) {
        BlockPos pos = new BlockPos(1, 1, 1);
        h.setBlock(pos, SpiritualFarming.KITCHEN.get());
        var stove = (SpiritKitchenEntity) h.getBlockEntity(pos);
        stove.setItem(0, new ItemStack(Items.BREAD));
        stove.setItem(1, new ItemStack(SpiritualFarming.PLANTS.get(FoodPlantKind.GREEN_WHEAT).get(), 2));
        stove.setItem(3, new ItemStack(CultivationResources.STONE.get()));
        stove.setItem(4, new ItemStack(Items.DIRT));
        for (int i = 0; i < 10; i++) SpiritKitchenEntity.tick(h.getLevel(), stove.getBlockPos(), h.getLevel().getBlockState(stove.getBlockPos()), stove);
        h.assertTrue(stove.getItem(3).getCount() == 1, "Blocked output does not consume fuel");
        stove.setItem(4, ItemStack.EMPTY);
        for (int i = 0; i < 79; i++) SpiritKitchenEntity.tick(h.getLevel(), stove.getBlockPos(), h.getLevel().getBlockState(stove.getBlockPos()), stove);
        h.assertTrue(stove.getItem(4).isEmpty(), "Spirit fire does not finish early");
        SpiritKitchenEntity.tick(h.getLevel(), stove.getBlockPos(), h.getLevel().getBlockState(stove.getBlockPos()), stove);
        h.assertTrue(stove.getItem(4).is(SpiritualFarming.FOODS.get(SpiritFoodKind.BREAD).get()), "Spirit fire finishes in 80 ticks");
        h.succeed();
    }
}
