/* COMPILE-TIME STUB — see software/bernie/geckolib/package-info.java */
package software.bernie.geckolib.animatable;

import net.minecraft.world.entity.Entity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;

public interface GeoEntity extends GeoAnimatable {
    @Override
    default double getTick(Object object) {
        return object instanceof Entity entity ? entity.tickCount : 0.0D;
    }
}
