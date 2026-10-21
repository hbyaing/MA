package cn.blockforge.generated.generatedmod.event;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** A held use button cannot turn the end of eating/drinking into a meditation click. */
@Mod.EventBusSubscriber(modid = GeneratedMod.MOD_ID)
public final class MeditationEvents {
    private static final String LAST_USE = "wuxingxiantu:last_item_use";
    private static final int RELEASE_GAP = 10;
    private MeditationEvents() {}
    @SubscribeEvent public static void finish(LivingEntityUseItemEvent.Finish event) {
        if (event.getEntity() instanceof ServerPlayer player)
            player.getPersistentData().putLong(LAST_USE, player.server.getTickCount());
    }
    public static boolean blocksMeditation(ServerPlayer player) {
        var tag = player.getPersistentData();
        long now = player.server.getTickCount();
        long gap = now - tag.getLong(LAST_USE);
        boolean blocked = player.isUsingItem() || tag.contains(LAST_USE) && gap >= 0 && gap <= RELEASE_GAP;
        if (blocked) tag.putLong(LAST_USE, now); // Repeated held-use packets extend the guard.
        else tag.remove(LAST_USE);
        return blocked;
    }
}
