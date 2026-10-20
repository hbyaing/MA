package cn.blockforge.generated.generatedmod.network.packet;
import cn.blockforge.generated.generatedmod.ClientPacketSink;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;
public record HeavenlyFurnaceStatePacket(CompoundTag tag){
    public static void encode(HeavenlyFurnaceStatePacket p,FriendlyByteBuf b){b.writeNbt(p.tag);}
    public static HeavenlyFurnaceStatePacket decode(FriendlyByteBuf b){var t=b.readNbt();return new HeavenlyFurnaceStatePacket(t==null?new CompoundTag():t);}
    public static void handle(HeavenlyFurnaceStatePacket p,Supplier<NetworkEvent.Context> s){var c=s.get();c.enqueueWork(()->ClientPacketSink.safe(ClientPacketSink.heavenlyFurnace,p.tag));c.setPacketHandled(true);}
}
