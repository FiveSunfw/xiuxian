package dev.xiuxian;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Five-slot stove. Progress is measured in 800 units so changing heat preserves elapsed work. */
public final class SpiritKitchenEntity extends BlockEntity implements WorldlyContainer, MenuProvider {
    private static final int COOK_UNITS = 800;
    private static final int[] TOP = {0};
    private static final int[] SIDE = {1, 2, 3};
    private static final int[] BOTTOM = {4, 2};
    private final NonNullList<ItemStack> items = NonNullList.withSize(5, ItemStack.EMPTY);
    private int progress, burnRemaining, burnTotal, heatMode;
    private String recipeId = "";
    private boolean containerRemainder;
    final ContainerData data = new ContainerData() {
        @Override public int get(int index) { return switch (index) { case 0 -> progress; case 1 -> burnRemaining; case 2 -> burnTotal; default -> heatMode; }; }
        @Override public void set(int index, int value) { switch(index) { case 0 -> progress = value; case 1 -> burnRemaining = value; case 2 -> burnTotal = value; case 3 -> heatMode = value; } }
        @Override public int getCount() { return 4; }
    };

    public SpiritKitchenEntity(BlockPos pos, BlockState state) { super(SpiritualFarming.ENTITY.get(), pos, state); }
    public static boolean fuel(ItemStack stack) { return stack.is(Items.COAL) || stack.is(Items.CHARCOAL) || stack.is(CultivationResources.STONE.get()); }
    public static void tick(Level level, BlockPos pos, BlockState state, SpiritKitchenEntity stove) {
        SpiritKitchenRecipes.Recipe recipe = SpiritKitchenRecipes.find(stove.items.get(0), stove.items.get(1), stove.items.get(2));
        String next = recipe == null ? "" : recipe.food().id;
        if (!next.equals(stove.recipeId)) { stove.recipeId = next; stove.progress = 0; }
        if (stove.burnRemaining > 0) stove.burnRemaining--;
        boolean ready = recipe != null && stove.canOutput(recipe.output());
        if (ready && stove.burnRemaining == 0 && fuel(stove.items.get(3))) {
            stove.heatMode = stove.items.get(3).is(CultivationResources.STONE.get()) ? 1 : 0;
            stove.burnTotal = stove.heatMode == 1 ? 3200 : 1600;
            stove.burnRemaining = stove.burnTotal;
            stove.items.get(3).shrink(1);
        }
        if (ready && stove.burnRemaining > 0) {
            stove.progress += stove.heatMode == 1 ? 10 : 4;
            if (stove.progress >= COOK_UNITS) { stove.cook(recipe); stove.progress = 0; }
        }
        boolean lit = stove.burnRemaining > 0;
        if (state.getValue(SpiritKitchenBlock.LIT) != lit) level.setBlock(pos, state.setValue(SpiritKitchenBlock.LIT, lit), 3);
        stove.setChanged();
    }
    private boolean canOutput(ItemStack result) {
        ItemStack output = items.get(4);
        return output.isEmpty() || ItemStack.isSameItemSameTags(output, result) && output.getCount() + result.getCount() <= output.getMaxStackSize();
    }
    private void cook(SpiritKitchenRecipes.Recipe recipe) {
        boolean honeyBottle = items.get(0).is(Items.HONEY_BOTTLE);
        items.get(0).shrink(1); items.get(1).shrink(recipe.plantCount());
        if (recipe.container() != null) items.get(2).shrink(1);
        if (honeyBottle) { items.set(2, new ItemStack(Items.GLASS_BOTTLE)); containerRemainder = true; }
        ItemStack output = recipe.output();
        if (items.get(4).isEmpty()) items.set(4, output); else items.get(4).grow(output.getCount());
    }
    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag); ContainerHelper.saveAllItems(tag, items);
        tag.putInt("Progress", progress); tag.putInt("BurnRemaining", burnRemaining); tag.putInt("BurnTotal", burnTotal);
        tag.putInt("HeatMode", heatMode); tag.putString("Recipe", recipeId); tag.putBoolean("ContainerRemainder", containerRemainder);
    }
    @Override public void load(CompoundTag tag) {
        super.load(tag); ContainerHelper.loadAllItems(tag, items);
        progress = Math.max(0, Math.min(COOK_UNITS - 1, tag.getInt("Progress")));
        burnRemaining = Math.max(0, tag.getInt("BurnRemaining")); burnTotal = Math.max(0, tag.getInt("BurnTotal"));
        heatMode = tag.getInt("HeatMode") == 1 ? 1 : 0; recipeId = tag.getString("Recipe"); containerRemainder = tag.getBoolean("ContainerRemainder");
    }
    @Override public Component getDisplayName() { return Component.translatable("block.xiuxian.spirit_kitchen"); }
    @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) { return new SpiritKitchenMenu(id, inventory, this, data); }
    @Override public int getContainerSize() { return items.size(); }
    @Override public boolean isEmpty() { return items.stream().allMatch(ItemStack::isEmpty); }
    @Override public ItemStack getItem(int slot) { return items.get(slot); }
    @Override public ItemStack removeItem(int slot, int amount) { ItemStack result = ContainerHelper.removeItem(items, slot, amount); if (slot == 2 && items.get(2).isEmpty()) containerRemainder = false; if (!result.isEmpty()) setChanged(); return result; }
    @Override public ItemStack removeItemNoUpdate(int slot) { ItemStack result = ContainerHelper.takeItem(items, slot); if (slot == 2) containerRemainder = false; return result; }
    @Override public void setItem(int slot, ItemStack stack) { items.set(slot, stack); if (slot == 2) containerRemainder = false; stack.setCount(Math.min(stack.getCount(), getMaxStackSize())); setChanged(); }
    @Override public boolean stillValid(Player player) { return level != null && level.getBlockEntity(worldPosition) == this && player.distanceToSqr(worldPosition.getX()+.5, worldPosition.getY()+.5, worldPosition.getZ()+.5) <= 64; }
    @Override public void clearContent() { items.clear(); containerRemainder = false; setChanged(); }
    @Override public boolean canPlaceItem(int slot, ItemStack stack) {
        return switch (slot) { case 0 -> SpiritKitchenRecipes.isMain(stack); case 1 -> SpiritKitchenRecipes.isPlant(stack); case 2 -> !containerRemainder && (stack.is(Items.BOWL) || stack.is(Items.GLASS_BOTTLE)); case 3 -> fuel(stack); default -> false; };
    }
    @Override public int[] getSlotsForFace(Direction face) { return face == Direction.UP ? TOP : face == Direction.DOWN ? BOTTOM : SIDE; }
    @Override public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction direction) { return direction != Direction.DOWN && canPlaceItem(slot, stack); }
    @Override public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) { return direction == Direction.DOWN && (slot == 4 || slot == 2 && containerRemainder && stack.is(Items.GLASS_BOTTLE)); }
}
