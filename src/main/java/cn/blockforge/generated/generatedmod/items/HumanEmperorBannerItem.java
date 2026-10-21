package cn.blockforge.generated.generatedmod.items;

import cn.blockforge.generated.generatedmod.HumanEmperorBannerMenu;
import cn.blockforge.generated.generatedmod.HeartDemonEntity;
import cn.blockforge.generated.generatedmod.SoulPearlData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.context.UseOnContext;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** 人皇幡：保存魂魄库存，并管理魂仆的放出、回收和引渡。 */
public final class HumanEmperorBannerItem extends SwordItem {
    private static final java.util.Set<Entity> SERVANT_INDEX =
            java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
    private static final String SOULS = "Souls";
    private static final String ENTITY_TYPE = "EntityType";
    private static final String COUNT = "Count";
    private static final String ACTIVE = "Active";
    private static final String DEAD = "Dead";
    private static final String DISPLAY_NAME = "DisplayName";
    private static final String BANNER_ID = "HumanEmperorBannerId";
    private static final String SERVANT_OWNER = "HumanEmperorBannerOwner";
    private static final String SERVANT_BANNER = "HumanEmperorBannerId";
    private static final String SERVANT_TYPE = "HumanEmperorBannerType";
    private static final String SOUL_POWER = "SoulPower";
    private static final String COMBAT_MODE = "CombatMode";
    private static final int SUMMON_AURA_COST = 10;
    private static final int MAX_SOUL_TYPES = 32;
    private static final int USE_DURATION = 72000;
    private static final int DAMAGE_INTERVAL = 10;
    private static final int VISUAL_INTERVAL = 4;
    private static final float BEAM_DAMAGE = 10.0F;
    private static final String SERVANT_TEAM = "wuxingxiantu_servants";
    /** 魂仆替主人吸引仇恨的有效半径：怪物原锁定玩家时，改锁定该范围内最近的魂仆。 */
    private static final double SERVANT_AGGRO_RANGE = 40.0D;
    /** 魂力上限固定 99999；耐久附魔不参与上限计算。魂力与血量 1:1 互换。 */
    public static final int MAX_SOUL_POWER = 99999;
    /** 复活一枚战死魂魄按“满血复活”消耗魂力，默认以 20 点生命值为基准。 */
    public static final int REVIVE_SOUL_COST = 20;
    /** 战斗姿态：0=自卫（仅玩家遇袭后反击），1=进攻（主动攻击敌对生物）。 */
    public static final int MODE_DEFENSE = 0;
    public static final int MODE_ATTACK = 1;

    public static final int ACTION_SUMMON = 0;
    public static final int ACTION_RECALL = 1;
    public static final int ACTION_ALL = 2;
    public static final int ACTION_MOVE_UP = 3;
    public static final int ACTION_MOVE_DOWN = 4;
    public static final int ACTION_TRANSFER = 5;
    public static final int ACTION_SET_ATTACK = 6;
    public static final int ACTION_SET_DEFENSE = 7;
    public static final int ACTION_TOGGLE_DEPLOY = 8;
    public static final int ACTION_TOGGLE_COMBAT = 9;
    /** 兼容旧客户端常量；旧版的魂仆安排入口仍映射到出战动作。 */
    public static final int ACTION_TOGGLE_MODE = ACTION_TOGGLE_COMBAT;

    /** 人皇幡的左键采用矛式标准近战：保留耐久、攻击冷却和命中反馈。 */
    public HumanEmperorBannerItem(Properties properties) {
        super(Tiers.NETHERITE, 8, -2.8F, properties);
    }

