package cn.blockforge.generated.generatedmod;

import net.minecraft.core.NonNullList;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/** 炼魂炉和化魄炉共用的服务端熔炼容器。 */
public final class SoulFurnaceBlockEntity extends BlockEntity implements Container {
    public enum Kind { SOUL_REFINING, SPIRIT_DISMANTLING }

    public static final int PEARL_SLOT = 0;
    public static final int FUEL_SLOT = 1;
    public static final int OUTPUT_SLOT_START = 2;
    /** 化魄炉使用完整的 3×3 产物区；炼魂炉仍只启用第一个产物槽。 */
    public static final int OUTPUT_SLOT_COUNT = 9;
    private static final int MAX_PROGRESS = 200;
    public static final int MAX_CYCLES = 64;
    private final NonNullList<ItemStack> items = NonNullList.withSize(OUTPUT_SLOT_START + OUTPUT_SLOT_COUNT, ItemStack.EMPTY);
    private final Kind kind;
    private int burnTime;
    private int progress;
    private int cycleCount = 1;
    private int remainingCycles;
    private final ContainerData data = new ContainerData() {
        @Override public int get(int index) {
            return switch (index) {
                case 0 -> burnTime;
                case 1 -> progress;
                case 2 -> cycleCount;
                case 3 -> remainingCycles;
                default -> 0;
            };
        }

        @Override public void set(int index, int value) {
            if (index == 0) burnTime = Math.max(0, value);
            else if (index == 1) progress = Math.max(0, Math.min(MAX_PROGRESS, value));
            else if (index == 2) cycleCount = clampCycles(value);
            else if (index == 3) remainingCycles = Math.max(0, Math.min(MAX_CYCLES, value));
        }

        @Override public int getCount() { return 4; }
    };

    public SoulFurnaceBlockEntity(net.minecraft.world.level.block.entity.BlockEntityType<?> type,
                                  BlockPos pos, BlockState state, Kind kind) {
        super(type, pos, state);
        this.kind = kind;
    }

    public Kind kind() { return kind; }
    public ItemStack pearl() { return items.get(PEARL_SLOT); }
    public ItemStack fuel() { return items.get(FUEL_SLOT); }
    public ItemStack output(int index) { return index >= 0 && index < OUTPUT_SLOT_COUNT ? items.get(OUTPUT_SLOT_START + index) : ItemStack.EMPTY; }
    public ContainerData dataAccess() { return data; }
    public boolean isBurning() { return burnTime > 0; }
    public int cycleCount() { return cycleCount; }
    public int remainingCycles() { return remainingCycles; }
    public boolean isCycleRunning() { return kind == Kind.SOUL_REFINING && (remainingCycles > 0 || progress > 0); }

    public void setCycleCount(int value) {
        int clamped = clampCycles(value);
        if (cycleCount == clamped) return;
        cycleCount = clamped;
        if (level != null && !level.isClientSide) setChanged();
    }

    /** 提交界面中的轮次时，立即让当前拘魂珠按新轮次继续烧炼。 */
    public void configureCycles(int value) {
        cycleCount = clampCycles(value);
        if (kind == Kind.SOUL_REFINING && isValidPearl()) {
            remainingCycles = Math.min(cycleCount, SoulPearlData.remainingRefinements(pearl()));
            if (remainingCycles <= 0) progress = 0;
        }
        if (level != null && !level.isClientSide) setChanged();
    }

    private static int clampCycles(int value) {
        return Math.max(1, Math.min(MAX_CYCLES, value));
    }

    /** 旧存档可能在炼魂炉的两个历史产物槽中留有物品，打开界面时安全退回玩家。 */
    public void returnSurplusRefiningOutputs(Player player) {
        if (kind != Kind.SOUL_REFINING || level == null || level.isClientSide) return;
        boolean changed = false;
        for (int i = 1; i < OUTPUT_SLOT_COUNT; i++) {
            int slot = OUTPUT_SLOT_START + i;
            ItemStack surplus = items.get(slot);
            if (surplus.isEmpty()) continue;
            player.getInventory().placeItemBackInInventory(surplus.copy());
            items.set(slot, ItemStack.EMPTY);
            changed = true;
        }
        if (changed) setChanged();
    }

