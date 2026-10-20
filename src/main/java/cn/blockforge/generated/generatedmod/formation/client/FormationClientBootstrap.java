package cn.blockforge.generated.generatedmod.formation.client;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT,bus=Mod.EventBusSubscriber.Bus.MOD)
public final class FormationClientBootstrap {
    @SubscribeEvent public static void geometry(ModelEvent.RegisterGeometryLoaders event){event.register("formation_mesh",MeshGeometryLoader.INSTANCE);}
    @SubscribeEvent public static void shaders(RegisterShadersEvent event){SwordArrayShield.register(event);SwordArrayGeometry.reset();}
    private FormationClientBootstrap(){}
}
