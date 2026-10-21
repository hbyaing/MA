package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.ArtCatalog;
import cn.blockforge.generated.generatedmod.CultivationData;
import cn.blockforge.generated.generatedmod.DivineArtCatalog;
import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.Network;
import cn.blockforge.generated.generatedmod.items.CultivationLogic;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/** 功法轮盘设置：普通功法使用八个槽位，神通使用独立槽位。 */
public final class ArtWheelScreen extends Screen {
    private static final int PANEL = 0xE8171A20;
    private static final int ALT = 0xD02B2525;
    private static final int GOLD = 0xFFE8C477;
    private static final int TEXT = 0xFFF4E8D0;
    private static final int MUTED = 0xFFC8B99C;
    private static final int RED = 0xFFDE837A;
    private static final int PINK = 0xFFFF69C8;
    private final Screen parent;
    private final Button[] slotButtons = new Button[8];
    private final Button[] divineSlotButtons = new Button[3];
    private int selectedSlot = -1;
    private int listScroll;
    private String[] previewArts;
    private String[] previewDivineArts;
    private String detailArt;
    private String detailDivineArt;

    public ArtWheelScreen(Screen parent) {
        super(Component.translatable("gui.wuxingxiantu.art_wheel.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        CultivationData data = GeneratedMod.data(minecraft.player);
        if (previewArts == null) previewArts = data == null ? new String[8] : data.wuxingxiantu$getEquippedArts().clone();
        if (previewDivineArts == null) previewDivineArts = data == null ? new String[3] : data.wuxingxiantu$getEquippedDivineArts();
        buildWidgets();
    }

    private boolean listOpen() {
        return selectedSlot >= 0;
    }

    private int panelTop(boolean listOpen) {
        int contentHeight = listOpen ? 184 + visibleRows() * 20 : 178;
        return Math.max(8, (height - contentHeight) / 2);
    }

    private int panelBottom(int top, boolean listOpen) {
        return top + (listOpen ? 184 + visibleRows() * 20 : 178);
    }

    private void buildWidgets() {
        CultivationData data = GeneratedMod.data(minecraft.player);
        int unlocked = data == null ? 0 : CultivationLogic.unlockedArtSlots(data.wuxingxiantu$getRealm());
        boolean open = listOpen();
        int panelWidth = Math.min(600, Math.max(320, width - 12));
        int left = (width - panelWidth) / 2;
        int top = panelTop(open);
        int gap = 6;
        int slotWidth = (panelWidth - 28 - gap * 3) / 4;
        for (int slot = 0; slot < 8; slot++) {
            int col = slot % 4;
            int row = slot / 4;
            int currentSlot = slot;
            slotButtons[slot] = addRenderableWidget(new InkButton(
                    left + 14 + col * (slotWidth + gap), top + 44 + row * 28, slotWidth, 22,
                    slotLabel(data, slot, unlocked), button -> selectSlot(currentSlot), GOLD));
        }
        int divineWidth = (panelWidth - 28 - gap * 2) / 3;
        for (int divineSlot = 0; divineSlot < 3; divineSlot++) {
            int currentSlot = 8 + divineSlot;
            divineSlotButtons[divineSlot] = addRenderableWidget(new InkButton(
                    left + 14 + divineSlot * (divineWidth + gap), top + 104, divineWidth, 22,
                    divineSlotLabel(divineSlot), button -> selectSlot(currentSlot), PINK));
        }

        if (open) {
            int rows = visibleRows();
            int popupTop = top + 132;
            int popupBottom = popupTop + 28 + rows * 20;
            int listX = left + 14;
            int listWidth = Math.max(126, (panelWidth - 42) * 5 / 11);
            if (selectedSlot >= 8) {
                String[] learned = learnedDivine(data);
                int maxScroll = Math.max(0, learned.length - rows);
                listScroll = Math.max(0, Math.min(listScroll, maxScroll));
                for (int row = 0; row < rows && row + listScroll < learned.length; row++) {
                    String divine = learned[row + listScroll];
                    addRenderableWidget(InkButton.flat(listX, popupTop + 24 + row * 20, listWidth, 18,
                            divineLabel(divine), button -> equipDivine(divine), PINK));
                }
            } else {
                String[] learned = ArtCatalog.sortLearned(data == null ? new String[0] : data.wuxingxiantu$getLearnedArts());
                int maxScroll = Math.max(0, learned.length - rows);
                listScroll = Math.max(0, Math.min(listScroll, maxScroll));
                for (int row = 0; row < rows && row + listScroll < learned.length; row++) {
                    String art = learned[row + listScroll];
                    addRenderableWidget(InkButton.flat(listX, popupTop + 24 + row * 20, listWidth, 18,
                            learnedLabel(data, art), button -> equipArt(art), ArtCatalog.tierColor(ArtCatalog.tier(art))));
                }
            }
            int actionGap = 6;
            int actionWidth = (panelWidth - 28 - actionGap * 3) / 4;
            int actionRow = popupBottom + 4;
            addRenderableWidget(new InkButton(left + 14, actionRow, actionWidth, 20,
                    Component.translatable("gui.wuxingxiantu.art_wheel.clear_current"), button -> clearSelected(), GOLD));
            addRenderableWidget(new InkButton(left + 14 + (actionWidth + actionGap), actionRow, actionWidth, 20,
                    Component.translatable("gui.wuxingxiantu.art_wheel.clear_all"), button -> clearAll(), GOLD));
            addRenderableWidget(new InkButton(left + 14 + (actionWidth + actionGap) * 2, actionRow, actionWidth, 20,
                    Component.translatable("gui.wuxingxiantu.art_wheel.close_list"), button -> closeList(), GOLD));
            addRenderableWidget(new InkButton(left + 14 + (actionWidth + actionGap) * 3, actionRow, actionWidth, 20,
                    Component.translatable("gui.wuxingxiantu.art_wheel.back"),
                    button -> minecraft.setScreen(parent), GOLD));
        } else {
            int actionY = panelBottom(top, false) - 28;
            int actionGap = 6;
            int half = (panelWidth - 28 - actionGap) / 2;
            addRenderableWidget(new InkButton(left + 14, actionY, half, 20,
                    Component.translatable("gui.wuxingxiantu.art_wheel.clear_all"), button -> clearAll(), GOLD));
            addRenderableWidget(new InkButton(left + 14 + half + actionGap, actionY, half, 20,
                    Component.translatable("gui.wuxingxiantu.art_wheel.back"),
                    button -> minecraft.setScreen(parent), GOLD));
        }
    }

    private int visibleRows() {
        return Math.max(3, Math.min(7, (height - 220) / 20));
    }

    private void selectSlot(int slot) {
        CultivationData data = GeneratedMod.data(minecraft.player);
        if (slot < 8 && (data == null || slot >= CultivationLogic.unlockedArtSlots(data.wuxingxiantu$getRealm()))) return;
        selectedSlot = selectedSlot == slot ? -1 : slot;
        detailArt = selectedSlot >= 0 && selectedSlot < 8 && previewArts != null ? previewArts[selectedSlot] : null;
        detailDivineArt = selectedSlot >= 8 && previewDivineArts != null ? previewDivineArts[selectedSlot - 8] : null;
        listScroll = 0;
        clearWidgets();
        buildWidgets();
    }

    private void closeList() {
        selectedSlot = -1;
        detailArt = null;
        detailDivineArt = null;
        clearWidgets();
        buildWidgets();
    }

    private void equipArt(String art) {
        if (selectedSlot < 0 || selectedSlot >= 8 || !ArtCatalog.isValid(art)) return;
        if (previewArts == null) previewArts = new String[8];
        previewArts[selectedSlot] = art;
        detailArt = art;
        Network.CHANNEL.sendToServer(new cn.blockforge.generated.generatedmod.network.packet.cultivation.EquipArtPacket(selectedSlot, art));
        clearWidgets();
        buildWidgets();
    }

    private void equipDivine(String divine) {
        int divineSlot = selectedSlot - 8;
        if (divineSlot < 0 || divineSlot >= 3 || !DivineArtCatalog.isValid(divine)) return;
        if (previewDivineArts == null) previewDivineArts = new String[3];
        previewDivineArts[divineSlot] = divine;
        detailDivineArt = divine;
        Network.CHANNEL.sendToServer(new cn.blockforge.generated.generatedmod.network.packet.divineart.EquipDivineArtPacket(divineSlot, divine));
        clearWidgets();
        buildWidgets();
    }

    private void clearSelected() {
        if (selectedSlot < 0) return;
        if (selectedSlot >= 8) {
            int divineSlot = selectedSlot - 8;
            if (previewDivineArts != null && divineSlot < previewDivineArts.length) previewDivineArts[divineSlot] = null;
            detailDivineArt = null;
            Network.CHANNEL.sendToServer(new cn.blockforge.generated.generatedmod.network.packet.divineart.EquipDivineArtPacket(divineSlot, null));
        } else {
            if (previewArts != null) previewArts[selectedSlot] = null;
            detailArt = null;
            Network.CHANNEL.sendToServer(new cn.blockforge.generated.generatedmod.network.packet.cultivation.EquipArtPacket(selectedSlot, null));
        }
        clearWidgets();
        buildWidgets();
    }

    private void clearAll() {
        CultivationData data = GeneratedMod.data(minecraft.player);
        if (data == null) return;
        int unlocked = CultivationLogic.unlockedArtSlots(data.wuxingxiantu$getRealm());
        if (previewArts == null) previewArts = data.wuxingxiantu$getEquippedArts().clone();
        for (int slot = 0; slot < unlocked; slot++) {
            previewArts[slot] = null;
            Network.CHANNEL.sendToServer(new cn.blockforge.generated.generatedmod.network.packet.cultivation.EquipArtPacket(slot, null));
        }
        if (previewDivineArts == null) previewDivineArts = new String[3];
        for (int slot = 0; slot < previewDivineArts.length; slot++) {
            previewDivineArts[slot] = null;
            Network.CHANNEL.sendToServer(new cn.blockforge.generated.generatedmod.network.packet.divineart.EquipDivineArtPacket(slot, null));
        }
        selectedSlot = -1;
        detailArt = null;
        detailDivineArt = null;
        clearWidgets();
        buildWidgets();
    }

    private Component slotLabel(CultivationData data, int slot, int unlocked) {
        if (slot >= unlocked) return Component.translatable("gui.wuxingxiantu.art_wheel.locked", slot + 1);
        String art = previewArts != null && slot < previewArts.length ? previewArts[slot] : null;
        if (art == null && data != null) {
            String[] arts = data.wuxingxiantu$getEquippedArts();
            art = slot < arts.length ? arts[slot] : null;
        }
        if (art == null) return Component.translatable("gui.wuxingxiantu.art_wheel.empty", slot + 1);
        return Component.translatable("gui.wuxingxiantu.art_wheel.slot", slot + 1, ArtCatalog.labeledName(art));
    }

    private Component divineSlotLabel(int slot) {
        String divine = previewDivineArts != null && slot < previewDivineArts.length ? previewDivineArts[slot] : null;
        if (divine == null || divine.isBlank()) {
            return Component.translatable("gui.wuxingxiantu.art_wheel.divine_empty", slot + 1).withStyle(style -> style.withColor(PINK));
        }
        return Component.translatable("gui.wuxingxiantu.art_wheel.divine_slot", slot + 1, DivineArtCatalog.labeledName(divine));
    }

    private String[] learnedDivine(CultivationData data) {
        return data == null ? new String[0] : data.wuxingxiantu$getLearnedDivineArts();
    }

    private Component learnedLabel(CultivationData data, String art) {
        int mastery = data == null ? 0 : data.wuxingxiantu$getArtMastery(art);
        Component label = ArtCatalog.labeledName(art);
        return Component.literal(label.getString() + "  熟练度 " + mastery + "/100")
                .withStyle(style -> style.withColor(ArtCatalog.tierColor(ArtCatalog.tier(art))));
    }

    private Component divineLabel(String divine) {
        return DivineArtCatalog.labeledName(divine);
    }

    private void drawArtDetail(GuiGraphics graphics, CultivationData data, int x, int y, int width) {
        if (detailArt == null || !ArtCatalog.isValid(detailArt)) {
            graphics.drawString(font, Component.translatable("gui.wuxingxiantu.art_wheel.detail_empty"), x, y, MUTED, false);
            return;
        }
        int mastery = data == null ? 0 : data.wuxingxiantu$getArtMastery(detailArt);
        graphics.drawString(font, ArtCatalog.labeledName(detailArt), x, y, ArtCatalog.tierColor(ArtCatalog.tier(detailArt)), false);
        graphics.drawString(font, Component.translatable("gui.wuxingxiantu.art_wheel.detail_stage", ArtCatalog.masteryName(mastery)), x, y + 14, MUTED, false);
        graphics.drawString(font, Component.translatable("gui.wuxingxiantu.art_wheel.detail_cost", ArtCatalog.manaCostDescription(detailArt, data==null?0:data.wuxingxiantu$getMaxMana())), x, y + 28, GOLD, false);
        graphics.drawString(font, Component.translatable("gui.wuxingxiantu.art_wheel.detail_cooldown", ArtCatalog.cooldownText(detailArt, mastery)), x, y + 42, GOLD, false);
        int lineY = y + 58;
        for (var line : font.split(ArtCatalog.function(detailArt), Math.max(80, width))) {
            graphics.drawString(font, line, x, lineY, TEXT, false);
            lineY += 10;
        }
        for (var line : font.split(Component.literal(ArtCatalog.currentEffect(detailArt, mastery)), Math.max(80, width))) {
            graphics.drawString(font, line, x, lineY, TEXT, false);
            lineY += 10;
        }
        for (var line : font.split(Component.literal(ArtCatalog.extraEffect(detailArt, mastery)), Math.max(80, width))) {
            graphics.drawString(font, line, x, lineY, MUTED, false);
            lineY += 10;
        }
    }

    private void drawDivineDetail(GuiGraphics graphics, int x, int y, int width) {
        if (detailDivineArt == null || !DivineArtCatalog.isValid(detailDivineArt)) {
            graphics.drawString(font, Component.translatable("gui.wuxingxiantu.art_wheel.detail_empty"), x, y, MUTED, false);
            return;
        }
        graphics.drawString(font, DivineArtCatalog.labeledName(detailDivineArt), x, y, PINK, false);
        graphics.drawString(font, Component.translatable("gui.wuxingxiantu.art_wheel.detail_divine_realm",
                DivineArtCatalog.requiredRealm(detailDivineArt) + 1), x, y + 14, MUTED, false);
        graphics.drawString(font, Component.translatable("gui.wuxingxiantu.art_wheel.detail_cost",
                DivineArtCatalog.costText(detailDivineArt)), x, y + 28, GOLD, false);
        graphics.drawString(font, Component.translatable("gui.wuxingxiantu.art_wheel.detail_cooldown",
                DivineArtCatalog.cooldownText(detailDivineArt)), x, y + 42, GOLD, false);
        graphics.drawString(font, Component.translatable("gui.wuxingxiantu.art_wheel.detail_divine_effect"), x, y + 58, PINK, false);
        int lineY = y + 70;
        for (var line : font.split(DivineArtCatalog.function(detailDivineArt), Math.max(80, width))) {
            graphics.drawString(font, line, x, lineY, TEXT, false);
            lineY += 10;
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderBackground(graphics);
        CultivationData data = GeneratedMod.data(minecraft.player);
        int unlocked = data == null ? 0 : CultivationLogic.unlockedArtSlots(data.wuxingxiantu$getRealm());
        int panelWidth = Math.min(600, Math.max(320, width - 12));
        int left = (width - panelWidth) / 2;
        boolean open = listOpen();
        int top = panelTop(open);
        int panelBottom = panelBottom(top, open);
        graphics.fill(left, top, left + panelWidth, panelBottom, PANEL);
        graphics.renderOutline(left, top, panelWidth, panelBottom - top, 0xFFD19B56);
        graphics.drawCenteredString(font, title, width / 2, top + 9, GOLD);
        graphics.drawCenteredString(font, Component.translatable("gui.wuxingxiantu.art_wheel.hint"), width / 2, top + 25, MUTED);
        graphics.drawString(font, Component.translatable("gui.wuxingxiantu.art_wheel.progress", unlocked, 8), left + 14, top + 35, MUTED, false);
        int gap = 6;
        int slotWidth = (panelWidth - 28 - gap * 3) / 4;
        for (int slot = 0; slot < 8; slot++) {
            int col = slot % 4;
            int row = slot / 4;
            int x = left + 14 + col * (slotWidth + gap);
            int y = top + 44 + row * 28;
            if (selectedSlot == slot) graphics.renderOutline(x - 1, y - 1, slotWidth + 2, 24, GOLD);
        }
        if (selectedSlot >= 8) {
            int divineSlot = selectedSlot - 8;
            int divineWidth = (panelWidth - 28 - gap * 2) / 3;
            graphics.renderOutline(left + 13 + divineSlot * (divineWidth + gap), top + 103, divineWidth + 2, 24, PINK);
        }
        if (open) {
            int rows = visibleRows();
            int popupTop = top + 132;
            int popupBottom = popupTop + 28 + rows * 20;
            int listX = left + 14;
            int listWidth = Math.max(126, (panelWidth - 42) * 5 / 11);
            int detailX = listX + listWidth + 10;
            int detailWidth = left + panelWidth - 14 - detailX;
            graphics.fill(left + 8, popupTop, left + panelWidth - 8, popupBottom, ALT);
            graphics.renderOutline(left + 8, popupTop, panelWidth - 16, popupBottom - popupTop, 0x997F5A42);
            if (selectedSlot >= 8) {
                graphics.drawString(font, Component.translatable("gui.wuxingxiantu.art_wheel.learned_divine_title"), listX, popupTop + 7, PINK, false);
                graphics.drawString(font, Component.translatable("gui.wuxingxiantu.art_wheel.divine_detail_title"), detailX, popupTop + 7, PINK, false);
                String[] learned = learnedDivine(data);
                if (learned.length == 0) {
                    graphics.drawString(font, Component.translatable("gui.wuxingxiantu.art_wheel.no_learned_divine"), listX, popupTop + 28, RED, false);
                } else {
                    if (learned.length > rows) graphics.drawString(font, Component.translatable("gui.wuxingxiantu.art_wheel.scroll_hint"), listX + listWidth - 74, popupTop + 7, MUTED, false);
                    for (int row = 0; row < rows && row + listScroll < learned.length; row++) {
                        int itemY = popupTop + 24 + row * 20;
                        if (mouseX >= listX && mouseX < listX + listWidth && mouseY >= itemY && mouseY < itemY + 18) detailDivineArt = learned[row + listScroll];
                    }
                    if (detailDivineArt == null || !DivineArtCatalog.isValid(detailDivineArt)) {
                        detailDivineArt = previewDivineArts == null ? null : previewDivineArts[selectedSlot - 8];
                    }
                }
                graphics.enableScissor(detailX, popupTop + 20, left + panelWidth - 8, popupBottom - 4);
                drawDivineDetail(graphics, detailX, popupTop + 24, detailWidth);
                graphics.disableScissor();
            } else {
                graphics.drawString(font, Component.translatable("gui.wuxingxiantu.art_wheel.learned_title", selectedSlot + 1), listX, popupTop + 7, GOLD, false);
                graphics.drawString(font, Component.translatable("gui.wuxingxiantu.art_wheel.detail_title"), detailX, popupTop + 7, GOLD, false);
                String[] learned = ArtCatalog.sortLearned(data == null ? new String[0] : data.wuxingxiantu$getLearnedArts());
                if (learned.length == 0) {
                    graphics.drawString(font, Component.translatable("gui.wuxingxiantu.art_wheel.no_learned"), listX, popupTop + 28, RED, false);
                } else {
                    if (learned.length > rows) graphics.drawString(font, Component.translatable("gui.wuxingxiantu.art_wheel.scroll_hint"), listX + listWidth - 74, popupTop + 7, MUTED, false);
                    for (int row = 0; row < rows && row + listScroll < learned.length; row++) {
                        int itemY = popupTop + 24 + row * 20;
                        if (mouseX >= listX && mouseX < listX + listWidth && mouseY >= itemY && mouseY < itemY + 18) detailArt = learned[row + listScroll];
                    }
                    if (detailArt == null || !ArtCatalog.isValid(detailArt)) detailArt = previewArts[selectedSlot];
                }
                graphics.enableScissor(detailX, popupTop + 20, left + panelWidth - 8, popupBottom - 4);
                drawArtDetail(graphics, data, detailX, popupTop + 24, detailWidth);
                graphics.disableScissor();
            }
        }
        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (!listOpen()) return super.mouseScrolled(mouseX, mouseY, delta);
        CultivationData data = GeneratedMod.data(minecraft.player);
        int count = selectedSlot >= 8 ? learnedDivine(data).length
                : data == null ? 0 : ArtCatalog.sortLearned(data.wuxingxiantu$getLearnedArts()).length;
        int maxScroll = Math.max(0, count - visibleRows());
        int old = listScroll;
        listScroll = Math.max(0, Math.min(maxScroll, listScroll - (int) Math.signum(delta)));
        if (old != listScroll) {
            clearWidgets();
            buildWidgets();
        }
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_R) return true;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
