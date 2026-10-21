package cn.blockforge.generated.generatedmod.storage.network;
import cn.blockforge.generated.generatedmod.storage.menu.BagMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;
public record BagScrollPacket(int menuId,int row){
 public BagScrollPacket(FriendlyByteBuf b){this(b.readVarInt(),b.readVarInt());}
 public void encode(FriendlyByteBuf b){b.writeVarInt(menuId);b.writeVarInt(row);}
 public static void handle(BagScrollPacket m,Supplier<NetworkEvent.Context> supplier){var c=supplier.get();c.enqueueWork(()->{var p=c.getSender();if(p!=null&&!cn.blockforge.generated.generatedmod.ParalysisControl.blocked(p)&&p.containerMenu instanceof BagMenu bag&&bag.containerId==m.menuId&&bag.stillValid(p))bag.scrollTo(m.row);});c.setPacketHandled(true);}
}
