package cn.blockforge.generated.generatedmod.network.packet.divineart;

import cn.blockforge.generated.generatedmod.*;
import static cn.blockforge.generated.generatedmod.Network.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record TransformAttackPacket() {
    public static void encode(TransformAttackPacket packet, FriendlyByteBuf buf) {}
    public static TransformAttackPacket decode(FriendlyByteBuf buf) { return new TransformAttackPacket(); }
    public static void handle(TransformAttackPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;
            CultivationData data = GeneratedMod.data(player);
            if (data == null || !"qi_shi_er_bian".equals(data.wuxingxiantu$getActiveDivineArt())
                    || !"minecraft:warden".equals(data.wuxingxiantu$getActivePossessedForm())
                     || !TransformAbilityRegistry.allowTransformAttack(player)) return;
             TransformAbilityRegistry.markTransformAttack(player);
            net.minecraft.world.entity.LivingEntity target = DivineArtLogic.lookTarget(player, 4.5D);
            if (target == null || target == player || !target.isAlive()) return;
            float damage = 14.0F + Math.max(0, data.wuxingxiantu$getRealm());
            if (target.hurt(player.damageSources().playerAttack(player), damage)) {
                target.knockback(0.55D, player.getX() - target.getX(), player.getZ() - target.getZ());
                player.swing(InteractionHand.MAIN_HAND);
            }
        });
        context.setPacketHandled(true);
    }
}
