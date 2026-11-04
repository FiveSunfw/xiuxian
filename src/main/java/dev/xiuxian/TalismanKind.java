package dev.xiuxian;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** One definition shared by drawing, previews, and JEI. */
public enum TalismanKind {
    FIRE("fire_talisman", HerbKind.SUN, 15, 70),
    ICE("ice_talisman", HerbKind.FROST, 15, 70),
    THUNDER("thunder_talisman", null, 40, 50),
    ARMOR("armor_talisman", HerbKind.STONE, 20, 65),
    HEAL("healing_talisman", HerbKind.WOOD, 20, 65),
    SPEED("swiftness_talisman", HerbKind.MARSH, 15, 70),
    ROCK("rockbreaking_talisman", HerbKind.QI, 15, 75),
    SIGHT("heavenly_eye_talisman", HerbKind.MOSS, 20, 65),
    WATER("waterward_talisman", HerbKind.TIDE, 15, 70);
    public final String id;
    public final HerbKind herb;
    public final int energy, baseChance;
    TalismanKind(String id, HerbKind herb, int energy, int baseChance) {
        this.id=id; this.herb=herb; this.energy=energy; this.baseChance=baseChance;
    }
    public Item catalyst() { return herb == null ? CultivationResources.STONE_2.get() : Xiuxian.ELIXIRS.get(herb).get(); }
    public int chance(int realm) { return Math.min(100, baseChance + Math.max(0, realm-1)*5); }
    public ItemStack output(boolean enhanced) {
        ItemStack stack=new ItemStack(Talismans.ITEMS.get(this).get());
        if(enhanced) stack.getOrCreateTag().putBoolean("Enhanced",true);
        return stack;
    }
    public static TalismanKind recipe(ItemStack catalyst) {
        for(var kind:values()) if(catalyst.is(kind.catalyst())) return kind;
        return null;
    }
}
