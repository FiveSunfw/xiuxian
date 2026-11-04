package dev.xiuxian;

import com.mojang.brigadier.Command;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
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

@Mod.EventBusSubscriber(modid = Xiuxian.ID)
public class GuideBook {
    private static final String RECEIVED = "xiuxian_guide_received";

    public static ItemStack create() {
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        CompoundTag tag = book.getOrCreateTag();
        tag.putString("title", "引气入门");
        tag.putString("author", "Xiuxian");
        ListTag pages = new ListTag();
        addPage(pages, "guide.xiuxian.intro");
        addPage(pages, "guide.xiuxian.herbs");
        for (HerbKind kind : HerbKind.values()) {
            pages.add(StringTag.valueOf(Component.Serializer.toJson(Component.translatable("guide.xiuxian.species",
                Component.translatable("block.xiuxian." + kind.id),
                Component.translatable("herb.xiuxian." + kind.id + ".habitat"),
                Component.translatable("herb.xiuxian." + kind.id + ".appearance")))));
        }
        addPage(pages, "guide.xiuxian.planting");
        addPage(pages, "guide.xiuxian.furnace");
        addPage(pages, "guide.xiuxian.brewing", FurnaceEntity.BREW_TICKS / 20);
        addPage(pages, "guide.xiuxian.cultivation",
            Math.round(Cultivation.HERB_CHANCE.get() * 100), Math.round(Cultivation.LIQUID_CHANCE.get() * 100));
        for (int i=17;i<=31;i++) { addPage(pages, "guide.xiuxian.new_"+i+"_left"); addPage(pages, "guide.xiuxian.new_"+i+"_right"); }
        tag.put("pages", pages);
        book.setHoverName(Component.translatable("guide.xiuxian.title"));
        return book;
    }

    private static void addPage(ListTag pages, String key, Object... args) {
        pages.add(StringTag.valueOf(Component.Serializer.toJson(Component.translatable(key, args))));
    }

    public static boolean isGuide(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return stack.is(Items.WRITTEN_BOOK) && tag != null
            && "引气入门".equals(tag.getString("title")) && "Xiuxian".equals(tag.getString("author"));
    }

    @SubscribeEvent public static void preventVanillaViewer(net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickItem event) {
        if (!event.getLevel().isClientSide && isGuide(event.getItemStack())) {
            event.setCanceled(true);
            event.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
        }
    }

    private static boolean give(ServerPlayer player) {
        ItemStack book = create();
        if (!player.getInventory().add(book) && player.drop(book, false) == null) return false;
        CompoundTag persistent = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        persistent.putBoolean(RECEIVED, true);
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persistent);
        return true;
    }

    @SubscribeEvent public static void firstJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && Cultivation.GIVE_GUIDE_ON_FIRST_JOIN.get()
                && !player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getBoolean(RECEIVED)) {
            give(player);
        }
    }

    @SubscribeEvent public static void commands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("xiuxian")
            .then(Commands.literal("guide").executes(context -> {
                if (!give(context.getSource().getPlayerOrException())) {
                    context.getSource().sendFailure(Component.translatable("message.xiuxian.guide_failed"));
                    return 0;
                }
                return Command.SINGLE_SUCCESS;
            })));
    }
}

