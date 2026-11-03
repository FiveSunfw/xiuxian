package dev.xiuxian;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.UUID;

/** Applies stored forging affixes from the actual equipped and held item stacks. */
@Mod.EventBusSubscriber(modid = Xiuxian.ID)
public final class ForgingEffects {
    private static final UUID ARMOR = UUID.fromString("b127c610-1d52-454d-b040-741ea33d6acd");
    private static final UUID SPEED = UUID.fromString("b127c610-1d52-454d-b040-741ea33d6ace");
    private static final UUID ATTACK_SPEED = UUID.fromString("b127c610-1d52-454d-b040-741ea33d6acf");
    private static final ThreadLocal<Boolean> REAPPLYING_EFFECT = ThreadLocal.withInitial(() -> false);

    private ForgingEffects() {}

    private static ItemStack armor(Player player, EquipmentSlot slot) { return player.getItemBySlot(slot); }
    public static boolean has(ItemStack stack, String id) { return ForgedItemData.materials(stack).contains(id); }
    private static boolean hasArmor(Player player, EquipmentSlot slot, String id) { return has(armor(player, slot), id); }

    public static double maxManaBonus(Player player) {
        int count = 0;
        for (EquipmentSlot slot : List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET))
            if (hasArmor(player, slot, "beast_marrow")) count++;
        if (has(player.getMainHandItem(), "beast_marrow")) count++;
        return 20.0 * count;
    }

    private static void setModifier(LivingEntity entity, Attribute attribute, UUID id, double amount, String name) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance == null) return;
        AttributeModifier current = instance.getModifier(id);
        if (Math.abs(amount) < 1e-8) {
            if (current != null) instance.removeModifier(id);
        } else if (current == null || Math.abs(current.getAmount() - amount) > 1e-8) {
            if (current != null) instance.removeModifier(id);
            instance.addTransientModifier(new AttributeModifier(id, name, amount, AttributeModifier.Operation.ADDITION));
        }
    }

    @SubscribeEvent public static void equipmentAttributes(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) return;
        double armorBonus = 0;
        double speedBonus = 0;
        for (EquipmentSlot slot : List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)) {
            if (hasArmor(player, slot, "rockhide_plate")) {
                armorBonus += slot == EquipmentSlot.CHEST ? 2 : slot == EquipmentSlot.HEAD || slot == EquipmentSlot.LEGS ? 1 : 0;
                speedBonus -= slot == EquipmentSlot.CHEST ? .005 : .003;
            }
            if (hasArmor(player, slot, "thick_rock_plate")) {
                armorBonus += slot == EquipmentSlot.CHEST ? 2 : slot == EquipmentSlot.HEAD ? 1 : 0;
                speedBonus -= slot == EquipmentSlot.CHEST ? .012 : slot == EquipmentSlot.HEAD ? .004 : 0;
            }
        }
        if (hasArmor(player, EquipmentSlot.HEAD, "wolf_pelt") && player.isCrouching()) speedBonus += .008;
        if (hasArmor(player, EquipmentSlot.CHEST, "wolf_pelt") && player.isSprinting()) speedBonus += .008;
        if (hasArmor(player, EquipmentSlot.LEGS, "wolf_pelt") && player.isSprinting()) speedBonus += .006;
        if (hasArmor(player, EquipmentSlot.CHEST, "jade_scale") && player.isInWater()) speedBonus += .015;
        if (hasArmor(player, EquipmentSlot.FEET, "king_tusk")) speedBonus -= .004;
        if (hasArmor(player, EquipmentSlot.FEET, "azure_fang") && movingTowardLastTarget(player)) speedBonus += .006;
        setModifier(player, Attributes.ARMOR, ARMOR, armorBonus, "xiuxian.forging_armor");
        setModifier(player, Attributes.MOVEMENT_SPEED, SPEED, speedBonus, "xiuxian.forging_speed");
        double attackSpeed = has(player.getMainHandItem(), "wolf_pelt") ? .08 : 0;
        setModifier(player, Attributes.ATTACK_SPEED, ATTACK_SPEED, attackSpeed, "xiuxian.forging_attack_speed");

        CompoundTag persistent = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        double manaBonus = maxManaBonus(player);
        if (Double.compare(persistent.getDouble("xiuxian_forging_mana_bonus"), manaBonus) != 0) {
            persistent.putDouble("xiuxian_forging_mana_bonus", manaBonus);
            player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persistent);
            if (Cultivation.initiated(player)) Cultivation.sync(player);
        }

        if (player.isEyeInFluid(net.minecraft.tags.FluidTags.WATER)) {
            if (hasArmor(player, EquipmentSlot.LEGS, "jade_scale") && player.tickCount % 3 == 0)
                player.setAirSupply(Math.min(player.getMaxAirSupply(), player.getAirSupply() + 1));
            else if (hasArmor(player, EquipmentSlot.CHEST, "python_sinew") && player.tickCount % 5 == 0)
                player.setAirSupply(Math.min(player.getMaxAirSupply(), player.getAirSupply() + 1));
            if (hasArmor(player, EquipmentSlot.HEAD, "jade_scale") && player.tickCount % 100 == 0)
                player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.NIGHT_VISION, 220, 0, true, false, false));
        }
        if (player.getBlockStateOn().is(Blocks.COBWEB) && hasArmor(player, EquipmentSlot.LEGS, "frost_silk")) {
            Vec3 motion = player.getDeltaMovement();
            player.setDeltaMovement(motion.x * 2.2, motion.y, motion.z * 2.2);
        }
    }

    private static boolean movingTowardLastTarget(Player player) {
        LivingEntity target = player.getLastHurtMob();
        if (target == null || !target.isAlive()) return false;
        Vec3 toward = target.position().subtract(player.position());
        Vec3 movement = player.getDeltaMovement();
        return toward.horizontalDistanceSqr() > 0 && movement.horizontalDistanceSqr() > .002
            && toward.normalize().dot(movement.normalize()) > .35;
    }

    @SubscribeEvent public static void jump(LivingEvent.LivingJumpEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        double bonus = (hasArmor(player, EquipmentSlot.HEAD, "spirit_horn") ? .08 : 0)
            + (hasArmor(player, EquipmentSlot.FEET, "spirit_horn") ? .08 : 0);
        if (bonus > 0) {
            Vec3 motion = player.getDeltaMovement();
            player.setDeltaMovement(motion.x, motion.y + Math.min(.15, bonus), motion.z);
        }
    }

    @SubscribeEvent public static void falling(LivingFallEvent event) {
        if (event.getEntity() instanceof Player player && hasArmor(player, EquipmentSlot.FEET, "wind_feather"))
            event.setDamageMultiplier(event.getDamageMultiplier() * .6F);
    }

    @SubscribeEvent public static void hurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (hasArmor(player, EquipmentSlot.HEAD, "thunder_scale") && event.getSource().getEntity() instanceof SpiritBeast beast
                && beast.kind == BeastKind.LIZARD) event.setAmount(event.getAmount() * .75F);
            if (hasArmor(player, EquipmentSlot.CHEST, "king_tusk") && event.getSource().getEntity() instanceof SpiritBeast beast
                && beast.getDeltaMovement().horizontalDistanceSqr() > .08) event.setAmount(event.getAmount() * .8F);
            if (event.getSource().getDirectEntity() instanceof LivingEntity attacker && attacker != player
                && has(player.getMainHandItem(), "thick_rock_plate")) event.setAmount(event.getAmount() * .92F);
        }
        if (event.getSource().getEntity() instanceof ServerPlayer attacker && !attacker.getPersistentData().getBoolean("xiuxian_forging_projectile_hit")
            && has(attacker.getMainHandItem(), "rockhide_plate")) event.setAmount(event.getAmount() * 1.08F);
    }

    @SubscribeEvent public static void knockback(LivingKnockBackEvent event) {
        if (event.getEntity() instanceof Player player && player.isInWater()
            && hasArmor(player, EquipmentSlot.LEGS, "python_sinew")) event.setStrength(event.getStrength() * .8F);
        if (event.getEntity() instanceof Player player && hasArmor(player, EquipmentSlot.FEET, "king_tusk"))
            event.setStrength(event.getStrength() * .8F);
    }

    @SubscribeEvent public static void shortenDebuffs(MobEffectEvent.Applicable event) {
        if (REAPPLYING_EFFECT.get() || !(event.getEntity() instanceof Player player) || player.level().isClientSide) return;
        MobEffectInstance effect = event.getEffectInstance();
        int duration = effect.getDuration();
        if (duration <= 0) return;
        double reduction = 0;
        if (effect.getEffect() == MobEffects.MOVEMENT_SLOWDOWN && hasArmor(player, EquipmentSlot.HEAD, "frost_silk"))
            reduction = .40;
        if (effect.getEffect() == MobEffects.POISON) {
            if (hasArmor(player, EquipmentSlot.CHEST, "venom_sac")) reduction = Math.max(reduction, .35);
            else if (hasArmor(player, EquipmentSlot.LEGS, "venom_sac")) reduction = Math.max(reduction, .25);
        }
        int trim = (int)Math.floor(duration * reduction);
        if (trim <= 0) return;
        event.setResult(net.minecraftforge.eventbus.api.Event.Result.DENY);
        MobEffectInstance shortened = new MobEffectInstance(effect.getEffect(), Math.max(1, duration - trim),
            effect.getAmplifier(), effect.isAmbient(), effect.isVisible(), effect.showIcon());
        effect.getCurativeItems().forEach(shortened::addCurativeItem);
        REAPPLYING_EFFECT.set(true);
        try { player.addEffect(shortened); }
        finally { REAPPLYING_EFFECT.set(false); }
    }

    public static double flightSpeed(ItemStack weapon, String art, boolean returning, boolean inWater) {
        double multiplier = 1;
        if (returning) {
            if (has(weapon, "frost_silk")) multiplier *= 1.10;
            if (has(weapon, "eagle_talon")) multiplier *= 1.25;
        } else {
            if (has(weapon, "spirit_horn")) multiplier *= 1.12;
            if (has(weapon, "azure_fang")) multiplier *= 1.10;
            if (has(weapon, "rockhide_plate")) multiplier *= .90;
            if (has(weapon, "wind_feather")) multiplier *= 1.15;
            if (has(weapon, "thick_rock_plate")) multiplier *= .80;
        }
        if (inWater && has(weapon, "jade_scale")) multiplier *= 1.12;
        return multiplier;
    }

    public static double swordDamageMultiplier(ItemStack weapon, LivingEntity target) {
        double multiplier = 1;
        if (has(weapon, "spirit_horn")) multiplier *= .92;
        if (has(weapon, "wolf_pelt")) multiplier *= .95;
        if (has(weapon, "rockhide_plate")) multiplier *= 1.08;
        if (has(weapon, "king_tusk")) multiplier *= 1.12;
        if (has(weapon, "thunder_scale") && target instanceof SpiritBeast beast && beast.kind == BeastKind.LIZARD) multiplier *= 1.08;
        return multiplier;
    }

    public static int armorPenetration(ItemStack weapon) { return has(weapon, "curved_tusk") ? 2 : 0; }

    public static void onSwordHit(ServerPlayer player, LivingEntity target) {
        ItemStack weapon = player.getMainHandItem();
        applyCooldownEffect(player, target, weapon, "viper_fang", "BleedUntil", 240, () ->
            target.hurt(player.damageSources().playerAttack(player), 2));
        applyCooldownEffect(player, target, weapon, "venom_sac", "VenomUntil", 240, () ->
            target.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.POISON, 60, 0)));
        applyCooldownEffect(player, target, weapon, "frost_gland", "FrostUntil", 200, () ->
            target.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 40, 1)));
        applyCooldownEffect(player, target, weapon, "python_sinew", "SinewUntil", 240, () ->
            target.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 40, 0)));
        if (has(weapon, "charged_spine")) {
            String hitsKey = "xiuxian_charged_hits_" + player.getUUID();
            String cooldownKey = "xiuxian_charged_until_" + player.getUUID();
            int hits = target.getPersistentData().getInt(hitsKey) + 1;
            target.getPersistentData().putInt(hitsKey, hits);
            if (hits >= 3 && player.level().getGameTime() >= target.getPersistentData().getLong(cooldownKey)) {
                target.getPersistentData().putInt(hitsKey, 0);
                target.getPersistentData().putLong(cooldownKey, player.level().getGameTime() + 160);
                target.hurt(player.damageSources().magic(), 2);
            }
        }
    }

    private static void applyCooldownEffect(ServerPlayer player, LivingEntity target, ItemStack weapon, String material,
                                           String cooldown, int ticks, Runnable apply) {
        if (!has(weapon, material)) return;
        String key = "xiuxian_forging_" + cooldown + "_" + player.getUUID();
        long now = player.level().getGameTime();
        if (now < target.getPersistentData().getLong(key)) return;
        apply.run();
        target.getPersistentData().putLong(key, now + ticks);
    }
}
