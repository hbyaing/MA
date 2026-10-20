package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.ArtCatalog;
import cn.blockforge.generated.generatedmod.CultivationData;
import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.HeartMethodCatalog;
import cn.blockforge.generated.generatedmod.Network;
import cn.blockforge.generated.generatedmod.items.FireSeedItem;
import cn.blockforge.generated.generatedmod.items.CultivationLogic;
import cn.blockforge.generated.generatedmod.items.SpiritSwordItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.BooleanSupplier;

/** 半透明水墨风内视面板：显示修炼状态、灵根、体质、功法、心法与领域。 */
public final class InnerVisionScreen extends Screen {
    private static final int INK = 0xE8171A20;
    private static final int PANEL = 0xD92B2525;
    private static final int PANEL_ALT = 0xB83A2B29;
    private static final int GOLD = 0xFFE8C477;
    private static final int TEXT = 0xFFF4E8D0;
    private static final int MUTED = 0xFFC8B99C;
    private static final int CYAN = 0xFF8EE7E8;
    private static final int RED = 0xFFDE837A;
    private static final int[] ROOT_COLORS = {0xFFE5C76B, 0xFF79C58B, 0xFF659BD0, 0xFFE87963, 0xFFC28B65};
    /** 混沌灵根的色块与文字：紫色系，与五行区分。 */
    private static final int CHAOS_ROOT_COLOR = 0xFFB48EE8;
    private static final int CHAOS_ROOT_TEXT = 0xFFDCCBFA;
    /** 变异上位灵根的显示配色：天雷为电光蓝，异火为焚金橙。 */
    private static final int THUNDER_ROOT_COLOR = 0xFF8FC7FF;
    private static final int THUNDER_ROOT_TEXT = 0xFFD6ECFF;
    private static final int EXOTIC_ROOT_COLOR = 0xFFFF9A45;
    private static final int EXOTIC_ROOT_TEXT = 0xFFFFE0BC;

    private Button woodDomainButton;
    private Button fireDomainButton;
    private Button goldDomainButton;
    private Button waterDomainButton;
    private Button earthDomainButton;
    private Button artWheelButton;
    private Button heartMethodButton;
    private Button tribulationButton;
    private Button crystallizeButton;
    private final Button[] fireButtons = new Button[9];
    private int breakthroughMarkerX;
    private int breakthroughMarkerY;
    private int breakthroughMarkerWidth;
    /** 变异灵根/领域切换热区：每帧渲染时重建，mouseClicked 在按钮之前拦截。 */
    private final java.util.List<SwitchTarget> switchTargets = new java.util.ArrayList<>();
    /** 文字悬停详情：每帧渲染时重建，鼠标停在文字上时显示对应说明。 */
    private final java.util.List<HoverTip> hoverTips = new java.util.ArrayList<>();
    private int lastMouseX;
    private int lastMouseY;

    private record SwitchTarget(int x, int y, int width, int height, int form, Component tooltip) {}
    private record HoverTip(int x, int y, int width, int height, Component tooltip) {}

    public InnerVisionScreen() {
        super(Component.translatable("gui.wuxingxiantu.inner_vision.title"));
    }

    @Override
    protected void init() {
        woodDomainButton = null;
        fireDomainButton = null;
        goldDomainButton = null;
        waterDomainButton = null;
        earthDomainButton = null;
        artWheelButton = null;
        heartMethodButton = null;
        tribulationButton = null;
        crystallizeButton = null;
        java.util.Arrays.fill(fireButtons, null);
        breakthroughMarkerX = breakthroughMarkerY = breakthroughMarkerWidth = 0;
        CultivationData data = GeneratedMod.data(minecraft.player);
        DomainBounds domains = domainBounds(data);
        if (domains != null && domains.width() >= 70 && domains.height() >= 48) {
            int count = domainCount(data);
            int columns = domainColumns(data, domains.width());
            int toggleGap = 4;
            int toggleWidth = Math.max(48, (domains.width() - 16 - (columns - 1) * toggleGap) / columns);
            int index = 0;
            if (hasGoldDomain(data)) {
                goldDomainButton = addDomainButton(domains, index++, toggleWidth, "gold", goldDomainLabel(data));
            }
            if (hasWaterDomain(data)) {
                waterDomainButton = addDomainButton(domains, index++, toggleWidth, "water", waterDomainLabel(data));
            }
            if (hasEarthDomain(data)) {
                earthDomainButton = addDomainButton(domains, index++, toggleWidth, "earth", earthDomainLabel(data));
            }
            if (hasWoodDomain(data)) {
                woodDomainButton = addDomainButton(domains, index++, toggleWidth, "wood", woodDomainLabel(data));
            }
            if (hasFireDomain(data)) {
                fireDomainButton = addDomainButton(domains, index, toggleWidth, "fire", fireDomainLabel(data));
            }
        }

        ColumnBounds right = rightColumnBounds();
        int artsHeight = artsHeight(right, data);
        String heartMethod = data == null ? "" : data.wuxingxiantu$getEquippedHeartMethod();
        boolean hasHeart = heartMethod != null && !heartMethod.isBlank();
        int requiredHeight = artsHeight + (hasHeart ? heartHeight(right.compact()) + 6 : 0)
                + fireHeight(right.compact()) + 6 + 20;
        if (right.height() >= requiredHeight && right.width() >= 88) {
            int artButtonWidth = Math.min(132, Math.max(88, right.width() - 16));
            artWheelButton = addRenderableWidget(new InkButton(
                    right.x() + right.width() - artButtonWidth - 8, right.y() + artsHeight - 25,
                    artButtonWidth, 20,
                    Component.translatable("gui.wuxingxiantu.inner_vision.btn_art_wheel"),
                    button -> minecraft.setScreen(new ArtWheelScreen(this)), GOLD));

            if (hasHeart) {
                int heartY = right.y() + artsHeight + 6;
                int heartHeight = heartHeight(right.compact());
                int heartButtonWidth = Math.min(132, Math.max(88, right.width() - 16));
                heartMethodButton = addRenderableWidget(new InkButton(
                        right.x() + right.width() - heartButtonWidth - 8, heartY + heartHeight - 25,
                        heartButtonWidth, 20,
                        Component.translatable("gui.wuxingxiantu.inner_vision.btn_heart_method"),
                        button -> minecraft.setScreen(new HeartMethodScreen(this)), GOLD));
            }
            int fireY = right.y() + artsHeight + (hasHeart ? heartHeight(right.compact()) + 6 : 0) + 6;
            int fireButtonWidth = Math.max(48, (right.width() - 16 - 8) / 3);
            for (int tier = 1; tier <= 9; tier++) {
                final int selectedTier = tier;
                if (data != null && data.wuxingxiantu$hasLearnedFire(tier)) {
                    int column = (tier - 1) % 3;
                    int row = (tier - 1) / 3;
                    fireButtons[tier - 1] = addRenderableWidget(new InkButton(
                            right.x() + 8 + column * (fireButtonWidth + 4), fireY + 22 + row * 20,
                            fireButtonWidth, 18, fireLabel(data, tier),
                            button -> selectFire(selectedTier), 0xFFE87963));
                }
            }
        }

        ColumnBounds leftColumn = leftColumnBounds();
        int stateLeft = leftColumn.x() + 8;
        int barWidth = stateBarWidth(leftColumn.width());
        int actionX = stateLeft + barWidth + 4;
        int actionWidth = manaActionWidth(leftColumn.width());
        int line = leftColumn.y() + 22;
        int cultivationBarY = line + (leftColumn.compact() ? 9 : 10);
        int manaLabelY = line + (leftColumn.compact() ? 19 : 21);
        int manaBarY = manaLabelY + (leftColumn.compact() ? 9 : 10);
        tribulationButton = addRenderableWidget(new InkButton(actionX, cultivationBarY - 5, actionWidth, 16,
                Component.translatable("gui.wuxingxiantu.inner_vision.btn_tribulation"),
                button -> Network.CHANNEL.sendToServer(new cn.blockforge.generated.generatedmod.network.packet.cultivation.TribulationPacket()), GOLD));
        crystallizeButton = addRenderableWidget(new InkButton(actionX, manaBarY - 5, actionWidth, 16,
                Component.translatable("gui.wuxingxiantu.inner_vision.btn_crystallize"), button ->
                        Network.CHANNEL.sendToServer(new cn.blockforge.generated.generatedmod.network.packet.cultivation.CrystallizePacket(net.minecraft.client.gui.screens.Screen.hasControlDown())), CYAN));
        int actionY = panelBottom() - 26;
        int closeWidth = Math.min(112, Math.max(72, panelWidth() / 4));
        addRenderableWidget(new InkButton(panelLeft() + panelWidth() - closeWidth - 14, actionY, closeWidth, 20,
                Component.translatable("gui.wuxingxiantu.inner_vision.btn_close"), button -> onClose(), GOLD));
    }

