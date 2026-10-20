package cn.blockforge.generated.generatedmod.items;

import cn.blockforge.generated.generatedmod.TalismanCatalog;
import cn.blockforge.generated.generatedmod.TalismanData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/** 印章：镌印台产物，携带某种符箓的符文（NBT），可反复拓印，无耐久限制。 */
public final class SealItem extends Item {
    public SealItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        String id = TalismanData.getTalisman(stack);
        if (TalismanCatalog.has(id)) {
            tooltip.add(Component.translatable("tooltip.wuxingxiantu.seal_engraved",
                    TalismanCatalog.title(id)));
            TalismanCatalog.Quality quality = TalismanData.quality(stack);
            if (quality != null) tooltip.add(Component.translatable(
                    "tooltip.wuxingxiantu.talisman_quality", quality.title()));
            tooltip.add(Component.translatable("tooltip.wuxingxiantu.seal_fixed_result"));
        } else {
            tooltip.add(Component.translatable("tooltip.wuxingxiantu.seal_empty"));
        }
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
