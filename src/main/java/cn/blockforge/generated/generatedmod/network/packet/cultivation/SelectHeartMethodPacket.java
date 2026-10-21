package cn.blockforge.generated.generatedmod.network.packet.cultivation;

import cn.blockforge.generated.generatedmod.*;
import static cn.blockforge.generated.generatedmod.Network.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record SelectHeartMethodPacket(String methodId) {
    public static void encode(SelectHeartMethodPacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.methodId == null ? "" : packet.methodId, 32);
    }
    public static SelectHeartMethodPacket decode(FriendlyByteBuf buf) {
        return new SelectHeartMethodPacket(buf.readUtf(32));
    }
    public static void handle(SelectHeartMethodPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;
            if (!HeartMethodCatalog.isValid(packet.methodId())) {
                player.displayClientMessage(Component.translatable("message.wuxingxiantu.heart_method_invalid"), true);
                return;
            }
            CultivationData data = GeneratedMod.data(player);
            if (data == null) return;
            if (!data.wuxingxiantu$hasLearnedHeartMethod(packet.methodId())) {
                player.displayClientMessage(Component.translatable("message.wuxingxiantu.heart_method_not_learned"), true);
                return;
            }
            // 统一进度门槛：启用/切换心法时再次校验操作者本人境界；
            // 旧存档已学习的数据保留不清除，达标后自动恢复可用。
            if (!ProgressionGate.canEquipHeartMethod(player, packet.methodId())) {
                ProgressionGate.deny(player, ProgressionGate.MSG_HEART_EQUIP,
                        ProgressionGate.requiredRealmForHeartMethod(packet.methodId()));
                return;
            }
            if (packet.methodId().equals(data.wuxingxiantu$getEquippedHeartMethod())) {
                player.displayClientMessage(Component.translatable("message.wuxingxiantu.heart_method_already"), true);
                return;
            }
            data.wuxingxiantu$setEquippedHeartMethod(packet.methodId());
            player.displayClientMessage(Component.translatable("message.wuxingxiantu.heart_method_changed",
                    HeartMethodCatalog.displayName(packet.methodId())), true);
            GeneratedMod.sync(player);
        });
        context.setPacketHandled(true);
    }
}
