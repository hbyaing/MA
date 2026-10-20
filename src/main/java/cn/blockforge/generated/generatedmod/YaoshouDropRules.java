package cn.blockforge.generated.generatedmod;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

/** 妖兽内丹掉落的唯一判定入口。新增妖兽只需加入标签，并设置自身品阶 NBT。 */
public final class YaoshouDropRules {
    public static final TagKey<EntityType<?>> YAOSHOU = TagKey.create(
            Registries.ENTITY_TYPE, new ResourceLocation(GeneratedMod.MOD_ID, "yaoshou"));
    private static final String GRADE_TAG = "wuxingxiantu_yaoshou_grade";

    private YaoshouDropRules() {}

    public static boolean isYaoshou(LivingEntity entity) {
        return entity != null && entity.getType().is(YAOSHOU);
    }

    /** 品阶从妖兽自身数据读取；未配置的标签实体按最低阶处理，不能产生高阶内丹。 */
    public static int grade(LivingEntity entity, int coreCount) {
        if (!isYaoshou(entity)) return 0;
        int maximum = Math.max(1, coreCount - 1); // 仙品内丹仍不从普通击杀掉落
        int configured = entity.getPersistentData().getInt(GRADE_TAG);
        return Math.max(1, Math.min(maximum, configured > 0 ? configured : 1));
    }

    public static int coreIndex(LivingEntity entity, int killerRealm, int coreCount) {
        if (!isYaoshou(entity) || coreCount <= 1) return -1;
        int playerLimit = Math.max(0, Math.min(coreCount - 2, killerRealm));
        return Math.min(playerLimit, grade(entity, coreCount) - 1);
    }
}
