package cn.blockforge.generated.generatedmod.client.qa.v5.mixin;

import cn.blockforge.generated.generatedmod.client.qa.v5.V5ClientAcceptance;

import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value=MouseHandler.class,remap=false)
public abstract class NoGrabMixin {
    @Inject(method={"grabMouse","m_91601_"},at=@At("HEAD"),cancellable=true,remap=false)
    private void block(CallbackInfo ci){V5ClientAcceptance.blockedGrabs++;ci.cancel();}
}
