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

/** Masks only render output, including custom Forge armor models; equipped stacks never change. */
public final class RobeArmorLayer extends HumanoidArmorLayer<AbstractClientPlayer,PlayerModel<AbstractClientPlayer>,HumanoidModel<AbstractClientPlayer>> {
    private final Model hidden=new Model(RenderType::entityCutoutNoCull){
        @Override public void renderToBuffer(PoseStack pose,VertexConsumer buffer,int light,int overlay,float r,float g,float b,float a){}
    };
    public RobeArmorLayer(RenderLayerParent<AbstractClientPlayer,PlayerModel<AbstractClientPlayer>> parent,
                          HumanoidModel<AbstractClientPlayer> inner,HumanoidModel<AbstractClientPlayer> outer,ModelManager manager){super(parent,inner,outer,manager);}
    @Override protected Model getArmorModelHook(AbstractClientPlayer player,ItemStack stack,EquipmentSlot slot,HumanoidModel<AbstractClientPlayer> model){
        return RobeLayer.profile(player).visibilityMask().hides(slot)?hidden:super.getArmorModelHook(player,stack,slot,model);
    }
}
