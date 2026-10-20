package cn.blockforge.generated.generatedmod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import java.util.Locale;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeConfigSpec;

/** 各页面共用的像素边框、按钮状态、数值格式与进度条。 */
public final class CultivationUi {
    private static final java.util.Map<String, net.minecraft.resources.ResourceLocation> TEXTURES = new java.util.HashMap<>();
    private static final net.minecraft.resources.ResourceLocation NAVIGATION = new net.minecraft.resources.ResourceLocation("wuxingxiantu", "textures/gui/cultivation_navigation.png");
    private static final net.minecraft.resources.ResourceLocation SUMMARY = new net.minecraft.resources.ResourceLocation("wuxingxiantu", "textures/gui/cultivation_summary.png");
    public static final int BACK = 0xC4161816, PANEL = 0xB8242722, GOLD = 0xFFE2C582;
    public static final int TEXT = 0xFFE6E3D5, MUTED = 0xFFB6B8AB, CYAN = 0xFF87BDCB;
    public static final int GREEN = 0xFF96BEA2, RED = 0xFFCF887A, BORDER = 0xFF998053;
    public static final int INK = 0xFF222822, PAPER = 0xFFC4C3B5;
    private CultivationUi() {}
    public static void summaryIcon(GuiGraphics g, int index, int x, int y) {
        com.mojang.blaze3d.systems.RenderSystem.enableBlend();
        com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
        g.blit(SUMMARY, x, y, index * 16, 0, 16, 16, 96, 16);
        com.mojang.blaze3d.systems.RenderSystem.disableBlend();
    }
    public static class Action extends Button {
        protected final BooleanSupplier selected;
        private long pressedUntil;
        public Action(int x, int y, int w, int h, Component label, OnPress action) {
            this(x, y, w, h, label, action, () -> false);
        }
        public Action(int x, int y, int w, int h, Component label, OnPress action, BooleanSupplier selected) {
            super(Button.builder(label, action).bounds(x, y, w, h));
            this.selected = selected;
        }
        @Override public void onPress() { pressedUntil = net.minecraft.Util.getMillis() + 140; super.onPress(); }
        protected void surface(GuiGraphics g, boolean chosen) {
            boolean pressed = active && (net.minecraft.Util.getMillis() < pressedUntil || isHovered()
                    && org.lwjgl.glfw.GLFW.glfwGetMouseButton(Minecraft.getInstance().getWindow().getWindow(), 0) == 1);
            buttonSurface(g, getX(), getY(), width, height, active, chosen, isHoveredOrFocused(), pressed);
        }
        @Override protected void renderWidget(GuiGraphics g, int mx, int my, float delta) {
            boolean chosen = selected.getAsBoolean();
            surface(g, chosen);
            var font = Minecraft.getInstance().font;
            fit(g, getMessage(), getX() + Math.max(4, (width - font.width(getMessage())) / 2), getY() + (height - 8) / 2,
                    width - 8, !active ? MUTED : chosen ? GOLD : TEXT, mx, my);
            if (isHoveredOrFocused() && font.width(getMessage()) > width - 8 && Minecraft.getInstance().screen != null)
                Minecraft.getInstance().screen.setTooltipForNextRenderPass(font.split(getMessage(), 240));
        }
    }
    public static class Tab extends Action {
        public Tab(int x, int y, int w, int h, Component label, OnPress action, BooleanSupplier selected) {
            super(x, y, w, h, label, action, selected);
        }
        @Override protected void renderWidget(GuiGraphics g, int mx, int my, float delta) {
            boolean chosen = selected.getAsBoolean();
            surface(g, chosen);
            if (chosen) g.fill(getX() + 7, getY() + height - 3, getX() + width - 7, getY() + height - 2, GOLD);
            var font = Minecraft.getInstance().font;
            fit(g, getMessage(), getX() + Math.max(4, (width - font.width(getMessage())) / 2), getY() + (height - 8) / 2,
                    width - 8, !active ? MUTED : chosen ? GOLD : TEXT, mx, my);
        }
    }
    public static class Navigation extends Action {
        private final int icon;
        public Navigation(int x, int y, int w, Component label, String pageId, OnPress action, BooleanSupplier selected) {
            super(x, y, w, 24, label, action, selected);
            this.icon = switch (pageId) { case "inventory" -> 0; case "vision" -> 1; case "arts" -> 2;
                case "sword" -> 3; case "advancements" -> 4; default -> 5; };
        }
        @Override protected void renderWidget(GuiGraphics g, int mx, int my, float delta) {
            boolean chosen = selected.getAsBoolean();
            surface(g, chosen);
            if (chosen) {
                g.fill(getX(), getY() + 3, getX() + 2, getY() + height - 3, GOLD);
            }
            com.mojang.blaze3d.systems.RenderSystem.enableBlend();
            com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
            g.blit(NAVIGATION, getX() + (width < 44 ? (width - 16) / 2 : 4), getY() + 4, icon * 16, 0, 16, 16, 96, 16);
            g.setColor(1, 1, 1, 1);
            com.mojang.blaze3d.systems.RenderSystem.disableBlend();
            if (width >= 44) fit(g, getMessage(), getX() + 24, getY() + 8, width - 26, !active ? MUTED : chosen ? GOLD : TEXT, mx, my);
            else if (isHoveredOrFocused()) tooltip(getMessage());
            if (!active) g.fill(getX() + 3, getY() + 3, getX() + 21, getY() + 21, 0x99252C2F);
        }
    }
    public static class Icon extends Action {
        private final Supplier<ItemStack> icon;
        private final Supplier<Component> hint;
        private final String index;
        private Supplier<Component> symbol;
        public Icon symbol(Supplier<Component> value) { symbol = value; return this; }
        public Icon(int x, int y, int size, String index, Supplier<ItemStack> icon,
                    Supplier<Component> hint, OnPress action, BooleanSupplier selected) {
            super(x, y, size, size, hint.get(), action, selected);
            this.icon = icon; this.hint = hint; this.index = index;
        }
        @Override protected void renderWidget(GuiGraphics g, int mx, int my, float delta) {
            slot(g, getX(), getY(), width, height, selected.getAsBoolean() ? GOLD : BORDER);
            if (isHoveredOrFocused()) g.fill(getX() + 1, getY() + 1, getX() + width - 1, getY() + height - 1, 0x2249B4BC);
            ItemStack stack = icon.get();
            if (symbol != null) g.drawCenteredString(Minecraft.getInstance().font, symbol.get(), getX() + width / 2, getY() + 5, GOLD);
            else if (!stack.isEmpty()) g.renderItem(stack, getX() + (width - 16) / 2, getY() + (height - 16) / 2 - 2);
            else g.drawCenteredString(Minecraft.getInstance().font, active ? "+" : "-", getX() + width / 2, getY() + 5, MUTED);
            if (!active) g.fill(getX() + 1, getY() + 1, getX() + width - 1, getY() + height - 1, 0x99252C2F);
            g.drawString(Minecraft.getInstance().font, index, getX() + 2, getY() + height - 9, active ? TEXT : MUTED, false);
            setMessage(hint.get());
            if (isHoveredOrFocused()) tooltip(hint.get());
        }
    }
    public static class Toggle extends Action {
        private final ForgeConfigSpec.BooleanValue value;
        public Toggle(int x, int y, int w, Component label, ForgeConfigSpec.BooleanValue value) {
            super(x, y, w, 22, label, b -> { value.set(!value.get()); value.save(); });
            this.value = value;
        }
        @Override protected void renderWidget(GuiGraphics g, int mx, int my, float delta) {
            var font = Minecraft.getInstance().font;
            surface(g, false);
            fit(g, getMessage(), getX() + 5, getY() + 7, width - 43, TEXT, mx, my);
            int x = getX() + width - 27, y = getY() + 6;
            g.fill(x, y, x + 24, y + 10, value.get() ? 0xFF305C49 : 0xFF192428);
            g.renderOutline(x - 1, y - 1, 26, 12, value.get() ? GREEN : BORDER);
            g.fill(x + (value.get() ? 15 : 2), y + 2, x + (value.get() ? 22 : 9), y + 8, value.get() ? GREEN : MUTED);
            if (isHoveredOrFocused()) tooltip(getMessage().copy().append(value.get() ? "：开" : "：关"));
        }
    }
    public static class Slider extends AbstractSliderButton {
        private final ForgeConfigSpec.DoubleValue setting;
        private final double min, max, step;
        private final String label;
        public Slider(int x, int y, int w, String label, ForgeConfigSpec.DoubleValue setting, double min, double max, double step) {
            super(x, y, w, 30, Component.literal(label), (setting.get() - min) / (max - min));
            this.setting = setting; this.min = min; this.max = max; this.step = step; this.label = label;
            updateMessage();
        }
        @Override protected void updateMessage() {
            if (setting != null) setMessage(Component.literal(label + "：" + String.format(Locale.ROOT, "%.2f", setting.get())));
        }
        @Override protected void applyValue() {
            double raw = min + value * (max - min);
            double rounded = value >= 1 ? max : value <= 0 ? min : Math.round(raw / step) * step;
            setting.set(Math.max(min, Math.min(max, Math.round(rounded * 100) / 100.0)));
            updateMessage();
        }
        @Override public void onRelease(double x, double y) { super.onRelease(x, y); setting.save(); }
        @Override public boolean keyPressed(int key, int scan, int mods) {
            boolean handled = super.keyPressed(key, scan, mods);
            if (handled) setting.save();
            return handled;
        }
        @Override public void renderWidget(GuiGraphics g, int mx, int my, float delta) {
            fit(g, getMessage(), getX(), getY() + 2, width, TEXT, mx, my);
            int y = getY() + 22, thumb = getX() + 3 + (int) ((width - 6) * value);
            g.fill(getX(), y - 1, getX() + width, y + 3, 0xFFC5CCC1);
            g.fill(getX(), y + 2, getX() + width, y + 3, BORDER);
            g.fill(getX(), y, thumb, y + 2, CYAN);
            g.fillGradient(thumb - 3, y - 3, thumb + 3, y + 5, isHoveredOrFocused() ? GOLD : CYAN, 0xFF304D46);
            g.renderOutline(thumb - 4, y - 4, 8, 10, BORDER);
            g.fill(thumb - 1, y - 1, thumb, y + 3, 0xFFDCFFFF);
        }
    }
    private static void buttonSurface(GuiGraphics g, int x, int y, int w, int h, boolean enabled, boolean selected, boolean hovered, boolean pressed) {
        String state = !enabled ? "disabled" : pressed ? "pressed" : selected ? "selected" : hovered ? "hover" : "normal";
        g.fill(x + 1, y + 2, x + w, y + h + 1, 0x5510110F);
        nineSlice(g, "button/button_" + state, x, y, w, h, 32, 24, 4);
    }
    public static void slot(GuiGraphics g, int x, int y, int w, int h, int border) {
        g.fillGradient(x, y, x + w, y + h, 0xFF454B46, 0xFF697068);
        g.renderOutline(x, y, w, h, border);
        g.fill(x + 1, y + 1, x + w - 1, y + 2, 0xFF263033);
        g.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, 0xFFB6C0B3);
        g.fill(x, y, x + 3, y + 1, TEXT);
        g.fill(x + w - 3, y + h - 1, x + w, y + h, BORDER);
    }
    public static void paper(GuiGraphics g, int x, int y, int w, int h) {
        for (int row = 0; row < h; row += 64) for (int col = 0; col < w; col += 64)
            g.blit(texture("background/paper_base"), x + col, y + row, 0, 0, Math.min(64, w - col), Math.min(64, h - row), 64, 64);
    }
    public static void ornamentFrame(GuiGraphics g, int x, int y, int w, int h) {
        nineSlice(g, "frame/frame_main", x, y, w, h, 32, 32, 11);
    }
    private static net.minecraft.resources.ResourceLocation texture(String name) {
        return TEXTURES.computeIfAbsent(name, key -> new net.minecraft.resources.ResourceLocation("wuxingxiantu", "textures/gui/wuxia/" + key + ".png"));
    }
    // Keep corners at native GUI size; stretch only the edges and center.
    private static void nineSlice(GuiGraphics g, String name, int x, int y, int w, int h, int tw, int th, int edge) {
        if (w <= 0 || h <= 0) return;
        int e = Math.min(edge, Math.min(w, h) / 2);
        int[] dx = {0, e, w - e, w}, dy = {0, e, h - e, h};
        int[] sx = {0, e, tw - e, tw}, sy = {0, e, th - e, th};
        com.mojang.blaze3d.systems.RenderSystem.enableBlend();
        com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
        for (int row = 0; row < 3; row++) for (int col = 0; col < 3; col++)
            if (dx[col + 1] > dx[col] && dy[row + 1] > dy[row])
                g.blit(texture(name), x + dx[col], y + dy[row], dx[col + 1] - dx[col], dy[row + 1] - dy[row],
                        sx[col], sy[row], sx[col + 1] - sx[col], sy[row + 1] - sy[row], tw, th);
        com.mojang.blaze3d.systems.RenderSystem.disableBlend();
    }
    public static void panel(GuiGraphics g, int x, int y, int w, int h, boolean primary) {
        com.mojang.blaze3d.systems.RenderSystem.enableBlend();
        com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
        var resource = texture("panel/panel_" + (primary ? "main" : "secondary"));
        for (int row = 0; row < h; row += 32) for (int col = 0; col < w; col += 32)
            g.blit(resource, x + col, y + row, 0, 0, Math.min(32, w - col), Math.min(32, h - row), 32, 32);
        com.mojang.blaze3d.systems.RenderSystem.disableBlend();
        g.renderOutline(x, y, w, h, primary ? BORDER : 0x806F674F);
    }
    public static void scenery(GuiGraphics g, int x, int y, int w, int h) {
        // Preserve the supplied painting's aspect ratio with a centered cover crop.
        int tw = 1672, th = 941;
        double scale = Math.max(w / (double) tw, h / (double) th);
        int sw = Math.min(tw, (int) Math.round(w / scale)), sh = Math.min(th, (int) Math.round(h / scale));
        g.blit(texture("background/landscape"), x, y, w, h, (tw - sw) / 2F, (th - sh) / 2F, sw, sh, tw, th);
    }
    private static void pixel(GuiGraphics g, int x, int y, int color) { g.fill(x, y, x + 1, y + 1, color); }
    public static void rule(GuiGraphics g, int x, int y, int w) {
        g.fill(x + 3, y, x + w - 3, y + 1, BORDER);
        for (int end : new int[]{x + 1, x + w - 2}) {
            g.fill(end - 1, y, end + 2, y + 1, GOLD);
            g.fill(end, y - 1, end + 1, y + 2, GOLD);
        }
    }
    public static void tooltip(Component text) {
        var mc = Minecraft.getInstance();
        if (mc.screen != null) mc.screen.setTooltipForNextRenderPass(mc.font.split(text, Math.min(240, mc.screen.width - 16)));
    }
    public static void fit(GuiGraphics g, Component text, int x, int y, int w, int color, int mx, int my) {
        var font = Minecraft.getInstance().font;
        boolean clipped = font.width(text) > w;
        if (clipped) g.drawString(font, font.plainSubstrByWidth(text.getString(), Math.max(0, w - font.width("..."))) + "...", x, y, color, false);
        else g.drawString(font, text, x, y, color, false);
        if (clipped && mx >= x && mx < x + w && my >= y && my < y + 10) tooltip(text);
    }
    public static String number(double value) {
        double magnitude = Math.abs(value);
        if (magnitude >= 100000000) return compactNumber(value / 100000000) + "亿";
        if (magnitude >= 10000) return compactNumber(value / 10000) + "万";
        return String.format(Locale.ROOT, "%.0f", value);
    }
    private static String compactNumber(double value) {
        return String.format(Locale.ROOT, Math.abs(value - Math.rint(value)) < .05 ? "%.0f" : "%.1f", value);
    }
    public static void bar(GuiGraphics g, String label, int x, int y, int w, double value, double max, int color, int mx, int my) {
        var font = Minecraft.getInstance().font;
        fit(g, Component.literal(label + " " + number(value) + " / " + number(max)), x, y, w, TEXT, mx, my);
        g.fill(x, y + 12, x + w, y + 19, 0xFF131713);
        int fill = (int) ((w - 2) * Math.max(0, Math.min(1, value / Math.max(1, max))));
        g.fillGradient(x + 1, y + 13, x + 1 + fill, y + 18, color, 0xFF000000 | (color & 0xFEFEFE) >> 1);
        g.renderOutline(x, y + 12, w, 7, BORDER);
        if (mx >= x && mx < x + w && my >= y && my < y + 18) {
            var screen = Minecraft.getInstance().screen;
            if (screen != null) screen.setTooltipForNextRenderPass(font.split(Component.literal(String.format(Locale.ROOT, "%s：%,.0f / %,.0f", label, value, max)), 240));
        }
    }
}
