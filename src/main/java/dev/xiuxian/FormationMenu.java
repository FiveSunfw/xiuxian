package dev.xiuxian;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;

public final class FormationMenu extends AbstractContainerMenu {
    final Container container;
    final ContainerData data;
    private final Player viewer;
    public FormationMenu(int id,Inventory inv,FriendlyByteBuf buffer) { this(id,inv,clientContainer(buffer),new SimpleContainerData(5)); }
    private static Container clientContainer(FriendlyByteBuf buffer) { buffer.readBlockPos();return new SimpleContainer(2); }
    public FormationMenu(int id,Inventory inv,Container container,ContainerData data) {
        super(Formations.MENU.get(),id);this.container=container;this.data=data;viewer=inv.player;
        for(int i=0;i<2;i++){final int slot=i;addSlot(new Slot(container,i,28+i*104,36){@Override public boolean mayPlace(ItemStack stack){return FormationEntity.accepts(slot,stack);}@Override public int getMaxStackSize(){return slot==0?1:64;}});}
        for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlot(new Slot(inv,col+row*9+9,12+col*18,166+row*18));
        for(int col=0;col<9;col++)addSlot(new Slot(inv,col,12+col*18,224));addDataSlots(data);updateData();
    }
    private void updateData() {
        if(viewer instanceof ServerPlayer&&container instanceof FormationEntity plate){data.set(0,plate.status().ordinal()|(plate.enabled()?256:0));data.set(1,plate.scale());data.set(2,(int)plate.energy());data.set(3,(int)Math.floor((plate.energy()%1)*100));data.set(4,(int)Math.round(plate.upkeep()*1000));}
    }
    @Override public void broadcastChanges(){updateData();super.broadcastChanges();}
    @Override public boolean stillValid(Player player){return container.stillValid(player);}
    @Override public boolean clickMenuButton(Player player,int id){if(id!=0||!(player instanceof ServerPlayer)||!(container instanceof FormationEntity plate)||!stillValid(player))return false;plate.setEnabled(!plate.enabled());broadcastChanges();return true;}
    @Override public ItemStack quickMoveStack(Player player,int index) {
        if(!stillValid(player))return ItemStack.EMPTY;
        var slot=slots.get(index);if(!slot.hasItem())return ItemStack.EMPTY;var stack=slot.getItem();var original=stack.copy();
        if(index<2){if(!moveItemStackTo(stack,2,38,true))return ItemStack.EMPTY;}
        else {int target=FormationEntity.accepts(0,stack)?0:FormationEntity.accepts(1,stack)?1:-1;if(target<0||!moveItemStackTo(stack,target,target+1,false))return ItemStack.EMPTY;}
        if(stack.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();slot.onTake(player,stack);return original;
    }
}
