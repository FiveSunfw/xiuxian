package dev.xiuxian;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public class QiHerbItem extends BlockItem {
    private final HerbKind kind;
    public QiHerbItem(HerbKind kind, net.minecraft.world.level.block.Block block) { super(block, new Properties()); this.kind = kind; }
    public HerbKind kind() { return kind; }

    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return InitiationItem.beginUse(level, player, hand);
    }
    @Override public int getUseDuration(ItemStack stack) { return 32; }
    @Override public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.EAT; }
    @Override public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        return InitiationItem.finishInitiation(stack, entity, false);
    }
}

