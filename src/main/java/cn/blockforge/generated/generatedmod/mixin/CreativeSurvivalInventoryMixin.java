package cn.blockforge.generated.generatedmod.mixin;

import cn.blockforge.generated.generatedmod.client.CultivationMainScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Keep the real creative abilities; suppress only vanilla's screen redirection. */
@Mixin(InventoryScreen.class)
public abstract class CreativeSurvivalInventoryMixin {
    @Redirect(method={"init","containerTick"},at=@At(value="INVOKE",
            target="Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;hasInfiniteItems()Z"),require=0,remap=false)
    private boolean wuxing$developmentInventory(MultiPlayerGameMode mode) {
        return !((Object)this instanceof CultivationMainScreen)&&mode.hasInfiniteItems();
    }
    @Redirect(method={"m_7856_","m_181908_"},at=@At(value="INVOKE",
            target="Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;m_105290_()Z"),require=0,remap=false)
    private boolean wuxing$productionInventory(MultiPlayerGameMode mode) {
        return !((Object)this instanceof CultivationMainScreen)&&mode.hasInfiniteItems();
    }
}
