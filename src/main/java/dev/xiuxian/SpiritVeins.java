package dev.xiuxian;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.*;

@Mod.EventBusSubscriber(modid = Xiuxian.ID)
public final class SpiritVeins extends SavedData {
    private final Map<Long, Map<BlockPos, Core>> chunks = new HashMap<>();
    static final class Core {
        final BlockPos pos; final int tier; final int surface;
        final Map<BlockPos, Integer> ores = new HashMap<>();
        Core(BlockPos pos, int tier, int surface) { this.pos=pos; this.tier=tier; this.surface=surface; }
    }
    static SpiritVeins get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(SpiritVeins::load, SpiritVeins::new, "xiuxian_spirit_veins");
    }
    void add(Core core) {
        chunks.computeIfAbsent(ChunkPos.asLong(core.pos), k -> new HashMap<>()).putIfAbsent(core.pos, core);
        setDirty();
    }
    void remove(BlockPos pos) {
        var bucket=chunks.get(ChunkPos.asLong(pos));
        if(bucket!=null) { bucket.remove(pos); if(bucket.isEmpty()) chunks.remove(ChunkPos.asLong(pos)); setDirty(); }
    }
    private List<Core> nearby(BlockPos pos) { return nearby(pos, 0); }
    private List<Core> nearby(BlockPos pos, int extraRadius) {
        List<Core> result=new ArrayList<>(); int cx=pos.getX()>>4, cz=pos.getZ()>>4;
        int range=(Collections.max(Cultivation.VEIN_RADII.stream().map(v -> v.get()).toList())+Math.max(0, extraRadius)+15)/16;
        for(int x=cx-range;x<=cx+range;x++) for(int z=cz-range;z<=cz+range;z++) {
            var bucket=chunks.get(ChunkPos.asLong(x,z)); if(bucket!=null) result.addAll(bucket.values());
        }
        return result;
    }
    BlockPos nearestLoadedCore(ServerLevel level, net.minecraft.world.phys.Vec3 origin, int radius) {
        BlockPos best=null;double distance=radius*radius;
        // Search index buckets using the requested detection radius, independent of aura settings.
        int chunksRadius=(radius+15)/16;BlockPos at=BlockPos.containing(origin);
        for(int x=(at.getX()>>4)-chunksRadius;x<=(at.getX()>>4)+chunksRadius;x++)
            for(int z=(at.getZ()>>4)-chunksRadius;z<=(at.getZ()>>4)+chunksRadius;z++) {
                var bucket=chunks.get(ChunkPos.asLong(x,z));if(bucket==null)continue;
                for(Core core:bucket.values()) {
                    if(!level.hasChunkAt(core.pos)||!level.getBlockState(core.pos).is(VeinBlocks.CORE.get()))continue;
                    double d=core.pos.getCenter().distanceToSqr(origin);if(d<=distance){distance=d;best=core.pos;}
                }
            }
        return best;
    }
    double multiplier(Player player) {
        int extra = player.hasEffect(SpiritualFarming.STARLIGHT.get()) ? 8 : 0;
        return multiplierAt(player.getX(), player.getY(), player.getZ(), extra);
    }
    double multiplierAt(BlockPos pos) { return multiplierAt(pos.getX()+.5, pos.getY(), pos.getZ()+.5, 0); }
    private double multiplierAt(double x, double y, double z, int extraRadius) {
        double best=1;
        for(Core core:nearby(BlockPos.containing(x,y,z), extraRadius)) {
            if(y<core.pos.getY() || y>core.surface+16) continue;
            double distance=Math.hypot(x-core.pos.getX()-.5,z-core.pos.getZ()-.5);
            double fraction=Math.max(0,1-distance/(Cultivation.VEIN_RADII.get(core.tier-1).get()+extraRadius));
            best=Math.max(best,1+(Cultivation.VEIN_MULTIPLIERS.get(core.tier-1).get()-1)*fraction);
        }
        return best;
    }
    boolean deplete(BlockPos pos) {
        for(Core core:nearby(pos)) if(core.ores.containsKey(pos)) { core.ores.put(pos,0); setDirty(); return true; }
        return false;
    }
    void forget(BlockPos pos) {
        for(Core core:nearby(pos)) if(core.ores.remove(pos)!=null) setDirty();
    }
    @SubscribeEvent public static void tick(TickEvent.LevelTickEvent event) {
        if(event.phase!=TickEvent.Phase.END || !(event.level instanceof ServerLevel level) || level.getGameTime()%20!=0) return;
        get(level).regenerate(level);
    }
    void regenerate(ServerLevel level) {
        SpiritVeins data=this;
        for(var bucket:data.chunks.values()) for(Core core:bucket.values()) {
            if(level.hasChunkAt(core.pos) && !level.getBlockState(core.pos).is(VeinBlocks.CORE.get())) continue;
            var iterator=core.ores.entrySet().iterator();
            while(iterator.hasNext()) {
                var ore=iterator.next(); BlockPos pos=ore.getKey();
                if(!level.hasChunkAt(pos)) continue;
                var state=level.getBlockState(pos);
                if(state.is(VeinBlocks.DEPLETED.get()) || state.is(VeinBlocks.DEEP_DEPLETED.get())) {
                    int seconds=ore.getValue()+1+(Formations.gathering(level,pos)>1?1:0);
                    if(seconds>=Cultivation.VEIN_REGEN_SECONDS.get()) {
                        level.setBlockAndUpdate(pos,(state.is(VeinBlocks.DEEP_DEPLETED.get())?CultivationResources.DEEP_ORE:CultivationResources.ORE).get().defaultBlockState()); seconds=0;
                    }
                    ore.setValue(seconds); data.setDirty();
                } else if(!state.is(CultivationResources.ORE.get()) && !state.is(CultivationResources.DEEP_ORE.get())) { iterator.remove(); data.setDirty(); }
            }
        }
    }
    static SpiritVeins load(CompoundTag tag) {
        SpiritVeins data=new SpiritVeins();
        for(Tag t:tag.getList("cores",10)) {
            CompoundTag c=(CompoundTag)t; int tier=c.getInt("tier"); if(tier<1||tier>3) continue;
            Core core=new Core(BlockPos.of(c.getLong("pos")),tier,c.getInt("surface"));
            for(Tag o:c.getList("ores",10)) { CompoundTag n=(CompoundTag)o; core.ores.put(BlockPos.of(n.getLong("pos")),n.getInt("seconds")); }
            data.add(core);
        }
        return data;
    }
    @Override public CompoundTag save(CompoundTag tag) {
        ListTag cores=new ListTag();
        for(var bucket:chunks.values()) for(Core core:bucket.values()) {
            CompoundTag c=new CompoundTag(); c.putLong("pos",core.pos.asLong()); c.putInt("tier",core.tier); c.putInt("surface",core.surface);
            ListTag ores=new ListTag(); core.ores.forEach((pos,seconds)->{ CompoundTag o=new CompoundTag(); o.putLong("pos",pos.asLong()); o.putInt("seconds",seconds); ores.add(o); });
            c.put("ores",ores); cores.add(c);
        }
        tag.put("cores",cores); return tag;
    }
}
