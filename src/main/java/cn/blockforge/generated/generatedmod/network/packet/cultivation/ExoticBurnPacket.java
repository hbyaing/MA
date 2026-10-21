package cn.blockforge.generated.generatedmod.network.packet.cultivation;

import cn.blockforge.generated.generatedmod.*;
import static cn.blockforge.generated.generatedmod.Network.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record ExoticBurnPacket(int entityId, java.util.UUID entityUuid, net.minecraft.resources.ResourceLocation dimension,
                               String sourceFireId, int duration, float intensity, java.util.UUID sourceUuid, long sequence) {
    public static void encode(ExoticBurnPacket packet, FriendlyByteBuf buf) {
        buf.writeVarInt(packet.entityId());
        buf.writeUUID(packet.entityUuid());buf.writeResourceLocation(packet.dimension());
        buf.writeUtf(packet.sourceFireId(),128);
        buf.writeVarInt(Math.max(0, Math.min(72000, packet.duration())));
        buf.writeFloat(packet.intensity());buf.writeBoolean(packet.sourceUuid()!=null);
        if(packet.sourceUuid()!=null)buf.writeUUID(packet.sourceUuid());buf.writeLong(packet.sequence());
    }
    public static ExoticBurnPacket decode(FriendlyByteBuf buf) {
        int id=buf.readVarInt();var uuid=buf.readUUID();var dimension=buf.readResourceLocation();String fire=buf.readUtf(128);
        int ticks=Math.max(0,Math.min(72000,buf.readVarInt()));float intensity=buf.readFloat();
        var source=buf.readBoolean()?buf.readUUID():null;
        return new ExoticBurnPacket(id,uuid,dimension,fire,ticks,Float.isFinite(intensity)?Math.max(0,Math.min(3,intensity)):0,source,buf.readLong());
    }
    public static void handle(ExoticBurnPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> ClientPacketSink.safe(ClientPacketSink.exoticBurnPacket, packet));
        context.setPacketHandled(true);
    }
}
