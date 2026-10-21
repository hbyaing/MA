package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.yujian.FlyingSwordRegistry;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;

/** Enabled only by -PswordModelTests; loads real Forge models and exits at the title screen. */
@Mod.EventBusSubscriber(modid = GeneratedMod.MOD_ID, value = Dist.CLIENT)
public final class SwordModelSmokeTest {
    private static boolean finished;
    private static final java.util.Map<String, Integer> EXPECTED = java.util.Map.of(
            "azure_steel_sword", 3264, "tiangang_sword", 4200, "big_dipper_sword", 18534);

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (finished || event.phase != TickEvent.Phase.END) return;
        var client = Minecraft.getInstance();
        if (client.getOverlay() != null || client.screen == null) return;
        finished = true;
        StringBuilder report = new StringBuilder();
        try {
            var models = client.getModelManager();
            for (var definition : FlyingSwordRegistry.definitions()) {
                String id = definition.itemId().getPath();
                if (!EXPECTED.containsKey(id)) continue;
                var world = models.getModel(FlyingSwordItemModels.worldModel(definition));
                require(world != models.getMissingModel(), "Missing world model: " + id);
                var quads = world.getQuads(null, null, RandomSource.create(0));
                require(quads.size() == EXPECTED.get(id), "Incomplete geometry: " + id + " " + quads.size());
                var textures = new java.util.HashSet<ResourceLocation>();
                int luminous = 0;
                for (var quad : quads) {
                    var texture = quad.getSprite().contents().name();
                    require(texture.getNamespace().equals(GeneratedMod.MOD_ID)
                            && texture.getPath().startsWith("item/" + id + "/material_"), "Wrong texture: " + texture);
                    textures.add(texture);
                    int[] vertices = quad.getVertices();
                    require(vertices.length == 32, "Unexpected vertex format");
                    if (texture.getPath().endsWith("material_0")) {
                        require(vertices[6] == 0, "Metal material unexpectedly emissive");
                    } else {
                        require(vertices[6] == 0xF000F0, "Emissive material lost its lightmap");
                        luminous++;
                    }
                }
                require(textures.size() == (id.equals("big_dipper_sword") ? 3 : 2), "Missing material");
                require(luminous > 0, "No emissive quads");
                var inventory = models.getModel(new ModelResourceLocation(definition.itemId(), "inventory"));
                require(inventory.isCustomRenderer(), "Missing held renderer");
                require(!inventory.applyTransform(ItemDisplayContext.GUI, new PoseStack(), false).isCustomRenderer(), "GUI recursion");
                var held = SwordHeldTransforms.get(definition.itemId());
                for (var context : new ItemDisplayContext[]{ItemDisplayContext.FIRST_PERSON_LEFT_HAND,
                        ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, ItemDisplayContext.THIRD_PERSON_LEFT_HAND,
                        ItemDisplayContext.THIRD_PERSON_RIGHT_HAND}) {
                    PoseStack pose = new PoseStack();
                    held.apply(pose, context, definition.handScale());
                    pose.translate(-.5, -.5, -.5);
                    var grip = held.gripOffset();
                    Vector3f result = pose.last().pose().transformPosition(new Vector3f(grip.x()/16, grip.y()/16, grip.z()/16));
                    require(result.length() < 1e-6, "Grip slipped: " + id + " " + context + " " + result);
                }
                report.append(id).append(": ").append(quads.size()).append(" quads, ")
                        .append(textures.size()).append(" textures, ").append(luminous)
                        .append(" emissive quads; all four grip transforms PASS\n");
            }
            require(report.toString().lines().count() == 3, "Missing registered swords");
            report.append("PASS: actual Forge OBJ baking, textures, emission, GUI dispatch and held transforms.\n");
        } catch (Throwable failure) {
            report.append("FAIL: ").append(failure).append('\n');
            com.mojang.logging.LogUtils.getLogger().error("SWORD_MODEL_TEST", failure);
        }
        try {
            java.nio.file.Files.writeString(java.nio.file.Path.of("sword-model-result.txt"), report);
        } catch (java.io.IOException failure) {
            throw new RuntimeException(failure);
        }
        client.stop();
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
