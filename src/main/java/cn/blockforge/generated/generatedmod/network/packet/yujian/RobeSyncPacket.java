package cn.blockforge.generated.generatedmod.network.packet.yujian;

import cn.blockforge.generated.generatedmod.*;
import static cn.blockforge.generated.generatedmod.Network.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import java.util.UUID;
import java.util.function.Supplier;

public record RobeSyncPacket(int entityId,java.util.UUID owner,ItemStack stack,ItemStack sword,boolean swordLocked){
    public static void encode(RobeSyncPacket p,FriendlyByteBuf b){b.writeVarInt(p.entityId);b.writeUUID(p.owner);b.writeItem(p.stack);b.writeItem(p.sword);b.writeBoolean(p.swordLocked);}
    public static RobeSyncPacket decode(FriendlyByteBuf b){return new RobeSyncPacket(b.readVarInt(),b.readUUID(),b.readItem(),b.readItem(),b.readBoolean());}
    public static void handle(RobeSyncPacket p,Supplier<NetworkEvent.Context> s){var c=s.get();c.enqueueWork(()->ClientPacketSink.safe(ClientPacketSink.robeSync,p));c.setPacketHandled(true);}
}
