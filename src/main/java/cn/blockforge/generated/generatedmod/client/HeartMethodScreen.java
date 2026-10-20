package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.CultivationData;
import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.HeartMethodCatalog;
import cn.blockforge.generated.generatedmod.Network;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** 心法切换面板：选择后由服务端校验并立即同步。 */
public final class HeartMethodScreen extends Screen {
    private static final int PANEL_COLOR = 0xF0181A20;
    private static final int PANEL_INNER = 0xB82B2525;
    private static final int GOLD = 0xFFE8C477;
    private static final int TEXT = 0xFFF4E8D0;
    private static final int MUTED = 0xFFC8B99C;
    private static final int CURRENT = 0xFF8EE7E8;
    private static final int[] METHOD_COLORS = {
            0xFFE5C76B, 0xFF79C58B, 0xFF659BD0, 0xFFE87963, 0xFFC28B65,
            0xFFE28A5B, 0xFFB78AE8, 0xFF8CC8D8, 0xFFF0D27A
    };

    private final Screen parent;
    private final Button[] methodButtons = new Button[HeartMethodCatalog.METHOD_IDS.size()];

    public HeartMethodScreen(Screen parent) {
        super(Component.translatable("gui.wuxingxiantu.heart_method.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int left = panelLeft();
        int top = panelTop();
        int columns = columns();
        int gap = 6;
        int contentWidth = panelWidth() - 24;
        int buttonWidth = Math.max(80, (contentWidth - gap * (columns - 1)) / columns);
        int gridTop = top + 62;
        for (int i = 0; i < HeartMethodCatalog.METHOD_IDS.size(); i++) {
            int index = i;
            int column = i % columns;
            int row = i / columns;
            String methodId = HeartMethodCatalog.METHOD_IDS.get(i);
            methodButtons[i] = Button.builder(methodLabel(methodId), button -> select(methodId))
                    .bounds(left + 12 + column * (buttonWidth + gap), gridTop + row * 28,
                            buttonWidth, 20).build();
            addRenderableWidget(methodButtons[i]);
        }
        int backWidth = Math.min(150, panelWidth() - 24);
        addRenderableWidget(Button.builder(Component.translatable("gui.wuxingxiantu.heart_method.back"), button -> goBack())
                .bounds(left + (panelWidth() - backWidth) / 2, top + panelHeight() - 28, backWidth, 20).build());
    }

    private void select(String methodId) {
        CultivationData data = GeneratedMod.data(minecraft.player);
        if (data == null || !HeartMethodCatalog.isValid(methodId)
                || methodId.equals(data.wuxingxiantu$getEquippedHeartMethod())) return;
        Network.CHANNEL.sendToServer(new cn.blockforge.generated.generatedmod.network.packet.cultivation.SelectHeartMethodPacket(methodId));
    }

    private void goBack() {
        minecraft.setScreen(parent);
    }

    private Component methodLabel(String methodId) {
        CultivationData data = GeneratedMod.data(minecraft.player);
        boolean current = data != null && methodId.equals(data.wuxingxiantu$getEquippedHeartMethod());
        boolean learned = data != null && data.wuxingxiantu$hasLearnedHeartMethod(methodId);
        String key = current ? "gui.wuxingxiantu.heart_method.current"
                : learned ? "gui.wuxingxiantu.heart_method.select"
                : "gui.wuxingxiantu.heart_method.not_learned";
        return Component.translatable(key, HeartMethodCatalog.shortName(methodId));
    }

    private void updateButtons() {
        CultivationData data = GeneratedMod.data(minecraft.player);
        String equipped = data == null ? "" : data.wuxingxiantu$getEquippedHeartMethod();
        for (int i = 0; i < methodButtons.length; i++) {
            String methodId = HeartMethodCatalog.METHOD_IDS.get(i);
            methodButtons[i].setMessage(methodLabel(methodId));
            methodButtons[i].active = data != null && data.wuxingxiantu$hasLearnedHeartMethod(methodId)
                    && !methodId.equals(equipped);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderBackground(graphics);
        int left = panelLeft();
        int top = panelTop();
        int panelWidth = panelWidth();
        int panelHeight = panelHeight();
        graphics.fill(0, 0, width, height, 0x4A100D0C);
        graphics.fill(left - 3, top - 3, left + panelWidth + 3, top + panelHeight + 3, 0xE8171A20);
        graphics.fill(left, top, left + panelWidth, top + panelHeight, PANEL_COLOR);
        graphics.renderOutline(left, top, panelWidth, panelHeight, 0xFFD19B56);
        graphics.renderOutline(left + 3, top + 3, panelWidth - 6, panelHeight - 6, 0x886A3A2D);
        graphics.drawCenteredString(font, title, width / 2, top + 10, GOLD);
        graphics.drawCenteredString(font, Component.translatable("gui.wuxingxiantu.heart_method.hint"),
                width / 2, top + 28, MUTED);

        CultivationData data = GeneratedMod.data(minecraft.player);
        Component equipped = data == null
                ? Component.translatable("gui.wuxingxiantu.heart_method.none")
                : HeartMethodCatalog.displayName(data.wuxingxiantu$getEquippedHeartMethod());
        Component currentLine = Component.translatable("gui.wuxingxiantu.heart_method.equipped", equipped);
        graphics.drawCenteredString(font, currentLine, width / 2, top + 44, CURRENT);

        int columns = columns();
        int gap = 6;
        int contentWidth = panelWidth - 24;
        int buttonWidth = Math.max(80, (contentWidth - gap * (columns - 1)) / columns);
        int gridTop = top + 62;
        for (int i = 0; i < HeartMethodCatalog.METHOD_IDS.size(); i++) {
            int column = i % columns;
            int row = i / columns;
            int x = left + 12 + column * (buttonWidth + gap);
            int y = gridTop + row * 28;
            graphics.fill(x, y, x + buttonWidth, y + 2, METHOD_COLORS[i % METHOD_COLORS.length]);
            graphics.fill(x, y + 19, x + buttonWidth, y + 20, PANEL_INNER);
        }
        updateButtons();
        super.render(graphics, mouseX, mouseY, delta);
    }

    private int columns() {
        return panelWidth() < 500 ? 2 : 3;
    }

    private int panelWidth() {
        return Math.min(600, Math.max(280, width - 12));
    }

    private int panelHeight() {
        return Math.min(420, Math.max(250, height - 12));
    }

    private int panelLeft() {
        return (width - panelWidth()) / 2;
    }

    private int panelTop() {
        return Math.max(6, (height - panelHeight()) / 2);
    }

    @Override
    public void onClose() {
        goBack();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
