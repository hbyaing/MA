package cn.blockforge.generated.generatedmod.client.furnacevolume;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL20;

/** Isolated client QA only. Never packaged into the production mod. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT)
public final class FurnaceMotionFixture implements FurnaceFireVolume.DrawProbe {
    private static final boolean ENABLED=Boolean.getBoolean("wuxingxiantu.guardGlintCapture")
            &&Boolean.getBoolean("wuxingxiantu.furnaceOnlyCapture")
            &&Boolean.getBoolean("wuxingxiantu.furnaceMotionCapture");
    private static final BlockPos CORE=new BlockPos(0,121,0);
    private static final Vec3 TARGET=new Vec3(.5,121.7,.5);
    private static final Vec3 ANCHOR=new Vec3(.5,121,.5);
    private static final String[] SCENES={"front-left","front-center","front-right","front-return",
            "north","east","south","west","near-front","far-front","high-front","low-front",
            "diagonal","near-side","bob-before","bob-walking","bob-stopped"};
    private static final int BEGIN=60,LENGTH=22,EXPECTED_CAPTURES=SCENES.length+1;
    private static final FurnaceMotionFixture INSTANCE=new FurnaceMotionFixture();
    private static final Vec3[] REFERENCES={Vec3.ZERO,new Vec3(-2,.3,0),new Vec3(2,.3,0),
            new Vec3(0,.3,-2),new Vec3(0,.3,2),new Vec3(0,1.1,0)};
    private static final List<Map<String,Object>> DIAGNOSTICS=new ArrayList<>();
    private static Matrix4f eventBodyProjection,eventProjection,eventPose,probeInverse;
    private static Vec3 eventEye;
    private static String fire;
    private static float actualTime,actualPhase,renderPartial;
    private static int width,height,drawSequence,stage=-1,lastPoseTick=-1;
    private static long renderFrame,eventFrame=-1,probeFrame=-1,alignedFrame=-1;
    private static int alignedDraw=-1,alignmentFrames,walkingBobFrames;
    private static float beforeWalk=-1,walkDelta;
    private static double eventYaw;
    private static double maxPixels,maxRayDistance,anchorError;
    private static double maxWalkingEyeOffset,maxWalkingOldRayDistance,maxWalkingNewRayDistance;
    private static boolean walkingBugExercised;
    private static Alignment lastAlignment;
    private static Vec3 previousCameraFeet;
    private static Entity desiredCamera;
    private static float desiredYaw,desiredPitch,eventCameraYaw,eventCameraPitch;
    private static boolean eventCameraExpected;

    private record Alignment(double pixels,double rayDistance,double oldRayDistance,
            double eyeOffset,Vec3 virtualEye,Vec3 physicalEye,List<Map<String,Object>> points){}

    public record Step(CompletableFuture<?> preparation,String capture,boolean complete){}

    /** Visible fixed geometry makes sideways drift distinguishable from animation. */
    public static void prepareWorld(ServerLevel level,BlockPos core){
        level.getServer().getWorldData().overworldData().setGameTime(240);
        for(int x=-12;x<=12;x++)for(int z=-12;z<=12;z++){
            level.setBlock(new BlockPos(x,120,z),((x+z&1)==0?Blocks.SMOOTH_STONE:Blocks.POLISHED_DEEPSLATE).defaultBlockState(),2);
            if(Math.abs(x)>2||Math.abs(z)>2)for(int y=121;y<=129;y++)level.setBlock(new BlockPos(x,y,z),Blocks.AIR.defaultBlockState(),2);
        }
        var markers=new net.minecraft.world.level.block.Block[]{Blocks.RED_CONCRETE,Blocks.YELLOW_CONCRETE,Blocks.LIME_CONCRETE,Blocks.BLUE_CONCRETE};
        int[][] positions={{-4,-4},{4,-4},{4,4},{-4,4}};
        for(int i=0;i<4;i++){
            for(int y=121;y<=122;y++)level.setBlock(new BlockPos(positions[i][0],y,positions[i][1]),markers[i].defaultBlockState(),2);
            level.setBlock(new BlockPos(positions[i][0],120,positions[i][1]),Blocks.SEA_LANTERN.defaultBlockState(),2);
        }
    }

    @SubscribeEvent public static void freezeAndInstall(TickEvent.ClientTickEvent event){
        if(!ENABLED||event.phase!=TickEvent.Phase.START)return;
        if(!System.getProperty("wuxingxiantu.perfBaselineShader","").isEmpty())
            throw new AssertionError("Motion fixture must run separately from paired performance probe");
        var mc=Minecraft.getInstance();
        if(mc.level!=null)mc.level.setGameTime(240);
        FurnaceFireVolume.drawProbe=INSTANCE;
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void beginRenderFrame(TickEvent.RenderTickEvent event){
        if(!ENABLED||event.phase!=TickEvent.Phase.START)return;
        renderFrame++;
        var mc=Minecraft.getInstance();
        if(desiredCamera!=null&&desiredCamera.level()==mc.level){
            mc.setCameraEntity(desiredCamera);mc.options.setCameraType(CameraType.FIRST_PERSON);
            rotateCameraEntity(desiredCamera,desiredYaw,desiredPitch);
        }
    }

    /** The host's possession cleanup normally restores a detached camera to the
     * player. Keep this disposable QA camera through every rendered frame; the
     * walking section still uses the actual player and vanilla bob/input. */
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void retainFixtureCamera(ViewportEvent.ComputeCameraAngles event){
        if(!ENABLED||desiredCamera==null)return;
        var mc=Minecraft.getInstance();
        if(event.getCamera()!=mc.gameRenderer.getMainCamera()||desiredCamera.level()!=mc.level)return;
        mc.setCameraEntity(desiredCamera);
        event.setYaw(desiredYaw);event.setPitch(desiredPitch);
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void observeWorldMatrices(RenderLevelStageEvent event){
        if(!ENABLED||event.getStage()!=RenderLevelStageEvent.Stage.AFTER_PARTICLES)return;
        var mc=Minecraft.getInstance();if(mc.level==null)return;
        var furnace=HostAdapter.read(mc.level.getBlockEntity(CORE),event.getPartialTick());
        if(furnace==null)return;
        eventEye=event.getCamera().getPosition();renderPartial=event.getPartialTick();eventYaw=furnace.yaw();
        eventCameraYaw=event.getCamera().getYRot();eventCameraPitch=event.getCamera().getXRot();
        eventCameraExpected=event.getCamera().getEntity()==desiredCamera;
        eventProjection=new Matrix4f(event.getProjectionMatrix());eventPose=new Matrix4f(event.getPoseStack().last().pose());
        Vec3 relative=furnace.anchor().subtract(eventEye);
        eventBodyProjection=new Matrix4f(event.getProjectionMatrix()).mul(event.getPoseStack().last().pose())
                .translate((float)relative.x,(float)relative.y,(float)relative.z).rotateY((float)furnace.yaw());
        anchorError=Math.max(anchorError,furnace.anchor().distanceTo(ANCHOR));
        eventFrame=renderFrame;
        observeSameFrameAlignment();
    }

    @Override public double clockSeconds(double gameSeconds){return 12;}
    @Override public void draw(int program,int vao,int w,int h,String id){
        // Freeze only the QA clock and brewing phase. Geometry, precision and fire recipe are unchanged.
        int time=GL20.glGetUniformLocation(program,"Time"),phase=GL20.glGetUniformLocation(program,"Phase");
        if(time>=0)GL20.glUniform1f(time,12);
        if(phase>=0)GL20.glUniform1f(phase,.5f);
        int inverse=GL20.glGetUniformLocation(program,"InverseLocalProjection");
        if(inverse<0)throw new AssertionError("Furnace shader did not expose InverseLocalProjection");
        float[] matrix=new float[16];GL20.glGetUniformfv(program,inverse,matrix);probeInverse=new Matrix4f().set(matrix);
        actualTime=time<0?12:GL20.glGetUniformf(program,time);actualPhase=phase<0?.5f:GL20.glGetUniformf(program,phase);
        width=w;height=h;fire=id;drawSequence++;
        probeFrame=renderFrame;
        observeSameFrameAlignment();
    }

    public static Step tick(Minecraft mc,int tick,int cameraId){
        if(tick<BEGIN){orient(mc,tick,mc.level.getEntity(cameraId));return new Step(null,null,false);}
        int index=(tick-BEGIN)/LENGTH,offset=(tick-BEGIN)%LENGTH;
        if(index>=SCENES.length){mc.options.keyRight.setDown(false);return new Step(null,null,true);}
        CompletableFuture<?> preparation=null;
        if(index!=stage){
            stage=index;previousCameraFeet=null;
            if(index==14){
                var p=mc.player;p.setNoGravity(false);p.noPhysics=false;p.setPos(.5,121,-6.5);p.xo=p.xOld=.5;p.yo=p.yOld=121;p.zo=p.zOld=-6.5;
                beforeWalk=p.walkDist;
                var id=p.getUUID();
                preparation=mc.getSingleplayerServer().submitAsync(()->{
                    var serverPlayer=mc.getSingleplayerServer().getPlayerList().getPlayer(id);
                    serverPlayer.setNoGravity(false);serverPlayer.noPhysics=false;
                    serverPlayer.teleportTo(.5,121,-6.5);
                });
            }
        }
        orient(mc,tick,mc.level.getEntity(cameraId));
        String capture=offset==16?"furnace-motion-"+String.format("%02d",index+1)+"-"+SCENES[index]+".png":null;
        if(index==1&&offset==8)capture="furnace-motion-02-front-slide-mid.png";
        return new Step(preparation,capture,false);
    }

    public static void orient(Minecraft mc,int tick,Entity camera){
        if(camera==null)return;
        int index=tick<BEGIN?0:Math.min(SCENES.length-1,(tick-BEGIN)/LENGTH),offset=Math.max(0,(tick-BEGIN)%LENGTH);
        boolean bob=index>=14;
        mc.options.bobView().set(bob);
        mc.options.keyRight.setDown(index==15);
        if(bob){
            desiredCamera=mc.player;desiredYaw=0;desiredPitch=9;
            mc.setCameraEntity(mc.player);mc.options.setCameraType(CameraType.FIRST_PERSON);
            rotateCameraEntity(mc.player,desiredYaw,desiredPitch);
            if(beforeWalk>=0)walkDelta=Math.max(walkDelta,mc.player.walkDist-beforeWalk);
            return;
        }
        if(lastPoseTick==tick)return;lastPoseTick=tick;
        mc.setCameraEntity(camera);mc.options.setCameraType(CameraType.FIRST_PERSON);
        Vec3 eye;boolean fixedAngle=false;
        switch(index){
            case 0->{eye=new Vec3(-3.5,123.25,-8.5);fixedAngle=true;}
            case 1->{eye=new Vec3(-3.5+4*Math.min(1,offset/14.),123.25,-8.5);fixedAngle=true;}
            case 2->{eye=new Vec3(4.5,123.25,-8.5);fixedAngle=true;}
            case 3->{eye=new Vec3(.5,123.25,-8.5);fixedAngle=true;}
            case 4->eye=new Vec3(.5,123.25,-8.5);
            case 5->eye=new Vec3(9.5,123.25,.5);
            case 6->eye=new Vec3(.5,123.25,9.5);
            case 7->eye=new Vec3(-8.5,123.25,.5);
            case 8->eye=new Vec3(.5,122.3,-3.7);
            case 9->eye=new Vec3(.5,124.4,-14.5);
            case 10->eye=new Vec3(.5,127.2,-8.5);
            case 11->eye=new Vec3(.5,121.35,-8.5);
            case 12->eye=new Vec3(7.5,124.4,-6.5);
            default->eye=new Vec3(-3.7,122.2,.5);
        }
        Vec3 feet=eye.subtract(0,camera.getEyeHeight(),0),old=previousCameraFeet==null?feet:previousCameraFeet;
        camera.setPos(feet);camera.xo=camera.xOld=old.x;camera.yo=camera.yOld=old.y;camera.zo=camera.zOld=old.z;
        previousCameraFeet=feet;
        Vec3 direction=TARGET.subtract(eye);
        float yaw=fixedAngle?0:(float)Math.toDegrees(Math.atan2(-direction.x,direction.z));
        float pitch=fixedAngle?9.8f:(float)-Math.toDegrees(Math.atan2(direction.y,Math.hypot(direction.x,direction.z)));
        desiredCamera=camera;desiredYaw=yaw;desiredPitch=pitch;
        rotateCameraEntity(camera,yaw,pitch);
    }

    private static void rotateCameraEntity(Entity camera,float yaw,float pitch){
        camera.setYRot(yaw);camera.yRotO=yaw;camera.setXRot(pitch);camera.xRotO=pitch;
        // Camera.setup reads LivingEntity.getViewYRot, which interpolates head
        // rotation rather than Entity.yRot. ArmorStand is a LivingEntity too.
        if(camera instanceof LivingEntity living){
            living.yHeadRot=living.yHeadRotO=yaw;
            living.yBodyRot=living.yBodyRotO=yaw;
        }
    }

    /** Native draws precede the LOWEST world observer, while shader-pack post draws
     * follow it. Match both observations by render frame rather than callback order. */
    private static void observeSameFrameAlignment(){
        if(eventFrame!=renderFrame||probeFrame!=renderFrame||eventBodyProjection==null
                ||probeInverse==null||eventEye==null||alignedFrame==renderFrame&&alignedDraw==drawSequence)return;
        lastAlignment=alignment();alignedFrame=renderFrame;alignedDraw=drawSequence;alignmentFrames++;
        maxPixels=Math.max(maxPixels,lastAlignment.pixels);
        maxRayDistance=Math.max(maxRayDistance,lastAlignment.rayDistance);
        var mc=Minecraft.getInstance();
        // The movement key is consumed by ordinary LocalPlayer input. Require
        // observed walking plus an actual projection-induced eye shift, not just
        // a checked bob option or an inverse-matrix round trip.
        if(stage==15&&mc.options.bobView().get()&&mc.getCameraEntity()==mc.player&&walkDelta>.1f){
            walkingBobFrames++;
            maxWalkingEyeOffset=Math.max(maxWalkingEyeOffset,lastAlignment.eyeOffset);
            maxWalkingOldRayDistance=Math.max(maxWalkingOldRayDistance,lastAlignment.oldRayDistance);
            maxWalkingNewRayDistance=Math.max(maxWalkingNewRayDistance,lastAlignment.rayDistance);
            walkingBugExercised|=lastAlignment.eyeOffset>.005&&lastAlignment.oldRayDistance>.005
                    &&lastAlignment.rayDistance<.0005;
        }
    }

    private static Alignment alignment(){
        var projectionFromShader=new Matrix4f(probeInverse).invert();
        var inversePhysicalVp=new Matrix4f(eventProjection).mul(eventPose).invert();
        var homogeneousEye=new Vector4f(0,0,1,0);probeInverse.transform(homogeneousEye);homogeneousEye.div(homogeneousEye.w);
        var virtualEye=new Vec3(homogeneousEye.x,homogeneousEye.y,homogeneousEye.z);
        Vec3 physicalDelta=eventEye.subtract(ANCHOR);double c=Math.cos(eventYaw),s=Math.sin(eventYaw);
        var physicalEye=new Vec3(c*physicalDelta.x-s*physicalDelta.z,physicalDelta.y,s*physicalDelta.x+c*physicalDelta.z);
        var points=new ArrayList<Map<String,Object>>();double pixels=0,rayDistance=0,oldRayDistance=0;
        for(Vec3 local:REFERENCES){
            var expected=new Vector4f((float)local.x,(float)local.y,(float)local.z,1);eventBodyProjection.transform(expected);
            var actual=new Vector4f((float)local.x,(float)local.y,(float)local.z,1);projectionFromShader.transform(actual);
            if(expected.w<=0||actual.w<=0)continue;
            float nx=expected.x/expected.w,ny=expected.y/expected.w;
            // Validate rays which are actually present in this viewport. Points
            // behind or far outside a deliberately close view cannot witness
            // furnace alignment and can amplify irrelevant round-trip error.
            if(Math.abs(nx)>1||Math.abs(ny)>1)continue;
            double dx=(actual.x/actual.w-nx)*width*.5,dy=(actual.y/actual.w-ny)*height*.5;
            pixels=Math.max(pixels,Math.hypot(dx,dy));
            var far=new Vector4f(nx,ny,1,1);probeInverse.transform(far);far.div(far.w);
            // Reproduce the production shader's homogeneous eye / far algorithm.
            var ray=new Vector3f(far.x-homogeneousEye.x,far.y-homogeneousEye.y,far.z-homogeneousEye.z).normalize();
            var toPoint=new Vector3f((float)local.x-homogeneousEye.x,(float)local.y-homogeneousEye.y,(float)local.z-homogeneousEye.z);
            double newMiss=new Vector3f(toPoint).cross(ray).length();rayDistance=Math.max(rayDistance,newMiss);
            // Independently exercise the previous implementation with the actual
            // bobbed event VP and its physical (unbobbed) camera origin.
            var oldFar=new Vector4f(nx,ny,1,1);inversePhysicalVp.transform(oldFar);oldFar.div(oldFar.w);
            var oldWorldDirection=new Vector3f(oldFar.x,oldFar.y,oldFar.z).normalize();
            var oldRay=new Vector3f((float)c*oldWorldDirection.x-(float)s*oldWorldDirection.z,
                    oldWorldDirection.y,(float)s*oldWorldDirection.x+(float)c*oldWorldDirection.z);
            var oldToPoint=new Vector3f((float)(local.x-physicalEye.x),(float)(local.y-physicalEye.y),(float)(local.z-physicalEye.z));
            double oldMiss=oldToPoint.cross(oldRay).length();oldRayDistance=Math.max(oldRayDistance,oldMiss);
            var point=new LinkedHashMap<String,Object>();point.put("local",List.of(local.x,local.y,local.z));
            point.put("world",List.of(ANCHOR.x+c*local.x+s*local.z,ANCHOR.y+local.y,ANCHOR.z-s*local.x+c*local.z));
            point.put("expectedPixel",List.of((nx+1)*width*.5,(ny+1)*height*.5));
            point.put("shaderPixel",List.of((actual.x/actual.w+1)*width*.5,(actual.y/actual.w+1)*height*.5));
            point.put("newRayMissBlocks",newMiss);point.put("oldRayMissBlocks",oldMiss);points.add(point);
        }
        return new Alignment(pixels,rayDistance,oldRayDistance,virtualEye.distanceTo(physicalEye),virtualEye,physicalEye,points);
    }

    public static Map<String,Object> capture(Minecraft mc,String name){
        observeSameFrameAlignment();
        if(lastAlignment==null||alignedFrame!=renderFrame||eventFrame!=renderFrame||probeFrame!=renderFrame||drawSequence==0)
            throw new AssertionError("No same-frame furnace draw/event matrices: render="+renderFrame+", event="+eventFrame+", probe="+probeFrame);
        if(!DIAGNOSTICS.isEmpty()&&!fire.equals(DIAGNOSTICS.get(0).get("fire")))throw new AssertionError("Fixed fire changed: "+fire);
        float yawError=Math.abs(net.minecraft.util.Mth.wrapDegrees(eventCameraYaw-desiredYaw));
        float pitchError=Math.abs(eventCameraPitch-desiredPitch);
        if(!eventCameraExpected||yawError>.1f||pitchError>.1f)
            throw new AssertionError("Fixture camera did not face the requested view: expected="+eventCameraExpected
                    +", yaw="+eventCameraYaw+" / "+desiredYaw+", pitch="+eventCameraPitch+" / "+desiredPitch);
        if(lastAlignment.points.isEmpty()||lastAlignment.pixels>.06||lastAlignment.rayDistance>.0005||anchorError>1e-8)
            throw new AssertionError("World-space field/model misalignment: pixel="+lastAlignment.pixels+", rayDistance="+lastAlignment.rayDistance+", anchor="+anchorError);
        if(Math.abs(actualTime-12)>.00001||Math.abs(actualPhase-.5)>.00001)
            throw new AssertionError("QA field clock/phase was not frozen");
        var row=new LinkedHashMap<String,Object>();row.put("name",name);row.put("scene",SCENES[Math.max(0,stage)]);
        row.put("fire",fire);row.put("fixedTimeSeconds",actualTime);row.put("fixedPhase",actualPhase);row.put("worldAnchor",List.of(ANCHOR.x,ANCHOR.y,ANCHOR.z));
        row.put("cameraEye",List.of(eventEye.x,eventEye.y,eventEye.z));row.put("partialTick",renderPartial);row.put("bobEnabled",mc.options.bobView().get());
        row.put("walkDistance",mc.player.walkDist);row.put("walkDistanceDelta",walkDelta);row.put("drawSequence",drawSequence);
        row.put("coverage",VolumeGpu.coverage);row.put("drawCpuMs",VolumeGpu.cpuMs);row.put("viewport",List.of(width,height));
        row.put("eventBodyProjection",matrix(eventBodyProjection));row.put("shaderInverseLocalProjection",matrix(probeInverse));
        row.put("eventProjection",matrix(eventProjection));row.put("eventPose",matrix(eventPose));row.put("furnaceYawRadians",eventYaw);
        row.put("renderFrame",renderFrame);row.put("eventFrame",eventFrame);row.put("probeFrame",probeFrame);
        row.put("cameraIsPlayer",mc.getCameraEntity()==mc.player);
        row.put("cameraEntityExpected",eventCameraExpected);row.put("cameraYawDegrees",eventCameraYaw);row.put("cameraPitchDegrees",eventCameraPitch);
        row.put("requestedYawDegrees",desiredYaw);row.put("requestedPitchDegrees",desiredPitch);
        row.put("virtualEyeLocal",List.of(lastAlignment.virtualEye.x,lastAlignment.virtualEye.y,lastAlignment.virtualEye.z));
        row.put("physicalEyeLocal",List.of(lastAlignment.physicalEye.x,lastAlignment.physicalEye.y,lastAlignment.physicalEye.z));
        row.put("virtualEyeOffsetBlocks",lastAlignment.eyeOffset);row.put("oldPhysicalOriginRayMissBlocks",lastAlignment.oldRayDistance);
        row.put("maxProjectionErrorPixels",lastAlignment.pixels);row.put("maxRayDistanceBlocks",lastAlignment.rayDistance);row.put("referencePoints",lastAlignment.points);
        DIAGNOSTICS.add(row);return row;
    }

    private static List<Float> matrix(Matrix4f value){float[] values=new float[16];value.get(values);var list=new ArrayList<Float>(16);for(float v:values)list.add(v);return list;}
    public static Map<String,Object> summary(){var row=new LinkedHashMap<String,Object>();row.put("fixedWorldAnchor",List.of(ANCHOR.x,ANCHOR.y,ANCHOR.z));
        row.put("expectedCaptures",EXPECTED_CAPTURES);row.put("observedCaptures",DIAGNOSTICS.size());row.put("maxProjectionErrorPixels",maxPixels);
        row.put("maxRayDistanceBlocks",maxRayDistance);row.put("anchorErrorBlocks",anchorError);row.put("walkingDistanceBlocks",walkDelta);
        row.put("sameFrameAlignmentObservations",alignmentFrames);row.put("walkingBobFrames",walkingBobFrames);
        row.put("maxWalkingVirtualEyeOffsetBlocks",maxWalkingEyeOffset);row.put("maxWalkingOldRayMissBlocks",maxWalkingOldRayDistance);
        row.put("maxWalkingNewRayMissBlocks",maxWalkingNewRayDistance);row.put("walkingBugExercised",walkingBugExercised);
        row.put("rayVerification","Actual shader homogeneous eye/far algorithm; old physical-origin algorithm independently checked on real walking event matrices");
        row.put("frozenTimeSeconds",12);row.put("frozenPhase",.5);row.put("referenceGeometry","5x5 furnace, checkerboard floor, four distinct colored reference pillars");return row;}
    public static void validate(int captureCount){
        if(captureCount!=EXPECTED_CAPTURES||DIAGNOSTICS.size()!=EXPECTED_CAPTURES||walkDelta<.1||alignmentFrames<EXPECTED_CAPTURES)
            throw new AssertionError("Furnace motion fixture incomplete: captures="+captureCount+", diagnostics="+DIAGNOSTICS.size()+", walking="+walkDelta);
        if(!walkingBugExercised||walkingBobFrames==0||maxWalkingEyeOffset<=.005||maxWalkingOldRayDistance<=.005
                ||maxWalkingNewRayDistance>=.0005||!Double.isFinite(maxRayDistance)||maxRayDistance>=.0005
                ||!Double.isFinite(maxPixels)||maxPixels>.06||anchorError>1e-8)
            throw new AssertionError("Actual walking bob did not validate the fix: frames="+walkingBobFrames
                    +", eyeOffset="+maxWalkingEyeOffset+", oldMiss="+maxWalkingOldRayDistance
                    +", newMiss="+maxWalkingNewRayDistance+", allFrameNewMiss="+maxRayDistance+", pixels="+maxPixels);
    }
    private FurnaceMotionFixture(){}
}
