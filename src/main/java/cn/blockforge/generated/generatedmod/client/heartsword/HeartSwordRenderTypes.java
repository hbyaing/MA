package cn.blockforge.generated.generatedmod.client.heartsword;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.io.IOException;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Untextured, unlit spirit light with ordinary scene occlusion and soft alpha. */
@Mod.EventBusSubscriber(modid = GeneratedMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class HeartSwordRenderTypes extends RenderType {
    private static ShaderInstance lightShader;
    public static final RenderType LIGHT = create("wuxing_heart_sword_light",
            DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 32768, false, true,
            CompositeState.builder()
                    .setShaderState(new ShaderStateShard(() -> lightShader))
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setWriteMaskState(COLOR_WRITE)
                    .setCullState(NO_CULL)
                    .createCompositeState(false));

    @SubscribeEvent
    public static void register(RegisterShadersEvent event) throws IOException {
        event.registerShader(new ShaderInstance(event.getResourceProvider(),
                ResourceLocation.fromNamespaceAndPath(GeneratedMod.MOD_ID, "heart_sword_light"),
                DefaultVertexFormat.POSITION_COLOR), loaded -> lightShader = loaded);
    }

    private HeartSwordRenderTypes(String name, VertexFormat format, VertexFormat.Mode mode, int size,
            boolean crumbling, boolean sorted, Runnable setup, Runnable clear) {
        super(name, format, mode, size, crumbling, sorted, setup, clear);
    }
}
