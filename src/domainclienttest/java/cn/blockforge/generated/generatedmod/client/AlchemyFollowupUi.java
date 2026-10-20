package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.*;
import cn.blockforge.generated.generatedmod.alchemy.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.nio.file.Path;

/** Test-only real menu/UI follow-up acceptance, never packaged in the mod. */
final class AlchemyFollowupUi {
    static final String[] NAMES={"roots-five-hd","roots-mutated-hd","focused-root-mana","focused-root-attack",
            "alchemy-special","alchemy-essence-tooltip","alchemy-ash","alchemy-ash-collected","alchemy-free-waste","special-pill-tooltip",
            "hover-gold","hover-wood","hover-water","hover-fire","hover-earth","hover-thunder","hover-exotic","hover-missing-gold"};
    private static final BlockPos CORE=new BlockPos(0,179,0);
    private static AbstractWidget focusedRoot;
    private static Object field(Object target,String name)throws Exception{
        for(Class<?> type=target.getClass();type!=null;type=type.getSuperclass())try{
            var f=type.getDeclaredField(name);f.setAccessible(true);return f.get(target);
        }catch(NoSuchFieldException ignored){}throw new NoSuchFieldException(name);
    }
    static CompletableFuture<?> configure(Minecraft mc,int stage)throws Exception{
        var id=mc.player.getUUID();
        if(stage<=3||stage==9||stage>=10){
            mc.setScreen(new CultivationMainScreen(mc.player,"vision"));
            return mc.getSingleplayerServer().submitAsync(()->{
                var p=mc.getSingleplayerServer().getPlayerList().getPlayer(id);var data=GeneratedMod.data(p);
                data.wuxingxiantu$setThunderRootActive(stage==1||stage==15);data.wuxingxiantu$setExoticRootActive(stage==1||stage==16);
                if(stage>=10)data.wuxingxiantu$setRootMask(stage==17?30:31);
                GeneratedMod.sync(p);
            });
        }
        if(stage==5)return null;
        if(stage==7){
            var screen=(AlchemyScreen)mc.screen;
            screen.mouseClicked((int)field(screen,"leftPos")+AlchemyUiLayout.CENTER_X,(int)field(screen,"topPos")+AlchemyUiLayout.CENTER_Y,0);
            return null;
        }
        return mc.getSingleplayerServer().submitAsync(()->{
            var p=mc.getSingleplayerServer().getPlayerList().getPlayer(id);var level=p.serverLevel();
            if(stage==4){
                p.closeContainer();level.setBlock(CORE,WuXingBlocks.ALCHEMY_FURNACE_IMMORTAL.get().defaultBlockState(),3);AlchemyFurnaceStructure.refreshAround(level,CORE);
                var furnace=(AlchemyFurnaceBlockEntity)level.getBlockEntity(CORE);furnace.setItem(8,cn.blockforge.generated.generatedmod.fire.FireBottleData.fresh(9));
                FormulaKnowledge.learnAll(p);GeneratedMod.data(p).wuxingxiantu$learnFire(9);
                net.minecraftforge.network.NetworkHooks.openScreen(p,furnace.menuProvider(),CORE);
                var menu=(AlchemyFurnaceMenu)p.containerMenu;
                for(int i=0;i<menu.recipes().size();i++)if(PillData.read(menu.recipes().get(i).getResultItem(level.registryAccess())).pill()==PillCatalog.CALMING){menu.clickMenuButton(p,2+i);break;}
            }else if(stage==6){
                var furnace=(AlchemyFurnaceBlockEntity)level.getBlockEntity(CORE);
                for(int i=0;i<8;i++)furnace.setItem(i,ItemStack.EMPTY);
                furnace.setItem(0,new ItemStack(cn.blockforge.generated.generatedmod.plants.SpiritPlantRegistry.SPIRIT_SPROUT_GRASS_ITEM.get(),4));
                furnace.reserve(FreeAlchemy.recipe(furnace));furnace.startBrewing(level.getGameTime(),level.getGameTime()+100);
                if(!furnace.failExperiment())throw new AssertionError("Failed batch did not leave ash");
            }else if(stage==8){
                var furnace=(AlchemyFurnaceBlockEntity)level.getBlockEntity(CORE);
                furnace.setItem(0,new ItemStack(cn.blockforge.generated.generatedmod.plants.SpiritPlantRegistry.SPIRIT_SPROUT_GRASS_ITEM.get(),3));p.containerMenu.clickMenuButton(p,255);
            }
            GeneratedMod.sync(p);
        });
    }
    static Map<String,Object> capture(Minecraft mc,int stage,Path output)throws Exception{
        var screen=mc.screen;if(screen==null)throw new AssertionError("Follow-up screen absent");int mx=0,my=0;
        var row=new LinkedHashMap<String,Object>();row.put("name",NAMES[stage]);
        if(stage<=3||stage>=10){
            var main=(CultivationMainScreen)screen;var page=(CultivationVisionPage)field(main,"page");
            var layout=(CultivationVisionLayout)field(page,"layout");
            int cx=(int)field(main,"contentX"),cy=(int)field(main,"contentY");float scale=(float)field(main,"pageScale");
            if(stage==2||stage==3){
                var roots=layout.roots();focusedRoot=null;
                for(var child:page.children())if(child instanceof AbstractWidget widget&&widget.getY()==roots.y()+18&&widget.getMessage().getString().equals("wood"))focusedRoot=widget;
                if(focusedRoot==null)throw new AssertionError("Root widget not found");page.setFocused(focusedRoot);
                var status=layout.status();mx=cx+Math.round((status.x()+48)*scale);my=cy+Math.round((status.y()+(stage==2?74:125))*scale);
                if(!focusedRoot.isFocused())throw new AssertionError("Root focus was not retained");row.put("rootFocused",true);
            }
            if(stage>=10){
                int root=stage<=14?stage-10:stage==15?1:stage==16?3:0;var roots=layout.roots();AbstractWidget target=null;
                String[] names={"gold","wood","water","fire","earth"};
                for(var child:page.children())if(child instanceof AbstractWidget widget&&widget.getY()==roots.y()+18&&widget.getMessage().getString().equals(names[root]))target=widget;
                if(target==null)throw new AssertionError("Root hover target absent");
                mx=cx+Math.round((target.getX()+14)*scale);my=cy+Math.round((target.getY()+14)*scale);
                row.put("buttonActive",target.active);row.put("expectedRoot",root);
                if((root==0||root==2||root==4)&&target.active)throw new AssertionError("Passive root unexpectedly clickable");
            }
        }else if(stage<9){
            if(!(screen instanceof AlchemyScreen))throw new AssertionError("Actual alchemy menu absent");
            var menu=(AlchemyFurnaceMenu)mc.player.containerMenu;
            if(stage==4&&PillData.read(menu.selectedRecipe().getResultItem(mc.level.registryAccess())).pill()!=PillCatalog.CALMING)throw new AssertionError("Special pill recipe not selected");
            if(stage==5){mx=(int)field(screen,"leftPos")+320;my=(int)field(screen,"topPos")+161;}
            if(stage==6&&!menu.furnace().completedResult().is(WuXingItems.PILL_ASH.get()))throw new AssertionError("Ash not synchronized to UI");
            if(stage==7&&menu.furnace().hasResult())throw new AssertionError("Center UI click did not collect ash");
            if(stage==7&&!menu.getCarried().is(WuXingItems.PILL_ASH.get()))throw new AssertionError("Normal center click did not put ash on cursor");
            if(stage==8&&!menu.freeMode())throw new AssertionError("Free alchemy mode not active");
            row.put("failurePercent",AlchemyFailureOdds.percent(menu.selectedRecipe(),menu.furnaceTier(),menu.ignitionTier(),menu.freeMode()));row.put("freeMode",menu.freeMode());
            row.put("hasResult",menu.furnace().hasResult());row.put("result",menu.furnace().completedResult().getHoverName().getString());
        }
        mc.getMainRenderTarget().bindWrite(true);
        com.mojang.blaze3d.systems.RenderSystem.setProjectionMatrix(new org.joml.Matrix4f().setOrtho(0,screen.width,screen.height,0,1000,21000),com.mojang.blaze3d.vertex.VertexSorting.ORTHOGRAPHIC_Z);
        var model=com.mojang.blaze3d.systems.RenderSystem.getModelViewStack();model.pushPose();model.setIdentity();model.translate(0,0,-11000);com.mojang.blaze3d.systems.RenderSystem.applyModelViewMatrix();
        try{
            com.mojang.blaze3d.systems.RenderSystem.clear(org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT,Minecraft.ON_OSX);
            var gui=new TrackingGraphics(mc);screen.renderWithTooltip(gui,mx,my,mc.getFrameTime());
            if(stage==9){var pill=PillData.create(PillCatalog.CALMING,PillData.Grade.PERFECT,PillData.Element.METAL,0);
                gui.renderComponentTooltip(mc.font,pill.getTooltipLines(mc.player,net.minecraft.world.item.TooltipFlag.Default.NORMAL),screen.width/2,screen.height/2);}
            gui.flush();String tooltip=gui.tooltip.toString();row.put("actualTooltip",tooltip);
            if(stage==2&&(!tooltip.contains("灵力")||tooltip.contains("灵根")))throw new AssertionError("Focused root overrode mana tooltip: "+tooltip);
            if(stage==3&&(!tooltip.contains("攻击")||tooltip.contains("灵根")))throw new AssertionError("Focused root overrode attack tooltip: "+tooltip);
            if(stage==5&&!tooltip.contains("存量 / 本炉吸收增量"))throw new AssertionError("Essence explanation missing");
            if(stage==9&&(!tooltip.contains("特殊")||tooltip.contains("下品")))throw new AssertionError("Special pill still graded: "+tooltip);
            if(stage>=10){
                String[] expected={"金灵根","木灵根","水灵根","火灵根","土灵根","天雷灵根","异火灵根","金灵根"};
                if(!tooltip.contains(expected[stage-10])||!tooltip.contains(stage==17?"未拥有":"已拥有"))throw new AssertionError("Root description absent or wrong: "+tooltip);
            }
            int error=org.lwjgl.opengl.GL11.glGetError();if(error!=0)throw new AssertionError("Follow-up GL error "+error);row.put("glError",error);
            if(mc.getMainRenderTarget().width!=1280||mc.getMainRenderTarget().height!=900)throw new AssertionError("Follow-up viewport resized");
            try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(output.resolve(NAMES[stage]+".png"));}
        }finally{model.popPose();com.mojang.blaze3d.systems.RenderSystem.applyModelViewMatrix();}
        return row;
    }
    private static final class TrackingGraphics extends GuiGraphics {
        final StringBuilder tooltip=new StringBuilder();
        TrackingGraphics(Minecraft mc){super(mc,mc.renderBuffers().bufferSource());}
        @Override public void renderTooltip(Font font,List<net.minecraft.util.FormattedCharSequence> lines,net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner positioner,int x,int y){
            for(var line:lines){line.accept((index,style,codepoint)->{tooltip.appendCodePoint(codepoint);return true;});tooltip.append('\n');}
            super.renderTooltip(font,lines,positioner,x,y);
        }
        @Override public void renderComponentTooltip(Font font,List<Component> lines,int x,int y){
            lines.forEach(line->tooltip.append(line.getString()).append('\n'));super.renderComponentTooltip(font,lines,x,y);
        }
    }
}
