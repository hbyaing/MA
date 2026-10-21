package cn.blockforge.generated.generatedmod;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.*;
import java.util.Map;

/** Explicit old-name aliases for world registry snapshots; never register duplicate items. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID)
public final class RegistryNameMigration {
    public static final Map<String,String> ALIASES=Map.ofEntries(
        Map.entry("inscription_table","ming_ke_tai"),Map.entry("reforge_table","jing_zhu_tai"),
        Map.entry("azure_steel_sword","mingjue_sword"),Map.entry("tiangang_sword","qingming_sword"),
        Map.entry("big_dipper_sword","jixiao_sword"),Map.entry("demon_slayer_sword","wuxiang_sword"),
        Map.entry("pure_yang_sword","guiyuan_sword"),Map.entry("xuanyuan_sword","tianque_sword"),Map.entry("realm_flight","dunguang"));
    private RegistryNameMigration(){}
    public static ResourceLocation canonical(ResourceLocation id){return id!=null&&id.getNamespace().equals(GeneratedMod.MOD_ID)?new ResourceLocation(GeneratedMod.MOD_ID,ALIASES.getOrDefault(id.getPath(),id.getPath())):id;}
    @SubscribeEvent public static void remap(MissingMappingsEvent event){
        for(var m:event.getMappings(ForgeRegistries.Keys.ITEMS,GeneratedMod.MOD_ID)){var target=canonical(m.getKey());if(!target.equals(m.getKey())){var value=ForgeRegistries.ITEMS.getValue(target);if(value!=null)m.remap(value);}}
        for(var m:event.getMappings(ForgeRegistries.Keys.BLOCKS,GeneratedMod.MOD_ID)){var target=canonical(m.getKey());if(!target.equals(m.getKey())){var value=ForgeRegistries.BLOCKS.getValue(target);if(value!=null)m.remap(value);}}
        for(var m:event.getMappings(ForgeRegistries.Keys.MOB_EFFECTS,GeneratedMod.MOD_ID)){var target=canonical(m.getKey());if(!target.equals(m.getKey())){var value=ForgeRegistries.MOB_EFFECTS.getValue(target);if(value!=null)m.remap(value);}}
        for(var m:event.getMappings(ForgeRegistries.Keys.MENU_TYPES,GeneratedMod.MOD_ID)){var target=canonical(m.getKey());if(!target.equals(m.getKey())){var value=ForgeRegistries.MENU_TYPES.getValue(target);if(value!=null)m.remap(value);}}
    }
}
