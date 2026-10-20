package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.fire.ExoticBurnSnapshots;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

/** Vanilla animated fire silhouettes, tinted by the independent exotic-burn snapshot. */
public final class ExoticBurnRenderer {
    static long renderedEntities;
    private static final String[] FIRE_NAMES={"red_flow","molten_gold","cold_bone","azure_wood","cobalt_sea",
            "violet_thunder","sun_saint","nether_void","nirvana"};
    private static final java.util.Set<String> WARNED=new java.util.HashSet<>();
    public static TextureAtlasSprite sprite(String fireId,boolean alternate) {
        var fire=ExoticBurnSnapshots.visual(fireId);
        if(fire==null){warn(fireId);return null;}
        var id=new ResourceLocation(GeneratedMod.MOD_ID,"block/exotic_fire_"+FIRE_NAMES[fire.tier()-1]+(alternate?"_1":"_0"));
        var sprite=Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS).getSprite(id);
        if(!id.equals(sprite.contents().name())){warn(fireId);return null;}
        return sprite;
    }
    private static void warn(String id) {
        if(WARNED.size()<256&&WARNED.add(id))com.mojang.logging.LogUtils.getLogger().warn(
                "[WuxingXiantu/ExoticBurn] Missing visual profile for fire id: {}",id);
    }
    public static void renderAtEntity(net.minecraft.world.entity.Entity raw,
            com.mojang.blaze3d.vertex.PoseStack pose,net.minecraft.client.renderer.MultiBufferSource buffers) {
        if(!(raw instanceof LivingEntity entity))return;
        var mc=Minecraft.getInstance();
        if(mc.level==null||entity.level()!=mc.level||entity.isInvisible()
                ||entity==mc.getCameraEntity()&&mc.options.getCameraType().isFirstPerson())return;
        var camera=mc.getEntityRenderDispatcher().camera;
        if(camera==null)return;
        if(entity.position().distanceToSqr(camera.getPosition())>64*64)return;
        var burn=ExoticBurnClientState.state(entity);if(burn==null)return;
        var id=ResourceLocation.tryParse(burn.sourceFireId());
        var profile=id==null?null:cn.blockforge.generated.generatedmod.client.weaponflame.WeaponFlameResources.fire(id);
        if(profile==null)return;
        var fire=ExoticBurnSnapshots.visual(burn.sourceFireId());
        // Nirvana cycles through its palette; the weapon profile only stores a static base tint.
        int tint=fire!=null&&fire.tier()==9
                ?cn.blockforge.generated.generatedmod.ExoticFireColorRegistry.color(fire.tier(),mc.level.getGameTime())
                :profile.colors().body();
        // The entity dispatcher has already translated to the interpolated entity origin.
        // Reuse that exact transform: no second camera subtraction or world-stage matrix.
        // Invoked after the entity renderer returns, before the dispatcher pops its origin.
        // This also covers custom entity renderers that do not emit RenderLivingEvent.
        pose.pushPose();
        try {
            float alpha=Mth.clamp(ExoticBurnClientState.remaining(entity)/6F,0,1);
            drawVanillaShape(pose,buffers,entity,camera.getYRot(),tint,alpha);
            renderedEntities++;
        } finally {pose.popPose();}
    }
    private static void drawVanillaShape(com.mojang.blaze3d.vertex.PoseStack pose,
            net.minecraft.client.renderer.MultiBufferSource buffers,LivingEntity entity,float cameraYaw,int tint,float alpha) {
        var first=net.minecraft.client.resources.model.ModelBakery.FIRE_0.sprite();
        var second=net.minecraft.client.resources.model.ModelBakery.FIRE_1.sprite();
        float scale=Math.max(.1F,entity.getBbWidth()*1.4F);
        float remainingHeight=entity.getBbHeight()/scale;
        pose.scale(scale,scale,scale);
        pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-cameraYaw));
        pose.translate(0,0,-.3F+(int)remainingHeight*.02F);
        var out=buffers.getBuffer(ExoticBurnRenderType.FIRE);
        float halfWidth=.5F,bottom=0,depth=0;
        // Match the original stacked, alternating and mirrored flame planes; bound pathological entity sizes.
        for(int layer=0;remainingHeight>0&&layer<32;layer++) {
            var frame=layer%2==0?first:second;
            float u0=frame.getU0(),u1=frame.getU1();
            if(layer/2%2==0){float swap=u0;u0=u1;u1=swap;}
            burnVertex(out,pose,halfWidth,bottom,depth,u1,frame.getV1(),tint,alpha);
            burnVertex(out,pose,-halfWidth,bottom,depth,u0,frame.getV1(),tint,alpha);
            burnVertex(out,pose,-halfWidth,bottom+1.4F,depth,u0,frame.getV0(),tint,alpha);
            burnVertex(out,pose,halfWidth,bottom+1.4F,depth,u1,frame.getV0(),tint,alpha);
            remainingHeight-=.45F;bottom+=.45F;halfWidth*=.9F;depth+=.03F;
        }
    }
    private static void burnVertex(com.mojang.blaze3d.vertex.VertexConsumer out,com.mojang.blaze3d.vertex.PoseStack pose,
            float x,float y,float z,float u,float v,int tint,float alpha) {
        out.vertex(pose.last().pose(),x,y,z).color(tint>>16&255,tint>>8&255,tint&255,Math.round(alpha*255)).uv(u,v).endVertex();
    }
    private ExoticBurnRenderer() {}
}
