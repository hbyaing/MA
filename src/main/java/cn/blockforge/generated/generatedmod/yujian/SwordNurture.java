package cn.blockforge.generated.generatedmod.yujian;

import cn.blockforge.generated.generatedmod.arts.ArtParticleLibrary;
import cn.blockforge.generated.generatedmod.network.packet.cultivation.ArtVisualPacket;

import cn.blockforge.generated.generatedmod.*;
import cn.blockforge.generated.generatedmod.items.SpiritSwordItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.*;

@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID)
public final class SwordNurture {
    private static final String SELECTED="WuxingSelectedSword";
    /** Equivalent to the mana used to condense the former single crystal. */
    public static int breakthroughCost() {
        return cn.blockforge.generated.generatedmod.items.CultivationLogic.CRYSTALLIZE_AURA_COST;
    }
    public static boolean breakthrough(ServerPlayer player,ItemStack sword) {
        var cultivation=GeneratedMod.data(player);
        if(cultivation==null||!player.isAlive()||player.isSpectator()
                ||!(sword.getItem() instanceof SpiritSwordItem spirit))return false;
        var nurture=SpiritSwordItem.nurture(sword);
        if(!nurture.ready(spirit.profile()))return false;
        int cost=breakthroughCost();float aura=cultivation.wuxingxiantu$getAura();
        if(!Float.isFinite(aura)||aura<cost) {
            player.displayClientMessage(net.minecraft.network.chat.Component.translatable(
                    "message.wuxingxiantu.sword_nurture_mana_missing",cost),true);
            return false;
        }
        cultivation.wuxingxiantu$setAura(aura-cost);
        nurture.nurtureExp=0;nurture.nurtureStage++;nurture.insightPoints++;
        SpiritSwordItem.saveNurture(sword,nurture);
        player.displayClientMessage(net.minecraft.network.chat.Component.translatable(
                "message.wuxingxiantu.sword_nurture_success",nurture.nurtureStage,cost),true);
        return true;
    }
    public static ItemStack current(ServerPlayer p) {
        var equipped=cn.blockforge.generated.generatedmod.robe.YujianSlot.activeFlyingSword(p);
        if(p.getVehicle() instanceof ControlledSwordEntity s && s.flightRoot())return equipped;
        if(p.getMainHandItem().getItem() instanceof SpiritSwordItem)return p.getMainHandItem();
        if(p.getPersistentData().hasUUID(SELECTED)) {
            UUID id=p.getPersistentData().getUUID(SELECTED);
            if(!equipped.isEmpty()&&id.equals(NurtureData.read(equipped).swordUuid))return equipped;
            for(int slot=0;slot<p.getInventory().getContainerSize();slot++) {
                var stack=p.getInventory().getItem(slot);
                if(stack.getItem() instanceof SpiritSwordItem && id.equals(NurtureData.read(stack).swordUuid))return stack;
            }
        }
        for(var stack:p.getInventory().items)if(stack.getItem() instanceof SpiritSwordItem)return stack;
        return equipped;
    }
    public static void select(ServerPlayer p,ItemStack stack) {
        var n=NurtureData.read(stack);n.write(stack,SwordProfile.of(stack));
        p.getPersistentData().putUUID(SELECTED,n.swordUuid);
    }
    public static boolean enabled(ItemStack stack,int mastery) {
        return !stack.isEmpty() && SwordProfile.of(stack).mastery(NurtureData.read(stack).nurtureStage,mastery);
    }
    public static void combat(ServerPlayer p,ItemStack stack,LivingEntity target) {
        if(!(stack.getItem() instanceof SpiritSwordItem) || !(target instanceof Enemy) || target==p || target.isAlliedTo(p))return;
        // 每个实际敌人、每把剑每五秒最多一次；无伤害、假人、友方不累计。
        var tag=target.getPersistentData();var n=NurtureData.read(stack);n.write(stack,SwordProfile.of(stack));
        String key="SwordNurtureHit_"+n.swordUuid;
        long now=p.level().getGameTime();
        if(tag.contains(key)&&now-tag.getLong(key)<100)return;
        tag.putLong(key,now);n.addExp(3,SwordProfile.of(stack));n.write(stack,SwordProfile.of(stack));
    }
    @SubscribeEvent public static void hit(net.minecraftforge.event.entity.living.LivingDamageEvent event) {
        if(event.getAmount()>0 && event.getSource().getDirectEntity() instanceof ServerPlayer p
                && event.getSource().getEntity()==p) {
            ItemStack stack = p.getMainHandItem().getItem() instanceof SpiritSwordItem ? p.getMainHandItem() : current(p);
            combat(p, stack, event.getEntity());
        }
    }
    public static boolean canCast(ServerPlayer p,ItemStack stack,String id) {
        var d=GeneratedMod.data(p);
        if(d==null||!SwordTechniqueRegistry.isTechnique(id)||stack!=current(p)||!p.isAlive()||p.isSpectator()
                ||!d.wuxingxiantu$getActiveDivineArt().isBlank()
                ||!ProgressionGate.canUseSwordTechnique(p,id,stack))return false;
        long now=p.level().getGameTime();
        return p.getPersistentData().getLong("SwordTechniqueCooldown_"+id)<=now
                && d.wuxingxiantu$getAura()>=ArtCatalog.finalManaCost(id,cn.blockforge.generated.generatedmod.items.CultivationLogic.maxAura(d));
    }
    public static boolean cast(ServerPlayer p,ItemStack stack,String id) {
        migrate(p,stack);
        if(!canCast(p,stack,id))return false;
        var d=GeneratedMod.data(p);var n=NurtureData.read(stack);
        if(!YujianController.executeTechnique(p,id,n.nurtureStage*10))return false;
        var start = p.position().add(0, 1.0, 0);
        var end = p.getEyePosition().add(p.getLookAngle().scale(Math.max(3.5D, ArtCatalog.attackProfile(id).range())));
        int mode = switch (id) {
            case "art_sword_pierce" -> ArtVisualPacket.PROJECTILE;
            case "art_sword_sweep" -> ArtVisualPacket.ARC;
            case "art_sword_guard" -> ArtVisualPacket.AURA;
            default -> ArtVisualPacket.AREA;
        };
        emitTechniqueVisuals(p, id, mode, start, end);
        // Preserve the existing Art Aura cost; only a successful release starts its per-player cooldown.
        d.wuxingxiantu$setAura(d.wuxingxiantu$getAura()-ArtCatalog.finalManaCost(id,cn.blockforge.generated.generatedmod.items.CultivationLogic.maxAura(d)));
        int cooldown=ArtCatalog.currentCooldownTicks(id,d.wuxingxiantu$getArtMastery(id));
        p.getPersistentData().putLong("SwordTechniqueCooldown_"+id,p.level().getGameTime()+cooldown);
        d.wuxingxiantu$addArtMastery(id,"nine_turns".equals(ProgressionGate.effectiveHeartMethod(p,d))?2:1);
        GeneratedMod.sync(p);return true;
    }

