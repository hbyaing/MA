package cn.blockforge.generated.generatedmod;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 炼器台周围承华台的“虚拟容器”视图：把半径内每个承华台的承载物品当作一个槽位，
 * 直接复用 RefiningRecipe 的 matches/consume 逻辑；setChanged 时统一清理空叠并同步客户端。
 */
public final class PedestalContainer implements Container {
    /** 炼器台核心到承华台的水平检索半径（格）。 */
    public static final int SEARCH_RADIUS = 4;

    private final List<ChenghuaPedestalBlockEntity> pedestals;

    public PedestalContainer(List<ChenghuaPedestalBlockEntity> pedestals) {
        this.pedestals = pedestals;
    }

    /** 收集核心周围有承载物的承华台，按距离由近到远排序（消耗时优先取近的）。 */
    public static PedestalContainer around(ServerLevel level, BlockPos corePos) {
        List<ChenghuaPedestalBlockEntity> found = new ArrayList<>();
        for (BlockPos probe : BlockPos.betweenClosed(
                corePos.offset(-SEARCH_RADIUS, -1, -SEARCH_RADIUS),
                corePos.offset(SEARCH_RADIUS, 2, SEARCH_RADIUS))) {
            if (!ChenghuaPedestalBlock.isPedestal(level.getBlockState(probe))) continue;
            BlockEntity entity = level.getBlockEntity(probe);
            if (entity instanceof ChenghuaPedestalBlockEntity pedestal && pedestal.hasFloating()) {
                found.add(pedestal);
            }
        }
        found.sort(Comparator.comparingDouble(p ->
                p.getBlockPos().distSqr(corePos)));
        return new PedestalContainer(found);
    }

    public List<ChenghuaPedestalBlockEntity> pedestals() {
        return pedestals;
    }

    @Override public int getContainerSize() { return pedestals.size(); }
    @Override public boolean isEmpty() {
        for (ChenghuaPedestalBlockEntity pedestal : pedestals) {
            if (pedestal.hasFloating()) return false;
        }
        return true;
    }

    @Override public ItemStack getItem(int slot) {
        return pedestals.get(slot).floatingStack();
    }

    @Override public ItemStack removeItem(int slot, int amount) {
        ChenghuaPedestalBlockEntity pedestal = pedestals.get(slot);
        ItemStack result = pedestal.floatingStack().split(amount);
        setChanged();
        return result;
    }

    @Override public ItemStack removeItemNoUpdate(int slot) {
        ItemStack result = pedestals.get(slot).floatingStack().copy();
        pedestals.get(slot).setFloating(ItemStack.EMPTY);
        return result;
    }

    @Override public void setItem(int slot, ItemStack stack) {
        pedestals.get(slot).setFloating(stack);
    }

    /** consume 直接原地 shrink 了槽位引用：归一 0 数量叠，并把每个承华台的最新承载推给客户端。 */
    @Override public void setChanged() {
        for (ChenghuaPedestalBlockEntity pedestal : pedestals) {
            ItemStack stack = pedestal.floatingStack();
            if (!stack.isEmpty() && stack.getCount() <= 0) {
                pedestal.setFloating(ItemStack.EMPTY);
            } else {
                // 部分消耗（如玄铁 4→3）也必须同步，否则客户端界面会拿着旧数量做可行性判断。
                ChenghuaPedestalBlock.refresh(pedestal.getLevel(), pedestal.getBlockPos());
            }
        }
    }

    @Override public boolean stillValid(Player player) {
        return true;
    }

    @Override public void clearContent() {
        for (ChenghuaPedestalBlockEntity pedestal : pedestals) {
            pedestal.setFloating(ItemStack.EMPTY);
        }
    }
}
