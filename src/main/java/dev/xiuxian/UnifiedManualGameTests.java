package dev.xiuxian;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.parameters.*;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.*;
import java.util.*;

@GameTestHolder(Xiuxian.ID)
@PrefixGameTestTemplate(false)
public class UnifiedManualGameTests {
    private static net.minecraftforge.common.util.FakePlayer player(GameTestHelper h){
        var p=FakePlayerFactory.get(h.getLevel(),new GameProfile(UUID.randomUUID(),"UnifiedManualTest"));
        CultivationFoundation.ensure(p);Cultivation.initiate(p);var tag=CultivationFoundation.data(p);tag.putDouble("Mana",100);CultivationFoundation.save(p,tag);
        var pos=h.absolutePos(new BlockPos(1,1,0));p.setPos(pos.getX()+.5,pos.getY(),pos.getZ()+.5);p.setYRot(0);p.setXRot(0);p.setOnGround(true);return p;
    }
    @GameTest(template="empty") public static void swordBooksTeachTheirOwnAttacks(GameTestHelper h){
        var target=h.spawn(EntityType.ZOMBIE,new BlockPos(1,1,2));target.setNoAi(true);target.setNoGravity(true);
        for(var manual:ManualDefinition.ALL){if(!manual.sword())continue;
            var p=player(h);var book=new ItemStack(FoundationItems.MANUALS.get(manual.id()).get());p.setItemSlot(EquipmentSlot.MAINHAND,book);
            book.use(h.getLevel(),p,InteractionHand.MAIN_HAND);h.assertTrue(CultivationFoundation.active(p)==null,"Reading sword book is not auto learning");
            h.assertTrue(SpellManuals.bookAction(p,new SpellManuals.BookAction(manual.id(),InteractionHand.MAIN_HAND,0)),"Sword book activates cultivation");
            h.assertTrue(SpellManuals.bookAction(p,new SpellManuals.BookAction(manual.id(),InteractionHand.MAIN_HAND,1)),"First sword page can be studied");
            p.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.IRON_SWORD));target.setHealth(20);target.invulnerableTime=0;
            h.assertTrue(SwordCombat.cast(p),"Same book supplies attack without a separate art: "+manual.id());
            h.assertTrue(SwordCombat.known(p).isEmpty(),"No standalone sword book was learned");
            h.assertTrue(SwordCombat.available(p).contains("recall"),"Recall is a shared utility");
            var persisted=p.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);persisted.putInt("xiuxian_level",6);p.getPersistentData().put(Player.PERSISTED_NBT_TAG,persisted);
            p.setItemSlot(EquipmentSlot.MAINHAND,book);
            boolean second=SpellManuals.bookAction(p,new SpellManuals.BookAction(manual.id(),InteractionHand.MAIN_HAND,2));
            h.assertTrue(second==(manual.coverage()>=6),"Second page respects fragment coverage");
            if(second){p.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.IRON_SWORD));var tag=CultivationFoundation.data(p);tag.putLong("SwordGlobalUntil",0);CultivationFoundation.save(p,tag);h.assertTrue(SwordCombat.cast(p),"Second sword art executes");}
            h.assertTrue(book.getCount()==1,"Learning keeps the sword manual");
            var projection=SwordCombat.current(p);if(projection!=null)projection.discard();
        }h.succeed();
    }
    @GameTest(template="empty") public static void fragmentUpgradePreservesPracticeAndKnowledge(GameTestHelper h){
        var p=player(h);String fragment="basic_sword_fragment_3";CultivationFoundation.learn(p,fragment);
        p.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(FoundationItems.MANUALS.get(fragment).get()));SpellManuals.bookAction(p,new SpellManuals.BookAction(fragment,InteractionHand.MAIN_HAND,1));
        ManualPractice.train(p,Cultivation.MANUAL_PRACTICE_BASE.get());h.assertTrue(ManualPractice.rank(p)==1,"Sword manuals gain independent levels");
        CultivationFoundation.learn(p,"basic_sword");h.assertTrue(ManualPractice.rank(p)==1&&SpellManuals.learned(p,"basic_sword")==1,"Completing same manual preserves rank and pages");
        var persisted=p.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);persisted.putInt("xiuxian_level",6);p.getPersistentData().put(Player.PERSISTED_NBT_TAG,persisted);
        p.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(FoundationItems.MANUALS.get("basic_sword").get()));SpellManuals.bookAction(p,new SpellManuals.BookAction("basic_sword",InteractionHand.MAIN_HAND,2));
        CultivationFoundation.select(p,fragment);h.assertTrue(!SwordCombat.available(p).contains("sweep"),"Three-layer fragment cannot use missing sixth-layer page");
        CultivationFoundation.learn(p,"stone_sword");h.assertTrue(ManualPractice.rank(p)==0,"Different manual resets practice");h.succeed();
    }
    @GameTest(template="empty") public static void rewardPoolHasFullAndPartialManuals(GameTestHelper h){
        h.assertTrue(ManualDefinition.ALL.size()==66,"All 22 families have three coverage variants");
        var families=new HashMap<String,Set<Integer>>();for(var manual:ManualDefinition.ALL)families.computeIfAbsent(manual.family(),k->new HashSet<>()).add(manual.coverage());
        h.assertTrue(families.size()==22&&families.values().stream().allMatch(v->v.equals(Set.of(3,6,9))),"Every family has full and both fragments");
        for(String path:ManualLoot.CHESTS){
            var actual=h.getLevel().getServer().getLootData().getLootTable(new net.minecraft.resources.ResourceLocation("minecraft",path));
            h.assertTrue(actual!=LootTable.EMPTY&&actual.getPool("xiuxian_cultivation_manuals")!=null,"Reward table is real and receives manual pool: "+path);
        }
        var table=LootTable.lootTable().withPool(ManualLoot.pool()).build();
        var params=new LootParams.Builder(h.getLevel()).withParameter(LootContextParams.ORIGIN,h.absolutePos(new BlockPos(1,1,1)).getCenter()).create(LootContextParamSets.CHEST);
        Set<Integer> coverage=new HashSet<>();Set<Boolean> routes=new HashSet<>();int dropped=0;
        var seeds=new java.util.Random(20261103L);
        for(int sample=0;sample<2000;sample++)for(var stack:table.getRandomItems(params,seeds.nextLong())){
            var manual=ManualDefinition.ALL.stream().filter(m->stack.is(FoundationItems.MANUALS.get(m.id()).get())).findFirst().orElseThrow();coverage.add(manual.coverage());routes.add(manual.sword());dropped++;
        }
        h.assertTrue(coverage.equals(Set.of(3,6,9))&&routes.size()==2&&dropped>0&&dropped<2000,"Seeded reward rolls contain both routes, complete books and fragments");
        h.succeed();
    }
}
