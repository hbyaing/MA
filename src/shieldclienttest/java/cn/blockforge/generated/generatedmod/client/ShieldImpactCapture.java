package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.ModEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.nio.file.*;

/** Real server arrows -> production packet -> production shader; no injected visual packets. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT)
public final class ShieldImpactCapture {
    private static final Path OUT=Path.of(Boolean.getBoolean("wuxingxiantu.anvilSizeCapture")
            ? "../../reports/anvil-size-client" : Boolean.getBoolean("wuxingxiantu.guardImpactCapture") ? "../../reports/guard-impact-client" : "../../reports/shield-impact-client");
    private static boolean opened,prepared,finished;
    private static int ticks,phase;private static volatile int wallCameraId=-1;
    private static int captured;
    private static double previousHit=-1000,observedHit=-1000;
    private static String pending;
    private static java.util.concurrent.CompletableFuture<Void> work;
    private static final long START=System.nanoTime();
    private static final boolean GUARD=Boolean.getBoolean("wuxingxiantu.guardImpactCapture");
    private static final boolean ANVIL_SIZE=Boolean.getBoolean("wuxingxiantu.anvilSizeCapture");
    private static double hitTime() throws Exception {
        var field=(GUARD?SpiritGuardLayer.class:SpiritShieldRenderer.class).getDeclaredField("HIT_TIMES");field.setAccessible(true);
        var state=((java.util.Map<?,?>)field.get(null)).get(Minecraft.getInstance().player);
        if(state==null)return -1000;
        return (double)state;
    }
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event) {
        if(event.phase!=TickEvent.Phase.END||finished)return;
        var mc=Minecraft.getInstance();
        try {
            Files.createDirectories(OUT);
            if((System.nanoTime()-START)/1_000_000_000>240)throw new AssertionError("Startup timeout");
            mc.options.pauseOnLostFocus=false;
            if(!opened&&mc.getOverlay()==null&&mc.screen instanceof net.minecraft.client.gui.screens.AccessibilityOnboardingScreen) {
                mc.options.onboardAccessibility=false;mc.options.save();mc.setScreen(new net.minecraft.client.gui.screens.TitleScreen());
            }
            if(!opened&&mc.getOverlay()==null&&mc.screen instanceof net.minecraft.client.gui.screens.TitleScreen) {
                opened=true;mc.getWindow().setWindowed(1280,900);mc.options.renderDistance().set(2);
                Files.deleteIfExists(OUT.resolve("failure.txt"));Files.deleteIfExists(OUT.resolve("complete.txt"));
                Files.deleteIfExists(OUT.resolve("contacts.txt"));
                mc.options.framerateLimit().set(60);mc.options.fov().set(55);mc.options.bobView().set(false);
                mc.options.hideGui=true;mc.getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);
                var rules=new GameRules();rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false,null);
                mc.createWorldOpenFlows().createFreshLevel("shield-impact-"+System.currentTimeMillis(),
                        new LevelSettings("Shield impact test",GameType.CREATIVE,false,net.minecraft.world.Difficulty.NORMAL,true,rules,WorldDataConfiguration.DEFAULT),
                        new WorldOptions(7215,false,false),net.minecraft.world.level.levelgen.presets.WorldPresets::createNormalWorldDimensions);
            }
            if(mc.player==null||mc.getSingleplayerServer()==null||mc.screen instanceof net.minecraft.client.gui.screens.ReceivingLevelScreen)return;
            if(mc.screen!=null)mc.setScreen(null);
            if(!prepared) {
                prepared=true;var id=mc.player.getUUID();
                work=mc.getSingleplayerServer().submitAsync(()->{
                    var p=mc.getSingleplayerServer().getPlayerList().getPlayer(id);var level=p.serverLevel();
                    level.setWeatherParameters(6000,0,false,false);level.setDayTime(6000);
                    level.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false,level.getServer());
                    for(int x=-10;x<=10;x++)for(int z=-10;z<=10;z++)level.setBlock(new BlockPos(x,120,z),Blocks.SMOOTH_STONE.defaultBlockState(),2);
                    p.teleportTo(level,.5,121,.5,180,0);
                    var boots=new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND_BOOTS);
                    boots.enchant(cn.blockforge.generated.generatedmod.SpiritEnchantment.SPIRIT_GUARD.get(),3);
                    p.setItemSlot(net.minecraft.world.entity.EquipmentSlot.FEET,boots);
                    cn.blockforge.generated.generatedmod.GeneratedMod.data(p).wuxingxiantu$setAura(1000);
                    cn.blockforge.generated.generatedmod.GeneratedMod.sync(p);

                    p.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1);
                    p.addEffect(new MobEffectInstance(MobEffects.ABSORPTION,2000,4,false,false));
                    p.addEffect(new MobEffectInstance(ModEffects.SPIRIT_SHIELD_VISUAL.get(),2000,0,false,false));
                    if(GUARD){p.removeAllEffects();p.setAbsorptionAmount(0);p.setGameMode(GameType.SURVIVAL);}
                    if(ANVIL_SIZE) {
                        p.removeAllEffects();p.teleportTo(level,2.5,121,-.2,0,38);
                        level.setDayTime(18000);
                        level.setBlock(new BlockPos(-1,121,2),Blocks.ANVIL.defaultBlockState()
                                .setValue(net.minecraft.world.level.block.AnvilBlock.FACING,net.minecraft.core.Direction.EAST),3);
                        level.setBlock(new BlockPos(2,121,2),cn.blockforge.generated.generatedmod.WuXingBlocks.XUANLING_ANVIL.get().defaultBlockState(),3);
                    }
                });
            }
            if(!work.isDone())return;work.join();ticks++;
            if(ANVIL_SIZE) {
                mc.setCameraEntity(mc.player);mc.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON);
                mc.player.setYRot(0);mc.player.yRotO=0;mc.player.setXRot(38);mc.player.xRotO=38;
                if(ticks==80)pending="anvil-size-comparison.png";
                if(ticks==100) {Files.writeString(OUT.resolve("anvil-size-complete.txt"),"Real client comparison: widened Xuanling left, vanilla right; no GL errors.\n");finished=true;mc.stop();}
                return;
            }
            mc.setCameraEntity(mc.player);mc.options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_FRONT);
            mc.player.setYRot(180);mc.player.yRotO=180;mc.player.setXRot(-8);mc.player.xRotO=-8;
            if(ticks==20) {
                if(cn.blockforge.generated.generatedmod.ArtCatalog.shieldDurationTicks("art_spirit_shield",0)!=400
                        ||cn.blockforge.generated.generatedmod.ArtCatalog.shieldDurationTicks("art_spirit_shield",100)!=520)
                    throw new AssertionError("Shield duration must be 20 to 26 seconds");
            }
            if(ticks==50)pending="idle.png";
            if(ticks==60||ticks==120||ticks==180||ticks==240) {
                phase=ticks/60;previousHit=hitTime();observedHit=-1000;captured=0;
                var id=mc.player.getUUID();int current=phase;
                work=mc.getSingleplayerServer().submitAsync(()->{
                    var p=mc.getSingleplayerServer().getPlayerList().getPlayer(id);var level=p.serverLevel();
                    p.invulnerableTime=0;
                    if(current>=2)p.setGameMode(GameType.SURVIVAL);
                    if(current==4)p.setAbsorptionAmount(1);
                    if(GUARD){p.setGameMode(GameType.SURVIVAL);p.setAbsorptionAmount(0);}
                    if(current==3) {
                        var attacker=net.minecraft.world.entity.EntityType.HUSK.create(level);
                        attacker.setPos(.5,121,-2);p.hurt(level.damageSources().mobAttack(attacker),3);
                    } else {
                        var arrow=new net.minecraft.world.entity.projectile.Arrow(level,.5,121.9,-5.5);
                        arrow.setDeltaMovement(0,0,1.8);arrow.setBaseDamage(2);arrow.setNoGravity(true);level.addFreshEntity(arrow);
                    }
                });
            }
            if(phase>0) {
                double hit=hitTime();
                if(hit>previousHit&&observedHit<0) {
                    observedHit=hit;
                    Files.writeString(OUT.resolve("contacts.txt"),"phase="+phase+" hit="+hit+" absorption="+mc.player.getAbsorptionAmount()+"\n",StandardOpenOption.CREATE,StandardOpenOption.APPEND);
                }
                double age=mc.level.getGameTime()-observedHit;
                if(observedHit>=0&&age>=2&&(captured&1)==0) { pending="impact-"+phase+"-early.png";captured|=1; }
                else if(observedHit>=0&&age>=6&&(captured&2)==0) { pending="impact-"+phase+".png";captured|=2; }
                if(ticks%60==35&&observedHit<0)throw new AssertionError("Missing impact phase "+phase);
            }
            if(GUARD&&ticks==300){var id=mc.player.getUUID();work=mc.getSingleplayerServer().submitAsync(()->{var p=mc.getSingleplayerServer().getPlayerList().getPlayer(id);var cam=net.minecraft.world.entity.EntityType.ARMOR_STAND.create(p.serverLevel());cam.setPos(.5,121.5,-4);cam.setNoGravity(true);cam.setInvisible(true);cam.setYRot(0);cam.setXRot(12);p.serverLevel().addFreshEntity(cam);wallCameraId=cam.getId();for(int x=-2;x<=2;x++)for(int y=121;y<=124;y++)for(int z:new int[]{-1,2})p.serverLevel().setBlock(new BlockPos(x,y,z),Blocks.STONE.defaultBlockState(),3);});}
            if(GUARD&&ticks>=310){var cam=mc.level.getEntity(wallCameraId);if(cam!=null){mc.setCameraEntity(cam);mc.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON);cam.setYRot(0);cam.yRotO=0;cam.setXRot(12);cam.xRotO=12;}}
            if(GUARD&&ticks==320)pending="wall-occlusion.png";
            if(ticks==(GUARD?340:285)) {
                Files.writeString(OUT.resolve("complete.txt"),GUARD?"Guard contacts and wall occlusion capture completed.\n":"4 real contacts passed: creative arrow, survival arrow, melee, shield depletion.\n");
                finished=true;mc.stop();
            }
        } catch(Throwable ex) {
            finished=true;try{Files.writeString(OUT.resolve("failure.txt"),ex.toString());}catch(Exception ignored){}mc.stop();
        }
    }
    @SubscribeEvent public static void frame(TickEvent.RenderTickEvent event) {
        if(event.phase!=TickEvent.Phase.END||pending==null)return;
        try {
            var mc=Minecraft.getInstance();
            int error=org.lwjgl.opengl.GL11.glGetError();
            if(error!=0)throw new AssertionError("OpenGL error "+error);
            if(pending.startsWith("impact")&&!GUARD) {
                var shader=SpiritShieldRenderer.Material.shader;
                float hit=shader.getUniform("Hit").getFloatBuffer().get(0);
                float age=shader.getUniform("HitAge").getFloatBuffer().get(0);
                float z=shader.getUniform("HitDirection").getFloatBuffer().get(2);
                float integrity=shader.getUniform("Integrity").getFloatBuffer().get(0);
                if(integrity<0||integrity>1)throw new AssertionError("Invalid shield integrity");
                if(phase==4&&integrity>.01)throw new AssertionError("Broken shield must turn red");

                if(hit<=0||age<=0||age>=1)throw new AssertionError("Ripple uniforms inactive");
                if(phase!=3&&z>-.9F)throw new AssertionError("Arrow impact not on incoming side");
                Files.writeString(OUT.resolve("uniforms.txt"),pending+" hit="+hit+" age="+age+" directionZ="+z+" integrity="+integrity+"\n",StandardOpenOption.CREATE,StandardOpenOption.APPEND);
            }
            var image=Screenshot.takeScreenshot(mc.getMainRenderTarget());
            try{image.writeToFile(OUT.resolve(pending));}finally{image.close();}
            pending=null;
        }catch(Exception ex){throw new RuntimeException(ex);}
    }
}

