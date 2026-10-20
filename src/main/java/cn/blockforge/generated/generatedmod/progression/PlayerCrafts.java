package cn.blockforge.generated.generatedmod.progression;

import cn.blockforge.generated.generatedmod.*;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.saveddata.SavedData;
import java.util.UUID;

/** World-owned accounts also receive completed jobs while their owner is offline. */
public final class PlayerCrafts extends SavedData {
    public static final String[] IDS={"alchemy","refining","talisman"};
    public static final String[] NAMES={"炼丹师","炼器师","符师"};
    public static final String[] RANKS={"一品","二品","三品","四品","五品","六品","七品","八品","九品"};
    public static final int[] XP={0,100,300,700,1500,3000,6000,12000,24000}, REALMS={0,0,1,1,2,3,4,5,6}, WORKS={0,2,3,4,5,6,7,8,9}, TIERS={0,0,1,1,2,2,3,3,3};
    private final CompoundTag accounts=new CompoundTag();
    public static PlayerCrafts get(MinecraftServer s){return s.overworld().getDataStorage().computeIfAbsent(PlayerCrafts::load,PlayerCrafts::new,"wuxingxiantu_player_crafts");}
    public static PlayerCrafts load(CompoundTag n){var d=new PlayerCrafts();d.accounts.merge(n.getCompound("Accounts"));if(n.getInt("RankVersion")<2){for(String owner:d.accounts.getAllKeys())for(String id:IDS){var row=d.accounts.getCompound(owner).getCompound(id);if(row.getBoolean("Learned"))row.putInt("Rank",Math.max(0,Math.min(4,row.getInt("Rank")))*2);}d.setDirty();}return d;}
    @Override public CompoundTag save(CompoundTag n){n.put("Accounts",accounts.copy());n.putInt("RankVersion",2);return n;}
    public CompoundTag account(UUID id){String key=id.toString();if(!accounts.contains(key))accounts.put(key,new CompoundTag());return accounts.getCompound(key);}
    public static boolean valid(String id){return java.util.Arrays.asList(IDS).contains(id);}
    public CompoundTag profession(UUID owner,String id){var a=account(owner);if(!a.contains(id))a.put(id,new CompoundTag());return a.getCompound(id);}
    public static int rank(ServerPlayer p,String id){var n=get(p.server).profession(p.getUUID(),id);return n.getBoolean("Learned")?Math.max(0,Math.min(8,n.getInt("Rank"))):0;}
    public static int bonus(ServerPlayer p,String id){return get(p.server).profession(p.getUUID(),id).getBoolean("Learned")?rank(p,id)+1:0;}
    public static int refiningCost(int base,int rank){return (int)Math.min(Integer.MAX_VALUE,(Math.max(0L,base)*(40-Math.max(0,Math.min(9,rank)))+39)/40);}
    public static float talismanScore(float score,int rank){if(!Float.isFinite(score))return 0;return score<50?Math.max(0,score):Math.min(100,score+Math.max(0,Math.min(9,rank)));}
    public String learnOrPromote(ServerPlayer p,String id){
        if(!valid(id))return "未知技艺";
        var n=profession(p.getUUID(),id);
        if(!n.getBoolean("Learned")){n.putBoolean("Learned",true);setDirty();return "已习得"+NAMES[java.util.Arrays.asList(IDS).indexOf(id)]+"，制作成功可积累心得。";}
        int next=Math.max(0,Math.min(8,n.getInt("Rank")))+1;
        if(next>8)return "已臻九品。";
        var d=GeneratedMod.data(p);
        if(d==null||d.wuxingxiantu$getRealm()<REALMS[next]||n.getInt("Xp")<XP[next]||n.getCompound("Works").size()<WORKS[next]||n.getInt("BestTier")<TIERS[next])return "晋升条件不足：查看百艺页面中的心得、代表作和境界要求。";
        n.putInt("Rank",next);setDirty();return "技艺考核通过，晋升"+RANKS[next]+"。";
    }
    public void receipt(UUID owner,String id,ItemStack result,int tier){
        if(!valid(id)||result.isEmpty())return;
        var n=profession(owner,id);if(!n.getBoolean("Learned"))return;
        tier=Math.max(0,Math.min(4,tier));
        String key=BuiltInRegistries.ITEM.getKey(result.getItem()).toString()+"/"+tier;
        var works=n.getCompound("Works");int count=Math.max(0,works.getInt(key));
        // The finite item registry bounds work keys. Repeated basic work cannot train a master indefinitely.
        if(works.size()<256||works.contains(key)){works.putInt(key,Math.min(100,count+1));n.put("Works",works);}
        int r=Math.max(0,Math.min(8,n.getInt("Rank")));
        int gain=(12+8*tier)/(1+count/4);
        if(tier*2+2<r)gain=0;
        n.putInt("Xp",Math.min(100000,Math.max(0,n.getInt("Xp"))+gain));
        n.putInt("BestTier",Math.max(n.getInt("BestTier"),tier));setDirty();
    }
    public static int itemTier(ItemStack s){var pill=cn.blockforge.generated.generatedmod.alchemy.PillData.read(s);return pill==null?Math.min(4,Math.max(0,s.getMaxDamage()/600)):pill.grade().ordinal();}

    /** Source scrolls supplement practice; they never create works, improve BestTier, or promote a rank. */
    public int studyAlchemyInheritance(ServerPlayer player,int tier) {
        if(tier<0||tier>3)return 0;
        var row=profession(player.getUUID(),"alchemy");
        if(!row.getBoolean("Learned"))return 0;
        int rank=Math.max(0,Math.min(8,row.getInt("Rank")));
        if(rank>=8)return 0;
        int current=Math.max(0,row.getInt("Xp"));
        int remaining=Math.max(0,XP[rank+1]-current);
        int granted=row.getInt("InheritanceRank")==rank?Math.max(0,row.getInt("InheritanceXpGranted")):0;
        // All four tiers share one finite allowance for this rank, also after save/reload.
        int allowance=Math.max(0,(XP[rank+1]-XP[rank])/2-granted);
        int offered=tier==3?(remaining+1)/2:new int[]{100,500,1000}[tier];
        int gain=Math.min(offered,Math.min(remaining,allowance));
        if(gain<=0)return 0;
        row.putInt("Xp",current+gain);
        row.putInt("InheritanceRank",rank);
        row.putInt("InheritanceXpGranted",granted+gain);
        setDirty();
        return gain;
    }
}
