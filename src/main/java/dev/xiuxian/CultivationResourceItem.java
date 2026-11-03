package dev.xiuxian;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;

public class CultivationResourceItem extends Item {
    public enum Kind { STONE, STONE_2, PILL }
    private final Kind kind;
    public CultivationResourceItem(Kind kind) { super(new Properties()); this.kind = kind; }
    private boolean ready(Player player) {
        if (player.level().isClientSide) return true;
        if (!Cultivation.initiated(player)) {
            if (!player.level().isClientSide) player.displayClientMessage(Component.translatable("message.xiuxian.need_initiation"), true);
            return false;
        }
        if (Cultivation.progress(player).level() >= CultivationProgress.MAX_LEVEL) {
            if (!player.level().isClientSide) player.displayClientMessage(Component.translatable("screen.xiuxian.progress_full"), true);
            return false;
        }
        return true;
    }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!ready(player)) return InteractionResultHolder.fail(stack);
        if (kind == Kind.PILL) { player.startUsingItem(hand); return InteractionResultHolder.consume(stack); }
        if (getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE).getType() != HitResult.Type.MISS) return InteractionResultHolder.pass(stack);
        if (player instanceof ServerPlayer server) {
            int amount = kind == Kind.STONE ? Cultivation.STONE_POINTS.get() : Cultivation.STONE_2_POINTS.get();
            if (Cultivation.addProgress(server, amount)) {
                if (!player.getAbilities().instabuild) stack.shrink(1);
                player.getCooldowns().addCooldown(this, 10);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
    @Override public int getUseDuration(ItemStack stack) { return kind == Kind.PILL ? 32 : 0; }
    @Override public UseAnim getUseAnimation(ItemStack stack) { return kind == Kind.PILL ? UseAnim.EAT : UseAnim.NONE; }
    @Override public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity instanceof ServerPlayer player && ready(player)) {
            CultivationProgress before = Cultivation.progress(player);
            int base = Cultivation.progressBase();
            if (Cultivation.addProgress(player, Cultivation.PILL_POINTS.get())) {
                if (!player.getAbilities().instabuild) stack.shrink(1);
                CultivationProgress after = Cultivation.progress(player);
                // Include thresholds crossed by this dose, and exclude points discarded at the cap.
                long gained = (long) after.points() - before.points()
                    + (long) base * (after.level() * (after.level() - 1) - before.level() * (before.level() - 1)) / 2;
                Component progress = after.level() == CultivationProgress.MAX_LEVEL
                    ? Component.translatable("screen.xiuxian.progress_full")
                    : Component.literal(after.points() + " / " + after.required(base));
                player.displayClientMessage(Component.translatable("message.xiuxian.pill_progress", gained, after.level(), progress), false);
            }
        }
        return stack;
    }
}

