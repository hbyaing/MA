package cn.blockforge.generated.generatedmod.network.packet;
import cn.blockforge.generated.generatedmod.CoreStructureBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;
public record CoreBuildInputPacket(BlockPos pos,boolean held){
    public static void encode(CoreBuildInputPacket p,FriendlyByteBuf b){b.writeBlockPos(p.pos);b.writeBoolean(p.held);}
    public static CoreBuildInputPacket decode(FriendlyByteBuf b){return new CoreBuildInputPacket(b.readBlockPos(),b.readBoolean());}
    public static void handle(CoreBuildInputPacket p,Supplier<NetworkEvent.Context> supplier){var c=supplier.get();c.enqueueWork(()->{if(c.getSender()!=null)CoreStructureBuilder.input(c.getSender(),p.pos,p.held);});c.setPacketHandled(true);}
}
