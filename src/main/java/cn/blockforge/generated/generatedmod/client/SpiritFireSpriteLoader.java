package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.SpiritFirePalette;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.animation.AnimationFrame;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraftforge.client.textures.ForgeTextureMetadata;
import net.minecraftforge.client.textures.ITextureAtlasSpriteLoader;

import java.util.ArrayList;
import java.util.List;

/** 在图集加载时一次性调色，不逐 tick 重建区块，也不叠乘原版橙色。 */
public final class SpiritFireSpriteLoader implements ITextureAtlasSpriteLoader {
    private final int tier;

    public SpiritFireSpriteLoader(int tier) { this.tier = tier; }

    @Override
    public SpriteContents loadContents(ResourceLocation name, Resource resource, FrameSize size,
                                       NativeImage image, AnimationMetadataSection animation,
                                       ForgeTextureMetadata metadata) {
        if (tier == 9) {
            int width = image.getWidth();
            int sourceFrames = image.getHeight() / size.height();
            // 四个颜色阶段，轮廓仍循环原素材的32帧；128帧首尾无缝。
            NativeImage rainbow = new NativeImage(width, size.height() * 128, false);
            boolean offset = name.getPath().endsWith("_0");
            for (int frame = 0; frame < 128; frame++) {
                int sourceFrame = (frame + (offset ? 16 : 0)) % sourceFrames;
                for (int y = 0; y < size.height(); y++) for (int x = 0; x < width; x++) {
                    rainbow.setPixelRGBA(x, frame * size.height() + y, SpiritFirePalette.recolor(
                            image.getPixelRGBA(x, sourceFrame * size.height() + y), tier, frame, x, y));
                }
            }
            image.close();
            image = rainbow;
            List<AnimationFrame> frames = new ArrayList<>();
            for (int i = 0; i < 128; i++) frames.add(new AnimationFrame(i, 1));
            animation = new AnimationMetadataSection(frames, size.width(), size.height(), 1, false);
        } else {
            for (int y = 0; y < image.getHeight(); y++) for (int x = 0; x < image.getWidth(); x++) {
                image.setPixelRGBA(x, y, SpiritFirePalette.recolor(image.getPixelRGBA(x, y), tier,
                        y / size.height(), x, y % size.height()));
            }
        }
        return new SpriteContents(name, size, image, animation, metadata);
    }

    @Override
    public TextureAtlasSprite makeSprite(ResourceLocation atlas, SpriteContents contents,
                                          int atlasWidth, int atlasHeight, int x, int y, int mipLevel) {
        return new SpiritSprite(atlas, contents, atlasWidth, atlasHeight, x, y);
    }

    private static final class SpiritSprite extends TextureAtlasSprite {
        private SpiritSprite(ResourceLocation atlas, SpriteContents contents, int width, int height, int x, int y) {
            super(atlas, contents, width, height, x, y);
        }
    }
}
