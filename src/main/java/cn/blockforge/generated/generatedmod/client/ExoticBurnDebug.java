package cn.blockforge.generated.generatedmod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="wuxingxiantu",value=Dist.CLIENT)
public final class ExoticBurnDebug {
    @SubscribeEvent public static void register(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("wuxing").then(Commands.literal("debug")
                .then(Commands.literal("exoticburn").executes(context->{
                    var mc=Minecraft.getInstance();
                    var target=mc.hitResult instanceof EntityHitResult hit?hit.getEntity():mc.player;
                    if(target==null||mc.player==null)return 0;
                    var burn=ExoticBurnClientState.state(target);
                    mc.player.displayClientMessage(Component.literal("Target: "+target.getUUID()
                            +" | Vanilla fire: "+target.isOnFire()+" | Vanilla ticks (client estimate): "+target.getRemainingFireTicks()
                            +" | Exotic burn: "+(burn!=null)+" | Fire id: "+(burn==null?"none":burn.sourceFireId())
                            +" | Exotic ticks: "+ExoticBurnClientState.remaining(target)+" | Source: "+(burn==null?"none":burn.sourceUuid())
                            +" | World draws: "+ExoticBurnRenderer.renderedEntities+" | Overlay draws: "+ExoticBurnOverlay.renderedFrames),false);
                    return 1;
                }))));
    }
    private ExoticBurnDebug() {}
}
