package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.ModEffects;
import com.google.gson.GsonBuilder;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Genuine finite-pool contacts and shatter packet GPU capture in a disposable client world. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT)
public final class ShieldShatterCapture {
    private static final boolean ENABLED=Boolean.getBoolean("wuxingxiantu.shieldShatterCapture");
    private static final Path OUTPUT=Path.of(System.getProperty("wuxingxiantu.shieldShatterOutput","../../reports/shield-shatter-client"));
    private static final long START=System.nanoTime();
    private static final int[] FRAME_TICKS={0,1,2,3,4,5,7,9,11,14,17,20,23,26,29,32,34,38,42,45,48,51,54,56};
    private static final List<Map<String,Object>> CAPTURES=new ArrayList<>(),CONTACTS=new ArrayList<>();
    private static final List<Map<String,Object>> SERVER_CONTACTS=java.util.Collections.synchronizedList(new ArrayList<>());
    private static final List<String> FAILURES=new ArrayList<>();
    private static CompletableFuture<?> preparation,reload;
    private static CompletableFuture<Integer> warmup;
    private static boolean combatReady;
    private static int combatReadyServerTick;
    private static boolean opened,prepared,finished,wall;
    private static boolean repeatOne,repeatTwo;
    private static int tick,cameraId=-1,firstFrame,wallFrame,initialShader,reloadedShader,initialShardShader,reloadedShardShader;
    private static int stage=0;
    private static String pending;
    private static double firstShatterTick=-1,secondShatterTick=-1,thirdShatterTick=-1;
    private static boolean interruptedCapture;

    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event){
        if(!ENABLED||finished||event.phase!=TickEvent.Phase.END)return;
        var mc=Minecraft.getInstance();
        try{
            Files.createDirectories(OUTPUT);
            if((System.nanoTime()-START)/1_000_000_000>300)throw new AssertionError("Shield shatter capture timeout");
            mc.setWindowActive(true);mc.options.pauseOnLostFocus=false;
            if(!opened&&mc.player!=null)throw new AssertionError("Fixture refuses to adopt an existing world");
            if(!opened&&mc.getOverlay()==null&&(mc.screen instanceof net.minecraft.client.gui.screens.TitleScreen
                    ||mc.screen instanceof net.minecraft.client.gui.screens.AccessibilityOnboardingScreen)){
                opened=true;Files.deleteIfExists(OUTPUT.resolve("failure.txt"));Files.deleteIfExists(OUTPUT.resolve("complete.txt"));
                Files.deleteIfExists(OUTPUT.resolve("contacts.txt"));
                Files.deleteIfExists(OUTPUT.resolve("server-contacts.jsonl"));
                mc.getWindow().setWindowed(1280,900);mc.resizeDisplay();
                org.lwjgl.glfw.GLFW.glfwSetWindowTitle(mc.getWindow().getWindow(),"shield-shatter-test (isolated)");
                if(!Boolean.getBoolean("wuxingxiantu.shieldShatterShowWindow"))org.lwjgl.glfw.GLFW.glfwHideWindow(mc.getWindow().getWindow());
                mc.options.renderDistance().set(2);mc.options.simulationDistance().set(5);mc.options.cloudStatus().set(net.minecraft.client.CloudStatus.OFF);
                mc.options.framerateLimit().set(60);mc.options.fov().set(48);mc.options.bobView().set(false);mc.options.hideGui=true;
                mc.getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);
                var rules=new GameRules();rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false,null);
                mc.createWorldOpenFlows().createFreshLevel("shield-shatter-test-"+System.currentTimeMillis(),
                    new LevelSettings("Shield shatter verification",GameType.SURVIVAL,false,net.minecraft.world.Difficulty.NORMAL,true,rules,WorldDataConfiguration.DEFAULT),
                    new WorldOptions(7215,false,false),net.minecraft.world.level.levelgen.presets.WorldPresets::createNormalWorldDimensions);
            }
            if(!opened||mc.player==null||mc.level==null||mc.getSingleplayerServer()==null
                    ||mc.screen instanceof net.minecraft.client.gui.screens.ReceivingLevelScreen||mc.getOverlay()!=null)return;
            if(mc.screen!=null)mc.setScreen(null);
            if(!prepared){
                prepared=true;var id=mc.player.getUUID();
                preparation=mc.getSingleplayerServer().submitAsync(()->{
                    var p=mc.getSingleplayerServer().getPlayerList().getPlayer(id);var level=p.serverLevel();
                    level.setDayTime(6000);level.setWeatherParameters(6000,0,false,false);
                    level.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false,level.getServer());
                    for(int x=-14;x<=14;x++)for(int z=-14;z<=14;z++){
                        level.setBlock(new BlockPos(x,120,z),Blocks.SMOOTH_STONE.defaultBlockState(),2);
                        for(int y=121;y<=127;y++)level.setBlock(new BlockPos(x,y,z),Blocks.AIR.defaultBlockState(),2);
                    }
                    p.teleportTo(level,.5,121,.5,180,0);p.removeAllEffects();p.setAbsorptionAmount(0);
                    p.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1);p.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);p.setHealth(100);
                    cast(p);
                    var camera=EntityType.ARMOR_STAND.create(level);camera.setNoGravity(true);camera.setInvisible(true);
                    camera.setPos(.5,120.45,-4.5);camera.setYRot(0);camera.setXRot(5);level.addFreshEntity(camera);cameraId=camera.getId();
                });return;
            }
            if(preparation!=null){if(!preparation.isDone())return;preparation.join();preparation=null;}
            // A newly joined real ServerPlayer has separate vanilla respawn damage protection.
            // Let it naturally expire instead of changing that production protection field.
            if(!combatReady){
                if(warmup==null){var id=mc.player.getUUID();warmup=mc.getSingleplayerServer().submit(()->mc.getSingleplayerServer().getPlayerList().getPlayer(id).tickCount);return;}
                if(!warmup.isDone())return;
                int serverTick=warmup.join();warmup=null;
                if(serverTick<80)return;
                combatReady=true;combatReadyServerTick=serverTick;
            }
            if(reload!=null){if(!reload.isDone())return;reload.join();reload=null;reloadedShader=System.identityHashCode(SpiritShieldRenderer.Material.shader);reloadedShardShader=shardShaderIdentity();}
            var camera=mc.level.getEntity(cameraId);if(camera==null)return;
            mc.setCameraEntity(camera);mc.options.setCameraType(CameraType.FIRST_PERSON);
            double cameraZ=wall?-12.5:-4.5;
            camera.setPos(.5,120.45,cameraZ);camera.xOld=camera.xo=.5;camera.yOld=camera.yo=120.45;camera.zOld=camera.zo=cameraZ;
            camera.setYRot(0);camera.yRotO=0;camera.setXRot(5);camera.xRotO=5;
            mc.player.setYRot(180);mc.player.yRotO=180;mc.player.yBodyRot=mc.player.yBodyRotO=180;mc.player.yHeadRot=mc.player.yHeadRotO=180;
            mc.player.setXRot(0);mc.player.xRotO=0;
            if(pending!=null)return;tick++;
            if(stage==0){
                if(tick==40){initialShader=System.identityHashCode(SpiritShieldRenderer.Material.shader);initialShardShader=shardShaderIdentity();expectPool(12);pending="01-blue-full.png";}
                if(tick==50)hurt(6);
                if(tick==65){expectPool(6);pending="02-purple-half.png";}
                if(tick==80)hurt(5);
                if(tick==95){expectPool(1);pending="03-red-low.png";}
                if(tick==110)hurt(1);
                if(tick==125){expectPool(0);if(shatterState()!=null)throw new AssertionError("Pool depletion must retain red shell until next hit");pending="04-red-empty-retained.png";}
                if(tick==140){arrow();stage=1;}
            }else if(stage==1){
                var state=shatterState();
                if(firstShatterTick<0&&state!=null){firstShatterTick=stateTick(state);writeContact("first-shatter",firstShatterTick);}
                if(firstShatterTick<0&&tick>180)throw new AssertionError("Final real arrow did not trigger shatter");
                if(firstShatterTick>=0){
                    double age=mc.level.getGameTime()-firstShatterTick;
                    if(age>=5&&!repeatOne){repeatOne=true;hurt(1);}
                    if(age>=10&&!repeatTwo){repeatTwo=true;hurt(1);}
                    if(state!=null&&Math.abs(stateTick(state)-firstShatterTick)>.01)throw new AssertionError("Repeat contacts reset shatter lifetime");
                    if(age>=62&&firstFrame>=FRAME_TICKS.length){
                        if(state!=null)throw new AssertionError("Expired fragment state was not removed");
                        pending="09-shatter-expired-clean.png";stage=4;tick=0;
                    }
                }
            }else if(stage==2){
                if(tick==20){expectPool(12);if(shatterState()!=null)throw new AssertionError("Recast retained old fragment state");pending="05-recast-blue-clean.png";}
                if(tick==30)reload=mc.reloadResourcePacks();
                if(tick==50)pending="06-resource-reload-blue.png";
                if(tick==60){submitWall(true);wall=true;}
                if(tick==78)pending="07-wall-blue.png";
                if(tick==85)hurt(12);
                if(tick==100){expectPool(0);pending="08-wall-red-empty.png";}
                if(tick==110){hurt(1);stage=3;tick=0;}
            }else if(stage==3){
                var state=shatterState();
                if(secondShatterTick<0&&state!=null){secondShatterTick=stateTick(state);writeContact("wall-shatter",secondShatterTick);}
                if(secondShatterTick<0&&tick>40)throw new AssertionError("Wall final real melee did not trigger shatter");
                if(secondShatterTick>=0&&mc.level.getGameTime()-secondShatterTick>=66&&wallFrame>=3){
                    wall=false;submitWall(false);stage=5;tick=0;
                }
            }else if(stage==4){
                if(tick==2){recast();stage=2;tick=0;}
            }else if(stage==5){
                if(tick==5)recast();
                if(tick==20){expectPool(12);hurt(12);}
                if(tick==35){expectPool(0);hurt(1);stage=6;tick=0;}
            }else if(stage==6){
                var state=shatterState();
                if(thirdShatterTick<0&&state!=null){thirdShatterTick=stateTick(state);writeContact("interrupted-shatter",thirdShatterTick);}
                if(thirdShatterTick<0&&tick>40)throw new AssertionError("Interrupted test contact did not trigger shatter");
                if(thirdShatterTick>=0){
                    double age=mc.level.getGameTime()-thirdShatterTick;
                    if(age>=4&&!interruptedCapture){interruptedCapture=true;pending="10-interrupted-shatter-active.png";}
                    if(age>=8&&interruptedCapture){if(state==null)throw new AssertionError("Burst expired before recast test");recast();stage=7;tick=0;}
                }
            }else if(stage==7){
                if(tick==15){expectPool(12);if(shatterState()!=null)throw new AssertionError("Mid-burst recast did not clear old fragments");pending="11-recast-during-break-blue-clean.png";}
                if(tick==30)pending="12-recast-settled-blue-clean.png";
                if(tick==40){
                    if(initialShader==0||reloadedShader==0||initialShader==reloadedShader)throw new AssertionError("Shader resource reload did not create a new instance");
                    if(initialShardShader==0||reloadedShardShader==0||initialShardShader==reloadedShardShader)throw new AssertionError("Shard shader resource reload did not create a new instance");
                    if(firstFrame!=FRAME_TICKS.length)throw new AssertionError("Incomplete shatter sequence "+firstFrame);
                    if(CAPTURES.size()!=39)throw new AssertionError("Incomplete screenshot set "+CAPTURES.size()+" / 39");
                    finish();
                }
            }
        }catch(Throwable error){FAILURES.add(error.toString());com.mojang.logging.LogUtils.getLogger().error("SHIELD_SHATTER_CAPTURE",error);finish();}
    }

    private static void cast(net.minecraft.server.level.ServerPlayer player){
        player.addEffect(new MobEffectInstance(ModEffects.SPIRIT_SHIELD_VISUAL.get(),1200,0,false,false,false));player.setAbsorptionAmount(12);
        var data=GeneratedMod.data(player);data.wuxingxiantu$setMaxMana(1_000_000);data.wuxingxiantu$setAura(1_000_000);GeneratedMod.sync(player);
    }
    private static void recast(){var mc=Minecraft.getInstance();var id=mc.player.getUUID();preparation=mc.getSingleplayerServer().submitAsync(()->cast(mc.getSingleplayerServer().getPlayerList().getPlayer(id)));}
    private static void hurt(float amount){
        var mc=Minecraft.getInstance();var id=mc.player.getUUID();
        preparation=mc.getSingleplayerServer().submitAsync(()->{
            var p=mc.getSingleplayerServer().getPlayerList().getPlayer(id);var attacker=EntityType.HUSK.create(p.serverLevel());
            attacker.setPos(.5,121,-2);attacker.setNoAi(true);attacker.setNoGravity(true);p.serverLevel().addFreshEntity(attacker);
            p.invulnerableTime=0;
            var row=new LinkedHashMap<String,Object>();row.put("kind","actual-server-melee");row.put("worldTime",p.level().getGameTime());row.put("serverPlayerTick",p.tickCount);
            row.put("requestedAmount",amount);row.put("poolBefore",p.getAbsorptionAmount());row.put("healthBefore",p.getHealth());row.put("effectBefore",p.hasEffect(ModEffects.SPIRIT_SHIELD_VISUAL.get()));
            boolean accepted=p.hurt(p.damageSources().mobAttack(attacker),amount);
            row.put("hurtReturned",accepted);row.put("poolAfter",p.getAbsorptionAmount());row.put("healthAfter",p.getHealth());row.put("effectAfter",p.hasEffect(ModEffects.SPIRIT_SHIELD_VISUAL.get()));
            attacker.discard();SERVER_CONTACTS.add(row);
            try{Files.writeString(OUTPUT.resolve("server-contacts.jsonl"),new GsonBuilder().create().toJson(row)+"\n",StandardOpenOption.CREATE,StandardOpenOption.APPEND);}
            catch(Exception error){throw new IllegalStateException("Cannot record actual server contact",error);}
        });
    }
    private static void arrow(){
        var mc=Minecraft.getInstance();var id=mc.player.getUUID();
        preparation=mc.getSingleplayerServer().submitAsync(()->{
            var p=mc.getSingleplayerServer().getPlayerList().getPlayer(id);p.invulnerableTime=0;
            var projectile=new net.minecraft.world.entity.projectile.Arrow(p.serverLevel(),.5,121.9,-2.5);
            // The camera armor stand is at -4.5; spawn beyond it so it cannot absorb this contact.
            projectile.setDeltaMovement(0,0,1.2);projectile.setBaseDamage(1);projectile.setNoGravity(true);p.serverLevel().addFreshEntity(projectile);
        });
    }
    private static void submitWall(boolean build){
        var mc=Minecraft.getInstance();preparation=mc.getSingleplayerServer().submitAsync(()->{
            var level=mc.getSingleplayerServer().overworld();for(int x=-8;x<=8;x++)for(int y=119;y<=130;y++)
                // Beyond the burst's first-second travel envelope: visible particles cannot simply fly to our side.
                level.setBlock(new BlockPos(x,y,-9),(build?Blocks.STONE:Blocks.AIR).defaultBlockState(),3);
        });
    }
    private static void expectPool(float pool){
        float actual=Minecraft.getInstance().player.getAbsorptionAmount();if(Math.abs(actual-pool)>.01)throw new AssertionError("Expected pool="+pool+", actual="+actual);
    }
    private static Object shatterState()throws Exception{
        Field field=SpiritShieldRenderer.class.getDeclaredField("SHATTERS");field.setAccessible(true);
        return ((Map<?,?>)field.get(null)).get(Minecraft.getInstance().player);
    }
    /** Kept reflective so acceptance code only observes production state and never becomes its source. */
    private static double stateTick(Object state)throws Exception{
        if(state instanceof Number n)return n.doubleValue();
        for(String name:new String[]{"startTick","startedAt","startTime","tick","time"}){
            try{var method=state.getClass().getDeclaredMethod(name);method.setAccessible(true);var value=method.invoke(state);if(value instanceof Number n)return n.doubleValue();}catch(NoSuchMethodException ignored){}
            try{var field=state.getClass().getDeclaredField(name);field.setAccessible(true);var value=field.get(state);if(value instanceof Number n)return n.doubleValue();}catch(NoSuchFieldException ignored){}
        }
        throw new AssertionError("No observable start tick on "+state.getClass().getName());
    }
    private static int shardShaderIdentity()throws Exception{
        var field=SpiritShieldRenderer.Material.class.getDeclaredField("shardShader");field.setAccessible(true);return System.identityHashCode(field.get(null));
    }
    private static int stateListSize(Object state,String name)throws Exception{
        if(state==null)return 0;var method=state.getClass().getDeclaredMethod(name);method.setAccessible(true);
        return ((java.util.Collection<?>)method.invoke(state)).size();
    }
    private static void writeContact(String name,double tick)throws Exception{
        var row=new LinkedHashMap<String,Object>();row.put("name",name);row.put("shatterTick",tick);row.put("observedWorldTime",Minecraft.getInstance().level.getGameTime());CONTACTS.add(row);
        Files.writeString(OUTPUT.resolve("contacts.txt"),name+" shatterTick="+tick+"\n",StandardOpenOption.CREATE,StandardOpenOption.APPEND);
    }
    @SubscribeEvent public static void frame(TickEvent.RenderTickEvent event){
        if(!ENABLED||finished||event.phase!=TickEvent.Phase.END)return;
        var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null||mc.getOverlay()!=null||mc.screen!=null)return;
        try{
            double age=firstShatterTick>=0?mc.level.getGameTime()+mc.getFrameTime()-firstShatterTick:-1;
            if(stage==1&&firstShatterTick>=0&&firstFrame<FRAME_TICKS.length&&age>=FRAME_TICKS[firstFrame]){
                pending=String.format(java.util.Locale.ROOT,"shatter-%02d-at-%02dt.png",firstFrame,FRAME_TICKS[firstFrame]);firstFrame++;
            }
            double wallAge=secondShatterTick>=0?mc.level.getGameTime()+mc.getFrameTime()-secondShatterTick:-1;
            if(stage==3&&secondShatterTick>=0&&wallFrame<3&&wallAge>=new int[]{5,20,60}[wallFrame]){
                pending="wall-shatter-"+wallFrame+".png";wallFrame++;
            }
            if(pending==null)return;
            int error=org.lwjgl.opengl.GL11.glGetError();if(error!=0)throw new AssertionError("OpenGL error "+error);
            String name=pending;pending=null;
            try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(OUTPUT.resolve(name));}
            var row=new LinkedHashMap<String,Object>();row.put("name",name);row.put("tick",tick);row.put("stage",stage);
            double captureAge=stage==3?wallAge:stage==6?mc.level.getGameTime()+mc.getFrameTime()-thirdShatterTick:age;
            row.put("worldTime",mc.level.getGameTime());row.put("partialTick",mc.getFrameTime());row.put("shatterAgeSeconds",captureAge/20);
            row.put("absorption",mc.player.getAbsorptionAmount());row.put("health",mc.player.getHealth());row.put("visualEffect",mc.player.hasEffect(ModEffects.SPIRIT_SHIELD_VISUAL.get()));
            var state=shatterState();row.put("hasShatterState",state!=null);row.put("patchCount",stateListSize(state,"patches"));row.put("sparkCount",stateListSize(state,"sparks"));
            row.put("geometryPending",state!=null&&stateListSize(state,"patches")==0);
            if(state!=null&&(stateListSize(state,"sparks")==0||(stateListSize(state,"patches")==0&&captureAge/20>.5)))
                throw new AssertionError("Shatter geometry was not prepared within its visible lifetime");
            row.put("wall",wall);row.put("glError",error);row.put("shaderIdentity",System.identityHashCode(SpiritShieldRenderer.Material.shader));row.put("shardShaderIdentity",shardShaderIdentity());
            row.put("cameraPosition",mc.gameRenderer.getMainCamera().getPosition().toString());CAPTURES.add(row);writeReport();
        }catch(Throwable error){FAILURES.add(error.toString());finish();}
    }
    private static void writeReport()throws Exception{
        var data=new LinkedHashMap<String,Object>();data.put("test","shield-shatter-real-client");data.put("isolated",true);data.put("captureCount",CAPTURES.size());
        data.put("combatReadyServerTick",combatReadyServerTick);synchronized(SERVER_CONTACTS){data.put("actualServerContacts",List.copyOf(SERVER_CONTACTS));}
        data.put("firstShatterTick",firstShatterTick);data.put("secondShatterTick",secondShatterTick);data.put("thirdShatterTick",thirdShatterTick);data.put("initialShader",initialShader);data.put("reloadedShader",reloadedShader);
        data.put("initialShardShader",initialShardShader);data.put("reloadedShardShader",reloadedShardShader);data.put("repeatHitOne",repeatOne);data.put("repeatHitTwo",repeatTwo);
        data.put("firstFrames",firstFrame);data.put("wallFrames",wallFrame);data.put("contacts",CONTACTS);data.put("captures",CAPTURES);data.put("failures",FAILURES);
        Files.writeString(OUTPUT.resolve("captures.json"),new GsonBuilder().setPrettyPrinting().create().toJson(data));
    }
    private static void finish(){
        if(finished)return;finished=true;
        try{Files.createDirectories(OUTPUT);writeReport();Files.writeString(OUTPUT.resolve(FAILURES.isEmpty()?"complete.txt":"failure.txt"),
            FAILURES.isEmpty()?"Real depletion, retained empty shell, next-contact shatter, repeat-hit lifetime, recast, wall occlusion and resource reload captured.\n":String.join("\n",FAILURES));}
        catch(Exception error){com.mojang.logging.LogUtils.getLogger().error("SHIELD_SHATTER_REPORT",error);}
        Minecraft.getInstance().stop();
    }
}
