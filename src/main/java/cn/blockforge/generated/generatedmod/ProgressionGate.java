package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.items.CultivationLogic;
import cn.blockforge.generated.generatedmod.yujian.SwordProfile;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

/**
 * 统一的玩家进度/境界门槛层：所有系统（功法、心法、丹药、灵根、异火、设施、
 * 灵剑、御剑、剑诀、符箓、魂魄）的境界判定都必须走这里，禁止在各 Item、
 * BlockEntity、Packet、Event 中重新散落硬编码境界数字。
 *
 * <p>设计原则：</p>
 * <ul>
 *   <li>“获得”与“使用”分离——高阶资源允许流通（持有、收藏、交易、展示），
 *       高阶力量必须检查操作者本人的进度；</li>
 *   <li>本类全部判定只读玩家数据、不产生副作用，客户端可用于 UI 提示，
 *       但权威调用点必须在服务端；服务端始终视玩家输入为不可信；</li>
 *   <li>旧存档兼容：不删除玩家已学习/已掌握的数据，只在使用时重新校验，
 *       条件满足后自动恢复可用；</li>
 *   <li>境界要求集中在本类的配置表中，后续可整体迁移到数据文件调整。</li>
 * </ul>
 */
public final class ProgressionGate {
    // ---- 统一境界顺序（与 CultivationLogic.REALMS 的下标一致） ----
    public static final int QI_CONDENSATION = 0;     // 练气
    public static final int FOUNDATION = 1;          // 筑基
    public static final int GOLDEN_CORE = 2;         // 金丹
    public static final int NASCENT_SOUL = 3;        // 元婴
    public static final int SPIRIT_TRANSFORMATION = 4; // 化神
    public static final int VOID_REFINEMENT = 5;     // 炼虚
    public static final int INTEGRATION = 6;         // 合体
    public static final int MAHAYANA = 7;            // 大乘
    public static final int EARTH_IMMORTAL = 8;      // 地仙

    /** 功法释放/学习提示消息的本地化 key；调用方 deny 时展示。 */
    public static final String MSG_ART_LEARN = "message.wuxingxiantu.gate.art_learn";
    public static final String MSG_ART_USE = "message.wuxingxiantu.gate.art_use";
    public static final String MSG_HEART_LEARN = "message.wuxingxiantu.gate.heart_learn";
    public static final String MSG_HEART_EQUIP = "message.wuxingxiantu.gate.heart_equip";
    public static final String MSG_PILL = "message.wuxingxiantu.gate.pill";
    public static final String MSG_ROOT = "message.wuxingxiantu.gate.root";
    public static final String MSG_FIRE_REFINE = "message.wuxingxiantu.gate.fire_refine";
    public static final String MSG_FIRE_USE = "message.wuxingxiantu.gate.fire_use";
    public static final String MSG_FIRE_NOT_MASTERED = "message.wuxingxiantu.gate.fire_not_mastered";
    public static final String MSG_FURNACE = "message.wuxingxiantu.gate.furnace";
    public static final String MSG_FORGE = "message.wuxingxiantu.gate.forge";
    public static final String MSG_FIRE_BORROW = "message.wuxingxiantu.gate.fire_borrow";
    public static final String MSG_SWORD = "message.wuxingxiantu.gate.sword";
    public static final String MSG_SWORD_SUPPRESS = "message.wuxingxiantu.gate.sword_suppress";
    public static final String MSG_YUJIAN_ART = "message.wuxingxiantu.gate.yujian_need_art";
    public static final String MSG_YUJIAN_REALM = "message.wuxingxiantu.gate.yujian_realm";
    public static final String MSG_TECHNIQUE = "message.wuxingxiantu.gate.technique";
    public static final String MSG_TALISMAN = "message.wuxingxiantu.gate.talisman";
    public static final String MSG_SOUL = "message.wuxingxiantu.gate.soul";
    public static final String MSG_THUNDER_GATE = "message.wuxingxiantu.gate.thunder";

    // ---- 集中配置：品级 → 最低境界 ----

