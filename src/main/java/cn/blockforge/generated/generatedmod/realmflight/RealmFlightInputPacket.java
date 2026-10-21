package cn.blockforge.generated.generatedmod.realmflight;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record RealmFlightInputPacket(float x,float y,float z,boolean boost) {
    public static void encode(RealmFlightInputPacket p,FriendlyByteBuf b){b.writeFloat(p.x);b.writeFloat(p.y);b.writeFloat(p.z);b.writeBoolean(p.boost);}
    public static RealmFlightInputPacket decode(FriendlyByteBuf b){return new RealmFlightInputPacket(b.readFloat(),b.readFloat(),b.readFloat(),b.readBoolean());}
    public static void handle(RealmFlightInputPacket p,Supplier<NetworkEvent.Context> supplier){var c=supplier.get();c.enqueueWork(()->{var player=c.getSender();if(player!=null)RealmFlightControl.input(player,new Vec3(p.x,p.y,p.z),p.boost);});c.setPacketHandled(true);}
}
