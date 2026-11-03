package dev.xiuxian;

import net.minecraft.world.item.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;
import net.minecraftforge.registries.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import java.util.*;

public final class SwordItems {
    public static final Map<String,RegistryObject<Item>> BOOKS=new LinkedHashMap<>();
    public static final DeferredRegister<EntityType<?>> ENTITIES=DeferredRegister.create(ForgeRegistries.ENTITY_TYPES,Xiuxian.ID);
    public static final RegistryObject<EntityType<FlyingSword>> FLYING=ENTITIES.register("flying_sword",()->EntityType.Builder.<FlyingSword>of(FlyingSword::new,MobCategory.MISC).sized(.4f,.4f).clientTrackingRange(8).updateInterval(1).noSave().build("xiuxian:flying_sword"));
    static { for(var art:SwordArt.ALL) BOOKS.put(art.id(),Xiuxian.ITEMS.register("sword_art_"+art.id(),()->new ArtBook(art))); }
    public static void register(net.minecraftforge.eventbus.api.IEventBus bus){ENTITIES.register(bus);}
    static class ArtBook extends Item {
        final SwordArt art;
        ArtBook(SwordArt art){super(new Properties());this.art=art;}
        @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand){
            var stack=player.getItemInHand(hand);
            if(player instanceof ServerPlayer server){
                SwordBookNetwork.open(server,art.id(),hand);
            }
            return InteractionResultHolder.sidedSuccess(stack,level.isClientSide);
        }
    }
}