    private boolean hasWoodDomain(CultivationData data) {
        return data != null && (data.wuxingxiantu$getRootMask() & (1 << 1)) != 0
                && data.wuxingxiantu$getRootQuality(1) >= 2;
    }

    private boolean hasFireDomain(CultivationData data) {
        return data != null && (data.wuxingxiantu$getRootMask() & (1 << 3)) != 0
                && data.wuxingxiantu$getRootQuality(3) >= 2;
    }

    private boolean hasGoldDomain(CultivationData data) {
        return data != null && (data.wuxingxiantu$getRootMask() & 1) != 0
                && data.wuxingxiantu$getRootQuality(0) >= 2;
    }

    private boolean hasWaterDomain(CultivationData data) {
        return data != null && (data.wuxingxiantu$getRootMask() & (1 << 2)) != 0
                && data.wuxingxiantu$getRootQuality(2) >= 2;
    }

    private boolean hasEarthDomain(CultivationData data) {
        return data != null && (data.wuxingxiantu$getRootMask() & (1 << 4)) != 0
                && data.wuxingxiantu$getRootQuality(4) >= 2;
    }

    private Component woodDomainLabel(CultivationData data) {
        return Component.translatable(CultivationLogic.isThunderForm(data)
                ? "gui.wuxingxiantu.inner_vision.thunder_domain_name"
                : "gui.wuxingxiantu.inner_vision.wood_domain_name");
    }

    private Component fireDomainLabel(CultivationData data) {
        return Component.translatable(CultivationLogic.isExoticForm(data)
                ? "gui.wuxingxiantu.inner_vision.exotic_domain_name"
                : "gui.wuxingxiantu.inner_vision.fire_domain_name");
    }

    private Component goldDomainLabel(CultivationData data) {
        return Component.translatable("gui.wuxingxiantu.inner_vision.gold_domain_name");
    }

    private Component waterDomainLabel(CultivationData data) {
        return Component.translatable("gui.wuxingxiantu.inner_vision.water_domain_name");
    }

    private Component earthDomainLabel(CultivationData data) {
        return Component.translatable("gui.wuxingxiantu.inner_vision.earth_domain_name");
    }

    private int domainCount(CultivationData data) {
        return (hasGoldDomain(data) ? 1 : 0) + (hasWaterDomain(data) ? 1 : 0)
                + (hasEarthDomain(data) ? 1 : 0) + (hasWoodDomain(data) ? 1 : 0)
                + (hasFireDomain(data) ? 1 : 0);
    }

    private int domainColumns(CultivationData data, int width) {
        int count = domainCount(data);
        if (count <= 1 || width < 120) return 1;
        if (count <= 2 || width < 190) return 2;
        return 3;
    }

    private Button addDomainButton(DomainBounds domains, int index, int buttonWidth, String domain, Component label) {
        CultivationData data = GeneratedMod.data(minecraft.player);
        int count = domainCount(data);
        int columns = domainColumns(data, domains.width());
        int rowCount = (count + columns - 1) / columns;
        int row = index / columns;
        int column = index % columns;
        int buttonHeight = 18;
        int rowGap = 4;
        int totalHeight = rowCount * buttonHeight + (rowCount - 1) * rowGap;
        int x = domains.x() + 8 + column * (buttonWidth + rowGap);
        int y = domains.y() + 20 + row * (buttonHeight + rowGap);
        BooleanSupplier enabled = () -> {
            CultivationData current = GeneratedMod.data(minecraft.player);
            if (current == null) return false;
            return switch (domain) {
                case "gold" -> current.wuxingxiantu$isGoldDomainEnabled();
                case "water" -> current.wuxingxiantu$isWaterDomainEnabled();
                case "earth" -> current.wuxingxiantu$isEarthDomainEnabled();
                case "wood" -> current.wuxingxiantu$isWoodDomainEnabled();
                case "fire" -> current.wuxingxiantu$isFireDomainEnabled();
                default -> false;
            };
        };
        int root=switch(domain){case "gold"->0;case "wood"->1;case "water"->2;case "fire"->3;default->4;};
        var widget=new DomainButton(x, y, buttonWidth, buttonHeight, label,
                button -> toggleDomain(domain), enabled, domainColor(domain));
        widget.root=root;
        return addRenderableWidget(widget);
    }

    /** 领域色条：木/火灵根显化出天雷/异火形态时换成对应的变异颜色，其余按五行本色。 */
    private int domainAccent(String domain, CultivationData data) {
        if ("wood".equals(domain) && CultivationLogic.isThunderForm(data)) return THUNDER_ROOT_COLOR;
        if ("fire".equals(domain) && CultivationLogic.isExoticForm(data)) return EXOTIC_ROOT_COLOR;
        return domainColor(domain);
    }

