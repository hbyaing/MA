package cn.blockforge.generated.generatedmod.network.packet.cultivation;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;
import cn.blockforge.generated.generatedmod.*;
import static cn.blockforge.generated.generatedmod.Network.*;

public record GoldDomainPacket(boolean enabled) {
        public static void encode(GoldDomainPacket packet, FriendlyByteBuf buf) { buf.writeBoolean(packet.enabled); }
        public static GoldDomainPacket decode(FriendlyByteBuf buf) { return new GoldDomainPacket(buf.readBoolean()); }
        public static void handle(GoldDomainPacket packet, Supplier<NetworkEvent.Context> supplier) {
            toggleDomain(supplier, packet.enabled(), 0, "gold");
        }
    }
