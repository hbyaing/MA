package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.OfferingItemEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;

public final class OfferingItemRenderer extends EntityRenderer<OfferingItemEntity> {
    private final net.minecraft.client.renderer.entity.ItemRenderer items;
    public OfferingItemRenderer(EntityRendererProvider.Context context){super(context);items=context.getItemRenderer();}
    @Override public void render(OfferingItemEntity entity,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light){
        AlchemyFurnaceEffects.observe(entity);
        float p=entity.progress(partial);
        if(p>=1)return;
        pose.pushPose();
        var correction=entity.path(p).subtract(entity.getPosition(partial));
        pose.translate(correction.x,correction.y,correction.z);
        pose.mulPose(Axis.YP.rotationDegrees(entity.age(partial)*8));
        float size=1-net.minecraft.util.Mth.clamp((p-.72F)/.28F,0,1)*.92F;
        pose.scale(size,size,size);
        items.renderStatic(entity.getItem(),ItemDisplayContext.GROUND,LightTexture.FULL_BRIGHT,
                net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,pose,buffers,entity.level(),entity.getId());
        pose.popPose();
    }
    @Override public ResourceLocation getTextureLocation(OfferingItemEntity entity){return InventoryMenu.BLOCK_ATLAS;}
}
