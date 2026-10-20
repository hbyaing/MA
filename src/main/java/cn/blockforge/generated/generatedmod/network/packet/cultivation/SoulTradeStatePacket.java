package cn.blockforge.generated.generatedmod.network.packet.cultivation;

import cn.blockforge.generated.generatedmod.*;
import static cn.blockforge.generated.generatedmod.Network.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.BitSet;
import java.util.function.Supplier;

public record SoulTradeStatePacket(int containerId, BitSet favorites, boolean resetScroll) {
    public SoulTradeStatePacket {
        favorites = (BitSet) favorites.clone();
    }

    public static void encode(SoulTradeStatePacket packet, FriendlyByteBuf buf) {
        buf.writeVarInt(packet.containerId());
        buf.writeBitSet(packet.favorites());
        buf.writeBoolean(packet.resetScroll());
    }

    public static SoulTradeStatePacket decode(FriendlyByteBuf buf) {
        return new SoulTradeStatePacket(buf.readVarInt(), buf.readBitSet(), buf.readBoolean());
    }

    public static void handle(SoulTradeStatePacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> ClientPacketSink.safe(ClientPacketSink.soulTradeStatePacket, packet));
        context.setPacketHandled(true);
    }
}
