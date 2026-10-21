package cn.blockforge.generated.generatedmod;

import net.minecraft.core.NonNullList;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** 愈魂室的修复逻辑：每点燃一份灵魂沙或灵魂土获得5点魂力。 */
public final class SoulHealingRoomBlockEntity extends BlockEntity implements Container {
    public static final int PEARL_SLOT = 0;
    public static final int FUEL_SLOT = 1;
    public static final int MAX_PROGRESS = 100;
    public static final int MAX_SOUL_POWER = SoulPearlData.MAX_SOUL_ENERGY;
    private static final int REPAIR_POWER_BATCH = 5;
    private static final int ENERGY_CHARGE_AMOUNT = 5;
    private final NonNullList<ItemStack> items = NonNullList.withSize(2, ItemStack.EMPTY);
    private int progress;
    private int soulPower;
    private final ContainerData data = new ContainerData() {
        @Override public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> soulPower;
                default -> 0;
            };
        }
        @Override public void set(int index, int value) {
            if (index == 0) progress = Math.max(0, Math.min(MAX_PROGRESS, value));
            if (index == 1) soulPower = Math.max(0, Math.min(MAX_SOUL_POWER, value));
        }
        @Override public int getCount() { return 2; }
    };

    public SoulHealingRoomBlockEntity(net.minecraft.world.level.block.entity.BlockEntityType<?> type,
                                      BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public ContainerData dataAccess() { return data; }
    public MenuProvider menuProvider() {
        return new SimpleMenuProvider((id, inventory, player) -> new SoulHealingRoomMenu(id, inventory, this),
                Component.translatable("block.wuxingxiantu.soul_healing_room"));
    }

    public void insertHeldItem(Player player, net.minecraft.world.InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        int slot = SoulPearlData.isRepairableSoulPearl(held) || SoulPearlData.isSoulEnergy(held)
                || held.getItem() instanceof cn.blockforge.generated.generatedmod.items.HumanEmperorBannerItem
                || SoulPearlData.isMyriad(held) && SoulPearlData.storedBrokenSoulTotal(held) > 0
                ? PEARL_SLOT : isFuel(held) ? FUEL_SLOT : -1;
        if (slot < 0 || !canPlaceItem(slot, held)) return;
        if (slot == PEARL_SLOT) {
            items.set(slot, held.copyWithCount(1));
            progress = 0;
            soulPower = SoulPearlData.isSoulEnergy(held) ? SoulPearlData.soulEnergy(held) : 0;
        } else if (items.get(slot).isEmpty()) items.set(slot, held.copyWithCount(1));
        else items.get(slot).grow(1);
        held.shrink(1);
        setChanged();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SoulHealingRoomBlockEntity room) {
        if (level.isClientSide || room.pearl().isEmpty()) return;
        // 人皇幡放入愈魂室：以魂力值（1 魂力 : 1 血量）补充幡内魂魄血量，燃料每份转化 200 魂力。
        if (room.pearl().getItem() instanceof cn.blockforge.generated.generatedmod.items.HumanEmperorBannerItem) {
            if (cn.blockforge.generated.generatedmod.items.HumanEmperorBannerItem.soulPower(room.pearl())
                    >= cn.blockforge.generated.generatedmod.items.HumanEmperorBannerItem.MAX_SOUL_POWER) return;
            if (!isFuel(room.fuel())) return;
            boolean soulSand = room.fuel().is(Items.SOUL_SAND);
            room.fuel().shrink(1);
            cn.blockforge.generated.generatedmod.items.HumanEmperorBannerItem.pourSoulPower(room.pearl(), 200);
            net.minecraft.world.Containers.dropItemStack(level, pos.getX() + .5D, pos.getY() + .7D,
                    pos.getZ() + .5D, new ItemStack(soulSand ? Items.SAND : Items.DIRT));
            room.setChanged();
            return;
        }
        if (SoulPearlData.isSoulEnergy(room.pearl())) {
            if (SoulPearlData.isCreativeSoulEnergy(room.pearl()) || SoulPearlData.soulEnergy(room.pearl()) >= SoulPearlData.MAX_SOUL_ENERGY) return;
            if (!isFuel(room.fuel())) return;
            boolean soulSand = room.fuel().is(Items.SOUL_SAND);
            room.fuel().shrink(1);
            SoulPearlData.setSoulEnergy(room.pearl(), SoulPearlData.soulEnergy(room.pearl()) + ENERGY_CHARGE_AMOUNT);
            room.soulPower = SoulPearlData.soulEnergy(room.pearl());
            net.minecraft.world.Containers.dropItemStack(level, pos.getX() + .5D, pos.getY() + .7D,
                    pos.getZ() + .5D, new ItemStack(soulSand ? Items.SAND : Items.DIRT));
            room.setChanged();
            return;
        }
        // 万魂珠可以直接放入愈魂室，修复其中的全部破碎魂魄。
        if (SoulPearlData.isMyriad(room.pearl())) {
            if (room.progress >= MAX_PROGRESS || SoulPearlData.storedBrokenSoulTotal(room.pearl()) <= 0) return;
            if (room.soulPower <= 0) {
                if (!isFuel(room.fuel())) return;
                boolean soulSand = room.fuel().is(Items.SOUL_SAND);
                room.fuel().shrink(1);
                net.minecraft.world.Containers.dropItemStack(level, pos.getX() + .5D, pos.getY() + .7D,
                        pos.getZ() + .5D, new ItemStack(soulSand ? Items.SAND : Items.DIRT));
                room.soulPower = REPAIR_POWER_BATCH;
            }
            room.soulPower--;
            room.progress = Math.min(MAX_PROGRESS, room.progress + 1);
            if (room.progress >= MAX_PROGRESS) {
                int repaired = SoulPearlData.repairMyriad(room.pearl());
                room.soulPower = 0;
                level.getServer().getPlayerList().getPlayers().stream()
                        .filter(player -> player.distanceToSqr(pos.getX() + .5D, pos.getY() + .5D, pos.getZ() + .5D) <= 64.0D)
                        .forEach(player -> player.displayClientMessage(Component.translatable(
                                "message.wuxingxiantu.myriad_soul_repaired", repaired), true));
                room.setChanged();
            }
            if (level.getGameTime() % 10L == 0L) room.setChanged();
            return;
        }
        // 完成后的魂珠保持满进度，机器停机，避免重复消耗燃料。
        if (room.progress >= MAX_PROGRESS) return;
        if (room.soulPower <= 0) {
            if (!isFuel(room.fuel())) return;
            boolean soulSand = room.fuel().is(Items.SOUL_SAND);
            room.fuel().shrink(1);
            net.minecraft.world.Containers.dropItemStack(level, pos.getX() + .5D, pos.getY() + .7D,
                    pos.getZ() + .5D, new ItemStack(soulSand ? Items.SAND : Items.DIRT));
            room.soulPower = REPAIR_POWER_BATCH;
        }
        room.soulPower--;
        room.progress = Math.min(MAX_PROGRESS, room.progress + 1);
        if (room.progress >= MAX_PROGRESS) {
            ItemStack repaired = room.pearl().copy();
            CompoundTag originalTag = repaired.hasTag() ? repaired.getTag().copy() : new CompoundTag();
            repaired = new ItemStack(WuXingItems.SOUL_PEARL.get(), repaired.getCount());
            repaired.setTag(originalTag);
            CompoundTag tag = repaired.getOrCreateTag();
            tag.putBoolean("IsBroken", false);
            room.items.set(PEARL_SLOT, repaired);
            room.soulPower = 0;
            if (level instanceof net.minecraft.server.level.ServerLevel server) {
                server.getPlayers(player -> player.distanceToSqr(pos.getX() + .5D, pos.getY() + .5D, pos.getZ() + .5D) <= 64.0D)
                        .forEach(player -> player.displayClientMessage(
                                Component.translatable("message.wuxingxiantu.soul_pearl_repaired"), true));
            }
            room.setChanged();
        }
        if (level.getGameTime() % 10L == 0L) room.setChanged();
    }

    public ItemStack pearl() { return items.get(PEARL_SLOT); }
    public ItemStack fuel() { return items.get(FUEL_SLOT); }
    public int progress() { return progress; }
    public int soulPower() { return soulPower; }
    public static boolean isFuel(ItemStack stack) { return stack.is(Items.SOUL_SAND) || stack.is(Items.SOUL_SOIL); }
    private static boolean isBrokenSoulPearl(ItemStack stack) {
        return SoulPearlData.isRepairableSoulPearl(stack);
    }

    @Override public int getContainerSize() { return 2; }
    @Override public boolean isEmpty() { return pearl().isEmpty() && fuel().isEmpty(); }
    @Override public ItemStack getItem(int slot) { return slot >= 0 && slot < 2 ? items.get(slot) : ItemStack.EMPTY; }
    @Override public ItemStack removeItem(int slot, int amount) {
        if (slot < 0 || slot >= 2) return ItemStack.EMPTY;
        ItemStack result = net.minecraft.world.ContainerHelper.removeItem(items, slot, amount);
        if (!result.isEmpty()) {
            if (slot == PEARL_SLOT && items.get(PEARL_SLOT).isEmpty()) resetRepairState();
            setChanged();
        }
        return result;
    }
    @Override public ItemStack removeItemNoUpdate(int slot) {
        if (slot < 0 || slot >= 2) return ItemStack.EMPTY;
        ItemStack result = net.minecraft.world.ContainerHelper.takeItem(items, slot);
        if (slot == PEARL_SLOT && items.get(PEARL_SLOT).isEmpty()) resetRepairState();
        return result;
    }
    @Override public void setItem(int slot, ItemStack stack) {
        if (slot >= 0 && slot < 2) {
            ItemStack previous = items.get(slot);
            items.set(slot, stack);
            if (slot == PEARL_SLOT && !net.minecraft.world.item.ItemStack.matches(previous, stack)) {
                resetRepairState();
                if (SoulPearlData.isSoulEnergy(stack)) soulPower = SoulPearlData.soulEnergy(stack);
            }
            setChanged();
        }
    }

    private void resetRepairState() {
        progress = 0;
        soulPower = 0;
    }
    @Override public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot == PEARL_SLOT) {
            return (isBrokenSoulPearl(stack) || SoulPearlData.isSoulEnergy(stack)
                    || stack.getItem() instanceof cn.blockforge.generated.generatedmod.items.HumanEmperorBannerItem
                    || SoulPearlData.isMyriad(stack) && SoulPearlData.storedBrokenSoulTotal(stack) > 0)
                    && pearl().isEmpty();
        }
        return slot == FUEL_SLOT && isFuel(stack);
    }
    @Override public int getMaxStackSize() { return 64; }
    @Override public boolean stillValid(Player player) { return Container.stillValidBlockEntity(this, player); }
    @Override public void clearContent() { items.set(0, ItemStack.EMPTY); items.set(1, ItemStack.EMPTY); resetRepairState(); setChanged(); }
    @Override public void startOpen(Player player) {}
    @Override public void stopOpen(Player player) {}
    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        net.minecraft.world.ContainerHelper.saveAllItems(tag, items);
        tag.putInt("Progress", progress);
        tag.putInt("SoulPower", soulPower);
    }
    @Override public void load(CompoundTag tag) {
        super.load(tag);
        net.minecraft.world.ContainerHelper.loadAllItems(tag, items);
        progress = Math.max(0, Math.min(MAX_PROGRESS, tag.getInt("Progress")));
        soulPower = Math.max(0, Math.min(MAX_SOUL_POWER, tag.getInt("SoulPower")));
    }
}
