package dev.xiuxian;

import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** All validation and resource changes happen together on the server thread. */
public final class TalismanTableEntity extends BlockEntity implements Container, MenuProvider {
    private final NonNullList<ItemStack> items=NonNullList.withSize(6,ItemStack.EMPTY);
    private int energy;
    private long nextDrawTick;
    public TalismanTableEntity(BlockPos pos,BlockState state) { super(Talismans.ENTITY.get(),pos,state); }
    public int energy() { return energy; }
    public TalismanKind recipe() { return TalismanKind.recipe(items.get(3)); }
    public enum Result { SUCCESS, FAILURE, MORTAL, MATERIALS, MANA, ENERGY, OUTPUT, COOLDOWN }
    public Result draw(ServerPlayer player, boolean stoneMode) { return draw(player,stoneMode,player.getRandom().nextDouble()); }
    // Package-visible random input allows deterministic success/failure acceptance tests.
    Result draw(ServerPlayer player,boolean stoneMode,double roll) {
        if(level==null || level.isClientSide || !stillValid(player)) return Result.MATERIALS;
        if(!Cultivation.initiated(player)) return report(player,Result.MORTAL);
        if(level.getGameTime()<nextDrawTick) return report(player,Result.COOLDOWN);
        var recipe=recipe();
        if(recipe==null || !items.get(0).is(Talismans.BRUSH.get()) || items.get(0).getDamageValue()>=items.get(0).getMaxDamage()
            || !items.get(1).is(Talismans.PAPER.get()) || !(items.get(2).is(Talismans.CINNABAR.get()) || items.get(2).is(Talismans.SPIRIT_CINNABAR.get()))) return report(player,Result.MATERIALS);
        ItemStack output=recipe.output(items.get(2).is(Talismans.SPIRIT_CINNABAR.get()));
        var existing=items.get(5);
        if(!existing.isEmpty() && (!ItemStack.isSameItemSameTags(existing,output) || existing.getCount()>=existing.getMaxStackSize())) return report(player,Result.OUTPUT);
        if(stoneMode) {
            if(energy<recipe.energy && !items.get(4).is(CultivationResources.STONE.get())) return report(player,Result.ENERGY);
        } else if(CultivationFoundation.mana(player)<recipe.energy) return report(player,Result.MANA);
        if(stoneMode) { if(energy<recipe.energy) { items.get(4).shrink(1); energy+=100; } energy-=recipe.energy; }
        else if(!CultivationFoundation.spendMana(player,recipe.energy)) return report(player,Result.MANA);
        items.get(1).shrink(1); items.get(2).shrink(1); items.get(3).shrink(1);
        // Direct wear also applies in creative: drawing is a production operation.
        var brush=items.get(0); brush.setDamageValue(brush.getDamageValue()+1);
        if(brush.getDamageValue()>=brush.getMaxDamage()) items.set(0,ItemStack.EMPTY);
        if(recipe.herb!=null) { ItemStack bottle=new ItemStack(Items.GLASS_BOTTLE); if(!player.getInventory().add(bottle)) player.drop(bottle,false); }
        nextDrawTick=level.getGameTime()+40;
        boolean success=roll*100<recipe.chance(Cultivation.progress(player).level());
        if(success) { if(existing.isEmpty()) items.set(5,output); else existing.grow(1); }
        level.playSound(null,worldPosition,success?SoundEvents.ENCHANTMENT_TABLE_USE:SoundEvents.FIRE_EXTINGUISH,SoundSource.BLOCKS,1,1);
        setChanged(); return report(player,success?Result.SUCCESS:Result.FAILURE);
    }
    private Result report(ServerPlayer player,Result result) { player.displayClientMessage(Component.translatable("talisman.xiuxian.draw."+result.name().toLowerCase(java.util.Locale.ROOT)),true); return result; }
    @Override protected void saveAdditional(CompoundTag tag) { super.saveAdditional(tag); ContainerHelper.saveAllItems(tag,items);tag.putInt("Energy",energy);tag.putLong("NextDrawTick",nextDrawTick); }
    @Override public void load(CompoundTag tag) { super.load(tag);ContainerHelper.loadAllItems(tag,items);energy=Math.max(0,Math.min(100,tag.getInt("Energy")));nextDrawTick=tag.getLong("NextDrawTick"); }
    @Override public Component getDisplayName() { return Component.translatable("block.xiuxian.talisman_table"); }
    @Override public AbstractContainerMenu createMenu(int id,Inventory inv,Player player) { return new TalismanMenu(id,inv,this,new SimpleContainerData(3)); }
    @Override public int getContainerSize() { return 6; }
    @Override public boolean isEmpty() { return items.stream().allMatch(ItemStack::isEmpty); }
    @Override public ItemStack getItem(int slot) { return items.get(slot); }
    @Override public ItemStack removeItem(int slot,int count) { var result=ContainerHelper.removeItem(items,slot,count);if(!result.isEmpty())setChanged();return result; }
    @Override public ItemStack removeItemNoUpdate(int slot) { return ContainerHelper.takeItem(items,slot); }
    @Override public void setItem(int slot,ItemStack stack) { items.set(slot,stack);stack.setCount(Math.min(getMaxStackSize(),stack.getCount()));setChanged(); }
    @Override public void clearContent() { items.clear();setChanged(); }
    @Override public boolean stillValid(Player player) { return level!=null && level.getBlockEntity(worldPosition)==this && player.distanceToSqr(worldPosition.getCenter())<=64; }
    @Override public boolean canPlaceItem(int slot,ItemStack stack) {
        return switch(slot) { case 0->stack.is(Talismans.BRUSH.get());case 1->stack.is(Talismans.PAPER.get());case 2->stack.is(Talismans.CINNABAR.get())||stack.is(Talismans.SPIRIT_CINNABAR.get());case 3->TalismanKind.recipe(stack)!=null;case 4->stack.is(CultivationResources.STONE.get());default->false; };
    }
}
