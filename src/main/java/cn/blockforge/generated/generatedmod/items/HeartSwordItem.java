package cn.blockforge.generated.generatedmod.items;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.UUID;

/** 心念凝成的本体剑；它不属于普通灵剑注册表，也不允许交易或附魔。 */
public final class HeartSwordItem extends SwordItem {
    public static final String OWNER_TAG = "HeartOwner";

    public HeartSwordItem(Item.Properties properties) {
        super(Tiers.DIAMOND, 4, -2.4F, properties.stacksTo(1));
    }

    public static ItemStack create(UUID owner) {
        ItemStack stack = new ItemStack(cn.blockforge.generated.generatedmod.WuXingItems.HEART_SWORD.get());
        stack.getOrCreateTag().putUUID(OWNER_TAG, owner);
        return stack;
    }

    public static boolean isOwnedBy(ItemStack stack, UUID owner) {
        return stack.getItem() instanceof HeartSwordItem
                && owner != null
                && stack.hasTag()
                && stack.getTag().hasUUID(OWNER_TAG)
                && owner.equals(stack.getTag().getUUID(OWNER_TAG));
    }

    public static UUID owner(ItemStack stack) {
        return stack.hasTag() && stack.getTag().hasUUID(OWNER_TAG)
                ? stack.getTag().getUUID(OWNER_TAG) : null;
    }

    @Override public boolean isEnchantable(ItemStack stack) { return false; }
    @Override public com.google.common.collect.Multimap<net.minecraft.world.entity.ai.attributes.Attribute,
            net.minecraft.world.entity.ai.attributes.AttributeModifier> getDefaultAttributeModifiers(net.minecraft.world.entity.EquipmentSlot slot) {
        var result=com.google.common.collect.ImmutableMultimap.<net.minecraft.world.entity.ai.attributes.Attribute,
                net.minecraft.world.entity.ai.attributes.AttributeModifier>builder();
        // The constructor's compatibility damage must not appear as a second authority in tooltips.
        super.getDefaultAttributeModifiers(slot).forEach((attribute,modifier)->{
            if(attribute!=net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE)result.put(attribute,modifier);
        });
        return result.build();
    }
    @Override public boolean isBookEnchantable(ItemStack stack, ItemStack book) { return false; }
    @Override public boolean isRepairable(ItemStack stack) { return false; }
    @Override public boolean isDamageable(ItemStack stack) { return false; }
    @Override public int getMaxDamage(ItemStack stack) { return 0; }
    @Override public void setDamage(ItemStack stack, int damage) { if (stack.hasTag()) stack.getTag().remove("Damage"); }
    @Override public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) { return true; }
    @Override public void inventoryTick(ItemStack stack, Level level, net.minecraft.world.entity.Entity entity,
                                        int slot, boolean selected) {
        if (!level.isClientSide && entity instanceof net.minecraft.server.level.ServerPlayer player
                && !isOwnedBy(stack, player.getUUID())) {
            player.getInventory().setItem(slot, ItemStack.EMPTY);
            return;
        }
        super.inventoryTick(stack, level, entity, slot, selected);
    }
    @Override public boolean mineBlock(ItemStack stack, Level level, net.minecraft.world.level.block.state.BlockState state,
                                       net.minecraft.core.BlockPos pos, LivingEntity miner) { return true; }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.wuxingxiantu.heart_sword_contract"));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
