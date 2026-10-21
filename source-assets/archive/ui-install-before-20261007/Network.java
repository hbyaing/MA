package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.items.CultivationLogic;
import cn.blockforge.generated.generatedmod.items.HumanEmperorBannerItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import cn.blockforge.generated.generatedmod.network.packet.divineart.DivineSyncPacket;
import cn.blockforge.generated.generatedmod.network.packet.divineart.CloudVisualPacket;
import cn.blockforge.generated.generatedmod.network.packet.cultivation.FireSyncPacket;
import cn.blockforge.generated.generatedmod.network.packet.cultivation.WrapVisualPacket;
import cn.blockforge.generated.generatedmod.network.packet.cultivation.ExoticBurnPacket;
import cn.blockforge.generated.generatedmod.network.packet.cultivation.FireStatePacket;
import cn.blockforge.generated.generatedmod.network.packet.divineart.OpenTransformSelectionPacket;
import cn.blockforge.generated.generatedmod.network.packet.divineart.DivineArtBannerPacket;
import cn.blockforge.generated.generatedmod.network.packet.cultivation.BannerStatePacket;
import cn.blockforge.generated.generatedmod.network.packet.cultivation.StopUsingItemPacket;
import cn.blockforge.generated.generatedmod.network.packet.yujian.SwordFlightStatePacket;
import cn.blockforge.generated.generatedmod.network.packet.yujian.SwordFlightInputPacket;
import cn.blockforge.generated.generatedmod.network.packet.cultivation.TribulationStatePacket;
import cn.blockforge.generated.generatedmod.network.packet.cultivation.OpenScreenPacket;














public final class Network {
    // Input snapshots and flight state require matching client/server builds.
    public static final SimpleChannel CHANNEL = cn.blockforge.generated.generatedmod.network.NetworkChannel.CHANNEL;
    public static void register() { cn.blockforge.generated.generatedmod.network.NetworkChannel.register(); }
    /** 寻真司南最近一次搜索到的目标：供“传送”请求使用，避免客户端伪造坐标。 */

    private Network() {}

    /** 需要服务端准星命中生灵的生灵技能（无目标时给出明确提示）。 */
    public static boolean isLookTargetAbility(String formId) {
        return "minecraft:warden".equals(formId) || "minecraft:guardian".equals(formId)
                || "minecraft:elder_guardian".equals(formId) || "minecraft:shulker".equals(formId)
                || "minecraft:illusioner".equals(formId);
    }

    public static void sendToPlayer(ServerPlayer player, Object packet) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void sendSwordFlightState(ServerPlayer player, boolean active) {
        sendToPlayer(player, new SwordFlightStatePacket(active));
    }

    public static void sendBannerState(ServerPlayer player, HumanEmperorBannerMenu menu) {
        HumanEmperorBannerMenu.BannerState state = menu.state();
        BannerSoulArrays banner = BannerSoulArrays.from(state.souls());
        BannerSoulArrays myriad = BannerSoulArrays.from(state.myriadSouls());
        sendToPlayer(player, new BannerStatePacket(menu.containerId,
                banner.types(), banner.counts(), banner.active(), banner.dead(), banner.modes(), banner.names(),
                myriad.types(), myriad.counts(), myriad.active(), myriad.dead(), myriad.modes(), myriad.names(),
                menu.isTransitReady(), state.soulPower(), state.combatMode()));
    }

    private record BannerSoulArrays(String[] types, int[] counts, int[] active, int[] dead, int[] modes, String[] names) {
        private static BannerSoulArrays from(List<HumanEmperorBannerItem.SoulView> souls) {
            String[] types = new String[souls.size()];
            int[] counts = new int[souls.size()];
            int[] active = new int[souls.size()];
            int[] dead = new int[souls.size()];
            int[] modes = new int[souls.size()];
            String[] names = new String[souls.size()];
            for (int i = 0; i < souls.size(); i++) {
                HumanEmperorBannerItem.SoulView soul = souls.get(i);
                types[i] = soul.entityType();
                counts[i] = soul.count();
                active[i] = soul.active();
                dead[i] = soul.dead();
                modes[i] = soul.combatMode();
                names[i] = soul.displayName();
            }
            return new BannerSoulArrays(types, counts, active, dead, modes, names);
        }
    }

