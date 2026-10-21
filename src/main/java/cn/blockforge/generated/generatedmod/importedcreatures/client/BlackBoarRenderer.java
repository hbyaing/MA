package cn.blockforge.generated.generatedmod.importedcreatures.client;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.importedcreatures.BlackBoarEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;

/** Source model and tusks; target name/HUD rules remain in charge of presentation. */
public final class BlackBoarRenderer extends MobRenderer<BlackBoarEntity,BlackBoarModel> {
    private static final ResourceLocation TEXTURE=new ResourceLocation(GeneratedMod.MOD_ID,"textures/entity/black_boar.png");
    public BlackBoarRenderer(EntityRendererProvider.Context context) {super(context,new BlackBoarModel(context.bakeLayer(BlackBoarModel.LAYER)),.65F);}
    @Override public ResourceLocation getTextureLocation(BlackBoarEntity entity) {return TEXTURE;}
    @Override protected void scale(BlackBoarEntity entity,PoseStack pose,float partial) {if(entity.king())pose.scale(1.45F,1.45F,1.45F);}
}
