package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.ArtCatalog;
import cn.blockforge.generated.generatedmod.DivineArtCatalog;
import cn.blockforge.generated.generatedmod.CultivationData;
import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.Network;
import cn.blockforge.generated.generatedmod.items.CultivationLogic;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/** 功法释放界面：点击选中的功法槽位后释放功法。 */
public final class ArtRadialScreen extends Screen {
    private static final int OVERLAY = 0x66100D0D;
    private static final int PANEL = 0xE8171A20;
    private static final int PANEL_ALT = 0xD02B2525;
    private static final int GOLD = 0xFFE8C477;
    private static final int PINK = 0xFFFF69C8;
    private static final int TEXT = 0xFFF4E8D0;
    private static final int MUTED = 0xFFC8B99C;
    private static final int EMPTY = 0xFF5E5048;
    private static final int LOCKED = 0xFF40383A;
    private static final int RADIUS = 92;
    private static final int NODE_SIZE = 42;
    private int hoveredSlot = -1;
    private int hoveredDivine = -1;
    private boolean swordMode;

    public ArtRadialScreen() {
        super(Component.translatable("gui.wuxingxiantu.art_radial.title"));
    }

    @Override protected void init() {
        refreshMode();
    }

    private void refreshMode() {
        swordMode=minecraft.player!=null
                &&minecraft.player.getVehicle() instanceof cn.blockforge.generated.generatedmod.yujian.ControlledSwordEntity sword
                &&sword.flightRoot()
                &&sword.flightState()==cn.blockforge.generated.generatedmod.yujian.FlyingSwordState.FLYING;
    }

    @Override public void tick() {
        super.tick();
        boolean previous=swordMode;refreshMode();
        if(previous!=swordMode){hoveredSlot=-1;hoveredDivine=-1;}
    }

    private ItemStack flightSword() {
        return minecraft.player==null?ItemStack.EMPTY:
                cn.blockforge.generated.generatedmod.robe.YujianSlot.activeFlyingSword(minecraft.player);
    }

    private String[] releaseSlots(CultivationData data) {
        if (!swordMode) {
            String[] slots=data==null?new String[8]:data.wuxingxiantu$getEquippedArts();
            for(int i=0;i<slots.length;i++)
                if(cn.blockforge.generated.generatedmod.yujian.SwordTechniqueRegistry.isTechnique(slots[i]))slots[i]=null;
            return slots;
        }
        String[] slots = new String[8];
        var stack = flightSword();
        if (data == null || !(stack.getItem() instanceof cn.blockforge.generated.generatedmod.items.SpiritSwordItem spirit)) return slots;
        var equipped = cn.blockforge.generated.generatedmod.yujian.NurtureData.read(stack).equippedTechniques;
        for (int i = 0; i < Math.min(spirit.profile().maxTechniqueSlots(), equipped.size()) && i < slots.length; i++)
            if (data.wuxingxiantu$knowsSwordTechnique(equipped.get(i))) slots[i] = equipped.get(i);
        return slots;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;
        boolean previous=swordMode;refreshMode();
        if(previous!=swordMode){hoveredSlot=-1;hoveredDivine=-1;return true;}
        CultivationData data = GeneratedMod.data(minecraft.player);
        int centerX = width / 2;
        int centerY = Math.max(112, height / 2 - 20);
        String active = data == null ? "" : data.wuxingxiantu$getActiveDivineArt();
        if (button == 0 && !swordMode && DivineArtCatalog.isValid(active)
                && Math.hypot(mouseX - centerX, mouseY - centerY) <= 34.0D) {
            Network.CHANNEL.sendToServer(new cn.blockforge.generated.generatedmod.network.packet.divineart.DivineReleasePacket(active));
            onClose();
        } else if (button == 0 && hoveredDivine >= 0) {
            String[] equipped = divineSlots(data);
            if (hoveredDivine < equipped.length && DivineArtCatalog.isValid(equipped[hoveredDivine])) {
                Network.CHANNEL.sendToServer(new cn.blockforge.generated.generatedmod.network.packet.divineart.DivineReleasePacket(equipped[hoveredDivine]));
                onClose();
            }
        } else if (button == 0 && hoveredSlot >= 0) {
            if (swordMode) {
                String id = releaseSlots(data)[hoveredSlot];
                if (id != null) Network.CHANNEL.sendToServer(new cn.blockforge.generated.generatedmod.network.packet.yujian.SwordNurtureActionPacket(
                        (byte)5, cn.blockforge.generated.generatedmod.robe.YujianSlot.NURTURE_SLOT, -1, -1, id));
            } else Network.CHANNEL.sendToServer(new cn.blockforge.generated.generatedmod.network.packet.cultivation.ArtReleasePacket(hoveredSlot));
            onClose();
        }
        return true;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        refreshMode();
        renderBackground(graphics);
        graphics.fill(0, 0, width, height, OVERLAY);

        CultivationData data = GeneratedMod.data(minecraft.player);
        int unlocked = data == null ? 0 : CultivationLogic.unlockedArtSlots(data.wuxingxiantu$getRealm());
        if (swordMode) unlocked = flightSword().getItem() instanceof cn.blockforge.generated.generatedmod.items.SpiritSwordItem spirit
                ? spirit.profile().maxTechniqueSlots() : 0;
        int centerX = width / 2;
        int centerY = Math.max(112, height / 2 - 20);
        int radius = Math.min(RADIUS, Math.max(58, Math.min(width, height) / 4));
        hoveredSlot = findHoveredSlot(data, unlocked, mouseX, mouseY, centerX, centerY, radius);
        hoveredDivine = swordMode ? -1 : findHoveredDivine(data, mouseX, mouseY, centerX, centerY, radius + 62);

        drawParticles(graphics, centerX, centerY, radius);
        drawRing(graphics, centerX, centerY, radius);
        drawCenter(graphics, data, unlocked, centerX, centerY);
        drawSlots(graphics, data, unlocked, centerX, centerY, radius);
        if (!swordMode) drawDivineNodes(graphics, data, centerX, centerY, radius + 62);
        drawDetail(graphics, data, centerX, centerY, radius);
        super.render(graphics, mouseX, mouseY, delta);
    }

