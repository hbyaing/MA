package cn.blockforge.generated.generatedmod.mixin;

import cn.blockforge.generated.generatedmod.crafting.BoundEquipmentCrafting;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public abstract class BoundEquipmentResultSlotMixin {
    @Inject(method={"mayPickup","m_8010_"},at=@At("HEAD"),cancellable=true,remap=false)
    private void wuxing$boundPickup(Player player,CallbackInfoReturnable<Boolean> callback) {
        if((Object)this instanceof ResultSlot && player.containerMenu.getSlot(0)==(Object)this
                && !BoundEquipmentCrafting.canTake(player.containerMenu,player)) callback.setReturnValue(false);
    }
}
