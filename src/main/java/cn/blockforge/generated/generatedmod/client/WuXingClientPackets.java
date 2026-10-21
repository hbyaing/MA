package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.ArtCatalog;
import cn.blockforge.generated.generatedmod.ClientPacketSink;
import cn.blockforge.generated.generatedmod.CultivationData;
import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.ExoticFireState;
import cn.blockforge.generated.generatedmod.PhysiqueType;
import net.minecraft.core.BlockPos;
import java.util.HashMap;
import java.util.Map;
import cn.blockforge.generated.generatedmod.items.CultivationLogic;
import cn.blockforge.generated.generatedmod.items.PhysiqueLogic;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.Item;

/** 把各 PLAY_TO_CLIENT 包的真实客户端处理逻辑装进桥接类；仅由客户端初始化路径调用。 */
public final class WuXingClientPackets {
    private WuXingClientPackets() {}

    public static void register() {
        ClientPacketSink.furnaceLoaded=cn.blockforge.generated.generatedmod.client.furnacevolume.HostAdapter::track;
        ClientPacketSink.manaLossTracking=enabled->{var p=Minecraft.getInstance().player;if(p!=null&&GeneratedMod.data(p) instanceof cn.blockforge.generated.generatedmod.CultivationDataImpl data)data.setManaLossEnabled(enabled);};
        ClientPacketSink.manaLoss=rows->{var player=Minecraft.getInstance().player;if(player!=null&&GeneratedMod.data(player) instanceof cn.blockforge.generated.generatedmod.CultivationDataImpl data)data.receiveManaLossRows(rows);};
        ClientPacketSink.tribulationVisual=cn.blockforge.generated.generatedmod.client.tribulation.TribulationEffects::accept;
        ClientPacketSink.dipperVisualPacket=DipperRitualEffects::accept;
        ClientPacketSink.weaponFlameHit=cn.blockforge.generated.generatedmod.client.weaponflame.WeaponFlameHitEffects::accept;
        ClientPacketSink.heartForgeContract=HeartForgeClientState::accept;
        ClientPacketSink.heartForgeVisible=HeartForgeClientState::visible;
        ClientPacketSink.flightSnapshot=packet->{
            var level=Minecraft.getInstance().level;
            if(level!=null&&level.getEntity(packet.entityId()) instanceof cn.blockforge.generated.generatedmod.yujian.ControlledSwordEntity sword)
                sword.acceptSnapshot(packet);
        };
        ClientPacketSink.mindscapeState=MindscapeClient::accept;
        ClientPacketSink.robeSync=RobeClient::accept;
        ClientPacketSink.syncPacket = WuXingClientPackets::handleSync;
        ClientPacketSink.cultivationVitals = p->{
            var data=localData();if(data==null)return;
            data.wuxingxiantu$setMaxMana(p.maxMana());data.wuxingxiantu$setCurrentMana(p.currentMana());
            data.wuxingxiantu$setBiguRemainTime(p.bigu());data.wuxingxiantu$restoreCultivation(p.cultivation());data.wuxingxiantu$setMerit(p.merit());
        };
        ClientPacketSink.openScreenPacket = WuXingClientPackets::handleOpenScreen;
        ClientPacketSink.tribulationStatePacket = packet -> WuXingClient
                .setTribulationState(packet.countdown(), packet.active(), packet.heartDemon());
        ClientPacketSink.soulTradeStatePacket = packet -> SoulTradeScreen
                .receiveFavoriteState(packet.containerId(), packet.favorites(), packet.resetScroll());
        ClientPacketSink.stopUsingItemPacket = WuXingClientPackets::handleStopUsingItem;
        ClientPacketSink.bannerStatePacket = WuXingClientPackets::handleBannerState;
        ClientPacketSink.divineArtBannerPacket = packet -> DivineArtBanner.play(packet.divineId());
        ClientPacketSink.fireSyncPacket = WuXingClientPackets::handleFireSync;
        ClientPacketSink.fireStatePacket = WuXingClientPackets::handleFireState;
        ClientPacketSink.exoticBurnPacket = WuXingClientPackets::handleExoticBurn;
        ClientPacketSink.wrapVisualPacket = packet -> ExoticFireOrbitRenderer.update(
                packet.entityId(), packet.tier(), packet.level());
        ClientPacketSink.divineSyncPacket = WuXingClientPackets::handleDivineSync;
        ClientPacketSink.cloudVisualPacket = packet -> JindouCloudRenderState.update(packet.player(), packet.visible());
        ClientPacketSink.openTransformSelectionPacket = packet -> {
            Minecraft client = Minecraft.getInstance();
            if (client.player != null && !(client.screen instanceof TransformSelectionScreen)) {
                client.setScreen(new TransformSelectionScreen());
            }
        };
        ClientPacketSink.compassResultPacket = packet -> {
            Minecraft client = Minecraft.getInstance();
            if (client.screen instanceof CreatureCompassScreen screen) {
                screen.receiveResult(packet.name(), packet.distance(), packet.dx(), packet.dy(), packet.dz());
            }
        };
        ClientPacketSink.heartSwordVisualPacket = HeartSwordClientEffects::accept;
    }

