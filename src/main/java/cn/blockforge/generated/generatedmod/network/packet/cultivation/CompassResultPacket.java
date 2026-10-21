package cn.blockforge.generated.generatedmod.network.packet.cultivation;

import cn.blockforge.generated.generatedmod.*;
import static cn.blockforge.generated.generatedmod.Network.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record CompassResultPacket(String name, double distance, int dx, int dy, int dz) {
    public static void encode(CompassResultPacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.name(), 128);
        buf.writeDouble(packet.distance());
        buf.writeVarInt(packet.dx());
        buf.writeVarInt(packet.dy());
        buf.writeVarInt(packet.dz());
    }
    public static CompassResultPacket decode(FriendlyByteBuf buf) {
        return new CompassResultPacket(buf.readUtf(128), buf.readDouble(),
                buf.readVarInt(), buf.readVarInt(), buf.readVarInt());
    }
    public static void handle(CompassResultPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> ClientPacketSink.safe(ClientPacketSink.compassResultPacket, packet));
        context.setPacketHandled(true);
    }
}
