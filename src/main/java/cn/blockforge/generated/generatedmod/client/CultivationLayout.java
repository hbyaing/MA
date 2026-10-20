package cn.blockforge.generated.generatedmod.client;

/** 中置窗口：左右固定留白，内容只在窗口内部滚动。尺寸均为缩放后的 GUI 像素。 */
public record CultivationLayout(int x, int y, int width, int height,
                                int contentX, int contentY, int contentWidth, int contentHeight) {
    public static final int INVENTORY_WIDTH = 208, INVENTORY_HEIGHT = 190;
    public static CultivationLayout of(int screenWidth, int screenHeight) {
        int margin = Math.max(24, screenWidth / 8);
        int w = Math.max(272, Math.min(480, screenWidth - margin * 2));
        int h = Math.min(294, screenHeight - 16);
        int x = (screenWidth - w) / 2, y = (screenHeight - h) / 2;
        int sidebar = w >= 432 ? 60 : w >= 340 ? 54 : 36;
        int header = w >= 432 && h >= 258 ? 24 : 22;
        return new CultivationLayout(x, y, w, h, x + sidebar + 16, y + header + 4, w - sidebar - 24, h - header - 12);
    }
    public int sidebarWidth() { return width >= 432 ? 60 : width >= 340 ? 54 : 36; }
    public int headerHeight() { return contentY - y - 4; }
    /** Background reaches the divider and outer frame; widgets retain their inner padding. */
    public int contentFrameX() { return contentX - 5; }
    public int contentFrameY() { return y + headerHeight(); }
    public int contentFrameWidth() { return x + width - 3 - contentFrameX(); }
    public int contentFrameHeight() { return y + height - 3 - contentFrameY(); }
    public boolean spaciousInventory() { return contentWidth >= 380 && contentHeight >= 252; }
    public int inventoryWidth() { return spaciousInventory() ? 250 : INVENTORY_WIDTH; }
    public int inventoryHeight() { return spaciousInventory() ? 252 : INVENTORY_HEIGHT; }
    public boolean hasOverview() { return contentWidth >= INVENTORY_WIDTH + 112; }
    public int inventoryX() { return hasOverview() ? contentX : contentX + (contentWidth - inventoryWidth()) / 2; }
    public int inventoryY() { return contentY + Math.max(0, (contentHeight - inventoryHeight()) / 2); }
    public int navigationHeight() { return Math.max(12, Math.min(32, (height - headerHeight() - 16) / CultivationPageRegistry.visible().size() - 4)); }
    public int navY(int index) { return y + headerHeight() + 8 + index * (navigationHeight() + 4); }
}
