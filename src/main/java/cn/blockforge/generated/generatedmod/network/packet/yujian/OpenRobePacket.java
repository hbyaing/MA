package cn.blockforge.generated.generatedmod.network.packet.yujian;

import cn.blockforge.generated.generatedmod.*;
import static cn.blockforge.generated.generatedmod.Network.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record OpenRobePacket(){
    public static void encode(OpenRobePacket p,FriendlyByteBuf b){}
    public static OpenRobePacket decode(FriendlyByteBuf b){return new OpenRobePacket();}
    public static void handle(OpenRobePacket p,Supplier<NetworkEvent.Context> s){var c=s.get();c.enqueueWork(()->{
        var player=c.getSender();if(player==null||!player.isAlive()||player.isSpectator()
                ||player.containerMenu!=player.inventoryMenu||!player.containerMenu.getCarried().isEmpty())return;
        net.minecraftforge.network.NetworkHooks.openScreen(player,new net.minecraft.world.SimpleMenuProvider(
                (id,inventory,owner)->new cn.blockforge.generated.generatedmod.robe.RobeMenu(id,inventory,new cn.blockforge.generated.generatedmod.robe.RobeContainer(player)),
                Component.translatable("menu.wuxingxiantu.robe")));
    });c.setPacketHandled(true);}
}
