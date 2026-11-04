package dev.xiuxian;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public enum SpiritFoodKind {
    RICE("jade_rice_meal", 8, .8F, Items.BOWL),
    BREAD("green_spirit_flatbread", 6, .6F, null),
    STEAK("dragon_pepper_steak", 10, 1F, null),
    STEW("earthfire_mutton_stew", 10, 1F, Items.BOWL),
    POTATO("armor_baked_potato", 7, .7F, null),
    SOUP("purple_shoot_fish_soup", 8, .8F, Items.BOWL),
    DRINK("dew_melon_drink", 2, .2F, Items.GLASS_BOTTLE),
    CAKE("starlight_honey_cake", 5, .6F, null);
    public final String id;
    public final int nutrition;
    public final float saturation;
    public final Item container;
    SpiritFoodKind(String id, int nutrition, float saturation, Item container) {
        this.id = id; this.nutrition = nutrition; this.saturation = saturation; this.container = container;
    }
}
