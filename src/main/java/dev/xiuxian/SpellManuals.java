package dev.xiuxian;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.*;
import net.minecraft.world.level.ClipContext;
import net.minecraftforge.network.*;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.api.distmarker.Dist;

public final class SpellManuals {
    public record State(int rank,double points,int required,int learned,int selected){}
    public record Open(String id,InteractionHand hand,boolean active,boolean compatible,int layer,int learned,int rank){}
    public record BookAction(String id,InteractionHand hand,int action){}
    public record Cast(boolean cycle){}
    public static State client;
    public static int learned(net.minecraft.world.entity.player.Player p,String id){return ManualDefinition.find(id)==null?0:CultivationFoundation.data(p).getInt("SpellPages_"+ManualDefinition.find(id).family())&3;}
    public static String element(ManualDefinition manual,int page){return manual.elements().get(page==1&&manual.elements().size()>1?1:0);}
    public static String name(ManualDefinition m,int page){
        if(m.sword())return SwordArt.find(SwordPages.art(m,page)).name();
        return switch(element(m,page)){
            case "金"->page==0?"金刃术":"金针雨";case "木"->page==0?"木刺术":"荆棘术";
            case "水"->page==0?"水箭术":"涌浪术";case "火"->page==0?"火弹术":"炎环术";
            case "土"->page==0?"碎岩术":"岩甲术";case "冰"->page==0?"冰锥术":"霜环术";
            case "雷"->page==0?"雷矢术":"连雷术";default->page==0?"风刃术":"风障术";
        };
    }
    public static void network(){
        Cultivation.CHANNEL.messageBuilder(State.class,7,NetworkDirection.PLAY_TO_CLIENT)
            .encoder((m,b)->{b.writeVarInt(m.rank);b.writeDouble(m.points);b.writeVarInt(m.required);b.writeVarInt(m.learned);b.writeVarInt(m.selected);})
            .decoder(b->new State(b.readVarInt(),b.readDouble(),b.readVarInt(),b.readVarInt(),b.readVarInt()))
            .consumerMainThread((m,c)->{client=m;c.get().setPacketHandled(true);}).add();
        Cultivation.CHANNEL.messageBuilder(Open.class,8,NetworkDirection.PLAY_TO_CLIENT)
            .encoder((m,b)->{b.writeUtf(m.id,64);b.writeEnum(m.hand);b.writeBoolean(m.active);b.writeBoolean(m.compatible);b.writeVarInt(m.layer);b.writeVarInt(m.learned);b.writeVarInt(m.rank);})
            .decoder(b->new Open(b.readUtf(64),b.readEnum(InteractionHand.class),b.readBoolean(),b.readBoolean(),b.readVarInt(),b.readVarInt(),b.readVarInt()))
            .consumerMainThread((m,c)->{DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->SpellManualScreen.open(m));c.get().setPacketHandled(true);}).add();
        Cultivation.CHANNEL.messageBuilder(BookAction.class,9,NetworkDirection.PLAY_TO_SERVER)
            .encoder((m,b)->{b.writeUtf(m.id,64);b.writeEnum(m.hand);b.writeVarInt(m.action);})
            .decoder(b->new BookAction(b.readUtf(64),b.readEnum(InteractionHand.class),b.readVarInt()))
            .consumerMainThread((m,c)->{var p=c.get().getSender();if(p!=null)bookAction(p,m);c.get().setPacketHandled(true);}).add();
        Cultivation.CHANNEL.messageBuilder(Cast.class,10,NetworkDirection.PLAY_TO_SERVER)
            .encoder((m,b)->b.writeBoolean(m.cycle)).decoder(b->new Cast(b.readBoolean()))
            .consumerMainThread((m,c)->{var p=c.get().getSender();if(p!=null){if(m.cycle)cycle(p);else cast(p);}c.get().setPacketHandled(true);}).add();
    }
    public static void sync(ServerPlayer p){var m=CultivationFoundation.active(p);var tag=CultivationFoundation.data(p);Cultivation.CHANNEL.send(PacketDistributor.PLAYER.with(()->p),new State(ManualPractice.rank(p),ManualPractice.points(p),ManualPractice.required(p),m==null?0:learned(p,m.id()),tag.getInt("SelectedSpell")&1));}
    public static void open(ServerPlayer p,String id,InteractionHand hand){var m=ManualDefinition.find(id);if(m==null)return;var active=CultivationFoundation.active(p);Cultivation.CHANNEL.send(PacketDistributor.PLAYER.with(()->p),new Open(id,hand,active!=null&&active.id().equals(id),Cultivation.initiated(p)&&(m.sword()||CultivationFoundation.roots(p).containsAll(m.elements())),Cultivation.progress(p).level(),learned(p,id),ManualPractice.rank(p)));}
    public static boolean bookAction(ServerPlayer p,BookAction action){
        var m=ManualDefinition.find(action.id);if(m==null||!p.isAlive()||p.isSpectator()||!p.getItemInHand(action.hand).is(FoundationItems.MANUALS.get(action.id).get()))return false;
        boolean accepted=false;
        if(action.action==0)accepted=CultivationFoundation.learn(p,action.id);
        else if(action.action==1||action.action==2){
            var active=CultivationFoundation.active(p);int page=action.action-1;
            if(active!=null&&active.id().equals(action.id)&&Cultivation.progress(p).level()>=(page==0?1:6)&&m.coverage()>=(page==0?1:6)){
                var tag=CultivationFoundation.data(p);tag.putInt("SpellPages_"+m.family(),learned(p,action.id)|(1<<page));tag.putInt("SelectedSpell",page);if(m.sword())tag.putString("SwordArt",SwordPages.art(m,page));CultivationFoundation.save(p,tag);accepted=true;
            }
        }
        Cultivation.sync(p);open(p,action.id,action.hand);return accepted;
    }
    private static void cycle(ServerPlayer p){var m=CultivationFoundation.active(p);if(m==null||m.sword())return;var tag=CultivationFoundation.data(p);int next=1-(tag.getInt("SelectedSpell")&1);if((learned(p,m.id())&(1<<next))==0)next=1-next;tag.putInt("SelectedSpell",next);CultivationFoundation.save(p,tag);p.displayClientMessage(Component.translatable("spell.xiuxian.selected",name(m,next)),true);sync(p);}
    public static boolean cast(ServerPlayer p){
        var m=CultivationFoundation.active(p);if(m==null||m.sword()||!p.isAlive()||p.isSpectator())return false;
        var tag=CultivationFoundation.data(p);int page=tag.getInt("SelectedSpell")&1;int required=page==0?1:6;
        if(m.coverage()<required||Cultivation.progress(p).level()<required||(learned(p,m.id())&(1<<page))==0){p.displayClientMessage(Component.translatable("spell.xiuxian.need_page"),true);return false;}
        long now=p.level().getGameTime();String element=element(m,page);String cooldown="SpellUntil_"+element+"_"+page;
        if(now<tag.getLong(cooldown)||now<tag.getLong("SpellGlobalUntil"))return false;
        LivingEntity target=SwordCombat.target(p);
        boolean defense=page==1&&(element.equals("土")||element.equals("风"));
        if(!defense&&target==null){p.displayClientMessage(Component.translatable("message.xiuxian.no_sword_target"),true);return false;}
        if(!CultivationFoundation.spendMana(p,page==0?8:22)){p.displayClientMessage(Component.translatable("message.xiuxian.not_enough_mana"),true);return false;}
        tag=CultivationFoundation.data(p);tag.putLong(cooldown,now+(page==0?25:100));tag.putLong("SpellGlobalUntil",now+10);CultivationFoundation.save(p,tag);
        double power=ManualPractice.power(p);
        if(defense){p.addEffect(new MobEffectInstance(element.equals("土")?MobEffects.DAMAGE_RESISTANCE:MobEffects.ABSORPTION,(int)(100*power),element.equals("土")?0:1));}
        else if(page==0)hit(p,target,element,damage(p,0));
        else{int count=0;for(var e:p.level().getEntitiesOfClass(LivingEntity.class,target.getBoundingBox().inflate(4),e->SwordCombat.validTarget(p,e)&&p.hasLineOfSight(e)&&p.distanceToSqr(e)<=900)){if(count++==5)break;hit(p,e,element,damage(p,1));}}
        p.displayClientMessage(Component.translatable("spell.xiuxian.cast",name(m,page)),true);sync(p);return true;
    }
    static float damage(ServerPlayer player,int page){
        var manual=CultivationFoundation.active(player);
        double quality=manual==null?1:1+manual.qualityTier()*.1;
        double base=(page==0?Cultivation.SPELL_PRIMARY_DAMAGE.get():Cultivation.SPELL_SECONDARY_DAMAGE.get())+Cultivation.progress(player).level()*(page==0?.6:.9);
        return (float)(base*ManualPractice.power(player)*quality);
    }
    static void hit(ServerPlayer p,LivingEntity target,String element,float damage){
        if(!SwordCombat.validTarget(p,target)||!p.hasLineOfSight(target))return;
        if(!target.hurt(p.damageSources().indirectMagic(p,p),damage))return;
        p.setLastHurtMob(target);
        if(element.equals("火"))target.setSecondsOnFire(2);
        if(element.equals("木")||element.equals("冰")||element.equals("水"))target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,element.equals("冰")?60:40,element.equals("木")?2:1));
        if(element.equals("风"))target.knockback(.5,p.getX()-target.getX(),p.getZ()-target.getZ());
        var particle=switch(element){case "火"->ParticleTypes.FLAME;case "水","冰"->ParticleTypes.SPLASH;case "木"->ParticleTypes.HAPPY_VILLAGER;case "雷"->ParticleTypes.ELECTRIC_SPARK;default->ParticleTypes.CRIT;};
        Vec3 start=p.getEyePosition(),end=target.getBoundingBox().getCenter();for(int i=0;i<16;i++){var point=start.lerp(end,i/15.0);p.serverLevel().sendParticles(particle,point.x,point.y,point.z,1,.04,.04,.04,0);}
    }
}
