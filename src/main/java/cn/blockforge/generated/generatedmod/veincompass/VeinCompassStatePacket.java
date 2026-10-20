package cn.blockforge.generated.generatedmod.veincompass;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

/** Presentation snapshot only. The existing per-dimension vein data remains authoritative. */
public record VeinCompassStatePacket(String dimension,int concentration,boolean hasVein,BlockPos center,int distance,int grade){
    public VeinCompassStatePacket{if(dimension.length()>128||concentration<0||concentration>100||distance<0||distance>384||grade<0||grade>5||hasVein&&grade==0)throw new IllegalArgumentException("vein compass state");center=center.immutable();}
    public VeinCompassStatePacket(FriendlyByteBuf buffer){this(buffer.readUtf(128),buffer.readVarInt(),buffer.readBoolean(),buffer.readBlockPos(),buffer.readVarInt(),buffer.readVarInt());}
    public void encode(FriendlyByteBuf buffer){buffer.writeUtf(dimension,128);buffer.writeVarInt(concentration);buffer.writeBoolean(hasVein);buffer.writeBlockPos(center);buffer.writeVarInt(distance);buffer.writeVarInt(grade);}
    public static void handle(VeinCompassStatePacket packet,Supplier<NetworkEvent.Context> context){var ctx=context.get();ctx.enqueueWork(()->net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT,()->()->cn.blockforge.generated.generatedmod.veincompass.client.VeinCompassHud.accept(packet)));ctx.setPacketHandled(true);}
}
