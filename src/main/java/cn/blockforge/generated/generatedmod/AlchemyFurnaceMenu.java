package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.registry.ModMenus;
import cn.blockforge.generated.generatedmod.items.FireSeedItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * 八卦炼丹面板菜单：八个八卦素材槽 + 一个异火瓶点火槽，下方为玩家物品栏。
 * 槽位和界面共用 {@link AlchemyUiLayout} 坐标。
 */
public final class AlchemyFurnaceMenu extends AbstractContainerMenu {
    public static final int IMAGE_WIDTH = AlchemyUiLayout.WIDTH;
    public static final int IMAGE_HEIGHT = AlchemyUiLayout.HEIGHT;

    public static final int MATERIAL_SLOT_START = AlchemyFurnaceBlockEntity.MATERIAL_SLOT_START;
    public static final int MATERIAL_SLOT_COUNT = AlchemyFurnaceBlockEntity.MATERIAL_SLOT_COUNT;
    public static final int IGNITION_SLOT = AlchemyFurnaceBlockEntity.IGNITION_SLOT;

    /** 太极静态装饰中心，悬停显示配方信息。 */
    public static final int CENTER_X = AlchemyUiLayout.CENTER_X;
    public static final int CENTER_Y = AlchemyUiLayout.CENTER_Y;
    /** 异火瓶点火槽位于八卦环下方正中。 */
    public static final int IGNITION_X = AlchemyUiLayout.FIRE_X;
    public static final int IGNITION_Y = AlchemyUiLayout.FIRE_Y;

    public static final int INVENTORY_X = AlchemyUiLayout.INVENTORY_COLUMNS[0];
    public static final int INVENTORY_Y = AlchemyUiLayout.INVENTORY_ROWS[0];
    public static final int HOTBAR_Y = AlchemyUiLayout.INVENTORY_ROWS[3];

    private final Container container;
    private final Level level;
    private final BlockPos blockPos;
    private final AlchemyFurnaceBlockEntity furnace;
    private final int machineSlotCount;
    private final Player viewer;
    private final net.minecraft.world.inventory.ContainerData viewData = new net.minecraft.world.inventory.SimpleContainerData(19);
    public boolean formulaUnlocked() { return viewData.get(8)!=0; }
    public boolean knowsFormula(AlchemyRecipe recipe) {
        if(recipe==null)return false;
        var pill=cn.blockforge.generated.generatedmod.alchemy.PillCatalog.byId(recipe.formulaId().getPath());
        int bits=(viewData.get(9)&0x7fff)|((viewData.get(10)&0x7fff)<<15);
        return pill!=null&&(bits&(1<<pill.ordinal()))!=0;
    }
    public int spiritFuel() { return (viewData.get(7)&0x7fff)|(viewData.get(11)<<15); }
    public int v5RequiredFuel(){return (viewData.get(12)&0x7fff)|(viewData.get(13)<<15);}
    public int v5Warning(){return viewData.get(14);}
    public int v5HeatMode(){return viewData.get(15);}
    public int v5StructureDamage(){return viewData.get(16);}
    public boolean v5Warming(){return viewData.get(17)!=0;}
    public int v5DeviceWear(){return viewData.get(18);}
    private int v5RequiredFuel;
    public int spiritCapacity() { return cn.blockforge.generated.generatedmod.alchemy.AlchemyBalance.fuelCapacity(furnaceTier()); }
    public enum State { SYNCING, MISSING, LOW_TIER, NEED_FIRE, NEED_AURA, BUSY, READY, BREWING, COMPLETE, FAILED, UNFORMED,
        NEED_FORMULA, NEED_REALM, FIRE_UNMASTERED, FIRE_REQUIREMENT, NEED_ALCHEMY, NEED_FUEL, NEED_HERB_AGE, DAMAGED }
    private net.minecraft.resources.ResourceLocation chosenRecipe;
    private boolean freeMode;
    public boolean freeMode() { return viewData.get(6) == 1; }

