package cn.blockforge.generated.generatedmod;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** 神通服务端状态机，负责消耗、冷却、持续效果和踏罡七星判定。 */
public final class DivineArtLogic {
    private static final org.slf4j.Logger LOGGER = com.mojang.logging.LogUtils.getLogger();
    private static final int DIVINE_EFFECT_DURATION = 1200;
    private static final int POSSESSION_EFFECT_DURATION = 20 * 30;


    private static final Map<UUID, AbilityState> SAVED_ABILITIES = new HashMap<>();
    private static final String ABILITY_SNAPSHOT="wuxingxiantu_divine_abilities";

    private static final Map<UUID, Long> PENDING_TRANSFORMS = new HashMap<>();

    private static final Map<UUID, Float> CLOUD_FLIGHT_SPEEDS = new HashMap<>();
    private static final Map<UUID, UUID> SOUL_BODIES = new HashMap<>();
    // 罡斗特效粒子：高亮度近白青色 + 大颗粒尺寸，夜幕下清晰可见。



    // 文档中的坐标以玩家脚部为原点，玩家释放瞬间的朝向为局部 +Z。




    private record AbilityState(GameType gameType, boolean mayFly, boolean flying, float speed,
                                boolean noPhysics, boolean noGravity) {}



    private DivineArtLogic() {}

    /** 发动成功时只同步状态；神通立绘由客户端自行短暂展示，不阻塞游戏输入。 */
    public static boolean release(ServerPlayer player, String id) {
        CultivationData checked = GeneratedMod.data(player);
        // 按键时该神通已是激活态，说明这次是"收束"而不是"发动"，不能再播立绘横幅挡视野。
        boolean wasConvergence = checked != null && id != null && id.equals(checked.wuxingxiantu$getActiveDivineArt());
        boolean success;
        try {
            success = tryRelease(player, id);
        } catch (Throwable throwable) {
            // 服务端状态异常绝不能掀翻（集成）服务器，把玩家弹出游戏；记录后按发动失败处理。
            LOGGER.warn("神通 {} 发动异常，已按失败处理", id, throwable);
            try {
                CultivationData data = GeneratedMod.data(player);
                if (data != null) clear(player);
            } catch (Throwable ignored) {
            }
            return false;
        }
        // 待选择阶段不算真正发动，不播放发动横幅；收束请求只清理旧横幅。
        CultivationData after = GeneratedMod.data(player);
        boolean pendingTransform = "qi_shi_er_bian".equals(id) && after != null
                && after.wuxingxiantu$getActiveDivineArt().isBlank();
        if (success && !pendingTransform && !"tian_di_wei_lu".equals(id)) Network.showDivineArtBanner(player, wasConvergence ? "" : id);
        return success;
    }