    public net.minecraft.world.MenuProvider menuProvider() {
        return new net.minecraft.world.SimpleMenuProvider(
                (id, inventory, player) -> new SoulFurnaceMenu(id, inventory, this),
                Component.translatable(kind == Kind.SOUL_REFINING
                        ? "block.wuxingxiantu.soul_refining_furnace"
                        : "block.wuxingxiantu.spirit_dismantling_furnace"));
    }

    /** 将玩家手中的一件灵珠或燃料放入机器，返回是否成功。 */
    public boolean insertHeldItem(Player player, net.minecraft.world.InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        int slot = isPearlItem(held) ? PEARL_SLOT : isFuelItem(held) ? FUEL_SLOT : -1;
        if (slot < 0 || !canPlaceItem(slot, held)) return false;
        ItemStack current = items.get(slot);
        if (current.isEmpty()) {
            items.set(slot, held.copyWithCount(1));
        } else {
            if (!ItemStack.isSameItemSameTags(current, held)
                    || current.getCount() >= Math.min(current.getMaxStackSize(), getMaxStackSize(slot))) return false;
            current.grow(1);
        }
        held.shrink(1);
        setChanged();
        return true;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SoulFurnaceBlockEntity be) {
        if (level.isClientSide) return;
        be.beginRefiningBatch();
        boolean canWork = be.canWork();
        if (!canWork) {
            // 缺燃料或产物槽满时暂停当前轮次；只有灵珠被取走或失效时才清空进度。
            if (be.pearl().isEmpty() || !be.isValidPearl()) {
                be.progress = 0;
                be.remainingCycles = 0;
            }
            be.updateLit(level, pos, state, false);
            return;
        }
        if (be.burnTime <= 0) {
            if (isEternalFuel(be.fuel())) {
                // 永燃燃料不随烧炼消耗，火苗随轮次持续 replenish。
                be.burnTime = 200;
            } else {
                int fuel = fuelBurnTime(be.fuel());
                if (fuel <= 0) {
                    be.updateLit(level, pos, state, false);
                    return;
                }
                be.burnTime = fuel;
                be.fuel().shrink(1);
            }
        }
        be.burnTime--;
        be.progress++;
        be.updateLit(level, pos, state, true);
        if (be.progress >= MAX_PROGRESS) {
            if (be.completeOperation(level)) {
                be.progress = 0;
                be.setChanged();
            } else {
                be.progress = MAX_PROGRESS - 1;
            }
        }
        if (level.getGameTime() % 20L == 0L) be.setChanged();
        if (be.burnTime <= 0 || !be.canWork()) be.updateLit(level, pos, state, false);
    }

    private void beginRefiningBatch() {
        if (kind != Kind.SOUL_REFINING || progress > 0 || remainingCycles > 0
                || !isValidPearl()) return;
        // 灵珠放入后立即同步本批剩余轮次；缺燃料只会暂停，不再让界面长期显示 0。
        remainingCycles = Math.min(cycleCount, SoulPearlData.remainingRefinements(pearl()));
    }

    private boolean canWork() {
        if (pearl().isEmpty() || !isValidPearl()) return false;
        if (burnTime <= 0 && !isFuelItem(fuel())) return false;
        if (kind == Kind.SOUL_REFINING) {
            if (SoulPearlData.isMyriad(pearl())) return SoulPearlData.storedSouls(pearl()).stream()
                    .anyMatch(soul -> soul.dead() > 0);
            ItemStack preview = pearl().copy();
            if (!SoulPearlData.canRefine(preview)) return false;
            SoulPearlData.refineBinding(preview);
            int cyclesLeft = remainingCycles <= 0 ? 1 : remainingCycles;
            return cyclesLeft > 1 || canStore(preview);
        }
        return hasOutputCapacity();
    }

