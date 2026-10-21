package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.economy.*;
import cn.blockforge.generated.generatedmod.fixtures.GameTestPlayer;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.*;
import net.minecraft.world.item.trading.*;
import net.minecraftforge.gametest.*;
import java.util.*;

@GameTestHolder("wuxingxiantu_entry")
@PrefixGameTestTemplate(false)
public final class SpiritTradeCurrencyGameTests {
    private static MerchantMenu menu(GameTestPlayer player,MerchantOffer offer){
        Villager villager=EntityType.VILLAGER.create(player.level());
        // Server-side AbstractVillager.overrideOffers is deliberately a no-op (client sync API).
        // Install the fixture through the real mutable server offer list.
        var offers=villager.getOffers();offers.clear();offers.add(offer);
        villager.setTradingPlayer(player);
        var menu=new MerchantMenu(71,player.getInventory(),villager);player.containerMenu=menu;
        menu.setSelectionHint(0);return menu;
    }
    private static long total(GameTestPlayer player,MerchantMenu menu){
        return SpiritCurrency.value(player.getInventory().items)
                +SpiritCurrency.value(List.of(menu.getSlot(0).getItem(),menu.getSlot(1).getItem()));
    }
    private static MerchantOffer offer(int cost,int uses){
        return new MerchantOffer(new ItemStack(WuXingItems.SPIRIT_STONE.get(),cost),new ItemStack(Items.APPLE),uses,1,0);
    }
    @GameTest(template="empty")
    public static void realTradeReservesChangeAndChargesOnlyOnPurchase(GameTestHelper h){
        try(var player=new GameTestPlayer(h.getLevel())){
            player.getInventory().setItem(0,new ItemStack(SpiritStoneGrade.MIDDLE.item()));
            var offer=offer(6,12);offer.addToSpecialPriceDiff(-2);
            var menu=menu(player,offer);menu.tryMoveItems(0);
            h.assertTrue(menu.getSlot(0).getItem().is(WuXingItems.SPIRIT_STONE.get())
                    &&menu.getSlot(0).getItem().getCount()==4&&menu.getSlot(2).getItem().is(Items.APPLE),
                    "High currency did not fill the vanilla discounted offer");
            h.assertTrue(total(player,menu)==9&&offer.getUses()==0,"Preview consumed value or a trade use");
            menu.clicked(2,0,ClickType.PICKUP,player);
            h.assertTrue(menu.getCarried().is(Items.APPLE)&&menu.getCarried().getCount()==1
                    &&offer.getUses()==1&&total(player,menu)==5,"Purchase/refill lost currency, output or merchant stock");
            menu.setCarried(ItemStack.EMPTY);
            menu.clicked(0,0,ClickType.PICKUP,player);
            h.assertTrue(SpiritCurrency.value(player.getInventory().items)+SpiritCurrency.value(List.of(menu.getCarried()))==5,
                    "Unspent reserved payment could not be reclaimed");
        }h.succeed();
    }
    @GameTest(template="empty")
    public static void shiftTradeRefillsButStopsAtVanillaStock(GameTestHelper h){
        try(var player=new GameTestPlayer(h.getLevel())){
            player.getInventory().setItem(0,new ItemStack(SpiritStoneGrade.SUPERIOR.item()));
            var offer=offer(4,3);var menu=menu(player,offer);menu.tryMoveItems(0);
            menu.clicked(2,0,ClickType.QUICK_MOVE,player);
            h.assertTrue(player.getInventory().countItem(Items.APPLE)==3&&offer.getUses()==3&&offer.isOutOfStock(),
                    "Shift trade failed to refill or ignored merchant stock");
            h.assertTrue(total(player,menu)==81-12,"Shift trade mischarged four-tier currency");
            menu.clicked(2,0,ClickType.QUICK_MOVE,player);
            h.assertTrue(total(player,menu)==69&&player.getInventory().countItem(Items.APPLE)==3,
                    "Exhausted offer duplicated a purchase");
        }h.succeed();
    }
    @GameTest(template="empty")
    public static void rejectedChangeAndInputReturnAreAtomic(GameTestHelper h){
        try(var player=new GameTestPlayer(h.getLevel())){
            for(int i=0;i<36;i++)player.getInventory().setItem(i,new ItemStack(Items.STONE,64));
            player.getInventory().setItem(0,new ItemStack(SpiritStoneGrade.EXQUISITE.item()));
            var payments=new SimpleContainer(3);
            var before=player.getInventory().items.stream().map(ItemStack::copy).toList();
            h.assertTrue(!SpiritTradePayments.reserve(player.getInventory(),payments,new ItemStack(WuXingItems.SPIRIT_STONE.get())),
                    "Full inventory accepted unplaceable change");
            for(int i=0;i<36;i++)h.assertTrue(ItemStack.matches(before.get(i),player.getInventory().getItem(i)),"Failed change mutated inventory");
            var diamond=new ItemStack(Items.DIAMOND);diamond.getOrCreateTag().putString("source","kept");payments.setItem(1,diamond);
            h.assertTrue(!SpiritTradePayments.reserve(player.getInventory(),payments,new ItemStack(WuXingItems.SPIRIT_STONE.get()))
                    &&ItemStack.matches(payments.getItem(1),diamond),"Returning a payment destroyed an unrelated tagged input");
        }h.succeed();
    }
    @GameTest(template="empty")
    public static void denominationsAndExistingPaymentSlotsConserveValue(GameTestHelper h){
        try(var player=new GameTestPlayer(h.getLevel())){
            var payments=new SimpleContainer(3);
            payments.setItem(0,new ItemStack(SpiritStoneGrade.MIDDLE.item()));
            player.getInventory().setItem(0,new ItemStack(WuXingItems.SPIRIT_STONE.get(),12));
            var target=new ItemStack(SpiritStoneGrade.MIDDLE.item(),2);
            h.assertTrue(SpiritTradePayments.reserve(player.getInventory(),payments,target)
                    &&ItemStack.matches(payments.getItem(0),target)&&SpiritCurrency.value(player.getInventory().items)==3,
                    "Existing payment and lower currency did not exchange to the offer denomination");
            long before=SpiritCurrency.value(player.getInventory().items);
            h.assertTrue(!SpiritTradePayments.reserve(player.getInventory(),payments,new ItemStack(SpiritStoneGrade.EXQUISITE.item()))
                    &&SpiritCurrency.value(player.getInventory().items)==before&&ItemStack.matches(payments.getItem(0),target),
                    "Insufficient funds consumed existing reservation");
        }h.succeed();
    }
    @GameTest(template="empty")
    public static void ordinaryAndTaggedOffersKeepVanillaRequirements(GameTestHelper h){
        try(var player=new GameTestPlayer(h.getLevel())){
            var offer=new MerchantOffer(new ItemStack(Items.EMERALD,2),new ItemStack(Items.BREAD),3,1,0);
            player.getInventory().setItem(0,new ItemStack(Items.EMERALD,2));
            var menu=menu(player,offer);menu.tryMoveItems(0);
            h.assertTrue(!SpiritTradePayments.supported(offer)&&menu.getSlot(2).getItem().is(Items.BREAD),"Non-currency trade was replaced");
            menu.clicked(2,0,ClickType.PICKUP,player);
            h.assertTrue(menu.getCarried().is(Items.BREAD)&&offer.getUses()==1,"Ordinary vanilla trade failed");
            var required=new ItemStack(WuXingItems.SPIRIT_STONE.get());required.getOrCreateTag().putBoolean("voucher",true);
            h.assertTrue(!SpiritTradePayments.supported(new MerchantOffer(required,new ItemStack(Items.APPLE),2,1,0)),
                    "Specific tagged payment was treated as fungible currency");
            h.assertTrue(!SpiritTradePayments.supported(new MerchantOffer(new ItemStack(WuXingItems.SPIRIT_STONE.get()),
                    new ItemStack(Items.DIAMOND),new ItemStack(Items.APPLE),2,1,0)),"Second-input requirement was bypassed");
        }h.succeed();
    }
}
