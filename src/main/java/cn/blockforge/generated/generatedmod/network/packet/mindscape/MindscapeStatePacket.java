package cn.blockforge.generated.generatedmod.network.packet.mindscape;

import cn.blockforge.generated.generatedmod.*;
import static cn.blockforge.generated.generatedmod.Network.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record MindscapeStatePacket(boolean unstable,int state,int remaining,String challenge){
    public static void encode(MindscapeStatePacket p,FriendlyByteBuf b){b.writeBoolean(p.unstable);b.writeVarInt(p.state);b.writeVarInt(p.remaining);b.writeUtf(p.challenge,256);}
    public static MindscapeStatePacket decode(FriendlyByteBuf b){return new MindscapeStatePacket(b.readBoolean(),b.readVarInt(),b.readVarInt(),b.readUtf(256));}
    public static void handle(MindscapeStatePacket p,Supplier<NetworkEvent.Context> s){var c=s.get();c.enqueueWork(()->ClientPacketSink.safe(ClientPacketSink.mindscapeState,p));c.setPacketHandled(true);}
}
