package dev.xiuxian;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.*;
import java.util.UUID;

@GameTestHolder(Xiuxian.ID)
@PrefixGameTestTemplate(false)
public class SpellManualGameTests {
    private static net.minecraftforge.common.util.FakePlayer player(GameTestHelper h,String... roots){
        var p=FakePlayerFactory.get(h.getLevel(),new GameProfile(UUID.randomUUID(),"SpellTest"));CultivationFoundation.ensure(p);Cultivation.initiate(p);
        var tag=CultivationFoundation.data(p);ListTag list=new ListTag();for(String root:roots)list.add(StringTag.valueOf(root));tag.put("Roots",list);tag.putInt("Grade",1);tag.putDouble("Mana",100);CultivationFoundation.save(p,tag);
        var pos=h.absolutePos(new BlockPos(1,1,0));p.setPos(pos.getX()+.5,pos.getY(),pos.getZ()+.5);p.setYRot(0);p.setXRot(0);return p;
    }
    @GameTest(template="empty") public static void ranksResetAndRemainTrainableAtCap(GameTestHelper h){
        var p=player(h,"金","木");CultivationFoundation.learn(p,"metal_origin");
        ManualPractice.train(p,Cultivation.MANUAL_PRACTICE_BASE.get());h.assertTrue(ManualPractice.rank(p)==1,"Independent practice levels manual");
        ManualPractice.promote(p,2);h.assertTrue(ManualPractice.rank(p)==3,"Realm promotion adds manual levels");
        CultivationFoundation.select(p,"metal_origin");h.assertTrue(ManualPractice.rank(p)==3,"Selecting current manual must not reset");
        var copy=player(h,"金");CultivationFoundation.clone(p,copy);h.assertTrue(ManualPractice.rank(copy)==3,"Death clone preserves practice");
        CultivationFoundation.learn(p,"greenwood");h.assertTrue(ManualPractice.rank(p)==0&&ManualPractice.points(p)==0,"Changing manuals clears level and progress");
        CultivationFoundation.select(p,"metal_origin");h.assertTrue(ManualPractice.rank(p)==0,"Returning does not recover old level");
        var persisted=p.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);persisted.putInt("xiuxian_level",9);persisted.putInt("xiuxian_progress",Cultivation.progressBase()*9);p.getPersistentData().put(Player.PERSISTED_NBT_TAG,persisted);
        h.assertTrue(Cultivation.cultivationRate(p)==0&&Cultivation.breathingPotential(p)>0,"Full realm still has practice potential");
        ManualPractice.train(p,Cultivation.breathingPotential(p));h.assertTrue(ManualPractice.points(p)>0,"Ninth-layer character can retrain");
        ManualPractice.train(p,1e9);h.assertTrue(ManualPractice.rank(p)==9&&ManualPractice.points(p)==0,"Practice caps without overflow");h.succeed();
    }
    @GameTest(template="empty") public static void booksUnlockTwoPagesAndAllManualsCast(GameTestHelper h){
        var zombie=h.spawn(EntityType.ZOMBIE,new BlockPos(1,1,2));zombie.setNoAi(true);zombie.setNoGravity(true);
        for(var manual:ManualDefinition.ALL){
            if(manual.sword())continue;
            var p=player(h,manual.elements().toArray(String[]::new));var book=new ItemStack(FoundationItems.MANUALS.get(manual.id()).get());p.setItemSlot(EquipmentSlot.MAINHAND,book);
            book.use(h.getLevel(),p,InteractionHand.MAIN_HAND);h.assertTrue(CultivationFoundation.active(p)==null,"Reading does not auto-activate");
            h.assertTrue(SpellManuals.bookAction(p,new SpellManuals.BookAction(manual.id(),InteractionHand.MAIN_HAND,0)),"Activate compatible manual");
            h.assertTrue(SpellManuals.bookAction(p,new SpellManuals.BookAction(manual.id(),InteractionHand.MAIN_HAND,1)),"First page available at layer one");
            h.assertTrue(!SpellManuals.bookAction(p,new SpellManuals.BookAction(manual.id(),InteractionHand.MAIN_HAND,2)),"Second page locked before layer six");
            zombie.setHealth(20);zombie.invulnerableTime=0;zombie.clearFire();
            h.assertTrue(SpellManuals.cast(p),"First spell executes: "+manual.id());h.assertTrue(!SpellManuals.cast(p),"Spell cooldown blocks repeat");
            var persisted=p.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);persisted.putInt("xiuxian_level",6);p.getPersistentData().put(Player.PERSISTED_NBT_TAG,persisted);
            h.assertTrue(SpellManuals.bookAction(p,new SpellManuals.BookAction(manual.id(),InteractionHand.MAIN_HAND,2)),"Second page available at layer six");
            var tag=CultivationFoundation.data(p);tag.putLong("SpellGlobalUntil",0);CultivationFoundation.save(p,tag);
            h.assertTrue(SpellManuals.cast(p),"Second spell executes: "+manual.id());
            h.assertTrue(book.getCount()==1&&SpellManuals.learned(p,manual.id())==3,"Both pages learned without consuming book");
        }h.succeed();
    }
    @GameTest(template="empty") public static void practiceIncreasesActualDamage(GameTestHelper h){
        var p=player(h,"金");CultivationFoundation.learn(p,"metal_origin");
        var first=h.spawn(EntityType.ZOMBIE,new BlockPos(1,1,2));first.setNoAi(true);first.setNoGravity(true);
        var second=h.spawn(EntityType.ZOMBIE,new BlockPos(2,1,2));second.setNoAi(true);second.setNoGravity(true);
        SpellManuals.hit(p,first,"金",(float)(4*ManualPractice.power(p)));
        ManualPractice.promote(p,4);
        SpellManuals.hit(p,second,"金",(float)(4*ManualPractice.power(p)));
        h.assertTrue(second.getHealth()<first.getHealth(),"Higher manual rank causes greater actual spell damage");h.succeed();
    }
}
