package cn.blockforge.generated.generatedmod.network.packet.cultivation;

import cn.blockforge.generated.generatedmod.*;
import static cn.blockforge.generated.generatedmod.Network.*;
import cn.blockforge.generated.generatedmod.items.HumanEmperorBannerItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record OpenBannerPacket() {
    public static void encode(OpenBannerPacket packet, FriendlyByteBuf buf) {}
    public static OpenBannerPacket decode(FriendlyByteBuf buf) { return new OpenBannerPacket(); }
    public static void handle(OpenBannerPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;
            int inventorySlot = cn.blockforge.generated.generatedmod.items.HumanEmperorBannerItem.findBannerSlot(player);
            if (inventorySlot < 0) return;
            cn.blockforge.generated.generatedmod.items.HumanEmperorBannerItem.openMenu(player, inventorySlot);
        });
        context.setPacketHandled(true);
    }
}
