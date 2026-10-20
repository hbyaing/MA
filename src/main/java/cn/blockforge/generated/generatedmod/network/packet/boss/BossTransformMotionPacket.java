package cn.blockforge.generated.generatedmod.network.packet.boss;

import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.api.distmarker.Dist;

public record BossTransformMotionPacket(UUID player,String form,String animation,long start,int duration){
    public static void encode(BossTransformMotionPacket p,FriendlyByteBuf b){b.writeUUID(p.player);b.writeUtf(p.form,64);b.writeUtf(p.animation,128);b.writeLong(p.start);b.writeVarInt(p.duration);}
    public static BossTransformMotionPacket decode(FriendlyByteBuf b){return new BossTransformMotionPacket(b.readUUID(),b.readUtf(64),b.readUtf(128),b.readLong(),b.readVarInt());}
    public static void handle(BossTransformMotionPacket p,Supplier<NetworkEvent.Context> supplier){var context=supplier.get();if(context.getDirection()==NetworkDirection.PLAY_TO_CLIENT&&p.duration>=0&&p.duration<=2400)context.enqueueWork(()->DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->cn.blockforge.generated.generatedmod.client.boss.BossTransformMotions.accept(p)));context.setPacketHandled(true);}
}
