package cn.blockforge.generated.generatedmod.network.packet.divineart;

import cn.blockforge.generated.generatedmod.*;
import static cn.blockforge.generated.generatedmod.Network.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record DivineReleasePacket(String divineId) {
    public static void encode(DivineReleasePacket packet, FriendlyByteBuf buf) { buf.writeUtf(packet.divineId(), 64); }
    public static DivineReleasePacket decode(FriendlyByteBuf buf) { return new DivineReleasePacket(buf.readUtf(64)); }
    public static void handle(DivineReleasePacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            CultivationData data = player == null ? null : GeneratedMod.data(player);
            if (player != null && data != null && DivineArtCatalog.isValid(packet.divineId())
                    && data.wuxingxiantu$hasLearnedDivineArt(packet.divineId())) {
                DivineArtLogic.release(player, packet.divineId());
            }
        });
        context.setPacketHandled(true);
    }
}
