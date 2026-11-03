package dev.xiuxian;

import com.mojang.authlib.GameProfile;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.*;
import java.util.UUID;

@GameTestHolder(Xiuxian.ID)
@PrefixGameTestTemplate(false)
public class FoundationGameTests {
    private static net.minecraftforge.common.util.FakePlayer player(GameTestHelper h) {
        var player=FakePlayerFactory.get(h.getLevel(),new GameProfile(UUID.randomUUID(),"FoundationTest"));
        CultivationFoundation.ensure(player); Cultivation.initiate(player); return player;
    }
    @GameTest(template="empty") public static void rootsAndKnowledgePersist(GameTestHelper h) {
        var player=player(h); var before=CultivationFoundation.data(player).copy();
        CultivationFoundation.ensure(player);
        h.assertTrue(before.equals(CultivationFoundation.data(player)),"Testing roots must never reroll");
        h.assertTrue(CultivationFoundation.roots(player).size()>=1 && CultivationFoundation.roots(player).size()<=5,"Root count range");
        h.assertTrue(CultivationFoundation.learn(player,"basic_sword"),"Any root can learn sword manual");
        var roots=CultivationFoundation.roots(player);
        for(var manual:ManualDefinition.ALL) if(!manual.sword()) {
            h.assertTrue(CultivationFoundation.learn(player,manual.id())==roots.containsAll(manual.elements()),"Spell manual requires every root");
        }
        h.assertTrue(!CultivationFoundation.select(player,"nonexistent"),"Reject unknown id");
        var copy=player(h); CultivationFoundation.clone(player,copy);
        h.assertTrue(CultivationFoundation.data(player).equals(CultivationFoundation.data(copy)),"Clone preserves roots and knowledge");
        h.assertTrue(CultivationFoundation.select(copy,"basic_sword"),"Known manuals remain selectable"); h.succeed();
    }
    @GameTest(template="empty") public static void coverageAndNinthLayer(GameTestHelper h) {
        var player=player(h); int base=Cultivation.progressBase();
        Cultivation.addProgress(player,Integer.MAX_VALUE);
        h.assertTrue(Cultivation.progress(player).equals(new CultivationProgress(1,base)),"Without manual first layer caps");
        h.assertTrue(Cultivation.cultivationRate(player)==0,"Blocked cultivation stops");
        var pill=new ItemStack(CultivationResources.PILL.get()); pill.finishUsingItem(h.getLevel(),player);
        h.assertTrue(pill.getCount()==1,"Blocked player must retain pill");
        CultivationFoundation.learn(player,"radiant_sword_fragment");
        h.assertTrue(Cultivation.progress(player).level()==2,"Learning a fragment unlocks promotion");
        Cultivation.addProgress(player,Integer.MAX_VALUE);
        h.assertTrue(Cultivation.progress(player).equals(new CultivationProgress(3,base*3)),"Fragment caps third layer");
        CultivationFoundation.learn(player,"basic_sword");
        var persisted=player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        persisted.putInt("xiuxian_level",9); persisted.putInt("xiuxian_progress",0);
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG,persisted);
        h.assertTrue(!CultivationFoundation.blocked(player),"Fresh ninth layer is not full");
        Cultivation.addProgress(player,100);
        h.assertTrue(Cultivation.progress(player).points()==100,"Ninth layer must accumulate");
        Cultivation.addProgress(player,Integer.MAX_VALUE);
        h.assertTrue(Cultivation.progress(player).equals(new CultivationProgress(9,base*9)),"Ninth layer caps at full");
        h.assertTrue(CultivationFoundation.blocked(player),"Full ninth layer waits for active breakthrough");h.succeed();
    }
    @GameTest(template="empty") public static void legacyAndEnergy(GameTestHelper h) {
        var player=player(h); var persisted=player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        persisted.remove("xiuxian_schema"); persisted.putInt("xiuxian_level",9); persisted.putInt("xiuxian_progress",0);
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG,persisted);
        h.assertTrue(Cultivation.progress(player).full(Cultivation.progressBase()),"Old ninth layer remains full");
        persisted.putInt("xiuxian_level",1); persisted.putInt("xiuxian_technique",2); player.getPersistentData().put(Player.PERSISTED_NBT_TAG,persisted);
        h.assertTrue(CultivationFoundation.coverage(player)==9,"Legacy knowledge retains coverage");
        CultivationFoundation.regenerate(player);
        double mana=CultivationFoundation.mana(player);
        h.assertTrue(mana>0 && mana<=100,"Initiated player restores bounded energy");
        var progress=Cultivation.progress(player);
        h.assertTrue(CultivationFoundation.spendMana(player,mana),"Energy can be spent");
        h.assertTrue(Cultivation.progress(player).equals(progress),"Energy use must not spend cultivation");
        h.assertTrue(!CultivationFoundation.spendMana(player,1)&&!CultivationFoundation.spendMana(player,Double.NaN),"Reject insufficient or invalid spending");h.succeed();
    }
}
