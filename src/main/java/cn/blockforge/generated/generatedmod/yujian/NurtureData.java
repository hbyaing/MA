package cn.blockforge.generated.generatedmod.yujian;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.item.ItemStack;
import java.util.*;

/** 每把灵剑独立持久化的服务器数据。所有变更都通过服务端验证后写回 ItemStack。 */
public final class NurtureData {
    public static final String TAG = "WuxingSwordNurture";
    private static final String UUID_KEY = "SwordUuid";
    private static final String EXP_KEY = "NurtureExp";
    private static final String STAGE_KEY = "NurtureStage";
    private static final String INSIGHT_KEY = "SwordInsightPoints";
    private static final String UNLOCKED_KEY = "UnlockedTechniques";
    private static final String EQUIPPED_KEY = "equipped_sword_techniques";
    private static final String MIGRATED_KEY = "SwordKnowledgeMigrated";
    public UUID swordUuid;
    public long nurtureExp;
    public int nurtureStage;
    public int insightPoints;
    public boolean knowledgeMigrated;
    public final LinkedHashSet<String> legacyUnlockedTechniques = new LinkedHashSet<>();
    public final ArrayList<String> equippedTechniques = new ArrayList<>();

    public static NurtureData read(ItemStack stack) {
        NurtureData data = new NurtureData();
        CompoundTag root = stack.hasTag() ? stack.getTag().getCompound(TAG) : new CompoundTag();
        data.swordUuid = root.hasUUID(UUID_KEY) ? root.getUUID(UUID_KEY) : null;
        data.nurtureExp = Math.max(0, root.getLong(EXP_KEY));
        data.nurtureStage = Math.max(0, root.getInt(STAGE_KEY));
        data.insightPoints = Math.max(0, root.getInt(INSIGHT_KEY));
        data.knowledgeMigrated = root.getBoolean(MIGRATED_KEY);
        if (!data.knowledgeMigrated) for (var tag : root.getList(UNLOCKED_KEY, 8)) data.legacyUnlockedTechniques.add(tag.getAsString());
        String equippedKey = root.contains(EQUIPPED_KEY, 9) ? EQUIPPED_KEY : "EquippedTechniques";
        for (var tag : root.getList(equippedKey, 8)) data.equippedTechniques.add(tag.getAsString());
        return data;
    }
    public void write(ItemStack stack, SwordProfile profile) {
        CompoundTag root = stack.getOrCreateTag().getCompound(TAG).copy();
        root.putUUID(UUID_KEY, swordUuid == null ? (swordUuid = UUID.randomUUID()) : swordUuid);
        root.putLong(EXP_KEY, Math.max(0, nurtureExp));
        root.putInt(STAGE_KEY, Math.max(0, Math.min(profile.maxNurtureStage(), nurtureStage)));
        root.putInt(INSIGHT_KEY, Math.max(0, insightPoints));
        if (knowledgeMigrated) {
            root.putBoolean(MIGRATED_KEY, true);
            root.remove(UNLOCKED_KEY);
        }
        ListTag equipped = new ListTag(); equipped.addAll(equippedTechniques.stream().map(StringTag::valueOf).toList()); root.put(EQUIPPED_KEY, equipped);
        root.remove("EquippedTechniques");
        stack.getOrCreateTag().put(TAG, root);
    }
    public long requirement(SwordProfile profile) { return 100L * (nurtureStage + 1L) * (nurtureStage + 1L); }
    public boolean ready(SwordProfile profile) { return nurtureStage < profile.maxNurtureStage() && nurtureExp >= requirement(profile); }
    public void addExp(long amount, SwordProfile profile) { nurtureExp = Math.min(requirement(profile), Math.max(0, nurtureExp + Math.max(0, amount))); }
}
