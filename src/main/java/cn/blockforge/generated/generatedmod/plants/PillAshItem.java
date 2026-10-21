package cn.blockforge.generated.generatedmod.plants;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

/** Three small substrate applications per specimen; never adds years or bypasses habitat. */
public final class PillAshItem extends Item {
    public PillAshItem(Properties properties){super(properties);}
    @Override public InteractionResult useOn(UseOnContext context){
        var level=context.getLevel();var pos=context.getClickedPos();var state=level.getBlockState(pos);
        if(!(state.getBlock() instanceof SpiritPlantBlock plant)||plant.isMature(state))return InteractionResult.PASS;
        if(level.isClientSide)return InteractionResult.SUCCESS;
        if(!(level instanceof ServerLevel server)||!plant.applyAsh(server,pos,state))return InteractionResult.FAIL;
        SpiritFertilizerItem.sendGrowthParticles(server,pos);
        if(context.getPlayer()==null||!context.getPlayer().getAbilities().instabuild)context.getItemInHand().shrink(1);
        return InteractionResult.CONSUME;
    }
}
