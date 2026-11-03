package dev.xiuxian;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

public class FurnaceMenu extends AbstractContainerMenu {
    private final Container furnace;
    private final ContainerData data;
    public FurnaceMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, containerAt(inv, buf), new SimpleContainerData(1));
    }
    private static Container containerAt(Inventory inv, FriendlyByteBuf buf) {
        var entity = inv.player.level().getBlockEntity(buf.readBlockPos());
        if (!(entity instanceof FurnaceEntity furnace)) throw new IllegalStateException("Herbal furnace is missing");
        return furnace;
    }
    public FurnaceMenu(int id, Inventory inv, Container furnace, ContainerData data) {
        super(Xiuxian.FURNACE_MENU.get(), id);
        this.furnace = furnace; this.data = data;
        checkContainerSize(furnace, 4);
        addSlot(new Slot(furnace, 0, 44, 35) { @Override public boolean mayPlace(ItemStack s) { return s.getItem() instanceof QiHerbItem; } });
        addSlot(new Slot(furnace, 1, 80, 35) { @Override public boolean mayPlace(ItemStack s) { return FurnaceEntity.isReagent(s); } });
        addSlot(new Slot(furnace, 2, 134, 35) { @Override public boolean mayPlace(ItemStack s) { return false; } });
        addSlot(new Slot(furnace, 3, 44, 58) { @Override public boolean mayPlace(ItemStack s) { return s.getItem() instanceof QiHerbItem || s.is(net.minecraft.world.item.Items.WHEAT); } });
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++) addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
        for (int col = 0; col < 9; col++) addSlot(new Slot(inv, col, 8 + col * 18, 142));
        addDataSlots(data);
    }
    public int progress() { return data.get(0); }
    @Override public boolean stillValid(Player player) { return furnace.stillValid(player); }
    @Override public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem(), original = stack.copy();
        if (index < 4) {
            if (!moveItemStackTo(stack, 4, 40, true)) return ItemStack.EMPTY;
        } else if (stack.getItem() instanceof QiHerbItem) {
            if (!moveItemStackTo(stack, 0, 1, false) && !moveItemStackTo(stack, 3, 4, false)) return ItemStack.EMPTY;
        } else if (FurnaceEntity.isReagent(stack)) {
            if (!moveItemStackTo(stack, 1, 2, false)) return ItemStack.EMPTY;
        } else if (stack.is(net.minecraft.world.item.Items.WHEAT)) {
            if (!moveItemStackTo(stack, 3, 4, false)) return ItemStack.EMPTY;
        } else if (index < 31) {
            if (!moveItemStackTo(stack, 31, 40, false)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(stack, 4, 31, false)) return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
        slot.onTake(player, stack);
        return original;
    }
}


