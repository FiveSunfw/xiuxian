package dev.xiuxian;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
public class AlchemyEntity extends BlockEntity implements Container,MenuProvider {
    private final NonNullList<ItemStack> items=NonNullList.withSize(6,ItemStack.EMPTY);
    int elapsed; double chance; ItemStack pending=ItemStack.EMPTY;
    final ContainerData data=new ContainerData(){public int get(int i){return i==0?elapsed:(int)Math.round(chance*100);}public void set(int i,int v){if(i==0)elapsed=v;else chance=v/100.0;}public int getCount(){return 2;}};
    public AlchemyEntity(BlockPos p,BlockState s){super(Alchemy.ENTITY.get(),p,s);}
    public Alchemy.Formula formula(){return Alchemy.formulas().stream().filter(f->items.get(0).is(Alchemy.item(f.id()+"_formula"))).findFirst().orElse(null);}
    public static double success(int realm,int furnace,int requiredRealm,int requiredFurnace){return Math.min(Cultivation.ALCHEMY_CAP.get(),Cultivation.ALCHEMY_BASE.get()+Math.max(0,realm-requiredRealm)*Cultivation.ALCHEMY_REALM.get()+Math.max(0,furnace-requiredFurnace)*Cultivation.ALCHEMY_FURNACE.get());}
    public boolean start(Player p){
        var f=formula();if(level==null||level.isClientSide||f==null||!Cultivation.initiated(p)||!pending.isEmpty()||!items.get(5).isEmpty())return false;
        boolean main=f.id().equals("qi_gathering")?items.get(1).getItem() instanceof QiHerbItem:items.get(1).is(f.main());
        if(!main||items.get(1).getCount()<f.mainCount()||!items.get(2).is(f.auxiliary())||items.get(2).getCount()<f.auxiliaryCount()||!items.get(3).is(CultivationResources.STONE.get())||items.get(3).getCount()<f.stones()||!(items.get(4).is(Items.COAL)||items.get(4).is(Items.CHARCOAL)))return false;
        chance=success(1,1,1,1);pending=new ItemStack(f.result());elapsed=0;
        items.get(1).shrink(f.mainCount());items.get(2).shrink(f.auxiliaryCount());items.get(3).shrink(f.stones());items.get(4).shrink(1);setChanged();return true;
    }
    public static void tick(Level l,BlockPos p,BlockState s,AlchemyEntity e){if(e.pending.isEmpty())return;if(++e.elapsed>=600){e.items.set(5,l.random.nextDouble()<e.chance?e.pending:new ItemStack(Alchemy.item("pill_residue")));e.pending=ItemStack.EMPTY;e.elapsed=0;}e.setChanged();}
    @Override protected void saveAdditional(CompoundTag t){super.saveAdditional(t);ContainerHelper.saveAllItems(t,items);t.putInt("Elapsed",elapsed);t.putDouble("Chance",chance);t.put("Pending",pending.save(new CompoundTag()));}
    @Override public void load(CompoundTag t){super.load(t);ContainerHelper.loadAllItems(t,items);elapsed=Math.max(0,Math.min(599,t.getInt("Elapsed")));chance=t.getDouble("Chance");pending=ItemStack.of(t.getCompound("Pending"));}
    @Override public Component getDisplayName(){return Component.translatable("block.xiuxian.basic_alchemy_furnace");}
    @Override public AbstractContainerMenu createMenu(int id,Inventory inv,Player p){return new AlchemyMenu(id,inv,this,data);}
    @Override public int getContainerSize(){return 6;}
    @Override public boolean isEmpty(){return items.stream().allMatch(ItemStack::isEmpty);}
    @Override public ItemStack getItem(int i){return items.get(i);}
    @Override public ItemStack removeItem(int i,int n){var s=ContainerHelper.removeItem(items,i,n);setChanged();return s;}
    @Override public ItemStack removeItemNoUpdate(int i){return ContainerHelper.takeItem(items,i);}
    @Override public void setItem(int i,ItemStack s){items.set(i,s);s.setCount(Math.min(s.getCount(),64));setChanged();}
    @Override public boolean stillValid(Player p){return level!=null&&level.getBlockEntity(worldPosition)==this&&p.distanceToSqr(worldPosition.getX()+.5,worldPosition.getY()+.5,worldPosition.getZ()+.5)<=64;}
    @Override public void clearContent(){items.clear();setChanged();}
}
