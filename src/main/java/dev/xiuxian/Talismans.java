package dev.xiuxian;

import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.entity.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.RegistryObject;
import java.util.*;

public final class Talismans {
    public static final RegistryObject<net.minecraft.world.level.levelgen.feature.Feature<?>> ORE_FEATURE=CultivationResources.FEATURES.register("cinnabar_ore",CinnabarFeature::new);
    public static final RegistryObject<Block> ORE=Xiuxian.BLOCKS.register("cinnabar_ore",()->new Block(BlockBehaviour.Properties.copy(Blocks.IRON_ORE)));
    public static final RegistryObject<Item> ORE_ITEM=Xiuxian.ITEMS.register("cinnabar_ore",()->new BlockItem(ORE.get(),new Item.Properties()));
    public static final RegistryObject<Item> CINNABAR=material("cinnabar"), DUST=material("spirit_stone_dust"), PAPER=material("blank_talisman_paper"), SPIRIT_CINNABAR=material("spirit_cinnabar");
    public static final RegistryObject<Item> BRUSH=Xiuxian.ITEMS.register("wolf_hair_brush",()->new Item(new Item.Properties().durability(64)));
    public static final RegistryObject<Block> TABLE=Xiuxian.BLOCKS.register("talisman_table",TalismanTableBlock::new);
    public static final RegistryObject<Item> TABLE_ITEM=Xiuxian.ITEMS.register("talisman_table",()->new BlockItem(TABLE.get(),new Item.Properties()));
    public static final RegistryObject<BlockEntityType<TalismanTableEntity>> ENTITY=Xiuxian.ENTITIES.register("talisman_table",()->BlockEntityType.Builder.of(TalismanTableEntity::new,TABLE.get()).build(null));
    public static final RegistryObject<MenuType<TalismanMenu>> MENU=Xiuxian.MENUS.register("talisman_table",()->IForgeMenuType.create(TalismanMenu::new));
    public static final Map<TalismanKind,RegistryObject<Item>> ITEMS=new EnumMap<>(TalismanKind.class);
    public static final RegistryObject<EntityType<TalismanProjectile>> PROJECTILE=Beasts.ENTITIES.register("talisman_projectile",()->EntityType.Builder.<TalismanProjectile>of(TalismanProjectile::new,MobCategory.MISC).sized(.25f,.25f).clientTrackingRange(8).updateInterval(1).build("xiuxian:talisman_projectile"));
    public static final RegistryObject<MobEffect> SHIELD=CultivationResources.EFFECTS.register("talisman_shield",TalismanEffects.Shield::new);
    public static final RegistryObject<MobEffect> SPEED=CultivationResources.EFFECTS.register("talisman_swiftness",()->new TalismanEffects.Marker(0xc9d889)
        .addAttributeModifier(Attributes.MOVEMENT_SPEED,"f6350886-a0c4-4f18-83d6-fb838dbe6280",.4,AttributeModifier.Operation.MULTIPLY_TOTAL)
        .addAttributeModifier(ForgeMod.STEP_HEIGHT_ADDITION.get(),"f6350886-a0c4-4f18-83d6-fb838dbe6281",.5,AttributeModifier.Operation.ADDITION));
    public static final RegistryObject<MobEffect> SIGHT=CultivationResources.EFFECTS.register("talisman_heavenly_eye",()->new TalismanEffects.Marker(0xc3a3ed));
    static { for(var kind:TalismanKind.values()) ITEMS.put(kind,Xiuxian.ITEMS.register(kind.id,()->new TalismanItem(kind))); }
    private static RegistryObject<Item> material(String id) { return Xiuxian.ITEMS.register(id,()->new Item(new Item.Properties())); }
    private Talismans() {}
    public static void register() {}
    public static boolean enhanced(ItemStack stack) { return stack.hasTag() && stack.getTag().getBoolean("Enhanced"); }
}
