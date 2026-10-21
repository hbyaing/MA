package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.items.HumanEmperorBannerItem;
import cn.blockforge.generated.generatedmod.registry.ModMenus;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import java.util.List;

/** 人皇幡菜单：管理魂魄顺序、渡灵魂珠和玩家物品栏。 */
public final class HumanEmperorBannerMenu extends AbstractContainerMenu {
    public static final int WIDTH = 420;
    public static final int HEIGHT = 320;
    public static final int LIST_X = 12;
    public static final int LIST_Y = 34;
    public static final int LIST_W = 205;
    public static final int LIST_H = 266;
    public static final int ACTION_X = 230;
    public static final int ACTION_W = 178;
    /** 渡灵槽移到渡灵按钮右侧：按钮收窄后腾出的空间正好放下一格槽位。 */
    public static final int MYRIAD_LIST_X = 236;
    public static final int MYRIAD_LIST_Y = 54;
    public static final int MYRIAD_LIST_W = 126;
    public static final int MYRIAD_LIST_H = 108;
    public static final int PEARL_X = 370;
    public static final int PEARL_Y = 54;
    public static final int INVENTORY_X = 238;
    public static final int INVENTORY_Y = 238;
    public static final int HOTBAR_Y = 296;
    /** 渡灵区只有一个槽位：空闲时收魂，生成魂珠后变为取出槽。 */
    public static final int PEARL_SLOT = 0;
    /** 保留旧名称，避免旧代码或旧客户端映射时出现槽位索引漂移。 */
    public static final int STORAGE_INPUT = PEARL_SLOT;
    public static final int TRANSIT_OUTPUT = PEARL_SLOT;
    private static final String STORED_PEARL = "BannerStoredPearl";
    private static final String TRANSIT_PEARL = "BannerTransitPearl";
    private static final String SUPPRESSED_MYRIAD = "BannerSuppressedMyriad";

    private final Player player;
    private final int inventorySlot;
    private final net.minecraft.core.BlockPos standPos;
    private final Level level;
    private final Container bannerContainer;
    private boolean processingInput;
    private boolean transitReady;
    private String lastBannerStateKey = "";
    /** 引渡到万魂珠后，保持该珠在槽内，避免后续菜单刷新把记录转回人皇幡。 */
    private ItemStack suppressedMyriad;

    public HumanEmperorBannerMenu(int id, Inventory inventory, int inventorySlot) {
        this(id, inventory, inventorySlot, null);
    }

    public HumanEmperorBannerMenu(int id, Inventory inventory, int inventorySlot, net.minecraft.core.BlockPos standPos) {
        super(ModMenus.HUMAN_EMPEROR_BANNER.get(), id);
        this.player = inventory.player;
        this.inventorySlot = inventorySlot;
        this.standPos = standPos;
        this.level = inventory.player.level();
        this.bannerContainer = new net.minecraft.world.SimpleContainer(1);
        loadPearlSlots();
        addSlot(new Slot(bannerContainer, PEARL_SLOT, PEARL_X, PEARL_Y) {
            @Override public boolean mayPlace(ItemStack stack) {
                return !transitReady && (SoulPearlData.isMyriad(stack) || SoulPearlData.isDestroyer(stack)
                        || SoulPearlData.isSoulEnergy(stack)
                        || SoulPearlData.isSoulPearl(stack) && SoulPearlData.hasEntity(stack)
                        || stack.is(net.minecraft.world.item.Items.DRAGON_EGG));
            }

            @Override public boolean mayPickup(Player player) {
                return transitReady || super.mayPickup(player);
            }

            @Override public void onTake(Player player, ItemStack stack) {
                super.onTake(player, stack);
                if (bannerContainer.getItem(PEARL_SLOT).isEmpty()) {
                    transitReady = false;
                    suppressedMyriad = null;
                    savePearlSlots();
                    broadcastChanges();
                }
            }
        });
        addPlayerInventory(inventory);
    }

    public static void openStandMenu(net.minecraft.server.level.ServerPlayer player, net.minecraft.core.BlockPos pos) {
        if (!(player.level().getBlockEntity(pos) instanceof HumanEmperorBannerStandBlockEntity stand)
                || stand.banner().isEmpty()) return;
        net.minecraftforge.network.NetworkHooks.openScreen(player, new net.minecraft.world.SimpleMenuProvider(
                (id, inventory, ignored) -> new HumanEmperorBannerMenu(id, inventory, -1, pos),
                Component.translatable("screen.wuxingxiantu.human_emperor_banner")),
                buffer -> {
                    buffer.writeVarInt(-1);
                    buffer.writeBoolean(true);
                    buffer.writeBlockPos(pos);
                });
    }

