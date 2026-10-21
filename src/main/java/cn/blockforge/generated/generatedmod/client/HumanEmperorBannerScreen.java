package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.HumanEmperorBannerMenu;
import cn.blockforge.generated.generatedmod.Network;
import cn.blockforge.generated.generatedmod.items.HumanEmperorBannerItem;
import cn.blockforge.generated.generatedmod.SoulPearlData;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;

/** 人皇幡界面：左侧为幡内魂魄，右上为万魂珠魂魄列表，右下为引渡操作和背包。 */
public final class HumanEmperorBannerScreen extends AbstractContainerScreen<HumanEmperorBannerMenu> {
    private static final int WIDTH = HumanEmperorBannerMenu.WIDTH;
    private static final int HEIGHT = HumanEmperorBannerMenu.HEIGHT;
    private static final int LIST_X = HumanEmperorBannerMenu.LIST_X;
    private static final int LIST_Y = HumanEmperorBannerMenu.LIST_Y;
    private static final int LIST_W = HumanEmperorBannerMenu.LIST_W;
    private static final int LIST_H = HumanEmperorBannerMenu.LIST_H;
    private static final int ACTION_X = HumanEmperorBannerMenu.ACTION_X;
    private static final int ACTION_W = HumanEmperorBannerMenu.ACTION_W;
    private static final int SOUL_ROW_Y = 72;
    private static final int SOUL_ROW_HEIGHT = 18;
    private static final int VISIBLE_ROWS = 12;
    private static final int MYRIAD_ROW_Y = 72;
    private static final int MYRIAD_ROW_HEIGHT = 18;
    private static final int MYRIAD_VISIBLE_ROWS = 5;
    private static final int INVENTORY_LABEL_Y = 224;
    /** 出战/守卫 状态图标：出战为交叉双剑 ⚔，守卫为盾牌上的十字 ⛨。 */
    private static final String ICON_ATTACK = "\u2694";
    private static final String ICON_DEFENSE = "\u26E8";

    private List<SoulView> souls = new ArrayList<>();
    private List<SoulView> myriadSouls = new ArrayList<>();
    private int selected = -1;
    private int myriadSelected = -1;
    private int listScroll;
    private int myriadScroll;
    private int soulPower = HumanEmperorBannerItem.MAX_SOUL_POWER;

    public HumanEmperorBannerScreen(HumanEmperorBannerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = WIDTH;
        imageHeight = HEIGHT;
        titleLabelX = 12;
        titleLabelY = 9;
    }

    @Override
    protected void init() {
        super.init();
        refreshFromMenu();
    }

    private void refreshFromMenu() {
        var state = menu.state();
        souls = views(state.souls());
        myriadSouls = views(state.myriadSouls());
        soulPower = state.soulPower();
        listScroll = Math.min(listScroll, maxListScroll());
        myriadScroll = Math.min(myriadScroll, maxMyriadScroll());
        if (selected >= souls.size()) selected = -1;
        if (myriadSelected >= myriadSouls.size()) myriadSelected = -1;
    }

    private static List<SoulView> views(List<HumanEmperorBannerItem.SoulView> source) {
        List<SoulView> result = new ArrayList<>();
        for (var soul : source) {
            result.add(new SoulView(soul.entityType(), soul.count(), soul.active(), soul.dead(),
                    soul.displayName(), soul.combatMode()));
        }
        return result;
    }

    private int maxListScroll() {
        return Math.max(0, souls.size() - VISIBLE_ROWS);
    }

    private int maxMyriadScroll() {
        return Math.max(0, myriadSouls.size() - MYRIAD_VISIBLE_ROWS);
    }

    private boolean myriadTool() {
        return SoulPearlData.isMyriad(menu.stateTransferTool());
    }

    private boolean releaseMode() {
        return myriadTool() && SoulPearlData.isMyriadReleaseMode(menu.stateTransferTool());
    }

