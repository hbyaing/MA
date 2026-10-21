package cn.blockforge.generated.generatedmod;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingConversionEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.TradeWithVillagerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.List;

@Mod.EventBusSubscriber(modid = GeneratedMod.MOD_ID)
public final class TaoistEvents {
    static final String SHRINE_MERCHANT_TAG = "WuxingShrineTaoist";
    private static final TalismanCatalog.Quality[] SALE_QUALITIES = {
            TalismanCatalog.Quality.LOW, TalismanCatalog.Quality.MID, TalismanCatalog.Quality.PERFECT
    };

    /** Only authored shrine merchants are fixed; ordinary villagers keep vanilla job selection. */
    @SubscribeEvent
    public static void protectShrineProfession(EntityJoinLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)
                || !(event.getEntity() instanceof Villager villager)) return;
        protectShrineMerchant(villager, level);
    }

    static void protectShrineMerchant(Villager villager, ServerLevel level) {
        boolean marked = villager.getPersistentData().getBoolean(SHRINE_MERCHANT_TAG);
        if (!marked) {
            // Migration of still-untraded Taoists on islands recorded by this mod.
            // A generic fisherman has no reliable legacy identity and must not be converted.
            if (villager.getVillagerData().getProfession() != WuXingProfessions.TAOIST.get()
                    || villager.getVillagerXp() > 0 || !level.dimension().equals(Level.OVERWORLD)) return;
            var entrance = SpiritShrineLocations.get(level).nearest(villager.blockPosition());
            if (entrance == null) return;
            var origin = entrance.subtract(SpiritShrineManager.ENTRANCE);
            var bounds = new net.minecraft.world.phys.AABB(origin, origin.offset(103, 74, 86));
            if (!bounds.contains(villager.position())) return;
            villager.getPersistentData().putBoolean(SHRINE_MERCHANT_TAG, true);
        }
        if (villager.getVillagerData().getProfession() != WuXingProfessions.TAOIST.get()) {
            villager.setVillagerData(villager.getVillagerData().setProfession(WuXingProfessions.TAOIST.get()));
            villager.refreshBrain(level);
        }
        // ResetProfession only clears novice jobs at zero XP; AssignProfessionFromJobSite
        // only changes unemployed villagers. One XP preserves this job without levelling up.
        if (villager.getVillagerXp() == 0) villager.setVillagerXp(1);
    }

    public static ItemStack randomSeal(RandomSource random) {
        return randomize(new ItemStack(WuXingItems.SEAL.get()), random);
    }

    public static ItemStack randomTalisman(RandomSource random, int count) {
        List<String> ids=TalismanCatalog.ids();
        return TalismanData.create(ids.get(random.nextInt(ids.size())),count,
                SALE_QUALITIES[random.nextInt(SALE_QUALITIES.length)]);
    }

    private static ItemStack randomize(ItemStack stack, RandomSource random) {
        List<String> ids = TalismanCatalog.ids();
        TalismanData.setTalisman(stack, ids.get(random.nextInt(ids.size())));
        TalismanData.setQuality(stack, SALE_QUALITIES[random.nextInt(SALE_QUALITIES.length)]);
        return stack;
    }

    /** Cancel infection only; normal damage/death and non-zombie conversions keep vanilla behavior. */
    @SubscribeEvent
    public static void preventInfection(LivingConversionEvent.Pre event) {
        if (event.getOutcome() == EntityType.ZOMBIE_VILLAGER
                && event.getEntity() instanceof Villager villager
                && villager.getVillagerData().getProfession() == WuXingProfessions.TAOIST.get()) {
            event.setCanceled(true);
        }
    }

    /** The sold copy keeps its NBT. Only the next seal on the merchant's shelf is randomized. */
    @SubscribeEvent
    public static void nextSeal(TradeWithVillagerEvent event) {
        if (!(event.getAbstractVillager() instanceof Villager villager)
                || villager.getVillagerData().getProfession() != WuXingProfessions.TAOIST.get()
                || !event.getMerchantOffer().getResult().is(WuXingItems.SEAL.get())) return;
        event.getMerchantOffer().getResult().setTag(randomSeal(villager.getRandom()).getTag());
        if (event.getEntity() instanceof ServerPlayer player) {
            player.sendMerchantOffers(player.containerMenu.containerId, villager.getOffers(),
                    villager.getVillagerData().getLevel(), villager.getVillagerXp(), true, true);
        }
    }
}
