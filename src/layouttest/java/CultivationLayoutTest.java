import cn.blockforge.generated.generatedmod.client.CultivationLayout;

/** Run directly with Java 17; no game runtime or JAR task required. */
public final class CultivationLayoutTest {
    public static void main(String[] args) {
        int cases = 0;
        for (int[] resolution : new int[][]{{1920,1080},{2560,1440},{1280,720},{1280,960},{320,240},{1440,480}}) {
            for (int scale = 1; scale <= 8; scale++) {
                int w = (resolution[0] + scale - 1) / scale, h = (resolution[1] + scale - 1) / scale;
                if (w < 320 || h < 240) continue;
                var l = CultivationLayout.of(w, h);
                check(l.x() >= 24 && w - l.x() - l.width() >= 24, "side margins");
                check(l.width() <= 480, "window width cap for JEI");
                if (w >= 800) check(l.x() >= 150 && w - l.x() - l.width() >= 150, "JEI side space at wide resolutions");
                check(l.y() >= 8 && l.y() + l.height() <= h - 8, "vertical bounds");
                check(l.contentX() > l.x() + l.sidebarWidth() + 6, "sidebar separation");
                check(l.contentFrameX()==l.contentX()-5,"background must meet divider edge");
                check(l.contentFrameY()==l.y()+l.headerHeight(),"background must meet header bottom");
                check(l.contentFrameX()+l.contentFrameWidth()==l.x()+l.width()-3,"background must meet right frame");
                check(l.contentFrameY()+l.contentFrameHeight()==l.y()+l.height()-3,"background must meet bottom frame");
                check(l.inventoryX() >= l.contentX() && l.inventoryX() + l.inventoryWidth() <= l.contentX() + l.contentWidth(), "inventory width");
                check(l.inventoryY() >= l.contentY() && l.inventoryY() + l.inventoryHeight() <= l.contentY() + l.contentHeight(), "inventory height");
                for (int i = 0; i < 6; i++) check(l.navY(i) >= l.y() + l.headerHeight() && l.navY(i) + l.navigationHeight() <= l.y() + l.height() - 5, "navigation bounds");
                cases++;
            }
        }
        System.out.println("PASS: " + cases + " resolution/GUI-scale layouts");
    }
    private static void check(boolean passed, String message) { if (!passed) throw new AssertionError(message); }
}
