package cn.blockforge.generated.generatedmod.divineart;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import cn.blockforge.generated.generatedmod.*;
import static cn.blockforge.generated.generatedmod.DivineArtLogic.*;


public final class PossessionController {

    private static final class PossessionState {
        private final UUID targetId;
        private final Vec3 origin;
        private final ResourceKey<Level> dimension;
        private final boolean targetNoAi;
        private final boolean targetNoGravity;
        private final boolean targetNoPhysics;
        private final Pose targetPose;
        private net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase<?> dragonPhase;
        private float yaw;
        private float pitch;
        private boolean forward;
        private boolean backward;
        private boolean left;
        private boolean right;
        private boolean jump;
        private boolean sneak;
        private boolean attack;
        private Vec3 abilityMotion = Vec3.ZERO;
        private int abilityMotionTicks;
        private Vec3 dragonPosition;
        private long lastInputTick = Long.MIN_VALUE;
        private int attackCooldown;

        private PossessionState(UUID targetId, Vec3 origin, ResourceKey<Level> dimension,
                                boolean targetNoAi, boolean targetNoGravity, boolean targetNoPhysics,
                                Pose targetPose, float yaw, float pitch) {
            this.targetId = targetId;
            this.origin = origin;
            this.dimension = dimension;
            this.targetNoAi = targetNoAi;
            this.targetNoGravity = targetNoGravity;
            this.targetNoPhysics = targetNoPhysics;
            this.targetPose = targetPose;
            this.yaw = yaw;
            this.pitch = pitch;
        }
    }

    private static final org.slf4j.Logger LOGGER = com.mojang.logging.LogUtils.getLogger();

    private static final Map<Class<?>, Field> DRAGON_HOVER_TARGET_FIELDS = new HashMap<>();

    private static final Map<UUID, PossessionState> POSSESSIONS = new HashMap<>();
    private static final Map<UUID,UUID> OWNERS_BY_TARGET = new HashMap<>();

    public static LivingEntity possessionTarget(Entity entity) {
        if (entity instanceof EnderDragonPart part) return part.parentMob;
        return entity instanceof LivingEntity living ? living : null;
    }

    public static LivingEntity possessedTarget(ServerPlayer player) {
        PossessionState state = POSSESSIONS.get(player.getUUID());
        if (state == null) return null;
        ServerLevel hostLevel = player.server.getLevel(state.dimension);
        Entity entity = hostLevel == null ? null : hostLevel.getEntity(state.targetId);
        return possessionTarget(entity);
    }

    public static String activeAbilityForm(ServerPlayer player) {
        CultivationData data = GeneratedMod.data(player);
        if (data == null) return "";
        LivingEntity target = possessedTarget(player);
        if (target != null) {
            ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(target.getType());
            return key == null ? "" : key.toString();
        }
        return data.wuxingxiantu$getActivePossessedForm();
    }

