package dev.xiuxian;

import com.mojang.authlib.GameProfile;
import net.minecraft.gametest.framework.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.*;
import java.util.UUID;

@GameTestHolder(Xiuxian.ID)
@PrefixGameTestTemplate(false)
public class SwordCombatGameTests {
    private static net.minecraftforge.common.util.FakePlayer player(GameTestHelper h){
        var p=FakePlayerFactory.get(h.getLevel(),new GameProfile(UUID.randomUUID(),"SwordTest"));
        CultivationFoundation.ensure(p);Cultivation.initiate(p);CultivationFoundation.learn(p,"basic_sword");
        p.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.IRON_SWORD));
        var tag=CultivationFoundation.data(p);tag.putDouble("Mana",100);CultivationFoundation.save(p,tag);
        var pos=h.absolutePos(new BlockPos(1,1,0));p.setPos(pos.getX()+.5,pos.getY(),pos.getZ()+.5);p.setYRot(0);p.setXRot(0);return p;
    }
    @GameTest(template="empty",timeoutTicks=80) public static void vanillaSwordActuallyHits(GameTestHelper h){
        var p=player(h);SwordCombat.learn(p,"drive");
        var zombie=h.spawn(EntityType.ZOMBIE,new BlockPos(1,1,2));zombie.setNoAi(true);zombie.setNoGravity(true);
        zombie.setItemSlot(EquipmentSlot.HEAD,new ItemStack(Items.DIAMOND_HELMET));
        h.assertTrue(SwordCombat.target(p)==zombie,"Ray must select the intended test target");
        h.assertTrue(SwordCombat.cast(p),"Learned art must launch with vanilla iron sword");
        var sword=SwordCombat.current(p);h.assertTrue(sword!=null&&sword.getItem().is(Items.IRON_SWORD),"Projection uses held sword appearance");
        h.assertTrue(p.getMainHandItem().is(Items.IRON_SWORD)&&p.getMainHandItem().getCount()==1,"Weapon stays in inventory");
        h.assertTrue(CultivationFoundation.mana(p)==94,"Launch spends energy");
        h.assertTrue(!SwordCombat.cast(p),"Repeated cast respects cooldown");
        h.runAfterDelay(16,()->{h.assertTrue(zombie.getHealth()<zombie.getMaxHealth(),"Real projectile must damage target");h.assertTrue(p.getLastHurtMob()==zombie,"Remote sword hit must enter combat recovery state");h.succeed();});
    }
    @GameTest(template="empty") public static void eligibilityAndArmorRecovery(GameTestHelper h){
        var p=player(h);SwordCombat.learn(p,"armor_break");
        var zombie=h.spawn(EntityType.ZOMBIE,new BlockPos(1,1,2));zombie.setNoAi(true);zombie.setNoGravity(true);
        double armor=zombie.getAttributeValue(Attributes.ARMOR);
        SwordCombat.strike(p,zombie,"armor_break",1);
        h.assertTrue(zombie.getAttributeValue(Attributes.ARMOR)<armor,"Armor break modifies real armor");
        zombie.getPersistentData().putLong("xiuxian_armor_until",h.getLevel().getGameTime());
        SwordCombat.armorExpiry(new net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent(zombie));
        h.assertTrue(zombie.getAttributeValue(Attributes.ARMOR)==armor,"Armor penalty expires without stacking");
        for(Item item:java.util.List.of(Items.WOODEN_SWORD,Items.STONE_SWORD,Items.IRON_SWORD,Items.GOLDEN_SWORD,Items.DIAMOND_SWORD,Items.NETHERITE_SWORD)){
            p.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(item));h.assertTrue(SwordCombat.swordReady(p),"Every vanilla sword supported");
        }
        p.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.STICK));h.assertTrue(!SwordCombat.swordReady(p),"Reject non-sword");
        p.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.IRON_SWORD));
        var tag=CultivationFoundation.data(p);tag.putString("Active","");CultivationFoundation.save(p,tag);
        h.assertTrue(!SwordCombat.cast(p),"No sword manual cannot cast");h.succeed();
    }
    @GameTest(template="empty") public static void wallBlocksTargeting(GameTestHelper h){
        var p=player(h);SwordCombat.learn(p,"drive");
        var zombie=h.spawn(EntityType.ZOMBIE,new BlockPos(1,1,2));zombie.setNoAi(true);zombie.setNoGravity(true);
        h.setBlock(new BlockPos(1,2,1),net.minecraft.world.level.block.Blocks.STONE);
        h.assertTrue(SwordCombat.target(p)==null,"Cannot select target through wall");
        h.assertTrue(!SwordCombat.cast(p)&&CultivationFoundation.mana(p)==100,"Invalid target must not spend energy");h.succeed();
    }
    @GameTest(template="empty") public static void allArtsHaveExecutableActions(GameTestHelper h){
        var zombie=h.spawn(EntityType.ZOMBIE,new BlockPos(1,1,2));zombie.setNoAi(true);zombie.setNoGravity(true);
        for(var art:SwordArt.ALL){
            var p=player(h);p.setOnGround(true);
            if(art.id().equals("recall")||art.id().equals("return_strike")){
                SwordCombat.learn(p,"drive");h.assertTrue(SwordCombat.cast(p),"Set up outgoing sword");
            }
            net.minecraft.world.entity.projectile.Arrow arrow=null;
            if(art.id().equals("intercept")){
                arrow=EntityType.ARROW.create(h.getLevel());arrow.setOwner(zombie);arrow.setPos(p.getX(),p.getEyeY(),p.getZ()+2);arrow.setDeltaMovement(0,0,-1);h.getLevel().addFreshEntity(arrow);
            }
            SwordCombat.learn(p,art.id());
            h.assertTrue(SwordCombat.cast(p),"Art must execute: "+art.id());
            h.assertTrue(CultivationFoundation.mana(p)<100,"Art spends spiritual energy");
            if(arrow!=null)h.assertTrue(arrow.isRemoved(),"Intercept removes incoming projectile");
            if(art.id().equals("step"))h.assertTrue(p.getDeltaMovement().horizontalDistance()>0,"Step imparts movement");
            if(art.id().equals("charge")){
                SwordCombat.guard(new net.minecraftforge.event.entity.living.LivingHurtEvent(p,p.damageSources().generic(),1));
                h.assertTrue(!CultivationFoundation.data(p).contains("SwordPreparingUntil"),"Damage interrupts preparation");
            }
            var projection=SwordCombat.current(p);if(projection!=null)projection.discard();
        }
        h.succeed();
    }

}
