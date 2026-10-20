package cn.blockforge.generated.generatedmod.client;
import cn.blockforge.generated.generatedmod.*;
import cn.blockforge.generated.generatedmod.network.packet.CoreBuildInputPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import java.util.*;

@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT)
public final class CoreBuildClient {
    private static Object world;
    private static final Set<BlockPos> PREVIEWS=new HashSet<>();
    private static BlockPos target,active;
    private static boolean armed;
    private static int progress;
    static {ClientPacketSink.coreBlueprint=CoreBuildClient::activate;
        ClientPacketSink.coreBuildProgress=packet->{if(packet.pos().equals(active)){progress=Math.max(0,Math.min(CoreStructureBuilder.BUILD_TICKS,packet.progress()));if(packet.finished()){armed=false;active=null;}}};}
    public static void activate(BlockPos pos){PREVIEWS.add(pos.immutable());if(pos.equals(target))return;target=pos.immutable();armed=false;}
    public static boolean visible(BlockPos pos){return PREVIEWS.contains(pos);}
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e){
        if(e.phase!=TickEvent.Phase.END)return;var mc=Minecraft.getInstance();
        if(world!=mc.level){world=mc.level;PREVIEWS.clear();target=active=null;progress=0;armed=false;}
        if(mc.player==null||mc.level==null)return;
        PREVIEWS.removeIf(p->!mc.level.hasChunkAt(p)||!CoreStructureBuilder.core(mc.level.getBlockState(p))||CoreStructureBuilder.complete(mc.level,p));
        if(!mc.options.keyUse.isDown())armed=true;
        boolean holding=armed&&target!=null&&PREVIEWS.contains(target)&&mc.screen==null&&mc.isWindowActive()&&mc.options.keyUse.isDown()
                &&mc.hitResult instanceof BlockHitResult hit&&hit.getBlockPos().equals(target);
        if(!holding){if(active!=null)Network.CHANNEL.sendToServer(new CoreBuildInputPacket(active,false));active=null;progress=0;return;}
        active=target;if(progress%3==0)Network.CHANNEL.sendToServer(new CoreBuildInputPacket(active,true));
        progress=Math.min(CoreStructureBuilder.BUILD_TICKS,progress+1);
        if(progress==CoreStructureBuilder.BUILD_TICKS)Network.CHANNEL.sendToServer(new CoreBuildInputPacket(active,true));
    }
    @SubscribeEvent public static void hud(RenderGuiEvent.Post e){
        if(active==null||progress<=0)return;var mc=Minecraft.getInstance();if(mc.options.hideGui)return;
        var g=e.getGuiGraphics();int x=mc.getWindow().getGuiScaledWidth()/2-62,y=mc.getWindow().getGuiScaledHeight()/2+28;
        g.fill(x,y,x+124,y+12,0xDD171B24);g.renderOutline(x,y,124,12,0xFFD4B66D);
        g.fill(x+2,y+2,x+2+120*progress/CoreStructureBuilder.BUILD_TICKS,y+10,0xFF68C9C1);
        g.drawCenteredString(mc.font,"搭建结构 · "+progress*100/CoreStructureBuilder.BUILD_TICKS+"%",x+62,y-12,0xFFE8DCC0);
    }
    private CoreBuildClient(){}
}
