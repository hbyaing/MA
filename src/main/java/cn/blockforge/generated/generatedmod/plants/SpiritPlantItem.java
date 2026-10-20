package cn.blockforge.generated.generatedmod.plants;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 灵植物品：既是药材，也是繁殖材料（植株本体可再次栽培养护）。
 * Tooltip 固定两行：药性 + 主要培养提示，另附野外稀有度。
 * 完整配方信息留给第三批丹方系统，本轮不做图鉴。
 */
public class SpiritPlantItem extends BlockItem {

    public SpiritPlantItem(SpiritPlantBlock block, Properties properties) {
        super(block, properties);
    }

    @Override
    public net.minecraft.world.InteractionResultHolder<ItemStack> use(Level level, net.minecraft.world.entity.player.Player player,
            net.minecraft.world.InteractionHand hand) {
        if (!((SpiritPlantBlock)getBlock()).definition().semiAquatic) return super.use(level, player, hand);
        var hit = getPlayerPOVHitResult(level, player, net.minecraft.world.level.ClipContext.Fluid.SOURCE_ONLY);
        if (hit.getType() != net.minecraft.world.phys.HitResult.Type.BLOCK)
            return net.minecraft.world.InteractionResultHolder.pass(player.getItemInHand(hand));
        var pos = hit.getBlockPos();
        if (!Substrate.isPlantWater(level.getFluidState(pos)) || !level.getFluidState(pos).isSource())
            return net.minecraft.world.InteractionResultHolder.pass(player.getItemInHand(hand));
        var surface = pos.above();
        if (!level.mayInteract(player, pos) || !player.mayUseItemAt(surface, net.minecraft.core.Direction.UP, player.getItemInHand(hand)))
            return net.minecraft.world.InteractionResultHolder.fail(player.getItemInHand(hand));
        var placementHit = new net.minecraft.world.phys.BlockHitResult(
                new net.minecraft.world.phys.Vec3(pos.getX()+.5,pos.getY()+1,pos.getZ()+.5),net.minecraft.core.Direction.UP,surface,false);
        var result = place(new net.minecraft.world.item.context.BlockPlaceContext(player, hand, player.getItemInHand(hand), placementHit));
        return new net.minecraft.world.InteractionResultHolder<>(result, player.getItemInHand(hand));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        appendHerbTooltip(getBlock(), tooltip);
    }

    /** 供其它灵植 BlockItem（龙鳞草、蓝蘑菇）复用的统一 Tooltip。 */
    public static void appendHerbTooltip(net.minecraft.world.level.block.Block block, List<Component> tooltip) {
        if (!(block instanceof SpiritPlantBlock plant)) return;
        SpiritPlantDefinition def = plant.definition();
        tooltip.add(Component.translatable("tooltip.wuxingxiantu.herb_properties",
                propertyNames(def)).withStyle(net.minecraft.ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.wuxingxiantu.wild_rarity",
                Component.translatable(def.rarity.translationKey())).withStyle(style->style.withColor(switch(def.rarity) {
                    case UNCOMMON -> cn.blockforge.generated.generatedmod.text.ItemTextPalette.UNCOMMON;
                    case RARE -> cn.blockforge.generated.generatedmod.text.ItemTextPalette.RARE;
                    case VERY_RARE -> cn.blockforge.generated.generatedmod.text.ItemTextPalette.EPIC;
                    case SPECIAL -> cn.blockforge.generated.generatedmod.text.ItemTextPalette.LEGENDARY;
                    default -> cn.blockforge.generated.generatedmod.text.ItemTextPalette.COMMON;
                })));
        tooltip.add(Component.translatable("tooltip.wuxingxiantu.hint." + def.id).withStyle(net.minecraft.ChatFormatting.GRAY));
    }

    /** 用枚举顺序拼接所有药性翻译名。 */
    public static Component propertyNames(SpiritPlantDefinition def) {
        Component result = null;
        for (HerbProperty property : def.properties) {
            Component name = Component.translatable(property.translationKey());
            if (result == null) result = name;
            else result = result.plainCopy().append(" · ").append(name);
        }
        return result == null ? Component.empty() : result;
    }
}
