package cn.blockforge.generated.generatedmod.importedcreatures.client;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.importedcreatures.ImportedCreatures;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,bus=Mod.EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class CreatureClient {
    @SubscribeEvent public static void layers(EntityRenderersEvent.RegisterLayerDefinitions event) {event.registerLayerDefinition(BlackBoarModel.LAYER,BlackBoarModel::layer);}
    @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ImportedCreatures.BLACK_BOAR.get(),BlackBoarRenderer::new);
        event.registerEntityRenderer(ImportedCreatures.BLACK_BOAR_KING.get(),BlackBoarRenderer::new);
    }
    @SubscribeEvent public static void meshes(ModelEvent.RegisterGeometryLoaders event) {event.register("imported_beast_mesh",MeshGeometryLoader.INSTANCE);}
    @SubscribeEvent public static void colors(RegisterColorHandlersEvent.Item event) {
        event.register((stack,index)->stack.hasTag()&&stack.getTag().contains("hide_color")?stack.getTag().getInt("hide_color"):0x4699FF,
                ImportedCreatures.HIDE.get());
    }
    private CreatureClient() {}
}
