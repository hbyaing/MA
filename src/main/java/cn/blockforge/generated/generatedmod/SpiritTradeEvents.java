package cn.blockforge.generated.generatedmod;

import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.ItemStack;
import cn.blockforge.generated.generatedmod.acquisition.EvidenceTrade;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** 将灵修者交易表接入自定义职业与原版牧师，兼容原版村庄生成与存档。 */
@Mod.EventBusSubscriber(modid = GeneratedMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class SpiritTradeEvents {
    private SpiritTradeEvents() {}

    @SubscribeEvent
    public static void addTrades(VillagerTradesEvent event) {
        visitTrades(event.getType(),event.getTrades()::get);
    }
    private static void visitTrades(VillagerProfession profession,
            java.util.function.IntFunction<java.util.List<net.minecraft.world.entity.npc.VillagerTrades.ItemListing>> trades) {
        if (profession == WuXingProfessions.ALCHEMIST.get()) {
            var basics = java.util.Arrays.stream(cn.blockforge.generated.generatedmod.alchemy.PillCatalog.values())
                    .filter(pill -> pill.source == cn.blockforge.generated.generatedmod.alchemy.PillCatalog.Source.BASIC).toList();
            for (int i = 0; i < basics.size(); i++) {
                trades.apply(1 + i / 2).add(new EvidenceTrade(new ItemStack(WuXingItems.SPIRIT_STONE.get(), 4 + i * 2),
                        cn.blockforge.generated.generatedmod.alchemy.FormulaItem.create(basics.get(i)), 12, 10, 0.05F));
            }
            trades.apply(4).add(new EvidenceTrade(new ItemStack(WuXingItems.SPIRIT_STONE.get(), 4),
                    new ItemStack(WuXingItems.SPIRIT_FERTILIZER.get(), 8), 12, 15, 0.05F));
            trades.apply(5).add(new EvidenceTrade(new ItemStack(WuXingItems.SPIRIT_STONE.get(), 8),
                    new ItemStack(WuXingItems.AURA_CRYSTAL.get()), 8, 20, 0.05F));
            return;
        }
        if (profession == WuXingProfessions.TAOIST.get()) {
            addTaoistTrades(trades);
            return;
        }
        if (profession != VillagerProfession.CLERIC && profession != WuXingProfessions.SPIRIT_CULTIVATOR.get()) return;
        trades.apply(1).add(new EvidenceTrade(new ItemStack(WuXingItems.SPIRIT_STONE.get(), 8), new ItemStack(WuXingItems.AURA_CRYSTAL.get()), 16, 2, 0.05F));
        trades.apply(1).add(new EvidenceTrade(new ItemStack(WuXingItems.SPIRIT_STONE.get(), 4), new ItemStack(WuXingItems.SPIRIT_LIQUID_BUCKET.get()), 12, 2, 0.05F));
        trades.apply(1).add(new EvidenceTrade(new ItemStack(WuXingItems.SPIRIT_STONE.get(), 2), pill(cn.blockforge.generated.generatedmod.alchemy.PillCatalog.AURA, 0), 16, 3, 0.05F));
        trades.apply(2).add(new EvidenceTrade(new ItemStack(WuXingItems.SPIRIT_STONE.get(), 16), new ItemStack(WuXingItems.AURA_CRYSTAL.get(), 3), 12, 5, 0.05F));
        trades.apply(2).add(new EvidenceTrade(new ItemStack(WuXingItems.SPIRIT_STONE.get(), 8), pill(cn.blockforge.generated.generatedmod.alchemy.PillCatalog.AURA, 1), 12, 5, 0.05F));
        trades.apply(2).add(new EvidenceTrade(new ItemStack(WuXingItems.SPIRIT_STONE.get(), 12), new ItemStack(WuXingItems.ART_SWORD_AURA.get()), 8, 8, 0.05F));
        trades.apply(3).add(new EvidenceTrade(new ItemStack(WuXingItems.SPIRIT_STONE.get(), 24), new ItemStack(WuXingBlocks.GATHERING_ARRAY_ITEM.get()), 8, 10, 0.05F));
        trades.apply(3).add(new EvidenceTrade(new ItemStack(WuXingItems.SPIRIT_STONE.get(), 16), pill(cn.blockforge.generated.generatedmod.alchemy.PillCatalog.CLEAR_HEART, 0), 8, 10, 0.05F));
        trades.apply(3).add(new EvidenceTrade(new ItemStack(WuXingItems.SPIRIT_STONE.get(), 20), new ItemStack(WuXingItems.HEART_TIANGANG.get()), 4, 12, 0.05F));
        trades.apply(4).add(new EvidenceTrade(new ItemStack(WuXingItems.SPIRIT_STONE.get(), 32), pill(cn.blockforge.generated.generatedmod.alchemy.PillCatalog.GATHERING, 0), 6, 15, 0.05F));
        trades.apply(5).add(new EvidenceTrade(new ItemStack(WuXingItems.SPIRIT_STONE.get(), 1), new ItemStack(WuXingItems.INSIGHT.get()), 2, 30, 0.05F));
        trades.apply(5).add(new EvidenceTrade(new ItemStack(WuXingItems.SPIRIT_STONE.get(), 96),
                new ItemStack(WuXingItems.GUANDING_MANUAL.get()), 1, 40, 0.05F));
    }

    @SubscribeEvent public static void sources(cn.blockforge.generated.generatedmod.acquisition.AcquisitionSourcesEvent event) {
        event.register("spirit-trades",sink->{
            for(var profession:java.util.List.of(WuXingProfessions.ALCHEMIST.get(),WuXingProfessions.TAOIST.get(),
                    WuXingProfessions.SPIRIT_CULTIVATOR.get(),VillagerProfession.CLERIC)) {
                var definitions=new java.util.TreeMap<Integer,java.util.List<net.minecraft.world.entity.npc.VillagerTrades.ItemListing>>();
                visitTrades(profession,level->definitions.computeIfAbsent(level,k->new java.util.ArrayList<>()));
                var id=net.minecraft.core.registries.BuiltInRegistries.VILLAGER_PROFESSION.getKey(profession);
                definitions.forEach((level,list)->list.forEach(listing->{
                    if(listing instanceof EvidenceTrade trade)trade.contribute(sink,id,level);
                }));
            }
        });
    }

    private static ItemStack pill(cn.blockforge.generated.generatedmod.alchemy.PillCatalog pill, int grade) {
        return cn.blockforge.generated.generatedmod.alchemy.PillData.create(pill,
                cn.blockforge.generated.generatedmod.alchemy.PillData.Grade.values()[grade],
                cn.blockforge.generated.generatedmod.alchemy.PillData.Element.METAL, 0);
    }

    /** Two entries per level ensure every Taoist sells all the required supplies as it levels up. */
    private static void addTaoistTrades(
            java.util.function.IntFunction<java.util.List<net.minecraft.world.entity.npc.VillagerTrades.ItemListing>> trades) {
        trades.apply(1).add(new EvidenceTrade(new ItemStack(WuXingItems.SPIRIT_STONE.get(), 4),
                new ItemStack(WuXingItems.TALISMAN_PAPER.get(), 12), 16, 2, 0.05F));
        trades.apply(1).add(new EvidenceTrade(new ItemStack(WuXingItems.SPIRIT_STONE.get(), 6),
                new ItemStack(WuXingItems.CINNABAR_INK.get()), 12, 2, 0.05F));
        trades.apply(2).add(new EvidenceTrade(new ItemStack(WuXingItems.SPIRIT_STONE.get(), 12),
                new ItemStack(WuXingItems.CARVING_KNIFE.get()), 8, 5, 0.05F));
        trades.apply(2).add(new EvidenceTrade(new ItemStack(WuXingItems.SPIRIT_STONE.get(), 8),
                new ItemStack(WuXingItems.SPIRIT_INK.get()), 12, 5, 0.05F));
        trades.apply(3).add((entity, random) -> new net.minecraft.world.item.trading.MerchantOffer(
                new ItemStack(WuXingItems.SPIRIT_STONE.get(), 8), TaoistEvents.randomTalisman(random, 2), 12, 10, 0.05F));
        trades.apply(3).add((entity, random) -> new net.minecraft.world.item.trading.MerchantOffer(
                new ItemStack(WuXingItems.SPIRIT_STONE.get(), 12), TaoistEvents.randomTalisman(random, 3), 12, 10, 0.05F));
        trades.apply(4).add((entity, random) -> new net.minecraft.world.item.trading.MerchantOffer(
                new ItemStack(WuXingItems.SPIRIT_STONE.get(), 24), TaoistEvents.randomSeal(random), 6, 15, 0.05F));
        trades.apply(4).add((entity, random) -> new net.minecraft.world.item.trading.MerchantOffer(
                new ItemStack(WuXingItems.SPIRIT_STONE.get(), 24), TaoistEvents.randomSeal(random), 6, 15, 0.05F));
        trades.apply(5).add((entity, random) -> {
            ItemStack result = TaoistEvents.randomTalisman(random, 3);
            TalismanData.setQuality(result, TalismanCatalog.Quality.PERFECT);
            return new net.minecraft.world.item.trading.MerchantOffer(new ItemStack(WuXingItems.SPIRIT_STONE.get(), 32),
                    result, 8, 30, 0.05F);
        });
        trades.apply(5).add(new EvidenceTrade(new ItemStack(WuXingItems.SPIRIT_STONE.get(), 16),
                new ItemStack(WuXingItems.TALISMAN_PAPER.get(), 64), 12, 20, 0.05F));
    }
}
