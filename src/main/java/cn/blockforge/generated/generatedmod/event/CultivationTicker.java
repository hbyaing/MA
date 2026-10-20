package cn.blockforge.generated.generatedmod.event;

import cn.blockforge.generated.generatedmod.*;
import static cn.blockforge.generated.generatedmod.GeneratedMod.*;
import cn.blockforge.generated.generatedmod.items.CultivationLogic;
import cn.blockforge.generated.generatedmod.items.PhysiqueLogic;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** High-frequency movement precedes 20-tick progression and attribute maintenance. */
public final class CultivationTicker {
    private static final int FREE_FLIGHT_AURA_COST = 25;
    private static final Map<UUID, Long> PLAYER_TICK_ERROR_LOG = new HashMap<>();
    public static void tickPlayer(ServerPlayer player, long gameTime) {
        try {
            CultivationData modeData = data(player);
            boolean soulProjectionActive = modeData != null
                    && ("yuan_shen_chu_qiao".equals(modeData.wuxingxiantu$getActiveDivineArt())
                    || "yi_hun_ji_po".equals(modeData.wuxingxiantu$getActiveDivineArt())
                    || "qi_shi_er_bian".equals(modeData.wuxingxiantu$getActiveDivineArt())
                    || "jin_dou_yun".equals(modeData.wuxingxiantu$getActiveDivineArt()));
            // 旁观模式不参与本模组普通 Tick；创造模式仍需运行灵力恢复和属性维护，
            // 但普通修为入口会拒绝创造模式。
            if (player.isSpectator() && !soulProjectionActive) {
                if (modeData != null && modeData.wuxingxiantu$getDivineStepOrigin() != null) {
                    DivineArtLogic.tick(player, modeData);
                } else {
                    DivineArtLogic.tickVisualEffects(player);
                }
                return;
            }
            CultivationData data = modeData;
            if (data == null) return;
            int realm = data.wuxingxiantu$getRealm();
            if (gameTime % 24000L == 0L && data.wuxingxiantu$getDaoXin() == 0) {
                data.wuxingxiantu$setDaysSinceDeath(data.wuxingxiantu$getDaysSinceDeath() + 1);
            }
            if (data.wuxingxiantu$getDaysSinceDeath() >= 3 && data.wuxingxiantu$getDaoXin() == 0) {
                data.wuxingxiantu$setDaoXin(1);
            }
            if (data.wuxingxiantu$getBiguRemainTime() > 0 && gameTime % 20L == 0L) {
                data.wuxingxiantu$setBiguRemainTime(data.wuxingxiantu$getBiguRemainTime() - 1);
            }
            // Before Foundation Establishment the fasting pill must actually
            // suppress vanilla hunger and exhaustion, not merely display a timer.
            if (realm == 0 && data.wuxingxiantu$getBiguRemainTime() > 0) {
                player.getFoodData().setFoodLevel(20);
                player.getFoodData().setSaturation(5.0F);
                player.getFoodData().setExhaustion(0.0F);
            }
            if (data.wuxingxiantu$isMeditating()) {
                BlockPos meditationPos = data.wuxingxiantu$getMeditationPos();
                boolean invalidSeat = meditationPos == null
                        || !(player.getVehicle() instanceof MeditationSeatEntity seat)
                        || !seat.cushionPos().equals(meditationPos)
                        || !(player.level().getBlockState(meditationPos).getBlock() instanceof CultivationBlock cushion)
                        || !cushion.isMeditationCushion();
                if (invalidSeat || player.getDeltaMovement().horizontalDistanceSqr() > 0.0025D
                        || player.getAbilities().flying) {
                    CultivationBlock.stopMeditation(player, data, false);
                } else {
                    player.setPose(net.minecraft.world.entity.Pose.SITTING);
                }
            }
            if (realm >= 1) player.getFoodData().setFoodLevel(20);
            cn.blockforge.generated.generatedmod.event.RealmVision.tick(player, realm >= 1);
            if ("yuan_shen_chu_qiao".equals(data.wuxingxiantu$getActiveDivineArt())
                    && player.hasEffect(ModEffects.YUAN_SHEN_CHU_QIAO.get())) {
                player.noPhysics = true;
                player.setNoGravity(true);
            }
            if ("fa_tian_xiang_di".equals(data.wuxingxiantu$getActiveDivineArt())
                    && player.hasEffect(ModEffects.FA_TIAN_XIANG_DI.get())) {
                player.setShiftKeyDown(false);
                if (player.getPose() != net.minecraft.world.entity.Pose.STANDING) {
                    player.setPose(net.minecraft.world.entity.Pose.STANDING);
                }
            }
            updateFlight(player, data, gameTime);
            DivineArtLogic.tick(player, data);
            TransformAbilityRegistry.tickPassive(player, data.wuxingxiantu$getActivePossessedForm());
            CultivationLogic.tickWaterWalking(player, data);
            if (gameTime % 20L == 0L) SpiritEnchantmentLogic.broadcastWrapVisual(player, data);
            SpiritEnchantmentLogic.tick(player, gameTime);
            if (player.tickCount % 20 != 0) return;
            CultivationLogic.applyRootEffects(player, data);
            PhysiqueLogic.tick(player, data);
            CultivationLogic.applyHealthBonus(player, data);
            if (data.wuxingxiantu$isMeditating()) {
                double cultivationReward = CultivationLogic.meditationCultivationReward(data);
                var gatheringPill = player.getEffect(ModEffects.PILL_GATHERING.get());
                if (gatheringPill != null) cultivationReward = cultivationReward
                        * Math.min(3.0D, 1.0D + 0.15D * Math.min(4, gatheringPill.getAmplifier() + 1));
                long beforeMeditationGain=data.wuxingxiantu$getCultivation();
                if(player.hasEffect(ModEffects.TALISMAN_GATHERING.get())) cultivationReward=cultivationReward*1.15D;
                CultivationLogic.gainTrainingCultivation(player, data, cultivationReward);
                if(data.wuxingxiantu$getCultivation()>beforeMeditationGain&&player.getVehicle() instanceof MeditationSeatEntity seat)seat.cultivationGain();
                if (player.tickCount % 100 == 0) {
                    player.displayClientMessage(Component.translatable("message.wuxingxiantu.meditation_reward",
                            data.wuxingxiantu$getCultivation()-beforeMeditationGain), true);
                }
                // Meditation grants cultivation only; crystals require an explicit reward.

            }
        } catch (Exception exception) {
            long now = player.level().getGameTime();
            long last = PLAYER_TICK_ERROR_LOG.getOrDefault(player.getUUID(), Long.MIN_VALUE);
            if (last == Long.MIN_VALUE || now - last >= 200L) {
                PLAYER_TICK_ERROR_LOG.put(player.getUUID(), now);
                com.mojang.logging.LogUtils.getLogger().error("Player cultivation tick failed for {}", player.getScoreboardName(), exception);
            }
        }
    }

