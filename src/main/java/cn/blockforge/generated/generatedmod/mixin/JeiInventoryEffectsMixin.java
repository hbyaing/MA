package cn.blockforge.generated.generatedmod.mixin;

import cn.blockforge.generated.generatedmod.client.CultivationMainScreen;
import net.minecraft.client.renderer.Rect2i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.List;

/** JEI is optional; reserve effects at their actual rendered coordinates. */
@Pseudo
@Mixin(targets="mezz.jei.library.plugins.vanilla.gui.InventoryEffectRendererGuiHandler", remap=false)
public abstract class JeiInventoryEffectsMixin {
    @Inject(method="getGuiExtraAreas", at=@At("HEAD"), cancellable=true, require=0, remap=false)
    private void wuxing$actualEffects(@Coerce Object screen, CallbackInfoReturnable<List<Rect2i>> result) {
        if(screen instanceof CultivationMainScreen cultivation) result.setReturnValue(cultivation.jeiEffectAreas());
    }
}