    /**
     * 使用时采用拉弓式动作：双臂水平前伸（右手小臂平行于地面），再由客户端的
     * HumanEmperorBannerPoseModel 逐帧补偿显示变换，使垂直的幡杆始终正立于手中。
     * 实际准星锁定和光束方向仍由 SoulCapturePearlItem.traceBeam 处理。
     */
    @Override public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.BOW; }

    private static void applyServantGlow(LivingEntity servant) {
        if (!(servant.level() instanceof ServerLevel level)) return;
        var scoreboard = level.getScoreboard();
        var team = scoreboard.getPlayerTeam(SERVANT_TEAM);
        if (team == null) {
            team = scoreboard.addPlayerTeam(SERVANT_TEAM);
            team.setDisplayName(Component.literal("Soul Servants"));
            team.setColor(ChatFormatting.DARK_PURPLE);
            team.setAllowFriendlyFire(false);
            team.setSeeFriendlyInvisibles(true);
        }
        scoreboard.addPlayerToTeam(servant.getStringUUID(), team);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        // 统一进度门槛：人皇幡允许持有、展示、放入背包，但捕魂/驭魂面板与
        // 右键魂光功能必须校验使用者本人的境界（赠送/交易不能绕过修炼进度）。
        if (!cn.blockforge.generated.generatedmod.ProgressionGate.canUseSoulSystem(
                player, cn.blockforge.generated.generatedmod.ProgressionGate.SOUL_BANNER)) {
            if (!level.isClientSide) {
                cn.blockforge.generated.generatedmod.ProgressionGate.deny(player,
                        cn.blockforge.generated.generatedmod.ProgressionGate.MSG_SOUL,
                        cn.blockforge.generated.generatedmod.ProgressionGate.requiredRealmForSoulFeature(
                                cn.blockforge.generated.generatedmod.ProgressionGate.SOUL_BANNER));
            }
            return InteractionResultHolder.fail(stack);
        }
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
                int inventorySlot = hand == InteractionHand.OFF_HAND ? 40 : player.getInventory().selected;
                openMenu(serverPlayer, inventorySlot);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (target == player) return InteractionResult.PASS;
        return use(player.level(), player, hand).getResult();
    }

    @Override public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        Level level = context.getLevel();
        // 潜行右键方块：将完整人皇幡插在点击面上；普通右键仍保留魂光/捕魂功能。
        if (player.isShiftKeyDown() && context.getClickedFace().getAxis().isVertical()) {
            net.minecraft.core.BlockPos standPos = context.getClickedPos().relative(context.getClickedFace());
            net.minecraft.world.level.block.state.BlockState target = level.getBlockState(standPos);
            if (!level.isEmptyBlock(standPos)
                    && !target.canBeReplaced(new net.minecraft.world.item.context.BlockPlaceContext(context))) {
                return InteractionResult.FAIL;
            }
            if (!level.isClientSide) {
                net.minecraft.core.BlockPos supportPos = standPos.below();
                net.minecraft.world.level.block.state.BlockState support = level.getBlockState(supportPos);
                if (!support.isFaceSturdy(level, supportPos, net.minecraft.core.Direction.UP)) return InteractionResult.FAIL;
                level.setBlock(standPos, cn.blockforge.generated.generatedmod.WuXingBlocks.HUMAN_EMPEROR_BANNER_STAND.get()
                        .defaultBlockState().setValue(cn.blockforge.generated.generatedmod.HumanEmperorBannerStandBlock.FACING,
                                player.getDirection().getOpposite()), 3);
                if (level.getBlockEntity(standPos) instanceof cn.blockforge.generated.generatedmod.HumanEmperorBannerStandBlockEntity stand) {
                    stand.setBanner(context.getItemInHand());
                    // 向上补齐隐形杆身格，使整根幡杆（而非仅底部座）可以右键交互。
                    cn.blockforge.generated.generatedmod.HumanEmperorBannerStandBlock.placePolesAbove(level, standPos);
                }
                if (!player.getAbilities().instabuild) context.getItemInHand().shrink(1);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return use(level, player, context.getHand()).getResult();
    }

    /** 打开指定背包槽位的人皇幡菜单，快捷键无需手持也能使用。 */
    public static void openMenu(ServerPlayer player, int inventorySlot) {
        if (inventorySlot < 0 || inventorySlot >= player.getInventory().getContainerSize()
                || !(player.getInventory().getItem(inventorySlot).getItem() instanceof HumanEmperorBannerItem)) return;
        ensureBannerId(player.getInventory().getItem(inventorySlot));
        net.minecraftforge.network.NetworkHooks.openScreen(player, new SimpleMenuProvider(
                (id, inventory, ignored) -> new HumanEmperorBannerMenu(id, inventory, inventorySlot),
                Component.translatable("screen.wuxingxiantu.human_emperor_banner")),
                buffer -> {
                    buffer.writeVarInt(inventorySlot);
                    buffer.writeBoolean(false);
                });
        if (player.containerMenu instanceof HumanEmperorBannerMenu menu) {
            cn.blockforge.generated.generatedmod.Network.sendBannerState(player, menu);
        }
    }

    public static int findBannerSlot(Player player) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            if (player.getInventory().getItem(slot).getItem() instanceof HumanEmperorBannerItem) return slot;
        }
        return -1;
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseTicks) {
        if (!(livingEntity instanceof ServerPlayer player) || !(level instanceof ServerLevel serverLevel)) return;
        if (remainingUseTicks % VISUAL_INTERVAL == 0) {
            SoulCapturePearlItem.emitBannerBeam(serverLevel, player, USE_DURATION - remainingUseTicks);
        }
        if (remainingUseTicks % DAMAGE_INTERVAL != 0) return;
        LivingEntity target = SoulCapturePearlItem.damageBeam(serverLevel, player, BEAM_DAMAGE);
        if (target != null && target.isDeadOrDying() && captureEntity(stack, target)) {
            player.displayClientMessage(Component.translatable(
                    "message.wuxingxiantu.banner_soul_captured", target.getDisplayName()), true);
        }
    }

    @Override public int getUseDuration(ItemStack stack) { return USE_DURATION; }

    /** 将人皇幡光线击杀的生物直接收容为幡内魂魄。 */
    private static boolean captureEntity(ItemStack banner, LivingEntity entity) {
        if (entity instanceof Player || isServant(entity)) return false;
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        if (id == null) return false;
        ensureBannerId(banner);
        CompoundTag tag = banner.getOrCreateTag();
        ListTag soulList = souls(tag);
        CompoundTag entry = findEntry(soulList, id.toString());
        if (entry == null) {
            if (soulList.size() >= MAX_SOUL_TYPES) return false;
            entry = new CompoundTag();
            entry.putString(ENTITY_TYPE, id.toString());
            entry.putInt(COUNT, 0);
            String displayName = displayNameFor(entity);
            if (!displayName.isEmpty()) entry.putString(DISPLAY_NAME, displayName);
            soulList.add(entry);
        }
        int count = Math.max(0, entry.getInt(COUNT));
        if (count >= 999) return false;
        entry.putInt(COUNT, count + 1);
        tag.put(SOULS, soulList);
        return true;
    }

    /** 归藏槽位提交的魂珠，服务端按槽位实际数量吸收；破碎魂珠写入战死状态。 */
    public static int absorbPearlStack(Player player, ItemStack banner, ItemStack pearl) {
        if (pearl.isEmpty() || !SoulPearlData.isSoulPearl(pearl)
                || !SoulPearlData.hasEntity(pearl)) return 0;
        String type = SoulPearlData.entityId(pearl);
        if (type.isEmpty()) return 0;
        ensureBannerId(banner);
        CompoundTag tag = banner.getOrCreateTag();
        ListTag soulList = souls(tag);
        CompoundTag entry = findEntry(soulList, type);
        if (entry == null) {
            if (soulList.size() >= MAX_SOUL_TYPES) return 0;
            entry = new CompoundTag();
            entry.putString(ENTITY_TYPE, type);
            entry.putInt(COUNT, 0);
            if (pearl.hasTag() && pearl.getTag().contains(DISPLAY_NAME, Tag.TAG_STRING)) {
                entry.putString(DISPLAY_NAME, pearl.getTag().getString(DISPLAY_NAME));
            }
            soulList.add(entry);
        }
        int room = 999 - Math.max(0, entry.getInt(COUNT));
        int amount = Math.min(pearl.getCount(), Math.max(0, room));
        if (amount <= 0) return 0;
        entry.putInt(COUNT, entry.getInt(COUNT) + amount);
        if (SoulPearlData.isBrokenSoulPearl(pearl)) {
            entry.putInt(DEAD, Math.min(999, Math.max(0, entry.getInt(DEAD)) + amount));
        }
        tag.put(SOULS, soulList);
        return amount;
    }

    /** 将万魂珠内的全部记录转移到人皇幡；万魂珠本身不会被消耗。 */
    public static int absorbMyriadStack(ItemStack banner, ItemStack myriad) {
        if (!SoulPearlData.isMyriad(myriad)) return 0;
        ensureBannerId(banner);
        CompoundTag bannerTag = banner.getOrCreateTag();
        ListTag bannerSouls = souls(bannerTag);
        List<SoulPearlData.StoredSoul> remaining = new ArrayList<>();
        int moved = 0;
        for (SoulPearlData.StoredSoul source : SoulPearlData.storedSouls(myriad)) {
            CompoundTag entry = findEntry(bannerSouls, source.entityType());
            if (entry == null) {
                if (bannerSouls.size() >= MAX_SOUL_TYPES) {
                    remaining.add(source);
                    continue;
                }
                entry = new CompoundTag();
                entry.putString(ENTITY_TYPE, source.entityType());
                entry.putInt(COUNT, 0);
                entry.putInt(ACTIVE, 0);
                entry.putInt(DEAD, 0);
                if (!source.displayName().isEmpty()) entry.putString(DISPLAY_NAME, source.displayName());
                bannerSouls.add(entry);
            }
            int current = Math.max(0, entry.getInt(COUNT));
            int room = Math.max(0, 999 - current);
            int amount = Math.min(room, source.count());
            if (amount <= 0) {
                remaining.add(source);
                continue;
            }
            int dead = Math.min(amount, source.dead());
            entry.putInt(COUNT, current + amount);
            entry.putInt(DEAD, Math.min(999, Math.max(0, entry.getInt(DEAD)) + dead));
            moved += amount;
            if (amount < source.count()) {
                remaining.add(new SoulPearlData.StoredSoul(source.entityType(), source.count() - amount,
                        source.dead() - dead, source.displayName()));
            }
        }
        bannerTag.put(SOULS, bannerSouls);
        SoulPearlData.writeStoredSouls(myriad, remaining);
        return moved;
    }

    /** 从人皇幡取出一枚魂魄并收入万魂珠，战死魂魄保留破碎状态。 */
    private static boolean absorbIntoMyriad(ItemStack banner, SoulEntry selected, ItemStack myriad) {
        if (selected == null || !SoulPearlData.isMyriad(myriad)) return false;
        CompoundTag tag = banner.getOrCreateTag();
        CompoundTag entry = findEntry(souls(tag), selected.entityType());
        if (entry == null) return false;
        boolean broken = entry.getInt(DEAD) > 0;
        if (!broken && available(entry) <= 0) return false;
        ItemStack pearl = SoulPearlData.createPearl(selected.entityType(), 0, broken, selected.displayName());
        if (!SoulPearlData.absorbIntoMyriad(myriad, pearl)) return false;
        entry.putInt(COUNT, Math.max(0, entry.getInt(COUNT) - 1));
        if (broken) entry.putInt(DEAD, Math.max(0, entry.getInt(DEAD) - 1));
        ListTag list = souls(tag);
        if (entry.getInt(COUNT) <= 0) list.remove(entry);
        tag.put(SOULS, list);
        return true;
    }

    /** 按选中条目的生物类型，从万魂珠释放一枚魂珠回人皇幡。 */
    private static boolean releaseFromMyriad(Player player, ItemStack banner, SoulEntry selected, ItemStack myriad) {
        if (selected == null || !SoulPearlData.isMyriad(myriad)
                || (!SoulPearlData.isMyriadReleaseMode(myriad) && !SoulPearlData.isCreativeMyriad(myriad))) return false;
        ItemStack pearl = SoulPearlData.extractFromMyriad(myriad, selected.entityType());
        if (pearl.isEmpty()) return false;
        int absorbed = absorbPearlStack(player, banner, pearl);
        if (absorbed == 1) return true;
        // 人皇幡已满或数据不合规时回滚，确保释放操作不会吞掉万魂珠内的魂魄。
        SoulPearlData.absorbIntoMyriad(myriad, pearl);
        return false;
    }

    /** 灭魂珠销毁一枚魂魄：战死、未出战、甚至正在跨维度游荡的出战魂仆都可强行抹灭。 */
    private static boolean destroySelected(ServerPlayer player, ItemStack banner, SoulEntry selected) {
        if (selected == null) return false;
        CompoundTag tag = banner.getOrCreateTag();
        ListTag list = souls(tag);
        CompoundTag entry = findEntry(list, selected.entityType());
        if (entry == null) return false;
        int dead = Math.max(0, entry.getInt(DEAD));
        int active = Math.max(0, entry.getInt(ACTIVE));
        int count = Math.max(0, entry.getInt(COUNT));
        if (dead > 0) {
            // 战死魂魄被灭魂珠彻底删除，不能只清除 Dead 标记后重新变成可召唤魂魄。
            entry.putInt(DEAD, dead - 1);
            entry.putInt(COUNT, Math.max(0, count - 1));
        } else if (count - active - dead > 0) entry.putInt(COUNT, count - 1);
        else if (active > 0) {
            discardOneServant(player, banner, selected.entityType());
            entry.putInt(ACTIVE, active - 1);
            entry.putInt(COUNT, Math.max(0, count - 1));
        } else return false;
        if (entry.getInt(COUNT) <= 0) list.remove(entry);
        tag.put(SOULS, list);
        return true;
    }

    /** 从全部维度强制抹灭一尊该类型出战魂仆（防止实体在远处失联导致魂魄无法销毁）。 */
    private static void discardOneServant(ServerPlayer player, ItemStack banner, String type) {
        CompoundTag tag = banner.getOrCreateTag();
        if (!tag.hasUUID(BANNER_ID)) return;
        UUID bannerId = tag.getUUID(BANNER_ID);
        outer:
        for (ServerLevel dimension : player.server.getAllLevels()) {
            for (Entity entity : cn.blockforge.generated.generatedmod.soul.ServantIndex.loaded(dimension)) {
                if (isOwnedServant(entity, player, bannerId)
                        && type.equals(entity.getPersistentData().getString(SERVANT_TYPE))) {
                    entity.discard();
                    break outer;
                }
            }
        }
    }

    /** 放入渡灵槽：魂能珠整体倾倒入幡魂力池；创造魂能珠执行自动复活。 */
    public static void onToolInserted(ServerPlayer player, ItemStack banner, ItemStack tool) {
        if (SoulPearlData.isSoulEnergy(tool) && !SoulPearlData.isCreativeSoulEnergy(tool)) {
            int stored = SoulPearlData.soulEnergy(tool);
            int added = pourSoulPower(banner, stored);
            if (added > 0) {
                SoulPearlData.setSoulEnergy(tool, stored - added);
                player.displayClientMessage(Component.translatable(
                        "message.wuxingxiantu.banner_soul_power_poured", added,
                        soulPower(banner), MAX_SOUL_POWER), true);
            }
        }
        if (SoulPearlData.isCreativeSoulEnergy(tool)) {
            int revived = reviveAllDead(banner);
            if (revived > 0) {
                player.displayClientMessage(Component.translatable(
                        "message.wuxingxiantu.soul_energy_auto_revived", revived), true);
            }
        }
    }

    /** 关闭界面前再次执行创造魂能珠的自动复活，避免未点击渡灵时遗漏。 */
    public static void onMenuClosed(ServerPlayer player, ItemStack banner, ItemStack tool) {
        if (!SoulPearlData.isCreativeSoulEnergy(tool)) return;
        int revived = reviveAllDead(banner);
        if (revived > 0) {
            player.displayClientMessage(Component.translatable(
                    "message.wuxingxiantu.soul_energy_auto_revived", revived), true);
        }
    }

    private static int reviveAllDead(ItemStack banner) {
        int revived = 0;
        CompoundTag tag = banner.getOrCreateTag();
        ListTag list = souls(tag);
        for (Tag raw : list) if (raw instanceof CompoundTag entry) {
            int dead = Math.max(0, entry.getInt(DEAD));
            if (dead > 0) {
                entry.putInt(DEAD, 0);
                revived += dead;
            }
        }
        tag.put(SOULS, list);
        return revived;
    }

    /** 复活一枚战死魂魄：普通魂能珠已并入幡魂力池，按 20 魂力 : 1 魂魄 从池中扣除。 */
    private static boolean reviveSelected(ItemStack banner, SoulEntry selected, ItemStack energyPearl) {
        if (selected == null || !SoulPearlData.isSoulEnergy(energyPearl)) return false;
        CompoundTag tag = banner.getOrCreateTag();
        CompoundTag entry = findEntry(souls(tag), selected.entityType());
        if (entry == null || entry.getInt(DEAD) <= 0) return false;
        if (!SoulPearlData.isCreativeSoulEnergy(energyPearl)) {
            if (!SoulPearlData.consumeSoulEnergy(energyPearl, 1) && !spendSoulPower(banner, REVIVE_SOUL_COST)) {
                return false;
            }
        }
        entry.putInt(DEAD, entry.getInt(DEAD) - 1);
        tag.put(SOULS, souls(tag));
        return true;
    }

    /** 兼容旧入口：将背包内完整魂珠批量归藏。 */
    public static int absorbSoulPearls(ServerPlayer player, ItemStack banner) {
        int absorbed = 0;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack pearl = player.getInventory().getItem(slot);
            int amount = absorbPearlStack(player, banner, pearl);
            if (amount <= 0) continue;
            pearl.shrink(amount);
            absorbed += amount;
        }
        if (absorbed > 0) player.getInventory().setChanged();
        return absorbed;
    }

    @Override public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.wuxingxiantu.human_emperor_banner"));
        int total = 0;
        for (SoulEntry soul : allSouls(stack)) {
            total += soul.count();
            EntityType<?> type = entityType(soul.entityType());
            tooltip.add(Component.translatable("tooltip.wuxingxiantu.banner_soul",
                    soulDisplayName(soul, type), soul.count()));
        }
        tooltip.add(Component.translatable("tooltip.wuxingxiantu.banner_total", total));
        tooltip.add(Component.translatable("tooltip.wuxingxiantu.banner_soul_power", soulPower(stack), MAX_SOUL_POWER));
        tooltip.add(Component.translatable("tooltip.wuxingxiantu.banner_mode"));
        super.appendHoverText(stack, level, tooltip, flag);
    }

    @Override public boolean isBarVisible(ItemStack stack) { return true; }
    @Override public int getBarWidth(ItemStack stack) {
        int total = allSouls(stack).stream().mapToInt(SoulEntry::count).sum();
        return Math.min(13, total * 13 / 32);
    }
    @Override public int getBarColor(ItemStack stack) { return 0x6B1B3A; }

    public static void ensureBannerId(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.hasUUID(BANNER_ID)) tag.putUUID(BANNER_ID, UUID.randomUUID());
    }

    public static boolean isServant(Entity entity) {
        if (entity == null) return false;
        return entity.getPersistentData().hasUUID(SERVANT_OWNER)
                && entity.getPersistentData().hasUUID(SERVANT_BANNER)
                && entity.getPersistentData().contains(SERVANT_TYPE, Tag.TAG_STRING);
    }

    public static void trackServant(Entity entity) { if (isServant(entity)) SERVANT_INDEX.add(entity); }
    public static void untrackServant(Entity entity) { SERVANT_INDEX.remove(entity); }

    /** 为攻击者挑选可替主人吸引仇恨的最近存活魂仆；仅在同一维度且在吸引半径内的魂仆，否则返回 null。 */
    public static LivingEntity findNearestServant(ServerPlayer owner, Mob attacker) {
        if (owner == null || attacker == null || attacker.level().isClientSide) return null;
        ServerLevel level = (ServerLevel) attacker.level();
        net.minecraft.world.phys.AABB box = attacker.getBoundingBox().inflate(SERVANT_AGGRO_RANGE);
        List<LivingEntity> candidates = level.getEntitiesOfClass(LivingEntity.class, box,
                candidate -> isServant(candidate) && candidate.isAlive()
                        && owner.getUUID().equals(candidate.getPersistentData().getUUID(SERVANT_OWNER)));
        LivingEntity nearest = null;
        double best = Double.MAX_VALUE;
        for (LivingEntity candidate : candidates) {
            double dist = candidate.distanceToSqr(attacker);
            if (dist < best) { best = dist; nearest = candidate; }
        }
        return nearest;
    }

    /** 魂仆死亡时只标记幡内条目，不生成掉落物；引渡时才会生成破碎魂珠。 */
    public static void onServantDeath(LivingEntity servant) {
        if (!isServant(servant) || servant.level().isClientSide) return;
        String type = servant.getPersistentData().getString(SERVANT_TYPE);
        UUID ownerId = servant.getPersistentData().getUUID(SERVANT_OWNER);
        UUID bannerId = servant.getPersistentData().getUUID(SERVANT_BANNER);
        if (servant.level() instanceof ServerLevel level) {
            ServerPlayer owner = level.getServer().getPlayerList().getPlayer(ownerId);
            if (owner != null) {
                ItemStack banner = findBanner(owner, bannerId);
                if (!banner.isEmpty()) {
                    markServantDead(banner, type);
                    if (owner.containerMenu instanceof HumanEmperorBannerMenu menu
                            && menu.banner() == banner) cn.blockforge.generated.generatedmod.Network.sendBannerState(owner, menu);
                }
                owner.displayClientMessage(Component.translatable(
                        "message.wuxingxiantu.banner_servant_death", servant.getDisplayName()), true);
            }
            level.sendParticles(ParticleTypes.SOUL, servant.getX(), servant.getY() + .6D, servant.getZ(),
                    16, .35D, .45D, .35D, .03D);
        }
    }

    private static ItemStack findBanner(ServerPlayer owner, UUID bannerId) {
        for (int i = 0; i < owner.getInventory().getContainerSize(); i++) {
            ItemStack candidate = owner.getInventory().getItem(i);
            if (candidate.getItem() instanceof HumanEmperorBannerItem && candidate.hasTag()
                    && candidate.getTag().hasUUID(BANNER_ID) && bannerId.equals(candidate.getTag().getUUID(BANNER_ID))) {
                return candidate;
            }
        }
        return ItemStack.EMPTY;
    }

    private static void markServantDead(ItemStack banner, String type) {
        CompoundTag tag = banner.getOrCreateTag();
        CompoundTag entry = findEntry(souls(tag), type);
        if (entry == null) return;
        entry.putInt(ACTIVE, Math.max(0, entry.getInt(ACTIVE) - 1));
        entry.putInt(DEAD, Math.min(999, entry.getInt(DEAD) + 1));
        tag.put(SOULS, souls(tag));
    }

    /** 每 tick 为魂仆提供跟随、战斗姿态、魂力治疗与粒子标记。 */
    public static void tickServants(net.minecraft.server.MinecraftServer server) {
        for (Entity entity : java.util.List.copyOf(SERVANT_INDEX)) {
                if (!entity.isAlive() || !(entity.level() instanceof ServerLevel level)
                        || entity.level().getServer() != server || !isServant(entity)) {
                    SERVANT_INDEX.remove(entity);
                    continue;
                }
                if (!isServant(entity) || !(entity instanceof LivingEntity living) || !living.isAlive()) continue;
                ServerPlayer owner = server.getPlayerList().getPlayer(living.getPersistentData().getUUID(SERVANT_OWNER));
                if (owner == null) continue;
                ItemStack banner = findBanner(owner, entity.getPersistentData().getUUID(SERVANT_BANNER));
                if (living instanceof Mob mob) {
                    int mode = banner.isEmpty() ? MODE_DEFENSE : combatMode(banner,
                            living.getPersistentData().getString(SERVANT_TYPE));
                    if (mode == MODE_DEFENSE) {
                        // 自卫姿态才跟随主人；进攻姿态下魂仆应留在战场持续索敌。
                        if (mob.distanceToSqr(owner) > 400.0D && living.level() == owner.level()) {
                            living.teleportTo(owner.getX() + 1.0D, owner.getY(), owner.getZ());
                            mob.setTarget(null);
                        } else if (!owner.isShiftKeyDown() && mob.getTarget() == null
                                && mob.distanceToSqr(owner) > 9.0D
                                && level.getGameTime()%5L==Math.floorMod(mob.getId(),5L)) {
                            mob.getNavigation().moveTo(owner, 1.15D);
                        }
                    } else if (mob.getTarget() == null) {
                        // 切换进攻姿态后清除上一姿态留下的回主人路径。
                        mob.getNavigation().stop();
                    }
                    if (mob.getTarget() == owner || mob.getTarget() != null && mob.getTarget().isAlliedTo(owner)) mob.setTarget(null);
                    if (mob.getTarget() != null && isServant(mob.getTarget())) mob.setTarget(null);
                    LivingEntity ownerTarget = owner.getLastHurtByMob() != null && owner.getLastHurtByMob().isAlive()
                            ? owner.getLastHurtByMob() : owner.getLastHurtMob();
                    if (ownerTarget == owner || ownerTarget != null && !ownerTarget.isAlive()) ownerTarget = null;
                    if (ownerTarget != null && ownerTarget.isAlliedTo(owner)) ownerTarget = null;
                    LivingEntity servantAttacker = mob.getLastHurtByMob() != null && mob.getLastHurtByMob().isAlive()
                            ? mob.getLastHurtByMob() : null;
                    if (mode == MODE_DEFENSE && mob.getTarget() != null && mob.getTarget() != ownerTarget
                            && mob.getTarget() != servantAttacker) {
                        // 自卫姿态：魂仆不保留任何自主目标，只对主人遇袭的目标反击；
                        // 但遭直接围攻时应反击眼前的攻击者，否则替主人吸引仇恨的魂仆只会挨打。
                        mob.setTarget(null);
                    }
                    if (mode == MODE_ATTACK && mob.getTarget() == null
                            && level.getGameTime() % 10L == Math.floorMod(mob.getId(), 10L)) {
                        // 进攻姿态：不跟随、不传送，主动寻找附近敌对生物。
                        net.minecraft.world.entity.monster.Monster prey =
                                level.getEntitiesOfClass(net.minecraft.world.entity.monster.Monster.class,
                                        mob.getBoundingBox().inflate(14.0D),
                                        candidate -> candidate != mob && !isServant(candidate)
                                                && candidate.isAlive() && !candidate.isInvulnerable()
                                                && !candidate.isAlliedTo(owner))
                                        .stream()
                                        .min(java.util.Comparator.comparingDouble(candidate -> candidate.distanceToSqr(mob)))
                                        .orElse(null);
                        if (prey != null) mob.setTarget(prey);
                    }
                    if (mode == MODE_DEFENSE && mob.getTarget() == null && ownerTarget != null
                            && !ownerTarget.isAlliedTo(owner) && mob.distanceToSqr(owner) < 48.0D * 48.0D) {
                        mob.setTarget(ownerTarget);
                    }
                }
                // 魂仆替主人吸引仇恨：周期性把附近正锁定主人（或将袭扰魂仆）的敌对生物重定向到魂仆，
                // 每次只处理一个仆从负责的一小撮敌人并按实体 ID 错峰，扩大吸引范围与力度。
                if (level.getGameTime() % 20L == Math.floorMod(living.getId(), 20L)) {
                    for (net.minecraft.world.entity.Mob enemy : level.getEntitiesOfClass(
                            net.minecraft.world.entity.Mob.class,
                            living.getBoundingBox().inflate(SERVANT_AGGRO_RANGE),
                            candidate -> candidate.isAlive() && candidate instanceof net.minecraft.world.entity.monster.Enemy
                                    && !isServant(candidate)
                                    && (candidate.getTarget() == owner || candidate.getTarget() == null))) {
                        enemy.setTarget(living);
                    }
                }
                if (!banner.isEmpty() && living.getHealth() <= living.getMaxHealth() - 1.0D
                        && level.getGameTime() % 20L == Math.floorMod(living.getId(),20L)) {
                    int stored = soulPower(banner);
                    if (stored > 0) {
                        living.heal(1.0F);
                        setSoulPower(banner, stored - 1);
                        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, living.getX(),
                                living.getY() + living.getBbHeight() * 0.7D, living.getZ(),
                                2, 0.3D, 0.3D, 0.3D, 0.02D);
                    }
                }
                applyServantGlow(living);
                if (level.getGameTime() % 5L == Math.floorMod(living.getId(),5L)) {
                    double particleY = living.getY() + living.getBbHeight() * .55D;
                    level.sendParticles(ParticleTypes.ENCHANT, living.getX(), particleY, living.getZ(),
                            5, living.getBbWidth() * .45D, living.getBbHeight() * .42D,
                            living.getBbWidth() * .45D, .035D);
                    level.sendParticles(ParticleTypes.SOUL, living.getX(), living.getY() + .20D, living.getZ(),
                            3, living.getBbWidth() * .28D, living.getBbHeight() * .20D,
                            living.getBbWidth() * .28D, .015D);
                }
        }
    }

    public static void handleMenuAction(ServerPlayer player, ItemStack banner, int action, int index) {
        if (!(banner.getItem() instanceof HumanEmperorBannerItem)) return;
        ensureBannerId(banner);
        List<SoulEntry> entries = allSouls(banner);
        HumanEmperorBannerMenu menu = player.containerMenu instanceof HumanEmperorBannerMenu value ? value : null;
        if (menu == null) return;
        ItemStack tool = menu.bannerContainer().getItem(HumanEmperorBannerMenu.PEARL_SLOT);
        boolean myriadTool = SoulPearlData.isMyriad(tool);
        if (myriadTool && action != ACTION_TRANSFER) return;
        SoulEntry selected = entryAt(entries, index);
        boolean selectedFromMyriad = false;
        if (action == ACTION_TRANSFER && selected == null && myriadTool) {
            selected = myriadEntryAt(tool, banner, index - entries.size());
            selectedFromMyriad = selected != null;
        }
        if (action == ACTION_SUMMON) {
            // 只有已归藏于人皇幡的魂魄才能出战，万魂珠独有条目必须先转入幡内。
            summon(player, banner, entryAt(entries, index));
        } else if (action == ACTION_RECALL) {
            int recalled = recallSelected(player.serverLevel(), player, banner, entryAt(entries, index));
            player.displayClientMessage(Component.translatable(
                    recalled > 0 ? "message.wuxingxiantu.banner_recalled" : "message.wuxingxiantu.banner_no_servants",
                    recalled), true);
        } else if (action == ACTION_ALL) {
            toggleAll(player, banner);
        } else if (action == ACTION_MOVE_UP || action == ACTION_MOVE_DOWN) {
            moveEntry(banner, index, action == ACTION_MOVE_UP ? -1 : 1);
        } else if (action == ACTION_TRANSFER) {
            transfer(player, banner, selected, selectedFromMyriad);
        } else if (action == ACTION_SET_ATTACK || action == ACTION_SET_DEFENSE) {
            if (selected == null) return;
            int mode = action == ACTION_SET_ATTACK ? MODE_ATTACK : MODE_DEFENSE;
            setCombatMode(banner, selected.entityType(), mode);
            player.displayClientMessage(Component.translatable("message.wuxingxiantu.banner_mode_set",
                    Component.translatable(mode == MODE_ATTACK
                            ? "gui.wuxingxiantu.banner_mode_attack" : "gui.wuxingxiantu.banner_mode_defense")), true);
        } else if (action == ACTION_TOGGLE_DEPLOY) {
            if (selected == null || selectedFromMyriad) return;
            if (selected.active() > 0) {
                int recalled = recallSelected(player.serverLevel(), player, banner, selected);
                if (recalled == 0) player.displayClientMessage(Component.translatable(
                        "message.wuxingxiantu.banner_no_servants"), true);
            } else {
                summon(player, banner, selected);
            }
        } else if (action == ACTION_TOGGLE_COMBAT) {
            if (selected == null || selectedFromMyriad) return;
            int next = combatMode(banner, selected.entityType()) == MODE_ATTACK ? MODE_DEFENSE : MODE_ATTACK;
            setCombatMode(banner, selected.entityType(), next);
            player.displayClientMessage(Component.translatable("message.wuxingxiantu.banner_mode_set",
                    Component.translatable(next == MODE_ATTACK
                            ? "gui.wuxingxiantu.banner_mode_attack" : "gui.wuxingxiantu.banner_mode_defense")), true);
        }
        player.containerMenu.broadcastChanges();
    }

    private static SoulEntry entryAt(List<SoulEntry> entries, int index) {
        return index >= 0 && index < entries.size() ? entries.get(index) : null;
    }

    private static SoulEntry myriadEntryAt(ItemStack myriad, ItemStack banner, int index) {
        if (!SoulPearlData.isMyriad(myriad)) return null;
        int visibleIndex = 0;
        for (SoulPearlData.StoredSoul soul : SoulPearlData.storedSouls(myriad)) {
            if (visibleIndex++ == index) {
                return new SoulEntry(soul.entityType(), soul.count(), 0, soul.dead(), soul.displayName());
            }
        }
        return null;
    }

    private static void moveEntry(ItemStack banner, int index, int delta) {
        CompoundTag tag = banner.getOrCreateTag();
        ListTag list = souls(tag);
        int target = index + delta;
        if (index < 0 || index >= list.size() || target < 0 || target >= list.size()) return;
        Tag current = list.get(index);
        list.set(index, list.get(target));
        list.set(target, current);
        tag.put(SOULS, list);
    }

    private static void summon(ServerPlayer player, ItemStack banner, SoulEntry selected) {
        if (selected == null) {
            player.displayClientMessage(Component.translatable("message.wuxingxiantu.banner_empty"), true);
            return;
        }
        summonOne(player, banner, selected.entityType());
    }

    private static void toggleAll(ServerPlayer player, ItemStack banner) {
        UUID bannerId = banner.getTag().getUUID(BANNER_ID);
        boolean hasServants = !countOwnedServants(player, bannerId).isEmpty();
        if (hasServants) recallServants(player.serverLevel(), player, banner);
        else {
            for (SoulEntry entry : allSouls(banner)) {
                while (available(banner, entry.entityType()) > 0
                        && countOwnedServants(player, bannerId).size() < cn.blockforge.generated.generatedmod.balance.GameplayConfig.SERVANT_LIMIT.get()) {
                    if (!summonOne(player, banner, entry.entityType())) break;
                }
            }
        }
    }

    private static boolean summonOne(ServerPlayer player, ItemStack banner, String type) {
        long active=SERVANT_INDEX.stream().filter(e->e.isAlive()&&!e.isRemoved()
                &&player.getUUID().equals(e.getPersistentData().getUUID(SERVANT_OWNER))).count();
        if(active>=cn.blockforge.generated.generatedmod.balance.GameplayConfig.SERVANT_LIMIT.get()){
            player.displayClientMessage(Component.translatable("message.wuxingxiantu.banner_servant_limit",
                    cn.blockforge.generated.generatedmod.balance.GameplayConfig.SERVANT_LIMIT.get()),true);return false;
        }
        var data = cn.blockforge.generated.generatedmod.GeneratedMod.data(player);
        if (data == null || data.wuxingxiantu$getAura() < SUMMON_AURA_COST) {
            player.displayClientMessage(Component.translatable("message.wuxingxiantu.banner_aura_missing", SUMMON_AURA_COST), true);
            return false;
        }
        CompoundTag tag = banner.getOrCreateTag();
        CompoundTag entry = findEntry(souls(tag), type);
        if (entry == null || available(entry) <= 0) {
            player.displayClientMessage(Component.translatable(entry != null && entry.getInt(DEAD) > 0
                    ? "message.wuxingxiantu.banner_dead_soul"
                    : "message.wuxingxiantu.banner_empty"), true);
            return false;
        }
        EntityType<?> entityType = entityType(type);
        if (entityType == null || !entityType.canSummon()) {
            player.displayClientMessage(Component.translatable("message.wuxingxiantu.banner_invalid_soul"), true);
            return false;
        }
        Entity created = entityType.create(player.serverLevel());
        if (!(created instanceof LivingEntity living)) return false;
        double angle = player.getYRot() * Math.PI / 180.0D;
        double x = player.getX() - Math.sin(angle) * 2.2D;
        double z = player.getZ() + Math.cos(angle) * 2.2D;
        living.moveTo(x, player.getY(), z, player.getYRot(), 0.0F);
        if (living instanceof HeartDemonEntity heartDemon) {
            // 心魔不能只按 EntityType 重新生成，释放时必须重新绑定主人才能恢复玩家状态。
            heartDemon.bindToServantOwner(player);
        }
        living.getPersistentData().putUUID(SERVANT_OWNER, player.getUUID());
        living.getPersistentData().putUUID(SERVANT_BANNER, tag.getUUID(BANNER_ID));
        living.getPersistentData().putString(SERVANT_TYPE, type);
        living.setGlowingTag(true);
        applyServantGlow(living);
        if (living instanceof Mob mob) mob.setPersistenceRequired();
        if (!player.serverLevel().addFreshEntity(living)) return false;
        data.wuxingxiantu$setAura(data.wuxingxiantu$getAura() - SUMMON_AURA_COST);
        entry.putInt(ACTIVE, entry.getInt(ACTIVE) + 1);
        tag.put(SOULS, souls(tag));
        player.serverLevel().sendParticles(ParticleTypes.SOUL, x, player.getY() + .8D, z, 20,
                .35D, .45D, .35D, .04D);
        player.displayClientMessage(Component.translatable("message.wuxingxiantu.banner_summoned", living.getType().getDescription()), true);
        cn.blockforge.generated.generatedmod.GeneratedMod.sync(player);
        return true;
    }

    private static int recallServants(ServerLevel level, ServerPlayer player, ItemStack banner) {
        return recallSelected(level, player, banner, null);
    }

    private static int recallSelected(ServerLevel level, ServerPlayer player, ItemStack banner, SoulEntry selected) {
        UUID bannerId = banner.getTag().getUUID(BANNER_ID);
        List<Entity> matches = new ArrayList<>();
        // 魂仆回收不限距离与维度：遍历服务端全部维度，确保跨维度出战的魂仆都能归幡。
        for (ServerLevel dimension : player.server.getAllLevels()) {
            for (Entity entity : cn.blockforge.generated.generatedmod.soul.ServantIndex.loaded(dimension)) {
                if (isOwnedServant(entity, player, bannerId)
                        && (selected == null || selected.entityType().equals(entity.getPersistentData().getString(SERVANT_TYPE)))) {
                    matches.add(entity);
                }
            }
        }
        for (Entity entity : matches) {
            decrementActive(banner, entity.getPersistentData().getString(SERVANT_TYPE));
            entity.discard();
        }
        if (!matches.isEmpty()) cn.blockforge.generated.generatedmod.GeneratedMod.sync(player);
        return matches.size();
    }

    private static boolean isOwnedServant(Entity entity, ServerPlayer player, UUID bannerId) {
        return isServant(entity) && player.getUUID().equals(entity.getPersistentData().getUUID(SERVANT_OWNER))
                && bannerId.equals(entity.getPersistentData().getUUID(SERVANT_BANNER));
    }

    private static void decrementActive(ItemStack banner, String type) {
        CompoundTag tag = banner.getOrCreateTag();
        CompoundTag entry = findEntry(souls(tag), type);
        if (entry == null) return;
        entry.putInt(ACTIVE, Math.max(0, entry.getInt(ACTIVE) - 1));
        tag.put(SOULS, souls(tag));
    }

    /** 跨维度统计这面人皇幡当前放出的魂仆。 */
    private static List<Entity> countOwnedServants(ServerPlayer player, UUID bannerId) {
        List<Entity> matches = new ArrayList<>();
        for (ServerLevel dimension : player.server.getAllLevels()) {
            for (Entity entity : cn.blockforge.generated.generatedmod.soul.ServantIndex.loaded(dimension)) {
                if (isOwnedServant(entity, player, bannerId)) matches.add(entity);
            }
        }
        return matches;
    }

    private static void transfer(ServerPlayer player, ItemStack banner, SoulEntry selected, boolean selectedFromMyriad) {
        if (selected == null) return;
        HumanEmperorBannerMenu menu = player.containerMenu instanceof HumanEmperorBannerMenu value ? value : null;
        if (menu == null) return;
        ItemStack tool = menu.bannerContainer().getItem(HumanEmperorBannerMenu.PEARL_SLOT);
        if (menu.isTransitReady()) {
            player.displayClientMessage(Component.translatable("message.wuxingxiantu.banner_slot_busy"), true);
            return;
        }
        // 灭魂珠拥有渡灵最高优先级：只要槽内是灭魂珠，渡灵即彻底销毁选中魂魄，
        // 出战中的记录也能强行销毁（连带跨维度收回实体），避免出现无法处理的卡死魂魄。
        if (SoulPearlData.isDestroyer(tool)) {
            if (destroySelected(player, banner, selected)) {
                player.displayClientMessage(Component.translatable("message.wuxingxiantu.banner_destroyed"), true);
                menu.setToolAfterAction(tool);
            } else {
                player.displayClientMessage(Component.translatable("message.wuxingxiantu.banner_destroy_failed"), true);
            }
            return;
        }
        if (SoulPearlData.isMyriad(tool)) {
            boolean release = SoulPearlData.isMyriadReleaseMode(tool);
            boolean changed = release
                    ? releaseFromMyriad(player, banner, selected, tool)
                    : absorbIntoMyriad(banner, selected, tool);
            if (changed) {
                player.displayClientMessage(Component.translatable(release
                        ? "message.wuxingxiantu.banner_myriad_released"
                        : "message.wuxingxiantu.banner_myriad_absorbed"), true);
                menu.setToolAfterAction(tool);
            } else {
                player.displayClientMessage(Component.translatable(release
                        ? "message.wuxingxiantu.banner_myriad_release_failed"
                        : "message.wuxingxiantu.banner_no_soul_to_transfer"), true);
            }
            return;
        }
        if (SoulPearlData.isSoulEnergy(tool)) {
            if (SoulPearlData.isCreativeSoulEnergy(tool)) {
                if (reviveSelected(banner, selected, tool)) {
                    player.displayClientMessage(Component.translatable(
                            "message.wuxingxiantu.banner_soul_revived",
                            soulPower(banner), MAX_SOUL_POWER), true);
                    menu.setToolAfterAction(tool);
                } else {
                    player.displayClientMessage(Component.translatable(
                            "message.wuxingxiantu.banner_revive_failed"), true);
                }
                return;
            }
            CompoundTag tag = banner.getOrCreateTag();
            CompoundTag entry = findEntry(souls(tag), selected.entityType());
            if (entry == null || entry.getInt(DEAD) <= 0) {
                player.displayClientMessage(Component.translatable(
                        "message.wuxingxiantu.banner_revive_failed"), true);
            } else if (SoulPearlData.soulEnergy(tool) <= 0 && soulPower(banner) < REVIVE_SOUL_COST) {
                player.displayClientMessage(Component.translatable(
                        "message.wuxingxiantu.banner_soul_power_missing", REVIVE_SOUL_COST), true);
            } else if (reviveSelected(banner, selected, tool)) {
                player.displayClientMessage(Component.translatable("message.wuxingxiantu.banner_soul_revived",
                        soulPower(banner), MAX_SOUL_POWER), true);
                menu.setToolAfterAction(tool);
            } else {
                player.displayClientMessage(Component.translatable(
                        "message.wuxingxiantu.banner_revive_failed"), true);
            }
            return;
        }
        if (!tool.isEmpty()) {
            player.displayClientMessage(Component.translatable("message.wuxingxiantu.banner_slot_busy"), true);
            return;
        }
        CompoundTag tag = banner.getOrCreateTag();
        CompoundTag entry = findEntry(souls(tag), selected.entityType());
        if (entry == null) return;
        boolean broken = entry.getInt(DEAD) > 0;
        int total = Math.max(0, entry.getInt(COUNT) - 1);
        if (broken) entry.putInt(DEAD, entry.getInt(DEAD) - 1);
        else if (available(entry) <= 0) return;
        entry.putInt(COUNT, total);
        ItemStack pearl = SoulPearlData.createPearl(selected.entityType(), 0, broken,
                entry.contains(DISPLAY_NAME, Tag.TAG_STRING) ? entry.getString(DISPLAY_NAME) : "");
        if (!menu.prepareTransitPearl(pearl)) return;
        if (entry.getInt(COUNT) <= 0) souls(tag).remove(entry);
        tag.put(SOULS, souls(tag));
        player.displayClientMessage(Component.translatable(
                broken ? "message.wuxingxiantu.banner_transfer_broken" : "message.wuxingxiantu.banner_transfer_done"), true);
    }

    /** 魂力值：0~99999，新幡默认满值；与血量 1:1，耐久附魔不会提升上限。 */
    public static int soulPower(ItemStack banner) {
        if (!(banner.getItem() instanceof HumanEmperorBannerItem)) return 0;
        CompoundTag tag = banner.getOrCreateTag();
        if (!tag.contains(SOUL_POWER)) {
            tag.putInt(SOUL_POWER, MAX_SOUL_POWER);
            return MAX_SOUL_POWER;
        }
        return Math.max(0, Math.min(MAX_SOUL_POWER, tag.getInt(SOUL_POWER)));
    }

    public static void setSoulPower(ItemStack banner, int value) {
        banner.getOrCreateTag().putInt(SOUL_POWER, Math.max(0, Math.min(MAX_SOUL_POWER, value)));
    }

    /** 向魂力池注入魂力，返回实际注入量（超出上限部分保留在来源中）。 */
    public static int pourSoulPower(ItemStack banner, int amount) {
        int current = soulPower(banner);
        int added = Math.max(0, Math.min(amount, MAX_SOUL_POWER - current));
        if (added > 0) setSoulPower(banner, current + added);
        return added;
    }

    public static boolean spendSoulPower(ItemStack banner, int amount) {
        int current = soulPower(banner);
        if (current < amount) return false;
        setSoulPower(banner, current - amount);
        return true;
    }

    /** 兼容旧存档的全局姿态；新条目会单独保存每种魂魄的状态。 */
    public static int combatMode(ItemStack banner) {
        return banner.hasTag() && banner.getTag().getInt(COMBAT_MODE) == MODE_ATTACK ? MODE_ATTACK : MODE_DEFENSE;
    }

    /** 返回选中魂魄的状态，旧条目没有字段时沿用幡的旧全局状态。 */
    public static int combatMode(ItemStack banner, String type) {
        CompoundTag entry = banner.hasTag() ? findEntry(souls(banner.getTag()), type) : null;
        if (entry != null && entry.contains(COMBAT_MODE, Tag.TAG_INT)) {
            return entry.getInt(COMBAT_MODE) == MODE_ATTACK ? MODE_ATTACK : MODE_DEFENSE;
        }
        return combatMode(banner);
    }

    public static void setCombatMode(ItemStack banner, String type, int mode) {
        CompoundTag entry = findEntry(souls(banner.getOrCreateTag()), type);
        if (entry == null) return;
        entry.putInt(COMBAT_MODE, mode == MODE_ATTACK ? MODE_ATTACK : MODE_DEFENSE);
        banner.getOrCreateTag().put(SOULS, souls(banner.getOrCreateTag()));
    }

    public static int toggleCombatMode(ItemStack banner) {
        int next = combatMode(banner) == MODE_DEFENSE ? MODE_ATTACK : MODE_DEFENSE;
        banner.getOrCreateTag().putInt(COMBAT_MODE, next);
        return next;
    }

    /** 龙蛋放入人皇幡渡灵槽：汲取残存龙魂，获得一枚可召唤的末影龙魂魄。 */
    public static int absorbDragonEgg(ItemStack banner, ItemStack input) {
        if (!(banner.getItem() instanceof HumanEmperorBannerItem)) return 0;
        if (!input.is(net.minecraft.world.item.Items.DRAGON_EGG)) return 0;
        CompoundTag tag = banner.getOrCreateTag();
        ListTag list = souls(tag);
        String type = "minecraft:ender_dragon";
        CompoundTag entry = findEntry(list, type);
        if (entry == null) {
            if (list.size() >= MAX_SOUL_TYPES) return 0;
            entry = new CompoundTag();
            entry.putString(ENTITY_TYPE, type);
            entry.putString(DISPLAY_NAME, "末影龙");
            list.add(entry);
        }
        entry.putInt(COUNT, Math.min(999, entry.getInt(COUNT) + 1));
        tag.put(SOULS, list);
        return 1;
    }

    public static HumanEmperorBannerMenu.BannerState bannerState(ItemStack stack, Level level) {
        return bannerState(stack, level, ItemStack.EMPTY);
    }

    /** 分开返回人皇幡与万魂珠列表，避免万魂珠魂魄被误认为可出战魂仆。 */
    public static HumanEmperorBannerMenu.BannerState bannerState(ItemStack stack, Level level, ItemStack transferTool) {
        List<SoulView> bannerSouls = new ArrayList<>();
        for (SoulEntry entry : allSouls(stack)) {
            bannerSouls.add(new SoulView(entry.entityType(), entry.count(), entry.active(), entry.dead(), entry.displayName(),
                    combatMode(stack, entry.entityType())));
        }
        List<SoulView> myriadSouls = new ArrayList<>();
        if (SoulPearlData.isMyriad(transferTool)) {
            for (SoulPearlData.StoredSoul stored : SoulPearlData.storedSouls(transferTool)) {
                myriadSouls.add(new SoulView(stored.entityType(), stored.count(), 0, stored.dead(), stored.displayName(),
                        MODE_DEFENSE));
            }
        }
        return new HumanEmperorBannerMenu.BannerState(bannerSouls, myriadSouls, soulPower(stack), combatMode(stack));
    }

    public static HumanEmperorBannerMenu.BannerState bannerState(ItemStack stack) {
        return bannerState(stack, null, ItemStack.EMPTY);
    }

    private static ListTag souls(CompoundTag tag) {
        return tag.contains(SOULS, Tag.TAG_LIST) ? tag.getList(SOULS, Tag.TAG_COMPOUND) : new ListTag();
    }

    private static List<SoulEntry> allSouls(ItemStack stack) {
        List<SoulEntry> result = new ArrayList<>();
        if (!stack.hasTag()) return result;
        for (Tag raw : souls(stack.getTag())) if (raw instanceof CompoundTag entry) {
            int count = Math.max(0, entry.getInt(COUNT));
            int active = Math.max(0, entry.getInt(ACTIVE));
            int dead = Math.max(0, entry.getInt(DEAD));
            String type = entry.getString(ENTITY_TYPE);
            if (count > 0 && !type.isEmpty()) result.add(new SoulEntry(type, count, active, dead,
                    entry.contains(DISPLAY_NAME, Tag.TAG_STRING) ? entry.getString(DISPLAY_NAME) : ""));
        }
        return result;
    }

    private static CompoundTag findEntry(ListTag souls, String type) {
        for (Tag raw : souls) if (raw instanceof CompoundTag entry && type.equals(entry.getString(ENTITY_TYPE))) return entry;
        return null;
    }

    private static int available(CompoundTag entry) {
        return Math.max(0, entry.getInt(COUNT) - entry.getInt(ACTIVE) - entry.getInt(DEAD));
    }

    private static int available(ItemStack stack, String type) {
        CompoundTag entry = findEntry(souls(stack.getOrCreateTag()), type);
        return entry == null ? 0 : available(entry);
    }

    private static EntityType<?> entityType(String id) {
        ResourceLocation key = ResourceLocation.tryParse(id);
        return key == null ? null : net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.get(key);
    }

    private static Component soulDisplayName(SoulEntry soul, EntityType<?> type) {
        if ("wuxingxiantu:heart_demon".equals(soul.entityType()) && !soul.displayName().isEmpty()) {
            return Component.translatable("entity.wuxingxiantu.heart_demon",
                    Component.literal(soul.displayName()));
        }
        return type == null ? Component.literal(soul.entityType()) : type.getDescription();
    }

    private static String displayNameFor(LivingEntity entity) {
        if (entity instanceof HeartDemonEntity heartDemon && !heartDemon.ownerName().isEmpty()) {
            return heartDemon.ownerName();
        }
        return entity.getDisplayName().getString();
    }

    public record SoulView(String entityType, int count, int active, int dead, String displayName, int combatMode) {
        public SoulView {
            displayName = displayName == null ? "" : displayName;
            combatMode = combatMode == MODE_ATTACK ? MODE_ATTACK : MODE_DEFENSE;
        }
    }
    private record SoulEntry(String entityType, int count, int active, int dead, String displayName) {
        int available() { return Math.max(0, count - active - dead); }
    }
}
