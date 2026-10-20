package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import java.lang.reflect.Field;
import java.util.List;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT,bus=Mod.EventBusSubscriber.Bus.MOD)
public final class RobeRenderEvents {
    @SubscribeEvent @SuppressWarnings({"rawtypes","unchecked"}) public static void layers(EntityRenderersEvent.AddLayers event){
        for(String skin:event.getSkins()){
            if(!((Object)event.getSkin(skin) instanceof PlayerRenderer renderer))continue;
            renderer.addLayer(new RobeLayer(renderer));
            // Match the existing renderer-list reflection pattern; generic type discovery survives obfuscation.
            for(Field field:LivingEntityRenderer.class.getDeclaredFields())if(List.class.isAssignableFrom(field.getType())){
                try{
                    field.setAccessible(true);List layers=(List)field.get(renderer);
                    for(int i=0;i<layers.size();i++)if(layers.get(i).getClass()==HumanoidArmorLayer.class){
                        layers.set(i,new RobeArmorLayer(renderer,
                                new HumanoidModel<AbstractClientPlayer>(event.getContext().bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                                new HumanoidModel<AbstractClientPlayer>(event.getContext().bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),event.getContext().getModelManager()));
                    }
                }catch(ReflectiveOperationException exception){throw new IllegalStateException("Cannot install robe armor visibility hook",exception);}
            }
        }
    }
    private RobeRenderEvents(){}
}
