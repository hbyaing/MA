package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.CultivationData;
import cn.blockforge.generated.generatedmod.DivineArtLogic;
import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.Network;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/** 七十二变发动时的形态选择界面；显示本地化生灵名称，服务端负责最终校验索引。
 * 顶部新增搜索栏：按本地化名称或形态 ID 做不区分大小写的模糊（子串）匹配。 */
public final class TransformSelectionScreen extends Screen {
    private static final int PANEL = 0xE8171A20;
    private static final int PANEL_ALT = 0xD02B2525;
    private static final int GOLD = 0xFFE8C477;
    private static final int PINK = 0xFFFF69C8;
    private static final int TEXT = 0xFFF4E8D0;
    private static final int MUTED = 0xFFC8B99C;
    private static final int RED = 0xFFDE837A;
    private static final int ROW_HEIGHT = 24;
    private static final int COLUMN_GAP = 6;
    private static final int MAX_COLUMNS = 2;

    private static final int Y_TITLE = 10;
    private static final int Y_HINT = 28;
    private static final int Y_CURRENT = 46;
    private static final int Y_SEARCH = 64;
    private static final int Y_BUTTONS = 88;
    private static final int SEARCH_HEIGHT = 18;

    private int scrollOffset;
    private int visibleRows;
    private int visibleColumns;
    /** 玩家已记录的全部形态（原生索引）。 */
    private String[] forms = new String[0];
    /** 过滤后仍可见的 forms 原生索引（按 searchQuery 模糊匹配）。 */
    private int[] filteredIndices = new int[0];
    private EditBox searchBox;
    private String searchQuery = "";
    private boolean selected;

    public TransformSelectionScreen() {
        super(Component.translatable("gui.wuxingxiantu.transform_selection.title"));
    }

    @Override
    protected void init() {
        // 搜索框与列表统一由 rebuildTransformWidgets 组装；这里只触发首次构建。
        rebuildTransformWidgets();
        setInitialFocus(searchBox);
    }

