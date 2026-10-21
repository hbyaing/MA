package cn.blockforge.generated.generatedmod.artifacts;

import cn.blockforge.generated.generatedmod.crafting.EquipmentCrafting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/** D23/D24: the target embryo is the sole auxiliary attack/max-durability authority. */
public final class ArtifactPropertyPolicy {
    public static boolean ready(ArtifactAux.Trait trait){return true;}
    public static boolean candidateTrait(ArtifactAux.Trait trait){return trait==ArtifactAux.Trait.SHARP||trait==ArtifactAux.Trait.TENACITY;}
    public static boolean canCandidate(ItemStack stack,ArtifactAux aux){
        return candidateTrait(aux.trait)&&EquipmentCrafting.eligible(stack)&&!EquipmentCrafting.read(stack).contains("Pending")
                &&(aux.trait==ArtifactAux.Trait.SHARP?stack.getItem() instanceof net.minecraft.world.item.SwordItem:stack.getMaxDamage()>0);
    }
    public static boolean candidate(ItemStack stack,ArtifactAux aux){
        if(!canCandidate(stack,aux))return false;
        int strength=aux.fire()>=5?10000:aux.fire()>=3?8500:6000;
        var candidate=new CompoundTag();candidate.putInt("Type",aux.trait==ArtifactAux.Trait.SHARP?0:1);
        candidate.putInt("Quality",strength==10000?3:strength>=8500?1:0);
        candidate.putInt("Bp",strength);candidate.putString("ArtifactAuxiliary",aux.id);
        EquipmentCrafting.write(stack).put("Pending",candidate);return true;
    }
    public static void sharpCandidate(ItemStack stack,ArtifactAux aux){candidate(stack,aux);}
    /** Source armor toughness is separate from maximum durability and wear reduction. */
    public static double toughness(ItemStack stack){return ArtifactData.artifact(stack)&&ArtifactData.kind(stack)!=null
            &&!ArtifactData.kind(stack).weapon()&&ArtifactData.has(stack,ArtifactAux.Trait.TENACITY)?2:0;}
    private ArtifactPropertyPolicy(){}
}
