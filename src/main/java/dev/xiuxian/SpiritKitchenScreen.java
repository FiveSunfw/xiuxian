package dev.xiuxian;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class SpiritKitchenScreen extends AbstractContainerScreen<SpiritKitchenMenu> {
    public SpiritKitchenScreen(SpiritKitchenMenu menu, Inventory inventory, Component title) { super(menu, inventory, title); imageHeight = 186; inventoryLabelY = 93; }
    @Override protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos-2, topPos-2, leftPos+178, topPos+188, 0xff563628);
        graphics.fill(leftPos, topPos, leftPos+176, topPos+186, 0xffd3bb93);
        graphics.fill(leftPos+4, topPos+4, leftPos+172, topPos+92, 0xffead6ad);
        for (var slot : menu.slots) {
            graphics.fill(leftPos+slot.x-1, topPos+slot.y-1, leftPos+slot.x+17, topPos+slot.y+17, 0xff57412f);
            graphics.fill(leftPos+slot.x, topPos+slot.y, leftPos+slot.x+16, topPos+slot.y+16, 0xffaa987c);
        }
        graphics.fill(leftPos+63, topPos+61, leftPos+129, topPos+68, 0xff725643);
        graphics.fill(leftPos+63, topPos+61, leftPos+63+66*menu.data.get(0)/800, topPos+68, 0xffe99846);
        int burn = menu.data.get(2) > 0 ? 18 * menu.data.get(1) / menu.data.get(2) : 0;
        graphics.fill(leftPos+42, topPos+63+18-burn, leftPos+47, topPos+81, menu.data.get(3)==1 ? 0xff76dfe0 : 0xffed953f);
    }
    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 6, 0xff513421, false);
        graphics.drawString(font, Component.translatable("screen.xiuxian.kitchen_inputs"), 8, 19, 0xff513421, false);
        graphics.drawString(font, Component.translatable("screen.xiuxian.kitchen_fuel"), 7, 82, 0xff513421, false);
    }
    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics); super.render(graphics, mouseX, mouseY, partialTick); renderTooltip(graphics, mouseX, mouseY);
    }
}