    private void refreshForms() {
        CultivationData data = GeneratedMod.data(minecraft.player);
        forms = data == null ? new String[0] : data.wuxingxiantu$getPossessedForms();
        rebuildFilteredIndices();
        visibleColumns = width >= 430 ? MAX_COLUMNS : 1;
        int panelHeight = Math.min(height - 20, 360);
        visibleRows = Math.max(1, (panelHeight - Y_BUTTONS - 48) / ROW_HEIGHT);
        int pageSize = Math.max(1, visibleRows * visibleColumns);
        int maxOffset = Math.max(0, filteredIndices.length - pageSize);
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxOffset));
    }

    /** 按搜索词对 forms 做不区分大小写的子串过滤；空关键词显示全部。 */
    private void rebuildFilteredIndices() {
        List<Integer> indices = new ArrayList<>();
        String query = searchQuery == null ? "" : searchQuery.trim().toLowerCase();
        for (int i = 0; i < forms.length; i++) {
            if (query.isEmpty()) {
                indices.add(i);
                continue;
            }
            String display = DivineArtLogic.formDisplayName(forms[i]).getString().toLowerCase();
            String id = forms[i].toLowerCase();
            if (display.contains(query) || id.contains(query)) indices.add(i);
        }
        filteredIndices = new int[indices.size()];
        for (int i = 0; i < indices.size(); i++) filteredIndices[i] = indices.get(i);
    }

    private int panelWidth() {
        return Math.min(560, Math.max(280, width - 20));
    }

    private int panelHeight() {
        int rows = Math.max(1, Math.min(visibleRows, (filteredIndices.length + visibleColumns - 1) / visibleColumns));
        return Math.min(height - 12, Math.max(170, Y_BUTTONS + rows * ROW_HEIGHT + 48));
    }

    private int panelLeft() { return (width - panelWidth()) / 2; }
    private int panelTop() { return Math.max(6, (height - panelHeight()) / 2); }

    private void rebuildTransformWidgets() {
        boolean searchFocused = searchBox != null && searchBox.isFocused();
        clearWidgets();
        refreshForms();
        int left = panelLeft();
        int top = panelTop();
        // 搜索框在 clearWidgets() 时被一并清掉了；这里把同一对象重新挂回渲染列表，
        // 其 responder（重建列表）仍在对象上，输入时不会被反复销毁。若输入前它正获得焦点，
        // 重建后要立刻把焦点还给它，否则打一个字就会失去焦点、后续字符无法输入。
        if (searchBox == null) {
            searchBox = new EditBox(font, left + 14, top + Y_SEARCH, panelWidth() - 28, SEARCH_HEIGHT,
                    Component.translatable("gui.wuxingxiantu.transform_selection.search"));
            searchBox.setMaxLength(64);
            searchBox.setValue(searchQuery);
            searchBox.setResponder(text -> {
                searchQuery = text == null ? "" : text;
                scrollOffset = 0;
                rebuildTransformWidgets();
            });
        }
        addRenderableWidget(searchBox);
        if (searchFocused) {
            setFocused(searchBox);
            searchBox.setFocused(true);
        }
        int contentLeft = left + 14;
        int contentWidth = panelWidth() - 28;
        int gap = visibleColumns == 2 ? COLUMN_GAP : 0;
        // 形态名字不长，按键按需收窄即可，不必拉满整栏；整列在内容区居中。
        int buttonWidth = Math.min(176, (contentWidth - gap) / visibleColumns);
        int gridWidth = buttonWidth * visibleColumns + gap * Math.max(0, visibleColumns - 1);
        int gridLeft = contentLeft + Math.max(0, (contentWidth - gridWidth) / 2);
        int pageSize = Math.max(1, visibleRows * visibleColumns);
        int count = Math.min(pageSize, Math.max(0, filteredIndices.length - scrollOffset));
        for (int local = 0; local < count; local++) {
            int actual = scrollOffset + local;
            int original = filteredIndices[actual];
            int column = local % visibleColumns;
            int row = local / visibleColumns;
            Component label = Component.literal((original + 1) + ". ")
                    .append(DivineArtLogic.formDisplayName(forms[original]));
            addRenderableWidget(InkButton.flat(gridLeft + column * (buttonWidth + gap),
                    top + Y_BUTTONS + row * ROW_HEIGHT, buttonWidth, 20, label,
                    button -> select(original), PINK));
        }

        int navigationY = top + panelHeight() - 28;
        int buttonWidthSmall = 62;
        if (scrollOffset > 0) {
            addRenderableWidget(new InkButton(left + 14, navigationY, buttonWidthSmall, 20,
                    Component.translatable("gui.wuxingxiantu.transform_selection.previous"), button -> {
                        scrollOffset = Math.max(0, scrollOffset - Math.max(1, visibleRows * visibleColumns));
                        rebuildTransformWidgets();
                    }, PINK));
        }
        int pageSizeForNavigation = Math.max(1, visibleRows * visibleColumns);
        if (scrollOffset + pageSizeForNavigation < filteredIndices.length) {
            addRenderableWidget(new InkButton(left + 14 + buttonWidthSmall + 6, navigationY, buttonWidthSmall, 20,
                    Component.translatable("gui.wuxingxiantu.transform_selection.next"), button -> {
                        scrollOffset = Math.min(Math.max(0, filteredIndices.length - pageSizeForNavigation),
                                scrollOffset + pageSizeForNavigation);
                        rebuildTransformWidgets();
                    }, PINK));
        }
        addRenderableWidget(new InkButton(left + panelWidth() - 104, navigationY, 90, 20,
                Component.translatable("gui.wuxingxiantu.transform_selection.close"),
                button -> onClose(), PINK));
    }

    private void select(int index) {
        if (index < 0 || index >= forms.length) return;
        selected = true;
        Network.CHANNEL.sendToServer(new cn.blockforge.generated.generatedmod.network.packet.divineart.TransformSelectPacket(index));
        onClose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderBackground(graphics);
        graphics.fill(0, 0, width, height, 0x66100D18);
        refreshForms();
        int left = panelLeft();
        int top = panelTop();
        int panelWidth = panelWidth();
        int panelHeight = panelHeight();
        graphics.fill(left, top, left + panelWidth, top + panelHeight, PANEL);
        graphics.renderOutline(left, top, panelWidth, panelHeight, PINK);
        graphics.fill(left + 1, top + 1, left + panelWidth - 1, top + 4, 0xFF8D4B78);
        graphics.drawCenteredString(font, title, width / 2, top + Y_TITLE, PINK);
        graphics.drawCenteredString(font,
                Component.translatable("gui.wuxingxiantu.transform_selection.hint"), width / 2, top + Y_HINT, MUTED);

        CultivationData data = GeneratedMod.data(minecraft.player);
        String active = data == null ? "" : data.wuxingxiantu$getActivePossessedForm();
        Component current = active == null || active.isBlank()
                ? Component.translatable("gui.wuxingxiantu.transform_selection.none")
                : Component.translatable("gui.wuxingxiantu.transform_selection.current",
                        DivineArtLogic.formDisplayName(active));
        graphics.drawCenteredString(font, current, width / 2, top + Y_CURRENT, GOLD);

        if (filteredIndices.length == 0) {
            Component msg = forms.length == 0
                    ? Component.translatable("gui.wuxingxiantu.transform_selection.empty")
                    : Component.translatable("gui.wuxingxiantu.transform_selection.no_match");
            graphics.drawCenteredString(font, msg, width / 2, top + 102, RED);
        } else {
            int pageSize = Math.max(1, visibleRows * visibleColumns);
            graphics.drawString(font, Component.translatable("gui.wuxingxiantu.transform_selection.progress",
                    Math.min(filteredIndices.length, scrollOffset + 1), filteredIndices.length), left + 14,
                    top + panelHeight - 48, MUTED, false);
        }
        // 搜索栏、形态按钮与导航按钮都是 widget，由 super.render 统一绘制在面板之上。
        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int pageSize = Math.max(1, visibleRows * visibleColumns);
        int maxOffset = Math.max(0, filteredIndices.length - pageSize);
        int next = Math.max(0, Math.min(maxOffset, scrollOffset - (int) Math.signum(delta) * pageSize));
        if (next != scrollOffset) {
            scrollOffset = next;
            rebuildTransformWidgets();
        }
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        if (!selected) {
            while (WuXingKeybinds.TRANSFORM_TOGGLE.consumeClick()) {
                // 丢弃打开界面期间缓存的切换按键，避免关闭界面后下一 tick 再次提交发动请求。
            }
            Network.CHANNEL.sendToServer(new cn.blockforge.generated.generatedmod.network.packet.divineart.TransformCancelPacket());
        }
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
