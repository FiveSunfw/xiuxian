package dev.xiuxian;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import java.util.ArrayList;
import java.util.HashSet;

/** Bounded connected growth; common deposits contain at most two ore blocks. */
public class SpiritVeinFeature extends Feature<NoneFeatureConfiguration> {
    private final int minimum, maximum;
    public SpiritVeinFeature(int minimum, int maximum) {
        super(NoneFeatureConfiguration.CODEC);
        this.minimum = minimum; this.maximum = maximum;
    }
    private static boolean replaceable(WorldGenLevel level, BlockPos pos) {
        var state = level.getBlockState(pos);
        return state.is(BlockTags.STONE_ORE_REPLACEABLES) || state.is(BlockTags.DEEPSLATE_ORE_REPLACEABLES);
    }
    @Override public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        int target = minimum + context.random().nextInt(maximum - minimum + 1), placed = 0;
        var ores = new ArrayList<BlockPos>();
        var frontier = new ArrayList<BlockPos>();
        var visited = new HashSet<BlockPos>();
        frontier.add(context.origin());
        while (placed < target && !frontier.isEmpty() && visited.size() < target * 12) {
            BlockPos pos = frontier.remove(context.random().nextInt(frontier.size()));
            if (!visited.add(pos) || !level.ensureCanWrite(pos) || !replaceable(level, pos)) continue;
            boolean deep = level.getBlockState(pos).is(BlockTags.DEEPSLATE_ORE_REPLACEABLES);
            level.setBlock(pos, (deep ? CultivationResources.DEEP_ORE : CultivationResources.ORE).get().defaultBlockState(), 2);
            ores.add(pos.immutable());
            placed++;
            for (Direction direction : Direction.values()) {
                BlockPos next = pos.relative(direction);
                if (!visited.contains(next)) frontier.add(next);
            }
        }
        if (minimum > 2 && placed > 1) {
            BlockPos core = ores.remove(0);
            level.setBlock(core, VeinBlocks.CORE.get().defaultBlockState(), 2);
            if (level.getBlockEntity(core) instanceof VeinBlocks.CoreEntity entity) {
                var tag = new net.minecraft.nbt.CompoundTag();
                var list = new net.minecraft.nbt.ListTag();
                var c = new net.minecraft.nbt.CompoundTag();
                c.putLong("pos",core.asLong()); c.putInt("tier",minimum < 20 ? 1 : minimum < 50 ? 2 : 3);
                c.putInt("surface",level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE_WG,core.getX(),core.getZ()));
                var entries = new net.minecraft.nbt.ListTag();
                for (BlockPos ore : ores) { var o = new net.minecraft.nbt.CompoundTag(); o.putLong("pos",ore.asLong()); entries.add(o); }
                c.put("ores",entries); list.add(c); tag.put("cores",list); entity.generated=tag; entity.setChanged();
            }
        }
        return placed > 0;
    }
}
