package cn.blockforge.generated.generatedmod.alchemy;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

/** One distribution shared by the server's persisted result and the UI. */
public final class AlchemyGradeOdds {
    private static final int[][] WEIGHTS = {{60,28,10,2},{35,45,17,3},{15,35,42,8},{8,22,50,20}};
    public static int percent(ItemStack result, PillData.Grade grade) {
        var pill = PillData.read(result);
        if (pill == null) return 0;
        if (pill.pill() == PillCatalog.CALMING) return grade == PillData.Grade.LOW ? 100 : 0;
        return WEIGHTS[pill.grade().ordinal()][grade.ordinal()];
    }
    public static ItemStack roll(ItemStack original, RandomSource random) {
        var pill = PillData.read(original);
        if (pill == null || pill.pill() == PillCatalog.CALMING) return original.copy();
        int draw = random.nextInt(100);
        PillData.Grade selected = PillData.Grade.PERFECT;
        for (var grade : PillData.Grade.values()) {
            draw -= percent(original, grade);
            if (draw < 0) { selected = grade; break; }
        }
        ItemStack result = original.copy();
        result.getOrCreateTag().putString("Grade", selected.name());
        return result;
    }
    private AlchemyGradeOdds() {}
}
