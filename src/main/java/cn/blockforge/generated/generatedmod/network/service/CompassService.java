package cn.blockforge.generated.generatedmod.network.service;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.network.NetworkEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import cn.blockforge.generated.generatedmod.network.packet.cultivation.CompassResultPacket;
import cn.blockforge.generated.generatedmod.*;
import static cn.blockforge.generated.generatedmod.Network.*;
import cn.blockforge.generated.generatedmod.network.packet.cultivation.CompassTeleportPacket;
import cn.blockforge.generated.generatedmod.network.packet.cultivation.CompassSearchPacket;


public final class CompassService {
        public static void open(ServerPlayer player) {
            if (permit(player, OPEN_AT)) sendToPlayer(player,
                    new cn.blockforge.generated.generatedmod.network.packet.cultivation.OpenScreenPacket(3, player.blockPosition(), 0));
        }
        public static void clear(UUID owner) {
            COMPASS_TARGETS.remove(owner);
            SEARCH_AT.remove(owner); TELEPORT_AT.remove(owner); OPEN_AT.remove(owner);
        }
        public static void clear() {
            COMPASS_TARGETS.clear(); SEARCH_AT.clear(); TELEPORT_AT.clear(); OPEN_AT.clear();
        }
        private static final Map<UUID,Long> SEARCH_AT=new HashMap<>(),TELEPORT_AT=new HashMap<>(),OPEN_AT=new HashMap<>();
        private static boolean held(ServerPlayer player){
            return player.isAlive()&&!player.isSpectator()&&(player.getMainHandItem().is(WuXingItems.CREATURE_COMPASS.get())
                    ||player.getOffhandItem().is(WuXingItems.CREATURE_COMPASS.get()));
        }
        private static boolean permit(ServerPlayer player,Map<UUID,Long> times){
            if(!held(player))return false;
            long now=player.server.overworld().getGameTime(),last=times.getOrDefault(player.getUUID(),Long.MIN_VALUE);
            int cooldown=times==SEARCH_AT?cn.blockforge.generated.generatedmod.balance.GameplayConfig.COMPASS_SEARCH_TICKS.get():20;
            if(last!=Long.MIN_VALUE&&now>=last&&now-last<cooldown){
                player.displayClientMessage(Component.translatable("message.wuxingxiantu.compass_cooldown"),true);return false;
            }
            times.put(player.getUUID(),now);return true;
        }

        public static void handleCompassTeleportPacket(CompassTeleportPacket packet, Supplier<NetworkEvent.Context> supplier) {
            NetworkEvent.Context context = supplier.get();
            context.enqueueWork(() -> {
                ServerPlayer player = context.getSender();
                if (player != null&&permit(player,TELEPORT_AT)) teleportToCompassTarget(player);
            });
            context.setPacketHandled(true);
        }

        public static void handleCompassSearchPacket(CompassSearchPacket packet, Supplier<NetworkEvent.Context> supplier) {
            NetworkEvent.Context context = supplier.get();
            context.enqueueWork(() -> {
                ServerPlayer player = context.getSender();
                if (player == null || packet.name() == null || packet.name().isBlank()||packet.name().length()>64||!permit(player,SEARCH_AT)) return;
                findNearestCreature(player, packet.name().trim());
            });
            context.setPacketHandled(true);
        }

    private static void findNearestCreature(ServerPlayer player, String name) {
        if (!(player.level() instanceof ServerLevel level)) return;
        net.minecraft.world.entity.LivingEntity best = null;
        double bestDist = Double.MAX_VALUE;
        String bestName = name;
        String query=name.toLowerCase(java.util.Locale.ROOT);
        net.minecraft.world.phys.AABB box = player.getBoundingBox().inflate(128.0D);
        for (net.minecraft.world.entity.LivingEntity living : level.getEntitiesOfClass(
                net.minecraft.world.entity.LivingEntity.class, box, entity -> entity != player && entity.isAlive())) {
            String label = living.getName().getString();
            String typeName = living.getType().getDescription().getString();
            if (!net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(living.getType()).toString().contains(query)
                    && !label.toLowerCase(java.util.Locale.ROOT).contains(query)
                    && !typeName.toLowerCase(java.util.Locale.ROOT).contains(query)) {
                continue;
            }
            double dist = living.distanceToSqr(player);
            if (dist < bestDist) { bestDist = dist; best = living; bestName = label; }
        }
        if (best == null) {
            COMPASS_TARGETS.remove(player.getUUID());
            player.displayClientMessage(Component.translatable(
                    "message.wuxingxiantu.compass_not_found", name), true);
            // 同时把“未找到”回发界面，让面板文字也给出反馈。
            sendToPlayer(player, new CompassResultPacket("", -1, 0, 0, 0));
            return;
        }
        int dx = best.blockPosition().getX() - player.blockPosition().getX();
        int dy = best.blockPosition().getY() - player.blockPosition().getY();
        int dz = best.blockPosition().getZ() - player.blockPosition().getZ();
        COMPASS_TARGETS.put(player.getUUID(), new CompassTarget(level.dimension(), best.getUUID(),
                bestName, best.blockPosition().immutable()));
        sendToPlayer(player, new CompassResultPacket(bestName,
                Math.sqrt(bestDist), dx, dy, dz));
    }

