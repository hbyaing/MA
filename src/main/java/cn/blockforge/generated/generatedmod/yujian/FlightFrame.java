package cn.blockforge.generated.generatedmod.yujian;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/** +Z is the blade direction; +Y is the deck normal. Shared physical/render convention. */
public final class FlightFrame {
    private FlightFrame() {}
    public static Quaternionf rotation(float yaw,float pitch,float roll){
        return new Quaternionf().rotateY((float)Math.toRadians(-yaw))
                .rotateX((float)Math.toRadians(pitch)).rotateZ((float)Math.toRadians(roll));
    }
    public static AABB riderBounds(Vec3 root,float yaw,float pitch,float roll,double offset){
        // Roll is a presentation transform. Keeping it out of the swept rider box prevents a
        // visual barrel roll from expanding the AABB and falsely triggering braking/collision.
        var rotation=rotation(yaw,pitch,0);
        AABB box=new AABB(root,root);
        for(int i=0;i<8;i++){
            Vector3f point=new Vector3f((i&1)==0?-0.32F:0.32F,
                    (float)offset+((i&2)==0?0:1.85F),(i&4)==0?-0.32F:0.32F);
            Vec3 corner=root.add(new Vec3(rotation.transform(point)));
            box=box.minmax(new AABB(corner,corner));
        }
        return box;
    }
}
