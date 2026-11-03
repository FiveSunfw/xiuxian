package dev.xiuxian;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.common.util.*;
import net.minecraftforge.gametest.*;
import java.util.*;

@GameTestHolder(Xiuxian.ID)
@PrefixGameTestTemplate(false)
public class GrowthResourceGameTests {
    private static FakePlayer player(GameTestHelper h, String name, boolean initiated) {
        FakePlayer p = FakePlayerFactory.get(h.getLevel(), new GameProfile(UUID.nameUUIDFromBytes(name.getBytes(java.nio.charset.StandardCharsets.UTF_8)), name));
        CompoundTag tag = new CompoundTag(); tag.putBoolean("xiuxian_initiated", initiated);
        p.getPersistentData().put(Player.PERSISTED_NBT_TAG, tag);
        p.getAbilities().instabuild = false;
        return p;
    }
    @GameTest(template = "empty")
    public static void techniqueAndFoodMultiplyWithoutStacking(GameTestHelper h) {
        FakePlayer p = player(h, "BoostTest", true);
        h.assertTrue(Cultivation.learnTechnique(p, 1), "First manual should learn");
        h.assertTrue(!Cultivation.learnTechnique(p, 1), "Same manual must not stack");
        double first = Cultivation.PROGRESS_PER_SECOND.get() * Cultivation.MANUAL_1_MULTIPLIER.get();
        h.assertTrue(Cultivation.cultivationRate(p) == first, "Manual multiplier missing");
        ItemStack meal = new ItemStack(CultivationResources.MEAL.get());
        ItemStack bowl = meal.finishUsingItem(h.getLevel(), p);
        h.assertTrue(bowl.is(Items.BOWL), "Meal must return bowl");
        h.assertTrue(Cultivation.cultivationRate(p) == first * Cultivation.FOOD_MULTIPLIER.get(), "Food must multiply manual");
        new ItemStack(CultivationResources.MEAL.get()).finishUsingItem(h.getLevel(), p);
        h.assertTrue(p.getEffect(CultivationResources.NOURISHMENT.get()).getDuration() == Cultivation.FOOD_SECONDS.get() * 20, "Food must refresh, not accumulate duration");
        p.removeEffect(CultivationResources.NOURISHMENT.get());
        h.assertTrue(Cultivation.cultivationRate(p) == first, "Removing food must restore normal rate");
        h.assertTrue(Cultivation.learnTechnique(p, 3) && !Cultivation.learnTechnique(p, 2), "Keep only highest technique");
        h.succeed();
    }
    @GameTest(template = "empty")
    public static void stoneAndPillReallyGrantProgress(GameTestHelper h) {
        FakePlayer p = player(h, "StoneTest", true);
        BlockPos pos = h.absolutePos(new BlockPos(2, 3, 2)); p.setPos(pos.getX()+0.5, pos.getY(), pos.getZ()+0.5); p.setXRot(-90);
        p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(CultivationResources.STONE.get(), 2));
        CultivationResources.STONE.get().use(h.getLevel(), p, InteractionHand.MAIN_HAND);
        h.assertTrue(Cultivation.progress(p).points() == Cultivation.STONE_POINTS.get(), "Stone must grant actual saved progress");
        h.assertTrue(p.getMainHandItem().getCount() == 1, "Stone must consume exactly one");
        ItemStack pill = new ItemStack(CultivationResources.PILL.get(), 2);
        pill.finishUsingItem(h.getLevel(), p);
        h.assertTrue(pill.getCount() == 1, "Pill consumption");
        h.assertTrue(Cultivation.progress(p).points() == Cultivation.STONE_POINTS.get()+Cultivation.PILL_POINTS.get(), "Pill must add progress");
        FakePlayer mortal = player(h, "MortalBoostTest", false);
        h.assertTrue(!Cultivation.addProgress(mortal, 100), "Cannot bypass initiation with cultivation resources");
        CompoundTag tag = p.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG); tag.putInt("xiuxian_level", 9); tag.putInt("xiuxian_progress", Cultivation.progressBase() * 9);
        p.getPersistentData().put(Player.PERSISTED_NBT_TAG, tag);
        ItemStack full = new ItemStack(CultivationResources.PILL.get()); full.finishUsingItem(h.getLevel(), p);
        h.assertTrue(full.getCount() == 1, "Do not consume at full realm");
        h.succeed();
    }
    @GameTest(template = "empty", timeoutTicks = 230)
    public static void herbalFurnaceRejectsOldPillIngredients(GameTestHelper h) {
        BlockPos pos = new BlockPos(1, 1, 1); h.setBlock(pos, Xiuxian.FURNACE.get());
        FurnaceEntity furnace = (FurnaceEntity) h.getBlockEntity(pos);
        furnace.setItem(0, new ItemStack(Xiuxian.HERB_ITEM.get())); furnace.setItem(1, new ItemStack(CultivationResources.STONE.get()));
        h.assertTrue(!FurnaceEntity.isReagent(new ItemStack(CultivationResources.STONE_2.get())), "Do not waste tier-two stones in basic recipe");
        h.runAfterDelay(205, () -> {
            h.assertTrue(furnace.getItem(2).isEmpty(), "Pills must now require an alchemy furnace");
            h.assertTrue(!furnace.getItem(0).isEmpty() && !furnace.getItem(1).isEmpty(), "Legacy ingredients must remain recoverable"); h.succeed();
        });
    }
    @GameTest(template = "empty")
    public static void scatteredOreIsSmallAndDropsWhiteStone(GameTestHelper h) {
        for (int x=0;x<5;x++) for(int y=0;y<5;y++) for(int z=0;z<5;z++) h.setBlock(new BlockPos(x,y,z), Blocks.STONE);
        var context = new FeaturePlaceContext<>(Optional.empty(), h.getLevel(), h.getLevel().getChunkSource().getGenerator(), RandomSource.create(42), h.absolutePos(new BlockPos(2,2,2)), NoneFeatureConfiguration.INSTANCE);
        h.assertTrue(new SpiritVeinFeature(1,2).place(context), "Scattered ore must place");
        int count=0;
        for(int x=0;x<5;x++) for(int y=0;y<5;y++) for(int z=0;z<5;z++) if(h.getBlockState(new BlockPos(x,y,z)).is(CultivationResources.ORE.get()))count++;
        h.assertTrue(count>=1 && count<=2, "Scattered ore must contain one or two blocks");
        BlockPos pos=h.absolutePos(new BlockPos(1,1,1));
        var drops=net.minecraft.world.level.block.Block.getDrops(CultivationResources.ORE.get().defaultBlockState(),h.getLevel(),pos,null,null,new ItemStack(Items.STONE_PICKAXE));
        h.assertTrue(drops.size()==1 && drops.get(0).is(CultivationResources.STONE.get()), "Normal mining must directly drop a spirit stone");
        h.succeed();
    }
}
