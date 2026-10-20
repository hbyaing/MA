package cn.blockforge.generated.generatedmod.client.heartsword;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.data.ModelData;

/** Splits the authored OBJ by atlas sprite without changing its mesh, UVs or PNGs. */
public final class HeartSwordMaterialModel extends BakedModelWrapper<BakedModel> {
    private static final ResourceLocation LIGHT = ResourceLocation.fromNamespaceAndPath(
            GeneratedMod.MOD_ID, "item/heart_sword_light");
    private static final ResourceLocation GEM = ResourceLocation.fromNamespaceAndPath(
            GeneratedMod.MOD_ID, "item/heart_sword_gem");
    private final List<BakedModel> passes;

    public HeartSwordMaterialModel(BakedModel original) {
        super(original);
        passes = List.of(new Layer(original, false), new Layer(original, true));
    }

    @Override
    public BakedModel applyTransform(ItemDisplayContext context, PoseStack pose, boolean left) {
        originalModel.applyTransform(context, pose, left);
        return this;
    }

    @Override
    public List<BakedModel> getRenderPasses(ItemStack stack, boolean fabulous) {
        return passes;
    }

    /** Cached once per resource bake; the supplied heart sword is a static OBJ. */
    private static final class Layer extends BakedModelWrapper<BakedModel> {
        private final List<List<BakedQuad>> faces;
        private final List<RenderType> types;
        private final List<BakedModel> passes;

        Layer(BakedModel original, boolean emissive) {
            super(original);
            types = List.of(emissive ? HeartSwordMaterialRenderTypes.EMISSIVE : HeartSwordMaterialRenderTypes.CRYSTAL);
            passes = List.of(this);
            var cached = new ArrayList<List<BakedQuad>>(Direction.values().length + 1);
            var random = RandomSource.create(42);
            for (int index = 0; index <= Direction.values().length; index++) {
                Direction face = index == Direction.values().length ? null : Direction.values()[index];
                random.setSeed(42);
                var selected = new ArrayList<BakedQuad>();
                for (BakedQuad quad : original.getQuads(null, face, random, ModelData.EMPTY, null)) {
                    ResourceLocation sprite = quad.getSprite().contents().name();
                    boolean glow = LIGHT.equals(sprite) || GEM.equals(sprite);
                    if (glow == emissive) selected.add(quad);
                }
                cached.add(List.copyOf(selected));
            }
            faces = List.copyOf(cached);
        }

        @Override
        public BakedModel applyTransform(ItemDisplayContext context, PoseStack pose, boolean left) {
            originalModel.applyTransform(context, pose, left);
            return this;
        }

        @Override
        public List<BakedModel> getRenderPasses(ItemStack stack, boolean fabulous) {
            return passes;
        }

        @Override
        public List<RenderType> getRenderTypes(ItemStack stack, boolean fabulous) {
            return types;
        }

        @Override
        public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource random) {
            return faces.get(side == null ? Direction.values().length : side.ordinal());
        }

        @Override
        public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource random,
                ModelData data, RenderType type) {
            return type == null || types.contains(type) ? getQuads(state, side, random) : List.of();
        }
    }
}