    private void drawRing(GuiGraphics graphics, int centerX, int centerY, int radius) {
        graphics.fill(centerX - radius - 2, centerY - 1, centerX - radius + 3, centerY + 2, 0xB86F5540);
        graphics.fill(centerX + radius - 3, centerY - 1, centerX + radius + 2, centerY + 2, 0xB86F5540);
        graphics.fill(centerX - 1, centerY - radius - 2, centerX + 2, centerY - radius + 3, 0xB86F5540);
        graphics.fill(centerX - 1, centerY + radius - 3, centerX + 2, centerY + radius + 2, 0xB86F5540);
        for (int i = 0; i < 64; i++) {
            double angle = Math.PI * 2.0D * i / 64.0D;
            int x = centerX + (int) Math.round(Math.cos(angle) * radius);
            int y = centerY + (int) Math.round(Math.sin(angle) * radius);
            graphics.fill(x - 1, y - 1, x + 2, y + 2, i % 4 == 0 ? 0xD9E8C477 : 0x9A8D6B4D);
        }
        graphics.renderOutline(centerX - radius - 18, centerY - radius - 18,
                radius * 2 + 36, radius * 2 + 36, 0x553A2925);
    }

    private void drawParticles(GuiGraphics graphics, int centerX, int centerY, int radius) {
        long time = minecraft.level == null ? 0L : minecraft.level.getGameTime();
        for (int i = 0; i < 12; i++) {
            double angle = (time * 0.025D) + i * Math.PI * 2.0D / 12.0D;
            double distance = radius + 12.0D + (i % 3) * 5.0D;
            int x = centerX + (int) Math.round(Math.cos(angle) * distance);
            int y = centerY + (int) Math.round(Math.sin(angle) * distance);
            int color = 0x80E8C477 | ((80 + (i % 3) * 30) << 24);
            graphics.fill(x, y, x + 2, y + 2, color);
        }
    }

