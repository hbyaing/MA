package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

/** Parchment category textures use Forge's native tab renderer; only page arrows need a proxy. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT)
public final class CreativeInventorySkin {
    private static java.lang.reflect.Field selectedTab;
    private static boolean unavailable;

    private static boolean ownCategory() {
        if(unavailable)return false;
        try {
            if(selectedTab==null)selectedTab=ObfuscationReflectionHelper.findField(CreativeModeInventoryScreen.class,"f_98507_");
            var tab=(CreativeModeTab)selectedTab.get(null);
            var id=BuiltInRegistries.CREATIVE_MODE_TAB.getKey(tab);
            return id!=null&&GeneratedMod.MOD_ID.equals(id.getNamespace());
        } catch(ReflectiveOperationException|RuntimeException error) {
            unavailable=true;
            com.mojang.logging.LogUtils.getLogger().warn("Creative page button skin unavailable; native navigation retained",error);
            return false;
        }
    }

    @SubscribeEvent public static void initialized(ScreenEvent.Init.Post event) {
        if(!(event.getScreen() instanceof CreativeModeInventoryScreen screen))return;
        for(var child:java.util.List.copyOf(screen.children())) {
            if(child instanceof Button button&&button.getClass()==Button.class
                    &&button.getWidth()==20&&button.getHeight()==20
                    &&button.getY()==screen.getGuiTop()-50
                    &&(button.getX()==screen.getGuiLeft()||button.getX()==screen.getGuiLeft()+screen.getXSize()-20)
                    &&("<".equals(button.getMessage().getString())||">".equals(button.getMessage().getString()))) {
                event.removeListener(button);
                event.addListener(new PageButton(button));
            }
        }
    }

    private static final class PageButton extends InkButton {
        private final Button original;
        PageButton(Button original) {
            super(original.getX(),original.getY(),original.getWidth(),original.getHeight(),original.getMessage(),ignored->original.onPress());
            this.original=original;
            active=original.active;visible=original.visible;
        }
        @Override protected void renderWidget(GuiGraphics graphics,int mouseX,int mouseY,float partialTick) {
            active=original.active;
            if(ownCategory()) {
                var texture=net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(GeneratedMod.MOD_ID,
                        "textures/gui/refining/"+("<".equals(getMessage().getString())?"previous":"next")+".png");
                com.mojang.blaze3d.systems.RenderSystem.enableBlend();
                com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
                float tint=!active?.45F:isHoveredOrFocused()?1F:.8F;
                graphics.setColor(tint,tint,tint,1);
                int sourceWidth="<".equals(getMessage().getString())?85:86;
                graphics.blit(texture,getX(),getY(),getWidth(),getHeight(),0,0,sourceWidth,85,sourceWidth,85);
                graphics.setColor(1,1,1,1);
                com.mojang.blaze3d.systems.RenderSystem.disableBlend();
            }
            else {
                original.setX(getX());original.setY(getY());original.setFocused(isFocused());
                original.render(graphics,mouseX,mouseY,partialTick);
            }
        }
    }
    private CreativeInventorySkin() {}
}
