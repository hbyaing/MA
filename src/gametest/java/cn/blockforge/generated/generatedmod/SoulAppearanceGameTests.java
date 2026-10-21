package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.divineart.PossessionController;
import cn.blockforge.generated.generatedmod.fixtures.GameTestPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("wuxingxiantu")
@PrefixGameTestTemplate(false)
public final class SoulAppearanceGameTests {
    @GameTest(template="furnace_empty",timeoutTicks=100,required=true)
    public static void ownFireCannotIgniteEitherProjectionBody(GameTestHelper h) {
        try(var player=new GameTestPlayer(h.getLevel())) {
            var pos=h.absolutePos(new BlockPos(3,1,1));
            player.setPos(pos.getX()+.5,pos.getY(),pos.getZ()+.5);player.join();
            var data=GeneratedMod.data(player);data.wuxingxiantu$setRealm(5);
            for(String art:new String[]{"yi_hun_ji_po","yuan_shen_chu_qiao"}) {
                data.wuxingxiantu$setAura(10000);data.wuxingxiantu$learnDivineArt(art);
                try {
                    h.assertTrue(DivineArtLogic.release(player,art),"Projection activation failed: "+art);
                    var body=(SoulBodyEntity)h.getLevel().getEntity(player.getPersistentData().getUUID("wuxingxiantu_soul_body"));
                    h.assertTrue(body!=null,"Missing body: "+art);
                    var fire=cn.blockforge.generated.generatedmod.fire.FireCatalog.tier(1).key();
                    cn.blockforge.generated.generatedmod.fire.ExoticBurnManager.ignite(body,fire,5,player,true);
                    h.assertTrue(!cn.blockforge.generated.generatedmod.fire.ExoticBurnManager.isBurning(body),"Own enchantment ignited body: "+art);
                    SpiritEnchantmentLogic.igniteByDomain(player,body,1);
                    h.assertTrue(!cn.blockforge.generated.generatedmod.fire.ExoticBurnManager.isBurning(body),"Own domain ignited body: "+art);
                    cn.blockforge.generated.generatedmod.fire.ExoticBurnManager.ignite(body,fire,60,(net.minecraft.world.entity.Entity)null);
                    h.assertTrue(cn.blockforge.generated.generatedmod.fire.ExoticBurnManager.isBurning(body),"Unowned environmental fire was incorrectly blocked");
                    cn.blockforge.generated.generatedmod.fire.ExoticBurnManager.clear(body);
                } finally {DivineArtLogic.stop(player,data,false);}
            }
        }
        h.succeed();
    }
    @GameTest(template="furnace_empty",timeoutTicks=100,required=true)
    public static void outOfBodyReturnsOnlyToOwnNearbyBody(GameTestHelper h) {
        try(var player=new GameTestPlayer(h.getLevel())) {
            var pos=h.absolutePos(new BlockPos(3,1,1));
            player.setPos(pos.getX()+.5,pos.getY(),pos.getZ()+.5);player.join();
            var originalMode=player.gameMode.getGameModeForPlayer();
            var data=GeneratedMod.data(player);
            data.wuxingxiantu$setRealm(5);data.wuxingxiantu$setAura(10000);
            data.wuxingxiantu$learnDivineArt("yuan_shen_chu_qiao");
            try {
                h.assertTrue(DivineArtLogic.release(player,"yuan_shen_chu_qiao"),"Out-of-body activation failed");
                var body=(SoulBodyEntity)h.getLevel().getEntity(player.getPersistentData().getUUID("wuxingxiantu_soul_body"));
                h.assertTrue(body!=null&&player.isSpectator(),"Missing body or projection mode");
                try(var stranger=new GameTestPlayer(h.getLevel())) {
                    h.assertTrue(!body.returnSoul(stranger),"Foreign player returned to another body");
                }
                player.setPos(body.getX()+8,body.getY(),body.getZ());
                h.assertTrue(!body.returnSoul(player),"Remote body accepted return");
                player.setPos(body.getX()+1,body.getY(),body.getZ());
                h.assertTrue(body.returnSoul(player),"Own nearby body rejected return");
                h.assertTrue(body.isRemoved()&&!player.hasEffect(ModEffects.YUAN_SHEN_CHU_QIAO.get()),"Return retained body or effect");
                h.assertTrue(player.gameMode.getGameModeForPlayer()==originalMode,"Return did not restore game mode");
                h.assertTrue(data.wuxingxiantu$getActiveDivineArt().isBlank(),"Return retained active art");
            } finally {DivineArtLogic.stop(player,data,false);}
        }
        h.succeed();
    }
    @GameTest(template="furnace_empty", timeoutTicks=100, required=true)
    public static void oneBodyAcrossPossessReleaseAndReattach(GameTestHelper h) {
        for(int x=0;x<7;x++)for(int z=0;z<7;z++)h.setBlock(new BlockPos(x,0,z),Blocks.STONE);
        try(var player=new GameTestPlayer(h.getLevel())) {
            var start=h.absolutePos(new BlockPos(3,1,1));
            player.setPos(start.getX()+.5,start.getY(),start.getZ()+.5);
            player.join();
            var data=GeneratedMod.data(player);
            h.assertTrue(data!=null,"Missing player capability");
            data.wuxingxiantu$setRealm(5);
            data.wuxingxiantu$setAura(10000);
            data.wuxingxiantu$learnDivineArt("yi_hun_ji_po");
            try {
                h.assertTrue(DivineArtLogic.release(player,"yi_hun_ji_po"),"Activation failed");
                var bodyId=player.getPersistentData().getUUID("wuxingxiantu_soul_body");
                var body=h.getLevel().getEntity(bodyId);
                h.assertTrue(body instanceof SoulBodyEntity,"No retained body at activation");
                var origin=body.position();
                assertPhase(h,player,0,bodyId);
                var host=h.spawn(EntityType.PIG,new BlockPos(3,1,3));
                h.assertTrue(PossessionController.tryPossess(player,host),"Attachment failed");
                assertPhase(h,player,1,bodyId);
                h.assertTrue(player.getCamera()==host,"Camera did not enter host");
                h.assertTrue(!PossessionController.tryPossess(player,host),"Repeated attachment accepted");
                assertPhase(h,player,1,bodyId);
                int duration=player.getEffect(ModEffects.YI_HUN_JI_PO.get()).getDuration();
                PossessionController.clearPossession(player);
                assertPhase(h,player,0,bodyId);
                h.assertTrue(player.getEffect(ModEffects.YI_HUN_JI_PO.get()).getDuration()==duration,
                        "Detachment changed remaining ability duration");
                h.assertTrue(player.getCamera()==player,"Camera not restored to soul");
                h.assertTrue(PossessionController.tryPossess(player,host),"Reattachment failed");
                assertPhase(h,player,1,bodyId);
                h.assertTrue(body.position().equals(origin),"Retained body moved away from activation point");
                h.assertTrue(!((SoulBodyEntity)body).returnSoul(player),"Attached player returned through detached-soul entrance");
                PossessionController.clearPossession(player);
                try(var stranger=new GameTestPlayer(h.getLevel())) {
                    h.assertTrue(!((SoulBodyEntity)body).returnSoul(stranger),"Foreign body accepted a player");
                }
                h.assertTrue(((SoulBodyEntity)body).returnSoul(player),"Own retained body did not close the art");
                h.assertTrue(body.isRemoved(),"Body remained after convergence");
                h.assertTrue(!player.hasEffect(ModEffects.YI_HUN_JI_PO.get()),"Soul phase survived convergence");
                h.assertTrue(!player.isInvisible(),"Appearance fix changed vanilla invisibility");
            } finally { DivineArtLogic.stop(player,data,false); }
        }
        h.succeed();
    }

