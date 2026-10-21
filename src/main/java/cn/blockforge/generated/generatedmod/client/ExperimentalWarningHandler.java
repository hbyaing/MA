package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.HudConfig;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.ConfirmExperimentalFeaturesScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = GeneratedMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ExperimentalWarningHandler {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onOpening(ScreenEvent.Opening event) {
        if (!HudConfig.HIDE_EXPERIMENTAL_WARNING.get()) return;
        Screen screen = event.getNewScreen();
        if (screen == null) return;
        BooleanConsumer callback;
        if (screen.getClass() == ConfirmExperimentalFeaturesScreen.class) {
            callback = ((ConfirmExperimentalFeaturesScreen) screen).callback;
        } else if (screen.getClass() == ConfirmScreen.class) {
            ConfirmScreen confirmation = (ConfirmScreen) screen;
            boolean creation = hasKey(confirmation.getTitle(), "selectWorld.warning.experimental.title")
                    && hasKey(confirmation.message, "selectWorld.warning.experimental.question");
            boolean loading = hasKey(confirmation.getTitle(), "selectWorld.backupQuestion.experimental")
                    && hasKey(confirmation.message, "selectWorld.backupWarning.experimental");
            if (!creation && !loading) return;
            callback = confirmation.callback;
        } else {
            return;
        }
        event.setCanceled(true);
        Minecraft client = Minecraft.getInstance();
        Screen previous = event.getCurrentScreen();
        // WorldOpenFlows releases the save lock AFTER setScreen returns. Always enqueue.
        client.tell(() -> {
            if (client.screen == previous) callback.accept(true);
        });
    }

    private static boolean hasKey(Component component, String key) {
        return component.getContents() instanceof TranslatableContents contents && key.equals(contents.getKey());
    }

    private ExperimentalWarningHandler() {}
}
