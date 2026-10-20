package cn.blockforge.generated.generatedmod.registry;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.WuXingItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/** 五行仙途创造模式标签页注册。 */
public final class ModTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, GeneratedMod.MOD_ID);

    public static final RegistryObject<CreativeModeTab> WUXINGXIANTU_TAB = TABS.register(
            "wuxingxiantu",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.wuxingxiantu"))
                    .icon(() -> new ItemStack(WuXingItems.SPIRIT_STONE.get()))
                    .displayItems((parameters, output) -> {
                        var groups=new java.util.HashMap<RegistryObject<net.minecraft.world.item.Item>,Integer>();
                        WuXingItems.ITEMS.getEntries().stream()
                                .sorted(java.util.Comparator.comparingInt(entry->groups.computeIfAbsent(entry,ModTabs::displayGroup))).forEach(item -> {
                            if (WuXingItems.BOSS_TEST_EGGS.contains(item)) return;
                            ItemStack stack = new ItemStack(item.get());
                            if(cn.blockforge.generated.generatedmod.WuXingItemSources.classify(stack)
                                    ==cn.blockforge.generated.generatedmod.WuXingItemSources.Classification.INTERNAL)return;
                            var pill = cn.blockforge.generated.generatedmod.alchemy.PillData.read(stack);
                            if (pill != null) return;
                            if (stack.getItem() instanceof cn.blockforge.generated.generatedmod.items.FireSeedItem fire && fire.tier() > 0) {
                                output.accept(cn.blockforge.generated.generatedmod.fire.FireBottleData.fresh(fire.tier()));
                                return;
                            }
                            if (item != WuXingItems.ALCHEMY_FORMULA && item != WuXingItems.TALISMAN) {
                                var herb=cn.blockforge.generated.generatedmod.plants.HerbAge.definition(stack);
                                if(herb!=null&&herb.id.startsWith("herb_")){
                                    for(int rank=1;rank<=herb.medicinalTierCap;rank++){
                                        var aged=stack.copy();cn.blockforge.generated.generatedmod.plants.HerbAge.setYears(aged,500+(rank-1)*2000);output.accept(aged);
                                    }
                                }else output.accept(stack);
                            }
                        });
                        for (var pill : cn.blockforge.generated.generatedmod.alchemy.PillCatalog.values()) {
                            output.accept(cn.blockforge.generated.generatedmod.alchemy.FormulaItem.create(pill));
                            for (var grade : cn.blockforge.generated.generatedmod.alchemy.PillData.Grade.values()) {
                                if(pill==cn.blockforge.generated.generatedmod.alchemy.PillCatalog.CALMING&&grade!=cn.blockforge.generated.generatedmod.alchemy.PillData.Grade.LOW)continue;
                                int elements = pill == cn.blockforge.generated.generatedmod.alchemy.PillCatalog.ROOT ? 5 : 1;
                                int tiers = pill == cn.blockforge.generated.generatedmod.alchemy.PillCatalog.CULTIVATION ? 9 : 1;
                                for (int element = 0; element < elements; element++) {
                                    for (int tier = 0; tier < tiers; tier++) {
                                        var produced=cn.blockforge.generated.generatedmod.alchemy.PillData.create(pill, grade,
                                                cn.blockforge.generated.generatedmod.alchemy.PillData.Element.values()[element], tier);
                                        if(cn.blockforge.generated.generatedmod.alchemy.MedicineTier.imported(pill))
                                            for(int rank=1;rank<=5;rank++)output.accept(cn.blockforge.generated.generatedmod.alchemy.MedicineTier.set(produced.copy(),rank));
                                        else output.accept(produced);
                                    }
                                }
                            }
                        }
                        cn.blockforge.generated.generatedmod.arts.ImportedSwordArts.ITEMS.getEntries().forEach(item->output.accept(item.get()));
                        cn.blockforge.generated.generatedmod.formation.FormationBootstrap.items().forEach(item->output.accept(item.get()));
                        cn.blockforge.generated.generatedmod.artifacts.ArtifactBootstrap.items().forEach(item->output.accept(item.get()));
                        output.accept(cn.blockforge.generated.generatedmod.storage.StorageRegistry.BAG_ITEM.get());
                        WuXingItems.BOSS_TEST_EGGS.forEach(item -> output.accept(item.get()));
                    })
                    .build());

    /** Stable sorting keeps the existing order within each functional group. */
    private static int displayGroup(RegistryObject<net.minecraft.world.item.Item> entry) {
        var item = entry.get();
        if (entry == WuXingItems.INSIGHT) return 3;
        if (entry == WuXingItems.GUANDING_MANUAL) return 4;
        if (item instanceof cn.blockforge.generated.generatedmod.items.ArtManualItem) return 5;
        if (item instanceof cn.blockforge.generated.generatedmod.items.DivineManualItem) return 6;
        if (item instanceof cn.blockforge.generated.generatedmod.items.HeartMethodItem) return 7;
        if (item instanceof cn.blockforge.generated.generatedmod.alchemy.CompendiumItem) return 9;
        if (item instanceof net.minecraft.world.item.BlockItem) return 1;
        if (item instanceof net.minecraft.world.item.TieredItem
                || item instanceof net.minecraft.world.item.ArmorItem
                || item instanceof cn.blockforge.generated.generatedmod.items.HumanEmperorBannerItem) return 2;
        if (item instanceof cn.blockforge.generated.generatedmod.items.SoulBindingPearlItem
                || item instanceof cn.blockforge.generated.generatedmod.items.SoulCapturePearlItem
                || item instanceof cn.blockforge.generated.generatedmod.items.SoulUtilityPearlItem
                || item instanceof cn.blockforge.generated.generatedmod.items.SoulEnergyPearlItem) return 8;
        String id = entry.getId().getPath();
        if(id.startsWith("spirit_stone_"))return 0;
        for(var materials:java.util.List.of(WuXingItems.GOLD_MATERIALS,WuXingItems.WOOD_MATERIALS,
                WuXingItems.WATER_MATERIALS,WuXingItems.FIRE_MATERIALS,WuXingItems.EARTH_MATERIALS))
            for(var material:materials)if(entry==material)return 0;
        if (java.util.Set.of("pill_ash","spirit_stone", "spirit_coal", "raw_dark_iron", "dark_iron",
                        "azure_steel", "tiangang_steel", "five_essence", "insect_carapace_fragment",
                        "spirit_devourer_sac", "peach_blossom", "peach_petals","soul_pearl","broken_soul_pearl").contains(id)) return 0;
        return 8;
    }

    private ModTabs() {}
}
