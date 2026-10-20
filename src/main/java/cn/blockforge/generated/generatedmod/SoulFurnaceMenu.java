package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** 炼魂炉与化魄炉共用菜单：上方为机器槽位，下方为完整玩家物品栏。 */
public final class SoulFurnaceMenu extends AbstractContainerMenu {
    public static final int PEARL_SLOT = 0;
    public static final int FUEL_SLOT = 1;
    public static final int OUTPUT_SLOT_START = 2;
    public static final int OUTPUT_SLOT_COUNT = SoulFurnaceBlockEntity.OUTPUT_SLOT_COUNT;

    public static final int PEARL_X = 47;
    public static final int PEARL_Y = 43;
    public static final int FUEL_X = 47;
    public static final int FUEL_Y = 74;
    public static final int OUTPUT_Y = 43;
    public static final int OUTPUT_GRID_X = 138;
    public static final int OUTPUT_GRID_COLUMNS = 3;
    public static final int CYCLE_LABEL_X = 98;
    public static final int CYCLE_LABEL_Y = 10;
    public static final int CYCLE_INPUT_X = 142;
    public static final int CYCLE_Y = 5;
    public static final int CYCLE_WIDTH = 36;
    public static final int CYCLE_HEIGHT = 18;
    public static final int CYCLE_BUTTON_X = 181;
    public static final int CYCLE_BUTTON_WIDTH = 30;
    public static final int INVENTORY_X = 29;
    public static final int INVENTORY_Y = 124;
    public static final int HOTBAR_Y = 182;

    private static final int MAX_PROGRESS = 200;
    private final Container container;
    private final ContainerData data;
    private final Level level;
    private final BlockPos blockPos;
    private final SoulFurnaceBlockEntity.Kind kind;
    private final int machineSlotCount;

    public SoulFurnaceMenu(int id, Inventory playerInventory, SoulFurnaceBlockEntity furnace) {
        this(id, playerInventory,
                new MenuContext(furnace, furnace.dataAccess(), furnace.kind()),
                furnace.getLevel(), furnace.getBlockPos());
    }

    /** 客户端由 NetworkHooks 传来的方块坐标构造菜单。 */
    public SoulFurnaceMenu(int id, Inventory playerInventory, BlockPos blockPos) {
        this(id, playerInventory, context(playerInventory.player.level(), blockPos),
                playerInventory.player.level(), blockPos);
    }

    public static SoulFurnaceMenu fromNetwork(int id, Inventory playerInventory, FriendlyByteBuf buffer) {
        return new SoulFurnaceMenu(id, playerInventory, buffer.readBlockPos());
    }

