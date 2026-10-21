package cn.blockforge.generated.generatedmod.client.firecoil;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.client.firecoil.FireCore.V;
import cn.blockforge.generated.generatedmod.client.weaponflame.WeaponFlameResources;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Matrix4f;
import org.lwjgl.opengl.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;

/** QA-only deterministic replay of the production volume draw; never part of a release. */
final class FireCoilFixedProbe {
    private static final int WARM=2,SAMPLES=3;
    private static final float[] BLACK={0,0,0,1},FAR={1};
    private record Fixed(String name,Recipe recipe,Draw draw) {}
    private record Variant(String name,int program,boolean owned) {}
    @FunctionalInterface private interface Draw {void run()throws Exception;}
    private static final class ProbeCamera extends Camera {
        ProbeCamera(Vec3 position){setPosition(position);}
    }
    @SuppressWarnings("unchecked")
    static Map<String,Object> run(Path out)throws Exception {
        var mc=Minecraft.getInstance();var main=mc.getMainRenderTarget();
        var savedProjection=new Matrix4f(RenderSystem.getProjectionMatrix());var sorting=RenderSystem.getVertexSorting();
        var model=RenderSystem.getModelViewStack();model.pushPose();
        String core=read("/firecoil/volume-reference-core.glsl");
        String wrapper=read("/assets/wuxingxiantu/firecoil/shaders/volume.wrapper.glsl");
        // The approved formula predates per-ray invariant caching; its preparation is intentionally empty.
        if(wrapper.contains("prepareFireVolume(")&&!core.contains("void prepareFireVolume("))
            core+="\nvoid prepareFireVolume(float t,float rayLength,float intensity,float opacity){}\n";
        if(wrapper.contains("fvPrepareRaySupport(")&&!core.contains("void fvPrepareRaySupport("))
            core+="\nvoid fvPrepareRaySupport(vec3 ro,vec3 rd,float first,float last,float stepLength,int steps){}\n";
        if(wrapper.contains("fvRaySampleSupported(")&&!core.contains("bool fvRaySampleSupported("))
            core+="\nbool fvRaySampleSupported(int index){return true;}\n";
        if(wrapper.contains("fvRayAnySupported(")&&!core.contains("bool fvRayAnySupported("))
            core+="\nbool fvRayAnySupported(){return true;}\n";
        if(wrapper.contains("fvRayWordEmpty(")&&!core.contains("bool fvRayWordEmpty("))
            core+="\nbool fvRayWordEmpty(int index){return false;}\n";
        String reference=wrapper.replace("__SHARED_FIRE_VOLUME_CORE__",core);
        int originalProgram=VolumeGpu.program,referenceProgram=compile(reference);
        String optimizedShader=read("/assets/wuxingxiantu/firecoil/shaders/volume.fsh");
        String noPointGrid=optimizedShader.replaceAll("(?m)^#define FC_POINT_GRID[^\\r\\n]*","");
        projectionEyeChecks();
        SupportBuildProbe.run(out);
        List<Variant> variants=List.of(new Variant("full",originalProgram,false),
                new Variant("first-person-low-0375",originalProgram,false),
                new Variant("no-ray",compile(disableRay(optimizedShader)),true),
                new Variant("no-point-grid",compile(noPointGrid),true));
        if(originalProgram==0)throw new AssertionError("Production shader was never rendered");
        Field locations=field("UNIFORMS"),recipeCache=field("uploadedRecipe");
        Map<String,Integer> uniforms=(Map<String,Integer>)locations.get(null),originalUniforms=new HashMap<>(uniforms);
        
        var constructor=Class.forName(VolumeGpu.class.getName()+"$State").getDeclaredConstructor();constructor.setAccessible(true);
        var saved=(AutoCloseable)constructor.newInstance();
        TextureTarget backup=null;
        List<Map<String,Object>> results=new ArrayList<>();
        Map<String,Object> unpackProbe=null;
        boolean previousDirectProbe=VolumeGpu.directProbe;
        boolean previousCoverageBudget=VolumeGpu.coverageBudgetEnabled;
        double previousScale=VolumeGpu.renderScale;
        try {
            // This isolated black-background equivalence probe bypasses deferred
            // shader-pack composition. Actual final-frame visibility is tested
            // separately by FireCoilCapture, after the real shader-pack pipeline.
            VolumeGpu.directProbe=true;
            VolumeGpu.coverageBudgetEnabled=false;
            VolumeGpu.renderScale=1.0;
            backup=new TextureTarget(main.width,main.height,true,Minecraft.ON_OSX);
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
            GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER,main.frameBufferId);
            GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER,backup.frameBufferId);
            GL30.glBlitFramebuffer(0,0,main.width,main.height,0,0,main.width,main.height,
                    GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT,GL11.GL_NEAREST);
            model.setIdentity();RenderSystem.applyModelViewMatrix();
            var projection=new Matrix4f().perspective((float)Math.toRadians(70),(float)main.width/main.height,.05f,100f);
            RenderSystem.setProjectionMatrix(projection,VertexSorting.DISTANCE_TO_ORIGIN);
            unpackProbe=checkUnpackIsolation(fixtures(projection).get(1));
            for(Fixed fixed:fixtures(projection)) {
                VolumeGpu.renderScale=1.0;VolumeGpu.program=referenceProgram;uniforms.clear();recipeCache.set(null,null);clear(main);fixed.draw.run();
                try(var before=Screenshot.takeScreenshot(main)) {
                    before.writeToFile(out.resolve("fixed-"+fixed.name+"-reference.png"));
                    for(Variant variant:variants) {
                        List<Double> optimized=new ArrayList<>(),baseline=new ArrayList<>();
                        List<Double> optimizedSupportCpu=new ArrayList<>(),baselineSupportCpu=new ArrayList<>();
                        // Alternate each pair. Program variants share frozen recipe, simulation and camera.
                        for(int sample=-WARM;sample<SAMPLES;sample++)for(int order=0;order<2;order++) {
                            boolean ref=((sample+WARM)&1)==order;
                            VolumeGpu.renderScale=!ref&&variant.name.endsWith("low-0375")?.375:1.0;
                            VolumeGpu.program=ref?referenceProgram:variant.program;uniforms.clear();recipeCache.set(null,null);clear(main);
                            GL11.glFinish();int[] query={GL15.glGenQueries(),GL15.glGenQueries()};
                            try {
                                GL33.glQueryCounter(query[0],GL33.GL_TIMESTAMP);fixed.draw.run();GL33.glQueryCounter(query[1],GL33.GL_TIMESTAMP);
                                double ms=(GL33.glGetQueryObjecti64(query[1],GL15.GL_QUERY_RESULT)-GL33.glGetQueryObjecti64(query[0],GL15.GL_QUERY_RESULT))/1e6;
                                if(sample>=0){(ref?baseline:optimized).add(ms);(ref?baselineSupportCpu:optimizedSupportCpu).add(VolumeGpu.supportCpuMs);}
                            }finally{for(int q:query)GL15.glDeleteQueries(q);}
                        }
                        VolumeGpu.renderScale=variant.name.endsWith("low-0375")?.375:1.0;
                        VolumeGpu.program=variant.program;uniforms.clear();recipeCache.set(null,null);clear(main);fixed.draw.run();
                        try(var after=Screenshot.takeScreenshot(main)) {
                            String filename="fixed-"+fixed.name+"-"+variant.name+".png";after.writeToFile(out.resolve(filename));
                            int max=0,changed=0,visible=0,optimizedVisible=0;double sum=0;
                            for(int y=0;y<main.height;y++)for(int x=0;x<main.width;x++) {
                                int a=before.getPixelRGBA(x,y),b=after.getPixelRGBA(x,y);boolean differs=false;
                                if((a&0x00ffffff)!=0)visible++;
                                if((b&0x00ffffff)!=0)optimizedVisible++;
                                for(int c=0;c<3;c++){int d=Math.abs(((a>>>(c*8))&255)-((b>>>(c*8))&255));max=Math.max(max,d);sum+=(double)d*d;differs|=d!=0;}
                                if(differs)changed++;
                            }
                            if(visible<=100)throw new AssertionError("Empty fixed probe "+fixed.name+": "+visible+" pixels, require >100");
                            if(optimizedVisible<=100)throw new AssertionError("Empty optimized fixed probe "+fixed.name+"/"+variant.name+": "+optimizedVisible+" pixels, require >100");
                            boolean reduced=variant.name.endsWith("low-0375");
                            if(!reduced&&max>1)throw new AssertionError("Fixed probe changed approved image "+fixed.name+"/"+variant.name+": max RGB difference "+max+" exceeds 1");
                            Map<String,Object> r=new LinkedHashMap<>();r.put("scene",fixed.name);r.put("variant",variant.name);r.put("file",filename);
                            r.put("referenceGpuMs",baseline);r.put("optimizedGpuMs",optimized);r.put("referenceMedianMs",median(baseline));r.put("optimizedMedianMs",median(optimized));
                            r.put("referenceSupportBuildCpuMs",baselineSupportCpu);r.put("optimizedSupportBuildCpuMs",optimizedSupportCpu);
                            r.put("referenceSupportBuildCpuMedianMs",median(baselineSupportCpu));r.put("optimizedSupportBuildCpuMedianMs",median(optimizedSupportCpu));
                            r.put("supportBuildCpuScope","Separately measured CPU wall time around SUPPORT_BOUNDS.build in the single replayed draw; distinct from GPU timestamp results");
                            r.put("reductionPercent",100*(1-median(optimized)/median(baseline)));r.put("maxRgbDifference255",max);
                            r.put("rgbRmse255",Math.sqrt(sum/(main.width*main.height*3.0)));r.put("changedPixels",changed);r.put("visibleReferencePixels",visible);r.put("visibleOptimizedPixels",optimizedVisible);r.put("maximumAllowedRgbDifference255",1);
                            r.put("recipeHash",fixed.recipe.hash());r.put("steps",fixed.recipe.i("segments"));r.put("renderScale",VolumeGpu.renderScale);results.add(r);
                            r.put("comparison",reduced?"Reduced-resolution reconstruction; different rays and softer fine edges, not image-equivalent":"Native same-sample equivalence");
                            r.put("maximumAllowedRgbDifference255",reduced?null:1);
                        }
                        int error=GL11.glGetError();if(error!=0)throw new AssertionError("Fixed probe "+variant.name+" GL "+error);
                    }
                }
            }
            VolumeGpu.program=originalProgram;uniforms.clear();recipeCache.set(null,null);
            projectionProbe(out);
            aggressiveProbe(out,projection);
            CloseRangeProbe.run(out);
        } finally {
            VolumeGpu.directProbe=previousDirectProbe;
            VolumeGpu.coverageBudgetEnabled=previousCoverageBudget;
            VolumeGpu.renderScale=previousScale;
            VolumeGpu.program=originalProgram;uniforms.clear();uniforms.putAll(originalUniforms);recipeCache.set(null,null);
            if(backup!=null) {
                GL11.glDisable(GL11.GL_SCISSOR_TEST);
                GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER,backup.frameBufferId);
                GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER,main.frameBufferId);
                GL30.glBlitFramebuffer(0,0,main.width,main.height,0,0,main.width,main.height,
                        GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT,GL11.GL_NEAREST);
                backup.destroyBuffers();
            }
            GL20.glDeleteProgram(referenceProgram);for(var variant:variants)if(variant.owned)GL20.glDeleteProgram(variant.program);saved.close();
            model.popPose();RenderSystem.applyModelViewMatrix();RenderSystem.setProjectionMatrix(savedProjection,sorting);
        }
        Map<String,Object> report=new LinkedHashMap<>();report.put("scope","Deterministic direct native production volume+composite, empty scene depth; bypasses deferred shader-pack composition and does not establish final-world shader-pack visibility");
        report.put("optimizedShaderSha256",sha(optimizedShader));report.put("curveAnalyticSha256",CurveCache.analyticSha256);
        report.put("referenceCoreSha256",sha(read("/firecoil/volume-reference-core.glsl")));report.put("referenceWrapperSha256",sha(wrapper));
        report.put("viewport",List.of(main.width,main.height));report.put("warmupPairs",WARM);report.put("samplePairs",SAMPLES);report.put("scenes",results);
        report.put("pixelUnpackIsolation",unpackProbe);
        Files.writeString(out.resolve("fixed-probe.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(report));return report;
    }
    private static final int[] UNPACK_KEYS={GL11.GL_UNPACK_ALIGNMENT,GL11.GL_UNPACK_ROW_LENGTH,GL11.GL_UNPACK_SKIP_PIXELS,GL11.GL_UNPACK_SKIP_ROWS,
            GL12.GL_UNPACK_IMAGE_HEIGHT,GL12.GL_UNPACK_SKIP_IMAGES,GL11.GL_UNPACK_SWAP_BYTES,GL11.GL_UNPACK_LSB_FIRST};
    private static final int[] POISON_UNPACK={8,331,7,108,347,3,1,1};
    private static void aggressivePrecision(boolean fast,int width,int height){
        VolumeGpu.cachedGlowEnabled=true;VolumeGpu.stepBudget=fast?192:512;
        VolumeGpu.renderScale=Math.min(fast?.30:.375,Math.sqrt((fast?90000D:180000D)/(width*(double)height)));
    }
    private static void projectionProbe(Path out)throws Exception {
        var mc=Minecraft.getInstance();var main=mc.getMainRenderTarget();
        var savedProjection=new Matrix4f(RenderSystem.getProjectionMatrix());
        boolean previous=VolumeGpu.tightProjection,previousGlow=VolumeGpu.cachedGlowEnabled;double savedScale=VolumeGpu.renderScale;
        var rows=new ArrayList<Map<String,Object>>();
        try {
            VolumeGpu.tightProjection=true;
            var random=new Random(20261016);int covered=0;
            for(int box=0;box<1000;box++){
                float x=random.nextFloat()*4-2,y=random.nextFloat()*4-2,z=random.nextFloat()*3-2;
                float sx=.01f+random.nextFloat(),sy=.01f+random.nextFloat(),sz=.01f+random.nextFloat();
                float[] lo={x-sx,y-sy,z-sz},hi={x+sx,y+sy,z+sz};
                var vp=new Matrix4f().perspective((float)Math.toRadians(70),16f/9,.5f,100).rotateY(random.nextFloat()-.5f);
                int[] bounds=VolumeGpu.projectForTest(lo,hi,vp,new float[3],1280,720);
                for(int a=0;a<3;a++)for(int b=0;b<3;b++)for(int c=0;c<3;c++){
                    var point=vp.transform(new org.joml.Vector4f(lo[0]+a*sx,lo[1]+b*sy,lo[2]+c*sz,1));
                    if(point.w<=.0001f)continue;
                    double px=(point.x/(double)point.w+1)*640,py=(point.y/(double)point.w+1)*360;
                    if(px<0||px>=1280||py<0||py>=720)continue;
                    covered++;if(px<bounds[0]||px>=bounds[2]||py<bounds[1]||py>=bounds[3])throw new AssertionError("Projection lost eye-positive support point "+box);
                }
            }
            if(covered<1000)throw new AssertionError("Insufficient sampled projection coverage");
            Files.writeString(out.resolve("projection-sampled-coverage.json"),"{\"boxes\":1000,\"visiblePointsCovered\":"+covered+",\"passed\":true}");
            VolumeGpu.cachedGlowEnabled=false;
            var json=Client.recipe(5,5).json();json.addProperty("radius",.15);json.addProperty("width",.04);json.addProperty("height",.6);
            json.addProperty("waveAmplitude",.02);json.addProperty("turbulence",.2);json.addProperty("tongueLength",.4);
            json.addProperty("wispRate",0);json.addProperty("emberRate",0);Recipe recipe=Recipe.parse(json.toString());
            var core=new FireCore();var zero=new V(0,0,0);for(int i=0;i<60;i++)core.tick(recipe,zero,0,true,1);
            for(float near:new float[]{.6f,1.2f}) {
                var projection=new Matrix4f().perspective((float)Math.toRadians(70),main.width/(float)main.height,near,100);
                RenderSystem.setProjectionMatrix(projection,VertexSorting.DISTANCE_TO_ORIGIN);
                var event=new RenderLevelStageEvent(RenderLevelStageEvent.Stage.AFTER_PARTICLES,mc.levelRenderer,new PoseStack(),projection,0,.5f,new ProbeCamera(new Vec3(-.55,.3,.7)),null);
                VolumeGpu.renderScale=.3;VolumeGpu.tightProjection=false;clear(main);VolumeGpu.render(event,recipe,core,zero,0,.5,1);double oldCoverage=VolumeGpu.coverage;
                try(var before=Screenshot.takeScreenshot(main)) {
                    VolumeGpu.tightProjection=true;clear(main);VolumeGpu.render(event,recipe,core,zero,0,.5,1);double nextCoverage=VolumeGpu.coverage;
                    try(var after=Screenshot.takeScreenshot(main)) {
                        int max=0,visible=0;
                        for(int y=0;y<main.height;y++)for(int x=0;x<main.width;x++){
                            int a=before.getPixelRGBA(x,y),b=after.getPixelRGBA(x,y);if((b&0xffffff)!=0)visible++;
                            for(int c=0;c<3;c++)max=Math.max(max,Math.abs(((a>>(c*8))&255)-((b>>(c*8))&255)));
                        }
                        if(max>1||visible<100||oldCoverage<.99||nextCoverage>=.95)throw new AssertionError("Eye/near projection mismatch "+near+": max="+max+", visible="+visible+", coverage="+oldCoverage+"/"+nextCoverage);
                        before.writeToFile(out.resolve("near-"+near+"-old.png"));after.writeToFile(out.resolve("near-"+near+"-new.png"));
                        rows.add(Map.of("nearPlane",near,"oldCoverage",oldCoverage,"newCoverage",nextCoverage,"maxRgb255",max,"visiblePixels",visible));
                    }
                }
                assertGl("near projection");
            }
        }finally{VolumeGpu.tightProjection=previous;VolumeGpu.cachedGlowEnabled=previousGlow;VolumeGpu.renderScale=savedScale;RenderSystem.setProjectionMatrix(savedProjection,VertexSorting.DISTANCE_TO_ORIGIN);}
        Files.writeString(out.resolve("projection-probe.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(rows));
    }
    private static void aggressiveProbe(Path out,Matrix4f projection)throws Exception {
        var main=Minecraft.getInstance().getMainRenderTarget();int w=main.width,h=main.height;
        boolean enabled=VolumeGpu.cachedGlowEnabled;double scale=VolumeGpu.renderScale;int budget=VolumeGpu.stepBudget;
        var rows=new ArrayList<Map<String,Object>>();
        try {
            for(int testWidth:new int[]{1280,1920,2560}){
                main.resize(testWidth,testWidth*9/16,Minecraft.ON_OSX);
                VolumeGpu.renderScale=Math.min(.375,Math.sqrt(180000D/(main.width*(double)main.height)));
                for(Fixed fixed:fixtures(projection)){
                    List<Double> old=new ArrayList<>(),next=new ArrayList<>();
                    for(int sample=-2;sample<7;sample++)for(int order=0;order<2;order++){
                        boolean cached=((sample+2)&1)==order;aggressivePrecision(cached,main.width,main.height);clearGlowProbe(main,fixed.name.contains("weapon"));
                        GL11.glFinish();int[] q={GL15.glGenQueries(),GL15.glGenQueries()};
                        try{
                            GL33.glQueryCounter(q[0],GL33.GL_TIMESTAMP);fixed.draw.run();GL33.glQueryCounter(q[1],GL33.GL_TIMESTAMP);
                            double ms=(GL33.glGetQueryObjecti64(q[1],GL15.GL_QUERY_RESULT)-GL33.glGetQueryObjecti64(q[0],GL15.GL_QUERY_RESULT))/1e6;
                            if(sample>=0)(cached?next:old).add(ms);
                        }finally{for(int query:q)GL15.glDeleteQueries(query);}
                    }
                    aggressivePrecision(false,main.width,main.height);clearGlowProbe(main,fixed.name.contains("weapon"));fixed.draw.run();
                    try(var before=Screenshot.takeScreenshot(main)){
                        aggressivePrecision(true,main.width,main.height);clearGlowProbe(main,fixed.name.contains("weapon"));fixed.draw.run();
                        try(var after=Screenshot.takeScreenshot(main)){
                            String file="aggressive-"+testWidth+"-"+fixed.name;
                            before.writeToFile(out.resolve(file+"-old.png"));after.writeToFile(out.resolve(file+"-new.png"));
                            int max=0,visibleOld=0,visibleNew=0;double sum=0;long changed=0;
                            for(int y=0;y<main.height;y++)for(int x=0;x<main.width;x++){
                                int a=before.getPixelRGBA(x,y),b=after.getPixelRGBA(x,y);boolean differs=false;
                                if((a&0xffffff)!=0)visibleOld++;if((b&0xffffff)!=0)visibleNew++;
                                for(int c=0;c<3;c++){int delta=Math.abs(((a>>(c*8))&255)-((b>>(c*8))&255));max=Math.max(max,delta);sum+=delta*delta;differs|=delta>0;}
                                if(differs)changed++;
                            }
                            if(Math.min(visibleOld,visibleNew)<100)throw new AssertionError("Empty cached glow comparison");
                            int active=GL20.glGetUniformi(VolumeGpu.composite,GL20.glGetUniformLocation(VolumeGpu.composite,"CachedGlow"));
                            rows.add(Map.of("scene",fixed.name,"width",testWidth,"oldGpuMs",median(old),"newGpuMs",median(next),"maxRgb255",max,"rmseRgb255",Math.sqrt(sum/(main.width*main.height*3.0)),"changedPixels",changed,"renderScale",VolumeGpu.renderScale,"cacheActive",active,"visibleMinimum",Math.min(visibleOld,visibleNew)));
                        }
                    }
                    assertGl("cached glow comparison");
                }
            }
        }finally{VolumeGpu.cachedGlowEnabled=enabled;VolumeGpu.renderScale=scale;VolumeGpu.stepBudget=budget;main.resize(w,h,Minecraft.ON_OSX);}
        Files.writeString(out.resolve("aggressive-probe.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(rows));
    }
    private static void clearGlowProbe(com.mojang.blaze3d.pipeline.RenderTarget main,boolean wall){
        clear(main);
        if(wall){GL11.glEnable(GL11.GL_SCISSOR_TEST);GL11.glScissor(0,0,main.width/2,main.height);GL11.glClearDepth(.9);GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);GL11.glClearDepth(1);GL11.glDisable(GL11.GL_SCISSOR_TEST);}
    }
    private static void projectionEyeChecks(){
        for(int i=0;i<4;i++){
            var view=new Matrix4f().rotateY(.4f).translate(-.2f,-1.4f,2.8f);
            var bob=new Matrix4f().translate(.03f*i,-.02f*i,.01f*i).rotateZ(.02f*i);
            var projection=new Matrix4f().perspective((float)Math.toRadians(70),16f/9,.05f,1024).mul(bob);
            var inverse=new Matrix4f(projection).mul(view).invert();
            var recovered=VolumeGpu.projectionEye(inverse,view);
            var expected=new Matrix4f(bob).mul(view).invert().transformPosition(new org.joml.Vector3f());
            if(Math.abs(recovered.x()-expected.x)>1e-4||Math.abs(recovered.y()-expected.y)>1e-4||Math.abs(recovered.z()-expected.z)>1e-4)
                throw new AssertionError("View-bob perspective eye lost translation: "+recovered+" vs "+expected);
        }
    }

    /** Allocate only temporary volume resources so the probe cannot invalidate the measured renderer. */
    @SuppressWarnings("unchecked")
    private static Map<String,Object> checkUnpackIsolation(Fixed fixed)throws Exception {
        int[] ambient=new int[UNPACK_KEYS.length];for(int i=0;i<ambient.length;i++)ambient[i]=GL11.glGetInteger(UNPACK_KEYS[i]);
        int ambientBuffer=GL11.glGetInteger(GL21.GL_PIXEL_UNPACK_BUFFER_BINDING),pbo=GL15.glGenBuffers();
        var stateConstructor=Class.forName(VolumeGpu.class.getName()+"$State").getDeclaredConstructor();stateConstructor.setAccessible(true);
        var saved=(AutoCloseable)stateConstructor.newInstance();
        var targets=new IntStatics(VolumeGpu.class,"lowFbo","lowTexture","lowWidth","lowHeight","depthFbo","depthTexture","width","height","depthFormat");
        Map<Object,Object> targetPool=(Map<Object,Object>)field("COLOR_TARGETS").get(null),savedTargetPool=new HashMap<>(targetPool);
        var glows=new IntStatics(VolumeGpu.class,"glowFbo","glowTexture","glowWidth","glowHeight");
        var curves=new IntStatics(CurveCache.class,"program","vao","texture","fbo");
        var supports=new IntStatics(VolumeGpu.class,"supportTexture","supportGridTexture");
        Field previous=field(CurveCache.class,"previous"),curveUniformField=field(CurveCache.class,"UNIFORMS");
        Object previousCurve=previous.get(null);String previousSha=CurveCache.analyticSha256;
        Map<String,Integer> curveUniforms=(Map<String,Integer>)curveUniformField.get(null),oldCurveUniforms=new HashMap<>(curveUniforms);
        List<Map<String,Object>> checks=new ArrayList<>();
        try {
            targetPool.clear();targets.clear();glows.clear();curves.clear();supports.clear();previous.set(null,null);curveUniforms.clear();
            GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER,pbo);GL15.glBufferData(GL21.GL_PIXEL_UNPACK_BUFFER,16L,GL15.GL_STATIC_DRAW);
            for(int i=0;i<UNPACK_KEYS.length;i++)GL11.glPixelStorei(UNPACK_KEYS[i],POISON_UNPACK[i]);
            assertGl("unpack injection");
            GlStateManager._activeTexture(GL13.GL_TEXTURE0);
            var targetMethod=VolumeGpu.class.getDeclaredMethod("targets",com.mojang.blaze3d.pipeline.RenderTarget.class);targetMethod.setAccessible(true);
            targetMethod.invoke(null,Minecraft.getInstance().getMainRenderTarget());checkUnpack("targets first allocation",pbo,checks);
            CurveCache.prepareBody(fixed.recipe,3.025);checkUnpack("curve cache first allocation and bake",pbo,checks);
            GlStateManager._glUseProgram(VolumeGpu.program);
            var supportMethod=VolumeGpu.class.getDeclaredMethod("supportUniforms",Recipe.class,float.class,boolean.class,int.class);supportMethod.setAccessible(true);
            supportMethod.invoke(null,fixed.recipe,3.025f,true,0);checkUnpack("support spheres and point grid first allocation and CPU uploads",pbo,checks);
            if(field("supportTexture").getInt(null)==0||field("supportGridTexture").getInt(null)==0)throw new AssertionError("Support upload did not allocate both textures");
            clear(Minecraft.getInstance().getMainRenderTarget());field("uploadedRecipe").set(null,null);fixed.draw.run();
            checkUnpack("complete production volume and composite draw",pbo,checks);
            if(VolumeGpu.draws!=1||VolumeGpu.lastGl!=0)throw new AssertionError("Injected-state production draw did not complete cleanly");
            double scale=VolumeGpu.renderScale;
            try{
                VolumeGpu.renderScale=.375;clear(Minecraft.getInstance().getMainRenderTarget());fixed.draw.run();
                checkUnpack("cached glow first allocation and production draw",pbo,checks);
                if(field("glowTexture").getInt(null)==0)throw new AssertionError("Glow texture was not allocated");
            }finally{VolumeGpu.renderScale=scale;}
        } finally {
            for(int i=0;i<UNPACK_KEYS.length;i++)GL11.glPixelStorei(UNPACK_KEYS[i],ambient[i]);
            GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER,ambientBuffer);GL15.glDeleteBuffers(pbo);
            var free=VolumeGpu.class.getDeclaredMethod("freeTargets");free.setAccessible(true);free.invoke(null);targetPool.putAll(savedTargetPool);targets.restore();
            var freeGlow=VolumeGpu.class.getDeclaredMethod("freeGlow");freeGlow.setAccessible(true);freeGlow.invoke(null);glows.restore();
            CurveCache.release();curves.restore();previous.set(null,previousCurve);curveUniforms.clear();curveUniforms.putAll(oldCurveUniforms);CurveCache.analyticSha256=previousSha;
            supports.deleteTextures();supports.restore();
            field("uploadedRecipe").set(null,null);saved.close();
        }
        for(int i=0;i<UNPACK_KEYS.length;i++)if(GL11.glGetInteger(UNPACK_KEYS[i])!=ambient[i])throw new AssertionError("Unpack probe failed to restore ambient parameter "+UNPACK_KEYS[i]);
        if(GL11.glGetInteger(GL21.GL_PIXEL_UNPACK_BUFFER_BINDING)!=ambientBuffer)throw new AssertionError("Unpack probe failed to restore ambient PBO");
        assertGl("unpack probe cleanup");
        Map<String,Object> report=new LinkedHashMap<>();report.put("injectedAlignmentRowLengthSkipPixelsSkipRowsImageHeightSkipImagesSwapBytesLsbFirst",POISON_UNPACK);
        report.put("injectedPixelUnpackBufferBytes",16);report.put("restoredStateAssertions",checks.size()*9+9);report.put("glErrorAssertions",checks.size()+2);
        report.put("probeRestoredAmbientState",true);
        report.put("supportTextureAllocationAssertions",2);
        report.put("productionDrawAssertions",2);report.put("checks",checks);return report;
    }
    private static void checkUnpack(String name,int buffer,List<Map<String,Object>> checks) {
        for(int i=0;i<UNPACK_KEYS.length;i++)if(GL11.glGetInteger(UNPACK_KEYS[i])!=POISON_UNPACK[i])throw new AssertionError(name+" changed unpack parameter "+UNPACK_KEYS[i]);
        if(GL11.glGetInteger(GL21.GL_PIXEL_UNPACK_BUFFER_BINDING)!=buffer)throw new AssertionError(name+" changed PBO binding");
        assertGl(name);checks.add(Map.of("stage",name,"restoredStates",9,"glError",0));
    }
    private static void assertGl(String name){int error=GL11.glGetError();if(error!=0)throw new AssertionError(name+" GL "+error);}
    private static final class IntStatics {
        private final Field[] fields;private final int[] values;
        IntStatics(Class<?> owner,String... names)throws Exception {fields=new Field[names.length];values=new int[names.length];for(int i=0;i<names.length;i++){fields[i]=field(owner,names[i]);values[i]=fields[i].getInt(null);}}
        void clear()throws Exception{for(Field f:fields)f.setInt(null,0);}
        void deleteTextures()throws Exception{for(Field f:fields){int id=f.getInt(null);if(id!=0)GlStateManager._deleteTexture(id);f.setInt(null,0);}}
        void restore()throws Exception{for(int i=0;i<fields.length;i++)fields[i].setInt(null,values[i]);}
    }
    private static List<Fixed> fixtures(Matrix4f projection) {
        var mc=Minecraft.getInstance();Recipe body=Client.recipe(5,5),weapon=Client.recipe(2,5);
        var core=new FireCore();V anchor=new V(0,0,0);for(int i=0;i<60;i++)core.tick(body,anchor,0,true,1);
        var event=new RenderLevelStageEvent(RenderLevelStageEvent.Stage.AFTER_PARTICLES,mc.levelRenderer,new PoseStack(),new Matrix4f(projection),0,.5f,
                new ProbeCamera(new Vec3(0,.88,2.75)),null);
        var firstCore=new FireCore();for(int i=0;i<60;i++)firstCore.tick(body,anchor,0,true,body.d("firstPerson"));
        var firstEvent=new RenderLevelStageEvent(RenderLevelStageEvent.Stage.AFTER_PARTICLES,mc.levelRenderer,new PoseStack(),new Matrix4f(projection),0,.5f,
                new ProbeCamera(new Vec3(0,1.62,.05)),null);
        var item=ForgeRegistries.ITEMS.getValue(new ResourceLocation(GeneratedMod.MOD_ID,"guiyuan_sword"));
        var a=WeaponFlameResources.anchor(new ItemStack(item));if(a==null)throw new AssertionError("Missing actual sword anchor");
        Vec3 h=a.hiltAnchor(),tip=a.tipAnchor(),axis=tip.subtract(h).normalize(),side=a.widthAxis(),mid=h.add(tip).scale(.5);
        var itemPose=new Matrix4f().translate(.1f,-.05f,-1.15f).rotateZ(-.35f).scale(1.15f).translate((float)-mid.x,(float)-mid.y,(float)-mid.z);
        float[] base={(float)h.x,(float)h.y,(float)h.z},direction={(float)axis.x,(float)axis.y,(float)axis.z},width={(float)side.x,(float)side.y,(float)side.z};
        return List.of(new Fixed("body-blue-V",body,()->VolumeGpu.render(event,body,core,anchor,0,.5,1)),
                new Fixed("body-first-person-inside",body,()->VolumeGpu.render(firstEvent,body,firstCore,anchor,0,.5,body.d("firstPerson"))),
                new Fixed("weapon-gold-V-close",weapon,()->VolumeGpu.renderWeapon(weapon,itemPose,base,direction,width,a.length(),Math.max(a.bladeWidth(),a.bladeThickness())*.5f)));
    }
    private static String disableRay(String source) {
        // The production wrapper guards all support preparation, empty-ray exits and word skips.
        return source.replace("#version 150","#version 150\n#define FC_DISABLE_RAY_SUPPORT 1");
    }
    private static void clear(com.mojang.blaze3d.pipeline.RenderTarget main) {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER,main.frameBufferId);RenderSystem.viewport(0,0,main.width,main.height);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);RenderSystem.colorMask(true,true,true,true);RenderSystem.depthMask(true);
        GL30.glClearBufferfv(GL11.GL_COLOR,0,BLACK);GL30.glClearBufferfv(GL11.GL_DEPTH,0,FAR);
    }
    private static Field field(String name)throws Exception{return field(VolumeGpu.class,name);}
    private static Field field(Class<?> owner,String name)throws Exception{Field f=owner.getDeclaredField(name);f.setAccessible(true);return f;}
    private static String read(String path)throws Exception{try(var in=FireCoilFixedProbe.class.getResourceAsStream(path)){if(in==null)throw new IllegalStateException(path);return new String(in.readAllBytes(),StandardCharsets.UTF_8);}}
    private static String sha(String source)throws Exception{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(source.getBytes(StandardCharsets.UTF_8)));}
    private static int compile(String fragment)throws Exception {
        Field vf=field("VERTEX");String vertex=(String)vf.get(null);int p=GL20.glCreateProgram(),v=0,f=0;
        try {v=shader(GL20.GL_VERTEX_SHADER,vertex);f=shader(GL20.GL_FRAGMENT_SHADER,fragment);GL20.glAttachShader(p,v);GL20.glAttachShader(p,f);
            GL30.glBindFragDataLocation(p,0,"result");GL20.glLinkProgram(p);if(GL20.glGetProgrami(p,GL20.GL_LINK_STATUS)==0)throw new IllegalStateException(GL20.glGetProgramInfoLog(p));return p;
        }catch(Exception e){GL20.glDeleteProgram(p);throw e;}finally{if(v!=0)GL20.glDeleteShader(v);if(f!=0)GL20.glDeleteShader(f);}
    }
    private static int shader(int type,String source){int s=GL20.glCreateShader(type);GL20.glShaderSource(s,source);GL20.glCompileShader(s);if(GL20.glGetShaderi(s,GL20.GL_COMPILE_STATUS)==0){String e=GL20.glGetShaderInfoLog(s);GL20.glDeleteShader(s);throw new IllegalStateException(e);}return s;}
    private static double median(List<Double> a){return a.stream().mapToDouble(Double::doubleValue).sorted().toArray()[a.size()/2];}
    private FireCoilFixedProbe(){}
}