    public static HumanEmperorBannerMenu fromNetwork(int id, Inventory inventory, FriendlyByteBuf buffer) {
        int slot = buffer.readVarInt();
        if (!buffer.readBoolean()) return new HumanEmperorBannerMenu(id, inventory, slot);
        return new HumanEmperorBannerMenu(id, inventory, -1,
                buffer.readBlockPos());
    }

    private void addPlayerInventory(Inventory inventory) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inventory, col + row * 9 + 9,
                        INVENTORY_X + col * 18, INVENTORY_Y + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inventory, col, INVENTORY_X + col * 18, HOTBAR_Y));
        }
    }

    private void loadPearlSlots() {
        ItemStack banner = banner();
        if (!banner.hasTag()) return;
        CompoundTag tag = banner.getTag();
        if (tag.contains(TRANSIT_PEARL, net.minecraft.nbt.Tag.TAG_COMPOUND)) {
            bannerContainer.setItem(PEARL_SLOT, ItemStack.of(tag.getCompound(TRANSIT_PEARL)));
            transitReady = true;
        } else if (tag.contains(STORED_PEARL, net.minecraft.nbt.Tag.TAG_COMPOUND)) {
            ItemStack stored = ItemStack.of(tag.getCompound(STORED_PEARL));
            bannerContainer.setItem(PEARL_SLOT, stored);
            if (tag.getBoolean(SUPPRESSED_MYRIAD) && SoulPearlData.isMyriad(stored)) {
                suppressedMyriad = stored.copy();
            }
        }
    }

    private void savePearlSlots() {
        ItemStack banner = banner();
        CompoundTag tag = banner.getOrCreateTag();
        tag.remove(STORED_PEARL);
        tag.remove(TRANSIT_PEARL);
        tag.remove(SUPPRESSED_MYRIAD);
        ItemStack pearl = bannerContainer.getItem(PEARL_SLOT);
        if (!pearl.isEmpty()) {
            tag.put(transitReady ? TRANSIT_PEARL : STORED_PEARL, pearl.save(new CompoundTag()));
            if (!transitReady && suppressedMyriad != null && ItemStack.matches(pearl, suppressedMyriad)) {
                tag.putBoolean(SUPPRESSED_MYRIAD, true);
            }
        }
    }

    @Override public void slotsChanged(Container container) {
        super.slotsChanged(container);
        absorbInputPearl(container);
    }

    @Override public void broadcastChanges() {
        absorbInputPearl(bannerContainer);
        super.broadcastChanges();
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            String stateKey = stateTransferTool().save(new CompoundTag()).toString() + ":" + transitReady
                    + ":" + HumanEmperorBannerItem.bannerState(banner(), level).souls().hashCode();
            if (!stateKey.equals(lastBannerStateKey)) {
                lastBannerStateKey = stateKey;
                Network.sendBannerState(serverPlayer, this);
            }
        }
    }

    private void absorbInputPearl(Container container) {
        if (container != bannerContainer || processingInput || transitReady || level.isClientSide) return;
        ItemStack input = bannerContainer.getItem(PEARL_SLOT);
        if (suppressedMyriad != null) {
            if (!input.isEmpty() && ItemStack.matches(input, suppressedMyriad)) return;
            suppressedMyriad = null;
        }
        if (input.isEmpty()) return;
        processingInput = true;
        try {
            if (input.is(net.minecraft.world.item.Items.DRAGON_EGG)) {
                // 龙蛋进入渡灵槽：立即汲取末影龙魂魄，蛋本身被消耗。
                if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                    if (HumanEmperorBannerItem.absorbDragonEgg(banner(), input) > 0) {
                        input.shrink(1);
                        serverPlayer.displayClientMessage(Component.translatable(
                                "message.wuxingxiantu.banner_dragon_soul"), true);
                        savePearlSlots();
                    }
                }
                return;
            }
            if (SoulPearlData.isMyriad(input)) {
                // 万魂珠必须留在渡灵槽，点击渡灵时才按珠子的吸收/释放模式转移一枚魂魄。
                savePearlSlots();
                return;
            } else if (SoulPearlData.isDestroyer(input) || SoulPearlData.isSoulEnergy(input)) {
                if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                    HumanEmperorBannerItem.onToolInserted(serverPlayer, banner(), input);
                }
            } else {
                int absorbed = HumanEmperorBannerItem.absorbPearlStack(player, banner(), input);
                if (absorbed > 0) input.shrink(absorbed);
            }
            savePearlSlots();
        } finally {
            processingInput = false;
        }
    }

    public int inventorySlot() { return inventorySlot; }
    public ItemStack banner() {
        if (standPos != null && level.getBlockEntity(standPos) instanceof HumanEmperorBannerStandBlockEntity stand) {
            return stand.banner();
        }
        return inventorySlot >= 0 && inventorySlot < player.getInventory().getContainerSize()
                ? player.getInventory().getItem(inventorySlot) : ItemStack.EMPTY;
    }

    /** 返回界面状态使用的万魂珠；客户端槽位同步稍晚时回退到人皇幡保存的副本。 */
    public ItemStack stateTransferTool() {
        ItemStack current = bannerContainer.getItem(PEARL_SLOT);
        if (!current.isEmpty()) return current;
        ItemStack banner = banner();
        if (!banner.hasTag()) return ItemStack.EMPTY;
        CompoundTag tag = banner.getTag();
        if (tag.contains(TRANSIT_PEARL, net.minecraft.nbt.Tag.TAG_COMPOUND)) {
            return ItemStack.of(tag.getCompound(TRANSIT_PEARL));
        }
        if (tag.contains(STORED_PEARL, net.minecraft.nbt.Tag.TAG_COMPOUND)) {
            return ItemStack.of(tag.getCompound(STORED_PEARL));
        }
        return ItemStack.EMPTY;
    }

    public Player player() { return player; }
    public Container bannerContainer() { return bannerContainer; }
    public boolean isTransitReady() { return transitReady; }
    public void setTransitReady(boolean value) { transitReady = value; }
    public boolean isMyriadReleaseMode() {
        return SoulPearlData.isMyriad(bannerContainer.getItem(PEARL_SLOT))
                && SoulPearlData.isMyriadReleaseMode(bannerContainer.getItem(PEARL_SLOT));
    }

    /** 仅人皇幡已经收容的条目可以安排出战或守卫。 */
    public boolean hasBannerSoul(int index) {
        return index >= 0 && index < HumanEmperorBannerItem.bannerState(banner(), level).souls().size();
    }

    /** 将生成的魂珠放回唯一渡灵槽位，并锁定为取出状态，避免被自动收魂。 */
    public boolean prepareTransitPearl(ItemStack pearl) {
        if (transitReady || !bannerContainer.getItem(PEARL_SLOT).isEmpty() || pearl.isEmpty()) return false;
        suppressedMyriad = null;
        transitReady = true;
        bannerContainer.setItem(PEARL_SLOT, pearl);
        return true;
    }

    /** 渡灵工具完成动作后回填槽位，避免万魂珠被再次当作输入自动转移。 */
    public void setToolAfterAction(ItemStack tool) {
        suppressedMyriad = SoulPearlData.isMyriad(tool) ? tool.copy() : null;
        processingInput = true;
        try {
            bannerContainer.setItem(PEARL_SLOT, tool);
        } finally {
            processingInput = false;
        }
    }

    public void handleAction(Player sender, int action, int index) {
        if (sender != player || !(sender instanceof net.minecraft.server.level.ServerPlayer serverPlayer)) return;
        HumanEmperorBannerItem.handleMenuAction(serverPlayer, banner(), action, index);
        savePearlSlots();
    }

    public BannerState state() {
        return HumanEmperorBannerItem.bannerState(banner(), level, stateTransferTool());
    }

    @Override public boolean stillValid(Player current) {
        if (current != player) return false;
        if (standPos != null) {
            return current.distanceToSqr(standPos.getX() + 0.5D, standPos.getY() + 0.5D,
                    standPos.getZ() + 0.5D) <= 64.0D
                    && level.getBlockEntity(standPos) instanceof HumanEmperorBannerStandBlockEntity stand
                    && !stand.banner().isEmpty();
        }
        return inventorySlot >= 0 && inventorySlot < current.getInventory().getContainerSize()
                && current.getInventory().getItem(inventorySlot).getItem() instanceof HumanEmperorBannerItem;
    }

    @Override public ItemStack quickMoveStack(Player movingPlayer, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot == null || !slot.hasItem()) return result;
        ItemStack source = slot.getItem();
        result = source.copy();
        if (index < 1) {
            if (!moveItemStackTo(source, 1, slots.size(), true)) return ItemStack.EMPTY;
        } else if (slot.mayPlace(source) && !moveItemStackTo(source, PEARL_SLOT, PEARL_SLOT + 1, false)) {
            return ItemStack.EMPTY;
        }
        if (source.isEmpty()) {
            slot.set(ItemStack.EMPTY);
            if (index == PEARL_SLOT) {
                transitReady = false;
                suppressedMyriad = null;
                savePearlSlots();
            }
        } else slot.setChanged();
        return result;
    }

    @Override public void removed(Player player) {
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            HumanEmperorBannerItem.onMenuClosed(serverPlayer, banner(), bannerContainer.getItem(PEARL_SLOT));
        }
        savePearlSlots();
        super.removed(player);
    }

    public record BannerState(List<HumanEmperorBannerItem.SoulView> souls,
                              List<HumanEmperorBannerItem.SoulView> myriadSouls,
                              int soulPower, int combatMode) {
        public BannerState {
            souls = List.copyOf(souls);
            myriadSouls = List.copyOf(myriadSouls);
        }
    }
}
