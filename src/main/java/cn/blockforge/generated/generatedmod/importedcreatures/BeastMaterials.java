package cn.blockforge.generated.generatedmod.importedcreatures;

import cn.blockforge.generated.generatedmod.ProgressionGate;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** Provenance is saved on each stack; source stage 2/5 becomes the target realm 0/1, middle phase. */
public final class BeastMaterials {
    public static ItemStack create(boolean blood,boolean king) {
        var stack=new ItemStack((blood?ImportedCreatures.BLOOD:ImportedCreatures.HIDE).get());var n=stack.getOrCreateTag();
        n.putInt("beast_schema",2);n.putString("source_entity","wuxingxiantu:"+(king?"black_boar_king":"black_boar"));
        n.putInt("source_realm",king?ProgressionGate.FOUNDATION:ProgressionGate.QI_CONDENSATION);n.putInt("source_phase",1);
        n.putString("element","earth");n.putInt("potency",king?2:1);n.putString("material_effect",blood?"stone_guard":"tenacity");
        if(!blood)n.putInt("hide_color",0x4699FF);
        return stack;
    }
    public static boolean blood(ItemStack stack) {return !stack.isEmpty()&&stack.is(ImportedCreatures.BLOOD.get());}
    public static boolean hide(ItemStack stack) {return !stack.isEmpty()&&stack.is(ImportedCreatures.HIDE.get());}
    public static int potency(ItemStack stack) {return stack.hasTag()?Math.max(1,Math.min(2,stack.getTag().getInt("potency"))):1;}
    public static Component source(ItemStack stack) {
        String id=stack.hasTag()?stack.getTag().getString("source_entity"):"";
        return switch(id) {
            case "wuxingxiantu:black_boar" -> Component.translatable("entity.wuxingxiantu.black_boar");
            case "wuxingxiantu:black_boar_king" -> Component.translatable("entity.wuxingxiantu.black_boar_king");
            default -> Component.translatable("beast.wuxingxiantu.unknown");
        };
    }
    private BeastMaterials() {}
}
