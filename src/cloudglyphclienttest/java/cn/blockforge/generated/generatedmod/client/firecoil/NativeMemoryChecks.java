package cn.blockforge.generated.generatedmod.client.firecoil;
import cn.blockforge.generated.generatedmod.client.*;
import com.mojang.blaze3d.vertex.*;
import java.nio.file.*;
import java.util.*;
final class NativeMemoryChecks {
    static void run(Path out)throws Exception{
        var mesh=new ReusableNativeMesh(16384);var cycles=new ArrayList<Map<String,Object>>();
        try(var state=new cn.blockforge.generated.generatedmod.client.realmflight.MeteorBloomRenderer.State()){
            for(int cycle=0;cycle<3;cycle++){
                for(int i=0;i<2000;i++){
                    var b=mesh.begin(VertexFormat.Mode.QUADS,DefaultVertexFormat.POSITION_COLOR);
                    for(int k=0;k<4;k++)b.vertex(k==1||k==2?1:0,k>=2?1:0,0).color(255,255,255,255).endVertex();
                    mesh.draw(net.minecraft.client.renderer.GameRenderer.getPositionColorShader());
                    if(mesh.allocations!=cycle+1||mesh.bytes()!=98304)throw new AssertionError("Native storage grew per draw");
                }
                cycles.add(Map.of("uploads",mesh.uploads,"nativeBytes",mesh.bytes(),"allocations",mesh.allocations));mesh.close();mesh.close();if(mesh.bytes()!=0||mesh.releases!=cycle+1)throw new AssertionError("Native release not idempotent");
            }
            var grown=mesh.begin(VertexFormat.Mode.QUADS,DefaultVertexFormat.POSITION_COLOR);
            for(int k=0;k<20000;k++)grown.vertex(0,0,0).color(255,255,255,255).endVertex();
            mesh.draw(net.minecraft.client.renderer.GameRenderer.getPositionColorShader());if(mesh.bytes()<=98304)throw new AssertionError("Growth path was not exercised");mesh.close();if(mesh.bytes()!=0)throw new AssertionError("Grown native buffer not freed");
            var material=Class.forName("cn.blockforge.generated.generatedmod.client.SoulPlayerVisuals$SoulMaterial");var type=material.getDeclaredMethod("type",net.minecraft.resources.ResourceLocation.class);type.setAccessible(true);var field=material.getDeclaredField("TYPES");field.setAccessible(true);
            for(int i=0;i<1000;i++)type.invoke(null,new net.minecraft.resources.ResourceLocation("wuxingxiantu","test_skin_"+i));
            if(((Map<?,?>)field.get(null)).size()>128)throw new AssertionError("Skin cache unbounded");SoulPlayerVisuals.clearNativeCaches();if(!((Map<?,?>)field.get(null)).isEmpty())throw new AssertionError("Cache survives cleanup");
        }finally{mesh.close();}
        if(org.lwjgl.opengl.GL11.glGetError()!=0)throw new AssertionError("Native stress introduced GL error");
        Files.writeString(out.resolve("native-memory-stress.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(Map.of("passed",true,"uploads",6000,"cycles",cycles,"finalNativeBytes",mesh.bytes(),"skinKeysTested",1000)));
    }
}
