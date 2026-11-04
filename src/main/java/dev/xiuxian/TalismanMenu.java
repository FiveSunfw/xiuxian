package dev.xiuxian;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

public final class TalismanMenu extends AbstractContainerMenu {
    final Container container;
    final ContainerData data;
    private boolean stoneMode;
    private final Player owner;
    public TalismanMenu(int id,Inventory inv,FriendlyByteBuf buffer) {
        this(id,inv,clientContainer(inv,buffer),new SimpleContainerData(3));
    }
    private static Container clientContainer(Inventory inv,FriendlyByteBuf buffer) {
        // Consume the opening position, but keep the menu's synced inventory independent of chunk BE packets.
        buffer.readBlockPos(); return new SimpleContainer(6) {
            @Override public boolean canPlaceItem(int slot,ItemStack stack) { return TalismanMenu.accepts(slot,stack); }
        };
    }
    private static boolean accepts(int slot,ItemStack stack) {
        return switch(slot) { case 0->stack.is(Talismans.BRUSH.get());case 1->stack.is(Talismans.PAPER.get());case 2->stack.is(Talismans.CINNABAR.get())||stack.is(Talismans.SPIRIT_CINNABAR.get());case 3->TalismanKind.recipe(stack)!=null;case 4->stack.is(CultivationResources.STONE.get());default->false; };
    }
    public TalismanMenu(int id,Inventory inv,Container container,ContainerData data) {
        super(Talismans.MENU.get(),id);this.container=container;this.data=data;this.owner=inv.player;
        int[][] positions={{12,41},{44,41},{76,41},{108,41},{12,78},{150,60}};
        for(int i=0;i<6;i++) { final int slot=i;addSlot(new Slot(container,i,positions[i][0],positions[i][1]) { @Override public boolean mayPlace(ItemStack s) { return accepts(slot,s); } }); }
        for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlot(new Slot(inv,9+row*9+col,12+col*18,164+row*18));
        for(int col=0;col<9;col++)addSlot(new Slot(inv,col,12+col*18,222));
        addDataSlots(data);
        if(inv.player instanceof ServerPlayer) updateData(inv.player);
    }
    boolean stoneMode() { return stoneMode; }
    private void updateData(Player player) { data.set(0,container instanceof TalismanTableEntity table?table.energy():0);data.set(1,(int)Math.floor(CultivationFoundation.mana(player)));data.set(2,Cultivation.initiated(player)?Cultivation.progress(player).level():0); }
    @Override public void broadcastChanges() { if(owner instanceof ServerPlayer) updateData(owner);super.broadcastChanges(); }
    @Override public boolean stillValid(Player player) { if(player instanceof ServerPlayer)updateData(player);return container.stillValid(player); }
    @Override public boolean clickMenuButton(Player player,int id) {
        if(!(player instanceof ServerPlayer server) || !(container instanceof TalismanTableEntity table) || !stillValid(player))return false;
        if(id==1) { stoneMode=false;return true; } if(id==2) { stoneMode=true;return true; }
        if(id!=0)return false;table.draw(server,stoneMode);updateData(player);broadcastChanges();return true;
    }
    @Override public ItemStack quickMoveStack(Player player,int index) {
        Slot slot=slots.get(index);if(!slot.hasItem())return ItemStack.EMPTY;
        var stack=slot.getItem();var original=stack.copy();
        if(index<6) { if(!moveItemStackTo(stack,6,42,true))return ItemStack.EMPTY; }
        else { boolean moved=false;for(int i=0;i<5;i++)if(accepts(i,stack)&&moveItemStackTo(stack,i,i+1,false)){moved=true;break;}if(!moved)return ItemStack.EMPTY; }
        if(stack.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();slot.onTake(player,stack);return original;
    }
}
