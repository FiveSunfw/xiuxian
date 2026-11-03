package dev.xiuxian;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkDirection;
import java.util.*;

@Mod.EventBusSubscriber(modid=Xiuxian.ID)
public final class SwordCombat {
    private static final UUID ARMOR_PENALTY=UUID.fromString("a717b960-428d-4ea8-9271-d93c8d9c00f4");
    public record Action(String action) {}
    public static void network(){
        Cultivation.CHANNEL.messageBuilder(Action.class,3,NetworkDirection.PLAY_TO_SERVER)
            .encoder((m,b)->b.writeUtf(m.action,32)).decoder(b->new Action(b.readUtf(32)))
            .consumerMainThread((m,c)->{var p=c.get().getSender();if(p!=null){if(m.action.equals("cycle"))cycle(p);else if(m.action.equals("cast"))cast(p);else if(m.action.equals("recall"))recall(p,false);}c.get().setPacketHandled(true);}).add();
    }
    static List<String> known(ServerPlayer p){return CultivationFoundation.data(p).getList("SwordArts",8).stream().map(Tag::getAsString).filter(id->SwordArt.find(id)!=null).toList();}
    static List<String> available(ServerPlayer p){
        var result=new java.util.ArrayList<>(known(p));
        var manual=CultivationFoundation.active(p);
        if(manual!=null&&manual.sword()){
            if(!result.contains("recall"))result.add("recall");
            for(int page=0;page<2;page++)if((SpellManuals.learned(p,manual.id())&(1<<page))!=0&&Cultivation.progress(p).level()>=(page==0?1:6)&&manual.coverage()>=(page==0?1:6)){
                String art=SwordPages.art(manual,page);if(!result.contains(art))result.add(art);
            }
        }
        return result;
    }
    static boolean learn(ServerPlayer p,String id){
        var tag=CultivationFoundation.data(p);var list=tag.getList("SwordArts",8);
        if(known(p).contains(id))return false;
        list.add(StringTag.valueOf(id));tag.put("SwordArts",list);tag.putString("SwordArt",id);CultivationFoundation.save(p,tag);return true;
    }
    public static boolean swordReady(ServerPlayer p){var m=CultivationFoundation.active(p);return p.isAlive()&&!p.isSpectator()&&m!=null&&m.sword()&&p.getMainHandItem().getItem() instanceof SwordItem;}
    static boolean isCurrent(ServerPlayer p,FlyingSword sword){var tag=CultivationFoundation.data(p);return tag.hasUUID("SwordProjection")&&tag.getUUID("SwordProjection").equals(sword.getUUID());}
    static FlyingSword current(ServerPlayer p){var tag=CultivationFoundation.data(p);if(!tag.hasUUID("SwordProjection"))return null;var e=p.serverLevel().getEntity(tag.getUUID("SwordProjection"));return e instanceof FlyingSword sword?sword:null;}
    static String manual(ServerPlayer p){var m=CultivationFoundation.active(p);return m==null?"":m.family();}
    static double flightSpeed(ServerPlayer p,String art){double speed=manual(p).equals("breeze_sword")?1.35:manual(p).equals("radiant_sword_fragment")?1.6:manual(p).equals("stone_sword")?.85:1.1;return art.equals("pierce")?speed*1.5:speed;}
    static boolean validTarget(ServerPlayer p,LivingEntity target){return target!=p&&target.isAlive()&&!target.isAlliedTo(p)&&!(target instanceof net.minecraft.world.entity.TamableAnimal pet&&pet.isOwnedBy(p))&&(!(target instanceof ServerPlayer other)||p.canHarmPlayer(other));}
    static LivingEntity target(ServerPlayer p){
        Vec3 start=p.getEyePosition(),end=start.add(p.getLookAngle().scale(24));
        var block=p.level().clip(new ClipContext(start,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p));
        double nearest=block.getLocation().distanceToSqr(start);LivingEntity selected=null;
        for(var entity:p.level().getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().expandTowards(p.getLookAngle().scale(24)).inflate(1),e->validTarget(p,e))){
            var hit=entity.getBoundingBox().inflate(.25).clip(start,end);
            if(hit.isPresent()&&hit.get().distanceToSqr(start)<nearest){nearest=hit.get().distanceToSqr(start);selected=entity;}
        }return selected;
    }
    static void message(ServerPlayer p,String key){p.displayClientMessage(Component.translatable("message.xiuxian."+key),true);}
    static void cycle(ServerPlayer p){
        var ids=available(p);if(ids.isEmpty()){message(p,"no_sword_art");return;}
        var tag=CultivationFoundation.data(p);int next=(ids.indexOf(tag.getString("SwordArt"))+1)%ids.size();tag.putString("SwordArt",ids.get(next));CultivationFoundation.save(p,tag);
        p.displayClientMessage(Component.translatable("message.xiuxian.art_selected",SwordArt.find(ids.get(next)).name()),true);
    }
    public static boolean cast(ServerPlayer p){
        if(!swordReady(p)){message(p,"need_sword_manual");return false;}
        var tag=CultivationFoundation.data(p);var art=SwordArt.find(tag.getString("SwordArt"));
        if(art==null||!available(p).contains(art.id())){message(p,"no_sword_art");return false;}
        if(art.id().equals("recall"))return recall(p,false);
        if(art.id().equals("return_strike"))return recall(p,true);
        long now=p.level().getGameTime();
        if(now<tag.getLong("SwordGlobalUntil")||now<tag.getLong("SwordUntil_"+art.id())){message(p,"art_cooldown");return false;}
        LivingEntity target=target(p);
        boolean projectile=List.of("drive","pierce","combo","armor_break","shadows").contains(art.id());
        if(projectile&&target==null){message(p,"no_sword_target");return false;}
        if(art.id().equals("step")&&!p.onGround()){message(p,"step_ground");return false;}
        Projectile incoming=null;
        if(art.id().equals("intercept")){
            incoming=p.level().getEntitiesOfClass(Projectile.class,p.getBoundingBox().inflate(6),e->!(e instanceof FlyingSword)&&e.getOwner()!=p&&e.getDeltaMovement().dot(p.position().subtract(e.position()))>0&&p.hasLineOfSight(e)).stream().min(Comparator.comparingDouble(p::distanceToSqr)).orElse(null);
            if(incoming==null){message(p,"no_projectile");return false;}
        }
        double cost=art.cost()*(manual(p).equals("river_sword")?.8:manual(p).equals("radiant_sword_fragment")?1.2:1);
        if(!CultivationFoundation.spendMana(p,cost)){message(p,"not_enough_mana");return false;}
        tag=CultivationFoundation.data(p);tag.putLong("SwordGlobalUntil",now+8);tag.putLong("SwordUntil_"+art.id(),now+art.cooldown());CultivationFoundation.save(p,tag);
        if(projectile||art.id().equals("guard")){
            var old=current(p);if(old!=null)old.discard();
            var sword=new FlyingSword(SwordItems.FLYING.get(),p.level());sword.setOwner(p);sword.setItem(p.getMainHandItem().copyWithCount(1));sword.setPos(p.getEyePosition().add(p.getLookAngle().scale(.6)));sword.target=target;sword.art=art.id();
            tag=CultivationFoundation.data(p);
            if(projectile&&tag.getLong("SwordChargedUntil")>now){sword.power=1.5;tag.remove("SwordChargedUntil");}
            tag.putUUID("SwordProjection",sword.getUUID());CultivationFoundation.save(p,tag);p.serverLevel().addFreshEntity(sword);
        }else if(art.id().equals("sweep")){
            int count=0;for(var e:p.level().getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(4),e->validTarget(p,e)&&p.hasLineOfSight(e)))
                if(e.position().subtract(p.position()).normalize().dot(p.getLookAngle())>.25&&count++<5)strike(p,e,"sweep",1);
        }else if(art.id().equals("step")){
            Vec3 horizontal=new Vec3(p.getLookAngle().x,0,p.getLookAngle().z).normalize().scale(1.1);p.setDeltaMovement(horizontal.add(0,.08,0));p.hurtMarked=true;
        }else if(art.id().equals("intercept")){incoming.discard();p.serverLevel().sendParticles(ParticleTypes.CRIT,p.getX(),p.getY()+1,p.getZ(),12,.5,.5,.5,.1);
        }else if(art.id().equals("charge")){
            tag=CultivationFoundation.data(p);tag.putLong("SwordPreparingUntil",now+30);tag.putInt("SwordPrepareHurt",p.getLastHurtByMobTimestamp());CultivationFoundation.save(p,tag);
        }
        p.displayClientMessage(Component.translatable("message.xiuxian.art_cast",art.name()),true);return true;
    }
    static boolean recall(ServerPlayer p,boolean strike){
        var sword=current(p);if(!swordReady(p)||sword==null)return false;
        String id=strike?"return_strike":"recall";if(!available(p).contains(id))return false;
        var tag=CultivationFoundation.data(p);long now=p.level().getGameTime();if(now<tag.getLong("SwordUntil_"+id))return false;
        double cost=(strike?12:1)*(manual(p).equals("returning_sword")?.6:1);
        if(!CultivationFoundation.spendMana(p,cost))return false;
        sword.art=strike?"return_strike":"recall";sword.returning=true;sword.returnDamage=strike;
        tag=CultivationFoundation.data(p);tag.putLong("SwordUntil_"+id,now+(strike?60:10));CultivationFoundation.save(p,tag);return true;
    }
    static double attackBase(ServerPlayer player,LivingEntity target){
        var attribute=player.getAttribute(Attributes.ATTACK_DAMAGE);
        double equipped=0;
        for(var modifier:player.getMainHandItem().getAttributeModifiers(EquipmentSlot.MAINHAND).get(Attributes.ATTACK_DAMAGE))
            if(modifier.getOperation()==AttributeModifier.Operation.ADDITION&&attribute.getModifier(modifier.getId())!=null)equipped+=modifier.getAmount();
        double weapon=player.getMainHandItem().getItem() instanceof SwordItem sword?sword.getDamage():0;
        double enchantment=net.minecraft.world.item.enchantment.EnchantmentHelper.getDamageBonus(player.getMainHandItem(),target.getMobType());
        return Math.max(0,attribute.getValue()-equipped)+(weapon+enchantment)*Cultivation.SWORD_WEAPON_SCALE.get();
    }
    static void strike(ServerPlayer p,LivingEntity victim,String art,double power){
        if(!validTarget(p,victim)||!p.hasLineOfSight(victim)||p.distanceToSqr(victim)>900)return;
        double multiplier=switch(art){case "pierce"->1.4;case "combo"->.65;case "sweep"->.8;case "shadows"->.45;case "return_strike"->1.1;default->1;};
        String manual=manual(p);
        multiplier*=manual.equals("breeze_sword")?.85:manual.equals("lone_sword")?(art.equals("sweep")?.8:1.25):manual.equals("river_sword")?.95:1;
        if(art.equals("return_strike")&&manual.equals("returning_sword"))multiplier*=1.3;
        if(art.equals("armor_break")||manual.equals("piercing_sword")){
            var armor=victim.getAttribute(Attributes.ARMOR);
            if(armor!=null){armor.removeModifier(ARMOR_PENALTY);armor.addTransientModifier(new AttributeModifier(ARMOR_PENALTY,"xiuxian.sword_armor_break",art.equals("armor_break")?-4:-2,AttributeModifier.Operation.ADDITION));victim.getPersistentData().putLong("xiuxian_armor_until",p.level().getGameTime()+60);}
        }
        float damage=(float)(attackBase(p,victim)*multiplier*power*ManualPractice.power(p));
        if(victim.hurt(p.damageSources().playerAttack(p),damage)) p.setLastHurtMob(victim);
        p.serverLevel().sendParticles(ParticleTypes.CRIT,victim.getX(),victim.getY()+1,victim.getZ(),art.equals("shadows")?18:5,.3,.4,.3,.1);
    }
    @SubscribeEvent public static void guard(LivingHurtEvent event){
        if(event.getEntity() instanceof ServerPlayer p){
            var tag=CultivationFoundation.data(p);
            if(tag.contains("SwordPreparingUntil")){tag.remove("SwordPreparingUntil");CultivationFoundation.save(p,tag);}
            var sword=current(p);
            if(event.getSource().getDirectEntity()!=null&&sword!=null&&sword.art.equals("guard")&&swordReady(p))event.setAmount(event.getAmount()*(manual(p).equals("stone_sword")?.55f:.7f));
        }
    }
    @SubscribeEvent public static void armorExpiry(LivingEvent.LivingTickEvent event){
        var e=event.getEntity();long until=e.getPersistentData().getLong("xiuxian_armor_until");if(!e.level().isClientSide&&until>0&&e.level().getGameTime()>=until){var a=e.getAttribute(Attributes.ARMOR);if(a!=null)a.removeModifier(ARMOR_PENALTY);e.getPersistentData().remove("xiuxian_armor_until");}
    }
    @SubscribeEvent public static void prepare(TickEvent.PlayerTickEvent event){
        if(event.phase!=TickEvent.Phase.END||!(event.player instanceof ServerPlayer p))return;
        var tag=CultivationFoundation.data(p);long until=tag.getLong("SwordPreparingUntil");if(until==0)return;
        if(!swordReady(p)||p.getLastHurtByMobTimestamp()!=tag.getInt("SwordPrepareHurt")){tag.remove("SwordPreparingUntil");CultivationFoundation.save(p,tag);return;}
        if(p.level().getGameTime()>=until){tag.remove("SwordPreparingUntil");tag.putLong("SwordChargedUntil",p.level().getGameTime()+100);CultivationFoundation.save(p,tag);message(p,"sword_charged");}
    }
}
