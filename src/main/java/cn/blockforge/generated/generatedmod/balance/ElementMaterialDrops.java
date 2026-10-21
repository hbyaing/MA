package cn.blockforge.generated.generatedmod.balance;

/** Luck improves a bounded chance; it never directly selects an item grade. */
public final class ElementMaterialDrops {
    public static int grade(float luck, double roll) {
        var chance=chances(luck);
        return roll<chance[3]?3:roll<chance[3]+chance[2]?2:roll<chance[3]+chance[2]+chance[1]?1:0;
    }
    /** Conditional grade probabilities, ordered low/mid/high/perfect. */
    public static double[] chances(float luck){
        double bonus=Float.isFinite(luck)?Math.max(0,Math.min(5,luck)):0;
        double perfect=.01+bonus*.008, high=.06+bonus*.012, mid=.23+bonus*.015;
        return new double[]{1-perfect-high-mid,mid,high,perfect};
    }
    private ElementMaterialDrops() {}
}
