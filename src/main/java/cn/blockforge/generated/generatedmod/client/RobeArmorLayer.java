package cn.blockforge.generated.generatedmod.client;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.model.*;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

/** Masks only render output, including custom Forge armor models; equipped stacks never change. */
public final class RobeArmorLayer extends HumanoidArmorLayer<AbstractClientPlayer,PlayerModel<AbstractClientPlayer>,HumanoidModel<AbstractClientPlayer>> {
    private SpiritGuardArmorRenderer.Capture capture;
    private final Model hidden=new Model(RenderType::entityCutoutNoCull){
        @Override public void renderToBuffer(PoseStack pose,VertexConsumer buffer,int light,int overlay,float r,float g,float b,float a){}
    };
    public RobeArmorLayer(RenderLayerParent<AbstractClientPlayer,PlayerModel<AbstractClientPlayer>> parent,
                          HumanoidModel<AbstractClientPlayer> inner,HumanoidModel<AbstractClientPlayer> outer,ModelManager manager){super(parent,inner,outer,manager);}
    @Override public void render(PoseStack pose,MultiBufferSource buffers,int light,AbstractClientPlayer player,
            float swing,float amount,float partial,float age,float yaw,float pitch){
        if(!SpiritGuardArmorRenderer.canRender(player,partial)){
            var previous=capture;capture=null;
            try {super.render(pose,buffers,light,player,swing,amount,partial,age,yaw,pitch);}
            finally {capture=previous;}
            return;
        }
        // Each capture belongs to this call, including nested renderer invocations and exceptions.
        var previous=capture;
        var current=new SpiritGuardArmorRenderer.Capture(buffers,player);
        capture=current;
        try {
            super.render(pose,current,light,player,swing,amount,partial,age,yaw,pitch);
            current.finish(pose,partial);
        } finally { capture=previous; }
    }
    @Override protected Model getArmorModelHook(AbstractClientPlayer player,ItemStack stack,EquipmentSlot slot,HumanoidModel<AbstractClientPlayer> model){
        if(RobeLayer.profile(player).visibilityMask().hides(slot)){
            if(capture!=null)capture.begin(stack,slot,false,true);
            return hidden;
        }
        var actual=super.getArmorModelHook(player,stack,slot,model);
        // Only the original armor model guarantees this mask's vanilla UV layout.
        if(capture!=null)capture.begin(stack,slot,actual==model,false);
        return actual;
    }
    @Override public ResourceLocation getArmorResource(Entity entity,ItemStack stack,EquipmentSlot slot,String type){
        var texture=super.getArmorResource(entity,stack,slot,type);
        if(capture!=null&&type==null)capture.texture(texture);
        return texture;
    }
}
