package cn.blockforge.generated.generatedmod.network.packet.cultivation;

import cn.blockforge.generated.generatedmod.*;
import static cn.blockforge.generated.generatedmod.Network.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record SoulTradeFavoritePacket(int containerId, int offerIndex) {
    public static void encode(SoulTradeFavoritePacket packet, FriendlyByteBuf buf) {
        buf.writeVarInt(packet.containerId());
        buf.writeVarInt(packet.offerIndex());
    }

    public static SoulTradeFavoritePacket decode(FriendlyByteBuf buf) {
        return new SoulTradeFavoritePacket(buf.readVarInt(), buf.readVarInt());
    }

    public static void handle(SoulTradeFavoritePacket packet,
                              Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || packet.containerId() < 0 || packet.offerIndex() < 0
                    || !(player.containerMenu instanceof SoulTradeMenu menu)
                    || menu.containerId != packet.containerId()) return;
            menu.handleFavoriteAction(player, packet.offerIndex());
        });
        context.setPacketHandled(true);
    }
}
