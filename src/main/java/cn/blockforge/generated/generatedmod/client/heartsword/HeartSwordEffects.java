package cn.blockforge.generated.generatedmod.client.heartsword;

import cn.blockforge.generated.generatedmod.client.HeartSwordClippedModel;
import cn.blockforge.generated.generatedmod.items.HeartSwordItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Called inside ItemRenderer after model drawing, with exactly the same grip/display matrix. */
public final class HeartSwordEffects {
    public static void afterItem(ItemStack stack, ItemDisplayContext view, PoseStack pose,
                                 MultiBufferSource buffers, BakedModel model) {
        if (!(stack.getItem() instanceof HeartSwordItem) || view == ItemDisplayContext.GUI
                || model.isCustomRenderer()) return;
        float reveal = model instanceof HeartSwordClippedModel clipped ? clipped.reveal() : 1;
        if (reveal <= 0) return;
        boolean summon = model instanceof HeartSwordClippedModel clipped && clipped.summoning();
        // Finish only this sword's material passes before drawing the transparent light meshes.
        if (buffers instanceof MultiBufferSource.BufferSource source) {
            for (var pass : model.getRenderPasses(stack, true))
                for (var type : pass.getRenderTypes(stack, true)) source.endBatch(type);
        }
        var client = Minecraft.getInstance();
        float time = client.level == null ? 0 : (client.level.getGameTime() % 24000) + client.getFrameTime();
        HeartSwordVfx.render(pose, buffers, time, reveal, summon, view.firstPerson());
    }

    private HeartSwordEffects() {}
}
