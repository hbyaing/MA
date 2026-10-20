package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.DivineArtCatalog;
import cn.blockforge.generated.generatedmod.GeneratedMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** 神通发动提示：仅在 HUD 顶部显示短暂文字，不再使用会伪装成断线界面的整屏图片。 */
@Mod.EventBusSubscriber(modid = GeneratedMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class DivineArtBanner {
    private static final int FADE_IN_TICKS = 4;
    private static final int HOLD_TICKS = 24;
    private static final int FADE_OUT_TICKS = 8;
    private static final int TOTAL_TICKS = FADE_IN_TICKS + HOLD_TICKS + FADE_OUT_TICKS;
    private static String activeDivineArt = "";
    private static int ticksLeft;

    private DivineArtBanner() {}

    /** 空 ID 只清理当前提示；发动成功后显示对应神通的本地化名称。 */
    public static void play(String divineArtId) {
        if (divineArtId == null || divineArtId.isBlank()) {
            activeDivineArt = "";
            ticksLeft = 0;
            return;
        }
        if (!DivineArtCatalog.isValid(divineArtId)) return;
        activeDivineArt = divineArtId;
        ticksLeft = TOTAL_TICKS;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || ticksLeft <= 0) return;
        ticksLeft--;
        if (ticksLeft == 0) activeDivineArt = "";
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        if (ticksLeft <= 0 || activeDivineArt.isBlank()) return;
        Minecraft client = Minecraft.getInstance();
        // 任何真实界面（包括暂停、设置、轮盘和断线界面）打开时都不叠加神通提示。
        if (client.player == null || client.level == null || client.screen != null || client.options.hideGui) return;

        int elapsed = TOTAL_TICKS - ticksLeft;
        float alpha = elapsed < FADE_IN_TICKS
                ? elapsed / (float) FADE_IN_TICKS
                : ticksLeft < FADE_OUT_TICKS ? ticksLeft / (float) FADE_OUT_TICKS : 1.0F;
        alpha = Math.max(0.0F, Math.min(1.0F, alpha));

        GuiGraphics graphics = event.getGuiGraphics();
        Component text = Component.translatable("gui.wuxingxiantu.divine_cast",
                DivineArtCatalog.name(activeDivineArt));
        int screenWidth = client.getWindow().getGuiScaledWidth();
        int panelWidth = Math.min(screenWidth - 24, Math.max(176, client.font.width(text) + 34));
        int panelHeight = 28;
        int left = (screenWidth - panelWidth) / 2;
        int top = WuXingClient.visible(client) && left < WuXingClient.hudRight(client) ? WuXingClient.hudBottom(client) + 6 : 18;
        int panelAlpha = Math.round(210.0F * alpha);
        int borderAlpha = Math.round(245.0F * alpha);
        int textAlpha = Math.round(255.0F * alpha);

        graphics.fill(left, top, left + panelWidth, top + panelHeight, (panelAlpha << 24) | 0xF1F2EE);
        graphics.renderOutline(left, top, panelWidth, panelHeight, (borderAlpha << 24) | 0x667368);
        graphics.fill(left + 4, top + 4, left + 7, top + panelHeight - 4,
                (borderAlpha << 24) | 0x963E35);
        graphics.drawCenteredString(client.font, text, screenWidth / 2, top + 10,
                (textAlpha << 24) | 0x272F2D);
    }
}
