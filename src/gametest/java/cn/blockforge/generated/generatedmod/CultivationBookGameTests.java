package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.acquisition.AcquisitionSourcesEvent;
import cn.blockforge.generated.generatedmod.acquisition.CultivationBookSources;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("wuxingxiantu")
@PrefixGameTestTemplate(false)
public final class CultivationBookGameTests {
    @GameTest(template = "furnace_empty")
    public static void allMissingBooksHaveBothSources(GameTestHelper h) {
        Set<String> existingArts = Set.of("art_sword_aura", "art_sword_pierce", "art_sword_flight",
                "art_sword_sweep", "art_sword_guard", "art_sword_array");
        var missing = ArtCatalog.ENTRIES.stream().map(ArtCatalog.Entry::id)
                .filter(id -> !existingArts.contains(id)).collect(Collectors.toSet());
        HeartMethodCatalog.METHOD_IDS.stream().filter(id -> !id.equals("tiangang"))
                .map(id -> "heart_method_" + id).forEach(missing::add);
        var defined = CultivationBookSources.BOOKS.stream().map(book -> book.item().getId().getPath())
                .collect(Collectors.toSet());
        h.assertTrue(defined.equals(missing), "Missing art/heart book acquisition coverage differs");
        h.assertTrue(defined.size() == CultivationBookSources.BOOKS.size(), "Duplicate book definitions");
        Map<ResourceLocation, Set<String>> evidence = new HashMap<>();
        CultivationBookSources.contribute(new AcquisitionSourcesEvent((id, source) -> {
            evidence.computeIfAbsent(id, key -> new HashSet<>()).add(source.type());
            h.assertTrue(Boolean.TRUE.equals(source.repeatable()) && source.minimumRealm() == 0,
                    "Unexpected source realm/repeatability");
        }));
        for (var book : CultivationBookSources.BOOKS) {
            h.assertTrue(evidence.get(book.item().getId()).equals(Set.of("trade", "fishing")),
                    "Missing one of the two actual sources: " + book.item().getId());
            h.assertTrue(book.offer().getBaseCostA().getCount() <= WuXingItems.SPIRIT_STONE.get().getMaxStackSize(),
                    "Book price exceeds a payment stack");
        }
        h.succeed();
    }

    @GameTest(template = "furnace_empty")
    public static void savedTradesSurviveMigrationAndUpgrade(GameTestHelper h) {
        var villager = h.spawn(EntityType.VILLAGER, new BlockPos(1, 1, 1));
        villager.setVillagerData(villager.getVillagerData()
                .setProfession(WuXingProfessions.SPIRIT_CULTIVATOR.get()).setLevel(3));
        villager.setVillagerXp(80);
        var saved = new MerchantOffers();
        var legacy = new MerchantOffer(new ItemStack(WuXingItems.SPIRIT_STONE.get(), 8),
                new ItemStack(WuXingItems.AURA_CRYSTAL.get()), 16, 2, 0.05F);
        legacy.increaseUses();
        saved.add(legacy);
        var renewal = CultivationBookSources.BOOKS.get(0).offer();
        renewal.increaseUses();
        saved.add(renewal);
        // Recreate the saved Offers NBT used by authored islands and loaded villagers.
        villager.setOffers(new MerchantOffers(saved.createTag()));
        var oldTrade = villager.getOffers().get(0);
        var oldBook = villager.getOffers().get(1);
        var tradeTag = oldTrade.createTag().copy();
        var bookTag = oldBook.createTag().copy();
        CultivationBookSources.ensureMerchantBooks(villager);
        assertShelf(h, villager, 3);
        int count = villager.getOffers().size();
        CultivationBookSources.ensureMerchantBooks(villager);
        h.assertTrue(villager.getOffers().size() == count, "Repeated interaction duplicated offers");
        h.assertTrue(villager.getOffers().get(0) == oldTrade && oldTrade.createTag().equals(tradeTag),
                "Existing saved trade was replaced or reset");
        h.assertTrue(villager.getOffers().get(1) == oldBook && oldBook.createTag().equals(bookTag),
                "Existing book uses or price changed during migration");
        villager.setVillagerData(villager.getVillagerData().setLevel(5));
        CultivationBookSources.ensureMerchantBooks(villager);
        assertShelf(h, villager, 5);
        h.assertTrue(villager.getOffers().size() == CultivationBookSources.BOOKS.size() + 1,
                "Master shelf not complete or legacy offer lost");
        h.assertTrue(oldBook.getUses() == 1, "Levelling reset purchased book stock");
        h.succeed();
    }

    private static void assertShelf(GameTestHelper h, Villager villager, int level) {
        for (var book : CultivationBookSources.BOOKS) {
            long count = villager.getOffers().stream().filter(offer -> offer.getResult().is(book.item().get())).count();
            h.assertTrue(count == (book.merchantLevel() <= level ? 1 : 0),
                    "Incorrect level gate or duplicate: " + book.item().getId());
        }
    }

    @GameTest(template = "furnace_empty")
    public static void clericsAndChildrenDoNotReceiveTheNewShelf(GameTestHelper h) {
        var cleric = h.spawn(EntityType.VILLAGER, new BlockPos(1, 1, 1));
        cleric.setVillagerData(cleric.getVillagerData().setProfession(VillagerProfession.CLERIC).setLevel(5));
        cleric.setOffers(new MerchantOffers());
        CultivationBookSources.ensureMerchantBooks(cleric);
        h.assertTrue(cleric.getOffers().isEmpty(), "Original cleric received spirit-only books");
        var child = h.spawn(EntityType.VILLAGER, new BlockPos(2, 1, 1));
        child.setAge(-24000);
        child.setVillagerData(child.getVillagerData().setProfession(WuXingProfessions.SPIRIT_CULTIVATOR.get()).setLevel(5));
        child.setOffers(new MerchantOffers());
        CultivationBookSources.ensureMerchantBooks(child);
        h.assertTrue(child.getOffers().isEmpty(), "Child received merchant books");
        h.succeed();
    }

    @GameTest(template = "furnace_empty")
    public static void fishingWeightsReachEveryBookWithoutGaps(GameTestHelper h) {
        Map<CultivationBookSources.Book, Integer> hits = new HashMap<>();
        for (int roll = 0; roll < CultivationBookSources.TOTAL_FISHING_WEIGHT; roll++) {
            hits.merge(CultivationBookSources.selectFishingBook(roll), 1, Integer::sum);
        }
        for (var book : CultivationBookSources.BOOKS) {
            h.assertTrue(hits.getOrDefault(book, 0) == book.fishingWeight(),
                    "Unreachable book or incorrect weight: " + book.item().getId());
        }
        h.succeed();
    }
}