    /** 同步玩家身上异火缠绕的环绕火焰视觉（品阶 0 表示关闭），供客户端渲染环绕粒子。 */
    public static void sendWrapVisual(ServerPlayer player, int tier, int level) {
        if (!(player.level() instanceof ServerLevel)) return;
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                new WrapVisualPacket(player.getId(), tier, level));
    }

    /** 同步玩家附近的异火品阶，让客户端的原版火焰模型能按位置染色。 */
    private record SentFire(net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension,
                            Map<BlockPos,Integer> snapshot) {}
    private static final Map<ServerPlayer,SentFire> LAST_FIRE_STATE=new java.util.WeakHashMap<>();
    public static void sendFireState(ServerPlayer player) {
        Map<BlockPos, Integer> snapshot = ExoticFireState.visibleSnapshot(player, 20);
        var old=LAST_FIRE_STATE.get(player);
        if(old!=null&&old.dimension().equals(player.level().dimension())&&old.snapshot().equals(snapshot))return;
        LAST_FIRE_STATE.put(player,new SentFire(player.level().dimension(),Map.copyOf(snapshot)));
        sendToPlayer(player, FireStatePacket.from(snapshot));
    }

    /** 点燃后立即同步，避免等待定时快照造成首帧仍使用原版火焰颜色。 */
    public static void sendFireStateAround(ServerLevel level, BlockPos firePos) {
        for (ServerPlayer player : level.players()) {
            if (player.blockPosition().distSqr(firePos) <= 20.0D * 20.0D) sendFireState(player);
        }
    }

    /** Replaces the cloud particle broadcast; carries no movement or authoritative divine state. */
    public static void sendCloudVisual(ServerPlayer player, boolean visible) {
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                new CloudVisualPacket(player.getUUID(), visible));
    }

    public static void sendDivineSync(ServerPlayer player) {
        CultivationData data = GeneratedMod.data(player);
        if (data == null) return;
        sendToPlayer(player, new DivineSyncPacket(data.wuxingxiantu$getLearnedDivineArts(),
                data.wuxingxiantu$getEquippedDivineArts(), data.wuxingxiantu$getActiveDivineArt(), data.wuxingxiantu$getDivineStepIndex(),
                data.wuxingxiantu$getPossessedForms(), data.wuxingxiantu$getActivePossessedForm()));
        sendToPlayer(player, new FireSyncPacket(data.wuxingxiantu$getLearnedFires(), data.wuxingxiantu$getEquippedFire()));
    }

    public static void stopUsingItem(ServerPlayer player, net.minecraft.world.item.Item item, int cooldownTicks) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new StopUsingItemPacket(
                item == null ? -1 : net.minecraft.world.item.Item.getId(item), Math.max(0, cooldownTicks)));
    }

    /** 神通发动成功后通知施术者客户端播放短暂提示。 */
    public static void showDivineArtBanner(ServerPlayer player, String divineArtId) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new DivineArtBannerPacket(divineArtId));
    }

    /** 七十二变发动后打开形态选择界面。 */
    public static void sendOpenTransformSelection(ServerPlayer player) {
        sendToPlayer(player, new OpenTransformSelectionPacket());
    }

    public static void sendOpenRefining(ServerPlayer player, BlockPos pos) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new OpenScreenPacket(2, pos, 0));
    }

    /** 寻真司南：请求客户端打开搜索界面。 */
    public static void sendOpenCompass(ServerPlayer player) {
        sendToPlayer(player, new OpenScreenPacket(3, BlockPos.ZERO, 0));
    }

    /** 在服务端 tick 中推进炼丹进度，避免客户端伪造完成结果。 */

    /** 炉子被破坏时取消绑定的炼丹任务，避免任务完成后向已不存在的炉体发放视觉事件。 */

    /** 停服前清理所有未完成任务，避免重载后炼器台保留亮态。 */

    /** 变异上位灵根形态切换：form 0 = 木灵根 ⇄ 天雷灵根（领域随之切换），form 1 = 火灵根 ⇄ 异火灵根。 */

    /** 从炼器台界面领取已炼成的成品（原右键领取途径作废）。 */

    /** Legacy toggle bridge; hold/release input uses SwordFlightInputPacket. */

    /** Compact client input snapshot; the server owns movement, collision, and damage. */

    /** 养剑界面请求；服务端只接受玩家明确提供的灵剑槽位与材料槽位。 */

    public static void toggleDomain(Supplier<NetworkEvent.Context> supplier, boolean enabled, int root, String name) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;
            CultivationData data = GeneratedMod.data(player);
            if (data == null || (data.wuxingxiantu$getRootMask() & (1 << root)) == 0
                    || data.wuxingxiantu$getRootQuality(root) < 2) return;
            switch (root) {
                case 0 -> data.wuxingxiantu$setGoldDomainEnabled(enabled);
                case 2 -> data.wuxingxiantu$setWaterDomainEnabled(enabled);
                case 4 -> data.wuxingxiantu$setEarthDomainEnabled(enabled);
                default -> { return; }
            }
            if (player.level() instanceof ServerLevel level) CultivationLogic.clearDomainEffects(level, player);
            CultivationLogic.refreshDomainBuffs(player, data);
            player.displayClientMessage(Component.translatable("message.wuxingxiantu." + name
                    + "_domain_" + (enabled ? "enabled" : "disabled")), true);
            GeneratedMod.sync(player);
        });
        context.setPacketHandled(true);
    }

    /** 每秒同步渡劫倒计时、活动状态和心魔状态，客户端只负责表现。 */
    public static void sendTribulationState(ServerPlayer player) {
        int countdown = TribulationManager.countdown(player);
        boolean active = TribulationManager.isActive(player);
        boolean demon = cn.blockforge.generated.generatedmod.mindscape.MindscapeManager.hasBoundDemon(player);
        sendToPlayer(player, new TribulationStatePacket(countdown, active, demon));
    }

    /** 客户端请求切换当前拘魂交易的收藏状态；服务端以当前菜单和真实交易索引为准。 */

    /** 服务端同步排序后的每条交易是否收藏，并通知客户端是否重置滚动位置。 */

    /** 服务端拒绝或完成丹药使用时，显式结束客户端的预测进食动画。 */

    /** 七十二变主动能力请求：客户端只发送按键意图，形态、目标与冷却全部由服务端决定。 */

    /** 七十二变苦力怕：松开 Y 后引爆并保持苦力怕形态。 */

    /** 七十二变界面选择一个已记录的生灵形态；索引由服务端重新校验。 */

    /** 关闭七十二变形态选择界面时取消尚未确认的发动。 */

    /** 服务端通知客户端打开七十二变形态选择界面。 */

    /** 玩家附近原版火焰的异火品阶快照；坐标使用相对玩家的有符号短整型，降低同步开销。 */

    /** 同步被异火缠绕点燃的实体，让客户端可以渲染动态异火火焰贴图。 */

    /** 玩家防具异火缠绕的环绕火焰视觉状态；tier 为 0 时表示当前应关闭效果。 */

    /** 符纸画布位集字节数组长度。 */
    public static final int CANVAS_BIT_BYTES =
            (TalismanCatalog.CANVAS_WIDTH * TalismanCatalog.CANVAS_HEIGHT + 7) / 8;

    /**
     * 符箓提交包（服务端处理）：玩家在镌印台/绘制台提交临摹好的符纸。
     * {@code bits} 为 CANVAS_WIDTH×CANVAS_HEIGHT 画布的位集字节数组；服务端据此评分分级。
     */

    /** 拓印台批量复制包（服务端处理）：一次把当前印章复制出 {@code copies} 张符咒。 */

    /**
     * 符箓平台翻页包（服务端处理）：镌印台 / 绘制台切换符箓时发送，
     * 把当前页码记在方块实体里，下次再开界面就能接着上次那一页，不必从头翻。
     */

    /** 寻真司南打开请求（服务端处理）：物品右键时发送，服务端回发客户端打开界面。 */

    /** 寻真司南传送包（服务端处理）：把玩家送到最近一次搜索到的生物附近。 */

    /** 寻真司南搜索包（服务端处理）：查询距离玩家最近的、名称包含目标名的活体。 */

    /** 寻真司南结果包（客户端处理）：把最近生物的位置与距离送回界面。 */

    /** V 键心剑操作请求；参数为空，所有上下文由服务端读取主手与玩家数据。 */

    /** 服务端确认后的动作广播；客户端只据此播放粒子，不修改任何游戏状态。 */

    /** 服务端：处理镌印台/绘制台的符文提交。 */

    /** 服务端：处理拓印台的批量复制。 */

    /** 该物品是否为可用的墨水（灵墨 / 朱砂墨）。 */

    /** 一瓶墨的剩余墨量：即尚未消耗的耐久。 */

    /** 竖排墨水栏的剩余墨量总量（按耐久累计，忽略空瓶等杂物）。 */

    /** 竖排墨水栏是否至少有一瓶还有余墨。 */

    /**
     * 从竖排墨水栏扣除指定墨量：优先扣墨水瓶的耐久，耐久扣光则整瓶作废，
     * 不再像旧版那样按物品个数消耗。
     */

    /** 把产物放入输出槽（槽满则整单驳回，不消耗材料）。 */

    /** 从印章读取品级；未记录时回退到下品。 */

    /** 服务端：搜索名称包含指定文本的最近活体，并回发结果。 */

    /** 服务端：把玩家传送到最近一次搜索目标的附近安全落点。 */

    /** 在目标周围由近及远找一个脚可站、头上两格通透的落点。 */

    public static void tickAlchemy(net.minecraft.server.MinecraftServer server){ cn.blockforge.generated.generatedmod.network.service.AlchemyService.tickAlchemy(server); }
    public static void cancelAlchemyAt(ServerLevel level, BlockPos pos){ cn.blockforge.generated.generatedmod.network.service.AlchemyService.cancelAlchemyAt(level, pos); }
    public static boolean isAlchemyBusy(net.minecraft.world.entity.player.Player player){ return cn.blockforge.generated.generatedmod.network.service.AlchemyService.isAlchemyBusy(player); }
    public static boolean hasAlchemyJob(net.minecraft.world.level.Level level, BlockPos pos){ return cn.blockforge.generated.generatedmod.network.service.AlchemyService.hasAlchemyJob(level, pos); }
    public static void clearRefining(ServerPlayer player){ cn.blockforge.generated.generatedmod.network.service.AlchemyService.clearRefining(player); }
    public static void clearPending(net.minecraft.server.MinecraftServer server){ cn.blockforge.generated.generatedmod.network.service.AlchemyService.clearPending(server); }
}
