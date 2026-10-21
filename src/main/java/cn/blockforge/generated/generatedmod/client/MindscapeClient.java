package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.*;
import cn.blockforge.generated.generatedmod.mindscape.MindscapeState;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT)
public final class MindscapeClient {
    private static MindscapeState state=MindscapeState.NONE;
    private static int remaining;
    private static java.util.UUID owner;
    private static boolean cancelSent;
    public static void accept(cn.blockforge.generated.generatedmod.network.packet.mindscape.MindscapeStatePacket packet){
        var player=Minecraft.getInstance().player;if(player==null)return;
        var data=GeneratedMod.data(player);if(data!=null)data.wuxingxiantu$setDaoHeartUnstable(packet.unstable());
        owner=player.getUUID();state=packet.state()>=0&&packet.state()<MindscapeState.values().length?MindscapeState.values()[packet.state()]:MindscapeState.NONE;
        remaining=Math.max(0,Math.min(300,packet.remaining()));if(state!=MindscapeState.MEDITATING)cancelSent=false;
    }
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event){
        if(event.phase!=TickEvent.Phase.END)return;
        var client=Minecraft.getInstance();if(client.player==null||!client.player.getUUID().equals(owner)){state=MindscapeState.NONE;owner=null;cancelSent=false;return;}
        if(state==MindscapeState.MEDITATING&&!cancelSent&&(client.options.keyUp.isDown()||client.options.keyDown.isDown()
                ||client.options.keyLeft.isDown()||client.options.keyRight.isDown()||client.options.keyJump.isDown()||client.options.keyShift.isDown())){
            cancelSent=true;Network.CHANNEL.sendToServer(new cn.blockforge.generated.generatedmod.network.packet.mindscape.MindscapeCancelPacket());
        }
    }
    @SubscribeEvent public static void render(net.minecraftforge.client.event.RenderGuiEvent.Post event){
        var client=Minecraft.getInstance();if(client.player==null||state==MindscapeState.NONE||client.options.hideGui)return;
        Component label=state==MindscapeState.MEDITATING?Component.translatable("hud.wuxingxiantu.mindscape_meditation",(remaining+19)/20)
                :Component.translatable("mindscape.wuxingxiantu.state."+state.name().toLowerCase(java.util.Locale.ROOT));
        int x=Math.max(4,(client.getWindow().getGuiScaledWidth()-client.font.width(label))/2);
        event.getGuiGraphics().drawString(client.font,label,x,client.getWindow().getGuiScaledHeight()-92,(state==MindscapeState.UNSTABLE||state==MindscapeState.MEDITATING)?0xDC143C:0xE5CB87,true);
    }
    private MindscapeClient(){}
}