    /** 功法按品阶的默认最低境界（下标 1..4 = 黄/玄/地/天：练气/练气/筑基/金丹）。个别功法可用 ART_OVERRIDES 单独覆盖。 */
    private static final int[] ART_TIER_REALM = {QI_CONDENSATION, QI_CONDENSATION, QI_CONDENSATION, FOUNDATION, GOLDEN_CORE};
    private static final Map<String, Integer> ART_OVERRIDES = Map.of(
            "art_sword_flight", GOLDEN_CORE,
            // 来源的境界下标从 1 开始；迁入后使用本框架天阶功法的金丹门槛。
            "art_wanjian_guizong", GOLDEN_CORE,
            "art_jiuxiao_sword_rain", GOLDEN_CORE);

    /** 心法独立最低境界。 */
    private static final Map<String, Integer> HEART_METHOD_REALM = Map.of(
            "yuan_hun", QI_CONDENSATION,
            "gathering", QI_CONDENSATION,
            "tiangang", FOUNDATION,
            "xuanshui", FOUNDATION,
            "thick_earth", FOUNDATION,
            "beast_taming", FOUNDATION,
            "nine_turns", GOLDEN_CORE,
            "burning_heaven", GOLDEN_CORE,
            "wanling", NASCENT_SOUL,
            "taixu", SPIRIT_TRANSFORMATION);

    /** 灵根品质（1 下品 … 4 极品）→ 最低境界。下标即品质。 */
    private static final int[] ROOT_QUALITY_REALM = {QI_CONDENSATION, QI_CONDENSATION, FOUNDATION, GOLDEN_CORE, NASCENT_SOUL};

    /** 炼丹炉品阶（0-5）→ 操作者最低境界。 */
    private static final int[] FURNACE_TIER_REALM = {QI_CONDENSATION, QI_CONDENSATION, FOUNDATION, GOLDEN_CORE, NASCENT_SOUL, SPIRIT_TRANSFORMATION};

    /** 灵剑品阶（SwordProfile.tier 0-7）→ 发挥完整力量的最低境界。 */
    private static final int[] SWORD_TIER_REALM = {QI_CONDENSATION, QI_CONDENSATION, FOUNDATION, GOLDEN_CORE,
            NASCENT_SOUL, SPIRIT_TRANSFORMATION, VOID_REFINEMENT, MAHAYANA};

    /** 符箓品质 → 最低境界。 */
    private static final Map<TalismanCatalog.Quality, Integer> TALISMAN_QUALITY_REALM = Map.of(
            TalismanCatalog.Quality.WASTE, QI_CONDENSATION,
            TalismanCatalog.Quality.LOW, QI_CONDENSATION,
            TalismanCatalog.Quality.MID, GOLDEN_CORE,
            TalismanCatalog.Quality.PERFECT, SPIRIT_TRANSFORMATION);

    /** 魂魄系统功能 → 最低境界。 */
    public static final String SOUL_CAPTURE = "soul_capture";
    public static final String SOUL_BINDING = "soul_binding";
    public static final String SOUL_UTILITY = "soul_utility";
    public static final String SOUL_BANNER = "soul_banner";
    public static final String SOUL_FURNACE = "soul_furnace";
    private static final Map<String, Integer> SOUL_FEATURE_REALM = Map.of(
            SOUL_CAPTURE, FOUNDATION,
            SOUL_BINDING, GOLDEN_CORE,
            SOUL_UTILITY, NASCENT_SOUL,
            SOUL_BANNER, SPIRIT_TRANSFORMATION,
            SOUL_FURNACE, NASCENT_SOUL);

    /** 御剑的绝对最低境界（与所执灵剑的境界要求取较大者）。 */
    public static final int YUJIAN_MINIMUM_REALM = GOLDEN_CORE;

    /** 天雷灵根解锁：最低境界、木灵根最低品质。 */
    public static final int THUNDER_ROOT_REALM = SPIRIT_TRANSFORMATION;
    public static final int THUNDER_ROOT_QUALITY = 3;
    /** 异火灵根显化：最低境界、火灵根最低品质、已掌握异火的最低品阶。 */
    public static final int EXOTIC_ROOT_REALM = NASCENT_SOUL;
    public static final int EXOTIC_ROOT_QUALITY = 3;
    public static final int EXOTIC_ROOT_FIRE_TIER = 6;

