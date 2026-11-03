package dev.xiuxian;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
public class AlchemyScreen extends AbstractContainerScreen<AlchemyMenu>{
    public AlchemyScreen(AlchemyMenu m,Inventory i,Component t){super(m,i,t);imageHeight=206;inventoryLabelY=112;}
    @Override protected void init(){super.init();addRenderableWidget(Button.builder(Component.translatable("screen.xiuxian.start_alchemy"),b->minecraft.gameMode.handleInventoryButtonClick(menu.containerId,0)).bounds(leftPos+45,topPos+94,90,20).build());}
    @Override protected void renderBg(GuiGraphics g,float partial,int x,int y){g.fill(leftPos-1,topPos-1,leftPos+177,topPos+207,0xffb97c49);g.fill(leftPos,topPos,leftPos+176,topPos+206,0xffe1ccb0);for(var s:menu.slots){g.fill(leftPos+s.x-1,topPos+s.y-1,leftPos+s.x+17,topPos+s.y+17,0xff795941);g.fill(leftPos+s.x,topPos+s.y,leftPos+s.x+16,topPos+s.y+16,0xffb5a38a);}g.fill(leftPos+40,topPos+82,leftPos+40+100*menu.data.get(0)/600,topPos+87,0xffbd713d);}
    @Override protected void renderLabels(GuiGraphics g,int x,int y){g.drawString(font,title,8,6,0xff543b29,false);var f=Alchemy.formulas().stream().filter(v->menu.container.getItem(0).is(Alchemy.item(v.id()+"_formula"))).findFirst().orElse(null);if(f!=null){boolean main=f.id().equals("qi_gathering")?menu.container.getItem(1).getItem() instanceof net.minecraft.world.item.BlockItem && menu.container.getItem(1).getItem() instanceof QiHerbItem:menu.container.getItem(1).is(f.main());boolean ready=main&&menu.container.getItem(1).getCount()>=f.mainCount()&&menu.container.getItem(2).is(f.auxiliary())&&menu.container.getItem(2).getCount()>=f.auxiliaryCount()&&menu.container.getItem(3).getCount()>=f.stones();g.drawString(font,Component.translatable("screen.xiuxian.alchemy_needs",f.mainCount(),f.auxiliaryCount(),f.stones()),8,113,ready?0xff32613b:0xffa63535,false);}}
    @Override public void render(GuiGraphics g,int x,int y,float p){renderBackground(g);super.render(g,x,y,p);renderTooltip(g,x,y);}
}
