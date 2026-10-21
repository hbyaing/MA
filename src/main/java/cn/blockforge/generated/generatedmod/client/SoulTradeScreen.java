package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.Network;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.trading.MerchantOffers;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/** 原版村民交易界面的拘魂交易扩展：每个可见交易行左侧都有独立收藏星标。 */
public final class SoulTradeScreen extends MerchantScreen {
    private static final int VISIBLE_ROWS = 7;
    private static final int ROW_HEIGHT = 20;
    private static final int STAR_SIZE = 14;
    private static final Field SELECTED_OFFER_FIELD = instanceIntField(0);
    private static final Field SCROLL_OFFSET_FIELD = instanceIntField(1);
    private static PendingState pendingState;

    private BitSet favorites = new BitSet();

    public SoulTradeScreen(MerchantMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        if (pendingState != null && pendingState.containerId() == getMenu().containerId) {
            PendingState state = pendingState;
            pendingState = null;
            applyFavoriteState(state.favorites(), state.resetScroll());
        }
    }

    public static void receiveFavoriteState(int containerId, BitSet favorites, boolean resetScroll) {
        Minecraft minecraft = Minecraft.getInstance();
        BitSet copy = (BitSet) favorites.clone();
        if (minecraft.screen instanceof SoulTradeScreen screen
                && screen.getMenu().containerId == containerId) {
            screen.applyFavoriteState(copy, resetScroll);
        } else {
            pendingState = new PendingState(containerId, copy, resetScroll);
        }
    }

    private void applyFavoriteState(BitSet updatedFavorites, boolean resetScroll) {
        favorites = (BitSet) updatedFavorites.clone();
        if (!resetScroll) return;
        setPrivateInt(SELECTED_OFFER_FIELD, 0);
        setPrivateInt(SCROLL_OFFSET_FIELD, 0);
        getMenu().setSelectionHint(0);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderFavoriteStars(graphics, mouseX, mouseY);
    }

    private void renderFavoriteStars(GuiGraphics graphics, int mouseX, int mouseY) {
        MerchantOffers offers = getMenu().getOffers();
        int offset = scrollOffset();
        int visible = Math.min(VISIBLE_ROWS, Math.max(0, offers.size() - offset));
        for (int row = 0; row < visible; row++) {
            int offerIndex = offset + row;
            int x = starX();
            int y = starY(row);
            boolean hovered = inside(mouseX, mouseY, x, y, STAR_SIZE, STAR_SIZE);
            boolean favorite = favorites.get(offerIndex);
            graphics.fill(x, y, x + STAR_SIZE, y + STAR_SIZE,
                    hovered ? 0xE04A3A24 : 0xD0221D18);
            graphics.renderOutline(x, y, STAR_SIZE, STAR_SIZE,
                    favorite ? 0xFFFFD35A : hovered ? 0xFFE0B46C : 0xFF746858);
            graphics.drawCenteredString(font, favorite ? "★" : "☆",
                    x + STAR_SIZE / 2, y + 3, favorite ? 0xFFFFD35A : 0xFFC9BDA7);
        }

        int hoveredRow = favoriteRowAt(mouseX, mouseY);
        if (hoveredRow >= 0) {
            int index = offset + hoveredRow;
            graphics.renderTooltip(font, Component.translatable(favorites.get(index)
                    ? "gui.wuxingxiantu.soul_trade.unfavorite"
                    : "gui.wuxingxiantu.soul_trade.favorite"), mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int row = favoriteRowAt(mouseX, mouseY);
            if (row >= 0) {
                int offerIndex = scrollOffset() + row;
                if (offerIndex >= 0 && offerIndex < getMenu().getOffers().size()) {
                    Network.CHANNEL.sendToServer(
                            new cn.blockforge.generated.generatedmod.network.packet.cultivation.SoulTradeFavoritePacket(getMenu().containerId, offerIndex));
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void removed() {
        if (pendingState != null && pendingState.containerId() == getMenu().containerId) {
            pendingState = null;
        }
        super.removed();
    }

    private int favoriteRowAt(double mouseX, double mouseY) {
        int visible = Math.min(VISIBLE_ROWS,
                Math.max(0, getMenu().getOffers().size() - scrollOffset()));
        for (int row = 0; row < visible; row++) {
            if (inside(mouseX, mouseY, starX(), starY(row), STAR_SIZE, STAR_SIZE)) return row;
        }
        return -1;
    }

    private int starX() {
        return getGuiLeft() - STAR_SIZE - 3;
    }

    private int starY(int row) {
        return getGuiTop() + 20 + row * ROW_HEIGHT;
    }

    private int scrollOffset() {
        if (SCROLL_OFFSET_FIELD == null) return 0;
        try {
            int max = Math.max(0, getMenu().getOffers().size() - VISIBLE_ROWS);
            return Mth.clamp(SCROLL_OFFSET_FIELD.getInt(this), 0, max);
        } catch (IllegalAccessException ignored) {
            return 0;
        }
    }

    private void setPrivateInt(Field field, int value) {
        if (field == null) return;
        try {
            field.setInt(this, value);
        } catch (IllegalAccessException ignored) {
            // 字段访问失败时仍可继续交易，仅不强制重置视觉选择。
        }
    }

    private static Field instanceIntField(int index) {
        List<Field> fields = new ArrayList<>();
        for (Field field : MerchantScreen.class.getDeclaredFields()) {
            if (field.getType() == int.class && !Modifier.isStatic(field.getModifiers())) fields.add(field);
        }
        if (index < 0 || index >= fields.size()) return null;
        Field field = fields.get(index);
        field.setAccessible(true);
        return field;
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private record PendingState(int containerId, BitSet favorites, boolean resetScroll) {
        private PendingState {
            favorites = (BitSet) favorites.clone();
        }
    }
}
