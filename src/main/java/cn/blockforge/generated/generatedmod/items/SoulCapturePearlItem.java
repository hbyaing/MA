package cn.blockforge.generated.generatedmod.items;

import cn.blockforge.generated.generatedmod.SoulPearlData;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import java.util.List;
import java.util.Optional;

/** 长按右键发射灵魂射线；首次被射线击杀的生物会被记录。 */
public final class SoulCapturePearlItem extends Item {
    private static final int USE_DURATION = 72000;
    private static final int DAMAGE_INTERVAL = 10;
    private static final int VISUAL_INTERVAL = 4;
    private static final int MAX_BEAM_SAMPLES = 10;
    private static final int MAX_BANNER_SECTIONS = 32;
    private static final int BANNER_PARTICLES_PER_SECTION = 4;
    // 人皇幡螺纹在 10 tick 内完成延伸，较原来的 30 tick 快 3 倍。
    private static final int BANNER_EXTENSION_TICKS = 10;
    private static final double RANGE = 24.0D;
    private static final double ENTITY_PADDING = 0.25D;
    private static final double BANNER_RADIUS = 0.72D;
    private static final double BANNER_TURNS = 4.5D;
    // 螺纹旋转速度同步提高 3 倍，保持形状参数不变。
    private static final double BANNER_ROTATION_SPEED = 0.90D;
    private static final float BEAM_DAMAGE = 4.0F;

    public SoulCapturePearlItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        // 统一进度门槛：拘魂珠属魂魄系统，境界不足无法启动捕魂射线。
        if (!cn.blockforge.generated.generatedmod.ProgressionGate.canUseSoulSystem(
                player, cn.blockforge.generated.generatedmod.ProgressionGate.SOUL_CAPTURE)) {
            if (!level.isClientSide) {
                cn.blockforge.generated.generatedmod.ProgressionGate.deny(player,
                        cn.blockforge.generated.generatedmod.ProgressionGate.MSG_SOUL,
                        cn.blockforge.generated.generatedmod.ProgressionGate.requiredRealmForSoulFeature(
                                cn.blockforge.generated.generatedmod.ProgressionGate.SOUL_CAPTURE));
            }
            return InteractionResultHolder.fail(stack);
        }
        if (SoulPearlData.hasEntity(stack)) {
            if (!level.isClientSide) {
                player.displayClientMessage(
                        Component.translatable("message.wuxingxiantu.soul_pearl_locked"), true);
            }
            return InteractionResultHolder.fail(stack);
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        ItemStack stack = context.getItemInHand();
        if (SoulPearlData.hasEntity(stack)) {
            if (!context.getLevel().isClientSide) {
                player.displayClientMessage(
                        Component.translatable("message.wuxingxiantu.soul_pearl_locked"), true);
            }
            return InteractionResult.FAIL;
        }
        player.startUsingItem(context.getHand());
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player,
                                                   LivingEntity target, InteractionHand hand) {
        if (target == player) return InteractionResult.PASS;
        if (SoulPearlData.hasEntity(stack)) {
            if (!player.level().isClientSide) {
                player.displayClientMessage(
                        Component.translatable("message.wuxingxiantu.soul_pearl_locked"), true);
            }
            return InteractionResult.sidedSuccess(player.level().isClientSide);
        }
        player.startUsingItem(hand);
        return InteractionResult.sidedSuccess(player.level().isClientSide);
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack,
                          int remainingUseTicks) {
        if (!(livingEntity instanceof Player player) || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (SoulPearlData.hasEntity(stack)) {
            player.stopUsingItem();
            return;
        }
        boolean visualTick = remainingUseTicks % VISUAL_INTERVAL == 0;
        boolean damageTick = remainingUseTicks % DAMAGE_INTERVAL == 0;
        if (!visualTick && !damageTick) return;

        if (visualTick) emitBeam(serverLevel, player);
        if (!damageTick) return;
        LivingEntity target = damageBeam(serverLevel, player, BEAM_DAMAGE);
        if (target != null && target.isDeadOrDying() && !SoulPearlData.hasEntity(stack)) {
            // 摄魄后保留摄魄珠本身，只有放入愈灵室修复时才会转为魂珠。
            SoulPearlData.recordEntity(stack, target);
            player.getInventory().setChanged();
            player.displayClientMessage(Component.translatable(
                    "message.wuxingxiantu.soul_beast_recorded",
                    target.getType().getDescription()), true);
            player.stopUsingItem();
        }
    }

    /** 绘制当前视线的摄魄光线；摄魄珠保留直线形态，但使用灵魂粒子。 */
    public static void emitBeam(ServerLevel level, Player player) {
        emitBeamParticles(level, traceBeam(level, player));
    }

    /** 绘制人皇幡的连续灵魂螺纹：从起点伸长到终点，并绕视线轴持续拧动。 */
    public static void emitBannerBeam(ServerLevel level, Player player, int elapsedTicks) {
        Beam beam = traceBeam(level, player);
        double distance = beam.start().distanceTo(beam.end());
        if (distance < 0.05D) return;
        double extension = Math.min(1.0D, Math.max(0.0D, elapsedTicks / (double) BANNER_EXTENSION_TICKS));
        if (extension <= 0.0D) return;
        Vec3 direction = beam.end().subtract(beam.start()).normalize();
        Vec3 reference = Math.abs(direction.y) < 0.92D ? new Vec3(0.0D, 1.0D, 0.0D)
                : new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 side = direction.cross(reference).normalize();
        Vec3 up = side.cross(direction).normalize();
        int sections = Math.min(MAX_BANNER_SECTIONS,
                Math.max(8, (int) Math.ceil(distance * extension / 0.5D)));
        double rotation = elapsedTicks * BANNER_ROTATION_SPEED;
        double visibleDistance = distance * extension;
        for (int section = 0; section <= sections; section++) {
            double along = section / (double) sections;
            double pathDistance = visibleDistance * along;
            double fade = Math.min(1.0D, Math.min(along / 0.16D, (1.0D - along) / 0.16D));
            if (fade <= 0.0D || level.random.nextDouble() > fade) continue;
            Vec3 center = beam.start().add(direction.scale(pathDistance));
            double twist = rotation + along * BANNER_TURNS * Math.PI * 2.0D;
            for (int particle = 0; particle < BANNER_PARTICLES_PER_SECTION; particle++) {
                double angle = twist + particle * Math.PI * 2.0D / BANNER_PARTICLES_PER_SECTION;
                Vec3 offset = side.scale(Math.cos(angle) * BANNER_RADIUS)
                        .add(up.scale(Math.sin(angle) * BANNER_RADIUS));
                Vec3 point = center.add(offset);
                level.sendParticles(ParticleTypes.SOUL, point.x, point.y, point.z,
                        1, 0.0D, 0.0D, 0.0D, 0.0D);
                if ((section + particle + elapsedTicks) % 11 == 0) {
                    level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, point.x, point.y, point.z,
                            1, 0.015D, 0.015D, 0.015D, 0.0D);
                }
            }
        }
    }