    public AlchemyFurnaceMenu(int id, Inventory playerInventory, AlchemyFurnaceBlockEntity furnace) {
        super(ModMenus.ALCHEMY_FURNACE.get(), id);
        this.furnace = furnace;
        this.freeMode = furnace.v5Batch();
        this.viewer = playerInventory.player;
        this.container = furnace;
        this.level = furnace.getLevel();
        this.blockPos = furnace.getBlockPos();
        checkContainerSize(this.container, AlchemyFurnaceBlockEntity.CONTAINER_SIZE);
        this.container.startOpen(playerInventory.player);

        for (int i = 0; i < MATERIAL_SLOT_COUNT; i++) {
            final int containerSlot = MATERIAL_SLOT_START + i;
            int x = AlchemyUiLayout.MATERIAL_SLOTS[i][0];
            int y = AlchemyUiLayout.MATERIAL_SLOTS[i][1];
            addSlot(new Slot(this.container, containerSlot, x, y) {
                @Override public boolean mayPlace(ItemStack stack) { return container.canPlaceItem(containerSlot, stack); }
                @Override public boolean mayPickup(Player player) { return !furnace.inventoryLocked(); }
                @Override public void setChanged() { super.setChanged(); furnace.sync(); }
            });
        }
        addSlot(new Slot(this.container, IGNITION_SLOT, IGNITION_X, IGNITION_Y) {
            @Override public boolean mayPlace(ItemStack stack) { return !furnace.busy() && isIgnition(stack); }
            @Override public boolean mayPickup(Player player) { return !furnace.inventoryLocked(); }
            @Override public int getMaxStackSize() { return 1; }
        });
        addSlot(new Slot(this.container,AlchemyFurnaceBlockEntity.CORE_SLOT,AlchemyUiLayout.CORE_X,AlchemyUiLayout.CORE_Y) {
            @Override public boolean mayPlace(ItemStack stack) { return container.canPlaceItem(AlchemyFurnaceBlockEntity.CORE_SLOT,stack); }
            @Override public boolean mayPickup(Player player) { return !furnace.inventoryLocked(); }
            @Override public int getMaxStackSize() { return 1; }
            @Override public void setChanged() { super.setChanged();furnace.sync(); }
        });
        this.machineSlotCount = slots.size();

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, column + row * 9 + 9,
                        AlchemyUiLayout.INVENTORY_COLUMNS[column], AlchemyUiLayout.INVENTORY_ROWS[row]));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column,
                    AlchemyUiLayout.INVENTORY_COLUMNS[column], HOTBAR_Y));
        }
        if(level instanceof net.minecraft.server.level.ServerLevel server)try{
            v5RequiredFuel=cn.blockforge.generated.generatedmod.alchemy.batch.V5Batch.profile(cn.blockforge.generated.generatedmod.alchemy.batch.V5Batch.currentProfile(server),cn.blockforge.generated.generatedmod.alchemy.AlchemyBalance.burnPerSecond(furnace.tier())).requiredFuel();
        }catch(RuntimeException e){v5RequiredFuel=Integer.MAX_VALUE;}
        refreshViewData();
        addDataSlots(viewData);
    }

    public AlchemyFurnaceMenu(int id, Inventory playerInventory, BlockPos blockPos) {
        this(id, playerInventory, furnaceAt(playerInventory.player.level(), blockPos));
    }

    public static AlchemyFurnaceMenu fromNetwork(int id, Inventory playerInventory, FriendlyByteBuf buffer) {
        return new AlchemyFurnaceMenu(id, playerInventory, buffer.readBlockPos());
    }

    private static AlchemyFurnaceBlockEntity furnaceAt(Level level, BlockPos blockPos) {
        if (level.getBlockEntity(blockPos) instanceof AlchemyFurnaceBlockEntity furnace) return furnace;
        AlchemyFurnaceBlockEntity fallback = new AlchemyFurnaceBlockEntity(
                cn.blockforge.generated.generatedmod.registry.ModBlockEntities.ALCHEMY_FURNACE.get(),
                blockPos, level.getBlockState(blockPos));
        fallback.setLevel(level);
        return fallback;
    }

    private static boolean isIgnition(ItemStack stack) {
        return stack.getCount() == 1 && cn.blockforge.generated.generatedmod.fire.FireBottleData.tier(stack) >= 1;
    }

    public AlchemyFurnaceBlockEntity furnace() {
        // Opening can arrive before the core's block update. Replace the
        // temporary client snapshot for result/animation reads once it loads.
        if(level!=null&&level.isClientSide&&level.getBlockEntity(blockPos) instanceof AlchemyFurnaceBlockEntity live)return live;
        return furnace;
    }
    public Container container() { return container; }
    public BlockPos blockPos() { return blockPos; }
    public State state() { return State.values()[Math.max(0, Math.min(State.values().length - 1, viewData.get(0)))]; }
    public int furnaceTier() { return viewData.get(1); }
    public boolean isBrewing() { return state() == State.BREWING; }
    public float brewingProgress() { return viewData.get(3) / 10000.0F; }
    public int ignitionTier() { return viewData.get(2); }
    public boolean ignited() { return ignitionTier() > 0; }
    public int outcome() { return viewData.get(5); }
    private java.util.List<AlchemyRecipe> freeRecipeSource;
    private final ItemStack[] freeInputs=new ItemStack[MATERIAL_SLOT_COUNT];
    private AlchemyRecipe freeRecipeResult;
    public java.util.List<AlchemyRecipe> recipes() {
        return cn.blockforge.generated.generatedmod.alchemy.FreeAlchemy.sortedRecipes(level);
    }
    private AlchemyRecipe freeRecipe() {
        var source=recipes();boolean changed=source!=freeRecipeSource;
        for(int i=0;i<MATERIAL_SLOT_COUNT&&!changed;i++)
            changed=freeInputs[i]==null||!ItemStack.matches(freeInputs[i],container.getItem(i));
        if(!changed)return freeRecipeResult;
        freeRecipeSource=source;
        for(int i=0;i<MATERIAL_SLOT_COUNT;i++)freeInputs[i]=container.getItem(i).copy();
        return freeRecipeResult=cn.blockforge.generated.generatedmod.alchemy.FreeAlchemy.attemptRecipe(container,source,level);
    }
    public AlchemyRecipe selectedRecipe() {
        if (freeMode()) return null;
        var recipes = recipes();
        int index = viewData.get(4) - 1;
        return index >= 0 && index < recipes.size() ? recipes.get(index) : null;
    }
    public int selectedRecipeIndex() { return viewData.get(4) - 1; }
    public boolean canSelectRecipe(int direction) {
        int index = selectedRecipeIndex(), count = recipes().size();
        return !isBrewing() && count > 0 && (direction == 0 ? index > 0 : index < count - 1);
    }
    @Override public boolean clickMenuButton(Player player, int button) {
        if (level.isClientSide || !stillValid(player)) return false;
        // Vanilla encodes the button ID in one byte; decode it unsigned for long recipe lists.
        button &= 255;
        if(button==249||button==250){
            boolean quick=button==249;boolean collected=(quick?furnace.collectResult(player):furnace.collectResultToCursor(player,this))>0;
            if(!collected&&furnace.hasResult())player.displayClientMessage(net.minecraft.network.chat.Component.literal(quick?
                    "背包空间不足，成品保留在丹炉中。":"鼠标所持物品不同或已满，成品保留在丹炉中。"),true);
            refreshViewData();broadcastChanges();return collected;
        }
        if(button>=245&&button<=247&&furnace.v5Batch()&&player instanceof net.minecraft.server.level.ServerPlayer owner){boolean changed=furnace.v5Control(owner,button);refreshViewData();broadcastChanges();return changed;}
        if(button==248&&furnace.v5Batch())return furnace.v5ServerRecord().hasUUID("Owner")&&furnace.v5ServerRecord().getUUID("Owner").equals(player.getUUID())&&furnace.sealV5Batch();
        if(button==253) { boolean changed=furnace.injectSpirit(player)>0;refreshViewData();broadcastChanges();return changed; }
        if(button==254) {
            boolean upgraded=furnace.upgradeCore(player);
            if(!upgraded)player.displayClientMessage(net.minecraft.network.chat.Component.literal("升阶需要更高级核心、对应境界，且丹炉处于空闲状态。"),true);
            return upgraded;
        }
        if(furnace.inventoryLocked())return false;
        if(button==251||button==252) {
            if(button==251&&freeMode())return false;
            var result=AlchemyMaterialTransfer.move(container,player.getInventory(),selectedRecipe(),button==252);
            if(result!=AlchemyMaterialTransfer.Result.OK)player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                    result==AlchemyMaterialTransfer.Result.INVENTORY_FULL?"背包空间不足，材料保留在丹炉中。":
                    result==AlchemyMaterialTransfer.Result.SLOTS_FULL?"八卦槽空间不足，请先清空无关材料。":
                    result==AlchemyMaterialTransfer.Result.AGE_MISSING?"缺少达到本丹阶的配方药材，请准备足龄引药。":"材料不足，无法放入完整一炉材料。"),true);
            furnace.sync();refreshViewData();broadcastChanges();
            return result==AlchemyMaterialTransfer.Result.OK;
        }
        if (button == 255) {
            freeMode = true; chosenRecipe = null;
            refreshViewData(); broadcastChanges(); return true;
        }
        var choices = recipes();
        if (choices.isEmpty()) return false;
        int current = selectedRecipeIndex();
        int next = button >= 2 ? button - 2 : current + (button == 0 ? -1 : 1);
        if (next < 0 || next >= choices.size()) return false;
        freeMode = false;
        chosenRecipe = choices.get(next).getId();
        refreshViewData();
        broadcastChanges();
        return true;
    }
    private void refreshViewData() {
        if (level == null || level.isClientSide) return;
        viewData.set(14,furnace.v5Warning());viewData.set(15,furnace.v5HeatMode());viewData.set(16,furnace.v5StructureDamage());viewData.set(17,furnace.v5Warming()?1:0);viewData.set(18,furnace.v5DeviceWear());
        viewData.set(12,v5RequiredFuel&0x7fff);viewData.set(13,v5RequiredFuel>>>15);
        viewData.set(1, furnace.tier());
        viewData.set(2, furnace.ignitionTier());
        viewData.set(3, Math.round(furnace.brewingProgress() * 10000));
        viewData.set(4, 0);
        viewData.set(5, furnace.outcome());
        viewData.set(6, freeMode ? 1 : 0);
        viewData.set(7,furnace.spiritFuel()&0x7fff);viewData.set(11,furnace.spiritFuel()>>>15);
        viewData.set(8,cn.blockforge.generated.generatedmod.alchemy.FormulaKnowledge.unlocked(viewer)?1:0);
        int learned=0;
        for(var pill:cn.blockforge.generated.generatedmod.alchemy.PillCatalog.values())
            if(cn.blockforge.generated.generatedmod.alchemy.FormulaKnowledge.knows(viewer,
                    new net.minecraft.resources.ResourceLocation("wuxingxiantu",pill.id)))learned|=1<<pill.ordinal();
        // Vanilla menu values travel as signed shorts. Split at 15 bits so
        // every formula, including ROOT and PHYSIQUE, survives transport.
        viewData.set(9,learned&0x7fff);viewData.set(10,(learned>>>15)&0x7fff);
        State state;
        boolean higherRecipe = false;
        AlchemyRecipe match = freeMode ? freeRecipe() : null;
        var recipes = recipes();
        for (int i = 0; !freeMode && i < recipes.size(); i++) {
            var recipe = recipes.get(i);
            if (chosenRecipe != null && recipe.getId().equals(chosenRecipe)) {
                match = recipe;
                viewData.set(4, i + 1);
                break;
            }
            if (chosenRecipe != null) continue;
            if (!recipe.matches(container, level)) continue;
            if (recipe.furnaceTier() > furnace.tier()) { higherRecipe = true; continue; }
            match = recipe;
            viewData.set(4, i + 1);
            break;
        }
        var data = GeneratedMod.data(viewer);
        if(furnace.v5Batch()&&furnace.hasResult())state=State.COMPLETE;
        else if(furnace.v5Batch()&&furnace.busy())state=State.BREWING;
        else if (!AlchemyFurnaceBlock.isFormed(furnace.getBlockState())) state = State.UNFORMED;
        else if (furnace.busy()) state = State.BREWING;
        else if (furnace.hasResult()) state = State.COMPLETE;
        else if (Network.isAlchemyBusy(viewer)) state = State.BUSY;
        else if (!cn.blockforge.generated.generatedmod.alchemy.FormulaKnowledge.unlocked(viewer)) state = State.NEED_ALCHEMY;
        else if (freeMode) {
            if(furnace.v5DeviceWear()>=1000)state=State.DAMAGED;
            else if (!furnace.hasAnyMaterial()) state=State.MISSING;
            else if (!ProgressionGate.canUseAlchemyFurnace(viewer,furnace.tier())) state=State.NEED_REALM;
            else if (!furnace.ignited()) state=State.NEED_FIRE;
            else if (!ProgressionGate.canUseFire(viewer,furnace.ignitionTier())) state=State.FIRE_UNMASTERED;
            else if (data==null) state=State.NEED_AURA;
            else if(furnace.spiritFuel()<v5RequiredFuel) state=State.NEED_FUEL;
            else state=State.READY;
        }
        else if (match == null) state = higherRecipe ? State.LOW_TIER
                : furnace.outcome() == 1 ? State.COMPLETE : furnace.outcome() == 2 ? State.FAILED : State.MISSING;
        else if (!freeMode && !cn.blockforge.generated.generatedmod.alchemy.FormulaKnowledge.knows(viewer, match.formulaId())) state = State.NEED_FORMULA;
        else if (match.furnaceTier() > furnace.tier()) state = State.LOW_TIER;
        else if (!ProgressionGate.canUseAlchemyFurnace(viewer, furnace.tier())
                || !ProgressionGate.isAtLeastRealm(viewer, match.minimumRealm())) state = State.NEED_REALM;
        else if (!match.matches(container, level)) state = match.tryIngredientReservation(container).isPresent()?State.NEED_HERB_AGE:State.MISSING;
        else if (!furnace.ignited()) state = State.NEED_FIRE;
        else if (!ProgressionGate.canUseFire(viewer, furnace.ignitionTier())) state = State.FIRE_UNMASTERED;
        else if (!match.acceptsFire(furnace.ignitionTier())) state = State.FIRE_REQUIREMENT;
        else if (data == null || data.wuxingxiantu$getAura() < match.auraCost(data)) state = State.NEED_AURA;
        else if(furnace.spiritFuel()<10*cn.blockforge.generated.generatedmod.alchemy.AlchemyBalance.burnPerSecond(furnace.tier()))state=State.NEED_FUEL;
        else state = State.READY;
        viewData.set(0, state.ordinal());
    }
    @Override public void broadcastChanges() {
        refreshViewData();
        super.broadcastChanges();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        Slot source = slots.get(index);
        if (!source.hasItem() || !source.mayPickup(player)) return ItemStack.EMPTY;
        ItemStack sourceStack = source.getItem();
        ItemStack original = sourceStack.copy();
        int playerStart = machineSlotCount;
        int mainInventoryEnd = playerStart + 27;
        int playerEnd = mainInventoryEnd + 9;

        if (index < machineSlotCount) {
            if (!moveItemStackTo(sourceStack, playerStart, playerEnd, true)) return ItemStack.EMPTY;
        } else if (!furnace.busy() && container.getItem(AlchemyFurnaceBlockEntity.CORE_SLOT).isEmpty()
                && container.canPlaceItem(AlchemyFurnaceBlockEntity.CORE_SLOT,sourceStack)
                && moveItemStackTo(sourceStack,AlchemyFurnaceBlockEntity.CORE_SLOT,AlchemyFurnaceBlockEntity.CORE_SLOT+1,false)) {
            // Upgrade core has its own persistent machine slot.
        } else if (!furnace.busy() && isIgnition(sourceStack)
                && container.getItem(IGNITION_SLOT).isEmpty()
                && moveItemStackTo(sourceStack, IGNITION_SLOT, IGNITION_SLOT + 1, false)) {
            // 已移入异火瓶点火槽。
        } else if (!furnace.busy() && !(sourceStack.getItem() instanceof FireSeedItem) && sourceStack.getMaxStackSize() > 0
                && moveItemStackTo(sourceStack, MATERIAL_SLOT_START, MATERIAL_SLOT_START + MATERIAL_SLOT_COUNT, false)) {
            // 已移入某个八卦素材槽。
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

    @Override
    public boolean stillValid(Player player) {
        if (level == null || !(level.getBlockEntity(blockPos) instanceof AlchemyFurnaceBlockEntity)
                || !AlchemyFurnaceBlock.isFormed(level.getBlockState(blockPos))&&!furnace().v5Batch()&&furnace().v5DeviceWear()==0) return false;
        if (furnace != null && level.getBlockEntity(blockPos) != furnace) return false;
        return player.distanceToSqr(blockPos.getX() + 0.5D, blockPos.getY() + 0.5D,
                blockPos.getZ() + 0.5D) <= 64.0D && container.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        container.stopOpen(player);
    }
}
