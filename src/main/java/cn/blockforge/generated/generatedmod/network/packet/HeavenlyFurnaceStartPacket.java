package cn.blockforge.generated.generatedmod.network.packet;
import cn.blockforge.generated.generatedmod.alchemy.HeavenlyFurnace;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.UUID;
import java.util.function.Supplier;
public record HeavenlyFurnaceStartPacket(UUID token,ResourceLocation recipe){
    public static void encode(HeavenlyFurnaceStartPacket p,FriendlyByteBuf b){b.writeUUID(p.token);b.writeResourceLocation(p.recipe);}
    public static HeavenlyFurnaceStartPacket decode(FriendlyByteBuf b){return new HeavenlyFurnaceStartPacket(b.readUUID(),b.readResourceLocation());}
    public static void handle(HeavenlyFurnaceStartPacket p,Supplier<NetworkEvent.Context> s){var c=s.get();c.enqueueWork(()->{if(c.getSender()!=null)HeavenlyFurnace.start(c.getSender(),p.token,p.recipe);});c.setPacketHandled(true);}
}
