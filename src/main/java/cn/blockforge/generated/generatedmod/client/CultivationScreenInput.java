package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** JEI overlays receive input first; remaining clicks belong to our page controls. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT)
public final class CultivationScreenInput {
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void click(ScreenEvent.MouseButtonPressed.Pre event) {
        if(event.getScreen() instanceof CultivationMainScreen screen
                &&screen.ownsClick(event.getMouseX(),event.getMouseY())) {
            screen.mouseClicked(event.getMouseX(),event.getMouseY(),event.getButton());
            event.setCanceled(true);
        }
    }
    @SubscribeEvent
    public static void tooltip(RenderTooltipEvent.GatherComponents event) {
        if(!(Minecraft.getInstance().screen instanceof CultivationMainScreen screen)||!screen.hasCustomPage())return;
        // JEI inherits InventoryScreen's crafting hotspot even while our inventory is hidden.
        event.getTooltipElements().removeIf(element->element.left().filter(text->text instanceof Component component
                &&component.getContents() instanceof TranslatableContents translated
                &&translated.getKey().equals("jei.tooltip.show.recipes")).isPresent());
    }
    private CultivationScreenInput() {}
}
