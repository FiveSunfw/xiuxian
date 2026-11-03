package dev.xiuxian;

import net.minecraft.gametest.framework.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.gametest.*;

@GameTestHolder(Xiuxian.ID)
@PrefixGameTestTemplate(false)
public class BeastGameTests {
    @GameTest(template="empty") public static void speciesAttributesAndGuaranteedDrops(GameTestHelper h){
        for(var kind:BeastKind.values()){
            var beast=Beasts.TYPES.get(kind).get().create(h.getLevel());var pos=h.absolutePos(new BlockPos(1,1,1));beast.setPos(pos.getX()+.5,pos.getY(),pos.getZ()+.5);h.getLevel().addFreshEntity(beast);
            h.assertTrue(beast.getMaxHealth()==kind.health&&beast.getAttributeValue(Attributes.ATTACK_DAMAGE)==kind.damage,"Species has fixed tier attributes: "+kind.id);
            h.assertTrue(beast.getMaxSpawnClusterSize()==(kind==BeastKind.WOLF?2:1),"Pack size limit");
            h.assertTrue(beast.removeWhenFarAway(100000)==(kind!=BeastKind.KING&&kind!=BeastKind.PYTHON),"Rare bosses remain for later challenges");
            beast.dropCustomDeathLoot(beast.damageSources().generic(),0,true);
            var items=h.getLevel().getEntitiesOfClass(ItemEntity.class,beast.getBoundingBox().inflate(2));
            String guaranteed=switch(kind){case RABBIT->"spirit_horn";case WOLF->"wolf_pelt";case BOAR->"rockhide_plate";case VIPER->"viper_fang";case SPIDER->"frost_silk";case EAGLE->"wind_feather";case LIZARD->"thunder_scale";case KING->"king_tusk";case PYTHON->"beast_marrow";};
            h.assertTrue(items.stream().anyMatch(e->e.getItem().is(Beasts.material(guaranteed))),"Guaranteed species drop");
            if(kind==BeastKind.KING)h.assertTrue(items.stream().filter(e->e.getItem().is(Beasts.material("thick_rock_plate"))).mapToInt(e->e.getItem().getCount()).sum()==2,"King drops exactly two thick plates");
            if(kind==BeastKind.PYTHON)h.assertTrue(items.stream().filter(e->e.getItem().is(Beasts.material("jade_scale"))).mapToInt(e->e.getItem().getCount()).sum()>=2,"Python drops at least two scales");
            items.forEach(Entity::discard);beast.discard();
        }h.succeed();
    }
    @GameTest(template="empty") public static void venomAndSilkAreRealEffects(GameTestHelper h){
        var target=h.spawn(EntityType.COW,new BlockPos(1,1,2));target.setNoAi(true);target.setNoGravity(true);
        var viper=Beasts.TYPES.get(BeastKind.VIPER).get().create(h.getLevel());var pos=h.absolutePos(new BlockPos(1,1,1));viper.setPos(pos.getX()+.5,pos.getY(),pos.getZ()+.5);h.getLevel().addFreshEntity(viper);
        viper.releaseAttack(target);h.assertTrue(target.hasEffect(MobEffects.POISON)&&target.getHealth()<target.getMaxHealth(),"Viper bite deals real damage and poison");
        var spider=Beasts.TYPES.get(BeastKind.SPIDER).get().create(h.getLevel());spider.setPos(viper.position());h.getLevel().addFreshEntity(spider);
        var silk=new FrostSilk(Beasts.SILK.get(),h.getLevel());silk.setOwner(spider);silk.onHitEntity(new net.minecraft.world.phys.EntityHitResult(target));
        h.assertTrue(target.hasEffect(MobEffects.MOVEMENT_SLOWDOWN),"Frost silk slows target");
        var cut=new FrostSilk(Beasts.SILK.get(),h.getLevel());cut.hurt(target.damageSources().generic(),1);h.assertTrue(cut.isRemoved(),"Silk projectile can be cut");
        viper.discard();spider.discard();h.succeed();
    }
    @GameTest(template="empty") public static void biomeSpawnListsAreRegistered(GameTestHelper h){
        var biomes=h.getLevel().registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.BIOME);
        String[] expected={"plains","forest","meadow","swamp","snowy_taiga","meadow","stony_peaks","dripstone_caves","swamp"};int index=0;
        for(var kind:BeastKind.values()){
            var biome=biomes.get(new net.minecraft.resources.ResourceLocation("minecraft",expected[index++]));
            h.assertTrue(biome.getMobSettings().getMobs(MobCategory.MONSTER).unwrap().stream().anyMatch(entry->entry.type==Beasts.TYPES.get(kind).get()&&entry.maxCount<=(kind==BeastKind.WOLF?2:1)),"Biome modifier registers species: "+kind.id);
        }
        h.assertTrue(!SpiritBeast.nearWater(h.getLevel(),h.absolutePos(new BlockPos(1,1,1))),"Dry site rejected for python habitat");
        h.setBlock(new BlockPos(1,0,1),net.minecraft.world.level.block.Blocks.WATER);
        h.assertTrue(SpiritBeast.nearWater(h.getLevel(),h.absolutePos(new BlockPos(1,1,1))),"Water-edge habitat accepted");h.succeed();
    }
}
