package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import cn.blockforge.generated.generatedmod.client.UiLayoutConfig;

/** 符箓平台菜单：镌印台 / 绘制台 / 拓印台 共用，按 kind 摆放槽位。 */
public final class TalismanPlatformMenu extends AbstractContainerMenu {
    private static String profile(TalismanPlatformBlockEntity.Kind kind){return switch(kind){case DRAW->"draw";case ENGRAVE->"engrave";case RUB->"rub";};}
    private static int x(String screen,String exact,String legacy,int fallback){return UiLayoutConfig.x(screen,exact,UiLayoutConfig.x(screen,legacy,fallback));}
    private static int y(String screen,String exact,String legacy,int fallback){return UiLayoutConfig.y(screen,exact,UiLayoutConfig.y(screen,legacy,fallback));}
    /** 面板宽度（与界面一致），供物品栏水平居中换算。 */
    public static final int PANEL_WIDTH = 430;
    public static final int PANEL_HEIGHT = 340;
    /** 玩家物品栏居中放置：9 列 × 18 宽 = 162，两侧各留 25。 */
    public static final int INVENTORY_X = 61;
    public static final int INVENTORY_Y = 250;
    public static final int HOTBAR_Y = 310;

    // 画布右侧机口与竖排墨水栏布局（与客户端界面共用，保证槽位/标注对得上）。
    // 机口竖排、标注写在格子右侧；墨水栏是一列竖格，今后可并排多种墨水。
    // 绘制台把墨水栏放在画布右侧（左），符纸等机口让到右列，方便玩家边画边看墨量。
    private static final int INK_STEP = 20;

    private final Container container;
    private final Level level;
    private final BlockPos blockPos;
    private final TalismanPlatformBlockEntity.Kind kind;

    public TalismanPlatformMenu(int id, Inventory playerInventory, TalismanPlatformBlockEntity blockEntity) {
        this(id, playerInventory, blockEntity, blockEntity.kind(),
                blockEntity.getLevel(), blockEntity.getBlockPos());
        this.selectedIndex = blockEntity.selectedIndex();
    }

    public TalismanPlatformMenu(int id, Inventory playerInventory, BlockPos blockPos) {
        this(id, playerInventory, context(playerInventory.player.level(), blockPos),
                kindOf(playerInventory.player.level(), blockPos),
                playerInventory.player.level(), blockPos);
    }

    public static TalismanPlatformMenu fromNetwork(int id, Inventory playerInventory, FriendlyByteBuf buffer) {
        net.minecraft.core.BlockPos pos = buffer.readBlockPos();
        int selected = buffer.readVarInt();
        TalismanPlatformMenu menu = new TalismanPlatformMenu(id, playerInventory, pos);
        menu.selectedIndex = selected;
        return menu;
    }

