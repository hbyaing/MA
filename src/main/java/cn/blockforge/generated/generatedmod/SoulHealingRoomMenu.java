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

/** 愈魂室菜单：破碎魂珠、灵魂沙/灵魂土和修复进度。 */
public final class SoulHealingRoomMenu extends AbstractContainerMenu {
    private final Container container;
    private final ContainerData data;
    private final Level level;
    private final BlockPos blockPos;
    private final int machineSlotCount;

    public SoulHealingRoomMenu(int id, Inventory inventory, SoulHealingRoomBlockEntity room) {
        this(id, inventory, room, room.dataAccess(), room.getLevel(), room.getBlockPos());
    }

    public SoulHealingRoomMenu(int id, Inventory inventory, BlockPos pos) {
        this(id, inventory, context(inventory.player.level(), pos).container(),
                context(inventory.player.level(), pos).data(), inventory.player.level(), pos);
    }

    public static SoulHealingRoomMenu fromNetwork(int id, Inventory inventory, FriendlyByteBuf buffer) {
        return new SoulHealingRoomMenu(id, inventory, buffer.readBlockPos());
    }

    private SoulHealingRoomMenu(int id, Inventory inventory, Container container, ContainerData data,
                                Level level, BlockPos blockPos) {
        super(ModMenus.SOUL_HEALING_ROOM.get(), id);
        this.container = container;
        this.data = data;
        this.level = level;
        this.blockPos = blockPos;
        checkContainerSize(container, 2);
        addSlot(new Slot(container, SoulHealingRoomBlockEntity.PEARL_SLOT, 68, 34) {
            @Override public boolean mayPlace(ItemStack stack) {
                return (SoulPearlData.isRepairableSoulPearl(stack) || SoulPearlData.isSoulEnergy(stack)
                        || stack.getItem() instanceof cn.blockforge.generated.generatedmod.items.HumanEmperorBannerItem
                        || SoulPearlData.isMyriad(stack) && SoulPearlData.storedBrokenSoulTotal(stack) > 0)
                        && container.getItem(SoulHealingRoomBlockEntity.PEARL_SLOT).isEmpty();
            }
            @Override public int getMaxStackSize() { return 1; }
        });
        addSlot(new Slot(container, SoulHealingRoomBlockEntity.FUEL_SLOT, 68, 69) {
            @Override public boolean mayPlace(ItemStack stack) { return SoulHealingRoomBlockEntity.isFuel(stack); }
        });
        machineSlotCount = slots.size();
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inventory, col + row * 9 + 9, 20 + col * 18, 115 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) addSlot(new Slot(inventory, col, 20 + col * 18, 173));
        addDataSlots(data);
    }

    private record Context(Container container, ContainerData data) {}
    private static Context context(Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof SoulHealingRoomBlockEntity room) {
            return new Context(room, room.dataAccess());
        }
        return new Context(new SimpleContainer(2), new SimpleContainerData(2));
    }

    public int progress() { return Math.max(0, Math.min(SoulHealingRoomBlockEntity.MAX_PROGRESS, data.get(0))); }
    public int soulPower() { return Math.max(0, Math.min(SoulHealingRoomBlockEntity.MAX_SOUL_POWER, data.get(1))); }
    public float progressScale() { return progress() / (float) SoulHealingRoomBlockEntity.MAX_PROGRESS; }
    public BlockPos blockPos() { return blockPos; }

    @Override public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        Slot source = slots.get(index);
        if (!source.hasItem()) return ItemStack.EMPTY;
        ItemStack original = source.getItem().copy();
        ItemStack moving = source.getItem();
        if (index < machineSlotCount) {
            if (!moveItemStackTo(moving, machineSlotCount, slots.size(), true)) return ItemStack.EMPTY;
        } else if ((SoulPearlData.isRepairableSoulPearl(moving) || SoulPearlData.isSoulEnergy(moving)
                || moving.getItem() instanceof cn.blockforge.generated.generatedmod.items.HumanEmperorBannerItem
                || SoulPearlData.isMyriad(moving) && SoulPearlData.storedBrokenSoulTotal(moving) > 0)
                && moveItemStackTo(moving, SoulHealingRoomBlockEntity.PEARL_SLOT, 1, false)) {
        } else if (SoulHealingRoomBlockEntity.isFuel(moving)
                && moveItemStackTo(moving, SoulHealingRoomBlockEntity.FUEL_SLOT, 2, false)) {
        } else return ItemStack.EMPTY;
        if (moving.isEmpty()) source.set(ItemStack.EMPTY); else source.setChanged();
        return original;
    }

    @Override public boolean stillValid(Player player) {
        return level != null && level.getBlockEntity(blockPos) == container
                && player.distanceToSqr(blockPos.getX() + .5D, blockPos.getY() + .5D, blockPos.getZ() + .5D) <= 64.0D;
    }

    @Override public void removed(Player player) {
        super.removed(player);
        container.stopOpen(player);
    }
}