    private boolean isValidPearl() {
        return kind == Kind.SOUL_REFINING
                ? (SoulPearlData.isBinding(pearl()) && SoulPearlData.hasOffers(pearl())
                    || SoulPearlData.isMyriad(pearl()) && !SoulPearlData.storedSouls(pearl()).isEmpty())
                : SoulPearlData.isCapture(pearl()) && SoulPearlData.hasEntity(pearl());
    }

    private boolean completeOperation(Level level) {
        if (kind == Kind.SOUL_REFINING && SoulPearlData.isMyriad(pearl())) {
            int repaired = SoulPearlData.repairMyriad(pearl());
            if (repaired <= 0) return false;
            remainingCycles = 0;
            if (level instanceof net.minecraft.server.level.ServerLevel server) {
                server.getPlayers(player -> player.distanceToSqr(worldPosition.getX() + 0.5D,
                                worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D) <= 64.0D)
                        .forEach(player -> player.displayClientMessage(
                                Component.translatable("message.wuxingxiantu.myriad_soul_repaired", repaired), true));
            }
            return true;
        }
        if (kind == Kind.SOUL_REFINING) {
            ItemStack refined = pearl().copy();
            if (SoulPearlData.refineBinding(refined) <= 0) return false;
            int cyclesLeft = remainingCycles <= 0 ? 1 : remainingCycles;
            if (cyclesLeft > 1) {
                // 中间轮次继续占用输入槽，成品只在最后一轮进入唯一产物槽。
                items.set(PEARL_SLOT, refined);
                remainingCycles = cyclesLeft - 1;
                return true;
            }
            if (!insertOutput(refined)) return false;
            items.set(PEARL_SLOT, ItemStack.EMPTY);
            remainingCycles = 0;
            if (level instanceof net.minecraft.server.level.ServerLevel server) {
                server.getPlayers(player -> player.distanceToSqr(worldPosition.getX() + 0.5D,
                                worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D) <= 64.0D)
                        .forEach(player -> player.displayClientMessage(
                                Component.translatable("message.wuxingxiantu.soul_furnace_done"), true));
            }
            return true;
        }
        if (!produceDrops(level)) return false;
        // 摄魄珠是可重复使用的容器；化魄只生成一批掉落物，不消耗输入珠。
        return true;
    }

    private boolean produceDrops(Level level) {
        if (!(level instanceof net.minecraft.server.level.ServerLevel server)) return false;
        EntityType<?> type = SoulPearlData.entityType(pearl());
        if (type == null) return false;
        Entity created = type.create(server);
        if (!(created instanceof LivingEntity entity)) return false;
        entity.setPos(Vec3.atCenterOf(worldPosition));
        ResourceLocation lootId = type.getDefaultLootTable();
        LootTable table = lootId == null ? null : server.getServer().getLootData().getLootTable(lootId);
        if (table == null) {
            entity.discard();
            return false;
        }
        LootParams params = new LootParams.Builder(server)
                .withParameter(LootContextParams.THIS_ENTITY, entity)
                .withParameter(LootContextParams.ORIGIN, entity.position())
                .withOptionalParameter(LootContextParams.DAMAGE_SOURCE, server.damageSources().generic())
                .withOptionalParameter(LootContextParams.KILLER_ENTITY, null)
                .withOptionalParameter(LootContextParams.DIRECT_KILLER_ENTITY, null)
                .withLuck(0.0F)
                .create(LootContextParamSets.ENTITY);
        List<ItemStack> drops = table.getRandomItems(params);
        NonNullList<ItemStack> snapshot = NonNullList.withSize(OUTPUT_SLOT_COUNT, ItemStack.EMPTY);
        for (int i = 0; i < OUTPUT_SLOT_COUNT; i++) snapshot.set(i, output(i).copy());
        for (ItemStack drop : drops) {
            if (!drop.isEmpty() && !insertOutput(drop.copy())) {
                restoreOutputs(snapshot);
                entity.discard();
                return false;
            }
        }

        int experience = SoulPearlData.experienceReward(pearl());
        if (experience < 0) experience = Math.max(0, entity.getExperienceReward());
        int crystalCount = experience <= 0 ? 0
                : Math.max(1, (experience + cn.blockforge.generated.generatedmod.items.ExperienceCrystalItem.EXPERIENCE_PER_CRYSTAL - 1)
                / cn.blockforge.generated.generatedmod.items.ExperienceCrystalItem.EXPERIENCE_PER_CRYSTAL);
        if (crystalCount > 0 && !insertOutput(new ItemStack(
                WuXingItems.EXPERIENCE_CRYSTAL.get(), crystalCount))) {
            restoreOutputs(snapshot);
            entity.discard();
            return false;
        }
        entity.discard();
        return true;
    }

