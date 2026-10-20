package cn.blockforge.generated.generatedmod.balance;

/** Shared by the server and tooltips. Values are absolute mana, never a fraction. */
public final class DivineArtCosts {
    public static final double SOUL_DISTANCE_SCALE = 64.0D;
    private DivineArtCosts() {}
    public static int activation(String id) {
        return switch (id) {
            case "tian_di_wei_lu" -> 800;
            case "ta_gang_bu_dou" -> Math.round(RealmBalance.mana(2)*.25F);
            case "fa_tian_xiang_di" -> Math.round(RealmBalance.mana(3)*.25F);
            case "yuan_shen_chu_qiao" -> Math.round(RealmBalance.mana(4)*.20F);
            case "yi_hun_ji_po" -> Math.round(RealmBalance.mana(5)*.15F);
            case "qi_shi_er_bian" -> Math.round(RealmBalance.mana(5)*.12F);
            case "jin_dou_yun" -> Math.round(RealmBalance.mana(5)*.10F);
            case "ri_yue_ting_lun" -> Math.round(RealmBalance.mana(5)*.20F);
            default -> 0;
        };
    }
    public static int perSecond(String id) {
        return switch (id) {
            case "fa_tian_xiang_di" -> Math.round(RealmBalance.mana(3)*.02F);
            case "yuan_shen_chu_qiao" -> Math.round(RealmBalance.mana(4)*.01F);
            case "yi_hun_ji_po" -> Math.round(RealmBalance.mana(5)*.01F);
            default -> 0;
        };
    }
    /** Soul upkeep grows continuously with real distance; no fixed horizontal/vertical range cutoff. */
    public static double soulDistanceMultiplier(double distance) {
        return Double.isFinite(distance) ? 1.0D+Math.max(0.0D,distance)/SOUL_DISTANCE_SCALE : Double.POSITIVE_INFINITY;
    }
    public static int maintenance(String id, double distance) {
        int base=perSecond(id);
        if(!"yuan_shen_chu_qiao".equals(id))return base;
        double cost=base*soulDistanceMultiplier(distance);
        return cost>=Integer.MAX_VALUE?Integer.MAX_VALUE:(int)Math.ceil(cost);
    }
}
