package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.fixtures.GameTestPlayer;
import cn.blockforge.generated.generatedmod.yujian.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.phys.*;
import net.minecraftforge.gametest.*;

@GameTestHolder("wuxingxiantu_firetargets")
@PrefixGameTestTemplate(false)
public final class FlightStallRegressionTests {
    @GameTest(template="empty") public static void bladeProfileKeepsBodyAndSpectrumIndependent(GameTestHelper h)throws Exception{
        try(var stream=FlightStallRegressionTests.class.getResourceAsStream("/assets/wuxingxiantu/firecoil/recipe.json")){
            var body=cn.blockforge.generated.generatedmod.client.firecoil.Recipe.parse(new String(java.util.Objects.requireNonNull(stream).readAllBytes(),java.nio.charset.StandardCharsets.UTF_8)).withSpectrum();
            String original=body.hash();
            var blade=body.forBlade(.778f,.024f);
            h.assertTrue(body.hash().equals(original),"Weapon adaptation mutated body recipe");
            h.assertTrue(blade.dynamicSpectrum(),"Blade profile lost Nirvana spectrum");
            h.assertTrue(blade.d("weaponWidth")<body.d("weaponWidth")*.5,"Narrow blade still uses body-sized flame width");
            h.assertTrue(blade.d("turbulence")<body.d("turbulence"),"Blade still has excessive lateral distortion");
            cn.blockforge.generated.generatedmod.client.firecoil.Recipe.parse(blade.json().toString());
            h.succeed();
        }
    }
    private static FlightSnapshot point(long tick){
        return new FlightSnapshot(1,tick,new Vec3(tick,200,0),0,0,0,0,1,0,0);
    }
    @GameTest(template="empty") public static void interpolationRefillsAfterConsecutiveDelayedTicks(GameTestHelper h){
        var buffer=new FlightInterpolation();
        for(int t=0;t<=10;t++){buffer.accept(point(t),t);buffer.sample(t);}
        // Packets resume after a 10-client-tick delay, without a server tick gap.
        double last=buffer.sample(20).position().x;
        for(int t=11;t<=22;t++){
            buffer.accept(point(t),t+10);
            double x=buffer.sample(t+10).position().x;
            h.assertTrue(x>=last&&x<=t,"Playback rewound or extrapolated after delayed delivery");last=x;
        }
        // Without rebuffering this equals 22 and holds every time a packet is late.
        h.assertTrue(last<=20&&last>=18,"Three-tick interpolation cushion was not restored");
        double a=buffer.sample(33).position().x,b=buffer.sample(34).position().x;
        h.assertTrue(b>a&&b<=22,"Recovered buffer cannot bridge a short packet gap");
        h.succeed();
    }
    @GameTest(template="empty") public static void collisionHaloReportsUnloadedInsteadOfWall(GameTestHelper h){
        try(var player=new GameTestPlayer(h.getLevel())){
            var origin=h.absolutePos(new net.minecraft.core.BlockPos(0,0,0));
            int cx=origin.getX()>>4,cz=origin.getZ()>>4;
            for(int x=cx-40;x<=cx+40;x++)for(int z=cz-40;z<=cz+40;z++){
                if(!h.getLevel().hasChunk(x,z)||h.getLevel().hasChunk(x+1,z))continue;
                double edge=(x+1)*16.,y=h.getLevel().getMaxBuildHeight()+30;
                var box=new AABB(edge-.55,y,z*16+7,edge-.15,y+1,z*16+8);
                h.assertTrue(FlightCollision.loaded(player,box),"Test body not loaded");
                var sweep=FlightCollision.sweep(player,box,new Vec3(.01,0,0),false);
                h.assertTrue(sweep.blocked()&&!sweep.loaded(),"Unloaded collision halo misclassified as solid wall");
                h.assertTrue(sweep.movement().lengthSqr()==0,"Moved into unvalidated collision region");
                h.assertTrue(FlightCollision.predict(player,box,new Vec3(.01,0,0))==FlightCollision.Prediction.UNLOADED,"Prediction disagrees with sweep loading state");
                h.succeed();return;
            }
            throw new IllegalStateException("No loaded/unloaded chunk boundary in test search area");
        }
    }
}
