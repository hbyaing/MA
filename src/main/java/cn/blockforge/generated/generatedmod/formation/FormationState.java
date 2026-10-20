package cn.blockforge.generated.generatedmod.formation;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import java.util.UUID;

public record FormationState(UUID owner,ResourceLocation dimension,Vec3 origin,long start,long seed) {
    public static final int RENDER_DISTANCE=160;
    public boolean valid(){return owner!=null&&dimension!=null&&origin!=null&&Double.isFinite(origin.x)
            &&Double.isFinite(origin.y)&&Double.isFinite(origin.z)&&Math.abs(origin.x)<=30_000_000
            &&Math.abs(origin.z)<=30_000_000&&Math.abs(origin.y)<=4096&&start>=0;}
    public boolean visible(ResourceLocation dimension,Vec3 eye){return this.dimension.equals(dimension)
            &&eye.distanceToSqr(origin)<=RENDER_DISTANCE*RENDER_DISTANCE;}
}
