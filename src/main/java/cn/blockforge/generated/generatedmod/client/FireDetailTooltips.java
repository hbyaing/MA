package cn.blockforge.generated.generatedmod.client;
import cn.blockforge.generated.generatedmod.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
final class FireDetailTooltips{
    static Component describe(Player player,CultivationData data){
        int tier=data.wuxingxiantu$getEquippedFire();if(tier<1)return Component.literal("当前异火：无\n尚未装备异火");
        return cn.blockforge.generated.generatedmod.text.ExoticFireDescriptions.tooltip(tier,player.level().getGameTime(),Component.literal(cn.blockforge.generated.generatedmod.items.FireSeedItem.getFireName(tier)+" · 已装备"+(ProgressionGate.canUseFire(player,tier)?"":" · 境界不足，暂不可驭使")));
    }
}
