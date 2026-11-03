package dev.xiuxian;

import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.particles.ParticleTypes;

/** A visual projection of the held sword, never an extra inventory item. */
public class FlyingSword extends ThrowableItemProjectile {
    LivingEntity target;
    String art="drive";
    boolean returning;
    boolean returnDamage;
    int hits;
    int nextHit;
    double power=1;
    public FlyingSword(EntityType<? extends ThrowableItemProjectile> type,Level level){super(type,level);setNoGravity(true);}
    @Override protected Item getDefaultItem(){return net.minecraft.world.item.Items.IRON_SWORD;}
    @Override public void tick(){
        if(level().isClientSide){super.tick();return;}
        if(!(getOwner() instanceof ServerPlayer owner)||!SwordCombat.swordReady(owner)||!owner.getMainHandItem().is(getItem().getItem())||!SwordCombat.isCurrent(owner,this)||tickCount>240||distanceToSqr(owner)>1600){discard();return;}
        if(art.equals("guard")){
            if(tickCount>100){discard();return;}
            if(tickCount%20==0&&!CultivationFoundation.spendMana(owner,4)){discard();return;}
            tickCount++;double angle=tickCount*.25;setPos(owner.getX()+Math.cos(angle)*1.2,owner.getY()+1,owner.getZ()+Math.sin(angle)*1.2);return;
        }
        if(target==null||!target.isAlive()||distanceToSqr(target)>1600)returning=true;
        if(returning){
            Vec3 to=owner.getEyePosition().subtract(position());
            if(to.length()<1){discard();return;}
            setDeltaMovement(to.normalize().scale(1.1));
        }else{
            Vec3 to=target.getBoundingBox().getCenter().subtract(position());
            if(!art.equals("pierce") || tickCount==0) setDeltaMovement(to.normalize().scale(SwordCombat.flightSpeed(owner,art)));
            if(art.equals("pierce") && tickCount>25) returning=true;
        }
        super.tick();
        if(tickCount%3==0)((net.minecraft.server.level.ServerLevel)level()).sendParticles(ParticleTypes.CRIT,getX(),getY(),getZ(),1,0,0,0,0);
    }
    @Override protected void onHitEntity(EntityHitResult hit){
        if(!(getOwner() instanceof ServerPlayer owner)||!(hit.getEntity() instanceof LivingEntity victim)||!SwordCombat.validTarget(owner,victim))return;
        if(returning&&!returnDamage)return;
        if(tickCount<nextHit)return;
        if(!returning&&victim!=target&&!art.equals("pierce"))return;
        if(returning){returnDamage=false;}else hits++;
        SwordCombat.strike(owner,victim,art,power);
        nextHit=tickCount+12;
        if(!returning&&(art.equals("combo")||art.equals("shadows"))&&hits<3){setPos(position().add(owner.getLookAngle().scale(-1.5)));return;}
        returning=true;
    }
    @Override protected void onHitBlock(BlockHitResult hit){returning=true;returnDamage=false;}
}
