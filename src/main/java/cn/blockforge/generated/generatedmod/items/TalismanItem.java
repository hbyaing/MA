package cn.blockforge.generated.generatedmod.items;

import cn.blockforge.generated.generatedmod.ProgressionGate;
import cn.blockforge.generated.generatedmod.TalismanCatalog;
import cn.blockforge.generated.generatedmod.TalismanData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.List;

/** 符咒：刻印或绘制的成品符箓，右键即触发对应效果并消耗一张。 */
public final class TalismanItem extends Item {
    private final String talismanId;
    public TalismanItem(Properties properties) {
        this(properties, "");
    }
    public TalismanItem(Properties properties, String talismanId) {
        super(properties);
        this.talismanId=talismanId;
    }
    public String talismanId(){return talismanId;}

    @Override public Component getName(ItemStack stack) {
        var quality=TalismanData.quality(stack);
        String id=TalismanData.getTalisman(stack);
        Component name=TalismanCatalog.has(id)?TalismanCatalog.title(id):super.getName(stack);
        return quality==null?name:cn.blockforge.generated.generatedmod.text.ItemTextPalette.colored(
                name,cn.blockforge.generated.generatedmod.text.ItemTextPalette.talismanGrade(quality.ordinal()));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        String id = TalismanData.getTalisman(stack);
        if (!TalismanCatalog.has(id)) {
            player.displayClientMessage(Component.translatable("message.wuxingxiantu.talisman_empty"), true);
            return InteractionResultHolder.fail(stack);
        }
        // 统一进度门槛：成品符箓按品质限制使用资格；购买/赠予的渠道不受影响。
        TalismanCatalog.Quality quality = TalismanData.quality(stack);
        if (!ProgressionGate.canUseTalisman(player, quality)) {
            if (!level.isClientSide) {
                ProgressionGate.deny(player, ProgressionGate.MSG_TALISMAN, ProgressionGate.requiredRealmForTalisman(quality));
            }
            return InteractionResultHolder.fail(stack);
        }
        if (TalismanCatalog.effect(id, player, level, stack)) {
            if (!level.isClientSide) {
                if (!player.getAbilities().instabuild) stack.shrink(1);
                player.displayClientMessage(Component.translatable("message.wuxingxiantu.talisman_used",
                        TalismanCatalog.title(id)), true);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
        return InteractionResultHolder.fail(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        String id = TalismanData.getTalisman(stack);
        if (TalismanCatalog.has(id)) {
            if(TalismanCatalog.modelCode(id)>0) tooltip.add(Component.translatable(TalismanCatalog.nameKey(id)+".description")
                    .withStyle(net.minecraft.ChatFormatting.AQUA));
            tooltip.add(Component.translatable("tooltip.wuxingxiantu.talisman_kind",
                    TalismanCatalog.title(id)).withStyle(net.minecraft.ChatFormatting.AQUA));
            net.minecraft.network.chat.Component quality =
                    TalismanData.quality(stack) == null
                            ? Component.translatable("tooltip.wuxingxiantu.talisman_no_quality")
                            : TalismanData.quality(stack).title();
            tooltip.add(Component.translatable("tooltip.wuxingxiantu.talisman_quality", quality)
                    .withStyle(style->style.withColor(cn.blockforge.generated.generatedmod.text.ItemTextPalette.talismanGrade(TalismanData.quality(stack)==null?0:TalismanData.quality(stack).ordinal()))));
            tooltip.add(Component.translatable("pill.wuxingxiantu.requirement",
                    cn.blockforge.generated.generatedmod.items.CultivationLogic.realmName(ProgressionGate.requiredRealmForTalisman(TalismanData.quality(stack))))
                    .withStyle(net.minecraft.ChatFormatting.GOLD));
            tooltip.add(Component.translatable("tooltip.wuxingxiantu.talisman_use").withStyle(net.minecraft.ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.translatable("tooltip.wuxingxiantu.talisman_empty").withStyle(net.minecraft.ChatFormatting.DARK_GRAY));
        }
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
