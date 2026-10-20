package cn.blockforge.generated.generatedmod.network.packet.heartsword;

import cn.blockforge.generated.generatedmod.*;
import static cn.blockforge.generated.generatedmod.Network.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record HeartSwordVisualPacket(int entityId, byte action) {
    public static void encode(HeartSwordVisualPacket packet, FriendlyByteBuf buf) {
        buf.writeVarInt(packet.entityId());
        buf.writeByte(packet.action());
    }
    public static HeartSwordVisualPacket decode(FriendlyByteBuf buf) {
        return new HeartSwordVisualPacket(buf.readVarInt(), buf.readByte());
    }
    public static void handle(HeartSwordVisualPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> ClientPacketSink.safe(ClientPacketSink.heartSwordVisualPacket, packet));
        context.setPacketHandled(true);
    }
}
