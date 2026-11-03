package dev.xiuxian;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import java.util.Set;

@Mod.EventBusSubscriber(modid=Xiuxian.ID)
public class ManualLoot {
    static final Set<String> CHESTS=Set.of("chests/simple_dungeon","chests/abandoned_mineshaft","chests/desert_pyramid","chests/jungle_temple","chests/igloo_chest","chests/shipwreck_treasure","chests/underwater_ruin_small","chests/underwater_ruin_big","chests/stronghold_library","chests/woodland_mansion","chests/pillager_outpost","chests/ancient_city","chests/nether_bridge","chests/bastion_treasure","chests/end_city_treasure","chests/buried_treasure","chests/stronghold_corridor","chests/stronghold_crossing","chests/bastion_other","chests/bastion_bridge","chests/bastion_hoglin_stable","chests/ruined_portal");
    static LootPool.Builder pool(){
        var pool=LootPool.lootPool().name("xiuxian_cultivation_manuals").setRolls(ConstantValue.exactly(1)).when(LootItemRandomChanceCondition.randomChance(.35f));
        for(var manual:ManualDefinition.ALL)pool.add(LootItem.lootTableItem(FoundationItems.MANUALS.get(manual.id()).get()).setWeight((manual.speed()<=1.25?4:manual.speed()<1.75?2:1)*(manual.coverage()==3?6:manual.coverage()==6?3:1)));
        return pool;
    }
    @SubscribeEvent public static void load(LootTableLoadEvent event){
        if(event.getName().getNamespace().equals("minecraft")&&CHESTS.contains(event.getName().getPath()))event.getTable().addPool(pool().build());
    }
}
