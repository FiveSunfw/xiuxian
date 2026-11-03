package dev.xiuxian;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = Xiuxian.ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {
    static final KeyMapping STATUS = new KeyMapping("key.xiuxian.status", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, "key.categories.xiuxian");
    @SubscribeEvent public static void keys(RegisterKeyMappingsEvent event) { event.register(STATUS); }
    @SubscribeEvent public static void setup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(Xiuxian.FURNACE_MENU.get(), FurnaceScreen::new);
            MenuScreens.register(Alchemy.MENU.get(), AlchemyScreen::new);
            for (var herb : Xiuxian.HERBS.values()) ItemBlockRenderTypes.setRenderLayer(herb.get(), RenderType.cutout());
        });
    }
    @Mod.EventBusSubscriber(modid = Xiuxian.ID, value = Dist.CLIENT)
    public static class Events {
        @SubscribeEvent public static void guide(net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickItem event) {
            if (event.getLevel().isClientSide && GuideBook.isGuide(event.getItemStack())) {
                Minecraft.getInstance().setScreen(new GuideScreen());
                event.setCanceled(true);
                event.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
            }
        }
        @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.END) {
                Minecraft mc = Minecraft.getInstance();
                while (STATUS.consumeClick()) if (mc.player != null && mc.screen == null) mc.setScreen(new StatusScreen());
            }
        }
        @SubscribeEvent public static void logout(net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggingOut event) { ClientState.clear(); }
    }
    static class StatusScreen extends Screen {
        StatusScreen() { super(Component.translatable("screen.xiuxian.status")); }
        @Override protected void init() {
            addRenderableWidget(net.minecraft.client.gui.components.Button.builder(Component.translatable("screen.xiuxian.manuals"), b -> minecraft.setScreen(new ManualScreen())).bounds(width/2-132,height/2+100,264,20).build());
        }
        @Override public boolean isPauseScreen() { return false; }
        @Override public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
            renderBackground(g);
            int x = width / 2 - 150, y = height / 2 - 128;
            g.fill(x-1, y-1, x+301, y+256, 0xff709c82);
            g.fill(x, y, x+300, y+255, 0xff17251f);
            g.drawCenteredString(font, title, width/2, y+15, 0xffd7f0bd);
            g.drawString(font, Component.translatable("screen.xiuxian.player", minecraft.player.getName()), x+18, y+40, 0xffffff);
            Component realm = Component.translatable(ClientState.level > 0 ? "realm.xiuxian.qi_" + ClientState.level : "screen.xiuxian.mortal");
            g.drawString(font, realm, x+18, y+61, 0xffd7f0bd);
            boolean full = ClientState.level == 9 && ClientState.required > 0 && ClientState.points >= ClientState.required;
            Component progress = Component.translatable(ClientState.level == 0 ? "screen.xiuxian.progress_locked" : full ? "screen.xiuxian.progress_full" : "screen.xiuxian.progress", ClientState.points, ClientState.required);
            g.drawString(font, progress, x+18, y+82, 0xffc3cec5);
            double ratio = full ? 1 : ClientState.required > 0 ? (double) ClientState.points / ClientState.required : 0;
            ratio = Math.max(0, Math.min(1, ratio));
            g.fill(x+18, y+98, x+282, y+112, 0xff435449);
            g.fill(x+19, y+99, x+281, y+111, 0xff0d1712);
            g.fill(x+19, y+99, x+19+(int)(262*ratio), y+111, 0xff79b777);
            g.drawCenteredString(font, String.format(java.util.Locale.ROOT, "%.1f%%", ratio * 100), width/2, y+101, 0xffffffff);
            String health = String.format(java.util.Locale.ROOT, "%.1f / %.1f", minecraft.player.getHealth(), minecraft.player.getMaxHealth());
            String damage = String.format(java.util.Locale.ROOT, "%.1f", ClientState.attackDamage);
            g.drawString(font, Component.translatable("screen.xiuxian.health", health), x+18, y+120, 0xffead0c5);
            g.drawString(font, Component.translatable("screen.xiuxian.attack", damage), x+18, y+139, 0xffead0c5);
            g.drawString(font, Component.translatable("screen.xiuxian.technique", Component.translatable("manual.xiuxian.tier_" + ClientState.technique)), x+18, y+158, 0xffc3cec5);
            if (ClientState.level > 0 && !full) {
                String rate = String.format(java.util.Locale.ROOT, "%.2f", ClientState.rate);
                g.drawString(font, Component.translatable(ClientState.rate > 0 ? "screen.xiuxian.breathing" : "screen.xiuxian.breathing_paused", rate), x+18, y+177, 0xff99ab9d);
            }
            var food = minecraft.player.getEffect(CultivationResources.NOURISHMENT.get());
            if (food != null) g.drawString(font, Component.translatable("screen.xiuxian.food_time", food.getDuration() / 20), x+18, y+193, 0xffc4d998);
            var state = ClientState.foundation;
            if (state != null) {
                g.drawString(font, Component.translatable("screen.xiuxian.mana", (int)state.mana(), (int)state.maximum()), x+18, y+209, 0xffb0d8e9);
                if (state.blocked() && !full) g.drawString(font, Component.translatable("screen.xiuxian.manual_gate"), x+18, y+223, 0xffe2c38d);
            }
            super.render(g, mouseX, mouseY, partial);
        }
        @Override public boolean keyPressed(int key, int scan, int mods) {
            if (STATUS.matches(key, scan)) { onClose(); return true; }
            return super.keyPressed(key, scan, mods);
        }
    }
    static class FurnaceScreen extends AbstractContainerScreen<FurnaceMenu> {
        FurnaceScreen(FurnaceMenu menu, Inventory inv, Component title) { super(menu, inv, title); }
        @Override protected void renderBg(GuiGraphics g, float partial, int mouseX, int mouseY) {
            g.fill(leftPos-1, topPos-1, leftPos+177, topPos+167, 0xff7fa68a);
            g.fill(leftPos, topPos, leftPos+176, topPos+166, 0xffc4d0bd);
            for (var slot : menu.slots) {
                g.fill(leftPos+slot.x-1, topPos+slot.y-1, leftPos+slot.x+17, topPos+slot.y+17, 0xff344f40);
                g.fill(leftPos+slot.x, topPos+slot.y, leftPos+slot.x+16, topPos+slot.y+16, 0xff8a9b80);
            }
            g.fill(leftPos+104, topPos+40, leftPos+127, topPos+47, 0xff344f40);
            g.fill(leftPos+104, topPos+40, leftPos+104+23*menu.progress()/FurnaceEntity.BREW_TICKS, topPos+47, 0xffa4e27e);
        }
        @Override protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
            g.drawString(font, title, titleLabelX, titleLabelY, 0xff344f40, false);
        }
        @Override public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
            renderBackground(g); super.render(g, mouseX, mouseY, partial); renderTooltip(g, mouseX, mouseY);
        }
    }
}

