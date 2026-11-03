package dev.xiuxian;

import net.minecraft.world.item.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;
import net.minecraftforge.registries.RegistryObject;
import java.util.*;

public final class FoundationItems {
    public static final Map<String, RegistryObject<Item>> MANUALS = new LinkedHashMap<>();
    public static final RegistryObject<Item> BLANK = Xiuxian.ITEMS.register("blank_manual", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> TESTER = Xiuxian.ITEMS.register("root_testing_stone", () -> new Item(new Item.Properties().stacksTo(1)) {
        @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
            if (player instanceof ServerPlayer server) {
                CultivationFoundation.ensure(server);
                var tag = CultivationFoundation.data(player);
                player.displayClientMessage(Component.translatable("message.xiuxian.roots",String.join("、",CultivationFoundation.roots(player)),Component.translatable("roots.xiuxian.grade_"+tag.getInt("Grade"))),false);
                Cultivation.sync(server);
            }
            return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand),level.isClientSide);
        }
    });
    static {
        for (var definition : ManualDefinition.ALL) MANUALS.put(definition.id(), Xiuxian.ITEMS.register(definition.id(), () -> new ManualItem(definition)));
    }
    public static void register() {}
    static class ManualItem extends Item {
        final ManualDefinition manual;
        ManualItem(ManualDefinition manual) { super(new Properties()); this.manual=manual; }
        @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
            ItemStack stack=player.getItemInHand(hand);
            if (player instanceof ServerPlayer server) {
                SpellManuals.open(server,manual.id(),hand);
            }
            return InteractionResultHolder.sidedSuccess(stack,level.isClientSide);
        }
    }
}
