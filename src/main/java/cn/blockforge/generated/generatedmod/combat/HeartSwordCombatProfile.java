package cn.blockforge.generated.generatedmod.combat;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.items.HeartSwordItem;
import cn.blockforge.generated.generatedmod.items.SpiritSwordItem;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Runtime authority shared by manifested and forged heart swords. Never stores combat values in NBT. */
public final class HeartSwordCombatProfile {
    public enum Trait { TRUE_DAMAGE }
    private static final Set<Trait> INHERENT_TRAITS=Set.copyOf(EnumSet.allOf(Trait.class));
    public static final ResourceKey<DamageType> HEART_TRUE=ResourceKey.create(Registries.DAMAGE_TYPE,
            new ResourceLocation(GeneratedMod.MOD_ID,"heart_true"));
    private static final ThreadLocal<Boolean> REPLACING=ThreadLocal.withInitial(()->false);

    public static double realmMultiplier(int realm) {return 1.10+.05*Math.max(0,Math.min(8,realm));}
    public static double stageMultiplier(int stage) {return 1.00+.02*Math.max(0,Math.min(3,stage));}
    public static double heartSwordRealmMultiplier(Player player) {
        var data=GeneratedMod.data(player);return data==null?1:realmMultiplier(data.wuxingxiantu$getRealm());
    }
    public static double heartSwordStageMultiplier(Player player) {
        var data=GeneratedMod.data(player);return data==null?1:stageMultiplier(data.wuxingxiantu$getCultivationStage());
    }
    public static boolean isHeartSword(ItemStack stack) {return stack.getItem() instanceof HeartSwordItem;}
    public static boolean isHeartForged(ItemStack stack) {return SpiritSwordItem.isHeartForged(stack);}
    public static boolean isValidHeartOwner(Player player,ItemStack stack) {
        if(player==null||GeneratedMod.data(player)==null)return false;
        return HeartSwordItem.isOwnedBy(stack,player.getUUID())
                ||HeartForgeContracts.active(player,stack);
    }
    public static Set<Trait> traits(Player player,ItemStack stack) {
        return isValidHeartOwner(player,stack)?INHERENT_TRAITS:Set.of();
    }
    // Evaluate a copy so looking at a tooltip never mutates live attributes or equipment.
    private static AttributeInstance withoutWeapon(Player player) {
        var actual=player.getAttribute(Attributes.ATTACK_DAMAGE);
        var copy=new AttributeInstance(Attributes.ATTACK_DAMAGE,ignored->{});
        if(actual==null)return copy;
        copy.replaceFrom(actual);
        for(var modifier:player.getMainHandItem().getAttributeModifiers(EquipmentSlot.MAINHAND).get(Attributes.ATTACK_DAMAGE))
            copy.removeModifier(modifier.getId());
        return copy;
    }
    public static double projectHeartSwordDamage(Player player) {
        return Math.max(0,withoutWeapon(player).getValue())
                *heartSwordRealmMultiplier(player)*heartSwordStageMultiplier(player);
    }
    public static double originalSwordDamage(Player player,ItemStack stack) {
        var copy=withoutWeapon(player);
        for(var modifier:stack.getAttributeModifiers(EquipmentSlot.MAINHAND).get(Attributes.ATTACK_DAMAGE)) {
            copy.removeModifier(modifier.getId());copy.addTransientModifier(modifier);
        }
        return Math.max(0,copy.getValue());
    }
    public static double resolveHeartForgedDamage(Player player,ItemStack stack) {
        double original=originalSwordDamage(player,stack);
        return isValidHeartOwner(player,stack)?Math.max(original,projectHeartSwordDamage(player)):original;
    }
    public static double attackBaseline(Player player,ItemStack stack) {
        if(!traits(player,stack).contains(Trait.TRUE_DAMAGE))return originalSwordDamage(player,stack);
        return isHeartSword(stack)?projectHeartSwordDamage(player):resolveHeartForgedDamage(player,stack);
    }
    /** Called only from Player.attack's attribute lookup, before vanilla cooldown and critical scaling. */
    public static double attackAttribute(Player player,Attribute attribute) {
        if(attribute!=Attributes.ATTACK_DAMAGE||!isValidHeartOwner(player,player.getMainHandItem()))
            return player.getAttributeValue(attribute);
        return attackBaseline(player,player.getMainHandItem());
    }
    /** Replaces one original hurt invocation; its result still drives every vanilla on-hit hook. */
    public static boolean meleeHit(Entity target,DamageSource original,float amount,Player attacker) {
        if(isHeartSword(attacker.getMainHandItem())&&!isValidHeartOwner(attacker,attacker.getMainHandItem()))return false;
        if(REPLACING.get()||!(target instanceof LivingEntity living)
                ||!traits(attacker,attacker.getMainHandItem()).contains(Trait.TRUE_DAMAGE))
            return target.hurt(original,amount);
        REPLACING.set(true);
        try {return applyHeartTrueDamage(attacker,living,amount);}
        finally {REPLACING.remove();}
    }
    public static boolean isHeartTrue(DamageSource source) {return source.is(HEART_TRUE);}
    /** Shared direct sword hit: a forged baseline replaces the old baseline, never adds to it. */
    public static boolean swordHit(Player player,ItemStack stack,LivingEntity target,double ordinaryBase,double enchantmentBonus,double multiplier,double cap) {
        boolean heart=traits(player,stack).contains(Trait.TRUE_DAMAGE);
        double base=heart?Math.max(ordinaryBase,attackBaseline(player,stack)):ordinaryBase;
        double scaled=(base+enchantmentBonus)*multiplier;
        // The legacy collision cap belongs to ordinary flight, not realm-grown heart damage.
        float amount=(float)(heart?scaled:Math.min(cap,scaled));
        boolean hit=cn.blockforge.generated.generatedmod.fire.WeaponFlameState.withWeapon(stack,
                ()->heart?applyHeartTrueDamage(player,target,amount):target.hurt(player.damageSources().playerAttack(player),amount));
        if(hit && !target.isAlive())SpiritSwordItem.addKill(stack);
        return hit;
    }
    public static boolean applyHeartTrueDamage(Player attacker,LivingEntity target,float amount) {
        if(attacker.level().isClientSide||!attacker.isAlive()||attacker.isSpectator()
                ||!target.isAlive()||target.isRemoved()||!(amount>0)||!Float.isFinite(amount)
                ||target instanceof Player player&&(player.isCreative()||player.isSpectator()))return false;
        var type=attacker.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(HEART_TRUE);
        return target.hurt(new DamageSource(type,attacker,attacker),amount);
    }
    /** Absorption stays intact, but contributes zero to this damage calculation. */
    public static float absorption(LivingEntity target,DamageSource source) {
        return isHeartTrue(source)?0:target.getAbsorptionAmount();
    }
    public static void setAbsorption(LivingEntity target,float amount,DamageSource source) {
        if(!isHeartTrue(source))target.setAbsorptionAmount(amount);
    }
    private HeartSwordCombatProfile() {}
}
