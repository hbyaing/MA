package cn.blockforge.generated.generatedmod.items;

import cn.blockforge.generated.generatedmod.CultivationData;
import cn.blockforge.generated.generatedmod.DivineArtCatalog;
import cn.blockforge.generated.generatedmod.DivineArtLogic;
import cn.blockforge.generated.generatedmod.GeneratedMod;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** 神通传承物品：学习后进入功法轮盘外圈。 */
public final class DivineManualItem extends Item {
    private final String divineId;
    public DivineManualItem(String divineId, Properties properties) { super(properties); this.divineId = divineId; }
    public String divineId() { return divineId; }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) return InteractionResultHolder.sidedSuccess(stack, true);
        CultivationData data = GeneratedMod.data(player);
        if (data == null || !DivineArtCatalog.isValid(divineId)) return InteractionResultHolder.fail(stack);
        if (data.wuxingxiantu$getRealm() < DivineArtCatalog.requiredRealm(divineId)) {
            player.displayClientMessage(Component.translatable("message.wuxingxiantu.divine_need_realm"), true);
            return InteractionResultHolder.fail(stack);
        }
        if (data.wuxingxiantu$hasLearnedDivineArt(divineId)) {
            // 已学神通若尚未装备，自动填入首个空槽，保证传承物和轮盘都能直接触发。
            equipIfMissing(data);
            if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer
                    && DivineArtCatalog.entry(divineId) != null) {
                DivineArtLogic.release(serverPlayer, divineId);
            }
            return InteractionResultHolder.sidedSuccess(stack, false);
        }
        if (!data.wuxingxiantu$learnDivineArt(divineId)) {
            player.displayClientMessage(Component.translatable("message.wuxingxiantu.divine_already_learned"), true);
            return InteractionResultHolder.fail(stack);
        }
        equipIfMissing(data);
        // 传承物同时作为已学神通的指定触发物，学习后保留在背包中。
        player.displayClientMessage(Component.translatable("message.wuxingxiantu.divine_learned", DivineArtCatalog.name(divineId)), false);
        GeneratedMod.sync((net.minecraft.server.level.ServerPlayer) player);
        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    private void equipIfMissing(CultivationData data) {
        for (int slot = 0; slot < 3; slot++) {
            if (divineId.equals(data.wuxingxiantu$getEquippedDivineArt(slot))) return;
        }
        for (int slot = 0; slot < 3; slot++) {
            if (data.wuxingxiantu$getEquippedDivineArt(slot).isBlank()) {
                data.wuxingxiantu$setEquippedDivineArt(slot, divineId);
                return;
            }
        }
    }
}
