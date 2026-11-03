package dev.xiuxian;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
@Mod.EventBusSubscriber(modid=Xiuxian.ID)
public class AlchemyEvents {
    @SubscribeEvent public static void poison(MobEffectEvent.Applicable event){if(event.getEffectInstance().getEffect()==net.minecraft.world.effect.MobEffects.POISON&&event.getEntity().hasEffect(Alchemy.ANTIDOTE.get()))event.setResult(net.minecraftforge.eventbus.api.Event.Result.DENY);}
    @SubscribeEvent public static void loot(net.minecraftforge.event.LootTableLoadEvent event){
        String path=event.getName().getPath();
        if(!event.getName().getNamespace().equals("minecraft")||!(path.equals("chests/simple_dungeon")||path.equals("chests/abandoned_mineshaft")))return;
        var pool=net.minecraft.world.level.storage.loot.LootPool.lootPool().name("xiuxian_formulas").setRolls(net.minecraft.world.level.storage.loot.providers.number.ConstantValue.exactly(1)).when(net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition.randomChance(.15f));
        for(String id:java.util.List.of("healing","cleansing","nourishing"))pool.add(net.minecraft.world.level.storage.loot.entries.LootItem.lootTableItem(Alchemy.item(id+"_formula")));
        event.getTable().addPool(pool.build());
    }
}
