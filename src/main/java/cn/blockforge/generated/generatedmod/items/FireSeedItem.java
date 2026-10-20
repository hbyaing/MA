package cn.blockforge.generated.generatedmod.items;

import cn.blockforge.generated.generatedmod.WuXingItems;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import cn.blockforge.generated.generatedmod.fire.FireBottleData;
import cn.blockforge.generated.generatedmod.fire.FireEvolution;
import cn.blockforge.generated.generatedmod.fire.FireRefinement;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.UseAnim;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

/** Bottles retain their contents through refinement and evolve in place. */
public final class FireSeedItem extends Item {
    private final int tier;

    public FireSeedItem(int tier, Properties properties) {
        super(properties);
        this.tier = tier;
    }

    public int tier() { return tier; }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (tier == 0) return InteractionResultHolder.pass(held);
        if (player.isShiftKeyDown() && hand == InteractionHand.MAIN_HAND) {
            if (player instanceof ServerPlayer server) FireEvolution.evolve(server, held);
            return InteractionResultHolder.sidedSuccess(held, level.isClientSide);
        }
        if (level.isClientSide) { player.startUsingItem(hand); return InteractionResultHolder.consume(held); }
        return player instanceof ServerPlayer server && FireRefinement.start(server, held, hand)
                ? InteractionResultHolder.consume(held) : InteractionResultHolder.fail(held);
    }

    @Override public int getUseDuration(ItemStack stack) { return FireRefinement.CHANNEL_TICKS; }
    @Override public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.BOW; }
    @Override public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remaining) {
        if (entity instanceof ServerPlayer player && !FireRefinement.valid(player, stack)) FireRefinement.cancel(player);
    }
    @Override public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity instanceof ServerPlayer player) FireRefinement.finish(player, stack);
        return stack;
    }
    @Override public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remaining) {
        if (entity instanceof ServerPlayer player) FireRefinement.cancel(player);
    }
    @Override public Component getName(ItemStack stack) {
        int contained = FireBottleData.tier(stack);
        return contained == 0 ? super.getName(stack) : Component.translatable("item.wuxingxiantu.filled_fire_bottle",
                Component.translatable(cn.blockforge.generated.generatedmod.ExoticFireColorRegistry.nameKey(contained)));
    }
    @Override public void appendHoverText(ItemStack stack, Level level, java.util.List<Component> lines,
                                          net.minecraft.world.item.TooltipFlag flag) {
        int contained = FireBottleData.tier(stack);
        if (contained == 0) return;
        long time=level==null?0:level.getGameTime();
        lines.add(Component.translatable("tooltip.wuxingxiantu.fire_tier", contained).withStyle(style->style.withColor(
                cn.blockforge.generated.generatedmod.ExoticFireColorRegistry.accentColor(contained,time))));
        var fire=cn.blockforge.generated.generatedmod.fire.FireCatalog.tier(contained);
        lines.add(Component.translatable("tooltip.wuxingxiantu.fire_rarity",
                Component.translatable("fire_rarity.wuxingxiantu."+fire.rarity())).withStyle(net.minecraft.ChatFormatting.GRAY));
        for (var essence : FireBottleData.Essence.values()) {
            double amount = FireBottleData.essence(stack, essence);
            if (amount > 0) lines.add(Component.translatable("tooltip.wuxingxiantu.fire_essence",
                    Component.translatable("essence.wuxingxiantu." + essence.name().toLowerCase(java.util.Locale.ROOT)),
                    String.format(java.util.Locale.ROOT, "%.1f", amount)).withStyle(net.minecraft.ChatFormatting.GOLD));
        }
        boolean hasRoute=FireEvolution.ROUTES.stream().anyMatch(route->route.from()==contained);
        boolean ready=FireEvolution.hasEssenceForRoute(stack);
        lines.add(Component.translatable(contained==9?"tooltip.wuxingxiantu.fire_final":!hasRoute
                ?"tooltip.wuxingxiantu.fire_no_route":ready?"tooltip.wuxingxiantu.fire_ready":"tooltip.wuxingxiantu.fire_growing")
                .withStyle(ready?net.minecraft.ChatFormatting.GREEN:net.minecraft.ChatFormatting.GRAY));
        lines.add(Component.translatable("tooltip.wuxingxiantu.fire_refinement").withStyle(net.minecraft.ChatFormatting.GRAY));
    }

    public static String getFireName(int tier) {
        return switch (Math.max(1, Math.min(9, tier))) {
            case 1 -> "赤炎流火";
            case 2 -> "熔金淬火";
            case 3 -> "枯骨冷火";
            case 4 -> "青木灵火";
            case 5 -> "碧海沧火";
            case 6 -> "紫霄雷火";
            case 7 -> "大日圣火";
            case 8 -> "九幽冥火";
            default -> "涅槃神火";
        };
    }

    public static Item seedForTier(int tier) {
        return switch (Math.max(1, Math.min(9, tier))) {
            case 1 -> WuXingItems.FIRE_SEED_RED_FLOW.get();
            case 2 -> WuXingItems.FIRE_SEED_MOLTEN_GOLD.get();
            case 3 -> WuXingItems.FIRE_SEED_COLD_ASH.get();
            case 4 -> WuXingItems.FIRE_SEED_AZURE_WOOD.get();
            case 5 -> WuXingItems.FIRE_SEED_COBALT_SEA.get();
            case 6 -> WuXingItems.FIRE_SEED_VIOLET_THUNDER.get();
            case 7 -> WuXingItems.FIRE_SEED_SUN_SAINT.get();
            case 8 -> WuXingItems.FIRE_SEED_NETHER_VOID.get();
            default -> WuXingItems.FIRE_SEED_NIRVANA.get();
        };
    }
}
