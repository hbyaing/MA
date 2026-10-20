package cn.blockforge.generated.generatedmod.items;

import cn.blockforge.generated.generatedmod.CultivationData;
import cn.blockforge.generated.generatedmod.GeneratedMod;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.level.Level;

public class AuraItem extends Item {
    private final float restorePercent;
    public AuraItem(float restorePercent, Properties properties) { super(properties); this.restorePercent = restorePercent; }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) { ItemStack stack = player.getItemInHand(hand); CultivationData data = GeneratedMod.data(player); if (!level.isClientSide && data != null) { float max = CultivationLogic.maxAura(data), before = data.wuxingxiantu$getAura(); data.wuxingxiantu$setAura(Math.min(max, before + max * restorePercent)); if (data.wuxingxiantu$getAura() > before) { stack.shrink(1); player.displayClientMessage(Component.translatable("message.wuxingxiantu.aura_restored", Math.round(data.wuxingxiantu$getAura() - before)), true); } } return InteractionResultHolder.sidedSuccess(stack, level.isClientSide); }
}
