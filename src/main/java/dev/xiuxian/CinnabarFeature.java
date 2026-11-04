package dev.xiuxian;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.levelgen.feature.*;
import net.minecraft.world.level.levelgen.feature.configurations.*;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import java.util.List;

public final class CinnabarFeature extends Feature<NoneFeatureConfiguration> {
    public CinnabarFeature() { super(NoneFeatureConfiguration.CODEC); }
    @Override public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        var state=Talismans.ORE.get().defaultBlockState();
        var targets=List.of(OreConfiguration.target(new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES),state),OreConfiguration.target(new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES),state));
        return Feature.ORE.place(new OreConfiguration(targets,4+context.random().nextInt(5)),context.level(),context.chunkGenerator(),context.random(),context.origin());
    }
}
