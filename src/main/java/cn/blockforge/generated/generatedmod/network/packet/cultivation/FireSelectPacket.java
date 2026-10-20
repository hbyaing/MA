package cn.blockforge.generated.generatedmod.network.packet.cultivation;

import cn.blockforge.generated.generatedmod.*;
import static cn.blockforge.generated.generatedmod.Network.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record FireSelectPacket(int tier) {
    public static void encode(FireSelectPacket packet, FriendlyByteBuf buf) { buf.writeByte(packet.tier()); }
    public static FireSelectPacket decode(FriendlyByteBuf buf) { return new FireSelectPacket(buf.readUnsignedByte()); }
    public static void handle(FireSelectPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || packet.tier() < 0 || packet.tier() > 9) return;
            CultivationData data = GeneratedMod.data(player);
            if (data == null) return;
            // 统一进度门槛：即使命令/旧存档/创造切换让低境界玩家“已学”高阶异火，
            // 装备驭使仍由 ProgressionGate 再验证。
            if (packet.tier() != 0 && !ProgressionGate.canUseFire(player, packet.tier())) {
                ProgressionGate.denyFireUse(player, packet.tier());
                return;
            }
            data.wuxingxiantu$setEquippedFire(packet.tier());
            cn.blockforge.generated.generatedmod.fire.WeaponFlameState.refresh(player);
            GeneratedMod.sync(player);
        });
        context.setPacketHandled(true);
    }
}
