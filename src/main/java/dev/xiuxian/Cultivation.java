package dev.xiuxian;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.*;
import net.minecraftforge.network.simple.SimpleChannel;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Xiuxian.ID)
public class Cultivation {
    private static final String INITIATED = "xiuxian_initiated";
    private static final String LEVEL = "xiuxian_level";
    private static final String POINTS = "xiuxian_progress";
    private static final UUID HEALTH_MODIFIER = UUID.fromString("4626619e-0b74-4bec-87f7-6020b97d5d12");
    private static final UUID DAMAGE_MODIFIER = UUID.fromString("30daf777-d52c-4a86-aa07-a17409999d91");
    private static final String TECHNIQUE = "xiuxian_technique";
    private static final String FRACTION = "xiuxian_fraction";
    public static final ForgeConfigSpec.IntValue DIFFICULTY, STONE_POINTS, STONE_2_POINTS, PILL_POINTS, FOOD_SECONDS;
    public static final ForgeConfigSpec.DoubleValue MANUAL_1_MULTIPLIER, MANUAL_2_MULTIPLIER, MANUAL_3_MULTIPLIER, FOOD_MULTIPLIER;
    public static final java.util.List<ForgeConfigSpec.IntValue> VEIN_RADII;
    public static final java.util.List<ForgeConfigSpec.DoubleValue> VEIN_MULTIPLIERS;
    public static final ForgeConfigSpec.IntValue VEIN_REGEN_SECONDS;
    public static final ForgeConfigSpec.DoubleValue ALCHEMY_BASE, ALCHEMY_REALM, ALCHEMY_FURNACE, ALCHEMY_CAP;
    public static final ForgeConfigSpec.IntValue MANUAL_PRACTICE_BASE;
    public static final ForgeConfigSpec.DoubleValue MANUAL_POWER_PER_RANK;
    public static final java.util.List<ForgeConfigSpec.DoubleValue> ROOT_GRADE_SPEEDS, MANUAL_QUALITY_SPEEDS;
    public static final ForgeConfigSpec.DoubleValue SWORD_WEAPON_SCALE, SPELL_PRIMARY_DAMAGE, SPELL_SECONDARY_DAMAGE;
    public static final ForgeConfigSpec CONFIG;
    public static final ForgeConfigSpec.DoubleValue HERB_CHANCE, LIQUID_CHANCE, HEALTH_PER_LEVEL, DAMAGE_PER_LEVEL;
    public static final ForgeConfigSpec.IntValue PROGRESS_PER_SECOND, BASE_PROGRESS;
    public static final ForgeConfigSpec.BooleanValue GIVE_GUIDE_ON_FIRST_JOIN, AUTO_CULTIVATION;
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(new ResourceLocation(Xiuxian.ID, "state"), () -> "11", "11"::equals, "11"::equals);
    static {
        var builder = new ForgeConfigSpec.Builder();
        // Preserve the existing config key when replacing leaves with whole herbs.
        HERB_CHANCE = builder.defineInRange("leafChance", 0.25, 0, 1);
        LIQUID_CHANCE = builder.defineInRange("liquidChance", 0.75, 0, 1);
        GIVE_GUIDE_ON_FIRST_JOIN = builder.define("giveGuideOnFirstJoin", true);
        AUTO_CULTIVATION = builder.define("automaticCultivation", true);
        PROGRESS_PER_SECOND = builder.defineInRange("cultivationPerSecond", 1, 1, 1000000);
        BASE_PROGRESS = builder.defineInRange("baseProgressPerLevel", 300, 1, 1000000);
        HEALTH_PER_LEVEL = builder.defineInRange("healthPerLevel", 2.0, 0.0, 100.0);
        DAMAGE_PER_LEVEL = builder.defineInRange("attackDamagePerLevel", 1.0, 0.0, 100.0);
        DIFFICULTY = builder.defineInRange("cultivationDifficultyMultiplier", 10, 1, 100);
        MANUAL_1_MULTIPLIER = builder.defineInRange("manualTier1Multiplier", 1.25, 1.0, 10.0);
        MANUAL_2_MULTIPLIER = builder.defineInRange("manualTier2Multiplier", 1.5, 1.0, 10.0);
        MANUAL_3_MULTIPLIER = builder.defineInRange("manualTier3Multiplier", 2.0, 1.0, 10.0);
        FOOD_MULTIPLIER = builder.defineInRange("spiritFoodMultiplier", 1.5, 1.0, 10.0);
        FOOD_SECONDS = builder.defineInRange("spiritFoodDurationSeconds", 300, 1, 3600);
        STONE_POINTS = builder.defineInRange("spiritStoneProgress", 20, 1, 1000000);
        STONE_2_POINTS = builder.defineInRange("tier2SpiritStoneProgress", 180, 1, 1000000);
        PILL_POINTS = builder.defineInRange("qiGatheringPillProgress", 100, 1, 1000000);
        VEIN_RADII = java.util.List.of(builder.defineInRange("smallVeinRadius",16,1,128),builder.defineInRange("mediumVeinRadius",32,1,128),builder.defineInRange("largeVeinRadius",48,1,128));
        VEIN_MULTIPLIERS = java.util.List.of(builder.defineInRange("smallVeinMultiplier",1.25,1,10),builder.defineInRange("mediumVeinMultiplier",1.5,1,10),builder.defineInRange("largeVeinMultiplier",2.0,1,10));
        VEIN_REGEN_SECONDS = builder.defineInRange("veinRegenerationSeconds",1800,1,86400);
        ALCHEMY_BASE=builder.defineInRange("alchemyBaseChance",0.6,0,1);
        ALCHEMY_REALM=builder.defineInRange("alchemyRealmBonus",0.15,0,1);
        ALCHEMY_FURNACE=builder.defineInRange("alchemyFurnaceBonus",0.15,0,1);
        ALCHEMY_CAP=builder.defineInRange("alchemyChanceCap",0.95,0,1);
        MANUAL_PRACTICE_BASE=builder.defineInRange("manualPracticeBase",300,1,1000000);
        MANUAL_POWER_PER_RANK=builder.defineInRange("manualPowerPerRank",0.08,0,1);
        ROOT_GRADE_SPEEDS=java.util.List.of(builder.defineInRange("lowRootSpeed",0.6,0.1,10),builder.defineInRange("middleRootSpeed",1.0,0.1,10),builder.defineInRange("highRootSpeed",1.8,0.1,10));
        MANUAL_QUALITY_SPEEDS=java.util.List.of(builder.defineInRange("yellowLowManualSpeed",1.25,1,20),builder.defineInRange("yellowMiddleManualSpeed",1.75,1,20),builder.defineInRange("yellowHighManualSpeed",2.5,1,20),builder.defineInRange("mysticLowManualSpeed",3.5,1,20));
        SWORD_WEAPON_SCALE=builder.defineInRange("flyingSwordWeaponScale",0.4,0,1);
        SPELL_PRIMARY_DAMAGE=builder.defineInRange("primarySpellDamage",8.0,0,100);
        SPELL_SECONDARY_DAMAGE=builder.defineInRange("secondarySpellDamage",12.0,0,100);
        CONFIG = builder.build();
    }
    private static CompoundTag data(Player player) { return player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG); }
    public static CultivationProgress progress(Player player) {
        CompoundTag tag = data(player);
        // Old saves stored only the initiation boolean: they become Qi Refining I.
        int level = tag.contains(LEVEL) ? tag.getInt(LEVEL) : tag.getBoolean(INITIATED) ? 1 : 0;
        int points = Math.max(0, tag.getInt(POINTS));
        if (level == 9 && tag.getInt("xiuxian_schema") == 0) points = Math.multiplyExact(progressBase(), 9);
        return new CultivationProgress(Math.max(0, Math.min(CultivationProgress.MAX_LEVEL, level)), points);
    }
    private static void save(Player player, CultivationProgress state) {
        CompoundTag tag = data(player);
        tag.putInt("xiuxian_schema", 1);
        tag.putBoolean(INITIATED, state.level() > 0);
        tag.putInt(LEVEL, state.level());
        tag.putInt(POINTS, state.points());
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, tag);
    }
    public static int progressBase() { return Math.multiplyExact(BASE_PROGRESS.get(), DIFFICULTY.get()); }
    public static int technique(Player player) { return Math.max(0, Math.min(3, data(player).getInt(TECHNIQUE))); }
    public static double cultivationRate(Player player) {
        return CultivationFoundation.blocked(player) ? 0 : breathingPotential(player);
    }
    public static double breathingPotential(Player player) {
        if (!AUTO_CULTIVATION.get() || !player.isAlive() || player.isSpectator() || !initiated(player)) return 0;
        double multiplier = switch (technique(player)) {
            case 1 -> MANUAL_1_MULTIPLIER.get();
            case 2 -> MANUAL_2_MULTIPLIER.get();
            case 3 -> MANUAL_3_MULTIPLIER.get();
            default -> 1.0;
        };
        var manual = CultivationFoundation.active(player);
        if (manual != null) multiplier = manual.cultivationMultiplier();
        multiplier *= CultivationFoundation.aptitude(player);
        if (player.hasEffect(CultivationResources.NOURISHMENT.get())) multiplier *= FOOD_MULTIPLIER.get();
        if (player.level() instanceof net.minecraft.server.level.ServerLevel server) multiplier *= SpiritVeins.get(server).multiplier(player);
        multiplier *= player.hasEffect(Alchemy.QI.get()) ? 1.35 : player.hasEffect(Alchemy.WARM.get()) ? 1.15 : 1;
        return PROGRESS_PER_SECOND.get() * multiplier;
    }
    public static boolean learnTechnique(ServerPlayer player, int tier) {
        if (!initiated(player) || tier < 1 || tier > 3 || tier <= technique(player)) return false;
        CompoundTag tag = data(player);
        tag.putInt(TECHNIQUE, tier);
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, tag);
        player.displayClientMessage(Component.translatable("message.xiuxian.manual_learned", tier), false);
        sync(player);
        return true;
    }
    private static void advance(ServerPlayer player, int points) {
        CultivationProgress before = progress(player), after = before.advance(points, progressBase(), CultivationFoundation.coverage(player));
        if (!after.equals(before)) save(player, after);
        if (CultivationFoundation.blocked(player)) {
            CompoundTag capped = data(player); capped.putDouble(FRACTION, 0);
            player.getPersistentData().put(Player.PERSISTED_NBT_TAG, capped);
        }
        float oldMaximum = player.getMaxHealth();
        applyAttributes(player);
        if (after.level() > before.level()) {
            ManualPractice.promote(player, after.level()-before.level());
            healNewCapacity(player, oldMaximum);
            player.displayClientMessage(Component.translatable("message.xiuxian.level_up", after.level()), false);
        }
    }
    public static void reconcileManual(ServerPlayer player) { advance(player, 0); }
    public static boolean addProgress(ServerPlayer player, int amount) {
        if (!initiated(player) || CultivationFoundation.blocked(player) || amount <= 0) return false;
        advance(player, amount);
        sync(player);
        return true;
    }
    public static boolean initiated(Player player) { return progress(player).level() > 0; }
    public static void initiate(ServerPlayer player) {
        if (initiated(player)) return;
        save(player, new CultivationProgress(1, 0));
        float oldMaximum = player.getMaxHealth();
        applyAttributes(player);
        healNewCapacity(player, oldMaximum);
        sync(player);
    }
    private static void modifier(AttributeInstance attribute, UUID id, String name, double amount) {
        AttributeModifier old = attribute.getModifier(id);
        if (old != null && Double.compare(old.getAmount(), amount) == 0) return;
        if (old != null) attribute.removeModifier(id);
        // Permanent modifiers restore max health before the saved current health is loaded.
        if (amount != 0) attribute.addPermanentModifier(new AttributeModifier(id, name, amount, AttributeModifier.Operation.ADDITION));
    }
    static void applyAttributes(ServerPlayer player) {
        CultivationProgress state = progress(player);
        modifier(player.getAttribute(Attributes.MAX_HEALTH), HEALTH_MODIFIER, "xiuxian.realm_health", state.bonus(HEALTH_PER_LEVEL.get()));
        modifier(player.getAttribute(Attributes.ATTACK_DAMAGE), DAMAGE_MODIFIER, "xiuxian.realm_attack", state.bonus(DAMAGE_PER_LEVEL.get()));
        if (player.getHealth() > player.getMaxHealth()) player.setHealth(player.getMaxHealth());
    }
    private static void healNewCapacity(ServerPlayer player, float oldMaximum) {
        float gained = player.getMaxHealth() - oldMaximum;
        if (gained > 0) player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + gained));
    }
    public record State(int level, int points, int required, double attackDamage, int technique, double rate) {}
    public static void registerNetwork() {
        CultivationFoundation.registerNetwork();
        SwordCombat.network();
        SwordBookNetwork.register();
        SpellManuals.network();
        CHANNEL.messageBuilder(State.class, 0, NetworkDirection.PLAY_TO_CLIENT)
            .encoder((msg, buf) -> { buf.writeVarInt(msg.level()); buf.writeVarInt(msg.points()); buf.writeVarInt(msg.required()); buf.writeDouble(msg.attackDamage()); buf.writeVarInt(msg.technique()); buf.writeDouble(msg.rate()); })
            .decoder(buf -> new State(buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readDouble(), buf.readVarInt(), buf.readDouble()))
            .consumerMainThread((msg, ctx) -> { ClientState.level = msg.level(); ClientState.points = msg.points(); ClientState.required = msg.required(); ClientState.attackDamage = msg.attackDamage(); ClientState.technique = msg.technique(); ClientState.rate = msg.rate(); ctx.get().setPacketHandled(true); }).add();
    }
    public static void sync(ServerPlayer player) {
        CultivationFoundation.sync(player);
        SpellManuals.sync(player);
        CultivationProgress state = progress(player);
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new State(state.level(), state.points(), state.required(progressBase()), player.getAttributeValue(Attributes.ATTACK_DAMAGE), technique(player), cultivationRate(player)));
    }
    @SubscribeEvent public static void tick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player) || player.tickCount % 20 != 0) return;
        if (!player.isAlive()) return;
        CultivationFoundation.ensure(player);
        CultivationFoundation.regenerate(player);
        ManualPractice.train(player, breathingPotential(player));
        CompoundTag tag = data(player);
        double fraction = tag.getDouble(FRACTION);
        if (!Double.isFinite(fraction) || fraction < 0 || fraction >= 1) fraction = 0;
        CultivationGain gain = CultivationGain.accumulate(fraction, cultivationRate(player));
        tag.putDouble(FRACTION, gain.remainder());
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, tag);
        advance(player, gain.points());
        sync(player);
    }
    @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) { CultivationFoundation.ensure(player); save(player, progress(player)); applyAttributes(player); sync(player); }
    }
    @SubscribeEvent public static void respawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            applyAttributes(player);
            if (!event.isEndConquered()) player.setHealth(player.getMaxHealth());
            sync(player);
        }
    }
    @SubscribeEvent public static void dimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) { applyAttributes(player); sync(player); }
    }
    @SubscribeEvent public static void clone(PlayerEvent.Clone event) {
        save(event.getEntity(), progress(event.getOriginal()));
        CultivationFoundation.clone(event.getOriginal(), event.getEntity());
        CompoundTag tag = data(event.getEntity());
        tag.putInt(TECHNIQUE, technique(event.getOriginal()));
        tag.putDouble(FRACTION, data(event.getOriginal()).getDouble(FRACTION));
        event.getEntity().getPersistentData().put(Player.PERSISTED_NBT_TAG, tag);
        if (event.getEntity() instanceof ServerPlayer player) applyAttributes(player);
    }
}

