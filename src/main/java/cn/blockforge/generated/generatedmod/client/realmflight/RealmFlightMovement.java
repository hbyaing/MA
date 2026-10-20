package cn.blockforge.generated.generatedmod.client.realmflight;

import cn.blockforge.generated.generatedmod.*;
import cn.blockforge.generated.generatedmod.realmflight.*;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT)
public final class RealmFlightMovement {
    private static net.minecraft.client.player.LocalPlayer jumpPlayer;
    private static net.minecraft.client.player.LocalPlayer accelerationPlayer;
    private static float accelerationSpeed;
    private static net.minecraft.client.multiplayer.ClientLevel inputLevel;
    private static net.minecraft.world.level.Level accelerationLevel;
    private static int lastPlayerTick;
    private static boolean jumpDown,firstFlying;
    private static int firstJump=-100;
    private static void restoreAcceleration(){
        if(accelerationPlayer==null)return;
        // Preserve a different speed supplied by another mode/mod mid-tick.
        if(accelerationPlayer.getAbilities().getFlyingSpeed()==0)
            accelerationPlayer.getAbilities().setFlyingSpeed(accelerationPlayer.level()==accelerationLevel?accelerationSpeed:.05F);
        accelerationPlayer=null;
    }
    @SubscribeEvent public static void resetDimensionInput(TickEvent.ClientTickEvent e){
        if(e.phase!=TickEvent.Phase.START)return;var mc=Minecraft.getInstance();var p=mc.player;
        if(inputLevel!=mc.level||p!=jumpPlayer||p!=null&&p.tickCount<lastPlayerTick){
            restoreAcceleration();
            if(jumpPlayer!=null)RealmFlightControl.release(jumpPlayer);
            if(p!=null)RealmFlightControl.release(p);
            inputLevel=mc.level;jumpPlayer=p;jumpDown=false;firstFlying=false;firstJump=-100;
        }
        lastPlayerTick=p==null?0:p.tickCount;
    }
    @SubscribeEvent public static void doubleJump(TickEvent.ClientTickEvent e){
        if(e.phase!=TickEvent.Phase.END)return;
        // Also restore an old player reference if its post-tick was skipped.
        // This must precede null/paused checks and the double-space toggle.
        restoreAcceleration();var mc=Minecraft.getInstance();var p=mc.player;
        if(p!=jumpPlayer){jumpPlayer=p;jumpDown=false;firstJump=-100;}
        if(p==null||mc.isPaused())return;
        boolean down=mc.screen==null&&mc.options.keyJump.isDown();
        if(down&&!jumpDown&&RealmFlightControl.eligible(p)){
            if(RealmFlightBalance.doubleJump(p.tickCount,firstJump)){
                boolean fly=!firstFlying;firstJump=-100;
                if(p.getAbilities().mayfly){p.getAbilities().flying=fly;RealmFlightControl.release(p);if(fly&&p.onGround())p.setDeltaMovement(p.getDeltaMovement().add(0,.16,0));p.onUpdateAbilities();}
                if(mc.getConnection()!=null)Network.CHANNEL.sendToServer(new RealmFlightTogglePacket(fly));
            }else{firstJump=p.tickCount;firstFlying=p.getAbilities().flying;}
        }
        jumpDown=down;
    }
    @SubscribeEvent public static void tick(TickEvent.PlayerTickEvent e){
        if(e.phase==TickEvent.Phase.END){if(e.player==accelerationPlayer)restoreAcceleration();return;}
        var mc=Minecraft.getInstance();if(e.player!=mc.player)return;
        restoreAcceleration();if(mc.isPaused())return;
        var p=mc.player;if(!RealmFlightControl.eligible(p)||!p.getAbilities().flying||cn.blockforge.generated.generatedmod.client.HeartSwordClientEffects.action(p)>=2){RealmFlightControl.release(p);return;}
        var o=mc.options;boolean controls=mc.screen==null;
        double forward=controls?((o.keyUp.isDown()?1:0)-(o.keyDown.isDown()?1:0)):0;
        double side=controls?((o.keyLeft.isDown()?1:0)-(o.keyRight.isDown()?1:0)):0;
        double up=controls?((o.keyJump.isDown()?1:0)-(o.keyShift.isDown()?1:0)):0;
        double yaw=Math.toRadians(p.getYRot());Vec3 input=new Vec3(side*Math.cos(yaw)-forward*Math.sin(yaw),up,forward*Math.cos(yaw)+side*Math.sin(yaw));
        if(input.lengthSqr()>1)input=input.normalize();boolean boost=controls&&o.keySprint.isDown();
        RealmFlightControl.input(p,input,boost);RealmFlightControl.tick(p);
        accelerationPlayer=p;accelerationLevel=p.level();accelerationSpeed=p.getAbilities().getFlyingSpeed();
        p.getAbilities().setFlyingSpeed(0);
        if(mc.getConnection()!=null)Network.CHANNEL.sendToServer(new RealmFlightInputPacket((float)input.x,(float)input.y,(float)input.z,boost));
    }
    private RealmFlightMovement(){}
}
