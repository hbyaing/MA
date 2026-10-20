package cn.blockforge.generated.generatedmod.client.firecoil;

import cn.blockforge.generated.generatedmod.XuanlingAnvilMenu;
import cn.blockforge.generated.generatedmod.client.XuanlingAnvilScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.*;
import java.nio.file.*;
import java.util.*;

final class AnvilUiChecks {
    static void run(Path out)throws Exception {
        var mc=Minecraft.getInstance();var results=new ArrayList<Map<String,Object>>();
        for(int gui:new int[]{2,3}){
            mc.setScreen(null);mc.getWindow().setWindowed(1280,960);mc.options.guiScale().set(gui);mc.resizeDisplay();
            var menu=new XuanlingAnvilMenu(76,mc.player.getInventory(),ContainerLevelAccess.NULL);
            var stacks=new ArrayList<ItemStack>();
            net.minecraft.core.registries.BuiltInRegistries.ITEM.stream().filter(i->i instanceof SmithingTemplateItem).limit(12).forEach(i->stacks.add(new ItemStack(i)));
            var f=XuanlingAnvilMenu.class.getDeclaredField("templates");f.setAccessible(true);f.set(menu,stacks);
            var screen=new XuanlingAnvilScreen(menu,mc.player.getInventory(),Component.literal("玄灵铁砧"));mc.setScreen(screen);
            if(menu.getSlot(0).x!=49||menu.getSlot(1).x!=91||menu.getSlot(2).x!=133)throw new AssertionError("Operation slots not compacted separately from template");
            var buttons=screen.children().stream().filter(c->c instanceof Button b&&b.getWidth()==114&&b.getHeight()==18).toList();
            if(buttons.size()!=7)throw new AssertionError("Expected seven template rows");
            int lastBottom=buttons.stream().mapToInt(c->((Button)c).getY()+((Button)c).getHeight()).max().orElseThrow();
            for(var c:screen.children())if(c instanceof Button b&&b.getWidth()==28&&b.getY()<lastBottom+2)throw new AssertionError("Template footer overlaps last row");
            save(out.resolve("anvil-ui-gui"+gui+".png"),screen);
            var next=screen.children().stream().filter(c->c instanceof Button b&&b.getMessage().getString().equals("›")).map(c->(Button)c).findFirst().orElseThrow();next.onPress();
            if(screen.children().stream().filter(c->c instanceof Button b&&b.getWidth()==114&&b.getHeight()==18).count()!=5)throw new AssertionError("Last template page wrong");
            save(out.resolve("anvil-ui-gui"+gui+"-page2.png"),screen);
            var sf=screen.getClass().getDeclaredField("templateSearch");sf.setAccessible(true);var search=(EditBox)sf.get(screen);
            search.setValue("无此模板");
            if(screen.children().stream().anyMatch(c->c instanceof Button b&&b.getWidth()==114&&b.getHeight()==18))throw new AssertionError("Empty template search retains rows");
            save(out.resolve("anvil-ui-gui"+gui+"-empty.png"),screen);search.setValue("");
            if(!menu.clickMenuButton(mc.player,11)||!ItemStack.isSameItemSameTags(menu.selectedTemplate().stack(),stacks.get(9)))throw new AssertionError("Template selection must keep the original library index");
            save(out.resolve("anvil-ui-gui"+gui+"-selected.png"),screen);
            var carried=new ItemStack(Items.DIAMOND,3);menu.setCarried(carried.copy());
            int previewX=(screen.width-176)/2+63+9,previewY=(screen.height-206)/2+menu.getSlot(0).y;
            screen.mouseClicked(previewX,previewY,0);
            if(!ItemStack.matches(carried,menu.getCarried())||menu.slots.size()!=39)throw new AssertionError("Read-only template display changed inventory");
            menu.setCarried(ItemStack.EMPTY);
            menu.clickMenuButton(mc.player,0);if(!menu.selectedTemplate().stack().isEmpty())throw new AssertionError("Repair mode must clear display");
            menu.clickMenuButton(mc.player,1);if(!ItemStack.isSameItemSameTags(menu.selectedTemplate().stack(),stacks.get(9)))throw new AssertionError("Forge mode must restore selection without cycling");
            f.set(menu,List.of());screen.resize(mc,mc.getWindow().getGuiScaledWidth(),mc.getWindow().getGuiScaledHeight());
            var forge=screen.children().stream().filter(c->c instanceof Button b&&b.getMessage().getString().equals("锻造")).map(c->(Button)c).findFirst().orElseThrow();
            if(forge.active||menu.clickMenuButton(mc.player,1))throw new AssertionError("Forge without templates must be disabled");
            save(out.resolve("anvil-ui-gui"+gui+"-forge-disabled.png"),screen);
            results.add(Map.of("gui",gui,"rows",7,"lastPage",5,"emptySearch",true));
        }
        mc.setScreen(null);Files.writeString(out.resolve("anvil-ui.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(Map.of("passed",true,"cases",results)));
    }
    private static void save(Path path,XuanlingAnvilScreen screen)throws Exception{
        TalismanUiChecks.render(path,()->{var mc=Minecraft.getInstance();var g=new GuiGraphics(mc,mc.renderBuffers().bufferSource());screen.render(g,0,0,0);g.flush();});
    }
}
