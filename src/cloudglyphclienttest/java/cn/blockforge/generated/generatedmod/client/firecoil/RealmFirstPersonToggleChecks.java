package cn.blockforge.generated.generatedmod.client.firecoil;
import cn.blockforge.generated.generatedmod.*;
import cn.blockforge.generated.generatedmod.client.realmflight.RealmFlightEffects;
import cn.blockforge.generated.generatedmod.realmflight.*;
import net.minecraft.client.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import java.nio.file.*;
import java.util.*;
final class RealmFirstPersonToggleChecks {
    private static java.lang.reflect.Field field(Class<?> c,String name)throws Exception{var f=c.getDeclaredField(name);f.setAccessible(true);return f;}
    @SuppressWarnings("unchecked") static void run(Path out,RenderLevelStageEvent source)throws Exception{
        var mc=Minecraft.getInstance();boolean before=HudConfig.REALM_FLIGHT_FIRST_PERSON_EFFECTS.get(),all=HudConfig.REALM_FLIGHT_EFFECTS.get();var camera=mc.options.getCameraType();
        var clear=RealmFlightEffects.class.getDeclaredMethod("discardPending");clear.setAccessible(true);
        try{
            HudConfig.REALM_FLIGHT_EFFECTS.set(true);
            RealmFlightEffects.accept(new RealmFlightVisualPacket(mc.player.getUUID(),true,8,3,false,true));
            var flights=(Map<UUID,?>)field(RealmFlightEffects.class,"FLIGHTS").get(null);Object flight=flights.get(mc.player.getUUID());
            if(flight==null)throw new AssertionError("Flight fixture not registered");
            var path=(RealmMeteorPath)field(flight.getClass(),"path").get(flight);var at=mc.player.position();
            for(int i=0;i<=60;i++)path.sample(new RealmMeteorPath.Vec(at.x,at.y+.9,at.z-30+i*.5),i,true);
            field(RealmFlightEffects.class,"clock").setLong(null,61);field(flight.getClass(),"glow").setDouble(flight,1);field(flight.getClass(),"previousGlow").setDouble(flight,1);
            field(flight.getClass(),"trailLength").setDouble(flight,18);field(flight.getClass(),"previousTrailLength").setDouble(flight,18);
            var pose=new PoseStack();var event=new RenderLevelStageEvent(source.getStage(),source.getLevelRenderer(),pose,source.getProjectionMatrix(),0,0,source.getCamera(),source.getFrustum());
            var results=new ArrayList<Map<String,Object>>();
            for(var mode:List.of(CameraType.FIRST_PERSON,CameraType.THIRD_PERSON_BACK))for(boolean enabled:new boolean[]{true,false,true}){
                mc.options.setCameraType(mode);HudConfig.REALM_FLIGHT_FIRST_PERSON_EFFECTS.set(enabled);clear.invoke(null);RealmFlightEffects.render(event);
                int vertices=RealmFlightEffects.renderedVertices;
                if(mode==CameraType.FIRST_PERSON&&!enabled&&vertices!=0)throw new AssertionError("First-person off still emits trail vertices: "+vertices);
                if((mode!=CameraType.FIRST_PERSON||enabled)&&vertices==0)throw new AssertionError("Toggle test lacks a visible positive-control trail: "+mode+"/"+enabled);
                results.add(Map.of("camera",mode.toString(),"enabled",enabled,"vertices",vertices));
            }
            Files.writeString(out.resolve("realm-first-person-toggle.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(Map.of("passed",true,"cases",results)));
        }finally{clear.invoke(null);((Map<?,?>)field(RealmFlightEffects.class,"FLIGHTS").get(null)).clear();HudConfig.REALM_FLIGHT_FIRST_PERSON_EFFECTS.set(before);HudConfig.REALM_FLIGHT_EFFECTS.set(all);mc.options.setCameraType(camera);}
    }
}
