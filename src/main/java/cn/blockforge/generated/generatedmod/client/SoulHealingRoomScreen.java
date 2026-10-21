package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.SoulHealingRoomMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** 愈魂室界面：显示破碎魂珠、能量燃料与修复进度。 */
public final class SoulHealingRoomScreen extends AbstractContainerScreen<SoulHealingRoomMenu> {
    private static final int WIDTH = 220;
    private static final int HEIGHT = 195;

    public SoulHealingRoomScreen(SoulHealingRoomMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = WIDTH;
        imageHeight = HEIGHT;
        titleLabelX = 10;
        titleLabelY = 8;
        inventoryLabelX = 20;
        inventoryLabelY = 103;
    }

    @Override protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int left = leftPos;
        int top = topPos;
        graphics.fill(left, top, left + WIDTH, top + HEIGHT, 0xF018202A);
        graphics.renderOutline(left, top, WIDTH, HEIGHT, 0xFF70C7D2);
        graphics.renderOutline(left + 4, top + 4, WIDTH - 8, HEIGHT - 8, 0xFF385B6B);
        // 参考图中的输入槽、进度条和燃料槽整体向右上方偏移。
        drawSlot(graphics, left + 68, top + 34);
        drawSlot(graphics, left + 68, top + 69);
        graphics.fill(left + 108, top + 39, left + 194, top + 55, 0xFF10151D);
        graphics.renderOutline(left + 108, top + 39, 86, 16, 0xFF527B8C);
        int filled = Math.round(80 * menu.progressScale());
        if (filled > 0) graphics.fill(left + 111, top + 42, left + 111 + filled, top + 52, 0xFF9C6FE8);
        graphics.drawCenteredString(font, Component.translatable("gui.wuxingxiantu.soul_healing_room.pearl"), left + 76, top + 22, 0xFFDAF4FF);
        graphics.drawCenteredString(font, Component.translatable("gui.wuxingxiantu.soul_healing_room.fuel"), left + 76, top + 89, 0xFFDAF4FF);
        graphics.drawString(font, Component.translatable("gui.wuxingxiantu.soul_healing_room.progress", menu.progress()), left + 110, top + 62, 0xFFD6B7FF, false);
        graphics.drawString(font, Component.translatable("gui.wuxingxiantu.soul_healing_room.power",
                menu.soulPower(), cn.blockforge.generated.generatedmod.SoulPearlData.MAX_SOUL_ENERGY),
                left + 110, top + 76, 0xFFFFD98A, false);
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++) {
            drawSlot(graphics, left + 20 + col * 18, top + 115 + row * 18);
        }
        for (int col = 0; col < 9; col++) drawSlot(graphics, left + 20 + col * 18, top + 173);
    }

    private static void drawSlot(GuiGraphics graphics, int x, int y) {
        graphics.fill(x - 1, y - 1, x + 17, y + 17, 0xFF131923);
        graphics.renderOutline(x - 1, y - 1, 18, 18, 0xFF527B8C);
    }

    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, 0xFFFFD98A, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0xFFDAF4FF, false);
    }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override public boolean isPauseScreen() { return false; }
}
