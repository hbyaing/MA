package cn.blockforge.generated.generatedmod.formation.client;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.formation.FormationMath.V;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.DoubleFunction;

/** 原阵法剑网格与同一48把近景/十字片远景分配，不将阵法替换成粒子占位。 */
final class SwordArrayGeometry {
    private static final ResourceLocation TEX=new ResourceLocation(GeneratedMod.MOD_ID,"formations/sword.png");
    private static final ResourceLocation FAR=new ResourceLocation(GeneratedMod.MOD_ID,"formations/sword_distance.png");
    private static float[] mesh;
    private static double tip;
    record Blade(V p,V f,double scale){}
    static void reset(){mesh=null;}
    static V dir(DoubleFunction<V> path,double time){return path.apply(time+.015).sub(path.apply(time-.015)).unit();}
    private static void load(){
        if(mesh!=null)return;
        try(var input=Minecraft.getInstance().getResourceManager().open(new ResourceLocation(GeneratedMod.MOD_ID,"formations/sword.json"))){
            var root=JsonParser.parseReader(new InputStreamReader(input,StandardCharsets.UTF_8)).getAsJsonObject();
            var vertices=root.getAsJsonArray("vertices");mesh=new float[vertices.size()];
            for(int i=0;i<mesh.length;i++)mesh[i]=vertices.get(i).getAsFloat();
            tip=root.getAsJsonObject("sockets").getAsJsonArray("tip").get(1).getAsDouble();
        }catch(java.io.IOException error){throw new IllegalStateException("Missing source formation sword mesh",error);}
    }
    private static V transform(Blade blade,double x,double y,double z){
        V forward=blade.f,right=forward.cross(Math.abs(forward.y())>.96?new V(0,0,1):new V(0,1,0)).unit();
        V up=right.cross(forward).unit();
        return blade.p.add(right.mul(x*blade.scale)).add(forward.mul(y*blade.scale)).add(up.mul(z*blade.scale));
    }
    static void draw(PoseStack pose,List<Blade> blades,V eye){
        load();var matrix=pose.last().pose();RenderSystem.enableDepthTest();RenderSystem.depthMask(true);
        RenderSystem.disableCull();RenderSystem.setShaderColor(1,1,1,1);
        for(boolean far:new boolean[]{false,true}){
            RenderSystem.disableBlend();RenderSystem.setShader(GameRenderer::getPositionTexColorShader);RenderSystem.setShaderTexture(0,far?FAR:TEX);
            var buffer=Tesselator.getInstance().getBuilder();buffer.begin(VertexFormat.Mode.TRIANGLES,DefaultVertexFormat.POSITION_TEX_COLOR);
            int detailed=0;
            for(var blade:blades){
                boolean lod=blade.p.sub(eye).length()>16;if(!lod)lod=detailed++>=48;if(lod!=far)continue;
                if(!far){
                    for(int k=0;k<mesh.length;k+=9){V vertex=transform(blade,mesh[k],mesh[k+1],mesh[k+2]);
                        buffer.vertex(matrix,(float)vertex.x(),(float)vertex.y(),(float)vertex.z()).uv(mesh[k+6],mesh[k+7]).color(255,255,255,255).endVertex();}
                }else{
                    for(int axis=0;axis<2;axis++)for(int index:new int[]{0,1,2,0,2,3}){
                        double[][] quad={{-.196,-.5295},{.196,-.5295},{.196,tip},{-.196,tip}};
                        double x=quad[index][0],y=quad[index][1];V vertex=transform(blade,axis==0?x:0,y,axis==0?0:x*.245);
                        float u=(float)((index==1||index==2?.5:0)+axis*.5),v=index<2?1:0;
                        buffer.vertex(matrix,(float)vertex.x(),(float)vertex.y(),(float)vertex.z()).uv(u,v).color(255,255,255,255).endVertex();
                    }
                }
            }
            BufferUploader.drawWithShader(buffer.end());
        }
        RenderSystem.enableCull();
    }
    private SwordArrayGeometry(){}
}
