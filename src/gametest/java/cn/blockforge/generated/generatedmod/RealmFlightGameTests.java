package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.fixtures.GameTestPlayer;
import cn.blockforge.generated.generatedmod.realmflight.*;
import net.minecraft.gametest.framework.*;
import net.minecraftforge.gametest.*;

@GameTestHolder("wuxingxiantu_realmflight")
@PrefixGameTestTemplate(false)
public final class RealmFlightGameTests {
    @GameTest(template="empty") public static void rolledBackWorldClockCannotReuseBoostInput(GameTestHelper h)throws Exception{
        try(var p=new GameTestPlayer(h.getLevel())){
            var d=GeneratedMod.data(p);d.wuxingxiantu$setRealm(3);d.wuxingxiantu$setAura(1000);p.getAbilities().flying=true;
            RealmFlightControl.input(p,new net.minecraft.world.phys.Vec3(1,0,0),true);RealmFlightControl.tick(p);
            var mapField=RealmFlightControl.class.getDeclaredField("SERVER_STATES");mapField.setAccessible(true);
            var state=((java.util.Map<?,?>)mapField.get(null)).get(p);var received=state.getClass().getDeclaredField("received");received.setAccessible(true);
            received.setLong(state,h.getLevel().getGameTime()+1000); // Simulate an input from the previous world's clock.
            h.assertTrue(!RealmFlightControl.boosting(p),"Negative packet age was treated as fresh boost");
            RealmFlightControl.tick(p);h.assertTrue(p.getDeltaMovement().lengthSqr()==0,"Old input or momentum survived clock rollback");
            RealmFlightControl.release(p);
        }h.succeed();
    }
    @GameTest(template="empty") public static void dimensionTransitionRebuildsPermissionsAndDropsMomentum(GameTestHelper h){
        try(var p=new GameTestPlayer(h.getLevel())){
            var d=GeneratedMod.data(p);d.wuxingxiantu$setRealm(3);d.wuxingxiantu$setAura(1000);
            p.getAbilities().mayfly=true;p.getAbilities().flying=true;p.getAbilities().setFlyingSpeed(0);
            RealmFlightControl.input(p,new net.minecraft.world.phys.Vec3(1,0,0),true);RealmFlightControl.tick(p);
            p.setDeltaMovement(new net.minecraft.world.phys.Vec3(9,2,9));RealmFlightControl.transition(p);
            h.assertTrue(p.getAbilities().mayfly&&!p.getAbilities().flying,"Return permission missing or auto-flight retained");
            h.assertTrue(p.getAbilities().getFlyingSpeed()==.05F&&p.getDeltaMovement().lengthSqr()==0,"Return inherited speed or momentum");
            h.assertTrue(!RealmFlightControl.boosting(p),"Boost packet survived dimension transition");
            d.wuxingxiantu$setAura(0);RealmFlightControl.transition(p);h.assertTrue(!p.getAbilities().mayfly,"Insufficient mana granted early realm flight");
            d.wuxingxiantu$setRealm(6);RealmFlightControl.transition(p);h.assertTrue(p.getAbilities().mayfly,"Free late realm flight permission lost");
            p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);p.getAbilities().flying=true;p.getAbilities().setFlyingSpeed(.12F);RealmFlightControl.transition(p);
            h.assertTrue(p.getAbilities().flying&&p.getAbilities().getFlyingSpeed()==.12F,"Creative flight was reset");
        }h.succeed();
    }
    @GameTest(template="empty") public static void movementControlNeverPublishesZeroFlightSpeed(GameTestHelper h) {
        try(var p=new GameTestPlayer(h.getLevel())) {
            var data=GeneratedMod.data(p);
            data.wuxingxiantu$setRealm(3);data.wuxingxiantu$setAura(1000);
            p.getAbilities().mayfly=true;p.getAbilities().flying=true;
            p.getAbilities().setFlyingSpeed(.05F);
            RealmFlightControl.input(p,new net.minecraft.world.phys.Vec3(1,0,0),false);
            RealmFlightControl.tick(p);
            h.assertTrue(p.getDeltaMovement().x>0,"Forward intent did not produce movement");
            h.assertTrue(p.getAbilities().getFlyingSpeed()>0,"Controller published zero acceleration in player abilities");
            RealmFlightControl.release(p);
        }
        h.succeed();
    }
    @GameTest(template="empty") public static void orphanedZeroFlightSpeedIsRecovered(GameTestHelper h) {
        try(var p=new GameTestPlayer(h.getLevel())) {
            GeneratedMod.data(p).wuxingxiantu$setRealm(3);
            RealmFlightControl.release(p);
            p.getAbilities().setFlyingSpeed(0);
            RealmFlightControl.release(p);
            h.assertTrue(p.getAbilities().getFlyingSpeed()>0,"Zero speed survived release without a controller state");
            p.getAbilities().setFlyingSpeed(0);
            p.getAbilities().flying=false;
            RealmFlightControl.tick(p);
            h.assertTrue(p.getAbilities().getFlyingSpeed()>0,"Stopped flight did not recover an orphaned zero speed");
        }
        h.succeed();
    }
    @GameTest(template="empty") public static void realmVisualEligibilityDoesNotGrantFlight(GameTestHelper h) {
        try(var p=new GameTestPlayer(h.getLevel())) {
            var data=GeneratedMod.data(p);
            data.wuxingxiantu$setRealm(2);p.getAbilities().flying=true;
            h.assertTrue(!RealmFlightTracking.active(p),"Low realm acquired visual flight");
            data.wuxingxiantu$setRealm(3);
            h.assertTrue(RealmFlightTracking.active(p),"Unlocked realm flight not recognized");
            data.wuxingxiantu$addPossessedForm("minecraft:bat");
            data.wuxingxiantu$setActivePossessedForm("minecraft:bat");
            h.assertTrue(RealmFlightControl.eligible(p)&&RealmFlightTracking.active(p),"Remembered transformation selection incorrectly blocks realm flight");
            p.getPersistentData().putBoolean(RealmFlightModePacket.VANILLA,true);
            h.assertTrue(!RealmFlightTracking.active(p)&&p.getAbilities().flying,"Vanilla choice changed flight ability or kept special visuals");
            p.getPersistentData().putBoolean(RealmFlightModePacket.VANILLA,false);
            p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);p.getAbilities().flying=true;
            h.assertTrue(!RealmFlightTracking.active(p),"Creative flight was replaced by realm visuals");
            p.setGameMode(net.minecraft.world.level.GameType.SPECTATOR);p.getAbilities().flying=true;
            h.assertTrue(!RealmFlightTracking.active(p),"Spectator flight was replaced by realm visuals");
            p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);p.getAbilities().flying=true;
            p.getAbilities().flying=false;
            h.assertTrue(!RealmFlightTracking.active(p)&&!p.getAbilities().flying,"Visual module granted flight");
            p.getAbilities().flying=true;data.wuxingxiantu$setActiveDivineArt("jin_dou_yun");
            h.assertTrue(!RealmFlightTracking.active(p),"Cloud and realm poses overlap");
        }
        h.succeed();
    }
    @GameTest(template="empty") public static void realmFlightVisualPacketRoundTrip(GameTestHelper h) {
        var id=java.util.UUID.randomUUID();
        for(boolean active:new boolean[]{false,true}) {
            var buffer=new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
            try {
                var packet=new RealmFlightVisualPacket(id,active,8,3,true,active);
                RealmFlightVisualPacket.encode(packet,buffer);
                h.assertTrue(packet.equals(RealmFlightVisualPacket.decode(buffer)),"Visual packet lost subject/state");
                buffer.clear();
                var mode=new RealmFlightModePacket(active);
                RealmFlightModePacket.encode(mode,buffer);
                h.assertTrue(mode.equals(RealmFlightModePacket.decode(buffer)),"Mode packet lost preference");
            } finally {buffer.release();}
        }
        h.succeed();
    }
}
