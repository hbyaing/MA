package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.items.HumanEmperorBannerItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import cn.blockforge.generated.generatedmod.HeartDemonEntity;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** 两种灵珠的服务端 NBT 数据和交易处理。 */
public final class SoulPearlData {
    private static final String OFFERS = "CapturedOffers";
    private static final String ENTITY_TYPE = "CapturedEntityType";
    private static final String EXPERIENCE_REWARD = "CapturedExperienceReward";
    private static final String REFINE_LEVEL = "RefineLevel";
    private static final String FAVORITE_OFFERS = "FavoriteOffers";
    private static final String PEARL_ID = "SoulPearlId";
    private static final String MYRIAD_SOULS = "Souls";
    private static final String SOUL_COUNT = "Count";
    private static final String SOUL_DEAD = "Dead";
    private static final String SOUL_ENTITY_TYPE = "EntityType";
    private static final String SOUL_DISPLAY_NAME = "DisplayName";
    private static final String SOUL_ENERGY = "SoulEnergy";
    private static final String MYRIAD_RELEASE_MODE = "MyriadReleaseMode";
    public static final int MAX_SOUL_ENERGY = 100;

    private SoulPearlData() {}

    public static boolean isBinding(ItemStack stack) {
        return stack.is(WuXingItems.SOUL_BINDING_PEARL.get());
    }

    public static boolean isCapture(ItemStack stack) {
        return stack.is(WuXingItems.SOUL_CAPTURE_PEARL.get());
    }

    public static boolean isMyriad(ItemStack stack) {
        return stack.is(WuXingItems.MYRIAD_SOUL_PEARL.get())
                || stack.is(WuXingItems.CREATIVE_MYRIAD_SOUL_PEARL.get());
    }

    public static boolean isCreativeMyriad(ItemStack stack) {
        return stack.is(WuXingItems.CREATIVE_MYRIAD_SOUL_PEARL.get());
    }

    /** 万魂珠默认处于吸收模式；右键切换后，渡灵按钮会从珠中释放对应魂魄。 */
    public static boolean isMyriadReleaseMode(ItemStack stack) {
        return isMyriad(stack) && stack.hasTag() && stack.getTag().getBoolean(MYRIAD_RELEASE_MODE);
    }

    public static boolean toggleMyriadMode(ItemStack stack) {
        if (!isMyriad(stack)) return false;
        boolean release = !isMyriadReleaseMode(stack);
        stack.getOrCreateTag().putBoolean(MYRIAD_RELEASE_MODE, release);
        return release;
    }

    public static void setMyriadReleaseMode(ItemStack stack, boolean release) {
        if (isMyriad(stack)) stack.getOrCreateTag().putBoolean(MYRIAD_RELEASE_MODE, release);
    }

    public static boolean isDestroyer(ItemStack stack) {
        return stack.is(WuXingItems.DESTROY_SOUL_PEARL.get());
    }

    public static boolean isSoulEnergy(ItemStack stack) {
        return stack.is(WuXingItems.SOUL_ENERGY_PEARL.get())
                || stack.is(WuXingItems.CREATIVE_SOUL_ENERGY_PEARL.get());
    }

    public static boolean isCreativeSoulEnergy(ItemStack stack) {
        return stack.is(WuXingItems.CREATIVE_SOUL_ENERGY_PEARL.get());
    }

    public static int soulEnergy(ItemStack stack) {
        if (!isSoulEnergy(stack)) return 0;
        return isCreativeSoulEnergy(stack) ? MAX_SOUL_ENERGY : Math.max(0,
                Math.min(MAX_SOUL_ENERGY, stack.getOrCreateTag().getInt(SOUL_ENERGY)));
    }

    public static void setSoulEnergy(ItemStack stack, int value) {
        if (!isSoulEnergy(stack) || isCreativeSoulEnergy(stack)) return;
        stack.getOrCreateTag().putInt(SOUL_ENERGY, Math.max(0, Math.min(MAX_SOUL_ENERGY, value)));
    }

    public static boolean consumeSoulEnergy(ItemStack stack, int amount) {
        if (!isSoulEnergy(stack) || amount < 0) return false;
        if (isCreativeSoulEnergy(stack)) return true;
        int current = soulEnergy(stack);
        if (current < amount) return false;
        setSoulEnergy(stack, current - amount);
        return true;
    }

    /** 人皇幡使用的独立魂珠；不会再把摄魄珠误当作幡内魂魄。 */
    public static boolean isSoulPearl(ItemStack stack) {
        return stack.is(WuXingItems.SOUL_PEARL.get()) || stack.is(WuXingItems.BROKEN_SOUL_PEARL.get());
    }

