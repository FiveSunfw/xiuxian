package dev.xiuxian;

import net.minecraft.world.item.ItemStack;

public enum FormationKind {
    GATHERING("gathering_formation_token",1), GUARDIAN("guardian_formation_token",1), ASSAULT("assault_formation_token",.5);
    public final String id;
    public final double upkeep;
    FormationKind(String id,double upkeep) { this.id=id;this.upkeep=upkeep; }
    public static FormationKind from(ItemStack stack) {
        for(var kind:values())if(stack.is(Formations.TOKENS.get(kind).get()))return kind;
        return null;
    }
}
