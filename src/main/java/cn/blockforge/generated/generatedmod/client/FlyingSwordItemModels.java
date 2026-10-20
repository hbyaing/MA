package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.yujian.FlyingSwordRegistry;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** GUI 使用图标；世界中的显示统一交给共享几何渲染器。 */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT,bus=Mod.EventBusSubscriber.Bus.MOD)
public final class FlyingSwordItemModels {
    public static ResourceLocation worldModel(cn.blockforge.generated.generatedmod.yujian.FlyingSwordDefinition definition) {
        String path = definition.itemId().getPath();
        // The last three swords are still placeholder assets. Keep their item IDs
        // and tuning, but render the verified Xuan Tie Jian mesh until their own
        // authored models replace this mapping.
        if (path.equals("wuxiang_sword") || path.equals("guiyuan_sword") || path.equals("tianque_sword"))
            path = "dark_iron_sword";
        return new ResourceLocation(definition.itemId().getNamespace(), "item/" + path + "_flying");
    }
    @SubscribeEvent public static void additional(ModelEvent.RegisterAdditional event){
        for(var definition:FlyingSwordRegistry.definitions()) {
            if(definition.baked3d())event.register(worldModel(definition));
        }
    }
    @SubscribeEvent public static void bake(ModelEvent.ModifyBakingResult event){
        for(var item:net.minecraftforge.registries.ForgeRegistries.ITEMS){
            if(!(item instanceof cn.blockforge.generated.generatedmod.items.SpiritSwordItem))continue;
            var itemId=net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(item);
            boolean imported=FlyingSwordRegistry.visual(new net.minecraft.world.item.ItemStack(item)).baked3d();
            var key=new ModelResourceLocation(itemId,"inventory");
            var icon=event.getModels().get(key);
            if(icon!=null)event.getModels().put(key,new BakedModelWrapper<BakedModel>(icon){
                @Override public boolean isCustomRenderer(){return imported||originalModel.isCustomRenderer();}
                @Override public boolean isGui3d(){return !imported&&originalModel.isGui3d();}
                @Override public BakedModel applyTransform(ItemDisplayContext context,PoseStack pose,boolean left){
                    if(context==ItemDisplayContext.THIRD_PERSON_RIGHT_HAND||context==ItemDisplayContext.THIRD_PERSON_LEFT_HAND) {
                        float size=HeartForgeLayout.THIRD_PERSON_SCALE;pose.scale(size,size,size);
                    }
                    if(!imported||context==ItemDisplayContext.GUI)return originalModel.applyTransform(context,pose,left);
                    return this;
                }
            });
        }
    }
}
