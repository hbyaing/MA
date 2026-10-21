package cn.blockforge.generated.generatedmod.alchemy;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = GeneratedMod.MOD_ID)
public final class AlchemyCommands {
    private AlchemyCommands() {}
    @SubscribeEvent public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("wuxing")
            .then(Commands.literal("alchemy").requires(s -> s.hasPermission(2))
                .then(Commands.argument("player", EntityArgument.player())
                    .then(Commands.literal("unlock").executes(c -> FormulaKnowledge.unlock(EntityArgument.getPlayer(c, "player")) ? 1 : 0))
                    .then(Commands.literal("all").executes(c -> FormulaKnowledge.learnAll(EntityArgument.getPlayer(c, "player"))))
                    .then(Commands.literal("reset").executes(c -> { FormulaKnowledge.reset(EntityArgument.getPlayer(c, "player")); return 1; }))
                    .then(Commands.literal("learn").then(Commands.argument("formula", ResourceLocationArgument.id())
                        .executes(c -> FormulaKnowledge.learn(EntityArgument.getPlayer(c, "player"), ResourceLocationArgument.getId(c, "formula")) ? 1 : 0))))));
    }
}
