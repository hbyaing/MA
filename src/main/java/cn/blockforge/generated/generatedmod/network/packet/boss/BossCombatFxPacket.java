package cn.blockforge.generated.generatedmod.network.packet.boss;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

/** Server-authoritative telegraph description. Coordinates are absolute, in the current arena. */
public record BossCombatFxPacket(int bossId, int effectId, int shape, Vec3 start, Vec3 end,
                                 float radius, float width, int color, int windup, int lifetime, String label) {
    public static final int CIRCLE = 0, LINE = 1, RING = 2, CONE = 3, MARK = 4, CLEAR = 5, REMOVE = 6;
    public static final int MAX_LABEL_LENGTH = 160;
    public static final int DANGER_COLOR = 0xF47B4A, SAFE_COLOR = 0x48DBC4;

    public BossCombatFxPacket {
        color &= 0xFFFFFF;
        windup = Math.max(0, Math.min(1200, windup));
        lifetime = Math.max(windup + 1, Math.min(2400, Math.max(1, lifetime)));
        if (Float.isFinite(radius)) radius = Math.max(0.05F, Math.min(80, radius));
        if (Float.isFinite(width)) width = Math.max(0.03F, Math.min(shape == CONE ? 360 : 80, width));
        if (shape == CLEAR || shape == REMOVE) { start = Vec3.ZERO; end = Vec3.ZERO; }
        label = label == null ? "" : label.replaceAll("[\\p{Cntrl}§]", " ").strip();
        if (label.length() > MAX_LABEL_LENGTH) label = label.substring(0, MAX_LABEL_LENGTH);
    }

    /** Bounds are independent of client classes, so malformed data cannot reach a render buffer. */
    public boolean valid() {
        return bossId >= 0 && shape >= CIRCLE && shape <= REMOVE
                && (shape == CLEAR || shape == REMOVE || (finite(start) && finite(end)
                && Float.isFinite(radius) && Float.isFinite(width)
                && (shape != LINE || start.distanceToSqr(end) <= 256 * 256)));
    }

    private static boolean finite(Vec3 p) {
        return p != null && Double.isFinite(p.x) && Double.isFinite(p.y) && Double.isFinite(p.z)
                && Math.abs(p.x) <= 30_000_000 && Math.abs(p.z) <= 30_000_000 && Math.abs(p.y) <= 2048;
    }

    public static void encode(BossCombatFxPacket p, FriendlyByteBuf buffer) {
        buffer.writeVarInt(p.bossId());
        buffer.writeVarInt(p.effectId());
        buffer.writeByte(p.shape());
        writeVector(buffer, p.start());
        writeVector(buffer, p.end());
        buffer.writeFloat(p.radius());
        buffer.writeFloat(p.width());
        buffer.writeInt(p.color());
        buffer.writeVarInt(p.windup());
        buffer.writeVarInt(p.lifetime());
        buffer.writeUtf(p.label(), MAX_LABEL_LENGTH);
    }

    public static BossCombatFxPacket decode(FriendlyByteBuf buffer) {
        return new BossCombatFxPacket(buffer.readVarInt(), buffer.readVarInt(), buffer.readUnsignedByte(),
                readVector(buffer), readVector(buffer), buffer.readFloat(), buffer.readFloat(), buffer.readInt(),
                buffer.readVarInt(), buffer.readVarInt(), buffer.readUtf(MAX_LABEL_LENGTH));
    }

    private static void writeVector(FriendlyByteBuf buffer, Vec3 p) {
        buffer.writeDouble(p.x);
        buffer.writeDouble(p.y);
        buffer.writeDouble(p.z);
    }

    private static Vec3 readVector(FriendlyByteBuf buffer) {
        return new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
    }

    public static void handle(BossCombatFxPacket p, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        if (context.getDirection() == NetworkDirection.PLAY_TO_CLIENT && p.valid()) {
            context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                    () -> () -> cn.blockforge.generated.generatedmod.client.boss.BossCombatFx.accept(p)));
        }
        context.setPacketHandled(true);
    }
}
