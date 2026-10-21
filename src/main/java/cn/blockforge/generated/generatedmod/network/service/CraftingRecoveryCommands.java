package cn.blockforge.generated.generatedmod.network.service;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Self-service status/retry only; never force-load chunks or cancel another player's materials. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID)
public final class CraftingRecoveryCommands {
    @SubscribeEvent public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("wuxing").then(Commands.literal("recover")
                .then(Commands.literal("crafting").executes(context->{
                    var player=context.getSource().getPlayerOrException();var jobs=CraftingJobs.get(player.server);
                    long now=player.server.overworld().getGameTime();String details=jobs.describe(player.getUUID(),now);
                    jobs.retryOwner(player.getUUID(),now);AlchemyService.deliverRefund(player);
                    context.getSource().sendSuccess(()->Component.literal(details.isEmpty()?"没有待处理的炼制任务。":
                            details+" 已请求重试；未加载区块仍需靠近后结算。"),false);
                    return 1;
                }))));
    }
    private CraftingRecoveryCommands(){}
}
