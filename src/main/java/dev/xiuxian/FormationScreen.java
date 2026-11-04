package dev.xiuxian;

import java.util.Locale;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class FormationScreen extends AbstractContainerScreen<FormationMenu> {
    private Button toggle;
    public FormationScreen(FormationMenu menu,Inventory inventory,Component title){super(menu,inventory,title);imageWidth=186;imageHeight=248;inventoryLabelY=155;}
    @Override protected void init(){super.init();toggle=addRenderableWidget(Button.builder(Component.translatable("formation.xiuxian.enable"),b->minecraft.gameMode.handleInventoryButtonClick(menu.containerId,0)).bounds(leftPos+43,topPos+140,100,20).build());}
    @Override protected void containerTick(){super.containerTick();toggle.setMessage(Component.translatable((menu.data.get(0)&256)!=0?"formation.xiuxian.disable":"formation.xiuxian.enable"));}
    @Override protected void renderBg(GuiGraphics g,float partial,int mouseX,int mouseY){g.fill(leftPos-2,topPos-2,leftPos+188,topPos+250,0xff435e61);g.fill(leftPos,topPos,leftPos+186,topPos+248,0xffc5d1c3);for(var slot:menu.slots){int x=leftPos+slot.x,y=topPos+slot.y;g.fill(x-1,y-1,x+17,y+17,0xff3c4f48);g.fill(x,y,x+16,y+16,0xff8e9d91);}}
    private void line(GuiGraphics g,Component text,int y) { float scale=Math.min(1f,170f/Math.max(1,font.width(text)));g.pose().pushPose();g.pose().translate(8,y,0);g.pose().scale(scale,scale,1);g.drawString(font,text,0,0,0xff30453e,false);g.pose().popPose(); }
    @Override protected void renderLabels(GuiGraphics g,int x,int y){
        g.drawString(font,title,8,8,0xff30453e,false);
        g.drawString(font,Component.translatable("formation.xiuxian.token_slot"),19,23,0xff30453e,false);g.drawString(font,Component.translatable("formation.xiuxian.fuel_slot"),124,23,0xff30453e,false);
        var kind=FormationKind.from(menu.container.getItem(0));line(g,kind==null?Component.translatable("formation.xiuxian.no_token"):Formations.TOKENS.get(kind).get().getDescription(),58);
        int status=menu.data.get(0)&255;var values=FormationEntity.Status.values();String key=status<values.length?values[status].name().toLowerCase(Locale.ROOT):"off";
        line(g,Component.translatable("formation.xiuxian.status."+key),73);
        line(g,Component.translatable("formation.xiuxian.scale."+menu.data.get(1)),88);
        g.drawString(font,Component.translatable("formation.xiuxian.energy",String.format(Locale.ROOT,"%.2f",menu.data.get(2)+menu.data.get(3)/100.0)),8,103,0xff30453e,false);
        g.drawString(font,Component.translatable("formation.xiuxian.upkeep",String.format(Locale.ROOT,"%.3f",menu.data.get(4)/1000.0)),8,119,0xff30453e,false);
    }
    @Override public void render(GuiGraphics g,int x,int y,float partial){renderBackground(g);super.render(g,x,y,partial);renderTooltip(g,x,y);}
}
