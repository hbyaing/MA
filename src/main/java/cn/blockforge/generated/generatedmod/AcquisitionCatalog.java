package cn.blockforge.generated.generatedmod;

import com.google.gson.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.*;

/** Evidence of direct acquisition routes; absence means unreviewed, not unobtainable. */
@Mod.EventBusSubscriber(modid = GeneratedMod.MOD_ID)
public final class AcquisitionCatalog {
    private static volatile Map<ResourceLocation, List<String>> routes = Map.of();
    private static volatile Map<ResourceLocation,List<cn.blockforge.generated.generatedmod.acquisition.SourceEvidence>> evidence=Map.of();
    public static List<cn.blockforge.generated.generatedmod.acquisition.SourceEvidence> evidence(ResourceLocation item) {
        return evidence.getOrDefault(item,List.of());
    }
    public static List<String> sources(ResourceLocation item) { return routes.getOrDefault(item, List.of()); }
    private static void add(Map<ResourceLocation, List<String>> result, ResourceLocation item, String route) {
        if (item != null && GeneratedMod.MOD_ID.equals(item.getNamespace()))
            result.computeIfAbsent(item, ignored -> new ArrayList<>()).add(route);
    }
    private static void loot(JsonElement node, String route, Map<ResourceLocation, List<String>> result) {
        if (node.isJsonArray()) { for (var value : node.getAsJsonArray()) loot(value, route, result); }
        else if (node.isJsonObject()) {
            var object = node.getAsJsonObject();
            if (object.has("type") && object.get("type").isJsonPrimitive()
                    && "minecraft:item".equals(object.get("type").getAsString()) && object.has("name"))
                add(result, ResourceLocation.tryParse(object.get("name").getAsString()), route);
            for (var value : object.entrySet()) loot(value.getValue(), route, result);
        }
    }
    private static void rebuild(MinecraftServer server) {
        var result = new TreeMap<ResourceLocation, List<String>>();
        for (var recipe : server.getRecipeManager().getRecipes()) {
            var stack = recipe.getResultItem(server.registryAccess());
            if (!stack.isEmpty()) add(result, net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()), "recipe:" + recipe.getId());
        }
        for (var entry : server.getResourceManager().listResources("loot_tables", id -> id.getPath().endsWith(".json")).entrySet()) {
            try (var reader = entry.getValue().openAsReader()) { loot(JsonParser.parseReader(reader), "loot:" + entry.getKey(), result); }
            catch (Exception exception) { com.mojang.logging.LogUtils.getLogger().warn("Cannot audit loot table {}", entry.getKey(), exception); }
        }
        var provided=new TreeMap<ResourceLocation,List<cn.blockforge.generated.generatedmod.acquisition.SourceEvidence>>();
        result.forEach((item,values)->values.forEach(route->{
            int split=route.indexOf(':');
            provided.computeIfAbsent(item,id->new ArrayList<>()).add(
                    new cn.blockforge.generated.generatedmod.acquisition.SourceEvidence(route.substring(0,split),
                            new ResourceLocation(route.substring(split+1)),List.of("data-driven route; conditions and input reachability require review"),
                            -1,null,List.of()));
        }));
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(
                new cn.blockforge.generated.generatedmod.acquisition.AcquisitionSourcesEvent((item,source)->{
                    if(!GeneratedMod.MOD_ID.equals(item.getNamespace()))return;
                    provided.computeIfAbsent(item,id->new ArrayList<>()).add(source);
                    add(result,item,source.describe());
                }));
        var immutable=new TreeMap<ResourceLocation,List<cn.blockforge.generated.generatedmod.acquisition.SourceEvidence>>();
        provided.forEach((item,list)->immutable.put(item,List.copyOf(list)));evidence=Collections.unmodifiableMap(immutable);
        var snapshot = new TreeMap<ResourceLocation, List<String>>();
        result.forEach((item, values) -> snapshot.put(item, List.copyOf(values)));
        routes = Collections.unmodifiableMap(snapshot);
    }
    @SubscribeEvent public static void started(ServerStartedEvent event) { rebuild(event.getServer()); }
    @SubscribeEvent public static void reloaded(OnDatapackSyncEvent event) {
        if (event.getPlayer() == null) rebuild(event.getPlayerList().getServer());
    }
    @SubscribeEvent public static void stopped(ServerStoppedEvent event) { routes = Map.of();evidence=Map.of(); }
    @SubscribeEvent public static void commands(RegisterCommandsEvent event) {
        event.getDispatcher().register(net.minecraft.commands.Commands.literal("wuxing")
                .then(net.minecraft.commands.Commands.literal("audit").requires(source -> source.hasPermission(2))
                .then(net.minecraft.commands.Commands.literal("acquisition").executes(context -> {
                    rebuild(context.getSource().getServer());
                    var text = new StringBuilder("# Acquisition Evidence\n\nDirect source evidence only; conditions, ingredient reachability, trades and scripted routes require review. UNREVIEWED does not mean probability zero.\n\n| Item | Classification | Evidence |\n|---|---|---|\n");
                    WuXingItemSources.auditTable().forEach((item, classification) -> text.append('|').append(item).append('|')
                            .append(classification).append('|').append(String.join("; ", sources(ResourceLocation.tryParse(item)))).append("|\n"));
                    try {
                        var path = context.getSource().getServer().getServerDirectory().toPath().resolve("reports/acquisition.md");
                        java.nio.file.Files.createDirectories(path.getParent());
                        java.nio.file.Files.writeString(path, text);
                        java.nio.file.Files.writeString(path.resolveSibling("acquisition-evidence.json"),
                                new GsonBuilder().setPrettyPrinting().create().toJson(evidence));
                        context.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal(path.toAbsolutePath().toString()), false);
                        return routes.size();
                    } catch (java.io.IOException exception) {
                        context.getSource().sendFailure(net.minecraft.network.chat.Component.literal(exception.getMessage())); return 0;
                    }
                }))));
    }
    private AcquisitionCatalog() {}
}
