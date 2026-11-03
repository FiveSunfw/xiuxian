package dev.xiuxian;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.MissingMappingsEvent;

@Mod.EventBusSubscriber(modid = Xiuxian.ID)
public class LegacyItems {
    @SubscribeEvent public static void remapLeaves(MissingMappingsEvent event) {
        for (var mapping : event.getMappings(ForgeRegistries.Keys.ITEMS, Xiuxian.ID)) {
            if (mapping.getKey().getPath().equals("qi_leaf")) mapping.remap(Xiuxian.HERB_ITEM.get());
        }
    }
}
