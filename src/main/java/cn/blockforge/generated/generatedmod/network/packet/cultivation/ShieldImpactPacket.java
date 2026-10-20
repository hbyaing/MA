package cn.blockforge.generated.generatedmod.network.packet.cultivation;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

/** A server-confirmed hit, independent of health/absorption metadata updates. */
public record ShieldImpactPacket(int entityId, Vec3 direction) {
    public static void encode(ShieldImpactPacket p,FriendlyByteBuf b) {
        b.writeVarInt(p.entityId);b.writeFloat((float)p.direction.x);b.writeFloat((float)p.direction.y);b.writeFloat((float)p.direction.z);
    }
    public static ShieldImpactPacket decode(FriendlyByteBuf b) {
        return new ShieldImpactPacket(b.readVarInt(),new Vec3(b.readFloat(),b.readFloat(),b.readFloat()));
    }
    public static void handle(ShieldImpactPacket p,Supplier<NetworkEvent.Context> supplier) {
        var ctx=supplier.get();
        if(Double.isFinite(p.direction.lengthSqr()) && p.direction.lengthSqr()>.001 && p.direction.lengthSqr()<2)
            ctx.enqueueWork(()->DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                    ()->()->{
                        var level=net.minecraft.client.Minecraft.getInstance().level;
                        if(level!=null&&level.getEntity(p.entityId) instanceof net.minecraft.world.entity.LivingEntity living
                            &&living.hasEffect(cn.blockforge.generated.generatedmod.ModEffects.SPIRIT_SHIELD_VISUAL.get()))
                            cn.blockforge.generated.generatedmod.client.SpiritShieldRenderer.impact(p.entityId,p.direction.normalize());
                        cn.blockforge.generated.generatedmod.client.SpiritGuardLayer.impact(p.entityId,p.direction.normalize());
                    }));
        ctx.setPacketHandled(true);
    }
}
