package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.yujian.ControlledSwordEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.*;
import net.minecraft.world.level.levelgen.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Runs only with -PflightClientTests in a separate generated test world. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT)
public final class FlightClientSmokeTest {
    private static boolean opened,prepared,finished;
    private static int ticks,capture;
    private static final java.util.Set<Integer> captured=new java.util.HashSet<>();
    private static final long START=System.nanoTime();
    private static float leftStart,rightStart,leftEnd,rightEnd;
    private static int maxSegments;
    private static final java.util.List<net.minecraft.core.BlockPos> cameraWall=new java.util.ArrayList<>();
    private static double retractedDistance;
    private static final StringBuilder telemetry=new StringBuilder("tick,roll,angular,speed,cameraRoll,trailSegments,cameraDistance,fovBoost\n");

    @SubscribeEvent(priority=net.minecraftforge.eventbus.api.EventPriority.HIGHEST)
    public static void tick(TickEvent.ClientTickEvent event){
        if(event.phase!=TickEvent.Phase.START||finished)return;
        var client=Minecraft.getInstance();
        try{
            client.setWindowActive(true);client.options.pauseOnLostFocus=false;
            if(!opened && client.getOverlay()==null && client.screen!=null && !(client.screen instanceof net.minecraft.client.gui.screens.TitleScreen)){
                com.mojang.logging.LogUtils.getLogger().info("Flight test dismissing startup screen {}",client.screen.getClass().getSimpleName());
                client.setScreen(new net.minecraft.client.gui.screens.TitleScreen());
            }
            if((System.nanoTime()-START)/1_000_000_000>300)throw new IllegalStateException("Flight client startup timed out");
            if(!opened && client.screen instanceof net.minecraft.client.gui.screens.TitleScreen && client.getOverlay()==null){
                opened=true;
                var settings=new LevelSettings("Sword flight verification",GameType.SURVIVAL,false,net.minecraft.world.Difficulty.PEACEFUL,true,new GameRules(),WorldDataConfiguration.DEFAULT);
                client.createWorldOpenFlows().createFreshLevel("flight-check-"+System.currentTimeMillis(),settings,new WorldOptions(712309L,false,false),
                        net.minecraft.world.level.levelgen.presets.WorldPresets::createNormalWorldDimensions);
            }
            if(client.player==null||client.getSingleplayerServer()==null)return;
            if(client.screen!=null){
                if(client.screen instanceof net.minecraft.client.gui.screens.ReceivingLevelScreen)return;
                com.mojang.logging.LogUtils.getLogger().info("Flight test dismissing in-world screen {}",client.screen.getClass().getSimpleName());
                client.setScreen(null);
            }
            if(!prepared){
                prepared=true;
                client.getSingleplayerServer().execute(()->{
                    var player=client.getSingleplayerServer().getPlayerList().getPlayer(client.player.getUUID());
                    player.teleportTo(0,240,0);player.setNoGravity(true);
                    player.getInventory().setItem(0,new net.minecraft.world.item.ItemStack(net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(new net.minecraft.resources.ResourceLocation(GeneratedMod.MOD_ID,"dark_iron_sword"))));
                    player.serverLevel().setDayTime(6000);
                });
            }
            ticks++;
            WuXingKeybinds.SWORD_FLIGHT.setDown(ticks>=30&&ticks<=42||ticks==900);
            client.options.keyUp.setDown(ticks>=80&&ticks<835);
            client.options.keySprint.setDown(ticks>=95&&ticks<835);
            client.options.keyLeft.setDown(ticks>=115&&ticks<350);
            client.options.keyRight.setDown(ticks>=480&&ticks<715);
            client.options.keyDown.setDown(ticks>=835&&ticks<900);
            client.player.setYRot(0);client.player.setXRot(0);
            if(client.player.getVehicle() instanceof ControlledSwordEntity s){
                maxSegments=Math.max(maxSegments,FlyingSwordEffects.renderedSegments);
                telemetry.append(ticks+","+s.roll()+","+s.angularSpeed()+","+s.flightSpeed()+","+YujianClientController.cameraRoll()+","+FlyingSwordEffects.renderedSegments+","+YujianClientController.cameraDistance()+","+YujianClientController.speedFovBoost()+"\n");
                if(ticks==115)leftStart=s.roll();
                if(ticks==350)leftEnd=s.roll();
                if(ticks==480)rightStart=s.roll();
                if(ticks==715)rightEnd=s.roll();
                if(ticks==835){
                    if(leftStart-leftEnd<1800||rightEnd-rightStart<1800)throw new IllegalStateException("Client roll did not complete five turns: "+(leftStart-leftEnd)+" / "+(rightEnd-rightStart));
                    if(Math.abs(net.minecraft.util.Mth.wrapDegrees(s.roll()))>4)throw new IllegalStateException("Client did not recover upright: "+s.roll());
                    if(maxSegments<100)throw new IllegalStateException("Trail geometry was not submitted");
                }
            }
            if(ticks==850)client.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON);
            if(ticks==875)client.options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_BACK);
            if(ticks==880)client.getSingleplayerServer().execute(()->{
                var p=client.getSingleplayerServer().getPlayerList().getPlayer(client.player.getUUID());
                var origin=p.getVehicle().blockPosition().offset(0,0,-2);
                for(int x=-2;x<=2;x++)for(int y=-1;y<=4;y++){
                    var pos=origin.offset(x,y,0);cameraWall.add(pos);
                    p.serverLevel().setBlockAndUpdate(pos,net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
                }
            });
            if(ticks==885){
                retractedDistance=YujianClientController.cameraDistance();
                if(retractedDistance>2.5)throw new IllegalStateException("Camera did not retract at wall: "+retractedDistance);
                capture=885;
            }
            if(ticks==886)client.getSingleplayerServer().execute(()->{
                var level=client.getSingleplayerServer().getPlayerList().getPlayer(client.player.getUUID()).serverLevel();
                for(var pos:cameraWall)level.setBlockAndUpdate(pos,net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            });
            if(ticks==890){
                double restored=YujianClientController.cameraDistance();
                if(restored<=retractedDistance||restored>=5.95)throw new IllegalStateException("Camera extension did not smooth: "+restored);
                capture=890;
            }
            if(ticks==70){
                if(!(client.player.getVehicle() instanceof ControlledSwordEntity))throw new IllegalStateException("B hold/release did not mount local player");
                if(client.options.getCameraType()!=net.minecraft.client.CameraType.THIRD_PERSON_BACK)throw new IllegalStateException("Mount camera did not activate");
                client.getSingleplayerServer().execute(()->client.getSingleplayerServer().getPlayerList().getPlayer(client.player.getUUID()).setNoGravity(false));
                capture=70;
            }
            if(ticks==110||ticks==220||ticks==345||ticks==475||ticks==600||ticks==710||ticks==835||ticks==860||ticks==930)capture=ticks;
            if(ticks==940){
                if(client.player.isPassenger())throw new IllegalStateException("B did not dismount");
                if(client.options.getCameraType()!=net.minecraft.client.CameraType.FIRST_PERSON)throw new IllegalStateException("Camera did not restore");
                java.nio.file.Files.writeString(java.nio.file.Path.of("flight-client-result.txt"),"PASS: B mount/exit; left turns="+(leftStart-leftEnd)/360+"; right turns="+(rightEnd-rightStart)/360+"; release upright; camera wall retract="+retractedDistance+" and smooth extension; max trail segments="+maxSegments+"; shaderCompat="+FlyingSwordEffects.shaderCompat()+"; screenshots and telemetry. No multiplayer latency test.");
                java.nio.file.Files.writeString(java.nio.file.Path.of("flight-telemetry.csv"),telemetry);
                finished=true;client.stop();
            }
        }catch(Exception ex){
            com.mojang.logging.LogUtils.getLogger().error("FLIGHT_CLIENT_FAIL",ex);
            try{java.nio.file.Files.writeString(java.nio.file.Path.of("flight-client-result.txt"),"FAIL: "+ex);}catch(Exception ignored){}
            finished=true;client.stop();
        }
    }
    @SubscribeEvent public static void screenshot(TickEvent.RenderTickEvent event){
        if(event.phase!=TickEvent.Phase.END||capture==0||!captured.add(capture))return;
        var client=Minecraft.getInstance();
        try(var image=net.minecraft.client.Screenshot.takeScreenshot(client.getMainRenderTarget())){
            image.writeToFile(java.nio.file.Path.of("flight-"+capture+".png"));
        }catch(Exception ex){throw new IllegalStateException(ex);}
    }
}
