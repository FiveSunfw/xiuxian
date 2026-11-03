package dev.xiuxian;

import com.mojang.authlib.GameProfile;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import java.util.UUID;

@GameTestHolder(Xiuxian.ID)
@PrefixGameTestTemplate(false)
public class CultivationAttributeGameTests {
    private static FakePlayer player(GameTestHelper helper, String name) {
        return FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.nameUUIDFromBytes(name.getBytes(java.nio.charset.StandardCharsets.UTF_8)), name));
    }
    private static void level(FakePlayer player, int level) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("xiuxian_initiated", level > 0);
        tag.putInt("xiuxian_level", level);
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, tag);
    }
    @GameTest(template = "empty")
    public static void legacyInitiationGetsRealAttributesOnce(GameTestHelper helper) {
        FakePlayer player = player(helper, "LegacyQiTest");
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("xiuxian_initiated", true);
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, tag);
        Cultivation.applyAttributes(player);
        Cultivation.applyAttributes(player);
        helper.assertTrue(Cultivation.progress(player).level() == 1, "Legacy boolean must migrate to level one");
        helper.assertTrue(player.getMaxHealth() == 22, "Level one must have real max health 22 without stacking");
        helper.assertTrue(player.getAttributeValue(Attributes.ATTACK_DAMAGE) == 2, "Level one must have real attack damage 2");
        helper.succeed();
    }
    @GameTest(template = "empty")
    public static void nextLevelReplacesPreviousBonuses(GameTestHelper helper) {
        FakePlayer player = player(helper, "SecondQiTest");
        level(player, 1); Cultivation.applyAttributes(player);
        level(player, 2); Cultivation.applyAttributes(player);
        Cultivation.applyAttributes(player);
        helper.assertTrue(player.getMaxHealth() == 24, "Level two must have max health 24");
        helper.assertTrue(player.getAttributeValue(Attributes.ATTACK_DAMAGE) == 3, "Level two must have attack damage 3");
        helper.succeed();
    }
    @GameTest(template = "empty")
    public static void preservesOtherAttributeSources(GameTestHelper helper) {
        FakePlayer player = player(helper, "ModdedQiTest");
        player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(30);
        player.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(5);
        level(player, 2); Cultivation.applyAttributes(player);
        helper.assertTrue(player.getMaxHealth() == 34, "Must preserve other health sources");
        helper.assertTrue(player.getAttributeValue(Attributes.ATTACK_DAMAGE) == 7, "Must preserve other attack sources");
        level(player, 0); Cultivation.applyAttributes(player);
        helper.assertTrue(player.getMaxHealth() == 30 && player.getAttributeValue(Attributes.ATTACK_DAMAGE) == 5, "Removing cultivation must only remove cultivation modifiers");
        helper.succeed();
    }
}
