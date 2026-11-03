package dev.xiuxian;

import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.registries.*;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import java.util.EnumMap;
import java.util.Map;

@Mod(Xiuxian.ID)
public class Xiuxian {
    public static final String ID = "xiuxian";
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ID);
    public static final DeferredRegister<BlockEntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, ID);
    public static final Map<HerbKind, RegistryObject<Block>> HERBS = new EnumMap<>(HerbKind.class);
    public static final Map<HerbKind, RegistryObject<Item>> HERB_ITEMS = new EnumMap<>(HerbKind.class);
    public static final Map<HerbKind, RegistryObject<Item>> ELIXIRS = new EnumMap<>(HerbKind.class);
    static {
        for (HerbKind kind : HerbKind.values()) {
            RegistryObject<Block> block = BLOCKS.register(kind.id, () -> new QiHerb(kind));
            HERBS.put(kind, block);
            HERB_ITEMS.put(kind, ITEMS.register(kind.id, () -> new QiHerbItem(kind, block.get())));
            ELIXIRS.put(kind, ITEMS.register(kind.elixirId, () -> new InitiationItem(true)));
        }
    }
    // Keep original IDs and references compatible with existing saves and integrations.
    public static final RegistryObject<Block> HERB = HERBS.get(HerbKind.QI);
    public static final RegistryObject<Item> HERB_ITEM = HERB_ITEMS.get(HerbKind.QI);
    public static final RegistryObject<Item> LIQUID = ELIXIRS.get(HerbKind.QI);
    public static final RegistryObject<Block> FURNACE = BLOCKS.register("herbal_furnace", HerbalFurnace::new);
    public static final RegistryObject<Item> FURNACE_ITEM = ITEMS.register("herbal_furnace", () -> new BlockItem(FURNACE.get(), new Item.Properties()));
    public static final RegistryObject<BlockEntityType<FurnaceEntity>> FURNACE_ENTITY = ENTITIES.register("herbal_furnace", () -> BlockEntityType.Builder.of(FurnaceEntity::new, FURNACE.get()).build(null));
    public static final RegistryObject<MenuType<FurnaceMenu>> FURNACE_MENU = MENUS.register("herbal_furnace", () -> IForgeMenuType.create(FurnaceMenu::new));
    public Xiuxian() {
        var bus = FMLJavaModLoadingContext.get().getModEventBus();
        CultivationResources.register(bus);
        BLOCKS.register(bus); ITEMS.register(bus); ENTITIES.register(bus); MENUS.register(bus);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Cultivation.CONFIG);
        Cultivation.registerNetwork();
        bus.addListener((net.minecraftforge.event.BuildCreativeModeTabContentsEvent e) -> {
            if (e.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
                for (HerbKind kind : HerbKind.values()) { e.accept(HERB_ITEMS.get(kind)); e.accept(ELIXIRS.get(kind)); }
                e.accept(FoundationItems.BLANK); e.accept(FoundationItems.TESTER); for(var manual:FoundationItems.MANUALS.values()) e.accept(manual);
                e.accept(FURNACE_ITEM); e.accept(CultivationResources.SPIRIT_COPPER); e.accept(Alchemy.FURNACE_ITEM); for(var item:Alchemy.ITEMS.values()) e.accept(item); e.accept(VeinBlocks.CORE_ITEM); e.accept(VeinBlocks.DEPLETED_ITEM);
                for (var item : java.util.List.of(CultivationResources.ORE_ITEM, CultivationResources.DEEP_ORE_ITEM, CultivationResources.STONE, CultivationResources.STONE_2, CultivationResources.PILL, CultivationResources.MEAL)) e.accept(item);
            }
        });
    }
}


