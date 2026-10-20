package cn.blockforge.generated.generatedmod.meditationqte;

import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

/** No pointer coordinate or claimed hit time crosses this client-to-server boundary. */
public record QteActionPacket(int action, UUID nonce, int level) {
    public static final int START = 0, HIT = 1, CANCEL = 2;
    public static final UUID NONE = new UUID(0, 0);
    public QteActionPacket {
        if (action < START || action > CANCEL || nonce == null || level < 0 || level > QteRules.LEVELS)
            throw new IllegalArgumentException("QTE action bounds");
    }
    public QteActionPacket(FriendlyByteBuf buffer) { this(buffer.readVarInt(), buffer.readUUID(), buffer.readVarInt()); }
    public void encode(FriendlyByteBuf buffer) { buffer.writeVarInt(action); buffer.writeUUID(nonce); buffer.writeVarInt(level); }
    public static void handle(QteActionPacket packet, Supplier<NetworkEvent.Context> supplier) {
        var context = supplier.get();
        context.enqueueWork(() -> {
            var player = context.getSender();
            if (player == null) return;
            if (packet.action == START) MeditationQte.start(player);
            else if (packet.action == HIT) MeditationQte.hit(player, packet.nonce, packet.level);
            else MeditationQte.cancel(player, packet.nonce);
        });
        context.setPacketHandled(true);
    }
}
