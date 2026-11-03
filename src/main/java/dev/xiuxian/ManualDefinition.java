package dev.xiuxian;

import java.util.List;

/** Shared catalogue for items, learning rules, and the client list. */
public record ManualDefinition(String id, String name, boolean sword, List<String> elements, int coverage, double speed) {
    public static final List<String> ELEMENTS = List.of("金", "木", "水", "火", "土", "冰", "雷", "风");
    public static final List<ManualDefinition> BASE = List.of(
        sword("basic_sword", "基础养剑诀", 9, 1.25),
        sword("breeze_sword", "清风剑经", 9, 1.35),
        sword("stone_sword", "磐石剑经", 9, 1.35),
        sword("river_sword", "长河剑经", 9, 1.35),
        sword("piercing_sword", "破锋剑诀", 9, 1.5),
        sword("returning_sword", "回澜剑经", 9, 1.5),
        sword("lone_sword", "孤锋剑典·炼气篇", 9, 1.75),
        sword("radiant_sword_fragment", "流光剑典·残篇", 3, 1.75),
        spell("metal_origin", "金元诀", 1.25, "金"), spell("greenwood", "青木诀", 1.25, "木"),
        spell("dark_water", "玄水诀", 1.25, "水"), spell("scarlet_fire", "赤火诀", 1.25, "火"),
        spell("thick_earth", "厚土诀", 1.25, "土"), spell("frost", "凝霜诀", 1.25, "冰"),
        spell("thunder", "引雷诀", 1.25, "雷"), spell("wind", "御风诀", 1.25, "风"),
        spell("water_fire", "水火相济经", 1.5, "水", "火"), spell("metal_wood", "金木交生经", 1.5, "金", "木"),
        spell("wood_fire", "木火生息经", 1.5, "木", "火"), spell("earth_metal", "土金凝元经", 1.5, "土", "金"),
        spell("wind_thunder", "风雷引", 1.75, "风", "雷"), spell("ice_water", "冰水归流经", 1.75, "冰", "水")
    );
    public static final List<ManualDefinition> ALL = expand();
    private static List<ManualDefinition> expand() {
        var result=new java.util.ArrayList<ManualDefinition>();
        for(var base:BASE){
            result.add(base);
            String title=base.name.replace("·炼气篇","").replace("·残篇","");
            if(base.coverage==9)result.add(new ManualDefinition(base.id+"_fragment_3",title+"·一至三层残篇",base.sword,base.elements,3,base.speed));
            else result.add(new ManualDefinition(base.id+"_complete",title+"·炼气篇",base.sword,base.elements,9,base.speed));
            result.add(new ManualDefinition(base.id+"_fragment_6",title+"·一至六层残篇",base.sword,base.elements,6,base.speed));
        }
        return List.copyOf(result);
    }
    public int qualityTier(){return speed<=1.25?0:speed<1.5?1:speed<1.75?2:3;}
    public double cultivationMultiplier(){return Cultivation.MANUAL_QUALITY_SPEEDS.get(qualityTier()).get();}
    public String family(){return id.replaceFirst("_(fragment_[36]|complete)$","");}
    private static ManualDefinition sword(String id, String name, int coverage, double speed) {
        return new ManualDefinition(id, name, true, List.of(), coverage, speed);
    }
    private static ManualDefinition spell(String id, String name, double speed, String... elements) {
        return new ManualDefinition(id, name, false, List.of(elements), 9, speed);
    }
    public static ManualDefinition find(String id) { return ALL.stream().filter(m -> m.id.equals(id)).findFirst().orElse(null); }
}
