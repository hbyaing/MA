package cn.blockforge.generated.generatedmod.items;

import cn.blockforge.generated.generatedmod.CultivationData;
import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.ProgressionGate;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** 功法秘籍：右键学习一次，学习后自动装备到第一个空槽。 */
public final class ArtManualItem extends Item {
    private final String artId;
    private final int tier;
    public ArtManualItem(String artId, int tier, Properties properties) { super(properties); this.artId = artId; this.tier = tier; }
    public String artId() { return artId; }
    public int tier() { return tier; }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) return InteractionResultHolder.sidedSuccess(stack, true);
        CultivationData data = GeneratedMod.data(player);
        if (data == null) {
            player.displayClientMessage(Component.translatable("message.wuxingxiantu.art_need_foundation"), true);
            return InteractionResultHolder.fail(stack);
        }
        // 统一进度门槛：每门功法有独立最低境界；境界不足不消耗秘籍、不学习。
        if (!ProgressionGate.canLearnArt(player, artId)) {
            ProgressionGate.deny(player, ProgressionGate.MSG_ART_LEARN, ProgressionGate.requiredRealmForArt(artId));
            return InteractionResultHolder.fail(stack);
        }
        if (!data.wuxingxiantu$learnArt(artId)) {
            player.displayClientMessage(Component.translatable("message.wuxingxiantu.art_already_learned"), true);
            return InteractionResultHolder.fail(stack);
        }
        if (!cn.blockforge.generated.generatedmod.yujian.SwordTechniqueRegistry.isTechnique(artId)) {
            String[] equipped = data.wuxingxiantu$getEquippedArts();
            int unlocked = CultivationLogic.unlockedArtSlots(data.wuxingxiantu$getRealm());
            for (int slot = 0; slot < unlocked; slot++) if (equipped[slot] == null) { data.wuxingxiantu$equipArt(slot, artId); break; }
        }
        stack.shrink(1);
        player.displayClientMessage(Component.translatable("message.wuxingxiantu.art_learned", getDescription()), false);
        GeneratedMod.sync((net.minecraft.server.level.ServerPlayer) player);
        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    @Override public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        return player == null ? InteractionResult.PASS
                : use(context.getLevel(), player, context.getHand()).getResult();
    }
}
