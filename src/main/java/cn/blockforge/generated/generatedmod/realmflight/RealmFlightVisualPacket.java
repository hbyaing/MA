package cn.blockforge.generated.generatedmod.realmflight;

import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

/** Server-authored eligibility. realm is authoritative; visualRealm is a validated cosmetic preference (0 follows current). */
public record RealmFlightVisualPacket(UUID player, boolean active, int realm, int stage, boolean ghost, boolean boost, int visualRealm) {
    public RealmFlightVisualPacket(UUID player, boolean active, int realm, int stage, boolean ghost, boolean boost) {
        this(player,active,realm,stage,ghost,boost,RealmFlightSelection.FOLLOW_CURRENT);
    }
    public static void encode(RealmFlightVisualPacket p, FriendlyByteBuf b) { b.writeUUID(p.player);b.writeBoolean(p.active);b.writeByte(p.realm);b.writeByte(p.stage);b.writeBoolean(p.ghost);b.writeBoolean(p.boost);b.writeByte(p.visualRealm); }
    public static RealmFlightVisualPacket decode(FriendlyByteBuf b) {return new RealmFlightVisualPacket(b.readUUID(),b.readBoolean(),b.readUnsignedByte(),b.readUnsignedByte(),b.readBoolean(),b.readBoolean(),b.readUnsignedByte());}
    public static void handle(RealmFlightVisualPacket p,Supplier<NetworkEvent.Context> supplier) {
        var context=supplier.get();
        context.enqueueWork(()->net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT,
                ()->()->cn.blockforge.generated.generatedmod.client.realmflight.RealmFlightVisuals.receive(p)));
        context.setPacketHandled(true);
    }
}
