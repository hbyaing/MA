package cn.blockforge.generated.generatedmod.network.packet.divineart;

import cn.blockforge.generated.generatedmod.*;
import static cn.blockforge.generated.generatedmod.Network.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record PossessTargetPacket(int entityId) {
    public static void encode(PossessTargetPacket packet, FriendlyByteBuf buf) { buf.writeVarInt(packet.entityId()); }
    public static PossessTargetPacket decode(FriendlyByteBuf buf) { return new PossessTargetPacket(buf.readVarInt()); }
    public static void handle(PossessTargetPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || !(player.level() instanceof ServerLevel level)) return;
            CultivationData data = GeneratedMod.data(player);
            if (data == null) return;
            String active=data.wuxingxiantu$getActiveDivineArt();
            if (!"yi_hun_ji_po".equals(active)&&!"yuan_shen_chu_qiao".equals(active)) return;
            net.minecraft.world.entity.Entity entity = level.getEntity(packet.entityId());
            // Projection right-clicks use this packet instead of vanilla entity interaction.
            // Never treat a retained body as a new possession target.
            if (entity instanceof SoulBodyEntity body) {
                body.returnSoul(player);
                return;
            }
            if (!"yi_hun_ji_po".equals(active)||DivineArtLogic.isPossessing(player)) return;
            net.minecraft.world.entity.LivingEntity target = DivineArtLogic.possessionTarget(entity);
            if (target != null) {
                DivineArtLogic.tryPossess(player, target);
            }
        });
        context.setPacketHandled(true);
    }
}
