package dev.xiuxian;

import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class FurnaceEntity extends BlockEntity implements Container, MenuProvider {
    public static final int BREW_TICKS = 200;
    private final NonNullList<ItemStack> items = NonNullList.withSize(4, ItemStack.EMPTY);
    private int progress;
    final ContainerData data = new ContainerData() {
        public int get(int index) { return progress; }
        public void set(int index, int value) { progress = value; }
        public int getCount() { return 1; }
    };
    public FurnaceEntity(BlockPos pos, BlockState state) { super(Xiuxian.FURNACE_ENTITY.get(), pos, state); }
    public static boolean isWater(ItemStack stack) { return stack.is(Items.POTION) && PotionUtils.getPotion(stack) == Potions.WATER; }
    public static boolean isReagent(ItemStack stack) { return isWater(stack); }
    public static void tick(Level level, BlockPos pos, BlockState state, FurnaceEntity furnace) {
        ItemStack output=brewResult(furnace.items.get(0),furnace.items.get(1),furnace.items.get(3));
        if(output.isEmpty()){if(furnace.progress!=0){furnace.progress=0;furnace.setChanged();}return;}
        if(!furnace.items.get(2).isEmpty())return;
        if(++furnace.progress>=BREW_TICKS){furnace.items.get(0).shrink(1);furnace.items.get(1).shrink(1);if(!furnace.items.get(3).isEmpty())furnace.items.get(3).shrink(1);furnace.items.set(2,output);furnace.progress=0;}
        furnace.setChanged();
    }
    public static ItemStack brewResult(ItemStack main,ItemStack water,ItemStack aux){
        if(!(main.getItem() instanceof QiHerbItem herb)||!isWater(water))return ItemStack.EMPTY;
        if(aux.isEmpty())return new ItemStack(Xiuxian.ELIXIRS.get(herb.kind()).get());
        String id=null;
        if(herb.kind()==HerbKind.WOOD&&aux.is(Items.WHEAT))id="healing_liquid";
        if(herb.kind()==HerbKind.FROST&&aux.is(Xiuxian.HERB_ITEMS.get(HerbKind.MARSH).get()))id="cleansing_liquid";
        if(herb.kind()==HerbKind.QI&&aux.is(Xiuxian.HERB_ITEMS.get(HerbKind.WOOD).get()))id="warming_liquid";
        return id==null?ItemStack.EMPTY:new ItemStack(Alchemy.item(id));
    }
    @Override protected void saveAdditional(CompoundTag tag) { super.saveAdditional(tag); ContainerHelper.saveAllItems(tag, items); tag.putInt("Progress", progress); }
    @Override public void load(CompoundTag tag) { super.load(tag); ContainerHelper.loadAllItems(tag, items); progress = Math.max(0, Math.min(BREW_TICKS - 1, tag.getInt("Progress"))); }
    @Override public Component getDisplayName() { return Component.translatable("block.xiuxian.herbal_furnace"); }
    @Override public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) { return new FurnaceMenu(id, inv, this, data); }
    @Override public int getContainerSize() { return 4; }
    @Override public boolean isEmpty() { return items.stream().allMatch(ItemStack::isEmpty); }
    @Override public ItemStack getItem(int slot) { return items.get(slot); }
    @Override public ItemStack removeItem(int slot, int count) { ItemStack result = ContainerHelper.removeItem(items, slot, count); if (!result.isEmpty()) setChanged(); return result; }
    @Override public ItemStack removeItemNoUpdate(int slot) { return ContainerHelper.takeItem(items, slot); }
    @Override public void setItem(int slot, ItemStack stack) { items.set(slot, stack); stack.setCount(Math.min(stack.getCount(), getMaxStackSize())); setChanged(); }
    @Override public boolean stillValid(Player player) { return level != null && level.getBlockEntity(worldPosition) == this && player.distanceToSqr(worldPosition.getX()+0.5, worldPosition.getY()+0.5, worldPosition.getZ()+0.5) <= 64; }
    @Override public boolean canPlaceItem(int slot, ItemStack stack) { return slot == 0 ? stack.getItem() instanceof QiHerbItem : slot == 1 ? isReagent(stack) : slot == 3 && (stack.getItem() instanceof QiHerbItem || stack.is(Items.WHEAT)); }
    @Override public void clearContent() { items.clear(); setChanged(); }
}


