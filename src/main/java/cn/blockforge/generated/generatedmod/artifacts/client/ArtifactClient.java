package cn.blockforge.generated.generatedmod.artifacts.client;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.artifacts.*;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/** Source rank 1..3 models derive from the single target equipment progression. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,bus=Mod.EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class ArtifactClient {
    @SubscribeEvent public static void setup(FMLClientSetupEvent event) {
        event.enqueueWork(()->{
            for(var kind:new ArtifactKind[]{ArtifactKind.SWORD,ArtifactKind.HAMMER}) {
                var item=ArtifactBootstrap.ARTIFACTS.get(kind);
                if(item.isPresent())ItemProperties.register(item.get(),new ResourceLocation(GeneratedMod.MOD_ID,"artifact_rank"),
                        (stack,level,entity,seed)->ArtifactData.modelRank(stack));
            }
        });
    }
    @SubscribeEvent public static void colors(RegisterColorHandlersEvent.Item event) {
        for(var kind:new ArtifactKind[]{ArtifactKind.ARMOR,ArtifactKind.LEGGINGS,ArtifactKind.BOOTS,ArtifactKind.HELMET}) {
            var item=ArtifactBootstrap.ARTIFACTS.get(kind);if(!item.isPresent())continue;
            int color=switch(kind) {
                case ARMOR->0xFFC0C8D8;case LEGGINGS->0xFFB8C0D0;
                case BOOTS->0xFFA8B0C0;case HELMET->0xFFD0D8E8;default->0xFFFFFFFF;
            };
            event.register((stack,tint)->tint==0?color:0xFFFFFFFF,item.get());
        }
    }
    private ArtifactClient() {}
}
