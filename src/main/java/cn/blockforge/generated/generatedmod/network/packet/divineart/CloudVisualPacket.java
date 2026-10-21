package cn.blockforge.generated.generatedmod.network.packet.divineart;

import cn.blockforge.generated.generatedmod.*;
import static cn.blockforge.generated.generatedmod.Network.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.UUID;
import java.util.function.Supplier;

public record CloudVisualPacket(UUID player, boolean visible) {
    public static void encode(CloudVisualPacket packet, FriendlyByteBuf buf) {
        buf.writeUUID(packet.player());
        buf.writeBoolean(packet.visible());
    }

    public static CloudVisualPacket decode(FriendlyByteBuf buf) {
        return new CloudVisualPacket(buf.readUUID(), buf.readBoolean());
    }

    public static void handle(CloudVisualPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> ClientPacketSink.safe(ClientPacketSink.cloudVisualPacket, packet));
        context.setPacketHandled(true);
    }
}
