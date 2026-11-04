package dev.xiuxian;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class TalismanScreen extends AbstractContainerScreen<TalismanMenu> {
    private boolean stone;
    public TalismanScreen(TalismanMenu menu,Inventory inv,Component title) { super(menu,inv,title);imageWidth=186;imageHeight=246;inventoryLabelY=153; }
    @Override protected void init() {
        super.init();
        addRenderableWidget(Button.builder(Component.translatable("talisman.xiuxian.mode."+(stone?"stone":"mana")),b->{stone=!stone;b.setMessage(Component.translatable("talisman.xiuxian.mode."+(stone?"stone":"mana")));minecraft.gameMode.handleInventoryButtonClick(menu.containerId,stone?2:1);}).bounds(leftPos+42,topPos+77,126,20).build());
        addRenderableWidget(Button.builder(Component.translatable("talisman.xiuxian.draw"),b->minecraft.gameMode.handleInventoryButtonClick(menu.containerId,0)).bounds(leftPos+42,topPos+128,126,20).build());
    }
    @Override protected void renderBg(GuiGraphics g,float partial,int mx,int my) {
        g.fill(leftPos-2,topPos-2,leftPos+188,topPos+248,0xff493526);g.fill(leftPos,topPos,leftPos+186,topPos+246,0xffd5c29a);
        for(var slot:menu.slots) { int x=leftPos+slot.x,y=topPos+slot.y;g.fill(x-1,y-1,x+17,y+17,0xff4b4032);g.fill(x,y,x+16,y+16,0xff9d927c); }
        var recipe=TalismanKind.recipe(menu.container.getItem(3));
        if(recipe!=null && menu.container.getItem(5).isEmpty()) { var preview=recipe.output(menu.container.getItem(2).is(Talismans.SPIRIT_CINNABAR.get()));g.renderFakeItem(preview,leftPos+150,topPos+60);g.fill(leftPos+150,topPos+60,leftPos+166,topPos+76,0x50d5c29a); }
    }
    @Override protected void renderLabels(GuiGraphics g,int x,int y) {
        g.drawString(font,title,8,8,0xff493526,false);
        for(int i=0;i<5;i++)g.drawString(font,Component.translatable("talisman.xiuxian.slot."+i),i==4?9:9+i*32,i==4?67:29,0xff493526,false);
        var recipe=TalismanKind.recipe(menu.container.getItem(3));
        if(recipe!=null) { g.drawString(font,recipe.output(menu.container.getItem(2).is(Talismans.SPIRIT_CINNABAR.get())).getHoverName(),8,19,0xff493526,false);g.drawString(font,Component.translatable("talisman.xiuxian.cost",recipe.energy,menu.data.get(2)==0?0:recipe.chance(menu.data.get(2))),8,101,0xff493526,false); }
        g.drawString(font,Component.translatable("talisman.xiuxian.energy",menu.data.get(1),menu.data.get(0)),8,115,0xff493526,false);
    }
    @Override public void render(GuiGraphics g,int x,int y,float partial) { renderBackground(g);super.render(g,x,y,partial);renderTooltip(g,x,y); }
}
