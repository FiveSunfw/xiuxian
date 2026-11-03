package dev.xiuxian;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
public class AlchemyMenu extends AbstractContainerMenu {
    final Container container;final ContainerData data;
    public AlchemyMenu(int id,Inventory inv,FriendlyByteBuf buf){this(id,inv,(AlchemyEntity)inv.player.level().getBlockEntity(buf.readBlockPos()),new SimpleContainerData(2));}
    public AlchemyMenu(int id,Inventory inv,Container c,ContainerData d){super(Alchemy.MENU.get(),id);container=c;data=d;int[][] coords={{8,26},{44,52},{80,52},{116,52},{8,78},{152,78}};for(int i=0;i<6;i++){final int slot=i;addSlot(new Slot(c,i,coords[i][0],coords[i][1]){@Override public boolean mayPlace(ItemStack s){return switch(slot){case 0->Alchemy.formulas().stream().anyMatch(f->s.is(Alchemy.item(f.id()+"_formula")));case 1->s.getItem() instanceof QiHerbItem;case 2->s.getItem() instanceof QiHerbItem||s.is(Items.WHEAT);case 3->s.is(CultivationResources.STONE.get());case 4->s.is(Items.COAL)||s.is(Items.CHARCOAL);default->false;};}});}for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlot(new Slot(inv,col+row*9+9,8+col*18,124+row*18));for(int col=0;col<9;col++)addSlot(new Slot(inv,col,8+col*18,182));addDataSlots(d);}
    @Override public boolean clickMenuButton(Player p,int id){return id==0&&container instanceof AlchemyEntity e&&e.start(p);}
    @Override public boolean stillValid(Player p){return container.stillValid(p);}
    @Override public ItemStack quickMoveStack(Player p,int index){var slot=slots.get(index);if(!slot.hasItem())return ItemStack.EMPTY;var s=slot.getItem();var copy=s.copy();if(index<6){if(!moveItemStackTo(s,6,42,true))return ItemStack.EMPTY;}else{boolean moved=false;for(int i=0;i<5;i++)if(slots.get(i).mayPlace(s)&&moveItemStackTo(s,i,i+1,false)){moved=true;break;}if(!moved)return ItemStack.EMPTY;}if(s.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();slot.onTake(p,s);return copy;}
}
