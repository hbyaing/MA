package cn.blockforge.generated.generatedmod.network.packet.divineart;

import cn.blockforge.generated.generatedmod.*;
import static cn.blockforge.generated.generatedmod.Network.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record TransformCreeperReleasePacket() {
    public static void encode(TransformCreeperReleasePacket packet, FriendlyByteBuf buf) {}
    public static TransformCreeperReleasePacket decode(FriendlyByteBuf buf) { return new TransformCreeperReleasePacket(); }
    public static void handle(TransformCreeperReleasePacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;
            CultivationData data = GeneratedMod.data(player);
            String active = data == null ? "" : data.wuxingxiantu$getActiveDivineArt();
            if (!"qi_shi_er_bian".equals(active) && !"yi_hun_ji_po".equals(active)) return;
            if (TransformAbilityRegistry.isCreeperCharging(player)) {
                TransformAbilityRegistry.creeperDetonate(player);
            }
        });
        context.setPacketHandled(true);
    }
}
