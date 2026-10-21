package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.*;
import cn.blockforge.generated.generatedmod.realmflight.RealmFlightControl;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.common.Mod;

/** Apply the saved view once on takeoff; leave in-flight F5 changes alone. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT)
public final class FlightCameraPreferences {
    private static Object player;
    private static boolean realmActive;
    public static CameraType type(int value){return value==0?CameraType.FIRST_PERSON:value==2?CameraType.THIRD_PERSON_FRONT:CameraType.THIRD_PERSON_BACK;}
    public static String label(int value){return value==0?"第一人称":value==2?"第三人称前视":"第三人称后视";}
    @SubscribeEvent(priority=EventPriority.LOW) public static void tick(TickEvent.ClientTickEvent e){
        if(e.phase!=TickEvent.Phase.END)return;var mc=Minecraft.getInstance();
        if(player!=mc.player){player=mc.player;realmActive=false;}
        boolean active=mc.player!=null&&!mc.player.isPassenger()&&RealmFlightControl.eligible(mc.player)&&mc.player.getAbilities().flying;
        if(active&&!realmActive&&!mc.options.getCameraType().isFirstPerson())mc.options.setCameraType(type(HudConfig.REALM_FLIGHT_DEFAULT_CAMERA.get()));
        // Keep the flight view on landing; sword entry/exit owns its separate rule.
        realmActive=active;
    }
    private FlightCameraPreferences(){}
}
