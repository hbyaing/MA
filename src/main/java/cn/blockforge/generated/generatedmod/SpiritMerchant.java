package cn.blockforge.generated.generatedmod;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

import java.util.BitSet;
import java.util.OptionalInt;
import java.util.UUID;

/** 不依附实体的灵魂交易商；收藏状态写回打开界面的那颗拘魂珠。 */
public final class SpiritMerchant implements Merchant {
    private final MerchantOffers offers = new MerchantOffers();
    private final UUID pearlId;
    private Player tradingPlayer;

    private SpiritMerchant(ItemStack pearl) {
        pearlId = SoulPearlData.ensurePearlId(pearl);
        reloadOffers(pearl);
    }

    public static void open(ServerPlayer player, ItemStack pearl) {
        SpiritMerchant merchant = new SpiritMerchant(pearl);
        merchant.setTradingPlayer(player);
        OptionalInt opened = player.openMenu(new SimpleMenuProvider(
                (containerId, inventory, menuPlayer) -> new SoulTradeMenu(containerId, inventory, merchant),
                Component.translatable("screen.wuxingxiantu.soul_trade")));
        if (opened.isEmpty()) {
            merchant.setTradingPlayer(null);
            return;
        }
        int containerId = opened.getAsInt();
        if (player.containerMenu instanceof SoulTradeMenu menu && menu.containerId == containerId) {
            menu.sendState(player, false);
        }
    }

    public boolean toggleFavorite(int offerIndex) {
        ItemStack pearl = currentPearl();
        if (pearl.isEmpty() || offerIndex < 0 || offerIndex >= offers.size()) return false;
        SoulPearlData.toggleFavorite(pearl, offers.get(offerIndex));
        reloadOffers(pearl);
        if (tradingPlayer != null) tradingPlayer.getInventory().setChanged();
        return true;
    }

    public MerchantOffers copyOffers() {
        MerchantOffers copy = new MerchantOffers();
        for (MerchantOffer offer : offers) copy.add(new MerchantOffer(offer.createTag()));
        return copy;
    }

    public BitSet favoriteFlags() {
        BitSet flags = new BitSet(offers.size());
        ItemStack pearl = currentPearl();
        if (pearl.isEmpty()) return flags;
        for (int i = 0; i < offers.size(); i++) {
            if (SoulPearlData.isFavorite(pearl, offers.get(i))) flags.set(i);
        }
        return flags;
    }

    private void reloadOffers(ItemStack pearl) {
        offers.clear();
        offers.addAll(SoulPearlData.offers(pearl));
    }

    private ItemStack currentPearl() {
        if (tradingPlayer == null) return ItemStack.EMPTY;
        for (int slot = 0; slot < tradingPlayer.getInventory().getContainerSize(); slot++) {
            ItemStack candidate = tradingPlayer.getInventory().getItem(slot);
            if (SoulPearlData.hasPearlId(candidate, pearlId)) return candidate;
        }
        if (tradingPlayer.containerMenu != null) {
            ItemStack carried = tradingPlayer.containerMenu.getCarried();
            if (SoulPearlData.hasPearlId(carried, pearlId)) return carried;
            for (var slot : tradingPlayer.containerMenu.slots) {
                if (SoulPearlData.hasPearlId(slot.getItem(), pearlId)) return slot.getItem();
            }
        }
        return ItemStack.EMPTY;
    }

    @Override public void setTradingPlayer(Player player) { tradingPlayer = player; }
    @Override public Player getTradingPlayer() { return tradingPlayer; }
    @Override public MerchantOffers getOffers() { return offers; }
    @Override public void overrideOffers(MerchantOffers replacement) {
        offers.clear();
        offers.addAll(replacement);
    }
    @Override public void notifyTrade(MerchantOffer offer) { offer.resetUses(); }
    @Override public void notifyTradeUpdated(ItemStack stack) {}
    @Override public int getVillagerXp() { return 0; }
    @Override public void overrideXp(int xp) {}
    @Override public boolean showProgressBar() { return false; }
    @Override public SoundEvent getNotifyTradeSound() { return SoundEvents.VILLAGER_YES; }
    @Override public boolean canRestock() { return false; }
    @Override public boolean isClientSide() { return tradingPlayer != null && tradingPlayer.level().isClientSide; }
}
