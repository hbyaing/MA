package cn.blockforge.generated.generatedmod;

import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidType;

/** 灵液的客户端渲染属性：淡蓝色、发光且使用专属水面纹理。 */
public final class SpiritLiquidFluidType extends FluidType {
    private static final ResourceLocation STILL_TEXTURE = new ResourceLocation(
            GeneratedMod.MOD_ID, "block/spirit_liquid_still");
    private static final ResourceLocation FLOWING_TEXTURE = new ResourceLocation(
            GeneratedMod.MOD_ID, "block/spirit_liquid_flowing");

    public SpiritLiquidFluidType(Properties properties) {
        super(properties);
    }

    @Override
    public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
        consumer.accept(new IClientFluidTypeExtensions() {
            @Override public ResourceLocation getStillTexture() {
                return new ResourceLocation("minecraft", "block/water_still");
            }
            @Override public ResourceLocation getFlowingTexture() {
                return new ResourceLocation("minecraft", "block/water_flow");
            }
            @Override public int getTintColor() { return 0xB85ED9F2; }
        });
    }
}
