package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.boss.VisualBossEntity;
import cn.blockforge.generated.generatedmod.boss.combat.BossCombatCatalog;
import cn.blockforge.generated.generatedmod.items.HeartSwordItem;
import cn.blockforge.generated.generatedmod.mindscape.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Opt-in isolated real-client captures of nine actual encounters, excluded from release artifacts. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT)
public final class BossCombatClientCapture {
    private static final long START=System.nanoTime();
    private static boolean opened,initialised,finished,inScene,capture;
    private static int scene,ticks,frames,layoutTicks;
    private static CompletableFuture<Void> pending;
    private static final String[] IDS={"muyan","ermei","bichen","shetao","shenyin","yihuo","pengju","pengzhi","pengjiao"};
    private static final int[] SKILLS={2,5,6,7,7,8,9,9,10};
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event) {
        if(finished||event.phase!=TickEvent.Phase.END)return;
        var mc=Minecraft.getInstance();
        try {
            if((System.nanoTime()-START)/1_000_000_000>360)throw new IllegalStateException("Boss capture timed out");
            mc.options.pauseOnLostFocus=false;
            if(!opened&&mc.getOverlay()==null&&(mc.screen instanceof net.minecraft.client.gui.screens.TitleScreen
                    ||mc.screen instanceof net.minecraft.client.gui.screens.AccessibilityOnboardingScreen)) {
                opened=true;mc.getWindow().setWindowed(1280,720);mc.options.renderDistance().set(4);mc.options.framerateLimit().set(60);
                mc.options.fov().set(85);mc.options.bobView().set(false);mc.options.guiScale().set(2);
                mc.resizeDisplay();
                mc.getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);
                mc.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON);
                var settings=new LevelSettings("Boss combat visual verification",GameType.CREATIVE,false,net.minecraft.world.Difficulty.NORMAL,true,new GameRules(),WorldDataConfiguration.DEFAULT);
                mc.createWorldOpenFlows().createFreshLevel("boss-combat-"+System.currentTimeMillis(),settings,new WorldOptions(91873L,false,false),net.minecraft.world.level.levelgen.presets.WorldPresets::createNormalWorldDimensions);
            }
            if(mc.player==null||mc.getSingleplayerServer()==null||mc.screen instanceof net.minecraft.client.gui.screens.ReceivingLevelScreen)return;
            if(scene>=IDS.length){layoutCapture(mc);return;}
            if(mc.screen!=null)mc.setScreen(null);
            if(pending!=null){if(!pending.isDone())return;pending.join();pending=null;}
            var uuid=mc.player.getUUID();
            if(!initialised) {
                initialised=true;pending=mc.getSingleplayerServer().submitAsync(()->{
                    var p=mc.getSingleplayerServer().getPlayerList().getPlayer(uuid);var level=p.serverLevel();
                    for(int x=-4;x<=4;x++)for(int z=-4;z<=4;z++)level.setBlock(new BlockPos(x,120,z),Blocks.SMOOTH_STONE.defaultBlockState(),2);
                    p.teleportTo(level,.5,121,.5,0,0);var data=GeneratedMod.data(p);data.wuxingxiantu$setRealm(8);data.wuxingxiantu$setMaxMana(1000000);data.wuxingxiantu$setAura(1000000);
                    p.setInvulnerable(true);
                });return;
            }
            if(!inScene) {
                inScene=true;ticks=0;int selected=scene;
                pending=mc.getSingleplayerServer().submitAsync(()->{
                    var p=mc.getSingleplayerServer().getPlayerList().getPlayer(uuid);
                    if(p.level().dimension()==MindscapeManager.DIMENSION)MindscapeManager.abort(p,true);
                    if(!MindscapeManager.enter(p,MindscapeChallenges.formalByIndex(selected,8),true))throw new IllegalStateException("Entry failed: "+IDS[selected]);
                    p.setItemInHand(InteractionHand.MAIN_HAND,HeartSwordItem.create(uuid));
                    p.setHealth(p.getMaxHealth());
                    var center=MindscapeInstances.center(GeneratedMod.data(p).wuxingxiantu$getMindscapeData().getInt("slot"));
                    p.teleportTo(p.serverLevel(),center.getX()+.5,76,center.getZ()-23.5,0,25);p.setNoGravity(true);p.getAbilities().flying=true;p.onUpdateAbilities();
                    p.serverLevel().setDayTime(6000);
                });return;
            }
            if(mc.level.dimension()!=MindscapeManager.DIMENSION)return;
            ticks++;mc.player.setXRot(ticks<90?25:5);mc.player.setYRot(0);
            if(ticks==60){int selected=scene;pending=mc.getSingleplayerServer().submitAsync(()->{
                var p=mc.getSingleplayerServer().getPlayerList().getPlayer(uuid);var tag=GeneratedMod.data(p).wuxingxiantu$getMindscapeData();
                var entity=p.serverLevel().getEntity(tag.getUUID("boss"));
                if(!(entity instanceof VisualBossEntity boss)||boss.combat()==null)throw new IllegalStateException("Combat entity not tracked");
                boss.combat().forceSkill(IDS[selected]+"_"+String.format(java.util.Locale.ROOT,"%02d",SKILLS[selected]));
            });}
            if(ticks==80)capture=true;
            if(ticks==90){int selected=scene;pending=mc.getSingleplayerServer().submitAsync(()->{
                var p=mc.getSingleplayerServer().getPlayerList().getPlayer(uuid);var tag=GeneratedMod.data(p).wuxingxiantu$getMindscapeData();
                var center=MindscapeInstances.center(tag.getInt("slot"));
                p.teleportTo(p.serverLevel(),center.getX()+.5,65.1,center.getZ()-4.5,0,5);p.getAbilities().flying=false;p.onUpdateAbilities();
                var boss=(VisualBossEntity)p.serverLevel().getEntity(tag.getUUID("boss"));
                boss.combat().forceSkill(IDS[selected]+"_01");
            });}
            if(ticks==108)capture=true;
        }catch(Throwable error){com.mojang.logging.LogUtils.getLogger().error("BOSS_COMBAT_CAPTURE",error);finish("FAIL: "+error);}
    }
    @SubscribeEvent public static void frame(TickEvent.RenderTickEvent event) {
        if(finished||event.phase!=TickEvent.Phase.END||!capture)return;
        var mc=Minecraft.getInstance();if(mc.player==null||mc.screen!=null||mc.getOverlay()!=null)return;
        try {
            Path dir=Path.of("captures");Files.createDirectories(dir);
            try(var frame=Screenshot.takeScreenshot(mc.getMainRenderTarget())){frame.writeToFile(dir.resolve(String.format(java.util.Locale.ROOT,"%02d-%s%s.png",scene+1,IDS[scene],ticks<90?"":"-ground")));}
            capture=false;frames++;if(ticks>=90){scene++;inScene=false;}
        }catch(Throwable error){finish("FAIL: "+error);}
    }
    private static void layoutCapture(Minecraft mc)throws Exception {
        if(pending!=null){if(!pending.isDone())return;pending.join();pending=null;}
        layoutTicks++;
        if(layoutTicks==1){
            var uuid=mc.player.getUUID();pending=mc.getSingleplayerServer().submitAsync(()->{
                var p=mc.getSingleplayerServer().getPlayerList().getPlayer(uuid);var tag=GeneratedMod.data(p).wuxingxiantu$getMindscapeData();
                var boss=(VisualBossEntity)p.serverLevel().getEntity(tag.getUUID("boss"));boss.stopCombat();
                p.setGameMode(GameType.SURVIVAL);p.setInvulnerable(true);
                var center=MindscapeInstances.center(tag.getInt("slot"));var at=MindscapeArenas.bossSpawn(center);boss.teleportTo(at.x,at.y,at.z);
            });return;
        }
        if(layoutTicks==10){
            var screen=new CultivationMainScreen(mc.player,"settings");mc.setScreen(screen);
            var field=CultivationMainScreen.class.getDeclaredField("page");field.setAccessible(true);
            var page=(CultivationSettingsPage)field.get(screen);
            var category=CultivationSettingsPage.class.getDeclaredField("category");category.setAccessible(true);category.setInt(page,3);
            screen.selectPage("settings");
        }
        if(layoutTicks==20){
            saveFrame(mc,"10-hud-settings.png");
            var field=CultivationMainScreen.class.getDeclaredField("page");field.setAccessible(true);
            var page=(CultivationSettingsPage)field.get(mc.screen);
            for(var child:page.children())if(child instanceof CultivationUi.Slider slider){
                String label=slider.getMessage().getString();
                double value=label.startsWith("人物横向")?1:label.startsWith("人物纵向")?.62:label.startsWith("Boss 横向")?0:label.startsWith("Boss 纵向")?.02:-1;
                if(value>=0){double x=slider.getX()+4+(slider.getWidth()-8)*value,y=slider.getY()+22;slider.onClick(x,y);slider.onRelease(x,y);}
            }
            if(cn.blockforge.generated.generatedmod.HudConfig.BOSS_AUTO.get())throw new IllegalStateException("Position slider failed to select manual mode");
            mc.setScreen(null);
        }
        if(layoutTicks==40)saveFrame(mc,"11-hud-moved.png");
        if(layoutTicks==45){mc.options.guiScale().set(3);mc.resizeDisplay();}
        if(layoutTicks==65)saveFrame(mc,"12-hud-moved-scale3.png");
        if(layoutTicks==70){cn.blockforge.generated.generatedmod.HudConfig.resetLayout();finish("Captured "+frames+" encounter frames and 3 HUD frames. Position sliders, manual mode and GUI scaling exercised. Other moves need further visual/play balance review.");}
    }
    private static void saveFrame(Minecraft mc,String name)throws Exception {
        try(var frame=Screenshot.takeScreenshot(mc.getMainRenderTarget())){frame.writeToFile(Path.of("captures",name));}
    }
    private static void finish(String message){finished=true;try{Files.writeString(Path.of("boss-capture-result.txt"),message);}catch(Exception error){throw new RuntimeException(error);}Minecraft.getInstance().stop();}
}