    public static boolean isBrokenSoulPearl(ItemStack stack) {
        return stack.is(WuXingItems.BROKEN_SOUL_PEARL.get());
    }

    /** 摄魄珠捕获生物后，与破碎魂珠一样需要放入愈灵室修复。 */
    public static boolean isRepairableSoulPearl(ItemStack stack) {
        return hasEntity(stack) && (isBrokenSoulPearl(stack) || isCapture(stack));
    }

    /** 为拘魂珠分配稳定标识，确保交易期间移动物品后仍能找到原珠。 */
    public static UUID ensurePearlId(ItemStack pearl) {
        CompoundTag tag = pearl.getOrCreateTag();
        if (!tag.hasUUID(PEARL_ID)) tag.putUUID(PEARL_ID, UUID.randomUUID());
        return tag.getUUID(PEARL_ID);
    }

    public static boolean hasPearlId(ItemStack pearl, UUID pearlId) {
        return isBinding(pearl) && pearlId != null && pearl.hasTag()
                && pearl.getTag().hasUUID(PEARL_ID)
                && pearlId.equals(pearl.getTag().getUUID(PEARL_ID));
    }

    public static boolean hasOffers(ItemStack stack) {
        return stack.hasTag() && stack.getTag().contains(OFFERS, Tag.TAG_COMPOUND);
    }

    public static boolean hasEntity(ItemStack stack) {
        return stack.hasTag() && stack.getTag().contains(ENTITY_TYPE, Tag.TAG_STRING);
    }

    /**
     * 将一个村民的交易合并进已有灵魂集合。交易数量不限，同一商品只保留总成本最低的一条。
     */
    public static void captureVillager(ItemStack pearl, AbstractVillager villager) {
        Map<String, MerchantOffer> cheapest = new LinkedHashMap<>();
        if (hasOffers(pearl)) {
            for (MerchantOffer offer : offers(pearl)) {
                mergeCheapest(cheapest, copyInfinite(offer));
            }
        }
        for (MerchantOffer offer : villager.getOffers()) {
            mergeCheapest(cheapest, copyInfinite(offer));
        }
        MerchantOffers combined = new MerchantOffers();
        combined.addAll(cheapest.values());
        pearl.getOrCreateTag().put(OFFERS, combined.createTag());
    }

    private static void mergeCheapest(Map<String, MerchantOffer> cheapest, MerchantOffer candidate) {
        String key = resultKey(candidate.getResult());
        MerchantOffer previous = cheapest.get(key);
        if (previous == null || price(candidate) < price(previous)) cheapest.put(key, candidate);
    }

    private static MerchantOffer copyInfinite(MerchantOffer offer) {
        return new MerchantOffer(offer.getBaseCostA().copy(), offer.getCostB().copy(), offer.getResult().copy(),
                0, Integer.MAX_VALUE, offer.getXp(), 0.0F, 0);
    }

    /** 商品数量不是商品种类的一部分，因此去掉保存标签中的 Count 后再比较。 */
    private static String resultKey(ItemStack stack) {
        CompoundTag tag = new CompoundTag();
        stack.save(tag);
        tag.remove("Count");
        return tag.toString();
    }

    private static int price(MerchantOffer offer) {
        return offer.getBaseCostA().getCount() + offer.getCostB().getCount();
    }

    /** 读取交易并将收藏项稳定地排在最前；同组内保留原始顺序。 */
    public static MerchantOffers offers(ItemStack pearl) {
        MerchantOffers offers = hasOffers(pearl)
                ? new MerchantOffers(pearl.getTag().getCompound(OFFERS)) : new MerchantOffers();
        Set<String> favorites = favoriteKeys(pearl);
        offers.sort(Comparator.comparing((MerchantOffer offer) -> !favorites.contains(offerKey(offer))));
        return offers;
    }

    public static String offerKey(MerchantOffer offer) {
        return resultKey(offer.getResult());
    }

    public static boolean isFavorite(ItemStack pearl, MerchantOffer offer) {
        return favoriteKeys(pearl).contains(offerKey(offer));
    }

    /** 切换一条交易的收藏状态并持久化到拘魂珠。 */
    public static boolean toggleFavorite(ItemStack pearl, MerchantOffer offer) {
        if (!isBinding(pearl) || !hasOffers(pearl)) return false;
        Set<String> favorites = favoriteKeys(pearl);
        String key = offerKey(offer);
        boolean nowFavorite;
        if (favorites.remove(key)) nowFavorite = false;
        else {
            favorites.add(key);
            nowFavorite = true;
        }
        writeFavoriteKeys(pearl, favorites);
        return nowFavorite;
    }

