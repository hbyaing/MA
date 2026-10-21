package cn.blockforge.generated.generatedmod.client;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
@Mod.EventBusSubscriber(modid="wuxingxiantu", value=Dist.CLIENT, bus=Mod.EventBusSubscriber.Bus.MOD)
public final class CauldronStructureVisuals {
    public static final ResourceLocation MODEL=new ResourceLocation("wuxingxiantu","block/cauldron_structure");
    @SubscribeEvent public static void register(ModelEvent.RegisterAdditional event) { event.register(MODEL); }
}
