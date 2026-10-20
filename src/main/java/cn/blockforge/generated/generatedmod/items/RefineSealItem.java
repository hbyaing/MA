package cn.blockforge.generated.generatedmod.items;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

/** Alternative consumable for the existing protected-refinement action, never a persistent buff. */
public final class RefineSealItem extends Item {
    public RefineSealItem(Properties properties){super(properties);}
    @Override public void appendHoverText(ItemStack stack,@Nullable Level level,List<Component> lines,TooltipFlag flag){
        super.appendHoverText(stack,level,lines,flag);
        lines.add(Component.translatable("item.wuxingxiantu.refine_seal.desc"));
    }
}
