package dev.xiuxian;

import net.minecraft.world.item.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.RegistryObject;
import java.util.*;

public final class Alchemy {
    public static final RegistryObject<Block> FURNACE=Xiuxian.BLOCKS.register("basic_alchemy_furnace",AlchemyBlock::new);
    public static final RegistryObject<Item> FURNACE_ITEM=Xiuxian.ITEMS.register("basic_alchemy_furnace",()->new BlockItem(FURNACE.get(),new Item.Properties()));
    public static final RegistryObject<BlockEntityType<AlchemyEntity>> ENTITY=Xiuxian.ENTITIES.register("basic_alchemy_furnace",()->BlockEntityType.Builder.of(AlchemyEntity::new,FURNACE.get()).build(null));
    public static final RegistryObject<MenuType<AlchemyMenu>> MENU=Xiuxian.MENUS.register("basic_alchemy_furnace",()->IForgeMenuType.create(AlchemyMenu::new));
    public static final Map<String,RegistryObject<Item>> ITEMS=new LinkedHashMap<>();
    public static final RegistryObject<MobEffect> WARM=CultivationResources.EFFECTS.register("warm_nourishment",()->new Marker(0xd6aa55));
    public static final RegistryObject<MobEffect> QI=CultivationResources.EFFECTS.register("qi_nourishment",()->new Marker(0x65ae85));
    public static final RegistryObject<MobEffect> ANTIDOTE=CultivationResources.EFFECTS.register("poison_immunity",()->new Marker(0xbce5eb));
    public static final RegistryObject<MobEffect> RECOVERY=CultivationResources.EFFECTS.register("herbal_recovery",Recovery::new);
    static {
        for(String id:List.of("healing_liquid","cleansing_liquid","warming_liquid","healing_pill","cleansing_pill","nourishing_pill")) ITEMS.put(id,Xiuxian.ITEMS.register(id,()->new Medicine(id)));
        for(String id:List.of("qi_gathering","healing","cleansing","nourishing")) ITEMS.put(id+"_formula",Xiuxian.ITEMS.register(id+"_formula",()->new Item(new Item.Properties().stacksTo(1))));
        ITEMS.put("pill_residue",Xiuxian.ITEMS.register("pill_residue",()->new Item(new Item.Properties())));
    }
    public static void register() {}
    public static Item item(String id) {return ITEMS.get(id).get();}
    public record Formula(String id,Item main,int mainCount,Item auxiliary,int auxiliaryCount,int stones,Item result) {}
    public static List<Formula> formulas() { return List.of(
        new Formula("qi_gathering",Xiuxian.HERB_ITEM.get(),3,Items.WHEAT,1,1,CultivationResources.PILL.get()),
        new Formula("healing",Xiuxian.HERB_ITEMS.get(HerbKind.WOOD).get(),3,Xiuxian.HERB_ITEMS.get(HerbKind.STONE).get(),1,1,item("healing_pill")),
        new Formula("cleansing",Xiuxian.HERB_ITEMS.get(HerbKind.FROST).get(),2,Xiuxian.HERB_ITEMS.get(HerbKind.MARSH).get(),2,1,item("cleansing_pill")),
        new Formula("nourishing",Xiuxian.HERB_ITEM.get(),3,Xiuxian.HERB_ITEMS.get(HerbKind.WOOD).get(),2,2,item("nourishing_pill"))); }
    static class Recovery extends MobEffect { Recovery(){super(MobEffectCategory.BENEFICIAL,0xd45662);} @Override public boolean isDurationEffectTick(int duration,int amplifier){return duration%(amplifier>0?20:40)==0;} @Override public void applyEffectTick(LivingEntity entity,int amplifier){entity.heal(1);}}
    static class Marker extends MobEffect {Marker(int color){super(MobEffectCategory.BENEFICIAL,color);}}
    static class Medicine extends Item {
        final String id;
        Medicine(String id){super(new Properties().stacksTo(id.endsWith("liquid")?16:64));this.id=id;}
        @Override public net.minecraft.world.InteractionResultHolder<ItemStack> use(Level l,net.minecraft.world.entity.player.Player p,net.minecraft.world.InteractionHand h){p.startUsingItem(h);return net.minecraft.world.InteractionResultHolder.consume(p.getItemInHand(h));}
        @Override public int getUseDuration(ItemStack s){return 32;}
        @Override public UseAnim getUseAnimation(ItemStack s){return id.endsWith("liquid")?UseAnim.DRINK:UseAnim.EAT;}
        @Override public ItemStack finishUsingItem(ItemStack stack,Level level,LivingEntity entity){
            if(!level.isClientSide){
                boolean pill=id.endsWith("pill");
                if(id.startsWith("healing")) entity.addEffect(new MobEffectInstance(RECOVERY.get(),160,pill?1:0));
                if(id.startsWith("cleansing")){entity.removeEffect(MobEffects.POISON);if(pill)entity.addEffect(new MobEffectInstance(ANTIDOTE.get(),3600));}
                if(id.startsWith("warming"))entity.addEffect(new MobEffectInstance(WARM.get(),3600));
                if(id.startsWith("nourishing"))entity.addEffect(new MobEffectInstance(QI.get(),6000));
                if(entity instanceof net.minecraft.world.entity.player.Player p && !p.getAbilities().instabuild){stack.shrink(1);if(!pill){if(stack.isEmpty())return new ItemStack(Items.GLASS_BOTTLE);if(!p.getInventory().add(new ItemStack(Items.GLASS_BOTTLE)))p.drop(new ItemStack(Items.GLASS_BOTTLE),false);}}
            }return stack;
        }
    }
}
