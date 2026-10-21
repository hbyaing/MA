package cn.blockforge.generated.generatedmod.network.packet.divineart;

import cn.blockforge.generated.generatedmod.*;
import static cn.blockforge.generated.generatedmod.Network.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record DivineArtBannerPacket(String divineId) {
    public static void encode(DivineArtBannerPacket packet, FriendlyByteBuf buf) { buf.writeUtf(packet.divineId(), 64); }
    public static DivineArtBannerPacket decode(FriendlyByteBuf buf) { return new DivineArtBannerPacket(buf.readUtf(64)); }
    public static void handle(DivineArtBannerPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> ClientPacketSink.safe(ClientPacketSink.divineArtBannerPacket, packet));
        context.setPacketHandled(true);
    }
}
