package dev.xiuxian;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(Xiuxian.ID)
@PrefixGameTestTemplate(false)
public final class ForgingOutputGameTests {
    @GameTest(template = "empty")
    public static void finishedEquipmentSynchronizesAndRemainsRetrievable(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, ForgingContent.BASIC_FORGING_TABLE.get());
        ForgingTableEntity table = (ForgingTableEntity)helper.getBlockEntity(pos);
        var player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "ForgeOutput"));
        table.setItem(0, new ItemStack(ForgingContent.BLANKS.get("spirit_gold_sword").get()));
        table.setItem(1, new ItemStack(CultivationResources.STONE.get()));
        table.setItem(2, new ItemStack(Beasts.material("spirit_horn")));
        helper.assertTrue(table.start(player), "Valid infusion starts");

        var clientMirror = new ForgingTableEntity(table.getBlockPos(), table.getBlockState());
        clientMirror.load(table.saveWithoutMetadata());
        clientMirror.setItem(1, new ItemStack(CultivationResources.STONE.get(), 2));
        helper.assertTrue(clientMirror.getItem(1).getCount() == 2, "Busy container accepts server slot synchronization");

        for (int i = 0; i < ForgingTableEntity.INFUSE_TICKS; i++)
            ForgingTableEntity.tick(helper.getLevel(), table.getBlockPos(), table.getBlockState(), table);
        ItemStack output = table.getItem(3).copy();
        helper.assertTrue(!output.isEmpty() && ForgedItemData.materials(output).contains("spirit_horn"), "Finished output exists on server");
        var restored = new ForgingTableEntity(table.getBlockPos(), table.getBlockState());
        restored.load(table.saveWithoutMetadata());
        helper.assertTrue(ItemStack.isSameItemSameTags(output, restored.getItem(3)), "Finished output survives save/load");

        clientMirror.load(table.saveWithoutMetadata());
        clientMirror.setItem(3, ItemStack.EMPTY);
        var menu = new ForgingMenu(1, player.getInventory(), clientMirror, new SimpleContainerData(3), 1);
        helper.assertTrue(!menu.getSlot(3).mayPlace(output), "Player cannot insert into output slot");
        menu.getSlot(3).set(output.copy());
        helper.assertTrue(ItemStack.isSameItemSameTags(output, menu.getSlot(3).getItem()), "Server output is visible in synchronized slot");
        helper.assertTrue(ItemStack.isSameItemSameTags(output, clientMirror.removeItem(3, 1)), "Synchronized output can be taken");
        helper.succeed();
    }
}
