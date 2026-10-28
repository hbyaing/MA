package cn.blockforge.generated.generatedmod.alchemy;

import cn.blockforge.generated.generatedmod.*;
import cn.blockforge.generated.generatedmod.items.CultivationLogic;
import cn.blockforge.generated.generatedmod.items.PhysiqueLogic;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Bounded effects for canonical and legacy pills. No pill changes a major realm. */
public final class PillEffects {

    public static boolean apply(Player player, CultivationData data, ItemStack stack) {
        PillData.View pill = PillData.read(stack);
        if (pill == null || !ProgressionGate.canUsePill(player, stack)) return false;
        int grade = pill.grade().value();
        switch (pill.pill()) {
            case GATHER_QI, ORIGIN_CONDENSING, MERIDIAN_EXPANSION, BARRIER_PREPARATION, REJUVENATION -> {
                if(!(player instanceof ServerPlayer server)||!MedicineProgress.apply(server,data,pill.pill(),grade,MedicineTier.of(stack)))return false;
            }
            case AURA -> {
                if (CultivationLogic.restoreMana(data, CultivationLogic.maxAura(data) * grade * 0.25F) <= 0) return false;
            }
            case GOLDEN_WOUND -> {
                if (player.getHealth() >= player.getMaxHealth()) return false;
                player.heal(player.getMaxHealth() * (0.05F + 0.15F * grade));
            }
            case BIGU -> {
                if (data.wuxingxiantu$getRealm() >= 1) return false;
                data.wuxingxiantu$setBiguRemainTime((int) Math.min(Integer.MAX_VALUE,
                        (long) data.wuxingxiantu$getBiguRemainTime() + 18000L * grade));
            }
            case CALMING -> {
                if (!(player instanceof ServerPlayer server)
                        || !cn.blockforge.generated.generatedmod.mindscape.MindscapeProgression.requestFromPill(server)) return false;
                player.displayClientMessage(Component.translatable("message.wuxingxiantu.calming_mindscape_ready"), false);
            }
            case CLEAR_HEART -> {
                boolean affected = data.wuxingxiantu$getDaoXin() != 0 || player.hasEffect(MobEffects.CONFUSION);
                if (!affected) return false;
                data.wuxingxiantu$setDaoXin(0);
                player.removeEffect(MobEffects.CONFUSION);
                buff(player, MobEffects.REGENERATION, 100 * grade, 0);
                // DaoHeartUnstable is a separate progression state and is never cleared here.
            }
            case CONTINUE_VEIN -> {
                if (data.wuxingxiantu$getMeridianStatus() == 0 && !player.hasEffect(MobEffects.WEAKNESS)) return false;
                data.wuxingxiantu$setMeridianStatus(0);
                player.removeEffect(MobEffects.WEAKNESS);
                player.removeEffect(ModEffects.MERIDIAN_DAMAGED.get());
                player.removeEffect(ModEffects.MERIDIAN_SHATTERED.get());
                buff(player, MobEffects.REGENERATION, 200 * grade, 0);
            }
            case GATHERING -> buff(player, ModEffects.PILL_GATHERING.get(), 1200 * grade, grade - 1);
            case CULTIVATION -> {
                if(!cn.blockforge.generated.generatedmod.mindscape.MindscapeManager.canGain(player,data,true))return false;
                long cap = CultivationLogic.breakthroughRequirement(data);
                long before = data.wuxingxiantu$getCultivation();
                if (before >= cap) return false;
                long gain = AlchemyBalance.cultivationGain(pill.cultivationTier(), grade);
                data.wuxingxiantu$setCultivation(before + Math.min(cap - before, gain));
            }
            case FOUNDATION, ROOT -> {
                int root = pill.element().ordinal();
                if (pill.pill() == PillCatalog.FOUNDATION) {
                    java.util.List<Integer> eligible = new java.util.ArrayList<>();
                    for (int i = 0; i < 5; i++) if (data.wuxingxiantu$getRootQuality(i) < grade) eligible.add(i);
                    if (eligible.isEmpty()) return false;
                    root = eligible.get(player.getRandom().nextInt(eligible.size()));
                }
                int current = data.wuxingxiantu$getRootQuality(root);
                if ((data.wuxingxiantu$getRootMask() & (1 << root)) != 0 && current >= grade) return false;
                data.wuxingxiantu$setRootMask(data.wuxingxiantu$getRootMask() | (1 << root));
                data.wuxingxiantu$setRootQuality(root, Math.max(current, grade));
                CultivationLogic.refreshChaosRoot(player, data);
            }
            case BREAKTHROUGH -> {
                return CultivationLogic.advanceMinorStage(player,data);
            }
            case TRIBULATION -> {
                if (!(player instanceof ServerPlayer server) || !TribulationManager.isActive(server)) return false;
                buff(player, ModEffects.PILL_TRIBULATION.get(), 1200 * grade, grade - 1);
            }
            case DIAMOND_BODY -> {
                buff(player, MobEffects.DAMAGE_RESISTANCE, 600 * grade, Math.min(2, grade - 1));
                buff(player, MobEffects.ABSORPTION, 600 * grade, grade - 1);
            }
            case BERSERK -> {
                buff(player, MobEffects.DAMAGE_BOOST, 450 * grade, Math.min(2, grade - 1));
                buff(player, MobEffects.MOVEMENT_SPEED, 450 * grade, 0);
            }
            case GALE -> {
                buff(player, MobEffects.MOVEMENT_SPEED, 600 * grade, Math.min(2, grade - 1));
                buff(player, MobEffects.JUMP, 600 * grade, Math.min(1, grade - 1));
            }
            case PROFOUND_WATER -> {
                buff(player, MobEffects.WATER_BREATHING, 1200 * grade, 0);
                buff(player, MobEffects.NIGHT_VISION, 1200 * grade, 0);
                buff(player, MobEffects.DIG_SPEED, 1200 * grade, Math.min(1, grade - 1));
            }
            case PHYSIQUE -> {
                PhysiqueType[] types = PhysiqueType.values();
                int end = Math.min(7, types.length);
                data.wuxingxiantu$setPhysique(types[player.getRandom().nextInt(end)]);
                PhysiqueLogic.apply(player, data);
                buff(player, MobEffects.REGENERATION, 200 * grade, Math.min(1, grade - 1));
            }
        }
        player.displayClientMessage(Component.translatable("message.wuxingxiantu.pill_applied", PillData.name(pill)), true);
        return true;
    }

