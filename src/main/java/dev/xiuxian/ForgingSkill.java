package dev.xiuxian;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Xiuxian.ID)
public final class ForgingSkill {
    private ForgingSkill() {}

    private static CompoundTag data(Player player) {
        return player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getCompound("xiuxian_forging");
    }
    private static void save(Player player, CompoundTag data) {
        CompoundTag persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        persisted.put("xiuxian_forging", data);
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
    }
    public static int level(Player player) { return Math.max(1, Math.min(9, 1 + data(player).getInt("Experience"))); }

    public static int requiredLevel(int qualityTier) { return switch (qualityTier) { case 1 -> 1; case 2 -> 3; default -> 6; }; }

    static int award(ServerLevel level, java.util.UUID owner) {
        if (owner == null) return 1;
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(owner);
        if (player != null) return gain(player, 1);
        ForgingProgressData.get(level).queue(owner);
        return -1;
    }

    private static int gain(ServerPlayer player, int amount) {
        CompoundTag data = data(player);
        int previous = level(player);
        int experience = Math.min(8, data.getInt("Experience") + amount);
        data.putInt("Experience", experience);
        int next = Math.min(9, 1 + experience);
        save(player, data);
        if (next > previous)
            player.displayClientMessage(net.minecraft.network.chat.Component.translatable("forging.xiuxian.skill_up", next), false);
        return next;
    }

    @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            int pending = ForgingProgressData.get(player.serverLevel()).take(player.getUUID());
            if (pending > 0) gain(player, pending);
        }
    }
}
