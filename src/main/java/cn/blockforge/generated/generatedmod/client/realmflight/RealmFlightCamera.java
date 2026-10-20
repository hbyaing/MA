package cn.blockforge.generated.generatedmod.client.realmflight;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.ModEffects;
import cn.blockforge.generated.generatedmod.client.HeartSwordClientEffects;
import cn.blockforge.generated.generatedmod.realmflight.FlightVisualDynamics;
import cn.blockforge.generated.generatedmod.realmflight.RealmFlightBalance;
import cn.blockforge.generated.generatedmod.realmflight.RealmFlightCameraDynamics;
import cn.blockforge.generated.generatedmod.realmflight.RealmFlightControl;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Third-person realm acceleration zoom; Camera retains ownership of its collision and direction. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT)
public final class RealmFlightCamera {
    private static final RealmFlightCameraDynamics DYNAMICS=new RealmFlightCameraDynamics();
    private static ClientLevel level;
    private static LocalPlayer player;
    private static Entity cameraEntity;
    private static boolean active,boosting;
    private static double horizontalSpeed,cruiseSpeed,boostedSpeed;
    private static double lastRequestedDistance=RealmFlightCameraDynamics.NORMAL_DISTANCE;

    public record Diagnostics(boolean active,boolean boosting,double horizontalSpeed,double cruiseSpeed,
            double boostedSpeed,double previousDistance,double currentDistance,double targetDistance,
            double interpolatedDistance,double lastRequestedDistance){}

    private static boolean eligible(LocalPlayer p){
        return p!=null&&RealmFlightControl.eligible(p)&&p.getAbilities().flying
                &&p.hasEffect(ModEffects.REALM_FLIGHT.get())&&!p.isSleeping()
                &&HeartSwordClientEffects.action(p)<2;
    }
    private static void reset(){
        DYNAMICS.reset();active=boosting=false;
        horizontalSpeed=cruiseSpeed=boostedSpeed=0;
        lastRequestedDistance=RealmFlightCameraDynamics.NORMAL_DISTANCE;
    }
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void tick(TickEvent.ClientTickEvent event){
        if(event.phase!=TickEvent.Phase.END)return;
        var mc=Minecraft.getInstance();var currentCameraEntity=mc.getCameraEntity();
        if(level!=mc.level||player!=mc.player||cameraEntity!=currentCameraEntity){
            reset();level=mc.level;player=mc.player;cameraEntity=currentCameraEntity;
        }
        if(level==null||player==null||cameraEntity!=player||mc.options.getCameraType().isFirstPerson()
                ||!eligible(player)){reset();return;}
        if(mc.isPaused())return;
        var data=GeneratedMod.data(player);
        if(data==null){reset();return;}
        active=true;boosting=RealmFlightControl.boosting(player);
        var velocity=player.getDeltaMovement();
        // Use the smaller observed displacement/remaining velocity: collision stalls and
        // teleport corrections cannot masquerade as sustained boosted flight.
        horizontalSpeed=Math.min(Math.hypot(velocity.x,velocity.z),
                Math.hypot(player.getX()-player.xOld,player.getZ()-player.zOld));
        int realm=data.wuxingxiantu$getRealm(),stage=data.wuxingxiantu$getCultivationStage();
        cruiseSpeed=RealmFlightBalance.speed(realm,stage,false)/20*FlightVisualDynamics.VELOCITY_RETENTION;
        boostedSpeed=RealmFlightBalance.speed(realm,stage,true)/20*FlightVisualDynamics.VELOCITY_RETENTION;
        DYNAMICS.tick(true,boosting,horizontalSpeed,cruiseSpeed,boostedSpeed);
    }

    /** Called immediately before vanilla Camera.getMaxZoom, while the camera is still at the eye. */
    public static double requestedDistance(double original,Camera camera,float partial){
        var mc=Minecraft.getInstance();
        if(!active||level!=mc.level||player!=mc.player||cameraEntity!=player||camera.getEntity()!=player
                ||camera!=mc.gameRenderer.getMainCamera()||mc.options.getCameraType().isFirstPerson()
                ||!eligible(player)){
            lastRequestedDistance=original;return original;
        }
        double extra=DYNAMICS.distance(partial)-RealmFlightCameraDynamics.NORMAL_DISTANCE;
        lastRequestedDistance=original+extra;
        return lastRequestedDistance;
    }
    public static Diagnostics diagnostics(float partial){
        return new Diagnostics(active,boosting,horizontalSpeed,cruiseSpeed,boostedSpeed,
                DYNAMICS.previousDistance(),DYNAMICS.currentDistance(),DYNAMICS.targetDistance(),
                DYNAMICS.distance(partial),lastRequestedDistance);
    }
    private RealmFlightCamera(){}
}
