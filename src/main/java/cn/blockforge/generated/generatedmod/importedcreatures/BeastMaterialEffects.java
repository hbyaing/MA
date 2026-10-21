package cn.blockforge.generated.generatedmod.importedcreatures;

import cn.blockforge.generated.generatedmod.alchemy.PillData;
import cn.blockforge.generated.generatedmod.crafting.EquipmentCrafting;
import java.util.List;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Source auxiliary purposes, adapted to target pill and equipment data instead of importing another progression tree. */
public final class BeastMaterialEffects {
    public static final String STONE="MyriadBeastStoneGuard",DEFENSE="BeastHideDefense";
    public static ItemStack infuseAlchemy(ItemStack result,List<ItemStack> consumed) {
        ItemStack out=result.copy();if(PillData.read(out)==null)return out;
        int count=0,potency=0;
        for(var part:consumed)if(BeastMaterials.blood(part)) {count=Math.min(8,count+part.getCount());potency=Math.max(potency,BeastMaterials.potency(part));}
        if(count==0)return out;
        var n=out.getOrCreateTagElement(STONE);
        n.putInt("Duration",Math.max(n.getInt("Duration"),Math.min(7200,(int)(600*(1+.5*(potency-1))*(1+.25*(count-1))))));
        // Target resistance has a maximum of III, matching its existing defensive pill ceiling.
        n.putInt("Amplifier",Math.max(n.getInt("Amplifier"),Math.min(2,potency+(count-1)/2)));
        return out;
    }
    /** Called only after the target pill's authoritative apply succeeded; raw blood is never edible. */
    public static void applyPill(Player player,ItemStack pill) {
        if(player.level().isClientSide||PillData.read(pill)==null||!pill.hasTag()||!pill.getTag().contains(STONE))return;
        var n=pill.getTag().getCompound(STONE);int duration=Math.max(20,Math.min(7200,n.getInt("Duration")));
        int amplifier=Math.max(0,Math.min(2,n.getInt("Amplifier")));
        var current=player.getEffect(MobEffects.DAMAGE_RESISTANCE);
        if(current!=null) {
            if(current.isInfiniteDuration()||current.getAmplifier()>amplifier)return;
            duration=Math.max(duration,current.getDuration());amplifier=Math.max(amplifier,current.getAmplifier());
        }
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,duration,amplifier));
    }
    public static ItemStack infuseRefining(ItemStack result,List<ItemStack> consumed) {
        ItemStack out=result.copy();if(!EquipmentCrafting.eligible(out))return out;
        int defense=0,potency=0;
        for(var part:consumed)if(BeastMaterials.hide(part)) {
            defense=Math.min(4,defense+Math.min(4,part.getCount())*BeastMaterials.potency(part));
            potency=Math.max(potency,BeastMaterials.potency(part));
        }
        if(defense==0)return out;
        var data=EquipmentCrafting.write(out);data.putInt(DEFENSE,Math.max(data.getInt(DEFENSE),defense));
        if(EquipmentCrafting.applies(out,"tough")&&potency>EquipmentCrafting.level(out,"tough")&&EquipmentCrafting.runeGate(out,"tough").isEmpty())
            EquipmentCrafting.setRune(out,"tough",potency,EquipmentCrafting.read(out).getCompound("Runes").getCompound("tough").getInt("Quality"));
        return out;
    }
    public static int defense(ItemStack stack) {return Math.max(0,Math.min(4,EquipmentCrafting.read(stack).getInt(DEFENSE)));}
    private BeastMaterialEffects() {}
}
