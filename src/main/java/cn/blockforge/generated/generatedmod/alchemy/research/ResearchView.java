package cn.blockforge.generated.generatedmod.alchemy.research;
import cn.blockforge.generated.generatedmod.*;
import cn.blockforge.generated.generatedmod.alchemy.*;
import cn.blockforge.generated.generatedmod.alchemy.batch.*;
import cn.blockforge.generated.generatedmod.alchemy.effects.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
/** Public observations only. Never sends original inventory, seed, rules or hidden process history. */
public final class ResearchView {
    public static boolean sample(ItemStack s){return !s.isEmpty()&&(PillSnapshot.isV5(s)||PillData.read(s)!=null||s.is(WuXingItems.PILL_ASH.get())||s.is(WuXingItems.WASTE_PILL.get())||s.is(WuXingItems.ALCHEMY_RESIDUE.get()));}
    public static boolean authenticate(ServerPlayer p,ItemStack s){
        if(!sample(s))return false;
        if(s.hasTag()&&s.getTag().hasUUID(V5Batch.KEY))try{var d=new BatchJournal(p.server).read(s.getTag().getUUID(V5Batch.KEY));if(!d.getString("State").equals("CLAIMED"))return false;return s.is(WuXingItems.V5_PILL.get())?PillSnapshot.matches(s,d):net.minecraft.world.item.ItemStack.isSameItemSameTags(s,V5Batch.result(d));}catch(RuntimeException e){return false;}
        return !PillSnapshot.isV5(s);
    }
    public static String effect(ItemStack s){var r=PillSnapshot.view(s);if(r!=null&&r.grade()>0)return PillSnapshot.moduleName(r.effects().get(0).id());var old=PillData.read(s);return old!=null?"旧制丹药，按现有记载":"残留物，尚无深入鉴定";}
    public static String fingerprint(ItemStack s){if(s.hasTag()&&s.getTag().hasUUID(V5Batch.KEY))return AbilityCompiler.hash(s.getTag().getUUID(V5Batch.KEY).toString());var item=s.copyWithCount(1);if(item.hasTag()){item.getTag().remove("display");item.getTag().remove("RepairCost");}return AbilityCompiler.hash(item.save(new net.minecraft.nbt.CompoundTag()).toString());}
    private ResearchView(){}
}
