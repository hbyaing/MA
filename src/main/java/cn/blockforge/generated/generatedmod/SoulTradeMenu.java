package cn.blockforge.generated.generatedmod;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantMenu;

/** 服务端拘魂交易菜单：验证收藏请求，并把重排后的交易与收藏状态同步给客户端。 */
public final class SoulTradeMenu extends MerchantMenu {
    private final SpiritMerchant merchant;

    public SoulTradeMenu(int containerId, Inventory inventory, SpiritMerchant merchant) {
        super(containerId, inventory, merchant);
        this.merchant = merchant;
    }

    public void handleFavoriteAction(ServerPlayer player, int offerIndex) {
        if (!stillValid(player)) return;
        boolean reordered = offerIndex >= 0 && merchant.toggleFavorite(offerIndex);
        if (reordered) {
            setSelectionHint(0);
            broadcastChanges();
        }
        sendState(player, reordered);
    }

    public void sendState(ServerPlayer player, boolean resetScroll) {
        player.sendMerchantOffers(containerId, merchant.getOffers(), 5, 0, false, false);
        Network.sendToPlayer(player,
                new cn.blockforge.generated.generatedmod.network.packet.cultivation.SoulTradeStatePacket(containerId, merchant.favoriteFlags(), resetScroll));
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        merchant.setTradingPlayer(null);
    }
}
