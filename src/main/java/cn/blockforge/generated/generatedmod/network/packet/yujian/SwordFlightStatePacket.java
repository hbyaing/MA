package cn.blockforge.generated.generatedmod.network.packet.yujian;

import cn.blockforge.generated.generatedmod.*;
import static cn.blockforge.generated.generatedmod.Network.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record SwordFlightStatePacket(boolean active) {
    public static void encode(SwordFlightStatePacket p, FriendlyByteBuf buf) { buf.writeBoolean(p.active); }
    public static SwordFlightStatePacket decode(FriendlyByteBuf buf) { return new SwordFlightStatePacket(buf.readBoolean()); }
    public static void handle(SwordFlightStatePacket p, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context=supplier.get();
        context.enqueueWork(() -> net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(
                net.minecraftforge.api.distmarker.Dist.CLIENT,
                () -> () -> cn.blockforge.generated.generatedmod.client.YujianClientController.setActive(p.active())));
        context.setPacketHandled(true);
    }
}
