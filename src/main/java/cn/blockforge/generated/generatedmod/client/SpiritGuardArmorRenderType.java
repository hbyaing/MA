package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Per-equipment-level passes consumed after the matching armor depth dependencies. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT,bus=Mod.EventBusSubscriber.Bus.MOD)
public final class SpiritGuardArmorRenderType extends RenderType {
    private static final ResourceLocation[] MASKS={
        new ResourceLocation(GeneratedMod.MOD_ID,"textures/effect/spirit_guard_cloud_1.png"),
        new ResourceLocation(GeneratedMod.MOD_ID,"textures/effect/spirit_guard_cloud_2.png"),
        new ResourceLocation(GeneratedMod.MOD_ID,"textures/effect/spirit_guard_cloud_mask.png"),
        new ResourceLocation(GeneratedMod.MOD_ID,"textures/effect/spirit_guard_cloud_4.png")};
    private record ArmorTexture(ResourceLocation texture,int level){}
    private static final Map<ArmorTexture,RenderType> TYPES=new LinkedHashMap<>();
    private static ShaderInstance shader;

    static ShaderInstance shader(){return shader;}
    static ResourceLocation mask(int level){return MASKS[Math.max(1,Math.min(4,level))-1];}
    static RenderType forTexture(ResourceLocation armorTexture,int level){
        level=Math.max(1,Math.min(4,level));
        var key=new ArmorTexture(armorTexture,level);
        var found=TYPES.get(key);if(found!=null)return found;
        // Texture identities are bounded. Live queued buffers never depend on this cache afterwards.
        if(TYPES.size()>=128)TYPES.clear();
        var created=create("spirit_guard_cloud",DefaultVertexFormat.NEW_ENTITY,VertexFormat.Mode.QUADS,
                16384,false,true,CompositeState.builder().setShaderState(new ShaderStateShard(()->shader))
                        .setTextureState(MultiTextureStateShard.builder().add(MASKS[level-1],true,false).add(armorTexture,false,false).build())
                        .setTransparencyState(LIGHTNING_TRANSPARENCY).setDepthTestState(LEQUAL_DEPTH_TEST)
                        .setWriteMaskState(COLOR_WRITE).setCullState(NO_CULL)
                        .createCompositeState(false));
        TYPES.put(key,created);return created;
    }

    @SubscribeEvent public static void register(RegisterShadersEvent event)throws IOException{
        SpiritGuardArmorRenderer.release();
        event.registerShader(new ShaderInstance(event.getResourceProvider(),
                new ResourceLocation(GeneratedMod.MOD_ID,"spirit_guard_cloud"),DefaultVertexFormat.NEW_ENTITY),loaded->{
                    shader=loaded;TYPES.clear();
                });
    }
    private SpiritGuardArmorRenderType(String name,VertexFormat format,VertexFormat.Mode mode,int size,
            boolean crumbling,boolean sorting,Runnable setup,Runnable clear){super(name,format,mode,size,crumbling,sorting,setup,clear);}
}
