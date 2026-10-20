package cn.blockforge.generated.generatedmod.mixin;

import cn.blockforge.generated.generatedmod.crafting.BoundEquipmentCrafting;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

/** Explicit development/SRG names follow the existing legacy Forge mixin configuration. */
@Mixin(CraftingMenu.class)
public abstract class BoundEquipmentCraftingMixin {
    @Inject(method={"slotChangedCraftingGrid","m_150546_"},at=@At("HEAD"),cancellable=true,remap=false)
    private static void wuxing$boundResult(AbstractContainerMenu menu, Level level, Player player,
                                          CraftingContainer grid, ResultContainer results, CallbackInfo callback) {
        if (BoundEquipmentCrafting.update(menu,level,player,grid,results)) callback.cancel();
    }

    @Inject(method={"quickMoveStack","m_7648_"},at=@At("HEAD"),cancellable=true,remap=false)
    private void wuxing$boundQuickMove(Player player,int index,CallbackInfoReturnable<ItemStack> callback) {
        if(index==0 && !BoundEquipmentCrafting.canTake((AbstractContainerMenu)(Object)this,player)) callback.setReturnValue(ItemStack.EMPTY);
    }
}
