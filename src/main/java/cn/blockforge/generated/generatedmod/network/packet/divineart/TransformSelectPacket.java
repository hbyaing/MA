package cn.blockforge.generated.generatedmod.network.packet.divineart;

import cn.blockforge.generated.generatedmod.*;
import static cn.blockforge.generated.generatedmod.Network.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record TransformSelectPacket(int index) {
    public static void encode(TransformSelectPacket packet, FriendlyByteBuf buf) {
        buf.writeByte(packet.index());
    }
    public static TransformSelectPacket decode(FriendlyByteBuf buf) {
        return new TransformSelectPacket(buf.readByte());
    }
    public static void handle(TransformSelectPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;
            CultivationData data = GeneratedMod.data(player);
            if (data == null) return;
            int index = packet.index();
            if (DivineArtLogic.confirmTransformSelection(player, index)) {
                String[] forms = data.wuxingxiantu$getPossessedForms();
                player.displayClientMessage(Component.translatable("message.wuxingxiantu.transform_changed",
                        DivineArtLogic.formDisplayName(forms[index])), true);
            }
        });
        context.setPacketHandled(true);
    }
}
