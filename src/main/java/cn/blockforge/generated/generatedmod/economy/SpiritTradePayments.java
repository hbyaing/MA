package cn.blockforge.generated.generatedmod.economy;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MerchantContainer;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import java.util.List;

/** Reserve a vanilla trade's exact current price, exchanging currency without consuming a purchase. */
public final class SpiritTradePayments {
    private SpiritTradePayments() {}

    public static boolean supported(MerchantOffer offer) {
        return offer != null && SpiritStoneGrade.of(offer.getCostA().getItem()) != null
                && !offer.getCostA().hasTag() && offer.getCostB().isEmpty();
    }

    /** True means this is a supported server-side selection, including an atomic rejected reservation. */
    public static boolean select(MerchantMenu menu,int index) {
        if(index<0 || index>=menu.getOffers().size())return false;
        MerchantOffer offer=menu.getOffers().get(index);
        if(!supported(offer))return false;
        Inventory inventory=null;
        for(Slot slot:menu.slots)if(slot.container instanceof Inventory found){inventory=found;break;}
        if(inventory==null || !(inventory.player instanceof ServerPlayer player)
                || player.containerMenu!=menu || !menu.stillValid(player))return false;
        if(offer.isOutOfStock())return true;
        reserve(inventory,menu.getSlot(0).container,offer.getCostA());
        return true;
    }

    /** Both payment slots are returned on copies before currency is reserved; rejection changes nothing. */
    public static boolean reserve(Inventory inventory,Container payments,ItemStack cost) {
        SpiritStoneGrade grade=SpiritStoneGrade.of(cost.getItem());
        if(grade==null || cost.hasTag() || cost.isEmpty() || cost.getCount()>cost.getMaxStackSize())return false;
        Inventory simulated=new Inventory(inventory.player);
        for(int i=0;i<36;i++)simulated.setItem(i,inventory.getItem(i).copy());
        for(int i=0;i<2;i++)if(!insert(simulated,payments.getItem(i)))return false;
        if(!SpiritCurrency.pay(simulated,(long)grade.value*cost.getCount()))return false;
        for(int i=0;i<36;i++)inventory.setItem(i,simulated.getItem(i));
        // Empty the second input first; vanilla recomputes the output from the fully committed reservation.
        payments.setItem(1,ItemStack.EMPTY);
        payments.setItem(0,cost.copy());
        inventory.setChanged();payments.setChanged();
        return true;
    }

    private static boolean insert(Inventory inventory,ItemStack source) {
        ItemStack left=source.copy();
        for(int pass=0;pass<2;pass++)for(int i=0;i<36&&!left.isEmpty();i++){
            ItemStack target=inventory.getItem(i);
            if(pass==0&&(target.isEmpty()||!ItemStack.isSameItemSameTags(target,left)))continue;
            if(pass==1&&!target.isEmpty())continue;
            int moved=Math.min(left.getCount(),Math.min(64,left.getMaxStackSize())-target.getCount());
            if(moved<=0)continue;
            if(target.isEmpty())inventory.setItem(i,left.copyWithCount(moved));else target.grow(moved);
            left.shrink(moved);
        }
        return left.isEmpty();
    }

    public static long inputValue(Slot result) {
        return SpiritCurrency.value(List.of(result.container.getItem(0),result.container.getItem(1)));
    }

    public static MerchantOffer offerBeforeTake(Slot result,net.minecraft.world.entity.player.Player player) {
        if(!(player instanceof ServerPlayer) || !(player.containerMenu instanceof MerchantMenu menu)
                || menu.getSlot(2)!=result || !(result.container instanceof MerchantContainer payments))return null;
        MerchantOffer offer=payments.getActiveOffer();
        return supported(offer)&&!offer.isOutOfStock()
                &&offer.satisfiedBy(payments.getItem(0),payments.getItem(1))?offer:null;
    }

    public static void afterTake(Slot result,net.minecraft.world.entity.player.Player player,
                                 MerchantOffer offer,long before,long cost) {
        if(offer==null || !(player.containerMenu instanceof MerchantMenu menu)
                || inputValue(result)!=before-cost)return;
        int index=menu.getOffers().indexOf(offer);
        if(index>=0)menu.tryMoveItems(index);
    }
}
