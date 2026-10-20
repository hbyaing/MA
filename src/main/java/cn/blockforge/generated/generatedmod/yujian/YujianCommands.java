package cn.blockforge.generated.generatedmod.yujian;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID)
public final class YujianCommands {
    @SubscribeEvent public static void register(RegisterCommandsEvent event){
        event.getDispatcher().register(Commands.literal("wuxing")
                .then(Commands.literal("yujian_debug").requires(s->s.hasPermission(2)).executes(c->{
                    var player=c.getSource().getPlayerOrException();
                    boolean enabled=!player.getPersistentData().getBoolean("YujianDebug");
                    player.getPersistentData().putBoolean("YujianDebug",enabled);
                    c.getSource().sendSuccess(()->Component.literal("Yujian debug: "+enabled),false);return 1;
                }))
                .then(Commands.literal("yujian_unlock_test").requires(s->s.hasPermission(2)).executes(c->{
                    var player=c.getSource().getPlayerOrException();var data=GeneratedMod.data(player);
                    if(data==null)return 0;
                    data.wuxingxiantu$learnArt(YujianController.FLIGHT);data.wuxingxiantu$setArtMastery(YujianController.FLIGHT,100);
                    GeneratedMod.sync(player);
                    c.getSource().sendSuccess(()->Component.translatable("message.wuxingxiantu.yujian_test_unlocked"),false);return 1;
                })));
    }
    private YujianCommands(){}
}
