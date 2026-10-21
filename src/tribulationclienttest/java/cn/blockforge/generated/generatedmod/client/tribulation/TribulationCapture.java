package cn.blockforge.generated.generatedmod.client.tribulation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.world.level.*;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import cn.blockforge.generated.generatedmod.TribulationManager;
import cn.blockforge.generated.generatedmod.tribulation.TribulationVisualPacket;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import java.nio.file.*;
import java.util.concurrent.CompletableFuture;
/** Opt-in independent-world production packet/shader capture, never touches existing saves. */
@Mod.EventBusSubscriber(modid="wuxingxiantu",value=Dist.CLIENT)
public final class TribulationCapture {
 private static boolean opened,ready,finished;private static int ticks,stage;private static CompletableFuture<?> work;
 private static long captureTick;private static String pending;private static final Path OUT=Path.of("E:/五行仙途/reports/tribulation-install-20261010/gpu");
 private static cn.blockforge.generated.generatedmod.yujian.ControlledSwordEntity cloudSword;
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e){if(!Boolean.getBoolean("wuxingxiantu.tribulationCapture")||finished||e.phase!=TickEvent.Phase.END)return;var mc=Minecraft.getInstance();try{
  mc.options.pauseOnLostFocus=false;
  if(!opened&&mc.getOverlay()==null&&(mc.screen instanceof net.minecraft.client.gui.screens.TitleScreen||mc.screen instanceof net.minecraft.client.gui.screens.AccessibilityOnboardingScreen)){opened=true;Files.createDirectories(OUT);mc.getWindow().setWindowed(1280,720);mc.options.renderDistance().set(8);mc.options.framerateLimit().set(60);mc.options.hideGui=true;mc.getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);mc.createWorldOpenFlows().createFreshLevel("tribulation-check-"+System.currentTimeMillis(),new LevelSettings("Tribulation verification",GameType.CREATIVE,false,net.minecraft.world.Difficulty.NORMAL,true,new GameRules(),WorldDataConfiguration.DEFAULT),new WorldOptions(712309,false,false),net.minecraft.world.level.levelgen.presets.WorldPresets::createNormalWorldDimensions);return;}
  if(mc.level==null||mc.player==null||mc.getSingleplayerServer()==null)return;
  if(!ready){ready=true;mc.setScreen(null);var id=mc.player.getUUID();work=mc.getSingleplayerServer().submit(()->{var p=mc.getSingleplayerServer().getPlayerList().getPlayer(id);for(int x=-8;x<=8;x++)for(int z=-8;z<=8;z++)p.serverLevel().setBlock(new BlockPos(x,219,z),Blocks.STONE.defaultBlockState(),2);p.teleportTo(p.serverLevel(),0,228,55,180,-25);p.getAbilities().flying=true;p.getAbilities().mayfly=true;p.onUpdateAbilities();p.serverLevel().setDayTime(6000);TribulationManager.startTest(p,7,false);});return;}
  if(work!=null){if(!work.isDone())return;work.join();work=null;}ticks++;
  if(ticks==40)Files.writeString(OUT.resolve("weather-client-active.json"),"{\"raining\":"+mc.level.isRaining()+",\"thundering\":"+mc.level.isThundering()+"}");
  if(ticks<40)return;
  if(ticks==250){TribulationEffects.accept(new TribulationVisualPacket(mc.player.getUUID(),mc.level.dimension().location().toString(),22250,7,1,1,18234,mc.level.getGameTime(),new Vec3(-50,290,0),new Vec3(50,294,0),false));captureTick=mc.level.getGameTime()+1;pending="sky-warning-lit.png";}
  if(ticks==260||ticks==270){TribulationEffects.accept(new TribulationVisualPacket(mc.player.getUUID(),mc.level.dimension().location().toString(),20000+ticks,7,ticks==260?4:5,1,18234,mc.level.getGameTime(),new Vec3(0,312,0),new Vec3(0,220,0),true));captureTick=mc.level.getGameTime()+3;pending=ticks==260?"domain-cluster.png":"thunder-art-cluster.png";}
  if(ticks==281){
   var id=mc.player.getUUID();work=mc.getSingleplayerServer().submit(()->{var p=mc.getSingleplayerServer().getPlayerList().getPlayer(id);TribulationManager.stopTest(p);p.serverLevel().setDayTime(18000);p.teleportTo(p.serverLevel(),10,210,15,180,16);});
   mc.options.cloudStatus().set(net.minecraft.client.CloudStatus.FANCY);
   cloudSword=cn.blockforge.generated.generatedmod.registry.ModEntities.CONTROLLED_SWORD.get().create(mc.level);
   cloudSword.ownerId=id;cloudSword.flightRoot(true);cloudSword.flightState(cn.blockforge.generated.generatedmod.yujian.FlyingSwordState.FLYING);cloudSword.setNoGravity(true);cloudSword.setPos(-14,205,0);cloudSword.setYRot(-90);cloudSword.flightSpeed(.65F);
   cloudSword.syncFlightDisplay(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND_SWORD));mc.level.putNonPlayerEntity(-57601,cloudSword);
  }
  if(ticks>281&&cloudSword!=null){cloudSword.setOldPosAndRot();cloudSword.setPos(-14+(ticks-281)*.65,205,0);cloudSword.setDeltaMovement(new Vec3(.65,0,0));}
  if(ticks==320){captureTick=mc.level.getGameTime();pending="sword-above-clouds.png";}
  if(ticks==321){Files.writeString(OUT.resolve("sword-cloud-render.json"),"{\"stage\":\""+cn.blockforge.generated.generatedmod.client.FlyingSwordEffects.renderStage+"\",\"segments\":"+cn.blockforge.generated.generatedmod.client.FlyingSwordEffects.renderedSegments+",\"depth\":\""+cn.blockforge.generated.generatedmod.client.TrailShaderBridge.depthStatus+"\"}");}
  if(ticks==322){mc.options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_BACK);var id=mc.player.getUUID();work=mc.getSingleplayerServer().submit(()->{var p=mc.getSingleplayerServer().getPlayerList().getPlayer(id);p.serverLevel().setDayTime(6000);cn.blockforge.generated.generatedmod.ParalysisControl.apply(p,200);});}
  if(ticks==328||ticks==330||ticks==334){captureTick=mc.level.getGameTime();pending="paralysis-entry-"+ticks+".png";}
  if(ticks==350){captureTick=mc.level.getGameTime();pending="paralysis.png";}
  if(stage<9&&ticks%24==0){int realm=stage<8?stage:7,kind=stage<8?0:2;var packet=new TribulationVisualPacket(mc.player.getUUID(),mc.level.dimension().location().toString(),10000+stage,realm,kind,stage<8?Math.min(1+stage,8):1,18234+stage,mc.level.getGameTime(),new Vec3(0,312,0),new Vec3(0,220,0),true);TribulationEffects.accept(packet);captureTick=mc.level.getGameTime()+3;pending=(kind==2?"final-cluster":"realm-"+realm)+".png";stage++;}
  if(ticks==372){var id=mc.player.getUUID();work=mc.getSingleplayerServer().submit(()->{var p=mc.getSingleplayerServer().getPlayerList().getPlayer(id);p.removeEffect(cn.blockforge.generated.generatedmod.ModEffects.PARALYSIS.get());p.setGameMode(GameType.SURVIVAL);p.setInvulnerable(true);p.fallDistance=0;TribulationManager.stopTest(p);for(var effect:new net.minecraft.world.effect.MobEffect[]{net.minecraft.world.effect.MobEffects.LUCK,net.minecraft.world.effect.MobEffects.DIG_SPEED,net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE,net.minecraft.world.effect.MobEffects.FIRE_RESISTANCE,net.minecraft.world.effect.MobEffects.WATER_BREATHING,net.minecraft.world.effect.MobEffects.DOLPHINS_GRACE,net.minecraft.world.effect.MobEffects.REGENERATION,net.minecraft.world.effect.MobEffects.JUMP,net.minecraft.world.effect.MobEffects.NIGHT_VISION,net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED})p.addEffect(new net.minecraft.world.effect.MobEffectInstance(effect,2000,1));});}
  if(ticks==378){mc.options.guiScale().set(2);mc.resizeDisplay();mc.options.hideGui=false;mc.setScreen(new cn.blockforge.generated.generatedmod.client.CultivationMainScreen(mc.player,"inventory"));}
  if(ticks==382){captureTick=mc.level.getGameTime();pending="inventory-frame.png";}
  if(ticks==386&&mc.screen instanceof cn.blockforge.generated.generatedmod.client.CultivationMainScreen screen)screen.selectPage("vision");
  if(ticks==390){captureTick=mc.level.getGameTime();pending="vision-effects.png";}
  if(ticks==400){mc.setScreen(null);mc.options.hideGui=true;mc.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON);var id=mc.player.getUUID();work=mc.getSingleplayerServer().submit(()->{var p=mc.getSingleplayerServer().getPlayerList().getPlayer(id);p.setGameMode(GameType.CREATIVE);p.getAbilities().flying=true;p.onUpdateAbilities();p.teleportTo(p.serverLevel(),0,228,14,180,-30);TribulationManager.stopTest(p);});}
  if(ticks==408){TribulationEffects.accept(new TribulationVisualPacket(mc.player.getUUID(),mc.level.dimension().location().toString(),51000,7,0,1,18234,mc.level.getGameTime(),new Vec3(0,260,0),new Vec3(0,220,0),true));captureTick=mc.level.getGameTime()+3;pending="white-core-near.png";}
  if(stage==9&&ticks>454){finished=true;var id=mc.player.getUUID();mc.getSingleplayerServer().submit(()->{var p=mc.getSingleplayerServer().getPlayerList().getPlayer(id);TribulationManager.stopTest(p);return p.serverLevel().isRaining();}).thenAccept(rain->{try{Files.writeString(OUT.resolve("weather.json"),"{\"rainingAfterStop\":"+rain+"}");}catch(Exception ex){throw new RuntimeException(ex);}}).join();Files.writeString(OUT.resolve("complete.txt"),"9 production visual captures completed");mc.stop();}
 }catch(Exception ex){finished=true;try{Files.writeString(OUT.resolve("failure.txt"),ex.toString());}catch(Exception ignored){}mc.stop();}}
 @SubscribeEvent(priority=net.minecraftforge.eventbus.api.EventPriority.LOWEST) public static void frame(TickEvent.RenderTickEvent e){if(pending==null||e.phase!=TickEvent.Phase.END)return;var mc=Minecraft.getInstance();if(mc.level==null||mc.level.getGameTime()<captureTick)return;try{if(pending.startsWith("paralysis")){var renderer=(net.minecraft.client.renderer.entity.player.PlayerRenderer)mc.getEntityRenderDispatcher().getRenderer(mc.player);var model=renderer.getModel();Files.writeString(OUT.resolve("paralysis-pose.jsonl"),"{\"capture\":\""+pending+"\",\"model\":\""+model.getClass().getName()+"\",\"leftArmX\":"+model.leftArm.xRot+",\"rightArmX\":"+model.rightArm.xRot+",\"headX\":"+model.head.xRot+",\"y\":"+mc.player.getY()+"}\n",StandardOpenOption.CREATE,StandardOpenOption.APPEND);}int gl=org.lwjgl.opengl.GL11.glGetError();Files.writeString(OUT.resolve("gl-errors.txt"),pending+"="+gl+"\n",StandardOpenOption.CREATE,StandardOpenOption.APPEND);var image=Screenshot.takeScreenshot(mc.getMainRenderTarget());try{image.writeToFile(OUT.resolve(pending));}finally{image.close();}pending=null;}catch(Exception ex){throw new RuntimeException(ex);}}
}

