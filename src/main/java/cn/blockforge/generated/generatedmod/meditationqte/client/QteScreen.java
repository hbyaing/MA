package cn.blockforge.generated.generatedmod.meditationqte.client;

import cn.blockforge.generated.generatedmod.MeditationSeatEntity;
import cn.blockforge.generated.generatedmod.Network;
import cn.blockforge.generated.generatedmod.client.CultivationUi;
import cn.blockforge.generated.generatedmod.client.InkButton;
import cn.blockforge.generated.generatedmod.meditationqte.*;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/** Standalone source cursor game, using target controls; it does not replace the delivery main UI. */
public final class QteScreen extends Screen {
    private QteStatePacket state;
    private long receivedMillis, closeAt;
    private boolean waiting, finished, cancelSent;
    private double lockedPosition;
    private InkButton lock;
    public QteScreen(QteStatePacket state) {
        super(Component.translatable("screen.wuxingxiantu.qte.title"));
        accept(state);
    }
    public void accept(QteStatePacket next) {
        if (finished) return;
        // Every successful round rotates the server nonce; late previous-round answers cannot act on the new round.
        if (state != null && next.result() >= QteStatePacket.FAIL && !next.nonce().equals(state.nonce())) return;
        state = next; receivedMillis = Util.getMillis(); waiting = false;
        finished = next.result() >= QteStatePacket.FAIL;
        if (finished) closeAt = receivedMillis + 1800;
    }
    @Override protected void init() {
        int panelWidth = Math.min(320, width - 12), x = (width - panelWidth) / 2, y = (height - 174) / 2;
        lock = addRenderableWidget(new InkButton(x + 14, y + 139, (panelWidth - 36) / 2, 22,
                Component.translatable("screen.wuxingxiantu.qte.lock"), button -> lockPointer()));
        addRenderableWidget(new InkButton(x + panelWidth / 2 + 4, y + 139, (panelWidth - 36) / 2, 22,
                Component.translatable("screen.wuxingxiantu.qte.cancel"), button -> onClose()));
    }
    private long serverNow() {
        int arrivalDelay = 0;
        if (minecraft != null && minecraft.player != null && minecraft.getConnection() != null) {
            var info = minecraft.getConnection().getPlayerInfo(minecraft.player.getUUID());
            if (info != null) arrivalDelay = Math.max(0, Math.min(500, info.getLatency()));
        }
        // One outbound transit for this snapshot plus one return transit for a lock packet.
        // Draw the server-receipt phase rather than a local pointer that always loses one-way latency.
        return state.serverMillis() + (Util.getMillis() - receivedMillis) + arrivalDelay;
    }
    private double position() { return QteRules.position(Math.max(0, serverNow() - state.startMillis()), state.level()); }
    @Override public void tick() {
        if (minecraft.player == null || !minecraft.player.isAlive()
                || !(minecraft.player.getVehicle() instanceof MeditationSeatEntity)) { onClose(); return; }
        if (finished && Util.getMillis() >= closeAt) { onClose(); return; }
        if (lock != null) lock.active = !waiting && !finished && serverNow() >= state.startMillis();
    }
    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        int panelWidth = Math.min(320, width - 12), x = (width - panelWidth) / 2, y = (height - 174) / 2;
        CultivationUi.panel(graphics, x, y, panelWidth, 174, true);
        graphics.drawCenteredString(font, title, width / 2, y + 12, CultivationUi.GOLD);
        graphics.drawCenteredString(font, Component.translatable("screen.wuxingxiantu.qte.round", state.level(), QteRules.LEVELS),
                width / 2, y + 32, CultivationUi.TEXT);
        int bx = x + 18, by = y + 60, bw = panelWidth - 36, bh = 20;
        graphics.fill(bx, by, bx + bw, by + bh, 0xFF4A4A52);
        double greenStart = 50 - QteRules.greenHalf(state.level()), greenEnd = 50 + QteRules.greenHalf(state.level());
        range(graphics, bx, by, bw, greenStart - QteRules.redHalf(state.level()), greenStart, 0xFFB03030);
        range(graphics, bx, by, bw, greenEnd, greenEnd + QteRules.redHalf(state.level()), 0xFFB03030);
        range(graphics, bx, by, bw, greenStart, greenEnd, 0xFF33C060);
        graphics.renderOutline(bx - 1, by - 1, bw + 2, bh + 2, CultivationUi.GOLD);
        double value = waiting || finished ? lockedPosition : position();
        int pointer = bx + (int) Math.round(bw * value / 100);
        graphics.fill(pointer - 1, by - 6, pointer + 1, by + bh + 6, waiting ? 0xFFFFE24A : 0xFFFFFFFF);
        Component result = finished ? Component.translatable("screen.wuxingxiantu.qte.result." + state.result(), state.reward())
                : Component.translatable(serverNow() < state.startMillis() ? "screen.wuxingxiantu.qte.ready"
                    : waiting ? "screen.wuxingxiantu.qte.waiting" : "screen.wuxingxiantu.qte.hint");
        var lines = font.split(result, panelWidth - 28);
        int lineY = y + 95;
        for (var line : lines) { graphics.drawCenteredString(font, line, width / 2, lineY, CultivationUi.TEXT); lineY += 11; }
        super.render(graphics, mouseX, mouseY, partialTick);
    }
    private static void range(GuiGraphics graphics, int x, int y, int width, double from, double to, int color) {
        graphics.fill(x + (int) Math.round(width * from / 100), y,
                x + (int) Math.round(width * to / 100), y + 20, color);
    }
    private void lockPointer() {
        if (waiting || finished || serverNow() < state.startMillis()) return;
        lockedPosition = position(); waiting = true;
        Network.CHANNEL.sendToServer(new QteActionPacket(QteActionPacket.HIT, state.nonce(), state.level()));
    }
    @Override public boolean keyPressed(int key, int scan, int modifiers) {
        if (key == GLFW.GLFW_KEY_SPACE) { lockPointer(); return true; }
        return super.keyPressed(key, scan, modifiers);
    }
    private void cancelSession() {
        if (!finished && !cancelSent && minecraft != null && minecraft.getConnection() != null) {
            cancelSent = true;
            Network.CHANNEL.sendToServer(new QteActionPacket(QteActionPacket.CANCEL, state.nonce(), state.level()));
        }
    }
    @Override public void onClose() { cancelSession(); super.onClose(); }
    @Override public void removed() { cancelSession(); super.removed(); }
    @Override public boolean isPauseScreen() { return false; }
}
