package cn.blockforge.generated.generatedmod;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** 符箓系统的服务端事件：把丢入异火的灵煤转化为灵墨。 */
@Mod.EventBusSubscriber(modid = GeneratedMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class TalismanForgeEvents {
    private TalismanForgeEvents() {}

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void levelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.level.isClientSide) return;
        ServerLevel level = (ServerLevel) event.level;
        if (level.getGameTime() % 10L != 0L) return;
        for (ItemEntity entity : level.getEntities(
                net.minecraft.world.entity.EntityType.ITEM,
                item -> item.isAlive() && !item.getItem().isEmpty())) {
            convertCoalInFire(level, entity);
        }
    }

    private static void convertCoalInFire(ServerLevel level, ItemEntity entity) {
        ItemStack stack = entity.getItem();
        if (!stack.is(WuXingItems.SPIRIT_COAL.get())) return;
        BlockPos pos = entity.blockPosition();
        boolean inFire = isExoticFire(level, pos) || level.getBlockState(pos).is(Blocks.FIRE);
        if (!inFire) return;
        ItemStack ink = new ItemStack(WuXingItems.SPIRIT_INK.get());
        stack.shrink(1);
        if (stack.isEmpty()) {
            entity.setItem(ink);
        } else {
            level.addFreshEntity(new ItemEntity(level, entity.getX(), entity.getY(), entity.getZ(), ink));
        }
    }

    private static boolean isExoticFire(Level level, BlockPos pos) {
        for (net.minecraftforge.registries.RegistryObject<net.minecraft.world.level.block.Block> fire : WuXingBlocks.EXOTIC_FIRES) {
            if (level.getBlockState(pos).is(fire.get())) return true;
        }
        return false;
    }
}
