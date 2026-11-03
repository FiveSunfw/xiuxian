package dev.xiuxian;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;

/** Opening a book is read-only; learning is validated against the actual held book. */
public final class SwordBookNetwork {
    public record Open(String id, InteractionHand hand, boolean known, boolean initiated) {}
    public record Learn(String id, InteractionHand hand) {}
    public static void register() {
        Cultivation.CHANNEL.messageBuilder(Open.class,4,NetworkDirection.PLAY_TO_CLIENT)
            .encoder((m,b)->{b.writeUtf(m.id,32);b.writeEnum(m.hand);b.writeBoolean(m.known);b.writeBoolean(m.initiated);})
            .decoder(b->new Open(b.readUtf(32),b.readEnum(InteractionHand.class),b.readBoolean(),b.readBoolean()))
            .consumerMainThread((m,c)->{DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->SwordArtScreen.open(m));c.get().setPacketHandled(true);}).add();
        Cultivation.CHANNEL.messageBuilder(Learn.class,5,NetworkDirection.PLAY_TO_SERVER)
            .encoder((m,b)->{b.writeUtf(m.id,32);b.writeEnum(m.hand);})
            .decoder(b->new Learn(b.readUtf(32),b.readEnum(InteractionHand.class)))
            .consumerMainThread((m,c)->{var p=c.get().getSender();if(p!=null)learn(p,m.id,m.hand);c.get().setPacketHandled(true);}).add();
    }
    public static void open(ServerPlayer player,String id,InteractionHand hand) {
        if(SwordArt.find(id)==null)return;
        Cultivation.CHANNEL.send(PacketDistributor.PLAYER.with(()->player),new Open(id,hand,SwordCombat.known(player).contains(id),Cultivation.initiated(player)));
    }
    public static boolean learn(ServerPlayer player,String id,InteractionHand hand) {
        if(!player.isAlive()||player.isSpectator()||SwordArt.find(id)==null||!player.getItemInHand(hand).is(SwordItems.BOOKS.get(id).get()))return false;
        if(!Cultivation.initiated(player)){player.displayClientMessage(Component.translatable("message.xiuxian.need_initiation"),true);return false;}
        boolean added=SwordCombat.learn(player,id);
        if(added)player.displayClientMessage(Component.translatable("message.xiuxian.art_learned",SwordArt.find(id).name()),false);
        open(player,id,hand);
        return added;
    }
}
