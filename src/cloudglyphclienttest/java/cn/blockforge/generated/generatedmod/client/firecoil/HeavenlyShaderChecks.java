package cn.blockforge.generated.generatedmod.client.firecoil;

import cn.blockforge.generated.generatedmod.client.*;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.*;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.*;
import java.nio.file.*;
import java.util.*;

/** Production renderer acceptance, excluded from releases. No player save is opened. */
final class HeavenlyShaderChecks {
    static void run(Path out,double ox,double oy,double oz)throws Exception{
        var mc=Minecraft.getInstance();var target=mc.getMainRenderTarget();int w=target.width,h=target.height;
        var center=new Vec3(ox,oy+4.5,oz+4.5);var captures=new ArrayList<Map<String,Object>>();
        int generations=HeavenlyFurnaceGpu.generation;
        try{
            target.resize(1920,1080,Minecraft.ON_OSX);
            capture(out,"shader-hero",center,new Vec3(.7,1.1,11.5),false,false,captures);
            int allocations=HeavenlyFurnaceGpu.targetAllocations;
            capture(out,"shader-side",center,new Vec3(11.5,1.1,.7),false,false,captures);
            capture(out,"shader-top",center,new Vec3(.05,12,.05),false,false,captures);
            capture(out,"shader-near",center,new Vec3(.6,.15,5.5),false,false,captures);
            capture(out,"shader-ring-low-angle",center,new Vec3(.4,-2.2,5.5),false,false,captures);
            capture(out,"shader-ring-upper-near",center,new Vec3(.3,3.8,5.7),false,false,captures);
            capture(out,"shader-wall",center,new Vec3(.7,1.1,11.5),true,false,captures);
            if(HeavenlyFurnaceGpu.targetAllocations!=allocations)throw new AssertionError("Targets reallocated at fixed size");
            capture(out,"shader-two-casts",center,new Vec3(.7,1.1,11.5),false,true,captures);
            HeavenlyFurnaceGpu.release();
            capture(out,"shader-after-resource-release",center,new Vec3(.7,1.1,11.5),false,false,captures);
            if(HeavenlyFurnaceGpu.generation<=generations)throw new AssertionError("GPU resources did not rebuild after release");
        }finally{target.resize(w,h,Minecraft.ON_OSX);mc.resizeDisplay();TrailShaderBridge.resetFrame();}
        Files.writeString(out.resolve("shader-acceptance.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(Map.of("passed",true,"worldScale",HeavenlyFurnaceGpu.WORLD_SCALE,"ringOuterDiameterBlocks",2*(2.13+.145)*HeavenlyFurnaceGpu.WORLD_SCALE,"nativeResolution",true,"surfaceSubdivisions",512,"volumeSteps",256,"depthPeels",8,"targetReuse",true,"explicitResourceReleaseRebuild",true,"captures",captures)));
    }
    private static void capture(Path out,String name,Vec3 center,Vec3 offset,boolean wall,boolean two,List<Map<String,Object>> captures)throws Exception{
        var mc=Minecraft.getInstance();var main=mc.getMainRenderTarget();main.bindWrite(true);
        RenderSystem.disableScissor();RenderSystem.depthMask(true);GL11.glClearDepth(1);RenderSystem.clearColor(.012f,.018f,.032f,1);RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT,Minecraft.ON_OSX);
        if(wall){RenderSystem.enableScissor(0,0,main.width/2,main.height);GL11.glClearDepth(.1);RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT,Minecraft.ON_OSX);GL11.glClearDepth(1);RenderSystem.disableScissor();}
        TrailShaderBridge.resetFrame();if(!TrailShaderBridge.captureDepth())throw new AssertionError("Missing world depth");
        var camera=new Camera();var eye=center.add(offset);var method=Camera.class.getDeclaredMethod("setPosition",double.class,double.class,double.class);method.setAccessible(true);method.invoke(camera,eye.x,eye.y,eye.z);
        var projection=new Matrix4f().perspective((float)Math.toRadians(50),main.width/(float)main.height,.05f,128);
        var pose=new PoseStack();pose.last().pose().set(new Matrix4f().lookAt(new Vector3f(),new Vector3f((float)-offset.x,(float)-offset.y,(float)-offset.z),new Vector3f(0,1,0)));
        var frustum=new Frustum(pose.last().pose(),projection);frustum.prepare(eye.x,eye.y,eye.z);
        var event=new RenderLevelStageEvent(RenderLevelStageEvent.Stage.AFTER_PARTICLES,mc.levelRenderer,pose,projection,0,0,camera,frustum);
        // Sentinel state catches framebuffer, texture, VAO and depth leakage from raw GL passes.
        var before=state();int draws=HeavenlyFurnaceGpu.draws;
        HeavenlyFurnaceGpu.render(event,center,0,500,1,0);
        if(two)HeavenlyFurnaceGpu.render(event,center.add(2.7,.4,-1.3),57,530,1,0);
        if(!HeavenlyFurnaceGpu.failure.isEmpty()||HeavenlyFurnaceGpu.draws<=draws)throw new AssertionError("No production GPU output: "+HeavenlyFurnaceGpu.failure);
        var after=state();if(!before.equals(after))throw new AssertionError("Raw GL state leaked: "+before+" -> "+after);
        int error=GL11.glGetError();if(error!=0)throw new AssertionError("Shader GL="+error);
        Path file=out.resolve(name+".png");int bright=0,left=0;
        try(var image=Screenshot.takeScreenshot(main)){
            image.writeToFile(file);
            for(int y=0;y<image.getHeight();y++)for(int x=0;x<image.getWidth();x++){
                int p=image.getPixelRGBA(x,y),r=p&255,g=p>>8&255,b=p>>16&255;
                if(Math.max(r,Math.max(g,b))>60){bright++;if(x<image.getWidth()/2-2)left++;}
            }
        }
        if(bright<3000)throw new AssertionError("Effect missing: "+name+" "+bright);
        if(wall&&left!=0)throw new AssertionError("Effect or bloom leaked through foreground: "+left);
        captures.add(Map.of("file",file.toString(),"name",name,"gl",error,"brightPixels",bright,"occludedHalfBrightPixels",wall?left:-1,"stateRestored",true));
    }
    private static List<Integer> state(){
        var s=new ArrayList<Integer>();for(int key:new int[]{GL20.GL_CURRENT_PROGRAM,GL30.GL_VERTEX_ARRAY_BINDING,GL15.GL_ARRAY_BUFFER_BINDING,GL30.GL_DRAW_FRAMEBUFFER_BINDING,GL30.GL_READ_FRAMEBUFFER_BINDING,GL13.GL_ACTIVE_TEXTURE,GL11.GL_DEPTH_FUNC,GL14.GL_BLEND_SRC_RGB,GL14.GL_BLEND_DST_RGB,GL14.GL_BLEND_SRC_ALPHA,GL14.GL_BLEND_DST_ALPHA,GL20.GL_BLEND_EQUATION_RGB,GL20.GL_BLEND_EQUATION_ALPHA,GL21.GL_PIXEL_UNPACK_BUFFER_BINDING,GL11.GL_UNPACK_ALIGNMENT})s.add(GL11.glGetInteger(key));
        for(int key:new int[]{GL11.GL_BLEND,GL11.GL_CULL_FACE,GL11.GL_DEPTH_TEST,GL11.GL_SCISSOR_TEST,GL30.GL_FRAMEBUFFER_SRGB})s.add(GL11.glIsEnabled(key)?1:0);s.add(GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK)?1:0);
        int active=GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);for(int i=0;i<17;i++){GL13.glActiveTexture(GL13.GL_TEXTURE0+i);s.add(GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D));s.add(GL11.glGetInteger(GL33.GL_SAMPLER_BINDING));}GL13.glActiveTexture(active);return s;
    }
}
