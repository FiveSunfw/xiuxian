package dev.xiuxian;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

/** Runtime-only loaded plates. Inventory and ownership remain on the block entity. */
final class FormationIndex {
    private static final Map<ServerLevel,FormationIndex> LEVELS=new IdentityHashMap<>();
    static FormationIndex get(ServerLevel level) { return LEVELS.computeIfAbsent(level,FormationIndex::new); }
    static void unload(ServerLevel level) { LEVELS.remove(level); }
    private final ServerLevel level;
    private final Map<Long,Map<BlockPos,FormationEntity>> buckets=new HashMap<>();
    private final Map<BlockPos,Set<FormationEntity>> flagUsers=new HashMap<>();
    private final Set<Long> unloadedChunks=new HashSet<>();
    private int scanTurn;
    private FormationIndex(ServerLevel level) { this.level=level; }
    boolean loaded(BlockPos pos) { return !unloadedChunks.contains(ChunkPos.asLong(pos))&&level.hasChunkAt(pos); }
    void add(FormationEntity plate) {
        var pos=plate.getBlockPos();var bucket=buckets.computeIfAbsent(ChunkPos.asLong(pos),k->new HashMap<>());
        if(bucket.get(pos)==plate)return;
        if(bucket.containsKey(pos))remove(pos);
        buckets.computeIfAbsent(ChunkPos.asLong(pos),k->new HashMap<>()).put(pos,plate);
        for(var flag:candidates(pos))flagUsers.computeIfAbsent(flag,k->new HashSet<>()).add(plate);
    }
    void remove(BlockPos pos) {
        var bucket=buckets.get(ChunkPos.asLong(pos));if(bucket==null)return;
        var removed=bucket.remove(pos);if(bucket.isEmpty())buckets.remove(ChunkPos.asLong(pos));if(removed==null)return;
        for(var flag:candidates(pos)){var users=flagUsers.get(flag);if(users!=null){users.remove(removed);if(users.isEmpty())flagUsers.remove(flag);}}
        unloadedChunks.removeIf(id->flagUsers.keySet().stream().noneMatch(flag->ChunkPos.asLong(flag)==id));
    }
    private List<BlockPos> candidates(BlockPos pos) {
        var result=new ArrayList<BlockPos>(24);
        for(int r:new int[]{4,8})for(Direction d:Direction.Plane.HORIZONTAL)for(int dy=-1;dy<=1;dy++)result.add(pos.relative(d,r).offset(0,dy,0));
        return result;
    }
    void flagChanged(BlockPos flag) { var users=flagUsers.get(flag);if(users!=null)for(var plate:List.copyOf(users))plate.flagChanged(); }
    void chunkChanged(ChunkPos chunk,boolean loading) {
        long id=chunk.toLong();if(loading)unloadedChunks.remove(id);else if(flagUsers.keySet().stream().anyMatch(pos->ChunkPos.asLong(pos)==id))unloadedChunks.add(id);
        if(!loading){var bucket=buckets.get(id);if(bucket!=null)for(var pos:List.copyOf(bucket.keySet()))remove(pos);}
        // Only plates within one chunk can depend on flags in this chunk (max distance 8).
        for(int x=chunk.x-1;x<=chunk.x+1;x++)for(int z=chunk.z-1;z<=chunk.z+1;z++){
            var bucket=buckets.get(ChunkPos.asLong(x,z));if(bucket!=null)for(var plate:List.copyOf(bucket.values()))plate.invalidate();
        }
    }
    List<FormationEntity> plates() { var result=new ArrayList<FormationEntity>();for(var bucket:buckets.values())result.addAll(bucket.values());return result; }
    List<FormationEntity> nearby(BlockPos pos) { return nearby(pos,16); }
    List<FormationEntity> nearby(BlockPos pos,int radius) {
        var result=new ArrayList<FormationEntity>();
        for(int x=(pos.getX()-radius)>>4;x<=(pos.getX()+radius)>>4;x++)for(int z=(pos.getZ()-radius)>>4;z<=(pos.getZ()+radius)>>4;z++){
            var bucket=buckets.get(ChunkPos.asLong(x,z));if(bucket!=null)result.addAll(bucket.values());
        }
        result.sort(Comparator.comparingLong(p->p.getBlockPos().asLong()));return result;
    }
    List<FormationEntity> at(BlockPos pos,FormationKind kind) {
        var result=new ArrayList<FormationEntity>();
        for(var plate:nearby(pos))if(plate.kind()==kind&&plate.scale()>0&&plate.contains(pos)&&plate.active())result.add(plate);
        return result;
    }
    void plantChanged(BlockPos pos,boolean removing) { for(var plate:nearby(pos))if(plate.kind()==FormationKind.GATHERING)plate.plantChanged(pos,removing); }
    int scanPlants() {
        var all=plates().stream().filter(p->p.kind()==FormationKind.GATHERING&&p.scanPending()&&p.active()).toList();if(all.isEmpty())return 0;int remaining=1024,attempts=16;
        while(remaining>0&&attempts-->0){var plate=all.get(Math.floorMod(scanTurn++,all.size()));remaining-=plate.scanPlants(Math.min(64,remaining));}
        return 1024-remaining;
    }
}
