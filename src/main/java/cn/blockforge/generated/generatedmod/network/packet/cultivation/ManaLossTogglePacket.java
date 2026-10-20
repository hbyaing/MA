package cn.blockforge.generated.generatedmod.network.packet.cultivation;
import cn.blockforge.generated.generatedmod.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;
public record ManaLossTogglePacket(boolean enabled){
    public static void encode(ManaLossTogglePacket p,FriendlyByteBuf b){b.writeBoolean(p.enabled);}
    public static ManaLossTogglePacket decode(FriendlyByteBuf b){return new ManaLossTogglePacket(b.readBoolean());}
    public static void handle(ManaLossTogglePacket p,Supplier<NetworkEvent.Context> supplier){var context=supplier.get();context.enqueueWork(()->{
        var player=context.getSender();if(player!=null&&GeneratedMod.data(player) instanceof CultivationDataImpl data){data.setManaLossEnabled(p.enabled);cn.blockforge.generated.generatedmod.network.CultivationSync.publish(player);}
    });context.setPacketHandled(true);}
}
