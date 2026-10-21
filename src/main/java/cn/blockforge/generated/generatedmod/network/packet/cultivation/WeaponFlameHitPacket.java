package cn.blockforge.generated.generatedmod.network.packet.cultivation;

import cn.blockforge.generated.generatedmod.ClientPacketSink;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

public record WeaponFlameHitPacket(ResourceLocation dimension,int target,UUID targetUuid,ResourceLocation fireId,Vec3 point) {
    public static void encode(WeaponFlameHitPacket p,FriendlyByteBuf b) {
        b.writeResourceLocation(p.dimension());b.writeVarInt(p.target());b.writeUUID(p.targetUuid());b.writeResourceLocation(p.fireId());
        b.writeDouble(p.point().x);b.writeDouble(p.point().y);b.writeDouble(p.point().z);
    }
    public static WeaponFlameHitPacket decode(FriendlyByteBuf b) {
        return new WeaponFlameHitPacket(b.readResourceLocation(),b.readVarInt(),b.readUUID(),b.readResourceLocation(),
                new Vec3(b.readDouble(),b.readDouble(),b.readDouble()));
    }
    public static void handle(WeaponFlameHitPacket p,Supplier<NetworkEvent.Context> supplier) {
        var context=supplier.get();context.enqueueWork(()->ClientPacketSink.safe(ClientPacketSink.weaponFlameHit,p));context.setPacketHandled(true);
    }
}
