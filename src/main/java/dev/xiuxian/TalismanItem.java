package dev.xiuxian;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public final class TalismanItem extends Item {
    public final TalismanKind kind;
    public TalismanItem(TalismanKind kind) { super(new Properties().stacksTo(16));this.kind=kind; }
    @Override public Component getName(ItemStack stack) { return Talismans.enhanced(stack)?Component.translatable("talisman.xiuxian.enhanced",super.getName(stack)):super.getName(stack); }
    private boolean allowed(Player player) {
        if(player.getCooldowns().isOnCooldown(this))return false;
        if(!Cultivation.initiated(player)) { player.displayClientMessage(Component.translatable("talisman.xiuxian.mortal_use"),true);return false; }return true;
    }
    static void consumed(Player player,ItemStack stack) {
        if(!player.getAbilities().instabuild)stack.shrink(1);
        for(var item:Talismans.ITEMS.values())player.getCooldowns().addCooldown(item.get(),20);
    }
    @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand) {
        ItemStack stack=player.getItemInHand(hand);
        if(level.isClientSide)return InteractionResultHolder.sidedSuccess(stack,true);
        if(!(player instanceof ServerPlayer server) || !allowed(player) || kind==TalismanKind.ROCK)return InteractionResultHolder.fail(stack);
        if(kind==TalismanKind.FIRE || kind==TalismanKind.ICE || kind==TalismanKind.THUNDER) {
            var projectile=new TalismanProjectile(Talismans.PROJECTILE.get(),level);projectile.setOwner(player);projectile.setItem(stack.copyWithCount(1));projectile.setPos(player.getX(),player.getEyeY()-.1,player.getZ());projectile.shootFromRotation(player,player.getXRot(),player.getYRot(),0,1.5f,0);level.addFreshEntity(projectile);
        } else TalismanEffects.activate(server,kind,Talismans.enhanced(stack));
        consumed(player,stack);return InteractionResultHolder.consume(stack);
    }
    @Override public InteractionResult useOn(UseOnContext context) {
        var player=context.getPlayer();if(player==null)return InteractionResult.PASS;
        if(kind!=TalismanKind.ROCK)return use(context.getLevel(),player,context.getHand()).getResult();
        if(context.getLevel().isClientSide)return InteractionResult.SUCCESS;
        if(!(player instanceof ServerPlayer server)||!allowed(player))return InteractionResult.FAIL;
        boolean enhanced=Talismans.enhanced(context.getItemInHand());
        if(!TalismanEffects.breakRocks(server,context.getClickedPos()))return InteractionResult.FAIL;
        if(enhanced)server.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DIG_SPEED,600,0));
        consumed(player,context.getItemInHand());return InteractionResult.CONSUME;
    }
}
