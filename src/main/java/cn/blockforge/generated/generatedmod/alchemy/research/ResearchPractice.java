package cn.blockforge.generated.generatedmod.alchemy.research;

import cn.blockforge.generated.generatedmod.alchemy.batch.*;
import cn.blockforge.generated.generatedmod.alchemy.effects.*;
import cn.blockforge.generated.generatedmod.progression.PlayerCrafts;
import cn.blockforge.generated.generatedmod.plants.HerbAge;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import java.util.*;

/** Actual terminal batches, including failures; no XP, promotion or hidden control multiplier. */
public final class ResearchPractice {
    public static void record(MinecraftServer server,CompoundTag doc){
        if(doc.getString("State").equals("ACTIVE"))return;
        var store=PlayerCrafts.get(server);var row=store.profession(doc.getUUID("Owner"),"alchemy");
        var records=row.getList("V5Practice",Tag.TAG_COMPOUND);String id=doc.getUUID("Id").toString();
        if(records.size()>=128||records.stream().anyMatch(t->((CompoundTag)t).getString("Batch").equals(id)))return;
        var core=V5Batch.core(doc);var record=new CompoundTag();record.putString("Batch",id);
        record.putString("Rules",core.rulesHash());record.putString("Outcome",core.status().name());record.putLong("Steps",core.tick());
        record.putInt("Fuel",doc.getInt("FuelSpent"));record.putInt("Damage",doc.getInt("StructureDamage"));record.putString("Entry",doc.getString("Entry"));
        var result=doc.contains("PillResult")?PillSnapshot.read(doc.getCompound("PillResult")):null;
        record.putInt("Grade",result==null?0:result.grade());record.putInt("Quantity",result==null?0:result.quantity());
        // Quantized age and material counts, not batch UUID/seed: a one-year tweak is not a new discovery.
        var features=new ArrayList<String>();for(var t:doc.getList("Materials",Tag.TAG_COMPOUND)){
            var item=ItemStack.of((CompoundTag)t);features.add(BuiltInRegistries.ITEM.getKey(item.getItem())+":"+item.getCount()+":"+(HerbAge.years(item)/100));
        }
        Collections.sort(features);String operations=core.operations().stream().map(o->o.action()+":"+o.amount()+":"+o.resource()).reduce("",(a,b)->a+";"+b);
        record.putString("Feature",AbilityCompiler.hash(features+"/"+operations+"/"+core.status()+"/"+record.getInt("Grade")));
        records.add(record);row.put("V5Practice",records);store.setDirty();
    }
    public static ListTag records(ServerPlayer p){return PlayerCrafts.get(p.server).profession(p.getUUID(),"alchemy").getList("V5Practice",Tag.TAG_COMPOUND).copy();}
    public static int diverse(ServerPlayer p){return (int)records(p).stream().map(t->((CompoundTag)t).getString("Feature")).distinct().count();}
    public static String outcome(CompoundTag n){return n.getInt("Grade")>0?n.getInt("Grade")+"品 / "+n.getInt("Quantity")+"颗":switch(n.getString("Outcome")){case "EXPLODED"->"压力或设备事故";case "ASHED"->"热损伤化灰";case "ABORTED"->"主动收束";default->"未成丹";};}
    private ResearchPractice(){}
}
