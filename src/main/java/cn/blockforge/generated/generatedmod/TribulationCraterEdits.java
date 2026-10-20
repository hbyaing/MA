package cn.blockforge.generated.generatedmod;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraft.server.level.ServerLevel;
/** External block edits revoke restoration ownership so a player's work is never overwritten. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID)
public final class TribulationCraterEdits {
 @SubscribeEvent(priority=EventPriority.LOWEST) public static void place(BlockEvent.EntityPlaceEvent e){if(!e.isCanceled())forget(e);}
 @SubscribeEvent(priority=EventPriority.LOWEST) public static void breakBlock(BlockEvent.BreakEvent e){if(!e.isCanceled())forget(e);}
 private static void forget(BlockEvent e){if(e.getLevel() instanceof ServerLevel level){var state=TribulationState.get(level.getServer());if(state.craters.remove(new TribulationState.CraterKey(level.dimension(),e.getPos().immutable()))!=null)state.setDirty();}}
}
