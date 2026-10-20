package cn.blockforge.generated.generatedmod.network.packet.cultivation;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

/** Authoritative, one-shot shell break; separate from ordinary shield/armor ripples. */
public record ShieldShatterPacket(int entityId, Vec3 direction) {
    public static void encode(ShieldShatterPacket p,FriendlyByteBuf b) {
        b.writeVarInt(p.entityId);b.writeFloat((float)p.direction.x);b.writeFloat((float)p.direction.y);b.writeFloat((float)p.direction.z);
    }
    public static ShieldShatterPacket decode(FriendlyByteBuf b) {
        return new ShieldShatterPacket(b.readVarInt(),new Vec3(b.readFloat(),b.readFloat(),b.readFloat()));
    }
    public static void handle(ShieldShatterPacket p,Supplier<NetworkEvent.Context> supplier) {
        var ctx=supplier.get();
        if(Double.isFinite(p.direction.lengthSqr()) && p.direction.lengthSqr()>.001 && p.direction.lengthSqr()<2)
            ctx.enqueueWork(()->DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                    ()->()->{
                        var level=net.minecraft.client.Minecraft.getInstance().level;
                        // The remove-effect packet may arrive first. The explicit event still plays.
                        if(level!=null&&level.getEntity(p.entityId) instanceof net.minecraft.world.entity.LivingEntity)
                            cn.blockforge.generated.generatedmod.client.SpiritShieldRenderer.shatter(p.entityId,p.direction.normalize());
                    }));
        ctx.setPacketHandled(true);
    }
}
