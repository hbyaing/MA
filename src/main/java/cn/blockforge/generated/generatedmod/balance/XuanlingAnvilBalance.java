package cn.blockforge.generated.generatedmod.balance;

/** Prices follow the configured Foundation Establishment mana pool, not the user's realm. */
public final class XuanlingAnvilBalance {
    public static int manaPerLevel() { return Math.max(1, RealmBalance.mana(1) / 15); }
    public static int repairCost() { return Math.max(1, RealmBalance.mana(1) / 2); }
    public static int operationCost(int levels) {
        return (int)Math.min(Integer.MAX_VALUE, (long)Math.max(0, levels) * manaPerLevel());
    }
    private XuanlingAnvilBalance() {}
}
