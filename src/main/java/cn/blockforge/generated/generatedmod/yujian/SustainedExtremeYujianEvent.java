package cn.blockforge.generated.generatedmod.yujian;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;

/** Notification only: the core mod never teleports or creates a dimension in response. */
public final class SustainedExtremeYujianEvent extends Event {
    public final ServerPlayer player;
    public final ItemStack sword;
    public final double speed;
    public final int ticks;
    public SustainedExtremeYujianEvent(ServerPlayer player, ItemStack sword, double speed, int ticks) {
        this.player=player;this.sword=sword.copy();this.speed=speed;this.ticks=ticks;
    }
}
