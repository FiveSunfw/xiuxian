package dev.xiuxian;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

public final class SpiritKitchenMenu extends AbstractContainerMenu {
    final Container container;
    final ContainerData data;
    public SpiritKitchenMenu(int id, Inventory inventory, FriendlyByteBuf buffer) {
        this(id, inventory, containerAt(inventory, buffer), new SimpleContainerData(4));
    }
    private static Container containerAt(Inventory inventory, FriendlyByteBuf buffer) {
        var entity = inventory.player.level().getBlockEntity(buffer.readBlockPos());
        if (!(entity instanceof SpiritKitchenEntity stove)) throw new IllegalStateException("Spirit kitchen is missing");
        return stove;
    }
    public SpiritKitchenMenu(int id, Inventory inventory, Container container, ContainerData data) {
        super(SpiritualFarming.MENU.get(), id); this.container = container; this.data = data;
        checkContainerSize(container, 5);
        int[][] coordinates = {{20, 31}, {55, 31}, {90, 31}, {20, 63}, {143, 45}};
        for (int i = 0; i < 5; i++) {
            final int slot = i;
            addSlot(new Slot(container, i, coordinates[i][0], coordinates[i][1]) {
                @Override public boolean mayPlace(ItemStack stack) { return slot != 4 && container.canPlaceItem(slot, stack); }
            });
        }
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++) addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 104 + row * 18));
        for (int col = 0; col < 9; col++) addSlot(new Slot(inventory, col, 8 + col * 18, 162));
        addDataSlots(data);
    }
    @Override public boolean stillValid(Player player) { return container.stillValid(player); }
    @Override public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index); if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem(), original = stack.copy();
        if (index < 5) { if (!moveItemStackTo(stack, 5, 41, true)) return ItemStack.EMPTY; }
        else {
            boolean moved = false;
            for (int target : new int[]{0,1,2,3}) if (slots.get(target).mayPlace(stack) && moveItemStackTo(stack, target, target + 1, false)) { moved = true; break; }
            if (!moved) return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged(); slot.onTake(player, stack); return original;
    }
}
