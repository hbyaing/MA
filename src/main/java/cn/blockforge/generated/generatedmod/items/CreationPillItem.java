package cn.blockforge.generated.generatedmod.items;

import cn.blockforge.generated.generatedmod.ArtCatalog;
import cn.blockforge.generated.generatedmod.CultivationData;
import cn.blockforge.generated.generatedmod.DivineArtCatalog;
import cn.blockforge.generated.generatedmod.HeartMethodCatalog;
import cn.blockforge.generated.generatedmod.TransformAbilityRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * 创造丹：服用后一步登天。
 * 境界直升地仙·巅峰且修为灵力圆满，习得全部功法并登峰造极，
 * 领悟所有神通与心法，集齐九阶异火，五行灵根尽达极品并凝聚混沌灵根。
 */
public final class CreationPillItem extends BasePillItem {
    /** 最高大境界（地仙）与最高小阶段（巅峰）。 */
    private static final int MAX_REALM = 8;
    private static final int MAX_STAGE = 3;
    /** 熟练度上限：登峰造极。 */
    private static final int MAX_MASTERY = 100;
    /** 五行灵根全开时的面具与极品品质。 */
    private static final int FULL_ROOT_MASK = 31;
    private static final int MAX_ROOT_QUALITY = 4;
    private static final int MAX_FIRE_TIER = 9;

    public CreationPillItem(Properties properties) {
        super(30, 0, properties);
    }

    @Override
    protected boolean applyEffect(Player player, CultivationData data) {
        // 满配但灵力不足时，仍允许服用以补满灵力；只有确实没有任何收益时才拒绝。
        if (isFullyEnlightened(player, data) && data.wuxingxiantu$hasCreationBlessing()
                && data.wuxingxiantu$getCurrentMana() >= CultivationLogic.maxAura(data)) {
            player.displayClientMessage(
                    Component.translatable("message.wuxingxiantu.creation_pill_no_change"), true);
            return false;
        }

        // 境界：地仙·巅峰，修为圆满，灵力充盈。
        if(player instanceof net.minecraft.server.level.ServerPlayer server) {
            if(!cn.blockforge.generated.generatedmod.mindscape.MindscapeManager.repairInvasion(server)) {
                server.displayClientMessage(Component.translatable("message.wuxingxiantu.creation_pill_restore_pending"),true);
                return false;
            }
        }
        // This explicit creative repair is allowed to replace an invasion, unlike ordinary purification pills.
        data.wuxingxiantu$restoreDaoXin(1);
        data.wuxingxiantu$setDaoHeartBeforeInvasion(1);
        data.wuxingxiantu$setHeartDemonInvasionCause("");
        data.wuxingxiantu$getMindscapeData().putString("state","NONE");
        data.wuxingxiantu$setRealm(MAX_REALM);
        // 创造丹是直接圆满，不属于在下界完成渡劫，因此不会凭所在维度获得鬼仙身份。
        data.wuxingxiantu$setGhostImmortal(false);
        data.wuxingxiantu$setCultivationStage(MAX_STAGE);
        data.wuxingxiantu$setCultivation(CultivationLogic.breakthroughRequirement(data));
        CultivationLogic.restoreMana(data, CultivationLogic.maxAura(data));

        // 功法：全部习得且直接登峰造极（含全部天阶最高级功法）。
        for (ArtCatalog.Entry entry : ArtCatalog.ENTRIES) {
            data.wuxingxiantu$learnArt(entry.id());
            data.wuxingxiantu$setArtMastery(entry.id(), MAX_MASTERY);
        }

        // 神通：全部领悟，并把空出的轮盘外圈槽位直接填上。
        for (String id : DivineArtCatalog.ids()) data.wuxingxiantu$learnDivineArt(id);
        String[] learnedDivineArts = data.wuxingxiantu$getLearnedDivineArts();
        for (int slot = 0; slot < 3; slot++) {
            if (!data.wuxingxiantu$getEquippedDivineArt(slot).isBlank()) continue;
            for (String id : learnedDivineArts) {
                if (!isEquippedInAnySlot(id, slot, data)) {
                    data.wuxingxiantu$setEquippedDivineArt(slot, id);
                    break;
                }
            }
        }

        // 心法：全部解锁。
        for (String method : HeartMethodCatalog.METHOD_IDS) data.wuxingxiantu$learnHeartMethod(method);

        // 异火：一至九阶尽数收服。
        for (int tier = 1; tier <= MAX_FIRE_TIER; tier++) data.wuxingxiantu$learnFire(tier);

        // 创造丹同时圆满功德与阳神纯度；功德用 long 最大值表示可持久化的无限值。
        data.wuxingxiantu$setMerit(Long.MAX_VALUE);
        data.wuxingxiantu$setYinShenPurity(100);

        // 七十二变：一次性解锁全部原版可变形生灵，方便直接覆盖大多数原版玩法需求。
        for (ResourceLocation id : BuiltInRegistries.ENTITY_TYPE.keySet()) {
            if (id == null || !"minecraft".equals(id.getNamespace())
                    || id.equals(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.PLAYER))) continue;
            if (TransformAbilityRegistry.isVanillaFormId(id.toString())) data.wuxingxiantu$addPossessedForm(id.toString());
        }
        for(String form:cn.blockforge.generated.generatedmod.boss.combat.BossPlayerSkills.FORMS)data.wuxingxiantu$addPossessedForm(form);
        if (data.wuxingxiantu$getPossessedForms().length > 0
                && data.wuxingxiantu$getActivePossessedForm().isBlank()) {
            data.wuxingxiantu$setActivePossessedForm(data.wuxingxiantu$getPossessedForms()[0]);
        }

