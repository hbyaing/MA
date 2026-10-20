package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.yujian.FlyingSwordConfig;
import cn.blockforge.generated.generatedmod.yujian.ManualRoll;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** 客户端御剑命令：只改本地配置，不向服务器发送命令包。 */
@Mod.EventBusSubscriber(modid = "wuxingxiantu", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class YujianClientCommands {
    private YujianClientCommands() {}

    @SubscribeEvent
    public static void register(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("wuxing")
                .then(Commands.literal("yujian")
                        .then(Commands.literal("camera")
                                .then(Commands.argument("mode", StringArgumentType.word())
                                        .suggests((context, builder) -> {
                                            builder.suggest("stable"); builder.suggest("follow"); builder.suggest("locked");
                                            return builder.buildFuture();
                                        }).executes(context -> setCamera(StringArgumentType.getString(context, "mode")))))
                        .then(Commands.literal("roll")
                                .then(Commands.argument("mode", StringArgumentType.word())
                                        .suggests((context, builder) -> {
                                            builder.suggest("hold"); builder.suggest("recenter");
                                            return builder.buildFuture();
                                        }).executes(context -> setRoll(StringArgumentType.getString(context, "mode")))))));
    }

    private static int setCamera(String value) {
        FlyingSwordConfig.CameraMode mode = switch (value.toLowerCase(java.util.Locale.ROOT)) {
            case "stable" -> FlyingSwordConfig.CameraMode.STABLE;
            case "follow", "follow_roll" -> FlyingSwordConfig.CameraMode.FOLLOW_ROLL;
            case "locked", "full_locked" -> FlyingSwordConfig.CameraMode.FULL_LOCKED;
            default -> null;
        };
        if (mode == null) return fail("用法：/wuxing yujian camera stable|follow|locked");
        FlyingSwordConfig.CAMERA_MODE.set(mode); FlyingSwordConfig.CAMERA_MODE.save();
        say("御剑镜头已切换为：" + modeName(mode));
        return 1;
    }

    private static int setRoll(String value) {
        ManualRoll.ReleaseMode mode = switch (value.toLowerCase(java.util.Locale.ROOT)) {
            case "hold", "hold_angle" -> ManualRoll.ReleaseMode.HOLD_ANGLE;
            case "recenter" -> ManualRoll.ReleaseMode.RECENTER;
            default -> null;
        };
        if (mode == null) return fail("用法：/wuxing yujian roll hold|recenter");
        FlyingSwordConfig.ROLL_RELEASE_MODE.set(mode); FlyingSwordConfig.ROLL_RELEASE_MODE.save();
        say("御剑松键行为已切换为：" + (mode == ManualRoll.ReleaseMode.HOLD_ANGLE ? "保持角度" : "自动回正"));
        return 1;
    }

    private static String modeName(FlyingSwordConfig.CameraMode mode) {
        return switch (mode) {
            case STABLE -> "稳定"; case FOLLOW_ROLL -> "跟随翻滚"; case FULL_LOCKED -> "完全锁定";
        };
    }
    private static void say(String text) {
        Minecraft.getInstance().player.displayClientMessage(Component.literal(text), false);
    }
    private static int fail(String text) { say(text); return 0; }
}
