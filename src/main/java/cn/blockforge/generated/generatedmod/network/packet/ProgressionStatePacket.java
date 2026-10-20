package cn.blockforge.generated.generatedmod.network.packet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;
public record ProgressionStatePacket(CompoundTag state){
    public static void encode(ProgressionStatePacket p,FriendlyByteBuf b){b.writeNbt(p.state);}
    public static ProgressionStatePacket decode(FriendlyByteBuf b){var n=b.readNbt();return new ProgressionStatePacket(n==null?new CompoundTag():n);}
    public static void handle(ProgressionStatePacket p,Supplier<NetworkEvent.Context> s){var c=s.get();c.enqueueWork(()->cn.blockforge.generated.generatedmod.ClientPacketSink.safe(cn.blockforge.generated.generatedmod.ClientPacketSink.playerProgression,p.state));c.setPacketHandled(true);}
}
