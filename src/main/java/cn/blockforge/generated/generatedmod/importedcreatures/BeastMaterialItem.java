package cn.blockforge.generated.generatedmod.importedcreatures;

import cn.blockforge.generated.generatedmod.ProgressionGate;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

public final class BeastMaterialItem extends Item {
    private final boolean blood;
    public BeastMaterialItem(boolean blood,Properties properties) {super(properties);this.blood=blood;}
    @Override public Component getName(ItemStack stack) {
        return Component.translatable("beast.wuxingxiantu."+(blood?"blood_name":"hide_name"),BeastMaterials.source(stack));
    }
    @Override public void appendHoverText(ItemStack stack,@Nullable Level level,List<Component> lines,TooltipFlag flag) {
        lines.add(Component.translatable("beast.wuxingxiantu.source",BeastMaterials.source(stack)));
        if(stack.hasTag()&&stack.getTag().contains("source_realm"))lines.add(Component.translatable("beast.wuxingxiantu.realm",
                ProgressionGate.realmDisplayName(stack.getTag().getInt("source_realm"))));
        lines.add(Component.translatable("beast.wuxingxiantu.potency",BeastMaterials.potency(stack)));
        lines.add(Component.translatable("beast.wuxingxiantu."+(blood?"blood_hint":"hide_hint")));
    }
}
