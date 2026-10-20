package cn.blockforge.generated.generatedmod;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Display.ItemDisplay;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.UUID;

/**
 * 灵蕴台的持久化状态：吸收灵石块，并像 Goety 祭坛基座（PedestalBlockEntity）那样
 * 用一个方块实体槽位保存悬浮物品——物品不落实体、随区块存盘，
 * 通过 getUpdatePacket 同步到客户端，由 SpiritPlatformBlockEntityRenderer 渲染悬浮旋转。
 *
 * 兼容处理：旧版本用 ItemDisplay 实体展示，加载后第一次 tick 会把这类实体收编进槽位并删除。
 */
public final class SpiritPlatformBlockEntity extends BlockEntity implements FloatingItemHolder {
    /** Storage capacity, not the cost of every conversion. */
    public static final int MAX_AURA = 99;
    private static final int AURA_PER_BLOCK = 1;
    private static final int SCAN_INTERVAL = 40;
    private static final String AURA_TAG = "Aura";
    private static final String ITEM_TAG = "FloatingItem";
    private static final String DISPLAY_TAG = "DisplayUUID";
    private static final String DISPLAY_MARKER = "wuxingxiantu_spirit_platform_display";

    private int aura;
    private ItemStack floating = ItemStack.EMPTY;
    private UUID legacyDisplayUuid;
    private boolean legacyChecked;

