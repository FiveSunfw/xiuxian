package dev.xiuxian;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.*;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.*;
import java.util.UUID;

@GameTestHolder(Xiuxian.ID)
@PrefixGameTestTemplate(false)
public class SpiritVeinGameTests {
    // Hash-map traversal order is not part of persisted vein or timer semantics.
    private static CompoundTag ordered(CompoundTag tag) {
        var cores=tag.getList("cores",10).stream().map(t->(CompoundTag)t).sorted(java.util.Comparator.comparingLong(t->t.getLong("pos"))).toList();
        net.minecraft.nbt.ListTag sortedCores=new net.minecraft.nbt.ListTag();
        for(var core:cores){
            net.minecraft.nbt.ListTag ores=new net.minecraft.nbt.ListTag();
            core.getList("ores",10).stream().map(t->(CompoundTag)t).sorted(java.util.Comparator.comparingLong(t->t.getLong("pos"))).forEach(ores::add);
            core.put("ores",ores);sortedCores.add(core);
        }
        tag.put("cores",sortedCores);return tag;
    }
    @GameTest(template="empty", timeoutTicks=100)
    public static void auraAndPersistence(GameTestHelper h) {
        var level=h.getLevel(); var data=SpiritVeins.get(level);
        BlockPos pos=h.absolutePos(new BlockPos(5,2,5));
        level.setBlockAndUpdate(pos,VeinBlocks.CORE.get().defaultBlockState());
        var core=new SpiritVeins.Core(pos,2,pos.getY()+70); data.add(core);
        var p=FakePlayerFactory.get(level,new GameProfile(UUID.randomUUID(),"VeinTester"));
        p.setPos(pos.getX()+.5,pos.getY()+70,pos.getZ()+.5);
        h.assertTrue(Math.abs(data.multiplier(p)-1.5)<.00001,"Deep core must reach surface");
        p.setPos(pos.getX()+16.5,pos.getY()+70,pos.getZ()+.5);
        h.assertTrue(Math.abs(data.multiplier(p)-1.25)<.00001,"Half radius must give half bonus");
        var q=FakePlayerFactory.get(level,new GameProfile(UUID.randomUUID(),"VeinOther"));
        q.setPos(pos.getX()+32.5,pos.getY()+70,pos.getZ()+.5);
        h.assertTrue(data.multiplier(q)==1,"Second player outside aura");
        var restored=SpiritVeins.load(data.save(new CompoundTag()));
        h.assertTrue(restored.multiplier(p)==data.multiplier(p),"Saved core must preserve aura");
        data.remove(pos); h.assertTrue(data.multiplier(p)==1,"Removed core must stop aura");
        h.succeed();
    }
    @GameTest(template="empty", timeoutTicks=100)
    public static void miningAndRegrowth(GameTestHelper h) {
        var level=h.getLevel(); var data=SpiritVeins.get(level);
        BlockPos pos=h.absolutePos(new BlockPos(4,2,4)), ore=pos.east();
        level.setBlockAndUpdate(pos,VeinBlocks.CORE.get().defaultBlockState());
        level.setBlockAndUpdate(ore,CultivationResources.ORE.get().defaultBlockState());
        var core=new SpiritVeins.Core(pos,1,pos.getY()+20); core.ores.put(ore,0); data.add(core);
        var p=FakePlayerFactory.get(level,new GameProfile(UUID.randomUUID(),"Miner"));
        var state=level.getBlockState(ore); level.removeBlock(ore,false);
        CultivationResources.ORE.get().playerDestroy(level,p,ore,state,null,new ItemStack(Items.STONE_PICKAXE));
        h.assertTrue(level.getBlockState(ore).is(VeinBlocks.DEPLETED.get()),"Mining must leave depleted ore");
        core.ores.put(ore,Cultivation.VEIN_REGEN_SECONDS.get()-1);
        var restored=SpiritVeins.load(data.save(new CompoundTag()));
        h.assertTrue(ordered(restored.save(new CompoundTag())).equals(ordered(data.save(new CompoundTag()))),"Timer must survive NBT round trip");
        h.runAfterDelay(25,()->{
            h.assertTrue(level.getBlockState(ore).is(CultivationResources.ORE.get()),"Loaded ore must regenerate");
            level.removeBlock(pos,false);
            h.assertTrue(!data.deplete(ore),"Core removal must sever regeneration"); h.succeed();
        });
    }
    @GameTest(template="empty", timeoutTicks=100)
    public static void generatedCoreRegisters(GameTestHelper h) {
        for (int x=0;x<7;x++) for(int y=0;y<7;y++) for(int z=0;z<7;z++) h.setBlock(new BlockPos(x,y,z),net.minecraft.world.level.block.Blocks.STONE);
        BlockPos pos=h.absolutePos(new BlockPos(3,3,3));
        var context=new net.minecraft.world.level.levelgen.feature.FeaturePlaceContext<>(java.util.Optional.empty(),h.getLevel(),h.getLevel().getChunkSource().getGenerator(),net.minecraft.util.RandomSource.create(42),pos,net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration.INSTANCE);
        h.assertTrue(new SpiritVeinFeature(12,16).place(context),"Vein generation must succeed");
        h.assertTrue(h.getLevel().getBlockState(pos).is(VeinBlocks.CORE.get()),"Generation must place a core");
        var entity=(VeinBlocks.CoreEntity)h.getLevel().getBlockEntity(pos);
        h.assertTrue(entity.generated.getList("cores",10).getCompound(0).getList("ores",10).size()>=11,"Generated ores must bind to core");
        entity.onLoad();
        h.runAfterDelay(2,()->{
            h.assertTrue(SpiritVeins.get(h.getLevel()).deplete(pos.east()) || SpiritVeins.get(h.getLevel()).save(new CompoundTag()).getList("cores",10).size()>0,"Loaded generated core must register");
            h.getLevel().removeBlock(pos,false); h.succeed();
        });
    }

}

