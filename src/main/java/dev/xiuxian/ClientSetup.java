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
    static final KeyMapping CAST = new KeyMapping("key.xiuxian.sword_cast", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, "key.categories.xiuxian");
    static final KeyMapping CYCLE = new KeyMapping("key.xiuxian.sword_cycle", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Z, "key.categories.xiuxian");
    static final KeyMapping RECALL = new KeyMapping("key.xiuxian.sword_recall", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, "key.categories.xiuxian");
    @SubscribeEvent public static void keys(RegisterKeyMappingsEvent event) { event.register(STATUS); event.register(CAST); event.register(CYCLE); event.register(RECALL); }
    @SubscribeEvent public static void renderers(net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers event) { event.registerEntityRenderer(SwordItems.FLYING.get(), net.minecraft.client.renderer.entity.ThrownItemRenderer::new); }
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
                while (CAST.consumeClick()) if (mc.player != null && mc.screen == null) Cultivation.CHANNEL.sendToServer(new SwordCombat.Action("cast"));
                while (CYCLE.consumeClick()) if (mc.player != null && mc.screen == null) Cultivation.CHANNEL.sendToServer(new SwordCombat.Action("cycle"));
                while (RECALL.consumeClick()) if (mc.player != null && mc.screen == null) Cultivation.CHANNEL.sendToServer(new SwordCombat.Action("recall"));
                while (STATUS.consumeClick()) if (mc.player != null && mc.screen == null) mc.setScreen(new StatusScreen());
            }
        }
        @SubscribeEvent public static void logout(net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggingOut event) { ClientState.clear(); }
    }
    static class StatusScreen extends Screen {
        private float panelScale;
        private int panelLeft, panelTop;
        StatusScreen() { super(Component.translatable("screen.xiuxian.status")); }
        @Override protected void init() {
            panelScale=Math.min(1f,Math.min((width-12f)/300,(height-12f)/255));
            panelLeft=(int)((width-300*panelScale)/2);
            panelTop=(int)((height-255*panelScale)/2);
            addRenderableWidget(net.minecraft.client.gui.components.Button.builder(Component.translatable("screen.xiuxian.manuals"), b -> minecraft.setScreen(new ManualScreen())).bounds(panelLeft+(int)(18*panelScale),panelTop+(int)(228*panelScale),(int)(264*panelScale),(int)(20*panelScale)).build());
            Cultivation.CHANNEL.sendToServer(new CultivationFoundation.Refresh());
        }
        @Override public boolean isPauseScreen() { return false; }
        @Override public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
            renderBackground(g);
            g.pose().pushPose();
            g.pose().translate(panelLeft,panelTop,0);
            g.pose().scale(panelScale,panelScale,1);
            g.fill(-1,-1,301,256,0xff709c82);
            g.fill(0,0,300,255,0xff17251f);
            g.drawCenteredString(font,title,150,14,0xffd7f0bd);
            g.drawString(font,Component.translatable("screen.xiuxian.player",minecraft.player.getName()),18,35,0xffffff);
            g.drawString(font,Component.translatable(ClientState.level>0?"realm.xiuxian.qi_"+ClientState.level:"screen.xiuxian.mortal"),18,53,0xffd7f0bd);
            boolean full=ClientState.level==9&&ClientState.required>0&&ClientState.points>=ClientState.required;
            Component progress=Component.translatable(ClientState.level==0?"screen.xiuxian.progress_locked":full?"screen.xiuxian.progress_full":"screen.xiuxian.progress",ClientState.points,ClientState.required);
            g.drawString(font,progress,18,72,0xffc3cec5);
            double ratio=ClientState.required>0?Math.max(0,Math.min(1,(double)ClientState.points/ClientState.required)):0;
            bar(g,88,ratio,0xff79b777);
            g.drawCenteredString(font,String.format(java.util.Locale.ROOT,"%.1f%%",ratio*100),150,90,0xffffffff);
            var state=ClientState.foundation;
            Component mana=state==null?Component.translatable("screen.xiuxian.mana_syncing"):Component.translatable("screen.xiuxian.mana",String.format(java.util.Locale.ROOT,"%.1f",state.mana()),String.format(java.util.Locale.ROOT,"%.0f",state.maximum()));
            g.drawString(font,mana,18,110,0xffb0d8e9);
            double manaRatio=state!=null&&state.maximum()>0?Math.max(0,Math.min(1,state.mana()/state.maximum())):0;
            bar(g,124,manaRatio,0xff67b8d6);
            String health=String.format(java.util.Locale.ROOT,"%.1f / %.1f",minecraft.player.getHealth(),minecraft.player.getMaxHealth());
            g.drawString(font,Component.translatable("screen.xiuxian.health",health),18,144,0xffead0c5);
            g.drawString(font,Component.translatable("screen.xiuxian.attack",String.format(java.util.Locale.ROOT,"%.1f",ClientState.attackDamage)),18,159,0xffead0c5);
            var manual=state==null?null:ManualDefinition.find(state.active());
            Component manualName=manual==null?Component.translatable("manual.xiuxian.tier_"+ClientState.technique):Component.literal(manual.name());
            g.drawString(font,Component.translatable("screen.xiuxian.technique",manualName),18,174,0xffc3cec5);
            if(ClientState.level>0&&!full)g.drawString(font,Component.translatable(ClientState.rate>0?"screen.xiuxian.breathing":"screen.xiuxian.breathing_paused",String.format(java.util.Locale.ROOT,"%.2f",ClientState.rate)),18,190,0xff99ab9d);
            var food=minecraft.player.getEffect(CultivationResources.NOURISHMENT.get());
            if(food!=null)g.drawString(font,Component.translatable("screen.xiuxian.food_time",food.getDuration()/20),18,205,0xffc4d998);
            if(state!=null&&state.blocked()&&!full)g.drawString(font,Component.translatable("screen.xiuxian.manual_gate"),18,219,0xffe2c38d);
            g.pose().popPose();
            super.render(g,mouseX,mouseY,partial);
        }
        private void bar(GuiGraphics g,int y,double fraction,int color) {
            g.fill(18,y,282,y+14,0xff435449);g.fill(19,y+1,281,y+13,0xff0d1712);g.fill(19,y+1,19+(int)(262*fraction),y+13,color);
        }
        @Override public boolean keyPressed(int key,int scan,int mods) {
            if(STATUS.matches(key,scan)){onClose();return true;}
            return super.keyPressed(key,scan,mods);
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

