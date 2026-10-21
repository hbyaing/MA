package cn.blockforge.generated.generatedmod;

import net.minecraft.network.chat.Component;

import java.util.List;

/** 集中维护心法 ID 与显示名称，供界面和服务端校验共用。 */
public final class HeartMethodCatalog {
    public static final List<String> METHOD_IDS = List.of(
            "yuan_hun", "gathering", "tiangang", "xuanshui", "nine_turns",
            "burning_heaven", "thick_earth", "taixu", "wanling", "beast_taming");

    private HeartMethodCatalog() {}

    public static boolean isValid(String methodId) {
        return methodId != null && METHOD_IDS.contains(methodId);
    }

    public static Component displayName(String methodId) {
        return Component.translatable("item.wuxingxiantu.heart_method_" + methodId);
    }

    public static Component shortName(String methodId) {
        return Component.translatable("gui.wuxingxiantu.heart_method.name." + methodId);
    }

    public static Component description(String methodId) {
        return Component.translatable("gui.wuxingxiantu.heart_method.description." + methodId,
                cn.blockforge.generated.generatedmod.balance.HeartMethodBalance.descriptionArguments(methodId));
    }
}
