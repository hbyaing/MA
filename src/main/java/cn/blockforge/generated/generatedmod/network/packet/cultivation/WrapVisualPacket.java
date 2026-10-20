package cn.blockforge.generated.generatedmod.network.packet.cultivation;

import cn.blockforge.generated.generatedmod.*;
import static cn.blockforge.generated.generatedmod.Network.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record WrapVisualPacket(int entityId, int tier, int level) {
    public static void encode(WrapVisualPacket packet, FriendlyByteBuf buf) {
        buf.writeVarInt(packet.entityId());
        buf.writeByte(Math.max(0, Math.min(9, packet.tier())));
        buf.writeByte(Math.max(0, Math.min(5, packet.level())));
    }
    public static WrapVisualPacket decode(FriendlyByteBuf buf) {
        return new WrapVisualPacket(buf.readVarInt(), buf.readUnsignedByte(), buf.readUnsignedByte());
    }
    public static void handle(WrapVisualPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> ClientPacketSink.safe(ClientPacketSink.wrapVisualPacket, packet));
        context.setPacketHandled(true);
    }
}
