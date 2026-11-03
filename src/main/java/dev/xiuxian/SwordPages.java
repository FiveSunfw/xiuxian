package dev.xiuxian;

/** The two combat pages bound to each sword manual. Recall is a shared utility. */
public final class SwordPages {
    public static String art(ManualDefinition manual,int page){
        if(page==0)return manual.family().equals("piercing_sword")?"pierce":"drive";
        return switch(manual.family()){
            case "breeze_sword"->"step";case "stone_sword"->"guard";case "river_sword"->"combo";
            case "piercing_sword"->"armor_break";case "returning_sword"->"return_strike";
            case "lone_sword"->"charge";case "radiant_sword_fragment"->"shadows";default->"sweep";
        };
    }
}
