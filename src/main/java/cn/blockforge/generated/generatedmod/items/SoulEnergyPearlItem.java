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

/** 用于复活人皇幡内战死魂魄的魂力容器。 */
public final class SoulEnergyPearlItem extends Item {
    private final boolean creative;

    public SoulEnergyPearlItem(Properties properties, boolean creative) {
        super(properties);
        this.creative = creative;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            player.displayClientMessage(Component.translatable(
                    creative ? "message.wuxingxiantu.creative_soul_energy_pearl" :
                            "message.wuxingxiantu.soul_energy_pearl_status",
                    SoulPearlData.soulEnergy(stack), SoulPearlData.MAX_SOUL_ENERGY), true);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable(creative
                ? "tooltip.wuxingxiantu.creative_soul_energy_pearl"
                : "tooltip.wuxingxiantu.soul_energy_pearl"));
        if (!creative) tooltip.add(Component.translatable("tooltip.wuxingxiantu.soul_energy", 
                SoulPearlData.soulEnergy(stack), SoulPearlData.MAX_SOUL_ENERGY));
    }
}
