package cn.blockforge.generated.generatedmod.yujian;

import java.util.Set;
import java.util.Map;
import net.minecraft.world.phys.Vec3;

/** Shared movement profiles and collision work budgets, without a fixed flight-speed cap. */
public final class YujianTuning {
    /** All sword tiers share the dark-iron sword as the speed reference. */
    /** 基础御剑速度提升 200%：原值 0.58，现为原值的 3 倍。 */
    public static final double BASE_FLIGHT_SPEED=0.58D*3.0D;
    public static final double SPRINT_FLIGHT_SPEED=BASE_FLIGHT_SPEED*1.5D;
    public static final double SWEEP_STEP=0.2;
    // Covers the fastest configured Dunxu displacement with 0.2-block swept checks.
    // This bounds collision work, not the requested flight speed.
    public static final int MAX_SWEEP_STEPS=512, MAX_COLLISION_BLOCKS=512, SAFE_HISTORY=48;
    public static final int DUNXU_TICKS=30, DUNXU_PRE_TICKS=2, DUNXU_EXIT_TICKS=4;
    public static final int DUNXU_COOLDOWN=40;
    public static final double DUNXU_ENTRY_SPEED=0.70;
    public static final int DUNXU_PREDICT_TICKS=4;
    public static final double DUNXU_ENTRY_AURA_FRACTION=0.01;
    public static final double DUNXU_TICK_AURA_FRACTION=0.015/20.0;
    public static final double HOVER_HORIZONTAL=0.12, HOVER_VERTICAL=0.07, HOVER_DAMPING=0.65;
    public static final double DECELERATION=0.12;
    public static final int INPUT_HEARTBEAT=4, DIAGNOSTIC_INTERVAL=5;
    public static final float MAX_FOV_BOOST=15;
    public record Profile(Set<YujianPhase> capabilities, boolean jinghong, double cruiseMultiplier, double jiyuMultiplier,
                          double dunxuMultiplier, double efficiency,
                          double jiyuAcceleration, double burstAcceleration, double fastHandling) {
        public Profile { capabilities=Set.copyOf(capabilities); }
        public boolean supports(YujianPhase phase) { return capabilities.contains(phase); }
        public int lingkongLevel(FlyingSwordDefinition sword) {
            String path=sword.itemId().getPath();
            if(path.endsWith("_sword"))path=path.substring(0,path.length()-6);
            return SwordProfile.of(path).lingkongLevel();
        }
        public double maxSpeed(YujianPhase phase, FlyingSwordDefinition sword) {
            double lingkong=1.0D+0.25D*lingkongLevel(sword);
            double speed=switch(phase) {
                case JIYU -> SPRINT_FLIGHT_SPEED*jiyuMultiplier*lingkong;
                case DUNXU -> BASE_FLIGHT_SPEED*dunxuMultiplier*lingkong;
                case LINGKONG -> BASE_FLIGHT_SPEED*lingkong;
                default -> BASE_FLIGHT_SPEED*cruiseMultiplier*lingkong;
            };
            return Math.max(0.01, speed*FlyingSwordConfig.SPEED_MULTIPLIER.get());
        }
        public float costPerTick(YujianPhase phase) {
            double perSecond=switch(phase) {
                case JIYU -> 7; case LINGKONG -> 3; case DUNXU -> 0; default -> 2;
            };
            return (float)(perSecond*efficiency/20.0);
        }
        /** Dunxu entry and upkeep are percentage based and charged by the controller. */
        public float startup(YujianPhase phase) { return 0.0F; }
        public float jinghongCost() { return (float)(16*efficiency); }
    }
    private static Profile profile(double efficiency, boolean jinghong, YujianPhase... phases) {
        return new Profile(Set.of(phases),jinghong,1,1,4,efficiency,0.14,0.26,0.65);
    }
    public static final Profile NONE=profile(1,false);
    private static final Map<String,Profile> PROFILES=Map.of(
            "dark_iron_sword",profile(1,false,YujianPhase.BASIC,YujianPhase.ACCELERATING,YujianPhase.JIYU),
            "mingjue_sword",profile(1,false,YujianPhase.BASIC,YujianPhase.ACCELERATING,YujianPhase.JIYU,YujianPhase.LINGKONG),
            "qingming_sword",profile(1,true,YujianPhase.BASIC,YujianPhase.ACCELERATING,YujianPhase.JIYU,YujianPhase.LINGKONG),
            "jixiao_sword",profile(0.95,true,YujianPhase.BASIC,YujianPhase.ACCELERATING,YujianPhase.JIYU,YujianPhase.LINGKONG),
            "wuxiang_sword",profile(0.95,true,YujianPhase.values()),
            "guiyuan_sword",profile(0.90,true,YujianPhase.values()),
            "tianque_sword",profile(0.85,true,YujianPhase.values()));
    public static Profile forSword(String id) { return PROFILES.getOrDefault(id,NONE); }
    public static Vec3 finiteVelocity(Vec3 velocity) {
        if(!Double.isFinite(velocity.x)||!Double.isFinite(velocity.y)||!Double.isFinite(velocity.z))return Vec3.ZERO;
        return velocity;
    }
    private YujianTuning() {}
}
