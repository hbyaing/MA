package cn.blockforge.generated.generatedmod.meditationqte;

import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public record QteStatePacket(UUID nonce, int result, int level, long startMillis, long serverMillis, long reward) {
    public static final int OPEN = 0, PASS = 1, FAIL = 2, DEVIATION = 3, WIN = 4, CANCELLED = 5;
    public QteStatePacket {
        if (nonce == null || result < OPEN || result > CANCELLED || level < 1 || level > QteRules.LEVELS || reward < 0)
            throw new IllegalArgumentException("QTE state bounds");
    }
    public QteStatePacket(FriendlyByteBuf buffer) {
        this(buffer.readUUID(), buffer.readVarInt(), buffer.readVarInt(), buffer.readLong(), buffer.readLong(), buffer.readVarLong());
    }
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUUID(nonce); buffer.writeVarInt(result); buffer.writeVarInt(level);
        buffer.writeLong(startMillis); buffer.writeLong(serverMillis); buffer.writeVarLong(reward);
    }
    public static void handle(QteStatePacket packet, Supplier<NetworkEvent.Context> supplier) {
        var context = supplier.get();
        context.enqueueWork(() -> net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT,
            () -> () -> cn.blockforge.generated.generatedmod.meditationqte.client.QteClient.accept(packet)));
        context.setPacketHandled(true);
    }
}
