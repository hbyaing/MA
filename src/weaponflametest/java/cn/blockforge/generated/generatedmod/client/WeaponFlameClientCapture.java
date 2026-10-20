package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.*;
import cn.blockforge.generated.generatedmod.fire.WeaponFlameState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.nio.file.Path;

/** Opt-in real-client captures. This source set is excluded from the release JAR. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT)
public final class WeaponFlameClientCapture {
    private static final long START=System.nanoTime();
    private static boolean opened,prepared,finished;
    private static int ticks,captures;
    private static int targetId;
    private static float cameraYaw=42,cameraPitch=18;
    private static long previousBurnDraws;
    private static String projectedBounds="";
    private static int livingEvents;
    private static net.minecraft.world.entity.decoration.ArmorStand camera;
    private static final int[] CAPTURE_TICKS={100,145,180,221,226,240,265,285,305,325,345,365,385,405,450};
    private static java.util.concurrent.CompletableFuture<Void> setup;
    private static final String LABEL=System.getProperty("wuxingxiantu.flameCapture","idle");
    private static final int FIRE_TIER=Integer.getInteger("wuxingxiantu.flameTier",1);
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event) {
        if(finished||event.phase!=TickEvent.Phase.END)return;
        var mc=Minecraft.getInstance();
        try {
            if((System.nanoTime()-START)/1_000_000_000>240)throw new IllegalStateException("Capture startup timeout");
            mc.options.pauseOnLostFocus=false;
            // A fresh isolated run directory must not wait for an onboarding button click.
            if(!opened&&mc.getOverlay()==null&&mc.screen instanceof net.minecraft.client.gui.screens.AccessibilityOnboardingScreen) {
                mc.options.onboardAccessibility=false;mc.options.save();
                mc.setScreen(new net.minecraft.client.gui.screens.TitleScreen());
            }
            if(!opened&&mc.getOverlay()==null&&mc.screen instanceof net.minecraft.client.gui.screens.TitleScreen) {
                opened=true;mc.getWindow().setWindowed(1600,900);
                mc.options.renderDistance().set(2);mc.options.framerateLimit().set(60);
                mc.options.fov().set(70);mc.options.bobView().set(false);
                mc.options.guiScale().set(2);
                mc.getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);
                mc.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON);
                var settings=new LevelSettings("Weapon flame capture",GameType.CREATIVE,false,
                        net.minecraft.world.Difficulty.NORMAL,true,new GameRules(),WorldDataConfiguration.DEFAULT);
                mc.createWorldOpenFlows().createFreshLevel("weapon-flame-"+System.currentTimeMillis(),settings,
                        new WorldOptions(712309L,false,false),net.minecraft.world.level.levelgen.presets.WorldPresets::createNormalWorldDimensions);
            }
            if(mc.player==null||mc.getSingleplayerServer()==null||mc.screen instanceof net.minecraft.client.gui.screens.ReceivingLevelScreen)return;
            if(mc.screen!=null&&ticks<470)mc.setScreen(null);
            if(!prepared) {
                prepared=true;var uuid=mc.player.getUUID();
                setup=mc.getSingleplayerServer().submitAsync(()->{
                    var server=mc.getSingleplayerServer();var p=server.getPlayerList().getPlayer(uuid);var level=p.serverLevel();
                    level.setWeatherParameters(6000,0,false,false);level.setDayTime(6000);
                    level.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false,server);
                    for(int x=-12;x<=12;x++)for(int z=-8;z<=20;z++)level.setBlock(new BlockPos(x,120,z),Blocks.SMOOTH_STONE.defaultBlockState(),2);
                    for(int x=-12;x<=12;x++)for(int y=121;y<=128;y++)level.setBlock(new BlockPos(x,y,10),Blocks.LIGHT_GRAY_CONCRETE.defaultBlockState(),2);
                    var data=GeneratedMod.data(p);data.wuxingxiantu$setRealm(Math.max(1,FIRE_TIER-1));
                    data.wuxingxiantu$learnFire(FIRE_TIER);data.wuxingxiantu$setEquippedFire(FIRE_TIER);
                    var sword=new ItemStack(WuXingItems.DARK_IRON_SWORD.get());sword.enchant(SpiritEnchantment.EXOTIC_FIRE_WRAP.get(),2);
                    p.setItemInHand(InteractionHand.MAIN_HAND,sword);WeaponFlameState.refresh(p);
                    p.teleportTo(level,.5,121,.5,0,8);p.setInvulnerable(true);
                    var target=net.minecraft.world.entity.EntityType.HUSK.create(level);
                    target.setNoAi(true);target.setPersistenceRequired();target.moveTo(.5,121,3,180,0);
                    target.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(1000);
                    target.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1);
                    target.setHealth(1000);level.addFreshEntity(target);targetId=target.getId();
                });
            }
            if(!setup.isDone())return;setup.join();ticks++;
            mc.player.setXRot(8);mc.player.setYRot(0);
            if(ticks==160) {
                camera=new net.minecraft.world.entity.decoration.ArmorStand(mc.level,3.6,122.0,-1.8);
                camera.setInvisible(true);camera.setNoGravity(true);
                camera.setYRot(42);camera.yRotO=42;camera.setXRot(18);camera.xRotO=18;
                camera.xOld=camera.getX();camera.yOld=camera.getY();camera.zOld=camera.getZ();
                camera.xo=camera.getX();camera.yo=camera.getY();camera.zo=camera.getZ();
                mc.setCameraEntity(camera);mc.options.fov().set(55);
            }
            if(ticks==220) {
                var target=mc.level.getEntity(targetId);
                if(target!=null){mc.gameMode.attack(mc.player,target);mc.player.swing(InteractionHand.MAIN_HAND);}
            }
            if(ticks==240) {
                var target=mc.level.getEntity(targetId);var burn=ExoticBurnClientState.state(target);
                var expected=cn.blockforge.generated.generatedmod.fire.FireCatalog.tier(FIRE_TIER).key().toString();
                if(burn==null||!expected.equals(burn.sourceFireId()))throw new AssertionError("Hit burn did not retain source fire palette");
                if(target.isOnFire())throw new AssertionError("Exotic visual enabled the vanilla burning timer");
            }
            if(ticks==250) {
                // The initial hit above verifies real gameplay. Extend only this isolated fixture
                // so the same burn remains visible throughout the full camera orbit.
                var uuid=mc.player.getUUID();
                mc.getSingleplayerServer().execute(()->{
                    var p=mc.getSingleplayerServer().getPlayerList().getPlayer(uuid);
                    var target=(net.minecraft.world.entity.LivingEntity)p.serverLevel().getEntity(targetId);
                    cn.blockforge.generated.generatedmod.fire.ExoticBurnManager.ignite(target,
                            cn.blockforge.generated.generatedmod.fire.FireCatalog.tier(FIRE_TIER).key(),180,p);
                });
            }
            if(ticks==320)mc.getSingleplayerServer().execute(()->{
                var target=mc.getSingleplayerServer().overworld().getEntity(targetId);
                if(target!=null)target.teleportTo(1.5,121,3.5);
            });
            if(camera!=null&&ticks>=250) {
                var target=mc.level.getEntity(targetId);
                if(target!=null) {
                    double angle=(ticks-250)*.042-1;
                    double x=target.getX()+Math.cos(angle)*5,z=target.getZ()+Math.sin(angle)*5;
                    double eyeY=target.getY()+1.8+Math.sin(angle*1.7)*1.4;
                    double dx=target.getX()-x,dz=target.getZ()-z,dy=target.getY()+target.getBbHeight()*.5-eyeY;
                    cameraYaw=(float)Math.toDegrees(Math.atan2(-dx,dz));
                    cameraPitch=(float)-Math.toDegrees(Math.atan2(dy,5));
                    camera.setPos(x,eyeY-camera.getEyeHeight(),z);
                    camera.setYRot(cameraYaw);camera.yRotO=cameraYaw;camera.setXRot(cameraPitch);camera.xRotO=cameraPitch;
                    camera.xOld=camera.xo=camera.getX();camera.yOld=camera.yo=camera.getY();camera.zOld=camera.zo=camera.getZ();
                }
            }
            if(ticks==450&&ExoticBurnClientState.state(mc.level.getEntity(targetId))!=null)
                throw new AssertionError("Burn did not expire after camera sweep");
            if(ticks>460) {
                if(Boolean.getBoolean("wuxingxiantu.flameCustomRenderer")&&livingEvents!=0)
                    throw new AssertionError("Custom renderer unexpectedly used living events");
                finish("PASS: "+captures+" frames; bare enchanted weapon, real attack, source palette, native fire flag suppressed, 360-degree yaw/pitch orbit, target reposition, burn expiry; customRenderer="
                        +Boolean.getBoolean("wuxingxiantu.flameCustomRenderer")+"; livingEvents="+livingEvents+". Visual acceptance is manual.");
            }

        } catch(Throwable error){com.mojang.logging.LogUtils.getLogger().error("WEAPON_FLAME_CAPTURE",error);finish("FAIL: "+error);}
    }
    @SubscribeEvent public static void hideOverlays(net.minecraftforge.client.event.RenderGuiOverlayEvent.Pre event) {
        if(ticks>=160)event.setCanceled(true);
    }
    @SubscribeEvent(priority=net.minecraftforge.eventbus.api.EventPriority.LOWEST)
    public static void camera(net.minecraftforge.client.event.ViewportEvent.ComputeCameraAngles event) {
        if(camera!=null&&ticks>=160) {
            Minecraft.getInstance().setCameraEntity(camera);
            event.setYaw(cameraYaw);event.setPitch(cameraPitch);event.setRoll(0);
        }
    }
    @SubscribeEvent public static void frame(TickEvent.RenderTickEvent event) {
        if(event.phase==TickEvent.Phase.START&&prepared) {
            var client=Minecraft.getInstance();
            client.options.hideGui=ticks>=160;
            // The mod restores the player camera during each tick; the fixture owns only this render.
            if(camera!=null&&ticks>=160)client.setCameraEntity(camera);
        }
        if(finished||event.phase!=TickEvent.Phase.END||!prepared||captures>=CAPTURE_TICKS.length||ticks<CAPTURE_TICKS[captures])return;
        var mc=Minecraft.getInstance();
        if(mc.player==null||mc.getOverlay()!=null||mc.screen!=null&&!(mc.screen instanceof net.minecraftforge.client.gui.ModListScreen))return;
        try {
            if(WeaponFlameState.fireId(mc.player.getMainHandItem(),mc.player)==null)throw new AssertionError("Held weapon fireId did not synchronize");
            if(ticks>=260&&ticks<=410) {
                if(ExoticBurnClientState.state(mc.level.getEntity(targetId))==null||ExoticBurnRenderer.renderedEntities<=previousBurnDraws)
                    throw new AssertionError("No target flame rendered during camera orbit at tick "+ticks);
                previousBurnDraws=ExoticBurnRenderer.renderedEntities;
            }
            Path dir=Path.of("captures");java.nio.file.Files.createDirectories(dir);
            try(var frame=Screenshot.takeScreenshot(mc.getMainRenderTarget())) {frame.writeToFile(dir.resolve(LABEL+"-"+captures+".png"));}
            java.nio.file.Files.writeString(dir.resolve(LABEL+"-views.csv"),
                    captures+","+ticks+","+cameraYaw+","+cameraPitch+","+projectedBounds+System.lineSeparator(),
                    java.nio.file.StandardOpenOption.CREATE,java.nio.file.StandardOpenOption.APPEND);
            captures++;
        } catch(Throwable error){finish("FAIL: "+error);}
    }
    @SubscribeEvent public static void fov(net.minecraftforge.client.event.ViewportEvent.ComputeFov event) {
        // Additional labelled diagnostic views reveal the off-screen tip without changing sword geometry/transforms.
        if(ticks>=140&&ticks<160&&!event.usedConfiguredFov())event.setFOV(140);
    }
    /** Record native entity screen bounds beside screenshots to detect detached flames. */
    @SubscribeEvent(priority=net.minecraftforge.eventbus.api.EventPriority.LOWEST)
    public static void anchor(net.minecraftforge.client.event.RenderLivingEvent.Post<?, ?> event) {
        if(event.getEntity().getId()!=targetId)return;
        livingEvents++;recordAnchor(event.getEntity(),event.getPoseStack());
    }
    static void recordAnchor(net.minecraft.world.entity.LivingEntity entity,com.mojang.blaze3d.vertex.PoseStack pose) {
        if(entity.getId()!=targetId)return;
        var mc=Minecraft.getInstance();
        var matrix=new org.joml.Matrix4f(com.mojang.blaze3d.systems.RenderSystem.getProjectionMatrix())
                .mul(com.mojang.blaze3d.systems.RenderSystem.getModelViewMatrix()).mul(pose.last().pose());
        float minX=Float.POSITIVE_INFINITY,minY=minX,maxX=Float.NEGATIVE_INFINITY,maxY=maxX;
        float radius=entity.getBbWidth()*.85F;
        for(int i=0;i<8;i++) {
            var point=matrix.transform(new org.joml.Vector4f((i&1)==0?-radius:radius,
                    (i&2)==0?0:entity.getBbHeight()+.6F,(i&4)==0?-radius:radius,1));
            float x=(point.x/point.w*.5F+.5F)*mc.getWindow().getWidth();
            float y=(.5F-point.y/point.w*.5F)*mc.getWindow().getHeight();
            if(!Float.isFinite(x)||!Float.isFinite(y)||point.w<=0)throw new AssertionError("Invalid native entity projection");
            minX=Math.min(minX,x);maxX=Math.max(maxX,x);minY=Math.min(minY,y);maxY=Math.max(maxY,y);
        }
        projectedBounds=minX+","+minY+","+maxX+","+maxY;
    }
    private static void finish(String text) {
        finished=true;
        try {java.nio.file.Files.writeString(Path.of(LABEL+"-result.txt"),text);}
        catch(java.io.IOException error){throw new RuntimeException(error);}
        Minecraft.getInstance().stop();
    }
}
