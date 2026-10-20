package cn.blockforge.generated.generatedmod.fire;

import net.minecraft.resources.ResourceLocation;

/** Rarity describes acquisition, independently of tier and the minimum player realm. */
public final class FireCatalog {
    public enum Acquisition { NATURAL, LIGHTNING_EVENT, EVOLUTION, END_RITUAL }
    public record Definition(String id, int tier, String rarity, int requiredRealm, Acquisition acquisition) {
        public ResourceLocation key() { return new ResourceLocation("wuxingxiantu", id); }
    }
    public static final Definition[] ALL = {
            new Definition("red_flow", 1, "common", 0, Acquisition.NATURAL),
            new Definition("molten_gold", 2, "rare", 1, Acquisition.EVOLUTION),
            new Definition("cold_ash", 3, "rare", 2, Acquisition.NATURAL),
            new Definition("azure_wood", 4, "rare", 3, Acquisition.NATURAL),
            new Definition("cobalt_sea", 5, "rare", 4, Acquisition.NATURAL),
            new Definition("violet_thunder", 6, "very_rare", 5, Acquisition.LIGHTNING_EVENT),
            new Definition("sun_saint", 7, "very_rare", 6, Acquisition.EVOLUTION),
            new Definition("nether_void", 8, "very_rare", 7, Acquisition.EVOLUTION),
            new Definition("nirvana", 9, "unique", 8, Acquisition.END_RITUAL)
    };
    public static Definition tier(int tier) { return tier >= 1 && tier <= 9 ? ALL[tier - 1] : null; }
    public static Definition id(String id) {
        for (Definition fire : ALL) if (fire.key().toString().equals(id)) return fire;
        return null;
    }
    private FireCatalog() {}
}
