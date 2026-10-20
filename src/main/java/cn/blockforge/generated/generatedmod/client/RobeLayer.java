package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.*;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public final class RobeLayer extends RenderLayer<AbstractClientPlayer,PlayerModel<AbstractClientPlayer>> {
    public record Context(AbstractClientPlayer player,ItemStack stack,PlayerModel<AbstractClientPlayer> playerModel,
                          PoseStack pose,MultiBufferSource buffers,int light,float partial,RobeRenderProfile profile){}
    @FunctionalInterface public interface Visual {void render(Context context);}
    private record Entry(RobeRenderProfile profile,Visual visual){}
    private static final Map<ResourceLocation,Entry> VISUALS=new HashMap<>();
    private static final Entry NONE=new Entry(RobeRenderProfile.NONE,context->{});
    public static long renderedTestFrames;
    static {register(new ResourceLocation(GeneratedMod.MOD_ID,"dev_test_robe"),RobeRenderProfile.NONE,context->renderedTestFrames++);}
    public static void register(ResourceLocation item,RobeRenderProfile profile,Visual visual){VISUALS.put(item,new Entry(profile,visual));}
    private static Entry entry(AbstractClientPlayer player){
        var stack=RobeClient.get(player.getUUID());if(stack.isEmpty())return NONE;
        return VISUALS.getOrDefault(net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(stack.getItem()),NONE);
    }
    public static RobeRenderProfile profile(AbstractClientPlayer player){return entry(player).profile();}
    public RobeLayer(RenderLayerParent<AbstractClientPlayer,PlayerModel<AbstractClientPlayer>> parent){super(parent);}
    @Override public void render(PoseStack pose,MultiBufferSource buffers,int light,AbstractClientPlayer player,
                                 float swing,float amount,float partial,float age,float yaw,float pitch){
        var entry=entry(player);if(entry==NONE)return;
        pose.pushPose();try{entry.visual().render(new Context(player,RobeClient.get(player.getUUID()).copy(),getParentModel(),pose,buffers,light,partial,entry.profile()));}
        finally{pose.popPose();}
    }
}
