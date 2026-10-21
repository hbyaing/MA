package cn.blockforge.generated.generatedmod.crafting;

import cn.blockforge.generated.generatedmod.items.HeartSwordItem;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import java.util.ArrayList;
import java.util.List;

/** Only crafting grids containing an actual owner tag receive additional equipment rules. */
public final class BoundEquipmentCrafting {
    private BoundEquipmentCrafting() {}
    private static boolean bound(ItemStack stack) {
        return stack.hasTag() && stack.getTag().contains(HeartSwordItem.OWNER_TAG);
    }

    public static boolean update(AbstractContainerMenu menu, Level level, Player player,
                                 CraftingContainer grid, ResultContainer results) {
        if (level.isClientSide || !(player instanceof ServerPlayer serverPlayer)) return false;
        List<ItemStack> inputs = new ArrayList<>();
        boolean hasOwner = false;
        for (int i=0;i<grid.getContainerSize();i++) {
            ItemStack stack = grid.getItem(i); inputs.add(stack); hasOwner |= bound(stack);
        }
        if (!hasOwner) return false;
        ItemStack output = ItemStack.EMPTY;
        if (EquipmentBond.canConsume(player, inputs)) {
            var found = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, grid, level);
            if (found.isPresent() && results.setRecipeUsed(level, serverPlayer, found.get())) {
                var assembled = found.get().assemble(grid, level.registryAccess());
                if (assembled.isItemEnabled(level.enabledFeatures())) output = EquipmentCrafting.inherit(assembled, inputs);
            }
        }
        // Match vanilla's result sync; no ingredient is touched before the normal ResultSlot.onTake.
        results.setItem(0, output);
        menu.setRemoteSlot(0, output);
        serverPlayer.connection.send(new ClientboundContainerSetSlotPacket(menu.containerId,menu.incrementStateId(),0,output));
        return true;
    }

    public static boolean canTake(AbstractContainerMenu menu, Player player) {
        int size = menu instanceof CraftingMenu ? 9 : menu instanceof InventoryMenu ? 4 : 0;
        if (size==0) return true;
        List<ItemStack> inputs = new ArrayList<>();
        for (int i=1;i<=size;i++) inputs.add(menu.getSlot(i).getItem());
        return EquipmentBond.canConsume(player, inputs);
    }
}
