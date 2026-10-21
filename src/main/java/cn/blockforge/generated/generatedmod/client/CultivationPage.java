package cn.blockforge.generated.generatedmod.client;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** 页面沿用 Screen 的完整输入和生命周期；注册后由主界面装配。 */
public abstract class CultivationPage extends Screen {
    protected CultivationMainScreen host;
    protected CultivationPage(String title) { super(Component.literal(title)); }
    final void attach(CultivationMainScreen host) { this.host = host; }
    public int contentHeight(int width, int viewportHeight) { return viewportHeight; }
    public boolean closeTransientUi() { return false; }
    @Override public void renderBackground(net.minecraft.client.gui.GuiGraphics graphics) {}
    @Override public void onClose() { host.onClose(); }
    @Override public boolean isPauseScreen() { return false; }
}
