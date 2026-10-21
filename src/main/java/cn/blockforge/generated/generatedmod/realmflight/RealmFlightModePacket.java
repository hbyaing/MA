package cn.blockforge.generated.generatedmod.realmflight;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

/** Cosmetic choice only: never changes mayfly, speed, cost or game mode. */
public record RealmFlightModePacket(boolean special, int preferredRealm) {
    public static final String VANILLA="wuxingxiantu:vanilla_dunguang_visual";
    public static final String VISUAL_REALM="wuxingxiantu:dunguang_visual_realm";
    public RealmFlightModePacket(boolean special) { this(special, RealmFlightSelection.FOLLOW_CURRENT); }
    public static void encode(RealmFlightModePacket p,FriendlyByteBuf b){b.writeBoolean(p.special);b.writeVarInt(p.preferredRealm);}
    public static RealmFlightModePacket decode(FriendlyByteBuf b){return new RealmFlightModePacket(b.readBoolean(),b.readVarInt());}
    public static void handle(RealmFlightModePacket p,Supplier<NetworkEvent.Context> supplier) {
        var context=supplier.get();
        context.enqueueWork(()->{
            var player=context.getSender();if(player==null)return;
            var data=cn.blockforge.generated.generatedmod.GeneratedMod.data(player);
            int actual=data==null?0:data.wuxingxiantu$getRealm();
            player.getPersistentData().putBoolean(VANILLA,!p.special);
            player.getPersistentData().putInt(VISUAL_REALM,RealmFlightSelection.sanitizePreference(actual,p.preferredRealm));
        });
        context.setPacketHandled(true);
    }
}
