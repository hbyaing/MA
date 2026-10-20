package cn.blockforge.generated.generatedmod.artifacts;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class ArtifactArmorItem extends ArmorItem {
    public final ArtifactKind kind;
    private final UUID armor,toughness;
    public ArtifactArmorItem(ArtifactKind kind){super(ArmorMaterials.NETHERITE,kind.armor,new Properties().rarity(Rarity.RARE));this.kind=kind;
        armor=UUID.nameUUIDFromBytes(("myriad/artifact/"+kind.id+"/armor").getBytes(StandardCharsets.UTF_8));
        toughness=UUID.nameUUIDFromBytes(("myriad/artifact/"+kind.id+"/toughness").getBytes(StandardCharsets.UTF_8));}
    @Override public Multimap<Attribute,AttributeModifier> getAttributeModifiers(EquipmentSlot slot,ItemStack stack){
        if(slot!=kind.armor.getSlot())return ImmutableMultimap.of();var values=ImmutableMultimap.<Attribute,AttributeModifier>builder();
        values.put(Attributes.ARMOR,new AttributeModifier(armor,"artifact armor",ArtifactData.defense(stack),AttributeModifier.Operation.ADDITION));
        double amount=ArtifactPropertyPolicy.toughness(stack);if(amount>0)values.put(Attributes.ARMOR_TOUGHNESS,new AttributeModifier(toughness,"artifact toughness",amount,AttributeModifier.Operation.ADDITION));
        return values.build();
    }
    @Override public Component getName(ItemStack stack){return ArtifactData.name(stack);}
    @Override public int getMaxDamage(ItemStack stack){return ArtifactData.durability(stack);}
    @Override public boolean isDamageable(ItemStack stack){return !ArtifactData.life(stack);}
    @Override public boolean isBarVisible(ItemStack stack){return !ArtifactData.life(stack)&&super.isBarVisible(stack);}
    @Override public boolean isFoil(ItemStack stack){return cn.blockforge.generated.generatedmod.crafting.EquipmentCrafting.refine(stack)>=3||super.isFoil(stack);}
    @Override public void setDamage(ItemStack stack,int damage){super.setDamage(stack,ArtifactData.life(stack)?0:damage);}
    @Override public boolean isValidRepairItem(ItemStack stack,ItemStack repair){return false;}
    @Override public void appendHoverText(ItemStack stack,Level level,List<Component> lines,TooltipFlag flag){ArtifactData.tooltip(stack,lines);}
    @Override public String getArmorTexture(ItemStack stack,net.minecraft.world.entity.Entity entity,EquipmentSlot slot,String type){
        return "minecraft:textures/models/armor/netherite_layer_"+(slot==EquipmentSlot.LEGS?2:1)+".png";
    }
}
