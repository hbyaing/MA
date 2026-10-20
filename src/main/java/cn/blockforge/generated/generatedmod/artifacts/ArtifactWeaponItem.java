package cn.blockforge.generated.generatedmod.artifacts;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import java.util.List;

public final class ArtifactWeaponItem extends SwordItem {
    public final ArtifactKind kind;
    public ArtifactWeaponItem(ArtifactKind kind){super(Tiers.IRON,0,-2.6F,new Properties().durability(1500).rarity(Rarity.RARE));this.kind=kind;}
    @Override public Multimap<Attribute,AttributeModifier> getAttributeModifiers(EquipmentSlot slot,ItemStack stack){
        if(slot!=EquipmentSlot.MAINHAND)return ImmutableMultimap.of();
        return ImmutableMultimap.of(Attributes.ATTACK_DAMAGE,new AttributeModifier(BASE_ATTACK_DAMAGE_UUID,"artifact attack",ArtifactData.attack(stack),AttributeModifier.Operation.ADDITION),
                Attributes.ATTACK_SPEED,new AttributeModifier(BASE_ATTACK_SPEED_UUID,"artifact speed",-2.6D,AttributeModifier.Operation.ADDITION));
    }
    @Override public Component getName(ItemStack stack){return ArtifactData.name(stack);}
    @Override public int getMaxDamage(ItemStack stack){return ArtifactData.durability(stack);}
    @Override public boolean isDamageable(ItemStack stack){return !ArtifactData.life(stack);}
    @Override public boolean isBarVisible(ItemStack stack){return !ArtifactData.life(stack)&&super.isBarVisible(stack);}
    @Override public boolean isFoil(ItemStack stack){return cn.blockforge.generated.generatedmod.crafting.EquipmentCrafting.refine(stack)>=3||super.isFoil(stack);}
    @Override public void setDamage(ItemStack stack,int damage){super.setDamage(stack,ArtifactData.life(stack)?0:damage);}
    @Override public boolean isValidRepairItem(ItemStack stack,ItemStack repair){return false;}
    @Override public void appendHoverText(ItemStack stack,Level level,List<Component> lines,TooltipFlag flag){ArtifactData.tooltip(stack,lines);}
}
