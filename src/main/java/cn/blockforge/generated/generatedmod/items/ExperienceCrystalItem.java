package cn.blockforge.generated.generatedmod.items;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/** 化魄炉凝结的经验单位；每枚固定储存 5 点原版经验。 */
public final class ExperienceCrystalItem extends Item {
    public static final int EXPERIENCE_PER_CRYSTAL = 5;

    public ExperienceCrystalItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            player.giveExperiencePoints(EXPERIENCE_PER_CRYSTAL);
            if (!player.getAbilities().instabuild) stack.shrink(1);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(
                "tooltip.wuxingxiantu.experience_crystal", EXPERIENCE_PER_CRYSTAL));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
