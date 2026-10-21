package cn.blockforge.generated.generatedmod.artifacts;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraftforge.registries.*;
import java.util.*;

public final class ArtifactBootstrap {
    public static final DeferredRegister<Item> ITEMS=DeferredRegister.create(ForgeRegistries.ITEMS,GeneratedMod.MOD_ID);
    public static final Map<ArtifactKind,RegistryObject<Item>> ARTIFACTS=new EnumMap<>(ArtifactKind.class);
    public static final Map<ArtifactAux,RegistryObject<Item>> AUXILIARIES=new EnumMap<>(ArtifactAux.class);
    static{
        for(var kind:ArtifactKind.values())ARTIFACTS.put(kind,ITEMS.register("artifact_"+kind.id,()->kind.weapon()?new ArtifactWeaponItem(kind):new ArtifactArmorItem(kind)));
        for(var aux:ArtifactAux.values())AUXILIARIES.put(aux,ITEMS.register("forge_aux_"+aux.id,()->new AuxiliaryItem(aux)));
    }
    public static final class AuxiliaryItem extends Item {
        public final ArtifactAux aux;
        AuxiliaryItem(ArtifactAux aux){super(new Properties());this.aux=aux;}
        @Override public void appendHoverText(ItemStack stack,net.minecraft.world.level.Level level,List<Component> lines,TooltipFlag flag){
            lines.add(Component.translatable("tooltip.wuxingxiantu.artifacts.aux."+aux.id));
            lines.add(Component.translatable("tooltip.wuxingxiantu.artifacts.aux_use",aux.fire()));
        }
    }
    public static List<RegistryObject<Item>> items(){var all=new ArrayList<>(ARTIFACTS.values());all.addAll(AUXILIARIES.values());return List.copyOf(all);}
    private ArtifactBootstrap(){}
}
