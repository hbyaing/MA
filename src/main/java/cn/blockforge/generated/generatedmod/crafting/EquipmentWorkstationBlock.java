package cn.blockforge.generated.generatedmod.crafting;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.phys.BlockHitResult;

/** Personal workbench container: closing returns its input, never leaves an unowned stack. */
public final class EquipmentWorkstationBlock extends Block {
    private final boolean inscription;
    public EquipmentWorkstationBlock(boolean inscription){super(BlockBehaviour.Properties.of().strength(4,12).sound(SoundType.METAL).requiresCorrectToolForDrops());this.inscription=inscription;}
    @Override public InteractionResult use(BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit){
        if(!level.isClientSide&&player instanceof ServerPlayer server)net.minecraftforge.network.NetworkHooks.openScreen(server,new SimpleMenuProvider((id,inv,p)->new EquipmentWorkstationMenu(id,inv,pos,inscription),Component.literal(inscription?"铭刻台":"精铸台")),buf->{buf.writeBlockPos(pos);buf.writeBoolean(inscription);});
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