    public static boolean tryPossess(ServerPlayer player, LivingEntity target) {
        target = possessionTarget(target);
        CultivationData data = GeneratedMod.data(player);
        if (target == null || data == null || !"yi_hun_ji_po".equals(data.wuxingxiantu$getActiveDivineArt())
                || !(target.level() instanceof ServerLevel) || target == player || !target.isAlive()
                || target instanceof Player || target.isInvulnerable()
                || target.getType().getCategory() == net.minecraft.world.entity.MobCategory.MISC
                || player.distanceToSqr(target) > 12.0D * 12.0D
                || !player.hasLineOfSight(target)) {
            return false;
        }
        if (POSSESSIONS.containsKey(player.getUUID())) return false;
        if (OWNERS_BY_TARGET.containsKey(target.getUUID())) return false;
        var originalDragonPhase=target instanceof EnderDragon dragon?dragon.getPhaseManager().getCurrentPhase().getPhase():null;
        boolean noAi = target instanceof Mob mob && mob.isNoAi();
        boolean noGravity = target.isNoGravity();
        boolean noPhysics = target.noPhysics;
        // 普通生物由寄魄逻辑接管 AI；末影龙的飞行、轨迹和多部件更新则必须保留
        // 原版 aiStep，否则 EnderDragonPart 与 EnderDragonRenderer 会失去同步并抖动。
        if (target instanceof Mob mob && !(target instanceof EnderDragon)) mob.setNoAi(true);
        boolean flyingTarget = isControlledFlyingTarget(target);
        target.setNoGravity(noGravity || flyingTarget);
        // 附身目标必须走实体碰撞路径，不能继承外部残留的 noPhysics 穿墙状态。
        target.noPhysics = false;
        if (target instanceof EnderDragon dragon) {
            // HOVERING 没有自动飞行目标，能保留原版翅膀/部件动画，同时避免战斗阶段
            // 与玩家输入互相抢写位置。只在附身建立时设置一次，绝不每 tick 重置阶段。
            dragon.getPhaseManager().setPhase(net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase.HOVERING);
            dragon.setDeltaMovement(Vec3.ZERO);
        }
        PossessionState state = new PossessionState(target.getUUID(), SoulAnchor.position(player), player.level().dimension(),
                noAi, noGravity, noPhysics, target.getPose(), target.getYRot(), target.getXRot());
        POSSESSIONS.put(player.getUUID(), state);
        state.dragonPhase=originalDragonPhase;
        OWNERS_BY_TARGET.put(target.getUUID(),player.getUUID());
        syncSoulAppearance(player, true);
        // 用带旋转的传送把旁观本体的视角（服务端与客户端两侧）对齐到生物当前朝向，
        // 这样附身后的第一个输入包携带的就是生物朝向，视角不会突跳。
        player.connection.teleport(player.getX(), player.getY(), player.getZ(), target.getYRot(), target.getXRot());
        ResourceLocation formId = BuiltInRegistries.ENTITY_TYPE.getKey(target.getType());
        if (formId != null) {
            data.wuxingxiantu$addPossessedForm(formId.toString());
            data.wuxingxiantu$setActivePossessedForm(formId.toString());
        }
        // 附身目标既是控制对象，也是七十二变的形态来源；无论该形态是否已存在，
        // 都同步一次完整状态，避免客户端仍停留在旧的形态列表。
        GeneratedMod.sync(player);
        player.setCamera(target);
        player.displayClientMessage(Component.translatable("message.wuxingxiantu.possession_started",
                target.getDisplayName()), true);
        player.serverLevel().sendParticles(ParticleTypes.SOUL, target.getX(), target.getY() + target.getBbHeight() * 0.55D,
                target.getZ(), 18, 0.35D, 0.45D, 0.35D, 0.02D);
        return true;
    }

    public static boolean isPossessing(ServerPlayer player) {
        return POSSESSIONS.containsKey(player.getUUID());
    }

    public static ServerPlayer possessingPlayer(Entity entity) {
        if (entity == null || entity.getServer() == null) return null;
        var owner=OWNERS_BY_TARGET.get(entity.getUUID());
        var player=owner==null?null:entity.getServer().getPlayerList().getPlayer(owner);
        return player!=null&&isPossessing(player)?player:null;
    }

    public static boolean setPossessionAbilityMotion(ServerPlayer player, Vec3 motion, int ticks) {
        PossessionState state = POSSESSIONS.get(player.getUUID());
        if (state == null || motion == null || !Double.isFinite(motion.x) || !Double.isFinite(motion.y)
                || !Double.isFinite(motion.z) || ticks <= 0) return false;
        state.abilityMotion = motion;
        state.abilityMotionTicks = Math.min(12, ticks);
        return true;
    }

    public static void onPossessedEntityDeath(LivingEntity entity) {
        for (Map.Entry<UUID, PossessionState> entry : new HashMap<>(POSSESSIONS).entrySet()) {
            if (!entry.getValue().targetId.equals(entity.getUUID())) continue;
            ServerPlayer player = entity.getServer() == null ? null
                    : entity.getServer().getPlayerList().getPlayer(entry.getKey());
            if (player == null) {
                POSSESSIONS.remove(entry.getKey());
                continue;
            }
            CultivationData data = GeneratedMod.data(player);
            if (data != null) stop(player, data, false);
        }
    }

    public static void setPossessionInput(ServerPlayer player, float yaw, float pitch,
                                          boolean forward, boolean backward, boolean left, boolean right,
                                          boolean jump, boolean sneak, boolean attack) {
        PossessionState state = POSSESSIONS.get(player.getUUID());
        if (state == null) return;
        if (!Float.isFinite(yaw) || !Float.isFinite(pitch)) return;
        state.yaw = Mth.wrapDegrees(yaw);
        state.pitch = Math.max(-90.0F, Math.min(90.0F, pitch));
        state.forward = forward;
        state.backward = backward;
        state.left = left;
        state.right = right;
        // 跳跃由宿主自身的碰撞和重力规则处理；飞行/游泳宿主则按跳跃键调整高度。
        state.jump = jump;
        state.sneak = sneak;
        state.attack = attack;
        state.lastInputTick = player.serverLevel().getGameTime();
    }

