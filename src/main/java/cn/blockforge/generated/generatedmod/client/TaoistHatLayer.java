package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.WuXingProfessions;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.VillagerRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Yellow Taoist robe and raised cap, with black robe binding and golden hat trim. */
public final class TaoistHatLayer extends RenderLayer<Villager, VillagerModel<Villager>> {
    private static final ResourceLocation CLOTH = new ResourceLocation("minecraft", "textures/block/yellow_wool.png");
    private static final ResourceLocation INK = new ResourceLocation("minecraft", "textures/block/black_wool.png");
    private static final ResourceLocation GOLD = new ResourceLocation("minecraft", "textures/block/gold_block.png");
    private final ModelPart cap;
    private final ModelPart trim;
    private final ModelPart robe;
    private final ModelPart binding;

    public TaoistHatLayer(RenderLayerParent<Villager, VillagerModel<Villager>> renderer) {
        super(renderer);
        MeshDefinition capMesh = new MeshDefinition();
        capMesh.getRoot().addOrReplaceChild("cap", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-4.1F, -13.1F, -4.1F, 8.2F, 3.2F, 8.2F)
                .addBox(-4.7F, -10.4F, -4.7F, 9.4F, 0.6F, 9.4F)
                .addBox(-1.0F, -14.4F, -2.8F, 2.0F, 1.4F, 5.6F)
                .addBox(-3.0F, -10.0F, 4.2F, 1.0F, 5.0F, 0.35F)
                .addBox(2.0F, -10.0F, 4.2F, 1.0F, 5.0F, 0.35F), PartPose.ZERO);
        cap = LayerDefinition.create(capMesh, 16, 16).bakeRoot();
        MeshDefinition trimMesh = new MeshDefinition();
        trimMesh.getRoot().addOrReplaceChild("trim", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-4.15F, -11.0F, -4.15F, 8.3F, 0.6F, 8.3F)
                .addBox(-0.7F, -12.8F, -4.25F, 1.4F, 2.0F, 0.2F), PartPose.ZERO);
        trim = LayerDefinition.create(trimMesh, 16, 16).bakeRoot();
        MeshDefinition robeMesh = new MeshDefinition();
        robeMesh.getRoot().addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-4.6F, -0.1F, -3.6F, 9.2F, 20.2F, 7.2F), PartPose.ZERO);
        robeMesh.getRoot().addOrReplaceChild("sleeves", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-8.55F, -2.55F, -2.55F, 5.1F, 9.1F, 5.1F)
                .addBox(3.45F, -2.55F, -2.55F, 5.1F, 9.1F, 5.1F)
                .addBox(-4.1F, 1.45F, -2.55F, 8.2F, 5.1F, 5.1F),
                PartPose.offsetAndRotation(0, 3, -1, -0.75F, 0, 0));
        robe = LayerDefinition.create(robeMesh, 64, 64).bakeRoot();
        MeshDefinition bindingMesh = new MeshDefinition();
        bindingMesh.getRoot().addOrReplaceChild("belt", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-4.65F, 10.0F, -3.65F, 9.3F, 1.2F, 7.3F)
                .addBox(-0.6F, 11.1F, -3.7F, 1.2F, 8.8F, 0.15F)
                .addBox(-2.4F, 0, -3.7F, 1.0F, 4.0F, 0.15F)
                .addBox(1.4F, 0, -3.7F, 1.0F, 4.0F, 0.15F), PartPose.ZERO);
        binding = LayerDefinition.create(bindingMesh, 64, 64).bakeRoot();
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buffers, int light, Villager villager,
                       float limbSwing, float limbAmount, float partialTick, float age,
                       float headYaw, float headPitch) {
        if (villager.isInvisible() || villager.getVillagerData().getProfession() != WuXingProfessions.TAOIST.get()) return;
        pose.pushPose();
        if (villager.isBaby()) {
            pose.scale(0.5F, 0.5F, 0.5F);
            pose.translate(0.0D, 1.5D, 0.0D);
        }
        robe.render(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(CLOTH)), light, OverlayTexture.NO_OVERLAY);
        binding.render(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(INK)), light, OverlayTexture.NO_OVERLAY);
        pose.popPose();
        pose.pushPose();
        if (villager.isBaby()) {
            pose.scale(0.75F, 0.75F, 0.75F);
            pose.translate(0.0D, 1.0D, 0.0D);
        }
        getParentModel().getHead().translateAndRotate(pose);
        cap.render(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(CLOTH)), light, OverlayTexture.NO_OVERLAY);
        trim.render(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(GOLD)), light, OverlayTexture.NO_OVERLAY);
        pose.popPose();
    }

    @Mod.EventBusSubscriber(modid = "wuxingxiantu", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class Registration {
        @SubscribeEvent
        public static void addLayers(EntityRenderersEvent.AddLayers event) {
            VillagerRenderer renderer = event.getRenderer(EntityType.VILLAGER);
            if (renderer != null) renderer.addLayer(new TaoistHatLayer(renderer));
        }
    }
}
