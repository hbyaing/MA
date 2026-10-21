package cn.blockforge.generated.generatedmod.storage;
import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.storage.StorageBagItem;
import cn.blockforge.generated.generatedmod.storage.BagData;
import cn.blockforge.generated.generatedmod.storage.menu.BagMenu;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.stats.Stats;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID)
public final class BagMagnetEvents {
 private static java.util.UUID pickupOwner(ItemEntity entity){var tag=new net.minecraft.nbt.CompoundTag();entity.addAdditionalSaveData(tag);return tag.hasUUID("Owner")?tag.getUUID("Owner"):null;}
 @SubscribeEvent public static void tick(TickEvent.PlayerTickEvent event){if(event.phase!=TickEvent.Phase.END||!(event.player instanceof ServerPlayer player)||!player.isAlive()||player.isSpectator()||cn.blockforge.generated.generatedmod.ParalysisControl.blocked(player)||player.tickCount%10!=0)return;
  for(int slot=0;slot<player.getInventory().getContainerSize();slot++){var bag=player.getInventory().getItem(slot);if(!(bag.getItem() instanceof StorageBagItem)||!bag.hasTag()||!bag.getTag().getBoolean("bag_magnet")||!bag.getTag().hasUUID("bag_id"))continue;
   var id=bag.getTag().getUUID("bag_id");BagData data=BagData.get(player.serverLevel());var inventory=data.inventory(id);boolean changed=false;
   AABB area=new AABB(player.getX()-4.5,player.getY()-4.5,player.getZ()-4.5,player.getX()+4.5,player.getY()+4.5,player.getZ()+4.5);
   for(ItemEntity entity:player.serverLevel().getEntitiesOfClass(ItemEntity.class,area)){if(!cn.blockforge.generated.generatedmod.storage.BagMagnetRules.permitted(entity.isAlive(),entity.hasPickUpDelay(),entity.getItem().getItem() instanceof StorageBagItem,pickupOwner(entity),player.getUUID())||!cn.blockforge.generated.generatedmod.storage.BagMagnetRules.within(entity.getX()-player.getX(),entity.getY()-player.getY(),entity.getZ()-player.getZ()))continue;
    if(net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.entity.player.EntityItemPickupEvent(player,entity)))continue;
    if(entity.getItem().isEmpty()||!BagMagnetRules.permitted(entity.isAlive(),entity.hasPickUpDelay(),entity.getItem().getItem() instanceof StorageBagItem,pickupOwner(entity),player.getUUID())||!BagMagnetRules.within(entity.getX()-player.getX(),entity.getY()-player.getY(),entity.getZ()-player.getZ()))continue;
    var stack=entity.getItem();int count=stack.getCount();var item=stack.getItem();if(inventory.insert(stack)){player.take(entity,count);player.awardStat(Stats.ITEM_PICKED_UP.get(item),count);entity.discard();changed=true;}
   }
   if(changed){data.setDirty();for(ServerPlayer other:player.server.getPlayerList().getPlayers())if(other.containerMenu instanceof BagMenu menu&&id.equals(menu.bagId))menu.storageChanged();}
   return;
  }
 }
}
