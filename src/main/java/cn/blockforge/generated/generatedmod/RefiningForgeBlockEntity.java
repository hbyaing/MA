package cn.blockforge.generated.generatedmod;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * 炼器台核心：既是未成型结构的客户端预览载体（由渲染器显示结构幽灵方块），
 * 也在炼制结束时承接成品——成品悬浮于异火之上，等待玩家右键炼器台核心领取。
 * 成品随区块存盘并同步到客户端，供渲染器绘制悬浮物品。
 */
public final class RefiningForgeBlockEntity extends BlockEntity {
    private static final String REWARD_TAG = "PendingReward";
    private static final String CRAFT_START_TAG = "CraftStart";
    private static final String CRAFT_FINISH_TAG = "CraftFinish";
    private static final String CRAFT_RECIPE_TAG = "CraftRecipe";

    private ItemStack pendingReward = ItemStack.EMPTY;
    private long craftStartTick;
    private long craftFinishTick;
    private boolean crafting;
    private String craftRecipeId;

    public RefiningForgeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /** 预览覆盖核心周围 3x3x2（含上层煤块提示），悬浮成品位于炉心上方，放大包围盒避免被视锥裁掉。 */
    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition).inflate(3.0D);
    }

    /** 是否有一件炼成待领取的成品悬浮于异火之上。 */
    public boolean hasPendingReward() {
        return !pendingReward.isEmpty();
    }

    /** 待领取的成品（复制品，客户端渲染与发放都基于它）。 */
    public ItemStack pendingReward() {
        return pendingReward.copy();
    }

    /** 炼制完成：写入成品并同步到周围客户端，让成品悬浮在异火上方等待领取。 */
    public void setPendingReward(ItemStack stack) {
        pendingReward = stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack.copy();
        crafting = false;
        setChanged();
        syncToClients();
    }

    /**
     * 玩家从界面领取：取走成品并清空，返回成品（发放/丢弃由调用方处理）。
     * 即使尚未待领取（客户端与服务端短暂不同步）也一律清空并同步，让客户端界面
     * 必然回到"开始炼器"态、悬浮成品必然消失，避免点击领取后按钮与剑残留。
     */
    public ItemStack takePendingReward() {
        ItemStack result = pendingReward;
        pendingReward = ItemStack.EMPTY;
        crafting = false;
        setChanged();
        syncToClients();
        return result;
    }

    /** 开始炼制：写入起止 tick 让客户端绘制百分比进度条。 */
    public void startCrafting(long startTick, long finishTick, String recipeId) {
        craftStartTick = startTick;
        craftFinishTick = finishTick;
        craftRecipeId = recipeId == null ? "" : recipeId;
        crafting = true;
        pendingReward = ItemStack.EMPTY;
        setChanged();
        syncToClients();
    }

    /** 结束炼制进度显示（成品已就位）。 */
    public void finishCrafting() {
        crafting = false;
        setChanged();
        syncToClients();
    }

    public boolean isCrafting() {
        return crafting;
    }

    public long craftStartTick() { return craftStartTick; }
    public long craftFinishTick() { return craftFinishTick; }
    public String craftRecipeId() { return craftRecipeId == null ? "" : craftRecipeId; }

    /** 炼制进度 0.0~1.0；未在炼制时返回 0。 */
    public float craftProgress(long now) {
        if (!crafting || craftFinishTick <= craftStartTick) return 0.0F;
        if (now <= craftStartTick) return 0.0F;
        if (now >= craftFinishTick) return 1.0F;
        return (now - craftStartTick) / (float) (craftFinishTick - craftStartTick);
    }

    @Override public void onLoad() {
        super.onLoad();
        if(level instanceof ServerLevel server)cn.blockforge.generated.generatedmod.network.service.AlchemyService.wakeAt(server,worldPosition);
    }
    private void syncToClients() {
        if (level instanceof ServerLevel server) {
            server.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            // sendBlockUpdated 在方块状态未变化时未必重发方块实体数据包（领取/进度翻转即属此类，
            // 此时成品清空/状态翻转后客户端仍停留在旧数据）。这里向追踪本区块的玩家补发一次数据包，
            // 保证客户端能同步到最新的成品与炼制状态。
            net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket packet = getUpdatePacket();
            if (packet != null) {
                for (var player : server.getChunkSource().chunkMap.getPlayers(new net.minecraft.world.level.ChunkPos(worldPosition),false)) {
                    player.connection.send(packet);
                }
            }
        }
    }

    @Override public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        if (!pendingReward.isEmpty()) {
            CompoundTag itemTag = new CompoundTag();
            pendingReward.save(itemTag);
            tag.put(REWARD_TAG, itemTag);
        }
        if (crafting) {
            tag.putLong(CRAFT_START_TAG, craftStartTick);
            tag.putLong(CRAFT_FINISH_TAG, craftFinishTick);
            if (craftRecipeId != null) tag.putString(CRAFT_RECIPE_TAG, craftRecipeId);
        }
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
        if(crafting){
            tag.putLong(CRAFT_START_TAG,craftStartTick);tag.putLong(CRAFT_FINISH_TAG,craftFinishTick);
            tag.putString(CRAFT_RECIPE_TAG,craftRecipeId());
        }
        if (!pendingReward.isEmpty()) {
            CompoundTag itemTag = new CompoundTag();
            pendingReward.save(itemTag);
            tag.put(REWARD_TAG, itemTag);
        }
    }

    @Override public void load(CompoundTag tag) {
        super.load(tag);
        pendingReward = tag.contains(REWARD_TAG) ? ItemStack.of(tag.getCompound(REWARD_TAG)) : ItemStack.EMPTY;
        // Progress survives reload; CraftingJobs owns the persistent settlement transaction.
        crafting = tag.contains(CRAFT_START_TAG) && tag.contains(CRAFT_FINISH_TAG);
        craftStartTick = tag.getLong(CRAFT_START_TAG);
        craftFinishTick = tag.getLong(CRAFT_FINISH_TAG);
        craftRecipeId = tag.getString(CRAFT_RECIPE_TAG);
    }
}
