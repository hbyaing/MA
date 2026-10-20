package cn.blockforge.generated.generatedmod.items;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;
import java.util.List;
public final class InsectMaterialItem extends Item {
    private final String description;
    public InsectMaterialItem(String id){super(new Item.Properties());description="tooltip.wuxingxiantu."+id;}
    @Override public void appendHoverText(ItemStack stack,Level level,List<Component> lines,TooltipFlag flag){
        lines.add(Component.translatable(description).withStyle(net.minecraft.ChatFormatting.GRAY));
    }
}
