package cn.blockforge.generated.generatedmod.tribulation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraft.commands.Commands;
import cn.blockforge.generated.generatedmod.TribulationManager;
/** Removable testing entry point. Delete this class to remove all /tribulationtest commands. */
@Mod.EventBusSubscriber(modid="wuxingxiantu")
public final class TribulationTestCommands {
 @SubscribeEvent public static void register(RegisterCommandsEvent e){
  var root=Commands.literal("tribulationtest").requires(s->s.hasPermission(2));
  String[] names={"练气","筑基","金丹","元婴","化神","炼虚","合体","大乘"};
  for(int i=0;i<8;i++){final int realm=i;for(String name:new String[]{names[i],Integer.toString(i+1)})root.then(Commands.literal(name)
    .executes(c->TribulationManager.startTest(c.getSource().getPlayerOrException(),realm,true)?1:0)
    .then(Commands.literal("真实").executes(c->TribulationManager.startTest(c.getSource().getPlayerOrException(),realm,true)?1:0))
    .then(Commands.literal("预览").executes(c->TribulationManager.startTest(c.getSource().getPlayerOrException(),realm,false)?1:0)));}
  root.then(Commands.literal("stop").executes(c->{TribulationManager.stopTest(c.getSource().getPlayerOrException());return 1;}));
  e.getDispatcher().register(root);
 }
}
