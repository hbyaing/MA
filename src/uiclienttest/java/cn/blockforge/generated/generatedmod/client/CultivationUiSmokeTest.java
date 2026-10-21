package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.*;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

/** Opt-in client smoke test. All fixtures live in a new run-ui-test world. */
@Mod.EventBusSubscriber(modid = GeneratedMod.MOD_ID, value = Dist.CLIENT)
public final class CultivationUiSmokeTest {
    private static final String[] PAGES = {"inventory", "vision", "arts", "sword", "advancements", "settings"};
    private static final int CASES = 20;
    private static final int[][] SIZES = {{1920,1080,3}, {1920,1080,4}, {2560,1440,3}, {2560,1440,4}, {1280,960,4}};
    private static int phase, ticks, step;
    private static boolean capture, finished;
    private static CompletableFuture<Void> language;
    private static volatile boolean fixtureReady;
    private static CultivationMainScreen screen;
    private static final Path OUTPUT = Path.of("ui-smoke");
    private static long started = System.currentTimeMillis();

    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || finished) return;
        Minecraft mc = Minecraft.getInstance();
        try {
            if (phase == 4 && step % CASES >= 13 && step % CASES <= 17
                    && mc.screen instanceof net.minecraft.client.gui.screens.PauseScreen) mc.setScreen(null);
            if (System.currentTimeMillis() - started > 240000) throw new IllegalStateException("Client smoke timeout, phase=" + phase);
            if (mc.getOverlay() != null) return;
            if (phase == 0 && mc.screen != null) {
                Files.createDirectories(OUTPUT);
                mc.options.languageCode = "zh_cn";
                mc.options.pauseOnLostFocus = false;
                mc.getLanguageManager().setSelected("zh_cn");
                mc.options.renderDistance().set(2); mc.options.simulationDistance().set(5);
                mc.getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);
                language = mc.reloadResourcePacks(); phase = 1;
            } else if (phase == 1 && language.isDone()) {
                String[] swordIds = {"peach", "dark_iron", "azure_steel", "tiangang", "big_dipper", "demon_slayer", "pure_yang", "xuanyuan"};
                String[] swordNames = {"桃木剑", "玄铁剑", "明玦剑", "青冥剑", "霁霄剑", "无相剑", "归元剑", "天阙剑"};
                for (int i = 0; i < swordIds.length; i++) {
                    String actual = net.minecraft.network.chat.Component.translatable("item.wuxingxiantu." + swordIds[i] + "_sword").getString();
                    if (!actual.equals(swordNames[i])) throw new IllegalStateException("Sword name: " + actual);
                }
                phase = 2;
                var rules = new GameRules();
                rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false, null);
                mc.createWorldOpenFlows().createFreshLevel("ui-smoke-" + System.currentTimeMillis(),
                        new LevelSettings("UI smoke", GameType.SURVIVAL, false, Difficulty.PEACEFUL, true, rules, WorldDataConfiguration.DEFAULT),
                        new WorldOptions(19840919L, false, false), access -> access.registryOrThrow(Registries.WORLD_PRESET)
                                .getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions());
            } else if (phase == 2 && mc.player != null && mc.getSingleplayerServer() != null) {
                phase = 3;
                var id = mc.player.getUUID();
                mc.getSingleplayerServer().execute(() -> {
                    var player = mc.getSingleplayerServer().getPlayerList().getPlayer(id);
                    var d = GeneratedMod.data(player);
                    d.wuxingxiantu$setRealm(7); d.wuxingxiantu$setCultivationStage(3);
                    d.wuxingxiantu$setRootMask(31);
                    for (int i = 0; i < 5; i++) d.wuxingxiantu$setRootQuality(i, 4);
                    d.wuxingxiantu$setAura(10000); d.wuxingxiantu$setCultivation(10000000);
                    for (var art : ArtCatalog.ENTRIES) d.wuxingxiantu$learnArt(art.id());
                    for (int i = 0; i < 8; i++) d.wuxingxiantu$equipArt(i, ArtCatalog.ENTRIES.get(i).id());
                    for (var art : DivineArtCatalog.ENTRIES) d.wuxingxiantu$learnDivineArt(art.id());
                    for (int i = 0; i < 3; i++) d.wuxingxiantu$setEquippedDivineArt(i, DivineArtCatalog.ENTRIES.get(i).id());
                    for (String heart : HeartMethodCatalog.METHOD_IDS) d.wuxingxiantu$learnHeartMethod(heart);
                    d.wuxingxiantu$setEquippedHeartMethod("yuan_hun");
                    for (int i = 1; i <= 9; i++) d.wuxingxiantu$learnFire(i);
                    d.wuxingxiantu$setEquippedFire(5);
                    player.getInventory().setItem(0, new ItemStack(WuXingItems.DARK_IRON_SWORD.get()));
                    player.getInventory().setItem(9, new ItemStack(Items.OAK_LOG, 32));
                    player.getInventory().setItem(10, new ItemStack(WuXingItems.AURA_CRYSTAL.get(), 16));
                    player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 24000));
                    player.giveExperienceLevels(27); player.giveExperiencePoints(17);
                    GeneratedMod.sync(player); player.inventoryMenu.broadcastChanges(); fixtureReady = true;
                });
            } else if (phase == 3 && fixtureReady && ++ticks > 40) {
                phase = 4; ticks = 0;
                mc.options.guiScale().set(3); mc.resizeDisplay();
                mc.setScreen(new InventoryScreen(mc.player));
                if (!(mc.screen instanceof CultivationMainScreen)) throw new IllegalStateException("Inventory entry was not replaced");
                screen = (CultivationMainScreen) mc.screen;
                if (screen.getMenu() != mc.player.inventoryMenu || screen.getMenu().slots.size() != 46)
                    throw new IllegalStateException("Vanilla inventory menu/slots changed");
                inventoryClicks(mc);
                configure(mc);
            } else if (phase == 4 && !capture && ++ticks > 12) {
                capture = true;
            }
        } catch (Throwable ex) { fail(mc, ex); }
    }

    private static void configure(Minecraft mc) {
        int group = step / CASES, page = step % CASES;
        int[] size = SIZES[group];
        org.lwjgl.glfw.GLFW.glfwSetWindowSize(mc.getWindow().getWindow(), size[0], size[1]);
        mc.options.guiScale().set(size[2]); mc.resizeDisplay();
        mc.getToasts().clear();
        mc.gui.getChat().clearMessages(true);
        mc.options.hideGui = page == 15;
        if (page >= 13 && page <= 17) {
            mc.setScreen(null);
            var id = mc.player.getUUID();
            mc.getSingleplayerServer().execute(() -> {
                var player = mc.getSingleplayerServer().getPlayerList().getPlayer(id);
                player.setGameMode(page == 16 ? GameType.CREATIVE : page == 17 ? GameType.SPECTATOR : GameType.SURVIVAL);
                player.setHealth(page == 14 ? 3 : player.getMaxHealth());
                player.setAbsorptionAmount(page == 14 ? 8 : 0);
                player.getFoodData().setFoodLevel(page == 14 ? 4 : 20);
            });
            ticks = 0;
            return;
        }
        if (mc.screen != screen) {
            var id = mc.player.getUUID();
            mc.getSingleplayerServer().execute(() -> mc.getSingleplayerServer().getPlayerList().getPlayer(id).setGameMode(GameType.SURVIVAL));
            mc.setScreen(screen);
        }
        screen.selectPage(page < 6 ? PAGES[page] : page == 6 ? "inventory" : page == 10 || page >= 18 ? "vision" : page == 11 ? "settings" : "arts");
        if (page == 5 || page == 11) {
            screen.scrollToTop();
            try {
                var field = CultivationMainScreen.class.getDeclaredField("page"); field.setAccessible(true);
                var active = (CultivationPage) field.get(screen);
                for (var widget : java.util.List.copyOf(active.children()))
                    if (widget instanceof CultivationUi.Tab action && action.getMessage().getString().equals(page == 5 ? "界面显示" : "御剑视角")) clickPageWidget(action);
                active = (CultivationPage) field.get(screen);
                long sliders = active.children().stream().filter(w -> w instanceof CultivationUi.Slider).count();
                if (sliders != (page == 11 ? 5 : 0)) throw new IllegalStateException("Settings category did not rebuild its controls");
            } catch (ReflectiveOperationException ex) { throw new IllegalStateException(ex); }
        }
        if (page == 1 || page == 18) {
            screen.scrollToTop();
            try {
                var field = CultivationMainScreen.class.getDeclaredField("page"); field.setAccessible(true);
                var active = (CultivationPage) field.get(screen);
                String target = page == 18 ? "药水状态" : "修炼状态";
                for (var widget : java.util.List.copyOf(active.children()))
                    if (widget instanceof CultivationUi.Action action && action.getMessage().getString().equals(target)) clickPageWidget(action);
            } catch (ReflectiveOperationException ex) { throw new IllegalStateException(ex); }
        }
        if (page == 6) {
            screen.getRecipeBookComponent().toggleVisibility();
            if (!screen.getRecipeBookComponent().isVisible()) throw new IllegalStateException("Recipe book failed to open");
        } else if (screen.getRecipeBookComponent().isVisible()) screen.getRecipeBookComponent().toggleVisibility();
        if (page == 7 || page == 8 || page == 9 || page == 12) {
            CultivationPage active = null;
            try {
                var f = CultivationMainScreen.class.getDeclaredField("page"); f.setAccessible(true); active = (CultivationPage) f.get(screen);
            } catch (ReflectiveOperationException e) { throw new IllegalStateException(e); }
            if (page == 7) active.mouseClicked(active.width - 40, 35, 0);
            else {
                for (var widget : java.util.List.copyOf(active.children())) if (widget instanceof CultivationUi.Action action && action.getMessage().getString().equals("返回配置")) action.onPress();
                String label = page == 9 ? "心法" : page == 12 ? "功法" : "神通";
                for (var widget : java.util.List.copyOf(active.children())) if (widget instanceof CultivationUi.Tab action && action.getMessage().getString().equals(label)) action.onPress();
                if (page == 12) {
                    screen.scrollToTop();
                    for (var widget : active.children()) if (widget instanceof net.minecraft.client.gui.components.EditBox box) {
                        try { clickPageWidget(box); } catch (ReflectiveOperationException ex) { throw new IllegalStateException(ex); }
                        screen.keyPressed(69, 0, 0); screen.charTyped('e', 0);
                        if (mc.screen != screen || !box.getValue().equals("e")) throw new IllegalStateException("Inventory key interrupted search input");
                        box.setValue(""); box.setFocused(false);
                    }
                    active.mouseScrolled(5, active.height - 60, -20);
                    active.mouseScrolled(active.width - 10, active.height - 60, -20);
                }
            }
        }
        if (page >= 10 && page <= 12 || page == 19) screen.mouseScrolled(screen.getGuiLeft() + screen.getXSize() - 10, screen.getGuiTop() + screen.getYSize() - 20, -100);
        ticks = 0;
    }
    private static void inventoryClicks(Minecraft mc) {
        var layout = CultivationLayout.of(screen.width, screen.height);
        var first = screen.getMenu().getSlot(9);
        var empty = screen.getMenu().getSlot(11);
        int x = layout.inventoryX() + first.x + 8, y = layout.inventoryY() + first.y + 8;
        screen.mouseClicked(x, y, 0); screen.mouseReleased(x, y, 0);
        if (!screen.getMenu().getCarried().is(Items.OAK_LOG)) throw new IllegalStateException("Item pickup failed");
        screen.selectPage("arts");
        try {
            var field = CultivationMainScreen.class.getDeclaredField("page"); field.setAccessible(true);
            if (field.get(screen) != null) throw new IllegalStateException("Page changed while carrying an item");
        } catch (ReflectiveOperationException ex) { throw new IllegalStateException(ex); }
        x = layout.inventoryX() + empty.x + 8; y = layout.inventoryY() + empty.y + 8;
        screen.mouseClicked(x, y, 0); screen.mouseReleased(x, y, 0);
        if (!screen.getMenu().getCarried().isEmpty() || !empty.getItem().is(Items.OAK_LOG)) throw new IllegalStateException("Item placement failed");
    }
    private static void clickPageWidget(net.minecraft.client.gui.components.AbstractWidget action) throws ReflectiveOperationException {
        var scaleField = CultivationMainScreen.class.getDeclaredField("pageScale"); scaleField.setAccessible(true);
        var xField = CultivationMainScreen.class.getDeclaredField("contentX"); xField.setAccessible(true);
        var yField = CultivationMainScreen.class.getDeclaredField("contentY"); yField.setAccessible(true);
        float scale = scaleField.getFloat(screen);
        double x = xField.getInt(screen) + (action.getX() + action.getWidth() / 2.0) * scale;
        double y = yField.getInt(screen) + (action.getY() + action.getHeight() / 2.0) * scale;
        if (!screen.mouseClicked(x, y, 0)) throw new IllegalStateException("Scaled control did not receive its click: " + action.getMessage());
        screen.mouseReleased(x, y, 0);
    }

    @SubscribeEvent public static void render(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !capture || finished) return;
        var mc = Minecraft.getInstance();
        try {
            int testCase = step % CASES;
            String[] extra = {"recipe", "wheel", "divine", "heart", "vision-bottom", "settings-bottom", "arts-scrolled", "hud", "hud-absorption", "hud-hidden", "hud-creative", "hud-spectator", "effects", "effects-bottom"};
            if (testCase >= 13 && testCase <= 17) verifyHud(mc, testCase);
            if (testCase == 18) {
                var effects = new net.minecraftforge.client.event.ScreenEvent.RenderInventoryMobEffects(screen, 100, true, 0);
                CultivationScreenEvents.effects(effects);
                if (!effects.isCanceled()) throw new IllegalStateException("Duplicate inventory effects visible");
            }
            String name = String.format("%02d-%s-scale%d", step, testCase < 6 ? PAGES[testCase] : extra[testCase - 6], mc.options.guiScale().get());
            try (var pixels = Screenshot.takeScreenshot(mc.getMainRenderTarget())) {
                pixels.writeToFile(OUTPUT.resolve(name + ".png"));
            }
            capture = false;
            if (++step >= CASES * SIZES.length) {
                mc.options.hideGui = false;
                mc.setScreen(screen); screen.keyPressed(256, 0, 0);
                if (mc.screen != null) throw new IllegalStateException("ESC failed to close the screen");
                Files.writeString(OUTPUT.resolve("result.txt"), "PASS: 100 real client captures; inventory replacement, original 46-slot menu, item pickup/place, carried-item navigation guard, recipe book, six pages, wheel, divine, heart, scroll, effects view and scrolling; HUD survival/absorption/F1/creative/spectator; duplicate health/armor/food/XP canceled, potion HUD and inventory duplicates hidden; other overlays preserved; eight sword names; 1920x1080 and 2560x1440 scales 3/4, 320x240 GUI boundary, ESC.\n");
                finished = true; mc.stop();
            } else configure(mc);
        } catch (Throwable ex) { fail(mc, ex); }
    }
    private static void verifyHud(Minecraft mc, int testCase) {
        if (mc.screen != null) throw new IllegalStateException("Gameplay capture obscured by " + mc.screen.getClass());
        boolean expected = testCase != 15 && testCase != 17;
        if (WuXingClient.visible(mc) != expected) throw new IllegalStateException("HUD visibility case " + testCase);
        for (var overlay : net.minecraftforge.client.gui.overlay.VanillaGuiOverlay.values()) {
            var event = new net.minecraftforge.client.event.RenderGuiOverlayEvent.Pre(mc.getWindow(), null, 0, overlay.type());
            WuXingClient.hideDuplicateOverlay(event);
            boolean duplicate = switch (overlay) {
                case PLAYER_HEALTH, ARMOR_LEVEL, FOOD_LEVEL, EXPERIENCE_BAR -> true;
                default -> false;
            };
            if (event.isCanceled() != (overlay == net.minecraftforge.client.gui.overlay.VanillaGuiOverlay.POTION_ICONS || expected && duplicate)) throw new IllegalStateException("Overlay cancellation: " + overlay);
        }
    }
    private static void fail(Minecraft mc, Throwable ex) {
        finished = true;
        com.mojang.logging.LogUtils.getLogger().error("UI_SMOKE_FAIL", ex);
        try { Files.createDirectories(OUTPUT); Files.writeString(OUTPUT.resolve("result.txt"), "FAIL: " + ex); } catch (Exception ignored) {}
        mc.stop();
    }
}
