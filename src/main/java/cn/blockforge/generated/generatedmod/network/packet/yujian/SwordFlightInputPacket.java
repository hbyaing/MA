package cn.blockforge.generated.generatedmod.network.packet.yujian;

import cn.blockforge.generated.generatedmod.*;
import static cn.blockforge.generated.generatedmod.Network.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record SwordFlightInputPacket(boolean summonHeld, boolean togglePressed,
                                      boolean forward, boolean brake, boolean up, boolean down,
                                      boolean rollLeft, boolean rollRight, boolean sprint, boolean recenter,
                                      float rollSensitivity, int releaseMode, float yaw, float pitch) {
    public static void encode(SwordFlightInputPacket p, FriendlyByteBuf buf) {
        buf.writeBoolean(p.summonHeld); buf.writeBoolean(p.togglePressed);
        buf.writeBoolean(p.forward); buf.writeBoolean(p.brake); buf.writeBoolean(p.up); buf.writeBoolean(p.down);
        buf.writeBoolean(p.rollLeft); buf.writeBoolean(p.rollRight); buf.writeBoolean(p.sprint); buf.writeBoolean(p.recenter);
        buf.writeFloat(p.rollSensitivity); buf.writeByte(p.releaseMode);
        buf.writeFloat(p.yaw); buf.writeFloat(p.pitch);
    }
    public static SwordFlightInputPacket decode(FriendlyByteBuf buf) {
        return new SwordFlightInputPacket(buf.readBoolean(),buf.readBoolean(),buf.readBoolean(),buf.readBoolean(),
                buf.readBoolean(),buf.readBoolean(),buf.readBoolean(),buf.readBoolean(),buf.readBoolean(),buf.readBoolean(),buf.readFloat(),
                buf.readByte(),buf.readFloat(),buf.readFloat());
    }
    public static void handle(SwordFlightInputPacket p, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context=supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player=context.getSender();
            if(player!=null) cn.blockforge.generated.generatedmod.yujian.YujianController.handleInput(player,
                    p.summonHeld,p.togglePressed,p.forward,p.brake,p.up,p.down,p.rollLeft,p.rollRight,p.sprint,p.recenter,
                    p.rollSensitivity,p.releaseMode,p.yaw,p.pitch);
        });
        context.setPacketHandled(true);
    }
}
