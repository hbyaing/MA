package cn.blockforge.generated.generatedmod.network.packet.cultivation;

import cn.blockforge.generated.generatedmod.*;
import static cn.blockforge.generated.generatedmod.Network.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record OpenScreenPacket(int screen, BlockPos pos, int furnaceTier) {
    public static void encode(OpenScreenPacket p, FriendlyByteBuf b) { b.writeInt(p.screen); b.writeBlockPos(p.pos); b.writeVarInt(p.furnaceTier); }
    public static OpenScreenPacket decode(FriendlyByteBuf b) { return new OpenScreenPacket(b.readInt(), b.readBlockPos(), b.readVarInt()); }
    public static void handle(OpenScreenPacket p, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> ClientPacketSink.safe(ClientPacketSink.openScreenPacket, p));
        context.setPacketHandled(true);
    }
}
