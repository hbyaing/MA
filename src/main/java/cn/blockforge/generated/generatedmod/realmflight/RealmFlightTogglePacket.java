package cn.blockforge.generated.generatedmod.realmflight;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

/** Explicit double-jump request; a visible buff alone cannot authorize flight. */
public record RealmFlightTogglePacket(boolean flying) {
    public static void encode(RealmFlightTogglePacket p,FriendlyByteBuf b){b.writeBoolean(p.flying);}
    public static RealmFlightTogglePacket decode(FriendlyByteBuf b){return new RealmFlightTogglePacket(b.readBoolean());}
    public static void handle(RealmFlightTogglePacket p,Supplier<NetworkEvent.Context> supplier){var c=supplier.get();c.enqueueWork(()->{
        var player=c.getSender();if(player==null||!RealmFlightControl.eligible(player))return;
        var d=GeneratedMod.data(player);
        boolean allowed=d.wuxingxiantu$getRealm()>=5||d.wuxingxiantu$getAura()>=1.25F;
        player.getAbilities().mayfly=allowed;player.getAbilities().flying=p.flying&&allowed;
        RealmFlightControl.release(player);
        if(player.getAbilities().flying&&player.onGround())player.setDeltaMovement(player.getDeltaMovement().add(0,.16,0));
        player.onUpdateAbilities();
    });c.setPacketHandled(true);}
}
