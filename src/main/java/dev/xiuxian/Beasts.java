package dev.xiuxian;

import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraftforge.registries.*;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraft.world.level.levelgen.Heightmap;
import java.util.*;

public final class Beasts {
    public static final DeferredRegister<EntityType<?>> ENTITIES=DeferredRegister.create(ForgeRegistries.ENTITY_TYPES,Xiuxian.ID);
    public static final Map<BeastKind,RegistryObject<EntityType<SpiritBeast>>> TYPES=new EnumMap<>(BeastKind.class);
    public static final Map<BeastKind,RegistryObject<Item>> EGGS=new EnumMap<>(BeastKind.class);
    public static final Map<String,RegistryObject<Item>> MATERIALS=new LinkedHashMap<>();
    public static final RegistryObject<EntityType<FrostSilk>> SILK=ENTITIES.register("frost_silk",()->EntityType.Builder.<FrostSilk>of(FrostSilk::new,MobCategory.MISC).sized(.35f,.35f).clientTrackingRange(6).updateInterval(1).build("xiuxian:frost_silk"));
    static {
        for(var kind:BeastKind.values()){
            var type=ENTITIES.register(kind.id,()->EntityType.Builder.<SpiritBeast>of((t,l)->new SpiritBeast(t,l,kind),MobCategory.MONSTER).sized(kind.width,kind.height).clientTrackingRange(8).build("xiuxian:"+kind.id));TYPES.put(kind,type);
            EGGS.put(kind,Xiuxian.ITEMS.register(kind.id+"_spawn_egg",()->new net.minecraftforge.common.ForgeSpawnEggItem(type,kind.color,0xc8dba2,new Item.Properties())));
        }
        for(String id:List.of("spirit_horn","wolf_pelt","azure_fang","rockhide_plate","curved_tusk","viper_fang","venom_sac","frost_silk","frost_gland","wind_feather","eagle_talon","thunder_scale","charged_spine","thick_rock_plate","king_tusk","jade_scale","python_sinew","beast_marrow"))MATERIALS.put(id,Xiuxian.ITEMS.register(id,()->new Item(new Item.Properties())));
    }
    public static Item material(String id){return MATERIALS.get(id).get();}
    public static void register(net.minecraftforge.eventbus.api.IEventBus bus){ENTITIES.register(bus);bus.addListener(Beasts::attributes);bus.addListener(Beasts::placements);}
    static void attributes(EntityAttributeCreationEvent event){for(var kind:BeastKind.values())event.put(TYPES.get(kind).get(),net.minecraft.world.entity.monster.Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,kind.health).add(Attributes.ATTACK_DAMAGE,kind.damage).add(Attributes.MOVEMENT_SPEED,kind==BeastKind.RABBIT?.34:kind==BeastKind.WOLF?.31:.25).add(Attributes.FOLLOW_RANGE,24).add(Attributes.FLYING_SPEED,.5).add(Attributes.ARMOR,kind==BeastKind.BOAR?4:kind==BeastKind.KING?7:0).build());}
    static void placements(SpawnPlacementRegisterEvent event){for(var kind:BeastKind.values())event.register(TYPES.get(kind).get(),SpawnPlacements.Type.ON_GROUND,Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,(t,l,r,p,random)->l.getDifficulty()!=net.minecraft.world.Difficulty.PEACEFUL&&net.minecraft.world.entity.Mob.checkMobSpawnRules(t,l,r,p,random)&&(kind!=BeastKind.KING||p.getY()<40)&&(kind!=BeastKind.SPIDER||p.getY()<90)&&(kind!=BeastKind.EAGLE||p.getY()>=75&&l.canSeeSky(p))&&(kind!=BeastKind.KING||random.nextInt(16)==0)&&(kind!=BeastKind.PYTHON||random.nextInt(24)==0&&SpiritBeast.nearWater(l,p))&&(!(kind==BeastKind.KING||kind==BeastKind.PYTHON)||l.getLevel().getEntitiesOfClass(SpiritBeast.class,new net.minecraft.world.phys.AABB(p).inflate(64),e->e.kind==kind).isEmpty()),SpawnPlacementRegisterEvent.Operation.REPLACE);}
}
