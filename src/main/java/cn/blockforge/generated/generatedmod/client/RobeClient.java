package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT)
public final class RobeClient {
    private static final Map<UUID,ItemStack> ROBES=new HashMap<>();
    private static final Map<UUID,cn.blockforge.generated.generatedmod.network.packet.yujian.RobeSyncPacket> EQUIPMENT=new HashMap<>();
    public static void accept(cn.blockforge.generated.generatedmod.network.packet.yujian.RobeSyncPacket packet){
        var client=Minecraft.getInstance();if(client.level==null)return;
        // StartTracking can arrive before the player spawn packet; UUID storage survives that ordering.
        ROBES.put(packet.owner(),packet.stack().copy());
        EQUIPMENT.put(packet.owner(),packet);
        var entity=client.level.getEntity(packet.entityId());
        if(entity instanceof net.minecraft.world.entity.player.Player player&&player.getUUID().equals(packet.owner())){
            apply(player,packet);
        }
    }
    public static ItemStack get(UUID player){return ROBES.getOrDefault(player,ItemStack.EMPTY);}
    private static void apply(net.minecraft.world.entity.player.Player player,cn.blockforge.generated.generatedmod.network.packet.yujian.RobeSyncPacket packet){
        var data=GeneratedMod.data(player);if(data!=null){data.wuxingxiantu$setRobeStack(packet.stack());data.wuxingxiantu$setYujianStack(packet.sword().copy());}
        player.getPersistentData().putBoolean("YujianSlotLocked",packet.swordLocked());
    }
    @SubscribeEvent public static void join(net.minecraftforge.event.entity.EntityJoinLevelEvent event){
        if(event.getLevel().isClientSide()&&event.getEntity() instanceof net.minecraft.world.entity.player.Player player){
            var packet=EQUIPMENT.get(player.getUUID());if(packet!=null)apply(player,packet);
        }
    }
    @SubscribeEvent public static void logout(net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggingOut event){ROBES.clear();EQUIPMENT.clear();}
    @SubscribeEvent public static void unload(net.minecraftforge.event.level.LevelEvent.Unload event){if(event.getLevel().isClientSide()){ROBES.clear();EQUIPMENT.clear();}}
    private RobeClient(){}
}
