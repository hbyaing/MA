package cn.blockforge.generated.generatedmod.network.packet.cultivation;

import cn.blockforge.generated.generatedmod.*;
import static cn.blockforge.generated.generatedmod.Network.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record StopUsingItemPacket(int itemId, int cooldownTicks) {
    public static void encode(StopUsingItemPacket packet, FriendlyByteBuf buf) {
        buf.writeVarInt(packet.itemId);
        buf.writeVarInt(packet.cooldownTicks);
    }
    public static StopUsingItemPacket decode(FriendlyByteBuf buf) {
        return new StopUsingItemPacket(buf.readVarInt(), buf.readVarInt());
    }
    public static void handle(StopUsingItemPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> ClientPacketSink.safe(ClientPacketSink.stopUsingItemPacket, packet));
        context.setPacketHandled(true);
    }
}
