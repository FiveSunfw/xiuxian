package dev.xiuxian;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.ItemLike;
import org.lwjgl.glfw.GLFW;

/** Read-only illustrated viewer for both existing and newly issued guide books. */
public class GuideScreen extends Screen {
    private static final int WIDTH = 420, HEIGHT = 270;
    private static final ResourceLocation PAPER = new ResourceLocation(Xiuxian.ID, "textures/gui/guide_book.png");
    private int page;
    private float scale, left, top;
    private final HerbKind[] herbs = HerbKind.values();

    public GuideScreen() { super(Component.translatable("guide.xiuxian.contents")); }
    private Component name(HerbKind kind) { return Component.translatable("block.xiuxian." + kind.id); }
    private Component label(String key) { return Component.translatable("guide.xiuxian." + key); }
    private Component chapter(int index) {
        if (index >= 1 && index <= 8) return name(herbs[index - 1]);
        return label(switch (index) {
            case 9 -> "plant_title";
            case 10 -> "furnace_title";
            case 11 -> "brew_title";
            case 12 -> "cultivate_title";
            case 13 -> "resource_title";
            case 14 -> "food_title";
            case 15 -> "pill_title";
            case 16 -> "stone_title";
            case 17 -> "journey_title";
            case 18 -> "roots_title";
            case 19 -> "energy_title";
            case 20 -> "manual_title";
            case 21 -> "sword_start_title";
            case 22 -> "sword_attack_title";
            case 23 -> "sword_defense_title";
            case 24 -> "sword_styles_title";
            case 25, 26 -> "spell_title";
            case 27, 28, 29 -> "beast_title";
            default -> "contents";
        });
    }
    @Override protected void init() {
        scale = Math.min(1.25f, Math.min((width - 12f) / WIDTH, (height - 12f) / HEIGHT));
        left = (width - WIDTH * scale) / 2;
        top = (height - HEIGHT * scale) / 2;
    }
    private void icon(GuiGraphics g, ItemStack stack, int x, int y, float size) {
        g.pose().pushPose();
        g.pose().translate(x, y, 0);
        g.pose().scale(size / 16, size / 16, 1);
        g.renderItem(stack, 0, 0);
        g.pose().popPose();
    }
    private int paragraph(GuiGraphics g, Component text, int x, int y, int width) {
        for (var line : font.split(text, width)) {
            g.drawString(font, line, x, y, 0xff473a2c, false);
            y += 11;
        }
        return y;
    }
    private void heading(GuiGraphics g, Component text, int x, int y) {
        g.drawString(font, text, x, y, 0xff694827, false);
        g.fill(x, y + 13, x + 157, y + 14, 0xffbca375);
    }
    private ItemStack herb(HerbKind kind) { return new ItemStack(Xiuxian.HERB_ITEMS.get(kind).get()); }
    private ItemStack elixir(HerbKind kind) { return new ItemStack(Xiuxian.ELIXIRS.get(kind).get()); }

