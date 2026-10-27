package cn.blockforge.generated.generatedmod.importedcontent;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.MissingMappingsEvent;

/** Explicit retirement: old saves discard these removed decorations and pills without missing-registry prompts. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID)
public final class ImportedContentEvents {
    @SubscribeEvent public static void removedContent(MissingMappingsEvent event) {
        for(var mapping:event.getMappings(ForgeRegistries.Keys.ITEMS,GeneratedMod.MOD_ID))
            if(java.util.Set.of("laoyang_doll","chengran_doll","laoyang_secret_pill").contains(mapping.getKey().getPath()))mapping.remap(Items.AIR);
        for(var mapping:event.getMappings(ForgeRegistries.Keys.BLOCKS,GeneratedMod.MOD_ID))
            if(java.util.Set.of("laoyang_doll","chengran_doll").contains(mapping.getKey().getPath()))mapping.remap(Blocks.AIR);
    }
    private ImportedContentEvents() {}
}
