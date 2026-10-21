package cn.blockforge.generated.generatedmod.network.packet.heartsword;

import cn.blockforge.generated.generatedmod.ClientPacketSink;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

/** Visual authority only; the server always checks the player's capability for combat. */
public record HeartForgeContractPacket(UUID owner, UUID contract) {
    public static void encode(HeartForgeContractPacket packet,FriendlyByteBuf buf) {
        buf.writeUUID(packet.owner());
        buf.writeNullable(packet.contract(),FriendlyByteBuf::writeUUID);
    }
    public static HeartForgeContractPacket decode(FriendlyByteBuf buf) {
        return new HeartForgeContractPacket(buf.readUUID(),buf.readNullable(FriendlyByteBuf::readUUID));
    }
    public static void handle(HeartForgeContractPacket packet,Supplier<NetworkEvent.Context> supplier) {
        var context=supplier.get();
        context.enqueueWork(()->ClientPacketSink.safe(ClientPacketSink.heartForgeContract,packet));
        context.setPacketHandled(true);
    }
}
