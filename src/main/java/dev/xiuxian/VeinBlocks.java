package dev.xiuxian;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraftforge.registries.RegistryObject;

public final class VeinBlocks {
    public static final RegistryObject<Block> CORE=Xiuxian.BLOCKS.register("spirit_vein_core",CoreBlock::new);
    public static final RegistryObject<Block> DEPLETED=Xiuxian.BLOCKS.register("depleted_spirit_ore",()->new Block(BlockBehaviour.Properties.copy(Blocks.IRON_ORE)));
    public static final RegistryObject<Block> DEEP_DEPLETED=Xiuxian.BLOCKS.register("depleted_deepslate_spirit_ore",()->new Block(BlockBehaviour.Properties.copy(Blocks.DEEPSLATE_IRON_ORE)));
    public static final RegistryObject<Item> CORE_ITEM=Xiuxian.ITEMS.register("spirit_vein_core",()->new BlockItem(CORE.get(),new Item.Properties()));
    public static final RegistryObject<Item> DEPLETED_ITEM=Xiuxian.ITEMS.register("depleted_spirit_ore",()->new BlockItem(DEPLETED.get(),new Item.Properties()));
    public static final RegistryObject<BlockEntityType<CoreEntity>> ENTITY=Xiuxian.ENTITIES.register("spirit_vein_core",()->BlockEntityType.Builder.of(CoreEntity::new,CORE.get()).build(null));
    static void register() {}
    static final class CoreBlock extends BaseEntityBlock {
        CoreBlock() { super(BlockBehaviour.Properties.copy(Blocks.IRON_ORE).strength(5).lightLevel(s->7)); }
        @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
        @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state) { return new CoreEntity(pos,state); }
        @Override public void onRemove(BlockState state,Level level,BlockPos pos,BlockState next,boolean moving) {
            if(!state.is(next.getBlock()) && level instanceof ServerLevel server) SpiritVeins.get(server).remove(pos);
            super.onRemove(state,level,pos,next,moving);
        }
    }
    public static final class CoreEntity extends BlockEntity {
        CompoundTag generated=new CompoundTag();
        CoreEntity(BlockPos pos,BlockState state) { super(ENTITY.get(),pos,state); }
        @Override public void load(CompoundTag tag) { super.load(tag); generated=tag.getCompound("vein"); }
        @Override protected void saveAdditional(CompoundTag tag) { super.saveAdditional(tag); tag.put("vein",generated); }
        @Override public void onLoad() {
            super.onLoad();
            if(level instanceof ServerLevel server && generated.contains("cores")) {
                server.getServer().execute(() -> {
                    if (!server.getBlockState(worldPosition).is(CORE.get())) return;
                    var saved = SpiritVeins.get(server);
                    for (var value : generated.getList("cores", 10)) {
                        CompoundTag tag = (CompoundTag) value;
                        var core = new SpiritVeins.Core(BlockPos.of(tag.getLong("pos")), tag.getInt("tier"), tag.getInt("surface"));
                        for (var ore : tag.getList("ores", 10)) {
                            CompoundTag entry = (CompoundTag) ore;
                            core.ores.put(BlockPos.of(entry.getLong("pos")), 0);
                        }
                        saved.add(core);
                    }
                });
            }
        }
    }
    public static final class Ore extends Block {
        public Ore(boolean deep) { super(BlockBehaviour.Properties.copy(deep?Blocks.DEEPSLATE_IRON_ORE:Blocks.IRON_ORE)); }
        @Override public void playerDestroy(Level level,Player player,BlockPos pos,BlockState state,BlockEntity entity,ItemStack tool) {
            super.playerDestroy(level,player,pos,state,entity,tool);
            if(level instanceof ServerLevel server) {
                var data=SpiritVeins.get(server);
                if(EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH,tool)>0) data.forget(pos);
                else if(data.deplete(pos)) server.setBlockAndUpdate(pos,(state.is(CultivationResources.DEEP_ORE.get())?DEEP_DEPLETED:DEPLETED).get().defaultBlockState());
            }
        }
    }
}
