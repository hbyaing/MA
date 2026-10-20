package cn.blockforge.generated.generatedmod.network.packet.cultivation;

import cn.blockforge.generated.generatedmod.*;
import static cn.blockforge.generated.generatedmod.Network.*;
import cn.blockforge.generated.generatedmod.items.CultivationLogic;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record RootFormPacket(int form) {
    public static void encode(RootFormPacket packet, FriendlyByteBuf buf) { buf.writeVarInt(packet.form()); }
    public static RootFormPacket decode(FriendlyByteBuf buf) { return new RootFormPacket(buf.readVarInt()); }
    public static void handle(RootFormPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;
            CultivationData data = GeneratedMod.data(player);
            if (data == null) return;
            if (packet.form() == 0) {
                if (!CultivationLogic.thunderRootAvailable(data)) {
                    if (data.wuxingxiantu$hasThunderRoot() && (data.wuxingxiantu$getRootMask() & 2) != 0) {
                        player.displayClientMessage(Component.literal("天雷灵根已解锁；显化需达到 "
                                + CultivationLogic.realmName(player, ProgressionGate.THUNDER_ROOT_REALM) + "，木灵根")
                                .append(Component.translatable("quality.wuxingxiantu." + (ProgressionGate.THUNDER_ROOT_QUALITY - 1)))
                                .append("及以上"), true);
                    } else player.displayClientMessage(Component.translatable("message.wuxingxiantu.thunder_root_locked",
                            Math.min(CultivationLogic.THUNDER_STRIKES_REQUIRED, data.wuxingxiantu$getThunderStrikes())), true);
                    return;
                }
                boolean next = !data.wuxingxiantu$isThunderRootActive();
                data.wuxingxiantu$setThunderRootActive(next);
                if (player.level() instanceof ServerLevel level) CultivationLogic.clearDomainEffects(level, player);
                CultivationLogic.refreshDomainBuffs(player, data);
                player.displayClientMessage(Component.translatable(next
                        ? "message.wuxingxiantu.thunder_root_switched"
                        : "message.wuxingxiantu.wood_root_switched"), false);
                GeneratedMod.sync(player);
                return;
            }
            if (packet.form() == 1) {
                if (!CultivationLogic.exoticRootAvailable(data)) {
                    player.displayClientMessage(Component.translatable("message.wuxingxiantu.exotic_root_locked"), true);
                    return;
                }
                boolean next = !data.wuxingxiantu$isExoticRootActive();
                data.wuxingxiantu$setExoticRootActive(next);
                if (player.level() instanceof ServerLevel level) CultivationLogic.clearDomainEffects(level, player);
                CultivationLogic.refreshDomainBuffs(player, data);
                player.displayClientMessage(Component.translatable(next
                        ? "message.wuxingxiantu.exotic_root_switched"
                        : "message.wuxingxiantu.fire_root_switched"), false);
                GeneratedMod.sync(player);
            }
        });
        context.setPacketHandled(true);
    }
}
