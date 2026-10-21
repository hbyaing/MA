package cn.blockforge.generated.generatedmod.storage.client;
import cn.blockforge.generated.generatedmod.storage.menu.BagMenu;
import cn.blockforge.generated.generatedmod.storage.network.BagStatePacket;
public final class ClientBagState{public static void apply(BagStatePacket p){var player=net.minecraft.client.Minecraft.getInstance().player;if(player!=null&&player.containerMenu instanceof BagMenu menu&&menu.containerId==p.menuId()){menu.info.set(0,p.row());menu.info.set(1,p.ranges());menu.info.set(2,p.magnet()?1:0);System.arraycopy(p.totals(),0,menu.totals,0,54);}}}