    private boolean insertOutput(ItemStack stack) {
        if (stack.isEmpty()) return true;
        int outputSlots = activeOutputSlotCount();
        for (int i = 0; i < outputSlots; i++) {
            ItemStack existing = output(i);
            int limit = Math.min(existing.getMaxStackSize(), getMaxStackSize(OUTPUT_SLOT_START + i));
            if (!existing.isEmpty() && ItemStack.isSameItemSameTags(existing, stack)
                    && existing.getCount() < limit) {
                int moved = Math.min(limit - existing.getCount(), stack.getCount());
                existing.grow(moved);
                stack.shrink(moved);
                if (stack.isEmpty()) return true;
            }
        }
        for (int i = 0; i < outputSlots && !stack.isEmpty(); i++) {
            if (!output(i).isEmpty()) continue;
            int moved = Math.min(stack.getMaxStackSize(),
                    Math.min(getMaxStackSize(OUTPUT_SLOT_START + i), stack.getCount()));
            ItemStack inserted = stack.copy();
            inserted.setCount(moved);
            items.set(OUTPUT_SLOT_START + i, inserted);
            stack.shrink(moved);
        }
        return stack.isEmpty();
    }

    private void restoreOutputs(NonNullList<ItemStack> snapshot) {
        for (int i = 0; i < OUTPUT_SLOT_COUNT; i++) {
            items.set(OUTPUT_SLOT_START + i, snapshot.get(i).copy());
        }
    }

    private boolean canStore(ItemStack stack) {
        if (stack.isEmpty()) return true;
        int remaining = stack.getCount();
        for (int i = 0; i < activeOutputSlotCount(); i++) {
            ItemStack existing = output(i);
            int limit = Math.min(stack.getMaxStackSize(), getMaxStackSize(OUTPUT_SLOT_START + i));
            if (existing.isEmpty()) remaining -= limit;
            else if (ItemStack.isSameItemSameTags(existing, stack)) remaining -= Math.max(0, limit - existing.getCount());
            if (remaining <= 0) return true;
        }
        return false;
    }

    private boolean hasOutputCapacity() {
        for (int i = 0; i < activeOutputSlotCount(); i++) {
            ItemStack existing = output(i);
            if (existing.isEmpty()) return true;
            int limit = Math.min(existing.getMaxStackSize(), getMaxStackSize(OUTPUT_SLOT_START + i));
            if (existing.getCount() < limit) return true;
        }
        return false;
    }

    private int activeOutputSlotCount() {
        return kind == Kind.SOUL_REFINING ? 1 : OUTPUT_SLOT_COUNT;
    }

    private boolean isPearlItem(ItemStack stack) {
        return kind == Kind.SOUL_REFINING
                ? SoulPearlData.isBinding(stack) || SoulPearlData.isMyriad(stack)
                : SoulPearlData.isCapture(stack);
    }

    public static boolean isFuelItem(ItemStack stack) {
        return fuelBurnTime(stack) > 0;
    }

    /** 永燃灵煤：在炼魂炉、化魄炉中作为永不消耗的燃料；永燃灵煤块本身不可入炉。 */
    public static boolean isEternalFuel(ItemStack stack) {
        return stack.is(WuXingItems.ETERNAL_SPIRIT_COAL.get());
    }