    private static CultivationData localData() {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return null;
        return GeneratedMod.data(client.player);
    }

    private static void handleSync(cn.blockforge.generated.generatedmod.network.packet.cultivation.SyncPacket p) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;
        CultivationData data = GeneratedMod.data(client.player);
        if (data == null) return;
        if(data instanceof cn.blockforge.generated.generatedmod.CultivationDataImpl impl)
            impl.setExoticFireHurtsNonHostile(p.exoticFireHurtsNonHostile());
        data.wuxingxiantu$setRealm(p.realm());
        data.wuxingxiantu$setGhostImmortal(p.ghostImmortal());
        data.wuxingxiantu$setYinShenPurity(p.yinShenPurity());
        data.wuxingxiantu$setCultivationStage(p.cultivationStage());
        data.wuxingxiantu$restoreDaoXin(p.daoXin());
        data.wuxingxiantu$restoreCultivation(p.cultivation());
        data.wuxingxiantu$setHeartForgeContract(p.heartForgeContract());
        data.wuxingxiantu$setMerit(p.merit());
        data.wuxingxiantu$setRootMask(p.rootMask());
        for (int i = 0; i < 5; i++) data.wuxingxiantu$setRootQuality(i, p.rootQualities()[i]);
        data.wuxingxiantu$setChaosRoot(p.chaosRoot());
        data.wuxingxiantu$setBiguRemainTime(p.biguRemainTime());
        data.wuxingxiantu$setMeditating(p.meditating());
        data.wuxingxiantu$setMeridianStatus(p.meridianStatus());
        data.wuxingxiantu$setWoodDomainEnabled(p.woodDomainEnabled());
        data.wuxingxiantu$setFireDomainEnabled(p.fireDomainEnabled());
        data.wuxingxiantu$setGoldDomainEnabled(p.goldDomainEnabled());
        data.wuxingxiantu$setWaterDomainEnabled(p.waterDomainEnabled());
        data.wuxingxiantu$setEarthDomainEnabled(p.earthDomainEnabled());
        data.wuxingxiantu$setThunderStrikes(p.thunderStrikes());
        data.wuxingxiantu$setThunderRoot(p.thunderUnlocked());
        data.wuxingxiantu$setExoticRoot(p.exoticUnlocked());
        // 放在 setRootMask 之后，避免“失去基础灵根自动回退本相”的守卫把同步值误清。
        data.wuxingxiantu$setThunderRootActive(p.thunderActive());
        data.wuxingxiantu$setExoticRootActive(p.exoticActive());
        if (p.physique() >= 0 && p.physique() < PhysiqueType.values().length)
            data.wuxingxiantu$setPhysique(PhysiqueType.values()[p.physique()]);
        // 体质先落地，再写入服务端权威上限，避免客户端 setter 用旧体质短暂覆盖同步值。
        data.setMaxMana(Math.max(0.0F, p.maxMana()));
        data.setCurrentMana(Math.min(Math.max(0.0F, p.maxMana()), Math.max(0.0F, p.currentMana())));
        data.wuxingxiantu$setPhysiqueLocked(false);
        data.wuxingxiantu$setEquippedHeartMethod(p.heartMethod());
        data.wuxingxiantu$setLearnedHeartMethods(p.learnedHeartMethods());
        // 境界和体质属性需要在客户端同步后重刷，否则内视面板会一直读到原版1.0攻击力。
        PhysiqueLogic.apply(client.player, data);
        CultivationLogic.applyHealthBonus(client.player, data);
        data.wuxingxiantu$setLearnedArts(p.learnedArts());
        data.wuxingxiantu$setKnownSwordTechniques(p.knownSwordTechniques());
        for (int i = 0; i < p.artMastery().length && i < ArtCatalog.ENTRIES.size(); i++)
            data.wuxingxiantu$setArtMastery(ArtCatalog.ENTRIES.get(i).id(), p.artMastery()[i]);
        for (int i = 0; i < 8; i++) data.wuxingxiantu$clearEquippedArt(i);
        String[] arts = p.arts();
        for (int i = 0; i < 8 && i < arts.length; i++) if (arts[i] != null) data.wuxingxiantu$equipArt(i, arts[i]);
        for (int i = 0; i < 3; i++) data.wuxingxiantu$clearPassiveArt(i);
        for (int i = 0; i < 3 && i < p.passiveArts().length; i++) if (p.passiveArts()[i] != null)
            data.wuxingxiantu$equipPassiveArt(i, p.passiveArts()[i]);
    }

    private static void handleOpenScreen(cn.blockforge.generated.generatedmod.network.packet.cultivation.OpenScreenPacket p) {
        // screen=1（炼丹炉）已改为服务端直接 NetworkHooks.openScreen 打开容器菜单，
        // 不再经由本包；这里只保留其余独立界面。
        net.minecraft.client.gui.screens.Screen screen = switch (p.screen()) {
            case 2 -> new RefiningForgeScreen(p.pos());
            case 3 -> new CreatureCompassScreen();
            default -> null;
        };
        if (screen != null) Minecraft.getInstance().setScreen(screen);
    }

    private static void handleStopUsingItem(cn.blockforge.generated.generatedmod.network.packet.cultivation.StopUsingItemPacket p) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;
        if (client.player.isUsingItem()) client.player.stopUsingItem();
        if (p.itemId() >= 0) {
            Item item = Item.byId(p.itemId());
            if (item != null) {
                if (p.cooldownTicks() > 0) client.player.getCooldowns().addCooldown(item, p.cooldownTicks());
                else client.player.getCooldowns().removeCooldown(item);
            }
        }
    }

    private static void handleBannerState(cn.blockforge.generated.generatedmod.network.packet.cultivation.BannerStatePacket p) {
        HumanEmperorBannerScreen.receiveState(p.containerId(), p.types(), p.counts(), p.active(), p.dead(),
                p.modes(), p.names(), p.myriadTypes(), p.myriadCounts(), p.myriadActive(), p.myriadDead(),
                p.myriadModes(), p.myriadNames(), p.transitReady(), p.soulPower(), p.combatMode());
    }

    private static void handleFireSync(cn.blockforge.generated.generatedmod.network.packet.cultivation.FireSyncPacket p) {
        CultivationData data = localData();
        if (data == null) return;
        data.wuxingxiantu$setLearnedFires(p.learned());
        data.wuxingxiantu$setEquippedFire(p.equipped());
    }

    private static void handleExoticBurn(cn.blockforge.generated.generatedmod.network.packet.cultivation.ExoticBurnPacket packet) {
        ExoticBurnClientState.accept(packet);
    }

    private static void handleFireState(cn.blockforge.generated.generatedmod.network.packet.cultivation.FireStatePacket packet) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null) return;
        Map<BlockPos, Integer> snapshot = new HashMap<>();
        for (int i = 0; i < packet.dx().length; i++) {
            BlockPos pos = new BlockPos(packet.dx()[i], packet.dy()[i], packet.dz()[i]);
            int tier = packet.tiers()[i];
            // 状态包可能先于区块方块包到达；不能因客户端暂时仍是空气而丢弃首帧颜色。
            if (tier >= 1 && tier <= 9) {
                snapshot.put(pos, tier);
            }
        }
        for (BlockPos pos : ExoticFireState.replaceClient(client.level, snapshot)) {
            client.levelRenderer.setBlocksDirty(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ());
        }
    }

    private static void handleDivineSync(cn.blockforge.generated.generatedmod.network.packet.divineart.DivineSyncPacket p) {
        CultivationData data = localData();
        if (data == null) return;
        data.wuxingxiantu$setLearnedDivineArts(p.learned());
        String[] equipped = p.equipped();
        for (int i = 0; i < 3; i++) {
            data.wuxingxiantu$setEquippedDivineArt(i, i < equipped.length ? equipped[i] : null);
        }
        data.wuxingxiantu$setActiveDivineArt(p.active());
        if (!"qi_shi_er_bian".equals(p.active()) && Minecraft.getInstance().player != null) {
            ModClientEvents.ClientForgeEvents.clearDisguise(Minecraft.getInstance().player.getUUID());
        }
        data.wuxingxiantu$setDivineStepIndex(p.step());
        data.wuxingxiantu$setPossessedForms(p.possessedForms());
        data.wuxingxiantu$setActivePossessedForm(p.activePossessedForm());
    }
}
