package cn.blockforge.generated.generatedmod;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("wuxingxiantu")
@PrefixGameTestTemplate(false)
public final class PossessionCameraGameTests {
    private static final class CameraPlayer extends FakePlayer {
        int corrections;
        CameraPlayer(net.minecraft.server.level.ServerLevel level) {
            super(level,new GameProfile(java.util.UUID.randomUUID(),"CameraTest"));
        }
        @Override public void teleportTo(double x,double y,double z) {
            corrections++;
            super.teleportTo(x,y,z);
        }
    }

    @GameTest(template="furnace_empty",timeoutTicks=100)
    public static void possessionDoesNotFightCameraFollow(GameTestHelper h) {
        for(int x=0;x<7;x++)for(int z=0;z<7;z++)h.setBlock(new BlockPos(x,0,z),Blocks.STONE);
        var player=new CameraPlayer(h.getLevel());
        var start=h.absolutePos(new BlockPos(3,1,1));
        player.setPos(start.getX()+0.5,start.getY(),start.getZ()+0.5);
        h.getLevel().addNewPlayer(player);
        var host=h.spawn(EntityType.PIG,new BlockPos(3,1,3));
        var data=GeneratedMod.data(player);
        h.assertTrue(data!=null,"Player cultivation capability missing");
        // Exercise camera retention with enough mana for all ten upkeep calls.
        data.wuxingxiantu$setRealm(5);
        data.wuxingxiantu$setAura(10000);
        data.wuxingxiantu$setActiveDivineArt("yi_hun_ji_po");
        data.wuxingxiantu$setDivineSoulOrigin(start);
        player.addEffect(new MobEffectInstance(ModEffects.YI_HUN_JI_PO.get(),200));
        h.assertTrue(DivineArtLogic.tryPossess(player,host),"Could not acquire host");
        int baseline=player.corrections;
        try {
            for(int i=0;i<10;i++) {
                // Reproduce vanilla ServerPlayer's camera-follow position before the mod tick.
                player.absMoveTo(host.getX(),host.getY(),host.getZ(),host.getYRot(),host.getXRot());
                DivineArtLogic.setPossessionInput(player,0,0,true,false,false,false,false,false,false);
                DivineArtLogic.tick(player,data);
            }
            h.assertTrue(player.corrections==baseline,"Possession sends repeated return-to-origin teleports");
            h.assertTrue(player.getCamera()==host,"Possession camera was reset");
        } finally {
            DivineArtLogic.stop(player,data,false);
            player.discard();
        }
        h.assertTrue(player.corrections==baseline+1,"Release must restore the recorded origin exactly once");
        h.assertTrue(!host.isNoAi(),"Host AI not restored after release");
        h.succeed();
    }
}
