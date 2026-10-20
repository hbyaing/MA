package cn.blockforge.generated.generatedmod.boss;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.HeartDemonEntity;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = GeneratedMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class BossTestCommands {
    private static final SimpleCommandExceptionType INVALID_TARGET = new SimpleCommandExceptionType(
            Component.translatable("boss.wuxingxiantu.invalid_target"));
    private static final SimpleCommandExceptionType INVALID_ANIMATION = new SimpleCommandExceptionType(
            Component.translatable("boss.wuxingxiantu.invalid_animation"));

    public static boolean isVisualTest(Entity entity) {
        return entity instanceof BossVisualEntity boss && boss.isVisualTest();
    }

    @SubscribeEvent public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("wuxing").requires(source -> source.hasPermission(2))
                .then(Commands.literal("boss_preview").then(Commands.argument("entity",EntityArgument.entity()).executes(context->{
                    if(!(EntityArgument.getEntity(context,"entity") instanceof VisualBossEntity boss)||!boss.previewOnly())throw INVALID_TARGET.create();
                    return success(context);
                })))
                .then(Commands.literal("boss_fight").then(Commands.argument("entity",EntityArgument.entity())
                        .then(Commands.argument("player",EntityArgument.player()).executes(context->{
                            if(!(EntityArgument.getEntity(context,"entity") instanceof VisualBossEntity boss)
                                    ||!boss.beginCombat(EntityArgument.getPlayer(context,"player")))throw INVALID_TARGET.create();
                            return success(context);
                        }))))
                .then(Commands.literal("boss_skill").then(Commands.argument("entity",EntityArgument.entity())
                        .then(Commands.argument("skill",StringArgumentType.word()).suggests((context,builder)->{
                            var entity=EntityArgument.getEntity(context,"entity");
                            var profile=entity instanceof HeartDemonEntity?cn.blockforge.generated.generatedmod.boss.combat.HeartDemonSkillPrograms.PROFILE
                                    :entity instanceof VisualBossEntity boss?cn.blockforge.generated.generatedmod.boss.combat.BossCombatCatalog.get(boss.visualDefinition().id()):null;
                            if(profile!=null)return SharedSuggestionProvider.suggest(java.util.stream.Stream.concat(
                                    profile.skills().stream().map(cn.blockforge.generated.generatedmod.boss.combat.BossCombatCatalog.Skill::id),
                                    java.util.stream.Stream.of(profile.id()+"_basic")),builder);
                            return builder.buildFuture();
                        }).executes(context->{
                            var entity=EntityArgument.getEntity(context,"entity");
                            var combat=entity instanceof VisualBossEntity boss?boss.combat():entity instanceof HeartDemonEntity heart?heart.combat():null;
                            if(combat==null||!combat.forceSkill(StringArgumentType.getString(context,"skill")))throw INVALID_TARGET.create();
                            return success(context);
                        }))))
                .then(Commands.literal("boss_phase").then(Commands.argument("entity", EntityArgument.entity())
                        .then(Commands.argument("phase", IntegerArgumentType.integer(1, 2)).executes(context -> {
                            if (!(EntityArgument.getEntity(context, "entity") instanceof HeartDemonEntity demon)) throw INVALID_TARGET.create();
                            demon.setVisualPhase(IntegerArgumentType.getInteger(context, "phase"));
                            return success(context);
                        }))))
                .then(Commands.literal("boss_bind").then(Commands.argument("entity", EntityArgument.entity())
                        .then(Commands.argument("player", EntityArgument.player()).executes(context -> {
                            if (!(EntityArgument.getEntity(context, "entity") instanceof HeartDemonEntity demon)) throw INVALID_TARGET.create();
                            demon.bindVisualOwner(EntityArgument.getPlayer(context, "player"));
                            return success(context);
                        }))))
                .then(Commands.literal("boss_health").then(Commands.argument("entity", EntityArgument.entity())
                        .then(Commands.argument("percent", FloatArgumentType.floatArg(0, 100)).executes(context -> {
                            LivingEntity entity = (LivingEntity) target(context);
                            float percent = FloatArgumentType.getFloat(context, "percent");
                            if (percent == 0) entity.kill();
                            else entity.setHealth(entity.getMaxHealth() * percent / 100.0F);
                            return success(context);
                        }))))
                .then(Commands.literal("boss_animations").then(Commands.argument("entity", EntityArgument.entity())
                        .executes(context -> {
                            var boss = (BossVisualEntity) target(context);
                            context.getSource().sendSuccess(() -> Component.literal(String.join(
                                    ", ", boss.visualDefinition().clips().keySet())), false);
                            return boss.visualDefinition().clips().size();
                        })))
                .then(Commands.literal("boss_animation").then(Commands.argument("entity", EntityArgument.entity())
                        .then(Commands.argument("clip", StringArgumentType.word()).suggests((context, builder) -> {
                            if (EntityArgument.getEntity(context, "entity") instanceof BossVisualEntity boss) {
                                builder.suggest("stop");
                                return SharedSuggestionProvider.suggest(boss.visualDefinition().clips().keySet(), builder);
                            }
                            return builder.buildFuture();
                        }).executes(context -> {
                            var boss = (BossVisualEntity) target(context);
                            String clip = StringArgumentType.getString(context, "clip");
                            if(!boss.isVisualTest())throw INVALID_TARGET.create();
                            if (!clip.equals("stop") && !boss.visualDefinition().clips().containsKey(clip)) throw INVALID_ANIMATION.create();
                            boss.previewAnimation(clip.equals("stop") ? "" : clip);
                            return success(context);
                        })))));
    }

    private static Entity target(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Entity entity = EntityArgument.getEntity(context, "entity");
        if (!(entity instanceof BossVisualEntity)) throw INVALID_TARGET.create();
        return entity;
    }

    private static int success(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(() -> Component.translatable("boss.wuxingxiantu.updated"), false);
        return 1;
    }

    private BossTestCommands() {}
}
