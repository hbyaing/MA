package cn.blockforge.generated.generatedmod.yujian;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

/** Optional region-protection hook. Cancellation always overrides the passable tag. */
@Cancelable
public final class YujianPassBlockEvent extends Event {
    public final ServerPlayer player;
    public final BlockPos pos;
    public final BlockState state;
    public YujianPassBlockEvent(ServerPlayer player, BlockPos pos, BlockState state) {
        this.player=player;this.pos=pos.immutable();this.state=state;
    }
}