    private ProgressionGate() {}

    // ---- 境界比较统一入口 ----

    public static int realm(CultivationData data) {
        return data == null ? 0 : Math.max(0, Math.min(EARTH_IMMORTAL, data.wuxingxiantu$getRealm()));
    }

    public static int realmOf(Player player) {
        return realm(player == null ? null : GeneratedMod.data(player));
    }

    /** 玩家本人境界是否达到要求；玩家或数据缺失一律按不满足处理。 */
    public static boolean isAtLeastRealm(Player player, int requiredRealm) {
        CultivationData data = player == null ? null : GeneratedMod.data(player);
        return isAtLeastRealm(data, requiredRealm);
    }

    public static boolean isAtLeastRealm(CultivationData data, int requiredRealm) {
        return data != null && realm(data) >= requiredRealm;
    }

    /** 创造模式旁路境界门槛；异火学习记录仍由实际使用入口验证。 */
    private static boolean bypass(Player player) {
        return player != null && player.isCreative();
    }

    // ---- 功法 ----

    public static int requiredRealmForArt(String artId) {
        Integer override = ART_OVERRIDES.get(artId);
        if (override != null) return override;
        return ART_TIER_REALM[Math.max(1, Math.min(4, ArtCatalog.tier(artId)))];
    }

    public static boolean canLearnArt(Player player, String artId) {
        return bypass(player) || isAtLeastRealm(player, requiredRealmForArt(artId));
    }

    /** 释放时重新验证：即使旧存档/命令已学会，境界不足同样禁止释放。 */
    public static boolean canUseArt(Player player, String artId) {
        return canLearnArt(player, artId);
    }

    // ---- 心法 ----

    public static int requiredRealmForHeartMethod(String methodId) {
        Integer required = HEART_METHOD_REALM.get(methodId);
        return required == null ? QI_CONDENSATION : required;
    }

    public static boolean canLearnHeartMethod(Player player, String methodId) {
        return bypass(player) || isAtLeastRealm(player, requiredRealmForHeartMethod(methodId));
    }

    /** 装备/启用时再次检查；GUI 按钮只是表现层，这里才是权威。 */
    public static boolean canEquipHeartMethod(Player player, String methodId) {
        return canLearnHeartMethod(player, methodId);
    }

    /**
     * 服务端效果计算使用的“当前实际生效心法”：境界不足时按无心法结算，
     * 保留学习数据不清除，境界达标后自动恢复生效。
     */
    public static String effectiveHeartMethod(Player player, CultivationData data) {
        if (data == null) return "";
        String equipped = data.wuxingxiantu$getEquippedHeartMethod();
        if (equipped == null || equipped.isBlank()) return "";
        if (player != null && bypass(player)) return equipped;
        return isAtLeastRealm(data, requiredRealmForHeartMethod(equipped)) ? equipped : "";
    }

    // ---- 丹药 ----

    /**
     * 玩家能否承受该品质（1 下品 … 4 极品）灵根丹/觉醒丹的药力。
     * 高阶丹药允许持有、赠送、购买；服用时校验使用者本人。
     */
    public static boolean canUsePill(Player player, int quality) {
        return bypass(player) || isAtLeastRealm(player, requiredRealmForRootQuality(quality));
    }

    public static int requiredRealmForPill(ItemStack stack) {
        if(cn.blockforge.generated.generatedmod.alchemy.effects.PillSnapshot.isV5(stack)){var result=cn.blockforge.generated.generatedmod.alchemy.effects.PillSnapshot.view(stack);return result==null?EARTH_IMMORTAL:requiredRealmForV5(result);}
        var pill = cn.blockforge.generated.generatedmod.alchemy.PillData.read(stack);
        if (pill == null) return QI_CONDENSATION;
        int realm = Math.max(pill.pill().minimumRealm,cn.blockforge.generated.generatedmod.alchemy.MedicineTier.of(stack)-1);
        if (pill.pill().progression) realm = Math.max(realm, requiredRealmForRootQuality(pill.grade().value()));
        if (pill.pill() == cn.blockforge.generated.generatedmod.alchemy.PillCatalog.CULTIVATION) {
            realm = Math.max(realm, pill.cultivationTier());
        }
        return realm;
    }

