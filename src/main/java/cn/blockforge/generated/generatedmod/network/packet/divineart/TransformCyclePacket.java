package cn.blockforge.generated.generatedmod.network.packet.divineart;

import cn.blockforge.generated.generatedmod.*;
import static cn.blockforge.generated.generatedmod.Network.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record TransformCyclePacket(boolean cycle) {
    public static void encode(TransformCyclePacket packet, FriendlyByteBuf buf) { buf.writeBoolean(packet.cycle()); }
    public static TransformCyclePacket decode(FriendlyByteBuf buf) { return new TransformCyclePacket(buf.readBoolean()); }
    public static void handle(TransformCyclePacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;
            CultivationData data = GeneratedMod.data(player);
            if (data == null || !"qi_shi_er_bian".equals(data.wuxingxiantu$getActiveDivineArt())) return;
            DivineArtLogic.cyclePossessedForm(player);
        });
        context.setPacketHandled(true);
    }
}
