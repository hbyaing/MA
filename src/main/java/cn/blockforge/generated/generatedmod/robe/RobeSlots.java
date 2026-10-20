package cn.blockforge.generated.generatedmod.robe;

import cn.blockforge.generated.generatedmod.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID)
public final class RobeSlots {
    public static final net.minecraft.tags.TagKey<net.minecraft.world.item.Item> ROBES=ItemTags.create(new ResourceLocation(GeneratedMod.MOD_ID,"robes"));
    private RobeSlots(){}
    private record SentEquipment(ItemStack robe,ItemStack sword,boolean locked) {}
    private static final java.util.Map<ServerPlayer,SentEquipment> LAST_SENT=new java.util.WeakHashMap<>();
    public static void syncIfChanged(ServerPlayer player) {
        var old=LAST_SENT.get(player);
        var data=GeneratedMod.data(player);if(data==null)return;
        if(old==null||old.locked()!=YujianSlot.locked(player)
                ||!ItemStack.matches(old.robe(),data.wuxingxiantu$getRobeStack())
                ||!ItemStack.matches(old.sword(),YujianSlot.activeFlyingSword(player)))sync(player);
    }

    public static boolean canEquipRobe(ItemStack stack){return !stack.isEmpty()&&stack.is(ROBES);}
    public static ItemStack getRobe(Player player){var data=GeneratedMod.data(player);return data==null?ItemStack.EMPTY:data.wuxingxiantu$getRobeStack().copy();}
    public static boolean hasRobe(Player player){return !getRobe(player).isEmpty();}
    public static boolean setRobe(Player player,ItemStack stack){
        if(player.level().isClientSide||cn.blockforge.generated.generatedmod.mindscape.MindscapeRules.isInMindscape(player)
                ||(!stack.isEmpty()&&(!canEquipRobe(stack)||stack.getCount()!=1)))return false;
        var data=GeneratedMod.data(player);if(data==null)return false;
        if(ItemStack.matches(data.wuxingxiantu$getRobeStack(),stack))return true;
        data.wuxingxiantu$setRobeStack(stack);if(player instanceof ServerPlayer server)sync(server);return true;
    }
    public static void sync(ServerPlayer player){
        LAST_SENT.put(player,new SentEquipment(getRobe(player),YujianSlot.activeFlyingSword(player).copy(),YujianSlot.locked(player)));
        Network.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(()->player),packet(player));
    }
    private static cn.blockforge.generated.generatedmod.network.packet.yujian.RobeSyncPacket packet(Player player){return new cn.blockforge.generated.generatedmod.network.packet.yujian.RobeSyncPacket(player.getId(),player.getUUID(),
            getRobe(player),YujianSlot.activeFlyingSword(player).copy(),YujianSlot.locked(player));}
    @SubscribeEvent public static void tick(net.minecraftforge.event.TickEvent.PlayerTickEvent event){
        if(event.phase!=net.minecraftforge.event.TickEvent.Phase.END||!(event.player instanceof ServerPlayer player)||player.tickCount%20!=0)return;
        ItemStack sword=YujianSlot.activeFlyingSword(player);
        if(sword.getItem() instanceof cn.blockforge.generated.generatedmod.items.SpiritSwordItem){
            sword.getItem().inventoryTick(sword,player.level(),player,YujianSlot.YUJIAN_SLOT,false);
            if(!YujianSlot.locked(player))syncIfChanged(player);
        }
    }
    @SubscribeEvent public static void drops(LivingDropsEvent event){
        if(!(event.getEntity() instanceof ServerPlayer player)||player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)||player.isSpectator())return;
        var data=GeneratedMod.data(player);if(data==null)return;
        ItemStack sword=data.wuxingxiantu$getYujianStack();
        data.wuxingxiantu$setYujianStack(ItemStack.EMPTY);
        drop(event,player,sword);
        ItemStack stack=getRobe(player);
        data.wuxingxiantu$setRobeStack(ItemStack.EMPTY);
        drop(event,player,stack);sync(player);
    }
    private static void drop(LivingDropsEvent event,ServerPlayer player,ItemStack stack){
        if(!stack.isEmpty()&&!net.minecraft.world.item.enchantment.EnchantmentHelper.hasVanishingCurse(stack)){
            var drop=new net.minecraft.world.entity.item.ItemEntity(player.level(),player.getX(),player.getY()+.5,player.getZ(),stack);
            drop.setDefaultPickUpDelay();event.getDrops().add(drop);
        }
    }
    @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent event){if(event.getEntity() instanceof ServerPlayer player)sync(player);}
    @SubscribeEvent public static void dimension(PlayerEvent.PlayerChangedDimensionEvent event){if(event.getEntity() instanceof ServerPlayer player)sync(player);}
    @SubscribeEvent public static void respawn(PlayerEvent.PlayerRespawnEvent event){if(event.getEntity() instanceof ServerPlayer player)sync(player);}
    @SubscribeEvent public static void track(PlayerEvent.StartTracking event){
        if(event.getEntity() instanceof ServerPlayer viewer&&event.getTarget() instanceof Player subject)
            Network.CHANNEL.send(PacketDistributor.PLAYER.with(()->viewer),packet(subject));
    }
}
