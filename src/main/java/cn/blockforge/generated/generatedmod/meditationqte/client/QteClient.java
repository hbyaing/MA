package cn.blockforge.generated.generatedmod.meditationqte.client;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.MeditationSeatEntity;
import cn.blockforge.generated.generatedmod.Network;
import cn.blockforge.generated.generatedmod.meditationqte.*;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = GeneratedMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class QteClient {
    public static final KeyMapping START = new KeyMapping("key.wuxingxiantu.meditation_qte",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, "key.categories.wuxingxiantu");
    private QteClient() {}
    @SubscribeEvent public static void register(RegisterKeyMappingsEvent event) { event.register(START); }
    public static void accept(QteStatePacket state) {
        var minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) return;
        if (minecraft.screen instanceof QteScreen screen) screen.accept(state);
        else if (state.result() == QteStatePacket.OPEN && minecraft.player.getVehicle() instanceof MeditationSeatEntity)
            minecraft.setScreen(new QteScreen(state));
    }
    @Mod.EventBusSubscriber(modid = GeneratedMod.MOD_ID, value = Dist.CLIENT)
    public static final class Events {
        private Events() {}
        @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            var minecraft = Minecraft.getInstance();
            while (START.consumeClick()) {
                if (minecraft.player != null && minecraft.level != null && minecraft.screen == null
                        && minecraft.player.getVehicle() instanceof MeditationSeatEntity)
                    Network.CHANNEL.sendToServer(new QteActionPacket(QteActionPacket.START, QteActionPacket.NONE, 0));
            }
        }
    }
}