    private static int fuelBurnTime(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        if (isEternalFuel(stack)) return 200;
        if (stack.is(WuXingItems.SPIRIT_COAL.get())) return 1600;
        if (stack.is(WuXingBlocks.SPIRIT_COAL_BLOCK_ITEM.get())) return 16000;
        return 0;
    }

    private int getMaxStackSize(int slot) {
        return slot == PEARL_SLOT ? 1 : 64;
    }

    private void updateLit(Level level, BlockPos pos, BlockState state, boolean lit) {
        if (state.hasProperty(SoulFurnaceBlock.LIT) && state.getValue(SoulFurnaceBlock.LIT) != lit) {
            level.setBlock(pos, state.setValue(SoulFurnaceBlock.LIT, lit), 3);
        }
    }

    @Override public int getContainerSize() { return items.size(); }
    @Override public boolean isEmpty() { return items.stream().allMatch(ItemStack::isEmpty); }
    @Override public ItemStack getItem(int slot) { return slot >= 0 && slot < items.size() ? items.get(slot) : ItemStack.EMPTY; }
    @Override public ItemStack removeItem(int slot, int amount) {
        if (slot < 0 || slot >= items.size()) return ItemStack.EMPTY;
        ItemStack result = net.minecraft.world.ContainerHelper.removeItem(items, slot, amount);
        if (!result.isEmpty()) {
            resetRefiningBatchIfPearl(slot);
            setChanged();
        }
        return result;
    }
    @Override public ItemStack removeItemNoUpdate(int slot) {
        if (slot < 0 || slot >= items.size()) return ItemStack.EMPTY;
        ItemStack result = net.minecraft.world.ContainerHelper.takeItem(items, slot);
        if (!result.isEmpty()) resetRefiningBatchIfPearl(slot);
        return result;
    }
    @Override public void setItem(int slot, ItemStack stack) {
        if (slot < 0 || slot >= items.size()) return;
        ItemStack previous = items.get(slot);
        if (slot == PEARL_SLOT && !sameStack(previous, stack)) resetRefiningBatchIfPearl(slot);
        items.set(slot, stack);
        setChanged();
    }

    private void resetRefiningBatchIfPearl(int slot) {
        if (kind == Kind.SOUL_REFINING && slot == PEARL_SLOT) {
            progress = 0;
            remainingCycles = 0;
        }
    }

    private static boolean sameStack(ItemStack first, ItemStack second) {
        return first.getCount() == second.getCount()
                && ItemStack.isSameItemSameTags(first, second);
    }
    @Override public void setChanged() { super.setChanged(); }
    @Override public boolean stillValid(Player player) { return Container.stillValidBlockEntity(this, player); }
    @Override public void clearContent() {
        for (int i = 0; i < items.size(); i++) items.set(i, ItemStack.EMPTY);
        setChanged();
    }
    @Override public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot == PEARL_SLOT) return stack.getCount() > 0 && isPearlItem(stack) && pearl().isEmpty();
        if (slot == FUEL_SLOT) return stack.getCount() > 0 && isFuelItem(stack);
        return false;
    }
    @Override public int getMaxStackSize() { return 64; }
    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        net.minecraft.world.ContainerHelper.saveAllItems(tag, items);
        tag.putInt("BurnTime", burnTime);
        tag.putInt("Progress", progress);
        tag.putInt("CycleCount", cycleCount);
        tag.putInt("RemainingCycles", remainingCycles);
    }
    @Override public void load(CompoundTag tag) {
        super.load(tag);
        net.minecraft.world.ContainerHelper.loadAllItems(tag, items);
        burnTime = Math.max(0, tag.getInt("BurnTime"));
        progress = Math.max(0, Math.min(MAX_PROGRESS, tag.getInt("Progress")));
        cycleCount = clampCycles(tag.contains("CycleCount") ? tag.getInt("CycleCount") : 1);
        remainingCycles = Math.max(0, Math.min(MAX_CYCLES, tag.getInt("RemainingCycles")));
    }
}