    public static boolean canUsePill(Player player, ItemStack stack) {
        return player != null && (bypass(player) || isAtLeastRealm(player, requiredRealmForPill(stack)));
    }

    /** New pill grade is independent of realm; actual module permissions set the minimum. */
    public static int requiredRealmForV5(cn.blockforge.generated.generatedmod.alchemy.effects.AbilityCompiler.Result r){
        int required=r.realm();for(var e:r.effects())required=Math.max(required,switch(e.id()){case shield,resistance->FOUNDATION;default->QI_CONDENSATION;});return required;
    }
    public static boolean canUseV5Pill(Player player,cn.blockforge.generated.generatedmod.alchemy.effects.AbilityCompiler.Result r){return player!=null&&r!=null&&r.grade()>0&&(bypass(player)||isAtLeastRealm(player,requiredRealmForV5(r)));}

    // ---- 灵根 ----

    public static int requiredRealmForRootQuality(int quality) {
        return ROOT_QUALITY_REALM[Math.max(1, Math.min(4, quality))];
    }

    public static boolean canAwakenRoot(Player player, int element, int quality) {
        return bypass(player) || isAtLeastRealm(player, requiredRealmForRootQuality(quality));
    }

    /** 天雷灵根解锁资格：境界 + 木灵根品质 + 有效雷击来源（由调用方判定后传入）。 */
    public static boolean canUnlockThunderRoot(Player player, CultivationData data, boolean validSource) {
        if (!validSource) return false;
        if (bypass(player)) return true;
        return isAtLeastRealm(data, THUNDER_ROOT_REALM)
                && data.wuxingxiantu$getRootQuality(1) >= THUNDER_ROOT_QUALITY;
    }

    /** 天雷灵根能否显化（已解锁由数据保证，这里追加境界与品质复核）。 */
    public static boolean canManifestThunderRoot(CultivationData data) {
        return data != null && isAtLeastRealm(data, THUNDER_ROOT_REALM)
                && data.wuxingxiantu$getRootQuality(1) >= THUNDER_ROOT_QUALITY;
    }

    /** 异火灵根能否显化：境界 + 火灵根品质 + 已掌握异火的最低品阶。 */
    public static boolean canManifestExoticRoot(CultivationData data) {
        if (data == null || !isAtLeastRealm(data, EXOTIC_ROOT_REALM)) return false;
        if (data.wuxingxiantu$getRootQuality(3) < EXOTIC_ROOT_QUALITY) return false;
        for (int tier = EXOTIC_ROOT_FIRE_TIER; tier <= 9; tier++) {
            if (data.wuxingxiantu$hasLearnedFire(tier)) return true;
        }
        return false;
    }

    // ---- 异火 ----

    public static int requiredRealmForFire(int fireTier) {
        var fire = cn.blockforge.generated.generatedmod.fire.FireCatalog.tier(fireTier);
        return fire == null ? QI_CONDENSATION : fire.requiredRealm();
    }

    /** Realm prerequisite for the non-consuming refinement channel. */
    public static boolean canRefineFire(Player player, int fireTier) {
        return meetsFireRealmRequirement(player, fireTier);
    }

    /** 仅检查品阶与境界，用于首次炼化；不代表已经掌握该异火。 */
    public static boolean meetsFireRealmRequirement(Player player, int fireTier) {
        return fireTier >= 1 && fireTier <= 9
                && (bypass(player) || isAtLeastRealm(player, requiredRealmForFire(fireTier)));
    }

    public static boolean meetsFireRealmRequirement(CultivationData data, int fireTier) {
        return fireTier >= 1 && fireTier <= 9 && isAtLeastRealm(data, requiredRealmForFire(fireTier));
    }

    private static boolean hasMasteredFire(CultivationData data, int fireTier) {
        return data != null && fireTier >= 1 && fireTier <= 9 && data.wuxingxiantu$hasLearnedFire(fireTier);
    }

    /** 实际使用必须本人已掌握；创造模式只旁路境界，不授予学习记录。 */
    public static boolean canUseFire(Player player, int fireTier) {
        return hasMasteredFire(player == null ? null : GeneratedMod.data(player), fireTier)
                && meetsFireRealmRequirement(player, fireTier);
    }

