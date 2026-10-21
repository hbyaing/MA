package cn.blockforge.generated.generatedmod.balance;

/** Heart-method values shared by gameplay, UI descriptions and regression checks. */
public final class HeartMethodBalance {
    private HeartMethodBalance() {}
    public static float manaMultiplier(String id) {
        return switch (id) { case "yuan_hun" -> 1.10F; case "wanling" -> 1.05F; default -> 1.0F; };
    }
    public static float regenerationMultiplier(String id, boolean meditating) {
        return switch (id) {
            case "gathering" -> meditating ? 1.20F : 1.0F;
            case "taixu", "wanling" -> 1.10F;
            default -> 1.0F;
        };
    }
    public static double cultivationMultiplier(String id, boolean meditating) {
        return switch (id) {
            case "yuan_hun", "wanling" -> 1.10D;
            case "gathering" -> meditating ? 1.15D : 1.0D;
            case "nine_turns" -> 1.20D;
            default -> 1.0D;
        };
    }
    public static long cultivationReward(String id, boolean meditating, long base) {
        return splitCultivationReward(id,meditating,base,0.0D).amount();
    }
    public record CultivationReward(long amount,double remainder) {}
    /** Carry fractional gains so a +15% method still works when early meditation grants one point. */
    public static CultivationReward splitCultivationReward(String id,boolean meditating,long base,double remainder) {
        double carry=Double.isFinite(remainder)&&remainder>=0&&remainder<1?remainder:0.0D;
        if(base<=0)return new CultivationReward(0,carry);
        double scaled=base*cultivationMultiplier(id,meditating)+carry;
        if(scaled>=Long.MAX_VALUE)return new CultivationReward(Long.MAX_VALUE,0);
        double nearest=Math.rint(scaled);
        if(Math.abs(scaled-nearest)<1.0E-8D)scaled=nearest;
        long whole=(long)Math.floor(scaled);
        return new CultivationReward(whole,scaled-whole);
    }
    public static float breakthroughFraction(String id) { return "nine_turns".equals(id) ? 0.40F : 0.50F; }
    public static double healthBonus(String id) {
        return switch (id) { case "thick_earth" -> 0.12D; case "wanling" -> 0.05D; default -> 0.0D; };
    }
    public static double attackBonus(String id) {
        return switch (id) { case "tiangang" -> 0.08D; case "wanling" -> 0.05D; default -> 0.0D; };
    }
    public static double speedBonus(String id) {
        return switch (id) { case "taixu" -> 0.12D; case "wanling" -> 0.05D; default -> 0.0D; };
    }
    public static double knockbackBonus(String id) { return "thick_earth".equals(id) ? 0.15D : 0.0D; }
    public static double swimBonus(String id) { return "xuanshui".equals(id) ? 0.20D : 0.0D; }
    public static float damageTakenMultiplier(String id) {
        return switch (id) { case "xuanshui" -> 0.92F; case "tiangang" -> 0.95F;
            case "thick_earth" -> 0.94F; default -> 1.0F; };
    }
    public static float fireDamageMultiplier(String id) { return "burning_heaven".equals(id) ? 1.15F : 1.0F; }
    public static int igniteSeconds(String id) { return "burning_heaven".equals(id) ? 2 : 0; }
    private static String percent(double value) {
        return String.format(java.util.Locale.ROOT, "%.0f", value * 100.0D);
    }
    public static Object[] descriptionArguments(String id) {
        return switch (id) {
            case "yuan_hun" -> new Object[]{percent(manaMultiplier(id)-1), percent(cultivationMultiplier(id,false)-1)};
            case "gathering" -> new Object[]{percent(regenerationMultiplier(id,true)-1), percent(cultivationMultiplier(id,true)-1)};
            case "tiangang" -> new Object[]{percent(attackBonus(id)), percent(1-damageTakenMultiplier(id))};
            case "xuanshui" -> new Object[]{percent(1-damageTakenMultiplier(id)), percent(swimBonus(id))};
            case "nine_turns" -> new Object[]{percent(cultivationMultiplier(id,false)-1), percent(1-breakthroughFraction(id)/.50F)};
            case "burning_heaven" -> new Object[]{percent(fireDamageMultiplier(id)-1), igniteSeconds(id)};
            case "thick_earth" -> new Object[]{percent(healthBonus(id)), percent(1-damageTakenMultiplier(id)),
                    String.format(java.util.Locale.ROOT,"%.2f",knockbackBonus(id))};
            case "taixu" -> new Object[]{percent(speedBonus(id)), percent(regenerationMultiplier(id,false)-1)};
            case "wanling" -> new Object[]{percent(healthBonus(id)), percent(regenerationMultiplier(id,false)-1),
                    percent(cultivationMultiplier(id,false)-1)};
            default -> new Object[0];
        };
    }
}