    private void action(int action, int index) {
        Network.CHANNEL.sendToServer(new cn.blockforge.generated.generatedmod.network.packet.cultivation.BannerActionPacket(menu.containerId, action, index));
    }

    private void transfer() {
        int index = releaseMode() ? souls.size() + myriadSelected : selected;
        if (index >= 0 && !menu.isTransitReady()) action(HumanEmperorBannerItem.ACTION_TRANSFER, index);
    }

    public static void receiveState(int containerId, String[] types, int[] counts, int[] active, int[] dead,
                                    int[] modes, String[] names, String[] myriadTypes, int[] myriadCounts,
                                    int[] myriadActive, int[] myriadDead, int[] myriadModes, String[] myriadNames,
                                    boolean transitReady, int soulPower, int combatMode) {
        var screen = net.minecraft.client.Minecraft.getInstance().screen;
        if (!(screen instanceof HumanEmperorBannerScreen banner) || banner.menu.containerId != containerId) return;
        banner.menu.setTransitReady(transitReady);
        banner.soulPower = soulPower;
        banner.souls = decodeViews(types, counts, active, dead, modes, names);
        banner.myriadSouls = decodeViews(myriadTypes, myriadCounts, myriadActive, myriadDead, myriadModes, myriadNames);
        banner.listScroll = Math.min(banner.listScroll, banner.maxListScroll());
        banner.myriadScroll = Math.min(banner.myriadScroll, banner.maxMyriadScroll());
        if (banner.selected >= banner.souls.size()) banner.selected = -1;
        if (banner.myriadSelected >= banner.myriadSouls.size()) banner.myriadSelected = -1;
    }

    private static List<SoulView> decodeViews(String[] types, int[] counts, int[] active, int[] dead,
                                                int[] modes, String[] names) {
        List<SoulView> result = new ArrayList<>();
        for (int i = 0; i < types.length && i < counts.length; i++) {
            result.add(new SoulView(types[i], counts[i], i < active.length ? active[i] : 0,
                    i < dead.length ? dead[i] : 0,
                    i < names.length && names[i] != null ? names[i] : "",
                    i < modes.length ? modes[i] : HumanEmperorBannerItem.MODE_DEFENSE));
        }
        return result;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + WIDTH, topPos + HEIGHT, 0xF0141020);
        graphics.renderOutline(leftPos, topPos, WIDTH, HEIGHT, 0xFFD19A4A);
        graphics.renderOutline(leftPos + 4, topPos + 4, WIDTH - 8, HEIGHT - 8, 0xFF5D3E73);

        graphics.fill(leftPos + LIST_X, topPos + LIST_Y,
                leftPos + LIST_X + LIST_W, topPos + LIST_Y + LIST_H, 0xD020182E);
        graphics.fill(leftPos + ACTION_X, topPos + LIST_Y,
                leftPos + ACTION_X + ACTION_W, topPos + LIST_Y + LIST_H, 0xD01B2435);
        graphics.renderOutline(leftPos + LIST_X, topPos + LIST_Y, LIST_W, LIST_H, 0xFF25263A);
        graphics.renderOutline(leftPos + ACTION_X, topPos + LIST_Y, ACTION_W, LIST_H, 0xFF25263A);

        graphics.drawString(font, Component.translatable("gui.wuxingxiantu.banner_souls_title"),
                leftPos + 20, topPos + 42, 0xFFFFD98A, false);
        graphics.drawString(font, Component.translatable("gui.wuxingxiantu.banner_order_hint"),
                leftPos + 20, topPos + 57, 0xFFBDAED0, false);
        if (!myriadTool()) {
            drawAction(graphics, 128, 35, 38, 22, "↑", HumanEmperorBannerItem.ACTION_MOVE_UP, selected > 0);
            drawAction(graphics, 174, 35, 38, 22, "↓", HumanEmperorBannerItem.ACTION_MOVE_DOWN,
                    selected >= 0 && selected < souls.size() - 1);
        }
        drawBannerSoulList(graphics);