    @Override public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        renderBackground(g);
        float mx = (mouseX - left) / scale, my = (mouseY - top) / scale;
        g.pose().pushPose();
        g.pose().translate(left, top, 0);
        g.pose().scale(scale, scale, 1);
        g.blit(PAPER, 0, 0, 0, 0, WIDTH, HEIGHT, WIDTH, HEIGHT);
        if (page == 0) {
            heading(g, title, 29, 27);
            for (int i = 1; i <= 12; i++) {
                int y = 46 + (i - 1) * 16;
                if (mx >= 26 && mx <= 195 && my >= y && my < y + 16) g.fill(26, y, 196, y + 16, 0x407c592f);
                ItemStack stack = i <= 8 ? herb(herbs[i - 1]) : new ItemStack(i == 10 ? Xiuxian.FURNACE_ITEM.get() : Items.BOOK);
                icon(g, stack, 28, y, 14);
                g.drawString(font, chapter(i), 47, y + 3, 0xff473a2c, false);
            }
            heading(g, label("atlas"), 229, 27);
            paragraph(g, label("overview"), 229, 50, 159);
            for (int i = 0; i < 8; i++) icon(g, herb(herbs[i]), 234 + i % 4 * 40, 158 + i / 4 * 40, 28);
        } else if (page >= 17) {
            heading(g, chapter(page), 29, 27);
            paragraph(g, label("new_"+page+"_left"), 29, 50, 159);
            heading(g, label("details"), 229, 27);
            paragraph(g, label("new_"+page+"_right"), 229, 50, 159);
        } else if (page <= 8) {
            HerbKind kind = herbs[page - 1];
            heading(g, name(kind), 29, 27);
            paragraph(g, Component.translatable("herb.xiuxian." + kind.id + ".environment"), 32, 50, 152);
            icon(g, herb(kind), 66, 83, 88);
            g.fill(44, 180, 181, 181, 0xffbca375);
            ItemLike terrain = switch (kind) {
                case FROST -> Blocks.SNOW_BLOCK;
                case STONE -> Blocks.STONE;
                case SUN, TIDE -> Blocks.SAND;
                case MARSH -> Blocks.MUD;
                case MOSS -> Blocks.DEEPSLATE;
                case WOOD -> Blocks.OAK_LOG;
                default -> Blocks.GRASS_BLOCK;
            };
            icon(g, new ItemStack(terrain), 49, 195, 30);
            icon(g, herb(kind), 98, 198, 25);
            icon(g, elixir(kind), 146, 198, 25);
            heading(g, label("habitat"), 229, 27);
            int y = paragraph(g, Component.translatable("herb.xiuxian." + kind.id + ".habitat"), 229, 49, 159) + 12;
            heading(g, label("appearance"), 229, y);
            y = paragraph(g, Component.translatable("herb.xiuxian." + kind.id + ".appearance"), 229, y + 21, 159) + 12;
            heading(g, label("uses"), 229, y);
            paragraph(g, label("common_use"), 229, y + 21, 159);
        } else {
            heading(g, chapter(page), 29, 27);
            Component text = switch (page) {
                case 13 -> label("manuals");
                case 14 -> label("food");
                case 15 -> label("pills");
                case 16 -> label("stones");
                case 9 -> label("planting");
                case 10 -> label("furnace");
                case 11 -> Component.translatable("guide.xiuxian.brewing", FurnaceEntity.BREW_TICKS / 20);
                default -> Component.translatable("guide.xiuxian.cultivation", Math.round(Cultivation.HERB_CHANCE.get() * 100), Math.round(Cultivation.LIQUID_CHANCE.get() * 100));
            };
            String body = text.getString();
            int headerEnd = body.indexOf("\n\n");
            paragraph(g, Component.literal(headerEnd >= 0 ? body.substring(headerEnd + 2) : body), 29, 50, 159);
            heading(g, page == 9 ? label("atlas") : chapter(page), 229, 27);
            if (page >= 13) {
                net.minecraft.world.level.ItemLike[] items = switch (page) {
                    case 13 -> new net.minecraft.world.level.ItemLike[]{FoundationItems.BLANK.get(), FoundationItems.TESTER.get(), FoundationItems.MANUALS.get("basic_sword").get()};
                    case 14 -> new net.minecraft.world.level.ItemLike[]{Items.WHEAT, Items.APPLE, CultivationResources.MEAL.get()};
                    case 15 -> new net.minecraft.world.level.ItemLike[]{Alchemy.FURNACE_ITEM.get(), Alchemy.item("qi_gathering_formula"), CultivationResources.PILL.get(), Alchemy.item("pill_residue")};
                    default -> new net.minecraft.world.level.ItemLike[]{CultivationResources.ORE_ITEM.get(), VeinBlocks.CORE_ITEM.get(), VeinBlocks.DEPLETED_ITEM.get()};
                };
                for (int i=0;i<items.length;i++) icon(g, new ItemStack(items[i]), 239+(i%2)*76, 79+(i/2)*82, 48);
            } else if (page == 10) {
                for (int row = 0; row < 3; row++) for (int col = 0; col < 3; col++) {
                    int x = 241 + col * 42, y = 58 + row * 42;
                    g.fill(x-1, y-1, x+35, y+35, 0x30694b2a);
                    if (col != 1) icon(g, new ItemStack(Blocks.COBBLESTONE), x, y, 34);
                    else if (row == 0) icon(g, new ItemStack(CultivationResources.STONE.get()), x, y, 34);
                    else if (row == 2) icon(g, new ItemStack(Blocks.CAMPFIRE), x, y, 34);
                }
                g.drawString(font, "↓", 303, 191, 0xff694827, false);
                icon(g, new ItemStack(Xiuxian.FURNACE_ITEM.get()), 292, 205, 32);
            } else if (page == 11) {
                icon(g, new ItemStack(Xiuxian.FURNACE_ITEM.get()), 287, 52, 48);
                int i = (int) (System.currentTimeMillis() / 2500 % herbs.length);
                icon(g, herb(herbs[i]), 232, 130, 32);
                g.drawString(font, "+", 270, 143, 0xff694827, false);
                icon(g, PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER), 281, 130, 32);
                g.drawString(font, "→", 319, 143, 0xff694827, false);
                icon(g, elixir(herbs[i]), 341, 130, 32);
            } else {
                for (int i = 0; i < herbs.length; i++) icon(g, page == 12 ? elixir(herbs[i]) : herb(herbs[i]), 239 + i % 4 * 39, 85 + i / 4 * 61, 30);
            }
        }
        g.drawString(font, label("beast_tab"), 140, 251, 0xfff2ddb5, false);
        g.drawString(font, label("spell_tab"), 325, 251, 0xfff2ddb5, false);
        g.drawString(font, label("cultivation_tab"), 85, 251, 0xfff2ddb5, false);
        g.drawString(font, "‹", 27, 251, 0xfff2ddb5, false);
        g.drawCenteredString(font, label("index"), 210, 251, 0xfff2ddb5);
        g.drawString(font, label("resource_tab"), 275, 251, 0xfff2ddb5, false);
        g.drawString(font, "›", 386, 251, 0xfff2ddb5, false);
        g.drawString(font, "×", 394, 6, 0xfff2ddb5, false);
        g.pose().popPose();
        super.render(g, mouseX, mouseY, partial);
    }
    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);
        double x = (mouseX-left)/scale, y = (mouseY-top)/scale;
        if (x >= 387 && x <= 410 && y >= 0 && y <= 20) { onClose(); return true; }
        if (y >= 247 && y <= 267) {
            if (x < 70) page = Math.max(0, page - 1);
            else if (x >= 130 && x <= 180) page = 27;
            else if (x >= 70 && x <= 160) page = 17;
            else if (x >= 320 && x <= 370) page = 25;
            else if (x >= 270 && x <= 375) page = 13;
            else if (x > 375) page = Math.min(29, page + 1);
            else page = 0;
            return true;
        }
        if (page == 0 && x >= 26 && x <= 197 && y >= 46 && y < 238) { page = 1 + (int)(y-46)/16; return true; }
        if (page == 0 && x >= 234 && x < 394 && y >= 158 && y < 238) {
            page = 1 + (int)(x-234)/40 + 4*(int)((y-158)/40); return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    @Override public boolean keyPressed(int key, int scan, int modifiers) {
        if (key == GLFW.GLFW_KEY_RIGHT) { page = Math.min(16, page + 1); return true; }
        if (key == GLFW.GLFW_KEY_LEFT) { page = Math.max(0, page - 1); return true; }
        return super.keyPressed(key, scan, modifiers);
    }
}


