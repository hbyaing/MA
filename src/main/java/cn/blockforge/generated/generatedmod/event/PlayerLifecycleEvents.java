package cn.blockforge.generated.generatedmod.event;

import cn.blockforge.generated.generatedmod.items.CultivationLogic;
import cn.blockforge.generated.generatedmod.items.PhysiqueLogic;
import cn.blockforge.generated.generatedmod.items.GuandingItem;
import cn.blockforge.generated.generatedmod.items.SpiritSwordItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import cn.blockforge.generated.generatedmod.*;
import static cn.blockforge.generated.generatedmod.GeneratedMod.*;

@Mod.EventBusSubscriber(modid = GeneratedMod.MOD_ID)
public final class PlayerLifecycleEvents {

    @SubscribeEvent public static void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) event.addCapability(CULTIVATION_CAP, new CultivationCapability.Provider());
    }

    @SubscribeEvent public static void clonePlayer(PlayerEvent.Clone event) {
        event.getEntity().getPersistentData().putLong("WuxingCompassTeleportUntil",
                event.getOriginal().getPersistentData().getLong("WuxingCompassTeleportUntil"));
        event.getEntity().getPersistentData().putDouble("WuxingRootTrainingRemainder",
                event.getOriginal().getPersistentData().getDouble("WuxingRootTrainingRemainder"));
        event.getOriginal().reviveCaps();
        event.getOriginal().getCapability(CultivationCapability.INSTANCE).ifPresent(oldData ->
                event.getEntity().getCapability(CultivationCapability.INSTANCE).ifPresent(newData -> {
                    newData.deserializeNBT(oldData.serializeNBT());
                    boolean mindscapeSnapshot = oldData.wuxingxiantu$getMindscapeData().getCompound("external_snapshot").getBoolean("valid")
                            && !oldData.wuxingxiantu$getMindscapeData().getCompound("external_snapshot").getBoolean("restored");
                    if(event.isWasDeath()&&!mindscapeSnapshot&&!event.getOriginal().isSpectator()
                            &&!event.getOriginal().level().getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_KEEPINVENTORY)){
                        newData.wuxingxiantu$setRobeStack(ItemStack.EMPTY);
                        newData.wuxingxiantu$setYujianStack(ItemStack.EMPTY);
                    }
                    if(!mindscapeSnapshot) oldData.wuxingxiantu$setYujianStack(ItemStack.EMPTY);
                }));
        event.getOriginal().invalidateCaps();
    }

    @SubscribeEvent public static void playerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if(event.getEntity() instanceof ServerPlayer server)cn.blockforge.generated.generatedmod.network.CultivationSync.forget(server);
        cn.blockforge.generated.generatedmod.network.service.CompassService.clear(event.getEntity().getUUID());
        if (event.getEntity() instanceof ServerPlayer player) {
            GuandingItem.interrupt(player);
            Network.clearRefining(player);
            DivineArtLogic.clear(player);
             TimeStopManager.clear(player);
            CultivationLogic.clearDomainEffects(player.serverLevel(), player);
        }
    }

    @SubscribeEvent public static void playerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            cn.blockforge.generated.generatedmod.network.CultivationSync.forget(player);
            CultivationData data = data(player);
            if (data != null) {
                CultivationLogic.applyHealthBonus(player, data);
                restoreDimensionHealth(player);
            }
            cn.blockforge.generated.generatedmod.realmflight.RealmFlightControl.transition(player);
            sync(player);
        }
    }

    @SubscribeEvent public static void beforeDimension(net.minecraftforge.event.entity.EntityTravelToDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if(TribulationManager.isActive(player)) {
                event.setCanceled(true);
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("天劫未尽，不可遁界。"),true);
                return;
            }
            player.getPersistentData().putFloat("wuxingxiantu_dimension_health", player.getHealth());
            player.getPersistentData().putFloat("wuxingxiantu_dimension_max_health", player.getMaxHealth());
        }
    }

    @SubscribeEvent public static void afterDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if(event.getEntity() instanceof ServerPlayer player)TribulationManager.dimensionChanged(player);
        if(event.getEntity() instanceof ServerPlayer server)cn.blockforge.generated.generatedmod.network.CultivationSync.forget(server);
        if(event.getEntity() instanceof ServerPlayer server)Network.clearRefining(server);
        cn.blockforge.generated.generatedmod.network.service.CompassService.clear(event.getEntity().getUUID());
        if (event.getEntity() instanceof ServerPlayer player) {
            CultivationData data = data(player);
            if (data != null) {
                CultivationLogic.applyHealthBonus(player, data);
                restoreDimensionHealth(player);
            }
            cn.blockforge.generated.generatedmod.realmflight.RealmFlightControl.transition(player);
            sync(player);
        }
    }

    private static void restoreDimensionHealth(ServerPlayer player) {
        var tag = player.getPersistentData();
        if (!tag.contains("wuxingxiantu_dimension_health")) return;
        float oldHealth = tag.getFloat("wuxingxiantu_dimension_health");
        float oldMax = tag.getFloat("wuxingxiantu_dimension_max_health");
        tag.remove("wuxingxiantu_dimension_health");
        tag.remove("wuxingxiantu_dimension_max_health");
        if (oldMax > 0.0F && oldHealth > 0.0F)
            player.setHealth(Math.min(player.getMaxHealth(), oldHealth / oldMax * player.getMaxHealth()));
    }

    @SubscribeEvent public static void playerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            TribulationManager.restore(player);
            cn.blockforge.generated.generatedmod.network.service.AlchemyService.deliverRefund(player);
            CultivationData data = data(player);
            if (data != null) {
                // 寄魄目标不跨服务器重启保存；登录时清理残留状态，避免无目标旁观。
                String activeDivine = data.wuxingxiantu$getActiveDivineArt();
                if ("yuan_shen_chu_qiao".equals(activeDivine)||"fa_tian_xiang_di".equals(activeDivine)
                        ||"yi_hun_ji_po".equals(activeDivine) || "qi_shi_er_bian".equals(activeDivine)
                        || "jin_dou_yun".equals(activeDivine) || "ri_yue_ting_lun".equals(activeDivine)) {
                    DivineArtLogic.clear(player);
                }
                // 罡斗星阵是进行中的施法，跨会话残留会把异常状态带回每次登录，直接清空。
                if (data.wuxingxiantu$getDivineStepOrigin() != null
                        || data.wuxingxiantu$getDivineStepIndex() != 0) {
                    data.wuxingxiantu$setDivineStepOrigin(null);
                    data.wuxingxiantu$setDivineStepIndex(0);
                }
                PlayerInitialization.initializeIfNeeded(data, player.getRandom());
                // 迁移旧版只用 WEAKNESS 表示的经脉寸断状态。
                if (data.wuxingxiantu$getMeridianStatus() == 0
                        && player.hasEffect(net.minecraft.world.effect.MobEffects.WEAKNESS)) {
                    data.wuxingxiantu$setMeridianStatus(2);
                }
                PhysiqueLogic.apply(player, data);
                CultivationLogic.applyHealthBonus(player, data);
                restoreDimensionHealth(player);
            }
            boolean selectedSword = false;
            cn.blockforge.generated.generatedmod.yujian.SwordNurture.migratePlayer(player);
            for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
                var stack = player.getInventory().getItem(slot);
                if (stack.getItem() instanceof cn.blockforge.generated.generatedmod.items.SpiritSwordItem) {
                    cn.blockforge.generated.generatedmod.yujian.SwordNurture.migrate(player, stack);
                    if (!selectedSword) cn.blockforge.generated.generatedmod.yujian.SwordNurture.select(player, stack);
                    selectedSword = true;
                }
            }
            sync(player);
        }
    }
    private PlayerLifecycleEvents() {}
}