    private SoulFurnaceMenu(int id, Inventory playerInventory, MenuContext context,
                            Level level, BlockPos blockPos) {
        super(ModMenus.SOUL_FURNACE.get(), id);
        this.container = context.container();
        this.data = context.data();
        this.kind = context.kind();
        this.level = level;
        this.blockPos = blockPos;
        checkContainerSize(this.container,
                SoulFurnaceBlockEntity.OUTPUT_SLOT_START + SoulFurnaceBlockEntity.OUTPUT_SLOT_COUNT);
        this.container.startOpen(playerInventory.player);
        if (this.container instanceof SoulFurnaceBlockEntity furnace) {
            furnace.returnSurplusRefiningOutputs(playerInventory.player);
        }

        addSlot(new Slot(this.container, PEARL_SLOT, PEARL_X, PEARL_Y) {
            @Override public boolean mayPlace(ItemStack stack) {
                return isPearl(stack) && container.getItem(PEARL_SLOT).isEmpty();
            }

            @Override public int getMaxStackSize() { return 1; }
        });
        addSlot(new Slot(this.container, FUEL_SLOT, FUEL_X, FUEL_Y) {
            @Override public boolean mayPlace(ItemStack stack) { return isFuel(stack); }
        });
        for (int i = 0; i < visibleOutputSlotCount(kind); i++) {
            final int containerSlot = OUTPUT_SLOT_START + i;
            addSlot(new Slot(this.container, containerSlot, outputX(kind, i), outputY(kind, i)) {
                @Override public boolean mayPlace(ItemStack stack) { return false; }
                @Override public boolean mayPickup(Player player) { return hasItem(); }
                @Override public int getMaxStackSize() { return 64; }
            });
        }
        this.machineSlotCount = slots.size();

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, column + row * 9 + 9,
                        INVENTORY_X + column * 18, INVENTORY_Y + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column,
                    INVENTORY_X + column * 18, HOTBAR_Y));
        }
        addDataSlots(this.data);
    }

    private static MenuContext context(Level level, BlockPos blockPos) {
        if (level.getBlockEntity(blockPos) instanceof SoulFurnaceBlockEntity furnace) {
            return new MenuContext(furnace, furnace.dataAccess(), furnace.kind());
        }
        SoulFurnaceBlockEntity.Kind kind = level.getBlockState(blockPos).getBlock() instanceof SoulFurnaceBlock furnace
                ? furnace.kind() : SoulFurnaceBlockEntity.Kind.SOUL_REFINING;
        return new MenuContext(new SimpleContainer(
                SoulFurnaceBlockEntity.OUTPUT_SLOT_START + SoulFurnaceBlockEntity.OUTPUT_SLOT_COUNT),
                new SimpleContainerData(4), kind);
    }

    private boolean isPearl(ItemStack stack) {
        return kind == SoulFurnaceBlockEntity.Kind.SOUL_REFINING
                ? SoulPearlData.isBinding(stack) || SoulPearlData.isMyriad(stack) : SoulPearlData.isCapture(stack);
    }

    private static boolean isFuel(ItemStack stack) {
        return SoulFurnaceBlockEntity.isFuelItem(stack);
    }

    public static int visibleOutputSlotCount(SoulFurnaceBlockEntity.Kind kind) {
        return kind == SoulFurnaceBlockEntity.Kind.SOUL_REFINING ? 1 : OUTPUT_SLOT_COUNT;
    }

    public static int outputX(SoulFurnaceBlockEntity.Kind kind, int index) {
        return kind == SoulFurnaceBlockEntity.Kind.SOUL_REFINING
                ? 154 : OUTPUT_GRID_X + Math.floorMod(index, OUTPUT_GRID_COLUMNS) * 18;
    }

    public static int outputY(SoulFurnaceBlockEntity.Kind kind, int index) {
        return kind == SoulFurnaceBlockEntity.Kind.SOUL_REFINING
                ? 58 : OUTPUT_Y + Math.floorDiv(Math.max(0, index), OUTPUT_GRID_COLUMNS) * 18;
    }

    public SoulFurnaceBlockEntity.Kind kind() { return kind; }
    public int burnTime() { return Math.max(0, data.get(0)); }
    public int progress() { return Math.max(0, data.get(1)); }
    public int cycleCount() { return Math.max(1, Math.min(SoulFurnaceBlockEntity.MAX_CYCLES, data.get(2))); }
    public int remainingCycles() { return Math.max(0, Math.min(SoulFurnaceBlockEntity.MAX_CYCLES, data.get(3))); }
    public int maxProgress() { return MAX_PROGRESS; }
    public float progressScale() { return Math.min(1.0F, progress() / (float) MAX_PROGRESS); }
    public boolean isProcessing() { return !container.getItem(PEARL_SLOT).isEmpty() && progress() > 0; }
    public boolean isRefining() { return kind == SoulFurnaceBlockEntity.Kind.SOUL_REFINING; }
    public BlockPos blockPos() { return blockPos; }

    @Override public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        Slot source = slots.get(index);
        if (!source.hasItem()) return ItemStack.EMPTY;
        ItemStack sourceStack = source.getItem();
        ItemStack original = sourceStack.copy();
        int playerStart = machineSlotCount;
        int mainInventoryEnd = playerStart + 27;
        int playerEnd = mainInventoryEnd + 9;

        if (index < machineSlotCount) {
            if (!moveItemStackTo(sourceStack, playerStart, playerEnd, true)) return ItemStack.EMPTY;
        } else if (isPearl(sourceStack)
                && container.getItem(PEARL_SLOT).isEmpty()
                && moveItemStackTo(sourceStack, PEARL_SLOT, PEARL_SLOT + 1, false)) {
            // 已移入灵珠槽。
        } else if (isFuel(sourceStack)
                && moveItemStackTo(sourceStack, FUEL_SLOT, FUEL_SLOT + 1, false)) {
            // 已移入燃料槽。
        } else if (index < mainInventoryEnd) {
            if (!moveItemStackTo(sourceStack, mainInventoryEnd, playerEnd, false)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(sourceStack, playerStart, mainInventoryEnd, false)) {
            return ItemStack.EMPTY;
        }

        if (sourceStack.isEmpty()) source.set(ItemStack.EMPTY);
        else source.setChanged();
        if (sourceStack.getCount() == original.getCount()) return ItemStack.EMPTY;
        source.onTake(player, sourceStack);
        return original;
    }

    @Override public boolean stillValid(Player player) {
        if (level == null || !(level.getBlockState(blockPos).getBlock() instanceof SoulFurnaceBlock)) return false;
        if (container instanceof SoulFurnaceBlockEntity furnace && level.getBlockEntity(blockPos) != furnace) return false;
        return player.distanceToSqr(blockPos.getX() + 0.5D, blockPos.getY() + 0.5D,
                blockPos.getZ() + 0.5D) <= 64.0D && container.stillValid(player);
    }

    @Override public void removed(Player player) {
        super.removed(player);
        container.stopOpen(player);
    }

    private record MenuContext(Container container, ContainerData data,
                               SoulFurnaceBlockEntity.Kind kind) {}
}
