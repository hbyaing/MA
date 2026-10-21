package cn.blockforge.generated.generatedmod.client.boss;

import cn.blockforge.generated.generatedmod.boss.BossVisualEntity;
import cn.blockforge.generated.generatedmod.network.packet.boss.BossTransformMotionPacket;
import net.minecraft.client.Minecraft;
import java.util.*;

public final class BossTransformMotions{
    private static final Map<UUID,BossTransformMotionPacket> STATES=new HashMap<>();
    private static net.minecraft.client.multiplayer.ClientLevel world;
    public static void accept(BossTransformMotionPacket packet){refresh();if(packet.duration()==0)STATES.remove(packet.player());else if(STATES.size()<256||STATES.containsKey(packet.player()))STATES.put(packet.player(),packet);}
    private static void refresh(){var current=Minecraft.getInstance().level;if(current!=world){STATES.clear();world=current;}if(world!=null)STATES.values().removeIf(p->world.getGameTime()>p.start()+p.duration());}
    public static void apply(UUID player,String form,BossVisualEntity entity){refresh();var state=STATES.get(player);if(state==null||!state.form().equals(form))entity.clientMotion("",0,0);else entity.clientMotion(state.animation(),state.start(),state.start()+state.duration());}
    private BossTransformMotions(){}
}