    /** Compiled V5 effects enter here only after the server has reserved one real dose. */
    public static void applyCompiledDose(ServerPlayer player,cn.blockforge.generated.generatedmod.alchemy.effects.AbilityCompiler.Result result){
        var data=GeneratedMod.data(player);
        for(var e:result.effects())switch(e.id()){
            case heal -> player.heal(e.magnitudeMilli()/1000F);
            case mana -> CultivationLogic.restoreMana(data,e.magnitudeMilli()/1000F);
            case regeneration,speed -> cn.blockforge.generated.generatedmod.alchemy.effects.PillRuntime.schedule(player,e);
            case resistance -> cn.blockforge.generated.generatedmod.alchemy.effects.PillRuntime.finiteNative(player,MobEffects.DAMAGE_RESISTANCE,e.duration(),(int)(e.magnitudeMilli()/1000)-1);
            case shield -> cn.blockforge.generated.generatedmod.alchemy.effects.PillRuntime.finiteNative(player,MobEffects.ABSORPTION,e.duration(),(int)(e.magnitudeMilli()/4000)-1);
            case cleanse -> {player.removeEffect(MobEffects.POISON);player.removeEffect(MobEffects.CONFUSION);player.removeEffect(MobEffects.WEAKNESS);}
            case cultivation -> CultivationLogic.gainCultivation(player,data,e.magnitudeMilli()/1000);
            case minor_breakthrough -> CultivationLogic.advanceMinorStage(player,data);
            default -> cn.blockforge.generated.generatedmod.alchemy.effects.ExtendedPillEffects.apply(player,e);
        }
        if(result.toxicity()>0)player.addEffect(new MobEffectInstance(MobEffects.POISON,Math.max(20,result.toxicity()/2000),0));
    }

    private static void buff(Player player, MobEffect effect, int duration, int strength) {
        MobEffectInstance current = player.getEffect(effect);
        if (current != null) {
            if (current.isInfiniteDuration()) return;
            duration = Math.max(duration, current.getDuration());
            strength = Math.max(strength, current.getAmplifier());
        }
        player.addEffect(new MobEffectInstance(effect, duration, strength));
    }

    private PillEffects() {}
}