        // 灵根：五行齐聚且皆为极品，同步凝聚混沌灵根。
        data.wuxingxiantu$setRootMask(FULL_ROOT_MASK);
        for (int i = 0; i < 5; i++) data.wuxingxiantu$setRootQuality(i, MAX_ROOT_QUALITY);
        data.wuxingxiantu$setChaosRoot(true);
        data.wuxingxiantu$setThunderStrikes(CultivationLogic.THUNDER_STRIKES_REQUIRED);
        data.wuxingxiantu$setThunderRoot(true);
        data.wuxingxiantu$setExoticRoot(true);

        // 清除所有功法与神通的服务端冷却，并永久开启万法无冷却。
        data.wuxingxiantu$clearTechniqueCooldowns();
        data.wuxingxiantu$setCreationBlessing(true);
        // 境界与灵根带来的生命、攻击上限立即生效，并顺势痊愈。
        CultivationLogic.applyHealthBonus(player, data);
        if (player.getHealth() < player.getMaxHealth()) player.setHealth(player.getMaxHealth());

        player.displayClientMessage(Component.translatable("message.wuxingxiantu.creation_pill_realm",
                CultivationLogic.realmName(player, MAX_REALM)), false);
        player.displayClientMessage(Component.translatable("message.wuxingxiantu.creation_pill_arts"), false);
        player.displayClientMessage(Component.translatable("message.wuxingxiantu.creation_pill_divine"), false);
        player.displayClientMessage(Component.translatable("message.wuxingxiantu.creation_pill_heart"), false);
        player.displayClientMessage(Component.translatable("message.wuxingxiantu.creation_pill_fire"), false);
        player.displayClientMessage(Component.translatable("message.wuxingxiantu.creation_pill_root"), false);
        player.displayClientMessage(Component.translatable("message.wuxingxiantu.creation_pill_cooldowns"), false);
        player.displayClientMessage(Component.translatable("message.wuxingxiantu.creation_pill_merit_purity"), false);
        player.displayClientMessage(Component.translatable("message.wuxingxiantu.creation_pill_dao_heart"), false);
        return true;
    }

    @Override
    protected Component successMessage(Player player, CultivationData data) { return null; }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.wuxingxiantu.creation_pill"));
        tooltip.add(Component.translatable("tooltip.wuxingxiantu.creation_pill_2"));
        tooltip.add(Component.translatable("tooltip.wuxingxiantu.creation_pill_3"));
        tooltip.add(Component.translatable("message.wuxingxiantu.creation_pill_dao_heart"));
        tooltip.add(Component.translatable("tooltip.wuxingxiantu.creative_test_badge")
                .withStyle(net.minecraft.ChatFormatting.GRAY));
    }

    /** 判断是否已经全部圆满，避免对满配玩家白白消耗一枚神丹。 */
    private static boolean isFullyEnlightened(Player player, CultivationData data) {
        if(data.wuxingxiantu$getDaoXin()!=1||!data.wuxingxiantu$getHeartDemonInvasionCause().isBlank()
                ||!cn.blockforge.generated.generatedmod.mindscape.MindscapeState.read(data.wuxingxiantu$getMindscapeData().getString("state")).equals(cn.blockforge.generated.generatedmod.mindscape.MindscapeState.NONE)
                ||data.wuxingxiantu$getMindscapeData().hasUUID("session")
                ||data.wuxingxiantu$getMindscapeData().contains("pending_challenge"))return false;
        if (data.wuxingxiantu$getRealm() != MAX_REALM || data.wuxingxiantu$getCultivationStage() != MAX_STAGE) return false;
        if (data.wuxingxiantu$getCultivation() < CultivationLogic.breakthroughRequirement(data)) return false;
        for (ArtCatalog.Entry entry : ArtCatalog.ENTRIES) {
            if (!data.wuxingxiantu$hasLearnedArt(entry.id())) return false;
            if (data.wuxingxiantu$getArtMastery(entry.id()) < MAX_MASTERY) return false;
        }
        for (String id : DivineArtCatalog.ids()) if (!data.wuxingxiantu$hasLearnedDivineArt(id)) return false;
        for (String method : HeartMethodCatalog.METHOD_IDS) if (!data.wuxingxiantu$hasLearnedHeartMethod(method)) return false;
        for (int tier = 1; tier <= MAX_FIRE_TIER; tier++) if (!data.wuxingxiantu$hasLearnedFire(tier)) return false;
        if (data.wuxingxiantu$getRootMask() != FULL_ROOT_MASK || !data.wuxingxiantu$hasChaosRoot()) return false;
        if (!data.wuxingxiantu$hasThunderRoot() || !data.wuxingxiantu$hasExoticRoot()) return false;
        for (int i = 0; i < 5; i++) if (data.wuxingxiantu$getRootQuality(i) < MAX_ROOT_QUALITY) return false;
        if (data.wuxingxiantu$getMerit() != Long.MAX_VALUE
                || data.wuxingxiantu$getYinShenPurity() < 100) return false;
        if(!java.util.Arrays.asList(data.wuxingxiantu$getPossessedForms()).containsAll(cn.blockforge.generated.generatedmod.boss.combat.BossPlayerSkills.FORMS))return false;
        long now = player.level().getGameTime();
        for (String id : DivineArtCatalog.ids()) if (data.wuxingxiantu$getDivineCooldown(id) > now) return false;
        for (int slot = 0; slot < 8; slot++) if (data.wuxingxiantu$getArtCooldown(slot) > now) return false;
        return true;
    }

    /** 检查神通是否已占用其余槽位，保证三个槽位尽量铺开不同神通。 */
    private static boolean isEquippedInAnySlot(String id, int skipSlot, CultivationData data) {
        for (int slot = 0; slot < 3; slot++) {
            if (slot != skipSlot && id.equals(data.wuxingxiantu$getEquippedDivineArt(slot))) return true;
        }
        return false;
    }
}
