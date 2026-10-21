package cn.blockforge.generated.generatedmod.client;

/** 中置窗口：左右固定留白，内容只在窗口内部滚动。尺寸均为缩放后的 GUI 像素。 */
public record CultivationLayout(int x, int y, int width, int height,
                                int contentX, int contentY, int contentWidth, int contentHeight) {
    public static final int INVENTORY_WIDTH = 208, INVENTORY_HEIGHT = 190;
    public static CultivationLayout of(int screenWidth, int screenHeight) {
        int margin = Math.max(40, screenWidth / 6);
        int w = Math.max(272, Math.min(432, screenWidth - margin * 2));
        int h = Math.min(224, screenHeight - 16);
        int x = (screenWidth - w) / 2, y = (screenHeight - h) / 2;
        int sidebar = w >= 340 ? 50 : w >= 280 ? 46 : 36;
        return new CultivationLayout(x, y, w, h, x + sidebar + 16, y + 26, w - sidebar - 24, h - 34);
    }
    public int sidebarWidth() { return width >= 340 ? 50 : width >= 280 ? 46 : 36; }
    public boolean hasOverview() { return contentWidth >= INVENTORY_WIDTH + 112; }
    public int inventoryX() { return hasOverview() ? contentX : contentX + (contentWidth - INVENTORY_WIDTH) / 2; }
    public int inventoryY() { return contentY + Math.max(0, (contentHeight - INVENTORY_HEIGHT) / 2); }
    public int navY(int index) { return y + 30 + index * 25; }
}