    public static boolean canUseFire(CultivationData data, int fireTier) {
        return hasMasteredFire(data, fireTier) && meetsFireRealmRequirement(data, fireTier);
    }

    /** 统一区分“未掌握”与“境界不足”，调用方不重复读取学习记录。 */
    public static boolean denyFireUse(Player player, int fireTier) {
        if (!hasMasteredFire(player == null ? null : GeneratedMod.data(player), fireTier)) {
            return deny(player, MSG_FIRE_NOT_MASTERED);
        }
        return deny(player, MSG_FIRE_USE, requiredRealmForFire(fireTier));
    }

    /**
     * “当前真正可驭使的装备异火”：旧存档或命令让低境界玩家持有已学高阶异火时，
     * 数据保留但实际战斗/炼制按未装备结算，境界达标后自动恢复。
     */
    public static int effectiveFire(CultivationData data, int equippedTier) {
        if (equippedTier <= 0) return 0;
        return canUseFire(data, equippedTier) ? equippedTier : 0;
    }

    // ---- 设施 ----

    public static int requiredRealmForFurnace(int furnaceTier) {
        return FURNACE_TIER_REALM[Math.max(0, Math.min(FURNACE_TIER_REALM.length - 1, furnaceTier))];
    }

    public static boolean canUseAlchemyFurnace(Player player, int furnaceTier) {
        return bypass(player) || isAtLeastRealm(player, requiredRealmForFurnace(furnaceTier));
    }

    /** 炼器台以火候（温度）折算品阶；配方要求的温度越高，操作者境界要求越高。 */
    public static int requiredRealmForRefiningTemperature(int temperature) {
        if (temperature <= 0) return QI_CONDENSATION;
        int fireTier = (temperature - 300) / 100;
        return requiredRealmForFire(Math.max(1, fireTier));
    }

    public static boolean canUseRefiningForge(Player player, int recipeTemperature) {
        return bypass(player) || isAtLeastRealm(player, requiredRealmForRefiningTemperature(recipeTemperature));
    }

    // ---- 灵剑 / 御剑 / 剑诀 ----

    /** 只有本模组认可的灵剑才可进入御剑与剑诀系统。 */
    public static boolean isSpiritSword(ItemStack stack) {
        return stack.getItem() instanceof cn.blockforge.generated.generatedmod.items.SpiritSwordItem;
    }

    public static int requiredRealmForSword(ItemStack stack) {
        SwordProfile profile = SwordProfile.of(stack);
        return SWORD_TIER_REALM[Math.max(0, Math.min(SWORD_TIER_REALM.length - 1, profile.tier()))];
    }

    /** 驾驭该剑的最低境界（高阶剑即使被赠送/拾取，境界不足也不能完整发挥）。 */
    public static boolean canUseSword(Player player, ItemStack stack) {
        if (!isSpiritSword(stack)) return false;
        return bypass(player) || isAtLeastRealm(player, requiredRealmForSword(stack));
    }

    /** 御剑所需最低境界：御剑绝对门槛与所执灵剑的境界要求取较大者。 */
    public static int yujianRequiredRealm(CultivationData data, ItemStack stack) {
        int swordRequired = isSpiritSword(stack) ? requiredRealmForSword(stack) : QI_CONDENSATION;
        return Math.max(YUJIAN_MINIMUM_REALM, swordRequired);
    }

    /** 御剑资格：合格灵剑 + 已学《御剑凌霄诀》+ 满足最低境界（剑阶与御剑门槛取大）。 */
    public static boolean canYujian(Player player, CultivationData data, ItemStack stack) {
        if (data == null || !isSpiritSword(stack)
                || !cn.blockforge.generated.generatedmod.yujian.FlyingSwordRegistry.canFly(stack)) return false;
        if (!data.wuxingxiantu$hasLearnedArt(cn.blockforge.generated.generatedmod.yujian.YujianController.FLIGHT)) return false;
        if (bypass(player)) return true;
        return isAtLeastRealm(data, Math.max(YUJIAN_MINIMUM_REALM, requiredRealmForSword(stack)));
    }

