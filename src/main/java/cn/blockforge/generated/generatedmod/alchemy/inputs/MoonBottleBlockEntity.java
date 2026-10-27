package cn.blockforge.generated.generatedmod.alchemy.inputs;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import cn.blockforge.generated.generatedmod.registry.ModBlockEntities;

public final class MoonBottleBlockEntity extends BlockEntity {
    private MoonBottleData contents=new MoonBottleData();
    private long observed=Long.MIN_VALUE;
    public MoonBottleBlockEntity(BlockPos pos,BlockState state){super(ModBlockEntities.MOON_BOTTLE.get(),pos,state);}
    public MoonBottleData contents(){return contents;}
    public java.util.Map<String,Long> release(long requested){var paid=contents.release(requested);if(!paid.isEmpty())setChanged();return paid;}
    public static void tick(Level level,BlockPos pos,BlockState state,MoonBottleBlockEntity bottle){
        if(!(level instanceof net.minecraft.server.level.ServerLevel server))return;
        long now=level.getGameTime();if(bottle.observed==now)return;
        // The first loaded tick establishes a baseline; unloading never produces catch-up moon stock.
        if(bottle.observed==Long.MIN_VALUE){bottle.observed=now;return;}
        if(now%20!=0)return;bottle.observed=now;
        if(bottle.contents.collect(AlchemyEnvironment.sample(server,pos))>0)bottle.setChanged();
    }
    @Override public void load(CompoundTag tag){super.load(tag);contents=MoonBottleData.load(tag.getCompound("MoonBottle"));observed=Long.MIN_VALUE;}
    @Override protected void saveAdditional(CompoundTag tag){super.saveAdditional(tag);tag.put("MoonBottle",contents.save());}
}
