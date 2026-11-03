package dev.xiuxian;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.*;
import net.minecraftforge.gametest.*;

@GameTestHolder(Xiuxian.ID)
@PrefixGameTestTemplate(false)
public class FurnaceGameTests {
    @GameTest(template = "empty", timeoutTicks = 230)
    public static void allHerbsProduceMatchingElixirs(GameTestHelper helper) {
        HerbKind[] herbs = HerbKind.values();
        for (int i = 0; i < herbs.length; i++) {
            BlockPos pos = new BlockPos(1 + i % 3, 1, 1 + i / 3);
            helper.setBlock(pos, Xiuxian.FURNACE.get());
            FurnaceEntity furnace = (FurnaceEntity) helper.getBlockEntity(pos);
            furnace.setItem(0, new ItemStack(Xiuxian.HERB_ITEMS.get(herbs[i]).get()));
            furnace.setItem(1, PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER));
        }
        helper.runAfterDelay(205, () -> {
            for (int i = 0; i < herbs.length; i++) {
                FurnaceEntity furnace = (FurnaceEntity) helper.getBlockEntity(new BlockPos(1 + i % 3, 1, 1 + i / 3));
                helper.assertTrue(furnace.getItem(2).is(Xiuxian.ELIXIRS.get(herbs[i]).get()), "Wrong elixir for " + herbs[i].id);
                helper.assertTrue(furnace.getItem(0).isEmpty() && furnace.getItem(1).isEmpty(), "Ingredients not consumed for " + herbs[i].id);
            }
            helper.succeed();
        });
    }
    @GameTest(template = "empty", timeoutTicks = 230)
    public static void brewAndConsumeIngredients(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, Xiuxian.FURNACE.get());
        FurnaceEntity furnace = (FurnaceEntity) helper.getBlockEntity(pos);
        furnace.setItem(0, new ItemStack(Xiuxian.HERB_ITEM.get(), 2));
        furnace.setItem(1, PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER));
        helper.runAfterDelay(205, () -> {
            helper.assertTrue(furnace.getItem(2).is(Xiuxian.LIQUID.get()), "Expected one elixir");
            helper.assertTrue(furnace.getItem(0).getCount() == 1, "Must consume exactly one herb");
            helper.assertTrue(furnace.getItem(1).isEmpty(), "Must consume water bottle");
            helper.succeed();
        });
    }
    @GameTest(template = "empty", timeoutTicks = 230)
    public static void fullOutputPreservesInputs(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, Xiuxian.FURNACE.get());
        FurnaceEntity furnace = (FurnaceEntity) helper.getBlockEntity(pos);
        furnace.setItem(0, new ItemStack(Xiuxian.HERB_ITEM.get()));
        furnace.setItem(1, PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER));
        furnace.setItem(2, new ItemStack(Xiuxian.LIQUID.get()));
        helper.runAfterDelay(205, () -> {
            helper.assertTrue(furnace.getItem(0).getCount() == 1 && FurnaceEntity.isWater(furnace.getItem(1)), "Full output must not consume ingredients");
            helper.assertTrue(furnace.data.get(0) == 0, "Full output must pause progress");
            helper.succeed();
        });
    }
    @GameTest(template = "empty", timeoutTicks = 230)
    public static void rejectsNonWaterPotion(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, Xiuxian.FURNACE.get());
        FurnaceEntity furnace = (FurnaceEntity) helper.getBlockEntity(pos);
        furnace.setItem(0, new ItemStack(Xiuxian.HERB_ITEM.get()));
        furnace.setItem(1, PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.POISON));
        helper.runAfterDelay(205, () -> {
            helper.assertTrue(furnace.getItem(2).isEmpty() && furnace.getItem(0).getCount() == 1, "Non-water potions must not brew");
            helper.succeed();
        });
    }
}




