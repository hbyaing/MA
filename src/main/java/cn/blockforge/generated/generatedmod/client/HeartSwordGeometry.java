package cn.blockforge.generated.generatedmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import cn.blockforge.generated.generatedmod.client.heartsword.HeartSwordRenderTypes;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import net.minecraft.util.Mth;
import cn.blockforge.generated.generatedmod.balance.HeartSwordTimeline;

/** Short, deterministic spark bursts following the forging palm in either view. */
final class HeartSwordGeometry {
    static void render(PoseStack pose,MultiBufferSource buffers,int action,float progress,float root,float tip) {
        float weight=HeartSwordTimeline.poseWeight(progress);
        if(weight<=0)return;
        // Transparent strokes must not write depth over the physical blade, even while fading.
        VertexConsumer out=buffers.getBuffer(HeartSwordRenderTypes.LIGHT);
        Matrix4f matrix=pose.last().pose();
        float clock=progress*HeartSwordTimeline.duration(action);
        for(int i=0;i<28;i++) {
            float lifetime=3.5F+(i%4)*.55F;
            float age=(clock+i*.731F)%lifetime;
            float born=clock-age;
            if(born<0)continue;
            float sweep=HeartSwordTimeline.sweep(action,born/HeartSwordTimeline.duration(action));
            float origin=Mth.lerp(sweep,root+.06F*(tip-root),tip-.10F*(tip-root));
            float angle=i*2.399963F+(float)Math.floor(born/lifetime)*.63F;
            float speed=.032F+(i%5)*.009F;
            Vector3f velocity=new Vector3f(Mth.cos(angle)*speed,((i%7)-3)*.009F,Mth.sin(angle)*speed);
            Vector3f head=new Vector3f(velocity).mul(age).add(0,origin-age*age*.002F,0);
            Vector3f tail=new Vector3f(head).fma(-.42F,velocity);
            float alpha=weight*(1-age/lifetime);
            float size=.0025F+(i%3)*.0008F;
            streak(out,matrix,tail,head,new Vector3f(size,0,0),alpha);
            streak(out,matrix,tail,head,new Vector3f(0,0,size),alpha);
        }
        if(buffers instanceof MultiBufferSource.BufferSource source)source.endBatch(HeartSwordRenderTypes.LIGHT);
    }
    private static void streak(VertexConsumer out,Matrix4f matrix,Vector3f tail,Vector3f head,Vector3f side,float alpha) {
        vertex(out,matrix,new Vector3f(tail).sub(side),alpha*.15F,false);
        vertex(out,matrix,new Vector3f(tail).add(side),alpha*.15F,false);
        vertex(out,matrix,new Vector3f(head).add(side),alpha,true);
        vertex(out,matrix,new Vector3f(head).sub(side),alpha,true);
    }
    private static void vertex(VertexConsumer out,Matrix4f matrix,Vector3f point,float alpha,boolean hot) {
        out.vertex(matrix,point.x,point.y,point.z).color(hot?.72F:.25F,hot?.94F:.72F,1F,alpha).endVertex();
    }
    private HeartSwordGeometry() {}
}
