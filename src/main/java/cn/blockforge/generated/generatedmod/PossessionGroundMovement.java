package cn.blockforge.generated.generatedmod;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;

/** Ground movement for AI-disabled possession hosts, using vanilla acceleration and tick order. */
public final class PossessionGroundMovement {
    private PossessionGroundMovement() {}

    public static void move(LivingEntity host,float strafe,float forward,boolean jump,boolean sneak) {
        boolean grounded=host.onGround();
        BlockPos below=BlockPos.containing(host.getX(),host.getY()-0.5000001D,host.getZ());
        float friction=host.level().getBlockState(below).getFriction(host.level(),below,host);
        double drag=grounded ? friction*0.91D : 0.91D;
        double speed=host.getAttributeValue(Attributes.MOVEMENT_SPEED);
        float acceleration=grounded ? (float)(speed*0.21600002D/(friction*friction*friction)) : 0.02F;
        if(sneak) acceleration*=0.3F;
        host.setSprinting(false);
        host.moveRelative(acceleration,new Vec3(strafe,0,forward));
        Vec3 velocity=host.getDeltaMovement();
        if(jump && grounded) {
            var strength=host.getAttribute(Attributes.JUMP_STRENGTH);
            double power=strength==null ? 0.42D : strength.getValue();
            float factor=host.level().getBlockState(host.blockPosition()).getBlock().getJumpFactor();
            if(factor==1.0F) factor=host.level().getBlockState(below).getBlock().getJumpFactor();
            var boost=host.getEffect(MobEffects.JUMP);
            power=power*factor+(boost==null ? 0 : 0.1D*(boost.getAmplifier()+1));
            velocity=new Vec3(velocity.x,power,velocity.z);
            host.setOnGround(false);
            host.hasImpulse=true;
        }
        // Move with the jump impulse first; gravity and drag affect the next tick.
        host.setDeltaMovement(velocity);
        host.move(MoverType.SELF,velocity);
        Vec3 collided=host.getDeltaMovement();
        double y=collided.y;
        if(!host.isNoGravity()) y-=0.08D;
        host.setDeltaMovement(collided.x*drag,y*0.98D,collided.z*drag);
    }
}
