package cn.blockforge.generated.generatedmod.network.packet.cultivation;

import cn.blockforge.generated.generatedmod.*;
import static cn.blockforge.generated.generatedmod.Network.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record TribulationPacket() {
    public static void encode(TribulationPacket packet, FriendlyByteBuf buf) {}
    public static TribulationPacket decode(FriendlyByteBuf buf) { return new TribulationPacket(); }
    public static void handle(TribulationPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) TribulationManager.start(player);
        });
        context.setPacketHandled(true);
    }
}
