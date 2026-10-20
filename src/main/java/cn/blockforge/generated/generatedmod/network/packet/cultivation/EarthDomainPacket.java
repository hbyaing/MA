package cn.blockforge.generated.generatedmod.network.packet.cultivation;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;
import cn.blockforge.generated.generatedmod.*;
import static cn.blockforge.generated.generatedmod.Network.*;

public record EarthDomainPacket(boolean enabled) {
        public static void encode(EarthDomainPacket packet, FriendlyByteBuf buf) { buf.writeBoolean(packet.enabled); }
        public static EarthDomainPacket decode(FriendlyByteBuf buf) { return new EarthDomainPacket(buf.readBoolean()); }
        public static void handle(EarthDomainPacket packet, Supplier<NetworkEvent.Context> supplier) {
            toggleDomain(supplier, packet.enabled(), 4, "earth");
        }
    }
