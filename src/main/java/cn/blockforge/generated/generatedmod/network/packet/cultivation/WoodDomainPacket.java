package cn.blockforge.generated.generatedmod.network.packet.cultivation;

import cn.blockforge.generated.generatedmod.*;
import static cn.blockforge.generated.generatedmod.Network.*;
import cn.blockforge.generated.generatedmod.items.CultivationLogic;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record WoodDomainPacket(boolean enabled) {
    public static void encode(WoodDomainPacket packet, FriendlyByteBuf buf) { buf.writeBoolean(packet.enabled); }
    public static WoodDomainPacket decode(FriendlyByteBuf buf) { return new WoodDomainPacket(buf.readBoolean()); }
    public static void handle(WoodDomainPacket packet, Supplier<NetworkEvent.Context> supplier) {
        toggleDomain(supplier, packet.enabled(), 1, "wood");
    }
}
