package dev.xiuxian;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.phys.shapes.*;

public final class FormationFlagBlock extends Block {
    public FormationFlagBlock() { super(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS).strength(1.5f).noOcclusion()); }
    @Override public VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context) { return Block.box(5,0,5,11,16,11); }
    @Override public void onPlace(BlockState state,Level level,BlockPos pos,BlockState old,boolean moving) { if(!state.is(old.getBlock())&&level instanceof ServerLevel server)FormationIndex.get(server).flagChanged(pos); }
    @Override public void onRemove(BlockState state,Level level,BlockPos pos,BlockState next,boolean moving) { if(!state.is(next.getBlock())&&level instanceof ServerLevel server)FormationIndex.get(server).flagChanged(pos);super.onRemove(state,level,pos,next,moving); }
}
