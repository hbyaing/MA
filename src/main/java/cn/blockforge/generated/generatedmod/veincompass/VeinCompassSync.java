package cn.blockforge.generated.generatedmod.veincompass;

import cn.blockforge.generated.generatedmod.*;
import cn.blockforge.generated.generatedmod.items.SpiritVeinCompassItem;
import cn.blockforge.generated.generatedmod.world.SpiritVeinData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

/** One server-to-holder update per second; never loads or generates chunks. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID)
public final class VeinCompassSync {
    public static boolean holding(Player player){return player!=null&&(player.getMainHandItem().getItem() instanceof SpiritVeinCompassItem||player.getOffhandItem().getItem() instanceof SpiritVeinCompassItem);}
    public static boolean permitted(Player player){return player!=null&&player.isAlive()&&!player.isSpectator()&&holding(player);}
    public static VeinCompassStatePacket snapshot(ServerPlayer player){var data=SpiritVeinData.get(player.serverLevel());var position=player.blockPosition();var vein=data.nearest(position,384);return new VeinCompassStatePacket(player.level().dimension().location().toString(),data.auraAt(position),vein.isPresent(),vein.map(SpiritVeinData.Entry::center).orElse(BlockPos.ZERO),vein.map(v->(int)Math.round(Math.sqrt(v.center().distSqr(position)))).orElse(0),vein.map(SpiritVeinData.Entry::grade).orElse(0));}
    @SubscribeEvent public static void tick(TickEvent.PlayerTickEvent event){if(event.phase!=TickEvent.Phase.END||!(event.player instanceof ServerPlayer player)||player.tickCount%20!=0||!permitted(player))return;Network.CHANNEL.send(PacketDistributor.PLAYER.with(()->player),snapshot(player));}
    private VeinCompassSync(){}
}
