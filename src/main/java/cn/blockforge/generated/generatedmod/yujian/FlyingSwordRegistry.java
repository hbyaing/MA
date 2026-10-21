package cn.blockforge.generated.generatedmod.yujian;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;

/** Central registry for flying sword definitions. Adding a sword does not require a new controller. */
public final class FlyingSwordRegistry {
    private static final Map<ResourceLocation, FlyingSwordDefinition> DEFINITIONS = new java.util.concurrent.ConcurrentHashMap<>();
    private static final FlyingSwordDefinition DEFAULT_VISUAL = new FlyingSwordDefinition(
            new ResourceLocation("wuxingxiantu", "default"),
            1.0F, 1.0F, 1.0F, 0.08D,
            YujianTuning.BASE_FLIGHT_SPEED, YujianTuning.SPRINT_FLIGHT_SPEED, 0.095D, 0.985D, 0.19D, 0.72D,
            0.75D, Math.toRadians(160)/400, Math.toRadians(450)/20, 0.65D, 4.0D,0,90,45,false,YujianTuning.NONE);

    static {
        register(new FlyingSwordDefinition(
                new ResourceLocation("wuxingxiantu", "dark_iron_sword"),
                1.0F, 1.12F, 1.08F, 0.055D,
                YujianTuning.BASE_FLIGHT_SPEED, YujianTuning.SPRINT_FLIGHT_SPEED, 0.092D, 0.985D, 0.18D, 0.68D,
                0.78D, Math.toRadians(160)/400, Math.toRadians(450)/20, 0.68D, 4.5D,90,0,0,true,YujianTuning.forSword("dark_iron_sword")));
        registerImported("mingjue_sword", 0.043395D);
        registerImported("qingming_sword", 0.038828D);
        registerImported("jixiao_sword", 0.024156D);
        registerTemplate("wuxiang_sword");
        registerTemplate("guiyuan_sword");
        registerTemplate("tianque_sword");
    }

    private FlyingSwordRegistry() {}

    private static void registerTemplate(String path) {
        // Placeholder blades use the complete Xuan-Tie movement/holding profile.
        // Their own damage and tuning profile remain independent numeric values.
        var d = DEFINITIONS.get(new ResourceLocation("wuxingxiantu", "dark_iron_sword"));
        if (d == null) d = DEFAULT_VISUAL;
        register(new FlyingSwordDefinition(new ResourceLocation("wuxingxiantu", path),
                d.handScale(), d.orbitScale(), d.flightScale(), d.riderOffset(),
                d.baseSpeed(), d.sprintSpeed(), d.acceleration(), d.drag(), d.turnRate(),
                d.pitchResponse(), d.bankingStrength(), d.rollAcceleration(), d.maxRollSpeed(),
                d.spiralSpeedMultiplier(), DEFAULT_VISUAL.impactDamage(), d.modelYaw(), d.modelPitch(), d.modelRoll(), true,YujianTuning.forSword(path)));
    }

    private static void registerImported(String path, double riderOffset) {
        // Import report: one-block total length, tip -X; use the shared movement profile.
        register(new FlyingSwordDefinition(new ResourceLocation("wuxingxiantu", path),
                1F, 1F, 1F, riderOffset,
                DEFAULT_VISUAL.baseSpeed(), DEFAULT_VISUAL.sprintSpeed(), DEFAULT_VISUAL.acceleration(), DEFAULT_VISUAL.drag(),
                DEFAULT_VISUAL.turnRate(), DEFAULT_VISUAL.pitchResponse(), DEFAULT_VISUAL.bankingStrength(),
                DEFAULT_VISUAL.rollAcceleration(), DEFAULT_VISUAL.maxRollSpeed(), DEFAULT_VISUAL.spiralSpeedMultiplier(),
                DEFAULT_VISUAL.impactDamage(), 90, 0, 0, true,YujianTuning.forSword(path)));
    }

    public static void register(FlyingSwordDefinition definition) {
        DEFINITIONS.put(definition.itemId(), definition);
    }

    public static FlyingSwordDefinition get(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !SwordProfile.of(stack).flightCapability()) return null;
        return DEFINITIONS.get(ForgeRegistries.ITEMS.getKey(stack.getItem()));
    }

    /** Rendering combat shadows does not grant flight permission. */
    public static FlyingSwordDefinition visual(ItemStack stack) {
        var definition = get(stack);
        return definition == null ? DEFAULT_VISUAL : definition;
    }

    public static boolean canFly(ItemStack stack) {
        return get(stack) != null;
    }

    public static java.util.List<FlyingSwordDefinition> definitions(){return java.util.List.copyOf(DEFINITIONS.values());}

    public static boolean isRegistered(ItemStack stack) {
        return stack != null && !stack.isEmpty()
                && DEFINITIONS.containsKey(ForgeRegistries.ITEMS.getKey(stack.getItem()));
    }
}
