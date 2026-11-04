package dev.xiuxian;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.effect.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.*;
import net.minecraftforge.common.ForgeMod;
import java.util.UUID;

@GameTestHolder(Xiuxian.ID)
@PrefixGameTestTemplate(false)
public class TalismanGameTests {
    private static ServerPlayer player(GameTestHelper h,int realm) {
        var p=FakePlayerFactory.get(h.getLevel(),new GameProfile(UUID.randomUUID(),"TalismanTest"));
        var tag=new CompoundTag();tag.putInt("xiuxian_level",realm);tag.putBoolean("xiuxian_initiated",realm>0);tag.putInt("xiuxian_schema",1);p.getPersistentData().put(Player.PERSISTED_NBT_TAG,tag);
        CultivationFoundation.ensure(p);CultivationFoundation.restoreMana(p,100);
        p.setPos(h.absolutePos(new BlockPos(1,1,1)).getCenter());return p;
    }
    private static TalismanTableEntity table(GameTestHelper h,BlockPos pos,TalismanKind kind,boolean enhanced) {
        h.setBlock(pos,Blocks.AIR);h.setBlock(pos,Talismans.TABLE.get());var table=(TalismanTableEntity)h.getBlockEntity(pos);
        table.setItem(0,new ItemStack(Talismans.BRUSH.get()));table.setItem(1,new ItemStack(Talismans.PAPER.get(),2));table.setItem(2,new ItemStack(enhanced?Talismans.SPIRIT_CINNABAR.get():Talismans.CINNABAR.get(),2));table.setItem(3,new ItemStack(kind.catalyst()));return table;
    }
    @GameTest(template="empty") public static void drawingGuardsAndExactCosts(GameTestHelper h) {
        var pos=new BlockPos(1,1,1);var t=table(h,pos,TalismanKind.FIRE,false);var mortal=player(h,0);
        h.assertTrue(t.draw(mortal,false,0)==TalismanTableEntity.Result.MORTAL,"Mortal rejected");
        var p=player(h,1);CultivationFoundation.spendMana(p,100);
        h.assertTrue(t.draw(p,false,0)==TalismanTableEntity.Result.MANA,"Insufficient mana rejected");
        h.assertTrue(t.getItem(1).getCount()==2&&t.getItem(0).getDamageValue()==0,"Rejected operation has no cost");
        CultivationFoundation.restoreMana(p,100);t.setItem(5,new ItemStack(Items.DIRT));
        h.assertTrue(t.draw(p,false,0)==TalismanTableEntity.Result.OUTPUT&&CultivationFoundation.mana(p)==100,"Blocked output preserves mana");
        t.setItem(5,ItemStack.EMPTY);h.assertTrue(t.draw(p,false,0)==TalismanTableEntity.Result.SUCCESS,"Draw succeeds");
        h.assertTrue(CultivationFoundation.mana(p)==85&&t.getItem(0).getDamageValue()==1&&t.getItem(1).getCount()==1,"Exact mana and material cost");
        h.assertTrue(t.getItem(5).is(Talismans.ITEMS.get(TalismanKind.FIRE).get())&&t.getItem(5).getCount()==1,"Exactly one result");
        h.assertTrue(p.getInventory().countItem(Items.GLASS_BOTTLE)==1,"Elixir bottle returned");
        h.assertTrue(t.draw(player(h,1),true,0)==TalismanTableEntity.Result.COOLDOWN,"Shared table cooldown prevents concurrent drawing");
        h.succeed();
    }
    @GameTest(template="empty") public static void stoneEnergyFailureDurabilityAndPersistence(GameTestHelper h) {
        var t=table(h,new BlockPos(1,1,1),TalismanKind.ICE,true);var p=player(h,1);t.setItem(4,new ItemStack(CultivationResources.STONE.get()));t.getItem(0).setDamageValue(63);
        h.assertTrue(t.draw(p,true,.99)==TalismanTableEntity.Result.FAILURE,"Forced failure");
        h.assertTrue(t.energy()==85&&CultivationFoundation.mana(p)==100,"Stone mode saves remainder and preserves mana");
        h.assertTrue(t.getItem(0).isEmpty()&&t.getItem(4).isEmpty()&&t.getItem(5).isEmpty(),"Last brush use and failed output");
        var loaded=new TalismanTableEntity(t.getBlockPos(),t.getBlockState());loaded.load(t.saveWithoutMetadata());
        h.assertTrue(loaded.energy()==85&&loaded.getItem(1).getCount()==1,"Energy and inventory persist");
        h.assertTrue(TalismanKind.THUNDER.chance(9)==90&&TalismanKind.FIRE.chance(9)==100,"Realm chance cap");
        var a=new TalismanMenu(80,p.getInventory(),t,new net.minecraft.world.inventory.SimpleContainerData(3));var b=new TalismanMenu(81,p.getInventory(),t,new net.minecraft.world.inventory.SimpleContainerData(3));
        a.clickMenuButton(p,2);h.assertTrue(a.stoneMode()&&!b.stoneMode(),"Power mode belongs to each menu");
        h.succeed();
    }
    @GameTest(template="empty") public static void allRecipesAndQualities(GameTestHelper h) {
        var p=player(h,9);
        for(var kind:TalismanKind.values())for(boolean enhanced:new boolean[]{false,true}) {
            var t=table(h,new BlockPos(1,1,1),kind,enhanced);CultivationFoundation.restoreMana(p,1000);
            h.assertTrue(t.draw(p,false,0)==TalismanTableEntity.Result.SUCCESS,"Recipe succeeds: "+kind);
            h.assertTrue(Talismans.enhanced(t.getItem(5))==enhanced,"Quality retained");
            var copy=new TalismanTableEntity(t.getBlockPos(),t.getBlockState());copy.load(t.saveWithoutMetadata());
            h.assertTrue(ItemStack.isSameItemSameTags(t.getItem(5),copy.getItem(5)),"Output sync/persistence preserves tags");
            h.assertTrue(t.getItem(5).getMaxStackSize()==16,"Talisman stack size");
        }h.succeed();
    }
    @GameTest(template="empty") public static void rockWhitelistAndCoreProtection(GameTestHelper h) {
        var p=player(h,1);var center=new BlockPos(2,2,2);
        h.setBlock(center,Blocks.STONE);h.setBlock(center.east(),CultivationResources.ORE.get());h.setBlock(center.west(),CultivationResources.DEEP_ORE.get());h.setBlock(center.north(),VeinBlocks.CORE.get());h.setBlock(center.south(),VeinBlocks.DEPLETED.get());h.setBlock(center.above(),Blocks.CHEST);h.setBlock(center.below(),Blocks.BEDROCK);
        h.assertTrue(TalismanEffects.breakRocks(p,h.absolutePos(center)),"Ordinary stone cleared");
        h.assertTrue(h.getBlockState(center).isAir(),"Stone removed");
        h.assertTrue(h.getBlockState(center.east()).is(CultivationResources.ORE.get())&&h.getBlockState(center.west()).is(CultivationResources.DEEP_ORE.get()),"Both ore variants protected");
        h.assertTrue(h.getBlockState(center.north()).is(VeinBlocks.CORE.get())&&h.getBlockState(center.south()).is(VeinBlocks.DEPLETED.get()),"Core and depleted ore protected");
        h.assertTrue(h.getBlockState(center.above()).is(Blocks.CHEST)&&h.getBlockState(center.below()).is(Blocks.BEDROCK),"Container and unbreakable protected");h.succeed();
    }
    @GameTest(template="empty") public static void buffsRestoreAndDoNotStack(GameTestHelper h) {
        var p=player(h,1);double step=p.getAttributeValue(ForgeMod.STEP_HEIGHT_ADDITION.get()),speed=p.getAttributeValue(Attributes.MOVEMENT_SPEED);
        TalismanEffects.activate(p,TalismanKind.SPEED,false);TalismanEffects.activate(p,TalismanKind.SPEED,false);
        h.assertTrue(Math.abs(p.getAttributeValue(Attributes.MOVEMENT_SPEED)-speed*1.4)<.00001,"Speed refresh does not stack");
        h.assertTrue(Math.abs(p.getAttributeValue(ForgeMod.STEP_HEIGHT_ADDITION.get())-step-.5)<.00001,"Step increase");p.removeEffect(Talismans.SPEED.get());
        h.assertTrue(p.getAttributeValue(ForgeMod.STEP_HEIGHT_ADDITION.get())==step,"Step restored");
        p.addEffect(new MobEffectInstance(MobEffects.ABSORPTION,1200,0));float other=p.getAbsorptionAmount();
        TalismanEffects.activate(p,TalismanKind.ARMOR,false);TalismanEffects.activate(p,TalismanKind.ARMOR,false);
        h.assertTrue(p.getAbsorptionAmount()==other+10,"Shield exact and refreshable");p.setAbsorptionAmount(p.getAbsorptionAmount()-3);TalismanEffects.reconcileShield(p);p.removeEffect(Talismans.SHIELD.get());
        h.assertTrue(p.getAbsorptionAmount()==other,"Expiration preserves other absorption");
        p.setHealth(5);p.addEffect(new MobEffectInstance(MobEffects.POISON,100));p.addEffect(new MobEffectInstance(MobEffects.WITHER,100));TalismanEffects.activate(p,TalismanKind.HEAL,true);
        h.assertTrue(p.getHealth()==13&&!p.hasEffect(MobEffects.POISON)&&!p.hasEffect(MobEffects.WITHER),"Enhanced healing and cleanse");
        TalismanEffects.activate(p,TalismanKind.WATER,true);h.assertTrue(p.getEffect(MobEffects.WATER_BREATHING).getDuration()==6000,"Enhanced water duration");h.succeed();
    }
    @GameTest(template="empty") public static void projectileDamageAndTerrain(GameTestHelper h) {
        var p=player(h,1);var cow=h.spawn(EntityType.COW,new BlockPos(2,2,2));cow.setNoAi(true);cow.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);cow.setHealth(100);
        var fire=new TalismanProjectile(Talismans.PROJECTILE.get(),h.getLevel());fire.setOwner(p);fire.setItem(TalismanKind.FIRE.output(false));fire.damageTarget(cow);
        h.assertTrue(cow.getHealth()==92&&cow.isOnFire(),"Fire damage 8 and ignition");cow.invulnerableTime=0;cow.clearFire();
        var ice=new TalismanProjectile(Talismans.PROJECTILE.get(),h.getLevel());ice.setOwner(p);ice.setItem(TalismanKind.ICE.output(true));ice.damageTarget(cow);
        h.assertTrue(cow.getHealth()==84&&cow.getEffect(MobEffects.MOVEMENT_SLOWDOWN).getAmplifier()==3&&cow.getTicksFrozen()==140,"Enhanced ice damage and effects");
        var zombie=h.spawn(EntityType.ZOMBIE,new BlockPos(2,2,3));zombie.setNoAi(true);zombie.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);zombie.getAttribute(Attributes.ARMOR).setBaseValue(0);zombie.setHealth(100);
        h.setBlock(new BlockPos(2,1,3),Blocks.OAK_PLANKS);
        var thunder=new TalismanProjectile(Talismans.PROJECTILE.get(),h.getLevel());thunder.setOwner(p);thunder.setItem(TalismanKind.THUNDER.output(true));thunder.impact(zombie.position(),zombie);
        h.assertTrue(Math.abs(zombie.getHealth()-73)<.01,"Enhanced thunder undead bonus once");h.assertTrue(h.getBlockState(new BlockPos(2,1,3)).is(Blocks.OAK_PLANKS),"Lightning keeps wooden terrain");
        fire.impact(h.absolutePos(new BlockPos(2,1,3)).getCenter(),null);h.assertTrue(h.getBlockState(new BlockPos(2,1,3)).is(Blocks.OAK_PLANKS),"Fire impact preserves terrain");h.succeed();
    }
    @GameTest(template="empty") public static void absorptionSourcesAndProtectedBreakEvents(GameTestHelper h) {
        var p=player(h,1);
        p.addEffect(new MobEffectInstance(MobEffects.ABSORPTION,1200,0));
        TalismanEffects.activate(p,TalismanKind.ARMOR,false);
        p.removeEffect(MobEffects.ABSORPTION);p.removeEffect(Talismans.SHIELD.get());
        h.assertTrue(p.getAbsorptionAmount()==0,"Removing another shield leaves no permanent absorption");
        var target=h.spawn(EntityType.COW,new BlockPos(3,1,3));target.setNoAi(true);TalismanEffects.shield(target,10,1200);
        target.hurt(target.damageSources().generic(),3);h.assertTrue(target.getAbsorptionAmount()==7,"Actual hit spends shield");target.removeEffect(Talismans.SHIELD.get());h.assertTrue(target.getAbsorptionAmount()==0,"Remaining shield removed");
        var pos=h.absolutePos(new BlockPos(2,1,1));h.setBlock(new BlockPos(2,1,1),Blocks.STONE);
        java.util.function.Consumer<net.minecraftforge.event.level.BlockEvent.BreakEvent> protection=e->{if(e.getPos().equals(pos))e.setCanceled(true);};
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(protection);
        try { h.assertTrue(!TalismanEffects.breakRocks(p,pos),"Protection event prevents rock destruction");h.assertTrue(h.getLevel().getBlockState(pos).is(Blocks.STONE),"Protected rock survives"); }
        finally { net.minecraftforge.common.MinecraftForge.EVENT_BUS.unregister(protection); }
        h.succeed();
    }
    @GameTest(template="empty") public static void oreToolsLootAndCrafting(GameTestHelper h) {
        var p=player(h,1);var state=Talismans.ORE.get().defaultBlockState();var pos=h.absolutePos(new BlockPos(1,1,1));
        h.assertTrue(!new ItemStack(Items.WOODEN_PICKAXE).isCorrectToolForDrops(state)&&new ItemStack(Items.STONE_PICKAXE).isCorrectToolForDrops(state),"Stone pick requirement");
        var normal=Block.getDrops(state,h.getLevel(),pos,null,p,new ItemStack(Items.STONE_PICKAXE));h.assertTrue(normal.size()==1&&normal.get(0).is(Talismans.CINNABAR.get())&&normal.get(0).getCount()==1,"Ore drops one cinnabar");
        var silk=new ItemStack(Items.DIAMOND_PICKAXE);silk.enchant(net.minecraft.world.item.enchantment.Enchantments.SILK_TOUCH,1);
        h.assertTrue(Block.getDrops(state,h.getLevel(),pos,null,p,silk).get(0).is(Talismans.ORE_ITEM.get()),"Silk Touch keeps ore");
        for(String id:new String[]{"spirit_stone_dust","blank_talisman_paper","spirit_cinnabar","wolf_hair_brush","talisman_table"})h.assertTrue(h.getLevel().getRecipeManager().byKey(new net.minecraft.resources.ResourceLocation(Xiuxian.ID,id)).isPresent(),"Crafting recipe loads: "+id);
        h.succeed();
    }
    @GameTest(template="empty") public static void projectileLifetimeAndFireSafety(GameTestHelper h) {
        var p=player(h,1);var pos=new BlockPos(2,1,2);h.setBlock(pos,Blocks.OAK_PLANKS);
        var shot=new TalismanProjectile(Talismans.PROJECTILE.get(),h.getLevel());shot.setOwner(p);shot.setItem(TalismanKind.THUNDER.output(false));shot.impact(h.absolutePos(pos.above()).getCenter(),null);
        var pet=h.spawn(EntityType.WOLF,new BlockPos(1,1,2));pet.setTame(true);pet.setOwnerUUID(p.getUUID());h.assertTrue(!shot.allowed(p)&&!shot.allowed(pet),"Owner and owned pet excluded");
        var timed=new TalismanProjectile(Talismans.PROJECTILE.get(),h.getLevel());timed.setPos(h.absolutePos(new BlockPos(2,40,2)).getCenter());timed.setItem(TalismanKind.ICE.output(true));timed.tickCount=100;timed.tick();h.assertTrue(timed.isRemoved(),"Projectile expires at 100 ticks");
        h.runAfterDelay(8,()->{h.assertTrue(h.getBlockState(pos).is(Blocks.OAK_PLANKS),"Lightning preserves wood after ticking");for(var cursor:BlockPos.betweenClosed(pos.offset(-1,0,-1),pos.offset(1,2,1)))h.assertTrue(!h.getBlockState(cursor).is(Blocks.FIRE),"No ground fire after lightning");h.succeed();});
    }
    @GameTest(template="empty") public static void eyeDetectionAndUseCooldown(GameTestHelper h) {
        var p=player(h,1);var core=h.absolutePos(new BlockPos(3,1,1));h.setBlock(new BlockPos(3,1,1),VeinBlocks.CORE.get());SpiritVeins.get(h.getLevel()).add(new SpiritVeins.Core(core,1,core.getY()));
        h.assertTrue(core.equals(SpiritVeins.get(h.getLevel()).nearestLoadedCore(h.getLevel(),p.position(),24)),"Eye locates loaded core");h.assertTrue(TalismanEffects.direction(p.blockPosition(),core).equals("east"),"Core direction east");
        var zombie=h.spawn(EntityType.ZOMBIE,new BlockPos(2,1,2));zombie.setNoAi(true);TalismanEffects.scan(p,24);h.assertTrue(zombie.hasEffect(MobEffects.GLOWING),"Nearby monster glows");
        var item=(TalismanItem)Talismans.ITEMS.get(TalismanKind.WATER).get();p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,TalismanKind.WATER.output(false));item.use(h.getLevel(),p,net.minecraft.world.InteractionHand.MAIN_HAND);
        h.assertTrue(p.getMainHandItem().isEmpty()&&p.getCooldowns().isOnCooldown(Talismans.ITEMS.get(TalismanKind.FIRE).get()),"Consume and shared cooldown");h.succeed();
    }
}
