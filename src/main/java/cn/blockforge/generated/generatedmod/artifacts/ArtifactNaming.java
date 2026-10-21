package cn.blockforge.generated.generatedmod.artifacts;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.crafting.EquipmentBond;
import java.util.List;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** D27: vanilla anvil naming and NBT copying, with the existing single-owner policy. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID)
public final class ArtifactNaming {
    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void preserveOwner(AnvilUpdateEvent event){
        var left=event.getLeft();var right=event.getRight();
        if(!ArtifactData.artifact(left)&&!ArtifactData.artifact(right))return;
        if(!EquipmentBond.canConsume(event.getPlayer(),List.of(left,right))){event.setCanceled(true);return;}
        // Vanilla copies the left item. A bound sacrifice must never lose its owner in an unbound result.
        if(EquipmentBond.owner(left)==null&&EquipmentBond.owner(right)!=null)event.setCanceled(true);
        // Allowed operations stay entirely vanilla: names, costs, repair and every unrelated NBT tag.
    }
    private ArtifactNaming(){}
}
