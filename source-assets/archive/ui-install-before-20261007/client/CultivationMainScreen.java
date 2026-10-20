package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.items.CultivationLogic;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import java.util.*;

/** 同一原版容器内切页；仅在本界面调整槽位坐标，关闭时还原。 */
public final class CultivationMainScreen extends InventoryScreen {
    private final Map<String, CultivationPage> pages = new HashMap<>();
    private List<CultivationPageRegistry.Entry> navigation;
    private final List<CultivationUi.Action> navButtons = new ArrayList<>();
    private CultivationPage page;
    private String pageId;
    private int panelX, panelY, panelW, panelH, contentX, contentY, contentW, contentH, scroll;
    private CultivationLayout layout;
    private net.minecraft.client.gui.components.AbstractWidget recipeButton;
    private final Map<Slot, int[]> originalSlotPositions = new IdentityHashMap<>();
    private final Map<String, Integer> pageScroll = new HashMap<>();
    private boolean pointerInPage, draggingScrollbar;
    private boolean recipeButtonClicked;
    private double scrollbarGrab;
    private float pageScale = 1;
    private CultivationUi.Action close;

    public CultivationMainScreen(Player player) { this(player, "inventory"); }
    public CultivationMainScreen(Player player, String pageId) {
        super(player); this.pageId = pageId;
        imageWidth = CultivationLayout.INVENTORY_WIDTH; imageHeight = CultivationLayout.INVENTORY_HEIGHT;
    }
    private boolean inventory() { return page == null; }
    boolean hasCustomPage() { return !inventory(); }
    boolean ownsClick(double x,double y) {
        if(!inventory())return x>=panelX&&x<panelX+panelW&&y>=panelY&&y<panelY+panelH;
        if(getRecipeBookComponent().isVisible()&&getRecipeBookComponent().isMouseOver(x,y))return false;
        return close.isMouseOver(x,y)||(recipeButton!=null&&recipeButton.isMouseOver(x,y))
                ||navButtons.stream().anyMatch(b->b.isMouseOver(x,y));
    }
    // Inherited recipe hotspots are relative to the actual inventory, not our outer frame.
    @Override public int getGuiLeft() { return leftPos; }
    @Override public int getGuiTop() { return topPos; }
    @Override public int getXSize() { return panelX + panelW - leftPos; }
    @Override public int getYSize() { return panelY + panelH - topPos; }
    @Override public net.minecraft.world.inventory.Slot getSlotUnderMouse() {
        return inventory()&&!getRecipeBookComponent().isVisible() ? super.getSlotUnderMouse() : null;
    }
    @Override protected void init() {
        // Use vanilla's modal recipe rendering, but give it a dedicated page instead of covering slots.
        int screenWidth = width;
        width = Math.min(width, 378);
        try { super.init(); } finally { width = screenWidth; }
        recipeButton = children().stream().filter(e -> e instanceof net.minecraft.client.gui.components.ImageButton)
                .map(e -> (net.minecraft.client.gui.components.AbstractWidget)e).findFirst().orElse(null);
        if (recipeButton != null) removeWidget(recipeButton);
        recipeButton = addRenderableWidget(new CultivationUi.Icon(0, 0, 20, "",
                () -> new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.BOOK),
                () -> Component.translatable(getRecipeBookComponent().isVisible()
                        ?"screen.wuxingxiantu.recipe_close":"screen.wuxingxiantu.recipe_open"),
                b -> { getRecipeBookComponent().toggleVisibility(); positionInventory(); recipeButtonClicked = true; },
                () -> getRecipeBookComponent().isVisible()));
        layout = CultivationLayout.of(width, height);
        panelW = layout.width(); panelH = layout.height(); panelX = layout.x(); panelY = layout.y();
        contentX = layout.contentX(); contentY = layout.contentY();
        contentW = layout.contentWidth(); contentH = layout.contentHeight();
        pageScale = contentW >= 286 ? .75F : 1;
        navigation = CultivationPageRegistry.visible();
        navButtons.clear();
        for (int i = 0; i < navigation.size(); i++) {
            var entry = navigation.get(i);
            navButtons.add(new CultivationUi.Navigation(panelX + 6, layout.navY(i), layout.sidebarWidth(),
                    Component.literal(entry.title()), entry.id(),
                    b -> selectPage(entry.id()), () -> pageId.equals(entry.id())));
        }
        close = new CultivationUi.Action(panelX + panelW - 22, panelY + 4, 16, 16,
                Component.literal("×"), b -> onClose());
        // Vanilla derives both drawing and hit areas from these virtual dimensions.
        // Include the left category tabs when centering within our content pane.
        int bookX=contentX+(contentW-147+28-24)/2;
        int bookY=contentY+Math.max(0,(contentH-166)/2);
        getRecipeBookComponent().init(2*bookX+147,2*bookY+166,minecraft,true,getMenu());
        positionInventorySlots();
        positionInventory();
        String initialPage = pageId;
        if (!getMenu().getCarried().isEmpty() || isQuickCrafting) { page = null; pageId = "inventory"; }
        else selectPage(initialPage);
    }
    public void selectPage(String id) {
        // 手持物品时不换页，避免隐藏正在进行的拖拽和合成操作。
        if (!getMenu().getCarried().isEmpty() || isQuickCrafting) return;
        var entry = navigation.stream().filter(e -> e.id().equals(id)).findFirst().orElse(navigation.get(0));
        if (page != null) { pageScroll.put(pageId, scroll); page.removed(); }
        pageId = entry.id(); scroll = pageScroll.getOrDefault(pageId, 0); pointerInPage = draggingScrollbar = false;
        page = entry.factory() == null ? null : pages.computeIfAbsent(pageId, key -> entry.factory().get());
        hoveredSlot = null;
        setFocused(null);
        if (recipeButton != null) { recipeButton.visible = inventory(); recipeButton.active = inventory(); }
        if (!inventory() && getRecipeBookComponent().isVisible()) getRecipeBookComponent().toggleVisibility();
        if (page != null) {
            page.attach(this);
            page.init(minecraft, pageWidth(), page.contentHeight(pageWidth(), contentViewportHeight()));
            scroll = Math.max(0, Math.min(scroll, page.height - contentViewportHeight()));
        }
    }
    private void positionInventory() {
        leftPos = layout.inventoryX(); topPos = layout.inventoryY();
        if (recipeButton != null) {
            recipeButton.active=getMenu().getCarried().isEmpty()&&!isQuickCrafting;
            boolean recipe=getRecipeBookComponent().isVisible();
            recipeButton.setX(recipe?contentX+contentW-22:leftPos+123);
            recipeButton.setY(recipe?contentY+contentH-22:topPos+70);
        }
    }
    private void positionInventorySlots() {
        // Slot indices and containers stay intact, so vanilla transfer/recipe logic still applies.
        for (int i = 0; i < getMenu().slots.size(); i++) {
            Slot slot = getMenu().slots.get(i);
            int x = slot.x, y = slot.y;
            if (slot instanceof cn.blockforge.generated.generatedmod.robe.EquipmentSlots.EquipmentSlot) {
                x = 90; y = slot.getContainerSlot() == cn.blockforge.generated.generatedmod.robe.YujianSlot.YUJIAN_SLOT ? 42 : 18;
            } else if (i == 0) { x = 184; y = 39; }
            else if (i <= 4) { x = 122 + (i - 1) % 2 * 20; y = 29 + (i - 1) / 2 * 20; }
            else if (i <= 8) { x = 8; y = 18 + (i - 5) * 18; }
            else if (i <= 35) { x = 23 + (i - 9) % 9 * 18; y = 108 + (i - 9) / 9 * 18; }
            else if (i <= 44) { x = 23 + (i - 36) * 18; y = 166; }
            else if (i == 45) { x = 90; y = 66; }
            else continue;
            originalSlotPositions.computeIfAbsent(slot, key -> new int[]{key.x, key.y});
            slot.x = x; slot.y = y;
        }
    }
    private void restoreInventorySlots() {
        originalSlotPositions.forEach((slot, position) -> { slot.x = position[0]; slot.y = position[1]; });
        originalSlotPositions.clear();
    }
    void scrollToTop() { scroll = 0; pageScroll.put(pageId, 0); }
    int contentViewportHeight() { return (int)(contentH / pageScale); }
    private int pageWidth() { return (int)(contentW / pageScale) - 8; }
    private double pageX(double x) { return (x - contentX) / pageScale; }
    private double pageY(double y) { return (y - contentY) / pageScale + scroll; }
    @Override public void containerTick() {
        super.containerTick();
        if (page != null) {
            page.tick();
            scroll = Math.max(0, Math.min(scroll, page.height - contentViewportHeight()));
        }
    }
    private boolean inPage(double x, double y) {
        return x >= contentX && x < contentX + contentW && y >= contentY && y < contentY + contentH;
    }
    private void frame(GuiGraphics g, int mx, int my, float delta) {
        CultivationUi.paper(g, panelX, panelY, panelW, panelH);
        CultivationUi.scenery(g, panelX + 3, panelY + 3, panelW - 6, panelH - 6);
        CultivationUi.panel(g, panelX + 4, panelY + 23, contentX - panelX - 11, panelH - 27, false);
        CultivationUi.panel(g, panelX + 4, panelY + 4, panelW - 8, 19, true);
        if (!inventory()) {
            CultivationUi.panel(g, contentX - 5, contentY - 3, contentW + 8, contentH + 5, true);
        }
        CultivationUi.ornamentFrame(g, panelX, panelY, panelW, panelH);
        g.fill(panelX + 4, panelY + 22, panelX + panelW - 5, panelY + 23, CultivationUi.BORDER);
        g.drawString(font, "五行仙途", panelX + 9, panelY + 9, CultivationUi.GOLD, false);
        String name = inventory()&&getRecipeBookComponent().isVisible()
                ?Component.translatable("screen.wuxingxiantu.recipes").getString()
                :navigation.stream().filter(e -> e.id().equals(pageId)).map(CultivationPageRegistry.Entry::title).findFirst().orElse("背包");
        int titleCenter = panelX + panelW / 2;
        g.drawString(font, name, titleCenter - font.width(name) / 2, panelY + 9, CultivationUi.TEXT, false);
        var data = GeneratedMod.data(minecraft.player);
        if (data != null) {
            String realm = CultivationLogic.realmStageName(minecraft.player, data);
            int available = Math.min(panelW / 2 - 48, font.width(realm));
            CultivationUi.fit(g, Component.literal(realm), panelX + panelW - 30 - available, panelY + 9, available, CultivationUi.GOLD, mx, my);
        }
        for (int i = 0; i < navButtons.size(); i++) {
            var b = navButtons.get(i);
            b.active = getMenu().getCarried().isEmpty() && !isQuickCrafting;
            b.render(g, mx, my, delta);
        }
        close.render(g, mx, my, delta);
        if (mx >= close.getX() && mx < close.getX() + close.getWidth() && my >= close.getY() && my < close.getY() + close.getHeight())
            CultivationUi.tooltip(Component.literal("关闭"));
    }
    void clipPage(GuiGraphics g, int x, int y, int w, int h) {
        g.enableScissor((int)(contentX + x * pageScale), (int)(contentY + (y - scroll) * pageScale),
                (int)Math.ceil(contentX + (x + w) * pageScale), (int)Math.ceil(contentY + (y + h - scroll) * pageScale));
    }
    @Override protected void renderBg(GuiGraphics g, float delta, int mx, int my) {
        frame(g, mx, my, delta);
        if(getRecipeBookComponent().isVisible()) {
            hoveredSlot=null;
            recipeButton.render(g,mx,my,delta);
            return;
        }
        // Draw sections around the same coordinates used for native slot interaction.
        g.fill(leftPos, topPos + 94, leftPos + imageWidth, topPos + imageHeight, 0xA6E0E2D9);
        g.fill(leftPos + 113, topPos + 18, leftPos + 114, topPos + 90, CultivationUi.BORDER);
        g.drawString(font, "装备", leftPos + 8, topPos + 4, CultivationUi.INK, false);
        g.drawString(font, "合成", leftPos + 122, topPos + 12, CultivationUi.INK, false);
        g.drawString(font, "行囊", leftPos + 23, topPos + 96, CultivationUi.INK, false);
        g.drawString(font, "配方", leftPos + 148, topPos + 76, CultivationUi.INK, false);
        CultivationUi.panel(g, leftPos + 30, topPos + 18, 54, 72, true);
        for (var slot : getMenu().slots) {
            if (slot.isActive()) CultivationUi.slot(g, leftPos + slot.x - 1, topPos + slot.y - 1, 18, 18, CultivationUi.BORDER);
        }
        if(!getRecipeBookComponent().isVisible()){
            for(var slot:getMenu().slots){
                if(!(slot instanceof cn.blockforge.generated.generatedmod.robe.EquipmentSlots.EquipmentSlot)||slot.hasItem())continue;
                boolean sword=slot.getContainerSlot()==cn.blockforge.generated.generatedmod.robe.YujianSlot.YUJIAN_SLOT;
                if(mx>=leftPos+slot.x&&mx<leftPos+slot.x+16&&my>=topPos+slot.y&&my<topPos+slot.y+16)
                    CultivationUi.tooltip(Component.literal(sword?"御剑槽":"法衣槽"));
            }
        }
        g.drawString(font, "→", leftPos + 166, topPos + 43, CultivationUi.INK, false);
        g.enableScissor(leftPos + 31, topPos + 19, leftPos + 83, topPos + 89);
        InventoryScreen.renderEntityInInventoryFollowsMouse(g, leftPos + 57, topPos + 88, 30,
                leftPos + 57 - mx, topPos + 38 - my, minecraft.player);
        g.disableScissor();
        var data = GeneratedMod.data(minecraft.player);
        int x = leftPos + imageWidth + 8, w = panelX + panelW - x - 10;
        if (data != null && layout.hasOverview() && !getRecipeBookComponent().isVisible()) {
            int y = contentY + 5;
            CultivationUi.panel(g, x - 5, contentY - 1, w + 8, contentH + 2, true);
            CultivationUi.ornamentFrame(g, x - 7, contentY - 3, w + 12, contentH + 6);
            CultivationUi.rule(g, x, y + 13, w);
            g.drawString(font, "修炼概览", x + 3, y, CultivationUi.GOLD, false);
            summaryRow(g, 0, "境界", Component.literal(CultivationLogic.realmStageName(minecraft.player, data)), x, y + 18, w, CultivationUi.GOLD, mx, my);
            CultivationUi.summaryIcon(g, 1, x + 1, y + 44);
            CultivationUi.bar(g, "修为", x + 22, y + 42, w - 22, data.wuxingxiantu$getCultivation(), CultivationLogic.breakthroughRequirement(data), CultivationUi.GOLD, mx, my);
            CultivationUi.summaryIcon(g, 2, x + 1, y + 69);
            CultivationUi.bar(g, "灵力", x + 22, y + 67, w - 22, data.wuxingxiantu$getCurrentMana(), CultivationLogic.maxAura(data), CultivationUi.CYAN, mx, my);
            var physique = Component.translatable("physique.wuxingxiantu." + data.wuxingxiantu$getPhysique().id());
            summaryRow(g, 3, "体质", physique, x, y + 94, w, CultivationUi.GREEN, mx, my);
            String heart = data.wuxingxiantu$getEquippedHeartMethod();
            Component method = heart == null || heart.isBlank() ? Component.literal("无")
                    : cn.blockforge.generated.generatedmod.HeartMethodCatalog.shortName(heart);
            summaryRow(g, 4, "当前心法", method, x, y + 120, w, CultivationUi.CYAN, mx, my);
            String[] domains = {"金光", CultivationLogic.isThunderForm(data) ? "天雷" : "木灵", "潮汐",
                    CultivationLogic.isExoticForm(data) ? "异火" : "业火", "重力"};
            boolean[] enabled = {data.wuxingxiantu$isGoldDomainEnabled(), data.wuxingxiantu$isWoodDomainEnabled(),
                    data.wuxingxiantu$isWaterDomainEnabled(), data.wuxingxiantu$isFireDomainEnabled(), data.wuxingxiantu$isEarthDomainEnabled()};
            List<String> active = new ArrayList<>();
            for (int i = 0; i < domains.length; i++) if (enabled[i]) active.add(domains[i]);
            summaryRow(g, 5, "当前领域",
                    Component.literal(active.isEmpty() ? "未激活" : String.join("、", active)), x, y + 146, w, CultivationUi.GOLD, mx, my);
        }
    }
    private void summaryRow(GuiGraphics g, int icon, String label, Component value,
                            int x, int y, int w, int color, int mx, int my) {
        CultivationUi.summaryIcon(g, icon, x + 1, y + 3);
        g.drawString(font, label, x + 23, y, CultivationUi.MUTED, false);
        CultivationUi.fit(g, value, x + 23, y + 11, w - 23, color, mx, my);
    }
    @Override protected void renderLabels(GuiGraphics g, int mx, int my) {}
    @Override public void renderBackground(GuiGraphics g) {
        if (minecraft.level != null) g.fill(0, 0, width, height, 0x55000000);
        else super.renderBackground(g);
    }
    @Override public void render(GuiGraphics g, int mx, int my, float delta) {
        if (inventory()) { positionInventory(); super.render(g, mx, my, delta); return; }
        renderBackground(g); frame(g, mx, my, delta);
        g.enableScissor(contentX, contentY, contentX + contentW, contentY + contentH);
        g.pose().pushPose(); g.pose().translate(contentX, contentY - scroll * pageScale, 0); g.pose().scale(pageScale, pageScale, 1);
        page.render(g, inPage(mx, my) ? (int)pageX(mx) : -1000, inPage(mx, my) ? (int)pageY(my) : -1000, delta);
        g.pose().popPose(); g.disableScissor();
        if (page.height > contentViewportHeight()) {
            int track = contentH, thumb = Math.max(14, track * contentViewportHeight() / page.height);
            int y = contentY + (track - thumb) * scroll / Math.max(1, page.height - contentViewportHeight());
            g.fill(contentX + contentW - 3, y, contentX + contentW - 1, y + thumb, CultivationUi.BORDER);
        }
    }
    @Override public boolean mouseClicked(double x, double y, int button) {
        if (close.mouseClicked(x, y, button)) return true;
        // 配方书展开时优先处理自身覆盖区域。
        boolean recipe = inventory() && getRecipeBookComponent().isVisible();
        if (!recipe || !getRecipeBookComponent().isMouseOver(x, y)) {
            for (var nav : navButtons) if (nav.mouseClicked(x, y, button)) return true;
        }
        if (inventory()) {
            if(recipe&&recipeButton.mouseClicked(x,y,button))return true;
            if(recipe) {
                getRecipeBookComponent().mouseClicked(x,y,button);
                positionInventory();
                return true;
            }
            boolean handled = super.mouseClicked(x, y, button);
            positionInventory();
            return handled;
        }
        if (button == 0 && page.height > contentViewportHeight() && x >= contentX + contentW - 6 && x < contentX + contentW && inPage(x, y)) {
            int thumb = Math.max(14, contentH * contentViewportHeight() / page.height);
            int top = contentY + (contentH - thumb) * scroll / Math.max(1, page.height - contentViewportHeight());
            scrollbarGrab = y >= top && y < top + thumb ? y - top : thumb / 2.0;
            draggingScrollbar = true; moveScrollbar(y); return true;
        }
        if (inPage(x, y)) { pointerInPage = true; return page.mouseClicked(pageX(x), pageY(y), button); }
        return false;
    }
    @Override public boolean mouseReleased(double x, double y, int button) {
        if (recipeButtonClicked) { recipeButtonClicked = false; return true; }
        if (inventory()) return super.mouseReleased(x, y, button);
        if (draggingScrollbar) { draggingScrollbar = false; return true; }
        boolean captured = pointerInPage; pointerInPage = false;
        return captured && page.mouseReleased(pageX(x), pageY(y), button);
    }
    @Override public boolean mouseDragged(double x, double y, int button, double dx, double dy) {
        if (draggingScrollbar) { moveScrollbar(y); return true; }
        return inventory() ? super.mouseDragged(x, y, button, dx, dy)
                : pointerInPage && page.mouseDragged(pageX(x), pageY(y), button, dx / pageScale, dy / pageScale);
    }
    private void moveScrollbar(double y) {
        int thumb = Math.max(14, contentH * contentViewportHeight() / page.height);
        scroll = (int)Math.max(0, Math.min(page.height - contentViewportHeight(),
                (y - contentY - scrollbarGrab) * (page.height - contentViewportHeight()) / Math.max(1, contentH - thumb)));
    }
    @Override public boolean mouseScrolled(double x, double y, double delta) {
        if (inventory()) return super.mouseScrolled(x, y, delta);
        if (!inPage(x, y)) return false;
        if (page.mouseScrolled(pageX(x), pageY(y), delta)) return true;
        scroll = Math.max(0, Math.min(Math.max(0, page.height - contentViewportHeight()), scroll - (int) (delta * 24)));
        return true;
    }
    @Override public boolean keyPressed(int key, int scan, int mods) {
        if (key == 258 && (mods & 2) != 0 && getMenu().getCarried().isEmpty() && !isQuickCrafting) {
            int index = 0;
            for (int i = 0; i < navigation.size(); i++) if (navigation.get(i).id().equals(pageId)) index = i;
            int next = Math.floorMod(index + ((mods & 1) != 0 ? -1 : 1), navigation.size());
            selectPage(navigation.get(next).id());
            return true;
        }
        if (inventory()) return super.keyPressed(key, scan, mods);
        if (key == 256 || minecraft.options.keyInventory.matches(key, scan)
                && !(page.getFocused() instanceof net.minecraft.client.gui.components.EditBox)) { onClose(); return true; }
        return page.keyPressed(key, scan, mods);
    }
    @Override public boolean charTyped(char ch, int mods) {
        return inventory() ? super.charTyped(ch, mods) : page.charTyped(ch, mods);
    }
    @Override public boolean keyReleased(int key, int scan, int mods) {
        return inventory() ? super.keyReleased(key, scan, mods) : page.keyReleased(key, scan, mods);
    }
    @Override public void mouseMoved(double x, double y) {
        if (inventory()) super.mouseMoved(x, y);
        else page.mouseMoved(pageX(x), pageY(y));
    }
    @Override public void resize(Minecraft client, int w, int h) {
        int previousScroll = scroll;
        super.resize(client, w, h);
        if (page != null) {
            page.resize(client, pageWidth(), page.contentHeight(pageWidth(), contentViewportHeight()));
            scroll = Math.min(previousScroll, Math.max(0, page.height - contentViewportHeight()));
        }
    }
    @Override protected boolean hasClickedOutside(double x, double y, int left, int top, int button) {
        if (x >= panelX && x < panelX + panelW && y >= panelY && y < panelY + panelH) return false;
        return super.hasClickedOutside(x, y, left, top, button);
    }
    @Override public void removed() { restoreInventorySlots(); if (page != null) page.removed(); super.removed(); }
}
