package cn.blockforge.generated.generatedmod.mixin;

import cn.blockforge.generated.generatedmod.economy.SpiritTradePayments;
import cn.blockforge.generated.generatedmod.economy.SpiritStoneGrade;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MerchantResultSlot.class)
public abstract class SpiritTradeResultMixin {
    @Unique private MerchantOffer wuxing$offer;
    @Unique private long wuxing$before,wuxing$price;

    @Inject(method={"onTake","m_142406_"},at=@At("HEAD"),remap=false)
    private void wuxing$beforeTrade(Player player,ItemStack result,CallbackInfo callback){
        Slot slot=(Slot)(Object)this;
        wuxing$offer=SpiritTradePayments.offerBeforeTake(slot,player);
        if(wuxing$offer!=null){
            wuxing$before=SpiritTradePayments.inputValue(slot);
            ItemStack cost=wuxing$offer.getCostA();
            wuxing$price=(long)SpiritStoneGrade.of(cost.getItem()).value*cost.getCount();
        }
    }

    @Inject(method={"onTake","m_142406_"},at=@At("TAIL"),remap=false)
    private void wuxing$afterTrade(Player player,ItemStack result,CallbackInfo callback){
        SpiritTradePayments.afterTake((Slot)(Object)this,player,wuxing$offer,wuxing$before,wuxing$price);
        wuxing$offer=null;
    }
}
