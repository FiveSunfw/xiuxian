package dev.xiuxian;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Holds profession experience earned while its owner is offline. */
public final class ForgingProgressData extends SavedData {
    private final Map<UUID, Integer> pending = new HashMap<>();

    public static ForgingProgressData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage()
            .computeIfAbsent(ForgingProgressData::load, ForgingProgressData::new, "xiuxian_forging_progress");
    }

    public static ForgingProgressData load(CompoundTag tag) {
        ForgingProgressData data = new ForgingProgressData();
        for (Tag value : tag.getList("Pending", Tag.TAG_COMPOUND)) {
            CompoundTag entry = (CompoundTag)value;
            if (entry.hasUUID("Player")) data.pending.put(entry.getUUID("Player"), Math.max(0, entry.getInt("Experience")));
        }
        return data;
    }

    public void queue(UUID player) {
        pending.merge(player, 1, Integer::sum);
        setDirty();
    }

    public int take(UUID player) {
        Integer experience = pending.remove(player);
        if (experience != null) setDirty();
        return experience == null ? 0 : experience;
    }

    @Override public CompoundTag save(CompoundTag tag) {
        ListTag entries = new ListTag();
        pending.forEach((player, experience) -> {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("Player", player);
            entry.putInt("Experience", experience);
            entries.add(entry);
        });
        tag.put("Pending", entries);
        return tag;
    }
}
