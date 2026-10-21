package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.fire.ExoticBurnSnapshots;
import cn.blockforge.generated.generatedmod.network.packet.cultivation.ExoticBurnPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT)
public final class ExoticBurnClientState {
    private static final ExoticBurnSnapshots CACHE=new ExoticBurnSnapshots();
    private static ClientLevel level;
    private static void ensureLevel() {
        var current=Minecraft.getInstance().level;
        if(current!=level){CACHE.clear();level=current;}
    }
    public static void accept(ExoticBurnPacket packet) {
        ensureLevel();if(level!=null)CACHE.accept(packet,level.dimension().location(),level.getGameTime());
    }
    public static ExoticBurnPacket state(Entity entity) {
        ensureLevel();return level==null||entity.level()!=level||!entity.isAlive()?null:CACHE.get(entity.getId(),entity.getUUID(),level.getGameTime());
    }
    public static boolean isBurning(Entity entity) {return state(entity)!=null;}
    public static java.util.List<ExoticBurnPacket> active() {
        ensureLevel();return level==null?java.util.List.of():CACHE.active(level.getGameTime());
    }
    @SubscribeEvent public static void beforeEntities(net.minecraftforge.client.event.RenderLevelStageEvent event) {
        if(event.getStage()!=net.minecraftforge.client.event.RenderLevelStageEvent.Stage.AFTER_SKY)return;
        for(var snapshot:active()) {
            var entity=level.getEntity(snapshot.entityId());
            if(entity!=null&&snapshot.entityUuid().equals(entity.getUUID()))entity.setSharedFlagOnFire(false);
        }
    }
    @SubscribeEvent public static void suppressVanillaOverlay(net.minecraftforge.client.event.RenderBlockScreenEffectEvent event) {
        if(event.getOverlayType()==net.minecraftforge.client.event.RenderBlockScreenEffectEvent.OverlayType.FIRE
                &&isBurning(event.getPlayer()))event.setCanceled(true);
    }
    public static int remaining(Entity entity) {
        return state(entity)==null?0:CACHE.remaining(entity.getId(),entity.getUUID(),level.getGameTime());
    }
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e) {
        if(e.phase!=TickEvent.Phase.END)return;ensureLevel();if(level!=null)CACHE.tick(level.getGameTime());
    }
    @SubscribeEvent public static void unload(EntityLeaveLevelEvent e) {
        if(e.getLevel().isClientSide)CACHE.remove(e.getEntity().getId(),e.getEntity().getUUID());
    }
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut e) {CACHE.clear();level=null;}
    private ExoticBurnClientState() {}
}
