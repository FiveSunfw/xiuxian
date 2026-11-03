package dev.xiuxian;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

public class TechniqueItem extends Item {
    private final int tier;
    public TechniqueItem(int tier) { super(new Properties().stacksTo(1)); this.tier = tier; }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer server) {
            if (!Cultivation.initiated(player)) {
                player.displayClientMessage(Component.translatable("message.xiuxian.need_initiation"), true);
                return InteractionResultHolder.fail(stack);
            }
            if (!Cultivation.learnTechnique(server, tier)) {
                player.displayClientMessage(Component.translatable("message.xiuxian.manual_known"), true);
                return InteractionResultHolder.fail(stack);
            }
            if (!player.getAbilities().instabuild) stack.shrink(1);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
