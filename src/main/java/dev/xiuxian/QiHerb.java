package dev.xiuxian;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class QiHerb extends BushBlock {
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 2);
    private final HerbKind kind;
    public QiHerb(HerbKind kind) {
        super(BlockBehaviour.Properties.copy(Blocks.POPPY).randomTicks().lightLevel(state -> kind == HerbKind.MOSS ? 3 : 0));
        this.kind = kind;
        registerDefaultState(stateDefinition.any().setValue(AGE, 0));
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(AGE); }
    @Override protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) { if (state.is(SpiritualFarming.SOIL.get()) || state.is(net.minecraft.tags.BlockTags.DIRT)) return true;
        return switch (kind) {
            case FROST -> state.is(Blocks.SNOW_BLOCK);
            case STONE, MOSS -> state.is(net.minecraft.tags.BlockTags.BASE_STONE_OVERWORLD) || state.is(Blocks.CALCITE);
            case SUN -> state.is(net.minecraft.tags.BlockTags.SAND) || state.is(net.minecraft.tags.BlockTags.TERRACOTTA);
            case TIDE -> state.is(net.minecraft.tags.BlockTags.SAND) || state.is(Blocks.GRAVEL) || state.is(Blocks.STONE);
            case MARSH -> state.is(Blocks.MUD) || state.is(Blocks.MOSS_BLOCK);
            default -> false;
        }; }
    @Override public boolean isRandomlyTicking(BlockState state) { return state.getValue(AGE) < 2; }
    @Override public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int chances = level.getBlockState(pos.below()).is(SpiritualFarming.SOIL.get()) ? 2 : 1;
        if ((kind == HerbKind.MOSS || level.getRawBrightness(pos.above(), 0) >= 9) && random.nextInt(5) < chances)
            level.setBlock(pos, state.setValue(AGE, state.getValue(AGE) + 1), 2);
    }
    @Override public void playerDestroy(Level level, net.minecraft.world.entity.player.Player player, BlockPos pos,
            BlockState state, net.minecraft.world.level.block.entity.BlockEntity entity, net.minecraft.world.item.ItemStack tool) {
        super.playerDestroy(level, player, pos, state, entity, tool);
        if (!level.isClientSide && !player.getAbilities().instabuild && level.getBlockState(pos.below()).is(SpiritualFarming.SOIL.get())
            && level.random.nextFloat() < .30F)
            popResource(level, pos, new net.minecraft.world.item.ItemStack(asItem(), 1 + level.random.nextInt(2)));
    }
}


