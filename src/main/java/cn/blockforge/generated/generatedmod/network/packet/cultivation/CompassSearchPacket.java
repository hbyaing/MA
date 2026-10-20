package cn.blockforge.generated.generatedmod.network.packet.cultivation;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;
import cn.blockforge.generated.generatedmod.*;
import static cn.blockforge.generated.generatedmod.Network.*;
import cn.blockforge.generated.generatedmod.network.service.CompassService;

public record CompassSearchPacket(String name) {
        public static void encode(CompassSearchPacket packet, FriendlyByteBuf buf) {
            buf.writeUtf(packet.name(), 128);
        }
        public static CompassSearchPacket decode(FriendlyByteBuf buf) {
            return new CompassSearchPacket(buf.readUtf(128));
        }
        public static void handle(CompassSearchPacket packet, Supplier<NetworkEvent.Context> supplier) {
            CompassService.handleCompassSearchPacket(packet, supplier);
        }
    }
