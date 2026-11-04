package dev.xiuxian;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.common.ForgeHooks;
import java.util.Set;

@Mod.EventBusSubscriber(modid=Xiuxian.ID)
public final class TalismanEffects {
    private static final String SHIELD_LEFT="xiuxian_talisman_shield_left", SHIELD_TOTAL="xiuxian_talisman_shield_total";
    private static final Set<Block> ROCKS=Set.of(Blocks.STONE,Blocks.DEEPSLATE,Blocks.COBBLESTONE,Blocks.TUFF,Blocks.DIRT,Blocks.GRAVEL);
    static class Marker extends MobEffect { Marker(int color) { super(MobEffectCategory.BENEFICIAL,color); } }
    static class Shield extends Marker {
        Shield() { super(0xdab85b); }
        @Override public void removeAttributeModifiers(LivingEntity entity,AttributeMap attributes,int amplifier) { clearShield(entity);super.removeAttributeModifiers(entity,attributes,amplifier); }
    }
    /** Keep only the unspent contribution, so expiration does not remove another absorption source. */
    static void reconcileShield(LivingEntity entity) {
        var tag=entity.getPersistentData();float left=tag.getFloat(SHIELD_LEFT);
        if(left<=0)return;
        float loss=Math.max(0,tag.getFloat(SHIELD_TOTAL)-entity.getAbsorptionAmount());
        tag.putFloat(SHIELD_LEFT,Math.max(0,left-loss));tag.putFloat(SHIELD_TOTAL,entity.getAbsorptionAmount());
    }
    static void clearShield(LivingEntity entity) {
        var tag=entity.getPersistentData();entity.setAbsorptionAmount(Math.max(0,entity.getAbsorptionAmount()-tag.getFloat(SHIELD_LEFT)));tag.remove(SHIELD_LEFT);tag.remove(SHIELD_TOTAL);
    }
    static void shield(LivingEntity player,int points,int duration) {
        clearShield(player);player.addEffect(new MobEffectInstance(Talismans.SHIELD.get(),duration,0));player.setAbsorptionAmount(player.getAbsorptionAmount()+points);
        player.getPersistentData().putFloat(SHIELD_LEFT,points);player.getPersistentData().putFloat(SHIELD_TOTAL,player.getAbsorptionAmount());
    }
    public static void activate(ServerPlayer player,TalismanKind kind,boolean enhanced) {
        int travel=enhanced?6000:3600;
        switch(kind) {
            case ARMOR->{int duration=enhanced?1800:1200;shield(player,enhanced?14:10,duration);effect(player,MobEffects.DAMAGE_RESISTANCE,duration,0);}
            case HEAL->{player.heal(enhanced?8:6);effect(player,MobEffects.REGENERATION,enhanced?300:200,1);player.removeEffect(MobEffects.POISON);player.removeEffect(MobEffects.WITHER);}
            case SPEED->{effect(player,Talismans.SPEED.get(),travel,0);effect(player,MobEffects.JUMP,travel,0);}
            case SIGHT->{effect(player,Talismans.SIGHT.get(),3600,enhanced?1:0);effect(player,MobEffects.NIGHT_VISION,3600,0);}
            case WATER->{effect(player,MobEffects.WATER_BREATHING,travel,0);effect(player,MobEffects.DOLPHINS_GRACE,travel,0);effect(player,MobEffects.NIGHT_VISION,travel,0);}
            default->{}
        }
        player.serverLevel().sendParticles(ParticleTypes.ENCHANT,player.getX(),player.getY()+1,player.getZ(),20,.5,.6,.5,.1);
    }
    private static void effect(LivingEntity player,MobEffect effect,int ticks,int amplifier) { player.addEffect(new MobEffectInstance(effect,ticks,amplifier)); }
    static boolean protectedRock(ServerLevel level,BlockPos pos) {
        if(!level.hasChunkAt(pos))return true;
        var state=level.getBlockState(pos);
        return !ROCKS.contains(state.getBlock())||state.getDestroySpeed(level,pos)<0||state.hasBlockEntity()||level.getBlockEntity(pos)!=null;
    }
    static boolean breakRocks(ServerPlayer player,BlockPos center) {
        var level=player.serverLevel();boolean any=false;
        for(BlockPos cursor:BlockPos.betweenClosed(center.offset(-1,-1,-1),center.offset(1,1,1))) {
            BlockPos pos=cursor.immutable();
            if(protectedRock(level,pos)||!level.mayInteract(player,pos)||!player.mayBuild())continue;
            if(ForgeHooks.onBlockBreakEvent(level,player.gameMode.getGameModeForPlayer(),player,pos)==-1)continue;
            any|=level.destroyBlock(pos,true,player);
        }
        return any;
    }
    static String direction(BlockPos player,BlockPos target) {
        double angle=Math.atan2(target.getX()-player.getX(),-(target.getZ()-player.getZ()));
        String[] names={"north","northeast","east","southeast","south","southwest","west","northwest"};
        return names[Math.floorMod((int)Math.round(angle/(Math.PI/4)),8)];
    }
    static void scan(ServerPlayer player,int radius) {
        ServerLevel level=player.serverLevel();double rangeSquared=radius*radius;
        for(var monster:level.getEntitiesOfClass(LivingEntity.class,player.getBoundingBox().inflate(radius),e->e instanceof net.minecraft.world.entity.monster.Enemy && e.isAlive() && e.distanceToSqr(player)<=rangeSquared)) effect(monster,MobEffects.GLOWING,60,0);
        BlockPos core=SpiritVeins.get(level).nearestLoadedCore(level,player.position(),radius);
        if(core!=null)player.displayClientMessage(Component.translatable("talisman.xiuxian.core",Component.translatable("talisman.xiuxian.direction."+direction(player.blockPosition(),core)),Component.translatable("talisman.xiuxian."+(core.getY()>player.getY()+2?"above":core.getY()<player.getY()-2?"below":"level")),String.format(java.util.Locale.ROOT,"%.1f",Math.sqrt(core.getCenter().distanceToSqr(player.position())))),true);
    }
    @SubscribeEvent(priority=net.minecraftforge.eventbus.api.EventPriority.LOWEST)
    public static void beforeDamage(net.minecraftforge.event.entity.living.LivingHurtEvent event) {
        var entity=event.getEntity();if(entity.getPersistentData().getFloat(SHIELD_LEFT)>0)
            entity.getPersistentData().putFloat(SHIELD_TOTAL,entity.getAbsorptionAmount());
    }
    @SubscribeEvent(priority=net.minecraftforge.eventbus.api.EventPriority.HIGHEST)
    public static void afterAbsorption(net.minecraftforge.event.entity.living.LivingDamageEvent event) {
        // Forge fires this after absorption has been spent, including fully absorbed hits.
        reconcileShield(event.getEntity());
    }
    @SubscribeEvent public static void tick(TickEvent.PlayerTickEvent event) {
        if(event.phase!=TickEvent.Phase.END||!(event.player instanceof ServerPlayer player))return;
        if(!player.isAlive()||player.isSpectator())return;
        var sight=player.getEffect(Talismans.SIGHT.get());if(sight!=null&&player.tickCount%40==0)scan(player,sight.getAmplifier()>0?32:24);
        if(player.hasEffect(Talismans.SHIELD.get())&&player.tickCount%10==0)player.serverLevel().sendParticles(ParticleTypes.END_ROD,player.getX(),player.getY()+1,player.getZ(),2,.4,.5,.4,0);
    }
}
