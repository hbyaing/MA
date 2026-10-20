package cn.blockforge.generated.generatedmod.client.firecoil;

import cn.blockforge.generated.generatedmod.*;
import cn.blockforge.generated.generatedmod.client.TalismanPlatformScreen;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import java.nio.file.*;
import java.lang.reflect.*;
import java.util.*;

/** Actual registered models and mouse/resize/Esc behavior in a disposable client. */
final class TalismanUiChecks {
    private static double scale(TalismanPlatformScreen s)throws Exception{return field(s.getClass(),"uiScale").getFloat(s);}
    private static void click(TalismanPlatformScreen s,double x,double y)throws Exception{s.mouseClicked(x*scale(s),y*scale(s),0);s.mouseReleased(x*scale(s),y*scale(s),0);}
    private static void button(TalismanPlatformScreen s,String text)throws Exception{
        var b=s.children().stream().filter(c->c instanceof net.minecraft.client.gui.components.Button w&&w.getMessage().getString().equals(text)).map(c->(net.minecraft.client.gui.components.Button)c).findFirst().orElseThrow();
        click(s,b.getX()+b.getWidth()/2.0,b.getY()+b.getHeight()/2.0);
    }
    private static Field field(Class<?> c,String n)throws Exception{var f=c.getDeclaredField(n);f.setAccessible(true);return f;}
    private static int coordinate(Object area,String name)throws Exception{var m=area.getClass().getDeclaredMethod(name);m.setAccessible(true);return (int)m.invoke(area);}
    private static String strokes(TalismanPlatformScreen s)throws Exception{return java.util.HexFormat.of().formatHex(TalismanCatalog.encode((boolean[][])field(s.getClass(),"drawn").get(s)));}
    static void render(Path file,Runnable drawing)throws Exception{
        var mc=Minecraft.getInstance();var projection=new org.joml.Matrix4f(RenderSystem.getProjectionMatrix());var sorting=RenderSystem.getVertexSorting();
        var model=RenderSystem.getModelViewStack();model.pushPose();model.setIdentity();model.translate(0,0,-11000);RenderSystem.applyModelViewMatrix();
        RenderSystem.setProjectionMatrix(new org.joml.Matrix4f().setOrtho(0,mc.getWindow().getGuiScaledWidth(),mc.getWindow().getGuiScaledHeight(),0,1000,21000),VertexSorting.ORTHOGRAPHIC_Z);
        try{mc.getMainRenderTarget().bindWrite(true);RenderSystem.clear(org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT|org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT,Minecraft.ON_OSX);drawing.run();try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(file);}
            if(org.lwjgl.opengl.GL11.glGetError()!=0)throw new AssertionError("Talisman UI GL error");
        }finally{model.popPose();RenderSystem.applyModelViewMatrix();RenderSystem.setProjectionMatrix(projection,sorting);}
    }
    static void run(Path out)throws Exception{
        var mc=Minecraft.getInstance();var constructor=TalismanPlatformMenu.class.getDeclaredConstructor(int.class,net.minecraft.world.entity.player.Inventory.class,net.minecraft.world.Container.class,TalismanPlatformBlockEntity.Kind.class,net.minecraft.world.level.Level.class,net.minecraft.core.BlockPos.class);constructor.setAccessible(true);
        var results=new ArrayList<Map<String,Object>>();
        mc.getWindow().setWindowed(1470,850);mc.options.guiScale().set(1);mc.resizeDisplay();
        render(out.resolve("registered-talismans.png"),()->{
            var g=new GuiGraphics(mc,mc.renderBuffers().bufferSource());g.fill(0,0,mc.getWindow().getGuiScaledWidth(),mc.getWindow().getGuiScaledHeight(),0xFF181D25);int i=0;
            for(String id:TalismanCatalog.ids()){
                var item=new ItemStack(WuXingItems.talismanItem(id));var model=mc.getItemRenderer().getModel(item,mc.level,mc.player,0);
                if(!model.getParticleIcon().contents().name().toString().equals("wuxingxiantu:item/talisman_"+id))throw new AssertionError("Wrong independent item texture: "+id);
                var legacy=new ItemStack(WuXingItems.TALISMAN.get());TalismanData.setTalisman(legacy,id);
                if(!mc.getItemRenderer().getModel(legacy,mc.level,mc.player,0).getParticleIcon().contents().name().equals(model.getParticleIcon().contents().name()))throw new AssertionError("Legacy model changed");
                int x=25+i++*200;g.pose().pushPose();g.pose().translate(x,45,0);g.pose().scale(8,8,1);g.renderItem(item,0,0);g.pose().popPose();
                g.drawString(mc.font,item.getHoverName(),x,185,0xFFFFFF);g.drawString(mc.font,"talisman_"+id,x,205,0xFFFFFF);
            }g.flush();
        });
        for(var kind:List.of(TalismanPlatformBlockEntity.Kind.DRAW,TalismanPlatformBlockEntity.Kind.ENGRAVE))for(int[] size:new int[][]{{1280,720,2},{1920,1080,2},{1280,960,4}}){
            mc.setScreen(null);mc.getWindow().setWindowed(size[0],size[1]);mc.options.guiScale().set(size[2]);mc.resizeDisplay();
            var menu=(TalismanPlatformMenu)constructor.newInstance(73,mc.player.getInventory(),new net.minecraft.world.SimpleContainer(TalismanPlatformBlockEntity.SIZE),kind,mc.level,mc.player.blockPosition());
            var screen=new TalismanPlatformScreen(menu,mc.player.getInventory(),Component.literal(kind==TalismanPlatformBlockEntity.Kind.DRAW?"绘制台":"镌印台"));mc.setScreen(screen);
            int inkStart=TalismanPlatformBlockEntity.inkStart(kind);
            if(inkStart>=0){menu.getSlot(inkStart).set(new ItemStack(WuXingItems.SPIRIT_INK.get()));menu.getSlot(inkStart+1).set(new ItemStack(WuXingItems.CINNABAR_INK.get()));}
            int left=(screen.width-TalismanPlatformMenu.PANEL_WIDTH)/2,top=(screen.height-TalismanPlatformMenu.PANEL_HEIGHT)/2;
            var normalCanvas=screen.getClass().getDeclaredMethod("canvas");normalCanvas.setAccessible(true);Object normalArea=normalCanvas.invoke(screen);
            click(screen,coordinate(normalArea,"x")+1,coordinate(normalArea,"y")+1);String small=strokes(screen);
            String prefix=kind.name().toLowerCase()+"-"+size[0]+"-gui"+size[2];
            var search=(net.minecraft.client.gui.components.EditBox)field(screen.getClass(),"search").get(screen);
            search.setValue("无此符箓");
            if(!((List<?>)field(screen.getClass(),"filtered").get(screen)).isEmpty())throw new AssertionError("Talisman empty search retains rows");
            render(out.resolve(prefix+"-empty-search.png"),()->{var g=new GuiGraphics(mc,mc.renderBuffers().bufferSource());screen.render(g,0,0,0);g.flush();});
            search.setValue("符");search.setFocused(true);
            render(out.resolve(prefix+"-search-text.png"),()->{var g=new GuiGraphics(mc,mc.renderBuffers().bufferSource());screen.render(g,0,0,0);g.flush();});
            search.setValue("");search.setFocused(false);
            screen.mouseScrolled((left+50)*scale(screen),(top+120)*scale(screen),-1);
            if(field(screen.getClass(),"scroll").getInt(screen)!=1)throw new AssertionError("Talisman list wheel scroll failed");
            screen.mouseScrolled((left+50)*scale(screen),(top+120)*scale(screen),1);
            screen.mouseClicked((left+118)*scale(screen),(top+105)*scale(screen),0);
            screen.mouseDragged((left+118)*scale(screen),(top+212)*scale(screen),0,0,107*scale(screen));
            screen.mouseReleased((left+118)*scale(screen),(top+212)*scale(screen),0);
            if(field(screen.getClass(),"scroll").getInt(screen)<=0)throw new AssertionError("Talisman scrollbar drag did not move list");
            search.setValue("不存在的符箓");search.setValue("");
            render(out.resolve(prefix+"-normal.png"),()->{var g=new GuiGraphics(mc,mc.renderBuffers().bufferSource());screen.render(g,0,0,0);g.flush();});
            if(kind==TalismanPlatformBlockEntity.Kind.DRAW&&size[0]==1920){
                var action=screen.children().stream().filter(c->c instanceof net.minecraft.client.gui.components.Button b&&b.getMessage().getString().equals("绘制符箓")).map(c->(net.minecraft.client.gui.components.Button)c).findFirst().orElseThrow();
                render(out.resolve("draw-button-hover.png"),()->{var g=new GuiGraphics(mc,mc.renderBuffers().bufferSource());screen.render(g,(int)((action.getX()+2)*safeScale(screen)),(int)((action.getY()+2)*safeScale(screen)),0);g.flush();});
                action.setFocused(true);render(out.resolve("draw-button-focus.png"),()->{var g=new GuiGraphics(mc,mc.renderBuffers().bufferSource());screen.render(g,0,0,0);g.flush();});action.setFocused(false);
                action.active=false;render(out.resolve("draw-button-disabled.png"),()->{var g=new GuiGraphics(mc,mc.renderBuffers().bufferSource());screen.render(g,0,0,0);g.flush();});action.active=true;
            }
            button(screen,"放大绘制");
            if(!field(screen.getClass(),"expanded").getBoolean(screen)||!strokes(screen).equals(small))throw new AssertionError("Expand lost strokes or button not clickable");
            var canvas=screen.getClass().getDeclaredMethod("canvas");canvas.setAccessible(true);Object area=canvas.invoke(screen);
            int x=coordinate(area,"x"),y=coordinate(area,"y"),cell=coordinate(area,"cell");
            for(var raw:(List<?>)field(screen.getClass(),"inkButtons").get(screen)){
                var ink=(net.minecraft.client.gui.components.Button)raw;
                if(ink.getX()<=x+16*cell+3)throw new AssertionError("Expanded brush overlaps paper edge");
            }
            if(inkStart>=0){
                if(kind==TalismanPlatformBlockEntity.Kind.DRAW)button(screen,"选用墨水 2");else click(screen,x+16*cell+115,y+32);
                if(menu.selectedInk()!=1)throw new AssertionError("Ink checkbox did not select second bottle");
                var color=screen.getClass().getDeclaredMethod("inkColor");color.setAccessible(true);
                if((int)color.invoke(screen)!=0xFFC0392B)throw new AssertionError("Ink selection and brush color disagree");
                int actions=y+96;
                if(actions<y+72)throw new AssertionError("Bottom action block overlaps ink slots");
            }
            if(cell<=3||x<0||y<0||x+16*cell+100>screen.width||y+32*cell>screen.height)throw new AssertionError("Expanded canvas does not fit/enlarge: "+prefix);
            screen.mouseClicked((x+5*cell+cell*.5)*scale(screen),(y+8*cell+cell*.5)*scale(screen),0);screen.mouseDragged((x+8*cell+cell*.5)*scale(screen),(y+10*cell+cell*.5)*scale(screen),0,3*cell*scale(screen),2*cell*scale(screen));screen.mouseReleased(x*scale(screen),y*scale(screen),0);
            boolean[][] drawn=(boolean[][])field(screen.getClass(),"drawn").get(screen);
            if(!drawn[8][5]||!drawn[10][8]||!drawn[0][0])throw new AssertionError("Expanded pointer mapping or stroke continuity wrong");
            String expanded=strokes(screen);screen.resize(mc,mc.getWindow().getGuiScaledWidth(),mc.getWindow().getGuiScaledHeight());
            if(!strokes(screen).equals(expanded))throw new AssertionError("Resize discarded strokes");
            render(out.resolve(prefix+"-expanded.png"),()->{var g=new GuiGraphics(mc,mc.renderBuffers().bufferSource());screen.render(g,0,0,0);g.flush();});
            screen.keyPressed(org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE,0,0);
            if(field(screen.getClass(),"expanded").getBoolean(screen)||mc.screen!=screen||!strokes(screen).equals(expanded))throw new AssertionError("Esc closes menu or loses strokes");
            button(screen,"放大绘制");button(screen,"返回工作台");
            if(field(screen.getClass(),"expanded").getBoolean(screen)||!strokes(screen).equals(expanded))throw new AssertionError("Back button lost strokes");
            if(screen.children().stream().anyMatch(c->c instanceof net.minecraft.client.gui.components.Button b&&b.getMessage().getString().equals("清空笔迹")))throw new AssertionError("Normal UI must not expose an eraser");
            button(screen,"放大绘制");
            if(screen.children().stream().anyMatch(c->c instanceof net.minecraft.client.gui.components.Button b&&b.getMessage().getString().equals("清空笔迹")))throw new AssertionError("Expanded UI must not expose an eraser");
            if(!strokes(screen).equals(expanded))throw new AssertionError("Expanding without an eraser must preserve strokes");
            results.add(Map.of("kind",kind.toString(),"window",List.of(size[0],size[1]),"guiScale",size[2],"logicalSize",List.of(screen.width,screen.height),"expandedCell",cell,"passed",true));
        }
        for(int[] rubSize:new int[][]{{1280,720,2},{1920,1080,2},{1280,960,4}}){
            int gui=rubSize[2];String rubPrefix="rub-"+rubSize[0]+"-gui"+gui;
            mc.setScreen(null);mc.getWindow().setWindowed(rubSize[0],rubSize[1]);mc.options.guiScale().set(gui);mc.resizeDisplay();
            var menu=(TalismanPlatformMenu)constructor.newInstance(73,mc.player.getInventory(),new net.minecraft.world.SimpleContainer(TalismanPlatformBlockEntity.SIZE),TalismanPlatformBlockEntity.Kind.RUB,mc.level,mc.player.blockPosition());
            var screen=new TalismanPlatformScreen(menu,mc.player.getInventory(),Component.literal("拓印台"));mc.setScreen(screen);
            render(out.resolve(rubPrefix+"-empty.png"),()->{var g=new GuiGraphics(mc,mc.renderBuffers().bufferSource());screen.render(g,0,0,0);g.flush();});
            var seal=new ItemStack(WuXingItems.SEAL.get());TalismanData.setTalisman(seal,"thunder");TalismanData.setQuality(seal,TalismanCatalog.Quality.MID);menu.getSlot(TalismanPlatformBlockEntity.RUB_SEAL).set(seal);
            menu.getSlot(TalismanPlatformBlockEntity.RUB_PAPER).set(new ItemStack(WuXingItems.TALISMAN_PAPER.get(),32));menu.getSlot(TalismanPlatformBlockEntity.OUTPUT).set(TalismanData.create("thunder",16,TalismanCatalog.Quality.MID));
            menu.getSlot(TalismanPlatformBlockEntity.inkStart(menu.kind())+1).set(new ItemStack(WuXingItems.CINNABAR_INK.get()));
            button(screen,"选用墨水 2");if(menu.selectedInk()!=1)throw new AssertionError("Compact rubbing ink choice failed");
            if(screen.children().size()!=4)throw new AssertionError("Compact rubbing retained nonessential widgets");
            for(int i=TalismanPlatformBlockEntity.SIZE;i<menu.slots.size();i++){var slot=menu.getSlot(i);if(slot.x<0||slot.x+16>240||slot.y<0||slot.y+16>250)throw new AssertionError("Compact inventory outside frame");}
            for(int row=0;row<3;row++)for(int col=0;col<9;col++){var slot=menu.getSlot(TalismanPlatformBlockEntity.SIZE+row*9+col);if(slot.x!=39+18*col||slot.y!=165+18*row)throw new AssertionError("Compact inventory is not contiguous vanilla layout");}
            for(int col=0;col<9;col++){var slot=menu.getSlot(TalismanPlatformBlockEntity.SIZE+27+col);if(slot.x!=39+18*col||slot.y!=222)throw new AssertionError("Compact hotbar layout changed");}
            menu.getSlot(TalismanPlatformBlockEntity.inkStart(menu.kind())).set(new ItemStack(WuXingItems.SPIRIT_INK.get()));
            render(out.resolve(rubPrefix+"-seal.png"),()->{var g=new GuiGraphics(mc,mc.renderBuffers().bufferSource());screen.render(g,0,0,0);g.flush();});
            var batch=screen.children().stream().filter(c->c instanceof net.minecraft.client.gui.components.Button b&&b.getMessage().getString().equals("批量拓印")).map(c->(net.minecraft.client.gui.components.Button)c).findFirst().orElseThrow();
            render(out.resolve(rubPrefix+"-hover.png"),()->{var g=new GuiGraphics(mc,mc.renderBuffers().bufferSource());screen.render(g,(int)((batch.getX()+2)*safeScale(screen)),(int)((batch.getY()+2)*safeScale(screen)),0);g.flush();});
            batch.setFocused(true);render(out.resolve(rubPrefix+"-focus.png"),()->{var g=new GuiGraphics(mc,mc.renderBuffers().bufferSource());screen.render(g,0,0,0);g.flush();});batch.setFocused(false);
            if(!batch.active)throw new AssertionError("Valid rubbing batch should be enabled");
            var copiesMethod=TalismanPlatformScreen.class.getDeclaredMethod("rubCopies");copiesMethod.setAccessible(true);
            if((int)copiesMethod.invoke(screen)!=16)throw new AssertionError("Batch preview differs from server limit");
            var selectedInk=menu.getSlot(TalismanPlatformBlockEntity.inkStart(menu.kind())+1).getItem();
            selectedInk.setDamageValue(selectedInk.getMaxDamage()-3*TalismanCatalog.drawCost("thunder"));
            if((int)copiesMethod.invoke(screen)!=3)throw new AssertionError("Selected ink capacity not reflected");
            selectedInk.setDamageValue(selectedInk.getMaxDamage());
            render(out.resolve(rubPrefix+"-disabled.png"),()->{var g=new GuiGraphics(mc,mc.renderBuffers().bufferSource());screen.render(g,0,0,0);g.flush();});
            if(batch.active||(int)copiesMethod.invoke(screen)!=0)throw new AssertionError("Exhausted ink must disable batch");
            selectedInk.setDamageValue(0);
            menu.getSlot(TalismanPlatformBlockEntity.OUTPUT).set(TalismanData.create("thunder",63,TalismanCatalog.Quality.MID));
            var blockedMethod=TalismanPlatformScreen.class.getDeclaredMethod("rubOutputBlocked");blockedMethod.setAccessible(true);
            if(!(boolean)blockedMethod.invoke(screen)||(int)copiesMethod.invoke(screen)!=0)throw new AssertionError("Partial output space must report blocked batch");
            menu.getSlot(TalismanPlatformBlockEntity.OUTPUT).set(ItemStack.EMPTY);
            menu.getSlot(TalismanPlatformBlockEntity.RUB_PAPER).set(ItemStack.EMPTY);
            if((int)copiesMethod.invoke(screen)!=0)throw new AssertionError("Missing paper must disable batch");
            if(screen.children().stream().anyMatch(c->c instanceof net.minecraft.client.gui.components.Button b&&b.getMessage().getString().equals("放大绘制")))throw new AssertionError("Rubbing must not expose drawing controls");
        }
        mc.setScreen(null);Files.writeString(out.resolve("registered-talisman-ui.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(Map.of("passed",true,"newModels",7,"legacyModels",7,"canvasCases",results)));
    }
    private static double safeScale(TalismanPlatformScreen screen){try{return scale(screen);}catch(Exception e){throw new IllegalStateException(e);}}
}
