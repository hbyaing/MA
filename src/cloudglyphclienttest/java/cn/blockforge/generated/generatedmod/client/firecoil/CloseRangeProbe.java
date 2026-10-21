package cn.blockforge.generated.generatedmod.client.firecoil;

import cn.blockforge.generated.generatedmod.HudConfig;
import cn.blockforge.generated.generatedmod.client.firecoil.FireCore.V;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.*;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.lwjgl.opengl.*;
import java.nio.file.*;
import java.util.*;

/** QA only: native versus bounded third-person integration at the user's close camera angle. */
final class CloseRangeProbe {
    private record Fixture(String name,int tier,Vec3 eye,Vec3 toward) {}
    private static final List<Fixture> FIXTURES=List.of(
            new Fixture("white-V-rear-low",3,new Vec3(.12,.22,-.65),new Vec3(.12,1.45,0)),
            new Fixture("blue-V-rear-low",5,new Vec3(.12,.22,-.65),new Vec3(.12,1.45,0)),
            new Fixture("dark-V-rear-low",8,new Vec3(.12,.22,-.65),new Vec3(.12,1.45,0)),
            new Fixture("blue-V-distant",5,new Vec3(0,1,-32),new Vec3(0,1,0)));
    static void run(Path out)throws Exception{
        var mc=Minecraft.getInstance();var main=mc.getMainRenderTarget();int w=main.width,h=main.height;
        var projection=new Matrix4f(RenderSystem.getProjectionMatrix());var sorting=RenderSystem.getVertexSorting();
        boolean enabled=VolumeGpu.coverageBudgetEnabled,opt=HudConfig.EXOTIC_FIRE_THIRD_PERSON_OPTIMIZATION.get();
        double scale=VolumeGpu.renderScale;int steps=VolumeGpu.stepBudget;var camera=mc.options.getCameraType();
        var rows=new ArrayList<Map<String,Object>>();
        try{
            mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);HudConfig.EXOTIC_FIRE_THIRD_PERSON_OPTIMIZATION.set(true);
            VolumeGpu.renderScale=1;VolumeGpu.stepBudget=512;
            for(int[] size:new int[][]{{1903,1439},{1920,1440}}){
                main.resize(size[0],size[1],Minecraft.ON_OSX);
                var perspective=new Matrix4f().perspective((float)Math.toRadians(70),size[0]/(float)size[1],.05f,100);
                RenderSystem.setProjectionMatrix(perspective,VertexSorting.DISTANCE_TO_ORIGIN);
                for(Fixture f:FIXTURES){
                    var recipe=Client.recipe(f.tier,5);var core=new FireCore();var zero=new V(0,0,0);
                    for(int i=0;i<60;i++)core.tick(recipe,zero,0,true,1);
                    var view=new Matrix4f().lookAt((float)f.eye.x,(float)f.eye.y,(float)f.eye.z,
                            (float)f.toward.x,(float)f.toward.y,(float)f.toward.z,0,1,0);
                    var event=new RenderLevelStageEvent(RenderLevelStageEvent.Stage.AFTER_PARTICLES,mc.levelRenderer,
                            new PoseStack(),perspective,0,.5f,mc.gameRenderer.getMainCamera(),null);
                    Runnable draw=()->{try{VolumeGpu.renderAttached(event,recipe,core,.5,1,view);}catch(Exception failure){throw new RuntimeException(failure);}};
                    var old=new ArrayList<Double>();var next=new ArrayList<Double>();
                    for(int sample=-2;sample<7;sample++)for(int order=0;order<2;order++){
                        boolean adaptive=((sample+2)&1)==order;VolumeGpu.coverageBudgetEnabled=adaptive;clear(main);GL11.glFinish();
                        int[] q={GL15.glGenQueries(),GL15.glGenQueries()};
                        try{
                            GL33.glQueryCounter(q[0],GL33.GL_TIMESTAMP);draw.run();GL33.glQueryCounter(q[1],GL33.GL_TIMESTAMP);
                            double ms=(GL33.glGetQueryObjecti64(q[1],GL15.GL_QUERY_RESULT)-GL33.glGetQueryObjecti64(q[0],GL15.GL_QUERY_RESULT))/1e6;
                            if(sample>=0)(adaptive?next:old).add(ms);
                        }finally{for(int query:q)GL15.glDeleteQueries(query);}
                    }
                    String name="close-"+size[0]+"x"+size[1]+"-"+f.name;
                    VolumeGpu.coverageBudgetEnabled=false;clear(main);draw.run();
                    try(var before=Screenshot.takeScreenshot(main)){
                        VolumeGpu.coverageBudgetEnabled=true;clear(main);draw.run();
                        int actualSteps=GL20.glGetUniformi(VolumeGpu.program,GL20.glGetUniformLocation(VolumeGpu.program,"Steps"));
                        if(actualSteps!=512)throw new AssertionError("Third-person close range changed sample steps to "+actualSteps);
                        if(VolumeGpu.integrationPixels>90000)throw new AssertionError("Production close draw exceeded 90000 integration pixels");
                        if(f.name.endsWith("distant")&&(VolumeGpu.lowWidth!=main.width||VolumeGpu.lowHeight!=main.height))
                            throw new AssertionError("Small distant effect did not preserve native integration target: coverage="+VolumeGpu.coverage+", target="+VolumeGpu.lowWidth+"x"+VolumeGpu.lowHeight+", pixels="+VolumeGpu.integrationPixels);
                        try(var after=Screenshot.takeScreenshot(main)){
                            before.writeToFile(out.resolve(name+"-native.png"));after.writeToFile(out.resolve(name+"-budget.png"));
                            int max=0,visibleOld=0,visibleNew=0;double sum=0;long changed=0;
                            for(int y=0;y<main.height;y++)for(int x=0;x<main.width;x++){
                                int a=before.getPixelRGBA(x,y),b=after.getPixelRGBA(x,y);boolean differs=false;
                                if((a&0xffffff)!=0)visibleOld++;if((b&0xffffff)!=0)visibleNew++;
                                for(int c=0;c<3;c++){int d=Math.abs(((a>>(c*8))&255)-((b>>(c*8))&255));max=Math.max(max,d);sum+=d*d;differs|=d!=0;}
                                if(differs)changed++;
                            }
                            if(Math.min(visibleOld,visibleNew)<100)throw new AssertionError("Close-range image is empty: "+f.name);
                            var row=new LinkedHashMap<String,Object>();row.put("scene",f.name);row.put("viewport",List.of(size[0],size[1]));
                            row.put("oldGpuMs",old);row.put("newGpuMs",next);row.put("oldMedianMs",median(old));row.put("newMedianMs",median(next));
                            row.put("reductionPercent",100*(1-median(next)/median(old)));row.put("actualSteps",actualSteps);
                            row.put("target",List.of(VolumeGpu.lowWidth,VolumeGpu.lowHeight));row.put("projectionCoverage",VolumeGpu.coverage);
                            row.put("integrationPixels",VolumeGpu.integrationPixels);
                            row.put("maxRgbDifference255",max);row.put("rgbRmse255",Math.sqrt(sum/(main.width*(double)main.height*3)));
                            row.put("changedPixels",changed);row.put("visibleNativePixels",visibleOld);row.put("visibleBudgetPixels",visibleNew);
                            row.put("comparison","Same 512 along-ray steps and density; fewer screen-space rays may soften tiny flame detail. Not image-equivalent.");
                            row.put("scope","Frozen production volume plus composite, black scene, bypasses final shader-pack pipeline; actual world close-camera frames are separate Capture scenes.");
                            rows.add(row);
                        }
                    }
                    // Both targets were warmed above. Switching draw sizes must reuse their cached color targets.
                    int colors=VolumeGpu.colorTargetAllocations,depths=VolumeGpu.depthTargetAllocations;
                    for(int repeat=0;repeat<4;repeat++)for(boolean adaptive:new boolean[]{false,true}){
                        VolumeGpu.coverageBudgetEnabled=adaptive;clear(main);draw.run();
                    }
                    if(colors!=VolumeGpu.colorTargetAllocations||depths!=VolumeGpu.depthTargetAllocations)
                        throw new AssertionError("Stable native/budget alternation allocated new color/depth targets");
                    rows.get(rows.size()-1).put("warmAlternationAllocations",Map.of("color",0,"depth",0,"draws",8));
                    if(GL11.glGetError()!=0)throw new AssertionError("Close range probe GL error");
                }
            }
            budgetMath(out);
        }finally{
            VolumeGpu.coverageBudgetEnabled=enabled;HudConfig.EXOTIC_FIRE_THIRD_PERSON_OPTIMIZATION.set(opt);
            VolumeGpu.renderScale=scale;VolumeGpu.stepBudget=steps;mc.options.setCameraType(camera);
            main.resize(w,h,Minecraft.ON_OSX);RenderSystem.setProjectionMatrix(projection,sorting);
        }
        Files.writeString(out.resolve("close-range-probe.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(rows));
    }
    private static void budgetMath(Path out)throws Exception{
        int checks=0;var random=new Random(20261016);
        for(int[] size:new int[][]{{1280,720},{1903,1439},{1920,1440},{3840,2160}})for(int i=0;i<250;i++){
            int x=random.nextInt(size[0]),y=random.nextInt(size[1]);int[] area={x,y,x+1+random.nextInt(size[0]-x),y+1+random.nextInt(size[1]-y)};
            int[] result=VolumeGpu.integrationSizeForTest(size[0],size[1],area,1,true);
            if(result[2]>90000)throw new AssertionError("Exceeded scissored integration budget: "+Arrays.toString(result));
            int[] plain=VolumeGpu.integrationSizeForTest(size[0],size[1],area,1,false);
            if(plain[0]!=size[0]||plain[1]!=size[1])throw new AssertionError("Disabled optimization changed native target");
            checks++;
        }
        Files.writeString(out.resolve("close-range-budget-math.json"),"{\"randomRectangles\":"+checks+",\"maximumScissoredPixels\":90000,\"passed\":true}");
    }
    private static void clear(com.mojang.blaze3d.pipeline.RenderTarget target){
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER,target.frameBufferId);RenderSystem.viewport(0,0,target.width,target.height);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);RenderSystem.colorMask(true,true,true,true);RenderSystem.depthMask(true);
        GL30.glClearBufferfv(GL11.GL_COLOR,0,new float[]{0,0,0,1});GL30.glClearBufferfv(GL11.GL_DEPTH,0,new float[]{1});
    }
    private static double median(List<Double> values){var a=values.stream().mapToDouble(Double::doubleValue).sorted().toArray();return a[a.length/2];}
    private CloseRangeProbe(){}
}
