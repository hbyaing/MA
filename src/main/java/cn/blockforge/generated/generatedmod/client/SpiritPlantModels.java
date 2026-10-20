package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.plants.SpiritPlantCatalog;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.List;

@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT,bus=Mod.EventBusSubscriber.Bus.MOD)
public final class SpiritPlantModels {
    @SubscribeEvent public static void bake(ModelEvent.ModifyBakingResult event){
        event.getModels().replaceAll((id,model)->{
            if(!id.getNamespace().equals(GeneratedMod.MOD_ID)||!(id instanceof ModelResourceLocation state)
                    ||state.getVariant().equals("inventory")||!SpiritPlantCatalog.BY_ID.containsKey(id.getPath()))return model;
            return new PlantModel(model);
        });
    }
    private static final class PlantModel extends BakedModelWrapper<BakedModel> {
        private final java.util.Map<BakedQuad,BakedQuad> quads=new java.util.concurrent.ConcurrentHashMap<>();
        PlantModel(BakedModel original){super(original);}
        private List<BakedQuad> foliage(List<BakedQuad> source){
            return source.stream().map(q->quads.computeIfAbsent(q,key->{
                // Keep the baked vertices intact so packed block/sky light and geometric
                // normals remain world-driven. Only clear per-quad diffuse shading.
                return new BakedQuad(key.getVertices(),key.getTintIndex(),key.getDirection(),key.getSprite(),false);
            })).toList();
        }
        @Override public boolean useAmbientOcclusion(){return false;}
        @Override public List<BakedQuad> getQuads(BlockState state,Direction side,RandomSource random){return foliage(originalModel.getQuads(state,side,random));}
        @Override public List<BakedQuad> getQuads(BlockState state,Direction side,RandomSource random,ModelData data,RenderType type){return foliage(originalModel.getQuads(state,side,random,data,type));}
    }
    private SpiritPlantModels(){}
}
