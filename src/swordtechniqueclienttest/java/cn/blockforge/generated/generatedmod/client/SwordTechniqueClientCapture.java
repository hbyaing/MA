package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.Network;
import cn.blockforge.generated.generatedmod.WuXingItems;
import cn.blockforge.generated.generatedmod.yujian.NurtureData;
import cn.blockforge.generated.generatedmod.yujian.SwordNurture;
import cn.blockforge.generated.generatedmod.yujian.SwordProfile;
import cn.blockforge.generated.generatedmod.yujian.YujianController;
import com.google.gson.GsonBuilder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Camera;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

/** Test-only real client captures for the five redesigned sword technique silhouettes. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID, value=Dist.CLIENT)
public final class SwordTechniqueClientCapture {
    private static final boolean ENABLED = Boolean.getBoolean("wuxingxiantu.swordTechniqueCapture");
    private static final Path OUTPUT = Path.of(System.getProperty("wuxingxiantu.swordTechniqueOutput", "../../reports/sword-technique-client"));
    private static final String[] TECHNIQUES = {"art_sword_pierce", "art_sword_sweep", "art_sword_guard"};
    private static final List<Map<String, Object>> CAPTURES = new ArrayList<>();
    private static final List<String> FAILURES = new ArrayList<>();
    private static final java.lang.reflect.Method CAMERA_POSITION = ObfuscationReflectionHelper.findMethod(Camera.class, "m_90581_", Vec3.class);
    private static final long START = System.nanoTime();
    private static boolean opened, prepared, finished;
    private static int tick, techniqueIndex = -1;
    private static java.util.concurrent.CompletableFuture<?> preparation;
    private static java.util.concurrent.CompletableFuture<?> castRequest;
    private static boolean castSent;
    private static String pending;

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void tick(TickEvent.ClientTickEvent event) {
        if (!ENABLED || finished || event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        try {
            Files.createDirectories(OUTPUT);
            if ((System.nanoTime() - START) / 1_000_000_000 > 240) throw new AssertionError("Sword technique capture watchdog exceeded 240 seconds");
            mc.setWindowActive(true); mc.options.pauseOnLostFocus = false;
            if (!opened && mc.player != null) throw new AssertionError("Fixture refuses to adopt an existing world");
            if (!opened && mc.getOverlay() == null && (mc.screen instanceof net.minecraft.client.gui.screens.TitleScreen
                    || mc.screen instanceof net.minecraft.client.gui.screens.AccessibilityOnboardingScreen)) {
                opened = true;
                mc.getWindow().setWindowed(1280, 800); mc.resizeDisplay();
                org.lwjgl.glfw.GLFW.glfwSetWindowTitle(mc.getWindow().getWindow(), "sword-technique-test (isolated)");
                if (!Boolean.getBoolean("wuxingxiantu.swordTechniqueShowWindow")) org.lwjgl.glfw.GLFW.glfwHideWindow(mc.getWindow().getWindow());
                mc.options.renderDistance().set(4); mc.options.simulationDistance().set(5); mc.options.cloudStatus().set(CloudStatus.OFF);
                mc.options.framerateLimit().set(60); mc.options.fov().set(55); mc.options.bobView().set(false); mc.options.hideGui = true;
                mc.getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);
                var rules = new GameRules(); rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false, null);
                mc.createWorldOpenFlows().createFreshLevel("sword-technique-test-" + System.currentTimeMillis(),
                        new LevelSettings("Sword technique visual verification", GameType.SURVIVAL, false, net.minecraft.world.Difficulty.PEACEFUL, true, rules, WorldDataConfiguration.DEFAULT),
                        new WorldOptions(92341, false, false), net.minecraft.world.level.levelgen.presets.WorldPresets::createNormalWorldDimensions);
            }
            if (!opened || mc.player == null || mc.level == null || mc.getSingleplayerServer() == null || mc.getOverlay() != null
                    || mc.screen instanceof net.minecraft.client.gui.screens.ReceivingLevelScreen) return;
            if (mc.screen != null) mc.setScreen(null);
            if (!prepared) {
                prepared = true;
                var id = mc.player.getUUID();
                preparation = mc.getSingleplayerServer().submitAsync(() -> {
                    var p = mc.getSingleplayerServer().getPlayerList().getPlayer(id);
                    var level = p.serverLevel();
                    level.setDayTime(6000); level.setWeatherParameters(6000, 0, false, false);
                    level.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false, level.getServer());
                    p.teleportTo(level, 0.5D, 180.0D, 0.5D, p.getYRot(), p.getXRot()); p.setNoGravity(true); p.removeAllEffects();
                    var stack = new ItemStack(WuXingItems.DARK_IRON_SWORD.get());
                    var n = NurtureData.read(stack); n.nurtureStage = 6; n.knowledgeMigrated = true;
                    for (String idTech : TECHNIQUES) { GeneratedMod.data(p).wuxingxiantu$learnSwordTechnique(idTech); n.equippedTechniques.add(idTech); }
                    n.write(stack, SwordProfile.of(stack)); p.getInventory().setItem(0, stack); p.getInventory().selected = 0;
                    var data = GeneratedMod.data(p); data.wuxingxiantu$setRealm(5); data.wuxingxiantu$setMaxMana(1_000_000); data.wuxingxiantu$setAura(1_000_000);
                    GeneratedMod.sync(p); p.containerMenu.broadcastChanges();
                });
                return;
            }
            if (preparation != null) { if (!preparation.isDone()) return; preparation.join(); preparation = null; }
            tick++;
            if (tick == 20) techniqueIndex = 0;
            if (techniqueIndex >= 0 && techniqueIndex < TECHNIQUES.length && tick >= 20 + techniqueIndex * 115 && tick < 20 + techniqueIndex * 115 + 2 && !castSent) {
                castSent = true; String id = mc.player.getUUID().toString(), art = TECHNIQUES[techniqueIndex];
                castRequest = mc.getSingleplayerServer().submitAsync(() -> {
                    var p = mc.getSingleplayerServer().getPlayerList().getPlayer(java.util.UUID.fromString(id));
                    if (!SwordNurture.cast(p, SwordNurture.current(p), art)) throw new AssertionError("Server rejected " + art);
                });
                pending = String.format("%02d-%s.png", techniqueIndex + 1, art);
            }
            if (pending != null && tick >= 25 + techniqueIndex * 115) {
                if (castRequest != null && !castRequest.isDone()) return;
                if (castRequest != null) { castRequest.join(); castRequest = null; }
                String name = pending; pending = null;
                try (var image = Screenshot.takeScreenshot(mc.getMainRenderTarget())) { image.writeToFile(OUTPUT.resolve(name)); }
                var row = new LinkedHashMap<String, Object>(); row.put("technique", TECHNIQUES[techniqueIndex]); row.put("tick", tick); row.put("path", name); row.put("glError", org.lwjgl.opengl.GL11.glGetError()); CAPTURES.add(row);
                write();
                if (++techniqueIndex < TECHNIQUES.length) { castSent = false; }
                else finish();
            }
        } catch (Throwable error) { FAILURES.add(error.toString()); com.mojang.logging.LogUtils.getLogger().error("SWORD_TECHNIQUE_CAPTURE", error); finish(); }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void camera(ViewportEvent.ComputeCameraAngles event) throws Exception {
        if (!ENABLED || finished) return;
        var mc = Minecraft.getInstance(); if (mc.player == null) return;
        Vec3 target = mc.player.position().add(0, 1.3, 0);
        Vec3 eye = target.add(8, 4.5, 8); Vec3 delta = target.subtract(eye);
        CAMERA_POSITION.invoke(event.getCamera(), eye);
        event.setYaw((float) Math.toDegrees(Math.atan2(-delta.x, delta.z)));
        event.setPitch((float) -Math.toDegrees(Math.atan2(delta.y, delta.horizontalDistance())));
        event.setRoll(0);
    }

    private static void write() throws Exception {
        var result = new LinkedHashMap<String, Object>(); result.put("test", "five-redesigned-sword-techniques"); result.put("result", FAILURES.isEmpty() ? "RUNNING" : "FAIL"); result.put("captures", CAPTURES); result.put("failures", FAILURES);
        Files.writeString(OUTPUT.resolve("captures.json"), new GsonBuilder().setPrettyPrinting().create().toJson(result));
    }
    private static void finish() {
        if (finished) return; finished = true;
        try { Files.createDirectories(OUTPUT); write(); Files.writeString(OUTPUT.resolve(FAILURES.isEmpty() ? "complete.txt" : "failure.txt"), FAILURES.isEmpty() ? "PASS: five actual client technique screenshots captured.\n" : String.join("\n", FAILURES)); }
        catch (Exception error) { com.mojang.logging.LogUtils.getLogger().error("SWORD_TECHNIQUE_CAPTURE_REPORT", error); }
        Minecraft.getInstance().stop();
    }
}