    private static void updateFlight(ServerPlayer player, CultivationData data, long gameTime) {
        if(data.wuxingxiantu$getRealm()>=3) {
            var effect=player.getEffect(ModEffects.REALM_FLIGHT.get());
            int amplifier=cn.blockforge.generated.generatedmod.realmflight.RealmFlightLevel.amplifierForRealm(data.wuxingxiantu$getRealm());
            if(effect==null||effect.getAmplifier()!=amplifier||effect.getDuration()<40) {
                // Remove a stale stronger instance as well: vanilla merging otherwise delays a realm downgrade.
                if(effect!=null&&effect.getAmplifier()!=amplifier)player.removeEffect(ModEffects.REALM_FLIGHT.get());
                player.addEffect(new net.minecraft.world.effect.MobEffectInstance(ModEffects.REALM_FLIGHT.get(),220,amplifier,true,false,true));
            }
        } else player.removeEffect(ModEffects.REALM_FLIGHT.get());
        if (!cn.blockforge.generated.generatedmod.realmflight.RealmFlightControl.eligible(player)) {
            cn.blockforge.generated.generatedmod.realmflight.RealmFlightControl.release(player);
            if(data.wuxingxiantu$getRealm()<3&&!player.isCreative()&&!player.isSpectator()) {
                boolean changed=player.getAbilities().mayfly||player.getAbilities().flying;
                player.getAbilities().mayfly=false;player.getAbilities().flying=false;
                player.getAbilities().setFlyingSpeed(.05F);if(changed)player.onUpdateAbilities();
            }
            return;
        }
        boolean allowed=data.wuxingxiantu$getRealm()>=5||data.wuxingxiantu$getAura()>=1.25F;
        boolean changed=player.getAbilities().mayfly!=allowed;
        player.getAbilities().mayfly=allowed;
        if(!allowed&&player.getAbilities().flying){player.getAbilities().flying=false;changed=true;}
        cn.blockforge.generated.generatedmod.realmflight.RealmFlightControl.tick(player);
        if(changed||gameTime%40==0)player.onUpdateAbilities();
    }

    private static int freeFlightCost(CultivationData data) {
        return FREE_FLIGHT_AURA_COST;
    }
    private CultivationTicker() {}
}
