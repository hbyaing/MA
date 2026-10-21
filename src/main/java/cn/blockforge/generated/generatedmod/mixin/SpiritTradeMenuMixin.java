package cn.blockforge.generated.generatedmod.mixin;

import cn.blockforge.generated.generatedmod.economy.SpiritTradePayments;
import net.minecraft.world.inventory.MerchantMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MerchantMenu.class)
public abstract class SpiritTradeMenuMixin {
    @Inject(method={"tryMoveItems","m_40072_"},at=@At("HEAD"),cancellable=true,remap=false)
    private void wuxing$exchangeCurrency(int index,CallbackInfo callback){
        if(SpiritTradePayments.select((MerchantMenu)(Object)this,index))callback.cancel();
    }
}
