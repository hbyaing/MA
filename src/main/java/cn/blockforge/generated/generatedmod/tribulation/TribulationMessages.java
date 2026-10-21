package cn.blockforge.generated.generatedmod.tribulation;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/** Shared styled announcements for formal trials and the removable preview commands. */
public final class TribulationMessages {
    private static final String[] NAMES = {"练气", "筑基", "金丹", "元婴", "化神", "炼虚", "合体", "大乘"};
    private static final String[] OMENS = {
        "初雷裂云，淬炼凡躯。", "青霆交织，考验道基。", "金雷聚势，叩问丹心。", "紫电围落，元婴受炼。",
        "十方雷动，神魂承压。", "虚空震鸣，群雷渐密。", "万钧雷潮，四面合围。", "登仙劫至，天威倾落。"
    };
    private static final ChatFormatting[] COLORS = {
        ChatFormatting.AQUA, ChatFormatting.GREEN, ChatFormatting.GOLD, ChatFormatting.LIGHT_PURPLE,
        ChatFormatting.BLUE, ChatFormatting.DARK_PURPLE, ChatFormatting.RED, ChatFormatting.GOLD
    };

    public static void announce(ServerPlayer player, int realm) {
        int r = TribulationPlan.realm(realm);
        player.displayClientMessage(Component.literal("【天劫将至】 ").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD)
            .append(Component.literal(NAMES[r] + "雷劫").withStyle(COLORS[r], ChatFormatting.BOLD))
            .append(Component.literal(" · " + TribulationPlan.total(r) + " 道劫雷").withStyle(ChatFormatting.YELLOW)), false);
        player.displayClientMessage(Component.literal(OMENS[r]).withStyle(COLORS[r])
            .append(Component.literal(" 雷云正聚，十五息后落雷。").withStyle(ChatFormatting.GRAY)), false);
        player.displayClientMessage(Component.literal("群雷将逐波加压，末道为 ").withStyle(ChatFormatting.GRAY)
            .append(Component.literal("集束天雷").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
            .append(Component.literal("。守住此身，迎劫而立。").withStyle(ChatFormatting.WHITE)), false);
    }

    public static Component countdown(int realm, int seconds) {
        ChatFormatting urgency = seconds > 10 ? ChatFormatting.AQUA : seconds > 5 ? ChatFormatting.YELLOW : ChatFormatting.RED;
        return Component.literal("【" + NAMES[TribulationPlan.realm(realm)] + "雷劫】 ").withStyle(COLORS[TribulationPlan.realm(realm)])
            .append(Component.literal(seconds > 5 ? "雷云凝聚 · " : "天雷将落 · ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(seconds + " 秒").withStyle(urgency, ChatFormatting.BOLD));
    }

    private TribulationMessages() {}
}