    public SpiritPlatformBlockEntity(net.minecraft.world.level.block.entity.BlockEntityType<?> type,
                                     BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public int aura() { return aura; }
    public boolean isFull() { return aura >= MAX_AURA; }
    public boolean hasFloating() { return !floating.isEmpty(); }

    /** 客户端渲染器读取的悬浮物品（只读语义，请自行 copy 再修改）。 */
    @Override public ItemStack floatingStack() { return floating; }

    /** 槽位空闲时放入一个物品；返回是否成功。一次只收一个，调用方负责切出单个。 */
    public boolean insertStack(ItemStack stack) {
        if (hasFloating() || stack.isEmpty()) return false;
        floating = stack;
        setChanged();
        syncToClients();
        return true;
    }

    /** 取回悬浮物品（放入/掉落由调用方处理）。 */
    public ItemStack takeFloating() {
        if (floating.isEmpty()) return ItemStack.EMPTY;
        ItemStack result = floating;
        floating = ItemStack.EMPTY;
        setChanged();
        syncToClients();
        return result;
    }

    /** 可被灵蕴台转化的粗坯：灵煤块或灵煤。 */
    public static boolean isCoarseCoal(ItemStack stack) {
        SpiritPlatformConversions.Conversion conversion = SpiritPlatformConversions.find(stack);
        return conversion != null && conversion.effect() == SpiritPlatformConversions.CompletionEffect.ETERNAL_FIRE;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SpiritPlatformBlockEntity platform) {
        if (level.isClientSide || !(level instanceof ServerLevel server)) return;
        if (level.getGameTime() % SCAN_INTERVAL != 0L) return;
        platform.migrateLegacyDisplay(server);
        SpiritPlatformConversions.Conversion conversion = SpiritPlatformConversions.find(platform.floating);
        int targetAura = conversion == null ? MAX_AURA : conversion.requiredAura();
        if (platform.aura < targetAura) platform.absorbNearbySpiritStoneBlocks(server, targetAura);
        if (conversion != null && platform.aura >= conversion.requiredAura()) {
            platform.convertFloating(server, conversion);
        }
    }

    private void absorbNearbySpiritStoneBlocks(ServerLevel server, int targetAura) {
        int absorbed = 0;
        for (BlockPos target : BlockPos.betweenClosed(
                worldPosition.offset(-3, 0, -3), worldPosition.offset(3, 0, 3))) {
            if (aura + absorbed >= Math.min(MAX_AURA, targetAura)) break;
            if (!server.getBlockState(target).is(WuXingBlocks.SPIRIT_STONE_BLOCK.get())) continue;
            if (server.setBlock(target, net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(), 3)) {
                absorbed += AURA_PER_BLOCK;
            }
        }
        if (absorbed > 0) {
            aura = Math.min(MAX_AURA, aura + absorbed);
            setChanged();
            syncToClients();
            server.sendParticles(net.minecraft.core.particles.ParticleTypes.ENCHANT,
                    worldPosition.getX() + 0.5D, worldPosition.getY() + 1.0D, worldPosition.getZ() + 0.5D,
                    absorbed * 4, 0.35D, 0.35D, 0.35D, 0.08D);
        }
    }

    /** 达到当前转化所需灵气后直接替换悬浮物品，保留剩余灵气和旧存档物品数量。 */
    private void convertFloating(ServerLevel server, SpiritPlatformConversions.Conversion conversion) {
        if (!conversion.matches(floating) || aura < conversion.requiredAura()) return;
        floating = conversion.result(floating.getCount());
        aura -= conversion.requiredAura();
        setChanged();
        syncToClients();
        if (conversion.effect() == SpiritPlatformConversions.CompletionEffect.SPIRIT_INFUSION) {
            server.sendParticles(net.minecraft.core.particles.ParticleTypes.ENCHANT,
                    worldPosition.getX() + 0.5D, worldPosition.getY() + 1.05D, worldPosition.getZ() + 0.5D,
                    32, 0.3D, 0.25D, 0.3D, 0.08D);
            server.sendParticles(net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME,
                    worldPosition.getX() + 0.5D, worldPosition.getY() + 1.05D, worldPosition.getZ() + 0.5D,
                    12, 0.2D, 0.2D, 0.2D, 0.02D);
            server.playSound(null, worldPosition, net.minecraft.sounds.SoundEvents.AMETHYST_BLOCK_CHIME,
                    net.minecraft.sounds.SoundSource.BLOCKS, 0.8F, 1.25F);
            return;
        }
        server.sendParticles(net.minecraft.core.particles.ParticleTypes.EXPLOSION_EMITTER,
                worldPosition.getX() + 0.5D, worldPosition.getY() + 1.05D, worldPosition.getZ() + 0.5D,
                1, 0.0D, 0.0D, 0.0D, 0.0D);
        server.sendParticles(net.minecraft.core.particles.ParticleTypes.FLAME,
                worldPosition.getX() + 0.5D, worldPosition.getY() + 1.05D, worldPosition.getZ() + 0.5D,
                64, 0.45D, 0.45D, 0.45D, 0.06D);
        server.sendParticles(net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME,
                worldPosition.getX() + 0.5D, worldPosition.getY() + 1.05D, worldPosition.getZ() + 0.5D,
                32, 0.4D, 0.4D, 0.4D, 0.05D);
        server.playSound(null, worldPosition, net.minecraft.sounds.SoundEvents.MAGMA_CUBE_DEATH,
                net.minecraft.sounds.SoundSource.BLOCKS, 0.8F, 1.4F);
    }

    /** 收编旧版本遗留的 ItemDisplay 展示实体：物品入槽位，实体删除。只做一次。 */
    private void migrateLegacyDisplay(ServerLevel server) {
        if (legacyChecked && legacyDisplayUuid == null) return;
        if (legacyDisplayUuid == null) {
            List<ItemDisplay> stray = server.getEntitiesOfClass(ItemDisplay.class,
                    new AABB(worldPosition).inflate(0.35D, 1.7D, 0.35D),
                    display -> display.getPersistentData().getBoolean(DISPLAY_MARKER));
            for (ItemDisplay display : stray) adoptAndDiscard(server, display);
            legacyChecked = true;
            return;
        }
        Entity entity = server.getEntity(legacyDisplayUuid);
        if (entity instanceof ItemDisplay display && !display.isRemoved()) adoptAndDiscard(server, display);
        legacyDisplayUuid = null;
        legacyChecked = true;
        setChanged();
    }

    private void adoptAndDiscard(ServerLevel server, ItemDisplay display) {
        ItemStack held = display.getSlot(0).get();
        display.discard();
        if (held.isEmpty()) return;
        if (floating.isEmpty()) {
            floating = held;
        } else {
            net.minecraft.world.Containers.dropItemStack(server,
                    worldPosition.getX() + 0.5D, worldPosition.getY() + 1.2D, worldPosition.getZ() + 0.5D, held);
        }
        setChanged();
        syncToClients();
    }

    /** 修改后把槽位内容推给周围客户端（渲染器读取的是客户端 BE）。 */
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
        tag.putInt(AURA_TAG, aura);
        if (!floating.isEmpty()) {
            CompoundTag itemTag = new CompoundTag();
            floating.save(itemTag);
            tag.put(ITEM_TAG, itemTag);
        }
        if (legacyDisplayUuid != null) tag.putUUID(DISPLAY_TAG, legacyDisplayUuid);
    }

    @Override public void load(CompoundTag tag) {
        super.load(tag);
        aura = Math.max(0, Math.min(MAX_AURA, tag.getInt(AURA_TAG)));
        floating = tag.contains(ITEM_TAG) ? ItemStack.of(tag.getCompound(ITEM_TAG)) : ItemStack.EMPTY;
        legacyDisplayUuid = tag.hasUUID(DISPLAY_TAG) ? tag.getUUID(DISPLAY_TAG) : null;
    }
}
