package cn.blockforge.generated.generatedmod.importedcontent;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.acquisition.*;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID)
public final class ImportedContentEvents {
    @SubscribeEvent public static void sources(AcquisitionSourcesEvent event) {
        event.register("imported-side-content",sink->sink.add(ImportedContent.LAOYANG_PILL.get(),new SourceEvidence("block_interaction",
                new ResourceLocation(GeneratedMod.MOD_ID,"laoyang_doll"),
                List.of("Right-click a placed Laoyang doll; per-player cooldown 40 server ticks; Chengran doll is decorative only",
                        "Dolls retain the source's creative-only acquisition; this does not establish an independent survival route"),
                0,true,List.of("wuxingxiantu:laoyang_doll"))));
    }
    private ImportedContentEvents() {}
}
