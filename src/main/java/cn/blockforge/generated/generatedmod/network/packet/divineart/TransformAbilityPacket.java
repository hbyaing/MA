package cn.blockforge.generated.generatedmod.network.packet.divineart;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;
import cn.blockforge.generated.generatedmod.*;
import static cn.blockforge.generated.generatedmod.Network.*;

public record TransformAbilityPacket(String skill) {
        public TransformAbilityPacket(){this("");}
        public static void encode(TransformAbilityPacket packet, FriendlyByteBuf buf) {buf.writeUtf(packet.skill(),64);}
        public static TransformAbilityPacket decode(FriendlyByteBuf buf) { return new TransformAbilityPacket(buf.readUtf(64)); }
        public static void handle(TransformAbilityPacket packet, Supplier<NetworkEvent.Context> supplier) {
            NetworkEvent.Context context = supplier.get();
            context.enqueueWork(() -> {
                ServerPlayer player = context.getSender();
                if (player == null) return;
                if(!packet.skill().isEmpty()){cn.blockforge.generated.generatedmod.boss.combat.BossPlayerSkills.cast(player,packet.skill());return;}
                CultivationData data = GeneratedMod.data(player);
                String active = data == null ? "" : data.wuxingxiantu$getActiveDivineArt();
                // 附身也可使用生灵技能；形态、宿主目标和冷却仍由服务端重新校验。
                if (!"qi_shi_er_bian".equals(active) && !"yi_hun_ji_po".equals(active)) return;
                String form = DivineArtLogic.activeAbilityForm(player);
                if (!TransformAbilityRegistry.use(player, form)) {
                    if (TransformAbilityRegistry.hasAbility(form)
                            && TransformAbilityRegistry.remainingCooldown(player) > 0L) {
                        player.displayClientMessage(Component.translatable("message.wuxingxiantu.transform_ability_cooldown"), true);
                    } else if (isLookTargetAbility(form)) {
                        // 需要准星命中生灵的技能（坚守者声波、守卫者灵光、潜影贝、幻术师）无目标时明确反馈。
                        player.displayClientMessage(Component.translatable("message.wuxingxiantu.transform_ability_no_target"), true);
                    }
                }
            });
            context.setPacketHandled(true);
        }
    }
