package cn.blockforge.generated.generatedmod;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

/** Persistent launch coordinates, independent of when a host is selected. */
public final class SoulAnchor {
    private static final String KEY = "wuxingxiantu_soul_anchor";
    public static void capture(ServerPlayer player) {
        var tag = new CompoundTag();
        tag.putString("dimension", player.level().dimension().location().toString());
        tag.putDouble("x", player.getX()); tag.putDouble("y", player.getY()); tag.putDouble("z", player.getZ());
        tag.putFloat("yaw", player.getYRot()); tag.putFloat("pitch", player.getXRot());
        player.getPersistentData().put(KEY, tag);
    }
    public static Vec3 position(ServerPlayer player) {
        if (!player.getPersistentData().contains(KEY)) return player.position();
        var tag = player.getPersistentData().getCompound(KEY);
        return new Vec3(tag.getDouble("x"),tag.getDouble("y"),tag.getDouble("z"));
    }
    public static double distance(ServerPlayer player) {
        if (!player.getPersistentData().contains(KEY)) return 0.0D;
        var tag=player.getPersistentData().getCompound(KEY);
        Vec3 origin=position(player);
        if(!player.level().dimension().location().toString().equals(tag.getString("dimension"))
                || !Double.isFinite(origin.x) || !Double.isFinite(origin.y) || !Double.isFinite(origin.z))
            return Double.POSITIVE_INFINITY;
        return player.position().distanceTo(origin);
    }
    public static void returnToBody(ServerPlayer player) {
        if (!player.getPersistentData().contains(KEY)) return;
        var tag = player.getPersistentData().getCompound(KEY).copy();
        var id = net.minecraft.resources.ResourceLocation.tryParse(tag.getString("dimension"));
        if (id == null) return;
        var level = player.server.getLevel(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION,id));
        double x=tag.getDouble("x"), y=tag.getDouble("y"), z=tag.getDouble("z");
        if (level == null || !Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)) return;
        player.getPersistentData().remove(KEY); // validated; dimension callbacks must not recursively teleport
        player.teleportTo(level,x,y,z,tag.getFloat("yaw"),tag.getFloat("pitch"));
        player.setDeltaMovement(Vec3.ZERO); player.fallDistance=0;
    }
    private SoulAnchor() {}
}
