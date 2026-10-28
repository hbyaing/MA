package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Persistent snapshot of a material block hidden by the assembled furnace. */
public final class AlchemyFurnacePartEntity extends BlockEntity {
    public BlockPos core = BlockPos.ZERO;
    public int coreTurn = -1;
    public BlockState original = Blocks.AIR.defaultBlockState();
    public CompoundTag originalData;

    public AlchemyFurnacePartEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ALCHEMY_FURNACE_PART.get(), pos, state);
    }

    public BlockEntity originalEntity() {
        return originalData == null ? null : BlockEntity.loadStatic(worldPosition, original, originalData.copy());
    }

    @Override public CompoundTag getUpdateTag() {
        CompoundTag tag=new CompoundTag();
        tag.putLong("Core",core.asLong());
        tag.putInt("CoreTurn",coreTurn);
        tag.put("Original",NbtUtils.writeBlockState(original));
        return tag;
    }
    @Override public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }

    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putLong("Core", core.asLong());
        tag.putInt("CoreTurn",coreTurn);
        tag.put("Original", NbtUtils.writeBlockState(original));
        if (originalData != null) tag.put("OriginalData", originalData.copy());
    }

    @Override public void load(CompoundTag tag) {
        super.load(tag);
        core = BlockPos.of(tag.getLong("Core"));
        coreTurn=tag.contains("CoreTurn")&&tag.getInt("CoreTurn")>=0&&tag.getInt("CoreTurn")<4?tag.getInt("CoreTurn"):-1;
        original = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), tag.getCompound("Original"));
        originalData = tag.contains("OriginalData") ? tag.getCompound("OriginalData").copy() : null;
    }
}
