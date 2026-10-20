package cn.blockforge.generated.generatedmod.veincompass.client;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.client.CultivationUi;
import cn.blockforge.generated.generatedmod.veincompass.*;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT,bus=Mod.EventBusSubscriber.Bus.FORGE)
public final class VeinCompassHud {
    private static VeinCompassStatePacket state;
    private static long receivedAt;
    public static void accept(VeinCompassStatePacket packet){var mc=Minecraft.getInstance();if(mc.level!=null&&VeinCompassSync.permitted(mc.player)&&packet.dimension().equals(mc.level.dimension().location().toString())){state=packet;receivedAt=mc.level.getGameTime();}}
    private static boolean valid(){var mc=Minecraft.getInstance();return state!=null&&mc.level!=null&&VeinCompassSync.permitted(mc.player)&&state.dimension().equals(mc.level.dimension().location().toString())&&mc.level.getGameTime()>=receivedAt&&mc.level.getGameTime()-receivedAt<=60;}
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event){if(event.phase==TickEvent.Phase.END&&!valid())state=null;}
    @SubscribeEvent public static void render(RenderGuiOverlayEvent.Post event){var mc=Minecraft.getInstance();if(!valid()||mc.options.hideGui||mc.screen!=null||!event.getOverlay().id().equals(VanillaGuiOverlay.HOTBAR.id()))return;
        var g=event.getGuiGraphics();int width=Math.min(280,mc.getWindow().getGuiScaledWidth()-16);
        int x=Math.min(cn.blockforge.generated.generatedmod.client.WuXingClient.hudLeft(mc),mc.getWindow().getGuiScaledWidth()-width-8);
        int y=cn.blockforge.generated.generatedmod.client.WuXingClient.hudBottom(mc)+6;
        if(y+50>mc.getWindow().getGuiScaledHeight()-45)y=Math.max(8,cn.blockforge.generated.generatedmod.client.WuXingClient.hudTop(mc)-56);
        if(width<100)return;
        CultivationUi.panel(g,x,y,width,50,false);int color=CompassPresentation.color(state.concentration());
        g.renderItem(mc.player.getMainHandItem().getItem() instanceof cn.blockforge.generated.generatedmod.items.SpiritVeinCompassItem?mc.player.getMainHandItem():mc.player.getOffhandItem(),x+7,y+15);
        g.drawString(mc.font,Component.translatable("hud.wuxingxiantu.vein.aura",state.concentration()),x+28,y+7,color,true);
        g.drawString(mc.font,Component.translatable("hud.wuxingxiantu.vein.quality."+CompassPresentation.quality(state.concentration())),x+28,y+20,CultivationUi.MUTED,true);
        Component line=Component.translatable("hud.wuxingxiantu.vein.none",384);
        if(state.hasVein()){double dx=state.center().getX()+.5-mc.player.getX(),dz=state.center().getZ()+.5-mc.player.getZ();line=Component.translatable("hud.wuxingxiantu.vein.target",Component.translatable("hud.wuxingxiantu.vein.direction."+CompassPresentation.direction(dx,dz)),state.distance(),state.grade());}
        CultivationUi.fit(g,line,x+28,y+34,width-36,CultivationUi.TEXT,-1,-1);
    }
    private VeinCompassHud(){}
}
