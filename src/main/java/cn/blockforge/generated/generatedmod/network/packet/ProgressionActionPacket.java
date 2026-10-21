package cn.blockforge.generated.generatedmod.network.packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;
public record ProgressionActionPacket(String action){
    public static void encode(ProgressionActionPacket p,FriendlyByteBuf b){b.writeUtf(p.action,40);}
    public static ProgressionActionPacket decode(FriendlyByteBuf b){return new ProgressionActionPacket(b.readUtf(40));}
    public static void handle(ProgressionActionPacket p,Supplier<NetworkEvent.Context> s){var c=s.get();c.enqueueWork(()->{if(c.getSender()!=null)cn.blockforge.generated.generatedmod.progression.SpiritualSense.action(c.getSender(),p.action);});c.setPacketHandled(true);}
}