    public static void tickPossession(ServerPlayer player, CultivationData data) {
        PossessionState state = POSSESSIONS.get(player.getUUID());
        if (state == null) return;
        if (player.level().dimension() != state.dimension || data.wuxingxiantu$getDivineSoulOrigin() == null) {
            stop(player, data, true);
            return;
        }
        ServerLevel targetLevel = player.server.getLevel(state.dimension);
        Entity entity = targetLevel == null ? null : targetLevel.getEntity(state.targetId);
        if (!(entity instanceof LivingEntity target) || !target.isAlive()) {
            player.displayClientMessage(Component.translatable("message.wuxingxiantu.possession_target_lost"), true);
            stop(player, data, false);
            return;
        }
        if (state.origin.distanceToSqr(target.position()) > 128.0D * 128.0D) {
            player.displayClientMessage(Component.translatable("message.wuxingxiantu.soul_too_far"), true);
            stop(player, data, true);
            return;
        }
        // 旋转取自自定义包里的客户端本地玩家视角（setPossessionInput 已写入 state）。
        // 旧版从相机实体读旋转回传会形成"服务端写生物、客户端再读生物"的死锁回环，现已在客户端修正来源。
        // The camera follows the host; movement is controlled only by explicit input packets.
        // ServerPlayer.tick follows the camera. Teleporting back each tick fights that
        // follow and sends correction packets that overwrite the client's mouse rotation.
        player.setDeltaMovement(Vec3.ZERO);
        player.noPhysics = true;
        player.setNoGravity(true);
        target.noPhysics = false;
        boolean dragonTarget = target instanceof EnderDragon;
        if (dragonTarget) {
            // EnderDragon.aiStep 会自行更新 positions、EnderDragonPart 和拍翼动画。
            // 这里不能再调用 moveRelative/move，否则原版飞行和自定义位移会在同一 tick
            // 互相覆盖，表现为抽动、方向反转和龙只平移不摆动。
            EnderDragon dragon = (EnderDragon) target;
            float dragonYaw = Mth.wrapDegrees(state.yaw + 180.0F);
            dragon.setYRot(dragonYaw);
            dragon.setXRot(state.pitch);
            dragon.setYHeadRot(dragonYaw);
            dragon.setYBodyRot(dragonYaw);
            dragon.yRotO = dragonYaw;
            dragon.xRotO = state.pitch;
            dragon.yHeadRotO = dragonYaw;
            dragon.yBodyRotO = dragonYaw;
            resetDragonHoverTarget(dragon);
            dragon.setDeltaMovement(Vec3.ZERO);
        }
        // 鱼类与其他水生生物必须同步游泳姿态，否则上岸/入水后会一直保持站立动画。
        boolean aquatic = isAquaticForm(target);
        boolean swimming = aquatic && target.isInWaterOrBubble();
        target.setSwimming(swimming);
        if (swimming) {
            target.setPose(Pose.SWIMMING);
        } else if (target.getPose() == Pose.SWIMMING) {
            target.setPose(Pose.STANDING);
        }
        if (!dragonTarget) {
            target.setYRot(state.yaw);
            target.setXRot(state.pitch);
            target.setYHeadRot(state.yaw);
            target.setYBodyRot(state.yaw);
            target.yRotO = state.yaw;
            target.xRotO = state.pitch;
            target.yHeadRotO = state.yaw;
            target.yBodyRotO = state.yaw;
        }
        if (target instanceof Mob mob && !dragonTarget) mob.setNoAi(true);

        // 网络输入短暂中断时必须刹车，避免断线后目标继续向前移动或重复攻击。
        boolean inputFresh = state.lastInputTick >= player.serverLevel().getGameTime() - 10L;
        boolean forwardPressed = inputFresh && state.forward;
        boolean backwardPressed = inputFresh && state.backward;
        boolean leftPressed = inputFresh && state.left;
        boolean rightPressed = inputFresh && state.right;
        // 跳跃输入已由客户端转发；普通宿主走地面跳跃，飞行/游泳宿主调整垂直移动。
        boolean jumpPressed = inputFresh && state.jump;
        boolean sneakPressed = inputFresh && state.sneak;
        boolean attackPressed = inputFresh && state.attack;

        // 服务端的目标生物不是本地控制实体，LivingEntity.travel 会直接跳过移动分支；
        // 因此在实体 tick 之后显式执行相对移动、重力和碰撞位移。
        // 只使用客户端明确发送的按键状态，禁止把本体位置误差当成移动输入。
        float forward = Mth.clamp((forwardPressed ? 1.0F : 0.0F) - (backwardPressed ? 1.0F : 0.0F), -1.0F, 1.0F);
        float strafe = Mth.clamp((leftPressed ? 1.0F : 0.0F) - (rightPressed ? 1.0F : 0.0F), -1.0F, 1.0F);
        // EnderDragon 的原版实体坐标朝向与玩家视角相差 180 度，不能直接套用
        // 普通 Mob.moveRelative；七十二变和附身都以玩家视线计算世界方向，避免前后左右反转。
        if (dragonTarget) {
            Vec3 dragonMotion = dragonInputMotion(state, forward, strafe, jumpPressed, sneakPressed);
            if (state.abilityMotionTicks > 0) {
                dragonMotion = state.abilityMotion;
                state.abilityMotionTicks--;
                state.abilityMotion = state.abilityMotion.scale(0.82D);
            }
            // EnderDragon 自己会在下一次 aiStep 中完成一次碰撞移动和部件更新；这里只写入
            // 下一 tick 的受控速度，不能在 aiStep 之后再次 move，避免主体和翅膀轨迹分离。
            target.setDeltaMovement(dragonMotion);
            target.setSprinting(forward > 0.1F && !sneakPressed);
            if (attackPressed && state.attackCooldown <= 0) {
                possessAttack(player, target);
                state.attackCooldown = 10;
            }
            if (state.attackCooldown > 0) state.attackCooldown--;
            return;
        }
        double inputLength = Math.sqrt(strafe * strafe + forward * forward);
        if (inputLength > 1.0D) {
            strafe /= (float) inputLength;
            forward /= (float) inputLength;
        }
        boolean movingForward = forward > 0.1F;
        boolean flyingTarget = isControlledFlyingTarget(target);
        if (state.abilityMotionTicks > 0) {
            Vec3 dash = state.abilityMotion;
            state.abilityMotionTicks--;
            state.abilityMotion = dash.scale(0.82D);
            target.setDeltaMovement(dash);
            target.hasImpulse = true;
            target.move(MoverType.SELF, dash);
            target.setSprinting(true);
            if (attackPressed && state.attackCooldown <= 0) {
                possessAttack(player, target);
                state.attackCooldown = 10;
            }
            if (state.attackCooldown > 0) state.attackCooldown--;
            if (player.tickCount % 5 == 0) player.setCamera(target);
            return;
        }
        if (!flyingTarget && !target.isInWaterOrBubble() && !target.isInLava()) {
            PossessionGroundMovement.move(target,strafe,forward,jumpPressed,sneakPressed);
            if (attackPressed && state.attackCooldown <= 0) {
                possessAttack(player,target);
                state.attackCooldown=10;
            }
            if (state.attackCooldown>0) state.attackCooldown--;
            if (player.tickCount%5==0) player.setCamera(target);
            return;
        }
        if (target instanceof Mob mob) {
            mob.setZza(forward);
            mob.setXxa(strafe);
            mob.setYya(flyingTarget || swimming ? (jumpPressed ? 1.0F : sneakPressed ? -1.0F : 0.0F) : (jumpPressed ? 1.0F : 0.0F));
        }
        var speedAttribute = flyingTarget ? Attributes.FLYING_SPEED : Attributes.MOVEMENT_SPEED;
        var speedInstance = target.getAttribute(speedAttribute);
        double attributeSpeed = speedInstance == null ? 0.0D : speedInstance.getValue();
        // 附身移动严格使用宿主自己的移动速度，不再套用元神状态速度或 0.1F 最低速度。
        float movementSpeed = attributeSpeed > 0.0D ? (float) attributeSpeed : target.getSpeed();
        movementSpeed *= sneakPressed ? 0.3F : 1.0F;
        // 竖直分量：飞行宿主用跳跃/潜行升降；水中游泳的鱼类生物则沿视线三维游动——
        // 前进键按俯仰角换算上下分量（抬头向上游、低头向下游），跳跃/潜行额外上浮/下潜。
        double verticalInput;
        if (flyingTarget) {
            verticalInput = jumpPressed ? 1.0D : sneakPressed ? -1.0D : 0.0D;
        } else if (swimming) {
            double keyVertical = jumpPressed ? 1.0D : sneakPressed ? -1.0D : 0.0D;
            double lookVertical = -Math.sin((double) state.pitch * Math.PI / 180.0D) * forward;
            verticalInput = keyVertical == 0.0D ? lookVertical : keyVertical;
        } else {
            verticalInput = 0.0D;
        }
        Vec3 input = new Vec3(strafe, verticalInput, forward);
        if (forward == 0.0F && strafe == 0.0F && !jumpPressed && !sneakPressed) {
            Vec3 stopped = target.getDeltaMovement();
            // 水中无输入时连同竖直动量一起刹停，鱼会悬停而不是继续上下漂。
            target.setDeltaMovement(0.0D, swimming ? 0.0D : stopped.y, 0.0D);
        }
        target.moveRelative(movementSpeed, input);
        Vec3 velocity = target.getDeltaMovement();
        if (jumpPressed) {
            if (flyingTarget) {
                velocity = velocity.add(0.0D, 0.08D, 0.0D);
            } else if (!swimming && target.onGround()) {
                // 跳跃高度使用宿主自己的 JUMP_STRENGTH（原版 jumpFromGround 的初速度），
                // 不再固定套用玩家默认 0.42，否则附身兔子/马等跳跃生物仍只有玩家跳高。
                var jumpStrengthInstance = target.getAttribute(Attributes.JUMP_STRENGTH);
                double jumpStrength = jumpStrengthInstance == null ? 0.42D : jumpStrengthInstance.getValue();
                velocity = new Vec3(velocity.x, Math.max(velocity.y, jumpStrength), velocity.z);
                target.setOnGround(false);
                target.hasImpulse = true;
            }
        }
        // 水中游泳走浮力模型：不叠加陆地重力，三轴统一水阻衰减，悬停与升降都干净利落。
        if (!flyingTarget && !swimming && !target.isNoGravity() && !target.onGround()) velocity = velocity.add(0.0D, -0.08D, 0.0D);
        if (flyingTarget && !jumpPressed && !sneakPressed) velocity = new Vec3(velocity.x, velocity.y * 0.6D, velocity.z);
        double horizontalDrag = swimming ? 0.9D : 0.91D;
        // 陆地宿主不做纵向阻尼：跳跃初速度 0.42 只受重力递减，高度接近原版；
        // 飞行/游泳宿主仍保留各自的纵向拖拽，维持悬停手感。
        double verticalDrag = swimming ? 0.9D : (flyingTarget ? 0.85D : 1.0D);
        target.setDeltaMovement(velocity.x * horizontalDrag, velocity.y * verticalDrag, velocity.z * horizontalDrag);
        target.move(MoverType.SELF, target.getDeltaMovement());
        target.setSprinting(movingForward && !sneakPressed);
        if (attackPressed && state.attackCooldown <= 0) {
            possessAttack(player, target);
            state.attackCooldown = 10;
        }
        if (state.attackCooldown > 0) state.attackCooldown--;
        if (player.tickCount % 5 == 0) player.setCamera(target);
    }

