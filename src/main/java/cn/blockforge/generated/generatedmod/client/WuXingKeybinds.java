package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.CultivationData;
import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.Network;
import cn.blockforge.generated.generatedmod.items.CultivationLogic;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

/** V 心剑请求；I 打开内视，R 打开功法轮盘。 */
@Mod.EventBusSubscriber(modid = GeneratedMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class WuXingKeybinds {
    private static final String KEY_CATEGORY = "key.categories.wuxingxiantu";
    public static final KeyMapping HEART_SWORD = new KeyMapping(
            "key.wuxingxiantu.heart_sword", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, KEY_CATEGORY);
    public static final KeyMapping INNER_VISION = new KeyMapping(
            "key.wuxingxiantu.inner_vision", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_I, KEY_CATEGORY);
    public static final KeyMapping ART_WHEEL = new KeyMapping(
            "key.wuxingxiantu.art_wheel", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, KEY_CATEGORY);
    public static final KeyMapping SWORD_FLIGHT = new KeyMapping(
            "key.wuxingxiantu.sword_flight", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_B, KEY_CATEGORY);
    public static final KeyMapping RECENTER_FLYING_SWORD = new KeyMapping(
            "key.wuxingxiantu.recenter_flying_sword", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, KEY_CATEGORY);
    public static final KeyMapping POSSESSION_RELEASE = new KeyMapping(
            "key.wuxingxiantu.possession_release", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_X, KEY_CATEGORY);
    public static final KeyMapping TRANSFORM_TOGGLE = new KeyMapping(
            "key.wuxingxiantu.transform_toggle", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Z, KEY_CATEGORY);
    public static final KeyMapping TRANSFORM_NEXT = new KeyMapping(
            "key.wuxingxiantu.transform_next", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_C, KEY_CATEGORY);
    public static final KeyMapping TRANSFORM_ABILITY = new KeyMapping(
            "key.wuxingxiantu.transform_ability", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Y, KEY_CATEGORY);
    private static boolean wardenAttackHeld;
    private static boolean creeperChargeHeld;

    private WuXingKeybinds() {}

    private static void suppressChargeAnimation(Minecraft client) {
        if (client.player == null) return;
        client.player.attackAnim = 0.0F;
        client.player.oAttackAnim = 0.0F;
        client.player.swinging = false;
        client.player.swingTime = 0;
    }

    private static boolean hasUsableArt(Minecraft client) {
        if (client.player == null) return false;
        CultivationData data = GeneratedMod.data(client.player);
        if (data == null) return false;
        int unlocked = CultivationLogic.unlockedArtSlots(data.wuxingxiantu$getRealm());
        String[] arts = data.wuxingxiantu$getEquippedArts();
        for (int slot = 0; slot < Math.min(unlocked, Math.min(8, arts.length)); slot++) {
            if (cn.blockforge.generated.generatedmod.ArtCatalog.isValid(arts[slot])) return true;
        }
        return data.wuxingxiantu$getLearnedDivineArts().length > 0
                || data.wuxingxiantu$getKnownSwordTechniques().length > 0;
    }

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(HEART_SWORD);
        event.register(INNER_VISION);
        event.register(ART_WHEEL);
        event.register(SWORD_FLIGHT);
        event.register(RECENTER_FLYING_SWORD);
        event.register(POSSESSION_RELEASE);
        event.register(TRANSFORM_TOGGLE);
        event.register(TRANSFORM_NEXT);
        event.register(TRANSFORM_ABILITY);
    }

    @Mod.EventBusSubscriber(modid = GeneratedMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static final class ClientEvents {
        private ClientEvents() {}

        /** Camera rotation is local; leave the host's network interpolation untouched. */
        @SubscribeEvent
        public static void possessionCameraTurn(net.minecraftforge.client.event.ViewportEvent.ComputeCameraAngles event) {
            Minecraft client = Minecraft.getInstance();
            if (client.player == null) return;
            CultivationData data = GeneratedMod.data(client.player);
            boolean possessing = data != null
                    && "yi_hun_ji_po".equals(data.wuxingxiantu$getActiveDivineArt());
            net.minecraft.world.entity.Entity camera = client.getCameraEntity();
            if (!possessing) {
                // 服务端收束包丢失或界面切换时，客户端也要立即回到玩家本体，
                // 否则下一次施展神通会继续沿用上一次生物视角。
                if (camera != null && camera != client.player) client.setCameraEntity(client.player);
                return;
            }
            if (camera == null || camera == client.player) return;
            boolean front = client.options.getCameraType() == net.minecraft.client.CameraType.THIRD_PERSON_FRONT;
            event.setYaw(client.player.getYRot() + (front ? 180.0F : 0.0F));
            event.setPitch(client.player.getXRot() * (front ? -1.0F : 1.0F));
        }

        /**
         * 已附身时本体只承载视角；清空原版移动输入，避免旁观相机位移触发位置校正和漂移。
         *
         * 移魂寄魄刚发动、尚未选中宿主时，玩家仍应以元神状态自由移动寻找目标；
         * 不能仅凭神通激活就锁死移动，必须确认客户端相机已经切换到宿主生物。
         */
        @SubscribeEvent
        public static void lockPossessedPlayerMovement(MovementInputUpdateEvent event) {
            if (!(event.getEntity() instanceof net.minecraft.client.player.LocalPlayer player)) return;
            CultivationData data = GeneratedMod.data(player);
            if (data == null || !"yi_hun_ji_po".equals(data.wuxingxiantu$getActiveDivineArt())
                    || Minecraft.getInstance().getCameraEntity() == player) return;
            event.getInput().leftImpulse = 0.0F;
            event.getInput().forwardImpulse = 0.0F;
            event.getInput().up = false;
            event.getInput().down = false;
            event.getInput().left = false;
            event.getInput().right = false;
            event.getInput().jumping = false;
            event.getInput().shiftKeyDown = false;
        }

        /** 寄魄和七十二变的攻击都由服务端重新校验；伪装实体不能发送玩家原版攻击包。 */
        @SubscribeEvent
        public static void possessionAttack(net.minecraftforge.client.event.InputEvent.InteractionKeyMappingTriggered event) {
            Minecraft client = Minecraft.getInstance();
            if (client.player == null || !event.isAttack()) return;
            CultivationData data = GeneratedMod.data(client.player);
            if (data == null) return;
            String active = data.wuxingxiantu$getActiveDivineArt();
            boolean possession = "yi_hun_ji_po".equals(active);
            var bossProfile="qi_shi_er_bian".equals(active)?cn.blockforge.generated.generatedmod.boss.combat.BossPlayerSkills.profile(data.wuxingxiantu$getActivePossessedForm()):null;
            if(bossProfile!=null){event.setCanceled(true);event.setSwingHand(false);Network.CHANNEL.sendToServer(new cn.blockforge.generated.generatedmod.network.packet.divineart.TransformAbilityPacket(bossProfile.id()+"_basic"));return;}
            boolean wardenTransform = "qi_shi_er_bian".equals(active)
                    && "minecraft:warden".equals(data.wuxingxiantu$getActivePossessedForm());
            boolean shulkerTransform = "qi_shi_er_bian".equals(active)
                    && "minecraft:shulker".equals(data.wuxingxiantu$getActivePossessedForm());
            if (possession || wardenTransform || shulkerTransform) {
                event.setCanceled(true);
                event.setSwingHand(false);
                if (wardenTransform && !wardenAttackHeld) {
                    wardenAttackHeld = true;
                    ModClientEvents.ClientForgeEvents.requestDisguiseAttack(client.player);
                    Network.CHANNEL.sendToServer(new cn.blockforge.generated.generatedmod.network.packet.divineart.TransformAttackPacket());
                }
                // 潜影贝左键挥击：与其走原版近战，不如直接展开开壳动画窗口，壳随每次攻击张开。
                if (shulkerTransform) {
                    ModClientEvents.ClientForgeEvents.requestDisguiseShulkerPeek(client.player);
                }
            }
        }

        /** 旁观模式不会稳定触发原版右键实体包，改为明确把准星目标交给服务端校验。 */
        @SubscribeEvent
        public static void possessionInteract(net.minecraftforge.client.event.InputEvent.InteractionKeyMappingTriggered event) {
            Minecraft client = Minecraft.getInstance();
            if (client.player == null || !event.isUseItem()) return;
            CultivationData data = GeneratedMod.data(client.player);
            if (data == null) return;
            String active = data.wuxingxiantu$getActiveDivineArt();
            if (!"yi_hun_ji_po".equals(active) && !"yuan_shen_chu_qiao".equals(active)) return;
            net.minecraft.world.entity.Entity picked = client.crosshairPickEntity;
            if(picked instanceof cn.blockforge.generated.generatedmod.SoulBodyEntity body) {
                event.setCanceled(true);
                event.setSwingHand(false);
                Network.CHANNEL.sendToServer(new cn.blockforge.generated.generatedmod.network.packet.divineart.PossessTargetPacket(body.getId()));
                return;
            }
            net.minecraft.world.entity.LivingEntity target =
                    cn.blockforge.generated.generatedmod.DivineArtLogic.possessionTarget(picked);
            if (target != null && target != client.player && target.isAlive()) {
                // 两种元神状态都禁止原版旁观右键；只有移魂寄魄额外发起自定义附身请求。
                event.setCanceled(true);
                event.setSwingHand(false);
                if ("yi_hun_ji_po".equals(active)) {
                    Network.CHANNEL.sendToServer(new cn.blockforge.generated.generatedmod.network.packet.divineart.PossessTargetPacket(target.getId()));
                }
            }
        }

        @SubscribeEvent
        public static void clientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            Minecraft client = Minecraft.getInstance();
            boolean releaseClicked = false;
            while (POSSESSION_RELEASE.consumeClick()) releaseClicked = true;
            if (client.player == null) {
                return;
            }
            if (client.screen == null && HEART_SWORD.consumeClick()) {
                Network.CHANNEL.sendToServer(new cn.blockforge.generated.generatedmod.network.packet.heartsword.HeartSwordRequestPacket());
                return;
            }
            CultivationData currentData = GeneratedMod.data(client.player);
            String active = currentData == null ? "" : currentData.wuxingxiantu$getActiveDivineArt();
            if (releaseClicked && active.isBlank()) Network.CHANNEL.sendToServer(new cn.blockforge.generated.generatedmod.yujian.SwordRecallPacket());
            if (!client.options.keyAttack.isDown() || currentData == null
                    || !"qi_shi_er_bian".equals(active)
                    || !"minecraft:warden".equals(currentData.wuxingxiantu$getActivePossessedForm())) {
                wardenAttackHeld = false;
            }

            // 轮盘是元神状态的正式收束入口：必须在元神输入分支之前处理 R，
            // 否则 R 会被每刻的元神 return 吞掉，玩家只能被迫等待效果结束。
            if (client.screen == null && ART_WHEEL.consumeClick() && hasUsableArt(client)) {
                client.setScreen(new ArtRadialScreen());
                return;
            }
            if (client.screen == null && INNER_VISION.consumeClick()) {
                client.setScreen(new InnerVisionScreen());
                return;
            }
            if (client.screen != null) return;

            if ("yi_hun_ji_po".equals(active)) {
                int flags = 0;
                if (client.options.keyUp.isDown()) flags |= 1;
                if (client.options.keyDown.isDown()) flags |= 1 << 1;
                if (client.options.keyLeft.isDown()) flags |= 1 << 2;
                if (client.options.keyRight.isDown()) flags |= 1 << 3;
                if (client.options.keyShift.isDown()) flags |= 1 << 5;
                if (client.options.keyAttack.isDown()) flags |= 1 << 6;
                if (client.options.keyJump.isDown()) flags |= 1 << 4;
                // 旋转必须取本地玩家视角；相机实体（被附身生物）的客户端朝向由服务端同步而来，
                // 拿它回传会形成“服务端写生物、客户端再读生物”的死循环，导致视角彻底锁死。
                float yaw = client.player.getYRot();
                float pitch = client.player.getXRot();
                // 跳跃和生灵技能都转发给服务端，由服务端按当前宿主形态校验并执行。
                Network.CHANNEL.sendToServer(new cn.blockforge.generated.generatedmod.network.packet.divineart.PossessionInputPacket(yaw, pitch, flags));
                if (TRANSFORM_ABILITY.consumeClick()) {
                    if ("minecraft:creeper".equals(currentData.wuxingxiantu$getActivePossessedForm())) {
                        ModClientEvents.ClientForgeEvents.requestDisguiseCreeperPrime(client.player);
                    }
                    Network.CHANNEL.sendToServer(new cn.blockforge.generated.generatedmod.network.packet.divineart.TransformAbilityPacket());
                }

                if (releaseClicked) {
                    Network.CHANNEL.sendToServer(new cn.blockforge.generated.generatedmod.network.packet.divineart.DivineReleasePacket("yi_hun_ji_po"));
                }
                return;
            }
            if ("yuan_shen_chu_qiao".equals(active) && releaseClicked) {
                // X 对元神出窍也可作为紧急收束键；它不会尝试执行寄魄逻辑。
                Network.CHANNEL.sendToServer(new cn.blockforge.generated.generatedmod.network.packet.divineart.DivineReleasePacket("yuan_shen_chu_qiao"));
                return;
            }
            if (TRANSFORM_TOGGLE.consumeClick()) {
                CultivationData transformData = GeneratedMod.data(client.player);
                if (transformData != null && (transformData.wuxingxiantu$hasLearnedDivineArt("qi_shi_er_bian")
                        || "qi_shi_er_bian".equals(transformData.wuxingxiantu$getActiveDivineArt()))) {
                    Network.CHANNEL.sendToServer(new cn.blockforge.generated.generatedmod.network.packet.divineart.DivineReleasePacket("qi_shi_er_bian"));
                }
            }
            if (TRANSFORM_NEXT.consumeClick()) {
                CultivationData transformData = GeneratedMod.data(client.player);
                if (transformData != null && "qi_shi_er_bian".equals(transformData.wuxingxiantu$getActiveDivineArt())) {
                    Network.CHANNEL.sendToServer(new cn.blockforge.generated.generatedmod.network.packet.divineart.TransformCyclePacket(true));
                }
            }
            // 七十二变苦力怕：长按 Y 维持膨胀，松开引爆并保持苦力怕形态。
            CultivationData creeperData = GeneratedMod.data(client.player);
            boolean creeperForm = creeperData != null && "qi_shi_er_bian".equals(creeperData.wuxingxiantu$getActiveDivineArt())
                    && "minecraft:creeper".equals(creeperData.wuxingxiantu$getActivePossessedForm());
            boolean yHeld = creeperForm && TRANSFORM_ABILITY.isDown();
            if (creeperForm) {
                if (yHeld && !creeperChargeHeld) {
                    creeperChargeHeld = true;
                    ModClientEvents.ClientForgeEvents.requestDisguiseCreeperPrime(client.player);
                    Network.CHANNEL.sendToServer(new cn.blockforge.generated.generatedmod.network.packet.divineart.TransformAbilityPacket());
                } else if (yHeld) {
                    // 持续按住时每帧续一次武装窗口，让伪装苦力怕一直保持膨胀。
                    ModClientEvents.ClientForgeEvents.requestDisguiseCreeperPrime(client.player);
                } else if (creeperChargeHeld) {
                    creeperChargeHeld = false;
                    Network.CHANNEL.sendToServer(new cn.blockforge.generated.generatedmod.network.packet.divineart.TransformCreeperReleasePacket());
                }
            } else {
                creeperChargeHeld = false;
                if (TRANSFORM_ABILITY.consumeClick()) {
                    CultivationData transformData = GeneratedMod.data(client.player);
                    if (transformData != null && "qi_shi_er_bian".equals(transformData.wuxingxiantu$getActiveDivineArt())) {
                        if(cn.blockforge.generated.generatedmod.boss.combat.BossPlayerSkills.profile(transformData.wuxingxiantu$getActivePossessedForm())!=null){client.setScreen(new BossTransformSkillScreen(transformData.wuxingxiantu$getActivePossessedForm()));return;}
                        if ("minecraft:shulker".equals(transformData.wuxingxiantu$getActivePossessedForm())) {
                            // 潜影贝发射潜影贝弹后让伪装壳张开，客户端只登记动画，不提交伤害。
                            ModClientEvents.ClientForgeEvents.requestDisguiseShulkerPeek(client.player);
                        }
                        Network.CHANNEL.sendToServer(new cn.blockforge.generated.generatedmod.network.packet.divineart.TransformAbilityPacket());
                    }
                }
            }
            
            // Sword flight uses a held input snapshot so summon, mount, and flight
            // transitions are distinct. YujianClientController owns that stream.
        }
    }
}
