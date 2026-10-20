package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.CultivationData;
import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.HudConfig;
import cn.blockforge.generated.generatedmod.items.CultivationLogic;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.Locale;

@Mod.EventBusSubscriber(modid = GeneratedMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class WuXingClient {
    static final int HUD_WIDTH = 252, HUD_HEIGHT = 100, MARGIN = 8;
    private static final int TEXT = 0xFFF4EFDA, GOLD = 0xFFD8BD79;
    private static final ResourceLocation PORTRAIT = texture("portrait_frame"), REALM = texture("realm_frame");
    private static final ResourceLocation HEALTH = texture("icon_health"), SPIRIT = texture("icon_spirit");
    private static final ResourceLocation CULTIVATION = texture("icon_cultivation");
    private static final ResourceLocation ARMOR = texture("icon_armor"), HUNGER = texture("icon_hunger");
    private static final ResourceLocation BAR = texture("bar_frame"), EXP_BAR = texture("exp_frame");
    private static final ResourceLocation HEALTH_FILL = texture("bar_fill_health"), SPIRIT_FILL = texture("bar_fill_spirit");
    private static final ResourceLocation CULTIVATION_FILL = texture("bar_fill_cultivation"), EXP_FILL = texture("bar_fill_exp");
    private static final ResourceLocation CLOUD_LEFT = texture("cloud_left"), CLOUD_RIGHT = texture("cloud_right");
    private static final ResourceLocation LEVEL = texture("level_frame");
    private static int tribulationCountdown;

    private WuXingClient() {}

    private static ResourceLocation texture(String name) {
        return new ResourceLocation(GeneratedMod.MOD_ID, "textures/gui/hud/" + name + ".png");
    }

    public static void setTribulationState(int countdown, boolean active, boolean heartDemon) {
        tribulationCountdown = Math.max(0, countdown);
    }

    static boolean visible(Minecraft client) {
        return replaceStatus(client) && !(client.screen instanceof CultivationMainScreen);
    }

    private static boolean replaceStatus(Minecraft client) {
        if(client.player==null||client.level==null||client.options.hideGui)return false;
        CultivationData data=GeneratedMod.data(client.player);
        if(data==null)return false;
        // Projection uses spectator mode for movement, but still needs the player's cultivation HUD.
        return !client.player.isSpectator()||"yuan_shen_chu_qiao".equals(data.wuxingxiantu$getActiveDivineArt());
    }

    static float hudScale(Minecraft client) {
        float availableWidth = Math.max(1, client.getWindow().getGuiScaledWidth() - MARGIN * 2);
        float availableHeight = Math.max(1, client.getWindow().getGuiScaledHeight() - MARGIN * 2);
        return (float) Math.min(HudConfig.SCALE.get(),
                Math.min(availableWidth / HUD_WIDTH, availableHeight / HUD_HEIGHT));
    }

    public static int hudBottom(Minecraft client) {
        return hudTop(client) + (int) Math.ceil(HUD_HEIGHT * hudScale(client));
    }

    public static int hudRight(Minecraft client) {
        return hudLeft(client) + (int) Math.ceil(HUD_WIDTH * hudScale(client));
    }

    public static int hudLeft(Minecraft client) {
        return MARGIN+(int)Math.round(Math.max(0,client.getWindow().getGuiScaledWidth()-MARGIN*2-HUD_WIDTH*hudScale(client))*HudConfig.PLAYER_X.get()/100);
    }

    public static int hudTop(Minecraft client) {
        return MARGIN+(int)Math.round(Math.max(0,client.getWindow().getGuiScaledHeight()-MARGIN*2-HUD_HEIGHT*hudScale(client))*HudConfig.PLAYER_Y.get()/100);
    }

    @SubscribeEvent
    public static void hideDuplicateOverlay(RenderGuiOverlayEvent.Pre event) {
        if (event.getOverlay().id().equals(VanillaGuiOverlay.POTION_ICONS.id())) {
            event.setCanceled(true);
            return;
        }
        if (!replaceStatus(Minecraft.getInstance())) return;
        var id = event.getOverlay().id();
        if (id.equals(VanillaGuiOverlay.PLAYER_HEALTH.id()) || id.equals(VanillaGuiOverlay.ARMOR_LEVEL.id())
                || id.equals(VanillaGuiOverlay.FOOD_LEVEL.id()) || id.equals(VanillaGuiOverlay.EXPERIENCE_BAR.id()))
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void renderHud(RenderGuiEvent.Post event) {
        Minecraft client = Minecraft.getInstance();
        if (!visible(client)) return;
        CultivationData data = GeneratedMod.data(client.player);
        var player = client.player;
        var g = event.getGuiGraphics();
        float scale = hudScale(client);
        g.pose().pushPose();
        g.pose().translate(hudLeft(client), hudTop(client), 0);
        g.pose().scale(scale, scale, 1);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        try {
            // Only local decorations have backgrounds; there is no HUD-wide panel.
            sprite(g, CLOUD_LEFT, 0, 0, 48, 24);
            sprite(g, CLOUD_RIGHT, 226, 45, 26, 52);
            drawPortrait(g, client);
            sprite(g, REALM, 15, 61, 22, 37);
            drawRealm(g, CultivationLogic.realmName(player, data.wuxingxiantu$getRealm()));

            float health = Math.max(0, player.getHealth()), maxHealth = Math.max(0, player.getMaxHealth());
            float absorption = player.getAbsorptionAmount();
            int healthTint = player.hasEffect(MobEffects.POISON) ? 0xFFA5B77E
                    : player.hasEffect(MobEffects.WITHER) ? 0xFFA8A0B0
                    : player.isFullyFrozen() ? 0xFF9AC7D6 : 0xFFFFFFFF;
            String healthText = number(health) + " / " + number(maxHealth)
                    + (absorption > 0 ? " +" + number(absorption) : "");
            mainBar(g, HEALTH, HEALTH_FILL, "生命", 9, ratio(health, maxHealth), healthText,
                    healthTint, absorption > 0 ? GOLD : TEXT);
            if (health <= maxHealth * .2F && player.tickCount % 20 < 10)
                g.renderOutline(100, 11, 140, 10, 0xCCED827A);

            float maxMana = Math.max(0, data.getMaxMana()), mana = Math.max(0, data.getCurrentMana());
            mainBar(g, SPIRIT, SPIRIT_FILL, "灵力", 27, ratio(mana, maxMana),
                    number(mana) + " / " + number(maxMana), 0xFFFFFFFF,
                    mana < maxMana * .2F ? CultivationUi.RED : TEXT);
            long cap = Math.max(0, CultivationLogic.breakthroughRequirement(data));
            long cultivation = Math.max(0, data.wuxingxiantu$getCultivation());
            mainBar(g, CULTIVATION, CULTIVATION_FILL, "修为", 45, ratio(cultivation, cap),
                    number(cultivation) + " / " + number(cap), 0xFFFFFFFF, TEXT);

            sprite(g, ARMOR, 54, 65, 15, 15);
            fitted(g, "护甲 " + player.getArmorValue(), 72, 67, 46, 9, TEXT, false);
            g.fill(120, 67, 121, 76, 0x80998053);
            sprite(g, HUNGER, 125, 65, 15, 15);
            fitted(g, "饥饿 " + player.getFoodData().getFoodLevel(), 143, 67, 42, 9, TEXT, false);
            sprite(g, LEVEL, 190, 64, 54, 17);
            fitted(g, "Lv." + player.experienceLevel, 196, 68, 42, 9, TEXT, 0.5F);

            fitted(g, "经验", 54, 86, 22, 9, TEXT, false);
            int needed = Math.max(0, player.getXpNeededForNextLevel());
            double xpRatio = ratio(player.experienceProgress, 1);
            int experience = Math.min(needed, (int) Math.round(xpRatio * needed));
            bar(g, EXP_BAR, EXP_FILL, 80, 85, 106, 10, needed > 0 ? xpRatio : 0, 0xFFFFFFFF);
            fitted(g, number(experience) + " / " + number(needed), 190, 86, 54, 9, TEXT, 0.5F);
        } finally {
            RenderSystem.setShaderColor(1, 1, 1, 1);
            RenderSystem.disableBlend();
            g.pose().popPose();
        }
        if (tribulationCountdown > 0)
            g.drawCenteredString(client.font, Component.translatable("hud.wuxingxiantu.tribulation_countdown", tribulationCountdown),
                    client.getWindow().getGuiScaledWidth() / 2,
                    Math.max(hudBottom(client) + 8, client.getWindow().getGuiScaledHeight() / 2 - 34), 0xFFBDA4FF);
    }

    private static void drawPortrait(GuiGraphics g, Minecraft client) {
        var skin = client.player.getSkinTextureLocation();
        PlayerFaceRenderer.draw(g, skin, 15, 13, 22);
        RenderSystem.enableBlend();
        g.blit(skin, 15, 35, 22, 22, 20, 20, 8, 12, 64, 64);
        g.blit(skin, 15, 35, 22, 22, 20, 36, 8, 12, 64, 64);
        int armWidth = "slim".equals(client.player.getModelName()) ? 3 : 4;
        g.blit(skin, 9, 35, 6, 22, 44, 20, armWidth, 12, 64, 64);
        g.blit(skin, 9, 35, 6, 22, 44, 36, armWidth, 12, 64, 64);
        g.blit(skin, 37, 35, 6, 22, 36, 52, armWidth, 12, 64, 64);
        g.blit(skin, 37, 35, 6, 22, 52, 52, armWidth, 12, 64, 64);
        sprite(g, PORTRAIT, 5, 8, 42, 52);
    }

    private static void drawRealm(GuiGraphics g, String realm) {
        int[] characters = realm.codePoints().toArray();
        int count = Math.max(1, characters.length);
        float scale = Math.min(1F, 23F / (count * 10F));
        float top = 65 + (23 - count * 10 * scale) / 2;
        for (int i = 0; i < characters.length; i++) {
            String glyph = new String(Character.toChars(characters[i]));
            var font = Minecraft.getInstance().font;
            g.pose().pushPose();
            g.pose().translate(26 - font.width(glyph) * scale / 2F, top + i * 10 * scale, 0);
            g.pose().scale(scale, scale, 1);
            g.drawString(font, glyph, 0, 0, GOLD, true);
            g.pose().popPose();
        }
    }

    private static void mainBar(GuiGraphics g, ResourceLocation icon, ResourceLocation fill,
                                String label, int y, double ratio, String value, int tint, int textColor) {
        sprite(g, icon, 54, y - 1, 16, 16);
        fitted(g, label, 74, y + 3, 22, 9, TEXT, false);
        bar(g, BAR, fill, 98, y, 144, 14, ratio, tint);
        fitted(g, value, 104, y + 3, 132, 9, textColor, true);
    }

    private static double ratio(double current, double maximum) {
        if (!Double.isFinite(current) || !Double.isFinite(maximum) || maximum <= 0) return 0;
        return Math.max(0, Math.min(1, current / maximum));
    }

    private static void bar(GuiGraphics g, ResourceLocation frame, ResourceLocation texture,
                            int x, int y, int w, int h, double ratio, int tint) {
        int width = (int) Math.round((w - 4) * ratio);
        g.fill(x + 2, y + 2, x + w - 2, y + h - 2, 0x50121514);
        if (width > 0) {
            RenderSystem.enableBlend();
            RenderSystem.setShaderColor(((tint >> 16) & 255) / 255F,
                    ((tint >> 8) & 255) / 255F, (tint & 255) / 255F, 1);
            // Source UV width matches the destination width: crop, never squeeze the fill.
            g.blit(texture, x + 2, y + 2, 0F, 0F, width, h - 4, w - 4, h - 4);
            RenderSystem.setShaderColor(1, 1, 1, 1);
        }
        sprite(g, frame, x, y, w, h);
    }

    private static void sprite(GuiGraphics g, ResourceLocation texture, int x, int y, int w, int h) {
        RenderSystem.enableBlend();
        g.blit(texture, x, y, 0F, 0F, w, h, w, h);
    }

    private static void fitted(GuiGraphics g, String text, int x, int y, int width, int height,
                                int color, boolean rightAligned) {
        fitted(g, text, x, y, width, height, color, rightAligned ? 1F : 0F);
    }

    private static void fitted(GuiGraphics g, String text, int x, int y, int width, int height,
                                int color, float alignment) {
        var font = Minecraft.getInstance().font;
        float scale = Math.min(1F, Math.min(width / (float) Math.max(1, font.width(text)), height / 9F));
        g.pose().pushPose();
        g.pose().translate(x + (width - font.width(text) * scale) * alignment,
                y + (height - 9 * scale) / 2, 0);
        g.pose().scale(scale, scale, 1);
        g.drawString(font, text, 0, 0, color, true);
        g.pose().popPose();
    }

    private static String number(double value) {
        if (value >= 100000000) return String.format(Locale.ROOT, "%.1f亿", value / 100000000);
        if (value >= 10000) return String.format(Locale.ROOT, "%.1f万", value / 10000);
        return Long.toString(Math.round(value));
    }
}