    private static void resetDragonHoverTarget(EnderDragon dragon) {
        try {
            Field field = DRAGON_HOVER_TARGET_FIELDS.computeIfAbsent(
                    net.minecraft.world.entity.boss.enderdragon.phases.DragonHoverPhase.class, type -> {
                        try {
                            Field value = type.getDeclaredField("targetLocation");
                            value.setAccessible(true);
                            return value;
                        } catch (ReflectiveOperationException | RuntimeException ignored) {
                            return null;
                        }
                    });
            if (field != null) field.set(dragon.getPhaseManager().getPhase(
                    net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase.HOVERING), dragon.position());
        } catch (IllegalAccessException | RuntimeException ignored) {
            // 映射或安全策略不允许反射时，仍保持稳定的零速度。
        }
    }

    private static Vec3 dragonInputMotion(PossessionState state, float forward, float strafe,
                                          boolean jumpPressed, boolean sneakPressed) {
        float yaw = (float) Math.toRadians(state.yaw);
        // Minecraft 的玩家视线前方为 (-sin(yaw), cos(yaw))；末影龙模型本身朝向反向，
        // 但移动方向必须仍然与准星一致，因此这里直接生成世界坐标速度而不是 moveRelative。
        double sin = Math.sin(yaw);
        double cos = Math.cos(yaw);
        double x = -sin * forward + cos * strafe;
        double z = cos * forward + sin * strafe;
        double y = jumpPressed ? 0.42D : sneakPressed ? -0.42D : 0.0D;
        double length = Math.sqrt(x * x + z * z);
        if (length > 1.0D) {
            x /= length;
            z /= length;
        }
        double speed = 0.65D;
        return new Vec3(x * speed, y, z * speed);
    }

