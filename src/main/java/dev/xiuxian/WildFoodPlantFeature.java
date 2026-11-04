package dev.xiuxian;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/** Places mature plants in small patches; existing chunks are not retroactively changed. */
public final class WildFoodPlantFeature extends Feature<NoneFeatureConfiguration> {
    private final FoodPlantKind kind;
    public WildFoodPlantFeature(FoodPlantKind kind) { super(NoneFeatureConfiguration.CODEC); this.kind = kind; }

    @Override public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        BlockState crop = SpiritualFarming.CROPS.get(kind).get().defaultBlockState().setValue(FoodPlantBlock.AGE, 3);
        int placed = 0;
        for (int i = 0; i < 12; i++) {
            BlockPos pos = origin.offset(random.nextInt(9) - 4, random.nextInt(5) - 2, random.nextInt(9) - 4);
            if (kind == FoodPlantKind.EARTH_GINGER && (pos.getY() >= 0 || level.getRawBrightness(pos, 0) > 7)) continue;
            if (kind == FoodPlantKind.STAR_FLOWER) {
                var biome = level.getBiome(pos);
                boolean highMountain = biome.is(Biomes.JAGGED_PEAKS) || biome.is(Biomes.FROZEN_PEAKS) ||
                    biome.is(Biomes.STONY_PEAKS) || biome.is(Biomes.SNOWY_SLOPES) || biome.is(Biomes.GROVE);
                if (highMountain && pos.getY() <= 110) continue;
            }
            if (level.isEmptyBlock(pos) && crop.canSurvive(level, pos)) {
                level.setBlock(pos, crop, 2);
                placed++;
            }
        }
        return placed > 0;
    }
}
