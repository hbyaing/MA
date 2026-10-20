package cn.blockforge.generated.generatedmod.realmflight;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.Network;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID)
public final class RealmFlightTracking {
    private static final Map<ServerPlayer,RealmFlightVisualPacket> LAST=new WeakHashMap<>();
    public static boolean active(Player p) {
        var data=GeneratedMod.data(p);
        return RealmFlightControl.eligible(p) && data!=null && data.wuxingxiantu$getRealm()>=3 && p.isAlive() && !p.isCreative() && !p.isSpectator()
                && !p.getPersistentData().getBoolean(RealmFlightModePacket.VANILLA)
                && p.getAbilities().flying && !p.isPassenger() && !p.isFallFlying() && !p.isInWater()
                && !"jin_dou_yun".equals(data.wuxingxiantu$getActiveDivineArt())
                ;
    }
    @SubscribeEvent public static void tick(TickEvent.ServerTickEvent e) {
        if(e.phase!=TickEvent.Phase.END)return;
        for(var p:e.getServer().getPlayerList().getPlayers()) {
            var packet=packet(p);var old=LAST.put(p,packet);
            if(!packet.equals(old))Network.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(()->p),packet);
        }
    }
    @SubscribeEvent public static void tracking(PlayerEvent.StartTracking e) {
        if(e.getEntity() instanceof ServerPlayer observer && e.getTarget() instanceof ServerPlayer target)
            Network.sendToPlayer(observer,packet(target));
    }
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){LAST.remove(e.getEntity());}
    @SubscribeEvent public static void respawn(PlayerEvent.PlayerRespawnEvent e){LAST.remove(e.getEntity());}
    @SubscribeEvent public static void dimension(PlayerEvent.PlayerChangedDimensionEvent e){LAST.remove(e.getEntity());}
    @SubscribeEvent public static void stopping(ServerStoppingEvent e){LAST.clear();}
    private static RealmFlightVisualPacket packet(ServerPlayer p){
        var d=GeneratedMod.data(p);
        int realm=d==null?0:Math.max(0,Math.min(8,d.wuxingxiantu$getRealm()));
        int visual=RealmFlightSelection.sanitizePreference(realm,p.getPersistentData().getInt(RealmFlightModePacket.VISUAL_REALM));
        return new RealmFlightVisualPacket(p.getUUID(),active(p),realm,
                d==null?0:Math.max(0,Math.min(3,d.wuxingxiantu$getCultivationStage())),
                d!=null&&d.wuxingxiantu$isGhostImmortal(),RealmFlightControl.boosting(p),visual);
    }
    private RealmFlightTracking(){}
}

