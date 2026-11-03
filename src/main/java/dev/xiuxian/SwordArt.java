package dev.xiuxian;

import java.util.List;

/** Trial costs and timings, in spiritual energy and server ticks. */
public record SwordArt(String id, String name, int cost, int cooldown) {
    public static final List<SwordArt> ALL=List.of(
        new SwordArt("drive","驭剑诀",6,20), new SwordArt("recall","归剑诀",1,10),
        new SwordArt("pierce","贯虹诀",12,60), new SwordArt("sweep","横扫诀",14,80),
        new SwordArt("combo","连锋诀",24,120), new SwordArt("armor_break","破甲诀",18,100),
        new SwordArt("guard","护身剑诀",8,120), new SwordArt("intercept","截击诀",10,40),
        new SwordArt("return_strike","回锋诀",12,60), new SwordArt("shadows","分光诀",24,120),
        new SwordArt("charge","蓄势诀",10,80), new SwordArt("step","踏锋步",8,40));
    public static SwordArt find(String id){return ALL.stream().filter(a->a.id.equals(id)).findFirst().orElse(null);}
}
