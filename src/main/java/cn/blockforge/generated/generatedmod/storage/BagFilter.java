package cn.blockforge.generated.generatedmod.storage;

import cn.blockforge.generated.generatedmod.alchemy.PillData;
import cn.blockforge.generated.generatedmod.economy.SpiritStoneGrade;
import cn.blockforge.generated.generatedmod.plants.HerbAge;
import net.minecraft.world.item.*;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.Locale;

public final class BagFilter {
    public static final String[] LABELS={"全部","草药","黄阶草药","玄阶草药","地阶草药","天阶草药","仙阶草药","丹药","灵石","方块","装备","其他"};
    public static boolean matches(ItemStack stack,String query,int category) {
        if(stack.isEmpty())return false;
        Item item=stack.getItem();
        boolean herb=HerbAge.definition(stack)!=null,pill=PillData.read(stack)!=null,stone=SpiritStoneGrade.of(item)!=null;
        boolean block=item instanceof BlockItem,equipment=item instanceof ArmorItem||item instanceof TieredItem||item instanceof BowItem
                ||item instanceof ShieldItem||item instanceof ElytraItem||item instanceof CrossbowItem||item instanceof FishingRodItem;
        boolean kind=switch(category){
            case 1->herb;case 2,3,4,5,6->herb&&HerbAge.effectiveTier(stack)==category-1;
            case 7->pill;case 8->stone;case 9->block;case 10->equipment;
            case 11->!herb&&!pill&&!stone&&!block&&!equipment;default->true;
        };
        String q=query.toLowerCase(Locale.ROOT);
        return kind&&(stack.getHoverName().getString().toLowerCase(Locale.ROOT).contains(q)||String.valueOf(ForgeRegistries.ITEMS.getKey(item)).contains(q));
    }
    private BagFilter() {}
}