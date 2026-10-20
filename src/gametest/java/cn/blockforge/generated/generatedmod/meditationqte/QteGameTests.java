package cn.blockforge.generated.generatedmod.meditationqte;

import cn.blockforge.generated.generatedmod.*;
import cn.blockforge.generated.generatedmod.fixtures.GameTestPlayer;
import cn.blockforge.generated.generatedmod.items.CultivationLogic;
import io.netty.buffer.Unpooled;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.gametest.*;

@GameTestHolder("wuxingxiantu_entry")
@PrefixGameTestTemplate(false)
public final class QteGameTests {
    private static GameTestPlayer seated(GameTestHelper helper) {
        var player = new GameTestPlayer(helper.getLevel());
        player.join();
        var pos = helper.absolutePos(new BlockPos(1, 1, 1));
        player.keepTicking(helper.getLevel(), pos);
        helper.getLevel().setBlockAndUpdate(pos, WuXingBlocks.MEDITATION_CUSHION.get().defaultBlockState());
        player.setPos(Vec3.atCenterOf(pos).add(0, 1, 0)); player.setDeltaMovement(Vec3.ZERO);
        var data = GeneratedMod.data(player);
        data.wuxingxiantu$restoreDaoXin(0); data.wuxingxiantu$setRealm(0); data.wuxingxiantu$setCultivationStage(0);
        data.wuxingxiantu$restoreCultivation(0);
        CultivationBlock.startMeditation(player, data, pos, 0);
        helper.assertTrue(MeditationQte.eligible(player), "Real target cushion did not qualify for the source game");
        return player;
    }
    private static void clear(GameTestPlayer player) {
        MeditationQte.logout(new PlayerEvent.PlayerLoggedOutEvent(player));
        CultivationBlock.stopMeditation(player, GeneratedMod.data(player), false);
    }
    @GameTest(template="empty") public static void realSeatRequiredAndCancelConsumesOnlyCurrentTargetStage(GameTestHelper helper) {
        try (var player = seated(helper)) {
            try {
                helper.assertTrue(MeditationQte.start(player) && MeditationQte.used(player), "Game failed to consume current target-stage attempt at entry");
                var snapshot = MeditationQte.snapshot(player);
                MeditationQte.cancel(player, UUID.randomUUID());
                helper.assertTrue(MeditationQte.active(player), "Foreign nonce cancelled another session");
                MeditationQte.cancel(player, snapshot.nonce());
                helper.assertTrue(!MeditationQte.active(player) && MeditationQte.used(player), "Cancel refunded the source one-attempt rule");
                var saved = player.getPersistentData().getCompound(MeditationQte.DATA_KEY).copy();
                player.getPersistentData().put(MeditationQte.DATA_KEY, saved.copy());
                helper.assertTrue(MeditationQte.used(player), "Attempt did not persist independently of the transient session");
                GeneratedMod.data(player).wuxingxiantu$setCultivationStage(1);
                helper.assertTrue(!MeditationQte.used(player), "New target small stage inherited the previous attempt");
                GeneratedMod.data(player).wuxingxiantu$setCultivationStage(0);
                helper.assertTrue(MeditationQte.used(player), "Returning to an already used stage renewed its attempt");
                CultivationBlock.stopMeditation(player, GeneratedMod.data(player), false);
                helper.assertTrue(!MeditationQte.eligible(player), "Standing player bypassed the real meditation seat requirement");
            } finally { clear(player); }
        } helper.succeed();
    }
    @GameTest(template="empty") public static void serverClockNonceAndRoundGateThreeRoundReward(GameTestHelper helper) {
        try (var player = seated(helper)) {
            try {
                long cap = CultivationLogic.breakthroughRequirement(GeneratedMod.data(player));
                helper.assertTrue(MeditationQte.start(player), "QTE did not start");
                var original = MeditationQte.snapshot(player);
                helper.assertTrue(MeditationQte.hitAt(player, UUID.randomUUID(), 1, original.startMillis() + 500, original.openedTick() + 20) == -1,
                        "Forged nonce accepted a mathematically perfect cursor");
                helper.assertTrue(MeditationQte.hitAt(player, original.nonce(), 2, original.startMillis() + 500, original.openedTick() + 20) == -1,
                        "Client skipped directly to another round");
                helper.assertTrue(MeditationQte.hitAt(player, original.nonce(), 1, original.startMillis() - 1, original.openedTick()) == -1
                        && MeditationQte.snapshot(player).level() == 1, "Hit before the server start time progressed the game");
                for (int level = 1; level <= 3; level++) {
                    var round = MeditationQte.snapshot(player);
                    int result = MeditationQte.hitAt(player, round.nonce(), level,
                            round.startMillis() + Math.round(QteRules.period(level) / 4.0), round.openedTick() + 20L * level);
                    helper.assertTrue(result == (level < 3 ? QteStatePacket.PASS : QteStatePacket.WIN), "Real three-round reward flow failed");
                    helper.assertTrue(MeditationQte.hitAt(player, round.nonce(), level, round.startMillis() + 500, round.openedTick() + 80) == -1,
                            "Replaying a consumed round advanced the new session");
                }
                long gained = GeneratedMod.data(player).wuxingxiantu$getCultivation();
                helper.assertTrue(!MeditationQte.active(player) && gained > 0 && gained <= Math.ceil(cap * .2), "QTE paid outside the shared cultivation cap");
                helper.assertTrue(GeneratedMod.data(player).wuxingxiantu$getRealm() == 0
                        && GeneratedMod.data(player).wuxingxiantu$getCultivationStage() == 0, "QTE bypassed a target breakthrough");
            } finally { clear(player); }
        } helper.succeed();
    }
    @GameTest(template="empty") public static void redCursorAppliesSingleSharedPenaltyAndFractionalGainSurvivesSave(GameTestHelper helper) {
        try (var player = seated(helper)) {
            try {
                MeditationQte.start(player); var session = MeditationQte.snapshot(player);
                int result = MeditationQte.hitAt(player, session.nonce(), 1, session.startMillis() + 400, session.openedTick() + 20);
                helper.assertTrue(result == QteStatePacket.DEVIATION && !MeditationQte.active(player), "Server red-zone timing did not terminate as deviation");
                var data = (CultivationDataImpl) GeneratedMod.data(player);
                helper.assertTrue(data.meditationPenaltyTicks() == 18000 && MeditationQte.cultivationMultiplier(player) == .5, "Source fifteen-minute penalty was not installed in target data");
                data.wuxingxiantu$restoreCultivation(0);
                data.wuxingxiantu$setCultivation(1);
                helper.assertTrue(data.wuxingxiantu$getCultivation() == 0, "One-point reward escaped the shared penalty");
                var restored = new CultivationDataImpl(); restored.deserializeNBT(data.serializeNBT());
                restored.wuxingxiantu$setCultivation(1);
                helper.assertTrue(restored.wuxingxiantu$getCultivation() == 1 && restored.meditationPenaltyTicks() == 18000,
                        "Penalty fractional carry or duration disappeared on real capability save/load");
                restored.wuxingxiantu$restoreCultivation(8);
                helper.assertTrue(restored.wuxingxiantu$getCultivation() == 8, "Saved authoritative restoration was incorrectly taxed as a reward");
                data.setMeditationPenaltyTicks(1);
                MeditationQte.tick(new TickEvent.PlayerTickEvent(TickEvent.Phase.END, player));
                helper.assertTrue(MeditationQte.cultivationMultiplier(player) == 1 && data.meditationPenaltyTicks() == 0,
                        "Online penalty failed to expire at the target server tick");
            } finally { clear(player); }
        } helper.succeed();
    }
    @GameTest(template="empty") public static void grayFailsAndSeatLossOrStageChangeCannotFinish(GameTestHelper helper) {
        try (var player = seated(helper)) {
            try {
                MeditationQte.start(player); var s = MeditationQte.snapshot(player);
                helper.assertTrue(MeditationQte.hitAt(player, s.nonce(), 1, s.startMillis(), s.openedTick()) == QteStatePacket.FAIL
                        && MeditationQte.deviationRemaining(player) == 0, "Gray cursor caused a reward or an unrelated deviation");
            } finally { clear(player); }
        }
        try (var player = seated(helper)) {
            try {
                MeditationQte.start(player); var s = MeditationQte.snapshot(player);
                CultivationBlock.stopMeditation(player, GeneratedMod.data(player), false);
                helper.assertTrue(MeditationQte.hitAt(player, s.nonce(), 1, s.startMillis() + 500, s.openedTick() + 20) == QteStatePacket.CANCELLED,
                        "Dismounted player completed a round");
            } finally { clear(player); }
        }
        try (var player = seated(helper)) {
            try {
                MeditationQte.start(player); var s = MeditationQte.snapshot(player);
                GeneratedMod.data(player).wuxingxiantu$setCultivationStage(1);
                helper.assertTrue(MeditationQte.hitAt(player, s.nonce(), 1, s.startMillis() + 500, s.openedTick() + 20) == QteStatePacket.CANCELLED,
                        "Old-stage session rewarded a new target stage");
            } finally { clear(player); }
        } helper.succeed();
    }
    @GameTest(template="empty") public static void heartDemonSoulProjectionParalysisAndFullCultivationCannotStart(GameTestHelper helper) {
        try (var player = seated(helper)) {
            try {
                var data = GeneratedMod.data(player);
                data.wuxingxiantu$setDaoHeartUnstable(true);
                helper.assertTrue(!MeditationQte.eligible(player), "Minigame bypassed heart-demon cultivation lock");
                data.wuxingxiantu$resolveHeartDemonInvasion(); data.wuxingxiantu$restoreDaoXin(0);
                data.wuxingxiantu$setActiveDivineArt("yuan_shen_chu_qiao");
                helper.assertTrue(!MeditationQte.eligible(player), "Projected soul entered seated game");
                data.wuxingxiantu$setActiveDivineArt("");
                player.addEffect(new MobEffectInstance(ModEffects.PARALYSIS.get(), 100));
                helper.assertTrue(!MeditationQte.eligible(player), "Paralysed player entered minigame");
                player.removeEffect(ModEffects.PARALYSIS.get());
                data.wuxingxiantu$restoreCultivation(CultivationLogic.breakthroughRequirement(data));
                helper.assertTrue(!MeditationQte.eligible(player), "Already-full cultivation gained another attempt");
            } finally { clear(player); }
        } helper.succeed();
    }
    @GameTest(template="empty") public static void timeoutAndLogoutEndSessionWithoutRefundingUsedAttempt(GameTestHelper helper) {
        try (var player = seated(helper)) {
            try {
                MeditationQte.start(player); var s = MeditationQte.snapshot(player);
                helper.assertTrue(MeditationQte.hitAt(player, s.nonce(), 1, s.startMillis() + 500,
                        s.openedTick() + QteRules.SESSION_TICKS + 1) == QteStatePacket.CANCELLED, "Expired server tick window accepted a hit");
                helper.assertTrue(MeditationQte.used(player) && GeneratedMod.data(player).wuxingxiantu$getCultivation() == 0, "Timeout refunded or rewarded the attempt");
            } finally { clear(player); }
        }
        try (var player = seated(helper)) {
            try {
                MeditationQte.start(player);
                MeditationQte.logout(new PlayerEvent.PlayerLoggedOutEvent(player));
                helper.assertTrue(!MeditationQte.active(player) && MeditationQte.used(player), "Logout left transient state or refunded persistent attempt");
            } finally { clear(player); }
        } helper.succeed();
    }
    @GameTest(template="empty") public static void sourceWidthsAndTwoRealPacketCodecsRemainBounded(GameTestHelper helper) {
        helper.assertTrue(QteRules.greenHalf(1) == 7.5 && QteRules.greenHalf(2) == 4 && QteRules.greenHalf(3) == 2
                && QteRules.period(1) == 2000 && QteRules.period(3) == 909, "Source timing or three green windows changed");
        var nonce = UUID.randomUUID();
        var action = new QteActionPacket(QteActionPacket.HIT, nonce, 2);
        var state = new QteStatePacket(nonce, QteStatePacket.PASS, 3, 1200, 600, 0);
        var buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            action.encode(buffer); helper.assertTrue(action.equals(new QteActionPacket(buffer)), "Actual QTE action codec changed");
            buffer.clear(); state.encode(buffer); helper.assertTrue(state.equals(new QteStatePacket(buffer)), "Actual QTE state codec changed");
        } finally { buffer.release(); }
        boolean rejected = false;
        try { new QteActionPacket(QteActionPacket.HIT, nonce, 4); } catch (IllegalArgumentException expected) { rejected = true; }
        helper.assertTrue(rejected && QteRules.judge(3, Double.NaN) == QteRules.GRAY, "Unbounded client round or nonfinite cursor was accepted");
        helper.succeed();
    }
}
