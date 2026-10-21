package cn.blockforge.generated.generatedmod.network.packet.cultivation;

import cn.blockforge.generated.generatedmod.CultivationData;
import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.Network;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

/** Three always-on art slots; server validates the learned, non-attack category. */
public record EquipPassiveArtPacket(int slot, String art) {
    public static void encode(EquipPassiveArtPacket p, FriendlyByteBuf b) {
        b.writeByte(p.slot); b.writeBoolean(p.art != null); if (p.art != null) b.writeUtf(p.art, 64);
    }
    public static EquipPassiveArtPacket decode(FriendlyByteBuf b) {
        return new EquipPassiveArtPacket(b.readUnsignedByte(), b.readBoolean() ? b.readUtf(64) : null);
    }
    public static void handle(EquipPassiveArtPacket p, Supplier<NetworkEvent.Context> supplier) {
        var context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || p.slot < 0 || p.slot >= 3) return;
            CultivationData data = GeneratedMod.data(player);
            if (data == null) return;
            if (p.art == null) data.wuxingxiantu$clearPassiveArt(p.slot);
            else if (!data.wuxingxiantu$equipPassiveArt(p.slot, p.art)) return;
            GeneratedMod.sync(player);
        });
        context.setPacketHandled(true);
    }
}
