package cn.blockforge.generated.generatedmod.network.packet.cultivation;

import cn.blockforge.generated.generatedmod.*;
import static cn.blockforge.generated.generatedmod.Network.*;
import cn.blockforge.generated.generatedmod.items.CultivationLogic;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record EquipArtPacket(int slot, String art) {
    public static void encode(EquipArtPacket packet, FriendlyByteBuf buf) {
        buf.writeByte(packet.slot); buf.writeBoolean(packet.art != null);
        if (packet.art != null) buf.writeUtf(packet.art, 64);
    }
    public static EquipArtPacket decode(FriendlyByteBuf buf) {
        int slot = buf.readByte();
        return new EquipArtPacket(slot, buf.readBoolean() ? buf.readUtf(64) : null);
    }
    public static void handle(EquipArtPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || packet.slot < 0 || packet.slot >= 8) return;
            CultivationData data = GeneratedMod.data(player);
            if (data == null || packet.slot >= CultivationLogic.unlockedArtSlots(data.wuxingxiantu$getRealm())) return;
            if (packet.art == null) data.wuxingxiantu$clearEquippedArt(packet.slot);
            else if (!data.wuxingxiantu$equipArt(packet.slot, packet.art)) return;
            GeneratedMod.sync(player);
        });
        context.setPacketHandled(true);
    }
}
