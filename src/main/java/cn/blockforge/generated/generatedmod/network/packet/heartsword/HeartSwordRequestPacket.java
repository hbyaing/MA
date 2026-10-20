package cn.blockforge.generated.generatedmod.network.packet.heartsword;

import cn.blockforge.generated.generatedmod.*;
import static cn.blockforge.generated.generatedmod.Network.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record HeartSwordRequestPacket() {
    public static void encode(HeartSwordRequestPacket packet, FriendlyByteBuf buf) {}
    public static HeartSwordRequestPacket decode(FriendlyByteBuf buf) { return new HeartSwordRequestPacket(); }
    public static void handle(HeartSwordRequestPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> HeartSwordSystem.request(context.getSender()));
        context.setPacketHandled(true);
    }
}
