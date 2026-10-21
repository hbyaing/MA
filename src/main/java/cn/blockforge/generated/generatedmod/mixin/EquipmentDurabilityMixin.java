package cn.blockforge.generated.generatedmod.mixin;

import cn.blockforge.generated.generatedmod.crafting.EquipmentCrafting;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Explicit development/SRG names avoid an ungenerated refmap in this legacy Forge build. */
@Mixin(ItemStack.class)
public abstract class EquipmentDurabilityMixin {
    @Inject(method={"getMaxDamage","m_41776_"},at=@At("RETURN"),cancellable=true,remap=false)
    private void wuxing$durability(CallbackInfoReturnable<Integer> result){result.setReturnValue(EquipmentCrafting.maxDamage((ItemStack)(Object)this,result.getReturnValue()));}
    @ModifyVariable(method={"hurt","m_220157_"},at=@At("HEAD"),argsOnly=true,ordinal=0,remap=false)
    private int wuxing$tough(int amount,int originalAmount,net.minecraft.util.RandomSource random,net.minecraft.server.level.ServerPlayer player){return EquipmentCrafting.durabilityCost((ItemStack)(Object)this,amount,random);}
}
