package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.HudConfig;
import cn.blockforge.generated.generatedmod.SpiritEnchantment;
import com.google.gson.GsonBuilder;
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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
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

/** Opt-in isolated GPU fixture. Existing launcher saves and profiles are never opened. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT)
public final class CloudGlyphClientCapture {
    private static final boolean ENABLED=Boolean.getBoolean("wuxingxiantu.guardGlintCapture")&&!Boolean.getBoolean("wuxingxiantu.talismanCapture")&&!Boolean.getBoolean("wuxingxiantu.lookbackCapture")&&!Boolean.getBoolean("wuxingxiantu.firecoilCapture");
    private static final boolean TIER_CAPTURE=Boolean.getBoolean("wuxingxiantu.guardTierCapture");
    private static final boolean CLOUD_CAPTURE=Boolean.getBoolean("wuxingxiantu.cloudOnlyCapture");
    private static final boolean CLOUD_MOTION=CLOUD_CAPTURE&&Boolean.getBoolean("wuxingxiantu.cloudMotionCapture");
    private static final boolean FURNACE_CAPTURE=Boolean.getBoolean("wuxingxiantu.furnaceOnlyCapture");
    private static final boolean FURNACE_MOTION=FURNACE_CAPTURE&&Boolean.getBoolean("wuxingxiantu.furnaceMotionCapture");
    private static final Path OUTPUT=Path.of(System.getProperty("wuxingxiantu.guardGlintOutput",
            TIER_CAPTURE?"../../reports/guard-tier-client":"../../reports/cloud-glyph-client"));
    private static final long START=System.nanoTime();
    private static final EquipmentSlot[] SLOTS={EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET};
    private static final List<Map<String,Object>> CAPTURES=new ArrayList<>();
    private static final List<String> FAILURES=new ArrayList<>();
    private static CompletableFuture<?> preparation,reload;
    private static boolean opened,prepared,finished,waitingHit,wall,selfView,inventoryPreview;
    private static int tick,cameraId=-1,hitPhase,hitCaptures;
    private static String pending,view="front";
    private static double previousHit=-1000,observedHit=-1000;
    private static int initialShaderIdentity,reloadedShaderIdentity;
    private static net.minecraft.world.phys.Vec3 cloudDrivenPosition;
    private static int lastCloudMotionStep=-1,cloudMotionFrames,cloudFastHistoryFrames;
    private static double cloudTravel,cloudMaxAnchorError,cloudMaxFutureSample,cloudMinOrigin=Double.POSITIVE_INFINITY,cloudMaxOrigin=Double.NEGATIVE_INFINITY;
    private static boolean cloudTeleportCleared;
    @SubscribeEvent public static void maintainCaptureTarget(TickEvent.ClientTickEvent event){
        if(!ENABLED||finished||event.phase!=TickEvent.Phase.START)return;
        var mc=Minecraft.getInstance();
        if(opened&&(mc.getMainRenderTarget().width!=1280||mc.getMainRenderTarget().height!=900)){
            // Hidden native windows can report 1x1 after a compositor resize.
            // Render an actual full-size offscreen game target, never accept its thumbnail.
            mc.getWindow().setWidth(1280);mc.getWindow().setHeight(900);mc.resizeDisplay();
        }
    }

    /** Move after vanilla flight input, before the production END history sampler.
     * The fixture drives actual position, not a velocity which vanilla may overwrite. */
    @SubscribeEvent(priority=net.minecraftforge.eventbus.api.EventPriority.HIGH)
    public static void driveCloudMotion(TickEvent.ClientTickEvent event){
        if(!ENABLED||!CLOUD_MOTION||finished||event.phase!=TickEvent.Phase.END||!prepared||preparation!=null||pending!=null)return;
        var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null||mc.getSingleplayerServer()==null)return;
        int step=tick+1;if(step<55||step>149||lastCloudMotionStep==step)return;lastCloudMotionStep=step;
        var p=mc.player;if(cloudDrivenPosition==null)cloudDrivenPosition=p.position();
        net.minecraft.world.phys.Vec3 velocity=net.minecraft.world.phys.Vec3.ZERO;
        if(step>=60&&step<100){double speed=step<80?2.5+(step-60)*.8:18;velocity=new net.minecraft.world.phys.Vec3(speed,0,0);}
        else if(step>=100&&step<120){double angle=(step-100)*Math.PI/40;velocity=new net.minecraft.world.phys.Vec3(Math.cos(angle)*18,0,Math.sin(angle)*18);}
        else if(step>=120&&step<133)velocity=new net.minecraft.world.phys.Vec3(0,0,24);
        var previous=cloudDrivenPosition;cloudDrivenPosition=step==135?new net.minecraft.world.phys.Vec3(-300.5,221,-300.5):previous.add(velocity);
        if(step!=135)cloudTravel+=velocity.length();else previous=cloudDrivenPosition;
        p.setNoGravity(true);p.noPhysics=true;p.setPos(cloudDrivenPosition);
        // Entity.getPosition and Camera.setup must share exactly this tick pair.
        p.xo=previous.x;p.yo=previous.y;p.zo=previous.z;
        p.xOld=previous.x;p.yOld=previous.y;p.zOld=previous.z;p.setDeltaMovement(velocity);
        var position=cloudDrivenPosition;var movement=velocity;var id=p.getUUID();
        mc.getSingleplayerServer().execute(()->{var serverPlayer=mc.getSingleplayerServer().getPlayerList().getPlayer(id);if(serverPlayer!=null){serverPlayer.setNoGravity(true);serverPlayer.setPos(position);serverPlayer.setDeltaMovement(movement);}});
    }

    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event) {
        if(!ENABLED||finished||event.phase!=TickEvent.Phase.END)return;
        var mc=Minecraft.getInstance();
        try {
            Files.createDirectories(OUTPUT);
            if((System.nanoTime()-START)/1_000_000_000>300)throw new AssertionError("Cloud glyph capture timed out");
            mc.setWindowActive(true);mc.options.pauseOnLostFocus=false;
            if(!opened&&mc.player!=null)throw new AssertionError("Fixture refuses to adopt an existing world");
            if(!opened&&mc.getOverlay()==null&&(mc.screen instanceof net.minecraft.client.gui.screens.TitleScreen
                    ||mc.screen instanceof net.minecraft.client.gui.screens.AccessibilityOnboardingScreen)) {
                opened=true;
                Files.deleteIfExists(OUTPUT.resolve("failure.txt"));Files.deleteIfExists(OUTPUT.resolve("complete.txt"));
                Files.deleteIfExists(OUTPUT.resolve("contacts.txt"));
                mc.getWindow().setWindowed(1280,900);mc.resizeDisplay();
                org.lwjgl.glfw.GLFW.glfwSetWindowTitle(mc.getWindow().getWindow(),"cloud-glyph-test (isolated)");
                // Hidden GLFW windows retain their framebuffer and render loop; no minimize/iconify call is used.
                if(!Boolean.getBoolean("wuxingxiantu.guardGlintShowWindow"))org.lwjgl.glfw.GLFW.glfwHideWindow(mc.getWindow().getWindow());
                mc.options.renderDistance().set(2);mc.options.simulationDistance().set(5);
                mc.options.cloudStatus().set(net.minecraft.client.CloudStatus.OFF);
                mc.options.framerateLimit().set(60);mc.options.fov().set(44);mc.options.bobView().set(false);
                mc.options.hideGui=true;mc.getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);
                HudConfig.SPIRIT_GUARD_VISUAL.set(true);
                var rules=new GameRules();rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false,null);
                mc.createWorldOpenFlows().createFreshLevel("cloud-glyph-test-"+System.currentTimeMillis(),
                    new LevelSettings("Cloud glyph shader verification",GameType.SURVIVAL,false,net.minecraft.world.Difficulty.NORMAL,true,rules,WorldDataConfiguration.DEFAULT),
                    new WorldOptions(7215,false,false),net.minecraft.world.level.levelgen.presets.WorldPresets::createNormalWorldDimensions);
            }
            if(!opened||mc.player==null||mc.level==null||mc.getSingleplayerServer()==null
                    ||mc.screen instanceof net.minecraft.client.gui.screens.ReceivingLevelScreen)return;
            if(mc.getOverlay()!=null)return;
            if(mc.screen!=null&&!inventoryPreview)mc.setScreen(null);
            if(!prepared) {
                prepared=true;var id=mc.player.getUUID();
                preparation=mc.getSingleplayerServer().submitAsync(()->{
                    var p=mc.getSingleplayerServer().getPlayerList().getPlayer(id);var level=p.serverLevel();
                    level.setWeatherParameters(6000,0,false,false);level.setDayTime(6000);
                    level.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false,level.getServer());
                    for(int x=-9;x<=9;x++)for(int z=-9;z<=9;z++){
                        level.setBlock(new BlockPos(x,120,z),Blocks.SMOOTH_STONE.defaultBlockState(),2);
                        // Tier previews clear the full sky column in this disposable world.
                        for(int y=121;y<(TIER_CAPTURE?level.getMaxBuildHeight():127);y++)
                            level.setBlock(new BlockPos(x,y,z),Blocks.AIR.defaultBlockState(),2);
                    }
                    p.teleportTo(level,.5,121,.5,180,0);p.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1);
                    p.getAttribute(Attributes.MAX_HEALTH).setBaseValue(80);p.setHealth(80);p.setAbsorptionAmount(0);p.removeAllEffects();
                    if(TIER_CAPTURE)equipTier(p,1,false);else equip(p,false,false,false);
                    var data=GeneratedMod.data(p);data.wuxingxiantu$setMaxMana(1_000_000);data.wuxingxiantu$setAura(1_000_000);GeneratedMod.sync(p);
                    var camera=EntityType.ARMOR_STAND.create(level);
                    camera.setPos(.5,120.45,-3.5);camera.setNoGravity(true);camera.setInvisible(true);
                    camera.setYRot(0);camera.setXRot(5);level.addFreshEntity(camera);cameraId=camera.getId();
                });return;
            }
            if(preparation!=null){if(!preparation.isDone())return;preparation.join();preparation=null;}
            if(reload!=null){if(!reload.isDone())return;reload.join();reload=null;reloadedShaderIdentity=shaderIdentity();}
            if(TIER_CAPTURE||CLOUD_CAPTURE)selfView=true;
            var camera=mc.level.getEntity(cameraId);if(camera==null&&!selfView)return;
            if(selfView){mc.setCameraEntity(mc.player);mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);}
            else {mc.setCameraEntity(camera);mc.options.setCameraType(CameraType.FIRST_PERSON);orientCamera(camera);}
            if(!FURNACE_MOTION){
                mc.player.setYRot(180);mc.player.yRotO=180;mc.player.yBodyRot=180;mc.player.yBodyRotO=180;
                mc.player.setXRot(0);mc.player.xRotO=0;mc.player.yHeadRot=180;mc.player.yHeadRotO=180;
            }
            if(pending!=null)return; // Do not skip captures if several ticks arrive between rendered frames.
            tick++;
            if(FURNACE_CAPTURE){furnaceTick(mc);return;}
            if(CLOUD_CAPTURE){cloudTick(mc);return;}
            if(TIER_CAPTURE){tierTick(mc);return;}
            if(tick==40)pending="01-iron-plain-front.png";
            if(tick==50)submitEquipment(true,false,false);
            if(tick==70)initialShaderIdentity=shaderIdentity();
            if(tick>=80&&tick<=120&&tick%8==0)pending="iron-cloud-front-"+tick+".png";
            if(tick==130)view="back";
            if(tick==144||tick==152||tick==160)pending="iron-cloud-back-"+tick+".png";
            if(tick==170)view="left";
            if(tick==184||tick==192)pending="iron-cloud-left-"+tick+".png";
            if(tick==200)view="right";
            if(tick==214||tick==222)pending="iron-cloud-right-"+tick+".png";
            if(tick==230){view="front";submitEquipment(true,false,true);}
            if(tick==248)pending="iron-mixed-slots-front.png";
            if(tick==260)submitEquipment(true,true,false);
            if(tick==280||tick==288||tick==296)pending="netherite-cloud-front-"+tick+".png";
            if(tick==310)startContact(false);
            if(tick==365)startContact(true);
            observeContact(mc);
            if(tick==420)pending="netherite-hit-expired.png";
            if(tick==430){wall=true;submitWall(true);}
            if(tick==448)pending="wall-occlusion-cloud.png";
            if(tick==450)HudConfig.SPIRIT_GUARD_VISUAL.set(false);
            if(tick==458)pending="wall-occlusion-off.png";
            if(tick==460){HudConfig.SPIRIT_GUARD_VISUAL.set(true);wall=false;submitWall(false);}
            if(tick==480)reload=mc.reloadResourcePacks();
            if(tick==500||tick==508)pending="resource-reload-cloud-"+tick+".png";
            if(tick==520)preparation=mc.getSingleplayerServer().submitAsync(()->mc.getSingleplayerServer().overworld().setDayTime(18000));
            if(tick==540||tick==548)pending="night-cloud-"+tick+".png";
            if(tick==550)HudConfig.SPIRIT_GUARD_VISUAL.set(false);
            if(tick==558)pending="night-visual-off.png";
            if(tick==560)HudConfig.SPIRIT_GUARD_VISUAL.set(true);
            if(tick==565){
                if(CAPTURES.size()!=30)throw new AssertionError("Incomplete capture set: "+CAPTURES.size()+" / 30");
                if(initialShaderIdentity==0||reloadedShaderIdentity==0)throw new AssertionError("Cloud shader was not registered across resource reload");
                if(initialShaderIdentity==reloadedShaderIdentity)throw new AssertionError("Resource reload did not replace the shader instance");
                if(hitPhase!=2||waitingHit)throw new AssertionError("Two genuine hit contacts were not observed");
            }
            // Use Minecraft's ordinary F5 player render path as well as the fixed camera.
            if(tick==566){
                selfView=true;submitEquipment(true,false,false);
                var server=mc.getSingleplayerServer();preparation=server.submitAsync(()->server.overworld().setDayTime(6000));
            }
            if(tick==576)pending="normal-f5-cloud.png";
            if(tick==578)HudConfig.SPIRIT_GUARD_VISUAL.set(false);
            if(tick==586)pending="normal-f5-visual-off.png";
            if(tick==588)HudConfig.SPIRIT_GUARD_VISUAL.set(true);
            if(tick==590){inventoryPreview=true;mc.setScreen(new net.minecraft.client.gui.screens.inventory.InventoryScreen(mc.player));}
            if(tick==598)pending="inventory-cloud.png";
            if(tick==600)HudConfig.SPIRIT_GUARD_VISUAL.set(false);
            if(tick==608)pending="inventory-visual-off.png";
            if(tick==610){inventoryPreview=false;mc.setScreen(null);HudConfig.SPIRIT_GUARD_VISUAL.set(true);}
            if(tick==616)pending="movement-before.png";
            if(tick==618)mc.options.keyRight.setDown(true);
            if(tick==624)pending="movement-during.png";
            if(tick==626)mc.options.keyRight.setDown(false);
            if(tick==628)pending="movement-stop-2.png";
            if(tick==630)pending="movement-stop-4.png";
            if(tick==638)pending="movement-stop-12.png";
            if(tick==645){
                if(CAPTURES.size()!=39)throw new AssertionError("Incomplete extended capture set: "+CAPTURES.size()+" / 39");
                finish();
            }
        }catch(Throwable error){FAILURES.add(error.toString());com.mojang.logging.LogUtils.getLogger().error("CLOUD_GLYPH_CAPTURE",error);finish();}
    }

    private static void equip(net.minecraft.server.level.ServerPlayer player,boolean enchanted,boolean netherite,boolean bootsOnly){
        var items=netherite?new net.minecraft.world.item.Item[]{Items.NETHERITE_HELMET,Items.NETHERITE_CHESTPLATE,Items.NETHERITE_LEGGINGS,Items.NETHERITE_BOOTS}
            :new net.minecraft.world.item.Item[]{Items.IRON_HELMET,Items.IRON_CHESTPLATE,Items.IRON_LEGGINGS,Items.IRON_BOOTS};
        for(int i=0;i<SLOTS.length;i++){
            var stack=new ItemStack(items[i]);
            if(enchanted&&(!bootsOnly||SLOTS[i]==EquipmentSlot.FEET))stack.enchant(SpiritEnchantment.SPIRIT_GUARD.get(),3);
            // A regular enchantment must retain vanilla purple foil alongside guard boots.
            if(bootsOnly&&SLOTS[i]==EquipmentSlot.LEGS)stack.enchant(net.minecraft.world.item.enchantment.Enchantments.ALL_DAMAGE_PROTECTION,2);
            player.setItemSlot(SLOTS[i],stack);
        }
    }
    private static void submitEquipment(boolean enchanted,boolean netherite,boolean bootsOnly){
        var mc=Minecraft.getInstance();var id=mc.player.getUUID();
        preparation=mc.getSingleplayerServer().submitAsync(()->equip(mc.getSingleplayerServer().getPlayerList().getPlayer(id),enchanted,netherite,bootsOnly));
    }
    private static void cloudTick(Minecraft mc){
        if(CLOUD_MOTION)mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
        if(tick==40)pending="cloud-before.png";
        int disable=CLOUD_MOTION?150:140;
        if(tick==50||tick==disable){boolean enable=tick==50;var id=mc.player.getUUID();preparation=mc.getSingleplayerServer().submitAsync(()->{var p=mc.getSingleplayerServer().getPlayerList().getPlayer(id);var d=GeneratedMod.data(p);d.wuxingxiantu$setRealm(8);d.wuxingxiantu$learnDivineArt("jin_dou_yun");d.wuxingxiantu$setEquippedDivineArt("jin_dou_yun");d.wuxingxiantu$setActiveDivineArt(enable?"jin_dou_yun":"");GeneratedMod.sync(p);});}
        if(CLOUD_MOTION){
            if(tick==70||tick==82||tick==83||tick==90||tick==100||tick==110||tick==120||tick==130||tick==134||tick==135||tick==138||tick==145)pending="cloud-active-"+tick+".png";
            if(tick==170)pending="cloud-after.png";
            if(tick==180){
                if(CAPTURES.size()!=14||cloudTravel<1000||cloudMotionFrames<30||cloudFastHistoryFrames<10||cloudMaxOrigin-cloudMinOrigin<256||!cloudTeleportCleared)
                    throw new AssertionError("Real motion incomplete: captures="+CAPTURES.size()+", travel="+cloudTravel+", frames="+cloudMotionFrames+", fastHistory="+cloudFastHistoryFrames+", origins="+cloudMinOrigin+".."+cloudMaxOrigin+", teleportClear="+cloudTeleportCleared);
                finish();
            }
            return;
        }
        if(tick==70||tick==100||tick==130)pending="cloud-active-"+tick+".png";
        if(tick==160)pending="cloud-after.png";
        if(tick==170){if(CAPTURES.size()!=5)throw new AssertionError("Cloud sequence incomplete");finish();}
    }
    private static void furnaceTick(Minecraft mc){
        if(tick==10){var id=mc.player.getUUID();preparation=mc.getSingleplayerServer().submitAsync(()->{
            var p=mc.getSingleplayerServer().getPlayerList().getPlayer(id);p.teleportTo(6.5,121,.5);var level=p.serverLevel();
            var core=new net.minecraft.core.BlockPos(0,121,0);
            for(int x=-2;x<=2;x++)for(int z=-2;z<=2;z++)for(int y=0;y<=3;y++){
                var state=cn.blockforge.generated.generatedmod.AlchemyFurnaceStructure.expected(x,z,y);
                level.setBlock(core.offset(x,y,z),state==null?cn.blockforge.generated.generatedmod.WuXingBlocks.ALCHEMY_FURNACE_LOW.get().defaultBlockState():state,3);
            }
            cn.blockforge.generated.generatedmod.AlchemyFurnaceStructure.refreshAround(level,core);
            if(Boolean.getBoolean("wuxingxiantu.furnaceDensityCapture"))for(int x=-6;x<=6;x++)for(int z=-6;z<=6;z++)
                level.setBlock(core.offset(x,-1,z),Blocks.GRASS_BLOCK.defaultBlockState(),3);
            if(FURNACE_MOTION)cn.blockforge.generated.generatedmod.client.furnacevolume.FurnaceMotionFixture.prepareWorld(level,core);
            var camera=level.getEntity(cameraId);camera.setPos(.5,121.5,-8.5);camera.setXRot(7);
        });}
        if(Boolean.getBoolean("wuxingxiantu.furnaceWarningCapture")) {
            if(tick==25){var id=mc.player.getUUID();preparation=mc.getSingleplayerServer().submitAsync(()->{
                var level=mc.getSingleplayerServer().getPlayerList().getPlayer(id).serverLevel();var camera=level.getEntity(cameraId);
                camera.setPos(1.5,123.6,-3.15);camera.setYRot(35);camera.setXRot(0);
            });}
            if(tick==35)pending="dragon-before.png";
            if(tick==40){var id=mc.player.getUUID();preparation=mc.getSingleplayerServer().submitAsync(()->{
                var owner=mc.getSingleplayerServer().getPlayerList().getPlayer(id);var be=(cn.blockforge.generated.generatedmod.AlchemyFurnaceBlockEntity)owner.serverLevel().getBlockEntity(new BlockPos(0,121,0));
                be.setItem(8,cn.blockforge.generated.generatedmod.fire.FireBottleData.fresh(1));be.setItem(0,new ItemStack(Items.DIRT));
                if(!be.beginExplosionWarning(owner))throw new AssertionError("Warning not started");
            });}
            if(tick==48||tick==65||tick==85)pending="dragon-warning-"+tick+".png";
            if(tick==108)pending="dragon-after.png";
            if(tick==115){if(CAPTURES.size()!=5)throw new AssertionError("Warning captures incomplete");finish();}
            return;
        }
        if((FURNACE_MOTION&&tick==40)||(!FURNACE_MOTION&&tick>=40&&tick<=280&&(tick-40)%30==0)){int tier=FURNACE_MOTION?1:(tick-40)/30+1;var id=mc.player.getUUID();preparation=mc.getSingleplayerServer().submitAsync(()->{
            var level=mc.getSingleplayerServer().getPlayerList().getPlayer(id).serverLevel();var core=new net.minecraft.core.BlockPos(0,121,0);
            var state=level.getBlockState(core);level.setBlock(core,state.setValue(cn.blockforge.generated.generatedmod.AlchemyFurnaceBlock.FORMED,true),3);
            var be=(cn.blockforge.generated.generatedmod.AlchemyFurnaceBlockEntity)level.getBlockEntity(core);
            try{var field=be.getClass().getDeclaredField("brewing");field.setAccessible(true);field.setBoolean(be,false);}catch(Exception e){throw new RuntimeException(e);}
            be.setItem(cn.blockforge.generated.generatedmod.AlchemyFurnaceBlockEntity.IGNITION_SLOT,cn.blockforge.generated.generatedmod.fire.FireBottleData.fresh(tier));
            if(Boolean.getBoolean("wuxingxiantu.furnaceUpperCapture")){
                var materials=new net.minecraft.world.item.Item[]{Items.WHEAT,Items.SUGAR,Items.AMETHYST_SHARD,Items.GLOWSTONE_DUST,Items.PAPER,Items.REDSTONE};
                for(int slot=0;slot<materials.length;slot++)be.setItem(slot,new ItemStack(materials[slot]));
                int elapsed=switch((tier-1)%3){case 0->320;case 1->1680;default->3440;};
                be.startBrewing(level.getGameTime()-elapsed,level.getGameTime()+4000-elapsed);
            }else be.startBrewing(level.getGameTime()-1000,level.getGameTime()+3000);
            var jobs=cn.blockforge.generated.generatedmod.network.service.CraftingJobs.get(level.getServer());
            jobs.remove(cn.blockforge.generated.generatedmod.network.service.CraftingJobs.Kind.ALCHEMY,new cn.blockforge.generated.generatedmod.network.service.CraftingJobs.JobKey(level.dimension(),core));
            jobs.add(new cn.blockforge.generated.generatedmod.network.service.CraftingJobs.PendingAlchemy(id,level.dimension(),core,be.finishTick(),new ItemStack(Items.APPLE),be.brewingRequestId(),cn.blockforge.generated.generatedmod.fire.FireBottleData.initialize(be.getItem(cn.blockforge.generated.generatedmod.AlchemyFurnaceBlockEntity.IGNITION_SLOT)).getUUID("BottleId"),tier,new double[8],new net.minecraft.resources.ResourceLocation("wuxingxiantu","root_awakening_pill"),1,0));
        });}
        if(FURNACE_MOTION){
            var update=cn.blockforge.generated.generatedmod.client.furnacevolume.FurnaceMotionFixture.tick(mc,tick,cameraId);
            if(update.preparation()!=null)preparation=update.preparation();
            if(update.capture()!=null)pending=update.capture();
            if(update.complete()){
                cn.blockforge.generated.generatedmod.client.furnacevolume.FurnaceMotionFixture.validate(CAPTURES.size());
                finish();
            }
            return;
        }
        if(tick>=58&&tick<=298&&(tick-58)%30==0)pending="furnace-tier-"+((tick-58)/30+1)+".png";
        if(tick==310||tick==325)pending="furnace-nirvana-"+tick+".png";
        if(tick==340){
            if(CAPTURES.size()!=11)throw new AssertionError("Furnace capture incomplete");
            if(Boolean.getBoolean("wuxingxiantu.furnaceUpperCapture"))preparation=mc.getSingleplayerServer().submitAsync(()->{
                var level=mc.getSingleplayerServer().overworld();var core=new net.minecraft.core.BlockPos(0,121,0);
                ((cn.blockforge.generated.generatedmod.AlchemyFurnaceBlockEntity)level.getBlockEntity(core)).finishBrewing(false);
                cn.blockforge.generated.generatedmod.network.service.CraftingJobs.get(level.getServer()).remove(cn.blockforge.generated.generatedmod.network.service.CraftingJobs.Kind.ALCHEMY,new cn.blockforge.generated.generatedmod.network.service.CraftingJobs.JobKey(level.dimension(),core));
            });else finish();
        }
        if(tick==350&&Boolean.getBoolean("wuxingxiantu.furnaceUpperCapture")){
            int bottom=cn.blockforge.generated.generatedmod.client.furnacevolume.FurnaceFireVolume.draws(),upper=cn.blockforge.generated.generatedmod.client.furnacevolume.FurnaceFireVolume.upperDraws();
            if(bottom!=0||upper!=0)throw new AssertionError("Stopped furnace retained volume owners: "+bottom+"/"+upper);
            try{Files.writeString(OUTPUT.resolve("stopped.json"),"{\"bottomDraws\":0,\"upperDraws\":0,\"actualBrewingStopped\":true}");}catch(java.io.IOException failure){throw new RuntimeException(failure);}finish();
        }
    }
    private static void equipTier(net.minecraft.server.level.ServerPlayer player,int tier,boolean mixed){
        var items=new net.minecraft.world.item.Item[]{Items.IRON_HELMET,Items.IRON_CHESTPLATE,Items.IRON_LEGGINGS,Items.IRON_BOOTS};
        for(int i=0;i<SLOTS.length;i++){
            var stack=new ItemStack(items[i]);stack.enchant(SpiritEnchantment.SPIRIT_GUARD.get(),mixed?i+1:tier);
            player.setItemSlot(SLOTS[i],stack);
        }
        // Advance by complete shader clock wraps: compare equal world-time phases
        // without changing uniforms or invoking the renderer from the fixture.
        long clock=6000L+(tier-1)*86400L;
        player.serverLevel().getServer().getWorldData().overworldData().setGameTime(clock);
        player.serverLevel().setDayTime(6000);
        player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTimePacket(clock,6000,false));
    }
    private static void submitTier(int tier,boolean mixed){
        var mc=Minecraft.getInstance();var id=mc.player.getUUID();
        preparation=mc.getSingleplayerServer().submitAsync(()->equipTier(mc.getSingleplayerServer().getPlayerList().getPlayer(id),tier,mixed));
    }
    private static void tierTick(Minecraft mc)throws Exception{
        if(tick==40){submitTier(1,false);initialShaderIdentity=shaderIdentity();}
        if(tick==90)submitTier(2,false);
        if(tick==140)submitTier(3,false);
        if(tick==190)submitTier(4,false);
        for(int tier=1;tier<=4;tier++){
            int start=40+(tier-1)*50;
            if(tick==start+20)pending="tier-"+tier+"-dynamic-a.png";
            if(tick==start+28)pending="tier-"+tier+"-dynamic-b.png";
            if(tick==start+30)startContact(false);
        }
        if(waitingHit){
            double current=hitTime();
            if(observedHit<0&&current>previousHit){
                observedHit=current;
                Files.writeString(OUTPUT.resolve("contacts.txt"),"tier="+hitPhase+" tick="+tick+" hit="+current+"\n",StandardOpenOption.CREATE,StandardOpenOption.APPEND);
            }
            if(observedHit>=0&&mc.level.getGameTime()-observedHit>=6){
                pending="tier-"+hitPhase+"-hit-peak.png";waitingHit=false;
            }
            if(tick==40+(hitPhase-1)*50+46&&observedHit<0)throw new AssertionError("No production tier hit packet "+hitPhase);
        }
        if(tick==240)submitTier(5,true);
        if(tick==260)pending="mixed-slots-1-2-3-4.png";
        if(tick==280){
            if(CAPTURES.size()!=13||hitPhase!=4||waitingHit||initialShaderIdentity==0)
                throw new AssertionError("Incomplete tier verification: captures="+CAPTURES.size()+", hits="+hitPhase);
            finish();
        }
    }
    private static void orientCamera(net.minecraft.world.entity.Entity camera){
        if(FURNACE_MOTION){cn.blockforge.generated.generatedmod.client.furnacevolume.FurnaceMotionFixture.orient(Minecraft.getInstance(),tick,camera);return;}
        if(FURNACE_CAPTURE){
            if(Boolean.getBoolean("wuxingxiantu.furnaceWarningCapture")){
                camera.setPos(1.5,122,-3.15);camera.xOld=camera.xo=1.5;camera.yOld=camera.yo=122;camera.zOld=camera.zo=-3.15;
                camera.setYRot(35);camera.yRotO=35;camera.setXRot(0);camera.xRotO=0;return;
            }
            boolean close=Boolean.getBoolean("wuxingxiantu.furnaceNearCapture");double y=close?121:121.5,z=close?-2.5:-8.5;float pitch=close?32:7;
            camera.setPos(.5,y,z);camera.xOld=camera.xo=.5;camera.yOld=camera.yo=y;camera.zOld=camera.zo=z;camera.setYRot(0);camera.yRotO=0;camera.setXRot(pitch);camera.xRotO=pitch;return;
        }
        double x=.5,z=-3.5;float yaw=0;
        switch(view){case "back"->{z=4.5;yaw=180;}case "left"->{x=-3.5;z=.5;yaw=-90;}case "right"->{x=4.5;z=.5;yaw=90;}default->{}}
        camera.setPos(x,120.45,z);camera.xOld=camera.xo=x;camera.yOld=camera.yo=120.45;camera.zOld=camera.zo=z;
        camera.setYRot(yaw);camera.yRotO=yaw;camera.setXRot(5);camera.xRotO=5;
    }
    private static void submitWall(boolean build){
        var mc=Minecraft.getInstance();
        preparation=mc.getSingleplayerServer().submitAsync(()->{
            var level=mc.getSingleplayerServer().overworld();
            for(int x=-2;x<=2;x++)for(int y=121;y<=124;y++)level.setBlock(new BlockPos(x,y,-1),
                (build?Blocks.STONE:Blocks.AIR).defaultBlockState(),3);
        });
    }
    private static double hitTime()throws Exception{
        var field=SpiritGuardLayer.class.getDeclaredField("HIT_TIMES");field.setAccessible(true);
        var state=((Map<?,?>)field.get(null)).get(Minecraft.getInstance().player);
        return state instanceof Number value?value.doubleValue():-1000;
    }
    private static void startContact(boolean arrow)throws Exception{
        var mc=Minecraft.getInstance();previousHit=hitTime();observedHit=-1000;hitCaptures=0;waitingHit=true;hitPhase++;
        var id=mc.player.getUUID();
        preparation=mc.getSingleplayerServer().submitAsync(()->{
            var p=mc.getSingleplayerServer().getPlayerList().getPlayer(id);var level=p.serverLevel();p.invulnerableTime=0;p.setAbsorptionAmount(0);
            // Start beyond the camera stand so the genuine projectile hits the
            // player instead of the invisible camera anchor's collision box.
            if(arrow){var projectile=new net.minecraft.world.entity.projectile.Arrow(level,.5,121.9,-2.5);
                projectile.setDeltaMovement(0,0,1.8);projectile.setBaseDamage(2);projectile.setNoGravity(true);level.addFreshEntity(projectile);}
            else {var attacker=EntityType.HUSK.create(level);attacker.setPos(.5,121,-2);p.hurt(level.damageSources().mobAttack(attacker),3);}
        });
    }
    private static void observeContact(Minecraft mc)throws Exception{
        if(!waitingHit)return;
        double current=hitTime();
        if(observedHit<0&&current>previousHit){observedHit=current;
            Files.writeString(OUTPUT.resolve("contacts.txt"),"phase="+hitPhase+" tick="+tick+" hit="+current+"\n",StandardOpenOption.CREATE,StandardOpenOption.APPEND);}
        if(observedHit>=0){double age=mc.level.getGameTime()-observedHit;
            if(age>=2&&(hitCaptures&1)==0){pending="impact-"+hitPhase+"-early.png";hitCaptures|=1;}
            else if(age>=6&&(hitCaptures&2)==0){pending="impact-"+hitPhase+"-peak.png";hitCaptures|=2;waitingHit=false;}
        }
        if((hitPhase==1&&tick==345||hitPhase==2&&tick==400)&&observedHit<0)throw new AssertionError("No production hit packet for phase "+hitPhase);
    }
    private static int shaderIdentity(){
        try{
            var renderer=Class.forName("cn.blockforge.generated.generatedmod.client.SpiritGuardArmorRenderer");
            for(var type:renderer.getDeclaredClasses())for(var field:type.getDeclaredFields())
                if(net.minecraft.client.renderer.ShaderInstance.class.isAssignableFrom(field.getType())){field.setAccessible(true);return System.identityHashCode(field.get(null));}
            for(var field:renderer.getDeclaredFields())if(net.minecraft.client.renderer.ShaderInstance.class.isAssignableFrom(field.getType())){field.setAccessible(true);return System.identityHashCode(field.get(null));}
            var type=Class.forName("cn.blockforge.generated.generatedmod.client.SpiritGuardArmorRenderType");
            for(var field:type.getDeclaredFields())if(net.minecraft.client.renderer.ShaderInstance.class.isAssignableFrom(field.getType())){field.setAccessible(true);return System.identityHashCode(field.get(null));}
        }catch(ReflectiveOperationException ignored){}
        return 0;
    }
    @SubscribeEvent(priority=net.minecraftforge.eventbus.api.EventPriority.LOWEST) public static void frame(TickEvent.RenderTickEvent event){
        if(!ENABLED||finished||event.phase!=TickEvent.Phase.END)return;
        try{
            var mc=Minecraft.getInstance();if(mc.getOverlay()!=null||mc.screen!=null&&!inventoryPreview)return;
            if(CLOUD_MOTION)observeCloudMotion(mc);
            if(pending==null)return;
            if(mc.getMainRenderTarget().width!=1280||mc.getMainRenderTarget().height!=900)throw new AssertionError("Capture target was resized away from 1280x900");
            int error=org.lwjgl.opengl.GL11.glGetError();if(error!=0)throw new AssertionError("OpenGL error "+error);
            String name=pending;pending=null;
            try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(OUTPUT.resolve(name));}
            var row=new LinkedHashMap<String,Object>();row.put("name",name);row.put("tick",tick);row.put("worldTime",mc.level.getGameTime());
            row.put("view",view);row.put("cameraPosition",mc.gameRenderer.getMainCamera().getPosition().toString());row.put("wall",wall);
            row.put("shaderIdentity",shaderIdentity());row.put("glError",error);row.put("hitAgeTicks",mc.level.getGameTime()-hitTime());
            row.put("selfF5View",selfView);row.put("inventoryPreview",inventoryPreview);row.put("playerPosition",mc.player.position().toString());
            row.put("shaderClockWrapped",mc.level.getGameTime()%86400);row.put("health",mc.player.getHealth());
            row.put("canRender",SpiritGuardArmorRenderer.canRender(mc.player,0));
            row.put("worldDraws",SpiritGuardArmorRenderer.worldDraws);row.put("guiDraws",SpiritGuardArmorRenderer.guiDraws);
            row.put("capturedVertices",SpiritGuardArmorRenderer.capturedVertices);row.put("bufferClass",SpiritGuardArmorRenderer.bufferClass);
            row.put("nativeFoilVertices",SpiritGuardArmorRenderer.nativeFoilVertices);
            if(Boolean.getBoolean("wuxingxiantu.furnaceWarningCapture")) {
                row.put("warningEyeQuads",AlchemyFurnaceEffects.warningEyeQuads);row.put("shakeDegrees",AlchemyFurnaceEffects.warningShakeDegrees);
                if(name.startsWith("dragon-warning")&&(AlchemyFurnaceEffects.warningEyeQuads!=96||AlchemyFurnaceEffects.warningShakeDegrees<=0))throw new AssertionError("Warning eyes/shake not active");
                if(name.equals("dragon-after.png")&&(AlchemyFurnaceEffects.warningEyeQuads!=0||AlchemyFurnaceEffects.warningShakeDegrees!=0))throw new AssertionError("Warning not stopped");
            }
            if(CLOUD_CAPTURE){row.put("cloudVolumes",JindouCloudRenderer.renderedVolumes);row.put("cloudActive",JindouCloudRenderState.isActive(mc.player));row.put("movement",mc.player.getDeltaMovement().toString());row.put("cloudMotion",JindouCloudRenderer.motionSnapshot);if(name.startsWith("cloud-active")&&JindouCloudRenderer.renderedVolumes==0)throw new AssertionError("Active cloud volume did not execute: active="+JindouCloudRenderState.isActive(mc.player)+", divine="+cn.blockforge.generated.generatedmod.GeneratedMod.data(mc.player).wuxingxiantu$getActiveDivineArt()+", camera="+mc.gameRenderer.getMainCamera().getPosition()+", player="+mc.player.position()+", shader="+(JindouCloudShader.shader!=null)+", trace="+JindouCloudRenderer.renderTrace);}
            if(FURNACE_CAPTURE&&!Boolean.getBoolean("wuxingxiantu.furnaceWarningCapture")){row.put("bottomVolumeDraws",cn.blockforge.generated.generatedmod.client.furnacevolume.FurnaceFireVolume.draws());if(cn.blockforge.generated.generatedmod.client.furnacevolume.FurnaceFireVolume.draws()==0)throw new AssertionError("Bottom volume did not draw");if(Boolean.getBoolean("wuxingxiantu.furnaceUpperCapture")){row.put("upperVolumeDraws",cn.blockforge.generated.generatedmod.client.furnacevolume.FurnaceFireVolume.upperDraws());if(cn.blockforge.generated.generatedmod.client.furnacevolume.FurnaceFireVolume.upperDraws()==0)throw new AssertionError("Upper volume did not draw");}}
            if(FURNACE_MOTION)row.put("furnaceMotion",cn.blockforge.generated.generatedmod.client.furnacevolume.FurnaceMotionFixture.capture(mc,name));
            if(SpiritGuardArmorRenderer.worldDraws+SpiritGuardArmorRenderer.guiDraws>0&&SpiritGuardArmorRenderer.nativeFoilVertices==0)
                throw new AssertionError("Guard pass suppressed native enchanted armor foil");
            var levels=new LinkedHashMap<String,Integer>();for(var slot:SLOTS)levels.put(slot.getName(),EnchantmentHelper.getItemEnchantmentLevel(SpiritEnchantment.SPIRIT_GUARD.get(),mc.player.getItemBySlot(slot)));
            var foil=new LinkedHashMap<String,Boolean>();for(var slot:SLOTS)foil.put(slot.getName(),mc.player.getItemBySlot(slot).hasFoil());
            if(name.equals("01-iron-plain-front.png")&&levels.values().stream().anyMatch(value->value!=0))throw new AssertionError("Plain armor was enchanted");
            if(name.contains("cloud-front")&&levels.values().stream().anyMatch(value->value!=3))throw new AssertionError("Equipped guard state did not sync");
            if(name.equals("iron-mixed-slots-front.png")&&(levels.get("feet")!=3||levels.get("legs")!=0
                    ||!foil.get("legs")||foil.get("head")||foil.get("chest")))throw new AssertionError("Mixed-slot enchantments did not sync");
            if(TIER_CAPTURE&&name.startsWith("tier-")){
                int expected=Integer.parseInt(name.split("-")[1]);
                if(levels.values().stream().anyMatch(value->value!=expected))throw new AssertionError("Tier equipment did not sync: "+name+" "+levels);
            }
            if(name.equals("mixed-slots-1-2-3-4.png")&&(levels.get("head")!=1||levels.get("chest")!=2||levels.get("legs")!=3||levels.get("feet")!=4))
                throw new AssertionError("Per-slot tier equipment did not sync: "+levels);
            row.put("guardLevels",levels);row.put("foilSlots",foil);row.put("visualEnabled",HudConfig.SPIRIT_GUARD_VISUAL.get());CAPTURES.add(row);writeReport();
        }catch(Throwable error){FAILURES.add(error.toString());finish();}
    }
    private static void observeCloudMotion(Minecraft mc){
        var snapshot=JindouCloudRenderer.motionSnapshot;if(snapshot.isEmpty())return;
        cloudMotionFrames++;
        double error=((Number)snapshot.get("anchorError")).doubleValue();
        double future=((Number)snapshot.get("latestVisibleTime")).doubleValue()-((Number)snapshot.get("renderTime")).doubleValue();
        cloudMaxAnchorError=Math.max(cloudMaxAnchorError,error);cloudMaxFutureSample=Math.max(cloudMaxFutureSample,future);
        if(error>1e-7||future>1e-7)throw new AssertionError("Cloud precedes same-frame player/camera interpolation: "+snapshot);
        if(tick>=60&&tick<133){
            double origin=((Number)snapshot.get("renderOriginX")).doubleValue();cloudMinOrigin=Math.min(cloudMinOrigin,origin);cloudMaxOrigin=Math.max(cloudMaxOrigin,origin);
            if(((Number)snapshot.get("sampledSpeed")).doubleValue()>16&&((Number)snapshot.get("visiblePoints")).intValue()>1&&!Boolean.TRUE.equals(snapshot.get("discontinuity")))cloudFastHistoryFrames++;
        }
        if(tick>=135&&tick<=138&&mc.player.getX()<-290&&((Number)snapshot.get("storedPoints")).intValue()==0)cloudTeleportCleared=true;
    }
    private static void writeReport()throws Exception{
        var data=new LinkedHashMap<String,Object>();data.put("test",FURNACE_MOTION?"furnace-fixed-field-real-camera-motion":CLOUD_CAPTURE?(CLOUD_MOTION?"jindou-cloud-real-moving-client":"jindou-cloud-real-client"):TIER_CAPTURE?"guard-tier-real-client":"cloud-glyph-real-client");data.put("isolated",true);
        data.put("tierCapture",TIER_CAPTURE);
        if(FURNACE_MOTION)data.put("furnaceMotionValidation",cn.blockforge.generated.generatedmod.client.furnacevolume.FurnaceMotionFixture.summary());
        if(CLOUD_MOTION){var motion=new LinkedHashMap<String,Object>();motion.put("travelBlocks",cloudTravel);motion.put("observedFrames",cloudMotionFrames);motion.put("continuousFastHistoryFrames",cloudFastHistoryFrames);motion.put("maxAnchorError",cloudMaxAnchorError);motion.put("maxFutureSampleTicks",cloudMaxFutureSample);motion.put("renderOriginXMin",Double.isFinite(cloudMinOrigin)?cloudMinOrigin:null);motion.put("renderOriginXMax",Double.isFinite(cloudMaxOrigin)?cloudMaxOrigin:null);motion.put("teleportCleared",cloudTeleportCleared);data.put("cloudMotionValidation",motion);}
        data.put("initialShaderIdentity",initialShaderIdentity);data.put("reloadedShaderIdentity",reloadedShaderIdentity);
        data.put("captureCount",CAPTURES.size());data.put("captures",CAPTURES);data.put("failures",FAILURES);
        Files.writeString(OUTPUT.resolve("captures.json"),new GsonBuilder().setPrettyPrinting().create().toJson(data));
    }
    private static void finish(){
        if(finished)return;finished=true;
        try{Files.createDirectories(OUTPUT);writeReport();Files.writeString(OUTPUT.resolve(FAILURES.isEmpty()?"complete.txt":"failure.txt"),
            FAILURES.isEmpty()?(FURNACE_MOTION?"Fixed world-space bottom flame; lateral camera motion, four directions, close/far/high/low views and genuine vanilla walking bob; local projection alignment verified.\n":CLOUD_CAPTURE?(CLOUD_MOTION?"Actual acceleration, >16 blocks/tick travel, crossing 256-block origins, turns, teleport trail reset; no future samples or anchor error.\n":"Isolated Jindou cloud on/off frames; no GL errors.\n"):TIER_CAPTURE?"Isolated guard tier GPU captures completed; four production hits, mixed equipment, no GL errors.\n"
                :"Isolated cloud glyph GPU captures completed; two production hits, shader reload, no GL errors.\n"):String.join("\n",FAILURES));}
        catch(Exception error){com.mojang.logging.LogUtils.getLogger().error("CLOUD_GLYPH_REPORT",error);}
        Minecraft.getInstance().options.keyRight.setDown(false);Minecraft.getInstance().stop();
    }
}
