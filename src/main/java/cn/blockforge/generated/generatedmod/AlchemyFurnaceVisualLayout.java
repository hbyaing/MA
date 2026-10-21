package cn.blockforge.generated.generatedmod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

/** Model pixels / 16, relative to the bottom-center core; see source-assets/*.bbmodel. */
public final class AlchemyFurnaceVisualLayout {
    public static final double FLOOR_Y = .56125;
    public static final Vec3 MATERIAL_CENTER = new Vec3(0, 1.38, 0);
    public static final Vec3 FIRE_CENTER = new Vec3(0, .64, 0);
    public static final double FIRE_HEIGHT = .84;
    public static final Vec3 MOUTH = new Vec3(0, 3.2217925, 0);
    public static final Vec3 RESULT = MOUTH.add(0, .78, 0);
    public static final double SMALL_RING = .43, LARGE_RING = .78, PICKUP_RADIUS = 2.5;
    public static final int COLLAPSE_TICKS = 8, CORE_TICKS = 10, RISE_TICKS = 42;
    public static final int REVEAL_TICK = 48, COMPLETE_TICKS = 60;
    public static float rotation(Direction inward) {
        return switch (inward) { case NORTH -> 180; case EAST -> 90; case WEST -> -90; default -> 0; };
    }
    public static Vec3 world(BlockPos core, Direction inward, Vec3 local) {
        return local.yRot((float)Math.toRadians(rotation(inward))).add(core.getX()+.5,core.getY(),core.getZ()+.5);
    }
    private AlchemyFurnaceVisualLayout() {}
}
