package cn.blockforge.generated.generatedmod.boss.combat;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.boss.VisualBossEntity;
import cn.blockforge.generated.generatedmod.mindscape.MindscapeManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Existing player packets supply all interactions; nodes cannot be activated across sessions. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID)
public final class BossCombatEvents {
    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void discardLoadedMechanics(net.minecraftforge.event.entity.EntityJoinLevelEvent event) {
        if(event.getLevel().isClientSide()||!event.loadedFromDisk())return;
        var tag=event.getEntity().getPersistentData();
        // Timelines are transient; their saved targets/echoes must not outlive a restarted encounter.
        if(tag.hasUUID(BossCombatController.PARENT)||tag.hasUUID("BossEchoParent"))event.setCanceled(true);
    }
    public static BossCombatController encounter(ServerPlayer player) {
        if(player.level().dimension()!=MindscapeManager.DIMENSION||GeneratedMod.data(player)==null)return null;
        var tag=GeneratedMod.data(player).wuxingxiantu$getMindscapeData();
        if(!tag.hasUUID("boss"))return null;
        var entity=player.serverLevel().getEntity(tag.getUUID("boss"));
        return entity instanceof VisualBossEntity visual?visual.combat()
                :entity instanceof cn.blockforge.generated.generatedmod.HeartDemonEntity heart?heart.combat():null;
    }
    public static boolean anchor(ServerPlayer p,BlockPos pos){var c=encounter(p);return c!=null&&c.anchor(pos,p);}
    public static boolean interact(ServerPlayer p,Entity node){
        if(node.level()!=p.level()||!node.getPersistentData().hasUUID(BossCombatController.PARENT))return false;
        var parent=p.serverLevel().getEntity(node.getPersistentData().getUUID(BossCombatController.PARENT));
        var combat=parent instanceof VisualBossEntity boss?boss.combat()
                :parent instanceof cn.blockforge.generated.generatedmod.HeartDemonEntity heart?heart.combat():parent instanceof ServerPlayer caster?BossPlayerSkills.encounter(caster):null;
        return combat!=null&&combat.interactNode(node,p);
    }
    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void useNode(net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract event) {
        if(!event.getTarget().getPersistentData().hasUUID(BossCombatController.PARENT))return;
        event.setCanceled(true);
        if(event.getEntity() instanceof ServerPlayer player)interact(player,event.getTarget());
    }
    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void useNodeAt(net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteractSpecific event) {
        if(!event.getTarget().getPersistentData().hasUUID(BossCombatController.PARENT))return;
        event.setCanceled(true);
        if(event.getEntity() instanceof ServerPlayer player)interact(player,event.getTarget());
    }
    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void attack(AttackEntityEvent event) {
        if(event.getTarget().getPersistentData().hasUUID(BossCombatController.PARENT)) {
            event.setCanceled(true);
            if(event.getEntity() instanceof ServerPlayer player)interact(player,event.getTarget());
        }else if(event.getEntity() instanceof ServerPlayer player&&BossPlayerSkills.profile(BossPlayerSkills.activeForm(player))!=null){
            event.setCanceled(true);var profile=BossPlayerSkills.profile(BossPlayerSkills.activeForm(player));BossPlayerSkills.cast(player,profile.id()+"_basic");
        }
    }
    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void nodeDamage(LivingAttackEvent event) {
        if(!event.getEntity().getPersistentData().hasUUID(BossCombatController.PARENT))return;
        event.setCanceled(true);
        if(event.getSource().getEntity() instanceof ServerPlayer player)interact(player,event.getEntity());
    }
    private BossCombatEvents(){}
}
