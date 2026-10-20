package cn.blockforge.generated.generatedmod.storage.network;
import cn.blockforge.generated.generatedmod.storage.menu.BagMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;
public record BagFilterPacket(int menuId,String query,int category){
 public BagFilterPacket(FriendlyByteBuf b){this(b.readVarInt(),b.readUtf(80),b.readVarInt());}
 public void encode(FriendlyByteBuf b){b.writeVarInt(menuId);b.writeUtf(query,80);b.writeVarInt(category);}
 public static void handle(BagFilterPacket m,Supplier<NetworkEvent.Context> supplier){var c=supplier.get();c.enqueueWork(()->{var p=c.getSender();if(p!=null&&!cn.blockforge.generated.generatedmod.ParalysisControl.blocked(p)&&p.containerMenu instanceof BagMenu bag&&bag.containerId==m.menuId&&bag.stillValid(p))bag.filter(m.query,m.category);});c.setPacketHandled(true);}
}
