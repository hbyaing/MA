package cn.blockforge.generated.generatedmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderHandEvent;
import cn.blockforge.generated.generatedmod.balance.HeartSwordTimeline;
import cn.blockforge.generated.generatedmod.client.heartsword.HeartSwordModelGeometry;

/** Shared grip and normalized timeline for both views; no gameplay item is created. */
public final class HeartSwordPose {
    public static void arms(HumanoidModel<?> model, LivingEntity player, float partial) {
        int action = HeartSwordClientEffects.action(player);
        if (action < 0 || action >= 2) return;
        float p = HeartSwordClientEffects.progress(player, partial);
        float weight=HeartSwordTimeline.poseWeight(p);
        var arm=HeartSwordClientEffects.leftHanded(player)?model.leftArm:model.rightArm;
        arm.xRot = net.minecraft.util.Mth.lerp(weight,arm.xRot,-.65F);
        arm.yRot = net.minecraft.util.Mth.lerp(weight,arm.yRot,0);
        arm.zRot = net.minecraft.util.Mth.lerp(weight,arm.zRot,0);
    }
    public static boolean firstPerson(RenderHandEvent event) {
        var mc = Minecraft.getInstance();
        var player = mc.player;
        int action = HeartSwordClientEffects.action(player);
        if (action < 0 || !(mc.getEntityRenderDispatcher().getRenderer(player) instanceof PlayerRenderer renderer)) return false;
        if (event.getHand() != net.minecraft.world.InteractionHand.MAIN_HAND) {
            if (action >= 2) event.setCanceled(true);
            return action >= 2;
        }
        event.setCanceled(true);
        float p = HeartSwordClientEffects.progress(player, event.getPartialTick());
        var pose = event.getPoseStack();
        boolean left=HeartSwordClientEffects.leftHanded(player);
        float handSign=left?-1:1;
        var grip=(action>=2?HeartForgeLayout.firstPerson(0,0):new HeartForgeLayout.Point(.38F,-.35F,-.8F)).handed(left);
        renderPalm(renderer,player,pose,event.getMultiBufferSource(),event.getPackedLight(),left,grip.x(),grip.y(),grip.z(),action>=2);
        if (action >= 2) {
            var palm=HeartForgeLayout.firstPerson(forgeAlong(HeartSwordClientEffects.sword(player),action,p,false),HeartForgeLayout.PALM_CLEARANCE).handed(left);
            renderPalm(renderer,player,pose,event.getMultiBufferSource(),event.getPackedLight(),!left,
                    palm.x(),palm.y(),palm.z(),true);
        }
        pose.pushPose();
        pose.translate(grip.x(),grip.y(),grip.z());
        if(action<2)pose.mulPose(Axis.YP.rotationDegrees(12*handSign));
        pose.mulPose(Axis.ZP.rotationDegrees((action>=2?90:20)*handSign));
        renderSword(player, action, p, pose, event.getMultiBufferSource(), event.getPackedLight(),
                left?ItemDisplayContext.FIRST_PERSON_LEFT_HAND:ItemDisplayContext.FIRST_PERSON_RIGHT_HAND);
        pose.popPose();
        return true;
    }
    private static void renderPalm(PlayerRenderer renderer,net.minecraft.client.player.AbstractClientPlayer player,
            PoseStack pose,MultiBufferSource buffers,int light,boolean left,double x,double y,double z,boolean forge) {
        if (player.isInvisible()) return;
        var model=renderer.getModel();
        var arm=left?model.leftArm:model.rightArm;
        var sleeve=left?model.leftSleeve:model.rightSleeve;
        var oldArm=arm.storePose();var oldSleeve=sleeve.storePose();
        boolean armVisible=arm.visible,sleeveVisible=sleeve.visible;
        float ax=arm.xScale,ay=arm.yScale,az=arm.zScale,sx=sleeve.xScale,sy=sleeve.yScale,sz=sleeve.zScale;
        pose.pushPose();
        try {
            // Draw neutral skin parts; renderRightHand/renderLeftHand would rerun the world animation.
            arm.loadPose(arm.getInitialPose());sleeve.copyFrom(arm);arm.visible=true;
            sleeve.visible=player.isModelPartShown(left?net.minecraft.world.entity.player.PlayerModelPart.LEFT_SLEEVE
                    :net.minecraft.world.entity.player.PlayerModelPart.RIGHT_SLEEVE);
            pose.translate(x,y,z);
            // Keep the shoulders below the blade; a -90 degree forearm points into the camera.
            pose.mulPose(Axis.ZP.rotationDegrees(left?-12:12));
            pose.mulPose(Axis.XP.rotationDegrees(forge?-30:-25));
            float center=("slim".equals(player.getModelName())?.5F:1F)*(left?1:-1);
            pose.translate(-(arm.x+center)/16,-(arm.y+10)/16,-arm.z/16);
            var skin=player.getSkinTextureLocation();
            arm.render(pose,buffers.getBuffer(net.minecraft.client.renderer.RenderType.entitySolid(skin)),light,
                    net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);
            sleeve.render(pose,buffers.getBuffer(net.minecraft.client.renderer.RenderType.entityTranslucent(skin)),light,
                    net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);
        } finally {
            arm.loadPose(oldArm);sleeve.loadPose(oldSleeve);
            arm.visible=armVisible;sleeve.visible=sleeveVisible;
            arm.xScale=ax;arm.yScale=ay;arm.zScale=az;sleeve.xScale=sx;sleeve.yScale=sy;sleeve.zScale=sz;
            pose.popPose();
        }
    }
    public static void renderSword(LivingEntity player, int action, float p, PoseStack pose, MultiBufferSource buffers, int light) {
        renderSword(player,action,p,pose,buffers,light,HeartSwordClientEffects.leftHanded(player)
                ?ItemDisplayContext.THIRD_PERSON_LEFT_HAND:ItemDisplayContext.THIRD_PERSON_RIGHT_HAND);
    }
    private static float forgeRoot(ItemStack stack) {
        return cn.blockforge.generated.generatedmod.yujian.FlyingSwordRegistry.visual(stack).baked3d()
                ?SwordHeldTransforms.get(stack).gripOffset().x()/16-.67F:(.225F+.22F)*.42F;
    }
    private static float forgeTip(ItemStack stack) {
        return cn.blockforge.generated.generatedmod.yujian.FlyingSwordRegistry.visual(stack).baked3d()
                ?SwordHeldTransforms.get(stack).gripOffset().x()/16-.012F:(1.98125F+.22F)*.42F;
    }
    public static float forgeAlong(ItemStack stack,int action,float progress,boolean thirdPerson) {
        float root=forgeRoot(stack),tip=forgeTip(stack),length=tip-root;
        float along=net.minecraft.util.Mth.lerp(HeartSwordTimeline.sweep(action,progress),root+.06F*length,tip-.10F*length);
        return along*(thirdPerson?HeartForgeLayout.THIRD_PERSON_SCALE:1);
    }
    private static void renderSword(LivingEntity player, int action, float p, PoseStack pose, MultiBufferSource buffers, int light, ItemDisplayContext context) {
        ItemStack stack = action <= 1 ? cn.blockforge.generated.generatedmod.items.HeartSwordItem.create(player.getUUID()) : HeartSwordClientEffects.sword(player);
        if (stack.isEmpty()) return;
        var renderer=Minecraft.getInstance().getItemRenderer();
        var model=renderer.getModel(stack,player.level(),player,player.getId());
        var definition=cn.blockforge.generated.generatedmod.yujian.FlyingSwordRegistry.visual(stack);
        if(action>=2 && definition.baked3d())
            model=Minecraft.getInstance().getModelManager().getModel(FlyingSwordItemModels.worldModel(definition));
        float reveal=HeartSwordTimeline.reveal(action,p);
        // Keep summon/dismiss size consistent with the held model in the current view.
        float heartSwordScale=action<=1?model.getTransforms().getTransform(context).scale.x():1;
        pose.pushPose();
        if(action>=2&&!context.firstPerson()) {
            float size=HeartForgeLayout.THIRD_PERSON_SCALE;pose.scale(size,size,size);
        }
        pose.pushPose();
        // Animation coordinates: grip at zero, tip on +Y. Bypass ordinary held-item rotations.
        if(action<=1) {
            pose.scale(heartSwordScale,heartSwordScale,heartSwordScale);
            pose.translate(0,-HeartSwordModelGeometry.GRIP_Y,0);
        } else if(definition.baked3d()) {
            var grip=SwordHeldTransforms.get(stack).gripOffset();
            pose.mulPose(Axis.ZP.rotationDegrees(-90));
            pose.mulPose(Axis.XP.rotationDegrees(-90));
            pose.translate(-grip.x()/16,-grip.y()/16,-grip.z()/16);
        } else if(model.isGui3d()) {
            pose.scale(.42F,.42F,.42F);
            pose.mulPose(Axis.YP.rotationDegrees(90));
            pose.translate(-.5,.22,-.5);
        } else {
            pose.mulPose(Axis.ZP.rotationDegrees(45));
            pose.translate(-.2,-.2,-.5);
        }
        if(action<=1) {
            model=new HeartSwordClippedModel(model,reveal,action==0);
        }
        pose.translate(.5,.5,.5);
        cn.blockforge.generated.generatedmod.client.weaponflame.WeaponFlameRenderer.renderPosed(
                renderer,stack,context,pose,buffers,light,
                net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,model,player);
        pose.popPose();
        if(action>=2) {
            // Item layers may use fixed buffers; submit the physical blade before its fading light.
            if(buffers instanceof MultiBufferSource.BufferSource source)
                for(var pass:model.getRenderPasses(stack,false))
                    for(var type:pass.getRenderTypes(stack,false))source.endBatch(type);
            // Summon/dismiss light is now emitted inside the same clipped OBJ matrix as the sword.
            HeartSwordGeometry.render(pose,buffers,action,p,forgeRoot(stack),forgeTip(stack));
        }
        pose.popPose();
    }
    private HeartSwordPose() {}
}
