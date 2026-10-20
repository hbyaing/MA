package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.Network;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

/** 寻真司南界面：输入生物名称，向服务端查询距离最近的生物，并可直接传送到它附近。
 *  关闭界面后仍保留上次的搜索内容与结果（存活于同一局游戏内）。 */
public final class CreatureCompassScreen extends Screen {
    private static final int C_MAIN = 0xE8171A20;
    private static final int C_BORDER = 0xFFD19B56;
    private static final int C_INPUT = 0xFF241E20;
    private static final int C_INPUT_BORDER = 0xFF6A5A43;
    private static final int C_TEXT = 0xFFF4E8D0;
    private static final int C_MUTED = 0xFFC8B99C;
    private static final int C_OK = 0xFF8EE7E8;
    private static final int C_BAD = 0xFFDE837A;
    private static final int C_GOLD = 0xFFE8C477;

    // —— 跨界面保留的状态：上次搜索词与结果记录 ——
    private static String savedQuery = "";
    private static boolean savedSearched;
    private static String savedResultName = "";
    private static int savedDist = -1;
    private static int savedDx;
    private static int savedDy;
    private static int savedDz;

    private EditBox input;
    private InkButton teleportButton;
    private String resName = "";
    private int resDist = -1;
    private int resDx;
    private int resDy;
    private int resDz;

    public CreatureCompassScreen() {
        super(Component.translatable("screen.wuxingxiantu.creature_compass"));
    }

    private int panelWidth() { return Math.min(300, Math.max(240, width - 16)); }
    private int panelHeight() { return Math.min(height - 16, 186); }
    private int panelLeft() { return (width - panelWidth()) / 2; }
    private int panelTop() { return Math.max(8, (height - panelHeight()) / 2); }

    @Override
    protected void init() {
        super.init();
        // 恢复上次的搜索内容与结果。
        resName = savedResultName;
        resDist = savedDist;
        resDx = savedDx;
        resDy = savedDy;
        resDz = savedDz;

        int left = panelLeft();
        int top = panelTop();
        int contentX = left + 14;
        int contentW = panelWidth() - 28;
        input = new BottomAlignedEditBox(font, contentX, top + 34, contentW, 18, 8,
                Component.translatable("gui.wuxingxiantu.compass_input"));
        input.setMaxLength(64);
        input.setBordered(false);
        input.setValue(savedQuery);
        addRenderableWidget(input);
        addFittedButton(contentX, contentW, top + 56,
                Component.translatable("gui.wuxingxiantu.compass_search"), b -> search(), C_GOLD);
        teleportButton = addFittedButton(contentX, contentW, top + 132,
                Component.translatable("gui.wuxingxiantu.compass_teleport"), b -> teleport(), C_OK);
        addFittedButton(contentX, contentW, top + panelHeight() - 24,
                Component.translatable("gui.wuxingxiantu.compass_close"), b -> onClose(), C_GOLD);
        setInitialFocus(input);
    }

    /** 按键只占文字所需的宽度，并在面板内容区水平居中，短按键不再被拉成整条。 */
    private InkButton addFittedButton(int contentX, int contentW, int y, Component label,
                                      InkButton.OnPress onPress, int accent) {
        int width = Math.min(contentW, Math.max(58, font.width(label) + 22));
        int x = contentX + (contentW - width) / 2;
        return addRenderableWidget(new InkButton(x, y, width, 18, label, onPress, accent));
    }

    private void search() {
        String name = input.getValue().trim();
        if (name.isEmpty()) return;
        savedQuery = name;
        savedSearched = true;
        savedDist = -1;
        savedResultName = "";
        resName = "";
        resDist = -1;
        Network.CHANNEL.sendToServer(new cn.blockforge.generated.generatedmod.network.packet.cultivation.CompassSearchPacket(name));
    }

    private void teleport() {
        if (resDist < 0) return;
        Network.CHANNEL.sendToServer(new cn.blockforge.generated.generatedmod.network.packet.cultivation.CompassTeleportPacket());
        onClose();
    }

    /** 收到服务端结果后刷新文本，并写入跨界面保留的记录。 */
    public void receiveResult(String name, double distance, int dx, int dy, int dz) {
        this.resName = name == null ? "" : name;
        this.resDist = (int) Math.round(distance);
        this.resDx = dx;
        this.resDy = dy;
        this.resDz = dz;
        savedResultName = this.resName;
        savedDist = this.resDist;
        savedDx = dx;
        savedDy = dy;
        savedDz = dz;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        int left = panelLeft();
        int top = panelTop();
        int pw = panelWidth();
        int ph = panelHeight();
        graphics.fill(left, top, left + pw, top + ph, C_MAIN);
        graphics.renderOutline(left, top, pw, ph, C_BORDER);
        graphics.renderOutline(left + 2, top + 2, pw - 4, ph - 4, 0x886A3A2D);
        graphics.drawCenteredString(font, title, left + pw / 2, top + 8, C_GOLD);
        graphics.drawString(font, Component.translatable("gui.wuxingxiantu.compass_input"),
                left + 14, top + 24, C_TEXT, false);
        // 自绘输入框底，替掉原版输入框描边，跟水墨面板统一。
        graphics.fill(left + 14, top + 34, left + pw - 14, top + 52, C_INPUT);
        graphics.renderOutline(left + 14, top + 34, pw - 28, 18, C_INPUT_BORDER);
        CultivationUi.softDivider(graphics,left+14,top+78,pw-28,false,0x667F5A42);
        graphics.drawString(font, Component.translatable("gui.wuxingxiantu.compass_result_header"),
                left + 14, top + 82, C_MUTED, false);
        teleportButton.active = resDist >= 0;
        super.render(graphics, mouseX, mouseY, partialTick);
        drawResult(graphics, left + 14, top + 94, pw - 28);
    }

    /** 结果文字按面板宽度自动换行并居中，避免越出 UI 框。 */
    private void drawResult(GuiGraphics graphics, int x, int y, int w) {
        if (resDist >= 0) {
            Component line = Component.translatable("message.wuxingxiantu.compass_found",
                    resName, resDist, resDx, resDy, resDz);
            List<FormattedCharSequence> lines = font.split(line, w);
            int lineY = y;
            for (int i = 0; i < lines.size() && i < 3; i++) {
                graphics.drawCenteredString(font, lines.get(i), x + w / 2, lineY, C_OK);
                lineY += 11;
            }
            return;
        }
        Component hint = savedSearched
                ? Component.translatable("gui.wuxingxiantu.compass_none")
                : Component.translatable("gui.wuxingxiantu.compass_idle");
        for (FormattedCharSequence seq : font.split(hint, w)) {
            graphics.drawCenteredString(font, seq, x + w / 2, y, savedSearched ? C_BAD : C_MUTED);
            y += 11;
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 257 && input != null && input.isFocused()) {
            search();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    /**
     * 1.20.1 的无边框输入框会把文字直接画在控件顶端（{@code bordered=false} 时不再垂直居中），
     * 导致搜索文字浮在输入框上沿。这里只在渲染时把控件临时下移一段，
     * 让光标与文字和自绘输入框的下缘贴齐，点击区域仍覆盖整个输入框。
     */
    private static final class BottomAlignedEditBox extends EditBox {
        private final int textDrop;

        BottomAlignedEditBox(Font font, int x, int y, int width, int height, int textDrop,
                             Component message) {
            super(font, x, y, width, height, message);
            this.textDrop = textDrop;
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            int base = getY();
            setY(base + textDrop);
            super.renderWidget(graphics, mouseX, mouseY, partialTick);
            setY(base);
        }
    }
}