    private static boolean tryRelease(ServerPlayer player, String id) {
        CultivationData data = GeneratedMod.data(player);
        DivineArtCatalog.Entry entry = DivineArtCatalog.entry(id);
        if (data == null || entry == null || !data.wuxingxiantu$hasLearnedDivineArt(id)) return false;
        if (data.wuxingxiantu$getRealm() < entry.requiredRealm()) {
            player.displayClientMessage(Component.translatable("message.wuxingxiantu.divine_need_realm"), true);
            return false;
        }
        long now = player.level().getGameTime();
        if (("fa_tian_xiang_di".equals(id) || "yuan_shen_chu_qiao".equals(id) || "yi_hun_ji_po".equals(id)
                || "qi_shi_er_bian".equals(id) || "jin_dou_yun".equals(id)
                || "ri_yue_ting_lun".equals(id))
                && id.equals(data.wuxingxiantu$getActiveDivineArt())) {
            stop(player, data, true);
            return true;
        }
        if (player.isSpectator()) return false;
        if (cn.blockforge.generated.generatedmod.alchemy.HeavenlyFurnace.active(player)) {
            if ("tian_di_wei_lu".equals(id)) cn.blockforge.generated.generatedmod.alchemy.HeavenlyFurnace.cancel(player);
            else {player.displayClientMessage(Component.literal("请先收束天地为炉。"), true);return false;}
            return true;
        }
        if (!data.wuxingxiantu$hasCreationBlessing() && data.wuxingxiantu$getDivineCooldown(id) > now) {
            player.displayClientMessage(Component.translatable("message.wuxingxiantu.divine_cooldown"), true);
            return false;
        }
        if ("tian_di_wei_lu".equals(id)) return cn.blockforge.generated.generatedmod.alchemy.HeavenlyFurnace.open(player);
        if ("fa_tian_xiang_di".equals(id) || "yuan_shen_chu_qiao".equals(id)
                || "yi_hun_ji_po".equals(id)) {

            int cost = activationCost(id);
            if (!player.isCreative() && data.wuxingxiantu$getAura() < cost) {
                player.displayClientMessage(Component.translatable("message.wuxingxiantu.divine_mana_missing", cost), true);
                return false;
            }
            if (!data.wuxingxiantu$getActiveDivineArt().isBlank()) stop(player, data, false);
            if (!player.isCreative()) data.wuxingxiantu$setAura(data.wuxingxiantu$getAura() - cost);
            data.wuxingxiantu$setActiveDivineArt(id);
            data.wuxingxiantu$setDivineCooldown(id, now + entry.cooldownTicks());
            saveAbilities(player);
            if ("fa_tian_xiang_di".equals(id)) {
                // 巨体不支持下蹲尺寸切换；先固定站姿，避免客户端与服务端反复刷新碰撞箱。
                player.setShiftKeyDown(false);
                player.setPose(Pose.STANDING);
                player.addEffect(new MobEffectInstance(ModEffects.FA_TIAN_XIANG_DI.get(), DIVINE_EFFECT_DURATION,
                        0, false, false, true));
                player.refreshDimensions();
                player.heal(player.getMaxHealth());
                GeneratedMod.applyGiantReach(player);
            } else {
                // 元神出窍切入旁观态；移魂寄魄保留原游戏模式，收束后才能准确恢复原始能力。
                saveAbilities(player);
                startSoul(player, data, "yuan_shen_chu_qiao".equals(id));
                player.addEffect(new MobEffectInstance(
                        "yi_hun_ji_po".equals(id) ? ModEffects.YI_HUN_JI_PO.get() : ModEffects.YUAN_SHEN_CHU_QIAO.get(),
                        "yi_hun_ji_po".equals(id) ? POSSESSION_EFFECT_DURATION : DIVINE_EFFECT_DURATION,
                        0, false, false, true));
            }
            player.displayClientMessage(Component.translatable("message.wuxingxiantu.divine_started_" + id)
                    .withStyle(style -> style.withColor(0xFFFF69C8)), true);
            emitAura(player.serverLevel(), player, id);
            GeneratedMod.sync(player);
            return true;
        }
        if ("qi_shi_er_bian".equals(id)) {
            if (!data.wuxingxiantu$getActiveDivineArt().isBlank()) stop(player, data, false);
            String[] forms = data.wuxingxiantu$getPossessedForms();
            if (forms.length == 0) {
                player.displayClientMessage(Component.translatable("message.wuxingxiantu.transform_no_forms"), true);
                return false;
            }
            // 形态界面确认前只登记待选择状态；关闭界面不能提前激活神通或写入冷却。
            PENDING_TRANSFORMS.put(player.getUUID(), now + 200L);
            GeneratedMod.sync(player);
            Network.sendOpenTransformSelection(player);
            return true;
        }
        if ("ri_yue_ting_lun".equals(id)) {
            int cost = activationCost(id);
            if (!player.isCreative() && data.wuxingxiantu$getAura() < cost) {
                player.displayClientMessage(Component.translatable("message.wuxingxiantu.divine_mana_missing", cost), true);
                return false;
            }
            if (!data.wuxingxiantu$getActiveDivineArt().isBlank()) stop(player, data, false);
            if (!player.isCreative()) data.wuxingxiantu$setAura(data.wuxingxiantu$getAura() - cost);
            data.wuxingxiantu$setActiveDivineArt(id);
            data.wuxingxiantu$setDivineCooldown(id, now + entry.cooldownTicks());
            TimeStopManager.activate(player);
            player.displayClientMessage(Component.translatable("message.wuxingxiantu.divine_started_ri_yue_ting_lun")
                    .withStyle(style -> style.withColor(0xFFFF69C8)), true);
            GeneratedMod.sync(player);
            return true;
        }
        if ("jin_dou_yun".equals(id)) {
            int cost = activationCost(id);
            if (!player.isCreative() && data.wuxingxiantu$getAura() < cost) {
                player.displayClientMessage(Component.translatable("message.wuxingxiantu.divine_mana_missing", cost), true);
                return false;
            }
            if (!data.wuxingxiantu$getActiveDivineArt().isBlank()) stop(player, data, false);
            if (!player.isCreative()) data.wuxingxiantu$setAura(data.wuxingxiantu$getAura() - cost);
            saveAbilities(player);
            float boostedSpeed = player.getAbilities().getFlyingSpeed() * 11.0F;
            CLOUD_FLIGHT_SPEEDS.put(player.getUUID(), boostedSpeed);
            player.getAbilities().mayfly = true;
            player.getAbilities().flying = true;
            player.getAbilities().setFlyingSpeed(boostedSpeed);
            player.onUpdateAbilities();
            data.wuxingxiantu$setActiveDivineArt(id);
            data.wuxingxiantu$setDivineCooldown(id, now + entry.cooldownTicks());
            player.displayClientMessage(Component.translatable("message.wuxingxiantu.divine_started_jin_dou_yun")
                    .withStyle(style -> style.withColor(0xFFFF69C8)), true);
            emitCloud(player);
            GeneratedMod.sync(player);
            return true;
        }
        if ("ta_gang_bu_dou".equals(id)) {
            if (data.wuxingxiantu$getDivineStepOrigin() != null) return true;
            int cost = activationCost("ta_gang_bu_dou");
            if (!player.isCreative() && data.wuxingxiantu$getAura() < cost) {
                player.displayClientMessage(Component.translatable("message.wuxingxiantu.divine_mana_missing", cost), true);
                return false;
            }
            if (!player.isCreative()) data.wuxingxiantu$setAura(data.wuxingxiantu$getAura() - cost);
            data.wuxingxiantu$setDivineCooldown(id, now + entry.cooldownTicks());
            data.wuxingxiantu$setDivineStepOrigin(player.blockPosition());
            data.wuxingxiantu$setDivineStepIndex(0);
            data.wuxingxiantu$setDivineStepYaw(player.getYRot());
            startDipperEffect(player);
            // 释放请求成功后立即同步踏罡进度，客户端可立刻看到已启动状态。
            GeneratedMod.sync(player);
            player.playNotifySound(BuiltInRegistries.SOUND_EVENT.get(
                    new ResourceLocation("minecraft", "entity.evoker.prepare_summon")),
                    SoundSource.PLAYERS, 1.0F, 1.0F);
            player.displayClientMessage(Component.translatable("message.wuxingxiantu.dipper_started"), true);
            return true;
        }
        return false;
    }

