package cn.blockforge.generated.generatedmod.client;

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

/** Entity burning owns its material independently of any future weapon appearance. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT,bus=Mod.EventBusSubscriber.Bus.MOD)
public final class ExoticBurnRenderType extends RenderType {
    private static ShaderInstance shader;
    public static final RenderType FIRE=create("exotic_burn",DefaultVertexFormat.POSITION_COLOR_TEX,
            VertexFormat.Mode.QUADS,32768,false,true,CompositeState.builder()
                    .setShaderState(new ShaderStateShard(()->shader))
                    .setTextureState(new TextureStateShard(TextureAtlas.LOCATION_BLOCKS,false,false))
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY).setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setWriteMaskState(COLOR_WRITE).setCullState(NO_CULL).createCompositeState(false));

    @SubscribeEvent public static void register(RegisterShadersEvent event)throws IOException {
        event.registerShader(new ShaderInstance(event.getResourceProvider(),
                ResourceLocation.fromNamespaceAndPath(GeneratedMod.MOD_ID,"exotic_burn"),DefaultVertexFormat.POSITION_COLOR_TEX),
                loaded->shader=loaded);
    }
    private ExoticBurnRenderType(String name,VertexFormat format,VertexFormat.Mode mode,int size,
            boolean crumbling,boolean sorted,Runnable setup,Runnable clear) {
        super(name,format,mode,size,crumbling,sorted,setup,clear);
    }
}
