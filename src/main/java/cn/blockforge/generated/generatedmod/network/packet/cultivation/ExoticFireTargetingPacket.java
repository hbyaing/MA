package cn.blockforge.generated.generatedmod.network.packet.cultivation;

import cn.blockforge.generated.generatedmod.CultivationDataImpl;
import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.network.CultivationSync;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

/** Sender-only wrapping preference: no entity ID, UUID or client-provided target. */
public record ExoticFireTargetingPacket(boolean enabled){
    public static void encode(ExoticFireTargetingPacket packet,FriendlyByteBuf buffer){buffer.writeBoolean(packet.enabled);}
    public static ExoticFireTargetingPacket decode(FriendlyByteBuf buffer){return new ExoticFireTargetingPacket(buffer.readBoolean());}
    public static void handle(ExoticFireTargetingPacket packet,Supplier<NetworkEvent.Context> supplier){
        var context=supplier.get();
        context.enqueueWork(()->apply(context.getSender(),packet.enabled));
        context.setPacketHandled(true);
    }
    /** The same sender-bound operation used by the network handler and server regression tests. */
    public static void apply(ServerPlayer sender,boolean enabled){
        if(sender!=null&&GeneratedMod.data(sender) instanceof CultivationDataImpl data){
            data.setExoticFireHurtsNonHostile(enabled);
            CultivationSync.publish(sender);
        }
    }
}
