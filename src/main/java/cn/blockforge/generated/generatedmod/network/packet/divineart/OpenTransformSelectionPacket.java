package cn.blockforge.generated.generatedmod.network.packet.divineart;

import cn.blockforge.generated.generatedmod.*;
import static cn.blockforge.generated.generatedmod.Network.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record OpenTransformSelectionPacket() {
    public static void encode(OpenTransformSelectionPacket packet, FriendlyByteBuf buf) {}
    public static OpenTransformSelectionPacket decode(FriendlyByteBuf buf) {
        return new OpenTransformSelectionPacket();
    }
    public static void handle(OpenTransformSelectionPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> ClientPacketSink.safe(ClientPacketSink.openTransformSelectionPacket, packet));
        context.setPacketHandled(true);
    }
}
