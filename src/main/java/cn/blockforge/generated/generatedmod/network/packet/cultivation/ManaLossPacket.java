package cn.blockforge.generated.generatedmod.network.packet.cultivation;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.api.distmarker.Dist;
import java.util.function.Supplier;
public record ManaLossPacket(ListTag rows,boolean enabled){
    public static void encode(ManaLossPacket p,FriendlyByteBuf b){var tag=new CompoundTag();tag.put("Rows",p.rows);tag.putBoolean("Enabled",p.enabled);b.writeNbt(tag);}
    public static ManaLossPacket decode(FriendlyByteBuf b){var tag=b.readNbt();return new ManaLossPacket(tag==null?new ListTag():tag.getList("Rows",10),tag==null||tag.getBoolean("Enabled"));}
    public static void handle(ManaLossPacket p,Supplier<NetworkEvent.Context> supplier){var ctx=supplier.get();ctx.enqueueWork(()->{cn.blockforge.generated.generatedmod.ClientPacketSink.manaLoss.accept(p.rows);cn.blockforge.generated.generatedmod.ClientPacketSink.manaLossTracking.accept(p.enabled);});ctx.setPacketHandled(true);}
}