    public static int favoriteCount(ItemStack pearl) {
        if (!hasOffers(pearl)) return 0;
        Set<String> favorites = favoriteKeys(pearl);
        int count = 0;
        for (MerchantOffer offer : new MerchantOffers(pearl.getTag().getCompound(OFFERS))) {
            if (favorites.contains(offerKey(offer))) count++;
        }
        return count;
    }

    private static Set<String> favoriteKeys(ItemStack pearl) {
        Set<String> favorites = new LinkedHashSet<>();
        if (!pearl.hasTag() || !pearl.getTag().contains(FAVORITE_OFFERS, Tag.TAG_LIST)) return favorites;
        ListTag list = pearl.getTag().getList(FAVORITE_OFFERS, Tag.TAG_STRING);
        for (int i = 0; i < list.size(); i++) favorites.add(list.getString(i));
        return favorites;
    }

    private static void writeFavoriteKeys(ItemStack pearl, Set<String> favorites) {
        ListTag list = new ListTag();
        favorites.forEach(key -> list.add(StringTag.valueOf(key)));
        pearl.getOrCreateTag().put(FAVORITE_OFFERS, list);
    }

    public static int refineBinding(ItemStack pearl) {
        if (!hasOffers(pearl)) return 0;
        CompoundTag tag = pearl.getOrCreateTag();
        int previousLevel = Math.max(0, tag.getInt(REFINE_LEVEL));
        int level = previousLevel == Integer.MAX_VALUE ? Integer.MAX_VALUE : previousLevel + 1;
        MerchantOffers refined = new MerchantOffers();
        for (MerchantOffer offer : offers(pearl)) {
            ItemStack a = offer.getBaseCostA().copy();
            ItemStack b = offer.getCostB().copy();
            a.setCount(Math.max(1, a.getCount() - 1));
            if (!b.isEmpty()) b.setCount(Math.max(1, b.getCount() - 1));
            refined.add(new MerchantOffer(a, b, offer.getResult().copy(), 0, Integer.MAX_VALUE,
                    offer.getXp(), 0.0F, 0));
        }
        tag.put(OFFERS, refined.createTag());
        tag.putInt(REFINE_LEVEL, level);
        return Math.max(1, level);
    }

    public static int refineLevel(ItemStack pearl) {
        return pearl.hasTag() ? Math.max(0, pearl.getTag().getInt(REFINE_LEVEL)) : 0;
    }

    /** 拘魂珠可无限次重复烧炼；价格降到一枚后仍可继续完成设定轮次。 */
    public static boolean canRefine(ItemStack pearl) {
        return hasOffers(pearl);
    }

    public static int remainingRefinements(ItemStack pearl) {
        return hasOffers(pearl) ? Integer.MAX_VALUE : 0;
    }

    public static void recordEntity(ItemStack pearl, LivingEntity entity) {
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        if (id == null) return;
        CompoundTag tag = pearl.getOrCreateTag();
        tag.putString(ENTITY_TYPE, id.toString());
        // 在捕获时固化经验值，之后每次化魄同类魄灵都得到相同数量，不受随机掉落影响。
        tag.putInt(EXPERIENCE_REWARD, Math.max(0, entity.getExperienceReward()));
        String displayName = displayNameFor(entity);
        if (!displayName.isEmpty()) tag.putString(SOUL_DISPLAY_NAME, displayName);
    }

    /** 创造万魂珠直接收服生物，不生成中间魂珠，也不限制容量。 */
    public static boolean captureEntityIntoMyriad(ItemStack myriad, LivingEntity entity) {
        if (!isCreativeMyriad(myriad) || entity == null || entity instanceof net.minecraft.world.entity.player.Player
                || HumanEmperorBannerItem.isServant(entity)) return false;
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        if (id == null) return false;
        CompoundTag tag = myriad.getOrCreateTag();
        ListTag list = tag.contains(MYRIAD_SOULS, Tag.TAG_LIST)
                ? tag.getList(MYRIAD_SOULS, Tag.TAG_COMPOUND) : new ListTag();
        String type = id.toString();
        CompoundTag entry = null;
        for (Tag raw : list) if (raw instanceof CompoundTag candidate
                && type.equals(candidate.getString(SOUL_ENTITY_TYPE))) {
            entry = candidate;
            break;
        }
        if (entry == null) {
            entry = new CompoundTag();
            entry.putString(SOUL_ENTITY_TYPE, type);
            entry.putInt(SOUL_COUNT, 0);
            entry.putInt(SOUL_DEAD, 0);
            list.add(entry);
        }
        entry.putInt(SOUL_COUNT, Math.max(0, entry.getInt(SOUL_COUNT)) + 1);
        String displayName = displayNameFor(entity);
        if (!displayName.isEmpty()) entry.putString(SOUL_DISPLAY_NAME, displayName);
        tag.put(MYRIAD_SOULS, list);
        return true;
    }

