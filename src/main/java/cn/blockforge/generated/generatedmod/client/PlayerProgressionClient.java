package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.*;
import cn.blockforge.generated.generatedmod.network.packet.ProgressionActionPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT,bus=Mod.EventBusSubscriber.Bus.MOD)
public final class PlayerProgressionClient {
    public static CompoundTag state=new CompoundTag();
    public static int rank(String id){var n=state.getCompound(id);return n.getBoolean("Learned")?Math.max(0,Math.min(8,n.getInt("Rank")))+1:0;}
    private static final KeyMapping SCAN=key("神识探查"),IDENTIFY=key("凝神辨识"),LOCK=key("神识锁定 / 解除");
    private static KeyMapping key(String name){return new KeyMapping(name,InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_UNKNOWN,"key.categories.wuxingxiantu");}
    public static void request(String action){if(Minecraft.getInstance().getConnection()!=null)Network.CHANNEL.sendToServer(new ProgressionActionPacket(action));}
    public static void accept(CompoundTag n){state=n.copy();SenseEffects.accept(state);}
    @SubscribeEvent public static void setup(FMLClientSetupEvent e){e.enqueueWork(()->ClientPacketSink.playerProgression=PlayerProgressionClient::accept);}
    @SubscribeEvent public static void keys(RegisterKeyMappingsEvent e){e.register(SCAN);e.register(IDENTIFY);e.register(LOCK);}
    @Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT)
    public static final class Events {
        @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut e){state=new CompoundTag();SenseEffects.clear();}
        @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e){
            if(e.phase!=TickEvent.Phase.END)return;var mc=Minecraft.getInstance();
            while(SCAN.consumeClick())if(mc.player!=null&&mc.screen==null)request("scan");
            while(IDENTIFY.consumeClick())if(mc.player!=null&&mc.screen==null)request("identify");
            while(LOCK.consumeClick())if(mc.player!=null&&mc.screen==null)request("lock");
        }
        @SubscribeEvent public static void hud(RenderGuiOverlayEvent.Post e){
            if(!e.getOverlay().id().getPath().equals("hotbar"))return;
            var mc=Minecraft.getInstance();if(mc.player==null||mc.screen!=null||mc.options.hideGui)return;
            String lock=state.getString("Lock");boolean channel=state.getInt("Channel")>0;
            int[] scanned=state.getIntArray("ScanTargets");
            if(lock.isEmpty()&&!channel&&scanned.length==0)return;
            String text=channel?"凝神 · "+(int)Math.ceil(state.getInt("Remaining")/20.0)+"秒":"神识锁定 · "+lock;
            int x=mc.getWindow().getGuiScaledWidth()/2,y=mc.getWindow().getGuiScaledHeight()/2+24;
            if(channel||!lock.isEmpty())e.getGuiGraphics().drawCenteredString(mc.font,text,x,y,CultivationUi.CYAN);
            for(int id:scanned)direction(e,mc,id,mc.getWindow().getGuiScaledHeight()/2-55,false);
            if(!lock.isEmpty())direction(e,mc,state.getInt("LockEntity"),y+15,true);
        }
        private static void direction(RenderGuiOverlayEvent.Post e,Minecraft mc,int id,int y,boolean lock){
            var target=mc.level.getEntity(id);if(!(target instanceof net.minecraft.world.entity.LivingEntity living)||!living.isAlive()||!mc.player.hasLineOfSight(target))return;
            double dx=target.getX()-mc.player.getX(),dz=target.getZ()-mc.player.getZ();
            double yaw=Math.toDegrees(Math.atan2(-dx,dz));float angle=net.minecraft.util.Mth.wrapDegrees((float)yaw-mc.player.getYRot());
            int center=mc.getWindow().getGuiScaledWidth()/2;
            String text=lock?(Math.abs(angle)<12?"前方":angle<0?"左侧":"右侧")+" · "+Math.round(mc.player.distanceTo(target))+"格":"◇";
            e.getGuiGraphics().drawCenteredString(mc.font,text,lock?center:center+(int)(angle/180*90),y,lock?CultivationUi.GOLD:CultivationUi.CYAN);
        }
    }
}
