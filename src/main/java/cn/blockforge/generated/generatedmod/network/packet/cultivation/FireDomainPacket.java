package cn.blockforge.generated.generatedmod.network.packet.cultivation;

import cn.blockforge.generated.generatedmod.*;
import static cn.blockforge.generated.generatedmod.Network.*;
import cn.blockforge.generated.generatedmod.items.CultivationLogic;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record FireDomainPacket(boolean enabled) {
    public static void encode(FireDomainPacket packet, FriendlyByteBuf buf) { buf.writeBoolean(packet.enabled); }
    public static FireDomainPacket decode(FriendlyByteBuf buf) { return new FireDomainPacket(buf.readBoolean()); }
    public static void handle(FireDomainPacket packet, Supplier<NetworkEvent.Context> supplier) {
        toggleDomain(supplier, packet.enabled(), 3, "fire");
    }
}
