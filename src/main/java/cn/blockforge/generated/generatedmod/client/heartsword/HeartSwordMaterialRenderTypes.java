package cn.blockforge.generated.generatedmod.client.heartsword;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.io.IOException;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Atlas-compatible material passes. Glass never occludes its own internal luminous strands. */
@Mod.EventBusSubscriber(modid = GeneratedMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class HeartSwordMaterialRenderTypes extends RenderType {
    private static ShaderInstance emissiveShader;
    public static final RenderType CRYSTAL = material("heart_sword_crystal", RENDERTYPE_ENTITY_TRANSLUCENT_SHADER);
    public static final RenderType EMISSIVE = material("heart_sword_emissive", new ShaderStateShard(() -> emissiveShader));

    private static RenderType material(String name, ShaderStateShard shader) {
        return create(name, DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 262144, false, true,
                CompositeState.builder().setShaderState(shader)
                        .setTextureState(new TextureStateShard(TextureAtlas.LOCATION_BLOCKS, false, false))
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setDepthTestState(LEQUAL_DEPTH_TEST).setWriteMaskState(COLOR_WRITE)
                        .setCullState(NO_CULL).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY)
                        .createCompositeState(false));
    }

    @SubscribeEvent
    public static void register(RegisterShadersEvent event) throws IOException {
        event.registerShader(new ShaderInstance(event.getResourceProvider(),
                ResourceLocation.fromNamespaceAndPath(GeneratedMod.MOD_ID, "heart_sword_emissive"),
                DefaultVertexFormat.NEW_ENTITY), loaded -> emissiveShader = loaded);
    }

    private HeartSwordMaterialRenderTypes(String name, VertexFormat format, VertexFormat.Mode mode,
            int size, boolean crumbling, boolean sorted, Runnable setup, Runnable clear) {
        super(name, format, mode, size, crumbling, sorted, setup, clear);
    }
}
