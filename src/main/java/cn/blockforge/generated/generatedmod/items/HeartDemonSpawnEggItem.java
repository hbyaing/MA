package cn.blockforge.generated.generatedmod.items;

import cn.blockforge.generated.generatedmod.HeartDemonEntity;
import cn.blockforge.generated.generatedmod.registry.ModEntities;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeSpawnEggItem;

/** Heart demon regression egg: bind the mirror combat state; the other nine eggs remain visual tests. */
public final class HeartDemonSpawnEggItem extends ForgeSpawnEggItem {
    public HeartDemonSpawnEggItem(Properties properties) {
        super(() -> ModEntities.HEART_DEMON.get(), 0x24132F, 0xB86CDB, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        InteractionResult result = super.useOn(context);
        if (result.consumesAction() && !context.getLevel().isClientSide
                && context.getPlayer() instanceof net.minecraft.server.level.ServerPlayer owner) {
            bindNearest(context.getLevel(), owner, context.getClickLocation().x(),
                    context.getClickLocation().y(), context.getClickLocation().z());
        }
        return result;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        InteractionResultHolder<ItemStack> result = super.use(level, player, hand);
        if (result.getResult().consumesAction() && !level.isClientSide
                && player instanceof net.minecraft.server.level.ServerPlayer owner) {
            bindNearest(level, owner, player.getX(), player.getY(), player.getZ());
        }
        return result;
    }

    private static void bindNearest(Level level, net.minecraft.server.level.ServerPlayer owner,
                                    double x, double y, double z) {
        HeartDemonEntity nearest = level.getEntitiesOfClass(HeartDemonEntity.class,
                        new net.minecraft.world.phys.AABB(x - 3.0D, y - 3.0D, z - 3.0D,
                                x + 3.0D, y + 3.0D, z + 3.0D),
                        entity -> entity.ownerUuid() == null)
                .stream()
                .min(java.util.Comparator.comparingDouble(entity -> entity.distanceToSqr(x, y, z)))
                .orElse(null);
        if (nearest == null) return;
        nearest.bindToOwner(owner);
        nearest.setCustomNameVisible(true);
    }
}
