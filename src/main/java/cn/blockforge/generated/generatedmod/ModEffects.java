package cn.blockforge.generated.generatedmod;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.network.chat.Component;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/** 可在物品栏查看说明的五行领域状态效果。领域本身的数值逻辑仍由 CultivationLogic 驱动。 */
public final class ModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, GeneratedMod.MOD_ID);
    public static final RegistryObject<MobEffect> SPIRIT_SHIELD_VISUAL = EFFECTS.register("spirit_shield_visual", () ->
            new MobEffect(MobEffectCategory.BENEFICIAL, 0x37BDE8) {
                @Override public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientMobEffectExtensions> consumer) {
                    consumer.accept(new net.minecraftforge.client.extensions.common.IClientMobEffectExtensions() {
                        @Override public boolean isVisibleInInventory(net.minecraft.world.effect.MobEffectInstance instance){return false;}
                        @Override public boolean isVisibleInGui(net.minecraft.world.effect.MobEffectInstance instance){return false;}
                    });
                }
            });
    public static final RegistryObject<MobEffect> PARALYSIS = EFFECTS.register("paralysis", () ->
            new MobEffect(MobEffectCategory.HARMFUL, 0x79CFFF) {}.addAttributeModifier(
                    Attributes.MOVEMENT_SPEED, "571c5b87-b5a5-4f85-adf1-d682c6ec984c", -1,
                    AttributeModifier.Operation.MULTIPLY_TOTAL));
    public static final RegistryObject<MobEffect> REALM_FLIGHT = register("dunguang",0x79DAD2,"effect.wuxingxiantu.dunguang.description");
    public static final RegistryObject<MobEffect> PILL_GATHERING = register("pill_gathering", 0x55C8B8,
            "effect.wuxingxiantu.pill_gathering.description");
    public static final RegistryObject<MobEffect> PILL_TRIBULATION = register("pill_tribulation", 0xE8CB60,
            "effect.wuxingxiantu.pill_tribulation.description");

    public static final RegistryObject<MobEffect> WOOD_DOMAIN = register("wood_domain", 0x5FAF72,
            "effect.wuxingxiantu.wood_domain.description");
    public static final RegistryObject<MobEffect> FIRE_DOMAIN = register("fire_domain", 0xD95845,
            "effect.wuxingxiantu.fire_domain.description");
    /** 天雷灵根显化后取代木灵领域显示的上位领域。 */
    public static final RegistryObject<MobEffect> THUNDER_DOMAIN = register("thunder_domain", 0x8FC7FF,
            "effect.wuxingxiantu.thunder_domain.description");
    /** 异火灵根显化后取代业火领域显示的上位领域。 */
    public static final RegistryObject<MobEffect> EXOTIC_DOMAIN = register("exotic_domain", 0xFF9A45,
            "effect.wuxingxiantu.exotic_domain.description");
    public static final RegistryObject<MobEffect> GOLD_DOMAIN = register("gold_domain", 0xE5B94F,
            "effect.wuxingxiantu.gold_domain.description");
    public static final RegistryObject<MobEffect> WATER_DOMAIN = register("water_domain", 0x4E9AD0,
            "effect.wuxingxiantu.water_domain.description");
    public static final RegistryObject<MobEffect> EARTH_DOMAIN = register("earth_domain", 0xB9805A,
            "effect.wuxingxiantu.earth_domain.description");
    public static final RegistryObject<MobEffect> TRIBULATION_FEAR = register("tribulation_fear", 0x32152E,
            "effect.wuxingxiantu.tribulation_fear.description");
    public static final RegistryObject<MobEffect> MERIDIAN_DAMAGED = registerAttribute("meridian_damaged", 0x9D5A38,
            "effect.wuxingxiantu.meridian_damaged.description", -0.30D);
    public static final RegistryObject<MobEffect> MERIDIAN_SHATTERED = registerAttribute("meridian_shattered", 0x3B173E,
            "effect.wuxingxiantu.meridian_shattered.description", -0.50D);
    public static final RegistryObject<MobEffect> ARMOR_BREAK = EFFECTS.register("armor_break", () -> new ArmorBreakEffect());
    public static final RegistryObject<MobEffect> FA_TIAN_XIANG_DI = registerDivine(
            "fa_tian_xiang_di", 0xE8B84A, "effect.wuxingxiantu.fa_tian_xiang_di.description", true);
    public static final RegistryObject<MobEffect> YUAN_SHEN_CHU_QIAO = registerDivine(
            "yuan_shen_chu_qiao", 0xB978D1, "effect.wuxingxiantu.yuan_shen_chu_qiao.description", false);
    public static final RegistryObject<MobEffect> YI_HUN_JI_PO = registerDivine(
            "yi_hun_ji_po", 0x6E3FA8, "effect.wuxingxiantu.yi_hun_ji_po.description", false);

    private ModEffects() {}
    public static final RegistryObject<MobEffect> TALISMAN_GATHERING = register("talisman_gathering",0x79DFFF,"effect.wuxingxiantu.talisman_gathering.description");
    public static final RegistryObject<MobEffect> TALISMAN_WARD = register("talisman_ward",0x8863A6,"effect.wuxingxiantu.talisman_ward.description");
    public static final RegistryObject<MobEffect> TALISMAN_PURGE = register("talisman_purge",0xFFD369,"effect.wuxingxiantu.talisman_purge.description");
    public static final RegistryObject<MobEffect> TALISMAN_BIND = EFFECTS.register("talisman_bind",()->
            new MobEffect(MobEffectCategory.HARMFUL,0x547D35) {}.addAttributeModifier(Attributes.MOVEMENT_SPEED,
                    "45b4c793-7d16-45bc-8f68-1b45ef21f119",-1,AttributeModifier.Operation.MULTIPLY_TOTAL));

    private static RegistryObject<MobEffect> register(String id, int color, String descriptionKey) {
        return EFFECTS.register(id, () -> new DomainEffect(color, descriptionKey));
    }

    private static RegistryObject<MobEffect> registerAttribute(String id, int color, String descriptionKey, double amount) {
        return EFFECTS.register(id, () -> new AttributeEffect(color, descriptionKey, amount));
    }

    private static RegistryObject<MobEffect> registerDivine(String id, int color, String descriptionKey, boolean giant) {
        return EFFECTS.register(id, () -> new DivineEffect(color, descriptionKey, giant));
    }

    private static final class DivineEffect extends MobEffect {
        private final String descriptionKey;
        private final boolean giant;

        private DivineEffect(int color, String descriptionKey, boolean giant) {
            super(MobEffectCategory.BENEFICIAL, color);
            this.descriptionKey = descriptionKey;
            this.giant = giant;
            if (giant) {
                addAttributeModifier(Attributes.ATTACK_DAMAGE,
                        "a4d9b3c1-49cf-4d01-9c34-000000000102", 0.50D, AttributeModifier.Operation.MULTIPLY_TOTAL);
                addAttributeModifier(Attributes.MAX_HEALTH,
                        "a4d9b3c1-49cf-4d01-9c34-000000000103", 1.0D, AttributeModifier.Operation.MULTIPLY_TOTAL);
                addAttributeModifier(Attributes.ARMOR,
                        "a4d9b3c1-49cf-4d01-9c34-000000000104", 20.0D, AttributeModifier.Operation.ADDITION);
                addAttributeModifier(Attributes.KNOCKBACK_RESISTANCE,
                        "a4d9b3c1-49cf-4d01-9c34-000000000106", 1.0D, AttributeModifier.Operation.ADDITION);
                addAttributeModifier(Attributes.MOVEMENT_SPEED,
                        "a4d9b3c1-49cf-4d01-9c34-000000000105", -0.30D, AttributeModifier.Operation.MULTIPLY_TOTAL);
            }
        }

        @Override
        public boolean isDurationEffectTick(int duration, int amplifier) {
            return true;
        }

        @Override
        public void applyEffectTick(net.minecraft.world.entity.LivingEntity entity, int amplifier) {
            if (!(entity instanceof net.minecraft.server.level.ServerPlayer player)) return;
            if (giant) {
                player.setSprinting(false);
                if (!player.hasEffect(net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE)
                        || player.getEffect(net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE).getAmplifier() < 3) {
                    player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                            net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE, 45, 3, true, false, true));
                }
            } else {
                // 方块碰撞由 DivineArtLogic 按具体神通统一管理：元神出窍可穿墙，
                // 移魂寄魄必须保留碰撞，不能在效果 tick 中无条件打开 noPhysics。
                player.setNoGravity(true);
                player.getAbilities().mayfly = true;
                player.getAbilities().flying = true;
                player.getAbilities().setFlyingSpeed(0.08F);
                player.onUpdateAbilities();
            }
        }

        @Override
        public Component getDisplayName() {
            return Component.translatable(getDescriptionId())
                    .append(Component.literal("："))
                    .append(Component.translatable(descriptionKey));
        }
    }

    private static final class DomainEffect extends MobEffect {
        private final String descriptionKey;

        private DomainEffect(int color, String descriptionKey) {
            super(MobEffectCategory.BENEFICIAL, color);
            this.descriptionKey = descriptionKey;
        }

        @Override
        public Component getDisplayName() {
            return Component.translatable(getDescriptionId())
                    .append(Component.literal("："))
                    .append(Component.translatable(descriptionKey));
        }
    }

    private static final class AttributeEffect extends MobEffect {
        private final String descriptionKey;

        private AttributeEffect(int color, String descriptionKey, double amount) {
            super(MobEffectCategory.HARMFUL, color);
            this.descriptionKey = descriptionKey;
            addAttributeModifier(Attributes.MOVEMENT_SPEED,
                    "b4d7d9d6-8a26-4b7b-9f37-4d04b7f2a101", amount, AttributeModifier.Operation.MULTIPLY_TOTAL);
            addAttributeModifier(Attributes.ATTACK_DAMAGE,
                    "b4d7d9d6-8a26-4b7b-9f37-4d04b7f2a102", amount, AttributeModifier.Operation.MULTIPLY_TOTAL);
            addAttributeModifier(Attributes.ARMOR,
                    "b4d7d9d6-8a26-4b7b-9f37-4d04b7f2a103", amount, AttributeModifier.Operation.MULTIPLY_TOTAL);
            addAttributeModifier(Attributes.MAX_HEALTH,
                    "b4d7d9d6-8a26-4b7b-9f37-4d04b7f2a104", amount, AttributeModifier.Operation.MULTIPLY_TOTAL);
        }

        @Override
        public Component getDisplayName() {
            return Component.translatable(getDescriptionId())
                    .append(Component.literal("："))
                    .append(Component.translatable(descriptionKey));
        }
    }

    private static final class ArmorBreakEffect extends MobEffect {
        ArmorBreakEffect() {
            super(MobEffectCategory.HARMFUL, 0x8E3B3B);
            addAttributeModifier(Attributes.ARMOR, "b4d7d9d6-8a26-4b7b-9f37-4d04b7f2a105", -0.50D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
            addAttributeModifier(Attributes.ARMOR_TOUGHNESS, "b4d7d9d6-8a26-4b7b-9f37-4d04b7f2a106", -0.50D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
        }
        @Override public Component getDisplayName() {
            return Component.translatable(getDescriptionId());
        }
    }
}
