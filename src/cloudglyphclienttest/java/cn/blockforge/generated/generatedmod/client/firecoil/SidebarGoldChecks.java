package cn.blockforge.generated.generatedmod.client.firecoil;

import cn.blockforge.generated.generatedmod.client.*;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import java.nio.file.*;
import java.util.*;

/** Real GUI captures only; this source set is excluded from the release jar. */
final class SidebarGoldChecks {
    @SuppressWarnings("unchecked")
    static void run(Path out) throws Exception {
        var mc=Minecraft.getInstance();
        var navField=CultivationMainScreen.class.getDeclaredField("navButtons");
        navField.setAccessible(true);
        var captures=new ArrayList<String>();
        mc.options.hideGui=false;
        for(int scale:new int[]{2,3}) {
            mc.setScreen(null);mc.getWindow().setWindowed(1280,720);
            mc.options.guiScale().set(scale);mc.resizeDisplay();
            var screen=new CultivationMainScreen(mc.player,"settings");mc.setScreen(screen);
            var navigation=(List<CultivationUi.Action>)navField.get(screen);
            for(String state:List.of("normal","hover","focus","disabled")) {
                var projection=new org.joml.Matrix4f(RenderSystem.getProjectionMatrix());
                var sorting=RenderSystem.getVertexSorting();var model=RenderSystem.getModelViewStack();
                model.pushPose();model.setIdentity();model.translate(0,0,-11000);RenderSystem.applyModelViewMatrix();
                RenderSystem.setProjectionMatrix(new org.joml.Matrix4f().setOrtho(0,screen.width,screen.height,0,1000,21000),VertexSorting.ORTHOGRAPHIC_Z);
                try {
                    var target=navigation.stream().filter(b->b.getMessage().getString().equals("百艺")).findFirst().orElseThrow();
                    target.active=!state.equals("disabled");target.setFocused(state.equals("focus"));
                    mc.getMainRenderTarget().bindWrite(true);
                    RenderSystem.clear(org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT|org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT,Minecraft.ON_OSX);
                    var g=new GuiGraphics(mc,mc.renderBuffers().bufferSource());
                    screen.render(g,state.equals("hover")?target.getX()+2:0,state.equals("hover")?target.getY()+2:0,0);g.flush();
                    String name="sidebar-gui"+scale+"-"+state+".png";
                    try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(out.resolve(name));}
                    if(org.lwjgl.opengl.GL11.glGetError()!=0)throw new AssertionError("Sidebar GL error");
                    captures.add(name);target.active=true;target.setFocused(false);
                } finally {model.popPose();RenderSystem.applyModelViewMatrix();RenderSystem.setProjectionMatrix(projection,sorting);}
            }
        }
        Files.writeString(out.resolve("sidebar-gold-ui.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(Map.of("passed",true,"captures",captures,"realPointerUsed",false)));
        mc.setScreen(null);
    }
}
