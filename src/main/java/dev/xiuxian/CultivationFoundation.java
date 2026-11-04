package dev.xiuxian;

import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.NetworkDirection;
import java.util.*;

/** Persistent aptitude, learned manuals, and recoverable spiritual energy. */
public final class CultivationFoundation {
    private static final String KEY = "xiuxian_foundation";
    static CompoundTag data(Player player) {
        return player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getCompound(KEY);
    }
    static void save(Player player, CompoundTag tag) {
        CompoundTag persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        persisted.put(KEY, tag);
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
    }
    public static void ensure(ServerPlayer player) {
        if (data(player).contains("Roots")) return;
        CompoundTag tag = new CompoundTag();
        List<String> elements = new ArrayList<>(ManualDefinition.ELEMENTS);
        Collections.shuffle(elements, new Random(player.getUUID().getMostSignificantBits() ^ player.getUUID().getLeastSignificantBits()));
        int count = 1 + player.getRandom().nextInt(5);
        ListTag roots = new ListTag();
        for (String element : elements.subList(0, count)) roots.add(StringTag.valueOf(element));
        tag.put("Roots", roots);
        tag.putInt("Grade", player.getRandom().nextInt(3));
        tag.putDouble("Mana", 0);
        save(player, tag);
    }
    public static List<String> roots(Player player) {
        return data(player).getList("Roots", 8).stream().map(Tag::getAsString).toList();
    }
    public static List<String> known(Player player) {
        return data(player).getList("Known", 8).stream().map(Tag::getAsString).filter(id -> ManualDefinition.find(id) != null).toList();
    }
    public static ManualDefinition active(Player player) { return ManualDefinition.find(data(player).getString("Active")); }
    public static double aptitude(Player player) {
        int count = roots(player).size();
        if (count == 0) return 1; // Legacy fake players and data before first login.
        double number = switch(count) { case 1 -> 1; case 2 -> 1.2; case 3 -> .85; case 4 -> .7; default -> .55; };
        return number * Cultivation.ROOT_GRADE_SPEEDS.get(Math.max(0,Math.min(2,data(player).getInt("Grade")))).get();
    }
    public static int coverage(Player player) {
        var manual = active(player);
        return manual != null ? manual.coverage() : Cultivation.technique(player) > 0 ? 9 : 1;
    }
    public static boolean blocked(Player player) {
        var progress = Cultivation.progress(player);
        return progress.full(Cultivation.progressBase()) && (progress.level() == 9 || progress.level() >= coverage(player));
    }
    public static boolean select(ServerPlayer player, String id) {
        var manual = ManualDefinition.find(id);
        if (manual == null || !known(player).contains(id) || !Cultivation.initiated(player)) return false;
        CompoundTag tag = data(player);
        if (active(player)==null || !manual.family().equals(active(player).family())) { ManualPractice.reset(tag); tag.putInt("SelectedSpell",0); }
        tag.putString("Active", id);
        if(manual.sword()){int pages=SpellManuals.learned(player,id);tag.putString("SwordArt",(pages&1)!=0?SwordPages.art(manual,0):(pages&2)!=0&&manual.coverage()>=6?SwordPages.art(manual,1):"recall");}
        save(player, tag);
        Cultivation.reconcileManual(player);
        Cultivation.sync(player);
        return true;
    }
    public static boolean learn(ServerPlayer player, String id) {
        ensure(player);
        var manual = ManualDefinition.find(id);
        if (manual == null || !Cultivation.initiated(player)) return false;
        if (!manual.sword() && !roots(player).containsAll(manual.elements())) return false;
        CompoundTag tag = data(player);
        ListTag learned = tag.getList("Known", 8);
        if (!known(player).contains(id)) learned.add(StringTag.valueOf(id));
        tag.put("Known", learned);
        save(player, tag);
        return select(player, id);
    }
    public static double maxMana(Player player) { return Cultivation.initiated(player) ? 80 + Cultivation.progress(player).level() * 20 + ForgingEffects.maxManaBonus(player) : 0; }
    public static double mana(Player player) { return Math.min(maxMana(player), Math.max(0, data(player).getDouble("Mana"))); }
    public static void restoreMana(ServerPlayer player, double amount) {
        if (!Cultivation.initiated(player) || !Double.isFinite(amount) || amount <= 0) return;
        CompoundTag tag = data(player);
        tag.putDouble("Mana", Math.min(maxMana(player), mana(player) + amount));
        save(player, tag);
        Cultivation.sync(player);
    }
    public static boolean spendMana(ServerPlayer player, double amount) {
        if (!Double.isFinite(amount) || amount <= 0 || mana(player) < amount) return false;
        CompoundTag tag = data(player); tag.putDouble("Mana", mana(player) - amount); save(player, tag); Cultivation.sync(player); return true;
    }
    public static void regenerate(ServerPlayer player) {
        if (!Cultivation.initiated(player) || player.isSpectator() || !player.isAlive()) return;
        boolean combat = player.tickCount - Math.max(player.getLastHurtByMobTimestamp(), player.getLastHurtMobTimestamp()) < 200;
        var manual = active(player);
        double recovery = (combat ? .5 : 2) * (manual == null ? 1 : manual.cultivationMultiplier());
        CompoundTag tag = data(player); tag.putDouble("Mana", Math.min(maxMana(player), mana(player) + recovery)); save(player, tag);
    }
    public static void clone(Player original, Player replacement) {
        var tag=data(original).copy();
        tag.remove("SwordProjection"); tag.remove("SwordPreparingUntil"); tag.remove("SwordChargedUntil");
        save(replacement, tag);
    }
    public record State(String roots, int grade, String active, String known, double mana, double maximum, boolean blocked) {}
    public record Select(String id) {}
    public record Refresh() {}
    public static void registerNetwork() {
        Cultivation.CHANNEL.messageBuilder(Refresh.class,6,NetworkDirection.PLAY_TO_SERVER)
            .encoder((m,b)->{}).decoder(b->new Refresh())
            .consumerMainThread((m,c)->{var p=c.get().getSender();if(p!=null){ensure(p);Cultivation.sync(p);}c.get().setPacketHandled(true);}).add();
        Cultivation.CHANNEL.messageBuilder(State.class, 1, NetworkDirection.PLAY_TO_CLIENT)
            .encoder((m,b)->{b.writeUtf(m.roots);b.writeVarInt(m.grade);b.writeUtf(m.active);b.writeUtf(m.known);b.writeDouble(m.mana);b.writeDouble(m.maximum);b.writeBoolean(m.blocked);})
            .decoder(b->new State(b.readUtf(),b.readVarInt(),b.readUtf(),b.readUtf(),b.readDouble(),b.readDouble(),b.readBoolean()))
            .consumerMainThread((m,c)->{ClientState.foundation=m;c.get().setPacketHandled(true);}).add();
        Cultivation.CHANNEL.messageBuilder(Select.class, 2, NetworkDirection.PLAY_TO_SERVER)
            .encoder((m,b)->b.writeUtf(m.id,64)).decoder(b->new Select(b.readUtf(64)))
            .consumerMainThread((m,c)->{var player=c.get().getSender();if(player!=null)select(player,m.id);c.get().setPacketHandled(true);}).add();
    }
    public static void sync(ServerPlayer player) {
        var manual=active(player);
        Cultivation.CHANNEL.send(PacketDistributor.PLAYER.with(()->player),new State(String.join("、",roots(player)),data(player).getInt("Grade"),manual==null?"":manual.id(),String.join(",",known(player)),mana(player),maxMana(player),blocked(player)));
    }
}
