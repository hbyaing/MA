package cn.blockforge.generated.generatedmod.alchemy;

import net.minecraft.network.chat.Component;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

/** Keep attribution supplied by the item tooltip after screen-specific recipe details. */
public final class AlchemyTooltipOrder {
    private static final Set<String> MOD_NAMES = Set.of(
            "万界仙途", "Myriad Ascension", "万界仙途 · Myriad Ascension",
            "五行仙途", "Wu Xing Xian Tu");

    public static List<Component> attributionLast(List<Component> original) {
        List<Component> content=new ArrayList<>(),sources=new ArrayList<>();
        for(var line:original) {
            String text=line.getString().trim();
            if(MOD_NAMES.contains(text))sources.add(line);
            else content.add(line);
        }
        content.addAll(sources);return content;
    }
    private AlchemyTooltipOrder() {}
}
