package cn.blockforge.generated.generatedmod;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("wuxingxiantu")
@PrefixGameTestTemplate(false)
public final class PossessionMovementGameTests {
    @GameTest(template="furnace_empty",timeoutTicks=100)
    public static void hostSpeedAndJump(GameTestHelper h) {
        for(int x=0;x<7;x++) for(int z=0;z<7;z++) h.setBlock(new BlockPos(x,0,z),Blocks.STONE);
        var host=h.spawn(EntityType.PIG,new BlockPos(3,1,3));host.setNoAi(true);
        Vec3 start=host.position();
        host.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.1);
        host.setOnGround(true);host.setDeltaMovement(Vec3.ZERO);
        PossessionGroundMovement.move(host,0,1,false,false);
        double slow=host.position().distanceTo(start);
        host.setPos(start);host.setOnGround(true);host.setDeltaMovement(Vec3.ZERO);
        host.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.3);
        PossessionGroundMovement.move(host,0,1,false,false);
        h.assertTrue(Math.abs(host.position().distanceTo(start)/slow-3)<0.01,"Host attribute does not control speed");
        host.setPos(start);host.setOnGround(true);host.setDeltaMovement(Vec3.ZERO);
        PossessionGroundMovement.move(host,0,0,true,false);
        h.assertTrue(host.getY()-start.y>0.419,"Gravity reduced initial jump impulse");
        double peak=host.getY();
        for(int i=0;i<18;i++) {
            PossessionGroundMovement.move(host,0,0,false,false);peak=Math.max(peak,host.getY());
        }
        h.assertTrue(peak-start.y>1.2 && peak-start.y<1.3,"Default host cannot clear a one-block obstacle");
        h.succeed();
    }
}
