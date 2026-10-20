package cn.blockforge.generated.generatedmod.event;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.acquisition.AcquisitionSourcesEvent;
import cn.blockforge.generated.generatedmod.acquisition.CultivationBookSources;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.Villager;
import net.minecraftforge.event.entity.player.ItemFishedEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = GeneratedMod.MOD_ID)
public final class CultivationBookEvents {
    private CultivationBookEvents() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void beforeTrade(PlayerInteractEvent.EntityInteract event) {
        if (event.getEntity() instanceof ServerPlayer && event.getTarget() instanceof Villager villager) {
            // Runs before vanilla opens the trading screen; rechecking handles upgrades and old saves.
            CultivationBookSources.ensureMerchantBooks(villager);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void fishBook(ItemFishedEvent event) {
        if (!(event.getHookEntity().getPlayerOwner() instanceof ServerPlayer player)
                || event.isCanceled() || event.getDrops().isEmpty()
                || !event.getHookEntity().isOpenWaterFishing()) return;
        var reward = CultivationBookSources.fishingReward(player.getRandom());
        if (!reward.isEmpty()) event.getDrops().add(reward);
    }

    @SubscribeEvent
    public static void sources(AcquisitionSourcesEvent event) {
        event.register("cultivation-books", CultivationBookSources::contribute);
    }
}
