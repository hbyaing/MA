package cn.blockforge.generated.generatedmod.client.weaponflame;

import cn.blockforge.generated.generatedmod.client.heartsword.HeartSwordEffects;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Final item-space bridge shared by Heart Sword and the volumetric exotic-fire wrap.
 * Preserves posed/custom model transforms; the old sprite wrap and swing fragments stay retired.
 */
public final class WeaponFlameRenderer {
    private static final ThreadLocal<ItemDisplayContext> SCRIPTED_VIEW=new ThreadLocal<>();

    public static void renderHeld(ItemRenderer renderer,ItemStack stack,ItemDisplayContext context,boolean left,
            PoseStack pose,MultiBufferSource buffers,int light,int overlay,BakedModel model,LivingEntity owner) {
        ItemDisplayContext previous=SCRIPTED_VIEW.get();SCRIPTED_VIEW.set(context);
        var priorOwner=cn.blockforge.generated.generatedmod.client.firecoil.Client.setWeaponOwner(owner instanceof net.minecraft.world.entity.player.Player p?p:null);
        try {renderer.render(stack,context,left,pose,buffers,light,overlay,model);}
        finally {restore(previous);cn.blockforge.generated.generatedmod.client.firecoil.Client.setWeaponOwner(priorOwner);}
    }

    public static void renderPosed(ItemRenderer renderer,ItemStack stack,ItemDisplayContext view,
            PoseStack pose,MultiBufferSource buffers,int light,int overlay,BakedModel model,LivingEntity owner) {
        ItemDisplayContext previous=SCRIPTED_VIEW.get();SCRIPTED_VIEW.set(view);
        var priorOwner=cn.blockforge.generated.generatedmod.client.firecoil.Client.setWeaponOwner(owner instanceof net.minecraft.world.entity.player.Player p?p:null);
        try {renderer.render(stack,ItemDisplayContext.NONE,false,pose,buffers,light,overlay,model);}
        finally {restore(previous);cn.blockforge.generated.generatedmod.client.firecoil.Client.setWeaponOwner(priorOwner);}
    }

    public static void afterItem(ItemStack stack,ItemDisplayContext context,PoseStack pose,
            MultiBufferSource buffers,BakedModel model) {
        ItemDisplayContext view=SCRIPTED_VIEW.get();
        HeartSwordEffects.afterItem(stack,view==null?context:view,pose,buffers,model);
        cn.blockforge.generated.generatedmod.client.firecoil.Client.weapon(stack,view==null?context:view,pose,buffers,model);
    }

    private static void restore(ItemDisplayContext previous) {
        if(previous==null)SCRIPTED_VIEW.remove();else SCRIPTED_VIEW.set(previous);
    }
    private WeaponFlameRenderer() {}
}