    private void drawCenter(GuiGraphics graphics, CultivationData data, int unlocked, int centerX, int centerY) {
        int size = 66;
        graphics.fill(centerX - size / 2, centerY - size / 2, centerX + size / 2, centerY + size / 2, PANEL);
        graphics.renderOutline(centerX - size / 2, centerY - size / 2, size, size, 0xFFD19B56);
        graphics.renderOutline(centerX - size / 2 + 4, centerY - size / 2 + 4, size - 8, size - 8, 0x886A3A2D);
        String active = data == null ? "" : data.wuxingxiantu$getActiveDivineArt();
        if (!swordMode && DivineArtCatalog.isValid(active)) {
            graphics.drawCenteredString(font, DivineArtCatalog.name(active), centerX, centerY - 11, PINK);
            graphics.drawCenteredString(font,
                    Component.translatable("gui.wuxingxiantu.art_radial.converge_hint"), centerX, centerY + 5, GOLD);
            graphics.drawCenteredString(font,
                    Component.translatable("gui.wuxingxiantu.art_radial.converge_key"), centerX, centerY + 20, MUTED);
        } else {
            graphics.drawCenteredString(font, Component.translatable(swordMode ? "gui.wuxingxiantu.sword_techniques" : "gui.wuxingxiantu.art_radial.title"), centerX, centerY - 10, GOLD);
            graphics.drawCenteredString(font, Component.translatable("gui.wuxingxiantu.art_radial.click_hint"), centerX, centerY + 9, MUTED);
            graphics.drawCenteredString(font, Component.translatable("gui.wuxingxiantu.art_radial.slot_count", unlocked, 8), centerX, centerY + 24, TEXT);
        }
    }

    private void drawSlots(GuiGraphics graphics, CultivationData data, int unlocked, int centerX, int centerY, int radius) {
        String[] arts = releaseSlots(data);
        for (int slot = 0; slot < 8; slot++) {
            double angle = -Math.PI / 2.0D + slot * Math.PI / 4.0D;
            int x = centerX + (int) Math.round(Math.cos(angle) * radius);
            int y = centerY + (int) Math.round(Math.sin(angle) * radius);
            boolean locked = slot >= unlocked;
            boolean hovered = slot == hoveredSlot;
            int background = locked ? LOCKED : hovered ? 0xFF594435 : 0xDD302629;
            int accent = locked ? 0xFF665952 : hovered ? GOLD : 0xFF8D6E4B;
            graphics.fill(x - NODE_SIZE / 2, y - NODE_SIZE / 2, x + NODE_SIZE / 2, y + NODE_SIZE / 2, background);
            graphics.renderOutline(x - NODE_SIZE / 2, y - NODE_SIZE / 2, NODE_SIZE, NODE_SIZE, accent);
            graphics.renderOutline(x - NODE_SIZE / 2 + 3, y - NODE_SIZE / 2 + 3, NODE_SIZE - 6, NODE_SIZE - 6,
                    hovered ? 0xCCF4E8D0 : 0x665E4D45);
            if (locked) {
                graphics.drawCenteredString(font, Component.literal("锁"), x, y - 5, MUTED);
                graphics.drawCenteredString(font, Component.literal(String.valueOf(slot + 1)), x, y + 8, MUTED);
                continue;
            }
            String art = slot < arts.length ? arts[slot] : null;
            if (!ArtCatalog.isValid(art)) {
                graphics.drawCenteredString(font, Component.literal("空"), x, y - 5, EMPTY);
                graphics.drawCenteredString(font, Component.literal(String.valueOf(slot + 1)), x, y + 8, MUTED);
                continue;
            }
            ItemStack stack = artStack(art);
            graphics.renderItem(stack, x - 8, y - 13);
            graphics.drawCenteredString(font, Component.literal(String.valueOf(slot + 1)), x, y + 10,
                    hovered ? GOLD : ArtCatalog.tierColor(ArtCatalog.tier(art)));
        }
    }

