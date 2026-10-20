package cn.blockforge.generated.generatedmod.boss;

import com.google.gson.Gson;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class BossVisualRegistry {
    private static final Map<String, BossVisualDefinition> DEFINITIONS = load();
    public static final List<String> TEST_ENTITY_IDS = List.of(
            "muyan", "ermei", "bichen", "shetao", "shenyin", "yihuo", "pengju", "pengzhi", "pengjiao");

    private static Map<String, BossVisualDefinition> load() {
        try (var stream = Objects.requireNonNull(BossVisualRegistry.class.getResourceAsStream(
                "/data/wuxingxiantu/boss_visuals.json"));
             var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            var definitions = new Gson().fromJson(reader, BossVisualDefinition[].class);
            Map<String, BossVisualDefinition> result = new LinkedHashMap<>();
            for (var definition : definitions) {
                if (result.put(definition.id(), definition) != null) throw new IllegalStateException(definition.id());
            }
            return Collections.unmodifiableMap(result);
        } catch (Exception exception) {
            throw new IllegalStateException("Cannot load boss visual definitions", exception);
        }
    }

    public static BossVisualDefinition get(String id) {
        return Objects.requireNonNull(DEFINITIONS.get(id), "Unknown boss visual: " + id);
    }

    private BossVisualRegistry() {}
}
