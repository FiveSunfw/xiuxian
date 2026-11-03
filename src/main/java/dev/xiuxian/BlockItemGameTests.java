package dev.xiuxian;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.BlockItem;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;

@GameTestHolder(Xiuxian.ID)
@PrefixGameTestTemplate(false)
public class BlockItemGameTests {
    @GameTest(template="empty")
    public static void everyModBlockHasGiveableItem(GameTestHelper helper){
        helper.assertTrue(Xiuxian.TAB.isPresent(),"Independent Xiuxian creative tab must be registered");
        var commandSource=helper.getLevel().getServer().createCommandSourceStack().withPermission(4);
        for(var entry:ForgeRegistries.BLOCKS.getEntries()){
            var id=entry.getKey().location();
            if(!id.getNamespace().equals(Xiuxian.ID))continue;
            helper.assertTrue(ForgeRegistries.ITEMS.containsKey(id),"Missing /give item for "+id);
            var item=ForgeRegistries.ITEMS.getValue(id);
            helper.assertTrue(item instanceof BlockItem blockItem&&blockItem.getBlock()==entry.getValue(),"Item ID must place matching block: "+id);
            var parsed=helper.getLevel().getServer().getCommands().getDispatcher().parse("give @p "+id,commandSource);
            helper.assertTrue(!parsed.getReader().canRead()&&parsed.getExceptions().isEmpty(),"/give must parse block item ID: "+id);
        }
        helper.succeed();
    }
}
