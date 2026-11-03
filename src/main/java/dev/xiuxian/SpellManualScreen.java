package dev.xiuxian;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class SpellManualScreen extends Screen {
    private final SpellManuals.Open state;
    private int page,left,top;
    public SpellManualScreen(SpellManuals.Open state,int page){super(Component.literal(ManualDefinition.find(state.id()).name()));this.state=state;this.page=page;}
    public static void open(SpellManuals.Open state){if(ManualDefinition.find(state.id())==null)return;int page=Minecraft.getInstance().screen instanceof SpellManualScreen old&&old.state.id().equals(state.id())?old.page:0;Minecraft.getInstance().setScreen(new SpellManualScreen(state,page));}

    @Override protected void init(){
        clearWidgets();left=width/2-150;top=height/2-120;
        boolean learned=page==0?state.active():(state.learned()&(1<<(page-1)))!=0;
        var button=Button.builder(Component.translatable(learned?(page==0?"spell.xiuxian.active":"spell.xiuxian.learned"):page==0?"spell.xiuxian.activate":"spell.xiuxian.study"),b->{b.active=false;Cultivation.CHANNEL.sendToServer(new SpellManuals.BookAction(state.id(),state.hand(),page));}).bounds(left+80,top+205,140,20).build();
        button.active=!learned&&state.compatible()&&(page==0||state.active()&&state.layer()>=(page==1?1:6)&&ManualDefinition.find(state.id()).coverage()>=(page==1?1:6));addRenderableWidget(button);
        addRenderableWidget(Button.builder(Component.literal("‹"),b->{page=Math.max(0,page-1);init();}).bounds(left+20,top+205,35,20).build());
        addRenderableWidget(Button.builder(Component.literal("›"),b->{page=Math.min(2,page+1);init();}).bounds(left+245,top+205,35,20).build());
    }
    @Override public boolean isPauseScreen(){return false;}
    private void text(GuiGraphics g,Component text,int y){for(var line:font.split(text,260)){g.drawString(font,line,left+20,top+y,0xff51442e,false);y+=12;}}
    @Override public void render(GuiGraphics g,int x,int y,float partial){
        renderBackground(g);g.fill(left-2,top-2,left+302,top+242,0xff7c8061);g.fill(left,top,left+300,top+240,0xffe2d9b6);
        g.renderItem(new ItemStack(FoundationItems.MANUALS.get(state.id()).get()),left+20,top+17);g.drawCenteredString(font,title,width/2,top+21,0xff51442e);
        var manual=ManualDefinition.find(state.id());
        if(page==0){text(g,Component.translatable("spell.xiuxian.practice_page",manual.coverage()),52);text(g,Component.translatable("spell.xiuxian.reset_notice"),105);text(g,Component.translatable("spell.xiuxian.keep_book"),157);}
        else{
            int required=page==1?1:6;g.drawCenteredString(font,Component.literal(SpellManuals.name(manual,page-1)),width/2,top+53,0xff51442e);
            text(g,Component.translatable(manual.coverage()<required?"manual.xiuxian.missing_page":"spell.xiuxian.page_requirement",required),78);
            text(g,Component.translatable(manual.sword()?"sword_art.xiuxian."+SwordPages.art(manual,page-1):"spell.xiuxian.effect_"+SpellManuals.element(manual,page-1)+"_"+(page-1)),104);
            text(g,Component.translatable(manual.sword()?"manual.xiuxian.sword_controls":"spell.xiuxian.controls",ClientSetup.CYCLE.getTranslatedKeyMessage(),ClientSetup.CAST.getTranslatedKeyMessage(),ClientSetup.RECALL.getTranslatedKeyMessage()),145);
        }
        g.drawCenteredString(font,Component.translatable(state.compatible()?"spell.xiuxian.book_rank":state.layer()==0?"message.xiuxian.need_initiation":"message.xiuxian.manual_root_mismatch",state.rank()),width/2,top+184,0xff685734);
        super.render(g,x,y,partial);
    }
}
