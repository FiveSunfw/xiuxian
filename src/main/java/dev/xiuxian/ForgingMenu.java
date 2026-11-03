package dev.xiuxian;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class ForgingMenu extends AbstractContainerMenu {
    final Container container;
    final ContainerData data;

    public ForgingMenu(int id, Inventory inventory, FriendlyByteBuf buffer) {
        this(id, inventory, containerAt(inventory, buffer), clientData(buffer), 0);
    }

    private static ContainerData clientData(FriendlyByteBuf buffer) {
        var data = new net.minecraft.world.inventory.SimpleContainerData(3);
        data.set(2, buffer.readVarInt());
        return data;
    }

    private static Container containerAt(Inventory inventory, FriendlyByteBuf buffer) {
        var entity = inventory.player.level().getBlockEntity(buffer.readBlockPos());
        if (!(entity instanceof ForgingTableEntity table)) throw new IllegalStateException("Forging table is missing");
        return table;
    }

    public ForgingMenu(int id, Inventory inventory, Container container, ContainerData data, int skillLevel) {
        super(ForgingContent.MENU.get(), id);
        this.container = container;
        this.data = data;
        if (data.getCount() > 2 && skillLevel > 0) data.set(2, skillLevel);
        checkContainerSize(container, 4);
        addDataSlots(data);
        addSlot(new Slot(container, 0, 17, 34) {
            @Override public boolean mayPlace(ItemStack stack) {
                return !busy() && (ForgingRules.isEquipmentBlank(stack) || ForgedItemData.isInfused(stack));
            }
            @Override public boolean mayPickup(Player player) { return !busy(); }
        });
        addSlot(new Slot(container, 1, 53, 34) {
            @Override public boolean mayPlace(ItemStack stack) { return !busy() && stack.is(CultivationResources.STONE.get()); }
            @Override public boolean mayPickup(Player player) { return !busy(); }
        });
        addSlot(new Slot(container, 2, 89, 34) {
            @Override public boolean mayPlace(ItemStack stack) { return !busy() && !ForgingRules.materialId(stack).isEmpty(); }
            @Override public boolean mayPickup(Player player) { return !busy(); }
        });
        addSlot(new Slot(container, 3, 143, 34) {
            @Override public boolean mayPlace(ItemStack stack) { return false; }
        });
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 9; col++) addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 128 + row * 18));
        for (int col = 0; col < 9; col++) addSlot(new Slot(inventory, col, 8 + col * 18, 186));
    }

    private boolean busy() { return progress() > 0 || container instanceof ForgingTableEntity table && table.busy(); }
    public int progress() { return data.get(0); }
    public int totalTicks() { return Math.max(1, data.get(1)); }
    public int skillLevel() { return data.getCount() > 2 ? data.get(2) : 1; }

    public ForgingRules.Preview preview() {
        if (busy()) return new ForgingRules.Preview("busy", 0, java.util.List.of(), "", null);
        if (!container.getItem(3).isEmpty()) return new ForgingRules.Preview("output", 0, java.util.List.of(), "", null);
        ForgingRules.Preview preview = ForgingRules.preview(container.getItem(0), container.getItem(2));
        if (preview.valid() && !container.getItem(1).is(CultivationResources.STONE.get()))
            return new ForgingRules.Preview("stone", 0, preview.existing(), "", preview.slot());
        if (preview.valid() && skillLevel() < ForgingSkill.requiredLevel(preview.tier()))
            return new ForgingRules.Preview("skill", preview.tier(),
                preview.existing(), "", preview.slot());
        return preview;
    }

    @Override public boolean clickMenuButton(Player player, int id) {
        return id == 0 && container instanceof ForgingTableEntity table && table.start(player);
    }

    @Override public boolean stillValid(Player player) { return container.stillValid(player); }

    @Override public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (index < 4) {
            if (!moveItemStackTo(stack, 4, 40, true)) return ItemStack.EMPTY;
        } else {
            boolean moved = false;
            for (int input = 0; input < 3; input++) {
                if (slots.get(input).mayPlace(stack) && moveItemStackTo(stack, input, input + 1, false)) {
                    moved = true;
                    break;
                }
            }
            if (!moved) return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
        slot.onTake(player, stack);
        return copy;
    }
}
