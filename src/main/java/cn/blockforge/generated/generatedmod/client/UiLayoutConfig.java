package cn.blockforge.generated.generatedmod.client;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Shared logical-pixel layout read by the client screens and their menus.
 * Missing entries always fall back to the authored in-game defaults, so a
 * partially edited workbench file cannot make a screen unusable.
 */
public final class UiLayoutConfig {
    private static final Gson GSON = new Gson();
    private static final String FILE = "wuxingxiantu-ui-layout.json";
    private static volatile Map<String, JsonObject> layouts;

    private UiLayoutConfig() {}

    private static Map<String, JsonObject> layouts() {
        var current = layouts;
        if (current != null) return current;
        Map<String, JsonObject> loaded = new HashMap<>();
        try {
            Path path = FMLPaths.CONFIGDIR.get().resolve(FILE);
            if (Files.isRegularFile(path)) {
                try (Reader reader = Files.newBufferedReader(path)) {
                    JsonElement root = JsonParser.parseReader(reader);
                    JsonObject entries = root.isJsonObject() && root.getAsJsonObject().has("layouts")
                            ? root.getAsJsonObject().getAsJsonObject("layouts") : root.getAsJsonObject();
                    if (entries != null) for (var entry : entries.entrySet())
                        if (entry.getValue().isJsonObject()) loaded.put(entry.getKey(), entry.getValue().getAsJsonObject());
                }
            }
        } catch (Exception ignored) {
            // Use defaults when the file is absent, incomplete, or being replaced by the workbench.
        }
        layouts = loaded;
        return loaded;
    }

    private static JsonObject part(String screen, String element) {
        JsonObject direct = layouts().get(screen + "." + element);
        if (direct != null) return direct;
        JsonObject profile = layouts().get(screen);
        if (profile != null && profile.has(element) && profile.get(element).isJsonObject())
            return profile.getAsJsonObject(element);
        return null;
    }

    public static int value(String screen, String element, String field, int fallback) {
        JsonObject part = part(screen, element);
        if (part == null || !part.has(field) || !part.get(field).isJsonPrimitive()) return fallback;
        try { return part.get(field).getAsInt(); } catch (RuntimeException ignored) { return fallback; }
    }

    public static int x(String screen, String element, int fallback) { return value(screen, element, "x", fallback); }
    public static int y(String screen, String element, int fallback) { return value(screen, element, "y", fallback); }
    public static int width(String screen, String element, int fallback) { return value(screen, element, "w", fallback); }
    public static int height(String screen, String element, int fallback) { return value(screen, element, "h", fallback); }

    /** Re-read after the in-game launcher or a development reload replaces the file. */
    public static void reload() { layouts = null; }
}
