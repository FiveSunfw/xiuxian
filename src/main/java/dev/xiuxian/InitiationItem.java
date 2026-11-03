package dev.xiuxian;

import net.minecraft.world.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;

public class InitiationItem extends Item {
    private final boolean liquid;
    public InitiationItem(boolean liquid) {
        super(new Item.Properties().stacksTo(liquid ? 1 : 64));
        this.liquid = liquid;
    }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return beginUse(level, player, hand);
    }
    static InteractionResultHolder<ItemStack> beginUse(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && Cultivation.initiated(player)) {
            player.displayClientMessage(Component.translatable("message.xiuxian.already"), true);
            return InteractionResultHolder.fail(stack);
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }
    @Override public int getUseDuration(ItemStack stack) { return 32; }
    @Override public UseAnim getUseAnimation(ItemStack stack) { return liquid ? UseAnim.DRINK : UseAnim.EAT; }
    @Override public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        return finishInitiation(stack, entity, liquid);
    }
    static ItemStack finishInitiation(ItemStack stack, LivingEntity entity, boolean liquid) {
        if (!(entity instanceof ServerPlayer player) || Cultivation.initiated(player)) return stack;
        double chance = liquid ? Cultivation.LIQUID_CHANCE.get() : Cultivation.HERB_CHANCE.get();
        boolean success = player.getRandom().nextDouble() < chance;
        if (success) Cultivation.initiate(player);
        player.displayClientMessage(Component.translatable(success ? "message.xiuxian.success" : "message.xiuxian.failure"), false);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
            if (liquid) {
                ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
                if (stack.isEmpty()) return bottle;
                if (!player.getInventory().add(bottle)) player.drop(bottle, false);
            }
        }
        return stack;
    }
}
