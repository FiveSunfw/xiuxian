package dev.xiuxian;

import com.mojang.authlib.GameProfile;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.*;
import java.util.UUID;

@GameTestHolder(Xiuxian.ID)
@PrefixGameTestTemplate(false)
public class BalanceGameTests {
    @GameTest(template="empty") public static void growthWeaponAndProductionBalance(GameTestHelper h){
        var p=FakePlayerFactory.get(h.getLevel(),new GameProfile(UUID.randomUUID(),"BalanceTest"));CultivationFoundation.ensure(p);Cultivation.initiate(p);
        var tag=CultivationFoundation.data(p);ListTag roots=new ListTag();roots.add(StringTag.valueOf("金"));tag.put("Roots",roots);tag.putInt("Grade",0);CultivationFoundation.save(p,tag);
        double low=CultivationFoundation.aptitude(p);tag.putInt("Grade",2);CultivationFoundation.save(p,tag);
        h.assertTrue(CultivationFoundation.aptitude(p)/low>=2.9,"Root grades now have a substantial growth gap");
        h.assertTrue(ManualDefinition.find("lone_sword").cultivationMultiplier()/ManualDefinition.find("basic_sword").cultivationMultiplier()>=2.5,"Manual quality now matters for growth");
        CultivationFoundation.learn(p,"basic_sword");p.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.IRON_SWORD));
        var attack=p.getAttribute(Attributes.ATTACK_DAMAGE);
        for(var modifier:p.getMainHandItem().getAttributeModifiers(EquipmentSlot.MAINHAND).get(Attributes.ATTACK_DAMAGE))if(attack.getModifier(modifier.getId())==null)attack.addTransientModifier(modifier);
        var victim=EntityType.COW.create(h.getLevel());
        h.assertTrue(SwordCombat.attackBase(p,victim)<attack.getValue(),"Flying sword must discount the equipped weapon contribution");
        for(String manual:new String[]{"basic_sword","metal_origin"}){
            CultivationFoundation.learn(p,manual);
            var furnace=new AlchemyEntity(h.absolutePos(new net.minecraft.core.BlockPos(1,1,1)),Alchemy.FURNACE.get().defaultBlockState());furnace.setLevel(h.getLevel());
            var formula=Alchemy.formulas().get(0);furnace.setItem(0,new ItemStack(Alchemy.item("qi_gathering_formula")));furnace.setItem(1,new ItemStack(formula.main(),formula.mainCount()));furnace.setItem(2,new ItemStack(formula.auxiliary(),formula.auxiliaryCount()));furnace.setItem(3,new ItemStack(CultivationResources.STONE.get(),formula.stones()));furnace.setItem(4,new ItemStack(Items.COAL));
            h.assertTrue(furnace.start(p),"Both sword and spell cultivators can practice alchemy");
        }
        h.assertTrue(SpellManuals.damage(p,0)>8&&SpellManuals.damage(p,1)>12,"Spell damage receives larger baseline and realm growth");h.succeed();
    }
}
