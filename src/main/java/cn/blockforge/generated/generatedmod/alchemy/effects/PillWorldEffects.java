package cn.blockforge.generated.generatedmod.alchemy.effects;

import cn.blockforge.generated.generatedmod.*;
import cn.blockforge.generated.generatedmod.alchemy.MedicineProgress;
import cn.blockforge.generated.generatedmod.progression.PlayerCrafts;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerPlayer;
import java.util.UUID;
import static cn.blockforge.generated.generatedmod.alchemy.effects.AbilityCompiler.*;

/** One existing medicine account, with per-dose markers for effects saved outside playerdata. */
public final class PillWorldEffects {
    private static final String KEY="V5DoseWorldReceipts";
    private static final ThreadLocal<UUID> CURRENT=new ThreadLocal<>();
    private static CompoundTag receipts(ServerPlayer p){var state=MedicineProgress.state(p);if(!state.contains(KEY))state.put(KEY,new CompoundTag());return state.getCompound(KEY);}
    public static void begin(UUID receipt){CURRENT.set(receipt);}public static void end(){CURRENT.remove();}
    public static boolean hasReceipt(ServerPlayer p,UUID receipt){return receipts(p).contains(receipt.toString());}
    public static boolean replaying(ServerPlayer p,Spec effect){UUID id=CURRENT.get();return id!=null&&receipts(p).getCompound(id.toString()).contains(effect.id().name());}
    private static CompoundTag record(ServerPlayer p,Spec effect){UUID id=CURRENT.get();if(id==null)return null;var receipts=receipts(p);if(receipts.size()>256)throw new IllegalStateException("Medicine dose marker budget");String key=id.toString();if(!receipts.contains(key))receipts.put(key,new CompoundTag());var row=receipts.getCompound(key);String module=effect.id().name(),hash=AbilityCompiler.hash(effect.toString());if(row.contains(module)){var existing=row.getCompound(module);if(!existing.getString("Spec").equals(hash))throw new IllegalStateException("World dose specification drift");return existing;}var value=new CompoundTag();value.putString("Spec",hash);row.put(module,value);PlayerCrafts.get(p.server).setDirty();return value;}
    public static void once(ServerPlayer p,Spec effect,Runnable apply){var marker=record(p,effect);if(marker!=null&&marker.getBoolean("Done"))return;apply.run();if(marker!=null)marker.putBoolean("Done",true);PlayerCrafts.get(p.server).setDirty();}
    public static void expand(ServerPlayer p,Spec effect){var data=GeneratedMod.data(p);var state=MedicineProgress.state(p);var marker=record(p,effect);int target;if(marker!=null&&marker.getBoolean("Done"))target=marker.getInt("TargetAuraBonus");else {String key="ExpansionRealm"+data.wuxingxiantu$getRealm();target=Math.min(25,data.wuxingxiantu$getAuraBonus()+(int)effect.magnitudeMilli()/1000);state.putInt(key,state.getInt(key)+1);if(marker!=null){marker.putInt("TargetAuraBonus",target);marker.putBoolean("Done",true);}PlayerCrafts.get(p.server).setDirty();}data.wuxingxiantu$setAuraBonus(Math.max(data.wuxingxiantu$getAuraBonus(),target));cn.blockforge.generated.generatedmod.items.CultivationLogic.refreshMaxMana(data);}
    public static int choice(ServerPlayer p,String domain,int size){UUID id=CURRENT.get();return id==null?p.getRandom().nextInt(size):Math.floorMod(AbilityCompiler.hash(id+"/"+domain).hashCode(),size);}
    public static void flush(ServerPlayer p,UUID receipt){if(!hasReceipt(p,receipt))return;var row=receipts(p).getCompound(receipt.toString());for(String key:row.getAllKeys())if(!row.getCompound(key).getBoolean("Done"))throw new IllegalStateException("Partial world dose retained");p.server.overworld().getDataStorage().save();var file=p.server.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).resolve("data/wuxingxiantu_player_crafts.dat");try{var saved=NbtIo.readCompressed(file.toFile());if(saved==null||!saved.getCompound("data").getCompound("Accounts").getCompound(p.getUUID().toString()).getCompound("MyriadMedicine").getCompound(KEY).contains(receipt.toString()))throw new IllegalStateException("Medicine dose marker not durable");}catch(java.io.IOException error){throw new IllegalStateException("Cannot confirm medicine dose marker",error);}}
    public static void acknowledge(ServerPlayer p,UUID receipt){if(receipts(p).contains(receipt.toString())){receipts(p).remove(receipt.toString());PlayerCrafts.get(p.server).setDirty();}}
    private PillWorldEffects(){}
}
