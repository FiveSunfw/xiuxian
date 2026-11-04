package dev.xiuxian;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public final class SpiritFoodItem extends Item {
    private static final int DURATION = 6000;
    private final SpiritFoodKind kind;
    public SpiritFoodItem(SpiritFoodKind kind) {
        super(new Properties().stacksTo(kind.container == null ? 64 : 1)
            .food(new FoodProperties.Builder().nutrition(kind.nutrition).saturationMod(kind.saturation).alwaysEat().build()));
        this.kind = kind;
    }
    @Override public int getUseDuration(ItemStack stack) { return kind == SpiritFoodKind.DRINK ? 16 : 32; }
    @Override public UseAnim getUseAnimation(ItemStack stack) { return kind == SpiritFoodKind.DRINK ? UseAnim.DRINK : UseAnim.EAT; }
    @Override public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (level.isClientSide || !(entity instanceof ServerPlayer player)) return result;
        switch (kind) {
            case RICE -> player.addEffect(new MobEffectInstance(SpiritualFarming.MANA_REGEN.get(), DURATION));
            case BREAD -> player.addEffect(new MobEffectInstance(CultivationResources.NOURISHMENT.get(), DURATION));
            case STEAK -> { player.addEffect(new MobEffectInstance(SpiritualFarming.ATTACK.get(), DURATION)); player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, DURATION)); }
            case STEW -> player.addEffect(new MobEffectInstance(SpiritualFarming.VITALITY.get(), DURATION));
            case POTATO -> { player.addEffect(new MobEffectInstance(SpiritualFarming.DEFENSE.get(), DURATION)); player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, DURATION)); }
            case SOUP -> { player.addEffect(new MobEffectInstance(SpiritualFarming.SPEED.get(), DURATION)); player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, DURATION)); }
            case DRINK -> { player.heal(6); CultivationFoundation.restoreMana(player, 30); }
            case CAKE -> { player.addEffect(new MobEffectInstance(SpiritualFarming.STARLIGHT.get(), DURATION)); player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, DURATION)); }
        }
        if (kind.container != null && !player.getAbilities().instabuild) {
            ItemStack container = new ItemStack(kind.container);
            if (result.isEmpty()) return container;
            if (!player.getInventory().add(container)) player.drop(container, false);
        }
        return result;
    }
}
