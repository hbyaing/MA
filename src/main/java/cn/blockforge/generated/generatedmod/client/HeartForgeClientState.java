package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.combat.HeartForgeContracts;
import cn.blockforge.generated.generatedmod.items.SpiritSwordItem;
import cn.blockforge.generated.generatedmod.network.packet.heartsword.HeartForgeContractPacket;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT)
public final class HeartForgeClientState {
    private static final Map<UUID,UUID> CONTRACTS=new HashMap<>();
    public static void accept(HeartForgeContractPacket packet) {
        if(packet.contract()==null)CONTRACTS.remove(packet.owner());
        else CONTRACTS.put(packet.owner(),packet.contract());
    }
    public static boolean visible(ItemStack stack) {
        UUID contract=HeartForgeContracts.contract(stack);
        return contract!=null&&contract.equals(CONTRACTS.get(SpiritSwordItem.heartOwner(stack)));
    }
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut event) {CONTRACTS.clear();}
    private HeartForgeClientState() {}
}
