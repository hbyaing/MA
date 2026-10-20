package cn.blockforge.generated.generatedmod.network.packet.cultivation;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;
import cn.blockforge.generated.generatedmod.*;
import static cn.blockforge.generated.generatedmod.Network.*;

public record WaterDomainPacket(boolean enabled) {
        public static void encode(WaterDomainPacket packet, FriendlyByteBuf buf) { buf.writeBoolean(packet.enabled); }
        public static WaterDomainPacket decode(FriendlyByteBuf buf) { return new WaterDomainPacket(buf.readBoolean()); }
        public static void handle(WaterDomainPacket packet, Supplier<NetworkEvent.Context> supplier) {
            toggleDomain(supplier, packet.enabled(), 2, "water");
        }
    }
