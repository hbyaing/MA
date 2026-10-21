package cn.blockforge.generated.generatedmod.knowledge;

import cn.blockforge.generated.generatedmod.WuXingItems;
import java.util.List;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.RegistryObject;

/** Registers on the existing item register; there is no second profession or knowledge capability. */
public final class KnowledgeItems {
    public static final RegistryObject<Item> MYRIAD_COMPENDIUM=WuXingItems.ITEMS.register("myriad_compendium",
            ()->new MyriadCompendiumItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
    public static final List<String> SCROLL_IDS=List.of("alchemy_inheritance_yellow","alchemy_inheritance_mystic",
            "alchemy_inheritance_earth","alchemy_inheritance_heaven");
    public static final List<RegistryObject<Item>> SCROLLS=java.util.stream.IntStream.range(0,4)
            .mapToObj(tier->WuXingItems.ITEMS.<Item>register(SCROLL_IDS.get(tier),
                    ()->new AlchemyInheritanceItem(tier,new Item.Properties().stacksTo(16)
                            .rarity(tier==3?Rarity.EPIC:tier==2?Rarity.RARE:Rarity.UNCOMMON))))
            .toList();
    public static void bootstrap() {}
    private KnowledgeItems() {}
}
