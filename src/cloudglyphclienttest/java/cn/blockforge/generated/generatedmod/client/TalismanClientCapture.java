package cn.blockforge.generated.generatedmod.client;
import cn.blockforge.generated.generatedmod.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.api.distmarker.Dist;
import java.nio.file.*;

/** Uses the actual baked models and item override predicates, without adopting a player world. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT)
public final class TalismanClientCapture {
    private static int frames;private static boolean done;
    @SubscribeEvent public static void tick(net.minecraftforge.event.TickEvent.ClientTickEvent e) {
        if(!Boolean.getBoolean("wuxingxiantu.talismanCapture")||done||e.phase!=net.minecraftforge.event.TickEvent.Phase.END)return;
        var mc=Minecraft.getInstance();mc.setWindowActive(true);
        if(mc.getOverlay()==null&&mc.screen instanceof net.minecraft.client.gui.screens.AccessibilityOnboardingScreen)
            mc.setScreen(new net.minecraft.client.gui.screens.TitleScreen());
    }
    @SubscribeEvent public static void render(ScreenEvent.Render.Post e) {
        if(!Boolean.getBoolean("wuxingxiantu.talismanCapture")||done)return;
        var mc=Minecraft.getInstance();if(mc.getOverlay()!=null||!(mc.screen instanceof net.minecraft.client.gui.screens.TitleScreen))return;
        try {
            if(frames++==0){mc.getWindow().setWindowed(1470,850);mc.resizeDisplay();mc.options.guiScale().set(1);mc.resizeDisplay();
                mc.options.pauseOnLostFocus=false;org.lwjgl.glfw.GLFW.glfwHideWindow(mc.getWindow().getWindow());}
            var g=e.getGuiGraphics();g.flush();g.pose().pushPose();g.pose().translate(0,0,500);
            g.fill(0,0,mc.screen.width,mc.screen.height,0xFF181D25);
            g.drawString(mc.font,"七种符箓 · 实际游戏物品模型与画符模板",20,15,0xFFFFFF);
            int i=0;
            for(String id:TalismanCatalog.ids()) {
                ItemStack stack=new ItemStack(WuXingItems.TALISMAN.get());TalismanData.setTalisman(stack,id);TalismanData.setQuality(stack,TalismanCatalog.Quality.LOW);
                var model=mc.getItemRenderer().getModel(stack,null,null,0);
                String sprite=model.getParticleIcon().contents().name().toString();
                if(!sprite.equals("wuxingxiantu:item/talisman_"+id))throw new AssertionError("Wrong override "+id+": "+sprite);
                int x=25+i++*200;
                g.pose().pushPose();g.pose().translate(x,45,0);g.pose().scale(8,8,1);g.renderItem(stack,0,0);g.pose().popPose();
                g.drawString(mc.font,TalismanCatalog.title(id),x,185,0xFFFFFF);
                boolean[][] glyph=TalismanCatalog.glyph(id);g.fill(x,220,x+128,220+256,0xFFE5D39A);
                for(int y=0;y<32;y++)for(int c=0;c<16;c++)if(glyph[y][c])g.fill(x+c*8,220+y*8,x+c*8+8,220+y*8+8,0xFFAD271C);
            }
            var table=new ItemStack(WuXingBlocks.HERB_PROCESSING_TABLE_ITEM.get());
            var state=WuXingBlocks.HERB_PROCESSING_TABLE.get().defaultBlockState();
            var blockModel=mc.getBlockRenderer().getBlockModel(state);
            var expected=java.util.Map.of(net.minecraft.core.Direction.UP,"top",net.minecraft.core.Direction.DOWN,"bottom",
                    net.minecraft.core.Direction.NORTH,"front",net.minecraft.core.Direction.SOUTH,"back",net.minecraft.core.Direction.WEST,"left",net.minecraft.core.Direction.EAST,"right");
            for(var face:expected.entrySet()) {
                var quads=blockModel.getQuads(state,face.getKey(),net.minecraft.util.RandomSource.create(1));
                if(quads.size()!=1||!quads.get(0).getSprite().contents().name().toString().equals("wuxingxiantu:block/herb_processing_table_"+face.getValue()))
                    throw new AssertionError("Table face missing/wrong: "+face.getKey());
            }
            g.pose().pushPose();g.pose().translate(25,550,0);g.pose().scale(10,10,1);g.renderItem(table,0,0);g.pose().popPose();
            g.drawString(mc.font,"灵药加工台 · 原版完整方块 · 六面贴图",215,570,0xFFFFFF);
            g.pose().popPose();g.flush();
            if(frames>=20) {
                var out=Path.of("E:/五行仙途/deliverables/task-20261015-talismans-implemented/client");Files.createDirectories(out);
                int gl=org.lwjgl.opengl.GL11.glGetError();if(gl!=0)throw new AssertionError("GL error "+gl);
                try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(out.resolve("符箓与加工台-游戏实际模型.png"));}
                Files.writeString(out.resolve("verification.txt"),"7/7 actual baked item sprites matched; 6/6 herb table face sprites matched; GL error=0; source-derived glyphs rendered.\n");done=true;mc.stop();
            }
        }catch(Throwable ex){done=true;try{Files.writeString(Path.of("E:/五行仙途/deliverables/task-20261015-talismans-implemented/client-failure.txt"),ex.toString());}catch(Exception ignored){}mc.stop();}
    }
}
