package cn.blockforge.generated.generatedmod.artifacts;

import cn.blockforge.generated.generatedmod.WuXingItems;
import cn.blockforge.generated.generatedmod.crafting.EquipmentBond;
import cn.blockforge.generated.generatedmod.crafting.EquipmentCrafting;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import java.util.*;

/** 不保存第二套精炼/经验/主人；源成色与外观始终从五行同一份物品成长数据推导。 */
public final class ArtifactData {
    public static boolean artifact(ItemStack stack){return stack.getItem() instanceof ArtifactWeaponItem||stack.getItem() instanceof ArtifactArmorItem;}
    public static ArtifactKind kind(ItemStack stack){return stack.getItem() instanceof ArtifactWeaponItem item?item.kind:stack.getItem() instanceof ArtifactArmorItem item?item.kind:null;}
    public static int finish(ItemStack stack){return Math.max(0,Math.min(3,EquipmentCrafting.read(stack).getInt("ForgeFinishQuality")));}
    public static int material(ItemStack stack){return Math.max(0,Math.min(3,EquipmentCrafting.read(stack).getInt("ArtifactMaterial")));}
    public static int quality(ItemStack stack){
        int refine=EquipmentCrafting.refine(stack),stage=refine>=7?2:refine>=4?1:0;
        int cap=new int[]{4,8,10,12}[material(stack)]+refine;
        return Math.max(1,Math.min(Math.min(12,cap),stage*4+finish(stack)+1));
    }
    public static int modelRank(ItemStack stack){return (quality(stack)-1)/4+1;}
    /** A single source coolant contributes three conduct, never another upgrade track. */
    public static int quenchConduct(ItemStack stack){return artifact(stack)&&EquipmentCrafting.read(stack).getInt("ArtifactQuenchConduct")==3?3:0;}
    public static int conduct(ItemStack stack){return (material(stack)==0?2:0)+EquipmentCrafting.level(stack,"control")+quenchConduct(stack);}
    public static float growth(ItemStack stack){return Math.max(0,Math.min(100,EquipmentCrafting.read(stack).getInt("BondNurture")))*.00125F;}
    public static boolean life(ItemStack stack){return EquipmentBond.owner(stack)!=null&&EquipmentCrafting.read(stack).getBoolean("BondLife");}
    public static int capacity(ItemStack stack){return Math.min(3,finish(stack)+1);}
    public static List<ArtifactAux> auxiliaries(ItemStack stack){
        var list=new ArrayList<ArtifactAux>();var saved=EquipmentCrafting.read(stack).getList("ArtifactAux",Tag.TAG_STRING);
        for(int i=0;i<saved.size()&&i<3;i++){var aux=ArtifactAux.find(saved.getString(i));if(aux!=null)list.add(aux);}return list;
    }
    public static boolean has(ItemStack stack,ArtifactAux.Trait trait){return auxiliaries(stack).stream().anyMatch(aux->aux.trait==trait);}
    public static void infuse(ItemStack stack,ArtifactAux aux){
        if(ArtifactPropertyPolicy.candidateTrait(aux.trait)){
            if(!ArtifactPropertyPolicy.candidate(stack,aux))return;
            if(aux.trait==ArtifactAux.Trait.SHARP)return;
            // Keep TENACITY solely for its unique armor-toughness trait; durability uses the target candidate.
        }
        var values=new ArrayList<>(auxiliaries(stack));int cap=capacity(stack);
        // 同槽选择替换，不累积辅材次数或词条等级；同种材料重复投入亦不额外增幅。
        values.removeIf(value->value==aux);if(values.size()>=cap)values.remove(0);values.add(aux);
        var saved=new ListTag();for(var value:values)saved.add(StringTag.valueOf(value.id));EquipmentCrafting.write(stack).put("ArtifactAux",saved);
    }
    public static float attack(ItemStack stack){
        var kind=kind(stack);if(kind==null)return 0;
        // Every auxiliary flat-attack bonus is absorbed into the one target embryo.
        return (kind.attack+quality(stack)*.8F+(material(stack)==1?1:0))*(1+growth(stack));
    }
    public static float defense(ItemStack stack){var kind=kind(stack);return kind==null?0:(kind.defense+quality(stack)*.5F+(material(stack)==2?1:0))*(1+growth(stack));}
    public static int durability(ItemStack stack){
        if(life(stack))return 0;
        boolean weapon=kind(stack)!=null&&kind(stack).weapon();int base=(weapon?1200:900)+quality(stack)*(weapon?80:90);
        int defects=Math.max(0,Math.min(6,EquipmentCrafting.read(stack).getInt("ArtifactDefects")));
        return Math.max(100,Math.round(base*Math.max(.4F,1-defects*.1F)*(1+growth(stack))));
    }
    public static Component name(ItemStack stack){return Component.translatable("item.wuxingxiantu.artifacts.name",
            Component.translatable("artifacts.wuxingxiantu.subgrade."+((quality(stack)-1)%4)),
            Component.translatable("artifacts.wuxingxiantu.grade."+((quality(stack)-1)/4)),
            Component.translatable("artifacts.wuxingxiantu.kind."+kind(stack).id));}
    /** Existing AlchemyService persistent settlement calls this after its precise-operation finish. */
    public static ItemStack infuseRefining(ItemStack original,List<ItemStack> consumed){
        if(!artifact(original))return original;var result=original.copy();var data=EquipmentCrafting.write(result);
        if(!data.contains("ArtifactMaterial")){
            int material=4;
            for(var part:consumed){
                int tier=part.is(Items.NETHERITE_INGOT)?3:part.is(Items.AMETHYST_SHARD)?2:
                        part.is(Items.IRON_INGOT)||part.is(WuXingItems.DARK_IRON.get())?1:part.is(Items.COPPER_INGOT)?0:-1;
                if(!part.isEmpty()&&tier>=0)material=Math.min(material,tier);
            }
            if(material==4)material=0;
            data.putInt("ArtifactMaterial",material);data.putInt("ArtifactDefects",3-finish(result));
        }
        for(var part:consumed)if(part.getItem() instanceof ArtifactBootstrap.AuxiliaryItem item&&ArtifactPropertyPolicy.ready(item.aux.trait))infuse(result,item.aux);
        return result;
    }
    public static void tooltip(ItemStack stack,List<Component> lines){
        lines.add(Component.translatable("tooltip.wuxingxiantu.artifacts.growth",EquipmentCrafting.refine(stack),finish(stack),
                Math.max(0,Math.min(100,EquipmentCrafting.read(stack).getInt("BondNurture")))));
        lines.add(Component.translatable("tooltip.wuxingxiantu.artifacts.material."+material(stack)));
        lines.add(Component.translatable("tooltip.wuxingxiantu.artifacts.defects",EquipmentCrafting.read(stack).getInt("ArtifactDefects"),capacity(stack)));
        for(var aux:auxiliaries(stack))lines.add(Component.translatable("tooltip.wuxingxiantu.artifacts.aux."+aux.id));
        lines.add(Component.translatable("tooltip.wuxingxiantu.artifacts.instructions"));
    }
    private ArtifactData(){}
}