        graphics.drawString(font, Component.translatable("gui.wuxingxiantu.banner_spirit_cross_title"),
                leftPos + ACTION_X + 10, topPos + 42, 0xFFFFD98A, false);
        drawMyriadList(graphics);
        drawSlot(graphics, leftPos + HumanEmperorBannerMenu.PEARL_X, topPos + HumanEmperorBannerMenu.PEARL_Y);

        boolean myriad = myriadTool();
        boolean canAssign = menu.hasBannerSoul(selected) && !myriad;

        boolean transferEnabled = myriad
                ? (releaseMode() ? myriadSelected >= 0 : selected >= 0) && !menu.isTransitReady()
                : selected >= 0 && !menu.isTransitReady();
        drawAction(graphics, ACTION_X + 4, 168, 74, 22, "gui.wuxingxiantu.banner_transfer",
                HumanEmperorBannerItem.ACTION_TRANSFER, transferEnabled);
        String transferHint = myriad
                ? (releaseMode() ? "gui.wuxingxiantu.banner_myriad_release_hint" : "gui.wuxingxiantu.banner_myriad_absorb_hint")
                : "gui.wuxingxiantu.banner_spirit_cross_hint";
        drawWrapped(graphics, Component.translatable(transferHint),
                leftPos + ACTION_X + 84, topPos + 168, ACTION_W - 94, 0xFFBDAED0);

