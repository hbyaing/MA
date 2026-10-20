package cn.blockforge.generated.generatedmod.network.packet.cultivation;

import cn.blockforge.generated.generatedmod.*;
import static cn.blockforge.generated.generatedmod.Network.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.Map;
import java.util.function.Supplier;

public record FireStatePacket(int[] dx, int[] dy, int[] dz, int[] tiers) {
    public static FireStatePacket from(Map<BlockPos, Integer> snapshot) {
        int size = Math.min(1024, snapshot.size());
        int[] dx = new int[size];
        int[] dy = new int[size];
        int[] dz = new int[size];
        int[] tiers = new int[size];
        int i = 0;
        for (Map.Entry<BlockPos, Integer> entry : snapshot.entrySet()) {
            if (i >= size) break;
            BlockPos pos = entry.getKey();
            dx[i] = pos.getX();
            dy[i] = pos.getY();
            dz[i] = pos.getZ();
            tiers[i++] = ExoticFireColorRegistry.clampTier(entry.getValue());
        }
        return new FireStatePacket(dx, dy, dz, tiers);
    }
    public static void encode(FireStatePacket packet, FriendlyByteBuf buf) {
        int count = Math.min(1024, Math.min(Math.min(packet.dx().length, packet.dy().length),
                Math.min(packet.dz().length, packet.tiers().length)));
        buf.writeVarInt(count);
        for (int i = 0; i < count; i++) {
            buf.writeInt(packet.dx()[i]);
            buf.writeInt(packet.dy()[i]);
            buf.writeInt(packet.dz()[i]);
            buf.writeByte(packet.tiers()[i]);
        }
    }
    public static FireStatePacket decode(FriendlyByteBuf buf) {
        int count = Math.min(1024, Math.max(0, buf.readVarInt()));
        int[] dx = new int[count];
        int[] dy = new int[count];
        int[] dz = new int[count];
        int[] tiers = new int[count];
        for (int i = 0; i < count; i++) {
            dx[i] = buf.readInt();
            dy[i] = buf.readInt();
            dz[i] = buf.readInt();
            tiers[i] = buf.readUnsignedByte();
        }
        return new FireStatePacket(dx, dy, dz, tiers);
    }
    public static void handle(FireStatePacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> ClientPacketSink.safe(ClientPacketSink.fireStatePacket, packet));
        context.setPacketHandled(true);
    }
}
