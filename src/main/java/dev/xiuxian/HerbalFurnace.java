package dev.xiuxian;

import net.minecraft.core.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

public class HerbalFurnace extends BaseEntityBlock {
    public HerbalFurnace() {
        // Vanilla furnace light emission requires LIT, which this block does not define.
        super(BlockBehaviour.Properties.copy(Blocks.FURNACE).lightLevel(state -> 0));
    }
    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new FurnaceEntity(pos, state); }
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, Xiuxian.FURNACE_ENTITY.get(), FurnaceEntity::tick);
    }
    @Override public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (player instanceof ServerPlayer server && level.getBlockEntity(pos) instanceof FurnaceEntity furnace)
            NetworkHooks.openScreen(server, furnace, pos);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
    @Override public void onRemove(BlockState state, Level level, BlockPos pos, BlockState next, boolean moving) {
        if (!state.is(next.getBlock())) {
            if (level.getBlockEntity(pos) instanceof FurnaceEntity furnace) Containers.dropContents(level, pos, furnace);
            super.onRemove(state, level, pos, next, moving);
        }
    }
}