    /** 保存用于人皇幡列表的纯文本名称；心魔必须使用绑定玩家的档案名。 */
    private static String displayNameFor(LivingEntity entity) {
        if (entity instanceof HeartDemonEntity heartDemon && !heartDemon.ownerName().isEmpty()) {
            return heartDemon.ownerName();
        }
        return entity.getDisplayName().getString();
    }

    public static int experienceReward(ItemStack pearl) {
        return hasEntity(pearl) && pearl.getTag().contains(EXPERIENCE_REWARD, Tag.TAG_INT)
                ? Math.max(0, pearl.getTag().getInt(EXPERIENCE_REWARD)) : -1;
    }

    public static EntityType<?> entityType(ItemStack pearl) {
        if (!hasEntity(pearl)) return null;
        ResourceLocation id = ResourceLocation.tryParse(pearl.getTag().getString(ENTITY_TYPE));
        return id == null ? null : BuiltInRegistries.ENTITY_TYPE.get(id);
    }

    public static String entityId(ItemStack pearl) {
        return hasEntity(pearl) ? pearl.getTag().getString(ENTITY_TYPE) : "";
    }

    public static boolean isBroken(ItemStack pearl) {
        return pearl.hasTag() && pearl.getTag().getBoolean("IsBroken");
    }

    /** 万魂珠内部的魂魄条目，使用与人皇幡相同的 Count/Dead 结构。 */
    public record StoredSoul(String entityType, int count, int dead, String displayName) {}

    public static java.util.List<StoredSoul> storedSouls(ItemStack pearl) {
        java.util.List<StoredSoul> result = new java.util.ArrayList<>();
        if (!isMyriad(pearl) || !pearl.hasTag() || !pearl.getTag().contains(MYRIAD_SOULS, Tag.TAG_LIST)) return result;
        for (Tag raw : pearl.getTag().getList(MYRIAD_SOULS, Tag.TAG_COMPOUND)) {
            if (!(raw instanceof CompoundTag entry)) continue;
            String type = entry.getString(SOUL_ENTITY_TYPE);
            int count = Math.max(0, entry.getInt(SOUL_COUNT));
            int dead = Math.min(count, Math.max(0, entry.getInt(SOUL_DEAD)));
            if (!type.isEmpty() && count > 0) {
                result.add(new StoredSoul(type, count, dead,
                        entry.contains(SOUL_DISPLAY_NAME, Tag.TAG_STRING) ? entry.getString(SOUL_DISPLAY_NAME) : ""));
            }
        }
        return result;
    }

    public static int storedSoulTotal(ItemStack pearl) {
        return storedSouls(pearl).stream().mapToInt(StoredSoul::count).sum();
    }

    /** 万魂珠内破碎魂魄数量；其余魂魄均为正常魂魄。 */
    public static int storedBrokenSoulTotal(ItemStack pearl) {
        return storedSouls(pearl).stream().mapToInt(StoredSoul::dead).sum();
    }

    public static int storedIntactSoulTotal(ItemStack pearl) {
        return Math.max(0, storedSoulTotal(pearl) - storedBrokenSoulTotal(pearl));
    }

    /** 将一颗完整或破碎魂珠加入万魂珠，万魂珠不限制总数。 */
    public static boolean absorbIntoMyriad(ItemStack myriad, ItemStack soulPearl) {
        if (!isMyriad(myriad) || !isSoulPearl(soulPearl) || !hasEntity(soulPearl)) return false;
        String type = entityId(soulPearl);
        if (type.isEmpty()) return false;
        CompoundTag tag = myriad.getOrCreateTag();
        ListTag list = tag.contains(MYRIAD_SOULS, Tag.TAG_LIST)
                ? tag.getList(MYRIAD_SOULS, Tag.TAG_COMPOUND) : new ListTag();
        CompoundTag entry = null;
        for (Tag raw : list) if (raw instanceof CompoundTag candidate
                && type.equals(candidate.getString(SOUL_ENTITY_TYPE))) { entry = candidate; break; }
        if (entry == null) {
            entry = new CompoundTag();
            entry.putString(SOUL_ENTITY_TYPE, type);
            entry.putInt(SOUL_COUNT, 0);
            entry.putInt(SOUL_DEAD, 0);
            if (soulPearl.hasTag() && soulPearl.getTag().contains(SOUL_DISPLAY_NAME, Tag.TAG_STRING))
                entry.putString(SOUL_DISPLAY_NAME, soulPearl.getTag().getString(SOUL_DISPLAY_NAME));
            list.add(entry);
        }
        int amount = Math.max(1, soulPearl.getCount());
        entry.putInt(SOUL_COUNT, Math.max(0, entry.getInt(SOUL_COUNT)) + amount);
        if (isBrokenSoulPearl(soulPearl)) entry.putInt(SOUL_DEAD,
                Math.max(0, entry.getInt(SOUL_DEAD)) + amount);
        tag.put(MYRIAD_SOULS, list);
        return true;
    }

