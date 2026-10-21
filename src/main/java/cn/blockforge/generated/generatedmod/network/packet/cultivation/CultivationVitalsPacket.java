package cn.blockforge.generated.generatedmod.network.packet.cultivation;

import cn.blockforge.generated.generatedmod.ClientPacketSink;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record CultivationVitalsPacket(float currentMana,float maxMana,int bigu,long cultivation,long merit){
    public static void encode(CultivationVitalsPacket p,FriendlyByteBuf b){
        int start=b.writerIndex();b.writeFloat(p.currentMana);b.writeFloat(p.maxMana);b.writeVarInt(p.bigu);
        b.writeVarLong(p.cultivation);b.writeVarLong(p.merit);
        cn.blockforge.generated.generatedmod.network.SyncMetrics.record(b.writerIndex()-start);
    }
    public static CultivationVitalsPacket decode(FriendlyByteBuf b){return new CultivationVitalsPacket(b.readFloat(),b.readFloat(),b.readVarInt(),b.readVarLong(),b.readVarLong());}
    public static void handle(CultivationVitalsPacket p,Supplier<NetworkEvent.Context> supplier){
        var context=supplier.get();context.enqueueWork(()->ClientPacketSink.safe(ClientPacketSink.cultivationVitals,p));context.setPacketHandled(true);
    }
}
