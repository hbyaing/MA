package cn.blockforge.generated.generatedmod.alchemy.batch;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.common.Mod;
import java.util.*;

/** Durable inventory delivery, including login recovery before the player can move the receipt. */
@Mod.EventBusSubscriber(modid="wuxingxiantu")
public final class BatchDelivery {
    public static boolean claim(ServerPlayer player,UUID batch){
        var journal=new BatchJournal(player.server);var doc=journal.read(batch);if(doc.isEmpty())return false;V5Batch.validate(doc);
        if(!doc.getUUID("Owner").equals(player.getUUID()))return false;
        if(doc.getString("State").equals("CLAIMED"))return true;
        if(doc.getString("State").equals("READY")){
            if(player.getInventory().getFreeSlot()<0)return false;
            // Index first: a crash cannot leave an unindexed delivery intent.
            journal.pending(player.getUUID(),batch,true);doc=doc.copy();doc.putString("State","DELIVERING");doc.putUUID("Recipient",player.getUUID());journal.write(doc);
        }
        if(!doc.getString("State").equals("DELIVERING")||!doc.hasUUID("Recipient")||!doc.getUUID("Recipient").equals(player.getUUID()))return false;
        return deliver(player,journal,doc);
    }
    private static boolean deliver(ServerPlayer player,BatchJournal journal,CompoundTag doc){
        V5Batch.validate(doc);
        UUID id=doc.getUUID("Id");boolean present=false;
        for(int i=0;i<player.getInventory().getContainerSize();i++){
            var item=player.getInventory().getItem(i);if(item.hasTag()&&item.getTag().hasUUID(V5Batch.KEY)&&item.getTag().getUUID(V5Batch.KEY).equals(id)){present=true;break;}
        }
        if(!present){int slot=player.getInventory().getFreeSlot();if(slot<0)return false;player.getInventory().setItem(slot,V5Batch.result(doc));}
        player.getInventory().setChanged();
        // Cursor stacks are deliberately not used: they are not part of player.dat.
        player.server.getPlayerList().save(player);
        try{
            var file=player.server.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).resolve("playerdata").resolve(player.getUUID()+".dat").toFile();
            var saved=net.minecraft.nbt.NbtIo.readCompressed(file);boolean durable=false;
            if(saved!=null)for(var entry:saved.getList("Inventory",net.minecraft.nbt.Tag.TAG_COMPOUND)){
                var item=((CompoundTag)entry).getCompound("tag");if(item.hasUUID(V5Batch.KEY)&&item.getUUID(V5Batch.KEY).equals(id))durable=true;
            }
            if(!durable)throw new IllegalStateException("Player receipt save not durable; delivery intent retained");
        }catch(java.io.IOException e){throw new IllegalStateException("Cannot verify player receipt save",e);}
        var next=doc.copy();next.putString("State","CLAIMED");journal.write(next);
        journal.pending(player.getUUID(),id,false);player.containerMenu.broadcastChanges();return true;
    }
    public static void recover(ServerPlayer player){
        var journal=new BatchJournal(player.server);
        for(UUID id:journal.pendingFor(player.getUUID())){
            var doc=journal.read(id);
            if(doc.getString("State").equals("DELIVERING")&&doc.hasUUID("Recipient")&&doc.getUUID("Recipient").equals(player.getUUID()))deliver(player,journal,doc);
            else journal.pending(player.getUUID(),id,false);
        }
    }
    @SubscribeEvent(priority=EventPriority.HIGHEST) public static void login(PlayerEvent.PlayerLoggedInEvent event){if(event.getEntity() instanceof ServerPlayer player)safeRecover(player);}
    @SubscribeEvent public static void tick(TickEvent.PlayerTickEvent event){if(event.phase==TickEvent.Phase.START&&event.player instanceof ServerPlayer player&&player.tickCount%20==0)safeRecover(player);}
    private static void safeRecover(ServerPlayer player){try{recover(player);}catch(RuntimeException e){com.mojang.logging.LogUtils.getLogger().error("V5 delivery retained for recovery",e);}}
    private BatchDelivery(){}
}
