package cn.blockforge.generated.generatedmod;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** 五行仙途客户端异火粒子注册；水滴缠绕粒子使用独立的 exotic_fire_droplet 贴图。 */
public final class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, GeneratedMod.MOD_ID);
    public static final RegistryObject<SimpleParticleType> SPIRIT_GUARD_GLOW = PARTICLES.register(
            "spirit_guard_glow", () -> new SimpleParticleType(false));
    /** 元神出窍沿用魂火动画与运动，仅替换为金白色图集。 */
    public static final RegistryObject<SimpleParticleType> GOLDEN_SOUL_FLAME = PARTICLES.register(
            "golden_soul_flame", () -> new SimpleParticleType(false));

    /** 兼容旧网络/资源引用；新发射器不再创建该贴图粒子。 */
    public static final RegistryObject<SimpleParticleType> EXOTIC_FIRE_FLAME = PARTICLES.register(
            "exotic_fire_flame", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> EXOTIC_FIRE_CORE = PARTICLES.register(
            "exotic_fire_core", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> EXOTIC_FIRE_OUTER = PARTICLES.register(
            "exotic_fire_outer", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> EXOTIC_FIRE_EMBER = PARTICLES.register(
            "exotic_fire_ember", () -> new SimpleParticleType(false));

    /** 异火缠绕水滴粒子：尖上圆下的实心水滴，按装备异火品阶染色。 */
    public static final RegistryObject<SimpleParticleType> EXOTIC_FIRE_DROPLET = PARTICLES.register(
            "exotic_fire_droplet", () -> new SimpleParticleType(false));
    /** 心剑专用白青光点/蓝色火花，不依赖原版粒子类型。 */
    public static final RegistryObject<SimpleParticleType> HEART_SWORD_SPARK = PARTICLES.register(
            "heart_sword_spark", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> HEART_MOTE = PARTICLES.register(
            "heart_mote", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> HEART_FLAME = PARTICLES.register(
            "heart_flame", () -> new SimpleParticleType(false));

    /** Each selected library material has its own atlas sprite; no vanilla placeholder particles. */
    public static final java.util.Map<String, RegistryObject<SimpleParticleType>> ART_MATERIALS = artMaterials();
    private static java.util.Map<String, RegistryObject<SimpleParticleType>> artMaterials() {
        var result = new java.util.LinkedHashMap<String, RegistryObject<SimpleParticleType>>();
        cn.blockforge.generated.generatedmod.arts.ArtVfxCatalog.materials().stream().sorted().forEach(id ->
                result.put(id, PARTICLES.register("art_" + id, () -> new SimpleParticleType(false))));
        return java.util.Collections.unmodifiableMap(result);
    }

    private ModParticles() {}
}
