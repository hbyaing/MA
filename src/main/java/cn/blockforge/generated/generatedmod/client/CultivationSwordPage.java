package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.Network;
import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.ArtCatalog;
import cn.blockforge.generated.generatedmod.ProgressionGate;
import cn.blockforge.generated.generatedmod.WuXingItems;
import cn.blockforge.generated.generatedmod.items.SpiritSwordItem;
import cn.blockforge.generated.generatedmod.yujian.*;
import cn.blockforge.generated.generatedmod.robe.YujianSlot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import java.util.*;

/** 灵剑页只发送既有请求；成长数据和装备校验仍由服务端处理。 */
public final class CultivationSwordPage extends CultivationPage {
    private int swordSlot = -1;
    private int detailsY, techniquesY, detailX, detailW, listBottom;
    private String selectedTechnique;
    private final List<CultivationUi.Action> swordButtons = new ArrayList<>();
    private final List<CultivationUi.Action> techniqueButtons = new ArrayList<>();
    private CultivationUi.Action breakthrough;
    private String snapshot = "";
    public CultivationSwordPage() { super("灵剑"); }
    @Override public int contentHeight(int w, int h) { return h; }
    private ItemStack sword() { return YujianSlot.nurtureStack(minecraft.player,swordSlot); }
    private NurtureData nurture() { return sword().getItem() instanceof SpiritSwordItem ? SpiritSwordItem.nurture(sword()) : null; }
    private SwordProfile profile() { return sword().getItem() instanceof SpiritSwordItem spirit ? spirit.profile() : null; }
    private boolean knows(String id) {
        var data = GeneratedMod.data(minecraft.player);
        return data != null && data.wuxingxiantu$knowsSwordTechnique(id);
    }
    private void rebuild() { init(); }
    private void request(byte action, int techniqueSlot, String id) {
        Network.CHANNEL.sendToServer(new cn.blockforge.generated.generatedmod.network.packet.yujian.SwordNurtureActionPacket(action, swordSlot, -1, techniqueSlot, id));
    }
    @Override protected void init() {
        clearWidgets(); swordButtons.clear(); techniqueButtons.clear(); breakthrough = null;
        List<Integer> swordSlots = new ArrayList<>();
        if(YujianSlot.accepts(YujianSlot.activeFlyingSword(minecraft.player)))swordSlots.add(YujianSlot.NURTURE_SLOT);
        for (int i = 0; i < minecraft.player.getInventory().getContainerSize(); i++)
            if (minecraft.player.getInventory().getItem(i).getItem() instanceof SpiritSwordItem) swordSlots.add(i);
        if (!swordSlots.contains(swordSlot)) { swordSlot = swordSlots.isEmpty() ? -1 : swordSlots.get(0); selectedTechnique = null; }
        boolean wide = width >= 280;
        detailX = wide ? 106 : 0; detailW = width - detailX - 8;
        int columns = wide ? 1 : 2;
        int w = wide ? 94 : Math.max(80, (width - 12) / 2);
        for (int i = 0; i < swordSlots.size(); i++) {
            int slot = swordSlots.get(i);
            swordButtons.add(addRenderableWidget(new CultivationUi.Action((i % columns) * (w + 4), 30 + i / columns * 25, w, 22,
                    slot==YujianSlot.NURTURE_SLOT?Component.translatable("slot.wuxingxiantu.yujian").append(": ").append(YujianSlot.nurtureStack(minecraft.player,slot).getHoverName()):YujianSlot.nurtureStack(minecraft.player,slot).getHoverName(),
                    b -> { swordSlot = slot; selectedTechnique = null; request((byte)4, -1, ""); rebuild(); }, () -> slot == swordSlot)));
        }
        listBottom = 40 + ((swordSlots.size() + columns - 1) / columns) * 25;
        detailsY = wide ? 30 : listBottom;
        if (sword().isEmpty()) { height = Math.max(130, detailsY); return; }
        var n = nurture(); var p = profile();
        int y = detailsY;
        y += textHeight(sword().getHoverName().getString() + " · 品阶：" + p.swordTier());
        y += textHeight("剑蕴阶段：" + n.nurtureStage + " / " + p.maxNurtureStage() + "    洞察点：" + n.insightPoints);
        y += 28;
        y += textHeight("御剑资格：" + (FlyingSwordRegistry.canFly(sword()) ? "可御剑" : "不可御剑") + "    剑技槽：" + n.equippedTechniques.size() + " / " + p.maxTechniqueSlots());
        var cultivation=GeneratedMod.data(minecraft.player);
        int cost=SwordNurture.breakthroughCost();
        breakthrough = addRenderableWidget(new CultivationUi.Action(detailX, y, detailW, 23,
                Component.translatable("screen.wuxingxiantu.sword_nurture_breakthrough",cost),
                b -> request((byte)0, -1, "")));
        breakthrough.active = n.ready(p) && cultivation!=null && cultivation.wuxingxiantu$getAura()>=cost;
        techniquesY = y + 38;
        int index = 0;
        for (var t : SwordTechniqueRegistry.all()) {
            boolean equipped = n.equippedTechniques.contains(t.id());
            boolean learned = knows(t.id());
            int rowY = techniquesY + 20 + index++ * 26;
            var button = addRenderableWidget(new CultivationUi.Action(detailX, rowY, detailW - 45, 23,
                    Component.literal(t.name() + " · " + (equipped ? (learned ? "已装备" : "已装备 / 未学会") : learned ? "已学会" : "学习需 " + t.insightCost() + " 洞察")),
                    b -> {
                        selectedTechnique = t.id();
                        var current = nurture();
                        if (current == null) return;
                        int slot = current.equippedTechniques.indexOf(t.id());
                        if (slot >= 0) request((byte)3, slot, t.id());
                        else if (knows(t.id())) request((byte)2, -1, t.id());
                        else request((byte)1, -1, t.id());
                    }, () -> t.id().equals(selectedTechnique)));
            boolean progression = ProgressionGate.canUseSword(minecraft.player, sword()) && ProgressionGate.canUseArt(minecraft.player, t.id());
            button.active = equipped || progression && (learned ? SwordTechniqueRegistry.supports(t, p, n)
                    && n.equippedTechniques.size() < p.maxTechniqueSlots() : SwordTechniqueRegistry.canLearn(t, p, n));
            techniqueButtons.add(button);
            var release = addRenderableWidget(new CultivationUi.Action(detailX + detailW - 41, rowY, 41, 23,
                    Component.literal("施展"), b -> { request((byte)5, -1, t.id()); onClose(); }));
            release.active = equipped && learned && progression && SwordTechniqueRegistry.supports(t, p, n)
                    && (!(minecraft.player.getMainHandItem().getItem() instanceof SpiritSwordItem) || minecraft.player.getMainHandItem() == sword());
        }
        // 携带大量灵剑时，滚动范围随实际内容增长，保证底部剑技仍可操作。
        height = Math.max(listBottom, techniquesY + 28 + techniqueButtons.size() * 26);
    }
    @Override public void tick() {
        StringBuilder now = new StringBuilder();
        var data = GeneratedMod.data(minecraft.player);
        if (data != null) now.append(Arrays.toString(data.wuxingxiantu$getKnownSwordTechniques())).append(data.wuxingxiantu$getRealm())
                .append(':').append(data.wuxingxiantu$getAura()>=SwordNurture.breakthroughCost());
        now.append(minecraft.player.getInventory().selected).append(minecraft.player.getAbilities().instabuild);
        var inventory = minecraft.player.getInventory();
        var equipped=YujianSlot.activeFlyingSword(minecraft.player);
        now.append("yujian:").append(equipped.getItem()).append(':').append(equipped.getCount()).append(':').append(equipped.getTag());
        // 包括副手与护甲，和选剑扫描使用同一范围，避免副手换剑后留下旧按钮。
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            var stack = inventory.getItem(i);
            now.append(i).append(':').append(stack.getItem()).append(':').append(stack.getCount()).append(':').append(stack.getTag()).append(';');
        }
        if (!now.toString().equals(snapshot)) { snapshot = now.toString(); rebuild(); }
    }
    private int textHeight(String text) { return font.split(Component.literal(text), detailW - 4).size() * 12 + 5; }
    private int line(GuiGraphics g, String text, int y, int color) {
        for (var row : font.split(Component.literal(text), detailW - 4)) { g.drawString(font, row, detailX, y, color, false); y += 12; }
        return y + 5;
    }
    @Override public void render(GuiGraphics g, int mx, int my, float delta) {
        int panelHeight = Math.max(host.contentViewportHeight(), height);
        CultivationUi.panel(g, -2, 0, width, panelHeight, false);
        if (detailX > 0) {
            CultivationBorderAssets.draw(g, "rule_gold_vertical", detailX - 8, 0, 4, panelHeight);
            g.drawString(font, "随身灵剑", 2, 6, CultivationUi.GOLD, false);
        } else if (!swordButtons.isEmpty()) {
            CultivationUi.rule(g, 0, detailsY - 7, width - 8);
        }
        line(g, "灵剑 / 养剑", 5, CultivationUi.GOLD);
        var n = nurture(); var p = profile();
        if (n == null || p == null) {
            line(g, "暂无灵剑", 84, CultivationUi.MUTED);
            super.render(g, mx, my, delta); return;
        }
        int y = line(g, sword().getHoverName().getString() + " · 品阶：" + p.swordTier(), detailsY, p.tier() >= 4 ? CultivationUi.GOLD : CultivationUi.TEXT);
        y = line(g, "剑蕴阶段：" + n.nurtureStage + " / " + p.maxNurtureStage() + "    洞察点：" + n.insightPoints, y, CultivationUi.CYAN);
        CultivationUi.bar(g, "剑蕴进度", detailX, y, detailW, n.nurtureExp, n.requirement(p), CultivationUi.GOLD, mx, my);
        line(g, "御剑资格：" + (FlyingSwordRegistry.canFly(sword()) ? "可御剑" : "不可御剑") + "    剑技槽：" + n.equippedTechniques.size() + " / " + p.maxTechniqueSlots(), y + 28, CultivationUi.TEXT);
        line(g, "剑技学习与装备", techniquesY, CultivationUi.GOLD);
        super.render(g, mx, my, delta);
        if(breakthrough!=null&&breakthrough.isMouseOver(mx,my)) {
            var cultivation=GeneratedMod.data(minecraft.player);
            if(cultivation!=null&&cultivation.wuxingxiantu$getAura()<SwordNurture.breakthroughCost())
                CultivationUi.tooltip(Component.translatable("message.wuxingxiantu.sword_nurture_mana_missing",SwordNurture.breakthroughCost()));
        }
        int index = 0;
        for (var technique : SwordTechniqueRegistry.all()) {
            var button = techniqueButtons.get(index++);
            if (mx >= button.getX() && mx < button.getX() + button.getWidth() && my >= button.getY() && my < button.getY() + button.getHeight())
                CultivationUi.tooltip(Component.literal(technique.name() + "\n" + technique.description()
                        + "\n灵力：" + ArtCatalog.manaCostDescription(technique.id(),GeneratedMod.data(minecraft.player)==null?0:GeneratedMod.data(minecraft.player).wuxingxiantu$getMaxMana()).getString() + " · 冷却：" + ArtCatalog.cooldownText(technique.id(),
                                GeneratedMod.data(minecraft.player) == null ? 0 : GeneratedMod.data(minecraft.player).wuxingxiantu$getArtMastery(technique.id()))
                        + "\n需求品阶：" + technique.requiredTier() + " · 剑蕴：" + technique.requiredStage()
                        + "\n洞察消耗：" + technique.insightCost()));
        }
    }
}
