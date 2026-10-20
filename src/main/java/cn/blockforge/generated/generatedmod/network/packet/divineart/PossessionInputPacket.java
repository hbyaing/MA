package cn.blockforge.generated.generatedmod.network.packet.divineart;

import cn.blockforge.generated.generatedmod.*;
import static cn.blockforge.generated.generatedmod.Network.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record PossessionInputPacket(float yaw, float pitch, int flags) {
    private static final int FORWARD = 1;
    private static final int BACKWARD = 1 << 1;
    private static final int LEFT = 1 << 2;
    private static final int RIGHT = 1 << 3;
    private static final int JUMP = 1 << 4;
    private static final int SNEAK = 1 << 5;
    private static final int ATTACK = 1 << 6;

    public static void encode(PossessionInputPacket packet, FriendlyByteBuf buf) {
        buf.writeFloat(packet.yaw());
        buf.writeFloat(packet.pitch());
        buf.writeVarInt(packet.flags() & 0x7F);
    }

    public static PossessionInputPacket decode(FriendlyByteBuf buf) {
        return new PossessionInputPacket(buf.readFloat(), buf.readFloat(), buf.readVarInt());
    }

    public static void handle(PossessionInputPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;
            int flags = packet.flags();
            DivineArtLogic.setPossessionInput(player, packet.yaw(), packet.pitch(),
                    (flags & FORWARD) != 0, (flags & BACKWARD) != 0,
                    (flags & LEFT) != 0, (flags & RIGHT) != 0,
                    (flags & JUMP) != 0, (flags & SNEAK) != 0, (flags & ATTACK) != 0);
        });
        context.setPacketHandled(true);
    }
}
