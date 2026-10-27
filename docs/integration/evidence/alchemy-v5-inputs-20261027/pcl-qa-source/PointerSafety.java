package cn.blockforge.generated.generatedmod.client.qa.myriad;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;
import java.util.Map;

/** Development-only invariant; this source set never enters the release JAR. */
@Mod.EventBusSubscriber(modid="codex_inputs_qa",value=Dist.CLIENT)
public final class PointerSafety {
    public static int blockedGrabs;
    private static int frames,violations;
    @SubscribeEvent public static void frame(TickEvent.RenderTickEvent event) {
        if(event.phase!=TickEvent.Phase.END)return;
        frames++;var minecraft=Minecraft.getInstance();
        if(minecraft.mouseHandler.isMouseGrabbed()||GLFW.glfwGetInputMode(minecraft.getWindow().getWindow(),GLFW.GLFW_CURSOR)!=GLFW.GLFW_CURSOR_NORMAL)violations++;
    }
    public static Map<String,Object> snapshot() {
        var minecraft=Minecraft.getInstance();int mode=GLFW.glfwGetInputMode(minecraft.getWindow().getWindow(),GLFW.GLFW_CURSOR);
        boolean passed=frames>0&&blockedGrabs>0&&violations==0&&!minecraft.mouseHandler.isMouseGrabbed()&&mode==GLFW.GLFW_CURSOR_NORMAL;
        return Map.of("passed",passed,"blockedGrabs",blockedGrabs,"checkedFrames",frames,"captureViolations",violations,"cursorMode",mode);
    }
    public static void verify(){if(!Boolean.TRUE.equals(snapshot().get("passed")))throw new AssertionError("Desktop pointer capture invariant: "+snapshot());}
    private PointerSafety(){}
}
