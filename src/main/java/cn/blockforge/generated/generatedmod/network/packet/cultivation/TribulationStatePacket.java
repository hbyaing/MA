package cn.blockforge.generated.generatedmod.network.packet.cultivation;

import cn.blockforge.generated.generatedmod.*;
import static cn.blockforge.generated.generatedmod.Network.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record TribulationStatePacket(int countdown, boolean active, boolean heartDemon) {
    public static void encode(TribulationStatePacket packet, FriendlyByteBuf buf) {
        buf.writeVarInt(packet.countdown()); buf.writeBoolean(packet.active()); buf.writeBoolean(packet.heartDemon());
    }
    public static TribulationStatePacket decode(FriendlyByteBuf buf) {
        return new TribulationStatePacket(buf.readVarInt(), buf.readBoolean(), buf.readBoolean());
    }
    public static void handle(TribulationStatePacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> ClientPacketSink.safe(ClientPacketSink.tribulationStatePacket, packet));
        context.setPacketHandled(true);
    }
}
