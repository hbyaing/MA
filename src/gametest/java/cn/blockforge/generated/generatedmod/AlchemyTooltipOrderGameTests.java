package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.alchemy.AlchemyTooltipOrder;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraftforge.gametest.*;

@GameTestHolder("wuxingxiantu_entry")
@PrefixGameTestTemplate(false)
public final class AlchemyTooltipOrderGameTests {
    @GameTest(template="empty")
    public static void attributionFollowsRecipeDetailsWithoutChangingStyle(GameTestHelper h) {
        var title=Component.literal("极品疾风丹");var detail=Component.literal("疾风草：炉内0/8（缺8）");
        for(String source:java.util.List.of("万界仙途","Myriad Ascension","万界仙途 · Myriad Ascension","五行仙途","Wu Xing Xian Tu")) {
            var name=Component.literal(source).withStyle(ChatFormatting.BLUE,ChatFormatting.ITALIC);
            var original=new java.util.ArrayList<Component>(java.util.List.of(title,name,detail));
            var reordered=AlchemyTooltipOrder.attributionLast(original);
            h.assertTrue(reordered.equals(java.util.List.of(title,detail,name))&&reordered.get(2)==name,"Attribution order or style changed: "+source);
            h.assertTrue(original.get(1)==name,"Original item tooltip mutated");
            h.assertTrue(AlchemyTooltipOrder.attributionLast(reordered).equals(reordered),"Repeated ordering changed tooltip");
        }
        h.assertTrue(AlchemyTooltipOrder.attributionLast(java.util.List.of(title,detail)).equals(java.util.List.of(title,detail)),"Attribution added when absent");
        h.succeed();
    }
}