    public static void tickVisualEffects(ServerPlayer player) {
        tickDipperEffect(player);
    }

    /** 每秒推进神通状态；出任何异常立即自我清场，防止坏状态把（集成）服务器每一 tick 掀翻一次。 */
    public static void tick(ServerPlayer player, CultivationData data) {
        try {
            tickInternal(player, data);
        } catch (Throwable throwable) {
            LOGGER.warn("神通 tick 异常，已强制结束全部神通状态", throwable);
            try {
                data.wuxingxiantu$setDivineStepOrigin(null);
                data.wuxingxiantu$setDivineStepIndex(0);
                clear(player);
            } catch (Throwable ignored) {
            }
        }
    }

    private static void tickInternal(ServerPlayer player, CultivationData data) {
        Long pendingUntil = PENDING_TRANSFORMS.get(player.getUUID());
        if (pendingUntil != null) {
            if (player.level().getGameTime() >= pendingUntil
                    || player.isSpectator() || !data.wuxingxiantu$getActiveDivineArt().isBlank()) {
                PENDING_TRANSFORMS.remove(player.getUUID());
            }
        }
        String active = data.wuxingxiantu$getActiveDivineArt();
        if (!active.isBlank()) {
            if ("ri_yue_ting_lun".equals(active)) {
                if (!TimeStopManager.isActive(player)) {
                    data.wuxingxiantu$setActiveDivineArt("");
                    GeneratedMod.sync(player);
                    return;
                }
            } else if ("jin_dou_yun".equals(active)) {
                if (!CLOUD_FLIGHT_SPEEDS.containsKey(player.getUUID())) {
                    CLOUD_FLIGHT_SPEEDS.put(player.getUUID(), Math.min(1.0F, player.getAbilities().getFlyingSpeed() * 11.0F));
                }
                player.getAbilities().mayfly = true;
                player.getAbilities().flying = true;
                player.getAbilities().setFlyingSpeed(CLOUD_FLIGHT_SPEEDS.get(player.getUUID()));
                player.onUpdateAbilities();
                if (player.tickCount % 2 == 0) emitCloud(player);
            } else if ("qi_shi_er_bian".equals(active)) {
                // 变身仅由客户端外观事件呈现，服务端只保留已选形态。
            } else if ("fa_tian_xiang_di".equals(active)) {
                if (!player.hasEffect(ModEffects.FA_TIAN_XIANG_DI.get())) {
                    stop(player, data, false);
                    return;
                }
                if (!player.isCreative() && player.tickCount % 20 == 0 && !drain(player, data, cn.blockforge.generated.generatedmod.balance.DivineArtCosts.perSecond(active))) {
                    stop(player, data, true);
                    return;
                }
                // 下蹲会触发巨型碰撞箱重算，锁定站姿可避免服务器主线程陷入刷新循环。
                player.setShiftKeyDown(false);
                if (player.getPose() != Pose.STANDING) player.setPose(Pose.STANDING);
                player.setSprinting(false);
                if (player.tickCount % 4 == 0) emitAura(player.serverLevel(), player, active);
            } else if ("yuan_shen_chu_qiao".equals(active) || "yi_hun_ji_po".equals(active)) {
                net.minecraft.world.effect.MobEffect projectionEffect = "yi_hun_ji_po".equals(active)
                        ? ModEffects.YI_HUN_JI_PO.get() : ModEffects.YUAN_SHEN_CHU_QIAO.get();
                if (!player.hasEffect(projectionEffect)) {
                    stop(player, data, false);
                    return;
                }
                if (!player.isCreative() && player.tickCount % 20 == 0 && !drain(player, data,
                        cn.blockforge.generated.generatedmod.balance.DivineArtCosts.maintenance(active,SoulAnchor.distance(player)))) {
                    stop(player, data, true);
                    return;
                }
                if (player.tickCount % 4 == 0) emitAura(player.serverLevel(), player, active);
                // 元神出窍允许穿墙；移魂寄魄在寻找宿主时也能飞行，但必须保留方块碰撞。
                // 附身后沿用原版相机跟随，由 tickPossession 接管无碰撞状态。
                player.noPhysics = "yuan_shen_chu_qiao".equals(active) || isPossessing(player);
                player.setNoGravity(true);
                // 移魂寄魄负责附身控制；元神出窍只保留本体元神自由移动。
                if ("yi_hun_ji_po".equals(active)) tickPossession(player, data);
            }
        }
        if (data.wuxingxiantu$getDivineStepOrigin() != null) {
            ensureDipperEffect(player, data);
            tickDipper(player, data);
        }
        tickDipperEffect(player);
    }

