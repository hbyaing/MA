package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.io.IOException;

@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT,bus=Mod.EventBusSubscriber.Bus.MOD)
public final class FirstPersonFlowMaterial {
    static ShaderInstance shader;
    @SubscribeEvent public static void register(RegisterShadersEvent e)throws IOException{
        e.registerShader(new ShaderInstance(e.getResourceProvider(),new ResourceLocation(GeneratedMod.MOD_ID,"first_person_flow"),DefaultVertexFormat.POSITION_TEX_COLOR),s->shader=s);
    }
    private FirstPersonFlowMaterial(){}
}
