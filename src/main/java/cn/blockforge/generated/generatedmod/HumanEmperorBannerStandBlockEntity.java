package cn.blockforge.generated.generatedmod;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** 地面人皇幡的持久数据；保存完整 ItemStack，确保魂魄、耐久与自定义标签不丢失。 */
public final class HumanEmperorBannerStandBlockEntity extends BlockEntity {
    private static final String BANNER_TAG = "Banner";
    private ItemStack banner = ItemStack.EMPTY;

    public HumanEmperorBannerStandBlockEntity(net.minecraft.world.level.block.entity.BlockEntityType<?> type,
                                               BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public ItemStack banner() { return banner; }

    public void setBanner(ItemStack stack) {
        banner = stack.copyWithCount(1);
        setChanged();
        syncToClients();
    }

    public ItemStack takeBanner() {
        if (banner.isEmpty()) return ItemStack.EMPTY;
        ItemStack result = banner;
        banner = ItemStack.EMPTY;
        setChanged();
        syncToClients();
        return result;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, HumanEmperorBannerStandBlockEntity entity) {
        // 预留给幡面飘动/魂火脉动；当前动画由客户端渲染器按世界时间驱动。
    }

    private void syncToClients() {
        if (level instanceof net.minecraft.server.level.ServerLevel server) {
            server.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (!banner.isEmpty()) {
            CompoundTag item = new CompoundTag();
            banner.save(item);
            tag.put(BANNER_TAG, item);
        }
    }

    @Override public void load(CompoundTag tag) {
        super.load(tag);
        banner = tag.contains(BANNER_TAG) ? ItemStack.of(tag.getCompound(BANNER_TAG)) : ItemStack.EMPTY;
    }

    @Override public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override public void handleUpdateTag(CompoundTag tag) { load(tag); }

    @Override public void onLoad() {
        super.onLoad();
        // 老存档兼容：更新前插好的幡在区块加载时补齐上方隐形杆身格，延长可交互范围。
        if (level != null && !level.isClientSide && !banner.isEmpty()
                && level.isEmptyBlock(worldPosition.above(1))) {
            HumanEmperorBannerStandBlock.placePolesAbove(level, worldPosition);
        }
    }
}
