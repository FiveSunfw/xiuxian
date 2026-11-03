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
public class AlchemyBlock extends BaseEntityBlock {
    public AlchemyBlock(){super(BlockBehaviour.Properties.copy(Blocks.COPPER_BLOCK).strength(3));}
    @Override public RenderShape getRenderShape(BlockState s){return RenderShape.MODEL;}
    @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new AlchemyEntity(p,s);}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> t){return l.isClientSide?null:createTickerHelper(t,Alchemy.ENTITY.get(),AlchemyEntity::tick);}
    @Override public InteractionResult use(BlockState s,Level l,BlockPos p,Player player,InteractionHand hand,BlockHitResult hit){if(player instanceof ServerPlayer server && l.getBlockEntity(p) instanceof AlchemyEntity e)NetworkHooks.openScreen(server,e,p);return InteractionResult.sidedSuccess(l.isClientSide);}
    @Override public void onRemove(BlockState s,Level l,BlockPos p,BlockState next,boolean moving){if(!s.is(next.getBlock())){if(l.getBlockEntity(p) instanceof AlchemyEntity e)Containers.dropContents(l,p,e);super.onRemove(s,l,p,next,moving);}}
}
