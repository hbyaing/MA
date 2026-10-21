package cn.blockforge.generated.generatedmod.items;

import cn.blockforge.generated.generatedmod.Network;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.List;

/** 寻真司南：右键打开界面输入生物名称，搜寻距离最近的生物。 */
public final class CreatureCompassItem extends Item {
    public CreatureCompassItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        // 让服务端回发打开界面事件，避免物品类直接引用客户端界面类（专用服务器会类加载失败）。
        if (player instanceof net.minecraft.server.level.ServerPlayer server)
            cn.blockforge.generated.generatedmod.network.service.CompassService.open(server);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.wuxingxiantu.creature_compass"));
    }
}
