package cn.blockforge.generated.generatedmod.alchemy.batch;

import cn.blockforge.generated.generatedmod.*;
import cn.blockforge.generated.generatedmod.alchemy.*;
import cn.blockforge.generated.generatedmod.fire.FireBottleData;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import java.util.*;

/** Heavenly adapter: same simulation, effects and delivery; separate, durably receipted player escrow. */
public final class PortableBatch {
    public static void validate(CompoundTag doc){
        if(!doc.getString("Entry").equals("heavenly"))return;
        {
            if(!doc.contains("PlayerInputs")||!doc.contains("Visual")||!doc.contains("InputCommitted")||doc.getInt("PlayerManaCost")<0)throw new IllegalArgumentException("Invalid portable escrow");
            var rows=doc.getList("PlayerInputs",Tag.TAG_COMPOUND);var slots=new HashSet<Integer>();if(rows.isEmpty()||rows.size()>36)throw new IllegalArgumentException("Portable input budget");
            for(var entry:rows){var row=(CompoundTag)entry;int slot=row.getInt("Slot"),count=row.getInt("Count");if(slot<0||slot>=36||!slots.add(slot)||count<1||count>ItemStack.of(row.getCompound("Before")).getCount())throw new IllegalArgumentException("Invalid portable input reservation");}
        }
    }
    private static CompoundTag persisted(ServerPlayer p){var root=p.getPersistentData();if(!root.contains(Player.PERSISTED_NBT_TAG))root.put(Player.PERSISTED_NBT_TAG,new CompoundTag());return root.getCompound(Player.PERSISTED_NBT_TAG);}
    public static CompoundTag current(ServerPlayer p){return new BatchJournal(p.server).portable(p.getUUID());}
    public static boolean pending(ServerPlayer p){var d=current(p);return !d.isEmpty()&&!d.getString("State").equals("CLAIMED");}
    public static int fuel(ServerPlayer p,AlchemyRecipe recipe){int tier=recipe.furnaceTier();return V5Batch.profile(V5Batch.currentProfile(p.serverLevel()),AlchemyBalance.burnPerSecond(tier)).requiredFuel();}
    public static boolean start(ServerPlayer p,AlchemyRecipe recipe,int[] used,CompoundTag shell){
        if(pending(p)||Network.isAlchemyBusy(p))return false;
        int tier=recipe.furnaceTier();
        var items=NonNullList.withSize(AlchemyFurnaceBlockEntity.CONTAINER_SIZE,ItemStack.EMPTY);var selected=new int[8];var additional=new ArrayList<ItemStack>();var paid=new ListTag();int at=0;
        var order=new ArrayList<Integer>();for(int i=0;i<used.length;i++)if(used[i]>0)order.add(i);
        order.sort(Comparator.comparingInt((Integer i)->p.getInventory().getItem(i).is(WuXingBlocks.MOON_BOTTLE_ITEM.get())?0:1).thenComparingInt(Integer::intValue));
        for(int i:order){var source=p.getInventory().getItem(i);if(used[i]>source.getCount())return false;var item=source.copyWithCount(used[i]);var row=new CompoundTag();row.putInt("Slot",i);row.put("Before",source.save(new CompoundTag()));row.putInt("Count",used[i]);paid.add(row);
            if(at<8){items.set(at,item);selected[at++]=item.getCount();}else additional.add(item);
        }
        items.set(AlchemyFurnaceBlockEntity.IGNITION_SLOT,FireBottleData.fresh(GeneratedMod.data(p).wuxingxiantu$getEquippedFire()));
        int fuel=fuel(p,recipe);int cost=Math.max(fuel,HeavenlyFurnace.cost(recipe,GeneratedMod.data(p)));if(!p.isCreative()&&GeneratedMod.data(p).wuxingxiantu$getAura()<cost)return false;
        var doc=V5Batch.prepare(p.serverLevel(),BlockPos.containing(HeavenlyFurnace.fireballCenter(shell)),p,tier,0,fuel,items,selected,"heavenly",recipe.getId().toString(),additional);
        var remainder=V5Batch.inventory(doc);int materialIndex=0;
        for(var e:paid){var row=(CompoundTag)e;if(materialIndex<8&&!remainder.get(materialIndex).isEmpty())row.put("Remainder",remainder.get(materialIndex).save(new CompoundTag()));materialIndex++;}
        doc.put("PlayerInputs",paid);doc.putInt("PlayerManaCost",p.isCreative()?0:cost);doc.put("Visual",shell.copy());doc.putBoolean("InputCommitted",false);
        var journal=new BatchJournal(p.server);journal.write(doc);journal.linkPortable(p.getUUID(),doc.getUUID("Id"));
        return commitInputs(p,journal,doc);
    }
    private static boolean commitInputs(ServerPlayer p,BatchJournal journal,CompoundTag doc){
        if(doc.getBoolean("InputCommitted"))return true;
        var receipts=persisted(p).getCompound("V5EntryReceipts");String key=doc.getUUID("Id").toString();
        if(!receipts.getBoolean(key)){
            for(var entry:doc.getList("PlayerInputs",Tag.TAG_COMPOUND)){var row=(CompoundTag)entry;int slot=row.getInt("Slot");if(slot<0||slot>=36||!ItemStack.matches(p.getInventory().getItem(slot),ItemStack.of(row.getCompound("Before"))))throw new IllegalStateException("Portable escrow input changed; retained for recovery");}
            var data=GeneratedMod.data(p);int cost=doc.getInt("PlayerManaCost");if(data==null||data.wuxingxiantu$getAura()<cost)throw new IllegalStateException("Portable escrow mana unavailable; retained for recovery");
            for(var entry:doc.getList("PlayerInputs",Tag.TAG_COMPOUND)){var row=(CompoundTag)entry;p.getInventory().getItem(row.getInt("Slot")).shrink(row.getInt("Count"));if(row.contains("Remainder"))p.getInventory().setItem(row.getInt("Slot"),ItemStack.of(row.getCompound("Remainder")));}
            data.wuxingxiantu$setAura(data.wuxingxiantu$getAura()-cost);receipts=new CompoundTag();receipts.putBoolean(key,true);persisted(p).put("V5EntryReceipts",receipts);
            data.wuxingxiantu$setDivineCooldown(HeavenlyFurnace.ID,p.level().getGameTime()+DivineArtCatalog.cooldownTicks(HeavenlyFurnace.ID));
        }
        p.getInventory().setChanged();p.server.getPlayerList().save(p);
        try{var saved=NbtIo.readCompressed(p.server.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).resolve("playerdata").resolve(p.getUUID()+".dat").toFile());
            if(saved==null||!saved.getCompound("ForgeData").getCompound(Player.PERSISTED_NBT_TAG).getCompound("V5EntryReceipts").getBoolean(key))throw new IllegalStateException("Portable payment save not confirmed");
        }catch(java.io.IOException e){throw new IllegalStateException("Portable payment receipt retained",e);}
        doc=doc.copy();doc.putBoolean("InputCommitted",true);journal.write(doc);GeneratedMod.sync(p);p.inventoryMenu.broadcastChanges();return true;
    }
    public static CompoundTag restore(ServerPlayer p){var journal=new BatchJournal(p.server);var doc=current(p);if(doc.isEmpty()||doc.getString("State").equals("CLAIMED"))return new CompoundTag();V5Batch.validate(doc);if(!commitInputs(p,journal,doc))return new CompoundTag();return journal.read(doc.getUUID("Id"));}
    public static CompoundTag advance(ServerPlayer p){
        var doc=current(p);if(doc.getString("State").equals("CLAIMED"))return doc;doc=restore(p);if(doc.isEmpty())return doc;var journal=new BatchJournal(p.server);
        if(doc.getString("State").equals("ACTIVE")){
            var shell=doc.getCompound("Visual");long now=p.level().getGameTime();
            if(now-shell.getLong("Start")<HeavenlyFurnace.GATHER_TICKS){doc.putLong("LastWorldTick",now);journal.write(doc);return doc;}
            if(now-doc.getLong("LastWorldTick")>=20){doc=V5Batch.advance(doc,p.serverLevel(),BlockPos.of(doc.getLong("Position")));journal.write(doc);}
        }
        if(!doc.getString("State").equals("ACTIVE"))BatchDelivery.claim(p,doc.getUUID("Id"));return journal.read(doc.getUUID("Id"));
    }
    public static void cancel(ServerPlayer p){var doc=restore(p);if(doc.isEmpty())return;var journal=new BatchJournal(p.server);if(doc.getString("State").equals("ACTIVE")){doc=V5Batch.seal(doc);journal.write(doc);}if(p.isAlive()&&!p.isSpectator())BatchDelivery.claim(p,doc.getUUID("Id"));}
    private PortableBatch(){}
}
