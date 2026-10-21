package cn.blockforge.generated.generatedmod.client;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;

/** Client registration contract; no gameplay attributes or common-side renderer references. */
public record RobeRenderProfile(ResourceLocation model,ResourceLocation texture,RenderMode renderType,VisibilityMask visibilityMask) {
    public enum RenderMode { CUTOUT, TRANSLUCENT }
    public record VisibilityMask(boolean head,boolean chest,boolean legs,boolean feet){
        public boolean hides(EquipmentSlot slot){return switch(slot){case HEAD->head;case CHEST->chest;case LEGS->legs;case FEET->feet;default->false;};}
    }
    public static final RobeRenderProfile NONE=new RobeRenderProfile(null,null,RenderMode.CUTOUT,new VisibilityMask(false,false,false,false));
}
