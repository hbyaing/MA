package cn.blockforge.generated.generatedmod.client.firecoil;
import cn.blockforge.generated.generatedmod.*;
import cn.blockforge.generated.generatedmod.client.*;
import cn.blockforge.generated.generatedmod.progression.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiGraphics;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexSorting;
import java.nio.file.*;
import java.util.*;
final class PlayerProgressionChecks {
    private static int tick;
    private static java.util.concurrent.CompletableFuture<?> setup;
    @SuppressWarnings("unchecked") static boolean advance(Path out)throws Exception{
        var mc=Minecraft.getInstance();
        mc.options.hideGui=false;
        if(tick==0){setup=mc.getSingleplayerServer().submit(()->{var p=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());GeneratedMod.data(p).wuxingxiantu$setRealm(2);GeneratedMod.sync(p);});tick++;return false;}
        if(!setup.isDone())return false;setup.join();tick++;
        if(tick==10)PlayerProgressionClient.request("craft:alchemy");
        if(tick==25)PlayerProgressionClient.request("craft:refining");
        if(tick==40)PlayerProgressionClient.request("craft:talisman");
        if(tick==55){mc.player.setNoGravity(true);mc.player.getAbilities().flying=true;PlayerProgressionClient.request("open");}
        if(tick<200)return false;
        if(tick==200){mc.setScreen(null);mc.getWindow().setWindowed(1280,720);mc.options.guiScale().set(2);mc.resizeDisplay();setup=mc.getSingleplayerServer().submit(()->{
            var p=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());p.connection.teleport(.5,129,.5,0,0);
            var cow=net.minecraft.world.entity.EntityType.COW.create(p.serverLevel());cow.setPos(.5,129,6.5);p.serverLevel().addFreshEntity(cow);
        });return false;}
        if(tick==215)PlayerProgressionClient.request("scan");
        if(tick==230)PlayerProgressionClient.request("lock");
        if(tick<250)return false;
        var state=PlayerProgressionClient.state;
        for(String id:PlayerCrafts.IDS)if(!state.getCompound(id).getBoolean("Learned"))throw new AssertionError("Real client/server learn action failed: "+id);
        if(!state.getBoolean("SenseOpen"))throw new AssertionError("Real opening/network snapshot failed: "+state);
        if(state.getString("Lock").isEmpty()||state.getIntArray("ScanTargets").length==0)throw new AssertionError("Actual scan/lock feedback missing: "+state);
        try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(out.resolve("sense-live-scan-lock.png"));}
        var result=new ArrayList<Map<String,Object>>();
        var pageField=CultivationMainScreen.class.getDeclaredField("page");pageField.setAccessible(true);
        var navField=CultivationMainScreen.class.getDeclaredField("navButtons");navField.setAccessible(true);
        for(int[] size:new int[][]{{1280,720,2},{1920,1080,2},{1280,960,4}})for(String id:List.of("crafts","sense")){
            mc.setScreen(null);mc.getWindow().setWindowed(size[0],size[1]);mc.options.guiScale().set(size[2]);mc.resizeDisplay();
            var screen=new CultivationMainScreen(mc.player,id);mc.setScreen(screen);var page=(PlayerProgressionPage)pageField.get(screen);
            var layout=CultivationLayout.of(screen.width,screen.height);
            var nav=(List<CultivationUi.Action>)navField.get(screen);
            for(var b:nav)if(b.getY()<layout.y()||b.getY()+b.getHeight()>layout.y()+layout.height())throw new AssertionError("Navigation outside frame");
            for(var child:page.children())if(child instanceof net.minecraft.client.gui.components.Button&&!(child instanceof CultivationUi.Action))throw new AssertionError("Vanilla button in custom page");
            for(String mode:List.of("normal","hover","focus","disabled")){
                var button=(CultivationUi.Action)page.children().get(id.equals("sense")?1:0);button.active=!mode.equals("disabled");button.setFocused(mode.equals("focus"));
                var original=PlayerProgressionClient.state.copy();if(id.equals("crafts")&&mode.equals("disabled"))PlayerProgressionClient.state.getCompound("alchemy").putInt("Rank",8);
                render(out.resolve(id+"-"+size[0]+"-gui"+size[2]+"-"+mode+".png"),screen,mode.equals("hover")?layout.contentX()+button.getX()+2:0,mode.equals("hover")?layout.contentY()+button.getY()+2:0);
                if(mode.equals("disabled")&&button.active)throw new AssertionError("Disabled appearance was not exercised");PlayerProgressionClient.state=original;
            }
            screen.mouseScrolled(layout.contentX()+20,layout.contentY()+60,-20);
            render(out.resolve(id+"-"+size[0]+"-gui"+size[2]+"-scrolled.png"),screen,0,0);
            result.add(Map.of("page",id,"size",List.of(size[0],size[1]),"scale",size[2],"navigation",nav.size(),"passed",true));
        }
        mc.setScreen(null);TalismanUiChecks.run(out);Files.writeString(out.resolve("player-progression-ui.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(Map.of("passed",true,"realServerLearnAndOpening",true,"realScanAndLock",true,"talismanUiRegression",true,"cases",result)));return true;
    }
    private static void render(Path file,CultivationMainScreen screen,int x,int y)throws Exception{
        var mc=Minecraft.getInstance();var projection=new org.joml.Matrix4f(RenderSystem.getProjectionMatrix());var sorting=RenderSystem.getVertexSorting();var mv=RenderSystem.getModelViewStack();mv.pushPose();mv.setIdentity();mv.translate(0,0,-11000);RenderSystem.applyModelViewMatrix();
        RenderSystem.setProjectionMatrix(new org.joml.Matrix4f().setOrtho(0,mc.getWindow().getGuiScaledWidth(),mc.getWindow().getGuiScaledHeight(),0,1000,21000),VertexSorting.ORTHOGRAPHIC_Z);
        try{mc.getMainRenderTarget().bindWrite(true);RenderSystem.clear(org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT|org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT,Minecraft.ON_OSX);var g=new GuiGraphics(mc,mc.renderBuffers().bufferSource());screen.render(g,x,y,0);g.flush();try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(file);}if(org.lwjgl.opengl.GL11.glGetError()!=0)throw new AssertionError("UI GL error");}
        finally{mv.popPose();RenderSystem.applyModelViewMatrix();RenderSystem.setProjectionMatrix(projection,sorting);}
    }
}
