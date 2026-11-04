package dev.xiuxian;

/** Food crops are independent from the initiation herbs. */
public enum FoodPlantKind {
    JADE_RICE("jade_rice"), GREEN_WHEAT("green_spirit_wheat"), DRAGON_PEPPER("dragon_blood_pepper"),
    PURPLE_BAMBOO("purple_jade_shoot"), ARMOR_POTATO("armor_potato"), DEW_MELON("dew_melon"),
    STAR_FLOWER("star_flower"), EARTH_GINGER("earthfire_ginger");
    public final String id;
    FoodPlantKind(String id) { this.id = id; }
}