    private static boolean isControlledFlyingTarget(LivingEntity target) {
        return target instanceof FlyingMob || target instanceof FlyingAnimal
                || target instanceof EnderDragon || target instanceof WitherBoss
                || target.getType() == EntityType.BLAZE;
    }

    private static boolean isAquaticForm(LivingEntity target) {
        return target.getType() == EntityType.AXOLOTL || target.getType() == EntityType.COD
                || target.getType() == EntityType.DOLPHIN || target.getType() == EntityType.GLOW_SQUID
                || target.getType() == EntityType.PUFFERFISH || target.getType() == EntityType.SALMON
                || target.getType() == EntityType.SQUID || target.getType() == EntityType.TADPOLE
                || target.getType() == EntityType.TROPICAL_FISH || target.getType() == EntityType.TURTLE
                || target.getType() == EntityType.GUARDIAN || target.getType() == EntityType.ELDER_GUARDIAN;
    }

    private static void possessAttack(ServerPlayer player, LivingEntity attacker) {
        // 先用生物可达的最大近战距离找目标，再用目标实体的实际碰撞箱校验攻击距离。
        double maxRange = 6.0D;
        Vec3 start = attacker.getEyePosition();
        Vec3 end = start.add(attacker.getViewVector(1.0F).scale(maxRange));
        net.minecraft.world.phys.EntityHitResult hit = ProjectileUtil.getEntityHitResult(attacker, start, end,
                attacker.getBoundingBox().expandTowards(end.subtract(start)).inflate(1.0D),
                entity -> entity instanceof LivingEntity living && living != attacker && living != player
                        && living.isAlive(), maxRange);
        if (hit == null || !(hit.getEntity() instanceof LivingEntity target)) return;
        if (attacker instanceof Mob mob && mob.distanceToSqr(target) > mob.getMeleeAttackRangeSqr(target)) return;
        if (!(attacker instanceof Mob) && attacker.distanceToSqr(target) > 9.0D) return;
        if (attacker.doHurtTarget(target)) {
            TransformAbilityRegistry.recordTransformedAttack(player, target);
        }
        attacker.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
    }

