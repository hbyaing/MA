package cn.blockforge.generated.generatedmod.client.boss;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Compact ability name, counterplay hint and windup meter. No camera or input changes. */
@Mod.EventBusSubscriber(modid = GeneratedMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class BossCombatHud {
    @SubscribeEvent
    public static void render(RenderGuiEvent.Post event) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null || client.options.hideGui || client.screen != null) return;
        BossCombatFx.Cue cue = BossCombatFx.cue(client.getFrameTime());
        if (cue == null) return;
        String[] parts = cue.label().split("[｜|]", 2);
        String name = parts[0].strip(), hint = parts.length > 1 ? parts[1].strip() : "";
        int screenWidth = client.getWindow().getGuiScaledWidth(), screenHeight = client.getWindow().getGuiScaledHeight();
        int available = Math.max(60, Math.min(320, screenWidth - 24));
        name = client.font.plainSubstrByWidth(name, available - 32);
        hint = client.font.plainSubstrByWidth(hint, available - 24);
        int width = Math.min(available, Math.max(180, Math.max(client.font.width(name), client.font.width(hint)) + 28));
        int height = hint.isBlank() ? 26 : 38, left = (screenWidth - width) / 2;
        int top = Math.max(8,screenHeight-82);
        float fade = (float) Math.min(1, Math.max(0, (Math.min(cue.lifetime(), cue.windup() > 0 ? cue.windup() + 12 : 45) - cue.age()) / 8));
        int background = ((int) (170 * fade) << 24) | 0x111720;
        int color = ((int) (235 * fade) << 24) | cue.color();
        var graphics = event.getGuiGraphics();
        graphics.fill(left, top, left + width, top + height, background);
        graphics.fill(left, top, left + 2, top + height, color);
        graphics.drawCenteredString(client.font, name, screenWidth / 2, top + 5, ((int) (255 * fade) << 24) | 0xFFF2DC);
        if (!hint.isBlank()) graphics.drawCenteredString(client.font, hint, screenWidth / 2, top + 17, ((int) (235 * fade) << 24) | 0xD4E2E6);
        double progress = cue.windup() > 0 ? Mth.clamp(cue.age() / cue.windup(), 0, 1) : 1;
        graphics.fill(left + 7, top + height - 5, left + width - 7, top + height - 3, 0x8056616D);
        graphics.fill(left + 7, top + height - 5, left + 7 + (int) ((width - 14) * progress), top + height - 3, color);
    }

    private BossCombatHud() {}
}
