package dev.xiuxian;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.minecraft.world.effect.*;
public class FrostSilk extends ThrowableItemProjectile {
    public FrostSilk(EntityType<? extends ThrowableItemProjectile> type,Level level){super(type,level);setNoGravity(true);}
    @Override protected Item getDefaultItem(){return Items.SNOWBALL;}
    @Override public boolean isPickable(){return true;}
    @Override public boolean hurt(net.minecraft.world.damagesource.DamageSource source,float amount){if(!level().isClientSide)discard();return true;}
    @Override public void tick(){super.tick();if(tickCount>80)discard();}
    @Override protected void onHitEntity(EntityHitResult hit){if(!level().isClientSide&&hit.getEntity() instanceof LivingEntity living&&living!=getOwner()){living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,80,2));discard();}}
    @Override protected void onHitBlock(BlockHitResult hit){if(!level().isClientSide)discard();}
}
