package cn.blockforge.generated.generatedmod.network.packet.cultivation;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;
import cn.blockforge.generated.generatedmod.*;
import static cn.blockforge.generated.generatedmod.Network.*;
import cn.blockforge.generated.generatedmod.network.service.CompassService;

public record CompassTeleportPacket() {
        public static void encode(CompassTeleportPacket packet, FriendlyByteBuf buf) {}
        public static CompassTeleportPacket decode(FriendlyByteBuf buf) { return new CompassTeleportPacket(); }
        public static void handle(CompassTeleportPacket packet, Supplier<NetworkEvent.Context> supplier) {
            CompassService.handleCompassTeleportPacket(packet, supplier);
        }
    }
