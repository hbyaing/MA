package cn.blockforge.generated.generatedmod.mixin;

import cn.blockforge.generated.generatedmod.plants.HerbAge;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Resolve catalogue copies on the server thread BEFORE insertion or creative dropping. */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class CreativeHerbYearsMixin {
    @Inject(method={"handleSetCreativeModeSlot","m_5964_"},at=@At("HEAD"),remap=false)
    private void wuxing$years(ServerboundSetCreativeModeSlotPacket packet,CallbackInfo ci){
        var listener=(ServerGamePacketListenerImpl)(Object)this;
        var player=listener.player;
        net.minecraft.network.protocol.PacketUtils.ensureRunningOnSameThread(packet,listener,player.serverLevel());
        if(player.getAbilities().instabuild)HerbAge.initializeTemplate(packet.getItem(),player.getRandom());
    }
}