    /** 从万魂珠取出一枚指定魂魄，优先取出破碎魂魄。 */
    public static ItemStack extractFromMyriad(ItemStack myriad, String type) {
        if (!isMyriad(myriad) || type == null || type.isEmpty()) return ItemStack.EMPTY;
        CompoundTag tag = myriad.getOrCreateTag();
        ListTag list = tag.contains(MYRIAD_SOULS, Tag.TAG_LIST)
                ? tag.getList(MYRIAD_SOULS, Tag.TAG_COMPOUND) : new ListTag();
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            if (!type.equals(entry.getString(SOUL_ENTITY_TYPE)) || entry.getInt(SOUL_COUNT) <= 0) continue;
            int count = Math.max(0, entry.getInt(SOUL_COUNT));
            int dead = Math.min(count, Math.max(0, entry.getInt(SOUL_DEAD)));
            boolean broken = dead > 0;
            String displayName = entry.contains(SOUL_DISPLAY_NAME, Tag.TAG_STRING)
                    ? entry.getString(SOUL_DISPLAY_NAME) : "";
            entry.putInt(SOUL_COUNT, count - 1);
            entry.putInt(SOUL_DEAD, broken ? dead - 1 : dead);
            if (entry.getInt(SOUL_COUNT) <= 0) list.remove(i);
            tag.put(MYRIAD_SOULS, list);
            return createPearl(type, 0, broken, displayName);
        }
        return ItemStack.EMPTY;
    }

    /** 修复万魂珠内全部破碎魂魄，返回修复数量。 */
    public static int repairMyriad(ItemStack myriad) {
        if (!isMyriad(myriad) || !myriad.hasTag()) return 0;
        CompoundTag tag = myriad.getTag();
        if (!tag.contains(MYRIAD_SOULS, Tag.TAG_LIST)) return 0;
        int repaired = 0;
        ListTag list = tag.getList(MYRIAD_SOULS, Tag.TAG_COMPOUND);
        for (Tag raw : list) if (raw instanceof CompoundTag entry) {
            repaired += Math.max(0, entry.getInt(SOUL_DEAD));
            entry.putInt(SOUL_DEAD, 0);
        }
        tag.put(MYRIAD_SOULS, list);
        return repaired;
    }

    public static void writeStoredSouls(ItemStack myriad, java.util.List<StoredSoul> stored) {
        if (!isMyriad(myriad)) return;
        ListTag list = new ListTag();
        for (StoredSoul soul : stored) {
            if (soul.count() <= 0 || soul.entityType().isEmpty()) continue;
            CompoundTag entry = new CompoundTag();
            entry.putString(SOUL_ENTITY_TYPE, soul.entityType());
            entry.putInt(SOUL_COUNT, soul.count());
            entry.putInt(SOUL_DEAD, Math.min(soul.count(), Math.max(0, soul.dead())));
            if (soul.displayName() != null && !soul.displayName().isEmpty())
                entry.putString(SOUL_DISPLAY_NAME, soul.displayName());
            list.add(entry);
        }
        myriad.getOrCreateTag().put(MYRIAD_SOULS, list);
    }

    /** 创建保留生物类型的魂珠；破碎魂珠会在幡内记录为战死状态。 */
    public static ItemStack createPearl(String entityId, int experienceReward, boolean broken, String displayName) {
        ItemStack pearl = new ItemStack(broken
                ? WuXingItems.BROKEN_SOUL_PEARL.get() : WuXingItems.SOUL_PEARL.get());
        CompoundTag tag = pearl.getOrCreateTag();
        tag.putString(ENTITY_TYPE, entityId == null ? "" : entityId);
        tag.putInt(EXPERIENCE_REWARD, Math.max(0, experienceReward));
        tag.putBoolean("IsBroken", broken);
        if (displayName != null && !displayName.isEmpty()) tag.putString("DisplayName", displayName);
        return pearl;
    }
}
