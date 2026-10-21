package cn.blockforge.generated.generatedmod.importedcreatures.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.IModelBuilder;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.SimpleUnbakedGeometry;
import net.minecraftforge.client.model.pipeline.QuadBakingVertexConsumer;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

/**
 * 用一张「四边面表」烘焙出来的方块几何。
 *
 * <p>方块自带的 JSON 模型只能摆方盒子，遇到八棱柱、斜边这类形状就没办法了；
 * 而玩家给的 Blockbench 模型（free 格式）里正好有八角收口的网格。这里把整座模型
 * 预先转成四边面表（{@code mesh/xxx.json}），烘焙时直接按面喂进模型构建器，
 * 于是它就成了一个普通的静态方块模型 —— 不用方块实体、不用每帧绘制，
 * 物品栏里也能照着原版方块的默认摆法显示立体模型。</p>
 *
 * <p>面表里每个四边面 32 个 float：4 个顶点 × (x, y, z, u, v, nx, ny, nz)。
 * 坐标是方块空间 0~1，u/v 是 0~1 的归一化贴图坐标。顶点顺序已经按原版
 * {@code FaceInfo} 排好（从外侧看逆时针），所以开着背面剔除也不会漏面；
 * 三边面在导出时补了一个重复顶点凑成四边面，渲染时那半个三角形是退化的。</p>
 */
public class MeshGeometry extends SimpleUnbakedGeometry<MeshGeometry> {

    /** 单个顶点的 float 数：位置 3 + UV 2 + 法线 3。 */
    private static final int STRIDE = 8;
    /** 单个四边面的 float 数。 */
    private static final int QUAD_FLOATS = STRIDE * 4;

    private final ResourceLocation mesh;
    private float[] quads;
    private boolean loaded;

    public MeshGeometry(ResourceLocation mesh) {
        this.mesh = mesh;
    }

    @Override
    protected void addQuads(IGeometryBakingContext owner, IModelBuilder<?> modelBuilder, ModelBaker baker,
                            Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform,
                            ResourceLocation modelLocation) {
        float[] data = quads();
        if (data.length < QUAD_FLOATS) {
            return;
        }
        // 整座模型共用一张图集，取模型 JSON 里 textures.particle 指向的那张。
        TextureAtlasSprite sprite = spriteGetter.apply(owner.getMaterial("particle"));
        QuadBakingVertexConsumer.Buffered quad = new QuadBakingVertexConsumer.Buffered();
        for (int base = 0; base + QUAD_FLOATS <= data.length; base += QUAD_FLOATS) {
            quad.setSprite(sprite);
            quad.setTintIndex(-1);
            quad.setShade(true);
            quad.setHasAmbientOcclusion(false);
            quad.setDirection(Direction.getNearest(data[base + 5], data[base + 6], data[base + 7]));
            for (int vertex = 0; vertex < 4; vertex++) {
                int at = base + vertex * STRIDE;
                quad.vertex(data[at], data[at + 1], data[at + 2]);
                quad.color(255, 255, 255, 255);
                quad.uv(sprite.getU(data[at + 3] * 16.0D), sprite.getV(data[at + 4] * 16.0D));
                quad.uv2(LightTexture.FULL_BRIGHT);
                quad.normal(data[at + 5], data[at + 6], data[at + 7]);
                quad.endVertex();
            }
            modelBuilder.addUnculledFace(quad.getQuad());
        }
    }

    /** 读取并缓存面表；读不到就返回空数组，方块顶多不显示，不会崩。 */
    private float[] quads() {
        if (loaded) {
            return quads == null ? new float[0] : quads;
        }
        loaded = true;
        try (InputStream in = Minecraft.getInstance().getResourceManager().open(mesh)) {
            JsonObject root = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8))
                    .getAsJsonObject();
            JsonArray list = root.getAsJsonArray("quads");
            float[] out = new float[list.size()];
            for (int index = 0; index < list.size(); index++) {
                out[index] = list.get(index).getAsFloat();
            }
            quads = out;
        } catch (Exception ex) {
            quads = new float[0];
        }
        return quads;
    }
}
