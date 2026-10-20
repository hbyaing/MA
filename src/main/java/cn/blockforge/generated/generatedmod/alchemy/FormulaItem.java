package cn.blockforge.generated.generatedmod.alchemy;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.WuXingItems;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public final class FormulaItem extends Item {
    public FormulaItem(Properties properties) { super(properties); }

    public static ItemStack create(PillCatalog pill) {
        ItemStack stack = new ItemStack(WuXingItems.ALCHEMY_FORMULA.get());
        stack.getOrCreateTag().putString("FormulaId", GeneratedMod.MOD_ID + ":" + pill.id);
        return stack;
    }

    private static ResourceLocation formula(ItemStack stack) {
        return stack.hasTag() ? ResourceLocation.tryParse(stack.getTag().getString("FormulaId")) : null;
    }

    @Override public Component getName(ItemStack stack) {
        ResourceLocation id = formula(stack);
        return id == null ? super.getName(stack) : Component.translatable("item.wuxingxiantu.formula_name",
                Component.translatable("item.wuxingxiantu." + id.getPath()));
    }

    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(player instanceof ServerPlayer server)) return InteractionResultHolder.success(stack);
        ResourceLocation formula = formula(stack);
        if (!FormulaKnowledge.valid(formula)) return InteractionResultHolder.fail(stack);
        boolean unlocked=FormulaKnowledge.unlock(server);
        boolean learned=FormulaKnowledge.learn(server, formula);
        if (!unlocked && !learned) {
            player.displayClientMessage(Component.translatable("message.wuxingxiantu.formula_known_or_invalid"), true);
            return InteractionResultHolder.fail(stack);
        }
        if (!player.isCreative()) stack.shrink(1);
        return InteractionResultHolder.consume(stack);
    }

    @Override public void appendHoverText(ItemStack stack, Level level, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.translatable("tooltip.wuxingxiantu.formula_shared_grades"));
    }
}