    private static void teleportToCompassTarget(ServerPlayer player) {
        long now=player.server.overworld().getGameTime();
        var playerTag=player.getPersistentData();
        if(playerTag.getLong("WuxingCompassTeleportUntil")>now){
            player.displayClientMessage(Component.translatable("message.wuxingxiantu.compass_cooldown"),true);return;
        }
        var data=GeneratedMod.data(player);
        boolean combat=player.level().dimension()==cn.blockforge.generated.generatedmod.mindscape.MindscapeManager.DIMENSION
                ||TribulationManager.isActive(player)||!player.serverLevel().getEntitiesOfClass(net.minecraft.world.entity.Mob.class,
                    player.getBoundingBox().inflate(64),mob->mob.isAlive()&&mob.getTarget()==player
                    &&(mob instanceof cn.blockforge.generated.generatedmod.boss.VisualBossEntity||mob instanceof HeartDemonEntity)).isEmpty();
        if(combat){player.displayClientMessage(Component.translatable("message.wuxingxiantu.compass_combat"),true);return;}
        if(data==null)return;
        float cost=data.wuxingxiantu$getMaxMana()*cn.blockforge.generated.generatedmod.balance.GameplayConfig.COMPASS_COST.get().floatValue();
        if(data.wuxingxiantu$getCurrentMana()<cost){player.displayClientMessage(Component.translatable("message.wuxingxiantu.low_mana"),true);return;}
        CompassTarget target = COMPASS_TARGETS.get(player.getUUID());
        if (target == null) {
            player.displayClientMessage(Component.translatable(
                    "message.wuxingxiantu.compass_no_target"), true);
            return;
        }
        if (!(player.level() instanceof ServerLevel level)) return;
        net.minecraft.world.entity.Entity entity=level.dimension().equals(target.dimension())?level.getEntity(target.entityId()):null;
        if(entity==null||!entity.isAlive()||entity.distanceToSqr(player)>128*128){
            COMPASS_TARGETS.remove(player.getUUID());
            sendToPlayer(player,new CompassResultPacket("",-1,0,0,0));
            player.displayClientMessage(Component.translatable("message.wuxingxiantu.compass_no_target"),true);
            return;
        }
        BlockPos center=entity.blockPosition();
        BlockPos landing = findLandingSpot(level, center);
        if (landing == null) {
            player.displayClientMessage(Component.translatable(
                    "message.wuxingxiantu.compass_no_target"), true);
            return;
        }
        player.teleportTo(level, landing.getX() + 0.5D, landing.getY(), landing.getZ() + 0.5D,
                player.getYRot(), player.getXRot());
        data.wuxingxiantu$setCurrentMana(data.wuxingxiantu$getCurrentMana()-cost);
        playerTag.putLong("WuxingCompassTeleportUntil",now+cn.blockforge.generated.generatedmod.balance.GameplayConfig.COMPASS_TELEPORT_TICKS.get());
        GeneratedMod.sync(player);
        player.displayClientMessage(Component.translatable(
                "message.wuxingxiantu.compass_teleported", target.name()), true);
    }

    private static BlockPos findLandingSpot(ServerLevel level, BlockPos center) {
        for (int radius = 0; radius <= 3; radius++) {
            for (int ix = -radius; ix <= radius; ix++) {
                for (int iz = -radius; iz <= radius; iz++) {
                    if (Math.max(Math.abs(ix), Math.abs(iz)) != radius) continue;
                    for (int iy = 2; iy >= -3; iy--) {
                        BlockPos pos = center.offset(ix, iy, iz);
                        if (isSafeStand(level, pos)) return pos;
                    }
                }
            }
        }
        return null;
    }

    private static boolean isSafeStand(ServerLevel level, BlockPos pos) {
        if(!level.hasChunkAt(pos)||!level.getWorldBorder().isWithinBounds(pos)||level.isOutsideBuildHeight(pos)
                ||level.isOutsideBuildHeight(pos.above())||!level.getFluidState(pos).isEmpty()||!level.getFluidState(pos.above()).isEmpty())return false;
        if (level.getBlockState(pos.below()).getCollisionShape(level, pos.below()).isEmpty()) return false;
        if (!level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()) return false;
        return level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).isEmpty();
    }

    private record CompassTarget(net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension,
                                 UUID entityId, String name, BlockPos pos) {}

    private static final Map<UUID, CompassTarget> COMPASS_TARGETS = new HashMap<>();

    private CompassService() {}
}
