package cn.blockforge.generated.generatedmod.network.packet.divineart;

import cn.blockforge.generated.generatedmod.*;
import static cn.blockforge.generated.generatedmod.Network.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record TransformCancelPacket() {
    public static void encode(TransformCancelPacket packet, FriendlyByteBuf buf) {}
    public static TransformCancelPacket decode(FriendlyByteBuf buf) { return new TransformCancelPacket(); }
    public static void handle(TransformCancelPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) DivineArtLogic.cancelTransformSelection(player);
        });
        context.setPacketHandled(true);
    }
}