        SoulView sel = selected >= 0 && selected < souls.size() ? souls.get(selected) : null;
        String modeLabel = (sel != null && canAssign)
                ? (sel.combatMode() == HumanEmperorBannerItem.MODE_ATTACK ? ICON_ATTACK : ICON_DEFENSE)
                : "gui.wuxingxiantu.banner_mode_compact";
        drawAction(graphics, ACTION_X + 4, 196, 54, 22, modeLabel,
                HumanEmperorBannerItem.ACTION_TOGGLE_COMBAT, canAssign);
        drawAction(graphics, ACTION_X + 62, 196, 54, 22, "gui.wuxingxiantu.banner_soul_compact",
                HumanEmperorBannerItem.ACTION_TOGGLE_DEPLOY, canAssign);
        drawAction(graphics, ACTION_X + 120, 196, 54, 22,
                allDeployed() ? "gui.wuxingxiantu.banner_all_recall" : "gui.wuxingxiantu.banner_all_release",
                HumanEmperorBannerItem.ACTION_ALL, !myriad && !souls.isEmpty());
        drawInventorySlots(graphics);
    }

    private void drawBannerSoulList(GuiGraphics graphics) {
        graphics.enableScissor(leftPos + LIST_X, topPos + SOUL_ROW_Y - 4,
                leftPos + LIST_X + LIST_W, topPos + LIST_Y + LIST_H - 8);
        for (int row = 0; row < VISIBLE_ROWS && row + listScroll < souls.size(); row++) {
            SoulView view = souls.get(row + listScroll);
            int index = row + listScroll;
            int y = topPos + SOUL_ROW_Y + row * SOUL_ROW_HEIGHT;
            boolean isSelected = index == selected;
            boolean dead = view.dead() > 0;
            boolean active = view.active() > 0;
            // 出战行：先铺一层绿色底（置于最底层，不覆盖“选中”紫色高亮与“战死”红色标记）
            if (active) graphics.fill(leftPos + 16, y - 3, leftPos + 213, y + 14, 0xFF1F6B3A);
            if (isSelected) graphics.fill(leftPos + 16, y - 3, leftPos + 213, y + 14,
                    dead ? 0xFF74313B : 0xFF4C356B);
            if (dead) graphics.fill(leftPos + 16, y - 3, leftPos + 19, y + 14, 0xFFE04A5F);
            int textColor = dead ? 0xFFFF8A8A : (active ? 0xFF8CE6A0 : 0xFFE4D7F0);
            graphics.drawString(font, Component.literal((index + 1) + "."), leftPos + 21, y, textColor, false);
            graphics.drawString(font, clipped(displayName(view), 88), leftPos + 42, y, textColor, false);
            String status = dead ? Component.translatable("gui.wuxingxiantu.banner_dead_count", view.dead()).getString()
                    : (view.combatMode() == HumanEmperorBannerItem.MODE_ATTACK ? ICON_ATTACK : ICON_DEFENSE);
            graphics.drawString(font, Component.literal(font.plainSubstrByWidth(status, 48)), leftPos + 134, y,
                    dead ? 0xFFFF6B76 : 0xFF8CE6C0, false);
            String count = "x" + view.count();
            graphics.drawString(font, Component.literal(count), leftPos + 196 - font.width(count), y,
                    0xFFFFD98A, false);
        }
        graphics.disableScissor();
        if (souls.size() > VISIBLE_ROWS) drawScrollTrack(graphics);
    }

    private void drawMyriadList(GuiGraphics graphics) {
        int x = HumanEmperorBannerMenu.MYRIAD_LIST_X;
        int y = HumanEmperorBannerMenu.MYRIAD_LIST_Y;
        graphics.fill(leftPos + x, topPos + y, leftPos + x + HumanEmperorBannerMenu.MYRIAD_LIST_W,
                topPos + y + HumanEmperorBannerMenu.MYRIAD_LIST_H, 0xD020182E);
        graphics.renderOutline(leftPos + x, topPos + y, HumanEmperorBannerMenu.MYRIAD_LIST_W,
                HumanEmperorBannerMenu.MYRIAD_LIST_H, 0xFF4C356B);
        Component mode = Component.translatable(releaseMode()
                ? "gui.wuxingxiantu.banner_myriad_release" : "gui.wuxingxiantu.banner_myriad_absorb");
        graphics.drawString(font, clipped(mode, 116), leftPos + x + 5, topPos + y + 3, 0xFFD6B4F2, false);
        for (int row = 0; row < MYRIAD_VISIBLE_ROWS && row + myriadScroll < myriadSouls.size(); row++) {
            SoulView view = myriadSouls.get(row + myriadScroll);
            int index = row + myriadScroll;
            int rowY = topPos + MYRIAD_ROW_Y + row * MYRIAD_ROW_HEIGHT;
            boolean isSelected = releaseMode() && index == myriadSelected;
            if (isSelected) graphics.fill(leftPos + x + 3, rowY - 2, leftPos + x + 123, rowY + 15, 0xFF4C356B);
            graphics.drawString(font, clipped(displayName(view), 82), leftPos + x + 6, rowY,
                    view.dead() > 0 ? 0xFFFF8A8A : 0xFFE4D7F0, false);
            String count = "x" + view.count();
            graphics.drawString(font, Component.literal(count), leftPos + x + 120 - font.width(count), rowY,
                    0xFFFFD98A, false);
        }
        if (myriadSouls.isEmpty()) {
            graphics.drawString(font, Component.translatable("gui.wuxingxiantu.banner_myriad_empty"),
                    leftPos + x + 5, topPos + y + 42, 0xFF8E849A, false);
        } else if (myriadSouls.size() > MYRIAD_VISIBLE_ROWS) {
            int trackTop = topPos + MYRIAD_ROW_Y - 3;
            int trackHeight = MYRIAD_VISIBLE_ROWS * MYRIAD_ROW_HEIGHT;
            int thumbHeight = Math.max(10, trackHeight * MYRIAD_VISIBLE_ROWS / myriadSouls.size());
            int thumbTop = trackTop + (trackHeight - thumbHeight) * myriadScroll / maxMyriadScroll();
            CultivationUi.scrollbar(graphics,leftPos+x+124,trackTop,trackHeight,thumbTop,thumbHeight);
        }
    }

    private void drawScrollTrack(GuiGraphics graphics) {
        int trackTop = topPos + SOUL_ROW_Y - 4;
        int trackHeight = VISIBLE_ROWS * SOUL_ROW_HEIGHT;
        int thumbHeight = Math.max(18, trackHeight * VISIBLE_ROWS / souls.size());
        int thumbTop = trackTop + (trackHeight - thumbHeight) * listScroll / maxListScroll();
        CultivationUi.scrollbar(graphics,leftPos+LIST_X+LIST_W-6,trackTop,trackHeight,thumbTop,thumbHeight);
    }

    private void drawInventorySlots(GuiGraphics graphics) {
        int inventoryLeft = leftPos + HumanEmperorBannerMenu.INVENTORY_X;
        int inventoryTop = topPos + HumanEmperorBannerMenu.INVENTORY_Y;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) drawSlot(graphics, inventoryLeft + col * 18, inventoryTop + row * 18);
        }
        for (int col = 0; col < 9; col++) drawSlot(graphics, inventoryLeft + col * 18,
                topPos + HumanEmperorBannerMenu.HOTBAR_Y);
    }

    private static void drawSlot(GuiGraphics graphics, int x, int y) {
        graphics.fill(x - 1, y - 1, x + 17, y + 17, 0xFF111622);
        graphics.renderOutline(x - 1, y - 1, 18, 18, 0xFF3B4156);
    }

    private void drawAction(GuiGraphics graphics, int x, int y, int width, int height,
                            String label, int action, boolean enabled) {
        int color = enabled ? 0xFF342A49 : 0xFF211D2A;
        int border = enabled ? 0xFF9C6BC9 : 0xFF4A4651;
        graphics.fill(leftPos + x, topPos + y, leftPos + x + width, topPos + y + height, color);
        graphics.renderOutline(leftPos + x, topPos + y, width, height, border);
        Component text = label.startsWith("gui.") ? Component.translatable(label) : Component.literal(label);
        text = Component.literal(font.plainSubstrByWidth(text.getString(), Math.max(4, width - 8)));
        int textWidth = font.width(text);
        graphics.drawString(font, text, leftPos + x + Math.max(4, (width - textWidth) / 2),
                topPos + y + (height - 8) / 2, enabled ? 0xFFF0E5FF : 0xFF77727F, false);
    }

    private void drawWrapped(GuiGraphics graphics, Component text, int x, int y, int width, int color) {
        int lineY = y;
        for (var line : font.split(text, width)) {
            graphics.drawString(font, line, x, lineY, color);
            lineY += 10;
        }
    }

    private Component clipped(Component text, int width) {
        return Component.literal(font.plainSubstrByWidth(text.getString(), width));
    }

    private boolean allDeployed() {
        return souls.stream().anyMatch(soul -> soul.active() > 0);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);
        int x = (int) mouseX - leftPos;
        int y = (int) mouseY - topPos;
        if (x >= 16 && x < 215 && y >= SOUL_ROW_Y - 5 && y < SOUL_ROW_Y + VISIBLE_ROWS * SOUL_ROW_HEIGHT) {
            int row = (y - (SOUL_ROW_Y - 5)) / SOUL_ROW_HEIGHT;
            int index = row + listScroll;
            selected = index >= 0 && index < souls.size() ? index : -1;
            if (!releaseMode()) myriadSelected = -1;
            return true;
        }
        if (hit(x, y, HumanEmperorBannerMenu.MYRIAD_LIST_X, MYRIAD_ROW_Y - 5,
                HumanEmperorBannerMenu.MYRIAD_LIST_W, MYRIAD_VISIBLE_ROWS * MYRIAD_ROW_HEIGHT)) {
            int row = (y - (MYRIAD_ROW_Y - 5)) / MYRIAD_ROW_HEIGHT;
            int index = row + myriadScroll;
            myriadSelected = index >= 0 && index < myriadSouls.size() ? index : -1;
            if (releaseMode()) selected = -1;
            return true;
        }
        boolean myriad = myriadTool();
        boolean canAssign = menu.hasBannerSoul(selected) && !myriad;
        if (!myriad && hit(x, y, 128, 35, 38, 22) && selected > 0) {
            action(HumanEmperorBannerItem.ACTION_MOVE_UP, selected); return true;
        }
        if (!myriad && hit(x, y, 174, 35, 38, 22) && selected >= 0 && selected < souls.size() - 1) {
            action(HumanEmperorBannerItem.ACTION_MOVE_DOWN, selected); return true;
        }
        if (hit(x, y, ACTION_X + 4, 168, 74, 22)) {
            transfer();
            return true;
        }
        if (hit(x, y, ACTION_X + 4, 196, 54, 22)) {
            if (canAssign) action(HumanEmperorBannerItem.ACTION_TOGGLE_COMBAT, selected);
            return true;
        }
        if (hit(x, y, ACTION_X + 62, 196, 54, 22)) {
            if (canAssign) action(HumanEmperorBannerItem.ACTION_TOGGLE_DEPLOY, selected);
            return true;
        }
        if (hit(x, y, ACTION_X + 120, 196, 54, 22)) {
            if (!myriad && !souls.isEmpty()) action(HumanEmperorBannerItem.ACTION_ALL, -1);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int x = (int) mouseX - leftPos;
        int y = (int) mouseY - topPos;
        if (x >= LIST_X && x < LIST_X + LIST_W && y >= LIST_Y && y < LIST_Y + LIST_H) {
            listScroll = Math.max(0, Math.min(maxListScroll(), listScroll - (int) Math.signum(delta)));
            return true;
        }
        if (x >= HumanEmperorBannerMenu.MYRIAD_LIST_X
                && x < HumanEmperorBannerMenu.MYRIAD_LIST_X + HumanEmperorBannerMenu.MYRIAD_LIST_W
                && y >= HumanEmperorBannerMenu.MYRIAD_LIST_Y
                && y < HumanEmperorBannerMenu.MYRIAD_LIST_Y + HumanEmperorBannerMenu.MYRIAD_LIST_H) {
            myriadScroll = Math.max(0, Math.min(maxMyriadScroll(), myriadScroll - (int) Math.signum(delta)));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    private static boolean hit(int x, int y, int left, int top, int width, int height) {
        return x >= left && x < left + width && y >= top && y < top + height;
    }

    private static Component displayName(SoulView soul) {
        if (soul.type().equals("wuxingxiantu:heart_demon") && !soul.displayName().isEmpty()) {
            return Component.translatable("entity.wuxingxiantu.heart_demon", Component.literal(soul.displayName()));
        }
        ResourceLocation key = ResourceLocation.tryParse(soul.type());
        var type = key == null ? null : BuiltInRegistries.ENTITY_TYPE.get(key);
        return type == null ? Component.literal(soul.type()) : type.getDescription();
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, 0xFFFFD98A, false);
        graphics.drawString(font, Component.translatable("gui.wuxingxiantu.banner_soul_power_value",
                soulPower, HumanEmperorBannerItem.MAX_SOUL_POWER), 12, 20, 0xFF9CE6D0, false);
        graphics.fill(12, 31, 213, 33, 0xFF20182E);
        graphics.fill(12, 31, 12 + (int) (201.0D * soulPower / HumanEmperorBannerItem.MAX_SOUL_POWER),
                33, 0xFF57C7A3);
        graphics.drawString(font, Component.translatable("gui.wuxingxiantu.banner_inventory_main"),
                HumanEmperorBannerMenu.INVENTORY_X, INVENTORY_LABEL_Y, 0xFFBDAED0, false);
        CultivationUi.softDivider(graphics,HumanEmperorBannerMenu.INVENTORY_X,
                HumanEmperorBannerMenu.HOTBAR_Y-6,9*18-1,false,0x7430364B);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean isPauseScreen() { return false; }

    private record SoulView(String type, int count, int active, int dead, String displayName, int combatMode) {
        private SoulView {
            displayName = displayName == null ? "" : displayName;
            combatMode = combatMode == HumanEmperorBannerItem.MODE_ATTACK
                    ? HumanEmperorBannerItem.MODE_ATTACK : HumanEmperorBannerItem.MODE_DEFENSE;
        }
    }
}
