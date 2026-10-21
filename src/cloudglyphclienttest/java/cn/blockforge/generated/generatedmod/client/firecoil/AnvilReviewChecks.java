package cn.blockforge.generated.generatedmod.client.firecoil;
import cn.blockforge.generated.generatedmod.*;
import cn.blockforge.generated.generatedmod.client.*;
import cn.blockforge.generated.generatedmod.client.anvillight.AnvilBlueLighting;
import net.minecraft.client.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.nio.file.*;
import java.util.*;
@Mod.EventBusSubscriber(modid="wuxingxiantu",value=net.minecraftforge.api.distmarker.Dist.CLIENT)
final class AnvilReviewChecks {
    private static int tick;private static java.util.concurrent.CompletableFuture<?> setup;
    private static String pending;private static Path output;private static final List<Map<String,Object>> shots=new ArrayList<>();
    static boolean advance(Path out)throws Exception{
        var mc=Minecraft.getInstance();output=out;
        if(tick==0){setup=mc.getSingleplayerServer().submit(()->{var p=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());var l=p.serverLevel();l.setDayTime(18000);for(int x=-8;x<=8;x++)for(int z=-6;z<=12;z++)l.setBlockAndUpdate(new BlockPos(x,128,z),Blocks.STONE.defaultBlockState());l.setBlockAndUpdate(new BlockPos(0,129,4),WuXingBlocks.XUANLING_ANVIL.get().defaultBlockState());p.connection.teleport(.5,129.1,-.5,0,14);});tick++;return false;}
        if(!setup.isDone()||pending!=null)return false;setup.join();tick++;
        mc.setScreen(null);mc.options.hideGui=true;mc.player.setYRot(tick<110?0:tick<145?-12:12);mc.player.yRotO=mc.player.getYRot();mc.player.setXRot(14);mc.player.xRotO=14;mc.player.setNoGravity(true);mc.player.getAbilities().flying=true;
        if(tick==90||tick==91||tick==92||tick==130||tick==165||tick==190||tick==245)pending="anvil-light-"+tick+".png";
        if(tick>175&&tick<200){mc.player.setYRot((tick-175)*.15f);mc.player.yRotO=mc.player.getYRot();}
        if(tick==200)setup=mc.getSingleplayerServer().submit(()->{var l=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID()).serverLevel();for(int x=-2;x<=2;x++)for(int y=129;y<=132;y++)l.setBlockAndUpdate(new BlockPos(x,y,2),Blocks.STONE.defaultBlockState());});
        if(tick<=255||pending!=null)return false;
        Files.writeString(out.resolve("anvil-light.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(Map.of("passed",shots.size()==7,"shaderPack",TrailShaderBridge.shaderPackDetected(),"shots",shots)));AnvilUiChecks.run(out);return true;
    }
    @SubscribeEvent public static void capture(TickEvent.RenderTickEvent event){
        if(!Boolean.getBoolean("wuxingxiantu.anvilReviewQa")||event.phase!=TickEvent.Phase.END||pending==null)return;
        var mc=Minecraft.getInstance();try{
            try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(output.resolve(pending));}
            shots.add(Map.of("file",pending,"runeDraws",AnvilBlueLighting.runeDraws(),"depth",TrailShaderBridge.depthStatus));pending=null;
        }catch(Exception e){throw new RuntimeException(e);}
    }
}
