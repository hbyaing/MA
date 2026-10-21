package cn.blockforge.generated.generatedmod.client.firecoil;
import cn.blockforge.generated.generatedmod.*;
import cn.blockforge.generated.generatedmod.client.*;
import cn.blockforge.generated.generatedmod.crafting.*;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import org.joml.Matrix4f;
import java.nio.file.*;
import java.util.*;

final class EquipmentWorkstationChecks {
    static void run(Path out)throws Exception{
        var mc=Minecraft.getInstance();var results=new ArrayList<Map<String,Object>>();
        for(int[] size:new int[][]{{1280,720,2},{1920,1080,3},{1280,960,4}})for(boolean engraving:new boolean[]{true,false}){
            mc.setScreen(null);mc.getWindow().setWindowed(size[0],size[1]);mc.options.guiScale().set(size[2]);mc.resizeDisplay();
            var menu=new EquipmentWorkstationMenu(23,mc.player.getInventory(),BlockPos.ZERO,engraving);menu.slots.get(0).set(new ItemStack(engraving?Items.DIAMOND_CHESTPLATE:WuXingItems.DARK_IRON_SWORD.get()));menu.setData(1,engraving?0:21);menu.setData(2,engraving?100:45);menu.setData(3,28);menu.setData(4,7);menu.setData(7,4);menu.setData(8,4900);menu.setData(9,1);
            var screen=new EquipmentWorkstationScreen(menu,mc.player.getInventory(),Component.literal(engraving?"铭刻台":"精铸台"));mc.setScreen(screen);
            var sf=screen.getClass().getDeclaredField("uiScale");sf.setAccessible(true);float uiScale=sf.getFloat(screen);
            if(engraving){
                var qf=screen.getClass().getDeclaredField("search");qf.setAccessible(true);var search=(net.minecraft.client.gui.components.EditBox)qf.get(screen);
                var ff=screen.getClass().getDeclaredField("filtered");ff.setAccessible(true);
                search.setValue("灵力");if(((List<?>)ff.get(screen)).size()!=1)throw new AssertionError("Rune search must filter");search.setValue("不存在");if(!((List<?>)ff.get(screen)).isEmpty())throw new AssertionError("Rune empty search");search.setValue("");
                var left=(screen.width-430)/2;var top=(screen.height-330)/2;
                screen.mouseScrolled((left+50)*uiScale,(top+110)*uiScale,-1);
                var scroll=screen.getClass().getDeclaredField("scroll");scroll.setAccessible(true);if(scroll.getInt(screen)!=1)throw new AssertionError("Rune wheel scroll");
                screen.mouseClicked((left+105)*uiScale,(top+85)*uiScale,0);screen.mouseDragged((left+105)*uiScale,(top+205)*uiScale,0,0,120*uiScale);screen.mouseReleased(0,0,0);if(scroll.getInt(screen)!=1)throw new AssertionError("Rune drag scrollbar");
                screen.mouseScrolled((left+50)*uiScale,(top+110)*uiScale,1);
                screen.mouseClicked((left+105)*uiScale,(top+90)*uiScale,0);screen.mouseDragged((left+105)*uiScale,(top+114)*uiScale,0,0,24*uiScale);screen.mouseReleased(0,0,0);
                if(scroll.getInt(screen)!=1)throw new AssertionError("Scrollbar small drag must use thumb travel");
                screen.mouseClicked((left+105)*uiScale,(top+80)*uiScale,0);screen.mouseReleased(0,0,0);if(scroll.getInt(screen)!=0)throw new AssertionError("Scroll up arrow");
                screen.mouseClicked((left+105)*uiScale,(top+211)*uiScale,0);screen.mouseReleased(0,0,0);if(scroll.getInt(screen)!=1)throw new AssertionError("Scroll down arrow");
                search.setFocused(true);screen.setFocused(search);if(!screen.keyPressed(org.lwjgl.glfw.GLFW.GLFW_KEY_1,0,0)||!screen.charTyped('1',0)||!search.getValue().equals("1"))throw new AssertionError("Search must consume container shortcuts and accept text");search.setValue("");search.setFocused(false);screen.setFocused(null);
            }
            for(var child:screen.children())if(child instanceof Button&&!(child instanceof InkButton))throw new AssertionError("Vanilla workbench button");
            if(screen.width<300||screen.height<236)throw new AssertionError("Workbench outside scaled viewport");
            String name="equipment-"+(engraving?"inscription":"reforge")+"-"+size[0]+"-gui"+size[2];save(out.resolve(name+".png"),screen);
            if(engraving){var f=screen.getClass().getDeclaredField("search");f.setAccessible(true);var box=(net.minecraft.client.gui.components.EditBox)f.get(screen);box.setFocused(true);box.setValue("灵力御体");box.setHighlightPos(0);save(out.resolve(name+"-search-text.png"),screen);box.setValue("测试长文本搜索铭纹中文输入验证");box.setHighlightPos(0);save(out.resolve(name+"-search-long.png"),screen);box.setValue("");box.setFocused(false);}
            save(out.resolve(name+"-equipment-hover.png"),screen);
            var originalMenu=mc.player.containerMenu;
            try{mc.player.containerMenu=menu;var left=(screen.width-430)/2;var top=(screen.height-330)/2;
                screen.mouseClicked((left+150)*uiScale,(top+70)*uiScale,0);screen.mouseReleased((left+150)*uiScale,(top+70)*uiScale,0);
                if(!menu.equipment().isEmpty()||menu.getCarried().isEmpty())throw new AssertionError("Large equipment frame edge must pick up the actual slot item");
                screen.mouseClicked((left+208)*uiScale,(top+127)*uiScale,1);screen.mouseReleased((left+208)*uiScale,(top+127)*uiScale,1);
                if(menu.equipment().isEmpty()||!menu.getCarried().isEmpty())throw new AssertionError("Large equipment frame edge must place into the actual slot");
            }finally{mc.player.containerMenu=originalMenu;}
            var cf=screen.getClass().getDeclaredField("confirm");cf.setAccessible(true);var actionButton=(Button)cf.get(screen);screen.setFocused(actionButton);actionButton.setFocused(true);save(out.resolve(name+"-focus.png"),screen);actionButton.setFocused(false);screen.setFocused(null);save(out.resolve(name+"-hover.png"),screen);
            if(!engraving){
                for(var child:List.copyOf(screen.children()))if(child instanceof Button b&&b.getMessage().getString().equals("重铸"))b.onPress();
                if(screen.children().stream().filter(c->c instanceof Button b&&Arrays.asList(EquipmentCrafting.TEMPER).contains(b.getMessage().getString())).count()!=4)throw new AssertionError("Reforge tab choices missing");
                menu.setData(1,32);save(out.resolve(name+"-tab.png"),screen);
                var n=EquipmentCrafting.write(menu.equipment());var pending=new net.minecraft.nbt.CompoundTag();pending.putInt("Type",2);pending.putInt("Quality",3);pending.putInt("Bp",9950);n.put("Pending",pending);save(out.resolve(name+"-candidate.png"),screen);
            }
            for(var child:screen.children())if(child instanceof Button b&&b.getMessage().getString().contains("刻印")){screen.setFocused(b);b.setFocused(true);break;}
            menu.setData(9,0);menu.setData(10,12);save(out.resolve(name+"-disabled.png"),screen);
            results.add(Map.of("inscription",engraving,"resolution",List.of(size[0],size[1]),"guiScale",size[2],"passed",true));
            mc.setScreen(null);
        }
        mc.getWindow().setWindowed(1280,720);mc.options.guiScale().set(2);mc.resizeDisplay();
        Files.writeString(out.resolve("equipment-ui-verification.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(Map.of("passed",true,"cases",results)));
    }
    private static void save(Path file,EquipmentWorkstationScreen screen)throws Exception{
        var mc=Minecraft.getInstance();var projection=new Matrix4f(RenderSystem.getProjectionMatrix());var sorting=RenderSystem.getVertexSorting();var model=RenderSystem.getModelViewStack();model.pushPose();model.setIdentity();RenderSystem.applyModelViewMatrix();
        try{RenderSystem.setProjectionMatrix(new Matrix4f().setOrtho(0,mc.getWindow().getGuiScaledWidth(),mc.getWindow().getGuiScaledHeight(),0,1000,21000),VertexSorting.ORTHOGRAPHIC_Z);model.translate(0,0,-11000);RenderSystem.applyModelViewMatrix();mc.getMainRenderTarget().bindWrite(true);RenderSystem.clear(org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT|org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT,Minecraft.ON_OSX);var g=new GuiGraphics(mc,mc.renderBuffers().bufferSource());int mx=0,my=0;if(file.toString().contains("-hover")){var bf=screen.getClass().getDeclaredField("confirm");bf.setAccessible(true);var button=(Button)bf.get(screen);var sf=screen.getClass().getDeclaredField("uiScale");sf.setAccessible(true);float scale=sf.getFloat(screen);mx=(int)((button.getX()+4)*scale);my=(int)((button.getY()+4)*scale);}if(file.toString().contains("-equipment-hover")){var sf=screen.getClass().getDeclaredField("uiScale");sf.setAccessible(true);float scale=sf.getFloat(screen);mx=(int)(((screen.width-430)/2+150)*scale);my=(int)(((screen.height-330)/2+70)*scale);}screen.render(g,mx,my,0);if(file.toString().contains("-equipment-hover")){var hf=net.minecraft.client.gui.screens.inventory.AbstractContainerScreen.class.getDeclaredField("hoveredSlot");hf.setAccessible(true);if(hf.get(screen)!=screen.getMenu().slots.get(0))throw new AssertionError("Full-frame hover must select equipment slot");}g.flush();try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(file);int colored=0;for(int y=0;y<image.getHeight();y+=4)for(int x=0;x<image.getWidth();x+=4)if((image.getPixelRGBA(x,y)&0xFFFFFF)!=0)colored++;if(colored<1000)throw new AssertionError("Workbench screenshot blank");}if(org.lwjgl.opengl.GL11.glGetError()!=0)throw new AssertionError("Workbench GL error");}
        finally{model.popPose();RenderSystem.applyModelViewMatrix();RenderSystem.setProjectionMatrix(projection,sorting);}
    }
}
