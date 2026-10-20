package cn.blockforge.generated.generatedmod.network.packet.cultivation;

import cn.blockforge.generated.generatedmod.*;
import static cn.blockforge.generated.generatedmod.Network.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record CompassOpenPacket() {
    public static void encode(CompassOpenPacket packet, FriendlyByteBuf buf) {}
    public static CompassOpenPacket decode(FriendlyByteBuf buf) { return new CompassOpenPacket(); }
    public static void handle(CompassOpenPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) cn.blockforge.generated.generatedmod.network.service.CompassService.open(player);
        });
        context.setPacketHandled(true);
    }
}
