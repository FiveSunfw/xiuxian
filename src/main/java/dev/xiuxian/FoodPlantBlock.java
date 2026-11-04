package dev.xiuxian;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

public final class FoodPlantBlock extends BushBlock implements BonemealableBlock {
    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;
    private final FoodPlantKind kind;

    public FoodPlantBlock(FoodPlantKind kind) {
        super(BlockBehaviour.Properties.copy(Blocks.POPPY).randomTicks());
        this.kind = kind;
        registerDefaultState(stateDefinition.any().setValue(AGE, 0));
    }

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(AGE); }
    @Override protected boolean mayPlaceOn(BlockState below, BlockGetter level, BlockPos pos) {
        if (below.is(SpiritualFarming.SOIL.get())) return true;
        return switch (kind) {
            case JADE_RICE, GREEN_WHEAT, STAR_FLOWER -> below.is(Blocks.FARMLAND) || below.is(BlockTags.DIRT);
            case DRAGON_PEPPER -> below.is(BlockTags.SAND) || below.is(Blocks.FARMLAND);
            case PURPLE_BAMBOO -> below.is(Blocks.PODZOL) || below.is(Blocks.DIRT) || below.is(Blocks.GRASS_BLOCK);
            case ARMOR_POTATO -> below.is(Blocks.STONE) || below.is(Blocks.DEEPSLATE) || below.is(Blocks.FARMLAND);
            case DEW_MELON -> below.is(Blocks.MUD) || below.is(Blocks.GRASS_BLOCK) || below.is(Blocks.FARMLAND);
            case EARTH_GINGER -> below.is(Blocks.STONE) || below.is(Blocks.DEEPSLATE) || below.is(Blocks.TUFF);
        };
    }
    @Override public boolean isRandomlyTicking(BlockState state) { return state.getValue(AGE) < 3; }

    boolean growthAllowed(ServerLevel level, BlockPos pos) {
        return growthAllowed(kind, level.getRawBrightness(pos, 0), level.isNight(), level.canSeeSky(pos));
    }
    static boolean growthAllowed(FoodPlantKind kind, int light, boolean night, boolean skyVisible) {
        return switch (kind) {
            case JADE_RICE, GREEN_WHEAT, DRAGON_PEPPER -> light >= 9;
            case STAR_FLOWER -> night && skyVisible;
            case EARTH_GINGER -> light <= 7;
            default -> true;
        };
    }

    double growthChance(ServerLevel level, BlockPos pos) {
        double chance = level.getBlockState(pos.below()).is(SpiritualFarming.SOIL.get()) ? .50 : .25;
        chance *= SpiritVeins.get(level).multiplierAt(pos);
        if (kind == FoodPlantKind.DEW_MELON) {
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                if (level.getFluidState(pos.relative(direction)).is(FluidTags.WATER)) { chance *= 1.5; break; }
            }
        }
        return Math.min(1, chance);
    }

    @Override public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(AGE) < 3 && growthAllowed(level, pos) && random.nextFloat() < growthChance(level, pos))
            level.setBlock(pos, state.setValue(AGE, state.getValue(AGE) + 1), 2);
    }

    @Override public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                           InteractionHand hand, BlockHitResult hit) {
        if (state.getValue(AGE) != 3) return InteractionResult.PASS;
        if (!level.isClientSide) {
            int amount = 1;
            if (level.getBlockState(pos.below()).is(SpiritualFarming.SOIL.get()) && level.random.nextFloat() < .30F)
                amount += 1 + level.random.nextInt(2);
            popResource(level, pos, new ItemStack(SpiritualFarming.PLANTS.get(kind).get(), amount));
            level.setBlock(pos, state.setValue(AGE, 1), 3);
            level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1, .9F + level.random.nextFloat() * .2F);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
    @Override public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, boolean client) {
        return state.getValue(AGE) < 3 && level.getBlockState(pos.below()).is(SpiritualFarming.SOIL.get());
    }
    @Override public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) { return true; }
    @Override public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        if (isValidBonemealTarget(level, pos, state, false)) level.setBlock(pos, state.setValue(AGE, state.getValue(AGE) + 1), 2);
    }
}
