package dev.xiuxian;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/** Active spell-manual practice is lost on changing manuals, learned pages are retained. */
public final class ManualPractice {
    public static final int MAX_RANK=9;
    public static int rank(Player p){return Math.max(0,Math.min(MAX_RANK,CultivationFoundation.data(p).getInt("ManualRank")));}
    public static double points(Player p){return Math.max(0,CultivationFoundation.data(p).getDouble("ManualPractice"));}
    public static int required(Player p){return rank(p)==MAX_RANK?0:Cultivation.MANUAL_PRACTICE_BASE.get()*(rank(p)+1);}
    public static double power(Player p){return 1+rank(p)*Cultivation.MANUAL_POWER_PER_RANK.get();}
    public static void reset(CompoundTag tag){tag.putInt("ManualRank",0);tag.putDouble("ManualPractice",0);}
    public static void train(ServerPlayer p,double gain){
        var manual=CultivationFoundation.active(p);
        if(manual==null||!Double.isFinite(gain)||gain<=0)return;
        int rank=rank(p);double total=points(p)+gain;
        while(rank<MAX_RANK&&total>=Cultivation.MANUAL_PRACTICE_BASE.get()*(rank+1)){total-=Cultivation.MANUAL_PRACTICE_BASE.get()*(rank+1);rank++;}
        var tag=CultivationFoundation.data(p);tag.putInt("ManualRank",rank);tag.putDouble("ManualPractice",rank==MAX_RANK?0:total);CultivationFoundation.save(p,tag);
    }
    public static void promote(ServerPlayer p,int layers){
        var manual=CultivationFoundation.active(p);if(manual==null||layers<=0)return;
        var tag=CultivationFoundation.data(p);int next=Math.min(MAX_RANK,rank(p)+layers);tag.putInt("ManualRank",next);if(next==MAX_RANK)tag.putDouble("ManualPractice",0);CultivationFoundation.save(p,tag);
    }
}
