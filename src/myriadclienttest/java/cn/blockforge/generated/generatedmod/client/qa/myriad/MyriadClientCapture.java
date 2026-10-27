package cn.blockforge.generated.generatedmod.client.qa.myriad;

import cn.blockforge.generated.generatedmod.*;
import cn.blockforge.generated.generatedmod.crafting.EquipmentCrafting;
import cn.blockforge.generated.generatedmod.artifacts.*;
import cn.blockforge.generated.generatedmod.economy.SpiritStoneGrade;
import cn.blockforge.generated.generatedmod.formation.*;
import cn.blockforge.generated.generatedmod.importedcreatures.*;
import cn.blockforge.generated.generatedmod.importedcreatures.client.BlackBoarRenderer;
import cn.blockforge.generated.generatedmod.importedcontent.*;
import cn.blockforge.generated.generatedmod.plants.*;
import cn.blockforge.generated.generatedmod.storage.*;
import cn.blockforge.generated.generatedmod.storage.client.BagScreen;
import cn.blockforge.generated.generatedmod.storage.menu.BagMenu;
import com.google.gson.GsonBuilder;
import net.minecraft.client.*;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.phys.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/** Real production captures in a new isolated world, using production menus, entities and block interactions. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT)
public final class MyriadClientCapture {
    private static final boolean ENABLED=Boolean.getBoolean("wuxingxiantu.myriadCapture");
    private static final boolean EXTERNAL=Boolean.getBoolean("wuxingxiantu.myriadExternalShaders");
    private static final boolean SHOW_WINDOW=Boolean.getBoolean("wuxingxiantu.myriadShowWindow");
    private static final String EXPECTED_PACK=System.getProperty("wuxingxiantu.myriadExpectedShaderPack","");
    private static final Path OUTPUT=Path.of(System.getProperty("wuxingxiantu.myriadCaptureOutput","captures")).resolve("run-"+System.currentTimeMillis());
    private static final BlockPos GROUND=new BlockPos(0,200,0);
    private static final Vec3 CENTER=Vec3.atBottomCenterOf(GROUND.above());
    private static final String[] CASES={"01-bag-normal","02-bag-filtered","03-two-boar-models","04-array-ground","05-array-dome","06-array-sword-streams","07-filled-bath","08-artifact-three-ranks-and-armor","09-held-vein-compass-hud","10-real-cushion-qte"};
    private static final BlockPos BATH=GROUND.above();
    private static final List<Map<String,Object>> CAPTURES=new ArrayList<>();
    private static final List<String> FAILURES=new ArrayList<>(),GL_ERRORS=new ArrayList<>();
    private static final long START=System.nanoTime();
    private static boolean opened,finished,requested,filtered,closedBag,formationRequested,shaderChecked,pointerExercised;
    private static int phase,phaseTicks,worldTicks;
    private static CompletableFuture<?> pending;
    private static Method cameraPosition;
    private static volatile long formationStart=-1;
    private static volatile UUID formationOwner;
    private static volatile int normalBoarId,kingBoarId;
    private static boolean sideContentRequested;
    private static boolean artifactContentRequested;
    private static boolean compassRequested,qteRequested,qteStarted;
    private static volatile List<Integer> artifactStandIds=List.of();
    private static volatile List<Map<String,Object>> sideContentServerEvidence=List.of();
    private static boolean shaderActive;
    private static String shaderPack="none";

    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event) {
        if(!ENABLED||finished||event.phase!=TickEvent.Phase.END)return;
        var minecraft=Minecraft.getInstance();
        try {
            Files.createDirectories(OUTPUT);minecraft.setWindowActive(true);minecraft.options.pauseOnLostFocus=false;
            if(!SHOW_WINDOW)GLFW.glfwHideWindow(minecraft.getWindow().getWindow());
            if((System.nanoTime()-START)/1_000_000_000L>360)throw new AssertionError("Myriad client capture timeout, phase="+phase+", screen="+minecraft.screen);
            if(!opened&&minecraft.player!=null)throw new AssertionError("Refuses to adopt an existing player save");
            if(!opened&&minecraft.getOverlay()==null&&(minecraft.screen instanceof net.minecraft.client.gui.screens.TitleScreen
                    ||minecraft.screen instanceof net.minecraft.client.gui.screens.AccessibilityOnboardingScreen)) {
                opened=true;minecraft.getWindow().setWindowed(1280,900);minecraft.options.guiScale().set(2);
                minecraft.options.renderDistance().set(4);minecraft.options.simulationDistance().set(5);minecraft.options.framerateLimit().set(40);
                minecraft.options.fov().set(55);minecraft.options.bobView().set(false);minecraft.options.setCameraType(CameraType.FIRST_PERSON);
                minecraft.resizeDisplay();if(!SHOW_WINDOW)GLFW.glfwHideWindow(minecraft.getWindow().getWindow());
                minecraft.getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);
                var rules=new GameRules();rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false,null);
                rules.getRule(GameRules.RULE_DAYLIGHT).set(false,null);rules.getRule(GameRules.RULE_WEATHER_CYCLE).set(false,null);
                minecraft.createWorldOpenFlows().createFreshLevel("myriad-acceptance-"+System.currentTimeMillis(),
                        new LevelSettings("Myriad isolated acceptance",GameType.CREATIVE,false,Difficulty.NORMAL,true,rules,WorldDataConfiguration.DEFAULT),
                        new WorldOptions(7215,false,false),net.minecraft.world.level.levelgen.presets.WorldPresets::createNormalWorldDimensions);
            }
            if(!opened||minecraft.player==null||minecraft.level==null||minecraft.getSingleplayerServer()==null||minecraft.getOverlay()!=null)return;
            if(minecraft.getMainRenderTarget().width!=1280||minecraft.getMainRenderTarget().height!=900) {
                minecraft.getWindow().setWidth(1280);minecraft.getWindow().setHeight(900);minecraft.resizeDisplay();
            }
            if(!pointerExercised){minecraft.mouseHandler.grabMouse();pointerExercised=true;PointerSafety.verify();}
            if(pending!=null){if(!pending.isDone())return;pending.join();pending=null;phaseTicks=0;}
            if(!shaderChecked){checkShaderPack();shaderChecked=true;}
            phaseTicks++;
            if(phase==0) {
                if(minecraft.screen!=null||++worldTicks<30)return;
                UUID playerId=minecraft.player.getUUID();pending=minecraft.getSingleplayerServer().submit(()->prepareWorld(minecraft,playerId));phase=1;return;
            }
            if(phase==1) {
                if(minecraft.screen instanceof BagScreen&&minecraft.player.containerMenu instanceof BagMenu bag
                        &&Arrays.stream(bag.totals).sum()>0&&phaseTicks>=20)requested=true;
            } else if(phase==2) {
                if(!(minecraft.screen instanceof BagScreen screen)||!(minecraft.player.containerMenu instanceof BagMenu bag))return;
                if(!filtered){Field field=BagScreen.class.getDeclaredField("search");field.setAccessible(true);((EditBox)field.get(screen)).setValue("minecraft:diamond");filtered=true;phaseTicks=0;}
                boolean onlyDiamond=bag.totals[0]>0;
                for(int i=1;i<=54;i++)if(bag.getSlot(i).hasItem()&&!bag.getSlot(i).getItem().is(Items.DIAMOND))onlyDiamond=false;
                if(onlyDiamond&&phaseTicks>=20)requested=true;
            } else if(phase==3) {
                if(!closedBag){if(minecraft.screen!=null)minecraft.screen.onClose();minecraft.options.hideGui=true;closedBag=true;phaseTicks=0;}
                if(minecraft.screen==null&&minecraft.level.getEntity(normalBoarId)!=null&&minecraft.level.getEntity(kingBoarId)!=null&&phaseTicks>=30)requested=true;
            } else if(phase>=4&&phase<=6) {
                if(!formationRequested){UUID playerId=minecraft.player.getUUID();pending=minecraft.getSingleplayerServer().submit(()->startFormation(minecraft,playerId));formationRequested=true;return;}
                if(!clientHasFormation())return;
                long age=minecraft.level.getGameTime()-formationStart;
                if(phase==4&&age>=60){if(age>=80)throw new AssertionError("Missed natural pre-dome ground capture window: "+age);requested=true;}
                if(phase==5&&age>=130)requested=true;
                if(phase==6&&age>=210){if(age>=240)throw new AssertionError("Missed natural sword-stream window: "+age);requested=true;}
            } else if(phase==7) {
                if(!sideContentRequested){UUID playerId=minecraft.player.getUUID();pending=minecraft.getSingleplayerServer().submit(()->prepareSideContent(minecraft,playerId));sideContentRequested=true;return;}
                if(clientHasFormation()||sideContentServerEvidence.size()!=1||phaseTicks<30)return;
                var bath=minecraft.level.getBlockState(BATH);
                if(bath.is(ImportedContent.SPIRIT_BATH.get())&&bath.getValue(SpiritBathBlock.FILLED))requested=true;
            } else if(phase==8) {
                if(!artifactContentRequested){pending=minecraft.getSingleplayerServer().submit(()->prepareArtifacts(minecraft));artifactContentRequested=true;return;}
                if(artifactStandIds.size()==3&&artifactStandIds.stream().allMatch(id->minecraft.level.getEntity(id)!=null)&&phaseTicks>=30)requested=true;
            } else if(phase==9) {
                minecraft.options.hideGui=false;
                if(!compassRequested){UUID id=minecraft.player.getUUID();pending=minecraft.getSingleplayerServer().submit(()->{
                    var player=minecraft.getSingleplayerServer().getPlayerList().getPlayer(id);
                    player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(WuXingItems.SPIRIT_VEIN_COMPASS.get()));
                    player.containerMenu.broadcastChanges();
                });compassRequested=true;return;}
                Field state=Class.forName("cn.blockforge.generated.generatedmod.veincompass.client.VeinCompassHud").getDeclaredField("state");state.setAccessible(true);
                if(state.get(null)!=null&&phaseTicks>=30)requested=true;
            } else if(phase==10) {
                if(!qteRequested){UUID id=minecraft.player.getUUID();pending=minecraft.getSingleplayerServer().submit(()->{
                    var player=minecraft.getSingleplayerServer().getPlayerList().getPlayer(id);var level=player.serverLevel();
                    player.setGameMode(GameType.SURVIVAL);player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
                    player.setShiftKeyDown(false);player.getAbilities().flying=false;player.setDeltaMovement(Vec3.ZERO);
                    player.teleportTo(CENTER.x,CENTER.y,CENTER.z);var data=GeneratedMod.data(player);
                    data.wuxingxiantu$setRealm(2);data.wuxingxiantu$restoreCultivation(0);data.wuxingxiantu$setDaoXin(0);
                    var state=WuXingBlocks.MEDITATION_CUSHION.get().defaultBlockState();level.setBlock(BATH,state,3);
                    var hit=new BlockHitResult(Vec3.atCenterOf(BATH),Direction.UP,BATH,false);
                    state.use(level,player,InteractionHand.MAIN_HAND,hit);
                    GeneratedMod.sync(player);
                });qteRequested=true;return;}
                // Wait for the real seat spawn/passenger packets before sending the normal K action.
                if(!qteStarted&&minecraft.player.getVehicle() instanceof MeditationSeatEntity&&phaseTicks>=12){
                    cn.blockforge.generated.generatedmod.Network.CHANNEL.sendToServer(
                        new cn.blockforge.generated.generatedmod.meditationqte.QteActionPacket(
                            cn.blockforge.generated.generatedmod.meditationqte.QteActionPacket.START,
                            cn.blockforge.generated.generatedmod.meditationqte.QteActionPacket.NONE,0));
                    qteStarted=true;phaseTicks=0;
                }
                if(minecraft.screen instanceof cn.blockforge.generated.generatedmod.meditationqte.client.QteScreen&&phaseTicks>=12)requested=true;
            }
        }catch(Throwable error){fail("SETUP phase="+phase,error);}
    }

    private static void prepareWorld(Minecraft minecraft,UUID playerId) {
        var server=minecraft.getSingleplayerServer();var player=Objects.requireNonNull(server.getPlayerList().getPlayer(playerId));var level=player.serverLevel();
        level.setDayTime(6000);level.setWeatherParameters(6000,0,false,false);
        for(int x=-24;x<=24;x++)for(int z=-24;z<=24;z++)level.setBlock(GROUND.offset(x,0,z),Blocks.SMOOTH_STONE.defaultBlockState(),2);
        player.teleportTo(CENTER.x,CENTER.y,CENTER.z+6);player.getAbilities().flying=true;player.onUpdateAbilities();
        for(var entity:List.of(ImportedCreatures.BLACK_BOAR.get(),ImportedCreatures.BLACK_BOAR_KING.get())) {
            var boar=Objects.requireNonNull(entity.create(level));boolean king=boar.king();
            boar.moveTo(CENTER.x+(king?2.3:-2.3),CENTER.y,CENTER.z,king?150:195,0);
            boar.setNoAi(true);boar.setPersistenceRequired();boar.setInvulnerable(true);boar.yBodyRot=boar.getYRot();
            level.addFreshEntity(boar);if(king)kingBoarId=boar.getId();else normalBoarId=boar.getId();
        }
        var bag=new ItemStack(StorageRegistry.BAG_ITEM.get());UUID bagId=UUID.randomUUID();bag.getOrCreateTag().putUUID("bag_id",bagId);
        player.getInventory().selected=0;player.setItemInHand(InteractionHand.MAIN_HAND,bag);
        var inventory=BagData.get(level).inventory(bagId);
        for(int i=0;i<64;i++)inventory.insert(new ItemStack(Items.DIAMOND,64));
        for(var grade:SpiritStoneGrade.values())inventory.insert(new ItemStack(grade.item(),64));
        for(int years:new int[]{500,1700}){var herb=new ItemStack(SpiritPlantRegistry.SPIRIT_SPROUT_GRASS_ITEM.get(),32);HerbAge.setYears(herb,years);inventory.insert(herb);}
        for(int i=0;i<64;i++){var sample=new ItemStack(Items.STONE,64);sample.setHoverName(Component.literal("验收样本 "+(i+1)));inventory.insert(sample);}
        BagData.get(level).setDirty();
        if(!bag.getItem().use(level,player,InteractionHand.MAIN_HAND).getResult().consumesAction()||!(player.containerMenu instanceof BagMenu))throw new AssertionError("Production storage-bag use did not open its real server menu");
    }

    private static void startFormation(Minecraft minecraft,UUID playerId) {
        var player=Objects.requireNonNull(minecraft.getSingleplayerServer().getPlayerList().getPlayer(playerId));var level=player.serverLevel();
        player.closeContainer();
        for(int id:new int[]{normalBoarId,kingBoarId}){var entity=level.getEntity(id);if(entity!=null)entity.discard();}
        for(int i=0;i<FormationBootstrap.swords().size();i++)player.getInventory().setItem(10+i,new ItemStack(FormationBootstrap.swords().get(i).get()));
        player.getInventory().setItem(20,new ItemStack(SpiritStoneGrade.EXQUISITE.item(),10));
        var chart=new ItemStack(FormationBootstrap.ZHUXIAN_SWORD_CHART.get());player.setItemInHand(InteractionHand.MAIN_HAND,chart);
        var hit=new BlockHitResult(Vec3.atCenterOf(GROUND).add(0,.5,0),Direction.UP,GROUND,false);
        if(!chart.useOn(new UseOnContext(player,InteractionHand.MAIN_HAND,hit)).consumesAction())throw new AssertionError("Real chart item entry rejected formation");
        var state=SwordArrayServer.active(player).orElseThrow(()->new AssertionError("Formation server state absent"));
        formationStart=state.start();formationOwner=state.owner();player.containerMenu.broadcastChanges();
    }

    private static boolean clientHasFormation()throws Exception {
        Field field=Class.forName("cn.blockforge.generated.generatedmod.formation.client.SwordArrayClient").getDeclaredField("ACTIVE");field.setAccessible(true);
        return ((Map<?,?>)field.get(null)).containsKey(formationOwner);
    }

    private static void prepareArtifacts(Minecraft minecraft) {
        var level=minecraft.getSingleplayerServer().overworld();
        for(BlockPos pos:List.of(BATH))level.setBlock(pos,Blocks.AIR.defaultBlockState(),3);
        var ids=new ArrayList<Integer>();
        for(int rank=1;rank<=3;rank++) {
            var stand=new net.minecraft.world.entity.decoration.ArmorStand(level,CENTER.x+(rank-2)*3,CENTER.y,CENTER.z);
            stand.setNoGravity(true);stand.setShowArms(true);stand.setNoBasePlate(true);stand.setInvulnerable(true);
            stand.setYRot(180);stand.yBodyRot=180;
            stand.setCustomName(Component.literal("法宝模型 · "+rank));stand.setCustomNameVisible(true);
            for(var kind:ArtifactKind.values()) {
                var stack=new ItemStack(ArtifactBootstrap.ARTIFACTS.get(kind).get());
                EquipmentCrafting.write(stack).putInt("Refine",rank==3?7:rank==2?4:0);
                stand.setItemSlot(kind==ArtifactKind.SWORD?net.minecraft.world.entity.EquipmentSlot.MAINHAND:
                        kind==ArtifactKind.HAMMER?net.minecraft.world.entity.EquipmentSlot.OFFHAND:kind.armor.getSlot(),stack);
            }
            level.addFreshEntity(stand);ids.add(stand.getId());
        }
        artifactStandIds=List.copyOf(ids);
    }

    private static void prepareSideContent(Minecraft minecraft,UUID playerId) {
        var player=Objects.requireNonNull(minecraft.getSingleplayerServer().getPlayerList().getPlayer(playerId));var level=player.serverLevel();
        // Close through the same real chart entry used to open the formation, after its six captures.
        var chart=player.getMainHandItem();
        var groundHit=new BlockHitResult(Vec3.atCenterOf(GROUND).add(0,.5,0),Direction.UP,GROUND,false);
        if(!chart.is(FormationBootstrap.ZHUXIAN_SWORD_CHART.get())
                ||!chart.useOn(new UseOnContext(player,InteractionHand.MAIN_HAND,groundHit)).consumesAction()
                ||SwordArrayServer.active(player).isPresent())throw new AssertionError("Production chart did not close the captured formation");
        level.setBlock(BATH,ImportedContent.SPIRIT_BATH.get().defaultBlockState(),3);
        var hit=new BlockHitResult(Vec3.atCenterOf(BATH),Direction.UP,BATH,false);
        for(String herb:SpiritBathBlockEntity.HERBS) {
            Item item=BuiltInRegistries.ITEM.get(new ResourceLocation(GeneratedMod.MOD_ID,herb));
            if(item==Items.AIR)throw new AssertionError("Bath acceptance herb missing: "+herb);
            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(item));
            var state=level.getBlockState(BATH);
            if(!state.use(level,player,InteractionHand.MAIN_HAND,hit).consumesAction()||!player.getMainHandItem().isEmpty())
                throw new AssertionError("Production bath use did not consume herb: "+herb);
        }
        player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
        if(!(level.getBlockEntity(BATH) instanceof SpiritBathBlockEntity bath)||bath.depositCount()!=4
                ||!level.getBlockState(BATH).getValue(SpiritBathBlock.FILLED))throw new AssertionError("Production bath did not become filled after four herbs");
        var evidence=new ArrayList<Map<String,Object>>();
        for(BlockPos pos:List.of(BATH)) {
            var state=level.getBlockState(pos);var row=new LinkedHashMap<String,Object>();
            row.put("position",List.of(pos.getX(),pos.getY(),pos.getZ()));row.put("block",BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString());
            row.put("state",state.toString());
            if(pos.equals(BATH)){row.put("filled",state.getValue(SpiritBathBlock.FILLED));row.put("depositedHerbs",bath.depositCount());row.put("filledThroughProductionUse",true);}
            evidence.add(row);
        }
        sideContentServerEvidence=List.copyOf(evidence);
    }
    private static void checkShaderPack()throws Exception {
        for(String name:new String[]{"net.irisshaders.iris.api.v0.IrisApi","net.coderbot.iris.api.v0.IrisApi"})try {
            Class<?> api=Class.forName(name);Object instance=api.getMethod("getInstance").invoke(null);
            shaderActive=(boolean)api.getMethod("isShaderPackInUse").invoke(instance);break;
        }catch(ClassNotFoundException ignored){}
        if(shaderActive)for(String name:new String[]{"net.irisshaders.iris.Iris","net.coderbot.iris.Iris"})try {
            Object result=Class.forName(name).getMethod("getCurrentPackName").invoke(null);
            shaderPack=result instanceof Optional<?> optional?String.valueOf(optional.orElse(null)):String.valueOf(result);break;
        }catch(ClassNotFoundException ignored){}
        if(EXTERNAL&&(!shaderActive||shaderPack.equals("none")||shaderPack.isBlank()||!EXPECTED_PACK.isBlank()&&!shaderPack.contains(EXPECTED_PACK)))
            throw new AssertionError("Requested external shader pack not actually active: "+shaderPack+", enabled="+shaderActive);
        if(!EXTERNAL&&shaderActive)throw new AssertionError("Native baseline unexpectedly has an active shader pack: "+shaderPack);
    }

    @SubscribeEvent(priority=EventPriority.LOWEST) public static void camera(ViewportEvent.ComputeCameraAngles event) {
        if(!ENABLED||finished||phase<3||phase>=9)return;
        try {
            if(cameraPosition==null){cameraPosition=Camera.class.getDeclaredMethod("setPosition",Vec3.class);cameraPosition.setAccessible(true);}
            Vec3 target=CENTER.add(0,phase==3?.7:phase==6?4:phase==5?2:phase==7?.45:0,0);
            Vec3 offset=phase==3?new Vec3(8,3.4,10):phase==4?new Vec3(23,24,26):phase==5?new Vec3(23,12,25):phase==7?new Vec3(4.5,4,7):phase==8?new Vec3(2,4,12):new Vec3(22,14,26);
            Vec3 position=target.add(offset),toward=target.subtract(position);cameraPosition.invoke(event.getCamera(),position);
            event.setYaw((float)Math.toDegrees(Math.atan2(-toward.x,toward.z)));event.setPitch((float)-Math.toDegrees(Math.atan2(toward.y,toward.horizontalDistance())));event.setRoll(0);
        }catch(Throwable error){fail("CAMERA",error);}
    }

    @SubscribeEvent(priority=EventPriority.LOWEST) public static void frame(TickEvent.RenderTickEvent event) {
        if(!ENABLED||finished||event.phase!=TickEvent.Phase.END)return;
        var minecraft=Minecraft.getInstance();if(minecraft.level==null||minecraft.getOverlay()!=null||phase==0)return;
        try {
            int error=GL11.glGetError();if(error!=GL11.GL_NO_ERROR){GL_ERRORS.add("phase "+phase+": "+error);throw new AssertionError("OpenGL error "+error);}
            if(!requested)return;PointerSafety.verify();
            var row=new LinkedHashMap<String,Object>();row.put("case",CASES[phase-1]);row.put("glError",error);
            row.put("width",minecraft.getMainRenderTarget().width);row.put("height",minecraft.getMainRenderTarget().height);
            row.put("shaderPackActive",shaderActive);row.put("shaderPack",shaderPack);
            if(phase<=2){var menu=(BagMenu)minecraft.player.containerMenu;row.put("bagId",menu.bagId);row.put("visibleTotals",menu.totals.clone());row.put("magnetEnabled",menu.info.get(2)!=0);}
            if(phase==3) {
                for(int id:new int[]{normalBoarId,kingBoarId}) {
                    var entity=minecraft.level.getEntity(id);if(!(entity instanceof BlackBoarEntity boar)||!(minecraft.getEntityRenderDispatcher().getRenderer(boar) instanceof BlackBoarRenderer))throw new AssertionError("Boar source renderer not active for entity "+id);
                }
                row.put("models",List.of("wuxingxiantu:black_boar","wuxingxiantu:black_boar_king"));
            }
            if(phase>=4&&phase<=6) {
                for(String name:new String[]{"ground","shader"}){Field field=Class.forName("cn.blockforge.generated.generatedmod.formation.client.SwordArrayShield").getDeclaredField(name);field.setAccessible(true);if(field.get(null)==null)throw new AssertionError("Formation "+name+" shader missing");}
                row.put("formationAgeTicks",minecraft.level.getGameTime()-formationStart);row.put("serverOwner",formationOwner);row.put("actualClientStateReceived",clientHasFormation());
                if(phase==6){Field field=Class.forName("cn.blockforge.generated.generatedmod.formation.client.SwordArrayGeometry").getDeclaredField("mesh");field.setAccessible(true);if(!(field.get(null) instanceof float[] mesh)||mesh.length==0)throw new AssertionError("Real sword-flow mesh was not rendered");row.put("sourceSwordMeshFloats",mesh.length);}
            }
            if(phase==7) {
                if(sideContentServerEvidence.size()!=1)throw new AssertionError("Missing server block evidence");
                var clientBlocks=new ArrayList<String>();
                for(BlockPos pos:List.of(BATH))clientBlocks.add(minecraft.level.getBlockState(pos).toString());
                if(!minecraft.level.getBlockState(BATH).getValue(SpiritBathBlock.FILLED))throw new AssertionError("Client bath is not filled at capture");
                row.put("serverBlocks",sideContentServerEvidence);row.put("clientBlockStates",clientBlocks);row.put("formationClosed",!clientHasFormation());
            }
            if(phase==8) {
                var ranks=new ArrayList<Integer>();int expectedRank=0;
                for(int id:artifactStandIds) {
                    expectedRank++;var stand=(net.minecraft.world.entity.decoration.ArmorStand)minecraft.level.getEntity(id);
                    for(var stack:List.of(stand.getMainHandItem(),stand.getOffhandItem())) {
                        var property=net.minecraft.client.renderer.item.ItemProperties.getProperty(stack.getItem(),new ResourceLocation(GeneratedMod.MOD_ID,"artifact_rank"));
                        int actual=property==null?-1:(int)property.call(stack,minecraft.level,minecraft.player,0);
                        if(actual!=expectedRank)throw new AssertionError("Artifact authored model rank mismatch: "+actual+" expected "+expectedRank);
                        ranks.add(actual);
                    }
                    for(var stack:stand.getArmorSlots())if(!ArtifactData.artifact(stack))throw new AssertionError("Source artifact armor missing from real stand");
                }
                row.put("actualRegisteredModelRanks",ranks);row.put("armorStands",artifactStandIds);
            }
            if(phase==9) {
                Field state=Class.forName("cn.blockforge.generated.generatedmod.veincompass.client.VeinCompassHud").getDeclaredField("state");state.setAccessible(true);
                if(!(state.get(null) instanceof cn.blockforge.generated.generatedmod.veincompass.VeinCompassStatePacket packet))throw new AssertionError("Real holder compass sync missing");
                row.put("serverCompassSnapshot",packet);row.put("realHeldItem",minecraft.player.getMainHandItem().getDescriptionId());
            }
            if(phase==10){
                if(!(minecraft.screen instanceof cn.blockforge.generated.generatedmod.meditationqte.client.QteScreen)
                        ||!(minecraft.player.getVehicle() instanceof MeditationSeatEntity))throw new AssertionError("Real QTE menu or seat missing");
                row.put("actualServerSessionAndSourceSeat",true);row.put("screen",minecraft.screen.getClass().getName());
            }
            String filename=CASES[phase-1]+".png";try(var image=Screenshot.takeScreenshot(minecraft.getMainRenderTarget())){image.writeToFile(OUTPUT.resolve(filename));}
            row.put("file",filename);CAPTURES.add(row);requested=false;phase++;phaseTicks=0;writeReport();
            if(CAPTURES.size()==CASES.length)finish();
        }catch(Throwable error){fail("RENDER phase="+phase,error);}
    }

    private static void writeReport()throws Exception {
        var report=new LinkedHashMap<String,Object>();report.put("test","myriad-real-client-acceptance");report.put("expectedExternalShaders",EXTERNAL);
        report.put("shaderPackActuallyActive",shaderActive);report.put("shaderPack",shaderPack);report.put("hiddenWindow",!SHOW_WINDOW);
        report.put("isolatedWorld",true);report.put("pointerSafety",PointerSafety.snapshot());report.put("captures",CAPTURES);
        report.put("glErrors",GL_ERRORS);report.put("failures",FAILURES);report.put("complete",CAPTURES.size()==CASES.length&&FAILURES.isEmpty());
        Files.writeString(OUTPUT.resolve("captures.json"),new GsonBuilder().setPrettyPrinting().create().toJson(report));
    }
    private static void fail(String location,Throwable error){FAILURES.add(location+": "+error);com.mojang.logging.LogUtils.getLogger().error("MYRIAD_CLIENT_CAPTURE "+location,error);finish();}
    private static void finish() {
        if(finished)return;finished=true;
        try {Files.createDirectories(OUTPUT);try{PointerSafety.verify();}catch(Throwable error){FAILURES.add(error.toString());}writeReport();
            Files.writeString(OUTPUT.resolve(CAPTURES.size()==CASES.length&&FAILURES.isEmpty()?"complete.txt":"failure.txt"),FAILURES.isEmpty()?CASES.length+" actual production captures completed; inspect PNGs and captures.json.\n":String.join("\n",FAILURES));
        }catch(Exception error){com.mojang.logging.LogUtils.getLogger().error("MYRIAD_CAPTURE_REPORT",error);}
        Minecraft.getInstance().stop();
    }
    private MyriadClientCapture(){}
}
