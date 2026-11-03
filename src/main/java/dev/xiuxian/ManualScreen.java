package dev.xiuxian;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.List;

public class ManualScreen extends Screen {
    private int page;
    public ManualScreen() { super(Component.translatable("screen.xiuxian.manuals")); }
    @Override public boolean isPauseScreen() { return false; }
    private List<String> learned() {
        var state=ClientState.foundation;
        return state == null || state.known().isEmpty() ? List.of() : List.of(state.known().split(","));
    }
    @Override protected void init() {
        clearWidgets();
        var ids=learned();
        page=Math.max(0,Math.min(page,Math.max(0,(ids.size()-1)/5)));
        for(int i=page*5;i<Math.min(ids.size(),page*5+5);i++) {
            String id=ids.get(i); var manual=ManualDefinition.find(id); if(manual==null)continue;
            addRenderableWidget(Button.builder(Component.literal(manual.name()+" · "+(manual.sword()?"剑修":"术修")+" · 1—"+manual.coverage()), b->Cultivation.CHANNEL.sendToServer(new CultivationFoundation.Select(id))).bounds(width/2-145,96+(i%5)*25,290,21).build());
        }
        addRenderableWidget(Button.builder(Component.literal("‹"),b->{page--;init();}).bounds(width/2-145,230,35,20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.back"),b->onClose()).bounds(width/2-95,230,190,20).build());
        addRenderableWidget(Button.builder(Component.literal("›"),b->{page++;init();}).bounds(width/2+110,230,35,20).build());
    }
    @Override public void onClose() { minecraft.setScreen(new ClientSetup.StatusScreen()); }
    @Override public void render(GuiGraphics g,int x,int y,float partial) {
        renderBackground(g);
        g.drawCenteredString(font,title,width/2,15,0xffd9e8c3);
        var state=ClientState.foundation;
        if(state!=null) {
            g.drawCenteredString(font,Component.translatable("message.xiuxian.roots",state.roots(),Component.translatable("roots.xiuxian.grade_"+state.grade())),width/2,37,0xffc1d9ce);
            var manual=ManualDefinition.find(state.active());
            g.drawCenteredString(font,Component.translatable("message.xiuxian.manual_active",manual==null?Component.translatable("manual.xiuxian.tier_"+ClientState.technique):Component.literal(manual.name())),width/2,56,0xffe1d1af);
        }
        if(learned().isEmpty())g.drawCenteredString(font,Component.translatable("screen.xiuxian.no_manuals"),width/2,125,0xffbecbbf);
        super.render(g,x,y,partial);
    }
}
