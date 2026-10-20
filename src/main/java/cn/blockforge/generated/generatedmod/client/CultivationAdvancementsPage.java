package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.fml.ModList;
import java.nio.file.Files;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import java.util.LinkedHashMap;
import java.util.Map;

/** 读取原版已同步进度；不在 UI 推算条件或发放奖励。 */
public final class CultivationAdvancementsPage extends CultivationPage implements ClientAdvancements.Listener {
    private final Map<Advancement, AdvancementProgress> progress = new LinkedHashMap<>();
    private final Map<ResourceLocation, DisplayInfo> definitions = new LinkedHashMap<>();
    private int filter, listX;
    public CultivationAdvancementsPage() { super("成就"); }
    @Override public int contentHeight(int w, int h) { return h; }
    @Override protected void init() {
        progress.clear();
        loadDefinitions();
        listX = width >= 280 ? 72 : 58;
        String[] names = {"全部", "未达成", "已达成"};
        for (int i = 0; i < names.length; i++) {
            final int value = i;
            addRenderableWidget(new CultivationUi.Tab(0, 5 + i * 27, listX - 10, 22, Component.literal(names[i]), b -> { filter = value; host.scrollToTop(); }, () -> filter == value));
        }
        if (minecraft.getConnection() != null) minecraft.getConnection().getAdvancements().setListener(this);
    }
    @Override public void render(GuiGraphics g, int mx, int my, float delta) {
        int panelHeight = Math.max(host.contentViewportHeight(), height);
        CultivationUi.panel(g, 0, 0, width, panelHeight, false);
        CultivationBorderAssets.draw(g, "rule_gold_vertical", listX - 5, 0, 4, panelHeight);
        g.drawString(font, "成就列表", listX, 6, CultivationUi.GOLD, false);
        CultivationUi.rule(g, listX, 21, width - listX - 5);
        int y = 30, count = 0;
        for (var entry : definitions.entrySet()) {
            var state = progress.entrySet().stream().filter(p -> p.getKey().getId().equals(entry.getKey()))
                    .map(Map.Entry::getValue).filter(java.util.Objects::nonNull).findFirst().orElse(null);
            var display = entry.getValue();
            boolean done = state != null && state.isDone();
            if (filter == 1 && done || filter == 2 && !done) continue;
            boolean hidden = display.isHidden() && !done;
            int top = y;
            if (!hidden) g.renderItem(display.getIcon(), listX + 3, y + 2);
            else g.drawString(font, "?", listX + 7, y + 6, CultivationUi.MUTED, false);
            CultivationUi.fit(g, hidden ? Component.literal("???") : display.getTitle(), listX + 26, y, width - listX - 32,
                    done ? CultivationUi.GOLD : CultivationUi.TEXT, mx, my);
            CultivationUi.fit(g, Component.literal(done ? "已达成" : state == null ? "进度未同步" : "未达成"), listX + 26, y + 14, width - listX - 32, done ? CultivationUi.GREEN : CultivationUi.MUTED, mx, my);
            y += 30;
            if (!hidden) for (var line : font.split(display.getDescription(), width - listX - 34)) {
                g.drawString(font, line, listX + 26, y, CultivationUi.MUTED, false); y += 12;
            }
            if (state != null && !hidden) {
                y += 5;
                g.fill(listX + 26, y, width - 8, y + 3, CultivationUi.PANEL);
                g.fill(listX + 26, y, listX + 26 + (int)((width - listX - 34) * state.getPercent()), y + 3, done ? CultivationUi.GOLD : CultivationUi.CYAN);
                if (mx >= 0 && mx < width && my >= top && my < y + 10 && state.getProgressText() != null)
                    CultivationUi.tooltip(Component.literal(state.getProgressText()));
                y += 8;
            }
            CultivationUi.softDivider(g,listX,y+7,width-listX-8,false);
            y += 20; count++;
        }
        if (count == 0) g.drawWordWrap(font, Component.literal("暂无" + (filter == 2 ? "已达成" : filter == 1 ? "未达成" : "可见") + "的修行成就"), listX, 38, width - listX - 12, CultivationUi.MUTED);
        height = Math.max(100, y + 8);
        super.render(g, mx, my, delta);
    }
    private void loadDefinitions() {
        definitions.clear();
        // 原版不会预先同步无进度的独立根成就；用模组自带 JSON 提供说明，进度仍只认服务器。
        var root = ModList.get().getModFileById(GeneratedMod.MOD_ID).getFile().findResource("data", GeneratedMod.MOD_ID, "advancements");
        if (!Files.isDirectory(root)) return;
        try (var files = Files.walk(root)) {
            for (var file : files.filter(p -> p.toString().endsWith(".json")).sorted().toList()) {
                try (var reader = Files.newBufferedReader(file)) {
                    var json = GsonHelper.parse(reader);
                    if (!json.has("display")) continue;
                    String path = root.relativize(file).toString().replace('\\', '/');
                    definitions.put(new ResourceLocation(GeneratedMod.MOD_ID, path.substring(0, path.length() - 5)),
                            DisplayInfo.fromJson(GsonHelper.getAsJsonObject(json, "display")));
                } catch (Exception ex) { com.mojang.logging.LogUtils.getLogger().warn("Unable to read advancement display {}", file, ex); }
            }
        } catch (java.io.IOException ex) { com.mojang.logging.LogUtils.getLogger().warn("Unable to list cultivation advancements", ex); }
    }
    private void add(Advancement a) {
        if (a.getId().getNamespace().equals(GeneratedMod.MOD_ID) && a.getDisplay() != null) {
            definitions.put(a.getId(), a.getDisplay()); progress.putIfAbsent(a, null);
        }
    }
    @Override public void onAddAdvancementRoot(Advancement a) { add(a); }
    @Override public void onAddAdvancementTask(Advancement a) { add(a); }
    @Override public void onRemoveAdvancementRoot(Advancement a) { progress.remove(a); }
    @Override public void onRemoveAdvancementTask(Advancement a) { progress.remove(a); }
    @Override public void onAdvancementsCleared() { progress.clear(); }
    @Override public void onSelectedTabChanged(Advancement a) {}
    @Override public void onUpdateAdvancementProgress(Advancement a, AdvancementProgress p) { add(a); if (progress.containsKey(a)) progress.put(a, p); }
    @Override public void removed() { if (minecraft.getConnection() != null) minecraft.getConnection().getAdvancements().setListener(null); }
}
