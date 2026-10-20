package cn.blockforge.generated.generatedmod.mindscape;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.*;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID)
public final class MindscapeCommands {
    @SubscribeEvent public static void register(RegisterCommandsEvent event){
        event.getDispatcher().register(Commands.literal("wuxing").then(Commands.literal("rechallenge").executes(c->{
            var player=c.getSource().getPlayerOrException();
            boolean accepted=MindscapeProgression.requestRepeat(player);
            player.displayClientMessage(Component.translatable("message.wuxingxiantu.rechallenge_"+(accepted?"ready":"locked")),true);
            return accepted?1:0;
        })));
        event.getDispatcher().register(Commands.literal("wuxing").then(Commands.literal("mindscape").requires(s->s.hasPermission(2))
                .then(Commands.literal("cast_skill").then(Commands.argument("skill",StringArgumentType.word())
                        .suggests((c,b)->SharedSuggestionProvider.suggest(java.util.stream.Stream.concat(
                                cn.blockforge.generated.generatedmod.boss.combat.BossCombatCatalog.all().stream().flatMap(p->p.skills().stream()),
                                cn.blockforge.generated.generatedmod.boss.combat.HeartDemonSkillPrograms.PROFILE.skills().stream())
                                .map(cn.blockforge.generated.generatedmod.boss.combat.BossCombatCatalog.Skill::id),b))
                        .executes(c->{var fight=cn.blockforge.generated.generatedmod.boss.combat.BossCombatEvents.encounter(c.getSource().getPlayerOrException());
                            return fight!=null&&fight.forceSkill(StringArgumentType.getString(c,"skill"))?1:0;})))
                .then(Commands.literal("status").executes(c->{var p=c.getSource().getPlayerOrException();
                    c.getSource().sendSuccess(()->Component.literal("Mindscape: "+MindscapeManager.state(p)+" unstable="
                            +GeneratedMod.data(p).wuxingxiantu$isDaoHeartUnstable()+" "+GeneratedMod.data(p).wuxingxiantu$getMindscapeData()),false);return 1;}))
                .then(Commands.literal("start_test").then(Commands.argument("challenge",StringArgumentType.word())
                        .suggests((c,b)->SharedSuggestionProvider.suggestResource(MindscapeChallenges.ids(),b))
                        .executes(c->MindscapeManager.enter(c.getSource().getPlayerOrException(),MindscapeChallenges.get(StringArgumentType.getString(c,"challenge")),true)?1:0)))
                .then(Commands.literal("complete_test").executes(c->MindscapeManager.complete(c.getSource().getPlayerOrException(),true)?1:0))
                .then(Commands.literal("force_complete")
                        .executes(c -> forceComplete(c.getSource(),c.getSource().getPlayerOrException()))
                        .then(Commands.argument("player",net.minecraft.commands.arguments.EntityArgument.player())
                                .executes(c -> forceComplete(c.getSource(),net.minecraft.commands.arguments.EntityArgument.getPlayer(c,"player")))))
                .then(Commands.literal("force_restore")
                        .executes(c -> restore(c.getSource(), c.getSource().getPlayerOrException()))
                        .then(Commands.argument("player", net.minecraft.commands.arguments.EntityArgument.player())
                                .executes(c -> restore(c.getSource(), net.minecraft.commands.arguments.EntityArgument.getPlayer(c, "player")))))
                .then(Commands.literal("reset").executes(c->{var p=c.getSource().getPlayerOrException();MindscapeManager.abort(p,true);MindscapeManager.markUnstable(p);GeneratedMod.sync(p);return 1;}))));
    }
    private static int restore(CommandSourceStack source, net.minecraft.server.level.ServerPlayer player) {
        if (!MindscapeManager.forceRestore(player)) {
            source.sendFailure(Component.translatable("message.wuxingxiantu.force_restore_failed"));
            return 0;
        }
        source.sendSuccess(() -> Component.translatable("message.wuxingxiantu.force_restore_success", player.getDisplayName()), true);
        return 1;
    }
    private MindscapeCommands(){}
    private static int forceComplete(CommandSourceStack source,net.minecraft.server.level.ServerPlayer player) {
        if(!MindscapeManager.complete(player,true)) {
            source.sendFailure(Component.translatable("message.wuxingxiantu.force_complete_failed"));return 0;
        }
        source.sendSuccess(() -> Component.translatable("message.wuxingxiantu.force_complete_success",player.getDisplayName()),true);
        return 1;
    }
}
