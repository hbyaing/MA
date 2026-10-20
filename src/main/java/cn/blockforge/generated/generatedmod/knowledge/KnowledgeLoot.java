package cn.blockforge.generated.generatedmod.knowledge;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.WuXingItems;
import cn.blockforge.generated.generatedmod.acquisition.AcquisitionSourcesEvent;
import cn.blockforge.generated.generatedmod.acquisition.SourceEvidence;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.event.LootTableLoadEvent;

/** Knowledge comes from existing world chests, and is advertised to the survival acquisition audit. */
public final class KnowledgeLoot {
    private static final List<List<String>> CHESTS=List.of(
            List.of("abandoned_mineshaft","simple_dungeon","desert_pyramid","jungle_temple"),
            List.of("stronghold_corridor","stronghold_crossing","stronghold_library","buried_treasure"),
            List.of("ancient_city","woodland_mansion","bastion_treasure"),
            List.of("end_city_treasure"));
    private static final int[] BOOKS={0,1,3,4};
    public static int scrollTier(ResourceLocation table) {
        if(!"minecraft".equals(table.getNamespace())||!table.getPath().startsWith("chests/"))return -1;
        String path=table.getPath().substring(7);
        for(int tier=0;tier<CHESTS.size();tier++)if(CHESTS.get(tier).contains(path))return tier;
        return -1;
    }
    public static void inject(LootTableLoadEvent event) {
        int tier=scrollTier(event.getName());if(tier<0)return;
        event.getTable().addPool(LootPool.lootPool().name("wuxingxiantu_alchemy_inheritance")
                .setRolls(ConstantValue.exactly(1)).add(LootItem.lootTableItem(KnowledgeItems.SCROLLS.get(tier).get()))
                .when(LootItemRandomChanceCondition.randomChance(tier==3?.06F:.10F)).build());
        event.getTable().addPool(LootPool.lootPool().name("wuxingxiantu_alchemy_ruin_knowledge")
                .setRolls(ConstantValue.exactly(1)).add(LootItem.lootTableItem(WuXingItems.ALCHEMY_BOOKS.get(BOOKS[tier]).get()))
                .when(LootItemRandomChanceCondition.randomChance(.08F)).build());
    }
    public static void sources(AcquisitionSourcesEvent event) {
        event.register("alchemy-inheritance",sink->{
            for(int tier=0;tier<CHESTS.size();tier++) {
                final int current=tier;
                var evidence=new SourceEvidence("loot_injection",new ResourceLocation(GeneratedMod.MOD_ID,"alchemy_inheritance_"+tier),
                        CHESTS.get(tier).stream().map(path->"minecraft:chests/"+path+"; random roll").toList(),
                        0,false,List.of());
                sink.add(KnowledgeItems.SCROLLS.get(current).get(),evidence);
                sink.add(WuXingItems.ALCHEMY_BOOKS.get(BOOKS[current]).get(),evidence);
            }
        });
    }
    private KnowledgeLoot() {}
}
