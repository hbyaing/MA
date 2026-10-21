package cn.blockforge.generated.generatedmod;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** 灵力附魔注册与适用范围定义。 */
public final class SpiritEnchantment {
    public static final DeferredRegister<Enchantment> ENCHANTMENTS =
            DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, GeneratedMod.MOD_ID);

    public static final RegistryObject<Enchantment> SPIRIT_MENDING = ENCHANTMENTS.register(
            "spirit_mending", () -> new SpiritMendingEnchantment());
    public static final RegistryObject<Enchantment> SPIRIT_GUARD = ENCHANTMENTS.register(
            "spirit_guard", () -> new SpiritGuardEnchantment());
    public static final RegistryObject<Enchantment> SPIRIT_SURGE = ENCHANTMENTS.register(
            "spirit_surge", () -> new SpiritSurgeEnchantment());
    public static final RegistryObject<Enchantment> SPIRIT_DRAIN = ENCHANTMENTS.register(
            "spirit_drain", () -> new SpiritDrainEnchantment());
    public static final RegistryObject<Enchantment> LIGHTNESS = ENCHANTMENTS.register(
            "lightness", () -> new LightnessEnchantment());
    public static final RegistryObject<Enchantment> EXOTIC_FIRE_WRAP = ENCHANTMENTS.register(
            "exotic_fire_wrap", () -> new ExoticFireWrapEnchantment());

    private static final EquipmentSlot[] ALL_SLOTS = {
            EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND, EquipmentSlot.HEAD,
            EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };
    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };
    private static final EquipmentSlot[] WEAPON_SLOTS = {EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND};

    private static abstract class BaseEnchantment extends Enchantment {
        private final int maxLevel;
        private final int minCost;

        protected BaseEnchantment(Rarity rarity, EnchantmentCategory category, EquipmentSlot[] slots,
                                  int maxLevel, int minCost) {
            super(rarity, category, slots);
            this.maxLevel = maxLevel;
            this.minCost = minCost;
        }

        @Override public int getMaxLevel() { return maxLevel; }
        @Override public int getMinCost(int level) { return minCost + (level - 1) * 10; }
        @Override public int getMaxCost(int level) { return getMinCost(level) + 15; }
        @Override public boolean canApplyAtEnchantingTable(ItemStack stack) { return canEnchant(stack); }
    }

    private static final class SpiritMendingEnchantment extends BaseEnchantment {
        private SpiritMendingEnchantment() {
            super(Rarity.RARE, EnchantmentCategory.BREAKABLE, ALL_SLOTS, 5, 10);
        }

        @Override public boolean canEnchant(ItemStack stack) { return stack.isDamageableItem(); }
        @Override protected boolean checkCompatibility(Enchantment other) {
            return other != Enchantments.MENDING && super.checkCompatibility(other);
        }
    }

    private static final class SpiritGuardEnchantment extends BaseEnchantment {
        private SpiritGuardEnchantment() {
            super(Rarity.RARE, EnchantmentCategory.ARMOR, ARMOR_SLOTS, 4, 15);
        }

        @Override public boolean canEnchant(ItemStack stack) { return false; }
        @Override public boolean isDiscoverable(){return false;}
        @Override public boolean isTradeable(){return false;}
        @Override public boolean isAllowedOnBooks(){return false;}
    }

    private static final class SpiritSurgeEnchantment extends BaseEnchantment {
        private SpiritSurgeEnchantment() {
            super(Rarity.RARE, EnchantmentCategory.ARMOR, ARMOR_SLOTS, 3, 10);
        }

        @Override public boolean canEnchant(ItemStack stack) { return stack.getItem() instanceof ArmorItem; }
    }

    private static final class SpiritDrainEnchantment extends BaseEnchantment {
        private SpiritDrainEnchantment() {
            super(Rarity.RARE, EnchantmentCategory.WEAPON, WEAPON_SLOTS, 5, 15);
        }

        @Override public boolean canEnchant(ItemStack stack) {
            return stack.getItem() instanceof SwordItem
                    || stack.getItem() instanceof AxeItem
                    || stack.getItem() instanceof BowItem
                    || stack.getItem() instanceof CrossbowItem;
        }
    }

    private static final class LightnessEnchantment extends BaseEnchantment {
        private LightnessEnchantment() {
            super(Rarity.UNCOMMON, EnchantmentCategory.WEAPON, WEAPON_SLOTS, 3, 10);
        }

        @Override public boolean canEnchant(ItemStack stack) {
            return stack.getItem() instanceof SwordItem
                    || stack.getItem() instanceof AxeItem
                    || stack.getItem() instanceof BowItem
                    || stack.getItem() instanceof CrossbowItem;
        }
    }

    /** 异火缠绕：防具与武器共用的一类附魔，可出现在同一名词分类下。 */
    private static final class ExoticFireWrapEnchantment extends BaseEnchantment {
        private static final EnchantmentCategory ARMOR_AND_WEAPON =
                EnchantmentCategory.create("armor_and_weapon", ExoticFireWrapEnchantment::isArmorOrWeaponItem);

        private ExoticFireWrapEnchantment() {
            super(Rarity.RARE, ARMOR_AND_WEAPON, ALL_SLOTS, 5, 15);
        }

        private static boolean isArmorOrWeaponItem(net.minecraft.world.item.Item item) {
            return item instanceof ArmorItem
                    || cn.blockforge.generated.generatedmod.fire.WeaponFlameState.eligible(item);
        }

        @Override public boolean canEnchant(ItemStack stack) { return isArmorOrWeaponItem(stack.getItem()); }
        @Override protected boolean checkCompatibility(Enchantment other) {
            return other != Enchantments.FIRE_ASPECT && super.checkCompatibility(other);
        }
    }

    private SpiritEnchantment() {}
}
