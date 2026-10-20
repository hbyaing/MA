package cn.blockforge.generated.generatedmod.fire;

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

/** Immutable persisted burn; no vanilla fire metadata is read or written. */
public record ExoticBurnState(ResourceLocation fireId,int remainingTicks,UUID sourceUuid,float intensity,
                              int damageTimer,int enchantmentLevel,boolean elemental) {
    public CompoundTag save() {
        var tag=new CompoundTag();tag.putString("fireId",fireId.toString());tag.putInt("remainingTicks",remainingTicks);
        if(sourceUuid!=null)tag.putUUID("source",sourceUuid);
        tag.putFloat("intensity",intensity);tag.putInt("damageTimer",damageTimer);
        tag.putInt("enchantmentLevel",enchantmentLevel);tag.putBoolean("elemental",elemental);return tag;
    }
    public static ExoticBurnState load(CompoundTag tag) {
        var id=ResourceLocation.tryParse(tag.getString("fireId"));int ticks=tag.getInt("remainingTicks");
        if(id==null||ticks<=0)return null;
        float intensity=tag.getFloat("intensity");
        return new ExoticBurnState(id,Math.min(72000,ticks),tag.hasUUID("source")?tag.getUUID("source"):null,
                Float.isFinite(intensity)?Math.max(1,Math.min(3,intensity)):1,
                Math.max(1,Math.min(20,tag.getInt("damageTimer"))),Math.max(1,Math.min(5,tag.getInt("enchantmentLevel"))),tag.getBoolean("elemental"));
    }
    public ExoticBurnState withTicks(int ticks,int timer) {
        return new ExoticBurnState(fireId,ticks,sourceUuid,intensity,timer,enchantmentLevel,elemental);
    }
}
