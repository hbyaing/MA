package cn.blockforge.generated.generatedmod.formation;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

/** 仅服务端向客户端发送；原版物品 useOn 已提供布阵请求入口。 */
public record SwordArrayStatePacket(FormationState cast,boolean stop) {
    public static void encode(SwordArrayStatePacket packet,FriendlyByteBuf buffer){
        var c=packet.cast;buffer.writeUUID(c.owner());buffer.writeResourceLocation(c.dimension());
        buffer.writeDouble(c.origin().x);buffer.writeDouble(c.origin().y);buffer.writeDouble(c.origin().z);
        buffer.writeLong(c.start());buffer.writeLong(c.seed());buffer.writeBoolean(packet.stop);
    }
    public static SwordArrayStatePacket decode(FriendlyByteBuf buffer){return new SwordArrayStatePacket(
            new FormationState(buffer.readUUID(),buffer.readResourceLocation(),
                    new Vec3(buffer.readDouble(),buffer.readDouble(),buffer.readDouble()),buffer.readLong(),buffer.readLong()),buffer.readBoolean());}
    public static void handle(SwordArrayStatePacket packet,Supplier<NetworkEvent.Context> supplier){
        var context=supplier.get();
        if(packet.cast.valid())context.enqueueWork(()->DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                ()->()->cn.blockforge.generated.generatedmod.formation.client.SwordArrayClient.accept(packet)));
        context.setPacketHandled(true);
    }
}
