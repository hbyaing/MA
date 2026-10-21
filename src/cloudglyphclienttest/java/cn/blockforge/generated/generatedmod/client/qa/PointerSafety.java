package cn.blockforge.generated.generatedmod.client.qa;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import org.lwjgl.glfw.GLFW;
import java.util.Map;

@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT)
public final class PointerSafety {
    public static int blockedGrabs;
    private static int frames,violations;
    @SubscribeEvent public static void frame(TickEvent.RenderTickEvent e){
        if(e.phase!=TickEvent.Phase.END)return;frames++;var mc=Minecraft.getInstance();
        if(mc.mouseHandler.isMouseGrabbed()||GLFW.glfwGetInputMode(mc.getWindow().getWindow(),GLFW.GLFW_CURSOR)!=GLFW.GLFW_CURSOR_NORMAL)violations++;
    }
    public static Map<String,Object> verify(){
        var mc=Minecraft.getInstance();int mode=GLFW.glfwGetInputMode(mc.getWindow().getWindow(),GLFW.GLFW_CURSOR);
        if(frames==0||blockedGrabs==0||violations!=0||mc.mouseHandler.isMouseGrabbed()||mode!=GLFW.GLFW_CURSOR_NORMAL)throw new AssertionError("QA pointer capture invariant: frames="+frames+", blocked="+blockedGrabs+", violations="+violations+", mode="+mode);
        return Map.of("passed",true,"blockedGrabs",blockedGrabs,"checkedFrames",frames,"captureViolations",violations,"cursorMode","NORMAL");
    }
}
