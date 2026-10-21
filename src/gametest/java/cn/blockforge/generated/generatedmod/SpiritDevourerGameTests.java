package cn.blockforge.generated.generatedmod;
import cn.blockforge.generated.generatedmod.entity.SpiritDevourerEntity;
import cn.blockforge.generated.generatedmod.registry.ModEntities;
import cn.blockforge.generated.generatedmod.plants.*;
import cn.blockforge.generated.generatedmod.fixtures.GameTestPlayer;
import net.minecraft.gametest.framework.*;
import net.minecraftforge.gametest.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.GameRules;
@GameTestHolder("wuxingxiantu_realmflight")
@PrefixGameTestTemplate(false)
public final class SpiritDevourerGameTests {
    @GameTest(template="empty") public static void insectFeedingRegressesOneStageAndSharesProtection(GameTestHelper h){
        var block=(SpiritPlantBlock)SpiritPlantRegistry.DRAGON_SCALE_GRASS.get();
        var pos=h.absolutePos(new BlockPos(1,2,1));h.getLevel().setBlockAndUpdate(pos.below(),net.minecraft.world.level.block.Blocks.END_STONE.defaultBlockState());
        h.getLevel().setBlockAndUpdate(pos,block.defaultBlockState().setValue(SpiritPlantBlock.AGE,7));
        var first=ModEntities.SPIRIT_DEVOURER.get().create(h.getLevel());var second=ModEntities.SPIRIT_DEVOURER.get().create(h.getLevel());
        boolean old=h.getLevel().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
        try {
            h.getLevel().getGameRules().getRule(GameRules.RULE_MOBGRIEFING).set(true,h.getLevel().getServer());
            h.assertTrue(first.feedPlant(pos),"Feeding failed");
            h.assertTrue(h.getLevel().getBlockState(pos).getValue(SpiritPlantBlock.AGE)==6,"Feeding must regress exactly one stage");
            h.assertTrue(!second.feedPlant(pos),"Second insect bypassed per-plant cooldown");
            h.assertTrue(first.full(),"Feeding did not enter sated state");
            var tag=new net.minecraft.nbt.CompoundTag();first.addAdditionalSaveData(tag);second.readAdditionalSaveData(tag);
            h.assertTrue(second.full(),"Sated state did not survive save/load");
        }finally{h.getLevel().getGameRules().getRule(GameRules.RULE_MOBGRIEFING).set(old,h.getLevel().getServer());first.discard();second.discard();}
        h.succeed();
    }
    @GameTest(template="empty") public static void insectMobGriefingOffStillFeedsWithoutDamage(GameTestHelper h){
        var block=(SpiritPlantBlock)SpiritPlantRegistry.DRAGON_SCALE_GRASS.get();
        var pos=h.absolutePos(new BlockPos(2,2,2));h.getLevel().setBlockAndUpdate(pos.below(),net.minecraft.world.level.block.Blocks.END_STONE.defaultBlockState());
        var state=block.defaultBlockState().setValue(SpiritPlantBlock.AGE,7);h.getLevel().setBlockAndUpdate(pos,state);
        var insect=ModEntities.SPIRIT_DEVOURER.get().create(h.getLevel());boolean old=h.getLevel().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
        try{
            h.getLevel().getGameRules().getRule(GameRules.RULE_MOBGRIEFING).set(false,h.getLevel().getServer());
            h.assertTrue(insect.feedPlant(pos)&&insect.full(),"MobGriefing off blocked harmless feeding");
            h.assertTrue(h.getLevel().getBlockState(pos).equals(state),"MobGriefing off still damaged plant");
        }finally{h.getLevel().getGameRules().getRule(GameRules.RULE_MOBGRIEFING).set(old,h.getLevel().getServer());insect.discard();}
        h.succeed();
    }
    @GameTest(template="empty") public static void realmFlightBuffRestoresAfterCreationAndSurvivalSwitch(GameTestHelper h){
        try(var p=new GameTestPlayer(h.getLevel())){
            var data=GeneratedMod.data(p);data.wuxingxiantu$setRealm(8);data.wuxingxiantu$setAura(1000);
            cn.blockforge.generated.generatedmod.event.CultivationTicker.tickPlayer(p,40);
            h.assertTrue(p.hasEffect(ModEffects.REALM_FLIGHT.get())&&p.getAbilities().mayfly,"Earth immortal lacks flight permission/buff");
            p.removeEffect(ModEffects.REALM_FLIGHT.get());p.getAbilities().mayfly=false;
            cn.blockforge.generated.generatedmod.event.CultivationTicker.tickPlayer(p,41);
            h.assertTrue(p.hasEffect(ModEffects.REALM_FLIGHT.get())&&p.getAbilities().mayfly,"Flight buff/permission not repaired");
            data.wuxingxiantu$setRealm(3);cn.blockforge.generated.generatedmod.event.CultivationTicker.tickPlayer(p,42);
            h.assertTrue(p.hasEffect(ModEffects.REALM_FLIGHT.get()),"Nascent soul lacks flight buff");
            data.wuxingxiantu$setRealm(2);cn.blockforge.generated.generatedmod.event.CultivationTicker.tickPlayer(p,43);
            h.assertTrue(!p.hasEffect(ModEffects.REALM_FLIGHT.get())&&!p.getAbilities().mayfly,"Lower realm retained flight");
        }h.succeed();
    }
}
