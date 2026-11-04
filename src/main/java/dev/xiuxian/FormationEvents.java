package dev.xiuxian;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.damagesource.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.*;
import net.minecraftforge.event.entity.*;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=Xiuxian.ID)
public final class FormationEvents {
    private static final Map<net.minecraft.server.level.ServerPlayer,Double> PLAYER_MULTIPLIERS=new WeakHashMap<>();
    @SubscribeEvent public static void playerMove(TickEvent.PlayerTickEvent event) {
        if(event.phase!=TickEvent.Phase.END||!(event.player instanceof net.minecraft.server.level.ServerPlayer player))return;
        double multiplier=Formations.gathering(player.serverLevel(),player.blockPosition());Double previous=PLAYER_MULTIPLIERS.put(player,multiplier);
        if(previous==null||Double.compare(previous,multiplier)!=0)Cultivation.sync(player);
        if(player.tickCount%50==0)guardianRecovery(player);
    }

    static boolean guardianRecovery(net.minecraft.server.level.ServerPlayer player) {
        if(!player.isAlive()||FormationIndex.get(player.serverLevel()).at(player.blockPosition(),FormationKind.GUARDIAN).isEmpty())return false;
        var effect=player.getEffect(net.minecraft.world.effect.MobEffects.REGENERATION);
        // A constantly refreshed 40-tick vanilla regeneration effect never reaches its 50-tick pulse.
        // Supply the same 1 HP / 50 ticks cadence; stronger or long-lived external regeneration takes precedence.
        if(effect==null||effect.getAmplifier()==0&&effect.getDuration()<=40){player.heal(1);return true;}return false;
    }
    @SubscribeEvent public static void levelUnload(LevelEvent.Unload event) { if(event.getLevel() instanceof ServerLevel server)FormationIndex.unload(server); }
    @SubscribeEvent public static void chunkLoad(ChunkEvent.Load event) { if(event.getLevel() instanceof ServerLevel server)FormationIndex.get(server).chunkChanged(event.getChunk().getPos(),true); }
    @SubscribeEvent public static void chunkUnload(ChunkEvent.Unload event) { if(event.getLevel() instanceof ServerLevel server)FormationIndex.get(server).chunkChanged(event.getChunk().getPos(),false); }
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void placed(BlockEvent.EntityPlaceEvent event) { if(event.getLevel() instanceof ServerLevel server)FormationIndex.get(server).plantChanged(event.getPos(),false); }
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void broken(BlockEvent.BreakEvent event) { if(event.getLevel() instanceof ServerLevel server)FormationIndex.get(server).plantChanged(event.getPos(),true); }
    @SubscribeEvent public static void tick(TickEvent.LevelTickEvent event) {
        if(event.phase!=TickEvent.Phase.END||!(event.level instanceof ServerLevel level))return;
        var index=FormationIndex.get(level);long now=level.getGameTime();var usedPlants=new HashSet<BlockPos>();
        for(var plate:index.plates()) {
            if(!plate.active()){plate.showActive();continue;}
            if(now%20==0) {
                if(plate.kind()==FormationKind.GATHERING)plate.growPlants(usedPlants);
                else if(plate.kind()==FormationKind.GUARDIAN)plate.guardPulse();
                else plate.assaultSlow();
            }
            if(now%30==0&&plate.kind()==FormationKind.ASSAULT)plate.attackPulse();
            if(now%40==0)plate.particles();
            plate.showActive();
        }
        if(now%20==0)gatheringRecovery(level);
        index.scanPlants();
    }
    static void gatheringRecovery(ServerLevel level) {
        for(var player:level.players())if(player.isAlive()&&!player.isSpectator()&&Cultivation.initiated(player)&&Formations.gathering(level,player.blockPosition())>1)CultivationFoundation.restoreMana(player,3);
    }
    static boolean blocksNatural(ServerLevel level,BlockPos pos,MobSpawnType source,LivingEntity entity) {
        return source==MobSpawnType.NATURAL&&entity instanceof Enemy&&!FormationIndex.get(level).at(pos,FormationKind.GUARDIAN).isEmpty();
    }
    @SubscribeEvent public static void spawn(MobSpawnEvent.PositionCheck event) {
        if(blocksNatural(event.getLevel().getLevel(),event.getEntity().blockPosition(),event.getSpawnType(),event.getEntity()))event.setResult(Event.Result.DENY);
    }
    static boolean protectBlock(ServerLevel level,BlockPos pos) {
        for(var plate:FormationIndex.get(level).at(pos,FormationKind.GUARDIAN))if(plate.spend(5))return true;
        return false;
    }
    static void protectExplosion(ServerLevel level,List<BlockPos> affected) {
        Set<FormationEntity> paid=new HashSet<>(),failed=new HashSet<>();
        affected.removeIf(pos->{
            for(var plate:FormationIndex.get(level).at(pos,FormationKind.GUARDIAN)){
                if(paid.contains(plate))return true;
                if(!failed.contains(plate)){if(plate.spend(5)){paid.add(plate);return true;}failed.add(plate);}
            }
            return false;
        });
    }
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void explosion(ExplosionEvent.Detonate event) { if(event.getLevel() instanceof ServerLevel server)protectExplosion(server,event.getAffectedBlocks()); }
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void destroy(LivingDestroyBlockEvent event) {
        if(event.getEntity() instanceof Enemy&&event.getEntity().level() instanceof ServerLevel level&&protectBlock(level,event.getPos()))event.setCanceled(true);
    }
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void enderman(EntityMobGriefingEvent event) {
        // Vanilla enderman pickup has only an entity-position griefing hook, not a target-block event.
        if(event.getEntity() instanceof EnderMan&&event.getEntity().level() instanceof ServerLevel level&&event.getResult()!=Event.Result.DENY
            &&(event.getResult()==Event.Result.ALLOW||level.getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_MOBGRIEFING))) {
            // Include the vanilla pickup reach so a mob just outside cannot grab a block inside.
            for(var plate:FormationIndex.get(level).nearby(event.getEntity().blockPosition(),18))
                if(plate.kind()==FormationKind.GUARDIAN&&plate.scale()>0&&plate.bounds().inflate(2).contains(event.getEntity().position())&&plate.active()&&plate.spend(5)){event.setResult(Event.Result.DENY);break;}
        }
    }
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void endermanPlace(BlockEvent.EntityPlaceEvent event) {
        if(event.getEntity() instanceof EnderMan&&event.getLevel() instanceof ServerLevel level&&protectBlock(level,event.getPos()))event.setCanceled(true);
    }
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void damage(LivingHurtEvent event) {
        if(!(event.getEntity().level() instanceof ServerLevel level)||!(event.getEntity() instanceof Enemy))return;
        var source=event.getSource();if(source.is(Formations.DAMAGE))return;
        boolean supported=source.is(DamageTypes.PLAYER_ATTACK)&&source.getDirectEntity() instanceof Player||source.getDirectEntity() instanceof TalismanProjectile;
        if(!supported||!(source.getEntity() instanceof Player))return;
        if(FormationIndex.get(level).at(event.getEntity().blockPosition(),FormationKind.ASSAULT).stream().anyMatch(plate->plate.hostile(event.getEntity())))event.setAmount(event.getAmount()*1.25f);
    }
}
