package dev.xiuxian;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class ForgingScreen extends AbstractContainerScreen<ForgingMenu> {
    private Button startButton;

    public ForgingScreen(ForgingMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageHeight = 210;
        inventoryLabelY = 117;
    }

    @Override protected void init() {
        super.init();
        startButton = addRenderableWidget(Button.builder(Component.translatable("screen.xiuxian.forging_start"),
            button -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 0))
            .bounds(leftPos + 48, topPos + 96, 80, 20).build());
    }

    @Override protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos - 2, topPos - 2, leftPos + 178, topPos + imageHeight + 2, 0xff49382c);
        graphics.fill(leftPos, topPos, leftPos + 176, topPos + imageHeight, 0xffb9a17d);
        graphics.fill(leftPos + 4, topPos + 4, leftPos + 172, topPos + 122, 0xffd4c19d);
        for (var slot : menu.slots) {
            graphics.fill(leftPos + slot.x - 1, topPos + slot.y - 1, leftPos + slot.x + 17, topPos + slot.y + 17, 0xff463a30);
            graphics.fill(leftPos + slot.x, topPos + slot.y, leftPos + slot.x + 16, topPos + slot.y + 16, 0xff99876d);
        }
        graphics.fill(leftPos + 8, topPos + 86, leftPos + 98, topPos + 91, 0xff58483a);
        int filled = 90 * menu.progress() / menu.totalTicks();
        graphics.fill(leftPos + 8, topPos + 86, leftPos + 8 + filled, topPos + 91, 0xff63a99a);
        startButton.active = menu.preview().valid();
    }

    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 6, 0xff35291f, false);
        graphics.drawString(font, Component.translatable("screen.xiuxian.forging_skill", menu.skillLevel()), 105, 6, 0xff4b5a37, false);
        graphics.drawCenteredString(font, Component.translatable("screen.xiuxian.forging_input_equipment"), 25, 21, 0xff493a2b);
        graphics.drawCenteredString(font, Component.translatable("screen.xiuxian.forging_input_stone"), 61, 21, 0xff493a2b);
        graphics.drawCenteredString(font, Component.translatable("screen.xiuxian.forging_input_material"), 97, 21, 0xff493a2b);
        graphics.drawCenteredString(font, Component.translatable("screen.xiuxian.forging_output"), 151, 21, 0xff493a2b);

        ForgingRules.Preview preview = menu.preview();
        Component summary = preview.valid()
            ? Component.translatable("screen.xiuxian.forging_preview_tier", ForgingRules.qualityName(preview.tier()))
            : Component.translatable("screen.xiuxian.forging_preview_invalid", Component.translatable("forging.xiuxian.error." + preview.error(), ForgingSkill.requiredLevel(preview.tier())));
        graphics.drawString(font, summary, 8, 51, preview.valid() ? 0xff32624e : 0xff8e3f32, false);
        if (preview.valid()) {
            String material = ForgingRules.materialId(menu.container.getItem(2));
            Component effect = Component.translatable(ForgingRules.effectKey(material, preview.slot()));
            for (var line : font.split(effect, 160)) {
                graphics.drawString(font, line, 8, 62, 0xff463d32, false);
                break;
            }
            if (!preview.existing().isEmpty()) {
                String existing = String.join("、", preview.existing().stream().map(id ->
                    ForgedItemData.materialName(id).getString()).toList());
                graphics.drawString(font, Component.translatable("screen.xiuxian.forging_existing", existing), 8, 73, 0xff694f35, false);
            }
        }
        if (menu.progress() > 0) {
            graphics.drawString(font, Component.translatable("screen.xiuxian.forging_progress",
                menu.progress() / 20, menu.totalTicks() / 20), 101, 83, 0xff27675a, false);
        }
    }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
