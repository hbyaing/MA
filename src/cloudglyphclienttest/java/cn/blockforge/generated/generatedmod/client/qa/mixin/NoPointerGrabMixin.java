package cn.blockforge.generated.generatedmod.client.qa.mixin;

import cn.blockforge.generated.generatedmod.client.qa.PointerSafety;

import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Dev capture source set only; never included in the player's mod jar. */
@Mixin(value=MouseHandler.class,remap=false)
public abstract class NoPointerGrabMixin {
    @Inject(method="grabMouse",at=@At("HEAD"),cancellable=true,remap=false)
    private void preventDesktopPointerCapture(CallbackInfo ci){PointerSafety.blockedGrabs++;ci.cancel();}
}
