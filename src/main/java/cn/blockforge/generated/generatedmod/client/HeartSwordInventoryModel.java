package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.client.heartsword.HeartSwordMaterialModel;
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

/** GUI-only icon; world and action views share the translucent heart sword 2.0 material passes. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT,bus=Mod.EventBusSubscriber.Bus.MOD)
public final class HeartSwordInventoryModel {
    private static final ResourceLocation ICON=ResourceLocation.fromNamespaceAndPath(GeneratedMod.MOD_ID,"item/heart_sword_inventory");
    @SubscribeEvent public static void register(ModelEvent.RegisterAdditional event){event.register(ICON);}
    @SubscribeEvent public static void bake(ModelEvent.ModifyBakingResult event) {
        var key=new ModelResourceLocation(ResourceLocation.fromNamespaceAndPath(GeneratedMod.MOD_ID,"heart_sword"),"inventory");
        var original=event.getModels().get(key);var icon=event.getModels().get(ICON);
        if(original==null||icon==null)return;
        event.getModels().put(key,new BakedModelWrapper<BakedModel>(new HeartSwordMaterialModel(original)) {
            @Override public BakedModel applyTransform(ItemDisplayContext context,PoseStack pose,boolean left) {
                return context==ItemDisplayContext.GUI?icon.applyTransform(context,pose,left):originalModel.applyTransform(context,pose,left);
            }
        });
    }
    private HeartSwordInventoryModel() {}
}
