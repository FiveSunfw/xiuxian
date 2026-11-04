package dev.xiuxian;

import com.mojang.authlib.GameProfile;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.*;
import net.minecraftforge.eventbus.api.Event;

@GameTestHolder(Xiuxian.ID)
@PrefixGameTestTemplate(false)
public final class FormationGameTests {
    private static final BlockPos CENTER=new BlockPos(24,7,24);
    private static ServerPlayer player(GameTestHelper h) {
        var p=FakePlayerFactory.get(h.getLevel(),new GameProfile(UUID.randomUUID(),"FormationTester"));
        var tag=new CompoundTag();tag.putInt("xiuxian_level",1);tag.putBoolean("xiuxian_initiated",true);tag.putInt("xiuxian_schema",1);p.getPersistentData().put(Player.PERSISTED_NBT_TAG,tag);CultivationFoundation.ensure(p);
        p.setPos(h.absolutePos(CENTER).getCenter());return p;
    }
    private static void flags(GameTestHelper h,BlockPos center,int distance,int dy) { for(Direction d:Direction.Plane.HORIZONTAL)h.setBlock(center.relative(d,distance).offset(0,dy,0),Formations.FLAG.get()); }
    private static FormationEntity plate(GameTestHelper h,BlockPos center,FormationKind kind,int distance,ServerPlayer owner) {
        h.setBlock(center,Formations.PLATE.get());var p=(FormationEntity)h.getBlockEntity(center);p.assignOwner(owner.getUUID());p.setItem(0,new ItemStack(Formations.TOKENS.get(kind).get()));p.setItem(1,new ItemStack(CultivationResources.STONE.get()));p.scale();flags(h,center,distance,0);p.setEnabled(true);return p;
    }
    private static void energy(FormationEntity plate,double amount) { plate.setItem(1,ItemStack.EMPTY);var tag=plate.saveWithoutMetadata();tag.putDouble("Energy",amount);plate.load(tag); }
    private static SpiritVeins.Core core(GameTestHelper h,BlockPos center,int tier) {
        var relative=center.below(3);h.setBlock(relative,VeinBlocks.CORE.get());var c=new SpiritVeins.Core(h.absolutePos(relative),tier,h.absolutePos(center).getY());SpiritVeins.get(h.getLevel()).add(c);return c;
    }
    private static net.minecraft.world.entity.monster.Zombie target(GameTestHelper h,BlockPos relative) {
        var mob=h.spawn(EntityType.ZOMBIE,relative);mob.setNoAi(true);mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);mob.getAttribute(Attributes.ARMOR).setBaseValue(0);mob.setHealth(100);return mob;
    }
    @GameTest(batch="formations",template="formation_arena") public static void structureScalesAndImmediateFlagRemoval(GameTestHelper h) {
        var p=player(h);var plate=plate(h,CENTER,FormationKind.GATHERING,4,p);h.assertTrue(plate.scale()==1&&plate.active(),"Small structure activates");
        flags(h,CENTER,8,1);h.assertTrue(plate.scale()==2,"Complete large structure takes priority with +1 height tolerance");
        h.setBlock(CENTER.east(8).above(),Blocks.AIR);h.assertTrue(plate.scale()==1,"Falls back to complete small structure");
        h.setBlock(CENTER.west(4),Blocks.AIR);h.assertTrue(!plate.active(),"Missing flag stops effects immediately");
        h.setBlock(CENTER.west(4).below(),Formations.FLAG.get());h.assertTrue(plate.active(),"Minus one flag height accepted");
        h.setBlock(CENTER.west(4).below(),Blocks.AIR);h.setBlock(CENTER.west(4).above(2),Formations.FLAG.get());h.assertTrue(!plate.active(),"Two blocks height difference rejected");h.succeed();
    }
    @GameTest(batch="formations",template="formation_arena") public static void sharedFlagsRedstoneAndBoundary(GameTestHelper h) {
        var p=player(h);var a=plate(h,CENTER.west(4),FormationKind.GATHERING,4,p);var b=plate(h,CENTER.east(4),FormationKind.GATHERING,4,p);
        h.assertTrue(a.active()&&b.active(),"Two plates share a flag");
        h.setBlock(CENTER,Blocks.AIR);h.assertTrue(!a.active()&&!b.active(),"Shared flag removal stops both");h.setBlock(CENTER,Formations.FLAG.get());
        h.setBlock(CENTER.west(4).above(),Blocks.REDSTONE_BLOCK);h.assertTrue(!a.active()&&a.status()==FormationEntity.Status.REDSTONE,"Redstone pauses without changing switch");h.setBlock(CENTER.west(4).above(),Blocks.AIR);h.assertTrue(a.active(),"Signal removal resumes");
        h.assertTrue(a.contains(h.absolutePos(CENTER.west(4).offset(8,4,8)))&&!a.contains(h.absolutePos(CENTER.west(4).offset(9,0,0))),"Inclusive block-coordinate bounds");h.succeed();
    }
    @GameTest(batch="formations",template="formation_arena") public static void energyIsIndependentOfVeinsAndSaved(GameTestHelper h) {
        var p=player(h);var plate=plate(h,CENTER,FormationKind.GATHERING,4,p);
        h.assertTrue(plate.active()&&plate.energy()==1199,"Ordinary stone provides 1200, upkeep costs 1");
        var loaded=new FormationEntity(plate.getBlockPos(),plate.getBlockState());loaded.load(plate.saveWithoutMetadata());h.assertTrue(loaded.energy()==plate.energy()&&loaded.enabled()&&loaded.ownerId().equals(p.getUUID()),"Energy, switch and owner persist");
        energy(plate,0);plate.setItem(1,new ItemStack(CultivationResources.STONE_2.get()));h.assertTrue(plate.active()&&plate.energy()==11999,"Tier two provides 12000");
        var c=core(h,CENTER,3);energy(plate,0);h.assertTrue(!plate.active()&&plate.upkeep()==1,"Strong vein does not self-power formation");
        plate.setItem(1,new ItemStack(CultivationResources.STONE.get()));h.assertTrue(plate.active()&&plate.energy()==1199,"Same upkeep on strongest vein");
        double remaining=plate.energy();h.getLevel().removeBlock(c.pos,false);h.assertTrue(plate.active()&&plate.energy()==remaining,"Removing core does not affect operation or charge again");
        flags(h,CENTER,8,0);h.assertTrue(plate.scale()==2&&plate.upkeep()==1,"Large gathering has same fixed upkeep anywhere");
        energy(plate,0);h.assertTrue(!plate.active(),"Large formation also needs fuel");h.succeed();
    }
    @GameTest(batch="formations",template="formation_arena") public static void gatheringMultipliesExistingRateOnce(GameTestHelper h) {
        var p=player(h);var plate=plate(h,CENTER,FormationKind.GATHERING,4,p);core(h,CENTER,3);plate.setEnabled(false);
        p.addEffect(new MobEffectInstance(CultivationResources.NOURISHMENT.get(),6000));double before=Cultivation.breathingPotential(p);plate.setEnabled(true);
        h.assertTrue(Math.abs(Formations.gathering(h.getLevel(),p.blockPosition())-1.35)<.000001,"Small formation remains 1.35 even at strongest vein");
        h.assertTrue(Math.abs(Cultivation.breathingPotential(p)-before*1.35)<.000001,"All existing multipliers preserved");
        var second=plate(h,CENTER.east(),FormationKind.GATHERING,4,p);h.assertTrue(Math.abs(Cultivation.breathingPotential(p)-before*1.35)<.000001,"Overlapping gathering does not multiply twice");
        h.getLevel().players().add(p);try{FormationEvents.gatheringRecovery(h.getLevel());h.assertTrue(CultivationFoundation.mana(p)==3,"Overlapping arrays restore exactly three mana once");}finally{h.getLevel().players().remove(p);}
        flags(h,CENTER,8,0);h.assertTrue(Math.abs(Formations.gathering(h.getLevel(),p.blockPosition())-1.5)<.000001,"Large strongest multiplier selected");
        p.setPos(h.absolutePos(CENTER.east(20)).getCenter());h.assertTrue(Formations.gathering(h.getLevel(),p.blockPosition())==1,"Leaving the region removes bonus");h.succeed();
    }
    @GameTest(batch="formations",template="formation_arena") public static void depletedOreRegeneratesInHalfAsManyCycles(GameTestHelper h) {
        var p=player(h);plate(h,CENTER,FormationKind.GATHERING,4,p);var c=core(h,CENTER,2);BlockPos a=h.absolutePos(CENTER.east(2)),b=h.absolutePos(CENTER.west(2));
        h.setBlock(CENTER.east(2),VeinBlocks.DEPLETED.get());h.setBlock(CENTER.west(2),VeinBlocks.DEEP_DEPLETED.get());h.setBlock(CENTER.north(2),VeinBlocks.DEPLETED.get());
        c.ores.put(a,0);c.ores.put(b,0);var veins=SpiritVeins.get(h.getLevel());int half=(Cultivation.VEIN_REGEN_SECONDS.get()+1)/2;
        for(int i=0;i<half-1;i++)veins.regenerate(h.getLevel());h.assertTrue(h.getLevel().getBlockState(a).is(VeinBlocks.DEPLETED.get()),"No early regeneration");
        veins.regenerate(h.getLevel());h.assertTrue(h.getLevel().getBlockState(a).is(CultivationResources.ORE.get())&&h.getLevel().getBlockState(b).is(CultivationResources.DEEP_ORE.get()),"Both ores restored at half time (900 cycles by default)");
        h.assertTrue(h.getBlockState(CENTER.north(2)).is(VeinBlocks.DEPLETED.get()),"Unbound ore never regenerates");
        h.setBlock(CENTER.east(2),VeinBlocks.DEPLETED.get());h.getLevel().removeBlock(c.pos,false);veins.regenerate(h.getLevel());h.assertTrue(h.getLevel().getBlockState(a).is(VeinBlocks.DEPLETED.get()),"Removed core severs regeneration");h.succeed();
    }
    @GameTest(batch="formations",template="formation_arena") public static void plantsHaveBoundedScansAndOneExtraRoll(GameTestHelper h) {
        h.getLevel().setDayTime(6000);var p=player(h);var a=plate(h,CENTER,FormationKind.GATHERING,4,p);var b=plate(h,CENTER.east(),FormationKind.GATHERING,4,p);var pos=CENTER.south(2);
        h.setBlock(pos.below(),SpiritualFarming.SOIL.get());h.setBlock(pos,SpiritualFarming.CROPS.get(FoodPlantKind.STAR_FLOWER).get());
        FormationIndex.get(h.getLevel()).plantChanged(h.absolutePos(pos),false);int scanned=FormationIndex.get(h.getLevel()).scanPlants();h.assertTrue(scanned<=1024,"Per-dimension scan budget");
        Set<BlockPos> used=new HashSet<>();int first=a.growPlants(used),second=b.growPlants(used);h.assertTrue(first==1&&second==0,"One extra random tick per plant per second across overlapping formations");
        h.assertTrue(h.getBlockState(pos).getValue(FoodPlantBlock.AGE)==0,"Daytime star flower still cannot grow");
        h.setBlock(pos,Blocks.AIR);FormationIndex.get(h.getLevel()).plantChanged(h.absolutePos(pos),true);h.assertTrue(a.growPlants(new HashSet<>())==0,"Broken plant removed from cache");h.succeed();
    }
    @GameTest(batch="formations",template="formation_arena") public static void guardianProtectsOnlyInsideAndChargesOnce(GameTestHelper h) {
        var p=player(h);var a=plate(h,CENTER,FormationKind.GUARDIAN,4,p);var b=plate(h,CENTER.east(),FormationKind.GUARDIAN,4,p);a.active();b.active();double before=a.energy()+b.energy();
        BlockPos inside=h.absolutePos(CENTER.south(2)),inside2=inside.east(),outside=h.absolutePos(CENTER.south(12));var blocks=new ArrayList<>(List.of(inside,inside2,outside));
        FormationEvents.protectExplosion(h.getLevel(),blocks);h.assertTrue(blocks.equals(List.of(outside)),"Only protected blocks removed from explosion");
        h.assertTrue(Math.abs(before-a.energy()-b.energy()-5)<.000001,"Overlapping protection charges one plate once per explosion");
        energy(a,0);energy(b,0);var unprotected=new ArrayList<>(List.of(inside));FormationEvents.protectExplosion(h.getLevel(),unprotected);h.assertTrue(unprotected.size()==1,"No paid protection without energy");h.succeed();
    }
    @GameTest(batch="formations",template="formation_arena") public static void guardianSpawnSourcesAndGriefing(GameTestHelper h) {
        var p=player(h);var plate=plate(h,CENTER,FormationKind.GUARDIAN,4,p);var zombie=target(h,CENTER.east(2));
        for(var source:new MobSpawnType[]{MobSpawnType.NATURAL,MobSpawnType.SPAWNER,MobSpawnType.COMMAND,MobSpawnType.SPAWN_EGG,MobSpawnType.EVENT})h.assertTrue(FormationEvents.blocksNatural(h.getLevel(),zombie.blockPosition(),source,zombie)==(source==MobSpawnType.NATURAL),"Only natural hostile spawns blocked");
        var pos=h.absolutePos(CENTER.south(2));h.setBlock(CENTER.south(2),Blocks.STONE);var event=new net.minecraftforge.event.entity.living.LivingDestroyBlockEvent(zombie,pos,Blocks.STONE.defaultBlockState());
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event);h.assertTrue(event.isCanceled(),"Monster block destruction event canceled");
        plate.guardPulse();h.assertTrue(zombie.getEffect(MobEffects.MOVEMENT_SLOWDOWN).getAmplifier()==2&&zombie.hasEffect(MobEffects.WEAKNESS),"Guardian debuffs intruder");
        var enderman=h.spawn(EntityType.ENDERMAN,CENTER.north(2));var grief=new net.minecraftforge.event.entity.EntityMobGriefingEvent(enderman);net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(grief);h.assertTrue(grief.getResult()==Event.Result.DENY,"Enderman griefing denied inside field");
        enderman.setPos(h.absolutePos(CENTER.east(9)).getCenter());var border=new net.minecraftforge.event.entity.EntityMobGriefingEvent(enderman);net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(border);h.assertTrue(border.getResult()==Event.Result.DENY,"Enderman pickup reach cannot cross protected boundary");
        boolean oldRule=h.getLevel().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);double beforeRule=plate.energy();
        h.getLevel().getGameRules().getRule(GameRules.RULE_MOBGRIEFING).set(false,h.getLevel().getServer());
        try{var alreadyDenied=new net.minecraftforge.event.entity.EntityMobGriefingEvent(enderman);net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(alreadyDenied);h.assertTrue(plate.energy()==beforeRule,"Already disabled mob griefing costs no interception energy");}finally{h.getLevel().getGameRules().getRule(GameRules.RULE_MOBGRIEFING).set(oldRule,h.getLevel().getServer());}
        p.setHealth(10);h.assertTrue(FormationEvents.guardianRecovery(p)&&p.getHealth()==11,"Short refreshed regeneration still restores health");h.succeed();
    }
    @GameTest(batch="formations",template="formation_arena") public static void assaultChargesPerTargetAndHonorsOwner(GameTestHelper h) {
        var p=player(h);h.getLevel().players().add(p);
        try {
            var plate=plate(h,CENTER,FormationKind.ASSAULT,4,p);var mobs=new ArrayList<net.minecraft.world.entity.monster.Zombie>();for(int i=0;i<6;i++)mobs.add(target(h,CENTER.offset(i-2,0,2)));
            energy(plate,20.5);h.assertTrue(plate.attackPulse()==2&&plate.energy()==0,"Only two targets attacked with twenty attack energy");h.assertTrue(mobs.stream().filter(e->e.getHealth()<100).count()==2,"Exactly two victims damaged");
            for(var mob:mobs){mob.invulnerableTime=0;mob.setHealth(100);mob.clearFire();}energy(plate,100.5);h.assertTrue(plate.attackPulse()==5,"Five target cap");
            double remaining=plate.energy();h.getLevel().players().remove(p);h.assertTrue(!plate.active()&&plate.status()==FormationEntity.Status.OWNER&&plate.energy()==remaining,"Offline owner pauses without upkeep");
        } finally { h.getLevel().players().remove(p); }h.succeed();
    }
    @GameTest(batch="formations",template="formation_arena") public static void independentAssaultAndVulnerabilityDoNotMultiply(GameTestHelper h) {
        var p=player(h);h.getLevel().players().add(p);
        try {
            core(h,CENTER,3);var a=plate(h,CENTER,FormationKind.ASSAULT,4,p);var b=plate(h,CENTER.east(),FormationKind.ASSAULT,4,p);var mob=target(h,CENTER.south(2));
            int first=a.attackPulse(),second=b.attackPulse();h.assertTrue(first==1&&second==1&&mob.getHealth()==80,"Independent plates bypass shared damage cooldown: hits="+first+","+second+" health="+mob.getHealth());
            var melee=new net.minecraftforge.event.entity.living.LivingHurtEvent(mob,mob.damageSources().playerAttack(p),8);net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(melee);h.assertTrue(melee.getAmount()==10,"Melee vulnerability applied once");
            var talisman=new TalismanProjectile(Talismans.PROJECTILE.get(),h.getLevel());talisman.setOwner(p);var spell=new net.minecraftforge.event.entity.living.LivingHurtEvent(mob,mob.damageSources().indirectMagic(talisman,p),8);net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(spell);h.assertTrue(spell.getAmount()==10,"Talisman vulnerability once");
            var own=new DamageSource(h.getLevel().registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE).getHolderOrThrow(Formations.DAMAGE),null,p);var formationHit=new net.minecraftforge.event.entity.living.LivingHurtEvent(mob,own,10);net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(formationHit);h.assertTrue(formationHit.getAmount()==10,"Formation damage never amplifies itself");
        } finally { h.getLevel().players().remove(p); }h.succeed();
    }
    @GameTest(batch="formations",template="formation_arena",timeoutTicks=80) public static void assaultRetainsPlayerKillLootAndExperience(GameTestHelper h) {
        var p=player(h);h.getLevel().players().add(p);var plate=plate(h,CENTER,FormationKind.ASSAULT,4,p);for(var floor:BlockPos.betweenClosed(CENTER.offset(-2,-1,0),CENTER.offset(2,-1,4)))h.setBlock(floor,Blocks.STONE);var beast=Beasts.TYPES.get(BeastKind.WOLF).get().create(h.getLevel());beast.setPos(h.absolutePos(CENTER.south(2)).getCenter());beast.setNoAi(true);beast.setHealth(10);h.getLevel().addFreshEntity(beast);
        h.assertTrue(plate.attackPulse()==1&&beast.isDeadOrDying(),"Assault kills credited target");
        h.runAfterDelay(25,()->{try {var area=plate.bounds();h.assertTrue(h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,area).stream().anyMatch(e->e.getItem().is(Beasts.material("wolf_pelt"))),"Normal beast loot kept");h.assertTrue(!h.getLevel().getEntitiesOfClass(ExperienceOrb.class,area).isEmpty(),"Player kill experience spawned");h.succeed();}finally{h.getLevel().players().remove(p);}});
    }
    @GameTest(batch="formations",template="formation_arena") public static void menuPermissionsAndFlagChunkUnload(GameTestHelper h) {
        var owner=player(h);var other=player(h);var at=CENTER.offset(-Math.floorMod(h.absolutePos(CENTER).getX(),16),0,0);owner.setPos(h.absolutePos(at).getCenter());var plate=plate(h,at,FormationKind.GATHERING,4,owner);
        h.assertTrue(plate.canManage(owner)&&!plate.canManage(other),"Only owner manages plate");var menu=new FormationMenu(93,owner.getInventory(),plate,new net.minecraft.world.inventory.SimpleContainerData(5));h.assertTrue(menu.stillValid(owner)&&!menu.stillValid(other),"Menu enforces permission");
        h.assertTrue(plate.active(),"Initially active");var flagChunk=new ChunkPos(h.absolutePos(at.west(4)));var index=FormationIndex.get(h.getLevel());index.chunkChanged(flagChunk,false);h.assertTrue(!plate.active(),"Unloaded flag chunk pauses structure");index.chunkChanged(flagChunk,true);h.assertTrue(plate.active(),"Flag chunk reload restores structure");
        var saved=plate.saveWithoutMetadata();var restored=new FormationEntity(plate.getBlockPos(),plate.getBlockState());restored.load(saved);h.assertTrue(restored.energy()==plate.energy()&&restored.getItem(0).is(Formations.TOKENS.get(FormationKind.GATHERING).get()),"Inventory and remaining energy survive reload");
        plate.removeItem(0,1);h.assertTrue(!plate.active(),"Removing token immediately withdraws field");h.succeed();
    }
    @GameTest(batch="formations",template="formation_arena") public static void craftingMaterialsAndCounts(GameTestHelper h) {
        var level=h.getLevel();
        for(String id:new String[]{"formation_plate","formation_flag","formation_heart_stone","gathering_formation_token","guardian_formation_token","assault_formation_token"}) {
            var recipe=level.getRecipeManager().byKey(new net.minecraft.resources.ResourceLocation(Xiuxian.ID,id)).orElseThrow();
            for(var ingredient:recipe.getIngredients())for(var item:ingredient.getItems())h.assertTrue(!(item.getItem() instanceof InitiationItem)&&!item.is(CultivationResources.PILL.get()),"No medicine dependency");
            h.assertTrue(recipe.getResultItem(level.registryAccess()).getCount()==(id.equals("formation_flag")?4:1),"Recipe output count");
            if(id.equals("formation_heart_stone"))h.assertTrue(recipe.getIngredients().size()==4&&recipe.getIngredients().stream().allMatch(i->i.test(new ItemStack(CultivationResources.STONE_2.get()))),"Heart costs four tier-two stones");
        }h.succeed();
    }
}
