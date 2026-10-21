package cn.blockforge.generated.generatedmod.network.packet.mindscape;

import cn.blockforge.generated.generatedmod.*;
import static cn.blockforge.generated.generatedmod.Network.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record MindscapeCancelPacket(){
    public static void encode(MindscapeCancelPacket p,FriendlyByteBuf b){}
    public static MindscapeCancelPacket decode(FriendlyByteBuf b){return new MindscapeCancelPacket();}
    public static void handle(MindscapeCancelPacket p,Supplier<NetworkEvent.Context> s){var c=s.get();c.enqueueWork(()->{var player=c.getSender();
        if(player!=null&&GeneratedMod.data(player)!=null&&GeneratedMod.data(player).wuxingxiantu$isDaoHeartUnstable())
            cn.blockforge.generated.generatedmod.mindscape.MindscapeManager.cancelMeditation(player);
    });c.setPacketHandled(true);}
}
