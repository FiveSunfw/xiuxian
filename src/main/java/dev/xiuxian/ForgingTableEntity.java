package dev.xiuxian;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/** One pending infusion and four persisted inventory slots per workstation. */
public final class ForgingTableEntity extends BlockEntity implements Container, MenuProvider {
    public static final int INFUSE_TICKS = 600;
    private final NonNullList<ItemStack> items = NonNullList.withSize(4, ItemStack.EMPTY);
    private int progress;
    private int skillLevelView = 1;
    private ItemStack pendingEquipment = ItemStack.EMPTY;
    private String pendingMaterial = "";
    private java.util.UUID pendingOwner;

    final ContainerData data = new ContainerData() {
        @Override public int get(int index) { return index == 0 ? progress : index == 1 ? INFUSE_TICKS : skillLevelView; }
        @Override public void set(int index, int value) {
            if (index == 0) progress = Math.max(0, Math.min(INFUSE_TICKS, value));
            else if (index == 2) skillLevelView = Math.max(1, Math.min(9, value));
        }
        @Override public int getCount() { return 3; }
    };

    public ForgingTableEntity(BlockPos pos, BlockState state) {
        super(ForgingContent.ENTITY.get(), pos, state);
    }

    public boolean busy() { return !pendingEquipment.isEmpty(); }
    public int progress() { return progress; }
    public ForgingRules.Preview preview() { return ForgingRules.preview(items.get(0), items.get(2)); }

    public boolean start(Player player) {
        if (level == null || level.isClientSide || busy() || !items.get(3).isEmpty()) return false;
        ForgingRules.Preview preview = preview();
        if (!preview.valid() || !items.get(1).is(CultivationResources.STONE.get())) return false;
        if (ForgingSkill.level(player) < ForgingSkill.requiredLevel(preview.tier())) return false;
        if (items.get(0).getCount() < 1 || items.get(1).getCount() < 1 || items.get(2).getCount() < 1) return false;

        pendingEquipment = items.get(0).copyWithCount(1);
        pendingMaterial = ForgingRules.materialId(items.get(2));
        pendingOwner = player.getUUID();
        items.get(0).shrink(1);
        items.get(1).shrink(1);
        items.get(2).shrink(1);
        progress = 0;
        setChanged();
        return true;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ForgingTableEntity table) {
        if (table.pendingEquipment.isEmpty() || !table.items.get(3).isEmpty()) return;
        if (++table.progress >= INFUSE_TICKS) {
            ItemStack result = ForgedItemData.infuse(table.pendingEquipment, table.pendingMaterial);
            if (!result.isEmpty()) table.items.set(3, result);
            int awardedLevel = ForgingSkill.award((ServerLevel)level, table.pendingOwner);
            if (awardedLevel > 0) table.skillLevelView = awardedLevel;
            table.pendingEquipment = ItemStack.EMPTY;
            table.pendingMaterial = "";
            table.pendingOwner = null;
            table.progress = 0;
        }
        table.setChanged();
    }

    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, items);
        tag.putInt("InfusionProgress", progress);
        tag.putInt("SkillLevel", skillLevelView);
        tag.put("PendingEquipment", pendingEquipment.save(new CompoundTag()));
        tag.putString("PendingMaterial", pendingMaterial);
        if (pendingOwner != null) tag.putUUID("PendingOwner", pendingOwner);
    }

    @Override public void load(CompoundTag tag) {
        super.load(tag);
        ContainerHelper.loadAllItems(tag, items);
        progress = Math.max(0, Math.min(INFUSE_TICKS - 1, tag.getInt("InfusionProgress")));
        skillLevelView = Math.max(1, Math.min(9, tag.getInt("SkillLevel")));
        pendingEquipment = ItemStack.of(tag.getCompound("PendingEquipment"));
        pendingMaterial = tag.getString("PendingMaterial");
        pendingOwner = tag.hasUUID("PendingOwner") ? tag.getUUID("PendingOwner") : null;
        if (pendingEquipment.isEmpty() || ForgingRules.materialLayer(pendingMaterial) == 0) {
            pendingEquipment = ItemStack.EMPTY;
            pendingMaterial = "";
            pendingOwner = null;
            progress = 0;
        }
    }

    public void dropPending() {
        if (level == null || pendingEquipment.isEmpty()) return;
        ContainersDrop.drop(level, worldPosition, pendingEquipment);
        pendingEquipment = ItemStack.EMPTY;
        pendingMaterial = "";
        pendingOwner = null;
        progress = 0;
    }

    @Override public Component getDisplayName() { return Component.translatable("block.xiuxian.basic_forging_table"); }
    @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        skillLevelView = ForgingSkill.level(player);
        return new ForgingMenu(id, inventory, this, data, ForgingSkill.level(player));
    }
    @Override public int getContainerSize() { return items.size(); }
    @Override public boolean isEmpty() { return items.stream().allMatch(ItemStack::isEmpty) && pendingEquipment.isEmpty(); }
    @Override public ItemStack getItem(int slot) { return items.get(slot); }
    @Override public ItemStack removeItem(int slot, int amount) {
        if (slot == 3 && busy()) return ItemStack.EMPTY;
        ItemStack result = ContainerHelper.removeItem(items, slot, amount);
        if (!result.isEmpty()) setChanged();
        return result;
    }
    @Override public ItemStack removeItemNoUpdate(int slot) {
        if (slot == 3 && busy()) return ItemStack.EMPTY;
        return ContainerHelper.takeItem(items, slot);
    }
    @Override public void setItem(int slot, ItemStack stack) {
        if (busy() || slot == 3) return;
        items.set(slot, stack);
        stack.setCount(Math.min(stack.getCount(), getMaxStackSize()));
        setChanged();
    }
    @Override public boolean canPlaceItem(int slot, ItemStack stack) {
        if (busy()) return false;
        return switch (slot) {
            case 0 -> ForgingRules.isEquipmentBlank(stack) || ForgedItemData.isInfused(stack);
            case 1 -> stack.is(CultivationResources.STONE.get());
            case 2 -> !ForgingRules.materialId(stack).isEmpty();
            default -> false;
        };
    }
    @Override public void clearContent() {
        if (busy()) dropPending();
        items.clear();
        setChanged();
    }
    @Override public boolean stillValid(Player player) {
        return level != null && level.getBlockEntity(worldPosition) == this &&
            player.distanceToSqr(Vec3.atCenterOf(worldPosition)) <= 64;
    }

    private static final class ContainersDrop {
        private static void drop(Level level, BlockPos pos, ItemStack stack) {
            net.minecraft.world.entity.item.ItemEntity item = new net.minecraft.world.entity.item.ItemEntity(
                level, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5, stack.copy());
            item.setDefaultPickUpDelay();
            level.addFreshEntity(item);
        }
    }
}
