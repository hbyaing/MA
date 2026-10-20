package cn.blockforge.generated.generatedmod.registry;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.HeartDemonEntity;
import cn.blockforge.generated.generatedmod.MeditationSeatEntity;
import cn.blockforge.generated.generatedmod.OfferingItemEntity;
import cn.blockforge.generated.generatedmod.SpiritOrbEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** 模组实体类型注册。 */
public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, GeneratedMod.MOD_ID);
    public static final RegistryObject<EntityType<cn.blockforge.generated.generatedmod.entity.SpiritDevourerEntity>> SPIRIT_DEVOURER = ENTITY_TYPES.register("spirit_devourer",()->EntityType.Builder
            .of(cn.blockforge.generated.generatedmod.entity.SpiritDevourerEntity::new,MobCategory.MONSTER).sized(.65F,.48F).clientTrackingRange(8).build("spirit_devourer"));
    public static final RegistryObject<EntityType<cn.blockforge.generated.generatedmod.SoulBodyEntity>> SOUL_BODY = ENTITY_TYPES.register(
            "soul_body", () -> EntityType.Builder.<cn.blockforge.generated.generatedmod.SoulBodyEntity>of(
                    cn.blockforge.generated.generatedmod.SoulBodyEntity::new, MobCategory.MISC)
                    .sized(.6F, 1.8F).clientTrackingRange(10).updateInterval(10).build("soul_body"));

    public static final RegistryObject<EntityType<cn.blockforge.generated.generatedmod.yujian.ControlledSwordEntity>> CONTROLLED_SWORD = ENTITY_TYPES.register(
            "controlled_sword", () -> EntityType.Builder.<cn.blockforge.generated.generatedmod.yujian.ControlledSwordEntity>of(cn.blockforge.generated.generatedmod.yujian.ControlledSwordEntity::new,MobCategory.MISC)
                    .sized(0.3F,0.3F).clientTrackingRange(8).updateInterval(1).noSave().build("controlled_sword"));

    public static final RegistryObject<EntityType<MeditationSeatEntity>> MEDITATION_SEAT = ENTITY_TYPES.register(
            "meditation_seat", () -> EntityType.Builder.<MeditationSeatEntity>of(MeditationSeatEntity::new, MobCategory.MISC)
                    .sized(0.1F, 0.1F)
                    .clientTrackingRange(10)
                    .updateInterval(1)
                    .noSave()
                    .build("meditation_seat"));

    public static final RegistryObject<EntityType<HeartDemonEntity>> HEART_DEMON = ENTITY_TYPES.register(
            "heart_demon", () -> EntityType.Builder.<HeartDemonEntity>of(HeartDemonEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F)
                    .clientTrackingRange(32)
                    .updateInterval(1)
                    .build("heart_demon"));

    public static final java.util.Map<String, RegistryObject<EntityType<cn.blockforge.generated.generatedmod.boss.VisualBossEntity>>> VISUAL_BOSSES = registerVisualBosses();

    private static java.util.Map<String, RegistryObject<EntityType<cn.blockforge.generated.generatedmod.boss.VisualBossEntity>>> registerVisualBosses() {
        var entries = new java.util.LinkedHashMap<String, RegistryObject<EntityType<cn.blockforge.generated.generatedmod.boss.VisualBossEntity>>>();
        for (String id : cn.blockforge.generated.generatedmod.boss.BossVisualRegistry.TEST_ENTITY_IDS) {
            var visual = cn.blockforge.generated.generatedmod.boss.BossVisualRegistry.get(id);
            entries.put(id, ENTITY_TYPES.register(id, () -> EntityType.Builder
                    .<cn.blockforge.generated.generatedmod.boss.VisualBossEntity>of(
                            (type, level) -> new cn.blockforge.generated.generatedmod.boss.VisualBossEntity(type, level, id), MobCategory.MISC)
                    .sized(visual.width(), visual.height()).clientTrackingRange(10).updateInterval(1).build(id)));
        }
        return java.util.Collections.unmodifiableMap(entries);
    }

    /** 承华台供奉异火的飞行供品（纯视觉，素材在点击炼制时已由服务端扣除；逐刻推送位置保证客户端平滑）。 */
    public static final RegistryObject<EntityType<OfferingItemEntity>> OFFERING_ITEM = ENTITY_TYPES.register(
            "offering_item", () -> EntityType.Builder.<OfferingItemEntity>of(OfferingItemEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(4)
                    .updateInterval(1)
                    .build("offering_item"));

    /** 灵力球：击杀怪物替代经验球掉落的蓝色灵体，拾取恢复灵力。 */
    public static final RegistryObject<EntityType<SpiritOrbEntity>> SPIRIT_ORB = ENTITY_TYPES.register(
            "spirit_orb", () -> EntityType.Builder.<SpiritOrbEntity>of(SpiritOrbEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(6)
                    .updateInterval(10)
                    .build("spirit_orb"));

    private ModEntities() {}
}
