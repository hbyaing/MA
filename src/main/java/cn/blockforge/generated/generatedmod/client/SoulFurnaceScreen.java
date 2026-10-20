package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.Network;
import cn.blockforge.generated.generatedmod.SoulFurnaceBlockEntity;
import cn.blockforge.generated.generatedmod.SoulFurnaceMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** 炼魂炉与化魄炉的共用界面；机器槽位和玩家物品栏严格使用菜单坐标。 */
public final class SoulFurnaceScreen extends AbstractContainerScreen<SoulFurnaceMenu> {
    private static final int WIDTH = 220;
    private static final int HEIGHT = 210;
    private static final Palette SOUL = new Palette(
            0xF0181A25, 0xFF9A6BE0, 0x885D477A, 0xFF302C42, 0xFF75658C,
            0xFFB85CFF, 0xFFFF8B35, 0xFFFFD98A, 0xFFD4C3EA, 0xFF9F90B7);
    /** 化魄炉贴图采用铜锈青绿、旧铜边框和暗青铜底色。 */
    private static final Palette DISMANTLING = new Palette(
            0xF0162420, 0xFF78A082, 0x88546E5C, 0xFF263A31, 0xFF6E8E78,
            0xFF65B58E, 0xFFD98A46, 0xFFD2BA7C, 0xFFC6D9CB, 0xFF88A393);
    private EditBox cycleInput;

    public SoulFurnaceScreen(SoulFurnaceMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = WIDTH;
        imageHeight = HEIGHT;
        titleLabelX = 10;
        titleLabelY = 7;
        inventoryLabelX = SoulFurnaceMenu.INVENTORY_X;
        inventoryLabelY = 112;
    }

    @Override
    protected void init() {
        super.init();
        cycleInput = null;
        if (!menu.isRefining()) return;
        cycleInput = new EditBox(font, leftPos + SoulFurnaceMenu.CYCLE_INPUT_X,
                topPos + SoulFurnaceMenu.CYCLE_Y, SoulFurnaceMenu.CYCLE_WIDTH,
                SoulFurnaceMenu.CYCLE_HEIGHT,
                Component.translatable("gui.wuxingxiantu.soul_furnace.cycle_input"));
        cycleInput.setValue(Integer.toString(menu.cycleCount()));
        cycleInput.setMaxLength(2);
        cycleInput.setBordered(true);
        cycleInput.setFilter(value -> value.isEmpty() || (value.length() <= 2
                && value.chars().allMatch(character -> character >= '0' && character <= '9')));
        addRenderableWidget(cycleInput);
        addRenderableWidget(new UiButton(
                leftPos + SoulFurnaceMenu.CYCLE_BUTTON_X, topPos + SoulFurnaceMenu.CYCLE_Y,
                SoulFurnaceMenu.CYCLE_BUTTON_WIDTH, SoulFurnaceMenu.CYCLE_HEIGHT,
                Component.translatable("gui.wuxingxiantu.soul_furnace.cycle_apply"),
                button -> applyCycleCount(), SOUL));
    }

