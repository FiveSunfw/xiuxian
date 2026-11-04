package dev.xiuxian;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.navigation.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.effect.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.item.Items;
import java.util.EnumSet;

public class SpiritBeast extends Monster {
    private static final EntityDataAccessor<Integer> PHASE=SynchedEntityData.defineId(SpiritBeast.class,EntityDataSerializers.INT);
    public final BeastKind kind;
    private int cooldown=40,windup,recovery;
    private Vec3 aimed=Vec3.ZERO;
    private boolean halfHealthAttack;
    private int retreatTicks;
    private int venomCooldown;
    private boolean charging;
    private net.minecraft.core.BlockPos home;
    private Vec3 retreatWater;
    public SpiritBeast(EntityType<? extends Monster> type,Level level,BeastKind kind){super(type,level);this.kind=kind;if(kind==BeastKind.SPIDER)navigation=new WallClimberNavigation(this,level);xpReward=kind.ordinal()+2;if(kind==BeastKind.EAGLE){moveControl=new FlyingMoveControl(this,20,true);navigation=new FlyingPathNavigation(this,level);setNoGravity(true);}}
    @Override protected void defineSynchedData(){super.defineSynchedData();entityData.define(PHASE,0);}
    public int phase(){return entityData.get(PHASE);}
    @Override protected void registerGoals(){
        goalSelector.addGoal(0,new FloatGoal(this));
        goalSelector.addGoal(1,new AvoidEntityGoal<>(this,Player.class,7,1.2,1.5,p->kind==BeastKind.RABBIT&&getTarget()==null));
        goalSelector.addGoal(2,new Goal(){
            {setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));}
            @Override public boolean canUse(){return getTarget()!=null&&getTarget().isAlive();}
            @Override public void tick(){var target=getTarget();if(target==null)return;getLookControl().setLookAt(target,30,30);if(windup>0||recovery>0){getNavigation().stop();if(kind==BeastKind.EAGLE&&recovery>0)getMoveControl().setWantedPosition(getX(),target.getY(),getZ(),.6);return;}if(kind==BeastKind.EAGLE)getMoveControl().setWantedPosition(target.getX(),target.getY()+3,target.getZ(),1);else getNavigation().moveTo(target,1.1);}
        });
        goalSelector.addGoal(6,new RandomStrollGoal(this,.8));goalSelector.addGoal(7,new LookAtPlayerGoal(this,Player.class,8));goalSelector.addGoal(8,new RandomLookAroundGoal(this));
        targetSelector.addGoal(1,new HurtByTargetGoal(this));
        targetSelector.addGoal(2,new NearestAttackableTargetGoal<>(this,Player.class,10,true,false,p->kind!=BeastKind.RABBIT&&(kind!=BeastKind.EAGLE||home==null||home.distToCenterSqr(p.position())<900)));
    }
    @Override public void tick(){
        super.tick();if(level().isClientSide)return;
        if(venomCooldown>0)venomCooldown--;
        if(home==null)home=blockPosition();
        if(kind==BeastKind.EAGLE)setNoGravity(recovery<=0);
        var target=getTarget();
        if(kind==BeastKind.EAGLE&&home!=null&&target!=null&&home.distToCenterSqr(target.position())>900){setTarget(null);target=null;}
        if(kind==BeastKind.EAGLE&&home!=null&&target==null&&home.distToCenterSqr(position())>64)getMoveControl().setWantedPosition(home.getX()+.5,home.getY()+3,home.getZ()+.5,1);
        if(target==null||!target.isAlive()||distanceToSqr(target)>900){setTarget(null);windup=0;entityData.set(PHASE,0);return;}
        if(kind==BeastKind.PYTHON&&getHealth()<getMaxHealth()/2&&!halfHealthAttack){halfHealthAttack=true;retreatTicks=60;windup=0;aimed=target.position();
            double nearest=Double.MAX_VALUE;
            for(int x=-6;x<=6;x++)for(int z=-6;z<=6;z++)for(int y=-2;y<=0;y++){
                var water=blockPosition().offset(x,y,z);if(level().getFluidState(water).is(net.minecraft.tags.FluidTags.WATER)&&water.distToCenterSqr(position())<nearest){nearest=water.distToCenterSqr(position());retreatWater=water.getCenter();}
            }}
        if(retreatTicks>0){
            entityData.set(PHASE,3);retreatTicks--;
            if(retreatTicks%10==0)((net.minecraft.server.level.ServerLevel)level()).sendParticles(ParticleTypes.SPLASH,aimed.x,aimed.y,aimed.z,12,1,.1,1,.05);
            if(retreatTicks>25){var direction=retreatWater==null?position().subtract(target.position()):retreatWater.subtract(position());setDeltaMovement(new Vec3(direction.x,0,direction.z).normalize().scale(.2).add(0,isInWater()?-.1:0,0));}
            if(retreatTicks==0){aimed=target.position();windup=24;}
            return;
        }
        if(recovery>0){
            if(charging&&horizontalCollision){charging=false;setDeltaMovement(Vec3.ZERO);recovery=40;}
            if(kind==BeastKind.KING&&recovery==15){
                ((net.minecraft.server.level.ServerLevel)level()).sendParticles(ParticleTypes.CLOUD,getX(),getY()+.1,getZ(),20,2,.1,2,.02);
                for(var victim:level().getEntitiesOfClass(Player.class,getBoundingBox().inflate(3.5),p->p.onGround()&&hasLineOfSight(p)))victim.hurt(damageSources().mobAttack(this),6);
            }
            recovery--;entityData.set(PHASE,2);return;
        }
        if(windup>0){
            entityData.set(PHASE,1);
            if(windup%5==0)((net.minecraft.server.level.ServerLevel)level()).sendParticles(kind==BeastKind.LIZARD?ParticleTypes.ELECTRIC_SPARK:kind==BeastKind.SPIDER?ParticleTypes.SNOWFLAKE:ParticleTypes.CRIT,getX(),getY()+getBbHeight(),getZ(),3,.2,.1,.2,.01);
            if(--windup==0)releaseAttack(target);return;
        }
        entityData.set(PHASE,0);
        if(cooldown>0)cooldown--;
        double range=kind==BeastKind.SPIDER?12:kind==BeastKind.EAGLE?10:kind==BeastKind.LIZARD?6:kind==BeastKind.BOAR||kind==BeastKind.KING?8:kind==BeastKind.PYTHON?5:2.4;
        if(cooldown==0&&distanceToSqr(target)<=range*range&&hasLineOfSight(target)){
            aimed=target.position();windup=kind==BeastKind.VIPER?(venomCooldown==0?24:8):kind==BeastKind.KING||kind==BeastKind.LIZARD?30:kind==BeastKind.BOAR||kind==BeastKind.PYTHON?24:16;getNavigation().stop();playSound(kind==BeastKind.EAGLE?net.minecraft.sounds.SoundEvents.PHANTOM_SWOOP:kind==BeastKind.LIZARD?net.minecraft.sounds.SoundEvents.AMETHYST_BLOCK_CHIME:net.minecraft.sounds.SoundEvents.WOLF_GROWL,.6f,kind==BeastKind.KING?.6f:1f);
        }

    }
    void releaseAttack(LivingEntity target){
        cooldown=kind==BeastKind.RABBIT?80:kind==BeastKind.VIPER?25:60;recovery=kind==BeastKind.EAGLE?40:kind==BeastKind.BOAR||kind==BeastKind.KING?30:15;
        if(kind==BeastKind.SPIDER){var silk=new FrostSilk(Beasts.SILK.get(),level());silk.setOwner(this);silk.setPos(getX(),getEyeY(),getZ());var direction=aimed.add(0,.5,0).subtract(silk.position());silk.shoot(direction.x,direction.y,direction.z,.7f,0);level().addFreshEntity(silk);return;}
        if(kind==BeastKind.LIZARD){for(var victim:level().getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(6),e->e instanceof Player&&e.isAlive()&&hasLineOfSight(e))){if(victim.position().subtract(position()).normalize().dot(getLookAngle())>.35)strike(victim);}return;}
        if(kind==BeastKind.BOAR||kind==BeastKind.KING||kind==BeastKind.WOLF||kind==BeastKind.EAGLE||kind==BeastKind.PYTHON){
            charging=true;
            var direction=aimed.subtract(position());if(kind!=BeastKind.EAGLE)direction=new Vec3(direction.x,0,direction.z);setDeltaMovement(direction.normalize().scale(kind==BeastKind.EAGLE?1.15:kind==BeastKind.WOLF?.8:1.05).add(0,kind==BeastKind.WOLF?.2:0,0));hurtMarked=true;

        }
        if(distanceToSqr(target)<Math.pow(getBbWidth()+target.getBbWidth()+1.2,2)&&hasLineOfSight(target))strike(target);
        if(kind==BeastKind.RABBIT){setTarget(null);}
    }
    @Override public void aiStep(){super.aiStep();if(!level().isClientSide&&charging&&recovery>0&&getDeltaMovement().horizontalDistanceSqr()>.08){var target=getTarget();if(target!=null&&distanceToSqr(target)<Math.pow((getBbWidth()+target.getBbWidth())*.5+.7,2)&&hasLineOfSight(target)){strike(target);charging=false;setDeltaMovement(Vec3.ZERO);}}}
    private void strike(LivingEntity target){
        if(target.invulnerableTime>0)return;
        if(target.hurt(damageSources().mobAttack(this),(float)getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE))&&kind==BeastKind.VIPER&&venomCooldown==0){target.addEffect(new MobEffectInstance(MobEffects.POISON,80,0));venomCooldown=100;}
    }
    public static boolean nearWater(net.minecraft.world.level.ServerLevelAccessor level,net.minecraft.core.BlockPos pos){
        for(int x=-4;x<=4;x++)for(int z=-4;z<=4;z++)if(level.getFluidState(pos.offset(x,-1,z)).is(net.minecraft.tags.FluidTags.WATER)||level.getFluidState(pos.offset(x,0,z)).is(net.minecraft.tags.FluidTags.WATER))return true;
        return false;
    }
    @Override protected net.minecraft.sounds.SoundEvent getAmbientSound(){return kind==BeastKind.RABBIT?net.minecraft.sounds.SoundEvents.RABBIT_AMBIENT:kind==BeastKind.WOLF?net.minecraft.sounds.SoundEvents.WOLF_GROWL:kind==BeastKind.EAGLE?net.minecraft.sounds.SoundEvents.PARROT_AMBIENT:kind==BeastKind.BOAR||kind==BeastKind.KING?net.minecraft.sounds.SoundEvents.PIG_AMBIENT:net.minecraft.sounds.SoundEvents.SPIDER_AMBIENT;}
    @Override protected net.minecraft.sounds.SoundEvent getHurtSound(DamageSource source){return net.minecraft.sounds.SoundEvents.GENERIC_HURT;}
    @Override protected net.minecraft.sounds.SoundEvent getDeathSound(){return net.minecraft.sounds.SoundEvents.GENERIC_DEATH;}
    @Override public boolean removeWhenFarAway(double distance){return kind!=BeastKind.KING&&kind!=BeastKind.PYTHON;}
    @Override public boolean onClimbable(){return kind==BeastKind.SPIDER&&horizontalCollision;}
    @Override public boolean canBreatheUnderwater(){return kind==BeastKind.PYTHON||kind==BeastKind.VIPER;}
    @Override public int getMaxSpawnClusterSize(){return kind==BeastKind.WOLF?2:1;}
    @Override protected float getDamageAfterArmorAbsorb(DamageSource source,float amount){
        if((kind==BeastKind.BOAR||kind==BeastKind.KING)&&source.getEntity()!=null){
            var direction=source.getEntity().position().subtract(position()).normalize();
            if(recovery>0||direction.dot(getLookAngle())<.3)return amount;
        }
        return super.getDamageAfterArmorAbsorb(source,amount);
    }
    @Override protected void dropCustomDeathLoot(DamageSource source,int looting,boolean recentlyHit){
        super.dropCustomDeathLoot(source,looting,recentlyHit);
        switch(kind){
            case RABBIT->{spawnAtLocation(Items.RABBIT);drop("spirit_horn",1);}
            case WOLF->{drop("wolf_pelt",1);drop("wolf_brush_hair",1+random.nextInt(2));chance("azure_fang",.5f);}
            case BOAR->{spawnAtLocation(new net.minecraft.world.item.ItemStack(Items.PORKCHOP,1+random.nextInt(2)));drop("rockhide_plate",1);chance("curved_tusk",.35f);}
            case VIPER->{drop("viper_fang",1);chance("venom_sac",.4f);}
            case SPIDER->{drop("frost_silk",1+random.nextInt(2));chance("frost_gland",.35f);}
            case EAGLE->{drop("wind_feather",1+random.nextInt(2));chance("eagle_talon",.35f);}
            case LIZARD->{drop("thunder_scale",1+random.nextInt(2));chance("charged_spine",.3f);}
            case KING->{drop("thick_rock_plate",2);drop("king_tusk",1);}
            case PYTHON->{drop("jade_scale",2+random.nextInt(2));drop("python_sinew",1);drop("beast_marrow",1);}
        }
    }
    private void drop(String id,int count){spawnAtLocation(new net.minecraft.world.item.ItemStack(Beasts.material(id),count));}
    private void chance(String id,float chance){if(random.nextFloat()<chance)drop(id,1);}
    @Override public void addAdditionalSaveData(CompoundTag tag){super.addAdditionalSaveData(tag);tag.putBoolean("HalfHealthAttack",halfHealthAttack);if(home!=null)tag.putLong("BeastHome",home.asLong());}
    @Override public void readAdditionalSaveData(CompoundTag tag){super.readAdditionalSaveData(tag);halfHealthAttack=tag.getBoolean("HalfHealthAttack");if(tag.contains("BeastHome"))home=net.minecraft.core.BlockPos.of(tag.getLong("BeastHome"));}
}
