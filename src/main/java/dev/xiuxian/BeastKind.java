package dev.xiuxian;

public enum BeastKind {
    RABBIT("spirit_hare","灵角兔",12,3,.55f,.7f,0xc3c6a5),
    WOLF("azure_fang_wolf","青牙狼",20,4,.8f,1f,0x526b65),
    BOAR("rockhide_boar","岩甲野猪",30,8,1.1f,1.1f,0x817462),
    VIPER("marsh_viper","泽毒蝰",24,4,.7f,.55f,0x54774e),
    SPIDER("frostback_spider","霜背蛛",32,6,1.25f,.75f,0x7796ac),
    EAGLE("windfeather_eagle","风翎鹰",34,9,.9f,.9f,0x829b8c),
    LIZARD("thundercrest_lizard","雷纹蜥",42,8,1f,.8f,0x645d83),
    KING("rockhide_beast_king","岩甲兽王",58,12,1.65f,1.7f,0x5e584f),
    PYTHON("jade_scale_python","碧鳞蟒",76,14,1.5f,1.1f,0x32766a);
    public final String id,name; public final int health,damage,color;public final float width,height;
    BeastKind(String id,String name,int health,int damage,float width,float height,int color){this.id=id;this.name=name;this.health=health;this.damage=damage;this.width=width;this.height=height;this.color=color;}
}
