package dev.xiuxian;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.minecraftforge.network.NetworkHooks;

public final class TalismanProjectile extends ThrowableItemProjectile {
    public TalismanProjectile(EntityType<? extends TalismanProjectile> type,Level level) { super(type,level); }
    @Override protected Item getDefaultItem() { return Talismans.ITEMS.get(TalismanKind.FIRE).get(); }
    TalismanKind kind() { return getItem().getItem() instanceof TalismanItem item?item.kind:TalismanKind.FIRE; }
    @Override protected float getGravity() { return 0; }
    @Override public Packet<ClientGamePacketListener> getAddEntityPacket() { return NetworkHooks.getEntitySpawningPacket(this); }
    @Override public void tick() {
        super.tick();
        if(!level().isClientSide) { if(tickCount>=100){discard();return;}((ServerLevel)level()).sendParticles(kind()==TalismanKind.FIRE?ParticleTypes.FLAME:kind()==TalismanKind.ICE?ParticleTypes.SNOWFLAKE:ParticleTypes.ELECTRIC_SPARK,getX(),getY(),getZ(),2,.04,.04,.04,0); }
    }
    boolean allowed(LivingEntity target) {
        if(target==getOwner()||!target.isAlive())return false;
        if(getOwner() instanceof Player player) {
            if(target instanceof TamableAnimal pet && player.getUUID().equals(pet.getOwnerUUID()))return false;
            if(target instanceof Player other && ((!level().isClientSide&&!level().getServer().isPvpAllowed())||!player.canHarmPlayer(other)))return false;
            if(player.isAlliedTo(target))return false;
        }
        return true;
    }
    @Override protected boolean canHitEntity(Entity entity) { return super.canHitEntity(entity) && (!(entity instanceof LivingEntity living)||allowed(living)); }
    private net.minecraft.world.damagesource.DamageSource typedDamage(net.minecraft.resources.ResourceKey<net.minecraft.world.damagesource.DamageType> type) { return new net.minecraft.world.damagesource.DamageSource(level().registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE).getHolderOrThrow(type),this,getOwner()); }
    void damageTarget(LivingEntity target) {
        if(!allowed(target))return;
        boolean enhanced=Talismans.enhanced(getItem());
        var sources=damageSources();var damage=sources.indirectMagic(this,getOwner());
        if(kind()==TalismanKind.FIRE) { if(target.hurt(typedDamage(net.minecraft.world.damagesource.DamageTypes.IN_FIRE),enhanced?10:8))target.setSecondsOnFire(enhanced?6:5); }
        else if(kind()==TalismanKind.ICE) { if(target.hurt(damage,enhanced?8:6)) { target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,enhanced?160:120,3));target.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN,enhanced?160:120,1));target.setTicksFrozen(Math.min(target.getTicksRequiredToFreeze(),target.getTicksFrozen()+140)); } }
        else if(kind()==TalismanKind.THUNDER && target instanceof Enemy) {
            boolean bonus=target.getMobType()==MobType.UNDEAD||target.getMobType()==MobType.WATER||level().isRainingAt(target.blockPosition());
            target.hurt(typedDamage(net.minecraft.world.damagesource.DamageTypes.LIGHTNING_BOLT),(enhanced?18:14)*(bonus?1.5f:1));
        }
    }
    void impact(Vec3 center,LivingEntity direct) {
        if(level().isClientSide)return;
        if(kind()==TalismanKind.THUNDER) {
            var bolt=EntityType.LIGHTNING_BOLT.create(level());if(bolt!=null){bolt.moveTo(center);bolt.setVisualOnly(true);level().addFreshEntity(bolt);}
            for(var target:level().getEntitiesOfClass(LivingEntity.class,new AABB(center,center).inflate(3),e->e.distanceToSqr(center)<=9))damageTarget(target);
        } else if(direct!=null)damageTarget(direct);
        else if(kind()==TalismanKind.FIRE)for(var target:level().getEntitiesOfClass(LivingEntity.class,new AABB(center,center).inflate(1.5),e->e.distanceToSqr(center)<=2.25))damageTarget(target);
        ((ServerLevel)level()).sendParticles(kind()==TalismanKind.ICE?ParticleTypes.SNOWFLAKE:ParticleTypes.CRIT,center.x,center.y,center.z,20,.4,.4,.4,.05);
        discard();
    }
    @Override protected void onHit(HitResult hit) { super.onHit(hit);if(!level().isClientSide)impact(hit.getLocation(),hit instanceof EntityHitResult entity && entity.getEntity() instanceof LivingEntity living?living:null); }
}
