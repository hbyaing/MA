package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.*;
import cn.blockforge.generated.generatedmod.fire.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Optional integrated-client smoke test; never launched by the source-only delivery. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT)
public final class ExoticBurnClientSmokeTest {
    private static boolean opened,prepared,finished;
    private static int ticks,coloredFrames,ordinaryFrames,effectFrames;
    private static volatile int exoticId=-1,ordinaryId=-1,effectId=-1;
    private static java.util.concurrent.CompletableFuture<Void> action;
    private static final long START=System.nanoTime();
    private static final BlockPos FIRE_POS=new BlockPos(6,240,0);
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event) {
        if(finished||event.phase!=TickEvent.Phase.END)return;
        var client=Minecraft.getInstance();
        try {
            if((System.nanoTime()-START)/1_000_000_000>240)throw new AssertionError("Test startup timeout");
            client.options.pauseOnLostFocus=false;
            if(!opened&&client.getOverlay()==null&&client.screen!=null) {
                opened=true;client.options.renderDistance().set(2);client.options.framerateLimit().set(60);
                var settings=new LevelSettings("Independent burn verification",GameType.SURVIVAL,false,
                        net.minecraft.world.Difficulty.PEACEFUL,true,new GameRules(),WorldDataConfiguration.DEFAULT);
                client.createWorldOpenFlows().createFreshLevel("independent-burn-"+System.currentTimeMillis(),settings,
                        new WorldOptions(712309L,false,false),net.minecraft.world.level.levelgen.presets.WorldPresets::createNormalWorldDimensions);
            }
            if(client.player==null||client.getSingleplayerServer()==null||client.screen instanceof net.minecraft.client.gui.screens.ReceivingLevelScreen)return;
            if(client.screen!=null)client.setScreen(null);
            if(!prepared) {
                prepared=true;var playerId=client.player.getUUID();
                action=client.getSingleplayerServer().submitAsync(()->{
                    var server=client.getSingleplayerServer();var p=server.getPlayerList().getPlayer(playerId);
                    p.teleportTo(p.serverLevel(),0,240,0,-90,8);p.setNoGravity(true);p.setInvulnerable(true);
                    var level=p.serverLevel();level.setWeatherParameters(6000,0,false,false);
                    level.getGameRules().getRule(GameRules.RULE_DOFIRETICK).set(false,server);
                    level.setBlockAndUpdate(FIRE_POS.below(),Blocks.STONE.defaultBlockState());
                    var exotic=cow(level,5.8);var ordinary=cow(level,9);var effect=cow(level,12);
                    exoticId=exotic.getId();ordinaryId=ordinary.getId();effectId=effect.getId();
                });
            }
            if(!action.isDone())return;action.join();ticks++;
            if(ticks==30) {
                var playerId=client.player.getUUID();
                action=client.getSingleplayerServer().submitAsync(()->{
                    var level=client.getSingleplayerServer().overworld();
                    var exotic=(Cow)level.getEntity(exoticId);var ordinary=(Cow)level.getEntity(ordinaryId);
                    var effect=(Cow)level.getEntity(effectId);var p=level.getServer().getPlayerList().getPlayer(playerId);
                    var fire=(ExoticFireBlock)WuXingBlocks.EXOTIC_FIRES[2].get();
                    level.setBlockAndUpdate(FIRE_POS,fire.defaultBlockState());
                    fire.entityInside(fire.defaultBlockState(),level,FIRE_POS,exotic);
                    require(ExoticBurnManager.isBurning(exotic)&&exotic.getRemainingFireTicks()<=0,"Block burn depends on vanilla timer");
                    ordinary.setSecondsOnFire(8);
                    SpiritEnchantmentLogic.igniteByDomain(p,effect,5);
                    ExoticBurnManager.ignite(p,FireCatalog.tier(3).key(),180,null);
                });
            }
            if(ticks>=130) {
                require(coloredFrames>0&&ordinaryFrames>0&&effectFrames>0,"Missing synchronized entity states");
                require(ExoticBurnRenderer.renderedEntities>0&&ExoticBurnOverlay.renderedFrames>0,"Independent render passes did not draw");
                var effect=client.level.getEntity(effectId);
                require(effect!=null&&!ExoticBurnClientState.isBurning(effect),"Expired burn remained on client");
                require(ExoticBurnRenderer.sprite("wuxingxiantu:missing_test_fire",false)==null,"Unknown fire fell back to ordinary fire");
                finish("PASS: independent server burn, tracked snapshots, timer expiry, ordinary-fire isolation, world/overlay draw calls and unknown-ID rejection. Pixel appearance requires manual review.");
            }
        } catch(Throwable error) {com.mojang.logging.LogUtils.getLogger().error("EXOTIC_BURN_TEST",error);finish("FAIL: "+error);}
    }
    private static Cow cow(net.minecraft.server.level.ServerLevel level,double x) {
        var cow=new Cow(EntityType.COW,level);cow.setPos(x,240,.5);cow.setNoAi(true);cow.setNoGravity(true);cow.setInvulnerable(true);
        level.addFreshEntity(cow);return cow;
    }
    @SubscribeEvent public static void frame(TickEvent.RenderTickEvent event) {
        if(finished||event.phase!=TickEvent.Phase.START)return;
        var mc=Minecraft.getInstance();if(mc.level==null)return;
        try {
            for(int id:new int[]{exoticId,effectId}) {
                var entity=mc.level.getEntity(id);if(entity==null)continue;
                var burn=ExoticBurnClientState.state(entity);if(burn==null)continue;
                require(!entity.displayFireAnimation(),"Exotic state enabled vanilla flame");
                int expected=id==exoticId?3:5;
                require(burn.sourceFireId().equals(FireCatalog.tier(expected).key().toString()),"Wrong source fire");
                for(boolean alternate:new boolean[]{false,true})require(ExoticBurnRenderer.sprite(burn.sourceFireId(),alternate)!=null,"Missing existing atlas sprite");
                if(id==exoticId)coloredFrames++;else effectFrames++;
            }
            var ordinary=mc.level.getEntity(ordinaryId);
            if(ordinary!=null&&ordinary.displayFireAnimation()) {
                require(!ExoticBurnClientState.isBurning(ordinary),"Ordinary fire acquired exotic state");ordinaryFrames++;
            }
        } catch(Throwable error) {finish("FAIL: "+error);}
    }
    private static void require(boolean value,String message) {if(!value)throw new AssertionError(message);}
    private static void finish(String result) {
        finished=true;
        try {java.nio.file.Files.writeString(java.nio.file.Path.of("exotic-burn-result.txt"),result);}
        catch(java.io.IOException error){throw new RuntimeException(error);}
        Minecraft.getInstance().stop();
    }
}
