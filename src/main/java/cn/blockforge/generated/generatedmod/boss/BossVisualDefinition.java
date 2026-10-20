package cn.blockforge.generated.generatedmod.boss;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import net.minecraft.resources.ResourceLocation;
import java.util.List;
import java.util.Map;

/** Presentation data only. Dimensions and health are provisional visual-test values. */
public record BossVisualDefinition(String id, String entityId, String modelId,
        String model, String texture, String animation, String glow,
        float scale, float width, float height, float eyeHeight, float shadow,
        double renderOffsetY, boolean floating, double renderRadius,
        List<String> idle, String move, Map<String, Double> clips, Bar bar) {
    public ResourceLocation resource(String path) {
        return new ResourceLocation(GeneratedMod.MOD_ID, path);
    }

    public record Bar(String texture, String frame, String fill, int textureWidth, int textureHeight,
            int cropX, int cropY, int cropWidth, int cropHeight,
            int fillX, int fillY, int fillWidth, int fillHeight, int screenWidth,
            float nameOffsetX, float nameOffsetY, int nameColor, boolean nameShadow,
            int nameAreaX, int nameAreaY, int nameAreaWidth, int nameAreaHeight) {}
}
