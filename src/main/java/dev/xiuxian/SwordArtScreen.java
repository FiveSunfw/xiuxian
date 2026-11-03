package dev.xiuxian;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class SwordArtScreen extends Screen {
    private final SwordBookNetwork.Open state;
    private final SwordArt art;
    private int left,top;
    public SwordArtScreen(SwordBookNetwork.Open state){super(Component.literal(SwordArt.find(state.id()).name()));this.state=state;this.art=SwordArt.find(state.id());}
    public static void open(SwordBookNetwork.Open state){if(SwordArt.find(state.id())!=null)Minecraft.getInstance().setScreen(new SwordArtScreen(state));}
    @Override public boolean isPauseScreen(){return false;}
    @Override protected void init(){
        left=width/2-150;top=height/2-120;
        var button=Button.builder(Component.translatable(state.known()?"screen.xiuxian.art_known":"screen.xiuxian.learn_art"),b->{b.active=false;Cultivation.CHANNEL.sendToServer(new SwordBookNetwork.Learn(state.id(),state.hand()));}).bounds(left+78,top+208,144,20).build();
        button.active=!state.known()&&state.initiated();addRenderableWidget(button);
    }
    private void text(GuiGraphics g,Component value,int x,int y,int color){for(var line:font.split(value,260)){g.drawString(font,line,x,y,color,false);y+=12;}}
    @Override public void render(GuiGraphics g,int x,int y,float partial){
        renderBackground(g);g.fill(left-2,top-2,left+302,top+242,0xff71634c);g.fill(left,top,left+300,top+240,0xffe6d7b5);
        g.renderItem(new ItemStack(SwordItems.BOOKS.get(art.id()).get()),left+20,top+18);
        g.drawCenteredString(font,title,width/2,top+22,0xff493d2c);
        text(g,Component.translatable("sword_art.xiuxian."+art.id()),left+20,top+51,0xff493d2c);
        text(g,Component.translatable("screen.xiuxian.art_requirements"),left+20,top+98,0xff69573b);
        text(g,Component.translatable("screen.xiuxian.art_controls",ClientSetup.CYCLE.getTranslatedKeyMessage(),ClientSetup.CAST.getTranslatedKeyMessage(),ClientSetup.RECALL.getTranslatedKeyMessage()),left+20,top+132,0xff435b58);
        g.drawString(font,Component.translatable("screen.xiuxian.art_cost",art.cost(),art.cooldown()/20.0),left+20,top+169,0xff69573b,false);
        g.drawCenteredString(font,Component.translatable(state.initiated()?"screen.xiuxian.book_retained":"message.xiuxian.need_initiation"),width/2,top+190,0xff69573b);
        super.render(g,x,y,partial);
    }
}
