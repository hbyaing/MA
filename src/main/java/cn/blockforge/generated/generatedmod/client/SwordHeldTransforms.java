package cn.blockforge.generated.generatedmod.client;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** 每把剑独立登记手持姿态；不修改原始模型及御剑实体姿态。 */
public final class SwordHeldTransforms {
    private static final Map<ResourceLocation, SwordHeldTransform> TRANSFORMS = new ConcurrentHashMap<>();
    private static final SwordHeldTransform DEFAULT = new SwordHeldTransform(
            new SwordHeldTransform.GripAnchor(8, 8, 8), rotation(0, 0, 0),
            rotation(0, -90, 25), 1, rotation(0, -90, 55), 1, rotation(0, 0, 0));
    static {
        // 原始 handle_core_174: [80,-2.9,-3] .. [115.2,2.9,3]，中心 [97.6,0,0]。
        // 既有导出映射为 baked = 8 + original / 16，得到 [14.1,8,8]。
        register(new ResourceLocation("wuxingxiantu", "dark_iron_sword"), new SwordHeldTransform(
                new SwordHeldTransform.GripAnchor(14.1F, 8, 8), rotation(-90, 135, 0),
                rotation(0, -90, 25), 1.4F, rotation(0, -65, 65), 1.4F, rotation(0, 0, 0)));
        registerImported("mingjue_sword", 13.283951F);
        registerImported("qingming_sword", 13.741935F);
        registerImported("jixiao_sword", 13.862542F);
    }
    private SwordHeldTransforms() {}
    private static void registerImported(String path, float gripX) {
        // Pixel-space anchors are derived from the source handle cores by the importer.
        register(new ResourceLocation("wuxingxiantu", path), new SwordHeldTransform(
                new SwordHeldTransform.GripAnchor(gripX, 8, 8), rotation(-90, 135, 0),
                rotation(0, -90, 25), 1.35F, rotation(0, -65, 65), 1.35F, rotation(0, 0, 0)));
    }
    private static SwordHeldTransform.Rotation rotation(float x, float y, float z) {
        return new SwordHeldTransform.Rotation(x, y, z);
    }
    public static void register(ResourceLocation item, SwordHeldTransform transform) {
        TRANSFORMS.put(java.util.Objects.requireNonNull(item), java.util.Objects.requireNonNull(transform));
    }
    public static SwordHeldTransform get(ResourceLocation item) {
        return TRANSFORMS.getOrDefault(item, DEFAULT);
    }
    public static SwordHeldTransform get(ItemStack stack) {
        return get(ForgeRegistries.ITEMS.getKey(stack.getItem()));
    }
}
