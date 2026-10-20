package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.event.CultivationTicker;
import cn.blockforge.generated.generatedmod.fixtures.GameTestPlayer;
import cn.blockforge.generated.generatedmod.realmflight.RealmFlightControl;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("wuxingxiantu_realmflight")
@PrefixGameTestTemplate(false)
public final class RealmFlightLevelGameTests {
    @GameTest(template="empty")
    public static void levelUpdatesAcrossRealmsWithoutStageOrSoulFlightChanges(GameTestHelper h) {
        try(var player=new GameTestPlayer(h.getLevel())) {
            var data=GeneratedMod.data(player);player.tickCount=1;data.wuxingxiantu$setAura(1000);
            data.wuxingxiantu$setRealm(8);
            player.addEffect(new MobEffectInstance(ModEffects.REALM_FLIGHT.get(),10000,0,true,false,true));
            CultivationTicker.tickPlayer(player,40);
            h.assertTrue(player.getEffect(ModEffects.REALM_FLIGHT.get()).getAmplifier()==5,
                    "The legacy fixed-Lv1 effect was not upgraded at Earth Immortal");

            long tick=41;
            for(int direction:new int[]{1,-1})for(int step=0;step<6;step++) {
                int realm=direction==1?3+step:8-step;
                data.wuxingxiantu$setRealm(realm);
                for(int stage=0;stage<=3;stage++) {
                    data.wuxingxiantu$setCultivationStage(stage);
                    var previous=player.getEffect(ModEffects.REALM_FLIGHT.get());
                    CultivationTicker.tickPlayer(player,tick++);
                    var effect=player.getEffect(ModEffects.REALM_FLIGHT.get());
                    h.assertTrue(effect!=null&&effect.getAmplifier()==realm-3,
                            "Realm level is stale after ascent/descent or stage change: realm="+realm+", stage="+stage);
                    h.assertTrue(player.getAbilities().mayfly,"Level metadata changed realm-flight permission");
                    if(previous!=null&&previous.getAmplifier()==realm-3)
                        h.assertTrue(effect==previous,"A stage change unnecessarily replaced the realm-flight effect");
                }
            }

            for(String art:new String[]{"yi_hun_ji_po","yuan_shen_chu_qiao"}) {
                var soulEffect=art.equals("yi_hun_ji_po")?ModEffects.YI_HUN_JI_PO.get():ModEffects.YUAN_SHEN_CHU_QIAO.get();
                data.wuxingxiantu$setActiveDivineArt(art);
                player.addEffect(new MobEffectInstance(soulEffect,1000,0,true,false,true));
                player.getAbilities().mayfly=true;player.getAbilities().flying=true;
                player.getAbilities().setFlyingSpeed(.08F);player.setNoGravity(true);
                boolean noPhysics=art.equals("yuan_shen_chu_qiao");player.noPhysics=noPhysics;
                var motion=new Vec3(.2,.1,.3);player.setDeltaMovement(motion);
                for(int realm:new int[]{8,3}) {
                    data.wuxingxiantu$setRealm(realm);CultivationTicker.tickPlayer(player,tick++);
                    h.assertTrue(player.getEffect(ModEffects.REALM_FLIGHT.get()).getAmplifier()==realm-3,
                            "Soul flight prevented display-level refresh");
                    h.assertTrue(!RealmFlightControl.eligible(player)&&player.getAbilities().flying
                                    &&player.getAbilities().mayfly&&player.getAbilities().getFlyingSpeed()==.08F,
                            "Updating the realm level took over soul-flight permissions or speed");
                    h.assertTrue(player.noPhysics==noPhysics&&player.isNoGravity()&&player.getDeltaMovement().equals(motion),
                            "Updating the realm level changed soul collision or motion");
                }
                data.wuxingxiantu$setActiveDivineArt("");player.removeEffect(soulEffect);
            }

            player.getAbilities().flying=false;player.setNoGravity(false);player.noPhysics=false;
            data.wuxingxiantu$setRealm(2);CultivationTicker.tickPlayer(player,tick);
            h.assertTrue(!player.hasEffect(ModEffects.REALM_FLIGHT.get())&&!player.getAbilities().mayfly,
                    "Realm-flight level remained below Nascent Soul");
        }
        h.succeed();
    }
}
