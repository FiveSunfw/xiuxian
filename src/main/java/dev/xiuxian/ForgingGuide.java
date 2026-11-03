package dev.xiuxian;

import com.mojang.brigadier.Command;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** A separate vanilla book, readable without the cultivation guide's custom viewer. */
@Mod.EventBusSubscriber(modid = Xiuxian.ID)
public final class ForgingGuide {
    private static final String RECEIVED = "xiuxian_forging_guide_received";

    public static ItemStack create() {
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        var tag = book.getOrCreateTag();
        tag.putString("title", "炼器指南");
        tag.putString("author", "Xiuxian");
        ListTag pages = new ListTag();
        for (int page = 1; page <= 5; page++) {
            pages.add(StringTag.valueOf(Component.Serializer.toJson(
                Component.translatable("guide.xiuxian.forging_" + page))));
        }
        tag.put("pages", pages);
        book.setHoverName(Component.translatable("guide.xiuxian.forging_title"));
        return book;
    }

    private static boolean give(ServerPlayer player) {
        ItemStack book = create();
        if (!player.getInventory().add(book) && player.drop(book, false) == null) return false;
        var persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        persisted.putBoolean(RECEIVED, true);
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
        return true;
    }

    @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && Cultivation.GIVE_GUIDE_ON_FIRST_JOIN.get()
            && !player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getBoolean(RECEIVED)) {
            give(player);
        }
    }

    @SubscribeEvent public static void commands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("xiuxian")
            .then(Commands.literal("forging_guide").executes(context -> {
                if (!give(context.getSource().getPlayerOrException())) return 0;
                return Command.SINGLE_SUCCESS;
            })));
    }
}
