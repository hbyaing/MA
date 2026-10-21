package cn.blockforge.generated.generatedmod.network.packet.divineart;

import cn.blockforge.generated.generatedmod.ClientPacketSink;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import java.util.UUID;
import java.util.function.Supplier;

/** Visual-only state; progress is confirmed by the server, never inferred from animation time. */
public record DipperVisualPacket(UUID player,ResourceLocation dimension,Vec3 origin,float yaw,
                                 long startedAt,int step,int phase,int age) {
    public static final int ACTIVE=0, COMPLETE=1, CANCEL=2;
    public static void encode(DipperVisualPacket p,FriendlyByteBuf b) {
        b.writeUUID(p.player);b.writeResourceLocation(p.dimension);
        b.writeDouble(p.origin.x);b.writeDouble(p.origin.y);b.writeDouble(p.origin.z);
        b.writeFloat(p.yaw);b.writeLong(p.startedAt);b.writeByte(p.step);b.writeByte(p.phase);b.writeVarInt(p.age);
    }
    public static DipperVisualPacket decode(FriendlyByteBuf b) {
        return new DipperVisualPacket(b.readUUID(),b.readResourceLocation(),
            new Vec3(b.readDouble(),b.readDouble(),b.readDouble()),b.readFloat(),b.readLong(),
            b.readUnsignedByte(),b.readUnsignedByte(),b.readVarInt());
    }
    public static void handle(DipperVisualPacket p,Supplier<NetworkEvent.Context> supplier) {
        var context=supplier.get();
        context.enqueueWork(()->ClientPacketSink.safe(ClientPacketSink.dipperVisualPacket,p));
        context.setPacketHandled(true);
    }
}
