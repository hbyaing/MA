package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.fixtures.GameTestPlayer;
import cn.blockforge.generated.generatedmod.progression.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.gametest.*;

@GameTestHolder("wuxingxiantu") @PrefixGameTestTemplate(false)
public final class SenseVisualGameTests {
    private static GameTestPlayer player(GameTestHelper h,int realm){var p=new GameTestPlayer(h.getLevel());p.join();var pos=h.absolutePos(BlockPos.ZERO);p.setPos(pos.getX()+.5,210,pos.getZ()+.5);p.setYRot(0);p.setXRot(0);GeneratedMod.data(p).wuxingxiantu$setRealm(realm);return p;}
    @GameTest(template="empty") public static void rejectedAndDuplicateActionsCannotInventVisuals(GameTestHelper h){
        var p=player(h,1);SpiritualSense.action(p,"scan");h.assertTrue(SpiritualSense.snapshot(p).getCompound("SenseVisual").isEmpty(),"Rejected cast created a visual receipt");
        h.runAfterDelay(6,()->{try(p){GeneratedMod.data(p).wuxingxiantu$setRealm(8);var account=PlayerCrafts.get(p.server).account(p.getUUID());account.putBoolean("SenseOpen",true);account.putFloat("Sense",100);
            SpiritualSense.action(p,"scan");var first=SpiritualSense.snapshot(p).getCompound("SenseVisual");h.assertTrue(first.getString("Kind").equals("scan")&&first.getFloat("Radius")==64,"Authorized scan receipt lost its real radius");
            SpiritualSense.action(p,"scan");h.assertTrue(SpiritualSense.snapshot(p).getCompound("SenseVisual").getLong("Serial")==first.getLong("Serial")&&account.getFloat("Sense")==92,"Replay created visual or double charge");
            first.putString("Kind","forged");h.assertTrue(SpiritualSense.snapshot(p).getCompound("SenseVisual").getString("Kind").equals("scan"),"Snapshot mutated authority");h.assertTrue(!account.contains("SenseVisual"),"Transient visual leaked into saved account");
        }h.succeed();});
    }
    @GameTest(template="empty") public static void movementCancelsOpeningAndItsVisualTogether(GameTestHelper h){
        try(var p=player(h,2)){SpiritualSense.action(p,"open");var first=SpiritualSense.snapshot(p).getCompound("SenseVisual");h.assertTrue(first.getString("Kind").equals("opening"),"Opening visual absent");
            p.setPos(p.getX()+1,p.getY(),p.getZ());SpiritualSense.tick(new TickEvent.PlayerTickEvent(TickEvent.Phase.END,p));var n=SpiritualSense.snapshot(p);
            h.assertTrue(n.getInt("Channel")==0&&!n.getBoolean("SenseOpen")&&n.getCompound("SenseVisual").getString("Kind").equals("interrupt")&&n.getCompound("SenseVisual").getLong("Serial")>first.getLong("Serial"),"Interrupted action left its channel visual active");
        }h.succeed();
    }
    @GameTest(template="empty") public static void identificationKeepsTargetAndDoesNotBecomePersistentLock(GameTestHelper h){
        try(var p=player(h,2)){var account=PlayerCrafts.get(p.server).account(p.getUUID());account.putBoolean("SenseOpen",true);account.putFloat("Sense",100);
            var cow=EntityType.COW.create(h.getLevel());cow.setNoAi(true);cow.setNoGravity(true);cow.setPos(p.getX(),p.getY(),p.getZ()+6);h.getLevel().addFreshEntity(cow);
            SpiritualSense.action(p,"identify");var first=SpiritualSense.snapshot(p).getCompound("SenseVisual");h.assertTrue(first.getString("Kind").equals("identify")&&first.getInt("Target")==cow.getId(),"Focus did not follow the actual visible target");
            for(int i=0;i<40;i++){p.tickCount++;SpiritualSense.tick(new TickEvent.PlayerTickEvent(TickEvent.Phase.END,p));}
            var n=SpiritualSense.snapshot(p);h.assertTrue(n.getInt("Channel")==0&&n.getString("Lock").isEmpty()&&n.getCompound("SenseVisual").getString("Kind").equals("identified"),"Identification never completed or became a lock");cow.discard();
        }h.succeed();
    }
}
