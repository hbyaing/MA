package cn.blockforge.generated.generatedmod.yujian;

import cn.blockforge.generated.generatedmod.ClientPacketSink;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

/** One post-collision server sample. No client position is accepted by this packet. */
public record FlightSnapshot(int entityId,long tick,Vec3 position,float yaw,float pitch,float roll,float bank,float speed,float angular,float jinghong){
    public static FlightSnapshot of(ControlledSwordEntity sword){
        return new FlightSnapshot(sword.getId(),sword.level().getGameTime(),sword.position(),sword.getYRot(),sword.getXRot(),
                sword.roll(),sword.bank(),sword.flightSpeed(),sword.angularSpeed(),sword.jinghongStrength());
    }
    public static void encode(FlightSnapshot p,FriendlyByteBuf b){
        b.writeVarInt(p.entityId);b.writeLong(p.tick);b.writeDouble(p.position.x);b.writeDouble(p.position.y);b.writeDouble(p.position.z);
        b.writeFloat(p.yaw);b.writeFloat(p.pitch);b.writeFloat(p.roll);b.writeFloat(p.bank);b.writeFloat(p.speed);b.writeFloat(p.angular);
        b.writeFloat(p.jinghong);
    }
    public static FlightSnapshot decode(FriendlyByteBuf b){
        return new FlightSnapshot(b.readVarInt(),b.readLong(),new Vec3(b.readDouble(),b.readDouble(),b.readDouble()),
                b.readFloat(),b.readFloat(),b.readFloat(),b.readFloat(),b.readFloat(),b.readFloat(),b.readFloat());
    }
    public static void handle(FlightSnapshot p,Supplier<NetworkEvent.Context> supplier){
        var context=supplier.get();context.enqueueWork(()->ClientPacketSink.flightSnapshot.accept(p));context.setPacketHandled(true);
    }
}
