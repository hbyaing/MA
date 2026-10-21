package cn.blockforge.generated.generatedmod.items;

import cn.blockforge.generated.generatedmod.world.SpiritVeinData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

/** Reveals only actual generated veins in this dimension, without loading unexplored chunks. */
public final class SpiritVeinCompassItem extends Item {
    public SpiritVeinCompassItem(Properties properties){super(properties);}
    @Override public InteractionResultHolder<ItemStack> use(Level world,Player player,InteractionHand hand){
        var stack=player.getItemInHand(hand);
        if(world instanceof ServerLevel level){
            if(player.getCooldowns().isOnCooldown(this))return InteractionResultHolder.fail(stack);
            var found=SpiritVeinData.get(level).nearest(player.blockPosition(),384);
            if(found.isEmpty())player.displayClientMessage(Component.translatable("message.wuxingxiantu.vein_not_found"),true);
            else {var vein=found.get();var p=vein.center();player.displayClientMessage(Component.translatable(
                "message.wuxingxiantu.vein_found",vein.grade(),p.getX(),p.getY(),p.getZ(),SpiritVeinData.get(level).auraAt(player.blockPosition())),false);}
            player.getCooldowns().addCooldown(this,40);
        }
        return InteractionResultHolder.sidedSuccess(stack,world.isClientSide);
    }
}