    private void applyCycleCount() {
        if (cycleInput == null) return;
        int cycles;
        try {
            cycles = Integer.parseInt(cycleInput.getValue());
        } catch (NumberFormatException ignored) {
            cycles = menu.cycleCount();
        }
        cycles = Math.max(1, Math.min(SoulFurnaceBlockEntity.MAX_CYCLES, cycles));
        cycleInput.setValue(Integer.toString(cycles));
        Network.CHANNEL.sendToServer(new cn.blockforge.generated.generatedmod.network.packet.cultivation.SoulFurnaceCyclePacket(menu.blockPos(), cycles));
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        Palette palette = palette();
        int left = leftPos;
        int top = topPos;
        graphics.fill(left, top, left + imageWidth, top + imageHeight, palette.panel());
        graphics.renderOutline(left, top, imageWidth, imageHeight, palette.border());
        graphics.renderOutline(left + 3, top + 3, imageWidth - 6, imageHeight - 6, palette.innerBorder());

        drawSlot(graphics, left + SoulFurnaceMenu.PEARL_X - 1, top + SoulFurnaceMenu.PEARL_Y - 1, palette);
        drawSlot(graphics, left + SoulFurnaceMenu.FUEL_X - 1, top + SoulFurnaceMenu.FUEL_Y - 1, palette);
        int outputSlots = SoulFurnaceMenu.visibleOutputSlotCount(menu.kind());
        for (int i = 0; i < outputSlots; i++) {
            drawSlot(graphics, left + SoulFurnaceMenu.outputX(menu.kind(), i) - 1,
                    top + SoulFurnaceMenu.outputY(menu.kind(), i) - 1, palette);
        }

        int progressLeft = left + 83;
        int progressTop = top + 43;
        int progressWidth = 36;
        int progressHeight = 48;
        graphics.fill(progressLeft, progressTop, progressLeft + progressWidth, progressTop + progressHeight,
                darken(palette.slot(), 18));
        graphics.renderOutline(progressLeft, progressTop, progressWidth, progressHeight, palette.slotBorder());
        int filled = Math.round((progressHeight - 6) * menu.progressScale());
        if (filled > 0) {
            graphics.fill(progressLeft + 3, progressTop + progressHeight - 3 - filled,
                    progressLeft + progressWidth - 3, progressTop + progressHeight - 3, palette.progress());
        }

        int burnHeight = Math.min(16, Math.round(16.0F * Math.min(1.0F, menu.burnTime() / 200.0F)));
        graphics.fill(left + 70, top + 74, left + 76, top + 90, darken(palette.slot(), 22));
        graphics.renderOutline(left + 70, top + 74, 6, 16, palette.slotBorder());
        if (burnHeight > 0) {
            graphics.fill(left + 72, top + 89 - burnHeight, left + 74, top + 89, palette.fire());
        }

        CultivationUi.softDivider(graphics,left+10,top+108,imageWidth-20,false,(palette.innerBorder()&0xFFFFFF)|0x74000000);
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                drawSlot(graphics,
                        left + SoulFurnaceMenu.INVENTORY_X + column * 18 - 1,
                        top + SoulFurnaceMenu.INVENTORY_Y + row * 18 - 1,
                        palette);
            }
        }
        for (int column = 0; column < 9; column++) {
            drawSlot(graphics,
                    left + SoulFurnaceMenu.INVENTORY_X + column * 18 - 1,
                    top + SoulFurnaceMenu.HOTBAR_Y - 1,
                    palette);
        }

        graphics.drawCenteredString(font, Component.translatable(menu.isRefining()
                        ? "gui.wuxingxiantu.soul_furnace.binding_pearl"
                        : "gui.wuxingxiantu.soul_furnace.capture_pearl"),
                left + SoulFurnaceMenu.PEARL_X + 9, top + 29, palette.text());
        graphics.drawCenteredString(font, Component.translatable("gui.wuxingxiantu.soul_furnace.fuel"),
                left + SoulFurnaceMenu.FUEL_X + 8, top + 96, palette.text());
        Component output = Component.translatable("gui.wuxingxiantu.soul_furnace.output");
        int outputCenter = menu.isRefining() ? 163 : SoulFurnaceMenu.OUTPUT_GRID_X + 27;
        graphics.drawCenteredString(font, output, left + outputCenter,
                top + (menu.isRefining() ? 40 : 29), palette.text());
        if (menu.isRefining()) {
            graphics.drawString(font, Component.translatable("gui.wuxingxiantu.soul_furnace.cycle"),
                    left + SoulFurnaceMenu.CYCLE_LABEL_X, top + SoulFurnaceMenu.CYCLE_LABEL_Y,
                    palette.text(), false);
            graphics.drawString(font, Component.translatable(
                            "gui.wuxingxiantu.soul_furnace.remaining", menu.remainingCycles()),
                    left + SoulFurnaceMenu.CYCLE_LABEL_X, top + 27, palette.idle(), false);
        }
        graphics.drawCenteredString(font, Component.translatable(menu.isProcessing()
                        ? "gui.wuxingxiantu.soul_furnace.processing"
                        : "gui.wuxingxiantu.soul_furnace.idle"),
                left + 101, top + 96, menu.isProcessing() ? palette.progress() : palette.idle());
    }

    private static void drawSlot(GuiGraphics graphics, int x, int y, Palette palette) {
        graphics.fill(x, y, x + 18, y + 18, palette.slot());
        graphics.renderOutline(x, y, 18, 18, palette.slotBorder());
    }

    private Palette palette() {
        return menu.kind() == SoulFurnaceBlockEntity.Kind.SPIRIT_DISMANTLING ? DISMANTLING : SOUL;
    }

    private static int darken(int argb, int amount) {
        int a = argb >>> 24;
        int r = Math.max(0, ((argb >>> 16) & 255) - amount);
        int g = Math.max(0, ((argb >>> 8) & 255) - amount);
        int b = Math.max(0, (argb & 255) - amount);
        return a << 24 | r << 16 | g << 8 | b;
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        Palette palette = palette();
        graphics.drawString(font, title, titleLabelX, titleLabelY, palette.title(), false);
        graphics.drawString(font, playerInventoryTitle,
                inventoryLabelX, inventoryLabelY, palette.text(), false);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 257 && cycleInput != null && cycleInput.isFocused()) {
            applyCycleCount();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private record Palette(int panel, int border, int innerBorder, int slot, int slotBorder,
                           int progress, int fire, int title, int text, int idle) {}

    /** 与界面槽位同款的 UI 风格按键，替代原版按钮贴图。 */
    private static final class UiButton extends Button {
        private final Palette palette;

        private UiButton(int x, int y, int width, int height, Component message, OnPress onPress,
                         Palette palette) {
            super(Button.builder(message, onPress).bounds(x, y, width, height));
            this.palette = palette;
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
            int background = isHoveredOrFocused() ? darken(palette.slot(), 8) : palette.slot();
            graphics.fill(getX(), getY(), getX() + width, getY() + height, background);
            graphics.renderOutline(getX(), getY(), width, height, palette.slotBorder());
            // 顶部内高光，模仿机器槽位的倒角质感。
            graphics.fill(getX() + 1, getY() + 1, getX() + width - 1, getY() + 2, palette.innerBorder());
            // 文字在按键框内水平+垂直居中。
            int textX = getX() + Math.max(1, (width - Minecraft.getInstance().font.width(getMessage())) / 2);
            int textY = getY() + Math.max(0, (height - Minecraft.getInstance().font.lineHeight) / 2);
            graphics.drawString(Minecraft.getInstance().font, getMessage(), textX, textY,
                    palette.text(), false);
        }
    }
}
