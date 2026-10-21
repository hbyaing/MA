package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.divinesurvey.*;
import cn.blockforge.generated.generatedmod.fixtures.GameTestPlayer;
import cn.blockforge.generated.generatedmod.progression.PlayerCrafts;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.gametest.*;
import java.util.*;

@GameTestHolder("wuxingxiantu_entry") @PrefixGameTestTemplate(false)
public final class DivineSurveyGameTests {
    @GameTest(template="empty") public static void surveyUsesExistingSeaAndRealmAndDoesNotTouchPlayerAura(GameTestHelper helper){
        try(var player=new GameTestPlayer(helper.getLevel())){
            player.join();var data=GeneratedMod.data(player);data.wuxingxiantu$setRealm(3);
            var account=PlayerCrafts.get(player.server).account(player.getUUID());account.putBoolean("SenseOpen",true);account.putFloat("Sense",40);
            DivineSurvey.toggle(player);helper.assertTrue(!DivineSurvey.active(player)&&account.getFloat("Sense")==40,"Yuan Ying started the Huashen survey or lost sense");
            data.wuxingxiantu$setRealm(4);account.putBoolean("SenseOpen",false);DivineSurvey.toggle(player);
            helper.assertTrue(!DivineSurvey.active(player),"Survey bypassed the existing sea opening");
            account.putBoolean("SenseOpen",true);float aura=data.wuxingxiantu$getAura();
            DivineSurvey.toggle(player);
            try{helper.assertTrue(DivineSurvey.active(player)&&account.getFloat("Sense")==32,"Survey did not debit exactly 8 existing sense");
                helper.assertTrue(data.wuxingxiantu$getAura()==aura&&data.wuxingxiantu$getRealm()==4,"Survey created another mana or realm authority");
                account.putFloat("Sense",0);player.tickCount=20;
                DivineSurvey.tick(new TickEvent.PlayerTickEvent(TickEvent.Phase.END,player));
                helper.assertTrue(!DivineSurvey.active(player)&&account.getFloat("Sense")==0,"Empty existing sense did not stop survey before chunk work");
            }finally{if(DivineSurvey.active(player))DivineSurvey.toggle(player);}
        }helper.succeed();
    }
    @GameTest(template="empty") public static void surveyGroupsAdjacentSameOreAndIncludesAllTargetSpiritGrades(GameTestHelper helper){
        Map<BlockPos,BlockState> blocks=new HashMap<>();blocks.put(new BlockPos(0,0,0),Blocks.DIAMOND_ORE.defaultBlockState());blocks.put(new BlockPos(1,0,0),Blocks.DIAMOND_ORE.defaultBlockState());blocks.put(new BlockPos(2,0,0),Blocks.GOLD_ORE.defaultBlockState());blocks.put(new BlockPos(8,0,0),Blocks.DIAMOND_ORE.defaultBlockState());
        var groups=DivineSurvey.groups(blocks);
        helper.assertTrue(groups.size()==3&&groups.stream().anyMatch(t->t.name().endsWith("|2")&&t.ex()-t.x()==2),"Ore cluster merging lost cardinal adjacency or material separation");
        for(var block:List.of(WuXingBlocks.SPIRIT_ORE,WuXingBlocks.SPIRIT_VEIN_ORE,WuXingBlocks.SPIRIT_VEIN_ORE_MIDDLE,WuXingBlocks.SPIRIT_VEIN_ORE_SUPERIOR,WuXingBlocks.SPIRIT_VEIN_ORE_EXQUISITE))helper.assertTrue(SurveyRules.spiritOre(block.get().defaultBlockState()),"Target spirit ore grade excluded");
        helper.assertTrue(SurveyRules.containsChunk(-8,-8,-1,-1)&&!SurveyRules.containsChunk(-8,-8,0,-1),"64-chunk survey bounds expanded");helper.succeed();
    }
    @GameTest(template="empty") public static void surveyPacketPreservesOreAndStructureKindsAndRejectsOversizedBatch(GameTestHelper helper){
        var target=new SurveyPacket.Target(-5,-40,8,-2,-38,9,"block.minecraft.diamond_ore|3",0xFF55AAFF,0);
        var structure=new SurveyPacket.Target(20,70,20,21,71,21,"结构 · minecraft:stronghold",0xFFFFCC44,1);
        var packet=new SurveyPacket("minecraft:overworld",2,17,List.of(target,structure));var buffer=new FriendlyByteBuf(Unpooled.buffer());
        try{packet.encode(buffer);helper.assertTrue(packet.equals(new SurveyPacket(buffer)),"Survey markers changed across the real wire codec");}finally{buffer.release();}
        boolean rejected=false;try{new SurveyPacket("minecraft:overworld",2,0,Collections.nCopies(513,target));}catch(IllegalArgumentException expected){rejected=true;}
        helper.assertTrue(rejected,"Unbounded survey packet was accepted");helper.succeed();
    }
}
