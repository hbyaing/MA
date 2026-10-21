package cn.blockforge.generated.generatedmod.network.packet.divineart;

import cn.blockforge.generated.generatedmod.*;
import static cn.blockforge.generated.generatedmod.Network.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record EquipDivineArtPacket(int slot, String divineId) {
    public static void encode(EquipDivineArtPacket packet, FriendlyByteBuf buf) {
        buf.writeByte(packet.slot());
        buf.writeBoolean(packet.divineId() != null);
        if (packet.divineId() != null) buf.writeUtf(packet.divineId(), 64);
    }
    public static EquipDivineArtPacket decode(FriendlyByteBuf buf) {
        return new EquipDivineArtPacket(buf.readByte(), buf.readBoolean() ? buf.readUtf(64) : null);
    }
    public static void handle(EquipDivineArtPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            CultivationData data = player == null ? null : GeneratedMod.data(player);
            if (data == null || packet.slot() < 0 || packet.slot() >= 3) return;
            if (packet.divineId() == null) data.wuxingxiantu$setEquippedDivineArt(packet.slot(), null);
            else if (!data.wuxingxiantu$hasLearnedDivineArt(packet.divineId())) return;
            else data.wuxingxiantu$setEquippedDivineArt(packet.slot(), packet.divineId());
            GeneratedMod.sync(player);
        });
        context.setPacketHandled(true);
    }
}