    private static boolean drain(ServerPlayer player, CultivationData data, int cost) {
        if (data.wuxingxiantu$getAura() < cost) return false;
        data.wuxingxiantu$setAura(data.wuxingxiantu$getAura() - cost);
        return true;
    }

    private static int activationCost(String id) {
        return cn.blockforge.generated.generatedmod.balance.DivineArtCosts.activation(id);
    }

    private static void startSoul(ServerPlayer player, CultivationData data, boolean phaseThroughBlocks) {
        SoulAnchor.capture(player);
        data.wuxingxiantu$setDivineSoulOrigin(player.blockPosition());
        // Both soul arts leave a physical shell; spectator mode only applies to the departing soul.
        {
            SoulBodyEntity body = cn.blockforge.generated.generatedmod.registry.ModEntities.SOUL_BODY.get().create(player.level());
            if (body == null) throw new IllegalStateException("Soul body entity unavailable");
            body.moveTo(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
            body.yBodyRot = player.yBodyRot;
            body.setOwner(player.getUUID());
            for (net.minecraft.world.entity.EquipmentSlot slot : net.minecraft.world.entity.EquipmentSlot.values()) {
                body.setItemSlot(slot, player.getItemBySlot(slot).copy());
                body.setDropChance(slot, 0);
            }
            if (!player.level().addFreshEntity(body))
                throw new IllegalStateException("Soul body could not join the level");
            SOUL_BODIES.put(player.getUUID(), body.getUUID());
            player.getPersistentData().putUUID("wuxingxiantu_soul_body", body.getUUID());
        }
        if (phaseThroughBlocks) {
            // 只有元神出窍使用旁观模式；移魂寄魄必须留在带碰撞的游戏模式中。
            player.setGameMode(GameType.SPECTATOR);
            player.noPhysics = true;
        } else {
            // 保留发动前的生存/创造模式，只临时授予飞行和无重力，方块碰撞仍由实体移动处理。
            player.noPhysics = false;
            player.getAbilities().mayfly = true;
            player.getAbilities().flying = true;
        }
        player.setNoGravity(true);
        player.onUpdateAbilities();
    }

    /** 将末影龙的可命中部件归一化为主体，便于附身和能力定位。 */


    /** 返回当前附身宿主；能力系统在附身期间应操作宿主而不是玩家本体。 */


    /** 当前附身宿主的实体类型 ID；七十二变状态则使用已选择形态。 */


    /** 移魂寄魄：记录返回位置，并将相机切换到视线内的生灵。 */




    /** 根据附身宿主反查控制它的玩家，用于统一处理主动攻击后的合法反击。 */


    /**
     * 给当前附身宿主登记一次短时能力位移。位移由附身 tick 统一执行，避免能力方法写入的
     * deltaMovement 在同一 tick 被普通移动刹车覆盖。
     */


    /** 确认七十二变形态；只有确认后才扣除灵力、激活神通并写入冷却。 */
    public static boolean confirmTransformSelection(ServerPlayer player, int index) {
        Long pendingUntil = PENDING_TRANSFORMS.get(player.getUUID());
        if (pendingUntil == null || player.level().getGameTime() >= pendingUntil) {
            PENDING_TRANSFORMS.remove(player.getUUID());
            return false;
        }
        CultivationData data = GeneratedMod.data(player);
        DivineArtCatalog.Entry entry = DivineArtCatalog.entry("qi_shi_er_bian");
        String[] forms = data == null ? new String[0] : data.wuxingxiantu$getPossessedForms();
        if (data == null || entry == null || player.isSpectator() || !data.wuxingxiantu$getActiveDivineArt().isBlank()
                || index < 0 || index >= forms.length) return false;
        long now = player.level().getGameTime();
        if (!data.wuxingxiantu$hasCreationBlessing()
                && data.wuxingxiantu$getDivineCooldown("qi_shi_er_bian") > now) {
            PENDING_TRANSFORMS.remove(player.getUUID());
            return false;
        }
        int cost = activationCost("qi_shi_er_bian");
        if (!player.isCreative() && data.wuxingxiantu$getAura() < cost) {
            PENDING_TRANSFORMS.remove(player.getUUID());
            player.displayClientMessage(Component.translatable("message.wuxingxiantu.divine_mana_missing", cost), true);
            return false;
        }
        if (!player.isCreative()) data.wuxingxiantu$setAura(data.wuxingxiantu$getAura() - cost);
        // 只有确认选中形态后才接管飞行权限；取消界面不会留下恢复快照。
        saveAbilities(player);
        data.wuxingxiantu$setActivePossessedForm(forms[index]);
        data.wuxingxiantu$setActiveDivineArt("qi_shi_er_bian");
        TransformAbilityRegistry.onTransformStart(player, forms[index]);
        data.wuxingxiantu$setDivineCooldown("qi_shi_er_bian", now + entry.cooldownTicks());
        PENDING_TRANSFORMS.remove(player.getUUID());
        player.displayClientMessage(Component.translatable("message.wuxingxiantu.divine_started_qi_shi_er_bian")
                .withStyle(style -> style.withColor(0xFFFF69C8)), true);
        GeneratedMod.sync(player);
        return true;
    }

    /** 取消尚未确认的七十二变形态选择，不产生神通效果或冷却。 */
    public static void cancelTransformSelection(ServerPlayer player) {
        PENDING_TRANSFORMS.remove(player.getUUID());
    }

    /** 切换七十二变的下一个已记录生灵形态。 */
    public static boolean cyclePossessedForm(ServerPlayer player) {
        CultivationData data = GeneratedMod.data(player);
        if (data == null || !data.wuxingxiantu$getActiveDivineArt().equals("qi_shi_er_bian")) return false;
        String[] forms = data.wuxingxiantu$getPossessedForms();
        if (forms.length == 0) return false;
        String active = data.wuxingxiantu$getActivePossessedForm();
        int index = 0;
        for (int i = 0; i < forms.length; i++) if (forms[i].equals(active)) { index = (i + 1) % forms.length; break; }
        data.wuxingxiantu$setActivePossessedForm(forms[index]);
        TransformAbilityRegistry.onTransformChanged(player, forms[index]);
        player.displayClientMessage(Component.translatable("message.wuxingxiantu.transform_changed",
                formDisplayName(forms[index])), true);
        GeneratedMod.sync(player);
        return true;
    }

    /** 将保存的实体类型 ID 转为本地化名称，任何界面和提示都不直接展示注册名。 */
    public static Component formDisplayName(String formId) {
        ResourceLocation key = ResourceLocation.tryParse(formId == null ? "" : formId);
        if (key != null && BuiltInRegistries.ENTITY_TYPE.containsKey(key)) {
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(key);
            if (type != null) {
                // 心魔实体名称需要玩家参数；形态列表只显示实体类型名，不能直接使用带占位符的实体翻译。
                if (key.equals(new ResourceLocation(GeneratedMod.MOD_ID, "heart_demon"))) {
                    return Component.translatable("entity.wuxingxiantu.heart_demon_form");
                }
                return type.getDescription();
            }
        }
        return Component.translatable("entity.wuxingxiantu.unknown");
    }

    /** 目标死亡时立即解除寄魄，避免死亡实体仍被相机或控制状态引用。 */


    /** 接收客户端的寄魄控制输入；所有移动和攻击仍在服务端执行。 */
















    public static void endTimeStop(ServerPlayer player) {
        CultivationData data = GeneratedMod.data(player);
        if (data == null || !"ri_yue_ting_lun".equals(data.wuxingxiantu$getActiveDivineArt())) return;
        data.wuxingxiantu$setActiveDivineArt("");
        player.displayClientMessage(Component.translatable("message.wuxingxiantu.divine_stopped"), true);
        GeneratedMod.sync(player);
    }

    public static void stop(ServerPlayer player, CultivationData data, boolean notify) {
        String active = data.wuxingxiantu$getActiveDivineArt();
        if (active.isBlank()) return;
        if ("jin_dou_yun".equals(active)) Network.sendCloudVisual(player, false);
        PENDING_TRANSFORMS.remove(player.getUUID());
        if ("ri_yue_ting_lun".equals(active)) TimeStopManager.clear(player);
        // 变身技能的临时冷却不跨越一次神通生命周期，避免换形态后继承旧技能状态。
        TransformAbilityRegistry.onTransformStop(player);
        TransformAbilityRegistry.clear(player);
        clearPossession(player);
        player.removeEffect(ModEffects.FA_TIAN_XIANG_DI.get());
        player.removeEffect(ModEffects.YUAN_SHEN_CHU_QIAO.get());
        player.removeEffect(ModEffects.YI_HUN_JI_PO.get());
        AbilityState state = SAVED_ABILITIES.remove(player.getUUID());
        if(state==null&&player.getPersistentData().contains(ABILITY_SNAPSHOT,10)) {
            var saved=player.getPersistentData().getCompound(ABILITY_SNAPSHOT);
            float speed=saved.getFloat("Speed");
            int mode=saved.getInt("Mode");
            if(mode>=0&&mode<=3&&Float.isFinite(speed)&&speed>=0)
                state=new AbilityState(GameType.byId(mode),saved.getBoolean("MayFly"),saved.getBoolean("Flying"),
                        speed,saved.getBoolean("NoPhysics"),saved.getBoolean("NoGravity"));
        }
        player.getPersistentData().remove(ABILITY_SNAPSHOT);
        CLOUD_FLIGHT_SPEEDS.remove(player.getUUID());
        UUID bodyId = SOUL_BODIES.remove(player.getUUID());
        if (bodyId == null && player.getPersistentData().hasUUID("wuxingxiantu_soul_body"))
            bodyId = player.getPersistentData().getUUID("wuxingxiantu_soul_body");
        if (bodyId != null) {
            for (ServerLevel level : player.server.getAllLevels()) {
                Entity body = level.getEntity(bodyId);
                if (body instanceof SoulBodyEntity) body.discard();
            }
        }
        player.getPersistentData().remove("wuxingxiantu_soul_body");
        TransformAbilityRegistry.clear(player);
        if (state != null) {
            player.setGameMode(state.gameType());
            player.getAbilities().mayfly = state.mayFly();
            player.getAbilities().flying = state.flying();
            player.getAbilities().setFlyingSpeed(state.speed());
            player.noPhysics = state.noPhysics();
            player.setNoGravity(state.noGravity());
            player.onUpdateAbilities();
        } else {
            // Legacy saves without a persistent snapshot cannot recover the original mode.
            if (player.isSpectator()) player.setGameMode(GameType.SURVIVAL);
            player.noPhysics = false;
            player.setNoGravity(false);
        }
        data.wuxingxiantu$setActiveDivineArt("");
        data.wuxingxiantu$setDivineSoulOrigin(null);
        SoulAnchor.returnToBody(player);
        cn.blockforge.generated.generatedmod.divineart.DipperEffects.clear(player);
        GeneratedMod.removeGiantReach(player);
        player.refreshDimensions();
        if (notify) player.displayClientMessage(Component.translatable("message.wuxingxiantu.divine_stopped"), true);
        GeneratedMod.sync(player);
    }

    private static void saveAbilities(ServerPlayer player) {
        // Restore sword-flight grants before another divine art snapshots flight abilities.
        cn.blockforge.generated.generatedmod.yujian.YujianController.stop(player);
        SAVED_ABILITIES.putIfAbsent(player.getUUID(), new AbilityState(
                player.gameMode.getGameModeForPlayer(), player.getAbilities().mayfly,
                player.getAbilities().flying, player.getAbilities().getFlyingSpeed(),
                player.noPhysics, player.isNoGravity()));
        if(!player.getPersistentData().contains(ABILITY_SNAPSHOT,10)) {
            var state=SAVED_ABILITIES.get(player.getUUID());
            var saved=new net.minecraft.nbt.CompoundTag();
            saved.putInt("Mode",state.gameType().getId());saved.putBoolean("MayFly",state.mayFly());
            saved.putBoolean("Flying",state.flying());saved.putFloat("Speed",state.speed());
            saved.putBoolean("NoPhysics",state.noPhysics());saved.putBoolean("NoGravity",state.noGravity());
            player.getPersistentData().put(ABILITY_SNAPSHOT,saved);
        }
    }

    public static void clear(ServerPlayer player) {
        CultivationData data = GeneratedMod.data(player);
        if (data != null) stop(player, data, false);
        SAVED_ABILITIES.remove(player.getUUID());
        cn.blockforge.generated.generatedmod.divineart.DipperEffects.clear(player);
    }









    public static LivingEntity lookTarget(LivingEntity source, double range) {
        Vec3 start = source.getEyePosition();
        Vec3 end = start.add(source.getLookAngle().normalize().scale(range));
        net.minecraft.world.phys.EntityHitResult hit = ProjectileUtil.getEntityHitResult(source, start, end,
                source.getBoundingBox().expandTowards(end.subtract(start)).inflate(1.0D),
                entity -> entity instanceof LivingEntity living && living != source && living.isAlive(), range * range);
        return hit != null && hit.getEntity() instanceof LivingEntity living ? living : null;
    }



















    private static void tickDipper(ServerPlayer player, CultivationData data){ cn.blockforge.generated.generatedmod.divineart.DipperEffects.tickDipper(player, data); }
    private static void startDipperEffect(ServerPlayer player){ cn.blockforge.generated.generatedmod.divineart.DipperEffects.startDipperEffect(player); }
    private static void ensureDipperEffect(ServerPlayer player, CultivationData data){ cn.blockforge.generated.generatedmod.divineart.DipperEffects.ensureDipperEffect(player, data); }
    private static void tickDipperEffect(ServerPlayer player){ cn.blockforge.generated.generatedmod.divineart.DipperEffects.tickDipperEffect(player); }
    private static void emitCloud(ServerPlayer player){ cn.blockforge.generated.generatedmod.divineart.DipperEffects.emitCloud(player); }
    private static void emitAura(ServerLevel level, ServerPlayer player, String id){ cn.blockforge.generated.generatedmod.divineart.DipperEffects.emitAura(level, player, id); }
    public static LivingEntity possessionTarget(Entity entity){ return cn.blockforge.generated.generatedmod.divineart.PossessionController.possessionTarget(entity); }
    public static LivingEntity possessedTarget(ServerPlayer player){ return cn.blockforge.generated.generatedmod.divineart.PossessionController.possessedTarget(player); }
    public static String activeAbilityForm(ServerPlayer player){ return cn.blockforge.generated.generatedmod.divineart.PossessionController.activeAbilityForm(player); }
    public static boolean tryPossess(ServerPlayer player, LivingEntity target){ return cn.blockforge.generated.generatedmod.divineart.PossessionController.tryPossess(player, target); }
    public static boolean isPossessing(ServerPlayer player){ return cn.blockforge.generated.generatedmod.divineart.PossessionController.isPossessing(player); }
    public static ServerPlayer possessingPlayer(Entity entity){ return cn.blockforge.generated.generatedmod.divineart.PossessionController.possessingPlayer(entity); }
    public static boolean setPossessionAbilityMotion(ServerPlayer player, Vec3 motion, int ticks){ return cn.blockforge.generated.generatedmod.divineart.PossessionController.setPossessionAbilityMotion(player, motion, ticks); }
    public static void onPossessedEntityDeath(LivingEntity entity){ cn.blockforge.generated.generatedmod.divineart.PossessionController.onPossessedEntityDeath(entity); }
    public static void setPossessionInput(ServerPlayer player, float yaw, float pitch,
                                          boolean forward, boolean backward, boolean left, boolean right,
                                          boolean jump, boolean sneak, boolean attack){ cn.blockforge.generated.generatedmod.divineart.PossessionController.setPossessionInput(player, yaw, pitch, forward, backward, left, right, jump, sneak, attack); }
    private static void tickPossession(ServerPlayer player, CultivationData data){ cn.blockforge.generated.generatedmod.divineart.PossessionController.tickPossession(player, data); }
    private static void clearPossession(ServerPlayer player){ cn.blockforge.generated.generatedmod.divineart.PossessionController.clearPossession(player); }
}
