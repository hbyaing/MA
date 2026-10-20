package cn.blockforge.generated.generatedmod.balance;

/** Authoritative realm tables, independent of Minecraft so boundary rules are testable. */
public final class RealmBalance {
    private static final int[] MANA = {100,300,1000,3200,10000,32000,100000,320000,1000000};
    private static volatile int[] configuredMana=MANA;
    public static void configureMana(int[] values){
        if(values.length!=9)throw new IllegalArgumentException("Nine realm values required");
        configuredMana=values.clone();
    }
    public static void resetMana(){configuredMana=MANA;}
    private static final int[] HEALTH = {40,70,140,300,600,1000,1600,2500,4000};
    private static final double[] ATTACK = {2,5,12,28,55,85,125,180,260};
    private static final long[] CULTIVATION = {25,250,2500,25000,250000,1250000,5000000,12500000,25000000};
    private static final double[] MEDITATION = {1,5,25,150,1000,3500,10000,19000,28000};
    private static final float[] REGEN = {.0025F,.0026F,.0028F,.003F,.0032F,.0034F,.0036F,.0038F,.004F};
    private static final float[] STAGE = {1F,1.06F,1.13F,1.20F};
    private RealmBalance() {}
    private static int realm(int value) { return Math.max(0,Math.min(8,value)); }
    public static int mana(int realm) { return configuredMana[realm(realm)]; }
    public static int health(int realm) { return HEALTH[realm(realm)]; }
    public static double attack(int realm) { return ATTACK[realm(realm)]; }
    public static double meditation(int realm) { return MEDITATION[realm(realm)]; }
    public static float regeneration(int realm) { return REGEN[realm(realm)]; }
    public static float stage(int stage) { return STAGE[Math.max(0,Math.min(3,stage))]; }
    public static long cultivation(int realm,int stage) { return CULTIVATION[realm(realm)]*(Math.max(0,Math.min(3,stage))+1L); }
}
