package cn.blockforge.generated.generatedmod.robe;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.items.SpiritSwordItem;
import cn.blockforge.generated.generatedmod.yujian.FlyingSwordRegistry;
import cn.blockforge.generated.generatedmod.yujian.YujianController;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class YujianSlot {
    public static final int YUJIAN_SLOT=1;
    public static final int NURTURE_SLOT=-2;
    public static ItemStack nurtureStack(Player player,int slot){
        return slot==NURTURE_SLOT?activeFlyingSword(player):slot>=0&&slot<player.getInventory().getContainerSize()
                ?player.getInventory().getItem(slot):ItemStack.EMPTY;
    }
    public static boolean accepts(ItemStack stack){
        return !stack.isEmpty()&&stack.getCount()==1&&stack.getItem() instanceof SpiritSwordItem
                &&FlyingSwordRegistry.canFly(stack);
    }
    public static ItemStack activeFlyingSword(Player player){
        var data=GeneratedMod.data(player);
        return data==null?ItemStack.EMPTY:data.wuxingxiantu$getYujianStack();
    }
    public static boolean locked(Player player){
        return player instanceof ServerPlayer server?YujianController.riding(server)
                :player.getPersistentData().getBoolean("YujianSlotLocked");
    }
    public static boolean set(Player player,ItemStack stack){
        if(player.level().isClientSide||cn.blockforge.generated.generatedmod.mindscape.MindscapeRules.isInMindscape(player)
                ||(!stack.isEmpty()&&!accepts(stack)))return false;
        if(locked(player)&&player instanceof ServerPlayer server)YujianController.stop(server);
        var data=GeneratedMod.data(player);if(data==null)return false;
        data.wuxingxiantu$setYujianStack(stack);
        if(player instanceof ServerPlayer server)RobeSlots.sync(server);
        return true;
    }
    public static ItemStack candidate(Player player){
        ItemStack equipped=activeFlyingSword(player);
        return equipped.isEmpty()?player.getMainHandItem():equipped;
    }
    public static ItemStack equipFromMainHand(ServerPlayer player){
        if(cn.blockforge.generated.generatedmod.mindscape.MindscapeRules.isInMindscape(player))return ItemStack.EMPTY;
        ItemStack equipped=activeFlyingSword(player);
        if(!equipped.isEmpty())return accepts(equipped)?equipped:ItemStack.EMPTY;
        var data=GeneratedMod.data(player);ItemStack original=player.getMainHandItem();
        if(data==null||locked(player)||!accepts(original))return ItemStack.EMPTY;
        player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
        data.wuxingxiantu$setYujianStack(original);
        player.getInventory().setChanged();player.containerMenu.broadcastChanges();
        RobeSlots.sync(player);
        return original;
    }
    private YujianSlot(){}
}
