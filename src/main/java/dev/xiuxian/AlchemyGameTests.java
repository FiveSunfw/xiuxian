package dev.xiuxian;
import net.minecraft.gametest.framework.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.*;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
@GameTestHolder(Xiuxian.ID)
@PrefixGameTestTemplate(false)
public class AlchemyGameTests {
    @GameTest(template="empty") public static void formulasAndBatchPersistence(GameTestHelper h){
        h.assertTrue(AlchemyEntity.success(1,1,1,1)==.6,"Base chance");h.assertTrue(AlchemyEntity.success(2,1,1,1)==.75,"Realm bonus");h.assertTrue(AlchemyEntity.success(1,2,1,1)==.75,"Furnace bonus");h.assertTrue(Math.abs(AlchemyEntity.success(2,2,1,1)-.9)<.00001,"Combined bonus");h.assertTrue(AlchemyEntity.success(8,8,1,1)==.95,"Chance cap");
        BlockPos pos=new BlockPos(1,1,1);h.setBlock(pos,Alchemy.FURNACE.get());var e=(AlchemyEntity)h.getBlockEntity(pos);var p=FakePlayerFactory.get(h.getLevel(),new GameProfile(UUID.randomUUID(),"Alchemist"));
        for(var f:Alchemy.formulas()){
            e.clearContent();e.setItem(0,new ItemStack(Alchemy.item(f.id()+"_formula")));e.setItem(1,new ItemStack(f.main(),f.mainCount()));e.setItem(2,new ItemStack(f.auxiliary(),f.auxiliaryCount()));e.setItem(3,new ItemStack(CultivationResources.STONE.get(),f.stones()));e.setItem(4,new ItemStack(Items.CHARCOAL));
            p.getPersistentData().remove(Player.PERSISTED_NBT_TAG);h.assertTrue(!e.start(p),"Mortal cannot start");var tag=new CompoundTag();tag.putBoolean("xiuxian_initiated",true);p.getPersistentData().put(Player.PERSISTED_NBT_TAG,tag);
            h.assertTrue(e.start(p),"Initiated player can start");h.assertTrue(!e.start(p),"Concurrent start rejected");h.assertTrue(e.getItem(0).getCount()==1&&e.getItem(1).isEmpty(),"Consume materials but preserve formula");
            e.chance=1;var saved=e.saveWithoutMetadata();var loaded=new AlchemyEntity(e.getBlockPos(),e.getBlockState());loaded.load(saved);h.assertTrue(!loaded.pending.isEmpty()&&loaded.chance==1,"Persist active batch");
            for(int i=0;i<600;i++)AlchemyEntity.tick(h.getLevel(),e.getBlockPos(),e.getBlockState(),e);h.assertTrue(e.getItem(5).is(f.result()),"Guaranteed success produces selected pill");
            e.setItem(5,ItemStack.EMPTY);e.pending=new ItemStack(f.result());e.chance=0;for(int i=0;i<600;i++)AlchemyEntity.tick(h.getLevel(),e.getBlockPos(),e.getBlockState(),e);h.assertTrue(e.getItem(5).is(Alchemy.item("pill_residue")),"Failure produces residue");
        }h.succeed();
    }
    @GameTest(template="empty") public static void auxiliaryBrews(GameTestHelper h){
        var water=net.minecraft.world.item.alchemy.PotionUtils.setPotion(new ItemStack(Items.POTION),net.minecraft.world.item.alchemy.Potions.WATER);
        h.assertTrue(FurnaceEntity.brewResult(new ItemStack(Xiuxian.HERB_ITEMS.get(HerbKind.WOOD).get()),water,new ItemStack(Items.WHEAT)).is(Alchemy.item("healing_liquid")),"Healing liquid");
        h.assertTrue(FurnaceEntity.brewResult(new ItemStack(Xiuxian.HERB_ITEMS.get(HerbKind.FROST).get()),water,new ItemStack(Xiuxian.HERB_ITEMS.get(HerbKind.MARSH).get())).is(Alchemy.item("cleansing_liquid")),"Cleansing liquid");
        h.assertTrue(FurnaceEntity.brewResult(new ItemStack(Xiuxian.HERB_ITEM.get()),water,new ItemStack(Xiuxian.HERB_ITEMS.get(HerbKind.WOOD).get())).is(Alchemy.item("warming_liquid")),"Warming liquid");
        h.assertTrue(FurnaceEntity.brewResult(new ItemStack(Xiuxian.HERB_ITEM.get()),water,new ItemStack(Items.DIRT)).isEmpty(),"Wrong auxiliary must not brew initiation liquid");h.succeed();
    }
}
