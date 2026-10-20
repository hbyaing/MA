package cn.blockforge.generated.generatedmod.client.weaponflame;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import org.joml.Vector3f;
import cn.blockforge.generated.generatedmod.client.weaponflame.WeaponFlameModules.Module;

/** Four reusable RGBA modules, clipped along the actual blade and swing paths. */
public final class PixelFlameMesh {
    private final PoseStack pose;
    private final MultiBufferSource buffers;
    private final ExoticFlameVisualProfile profile;
    public PixelFlameMesh(PoseStack pose,MultiBufferSource buffers,ExoticFlameVisualProfile profile) {
        this.pose=pose;this.buffers=buffers;this.profile=profile;
    }
    public static void flush(MultiBufferSource.BufferSource buffers) {
        for(Module module:Module.values())buffers.endBatch(WeaponFlameRenderTypes.module(module));
    }
    public void shard(Vector3f base,Vector3f tip,Vector3f hint,float width,float alpha,int seed) {
        module(base,tip,hint,width,alpha,Module.SCATTER);
    }
    /** Preserve the visible aspect ratio despite large transparent source margins. */
    public void module(Vector3f base,Vector3f tip,Vector3f hint,float width,float alpha,Module module) {
        var axis=new Vector3f(tip).sub(base);
        if(axis.lengthSquared()<.000001F||width<=0||alpha<=0)return;
        float length=Math.min(axis.length(),width*module.aspect);
        axis.normalize();
        var side=perpendicular(hint,axis).mul(length/module.aspect*.5F);
        var start=new Vector3f(tip).fma(-length,axis);
        quad(new Vector3f(start).add(side),new Vector3f(tip).add(side),
                new Vector3f(tip).sub(side),new Vector3f(start).sub(side),module,0,1,alpha,alpha);
    }
    /** Split at sprite/gap boundaries instead of repeating a whole sprite per mesh segment. */
    public void arcSlice(Vector3f base,Vector3f tip,Vector3f side0,Vector3f side1,
            float width,double from,double to,float alpha0,float alpha1,boolean fading) {
        if(width<=0||to<=from||!Double.isFinite(from)||!Double.isFinite(to))return;
        WeaponFlameModules.slices(from,to,fading,slice->{
            float t0=(float)((slice.start()-from)/(to-from)),t1=(float)((slice.end()-from)/(to-from));
            var a=new Vector3f(base).lerp(tip,t0);var b=new Vector3f(base).lerp(tip,t1);
            var tangent=new Vector3f(tip).sub(base);
            if(tangent.lengthSquared()<.000001F)return;
            tangent.normalize();
            var left=perpendicular(new Vector3f(side0).lerp(side1,t0),tangent).mul(width*slice.width()*.5F);
            var right=perpendicular(new Vector3f(side0).lerp(side1,t1),tangent).mul(width*slice.width()*.5F);
            quad(new Vector3f(a).add(left),new Vector3f(b).add(right),
                    new Vector3f(b).sub(right),new Vector3f(a).sub(left),slice.module(),slice.u0(),slice.u1(),
                    Mth.lerp(t0,alpha0,alpha1),Mth.lerp(t1,alpha0,alpha1));
        });
    }
    public void ember(Vector3f center,Vector3f right,Vector3f up,float size,float alpha,int seed) {
        if(size<=0||alpha<=0)return;
        float angle=seed*2.39996F,c=Mth.cos(angle),s=Mth.sin(angle);
        var x=new Vector3f(right).mul(c).fma(s,up).mul(size*Module.SCATTER.aspect);
        var y=new Vector3f(up).mul(c).fma(-s,right).mul(size);
        quad(new Vector3f(center).sub(x).add(y),new Vector3f(center).add(x).add(y),
                new Vector3f(center).add(x).sub(y),new Vector3f(center).sub(x).sub(y),Module.SCATTER,0,1,alpha,alpha);
    }
    private static Vector3f perpendicular(Vector3f hint,Vector3f axis) {
        var side=new Vector3f(hint).fma(-hint.dot(axis),axis);
        if(side.lengthSquared()<.001F)side.set(axis).cross(Math.abs(axis.y)<.9F?new Vector3f(0,1,0):new Vector3f(1,0,0));
        return side.normalize();
    }
    private void quad(Vector3f a,Vector3f b,Vector3f c,Vector3f d,Module module,float start,float end,float alpha0,float alpha1) {
        var out=buffers.getBuffer(WeaponFlameRenderTypes.module(module));
        float u0=Mth.lerp(start,module.u0,module.u1),u1=Mth.lerp(end,module.u0,module.u1);
        vertex(out,a,u0,module.v0,alpha0);vertex(out,b,u1,module.v0,alpha1);
        vertex(out,c,u1,module.v1,alpha1);vertex(out,d,u0,module.v1,alpha0);
    }
    private void vertex(VertexConsumer out,Vector3f p,float u,float v,float alpha) {
        int color=profile.colors().body();
        out.vertex(pose.last().pose(),p.x,p.y,p.z).color(color>>16&255,color>>8&255,color&255,
                Math.round(255*Mth.clamp(alpha,0,1))).uv(u,v).endVertex();
    }
}