    private void drawDivineNodes(GuiGraphics graphics, CultivationData data, int centerX, int centerY, int radius) {
        String[] equippedArts = divineSlots(data);
        for (int i = 0; i < 3; i++) {
            String id = i < equippedArts.length ? equippedArts[i] : null;
            boolean equipped = DivineArtCatalog.isValid(id);
            boolean known = equipped;
            double angle = -Math.PI / 2.0D + i * Math.PI * 2.0D / 3.0D;
            int x = centerX + (int) Math.round(Math.cos(angle) * radius);
            int y = centerY + (int) Math.round(Math.sin(angle) * radius);
            boolean hovered = i == hoveredDivine;
            int color = equipped ? (hovered ? 0xFFFFB4E4 : 0xFFFF69C8) : known ? 0xFF9B6B8C : 0xFF66485A;
            graphics.fill(x - 25, y - 13, x + 25, y + 13, 0xDD241829);
            graphics.renderOutline(x - 25, y - 13, 50, 26, color);
            graphics.drawCenteredString(font, Component.literal(equipped ? "【已装备】" : known ? "【神通】" : "未学"), x, y - 8, color);
            if (known) graphics.drawCenteredString(font, DivineArtCatalog.name(id), x, y + 3, 0xFFF4D9EA);
        }
    }

    private int findHoveredDivine(CultivationData data, int mouseX, int mouseY, int centerX, int centerY, int radius) {
        if (data == null) return -1;
        String[] equipped = divineSlots(data);
        int best = -1;
        for (int i = 0; i < 3; i++) {
            if (i >= equipped.length || !DivineArtCatalog.isValid(equipped[i])) continue;
            double angle = -Math.PI / 2.0D + i * Math.PI * 2.0D / 3.0D;
            double x = centerX + Math.cos(angle) * radius;
            double y = centerY + Math.sin(angle) * radius;
            if (Math.hypot(mouseX - x, mouseY - y) <= 30.0D) { best = i; break; }
        }
        return best;
    }