    /** Distinct readable silhouettes for the five sword techniques.  Damage remains server-side. */
    private static void emitTechniqueVisuals(ServerPlayer p, String id, int mode, net.minecraft.world.phys.Vec3 start,
                                             net.minecraft.world.phys.Vec3 end) {
        double range = Math.max(1.0D, ArtCatalog.attackProfile(id).width() > 0
                ? ArtCatalog.attackProfile(id).width() : ArtCatalog.attackProfile(id).range());
        ArtParticleLibrary.emit(p.serverLevel(), id, mode, start, end, range, 0);
        switch (id) {
            case "art_sword_pierce" -> {
                ArtParticleLibrary.emit(p.serverLevel(), id, ArtVisualPacket.TRAIL, start, end, 0.22D, 0);
                ArtParticleLibrary.emit(p.serverLevel(), id, ArtVisualPacket.IMPACT, end, end, 0.75D, 1);
            }
            case "art_sword_sweep" -> {
                ArtParticleLibrary.emit(p.serverLevel(), id, ArtVisualPacket.BOUNDARY, start, start, 3.2D, 0);
                ArtParticleLibrary.emit(p.serverLevel(), id, ArtVisualPacket.ARC, start.add(0, 0.6D, 0), end, 3.8D, 1);
            }
            case "art_sword_guard" -> {
                ArtParticleLibrary.emit(p.serverLevel(), id, ArtVisualPacket.BOUNDARY, start, start, 1.8D, 0);
                ArtParticleLibrary.emit(p.serverLevel(), id, ArtVisualPacket.TRAIL, start.add(0, 0.35D, 0),
                        start.add(0, 1.8D, 0), 0.16D, 1);
            }
            case "art_sword_array" -> {
                ArtParticleLibrary.emit(p.serverLevel(), id, ArtVisualPacket.BOUNDARY, end, end, 3.15D, 0);
                ArtParticleLibrary.emit(p.serverLevel(), id, ArtVisualPacket.IMPACT, end.add(0, 5.0D, 0),
                        end.add(0, 5.0D, 0), 1.1D, 1);
            }
            case "art_myriad_swords" -> {
                ArtParticleLibrary.emit(p.serverLevel(), id, ArtVisualPacket.BOUNDARY, end, end, 4.8D, 0);
                ArtParticleLibrary.emit(p.serverLevel(), id, ArtVisualPacket.AREA, end, end, 5.4D, 1);
                ArtParticleLibrary.emit(p.serverLevel(), id, ArtVisualPacket.IMPACT, end.add(0, 5.5D, 0),
                        end.add(0, 5.5D, 0), 1.4D, 1);
            }
            default -> { }
        }
    }
    public static void migrate(ServerPlayer p,ItemStack stack) {
        var d=GeneratedMod.data(p);if(d==null||!(stack.getItem() instanceof SpiritSwordItem spirit))return;
        spirit.initialize(stack);
        migratePlayer(p);
        boolean changed=false;
        var n=NurtureData.read(stack);
        if(!n.knowledgeMigrated) {
            for(String id:n.legacyUnlockedTechniques)changed|=d.wuxingxiantu$learnSwordTechnique(id);
            n.knowledgeMigrated=true;
            n.write(stack,spirit.profile());
            p.getInventory().setChanged();
        }
        if(changed)GeneratedMod.sync(p);
    }
    public static void migratePlayer(ServerPlayer p) {
        var d=GeneratedMod.data(p);if(d==null)return;
        var pending=p.getPersistentData().getCompound("SwordLegacyTechniques");
        boolean changed=false;
        for(var t:SwordTechniqueRegistry.all())if(pending.getBoolean(t.id())||d.wuxingxiantu$hasLearnedArt(t.id())) {
            changed|=d.wuxingxiantu$learnSwordTechnique(t.id());
        }
        p.getPersistentData().remove("SwordLegacyTechniques");
        String[] old=d.wuxingxiantu$getLearnedArts();
        String[] kept=Arrays.stream(old).filter(id->!SwordTechniqueRegistry.isTechnique(id)).toArray(String[]::new);
        if(kept.length!=old.length) {
            d.wuxingxiantu$setLearnedArts(kept);
            changed=true;
        }
        if(changed)GeneratedMod.sync(p);
    }
}
