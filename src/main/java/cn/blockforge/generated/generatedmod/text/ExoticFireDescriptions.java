package cn.blockforge.generated.generatedmod.text;

import cn.blockforge.generated.generatedmod.ExoticFireColorRegistry;
import cn.blockforge.generated.generatedmod.WuXingBlocks;
import cn.blockforge.generated.generatedmod.fire.FireBottleData;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** Shared, tier-colored descriptions for bottles, registered fire items and the inner-vision selector. */
public final class ExoticFireDescriptions {
    public static int tier(ItemStack stack) {
        int bottled=FireBottleData.tier(stack);
        if(bottled>0)return bottled;
        for(int i=0;i<WuXingBlocks.EXOTIC_FIRE_ITEMS.length;i++)
            if(stack.is(WuXingBlocks.EXOTIC_FIRE_ITEMS[i].get()))return i+1;
        return 0;
    }
    public static int color(int tier,long time) {
        return tier==8?ExoticFireColorRegistry.accentColor(tier,time):ExoticFireColorRegistry.color(tier,time);
    }
    public static List<Component> lines(int tier,long time) {
        var lines=new ArrayList<Component>();
        int color=color(tier,time);
        lines.add(ItemTextPalette.colored(Component.translatable("tooltip.wuxingxiantu.fire_tier",tier),color));
        for(String suffix:new String[]{"lore."+tier,"weapon."+tier,"special."+tier,"requirement","levels","burn","stacks","armor","armor_interval","targets"})
            lines.add(ItemTextPalette.colored(Component.translatable("tooltip.wuxingxiantu.fire_detail."+suffix),color));
        return lines;
    }
    public static Component tooltip(int tier,long time,Component state) {
        var result=ItemTextPalette.colored(state,color(tier,time));
        for(var line:lines(tier,time))result.append("\n").append(line);
        return result;
    }
    private ExoticFireDescriptions() {}
}