    /** 对当前视线上的首个生物造成一次光线伤害，并返回被命中的目标。 */
    public static LivingEntity damageBeam(ServerLevel level, Player player, float damage) {
        LivingEntity target = traceBeam(level, player).target();
        if (target != null && target.isAlive()) {
            target.hurt(level.damageSources().indirectMagic(player, player), damage);
        }
        return target;
    }

    /** 玩家背包中是否有尚未封存生物的摄魄珠。 */
    public static boolean hasUsablePearl(Player player) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack candidate = player.getInventory().getItem(slot);
            if (candidate.getItem() instanceof SoulCapturePearlItem && !SoulPearlData.hasEntity(candidate)) return true;
        }
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, net.minecraft.world.item.TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        EntityType<?> capturedType = SoulPearlData.entityType(stack);
        if (capturedType != null) {
            tooltip.add(Component.translatable("tooltip.wuxingxiantu.soul_spirit", capturedType.getDescription()));
        }
        if (SoulPearlData.isBroken(stack)) {
            tooltip.add(Component.translatable("tooltip.wuxingxiantu.soul_pearl_broken"));
        }
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return USE_DURATION;
    }

    private static Beam traceBeam(Level level, Player player) {
        Vec3 start = player.getEyePosition(1.0F);
        Vec3 direction = player.getViewVector(1.0F).normalize();
        Vec3 maximumEnd = start.add(direction.scale(RANGE));
        BlockHitResult blockHit = level.clip(new ClipContext(
                start, maximumEnd, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        Vec3 end = blockHit.getType() == HitResult.Type.MISS
                ? maximumEnd : blockHit.getLocation();

        LivingEntity closest = null;
        Vec3 closestPoint = end;
        double closestDistance = start.distanceToSqr(end);
        AABB searchBox = new AABB(start, end).inflate(1.0D);
        for (LivingEntity candidate : level.getEntitiesOfClass(LivingEntity.class, searchBox,
                entity -> entity != player && !(entity instanceof net.minecraft.world.entity.player.Player)
                        && !HumanEmperorBannerItem.isServant(entity) && entity.isAlive() && entity.isPickable())) {
            AABB hitBox = candidate.getBoundingBox().inflate(ENTITY_PADDING);
            Optional<Vec3> clipped = hitBox.clip(start, end);
            if (clipped.isEmpty() && hitBox.contains(start)) clipped = Optional.of(start);
            if (clipped.isEmpty()) continue;
            Vec3 hitPoint = clipped.get();
            double distance = start.distanceToSqr(hitPoint);
            if (distance < closestDistance) {
                closest = candidate;
                closestPoint = hitPoint;
                closestDistance = distance;
            }
        }
        return new Beam(start, closestPoint, closest);
    }

    private static void emitBeamParticles(ServerLevel level, Beam beam) {
        double distance = beam.start().distanceTo(beam.end());
        int samples = Math.min(MAX_BEAM_SAMPLES, Math.max(1, (int) Math.ceil(distance / 0.55D)));
        for (int i = 0; i <= samples; i++) {
            double fraction = i / (double) samples;
            Vec3 point = beam.start().lerp(beam.end(), fraction);
            level.sendParticles(ParticleTypes.SOUL, point.x, point.y, point.z,
                    1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
        level.sendParticles(ParticleTypes.SOUL, beam.end().x, beam.end().y, beam.end().z,
                4, 0.08D, 0.08D, 0.08D, 0.01D);
    }

    private record Beam(Vec3 start, Vec3 end, LivingEntity target) {}
}
