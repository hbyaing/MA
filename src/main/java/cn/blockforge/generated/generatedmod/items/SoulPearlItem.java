package cn.blockforge.generated.generatedmod.items;

import cn.blockforge.generated.generatedmod.SoulPearlData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/** 人皇幡专用魂珠；完整魂珠可召唤，破碎魂珠归藏后记录为战死状态。 */
public final class SoulPearlItem extends Item {
    private final boolean broken;

    public SoulPearlItem(Properties properties, boolean broken) {
        super(properties);
        this.broken = broken;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        EntityName.add(tooltip, stack);
        tooltip.add(Component.translatable(broken
                ? "tooltip.wuxingxiantu.broken_soul_pearl"
                : "tooltip.wuxingxiantu.soul_pearl"));
    }

    private static final class EntityName {
        private static void add(List<Component> tooltip, ItemStack stack) {
            var type = SoulPearlData.entityType(stack);
            if (type != null) tooltip.add(Component.translatable("tooltip.wuxingxiantu.soul_spirit", type.getDescription()));
        }
    }
}
