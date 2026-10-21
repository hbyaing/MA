package cn.blockforge.generated.generatedmod;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 承华台的持久化状态：与灵蕴台一致的 Goety 基座语义——用一个槽位保存承载物品，
 * 随区块存盘并经 getUpdatePacket 同步到客户端，由 PedestalFloatingRenderer 悬浮展示。
 * 炼器台启动炼制时优先从这里抽取素材（见 PedestalContainer 与 cn.blockforge.generated.generatedmod.network.packet.alchemy.RefiningPacket）。
 */
public final class ChenghuaPedestalBlockEntity extends BlockEntity implements FloatingItemHolder {
    private static final String ITEM_TAG = "FloatingItem";

    private ItemStack floating = ItemStack.EMPTY;

    public ChenghuaPedestalBlockEntity(net.minecraft.world.level.block.entity.BlockEntityType<?> type,
                                       BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override public ItemStack floatingStack() { return floating; }

    /** 槽位空闲时放入一叠物品；返回是否成功。整叠存入（调用方负责切好数量）。 */
    public boolean insertStack(ItemStack stack) {
        if (hasFloating() || stack.isEmpty()) return false;
        floating = stack;
        setChanged();
        syncToClients();
        return true;
    }

    /** 取回承载物品（放入/掉落由调用方处理）。 */
    public ItemStack takeFloating() {
        if (floating.isEmpty()) return ItemStack.EMPTY;
        ItemStack result = floating;
        floating = ItemStack.EMPTY;
        setChanged();
        syncToClients();
        return result;
    }

    /** 炼器台抽取素材后的写回：置空或替换为新的一叠，并同步客户端。 */
    public void setFloating(ItemStack stack) {
        floating = stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack;
        setChanged();
        syncToClients();
    }

    /** 修改后把槽位内容推给周围客户端（渲染器与炼器校验读取的是客户端 BE）。 */
    private void syncToClients() {
        if (level instanceof ServerLevel server) {
            server.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override public void handleUpdateTag(CompoundTag tag) {
        load(tag);
    }

    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (!floating.isEmpty()) {
            CompoundTag itemTag = new CompoundTag();
            floating.save(itemTag);
            tag.put(ITEM_TAG, itemTag);
        }
    }

    @Override public void load(CompoundTag tag) {
        super.load(tag);
        floating = tag.contains(ITEM_TAG) ? ItemStack.of(tag.getCompound(ITEM_TAG)) : ItemStack.EMPTY;
    }
}
