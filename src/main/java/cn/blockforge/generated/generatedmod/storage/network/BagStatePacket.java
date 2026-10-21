package cn.blockforge.generated.generatedmod.storage.network;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;
public record BagStatePacket(int menuId,int row,int ranges,boolean magnet,long[] totals){
 private static long[] readTotals(FriendlyByteBuf b){int length=b.readVarInt();if(length!=54)throw new IllegalArgumentException("Invalid bag view");long[] totals=new long[54];for(int i=0;i<54;i++)totals[i]=b.readVarLong();return totals;}
 public BagStatePacket(FriendlyByteBuf b){this(b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readBoolean(),readTotals(b));}
 public void encode(FriendlyByteBuf b){b.writeVarInt(menuId);b.writeVarInt(row);b.writeVarInt(ranges);b.writeBoolean(magnet);b.writeVarInt(54);for(long n:totals)b.writeVarLong(n);}
 public static void handle(BagStatePacket m,Supplier<NetworkEvent.Context> supplier){var c=supplier.get();c.enqueueWork(()->net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT,()->()->cn.blockforge.generated.generatedmod.storage.client.ClientBagState.apply(m)));c.setPacketHandled(true);}
}
