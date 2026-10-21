package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** 符箓平台方块实体：镌印台 / 绘制台 / 拓印台 共用同一容器逻辑。 */
public final class TalismanPlatformBlockEntity extends BlockEntity implements Container {
    public enum Kind { ENGRAVE, DRAW, RUB }

    /** 容器容量：机口 + 竖排墨水栏 + 成品口。 */
    public static final int SIZE = 6;
    // 镌印台：刻刀、金锭。
    public static final int ENGRAVE_KNIFE = 0;
    public static final int ENGRAVE_GOLD = 1;
    // 绘制台：符纸 + 竖排墨水栏。
    public static final int DRAW_PAPER = 0;
    // 拓印台：印章、符纸 + 竖排墨水栏。
    public static final int RUB_SEAL = 0;
    public static final int RUB_PAPER = 1;
    /** 成品输出口（所有平台统一为最后一个格位）。 */
    public static final int OUTPUT = SIZE - 1;
    /** 竖排墨水栏可容纳的墨水瓶个数（未来多墨水共画一张符）。 */
    public static final int INK_COUNT = 3;

    /** 各平台竖排墨水栏的起始格位；镌印台无墨水栏。 */
    public static int inkStart(Kind kind) {
        return switch (kind) {
            case ENGRAVE -> -1;
            case DRAW -> 1;
            case RUB -> 2;
        };
    }

    /** 该格位是否为竖排墨水栏的一员。 */
    public static boolean isInkSlot(Kind kind, int index) {
        int start = inkStart(kind);
        return start >= 0 && index >= start && index < start + INK_COUNT;
    }

    /** 该格位是否为该平台实际使用的机口（排除空闲格位）。 */
    public static boolean isActiveSlot(Kind kind, int index) {
        if (index == OUTPUT || isInkSlot(kind, index)) return true;
        return switch (kind) {
            case ENGRAVE -> index == ENGRAVE_KNIFE || index == ENGRAVE_GOLD;
            case DRAW -> index == DRAW_PAPER;
            case RUB -> index == RUB_SEAL || index == RUB_PAPER;
        };
    }

    private final NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
    private final Kind kind;
    /** 上次翻到第几页符箓（镌印台 / 绘制台用），关界面后仍保留，下次接着翻。 */
    private int selectedIndex;
    private int selectedInk;
    public int selectedInk(){return selectedInk;}
    public void selectInk(int index){selectedInk=Math.max(0,Math.min(INK_COUNT-1,index));setChanged();}

    public TalismanPlatformBlockEntity(BlockPos pos, BlockState state, Kind kind) {
        super(ModBlockEntities.TALISMAN_PLATFORM.get(), pos, state);
        this.kind = kind;
    }

    // 由注册工厂使用固定 kind 的构造。
    public TalismanPlatformBlockEntity(net.minecraft.world.level.block.entity.BlockEntityType<?> type,
                                       BlockPos pos, BlockState state, Kind kind) {
        super(type, pos, state);
        this.kind = kind;
    }

    public Kind kind() { return kind; }

    /** 已选符箓页码（越界统一夹到 0）。 */
    public int selectedIndex() { return net.minecraft.util.Mth.clamp(selectedIndex,0,Math.max(0,TalismanCatalog.ids().size()-1)); }

    public void setSelectedIndex(int index) {
        int clamped = net.minecraft.util.Mth.clamp(index,0,Math.max(0,TalismanCatalog.ids().size()-1));
        if (clamped == selectedIndex) return;
        selectedIndex = clamped;
        setChanged();
    }

    public MenuProvider menuProvider() {
        return new SimpleMenuProvider(
                (id, inventory, player) -> new TalismanPlatformMenu(id, inventory, this),
                Component.translatable("block.wuxingxiantu." + blockId(kind)));
    }

    public static String blockId(Kind kind) {
        return switch (kind) {
            case ENGRAVE -> "engraving_platform";
            case DRAW -> "drawing_platform";
            case RUB -> "rubbing_platform";
        };
    }

    @Override public int getContainerSize() { return SIZE; }
    @Override public boolean isEmpty() { return items.stream().allMatch(ItemStack::isEmpty); }
    @Override public ItemStack getItem(int slot) { return slot >= 0 && slot < SIZE ? items.get(slot) : ItemStack.EMPTY; }
    @Override public ItemStack removeItem(int slot, int amount) {
        if (slot < 0 || slot >= SIZE) return ItemStack.EMPTY;
        ItemStack result = ContainerHelper.removeItem(items, slot, amount);
        if (!result.isEmpty()) setChanged();
        return result;
    }
    @Override public ItemStack removeItemNoUpdate(int slot) {
        if (slot < 0 || slot >= SIZE) return ItemStack.EMPTY;
        return ContainerHelper.takeItem(items, slot);
    }
    @Override public void setItem(int slot, ItemStack stack) {
        if (slot < 0 || slot >= SIZE) return;
        items.set(slot, stack);
        setChanged();
    }
    @Override public boolean stillValid(Player player) { return Container.stillValidBlockEntity(this, player); }
    @Override public void clearContent() {
        for (int i = 0; i < SIZE; i++) items.set(i, ItemStack.EMPTY);
        setChanged();
    }
    @Override public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot != OUTPUT && isActiveSlot(kind, slot);
    }
    @Override public int getMaxStackSize() { return 64; }

    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("SelectedInk",selectedInk);
        ContainerHelper.saveAllItems(tag, items);
        tag.putInt("SelectedTalisman", selectedIndex);
    }
    @Override public void load(CompoundTag tag) {
        super.load(tag);
        selectedInk=Math.max(0,Math.min(INK_COUNT-1,tag.getInt("SelectedInk")));
        ContainerHelper.loadAllItems(tag, items);
        selectedIndex = net.minecraft.util.Mth.clamp(tag.getInt("SelectedTalisman"),0,Math.max(0,TalismanCatalog.ids().size()-1));
    }
}
