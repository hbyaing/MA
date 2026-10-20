package cn.blockforge.generated.generatedmod;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import cn.blockforge.generated.generatedmod.items.BasePillItem;
import cn.blockforge.generated.generatedmod.items.CultivationLogic;
import cn.blockforge.generated.generatedmod.items.PhysiqueLogic;
import cn.blockforge.generated.generatedmod.items.SpiritSwordItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/** 五行仙途管理员与玩家指令。所有数据修改都在服务端完成，并立即同步客户端。 */
@Mod.EventBusSubscriber(modid = GeneratedMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ModCommands {
    private static final String[] REALM_IDS = {
            "lianqi", "zhuji", "jindan", "yuanying", "huashen", "lianxu", "heti", "dacheng", "xian"
    };
    private static final String[] REALM_NAMES = {
            "练气", "筑基", "金丹", "元婴", "化神", "炼虚", "合体", "大乘", "地仙"
    };
    private static final String[] ROOT_IDS = {"gold", "wood", "water", "fire", "earth"};
    private static final String[] ROOT_NAMES = {"金灵根", "木灵根", "水灵根", "火灵根", "土灵根"};
    private static final String[] HEART_METHODS = {
            "yuan_hun", "gathering", "tiangang", "xuanshui", "nine_turns",
            "burning_heaven", "thick_earth", "taixu", "wanling", "beast_taming"
    };
    private static final String[] HEART_NAMES = {
            "混元心法", "聚灵心法", "天罡心法", "玄水心法", "九转心法",
            "焚天心法", "厚土心法", "太虚心法", "万灵心法", "驭兽心法"
    };
    private static final String[] PHYSIQUE_NAMES = {
            "太初凡体", "青莲灵体", "玄黄圣体", "九窍灵脉",
            "战王圣体", "太阴寒体", "太阳炎体", "万灵道体"
    };
    private static final String[] PHYSIQUE_IDS = Arrays.stream(PhysiqueType.values())
            .map(PhysiqueType::id).toArray(String[]::new);
    private static final String[] ART_IDS = {
            "art_sword_aura", "art_renewal", "art_spirit_shield", "art_immobilize", "art_light_body",
            "art_golden_bell", "art_ice_spike", "art_spirit_gathering", "art_armor_break", "art_flame_wave",
            "art_flash_step", "art_purification", "art_bloodlust", "art_thunder", "art_myriad_swords",
            "art_immovable_mountain", "art_human_harmony", "art_five_elements",
            "art_sword_flight", "art_sword_pierce", "art_sword_sweep", "art_sword_guard", "art_sword_array"
    };
    private static final String[] ART_NAMES = {
            "剑气斩", "回春术", "灵力护盾", "定身咒", "轻身术", "金钟罩", "冰锥术", "聚灵术",
            "破甲斩", "烈焰波", "闪现", "净化术", "嗜血术", "雷霆万钧", "万剑诀", "不动如山",
            "天人合一", "五行灭世", "御剑凌霄诀", "飞星贯日诀", "回风环刃诀", "六御金罡诀", "太虚落剑诀"
    };
    private static final String[] PILL_IDS = java.util.Arrays.stream(
            cn.blockforge.generated.generatedmod.alchemy.PillCatalog.values()).map(pill -> pill.id).toArray(String[]::new);
    private static final String[] SWORD_IDS = {
            "peach_sword", "dark_iron_sword", "mingjue_sword", "qingming_sword",
            "jixiao_sword", "wuxiang_sword", "guiyuan_sword", "tianque_sword"
    };
    private static final String[] SWORD_NAMES = {
            "桃木剑", "玄铁剑", "明玦剑", "青冥剑", "霁霄剑", "无相剑", "归元剑", "天阙剑"
    };

    private static final SuggestionProvider<CommandSourceStack> ONLINE_PLAYERS = (context, builder) -> {
        for (ServerPlayer player : context.getSource().getServer().getPlayerList().getPlayers()) {
            builder.suggest(player.getGameProfile().getName());
        }
        return builder.buildFuture();
    };
    private static final SuggestionProvider<CommandSourceStack> REALMS = suggestions(REALM_IDS, REALM_NAMES);
    private static final SuggestionProvider<CommandSourceStack> ROOTS = suggestions(ROOT_IDS, ROOT_NAMES);
    private static final SuggestionProvider<CommandSourceStack> PHYSIQUES = suggestions(PHYSIQUE_IDS, PHYSIQUE_NAMES);
    private static final SuggestionProvider<CommandSourceStack> HEARTS = suggestions(HEART_METHODS, HEART_NAMES);
    private static final SuggestionProvider<CommandSourceStack> ARTS = suggestions(ART_IDS, ART_NAMES);
    private static final SuggestionProvider<CommandSourceStack> PILLS = suggestions(PILL_IDS, new String[0]);
    private static final SuggestionProvider<CommandSourceStack> SWORDS = suggestions(SWORD_IDS, SWORD_NAMES);

    private ModCommands() {}

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("locate").requires(ModCommands::isOp)
                .then(Commands.literal("structure")
                        .then(Commands.literal("wuxingxiantu:spirit_shrine_island").executes(ModCommands::locateSpiritShrine))
                        .then(Commands.literal("spirit_shrine_island").executes(ModCommands::locateSpiritShrine))));
        dispatcher.register(Commands.literal("wuxing")
                .then(Commands.literal("info").executes(ModCommands::info))
                .then(Commands.literal("help").executes(ModCommands::help))
                .then(Commands.literal("locate").requires(ModCommands::isOp)
                        .executes(ModCommands::locateSpiritShrine))
                .then(Commands.literal("setMana").requires(ModCommands::isOp)
                        .then(Commands.argument("player", StringArgumentType.word()).suggests(ONLINE_PLAYERS)
                                .then(Commands.argument("value", FloatArgumentType.floatArg(0.0F))
                                        .executes(ModCommands::setMana))))
                .then(Commands.literal("setCultivation").requires(ModCommands::isOp)
                        .then(Commands.argument("player", StringArgumentType.word()).suggests(ONLINE_PLAYERS)
                                .then(Commands.argument("value", com.mojang.brigadier.arguments.LongArgumentType.longArg(0L))
                                        .executes(ModCommands::setCultivation))))
                .then(Commands.literal("setRealm").requires(ModCommands::isOp)
                        .then(Commands.argument("player", StringArgumentType.word()).suggests(ONLINE_PLAYERS)
                                .then(Commands.argument("realm", StringArgumentType.word()).suggests(REALMS)
                                        .executes(ModCommands::setRealm))))
                .then(Commands.literal("addRoot").requires(ModCommands::isOp)
                        .then(Commands.argument("player", StringArgumentType.word()).suggests(ONLINE_PLAYERS)
                                .then(Commands.argument("root", StringArgumentType.word()).suggests(ROOTS)
                                        .executes(ModCommands::addRoot))))
                .then(Commands.literal("removeRoot").requires(ModCommands::isOp)
                        .then(Commands.argument("player", StringArgumentType.word()).suggests(ONLINE_PLAYERS)
                                .then(Commands.argument("root", StringArgumentType.word()).suggests(ROOTS)
                                        .executes(ModCommands::removeRoot))))
                .then(Commands.literal("setPhysique").requires(ModCommands::isOp)
                        .then(Commands.argument("player", StringArgumentType.word()).suggests(ONLINE_PLAYERS)
                                .then(Commands.argument("physique", StringArgumentType.word()).suggests(PHYSIQUES)
                                        .executes(ModCommands::setPhysique))))
                .then(Commands.literal("setHeartMethod").requires(ModCommands::isOp)
                        .then(Commands.argument("player", StringArgumentType.word()).suggests(ONLINE_PLAYERS)
                                .then(Commands.argument("heartMethod", StringArgumentType.word()).suggests(HEARTS)
                                        .executes(ModCommands::setHeartMethod))))
                .then(Commands.literal("addArt").requires(ModCommands::isOp)
                        .then(Commands.argument("player", StringArgumentType.word()).suggests(ONLINE_PLAYERS)
                                .then(Commands.argument("art", StringArgumentType.word()).suggests(ARTS)
                                        .executes(ModCommands::addArt))))
                .then(Commands.literal("unlockArtSlots").requires(ModCommands::isOp)
                        .then(Commands.argument("player", StringArgumentType.word()).suggests(ONLINE_PLAYERS)
                                .executes(ModCommands::unlockArtSlots)))
                .then(Commands.literal("reset").requires(ModCommands::isOp)
                        .then(Commands.argument("player", StringArgumentType.word()).suggests(ONLINE_PLAYERS)
                                .executes(ModCommands::reset)))
                .then(Commands.literal("strike").requires(ModCommands::isOp)
                        .then(Commands.argument("player", StringArgumentType.word()).suggests(ONLINE_PLAYERS)
                                .executes(ModCommands::strike)))
                .then(Commands.literal("givePill").requires(ModCommands::isOp)
                        .then(Commands.argument("player", StringArgumentType.word()).suggests(ONLINE_PLAYERS)
                                .then(Commands.argument("pill", StringArgumentType.word()).suggests(PILLS)
                                        .then(Commands.argument("count", IntegerArgumentType.integer(1, 64))
                                                .executes(ModCommands::givePill)))))
                .then(Commands.literal("giveSword").requires(ModCommands::isOp)
                        .then(Commands.argument("player", StringArgumentType.word()).suggests(ONLINE_PLAYERS)
                                .then(Commands.argument("tier", StringArgumentType.word()).suggests(SWORDS)
                                        .executes(ModCommands::giveSword))))
                .then(Commands.literal("setSwordSpirit").requires(ModCommands::isOp)
                        .then(Commands.argument("player", StringArgumentType.word()).suggests(ONLINE_PLAYERS)
                                .then(Commands.argument("value", IntegerArgumentType.integer(0))
                                                .executes(ModCommands::setSwordSpirit)))));
    }

    private static boolean isOp(CommandSourceStack source) {
        return source.hasPermission(2);
    }

    private static SuggestionProvider<CommandSourceStack> suggestions(String[] ids, String[] ignoredNames) {
        return (context, builder) -> {
            for (String id : ids) builder.suggest(id);
            return builder.buildFuture();
        };
    }

    private static String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private static ServerPlayer target(CommandContext<CommandSourceStack> context) {
        String requested = StringArgumentType.getString(context, "player");
        for (ServerPlayer player : context.getSource().getServer().getPlayerList().getPlayers()) {
            if (player.getGameProfile().getName().equalsIgnoreCase(requested)) return player;
        }
        return null;
    }

    private static ServerPlayer requireTarget(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = target(context);
        if (player == null) {
            String requested = StringArgumentType.getString(context, "player");
            context.getSource().sendFailure(Component.literal("玩家" + requested + "不存在"));
        }
        return player;
    }

    private static CultivationData data(ServerPlayer player) {
        return GeneratedMod.data(player);
    }

    private static int info(CommandContext<CommandSourceStack> context) {
        ServerPlayer player;
        try {
            player = context.getSource().getPlayerOrException();
        } catch (Exception exception) {
            context.getSource().sendFailure(Component.literal("该指令只能由玩家执行"));
            return 0;
        }
        CultivationData data = data(player);
        if (data == null) {
            context.getSource().sendFailure(Component.literal("修炼数据不可用"));
            return 0;
        }
        CommandSourceStack source = context.getSource();
        source.sendSuccess(() -> Component.translatable("message.wuxingxiantu.cultivation_info_header")
                .withStyle(net.minecraft.ChatFormatting.GOLD), false);
        source.sendSuccess(() -> Component.literal("§e玩家：§f" + player.getGameProfile().getName()), false);
        source.sendSuccess(() -> Component.literal("§e境界：§f" + CultivationLogic.realmStageName(player, data)
                + " §7(" + data.wuxingxiantu$getRealm() + ")"), false);
        source.sendSuccess(() -> Component.literal("§e修为：§f" + data.wuxingxiantu$getCultivation() + "/"
                + CultivationLogic.breakthroughRequirement(data)), false);
        source.sendSuccess(() -> Component.literal("§e灵力：§f" + format(data.wuxingxiantu$getAura()) + "/"
                + format(CultivationLogic.maxAura(data))), false);
        source.sendSuccess(() -> Component.literal("§e功德值：§f" + formatMerit(data.wuxingxiantu$getMerit())), false);
        source.sendSuccess(() -> Component.literal("§e攻击力：§f" + format((float) player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE))), false);
        source.sendSuccess(() -> Component.literal("§e灵根：§f" + rootSummary(data)), false);
        source.sendSuccess(() -> Component.literal("§e体质：§f" + data.wuxingxiantu$getPhysique().displayName()), false);
        source.sendSuccess(() -> Component.literal("§e心法：§f" + data.wuxingxiantu$getEquippedHeartMethod()), false);
        source.sendSuccess(() -> Component.literal("§e道心：§f" + data.wuxingxiantu$getDaoXin()), false);
        source.sendSuccess(() -> Component.literal("§e功法：§f" + artSummary(data)), false);
        source.sendSuccess(() -> Component.literal("§e打坐：§f" + (data.wuxingxiantu$isMeditating() ? "是" : "否")), false);
        ItemStack sword = heldOrInventorySword(player);
        if (!sword.isEmpty()) {
            source.sendSuccess(() -> Component.literal("§e本命灵器：§f" + sword.getHoverName().getString()
                    + " §7剑灵力 " + SpiritSwordItem.getSpirit(sword) + "/" + SpiritSwordItem.getMaxSpirit(sword)), false);
        }
        return 1;
    }

    private static int help(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        source.sendSuccess(() -> Component.literal("§6===== /wuxing 指令帮助 ====="), false);
        String[] lines = {
                "/wuxing info - 查看自身修炼信息",
                "/wuxing help - 查看帮助",
                "/wuxing setMana <玩家> <数值> - 设置灵力值（OP）",
                "/wuxing setCultivation <玩家> <数值> - 设置修为值（OP）",
                "/wuxing setRealm <玩家> <境界> - 设置境界（OP）",
                "/wuxing addRoot <玩家> <灵根> - 添加灵根（OP）",
                "/wuxing removeRoot <玩家> <灵根> - 移除灵根（OP）",
                "/wuxing setPhysique <玩家> <体质> - 设置体质（OP）",
                "/wuxing setHeartMethod <玩家> <心法> - 设置心法（OP）",
                "/wuxing addArt <玩家> <功法> - 添加功法（OP）",
                "/wuxing unlockArtSlots <玩家> - 解锁全部功法槽（OP）",
                "/wuxing reset <玩家> - 重置玩家数据（OP）",
                "/wuxing strike <玩家> - 降下天雷（OP）",
                "/wuxing givePill <玩家> <丹药> <数量> - 给予丹药（OP）",
                "/wuxing giveSword <玩家> <品阶> - 给予本命灵器（OP）",
                "/wuxing setSwordSpirit <玩家> <数值> - 设置剑灵力值（OP）",
                "/wuxing locate - 定位最近的灵修者结构（OP）",
                "/locate structure wuxingxiantu:spirit_shrine_island - 定位最近的灵修者结构（OP）"
        };
        for (String line : lines) source.sendSuccess(() -> Component.literal(line), false);
        return 1;
    }

    private static int locateSpiritShrine(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerLevel level = source.getLevel();
        if (!level.dimension().equals(net.minecraft.world.level.Level.OVERWORLD)) {
            return fail(context, "灵修者结构只生成在主世界");
        }
        BlockPos from = BlockPos.containing(source.getPosition());
        SpiritShrineManager.LocateResult result = SpiritShrineManager.locate(level,from);
        if (result == null) {
            return fail(context, "搜索范围内未找到浮空仙岛或符合高度要求的候选位置");
        }
        BlockPos nearest = result.position();
        double distance = Math.sqrt(nearest.distSqr(from));
        source.sendSuccess(() -> Component.literal((result.generated() ? "已生成的浮空仙岛位于 " : "浮空仙岛候选入口位于 ")
                + nearest.getX() + ", " + nearest.getY() + ", " + nearest.getZ()
                + "（距离约 " + Math.round(distance) + " 格）"
                + (result.generated() ? "" : "；到达后需加载周边区块，最终生成仍受地形和已有建筑限制"))
                .withStyle(style -> style.withClickEvent(new net.minecraft.network.chat.ClickEvent(
                        net.minecraft.network.chat.ClickEvent.Action.SUGGEST_COMMAND,
                        "/tp @s " + nearest.getX() + " " + nearest.getY() + " " + nearest.getZ()))), false);
        return 1;
    }

    private static int setMana(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = requireTarget(context);
        if (player == null) return 0;
        CultivationData data = data(player);
        if (data == null) return fail(context, "修炼数据");
        float value = FloatArgumentType.getFloat(context, "value");
        data.wuxingxiantu$setCurrentMana(value);
        GeneratedMod.sync(player);
        return success(context, "已将" + player.getGameProfile().getName() + "的灵力设置为" + format(data.wuxingxiantu$getAura()));
    }

    private static int setCultivation(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = requireTarget(context);
        if (player == null) return 0;
        CultivationData data = data(player);
        if (data == null) return fail(context, "修炼数据");
        long value = com.mojang.brigadier.arguments.LongArgumentType.getLong(context, "value");
        data.wuxingxiantu$setCultivation(value);
        GeneratedMod.sync(player);
        return success(context, "已将" + player.getGameProfile().getName() + "的修为设置为" + data.wuxingxiantu$getCultivation());
    }

    private static int setRealm(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = requireTarget(context);
        if (player == null) return 0;
        Integer realm = realmValue(StringArgumentType.getString(context, "realm"));
        if (realm == null) return invalid(context, "境界", StringArgumentType.getString(context, "realm"));
        CultivationData data = data(player);
        if (data == null) return fail(context, "修炼数据");
        data.wuxingxiantu$setRealm(realm);
        data.wuxingxiantu$setCurrentMana(CultivationLogic.maxAura(data));
        data.wuxingxiantu$setCultivationStage(0);
        data.wuxingxiantu$setCultivation(0L);
        data.wuxingxiantu$setPhysiqueLocked(false);
        PhysiqueLogic.apply(player, data);
        CultivationLogic.applyHealthBonus(player, data);
        GeneratedMod.sync(player);
        return success(context, "已将" + player.getGameProfile().getName() + "的境界设置为" + CultivationLogic.realmName(player, realm));
    }

    private static int addRoot(CommandContext<CommandSourceStack> context) {
        return changeRoot(context, true);
    }

    private static int removeRoot(CommandContext<CommandSourceStack> context) {
        return changeRoot(context, false);
    }

    private static int changeRoot(CommandContext<CommandSourceStack> context, boolean add) {
        ServerPlayer player = requireTarget(context);
        if (player == null) return 0;
        String requested = StringArgumentType.getString(context, "root");
        Integer root = rootValue(requested);
        if (root == null) return invalid(context, "灵根", requested);
        CultivationData data = data(player);
        if (data == null) return fail(context, "修炼数据");
        int mask = data.wuxingxiantu$getRootMask();
        boolean exists = (mask & (1 << root)) != 0;
        if (add && exists) return success(context, player.getGameProfile().getName() + "已经拥有" + ROOT_NAMES[root]);
        if (!add && !exists) return success(context, player.getGameProfile().getName() + "没有" + ROOT_NAMES[root]);
        data.wuxingxiantu$setRootMask(add ? mask | (1 << root) : mask & ~(1 << root));
        if (add) data.wuxingxiantu$setRootQuality(root, Math.max(1, data.wuxingxiantu$getRootQuality(root)));
        else data.wuxingxiantu$setRootQuality(root, 0);
        CultivationLogic.refreshChaosRoot(player, data);
        GeneratedMod.sync(player);
        return success(context, (add ? "已为" : "已从") + player.getGameProfile().getName() + (add ? "添加" : "移除") + ROOT_NAMES[root]);
    }

    private static int setPhysique(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = requireTarget(context);
        if (player == null) return 0;
        String requested = StringArgumentType.getString(context, "physique");
        PhysiqueType physique = physiqueValue(requested);
        if (physique == null) return invalid(context, "体质", requested);
        CultivationData data = data(player);
        if (data == null) return fail(context, "修炼数据");
        data.wuxingxiantu$setPhysique(physique);
        PhysiqueLogic.apply(player, data);
        GeneratedMod.sync(player);
        return success(context, "已将" + player.getGameProfile().getName() + "的体质设置为" + physique.displayName());
    }

    private static int setHeartMethod(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = requireTarget(context);
        if (player == null) return 0;
        String requested = StringArgumentType.getString(context, "heartMethod");
        String method = heartMethodValue(requested);
        if (method == null) return invalid(context, "心法", requested);
        CultivationData data = data(player);
        if (data == null) return fail(context, "修炼数据");
        data.wuxingxiantu$setEquippedHeartMethod(method);
        GeneratedMod.sync(player);
        return success(context, "已将" + player.getGameProfile().getName() + "的心法设置为" + HEART_NAMES[indexOf(HEART_METHODS, method)]);
    }

    private static int addArt(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = requireTarget(context);
        if (player == null) return 0;
        String requested = StringArgumentType.getString(context, "art");
        String art = artValue(requested);
        if (art == null) return invalid(context, "功法", requested);
        CultivationData data = data(player);
        if (data == null) return fail(context, "修炼数据");
        if (!data.wuxingxiantu$learnArt(art)) return success(context, player.getGameProfile().getName() + "已经学会该功法");
        String[] equipped = data.wuxingxiantu$getEquippedArts();
        for (int slot = 0; slot < 8; slot++) {
            if (equipped[slot] == null) {
                data.wuxingxiantu$equipArt(slot, art);
                break;
            }
        }
        GeneratedMod.sync(player);
        return success(context, "已为" + player.getGameProfile().getName() + "添加功法" + artName(art));
    }

    private static int unlockArtSlots(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = requireTarget(context);
        if (player == null) return 0;
        CultivationData data = data(player);
        if (data == null) return fail(context, "修炼数据");
        String[] learned = data.wuxingxiantu$getLearnedArts();
        int slot = 0;
        for (String art : learned) {
            if (art != null && slot < 8) data.wuxingxiantu$equipArt(slot++, art);
        }
        GeneratedMod.sync(player);
        return success(context, "已为" + player.getGameProfile().getName() + "解锁并填充全部功法槽");
    }

    private static int reset(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = requireTarget(context);
        if (player == null) return 0;
        CultivationData data = data(player);
        if (data == null) return fail(context, "修炼数据");
        if (data.wuxingxiantu$isMeditating()) CultivationBlock.stopMeditation(player, data, false);
        data.deserializeNBT(new net.minecraft.nbt.CompoundTag());
        PlayerInitialization.initializeIfNeeded(data, player.getRandom());
        PhysiqueLogic.apply(player, data);
        CultivationLogic.applyHealthBonus(player, data);
        player.setHealth(player.getMaxHealth());
        GeneratedMod.sync(player);
        return success(context, "已重置玩家" + player.getGameProfile().getName() + "的修炼数据");
    }

    private static int strike(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = requireTarget(context);
        if (player == null) return 0;
        ServerLevel level = player.serverLevel();
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
        if (bolt == null) return fail(context, "天雷");
        bolt.getPersistentData().putBoolean("wuxingxiantu_artificial_lightning", true);
        bolt.moveTo(player.getX(), player.getY(), player.getZ());
        level.addFreshEntity(bolt);
        return success(context, "天雷已降下于" + player.getGameProfile().getName());
    }

    private static int givePill(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = requireTarget(context);
        if (player == null) return 0;
        String requested = StringArgumentType.getString(context, "pill");
        int count = IntegerArgumentType.getInteger(context, "count");
        var serialized = new net.minecraft.nbt.CompoundTag();
        serialized.putString("id", requested.contains(":") ? requested : GeneratedMod.MOD_ID + ":" + requested);
        serialized.putByte("Count", (byte) count);
        ItemStack stack = ItemStack.of(cn.blockforge.generated.generatedmod.alchemy.LegacyStackMigration.migrate(serialized));
        if (!(stack.getItem() instanceof BasePillItem)) return invalid(context, "丹药", requested);
        if (cn.blockforge.generated.generatedmod.alchemy.PillData.read(stack) != null)
            stack = cn.blockforge.generated.generatedmod.alchemy.PillData.normalize(stack);
        player.getInventory().placeItemBackInInventory(stack);
        return success(context, "已给予" + player.getGameProfile().getName() + " " + count + "个" + stack.getHoverName().getString());
    }

    private static int giveSword(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = requireTarget(context);
        if (player == null) return 0;
        String requested = StringArgumentType.getString(context, "tier");
        String id = swordValue(requested);
        if (id == null) return invalid(context, "品阶", requested);
        Item item = modItem(id);
        if (!(item instanceof SpiritSwordItem sword)) return invalid(context, "品阶", requested);
        ItemStack stack = new ItemStack(sword);
        sword.initialize(stack);
        player.getInventory().placeItemBackInInventory(stack);
        return success(context, "已给予" + player.getGameProfile().getName() + " " + stack.getHoverName().getString());
    }

    private static int setSwordSpirit(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = requireTarget(context);
        if (player == null) return 0;
        ItemStack sword = heldOrInventorySword(player);
        if (sword.isEmpty()) return fail(context, "本命灵器");
        int value = IntegerArgumentType.getInteger(context, "value");
        SpiritSwordItem.setSpirit(sword, value);
        return success(context, "已将" + player.getGameProfile().getName() + "的剑灵力设置为"
                + SpiritSwordItem.getSpirit(sword) + "/" + SpiritSwordItem.getMaxSpirit(sword));
    }

    private static Item modItem(String requested) {
        String id = normalize(requested);
        if (id.startsWith("wuxingxiantu:")) id = id.substring("wuxingxiantu:".length());
        return ForgeRegistries.ITEMS.getValue(new ResourceLocation(GeneratedMod.MOD_ID, id));
    }

    private static ItemStack heldOrInventorySword(ServerPlayer player) {
        if (player.getMainHandItem().getItem() instanceof SpiritSwordItem) return player.getMainHandItem();
        if (player.getOffhandItem().getItem() instanceof SpiritSwordItem) return player.getOffhandItem();
        for (ItemStack stack : player.getInventory().items) if (stack.getItem() instanceof SpiritSwordItem) return stack;
        return ItemStack.EMPTY;
    }

    private static String rootSummary(CultivationData data) {
        List<String> roots = new ArrayList<>();
        for (int i = 0; i < ROOT_IDS.length; i++) {
            if ((data.wuxingxiantu$getRootMask() & (1 << i)) != 0) {
                // 变异上位灵根显化时按显化名展示；未显化但在淬体途中则附带进度。
                if (i == 1 && CultivationLogic.isThunderForm(data)) {
                    roots.add("天雷灵根·" + qualityName(data.wuxingxiantu$getRootQuality(1)));
                } else if (i == 3 && CultivationLogic.isExoticForm(data)) {
                    roots.add("异火灵根·" + qualityName(data.wuxingxiantu$getRootQuality(3)));
                } else if (i == 1 && !data.wuxingxiantu$hasThunderRoot() && data.wuxingxiantu$getThunderStrikes() > 0) {
                    roots.add(ROOT_NAMES[i] + "·" + qualityName(data.wuxingxiantu$getRootQuality(i))
                            + "§7(天雷淬体 " + data.wuxingxiantu$getThunderStrikes() + "/99)§f");
                } else {
                    roots.add(ROOT_NAMES[i] + "·" + qualityName(data.wuxingxiantu$getRootQuality(i)));
                }
            }
        }
        return roots.isEmpty() ? "无" : String.join("、", roots);
    }

    private static String artSummary(CultivationData data) {
        String[] arts = data.wuxingxiantu$getLearnedArts();
        List<String> names = new ArrayList<>();
        for (String art : arts) if (art != null) names.add(artName(art));
        return names.isEmpty() ? "无" : String.join("、", names);
    }

    private static String artName(String id) {
        int index = indexOf(ART_IDS, id);
        return index >= 0 ? ART_NAMES[index] : id;
    }

    private static String qualityName(int quality) {
        return switch (Math.max(1, Math.min(4, quality))) {
            case 2 -> "中品";
            case 3 -> "上品";
            case 4 -> "极品";
            default -> "下品";
        };
    }

    private static Integer realmValue(String value) {
        String normalized = normalize(value);
        // 旧版使用 hedao；保留解析兼容，但新的提示与补全统一使用 heti（合体）。
        if (normalized.equals("hedao")) return 6;
        for (int i = 0; i < REALM_IDS.length; i++) {
            if (normalized.equals(REALM_IDS[i])) return i;
        }
        return null;
    }

    private static Integer rootValue(String value) {
        String normalized = normalize(value);
        for (int i = 0; i < ROOT_IDS.length; i++) {
            if (normalized.equals(ROOT_IDS[i])) return i;
        }
        return null;
    }

    private static PhysiqueType physiqueValue(String value) {
        String normalized = normalize(value);
        for (int i = 0; i < PHYSIQUE_IDS.length; i++) {
            if (normalized.equals(PHYSIQUE_IDS[i])) return PhysiqueType.values()[i];
        }
        return null;
    }

    private static String heartMethodValue(String value) {
        String normalized = normalize(value);
        for (String method : HEART_METHODS) {
            if (normalized.equals(method)) return method;
        }
        return null;
    }

    private static String artValue(String value) {
        String normalized = normalize(value);
        for (String art : ART_IDS) {
            if (normalized.equals(art)) return art;
        }
        return null;
    }

    private static String swordValue(String value) {
        String normalized = normalize(value);
        for (String sword : SWORD_IDS) {
            if (normalized.equals(sword)) return sword;
        }
        return null;
    }

    private static int indexOf(String[] values, String wanted) {
        for (int i = 0; i < values.length; i++) if (values[i].equals(wanted)) return i;
        return -1;
    }

    private static String formatMerit(long value) {
        return value == Long.MAX_VALUE ? "∞" : Long.toString(value);
    }

    private static String format(float value) {
        return value == Math.round(value) ? Integer.toString(Math.round(value)) : String.format(Locale.ROOT, "%.1f", value);
    }

    private static int success(CommandContext<CommandSourceStack> context, String message) {
        context.getSource().sendSuccess(() -> Component.literal(message), true);
        return 1;
    }

    private static int invalid(CommandContext<CommandSourceStack> context, String type, String value) {
        context.getSource().sendFailure(Component.literal(value + "不存在，请检查拼写"));
        return 0;
    }

    private static int fail(CommandContext<CommandSourceStack> context, String type) {
        context.getSource().sendFailure(Component.literal(type + "不可用"));
        return 0;
    }
}
