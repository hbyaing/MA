package cn.blockforge.generated.generatedmod.network.packet.cultivation;

import cn.blockforge.generated.generatedmod.*;
import static cn.blockforge.generated.generatedmod.Network.*;
import cn.blockforge.generated.generatedmod.items.HumanEmperorBannerItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record BannerActionPacket(int containerId, int action, int index) {
    public static void encode(BannerActionPacket packet, FriendlyByteBuf buf) {
        buf.writeVarInt(packet.containerId());
        buf.writeByte(packet.action());
        buf.writeVarInt(packet.index());
    }
    public static BannerActionPacket decode(FriendlyByteBuf buf) {
        return new BannerActionPacket(buf.readVarInt(), buf.readByte(), buf.readVarInt());
    }
    public static void handle(BannerActionPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || !(player.containerMenu instanceof HumanEmperorBannerMenu menu)
                    || menu.containerId != packet.containerId()) return;
            int bannerSoulCount = menu.state().souls().size();
            int myriadSoulCount = menu.state().myriadSouls().size();
            boolean validIndex = packet.index() >= -1
                    && (packet.action() == cn.blockforge.generated.generatedmod.items.HumanEmperorBannerItem.ACTION_TRANSFER
                    ? packet.index() < bannerSoulCount + myriadSoulCount
                    : packet.index() < bannerSoulCount);
            if (packet.action() < 0 || packet.action() > cn.blockforge.generated.generatedmod.items.HumanEmperorBannerItem.ACTION_TOGGLE_COMBAT
                    || !validIndex) return;
            menu.handleAction(player, packet.action(), packet.index());
            sendBannerState(player, menu);
        });
        context.setPacketHandled(true);
    }
}
