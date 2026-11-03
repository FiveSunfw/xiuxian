package dev.xiuxian;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

@Mod.EventBusSubscriber(modid=Xiuxian.ID)
public class ManualLoot {
    @SubscribeEvent public static void load(LootTableLoadEvent event) {
        if (!event.getName().getNamespace().equals("minecraft")) return;
        String path=event.getName().getPath();
        if (!path.equals("chests/simple_dungeon") && !path.equals("chests/abandoned_mineshaft")) return;
        var pool=LootPool.lootPool().name("xiuxian_advanced_manuals").setRolls(ConstantValue.exactly(1)).when(LootItemRandomChanceCondition.randomChance(.25f));
        for (var manual:ManualDefinition.ALL) {
            if (manual.speed()<=1.25) continue;
            pool.add(LootItem.lootTableItem(FoundationItems.MANUALS.get(manual.id()).get()));
        }
        event.getTable().addPool(pool.build());
    }
}