    private TalismanPlatformMenu(int id, Inventory playerInventory, Container container,
                                 TalismanPlatformBlockEntity.Kind kind, Level level, BlockPos blockPos) {
        super(ModMenus.TALISMAN_PLATFORM.get(), id);
        this.container = container;
        this.kind = kind;
        this.level = level;
        this.blockPos = blockPos;
        checkContainerSize(container, TalismanPlatformBlockEntity.SIZE);
        container.startOpen(playerInventory.player);
        addDataSlot(new net.minecraft.world.inventory.DataSlot(){
            @Override public int get(){return container instanceof TalismanPlatformBlockEntity be?be.selectedInk():selectedInk;}
            @Override public void set(int value){selectedInk=value;}
        });

        // 放置在此处的槽位由 kind 决定位置：左侧是符文绘制区，右侧是机口与竖排墨水栏。
        for (int i = 0; i < TalismanPlatformBlockEntity.SIZE; i++) {
            final int slotIndex = i;
            addSlot(new Slot(container, i, slotX(kind, i), slotY(kind, i)) {
                @Override public boolean mayPlace(ItemStack stack) {
                    return slotIndex != TalismanPlatformBlockEntity.OUTPUT
                            && TalismanPlatformBlockEntity.isActiveSlot(kind, slotIndex);
                }
                @Override public boolean mayPickup(Player player) { return slotIndex != TalismanPlatformBlockEntity.OUTPUT || hasItem(); }
                @Override public int getMaxStackSize() { return 64; }
            });
        }
        this.machineSlotCount = slots.size();

        String profile=profile(kind);
        int invX=UiLayoutConfig.x(profile,"inventory",kind!=TalismanPlatformBlockEntity.Kind.RUB?135:39);
        int invY=UiLayoutConfig.y(profile,"inventory",kind!=TalismanPlatformBlockEntity.Kind.RUB?248:165);
        // The workbench's inventory rectangle is a visual panel anchor. Keep
        // actual vanilla slot spacing at 18px unless an explicit step is
        // authored; deriving it from the panel width spreads slots apart.
        int configuredStepX=UiLayoutConfig.width(profile,"inventoryStepX",18),configuredStepY=UiLayoutConfig.height(profile,"inventoryStepY",18);
        // Older workbench exports represented these values as a full panel
        // rectangle. Treat oversized values as that legacy shape and retain
        // the vanilla 18px grid; small values remain directly editable.
        int invStep=Math.max(18,configuredStepX<=32?configuredStepX:18);
        int rowStep=Math.max(18,configuredStepY<=32?configuredStepY:18);
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, column + row * 9 + 9,
                        invX + column * invStep, invY + row * rowStep));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, invX + column * invStep, invY + 3 * rowStep));
        }
    }

    private int selectedInk;
    public int selectedInk(){return !level.isClientSide&&container instanceof TalismanPlatformBlockEntity be?be.selectedInk():selectedInk;}
    public void previewInk(int index){selectedInk=index;}
    @Override public boolean clickMenuButton(Player player,int button){
        int index=button-100;
        if(index<0||index>=TalismanPlatformBlockEntity.INK_COUNT||TalismanPlatformBlockEntity.inkStart(kind)<0||!stillValid(player))return false;
        if(container instanceof TalismanPlatformBlockEntity be)be.selectInk(index);
        selectedInk=index;broadcastChanges();return true;
    }
    private int machineSlotCount;
    /** 打开界面时带进来的、上次翻到的符箓页码。 */
    private int selectedIndex;

    private static Container context(Level level, BlockPos blockPos) {
        if (level.getBlockEntity(blockPos) instanceof TalismanPlatformBlockEntity be) return be;
        return new SimpleContainer(TalismanPlatformBlockEntity.SIZE);
    }

    private static TalismanPlatformBlockEntity.Kind kindOf(Level level, BlockPos blockPos) {
        if (level.getBlockEntity(blockPos) instanceof TalismanPlatformBlockEntity be) return be.kind();
        return TalismanPlatformBlockEntity.Kind.ENGRAVE;
    }

    /** 使用菜单容器重建一个可写容器，供服务端逻辑读取。 */
    public Container container() { return container; }
    public TalismanPlatformBlockEntity.Kind kind() { return kind; }
    public BlockPos blockPos() { return blockPos; }
    public int selectedIndex() { return selectedIndex; }

    public int matrixStart() { return 0; }

    /** 机口列 x 坐标：绘制台把墨水栏放左、符纸让到右列；拓印台/镌印台机口靠左。 */
    public static int machineX(TalismanPlatformBlockEntity.Kind kind) {
        return UiLayoutConfig.x(profile(kind),"machine",290);
    }

    /** 竖排墨水栏 x 坐标；镌印台没有墨水栏，返回界面外的哨兵值。 */
    public static int inkX(TalismanPlatformBlockEntity.Kind kind) {
        return switch (kind) {
            case ENGRAVE -> -1000;
            case DRAW -> x(profile(kind),"inkSlot","ink",303);
            case RUB -> x(profile(kind),"inkSlot","ink",118);
        };
    }

    private static int machineY(TalismanPlatformBlockEntity.Kind kind, int index) {
        if (index == TalismanPlatformBlockEntity.OUTPUT) return y(profile(kind),"outputSlot","output",171);
        return switch (kind) {
            case ENGRAVE -> y(profile(kind),index==TalismanPlatformBlockEntity.ENGRAVE_KNIFE?"engraveKnifeSlot":"engraveGoldSlot","engraveInputSlot",95);
            case DRAW -> y(profile(kind),"drawPaperSlot","inputs",171);
            case RUB -> index == TalismanPlatformBlockEntity.RUB_SEAL ? y(profile(kind),"rubSealSlot","inputs",95) : y(profile(kind),"rubPaperSlot","output",171);
        };
    }

    private static int inkY(TalismanPlatformBlockEntity.Kind kind, int index) {
        int start = TalismanPlatformBlockEntity.inkStart(kind);
        if (start < 0) return -1000;
        // 已批准布局：绘制台19像素行距，紧凑拓印台20像素行距。
        int base=y(profile(kind),"inkSlot","ink",kind==TalismanPlatformBlockEntity.Kind.DRAW?64:56);
        int configuredStep=UiLayoutConfig.height(profile(kind),"inkStepY",kind==TalismanPlatformBlockEntity.Kind.DRAW?19:20);
        int step=Math.max(1,configuredStep<=32?configuredStep:(kind==TalismanPlatformBlockEntity.Kind.DRAW?19:20));
        return base+(index-start)*step;
    }

    public static int slotX(TalismanPlatformBlockEntity.Kind kind, int index) {
        if (!TalismanPlatformBlockEntity.isActiveSlot(kind, index)) return -1000;
        if(kind==TalismanPlatformBlockEntity.Kind.RUB)return index==TalismanPlatformBlockEntity.OUTPUT?x(profile(kind),"outputSlot","output",216):index==TalismanPlatformBlockEntity.RUB_SEAL?x(profile(kind),"rubSealSlot","seal",20):index==TalismanPlatformBlockEntity.RUB_PAPER?x(profile(kind),"rubPaperSlot","paper",188):inkX(kind);
        if(kind==TalismanPlatformBlockEntity.Kind.ENGRAVE)return index==TalismanPlatformBlockEntity.OUTPUT?x(profile(kind),"outputSlot","output",354):x(profile(kind),index==TalismanPlatformBlockEntity.ENGRAVE_KNIFE?"engraveKnifeSlot":"engraveGoldSlot","engraveInputSlot",index==TalismanPlatformBlockEntity.ENGRAVE_KNIFE?303:364);
        if(kind==TalismanPlatformBlockEntity.Kind.DRAW){if(index==TalismanPlatformBlockEntity.OUTPUT)return x(profile(kind),"outputSlot","output",354);if(index==TalismanPlatformBlockEntity.DRAW_PAPER)return x(profile(kind),"drawPaperSlot","paper",364);}
        if (TalismanPlatformBlockEntity.isInkSlot(kind, index)) return inkX(kind);
        if (kind == TalismanPlatformBlockEntity.Kind.RUB && index == TalismanPlatformBlockEntity.RUB_SEAL) return 24;
        if (index == TalismanPlatformBlockEntity.OUTPUT) return kind == TalismanPlatformBlockEntity.Kind.ENGRAVE ? 290 : 336;
        if (kind == TalismanPlatformBlockEntity.Kind.ENGRAVE && index == TalismanPlatformBlockEntity.ENGRAVE_GOLD) return 336;
        return machineX(kind);
    }

    public static int slotY(TalismanPlatformBlockEntity.Kind kind, int index) {
        if (!TalismanPlatformBlockEntity.isActiveSlot(kind, index)) return -1000;
        if(kind==TalismanPlatformBlockEntity.Kind.RUB){if(TalismanPlatformBlockEntity.isInkSlot(kind,index))return inkY(kind,index);if(index==TalismanPlatformBlockEntity.OUTPUT)return y(profile(kind),"outputSlot","output",171);return index==TalismanPlatformBlockEntity.RUB_SEAL?y(profile(kind),"rubSealSlot","seal",76):y(profile(kind),"rubPaperSlot","paper",76);}
        if(kind==TalismanPlatformBlockEntity.Kind.ENGRAVE)return index==TalismanPlatformBlockEntity.OUTPUT?y(profile(kind),"outputSlot","output",278):y(profile(kind),index==TalismanPlatformBlockEntity.ENGRAVE_KNIFE?"engraveKnifeSlot":"engraveGoldSlot","engraveInputSlot",83);
        if(kind==TalismanPlatformBlockEntity.Kind.DRAW){if(index==TalismanPlatformBlockEntity.OUTPUT)return y(profile(kind),"outputSlot","output",278);if(index==TalismanPlatformBlockEntity.DRAW_PAPER)return y(profile(kind),"drawPaperSlot","paper",83);}
        if (TalismanPlatformBlockEntity.isInkSlot(kind, index)) return inkY(kind, index);
        return machineY(kind, index);
    }

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
            if (sourceStack.isEmpty()) return ItemStack.EMPTY;
            if (!moveItemStackTo(sourceStack, playerStart, playerEnd, true)) return ItemStack.EMPTY;
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
        if (level == null || !(level.getBlockState(blockPos).getBlock() instanceof TalismanPlatformBlock)) return false;
        if (level.getBlockEntity(blockPos) instanceof TalismanPlatformBlockEntity be && be.kind() == kind
                && container instanceof TalismanPlatformBlockEntity target && be != target) return false;
        return player.distanceToSqr(blockPos.getX() + 0.5D, blockPos.getY() + 0.5D,
                blockPos.getZ() + 0.5D) <= 64.0D && container.stillValid(player);
    }

    @Override public void removed(Player player) {
        super.removed(player);
        container.stopOpen(player);
    }
}