    private void drawDetail(GuiGraphics graphics, CultivationData data, int centerX, int centerY, int radius) {
        int panelWidth = Math.min(390, width - 16);
        int panelHeight = hoveredDivine >= 0 ? 126 : 94;
        int panelX = centerX - panelWidth / 2;
        int panelY = Math.min(height - panelHeight - 8, centerY + radius + 24);
        graphics.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, PANEL_ALT);
        graphics.renderOutline(panelX, panelY, panelWidth, panelHeight, 0x997F5A42);
        if (data == null) {
            graphics.drawCenteredString(font, Component.translatable("gui.wuxingxiantu.art_radial.none_selected"), centerX, panelY + 25, MUTED);
            return;
        }
        if (hoveredDivine >= 0) {
            drawDivineDetail(graphics, data, panelX, panelY, panelWidth);
            return;
        }
        if (hoveredSlot < 0) {
            graphics.drawCenteredString(font, Component.translatable("gui.wuxingxiantu.art_radial.none_selected"), centerX, panelY + 25, MUTED);
            return;
        }
        String[] arts = releaseSlots(data);
        String art = hoveredSlot < arts.length ? arts[hoveredSlot] : null;
        if (!ArtCatalog.isValid(art)) {
            graphics.drawCenteredString(font, Component.translatable("gui.wuxingxiantu.art_radial.empty_selected"), centerX, panelY + 25, MUTED);
            return;
        }
        int mastery = data.wuxingxiantu$getArtMastery(art);
        int left = panelX + 10;
        graphics.drawString(font, ArtCatalog.artName(art), left, panelY + 8, ArtCatalog.tierColor(ArtCatalog.tier(art)), false);
        graphics.drawString(font, Component.translatable("gui.wuxingxiantu.art_radial.tier_type",
                ArtCatalog.tierName(ArtCatalog.tier(art)), ArtCatalog.type(art).typeName()), left, panelY + 23, MUTED, false);
        graphics.drawString(font, ArtCatalog.manaCostDescription(art,data.wuxingxiantu$getMaxMana()), left, panelY + 38, GOLD, false);
        int barX = panelX + panelWidth - 114;
        int barY = panelY + 22;
        graphics.drawString(font, Component.translatable("gui.wuxingxiantu.art_radial.mastery", mastery), barX, panelY + 8, TEXT, false);
        graphics.fill(barX, barY, barX + 104, barY + 7, 0xFF1A2021);
        int fill = Math.round(100 * mastery / 100.0F);
        if (fill > 0) graphics.fill(barX + 1, barY + 1, barX + 1 + fill, barY + 6, ArtCatalog.masteryColor(mastery));
        graphics.renderOutline(barX, barY, 104, 7, 0xFF765D50);
        int descriptionY = panelY + 36;
        int descriptionWidth = panelX + panelWidth - barX - 10;
        int maxLines = Math.max(1, (panelHeight - 42) / 10);
        int line = 0;
        for (var textLine : font.split(ArtCatalog.function(art), Math.max(70, descriptionWidth))) {
            if (line++ >= maxLines) break;
            graphics.drawString(font, textLine, barX, descriptionY, MUTED, false);
            descriptionY += 10;
        }
    }

    private void drawDivineDetail(GuiGraphics graphics, CultivationData data, int panelX, int panelY, int panelWidth) {
        String[] equipped = divineSlots(data);
        String divine = hoveredDivine < equipped.length ? equipped[hoveredDivine] : null;
        if (!DivineArtCatalog.isValid(divine)) {
            graphics.drawCenteredString(font, Component.translatable("gui.wuxingxiantu.art_radial.empty_selected"),
                    panelX + panelWidth / 2, panelY + 25, MUTED);
            return;
        }
        int left = panelX + 10;
        graphics.drawString(font, DivineArtCatalog.labeledName(divine), left, panelY + 8, 0xFFFF69C8, false);
        graphics.drawString(font, Component.translatable("gui.wuxingxiantu.art_radial.divine_realm",
                DivineArtCatalog.requiredRealm(divine) + 1), left, panelY + 23, MUTED, false);
        graphics.drawString(font, Component.translatable("gui.wuxingxiantu.art_radial.divine_cost",
                DivineArtCatalog.costText(divine)), left, panelY + 38, GOLD, false);
        graphics.drawString(font, Component.translatable("gui.wuxingxiantu.art_radial.divine_cooldown",
                DivineArtCatalog.cooldownText(divine)), left, panelY + 53, GOLD, false);
        graphics.drawString(font, Component.translatable("gui.wuxingxiantu.art_radial.divine_effect"), left, panelY + 68, 0xFFFFB4E4, false);
        int lineY = panelY + 82;
        int maxLines = Math.max(1, (panelY + 126 - 8 - lineY) / 10);
        int line = 0;
        for (var textLine : font.split(DivineArtCatalog.function(divine), panelWidth - 20)) {
            if (line++ >= maxLines) break;
            graphics.drawString(font, textLine, left, lineY, TEXT, false);
            lineY += 10;
        }
    }

    private int findHoveredSlot(CultivationData data, int unlocked, int mouseX, int mouseY,
                                int centerX, int centerY, int radius) {
        if (data == null) return -1;
        String[] arts = releaseSlots(data);
        int best = -1;
        double bestDistance = Double.MAX_VALUE;
        for (int slot = 0; slot < Math.min(8, unlocked); slot++) {
            if (slot >= arts.length || !ArtCatalog.isValid(arts[slot])) continue;
            double angle = -Math.PI / 2.0D + slot * Math.PI / 4.0D;
            double nodeX = centerX + Math.cos(angle) * radius;
            double nodeY = centerY + Math.sin(angle) * radius;
            double distance = Math.hypot(mouseX - nodeX, mouseY - nodeY);
            if (distance <= NODE_SIZE * 0.70D && distance < bestDistance) {
                best = slot;
                bestDistance = distance;
            }
        }
        return best;
    }

    private String[] divineSlots(CultivationData data) {
        String[] equipped = data == null ? new String[3] : data.wuxingxiantu$getEquippedDivineArts();
        if (data == null) return equipped;
        String[] learned = data.wuxingxiantu$getLearnedDivineArts();
        for (int i = 0; i < Math.min(3, learned.length); i++) {
            if (!DivineArtCatalog.isValid(equipped[i])) equipped[i] = learned[i];
        }
        return equipped;
    }

    private ItemStack artStack(String id) {
        return new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation(GeneratedMod.MOD_ID, id)));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
