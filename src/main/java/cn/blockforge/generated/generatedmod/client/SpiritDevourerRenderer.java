package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.entity.SpiritDevourerEntity;
import com.google.gson.*;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;
import java.io.*;
import java.util.*;

/** Supplied free-form mesh, grouped skeletal transforms and six procedural states. */
public final class SpiritDevourerRenderer extends EntityRenderer<SpiritDevourerEntity> {
    private static final int[] QUAD_INDICES={0,1,2,2};
    private record Bone(String name,float x,float y,float z,float rx,float ry,float rz){}
    private record Triangle(float[][] points,Vector3f normal){}
    private static final class Part {final List<Bone> bones;final int texture;final List<Triangle> faces=new ArrayList<>();Part(List<Bone>b,int t){bones=b;texture=t;}}
    private final List<Part> parts=new ArrayList<>();
    public SpiritDevourerRenderer(EntityRendererProvider.Context context){super(context);shadowRadius=.3F;load();}
    private static ResourceLocation resource(String path){return new ResourceLocation(GeneratedMod.MOD_ID,path);}
    private void load(){
        try(var reader=Minecraft.getInstance().getResourceManager().openAsReader(resource("models/entity/spirit_devourer.json"))){
            Map<String,Part> grouped=new LinkedHashMap<>();
            for(var element:JsonParser.parseReader(reader).getAsJsonObject().getAsJsonArray("triangles")) {
                var face=element.getAsJsonObject();int texture=face.get("texture").getAsInt();
                var chain=face.getAsJsonArray("chain");String key=texture+chain.toString();
                var part=grouped.get(key);
                if(part==null){List<Bone> bones=new ArrayList<>();for(var b:chain){var o=b.getAsJsonObject();var p=o.getAsJsonArray("origin");var r=o.getAsJsonArray("rotation");bones.add(new Bone(o.get("name").getAsString(),p.get(0).getAsFloat(),p.get(1).getAsFloat(),p.get(2).getAsFloat(),r.get(0).getAsFloat(),r.get(1).getAsFloat(),r.get(2).getAsFloat()));}part=new Part(bones,texture);grouped.put(key,part);}
                float[][] points=new float[3][5];var vertices=face.getAsJsonArray("points");
                for(int i=0;i<3;i++)for(int j=0;j<5;j++)points[i][j]=vertices.get(i).getAsJsonArray().get(j).getAsFloat();
                var a=new Vector3f(points[1][0]-points[0][0],points[1][1]-points[0][1],points[1][2]-points[0][2]);
                var b=new Vector3f(points[2][0]-points[0][0],points[2][1]-points[0][1],points[2][2]-points[0][2]);
                a.cross(b);if(a.lengthSquared()<1e-9)continue;a.normalize();part.faces.add(new Triangle(points,a));
            }
            parts.addAll(grouped.values());
        }catch(IOException|RuntimeException e){throw new IllegalStateException("Cannot load supplied spirit devourer mesh",e);}
    }
    @Override public ResourceLocation getTextureLocation(SpiritDevourerEntity e){return resource("textures/entity/spirit_devourer/0.png");}
    @Override public void render(SpiritDevourerEntity e,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light){
        pose.pushPose();pose.mulPose(Axis.YP.rotationDegrees(180-yaw));pose.scale(.075F,.075F,.075F);
        float time=e.tickCount+partial,walk=(float)Math.min(1,e.getDeltaMovement().horizontalDistance()*12);
        if(e.deathTime>0)pose.mulPose(Axis.ZP.rotationDegrees(Math.min(90,(e.deathTime+partial)*5)));
        if(e.attacking()>0)pose.translate(0,0,-Math.sin((10-e.attacking()+partial)*Math.PI/10)*.6);
        int overlay=OverlayTexture.pack(0,e.hurtTime>0);
        for(var part:parts){
            pose.pushPose();boolean abdomen=false;
            for(var bone:part.bones){
                float rx=bone.rx,ry=bone.ry,rz=bone.rz;String name=bone.name;
                abdomen|=name.startsWith("abdomen");
                if(name.matches("(left|right)_(front|middle|hind)_.*")&&!name.contains("segment")){
                    float phase=(name.contains("middle")?3.14159F:0)+(name.startsWith("left")?0:3.14159F);
                    ry+=Math.sin(time*.6+phase)*18*walk;rz+=Math.cos(time*.6+phase)*9*walk;
                    if(e.deathTime>0)rz+=name.startsWith("left")?35:-35;
                }
                if(name.contains("antenna")){ry+=Math.sin(time*(e.feeding()>0?.25:.07))*7;rx+=Math.sin(time*.1)*4;}
                if(name.contains("mandible")&&(e.feeding()>0||e.attacking()>0))ry+=(name.startsWith("left")?1:-1)*(6+Math.sin(time*.8)*5);
                if(name.startsWith("body")){pose.translate(0,Math.sin(time*.08)*.025,0);if(e.hurtTime>0)rx+=12;}
                if(name.startsWith("abdomen")&&e.feeding()>0)pose.scale(1,1+(float)Math.sin(time*.2)*.015F,1);
                pose.translate(bone.x,bone.y,bone.z);pose.mulPose(Axis.ZP.rotationDegrees(rz));pose.mulPose(Axis.YP.rotationDegrees(ry));pose.mulPose(Axis.XP.rotationDegrees(rx));pose.translate(-bone.x,-bone.y,-bone.z);
            }
            var consumer=buffers.getBuffer(RenderType.entityTranslucent(resource("textures/entity/spirit_devourer/"+part.texture+".png")));
            int lit=abdomen&&(e.full()||e.feeding()>0)?LightTexture.FULL_BRIGHT:light;
            var last=pose.last();
            for(var tri:part.faces)for(int index:QUAD_INDICES){
                var v=tri.points[index];consumer.vertex(last.pose(),v[0],v[1],v[2]).color(1F,1F,1F,1F).uv(v[3],v[4]).overlayCoords(overlay).uv2(lit).normal(last.normal(),tri.normal.x,tri.normal.y,tri.normal.z).endVertex();
            }
            pose.popPose();
        }
        pose.popPose();super.render(e,yaw,partial,pose,buffers,light);
    }
}
