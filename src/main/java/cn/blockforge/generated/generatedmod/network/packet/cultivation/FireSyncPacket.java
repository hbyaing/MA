package cn.blockforge.generated.generatedmod.network.packet.cultivation;

import cn.blockforge.generated.generatedmod.*;
import static cn.blockforge.generated.generatedmod.Network.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record FireSyncPacket(int[] learned, int equipped) {
    public static void encode(FireSyncPacket packet, FriendlyByteBuf buf) {
        // 上限必须与 decode 的 min(9, ...) 一致，否则异火数量超过 9 时字节流错位炸包。
        int count = Math.min(9, packet.learned().length);
        buf.writeVarInt(count);
        for (int i = 0; i < count; i++) buf.writeByte(packet.learned()[i]);
        buf.writeByte(packet.equipped());
    }
    public static FireSyncPacket decode(FriendlyByteBuf buf) {
        int count = Math.min(9, Math.max(0, buf.readVarInt()));
        int[] learned = new int[count];
        for (int i = 0; i < count; i++) learned[i] = buf.readUnsignedByte();
        return new FireSyncPacket(learned, buf.readUnsignedByte());
    }
    public static void handle(FireSyncPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> ClientPacketSink.safe(ClientPacketSink.fireSyncPacket, packet));
        context.setPacketHandled(true);
    }
}
