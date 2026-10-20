package cn.blockforge.generated.generatedmod.formation.client;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.client.model.geometry.IGeometryLoader;

/**
 * 模型 JSON 里的 {@code "loader": "wuxingxiantu:mesh"}，用来读取 {@link MeshGeometry} 的面表。
 *
 * <pre>
 * {
 *   "loader": "wuxingxiantu:mesh",
 *   "mesh": "wuxingxiantu:mesh/xxx.json",
 *   "textures": { "particle": "wuxingxiantu:block/immortal_alchemy_furnace" },
 *   "render_type": "minecraft:cutout"
 * }
 * </pre>
 *
 * <p>其余字段（{@code textures}、{@code display}、{@code render_type} 等）仍由原版的方块模型
 * 反序列化器处理，所以 {@code display} 里写的摆放照样生效。</p>
 */
public class MeshGeometryLoader implements IGeometryLoader<MeshGeometry> {

    public static final MeshGeometryLoader INSTANCE = new MeshGeometryLoader();

    private MeshGeometryLoader() {
    }

    @Override
    public MeshGeometry read(JsonObject json, JsonDeserializationContext context) throws JsonParseException {
        ResourceLocation mesh = new ResourceLocation(GsonHelper.getAsString(json, "mesh"));
        return new MeshGeometry(mesh);
    }
}