    private int domainColor(String domain) {
        return switch (domain) {
            case "gold" -> 0xFFE5C76B;
            case "wood" -> 0xFF79C58B;
            case "water" -> 0xFF659BD0;
            case "fire" -> 0xFFE87963;
            case "earth" -> 0xFFC28B65;
            default -> GOLD;
        };
    }

    private void toggleDomain(String domain) {
        CultivationData data = GeneratedMod.data(minecraft.player);
        if (data == null) return;
        boolean enabled = switch (domain) {
            case "gold" -> !data.wuxingxiantu$isGoldDomainEnabled();
            case "water" -> !data.wuxingxiantu$isWaterDomainEnabled();
            case "earth" -> !data.wuxingxiantu$isEarthDomainEnabled();
            case "wood" -> !data.wuxingxiantu$isWoodDomainEnabled();
            case "fire" -> !data.wuxingxiantu$isFireDomainEnabled();
            default -> true;
        };
        switch (domain) {
            case "gold" -> Network.CHANNEL.sendToServer(new cn.blockforge.generated.generatedmod.network.packet.cultivation.GoldDomainPacket(enabled));
            case "water" -> Network.CHANNEL.sendToServer(new cn.blockforge.generated.generatedmod.network.packet.cultivation.WaterDomainPacket(enabled));
            case "earth" -> Network.CHANNEL.sendToServer(new cn.blockforge.generated.generatedmod.network.packet.cultivation.EarthDomainPacket(enabled));
            case "wood" -> Network.CHANNEL.sendToServer(new cn.blockforge.generated.generatedmod.network.packet.cultivation.WoodDomainPacket(enabled));
            case "fire" -> Network.CHANNEL.sendToServer(new cn.blockforge.generated.generatedmod.network.packet.cultivation.FireDomainPacket(enabled));
            default -> { }
        }
    }

    private void toggleWoodDomain() {
        CultivationData data = GeneratedMod.data(minecraft.player);
        if (data != null) Network.CHANNEL.sendToServer(
                new cn.blockforge.generated.generatedmod.network.packet.cultivation.WoodDomainPacket(!data.wuxingxiantu$isWoodDomainEnabled()));
    }

    private void toggleFireDomain() {
        CultivationData data = GeneratedMod.data(minecraft.player);
        if (data != null) Network.CHANNEL.sendToServer(
                new cn.blockforge.generated.generatedmod.network.packet.cultivation.FireDomainPacket(!data.wuxingxiantu$isFireDomainEnabled()));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        lastMouseX = mouseX;
        lastMouseY = mouseY;
        switchTargets.clear();
        hoverTips.clear();
        renderBackground(graphics);
        int left = panelLeft();
        int top = panelTop();
        int width = panelWidth();
        int height = panelHeight();
        graphics.fill(0, 0, this.width, this.height, 0x35140D0C);
        graphics.fill(left - 3, top - 3, left + width + 3, top + height + 3, INK);
        graphics.fill(left, top, left + width, top + height, PANEL);
        graphics.renderOutline(left, top, width, height, 0xFFD19B56);
        graphics.renderOutline(left + 3, top + 3, width - 6, height - 6, 0x886A3A2D);
        graphics.drawString(font, title, left + 12, top + 10, GOLD, false);

        CultivationData data = GeneratedMod.data(minecraft.player);
        if (data != null) {
            if (woodDomainButton != null) woodDomainButton.setMessage(woodDomainLabel(data));
            if (fireDomainButton != null) fireDomainButton.setMessage(fireDomainLabel(data));
            if (goldDomainButton != null) goldDomainButton.setMessage(goldDomainLabel(data));
            if (waterDomainButton != null) waterDomainButton.setMessage(waterDomainLabel(data));
            if (earthDomainButton != null) earthDomainButton.setMessage(earthDomainLabel(data));
            if (woodDomainButton instanceof DomainButton woodToggle) {
                woodToggle.setAccent(domainAccent("wood", data));
            }
            if (fireDomainButton instanceof DomainButton fireToggle) {
                fireToggle.setAccent(domainAccent("fire", data));
            }
            for (int tier = 1; tier <= 9; tier++) {
                if (fireButtons[tier - 1] != null) fireButtons[tier - 1].setMessage(fireLabel(data, tier));
            }
        }
        if (data == null) {
            graphics.drawCenteredString(font, Component.translatable("gui.wuxingxiantu.inner_vision.no_data"),
                    this.width / 2, top + 70, RED);
            super.render(graphics, mouseX, mouseY, delta);
            return;
        }

        Component playerRealm = Component.translatable("gui.wuxingxiantu.inner_vision.player_realm", minecraft.player.getName(),
                Component.literal(CultivationLogic.realmStageName(minecraft.player, data)));
        graphics.drawString(font, playerRealm, left + width - 12 - font.width(playerRealm), top + 10, TEXT, false);

        ColumnBounds leftColumn = leftColumnBounds();
        ColumnBounds rightColumn = rightColumnBounds();
        drawLeftColumn(graphics, data, leftColumn.x(), leftColumn.y(), leftColumn.width(), leftColumn.height(), leftColumn.compact());
        // 领域开关紧随左栏灵根下方，不再占用右栏底部。
        DomainBounds domains = domainBounds(data);
        if (domains != null) {
            section(graphics, domains.x(), domains.y(), domains.width(), domains.height(),
                    "gui.wuxingxiantu.inner_vision.domains");
            drawDomainEntries(graphics, data, domains.x(), domains.y(), domains.width());
        }
        if (rightColumn.height() >= 44) {
            drawRightColumn(graphics, data, rightColumn.x(), rightColumn.y(), rightColumn.width(), rightColumn.height(), rightColumn.compact());
        }
        super.render(graphics, mouseX, mouseY, delta);
        if (breakthroughMarkerWidth > 0 && mouseX >= breakthroughMarkerX - 4
                && mouseX <= breakthroughMarkerX + 4 && mouseY >= breakthroughMarkerY - 3
                && mouseY <= breakthroughMarkerY + 10) {
            graphics.renderTooltip(font, Component.translatable(
                    "gui.wuxingxiantu.inner_vision.breakthrough_point"), mouseX, mouseY);
        }
        drawDomainSwitchIcons(graphics, data);
        for (HoverTip tip : hoverTips) {
            if (mouseX >= tip.x() && mouseX < tip.x() + tip.width()
                    && mouseY >= tip.y() && mouseY < tip.y() + tip.height()) {
                graphics.renderTooltip(font, tip.tooltip(), mouseX, mouseY);
                break;
            }
        }
        for (SwitchTarget target : switchTargets) {
            if (mouseX >= target.x() && mouseX < target.x() + target.width()
                    && mouseY >= target.y() && mouseY < target.y() + target.height()) {
                graphics.renderTooltip(font, target.tooltip(), mouseX, mouseY);
                break;
            }
        }
    }