    public static void clearPossession(ServerPlayer player) {
        PossessionState state = POSSESSIONS.remove(player.getUUID());
        syncSoulAppearance(player, false);
        player.setCamera(player);
        if (state == null) return;
        OWNERS_BY_TARGET.remove(state.targetId,player.getUUID());
        ServerLevel hostLevel = player.server.getLevel(state.dimension);
        Entity entity = hostLevel == null ? null : hostLevel.getEntity(state.targetId);
        if (entity instanceof LivingEntity target) {
            if(target instanceof EnderDragon dragon&&target.isAlive()&&state.dragonPhase!=null)
                dragon.getPhaseManager().setPhase(state.dragonPhase);
            if (target instanceof Mob mob) mob.setNoAi(state.targetNoAi);
            target.setNoGravity(state.targetNoGravity);
            target.noPhysics = state.targetNoPhysics;
            target.setPose(state.targetPose);
            target.setSprinting(false);
            Vec3 velocity = target.getDeltaMovement();
            target.setDeltaMovement(0.0D, velocity.y, 0.0D);
        }
        if (player.level().dimension().equals(state.dimension)) {
            player.teleportTo(state.origin.x,state.origin.y,state.origin.z);
        }
    }
    /** The existing tracked effect carries the visual phase to every observing client. */
    private static void syncSoulAppearance(ServerPlayer player, boolean attached) {
        var effect = ModEffects.YI_HUN_JI_PO.get();
        var current = player.getEffect(effect);
        if (current == null) return;
        int remaining = current.getDuration();
        // Replace instead of stacking: no hidden higher-level effect may reappear later.
        player.removeEffectNoUpdate(effect);
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(effect, remaining,
                attached ? 1 : 0, false, false, true));
    }
    private PossessionController() {}
}
