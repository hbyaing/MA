package cn.blockforge.generated.generatedmod.network.packet.cultivation;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record SelectRootPacket(int root) {
    public static void encode(SelectRootPacket packet, FriendlyByteBuf buf) { buf.writeInt(packet.root); }
    public static SelectRootPacket decode(FriendlyByteBuf buf) { return new SelectRootPacket(buf.readInt()); }
    public static void handle(SelectRootPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        // Retired packet ID 3 remains reserved; clients cannot choose or reroll roots.
        context.setPacketHandled(true);
    }
}
