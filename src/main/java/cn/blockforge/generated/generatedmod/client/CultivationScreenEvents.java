package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** 仅替换原版生存背包，不抢占其他模组的背包子类和世界容器。 */
@Mod.EventBusSubscriber(modid = GeneratedMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CultivationScreenEvents {
    private CultivationScreenEvents() {}
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void relocateAccessories(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof CultivationMainScreen main)) return;
        for (var child : java.util.List.copyOf(main.children())) {
            if (child instanceof net.minecraft.client.gui.components.Button original
                    && child.getClass().getName().equals("top.theillusivec4.curios.client.gui.CuriosButton")) {
                event.removeListener(original);
                var proxy = new net.minecraft.client.gui.components.ImageButton(0, 0, 14, 14, 50, 0, 14,
                        net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("curios", "textures/gui/inventory.png"),
                        ignored -> original.onPress());
                proxy.setTooltip(net.minecraft.client.gui.components.Tooltip.create(net.minecraft.network.chat.Component.literal("饰品")));
                event.addListener(proxy);
                main.attachAccessories(proxy);
                break;
            }
        }
    }
    @SubscribeEvent
    public static void effects(ScreenEvent.RenderInventoryMobEffects event) {
        if (event.getScreen() instanceof CultivationMainScreen) event.setCanceled(true);
    }
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void open(ScreenEvent.Opening event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.gameMode == null || event.getNewScreen() == null) return;
        if (event.getNewScreen().getClass() == InventoryScreen.class) {
            event.setNewScreen(new CultivationMainScreen(mc.player));
        } else if (event.getNewScreen() instanceof InnerVisionScreen) {
            event.setNewScreen(new CultivationMainScreen(mc.player, "vision"));
        }
    }
}
