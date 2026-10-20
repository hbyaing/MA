package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.items.CultivationLogic;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("wuxingxiantu")
@PrefixGameTestTemplate(false)
public final class BalanceGameplayGameTests {
    private static FakePlayer player(GameTestHelper h) {
        var p=new FakePlayer(h.getLevel(),new GameProfile(UUID.randomUUID(),"BalanceTest"));
        var pos=h.absolutePos(new BlockPos(2,2,2));
        p.setPos(pos.getX()+.5,pos.getY(),pos.getZ()+.5);
        h.getLevel().addNewPlayer(p);
        var data=GeneratedMod.data(p);
        data.wuxingxiantu$setRealm(5);
        data.wuxingxiantu$setDaoXin(0);
        data.wuxingxiantu$setEquippedHeartMethod("beast_taming");
        CultivationLogic.applyHealthBonus(p,data);
        return p;
    }
    private static void near(GameTestHelper h,double expected,double actual,String message) {
        h.assertTrue(Math.abs(expected-actual)<.01D,message+": "+expected+" != "+actual);
    }

    @GameTest(template="furnace_empty")
    public static void heartBonusesSwitchWithoutStackingOrFreeHealing(GameTestHelper h) {
        var p=player(h);
        try {
            var data=GeneratedMod.data(p);
            float baseHealth=p.getMaxHealth();
            double baseAttack=p.getAttributeValue(Attributes.ATTACK_DAMAGE);
            p.setHealth(baseHealth/2);
            data.wuxingxiantu$setEquippedHeartMethod("thick_earth");
            CultivationLogic.applyHealthBonus(p,data);
            near(h,baseHealth*1.12,p.getMaxHealth(),"Thick Earth health did not scale");
            near(h,.5,p.getHealth()/p.getMaxHealth(),"Switching method changed health fraction");
            CultivationLogic.applyHealthBonus(p,data);
            near(h,baseHealth*1.12,p.getMaxHealth(),"Repeated maintenance stacked health");
            data.wuxingxiantu$setEquippedHeartMethod("tiangang");
            CultivationLogic.applyHealthBonus(p,data);
            near(h,baseHealth,p.getMaxHealth(),"Previous method health remained");
            near(h,baseAttack*1.08,p.getAttributeValue(Attributes.ATTACK_DAMAGE),"Tiangang attack did not scale");
            data.wuxingxiantu$setRealm(0);
            CultivationLogic.applyHealthBonus(p,data);
            double gatedAttack=p.getAttributeValue(Attributes.ATTACK_DAMAGE);
            data.wuxingxiantu$setEquippedHeartMethod("beast_taming");
            CultivationLogic.applyHealthBonus(p,data);
            near(h,gatedAttack,p.getAttributeValue(Attributes.ATTACK_DAMAGE),"Realm gate left Tiangang attack modifier");
        } finally { p.discard(); }
        h.succeed();
    }

    @GameTest(template="furnace_empty")
    public static void methodChangesRecomputeManaAndNineTurnsRewards(GameTestHelper h) {
        var p=player(h);
        try {
            var data=GeneratedMod.data(p);
            float baseMana=CultivationLogic.maxAura(data);
            data.wuxingxiantu$setEquippedHeartMethod("yuan_hun");
            near(h,baseMana*1.10,CultivationLogic.maxAura(data),"Mana did not refresh when equipping Yuan Hun");
            data.wuxingxiantu$setEquippedHeartMethod("nine_turns");
            near(h,baseMana,CultivationLogic.maxAura(data),"Yuan Hun mana bonus remained after switch");
            near(h,Math.round(baseMana*.40F),CultivationLogic.breakthroughAuraCost(data),"Nine Turns breakthrough discount missing");
            data.wuxingxiantu$setCultivation(0);
            CultivationLogic.gainCultivation(p,data,100);
            near(h,120,data.wuxingxiantu$getCultivation(),"Nine Turns real cultivation gain missing");
            data.wuxingxiantu$setEquippedHeartMethod("gathering");
            data.wuxingxiantu$setCultivation(0);
            data.wuxingxiantu$setMeditating(true);
            for(int reward=0;reward<20;reward++)CultivationLogic.gainCultivation(p,data,1);
            near(h,23,data.wuxingxiantu$getCultivation(),"One-point meditation lost its fractional heart bonus");
            data.wuxingxiantu$setMeditating(false);
        } finally { p.discard(); }
        h.succeed();
    }

    @GameTest(template="furnace_empty")
    public static void defenseScalesAcrossRealmsAndNeverWrapsAmplifier(GameTestHelper h) {
        int early=ArtCatalog.shieldAmplifier("art_immovable_mountain",140);
        int late=ArtCatalog.shieldAmplifier("art_immovable_mountain",4000);
        h.assertTrue(4*(late+1)>=4000*.18,"Late defense lost its maximum-health scaling");
        h.assertTrue(late>early,"Defense strength did not grow with health");
        h.assertTrue(ArtCatalog.shieldAmplifier("art_immovable_mountain",1_000_000)<=255,
                "Giant-form absorption would wrap during save/networking");
        h.succeed();
    }

    @GameTest(template="furnace_empty")
    public static void allArtCostsMeetRecoveryTargetWithoutDamageNerf(GameTestHelper h) {
        for(var art:ArtCatalog.ENTRIES) {
            int realm=ProgressionGate.requiredRealmForArt(art.id());
            float mana=cn.blockforge.generated.generatedmod.balance.RealmBalance.mana(realm);
            double seconds=ArtCatalog.finalManaCost(art.id(),mana)
                    /(mana*cn.blockforge.generated.generatedmod.balance.RealmBalance.regeneration(realm));
            h.assertTrue(seconds>=19.9D&&seconds<=35.1D,"Unexpected recovery time for "+art.id()+": "+seconds);
        }
        near(h,1.12,ArtCatalog.damageInjectionMultiplier("art_sword_aura"),"Yellow damage infusion changed with cost");
        near(h,1.30,ArtCatalog.damageInjectionMultiplier("art_ice_spike"),"Profound damage infusion changed with cost");
        near(h,1.48,ArtCatalog.damageInjectionMultiplier("art_flame_wave"),"Earth damage infusion changed with cost");
        near(h,1.84,ArtCatalog.damageInjectionMultiplier("art_five_elements"),"Heaven damage infusion changed with cost");
        h.succeed();
    }

    @GameTest(template="furnace_empty")
    public static void soulRangeUsesThePersistentLaunchPosition(GameTestHelper h) {
        var p=player(h);
        try {
            SoulAnchor.capture(p);
            var origin=p.position();
            p.setPos(origin.x+128,origin.y,origin.z);
            near(h,128,SoulAnchor.distance(p),"Soul distance did not use its launch anchor");
            near(h,3,cn.blockforge.generated.generatedmod.balance.DivineArtCosts.soulDistanceMultiplier(SoulAnchor.distance(p)),
                    "128-block projection should triple upkeep, not end the art");
            p.setPos(origin.x,origin.y+192,origin.z);
            near(h,192,SoulAnchor.distance(p),"Vertical separation was omitted from soul upkeep");
        } finally { p.discard(); }
        h.succeed();
    }
}
