package cn.blockforge.generated.generatedmod.combat;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.items.SpiritSwordItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import java.util.UUID;
import cn.blockforge.generated.generatedmod.ClientPacketSink;
import cn.blockforge.generated.generatedmod.Network;
import cn.blockforge.generated.generatedmod.network.packet.heartsword.HeartForgeContractPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.ServerLifecycleHooks;

/** Player-owned authority, independent of where the physical sword is stored. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID)
public final class HeartForgeContracts {
    public static final String TAG="HeartForgeContract";
    public static UUID contract(ItemStack stack) {
        return stack.hasTag()&&stack.getTag().hasUUID(TAG)?stack.getTag().getUUID(TAG):null;
    }
    public static boolean active(Player owner,ItemStack stack) {
        var data=GeneratedMod.data(owner);
        return data!=null&&SpiritSwordItem.isHeartForged(stack)&&owner.getUUID().equals(SpiritSwordItem.heartOwner(stack))
                &&contract(stack)!=null&&contract(stack).equals(data.wuxingxiantu$getHeartForgeContract());
    }
    public static void bind(Player owner,ItemStack stack) {
        var data=GeneratedMod.data(owner);
        if(data==null||!SpiritSwordItem.isSword(stack)||!cn.blockforge.generated.generatedmod.crafting.EquipmentBond.canBind(owner,stack))return;
        UUID id=UUID.randomUUID();
        SpiritSwordItem.setHeartForged(stack,owner.getUUID());
        stack.getOrCreateTag().putUUID(TAG,id);
        data.wuxingxiantu$setHeartForgeContract(id);
        publish(owner,id);
    }
    public static void revoke(Player owner) {
        var data=GeneratedMod.data(owner);
        if(data!=null)data.wuxingxiantu$setHeartForgeContract(null);
        publish(owner,null);
    }
    public static boolean visible(ItemStack stack) {
        if(!SpiritSwordItem.isHeartForged(stack)||contract(stack)==null)return false;
        if(EffectiveSide.get().isClient())return ClientPacketSink.heartForgeVisible.test(stack);
        var server=ServerLifecycleHooks.getCurrentServer();
        var owner=server==null?null:server.getPlayerList().getPlayer(SpiritSwordItem.heartOwner(stack));
        return owner!=null&&active(owner,stack);
    }
    private static void publish(Player owner,UUID contract) {
        if(owner instanceof ServerPlayer)
            Network.CHANNEL.send(PacketDistributor.ALL.noArg(),new HeartForgeContractPacket(owner.getUUID(),contract));
    }
    @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent event) {
        if(!(event.getEntity() instanceof ServerPlayer player))return;
        for(var owner:player.server.getPlayerList().getPlayers()) {
            var data=GeneratedMod.data(owner);
            Network.CHANNEL.send(PacketDistributor.PLAYER.with(()->player),
                    new HeartForgeContractPacket(owner.getUUID(),data==null?null:data.wuxingxiantu$getHeartForgeContract()));
        }
        var data=GeneratedMod.data(player);
        publish(player,data==null?null:data.wuxingxiantu$getHeartForgeContract());
    }
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {publish(event.getEntity(),null);}
    private HeartForgeContracts() {}
}
