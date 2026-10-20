package cn.blockforge.generated.generatedmod.client.weaponflame;

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

@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT,bus=Mod.EventBusSubscriber.Bus.MOD)
public final class WeaponFlameRenderTypes extends RenderType {
    private static ShaderInstance shader;
    private static ShaderInstance referenceShader;
    static final RenderType REFERENCE=type("weapon_flame_reference",new ShaderStateShard(()->referenceShader));
    static final RenderType RIBBON=type("weapon_flame_ribbon",new ShaderStateShard(()->referenceShader));
    private static final RenderType[] MODULES=java.util.Arrays.stream(WeaponFlameModules.Module.values())
            .map(module->type("modular_v1/"+module.texture,new ShaderStateShard(()->referenceShader)))
            .toArray(RenderType[]::new);
    static RenderType module(WeaponFlameModules.Module module){return MODULES[module.ordinal()];}
    public static final RenderType EXOTIC_BURN=type("exotic_burn_vanilla",
            net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS,new ShaderStateShard(()->referenceShader));
    static final RenderType BAND=type("weapon_flame_band",new ShaderStateShard(()->shader));
    static final RenderType ATTACHMENT=type("weapon_flame",POSITION_COLOR_TEX_SHADER);
    static final RenderType SPARK=create("weapon_flame_sparks",DefaultVertexFormat.POSITION_COLOR,VertexFormat.Mode.QUADS,2048,false,true,
            CompositeState.builder().setShaderState(POSITION_COLOR_SHADER)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY).setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setWriteMaskState(COLOR_WRITE).setCullState(NO_CULL).createCompositeState(false));
    private static RenderType type(String texture,ShaderStateShard shaderState) {
        return type(texture,ResourceLocation.fromNamespaceAndPath(GeneratedMod.MOD_ID,"textures/effect/"+texture+".png"),shaderState);
    }
    private static RenderType type(String name,ResourceLocation texture,ShaderStateShard shaderState) {
        return create(name,DefaultVertexFormat.POSITION_COLOR_TEX,VertexFormat.Mode.QUADS,32768,false,true,
                CompositeState.builder().setShaderState(shaderState)
                        .setTextureState(new TextureStateShard(texture,false,false))
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY).setDepthTestState(LEQUAL_DEPTH_TEST)
                        .setWriteMaskState(COLOR_WRITE).setCullState(NO_CULL).createCompositeState(false));
    }
    @SubscribeEvent public static void register(RegisterShadersEvent event)throws IOException {
        event.registerShader(new ShaderInstance(event.getResourceProvider(),
                ResourceLocation.fromNamespaceAndPath(GeneratedMod.MOD_ID,"weapon_flame"),DefaultVertexFormat.POSITION_COLOR_TEX),
                loaded->shader=loaded);
        event.registerShader(new ShaderInstance(event.getResourceProvider(),
                ResourceLocation.fromNamespaceAndPath(GeneratedMod.MOD_ID,"weapon_flame_reference"),DefaultVertexFormat.POSITION_COLOR_TEX),
                loaded->referenceShader=loaded);
    }
    private WeaponFlameRenderTypes(String name,VertexFormat format,VertexFormat.Mode mode,int size,
            boolean crumbling,boolean sorted,Runnable setup,Runnable clear) {
        super(name,format,mode,size,crumbling,sorted,setup,clear);
    }
}
