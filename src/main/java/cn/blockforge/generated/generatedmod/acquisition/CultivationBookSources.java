package cn.blockforge.generated.generatedmod.acquisition;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.WuXingItems;
import cn.blockforge.generated.generatedmod.WuXingProfessions;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraftforge.registries.RegistryObject;

/** One definition drives the merchant shelf, fishing reward, tooltip and source audit. */
public final class CultivationBookSources {
    public static final float FISHING_CHANCE = 0.02F;

    public record Book(RegistryObject<Item> item, int merchantLevel, int spiritStones, int fishingWeight) {
        public MerchantOffer offer() {
            return new MerchantOffer(new ItemStack(WuXingItems.SPIRIT_STONE.get(), spiritStones),
                    new ItemStack(item.get()), 2, merchantLevel * 5, 0.05F);
        }
    }

    // The six arts and Tiangang with existing sources keep those sources unchanged.
    public static final List<Book> BOOKS = List.of(
            new Book(WuXingItems.ART_RENEWAL, 1, 12, 40),
            new Book(WuXingItems.ART_SPIRIT_SHIELD, 1, 12, 40),
            new Book(WuXingItems.ART_IMMOBILIZE, 1, 12, 40),
            new Book(WuXingItems.ART_LIGHT_BODY, 1, 12, 40),
            new Book(WuXingItems.ART_GOLDEN_BELL, 2, 24, 20),
            new Book(WuXingItems.ART_ICE_SPIKE, 2, 24, 20),
            new Book(WuXingItems.ART_SPIRIT_GATHERING, 2, 24, 20),
            new Book(WuXingItems.ART_ARMOR_BREAK, 2, 24, 20),
            new Book(WuXingItems.ART_FLAME_WAVE, 3, 40, 8),
            new Book(WuXingItems.ART_FLASH_STEP, 3, 40, 8),
            new Book(WuXingItems.ART_PURIFICATION, 3, 40, 8),
            new Book(WuXingItems.ART_BLOODLUST, 3, 40, 8),
            new Book(WuXingItems.ART_THUNDER, 3, 40, 8),
            new Book(WuXingItems.ART_MYRIAD_SWORDS, 5, 64, 2),
            new Book(WuXingItems.ART_MOUNTAIN, 5, 64, 2),
            new Book(WuXingItems.ART_HARMONY, 5, 64, 2),
            new Book(WuXingItems.ART_FIVE_ELEMENTS, 5, 64, 2),
            new Book(WuXingItems.HEART_YUAN_HUN, 1, 12, 40),
            new Book(WuXingItems.HEART_GATHERING, 2, 16, 24),
            new Book(WuXingItems.HEART_XUANSHUI, 3, 20, 12),
            new Book(WuXingItems.HEART_NINE_TURNS, 4, 40, 6),
            new Book(WuXingItems.HEART_BURNING_HEAVEN, 4, 40, 6),
            new Book(WuXingItems.HEART_THICK_EARTH, 4, 40, 6),
            new Book(WuXingItems.HEART_TAIXU, 5, 56, 2),
            new Book(WuXingItems.HEART_BEAST_TAMING, 5, 56, 2),
            new Book(WuXingItems.HEART_WANLING, 5, 64, 1));

    public static final int TOTAL_FISHING_WEIGHT = BOOKS.stream().mapToInt(Book::fishingWeight).sum();

    private CultivationBookSources() {}

    public static Book find(ResourceLocation id) {
        return BOOKS.stream().filter(book -> book.item().getId().equals(id)).findFirst().orElse(null);
    }

    /** Append, never replace: existing prices, tags, uses and stock survive the migration. */
    public static void ensureMerchantBooks(Villager villager) {
        if (villager.level().isClientSide || villager.isBaby()
                || villager.getVillagerData().getProfession() != WuXingProfessions.SPIRIT_CULTIVATOR.get()) return;
        var offers = villager.getOffers();
        int level = villager.getVillagerData().getLevel();
        for (Book book : BOOKS) {
            if (level < book.merchantLevel()) continue;
            if (offers.stream().noneMatch(offer -> offer.getResult().is(book.item().get()))) {
                offers.add(book.offer());
            }
        }
    }

    public static Book selectFishingBook(int roll) {
        if (roll < 0 || roll >= TOTAL_FISHING_WEIGHT) throw new IllegalArgumentException("Fishing book roll: " + roll);
        for (Book book : BOOKS) {
            roll -= book.fishingWeight();
            if (roll < 0) return book;
        }
        throw new IllegalStateException("Invalid fishing book weights");
    }

    public static ItemStack fishingReward(RandomSource random) {
        if (random.nextFloat() >= FISHING_CHANCE) return ItemStack.EMPTY;
        return new ItemStack(selectFishingBook(random.nextInt(TOTAL_FISHING_WEIGHT)).item().get());
    }

    public static void contribute(AcquisitionSourcesEvent event) {
        ResourceLocation merchant = WuXingProfessions.SPIRIT_CULTIVATOR.getId();
        ResourceLocation fishing = new ResourceLocation(GeneratedMod.MOD_ID, "cultivation_book_fishing");
        for (Book book : BOOKS) {
            event.add(book.item().get(), new SourceEvidence("trade", merchant,
                    List.of("adult villager level >= " + book.merchantLevel(),
                            "complete book shelf added before trading; includes saved merchants",
                            "2 uses per restock; normal villager price adjustments; no acquisition realm gate"),
                    0, true, List.of(book.spiritStones() + "x" + WuXingItems.SPIRIT_STONE.getId())));
            event.add(book.item().get(), new SourceEvidence("fishing", fishing,
                    List.of("successful, non-cancelled open-water fishing; nonempty original drops",
                            "additional book chance " + FISHING_CHANCE + "; weight "
                                    + book.fishingWeight() + "/" + TOTAL_FISHING_WEIGHT,
                            "one extra book; no acquisition realm gate; study gates unchanged"),
                    0, true, List.of("minecraft:fishing_rod")));
        }
    }
}
