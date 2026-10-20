package cn.blockforge.generated.generatedmod.divineart;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

/** Shared immutable star positions: visuals and authoritative footfall checks use one anchor. */
public final class DipperPattern {
    public static final int COUNT = 7;
    public static final double HORIZONTAL_SCALE = 5;
    public static final int DURATION_TICKS = 1200;
    private static final Vec3[] STARS = {
        new Vec3(-.5,1.5,2.5), new Vec3(.5,1.5,2.5), new Vec3(1,1.5,1),
        new Vec3(.5,1.5,-1), new Vec3(-.5,1.5,-2.5), new Vec3(-1,1.5,-4), new Vec3(-1,1.5,-5.5)
    };
    public static Vec3 anchor(BlockPos pos) { return new Vec3(pos.getX()+.5,pos.getY(),pos.getZ()+.5); }
    public static Vec3 world(Vec3 anchor, float yaw, Vec3 local) {
        double angle=Math.toRadians(yaw), c=Math.cos(angle), s=Math.sin(angle);
        return anchor.add((local.x*c-local.z*s)*HORIZONTAL_SCALE,local.y,(local.x*s+local.z*c)*HORIZONTAL_SCALE);
    }
    public static Vec3 star(Vec3 anchor,float yaw,int index) {
        return world(anchor,yaw,STARS[Math.max(0,Math.min(COUNT-1,index))]);
    }
    private DipperPattern() {}
}
