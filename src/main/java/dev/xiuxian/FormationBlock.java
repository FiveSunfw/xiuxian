package dev.xiuxian;

import net.minecraft.core.*;
import net.minecraft.server.level.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;
import net.minecraftforge.network.NetworkHooks;

public final class FormationBlock extends BaseEntityBlock {
    public static final BooleanProperty ACTIVE=BlockStateProperties.LIT;
    public FormationBlock() { super(BlockBehaviour.Properties.copy(Blocks.STONE).strength(3).lightLevel(s->s.getValue(ACTIVE)?7:0));registerDefaultState(stateDefinition.any().setValue(ACTIVE,false)); }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder) { builder.add(ACTIVE); }
    @Override public VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context) { return Block.box(0,0,0,16,8,16); }
    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state) { return new FormationEntity(pos,state); }
    @Override public void setPlacedBy(Level level,BlockPos pos,BlockState state,LivingEntity entity,ItemStack stack) {
        if(level.getBlockEntity(pos) instanceof FormationEntity plate && entity instanceof Player player)plate.assignOwner(player.getUUID());
    }
    @Override public void neighborChanged(BlockState state,Level level,BlockPos pos,Block block,BlockPos from,boolean moving) {
        if(level instanceof ServerLevel && level.getBlockEntity(pos) instanceof FormationEntity plate)plate.invalidate();
    }
    @Override public InteractionResult use(BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit) {
        if(player instanceof ServerPlayer server && level.getBlockEntity(pos) instanceof FormationEntity plate) {
            if(plate.canManage(player))NetworkHooks.openScreen(server,plate,pos);
            else player.displayClientMessage(Component.translatable("formation.xiuxian.denied"),true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
    @Override public void onRemove(BlockState state,Level level,BlockPos pos,BlockState next,boolean moving) {
        if(!state.is(next.getBlock())) {
            if(level instanceof ServerLevel server)FormationIndex.get(server).remove(pos);
            if(level.getBlockEntity(pos) instanceof FormationEntity plate)Containers.dropContents(level,pos,plate);
            super.onRemove(state,level,pos,next,moving);
        }
    }
}