    /** 剑诀：既要求已装备该剑诀，也要求能驾驭当前灵剑与剑诀本身境界。 */
    public static boolean canUseSwordTechnique(Player player, String techniqueId, ItemStack stack) {
        if (!isSpiritSword(stack)) return false;
        var data = GeneratedMod.data(player);
        var nurture = cn.blockforge.generated.generatedmod.yujian.NurtureData.read(stack);
        return data != null && data.wuxingxiantu$knowsSwordTechnique(techniqueId)
                && nurture.equippedTechniques.contains(techniqueId)
                && nurture.equippedTechniques.indexOf(techniqueId) < SwordProfile.of(stack).maxTechniqueSlots()
                && cn.blockforge.generated.generatedmod.yujian.SwordTechniqueRegistry.supports(
                        cn.blockforge.generated.generatedmod.yujian.SwordTechniqueRegistry.get(techniqueId), SwordProfile.of(stack), nurture)
                && canUseSword(player, stack) && canUseArt(player, techniqueId);
    }

    public static boolean canJinghong(Player player, ItemStack stack) {
        var data=GeneratedMod.data(player);
        var definition=cn.blockforge.generated.generatedmod.yujian.FlyingSwordRegistry.get(stack);
        return definition!=null && definition.advanced().jinghong() && canYujian(player,data,stack)
                && (bypass(player) || isAtLeastRealm(data,cn.blockforge.generated.generatedmod.yujian.JinghongBurst.REALM))
                && data.wuxingxiantu$getArtMastery(cn.blockforge.generated.generatedmod.yujian.YujianController.FLIGHT)
                        >=cn.blockforge.generated.generatedmod.yujian.JinghongBurst.MASTERY;
    }

    public static boolean canYujianPhase(Player player, ItemStack stack,
            cn.blockforge.generated.generatedmod.yujian.YujianPhase phase) {
        var data=GeneratedMod.data(player);
        var definition=cn.blockforge.generated.generatedmod.yujian.FlyingSwordRegistry.get(stack);
        return definition!=null && definition.advanced().supports(phase) && canYujian(player,data,stack)
                && (bypass(player) || isAtLeastRealm(data,phase.realm))
                && data.wuxingxiantu$getArtMastery(cn.blockforge.generated.generatedmod.yujian.YujianController.FLIGHT)>=phase.mastery;
    }

    // ---- 符箓 ----

    public static int requiredRealmForTalisman(TalismanCatalog.Quality quality) {
        Integer required = quality == null ? null : TALISMAN_QUALITY_REALM.get(quality);
        return required == null ? QI_CONDENSATION : required;
    }

    public static boolean canUseTalisman(Player player, TalismanCatalog.Quality quality) {
        return bypass(player) || isAtLeastRealm(player, requiredRealmForTalisman(quality));
    }

    // ---- 魂魄系统 ----

    public static int requiredRealmForSoulFeature(String featureId) {
        Integer required = SOUL_FEATURE_REALM.get(featureId);
        return required == null ? QI_CONDENSATION : required;
    }

    public static boolean canUseSoulSystem(Player player, String featureId) {
        return bypass(player) || isAtLeastRealm(player, requiredRealmForSoulFeature(featureId));
    }

    // ---- 统一失败反馈 ----

    /**
     * 展示一条门槛拒绝提示（自动附带所需境界名）并恒返回 false，便于写进
     * {@code if (!gate(...) && deny(...)) return;} 之类的分支。
     */
    public static boolean deny(Player player, String messageKey, int requiredRealm) {
        if (player == null || player.level().isClientSide) return false;
        player.displayClientMessage(Component.translatable(messageKey,
                CultivationLogic.realmName(player, requiredRealm)), true);
        return false;
    }

    /** 无境界参数的拒绝（例如“未学习御剑凌霄诀”）。 */
    public static boolean deny(Player player, String messageKey) {
        if (player == null || player.level().isClientSide) return false;
        player.displayClientMessage(Component.translatable(messageKey), true);
        return false;
    }

    /** 供 UI 与审计输出使用的可读境界名。 */
    public static String realmDisplayName(int realm) {
        return CultivationLogic.realmName(Math.max(0, Math.min(EARTH_IMMORTAL, realm)));
    }
}
