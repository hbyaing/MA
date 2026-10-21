package cn.blockforge.generated.generatedmod.client.qa.myriad.mixin;

import cn.blockforge.generated.generatedmod.client.qa.myriad.PointerSafety;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Opt-in test source only. Explicit development/SRG names protect both mapped launch routes. */
@Mixin(value=MouseHandler.class,remap=false)
public abstract class NoPointerGrabMixin {
    @Inject(method={"grabMouse","m_91601_"},at=@At("HEAD"),cancellable=true,remap=false)
    private void preventCapture(CallbackInfo callback){PointerSafety.blockedGrabs++;callback.cancel();}
}
