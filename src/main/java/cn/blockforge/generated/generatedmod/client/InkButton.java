package cn.blockforge.generated.generatedmod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

/**
 * 五行仙途统一的自绘水墨按钮：不使用原版按键贴图，而是深墨底 + 描边，
 * 悬停提亮、禁用变暗，文字在按钮框内水平居中，过长时自动等比缩小，保证在窄栏里也不溢出。
 * 内视、功法轮盘、七十二变、寻真司南等界面共用，观感与面板一致。
 * 不再绘制左侧竖杠——需要色条的场合（如内视的领域开关）由各自的专用按钮自行绘制。
 */
public class InkButton extends Button {
    private static final int BG = 0xFF302629;
    private static final int BG_HOVER = 0xFF4B3B32;
    private static final int BG_DISABLED = 0xFF241E20;
    private static final int BORDER = 0xFF765D50;
    private static final int TEXT = 0xFFF4E8D0;
    private static final int MUTED = 0xFFB5A79A;
    private static final int DEFAULT_ACCENT = 0xFFE8C477;
    /** 文字两侧各留的边距，保证居中后不贴边。 */
    private static final int PADDING = 4;

    private final int accent;

    public InkButton(int x, int y, int width, int height, Component message, OnPress onPress) {
        this(x, y, width, height, message, onPress, DEFAULT_ACCENT);
    }

    public InkButton(int x, int y, int width, int height, Component message, OnPress onPress, int accent) {
        super(Button.builder(message, onPress).bounds(x, y, width, height));
        this.accent = accent;
    }

    /** 列表项与紧凑按钮的语义别名，样式与普通按钮一致（文字居中，无左侧色条）。 */
    public static InkButton flat(int x, int y, int width, int height, Component message, OnPress onPress, int accent) {
        return new InkButton(x, y, width, height, message, onPress, accent);
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        int border = active ? accent : BORDER;
        int background = !active ? BG_DISABLED : isHoveredOrFocused() ? BG_HOVER : BG;
        graphics.fill(getX(), getY(), getX() + width, getY() + height, background);
        graphics.renderOutline(getX(), getY(), width, height, border);

        Font font = Minecraft.getInstance().font;
        Component message = getMessage();
        int available = Math.max(8, width - PADDING * 2);
        int textWidth = font.width(message);
        float scale = textWidth > available && textWidth > 0
                ? Math.max(0.5F, available / (float) textWidth) : 1.0F;
        var pose = graphics.pose();
        pose.pushPose();
        // 水平居中：按缩放后的实际宽度摆正起点。
        float scaledWidth = textWidth * scale;
        pose.translate(getX() + (width - scaledWidth) / 2.0F,
                getY() + (height - font.lineHeight * scale) / 2.0F, 0.0F);
        pose.scale(scale, scale, 1.0F);
        graphics.drawString(font, message, 0, 0, active ? TEXT : MUTED, false);
        pose.popPose();
    }
}
