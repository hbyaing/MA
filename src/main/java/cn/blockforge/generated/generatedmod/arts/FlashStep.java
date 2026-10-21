package cn.blockforge.generated.generatedmod.arts;

import cn.blockforge.generated.generatedmod.yujian.FlightCollision;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Short horizontal dash: every swept volume and landing must already be loaded and safe. */
public final class FlashStep {
    private FlashStep() {}
    public static Vec3 destination(ServerPlayer player,double distance) {
        if(player.isPassenger()||!Double.isFinite(distance)||distance<=0)return null;
        Vec3 start=player.position(),look=player.getLookAngle();
        Vec3 direction=new Vec3(look.x,0,look.z);
        if(direction.lengthSqr()<0.0001)return null;
        direction=direction.normalize();
        AABB original=player.getBoundingBox(),previous=original;
        Vec3 result=null;
        for(double step=.125;step<=Math.min(10,distance);step+=.125) {
            Vec3 offset=direction.scale(step);
            AABB box=original.move(offset),sweep=previous.minmax(box);
            if(!safeVolume(player,sweep)||!supported(player,box))break;
            if(step>=.5)result=start.add(offset);
            previous=box;
        }
        return result;
    }
    private static boolean safeVolume(ServerPlayer player,AABB box) {
        var level=player.serverLevel();
        return box.minY>=level.getMinBuildHeight()&&box.maxY<level.getMaxBuildHeight()
                &&FlightCollision.loaded(player,box.inflate(1))&&level.noCollision(player,box)
                &&!level.containsAnyLiquid(box);
    }
    private static boolean supported(ServerPlayer player,AABB box) {
        var level=player.serverLevel();
        AABB feet=new AABB(box.minX,box.minY-.08,box.minZ,box.maxX,box.minY+.01,box.maxZ);
        if(!FlightCollision.loaded(player,feet.inflate(1))||level.containsAnyLiquid(feet)
                ||!level.getBlockCollisions(player,feet).iterator().hasNext())return false;
        for(BlockPos pos:BlockPos.betweenClosed(BlockPos.containing(feet.minX,feet.minY,feet.minZ),
                BlockPos.containing(feet.maxX,box.maxY,feet.maxZ))) {
            var state=level.getBlockState(pos);
            if(state.is(net.minecraft.tags.BlockTags.FIRE)||state.is(Blocks.MAGMA_BLOCK)
                    ||state.is(Blocks.CACTUS)||state.is(Blocks.SWEET_BERRY_BUSH)
                    ||state.is(Blocks.POWDER_SNOW)||state.is(Blocks.WITHER_ROSE)
                    ||state.is(Blocks.CAMPFIRE)||state.is(Blocks.SOUL_CAMPFIRE))return false;
        }
        return true;
    }
}