    @GameTest(template="furnace_empty", required=true)
    public static void dragonPhaseAndReverseIndexRestoreOnDetach(GameTestHelper h) {
        try(var player=new GameTestPlayer(h.getLevel())) {
            var pos=h.absolutePos(new BlockPos(3,3,1));player.setPos(pos.getX(),pos.getY(),pos.getZ());player.join();
            var data=GeneratedMod.data(player);data.wuxingxiantu$setActiveDivineArt("yi_hun_ji_po");
            var dragon=EntityType.ENDER_DRAGON.create(h.getLevel());
            h.assertTrue(dragon!=null,"Dragon fixture could not be constructed");
            dragon.setPos(player.getX(),player.getY(),player.getZ()+4);
            h.assertTrue(h.getLevel().addFreshEntity(dragon),"Dragon fixture was not added");
            var original=net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase.HOLDING_PATTERN;
            dragon.getPhaseManager().setPhase(original);
            try {
                h.assertTrue(PossessionController.tryPossess(player,dragon),"Dragon possession rejected");
                PossessionController.setPossessionInput(player,0,0,true,false,false,false,false,false,false);
                h.assertTrue(PossessionController.possessingPlayer(dragon)==player,"Input lost reverse ownership");
                PossessionController.clearPossession(player);
                h.assertTrue(dragon.getPhaseManager().getCurrentPhase().getPhase()==original
                        &&PossessionController.possessingPlayer(dragon)==null,"Detach left dragon phase or reverse ownership");
            } finally {PossessionController.clearPossession(player);dragon.discard();}
        }
        h.succeed();
    }

    private static void assertPhase(GameTestHelper h,GameTestPlayer player,int phase,java.util.UUID bodyId) {
        var effect=player.getEffect(ModEffects.YI_HUN_JI_PO.get());
        h.assertTrue(effect!=null && effect.getAmplifier()==phase,"Wrong synchronized soul phase: "+phase);
        h.assertTrue(bodyId.equals(player.getPersistentData().getUUID("wuxingxiantu_soul_body")),"Body identity changed");
        int count=0;
        for(var entity:h.getLevel().getAllEntities())
            if(entity instanceof SoulBodyEntity body && body.owner().filter(player.getUUID()::equals).isPresent())count++;
        h.assertTrue(count==1,"Expected exactly one retained body, got "+count);
    }
}
