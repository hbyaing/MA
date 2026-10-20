package cn.blockforge.generated.generatedmod.client;
import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.items.CultivationLogic;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT)
public final class MeditationRateHud{
    @SubscribeEvent public static void render(RenderGuiEvent.Post e){
        var mc=net.minecraft.client.Minecraft.getInstance();if(mc.player==null||mc.options.hideGui||mc.screen!=null)return;
        var d=GeneratedMod.data(mc.player);if(d==null||!d.wuxingxiantu$isMeditating())return;
        double rate=cn.blockforge.generated.generatedmod.balance.RootBalance.training(d)*CultivationLogic.meditationCultivationReward(d)*cn.blockforge.generated.generatedmod.balance.HeartMethodBalance.cultivationMultiplier(cn.blockforge.generated.generatedmod.ProgressionGate.effectiveHeartMethod(mc.player,d),true);
        var pill=mc.player.getEffect(cn.blockforge.generated.generatedmod.ModEffects.PILL_GATHERING.get());if(pill!=null)rate*=Math.min(3,1+.15*Math.min(4,pill.getAmplifier()+1));
        if(mc.player.hasEffect(cn.blockforge.generated.generatedmod.ModEffects.TALISMAN_GATHERING.get()))rate*=1.15;
        if(d instanceof cn.blockforge.generated.generatedmod.CultivationDataImpl actual&&actual.meditationPenaltyTicks()>0)rate*=.5;
        if(mc.player.isCreative()||mc.player.isSpectator()||d.wuxingxiantu$getCultivation()>=CultivationLogic.breakthroughRequirement(d)||!cn.blockforge.generated.generatedmod.mindscape.MindscapeManager.canGain(mc.player,d,false))rate=0;
        String text="修炼 "+CultivationUi.number(rate)+"/秒  ·  灵力回复 "+CultivationUi.number(CultivationLogic.regeneration(d,true))+"/秒";
        int x=(mc.getWindow().getGuiScaledWidth()-mc.font.width(text))/2,y=mc.getWindow().getGuiScaledHeight()-90;
        e.getGuiGraphics().fill(x-4,y-3,x+mc.font.width(text)+4,y+11,0xA0101D2C);
        e.getGuiGraphics().drawString(mc.font,text,x,y,0x80D9F2,false);
    }
}
