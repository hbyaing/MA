package cn.blockforge.generated.generatedmod.tribulation;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import java.util.UUID;
import java.util.function.Supplier;
import cn.blockforge.generated.generatedmod.ClientPacketSink;
public record TribulationVisualPacket(UUID owner,String dimension,long eventId,int realm,int kind,int count,int seed,long tick,Vec3 start,Vec3 end,boolean grounded) {
 public static void encode(TribulationVisualPacket p,FriendlyByteBuf b){b.writeUUID(p.owner);b.writeUtf(p.dimension);b.writeLong(p.eventId);b.writeVarInt(p.realm);b.writeVarInt(p.kind);b.writeVarInt(p.count);b.writeInt(p.seed);b.writeLong(p.tick);for(Vec3 v:new Vec3[]{p.start,p.end}){b.writeDouble(v.x);b.writeDouble(v.y);b.writeDouble(v.z);}b.writeBoolean(p.grounded);}
 public static TribulationVisualPacket decode(FriendlyByteBuf b){return new TribulationVisualPacket(b.readUUID(),b.readUtf(128),b.readLong(),b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readInt(),b.readLong(),new Vec3(b.readDouble(),b.readDouble(),b.readDouble()),new Vec3(b.readDouble(),b.readDouble(),b.readDouble()),b.readBoolean());}
 public static void handle(TribulationVisualPacket p,Supplier<NetworkEvent.Context> s){var c=s.get();c.enqueueWork(()->ClientPacketSink.safe(ClientPacketSink.tribulationVisual,p));c.setPacketHandled(true);}
}
