package cn.blockforge.generated.generatedmod.network.packet.yujian;

import cn.blockforge.generated.generatedmod.*;
import static cn.blockforge.generated.generatedmod.Network.*;
import cn.blockforge.generated.generatedmod.items.SpiritSwordItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record SwordNurtureActionPacket(byte action, int swordSlot, int materialSlot, int techniqueSlot, String technique) {
    public static void encode(SwordNurtureActionPacket p, FriendlyByteBuf buf) {
        buf.writeByte(p.action); buf.writeByte(p.swordSlot); buf.writeByte(p.materialSlot); buf.writeByte(p.techniqueSlot);
        buf.writeUtf(p.technique == null ? "" : p.technique, 64);
    }
    public static SwordNurtureActionPacket decode(FriendlyByteBuf buf) {
        return new SwordNurtureActionPacket(buf.readByte(), buf.readByte(), buf.readByte(), buf.readByte(), buf.readUtf(64));
    }
    public static void handle(SwordNurtureActionPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;
            ItemStack sword = cn.blockforge.generated.generatedmod.robe.YujianSlot.nurtureStack(player,packet.swordSlot);
            if (!(sword.getItem() instanceof SpiritSwordItem spirit)) return;
            cn.blockforge.generated.generatedmod.yujian.SwordNurture.migrate(player, sword);
            CultivationData cultivation = GeneratedMod.data(player);
            if (cultivation == null || !player.isAlive() || player.isSpectator()) return;
            var profile = spirit.profile();
            var data = SpiritSwordItem.nurture(sword);
            if (packet.action == 0) {
                if (!cn.blockforge.generated.generatedmod.yujian.SwordNurture.breakthrough(player,sword)) return;
            } else if (packet.action == 1) {
                var technique = cn.blockforge.generated.generatedmod.yujian.SwordTechniqueRegistry.get(packet.technique);
                if (cultivation.wuxingxiantu$knowsSwordTechnique(packet.technique)
                        || !ProgressionGate.canUseSword(player, sword) || !ProgressionGate.canLearnArt(player, packet.technique)) return;
                if (!cn.blockforge.generated.generatedmod.yujian.SwordTechniqueRegistry.canLearn(technique, profile, data)) return;
                if (!cultivation.wuxingxiantu$learnSwordTechnique(technique.id())) return;
                data.insightPoints -= technique.insightCost();
                SpiritSwordItem.saveNurture(sword, data);
            } else if (packet.action == 2) {
                if (!cultivation.wuxingxiantu$knowsSwordTechnique(packet.technique)
                        || !cn.blockforge.generated.generatedmod.yujian.SwordTechniqueRegistry.supports(
                                cn.blockforge.generated.generatedmod.yujian.SwordTechniqueRegistry.get(packet.technique), profile, data)
                        || !ProgressionGate.canUseSword(player, sword) || !ProgressionGate.canUseArt(player, packet.technique)
                        || data.equippedTechniques.contains(packet.technique)
                        || data.equippedTechniques.size() >= profile.maxTechniqueSlots()) return;
                data.equippedTechniques.add(packet.technique); SpiritSwordItem.saveNurture(sword, data);
            } else if (packet.action == 3 && packet.techniqueSlot >= 0 && packet.techniqueSlot < data.equippedTechniques.size()) {
                data.equippedTechniques.remove(packet.techniqueSlot); SpiritSwordItem.saveNurture(sword, data);
            } else if (packet.action == 4) {
                cn.blockforge.generated.generatedmod.yujian.SwordNurture.select(player, sword);
            } else if (packet.action == 5) {
                cn.blockforge.generated.generatedmod.yujian.SwordNurture.select(player, sword);
                if (!cn.blockforge.generated.generatedmod.yujian.SwordNurture.cast(player, sword, packet.technique))
                    player.displayClientMessage(Component.translatable("message.wuxingxiantu.sword_technique_denied"), true);
            }
            player.getInventory().setChanged();
            player.containerMenu.broadcastChanges();
            cn.blockforge.generated.generatedmod.robe.RobeSlots.sync(player);
            GeneratedMod.sync(player);
        });
        context.setPacketHandled(true);
    }
}
