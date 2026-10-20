package cn.blockforge.generated.generatedmod.network.packet.yujian;

import cn.blockforge.generated.generatedmod.*;
import static cn.blockforge.generated.generatedmod.Network.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record SwordFlightPacket() {
    public static void encode(SwordFlightPacket packet, FriendlyByteBuf buf) {}
    public static SwordFlightPacket decode(FriendlyByteBuf buf) { return new SwordFlightPacket(); }
    public static void handle(SwordFlightPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;
            if (cn.blockforge.generated.generatedmod.yujian.YujianController.riding(player)) {
                cn.blockforge.generated.generatedmod.yujian.YujianController.stop(player);
                return;
            }
            if (!cn.blockforge.generated.generatedmod.yujian.YujianController.canCast(player, "art_sword_flight")) {
                // 统一失败反馈：区分未学御剑凌霄诀、无合格灵剑与境界不足。
                String reason = cn.blockforge.generated.generatedmod.yujian.YujianController.flightDenialReason(player);
                if (reason == null) return;
                if (reason.equals(ProgressionGate.MSG_YUJIAN_REALM)) {
                    var items = cn.blockforge.generated.generatedmod.yujian.YujianController.implementsFor(player,
                            cn.blockforge.generated.generatedmod.yujian.YujianController.FLIGHT);
                    ProgressionGate.deny(player, reason, ProgressionGate.yujianRequiredRealm(
                            GeneratedMod.data(player), items.isEmpty() ? ItemStack.EMPTY : items.get(0)));
                } else {
                    player.displayClientMessage(Component.translatable(reason), true);
                }
                return;
            }
            cn.blockforge.generated.generatedmod.yujian.YujianController.cast(player, "art_sword_flight", 0);
        });
        context.setPacketHandled(true);
    }
}