    /** 在木灵/业火领域按钮右端绘制变异切换图标；领域与灵根共用同一个显化开关。 */
    private void drawDomainSwitchIcons(GuiGraphics graphics, CultivationData data) {
        if (data == null) return;
        if (woodDomainButton != null && woodDomainButton.active && CultivationLogic.thunderRootAvailable(data)) {
            boolean thunderForm = CultivationLogic.isThunderForm(data);
            drawSwitchIcon(graphics, woodDomainButton.getX() + woodDomainButton.getWidth() - 11,
                    woodDomainButton.getY() + 5);
            registerSwitchZone(woodDomainButton.getX() + woodDomainButton.getWidth() - 13,
                    woodDomainButton.getY() + 4, 13, 12, 0, Component.translatable(thunderForm
                            ? "gui.wuxingxiantu.inner_vision.switch_domain_to_wood"
                            : "gui.wuxingxiantu.inner_vision.switch_domain_to_thunder"));
        }
        if (fireDomainButton != null && fireDomainButton.active && CultivationLogic.exoticRootAvailable(data)) {
            boolean exoticForm = CultivationLogic.isExoticForm(data);
            drawSwitchIcon(graphics, fireDomainButton.getX() + fireDomainButton.getWidth() - 11,
                    fireDomainButton.getY() + 5);
            registerSwitchZone(fireDomainButton.getX() + fireDomainButton.getWidth() - 13,
                    fireDomainButton.getY() + 4, 13, 12, 1, Component.translatable(exoticForm
                            ? "gui.wuxingxiantu.inner_vision.switch_domain_to_fire"
                            : "gui.wuxingxiantu.inner_vision.switch_domain_to_exotic"));
        }
    }

    /** 变异切换图标：一上一下两条反向箭头（8×8），悬停时提亮。 */
    private void drawSwitchIcon(GuiGraphics graphics, int x, int y) {
        boolean hovered = lastMouseX >= x - 2 && lastMouseX <= x + 9 && lastMouseY >= y - 1 && lastMouseY <= y + 9;
        int color = hovered ? 0xFFFFD98A : 0xFFC8B99C;
        // 上箭头（左侧）
        graphics.fill(x + 1, y + 2, x + 4, y + 3, color);
        graphics.fill(x + 2, y + 1, x + 3, y + 2, color);
        graphics.fill(x + 2, y + 3, x + 3, y + 8, color);
        // 下箭头（右侧）
        graphics.fill(x + 5, y, x + 6, y + 5, color);
        graphics.fill(x + 4, y + 5, x + 7, y + 6, color);
        graphics.fill(x + 5, y + 6, x + 6, y + 7, color);
    }

    private void registerSwitchZone(int x, int y, int width, int height, int form, Component tooltip) {
        switchTargets.add(new SwitchTarget(x, y, width, height, form, tooltip));
    }

    /** 登记一块文字悬停区，渲染末尾统一显示详情。 */
    private void registerTip(int x, int y, int width, int height, String tooltipKey) {
        hoverTips.add(new HoverTip(x, y, Math.max(16, width), Math.max(10, height),
                Component.translatable(tooltipKey)));
    }

    /** 登记一块文字悬停区，tooltip 由调用方给定。 */
    private void registerTip(int x, int y, int width, int height, Component tooltip) {
        hoverTips.add(new HoverTip(x, y, Math.max(16, width), Math.max(10, height), tooltip));
    }

