package cn.blockforge.generated.generatedmod.plants;
import cn.blockforge.generated.generatedmod.GeneratedMod;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID)
public final class HerbAgeTooltip {
    @SubscribeEvent public static void tooltip(ItemTooltipEvent event){
        var stack=event.getItemStack();var def=HerbAge.definition(stack);if(def==null)return;
        event.getToolTip().add(Component.translatable("tooltip.wuxingxiantu.herb_years",HerbAge.years(stack)).withStyle(net.minecraft.ChatFormatting.GRAY));
        event.getToolTip().add(Component.translatable("tooltip.wuxingxiantu.herb_age_cultivation").withStyle(net.minecraft.ChatFormatting.DARK_GRAY));
    }
}
