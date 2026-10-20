package cn.blockforge.generated.generatedmod.network.packet;
import cn.blockforge.generated.generatedmod.ClientPacketSink;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;
public record CoreBuildProgressPacket(BlockPos pos,int progress,boolean finished){
    public static void encode(CoreBuildProgressPacket p,FriendlyByteBuf b){b.writeBlockPos(p.pos);b.writeVarInt(p.progress);b.writeBoolean(p.finished);}
    public static CoreBuildProgressPacket decode(FriendlyByteBuf b){return new CoreBuildProgressPacket(b.readBlockPos(),b.readVarInt(),b.readBoolean());}
    public static void handle(CoreBuildProgressPacket p,Supplier<NetworkEvent.Context> supplier){var c=supplier.get();c.enqueueWork(()->ClientPacketSink.coreBuildProgress.accept(p));c.setPacketHandled(true);}
}
