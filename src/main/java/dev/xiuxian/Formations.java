package dev.xiuxian;

import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.RegistryObject;

public final class Formations {
    public static final RegistryObject<Block> PLATE=Xiuxian.BLOCKS.register("formation_plate",FormationBlock::new);
    public static final RegistryObject<Item> PLATE_ITEM=Xiuxian.ITEMS.register("formation_plate",()->new BlockItem(PLATE.get(),new Item.Properties()));
    public static final RegistryObject<Block> FLAG=Xiuxian.BLOCKS.register("formation_flag",FormationFlagBlock::new);
    public static final RegistryObject<Item> FLAG_ITEM=Xiuxian.ITEMS.register("formation_flag",()->new BlockItem(FLAG.get(),new Item.Properties()));
    public static final RegistryObject<Item> HEART=Xiuxian.ITEMS.register("formation_heart_stone",()->new Item(new Item.Properties()));
    public static final Map<FormationKind,RegistryObject<Item>> TOKENS=new EnumMap<>(FormationKind.class);
    public static final RegistryObject<BlockEntityType<FormationEntity>> ENTITY=Xiuxian.ENTITIES.register("formation_plate",()->BlockEntityType.Builder.of(FormationEntity::new,PLATE.get()).build(null));
    public static final RegistryObject<MenuType<FormationMenu>> MENU=Xiuxian.MENUS.register("formation_plate",()->IForgeMenuType.create(FormationMenu::new));
    public static final ResourceKey<DamageType> DAMAGE=ResourceKey.create(Registries.DAMAGE_TYPE,new ResourceLocation(Xiuxian.ID,"formation_assault"));
    static { for(var kind:FormationKind.values())TOKENS.put(kind,Xiuxian.ITEMS.register(kind.id,()->new Item(new Item.Properties().stacksTo(1)))); }
    private Formations() {}
    public static void register() {}
    public static boolean fuel(ItemStack stack) { return stack.is(CultivationResources.STONE.get())||stack.is(CultivationResources.STONE_2.get()); }
    public static double gathering(ServerLevel level,net.minecraft.core.BlockPos pos) {
        return FormationIndex.get(level).at(pos,FormationKind.GATHERING).stream().mapToDouble(FormationEntity::gatheringMultiplier).max().orElse(1);
    }
}
