package cn.blockforge.generated.generatedmod.plants;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.server.level.ServerPlayer;

/** Server fallback for catalogue copies inserted by commands or other mods. */
@Mod.EventBusSubscriber(modid="wuxingxiantu")
public final class HerbInventoryEvents {
    @SubscribeEvent public static void tick(TickEvent.PlayerTickEvent event){
        if(event.phase!=TickEvent.Phase.START||!(event.player instanceof ServerPlayer player))return;
        boolean changed=false;
        for(int i=0;i<player.getInventory().getContainerSize();i++)
            changed|=HerbAge.initializeTemplate(player.getInventory().getItem(i),player.getRandom());
        changed|=HerbAge.initializeTemplate(player.containerMenu.getCarried(),player.getRandom());
        if(changed){player.getInventory().setChanged();player.containerMenu.broadcastChanges();}
    }
    private HerbInventoryEvents(){}
}