    /** 画一行信息文字，并让整行文字悬停时显示对应的详细说明。 */
    private void drawTipLine(GuiGraphics g, Component text, int x, int y, int color, String tooltipKey) {
        g.drawString(font, text, x, y, color, false);
        registerTip(x - 1, y - 1, font.width(text) + 2, 11, tooltipKey);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            for (int i = switchTargets.size() - 1; i >= 0; i--) {
                SwitchTarget target = switchTargets.get(i);
                if (mouseX >= target.x() && mouseX < target.x() + target.width()
                        && mouseY >= target.y() && mouseY < target.y() + target.height()) {
                    Network.CHANNEL.sendToServer(new cn.blockforge.generated.generatedmod.network.packet.cultivation.RootFormPacket(target.form()));
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void drawLeftColumn(GuiGraphics g, CultivationData data, int x, int y, int w, int h, boolean compact) {
        ColumnBounds bounds = new ColumnBounds(x, y, w, h, compact);
        int line = y + 22;
        int manaY = line + (compact ? 19 : 21);
        int healthY = line + (compact ? 39 : 43);
        // 修炼状态固定六行：攻击、功德、阳神纯度、道心、经脉、体质。
        // 阳神纯度紧贴道心上方、左端对齐；体质并入本栏，位于经脉下方。
        int step = compact ? 12 : 14;
        int infoLine = leftInfoLine(bounds);
        int stateHeight = leftStateHeight(bounds);
        section(g, x, y, w, stateHeight, "gui.wuxingxiantu.inner_vision.cultivation_state");
        int maxMana = Math.max(1, Math.round(CultivationLogic.maxAura(data)));
        int mana = Math.max(0, Math.min(maxMana, Math.round(data.wuxingxiantu$getCurrentMana())));
        long cultivationCap = CultivationLogic.breakthroughRequirement(data);
        int barWidth = stateBarWidth(w);
        if (tribulationButton != null) {
            tribulationButton.active = cn.blockforge.generated.generatedmod.TribulationManager.canStart(minecraft.player, data);
        }
        if (crystallizeButton != null) crystallizeButton.active = mana >= CultivationLogic.CRYSTALLIZE_AURA_COST;
        drawLongBar(g, x + 8, line, barWidth, data.wuxingxiantu$getCultivation(), cultivationCap,
                "gui.wuxingxiantu.inner_vision.cultivation", compact);
        registerTip(x + 8, line - 1, barWidth, 11, "gui.wuxingxiantu.inner_vision.cultivation_tip");
        drawBar(g, x + 8, manaY, barWidth, mana, maxMana, "gui.wuxingxiantu.inner_vision.mana", compact);
        registerTip(x + 8, manaY - 1, barWidth, 11, "gui.wuxingxiantu.inner_vision.mana_tip");
        drawBreakthroughMarker(g, x + 8, manaY, barWidth, maxMana, data);
        drawBar(g, x + 8, healthY, barWidth, Math.round(minecraft.player.getHealth()),
                Math.round(minecraft.player.getMaxHealth()), "gui.wuxingxiantu.inner_vision.health", compact);
        registerTip(x + 8, healthY - 1, barWidth, 11, "gui.wuxingxiantu.inner_vision.health_tip");

        Component attack = Component.translatable("gui.wuxingxiantu.inner_vision.attack",
                String.format(java.util.Locale.ROOT, "%.1f", cn.blockforge.generated.generatedmod.combat.HeartSwordCombatProfile.attackAttribute(
                        minecraft.player, net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE)));
        Component merit = Component.translatable("gui.wuxingxiantu.inner_vision.merit", meritText(data));
        Component purity = Component.translatable("gui.wuxingxiantu.inner_vision.yang_shen_purity",
                data.wuxingxiantu$getYinShenPurity());
        Component dao = Component.translatable("gui.wuxingxiantu.inner_vision.dao",
                Component.translatable(daoKey(data.wuxingxiantu$getDaoXin())));
        Component meridian = Component.translatable("gui.wuxingxiantu.inner_vision.meridian",
                Component.translatable(meridianKey(data.wuxingxiantu$getMeridianStatus())));
        String physiqueId = data.wuxingxiantu$getPhysique().id();
        Component physique = Component.translatable("gui.wuxingxiantu.inner_vision.physique_line",
                Component.translatable("physique.wuxingxiantu." + physiqueId));

        drawTipLine(g, attack, x + 8, infoLine, TEXT, "gui.wuxingxiantu.inner_vision.attack_tip");
        drawTipLine(g, merit, x + 8, infoLine + step,
                data.wuxingxiantu$getMerit() < 0 ? RED : GOLD, "gui.wuxingxiantu.inner_vision.merit_tip");
        drawTipLine(g, purity, x + 8, infoLine + step * 2, GOLD,
                "gui.wuxingxiantu.inner_vision.yang_shen_purity_tip");
        drawTipLine(g, dao, x + 8, infoLine + step * 3, TEXT, "gui.wuxingxiantu.inner_vision.dao_tip");
        registerTip(x + 7, infoLine + step * 3 - 1, barWidth, 11,
                Component.translatable("gui.wuxingxiantu.inner_vision.dao_tip")
                        .append(" ").append(Component.translatable(daoEffectKey(data.wuxingxiantu$getDaoXin()))));
        drawTipLine(g, meridian, x + 8, infoLine + step * 4,
                data.wuxingxiantu$getMeridianStatus() >= 2 ? RED
                        : data.wuxingxiantu$getMeridianStatus() == 1 ? GOLD : CYAN,
                "gui.wuxingxiantu.inner_vision.meridian_tip");
        g.drawString(font, physique, x + 8, infoLine + step * 5, CYAN, false);
        registerTip(x + 7, infoLine + step * 5 - 1, font.width(physique) + 2, 11,
                Component.translatable("gui.wuxingxiantu.inner_vision.physique_tooltip",
                        Component.translatable("physique.wuxingxiantu." + physiqueId),
                        Component.translatable(physiqueDescriptionKey(physiqueId))));

        int rootY = y + stateHeight + 4;
        int mask = data.wuxingxiantu$getRootMask();
        boolean chaosRoot = data.wuxingxiantu$hasChaosRoot();
        int rootRowSpacing = compact ? 13 : 18;
        // 高度按实际行数收紧，避免旧版固定 h/3 造成的底部留白。
        int rootHeight = leftRootHeight(data, compact);
        section(g, x, rootY, w, rootHeight, "gui.wuxingxiantu.inner_vision.roots");
        int rootLine = rootY + 22;
        int shown = 0;
        for (int root = 0; root < 5; root++) {
            if ((mask & (1 << root)) == 0) continue;
            int col = shown % 2;
            int row = shown / 2;
            int rx = x + 8 + col * (w / 2);
            int ry = rootLine + row * rootRowSpacing;
            // 变异上位灵根显化时，本根条目就地换成天雷/异火灵根，并共用其品质。
            boolean thunderForm = root == 1 && CultivationLogic.isThunderForm(data);
            boolean exoticForm = root == 3 && CultivationLogic.isExoticForm(data);
            int swatch = thunderForm ? THUNDER_ROOT_COLOR : exoticForm ? EXOTIC_ROOT_COLOR : ROOT_COLORS[root];
            int textColor = thunderForm ? THUNDER_ROOT_TEXT : exoticForm ? EXOTIC_ROOT_TEXT : TEXT;
            g.fill(rx, ry + 1, rx + 7, ry + 8, swatch);
            Component rootName = Component.translatable(thunderForm ? "root.wuxingxiantu.thunder"
                    : exoticForm ? "root.wuxingxiantu.exotic_fire" : "root.wuxingxiantu." + root);
            Component quality = Component.translatable("quality.wuxingxiantu." + qualityKey(data.wuxingxiantu$getRootQuality(root)));
            Component entry = Component.translatable("gui.wuxingxiantu.inner_vision.root_entry", rootName, quality);
            g.drawString(font, entry, rx + 11, ry, textColor, false);
            int iconX = rx + 13 + font.width(entry);
            if (root == 1 && CultivationLogic.thunderRootAvailable(data)) {
                drawSwitchIcon(g, iconX, ry + 1);
                registerSwitchZone(iconX - 2, ry - 1, 12, 11, 0, Component.translatable(thunderForm
                        ? "gui.wuxingxiantu.inner_vision.switch_to_wood"
                        : "gui.wuxingxiantu.inner_vision.switch_to_thunder"));
            } else if (root == 3 && CultivationLogic.exoticRootAvailable(data)) {
                drawSwitchIcon(g, iconX, ry + 1);
                registerSwitchZone(iconX - 2, ry - 1, 12, 11, 1, Component.translatable(exoticForm
                        ? "gui.wuxingxiantu.inner_vision.switch_to_fire"
                        : "gui.wuxingxiantu.inner_vision.switch_to_exotic"));
            }
            shown++;
        }
        if (chaosRoot) {
            int col = shown % 2;
            int row = shown / 2;
            int rx = x + 8 + col * (w / 2);
            int ry = rootLine + row * rootRowSpacing;
            g.fill(rx, ry + 1, rx + 7, ry + 8, CHAOS_ROOT_COLOR);
            g.drawString(font, Component.translatable("root.wuxingxiantu.chaos"), rx + 11, ry, CHAOS_ROOT_TEXT, false);
            shown++;
        }
        if (shown == 0) g.drawString(font, Component.translatable("gui.wuxingxiantu.inner_vision.no_roots"),
                x + 8, rootLine, MUTED, false);
    }

    private void drawRightColumn(GuiGraphics g, CultivationData data, int x, int y, int w, int h, boolean compact) {
        ColumnBounds bounds = new ColumnBounds(x, y, w, h, compact);
        int artsHeight = artsHeight(bounds, data);
        section(g, x, y, w, artsHeight, "gui.wuxingxiantu.inner_vision.arts");
        String[] arts = data.wuxingxiantu$getEquippedArts();
        int unlocked = CultivationLogic.unlockedArtSlots(data.wuxingxiantu$getRealm());
        for (int slot = 0; slot < 8; slot++) {
            int col = slot % 2;
            int row = slot / 2;
            int sx = x + 8 + col * (w / 2);
            int sy = y + 22 + row * (compact ? 20 : 19);
            if (slot >= unlocked) {
                g.drawString(font, Component.translatable("gui.wuxingxiantu.inner_vision.locked", slot + 1), sx, sy, MUTED, false);
            } else if (arts[slot] == null) {
                g.drawString(font, Component.translatable("gui.wuxingxiantu.inner_vision.empty_slot", slot + 1), sx, sy, MUTED, false);
            } else {
                String art = arts[slot];
                int mastery = data.wuxingxiantu$getArtMastery(art);
                Component name = Component.translatable("gui.wuxingxiantu.inner_vision.art_name", slot + 1, ArtCatalog.labeledName(art));
                g.drawString(font, name, sx, sy, ArtCatalog.tierColor(ArtCatalog.tier(art)), false);
                int barY = sy + (compact ? 9 : 11);
                int barWidth = Math.max(30, w / 2 - 18);
                int fill = Math.round((barWidth - 2) * mastery / 100.0F);
                g.fill(sx, barY, sx + barWidth, barY + 4, 0xFF1A2021);
                if (fill > 0) g.fill(sx + 1, barY + 1, sx + 1 + fill, barY + 3, ArtCatalog.masteryColor(mastery));
                g.renderOutline(sx, barY, barWidth, 4, 0xFF765D50);
            }
        }

        String heartMethod = data.wuxingxiantu$getEquippedHeartMethod();
        boolean hasHeart = heartMethod != null && !heartMethod.isBlank();
        int nextY = y + artsHeight + 6;
        if (hasHeart) {
            int heartHeight = heartHeight(compact);
            section(g, x, nextY, w, heartHeight, "gui.wuxingxiantu.inner_vision.heart_method");
            g.drawString(font, Component.translatable("item.wuxingxiantu.heart_method_" + heartMethod),
                    x + 8, nextY + 22, GOLD, false);
            g.drawString(font, HeartMethodCatalog.description(heartMethod),
                    x + 8, nextY + 38, MUTED, false);
            nextY += heartHeight + 6;
        }

        int fireHeight = fireHeight(compact);
        if (nextY + fireHeight <= y + h) {
            section(g, x, nextY, w, fireHeight, "gui.wuxingxiantu.inner_vision.fires");
            drawFireEntries(g, data, x, nextY, w, compact);
            nextY += fireHeight + 6;
        }

        boolean details = rightShowsDetails(bounds);
        if (details) {
            int swordHeight = swordHeight(compact);
            section(g, x, nextY, w, swordHeight, "gui.wuxingxiantu.inner_vision.spirit_sword");
            ItemStack swordStack = findSword();
            if (swordStack.isEmpty()) {
                g.drawString(font, Component.translatable("gui.wuxingxiantu.inner_vision.no_sword"),
                        x + 8, nextY + 22, MUTED, false);
            } else if (swordStack.getItem() instanceof SpiritSwordItem sword) {
                g.drawString(font, swordStack.getHoverName(), x + 8, nextY + 22, CYAN, false);
                g.drawString(font, Component.translatable("gui.wuxingxiantu.inner_vision.sword_tier",
                        Component.translatable("item.wuxingxiantu." + sword.tierId() + "_sword")), 
                        x + 8, nextY + 37, TEXT, false);
                g.drawString(font, Component.translatable("gui.wuxingxiantu.inner_vision.sword_spirit",
                        SpiritSwordItem.getSpirit(swordStack), SpiritSwordItem.getMaxSpirit(swordStack)),
                        x + 8, nextY + 52, MUTED, false);
            }
            nextY += swordHeight + 6;

            int equipmentHeight = equipmentHeight(compact);
            section(g, x, nextY, w, equipmentHeight, "gui.wuxingxiantu.inner_vision.equipment");
            drawEquipment(g, data, x, nextY, w);
            nextY += equipmentHeight + 6;
        }
    }

    private void drawEquipment(GuiGraphics g, CultivationData data, int x, int y, int w) {
        EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        String[] keys = {"helmet", "chestplate", "leggings", "boots"};
        for (int i = 0; i < slots.length; i++) {
            int col = i % 2;
            int row = i / 2;
            ItemStack stack = minecraft.player.getItemBySlot(slots[i]);
            Component value = stack.isEmpty()
                    ? Component.translatable("gui.wuxingxiantu.inner_vision.empty") : stack.getHoverName();
            Component entry = Component.translatable("gui.wuxingxiantu.inner_vision.equipment_entry",
                    Component.translatable("gui.wuxingxiantu.inner_vision." + keys[i]), value);
            g.drawString(font, entry, x + 8 + col * (w / 2), y + 22 + row * 18, TEXT, false);
        }
    }

    private ItemStack findSword() {
        if (minecraft.player.getMainHandItem().getItem() instanceof SpiritSwordItem) return minecraft.player.getMainHandItem();
        if (minecraft.player.getOffhandItem().getItem() instanceof SpiritSwordItem) return minecraft.player.getOffhandItem();
        for (ItemStack stack : minecraft.player.getInventory().items) {
            if (stack.getItem() instanceof SpiritSwordItem) return stack;
        }
        return ItemStack.EMPTY;
    }

    private int artsHeight(ColumnBounds bounds, CultivationData data) {
        return bounds.compact() ? 104 : 126;
    }

    private int heartHeight(boolean compact) {
        // 心法栏同时展示名称和能力摘要，按钮放在摘要下方。
        return compact ? 76 : 80;
    }

    private int fireHeight(boolean compact) {
        return compact ? 88 : 90;
    }

    private Component fireLabel(CultivationData data, int tier) {
        String marker = data != null && data.wuxingxiantu$getEquippedFire() == tier ? "◆ " : "";
        return Component.literal(marker + FireSeedItem.getFireName(tier));
    }

    /** 再次点击当前异火时发送 0，清除装备状态。 */
    private void selectFire(int tier) {
        CultivationData data = GeneratedMod.data(minecraft.player);
        int equipped = data == null ? 0 : data.wuxingxiantu$getEquippedFire();
        Network.CHANNEL.sendToServer(new cn.blockforge.generated.generatedmod.network.packet.cultivation.FireSelectPacket(equipped == tier ? 0 : tier));
    }

    private String meritText(CultivationData data) {
        return data.wuxingxiantu$getMerit() == Long.MAX_VALUE
                ? "∞" : Long.toString(data.wuxingxiantu$getMerit());
    }

    private void drawFireEntries(GuiGraphics g, CultivationData data, int x, int y, int w, boolean compact) {
        int buttonWidth = Math.max(48, (w - 16 - 8) / 3);
        for (int tier = 1; tier <= 9; tier++) {
            int column = (tier - 1) % 3;
            int row = (tier - 1) / 3;
            int sx = x + 8 + column * (buttonWidth + 4);
            int sy = y + 22 + row * 20;
            if (!data.wuxingxiantu$hasLearnedFire(tier)) {
                g.drawString(font, Component.translatable("gui.wuxingxiantu.inner_vision.fire_locked", tier),
                        sx + 4, sy + 4, MUTED, false);
            }
        }
    }

    private int swordHeight(boolean compact) {
        return compact ? 64 : 72;
    }

    private int equipmentHeight(boolean compact) {
        return compact ? 72 : 84;
    }

    private int domainHeight(boolean compact, int width) {
        CultivationData data = GeneratedMod.data(minecraft.player);
        int count = domainCount(data);
        if (count == 0) return 0;
        int columns = domainColumns(data, width);
        int rows = (count + columns - 1) / columns;
        return 28 + rows * 22;
    }

    private void drawDomainEntries(GuiGraphics g, CultivationData data, int x, int y, int width) {
        // 领域能力说明统一放进物品栏的 buff 悬停文本，内视面板只保留可操作的开关列表。
    }

    private boolean rightShowsDetails(ColumnBounds bounds) {
        // 领域面板需要完整保留在右栏底部，空间不足时优先隐藏详情卡片。
        return !bounds.compact() && bounds.height() >= 510;
    }

    private DomainBounds domainBounds(CultivationData data) {
        if (!hasWoodDomain(data) && !hasFireDomain(data) && !hasGoldDomain(data)
                && !hasWaterDomain(data) && !hasEarthDomain(data)) return null;
        // 优先放在左栏灵根下方——那里通常留有大片空白，视觉上属于修炼状态的自然延伸。
        ColumnBounds left = leftColumnBounds();
        int leftTop = leftRootsBottom(data);
        int leftHeight = domainHeight(left.compact(), left.width());
        if (leftHeight > 0 && leftTop + leftHeight <= left.y() + left.height()) {
            return new DomainBounds(left.x(), leftTop, left.width(), leftHeight, left.compact());
        }
        // 左栏放不下时退回右栏底部，保证小窗口下也能操作。
        ColumnBounds right = rightColumnBounds();
        int domainY = right.y() + artsHeight(right, data) + 6;
        if (data.wuxingxiantu$getEquippedHeartMethod() != null
                && !data.wuxingxiantu$getEquippedHeartMethod().isBlank()) {
            domainY += heartHeight(right.compact()) + 6;
        }
        domainY += fireHeight(right.compact()) + 6;
        if (rightShowsDetails(right)) {
            domainY += swordHeight(right.compact()) + 6;
            domainY += equipmentHeight(right.compact()) + 6;
        }
        int rightHeight = domainHeight(right.compact(), right.width());
        if (domainY + rightHeight > right.y() + right.height()) return null;
        return new DomainBounds(right.x(), domainY, right.width(), rightHeight, right.compact());
    }

    /** 左栏修炼状态六行文字的首行 Y（与 drawLeftColumn 共用同一套间距）。 */
    private int leftInfoLine(ColumnBounds left) {
        boolean compact = left.compact();
        return left.y() + 22 + (compact ? 39 : 43) + (compact ? 20 : 21);
    }

    /** 左栏修炼状态卡片高度。 */
    private int leftStateHeight(ColumnBounds left) {
        int step = left.compact() ? 12 : 14;
        return leftInfoLine(left) - left.y() + step * 5 + 11;
    }

    /** 左栏灵根卡片高度，按实际灵根行数收紧。 */
    private int leftRootHeight(CultivationData data, boolean compact) {
        int mask = data == null ? 0 : data.wuxingxiantu$getRootMask();
        boolean chaosRoot = data != null && data.wuxingxiantu$hasChaosRoot();
        int rootCount = Integer.bitCount(mask) + (chaosRoot ? 1 : 0);
        int rootRows = Math.max(1, (rootCount + 1) / 2);
        int rootRowSpacing = compact ? 13 : 18;
        return Math.max(40, 37 + (rootRows - 1) * rootRowSpacing);
    }

    /** 左栏灵根卡片底边，领域开关从这里接着往下排。 */
    private int leftRootsBottom(CultivationData data) {
        ColumnBounds left = leftColumnBounds();
        return left.y() + leftStateHeight(left) + 4 + leftRootHeight(data, left.compact());
    }

    private ColumnBounds leftColumnBounds() {
        int left = panelLeft();
        int top = panelTop();
        int width = panelWidth();
        boolean narrow = width < 460;
        int contentTop = top + 30;
        int contentBottom = top + panelHeight() - (narrow ? 44 : 34);
        int contentHeight = Math.max(100, contentBottom - contentTop);
        boolean compact = narrow || contentHeight < 300;
        if (!narrow) return new ColumnBounds(left + 10, contentTop, (width - 28) / 2, contentHeight, compact);
        int leftHeight = Math.min(contentHeight, Math.max(220, contentHeight / 2));
        return new ColumnBounds(left + 10, contentTop, width - 20, leftHeight, true);
    }

    private ColumnBounds rightColumnBounds() {
        int left = panelLeft();
        int top = panelTop();
        int width = panelWidth();
        boolean narrow = width < 460;
        int contentTop = top + 30;
        int contentBottom = top + panelHeight() - (narrow ? 44 : 34);
        int contentHeight = Math.max(100, contentBottom - contentTop);
        boolean compact = narrow || contentHeight < 300;
        if (!narrow) {
            int columnWidth = (width - 28) / 2;
            return new ColumnBounds(left + 14 + columnWidth + 8, contentTop, columnWidth, contentHeight, compact);
        }
        ColumnBounds leftColumn = leftColumnBounds();
        int rightY = leftColumn.y() + leftColumn.height() + 6;
        return new ColumnBounds(left + 10, rightY, width - 20, Math.max(0, contentBottom - rightY), true);
    }

    private void section(GuiGraphics g, int x, int y, int w, int h, String titleKey) {
        if (w <= 0 || h <= 0) return;
        g.fill(x, y, x + w, y + h, PANEL_ALT);
        g.renderOutline(x, y, w, h, 0x997F5A42);
        g.fill(x, y, x + w, y + 2, GOLD);
        g.drawString(font, Component.translatable(titleKey), x + 8, y + 7, GOLD, false);
    }

    private void drawLongBar(GuiGraphics g, int x, int y, int w, long value, long max, String labelKey, boolean compact) {
        long safeMax = Math.max(1L, max);
        long safeValue = Math.max(0L, Math.min(safeMax, value));
        int fill = Math.round((w - 2) * safeValue / (float) safeMax);
        drawBarLabel(g, Component.translatable(labelKey, safeValue, safeMax), x, y, w);

        int barY = y + (compact ? 9 : 10);
        g.fill(x, barY, x + w, barY + 6, 0xFF1A2021);
        if (fill > 0) g.fillGradient(x + 1, barY + 1, x + 1 + fill, barY + 5, 0xFFE8C477, 0xFFB2763B);
        g.renderOutline(x, barY, w, 6, 0xFF8D6E4B);
    }

    private void drawBreakthroughMarker(GuiGraphics g, int x, int y, int w, int max, CultivationData data) {
        breakthroughMarkerWidth = w;
        breakthroughMarkerX = x + Math.round(w * 0.70F);
        breakthroughMarkerY = y + 9;
        g.fill(breakthroughMarkerX - 1, breakthroughMarkerY, breakthroughMarkerX + 1,
                breakthroughMarkerY + 6, 0xFFFFD36A);
        g.fill(breakthroughMarkerX - 2, breakthroughMarkerY + 2, breakthroughMarkerX + 2,
                breakthroughMarkerY + 4, 0xFFFFF0B0);
    }

    private void drawBarLabel(GuiGraphics g, Component label, int x, int y, int w) {
        g.drawString(font, label, x, y - 1, TEXT, false);
    }

    private void drawBar(GuiGraphics g, int x, int y, int w, int value, int max, String labelKey, boolean compact) {
        int safeMax = Math.max(1, max);
        int safeValue = Math.max(0, Math.min(safeMax, value));
        int fill = Math.round((w - 2) * safeValue / (float) safeMax);
        drawBarLabel(g, Component.translatable(labelKey, safeValue, safeMax), x, y, w);
        int barY = y + (compact ? 9 : 10);
        g.fill(x, barY, x + w, barY + 6, 0xFF1A2021);
        if (fill > 0) g.fillGradient(x + 1, barY + 1, x + 1 + fill, barY + 5, 0xFFB7F4F0, 0xFF3CA8B4);
        g.renderOutline(x, barY, w, 6, 0xFF6C8D87);
    }

    private static int qualityKey(int quality) {
        return Math.max(0, Math.min(3, quality - 1));
    }

    private static String meridianKey(int status) {
        return switch (Math.max(0, Math.min(2, status))) {
            case 1 -> "gui.wuxingxiantu.inner_vision.meridian_damaged";
            case 2 -> "gui.wuxingxiantu.inner_vision.meridian_shattered";
            default -> "gui.wuxingxiantu.inner_vision.meridian_intact";
        };
    }

    private static String physiqueDescriptionKey(String physiqueId) {
        return "gui.wuxingxiantu.inner_vision.physique_desc_"
                + ("taichu".equals(physiqueId) ? "fan" : physiqueId);
    }

    /** 内部数值沿用旧存档语义，显示键按文档定义重新对齐。 */
    private static String daoKey(int value) {
        return "dao.wuxingxiantu." + Math.max(0, Math.min(8, value));
    }
    private static String daoEffectKey(int value) {
        return "dao.effect.wuxingxiantu." + Math.max(0, Math.min(8, value));
    }

    private int manaActionWidth(int columnWidth) {
        return Math.min(54, Math.max(46, columnWidth / 5));
    }

    private int stateBarWidth(int columnWidth) {
        return Math.max(40, columnWidth - 16 - manaActionWidth(columnWidth) - 4);
    }

    private int panelWidth() { return Math.min(560, Math.max(280, width - 16)); }
    private int panelHeight() { return Math.min(500, Math.max(220, height - 8)); }
    private int panelLeft() { return Math.max(0, (width - panelWidth()) / 2); }
    private int panelTop() { return Math.max(4, (height - panelHeight()) / 2); }
    private int panelBottom() { return panelTop() + panelHeight(); }

    @Override
    public boolean isPauseScreen() { return false; }

    private static final class DomainButton extends Button {
        private final BooleanSupplier enabled;
        private int accent;
        private int root;

        private DomainButton(int x, int y, int width, int height, Component message, OnPress onPress,
                             BooleanSupplier enabled, int accent) {
            super(Button.builder(message, onPress).bounds(x, y, width, height));
            this.enabled = enabled;
            this.accent = accent;
        }

        /** 天雷/异火显化时由界面每帧刷新色条颜色。 */
        private void setAccent(int accent) {
            this.accent = accent;
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
            if(isHoveredOrFocused()){
                var mc=net.minecraft.client.Minecraft.getInstance();
                CultivationUi.tooltip(DomainTooltips.describe(mc.player,GeneratedMod.data(mc.player),root));
            }
            boolean active = enabled.getAsBoolean();
            int background = isHoveredOrFocused() ? 0xFF4B3B32 : 0xFF302629;
            graphics.fill(getX(), getY(), getX() + width, getY() + height, background);
            graphics.renderOutline(getX(), getY(), width, height, active ? accent : 0xFF765D50);
            graphics.fill(getX() + 2, getY() + 2, getX() + 5, getY() + height - 2, accent);
            int markX = getX() + 7;
            int markY = getY() + Math.max(0, (height - 12) / 2);
            graphics.fill(markX, markY, markX + 12, markY + 12, active ? 0xFF41352F : 0xFF2A2324);
            graphics.renderOutline(markX, markY, 12, 12, active ? accent : 0xFF9D8B7B);
            if (active) {
                int mark = accent;
                graphics.fill(markX + 2, markY + 5, markX + 5, markY + 8, mark);
                graphics.fill(markX + 4, markY + 7, markX + 7, markY + 10, mark);
                graphics.fill(markX + 6, markY + 5, markX + 9, markY + 8, mark);
                graphics.fill(markX + 8, markY + 3, markX + 11, markY + 6, mark);
            }
            int textY = getY() + Math.max(0, (height - Minecraft.getInstance().font.lineHeight) / 2);
            graphics.drawString(Minecraft.getInstance().font, getMessage(), getX() + 23, textY,
                    active ? 0xFFF4E8D0 : 0xFFB5A79A, false);
        }
    }

    private record ColumnBounds(int x, int y, int width, int height, boolean compact) {}
    private record DomainBounds(int x, int y, int width, int height, boolean compact) {}
}
