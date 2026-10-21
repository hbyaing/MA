package cn.blockforge.generated.generatedmod.formation;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.Network;
import cn.blockforge.generated.generatedmod.crafting.EquipmentBond;
import cn.blockforge.generated.generatedmod.economy.SpiritCurrency;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;
import java.util.*;

/** 来源真实行为：四剑布景和阵图展示、服务端付费维持；无直接伤害或护罩防护。 */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID)
public final class SwordArrayServer {
    private static final Map<UUID,Active> ACTIVE=new HashMap<>();
    private static final class Active {
        final FormationState state;
        long nextPayment=SwordArrayUpkeep.READY,lastTick=-1,nextSync=100;
        Active(FormationState state){this.state=state;}
    }
    public static Optional<FormationState> active(ServerPlayer player){var active=ACTIVE.get(player.getUUID());return active==null?Optional.empty():Optional.of(active.state);}
    public static List<FormationState> snapshot(){return ACTIVE.values().stream().map(active->active.state).toList();}
    public static boolean hasSwords(ServerPlayer player){
        for(var sword:FormationBootstrap.swords())if(player.getInventory().items.stream()
                .noneMatch(stack->stack.is(sword.get())&&EquipmentBond.canUse(player,stack)))return false;
        return true;
    }
    /** 唯一入口验证手上真实剑图、点击距离和已加载地面；客户端不传阵心或费用。 */
    public static boolean useChart(ServerPlayer player,ItemStack chart,BlockPos ground){
        if(!player.isAlive()||player.isSpectator()||!chart.is(FormationBootstrap.ZHUXIAN_SWORD_CHART.get())
                ||(chart!=player.getMainHandItem()&&chart!=player.getOffhandItem())||!EquipmentBond.canUse(player,chart))return false;
        if(ACTIVE.containsKey(player.getUUID())){close(player.getUUID());tell(player,"message.wuxingxiantu.formation.closed");return true;}
        var data=GeneratedMod.data(player);
        if(GeneratedMod.isSoulProjection(player)||data!=null&&!data.wuxingxiantu$getActiveDivineArt().isBlank())return false;
        var level=player.serverLevel();Vec3 center=Vec3.atBottomCenterOf(ground.above());
        if(!level.hasChunkAt(ground)||!level.getWorldBorder().isWithinBounds(ground)
                ||level.isOutsideBuildHeight(ground.above())||player.distanceToSqr(Vec3.atCenterOf(ground))>64
                ||level.getBlockState(ground).getCollisionShape(level,ground).isEmpty())return false;
        if(!hasSwords(player)){tell(player,"message.wuxingxiantu.formation.need_swords");return false;}
        if(SpiritCurrency.value(player.getInventory().items)<SwordArrayUpkeep.COST_LOW){tell(player,"message.wuxingxiantu.formation.need_stones");return false;}
        var state=new FormationState(player.getUUID(),level.dimension().location(),center,level.getGameTime(),player.getRandom().nextLong());
        if(!state.valid())return false;
        ACTIVE.put(player.getUUID(),new Active(state));broadcast(state,false);
        tell(player,"message.wuxingxiantu.formation.opened");return true;
    }
    public static void tick(ServerPlayer player){
        var active=ACTIVE.get(player.getUUID());if(active==null)return;
        advance(player,player.level().getGameTime()-active.state.start());
    }
    /** 服务端时钟适配与阵法状态推进分开，费用按已经经过的游戏刻结算。 */
    static void advance(ServerPlayer player,long age){
        var active=ACTIVE.get(player.getUUID());if(active==null)return;
        if(active.lastTick==age)return;active.lastTick=age;
        if(!player.isAlive()||player.isRemoved()||player.isSpectator()||age<0
                ||!player.level().dimension().location().equals(active.state.dimension())||!hasSwords(player)){
            close(player.getUUID());return;
        }
        long periods=SwordArrayUpkeep.periodsDue(age,active.nextPayment);
        if(periods>0){
            if(periods>Long.MAX_VALUE/SwordArrayUpkeep.COST_LOW
                    ||!SpiritCurrency.pay(player.getInventory(),periods*SwordArrayUpkeep.COST_LOW)){
                close(player.getUUID());tell(player,"message.wuxingxiantu.formation.empty");return;
            }
            active.nextPayment+=periods*SwordArrayUpkeep.PERIOD;
            player.containerMenu.broadcastChanges();
        }
        if(age>=active.nextSync){active.nextSync=age+100;broadcast(active.state,false);}
    }
    public static void close(UUID owner){var active=ACTIVE.remove(owner);if(active!=null)broadcast(active.state,true);}
    private static void broadcast(FormationState state,boolean stop){
        var server=ServerLifecycleHooks.getCurrentServer();if(server==null)return;
        for(var observer:server.getPlayerList().getPlayers())
            if(stop||observer.level().dimension().location().equals(state.dimension()))Network.sendToPlayer(observer,new SwordArrayStatePacket(state,stop));
    }
    public static void sync(ServerPlayer observer){
        for(var packet:snapshotFor(observer))Network.sendToPlayer(observer,packet);
    }
    static List<SwordArrayStatePacket> snapshotFor(ServerPlayer observer){return ACTIVE.values().stream()
            .filter(active->observer.level().dimension().location().equals(active.state.dimension()))
            .map(active->new SwordArrayStatePacket(active.state,false)).toList();}
    private static void tell(ServerPlayer player,String key){player.displayClientMessage(Component.translatable(key),true);}
    @SubscribeEvent public static void playerTick(TickEvent.PlayerTickEvent event){if(event.phase==TickEvent.Phase.END&&event.player instanceof ServerPlayer player)tick(player);}
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent event){close(event.getEntity().getUUID());}
    @SubscribeEvent public static void death(LivingDeathEvent event){if(event.getEntity() instanceof ServerPlayer)close(event.getEntity().getUUID());}
    @SubscribeEvent public static void dimension(PlayerEvent.PlayerChangedDimensionEvent event){close(event.getEntity().getUUID());if(event.getEntity() instanceof ServerPlayer player)sync(player);}
    @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent event){if(event.getEntity() instanceof ServerPlayer player)sync(player);}
    @SubscribeEvent public static void respawn(PlayerEvent.PlayerRespawnEvent event){if(event.getEntity() instanceof ServerPlayer player)sync(player);}
    @SubscribeEvent public static void stopped(ServerStoppedEvent event){ACTIVE.clear();}
    private SwordArrayServer(){}
}
