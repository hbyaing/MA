package cn.blockforge.generated.generatedmod;
import cn.blockforge.generated.generatedmod.fixtures.GameTestPlayer;
import cn.blockforge.generated.generatedmod.progression.*;
import net.minecraft.gametest.framework.*;
import net.minecraftforge.gametest.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.*;
import net.minecraftforge.event.TickEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.BlockPos;
@GameTestHolder("wuxingxiantu_talismans") @PrefixGameTestTemplate(false)
public final class PlayerProgressionGameTests {
    @GameTest(template="empty") public static void nineRanksAdvanceIndividuallyAndMigrateOnce(GameTestHelper h){
        var id=java.util.UUID.randomUUID();var legacy=new CompoundTag();var accounts=new CompoundTag();var owner=new CompoundTag();
        for(int i=0;i<3;i++){var row=new CompoundTag();row.putBoolean("Learned",true);row.putInt("Rank",i*2);owner.put(PlayerCrafts.IDS[i],row);}accounts.put(id.toString(),owner);legacy.put("Accounts",accounts);
        var migrated=PlayerCrafts.load(legacy);for(int i=0;i<3;i++)h.assertTrue(migrated.profession(id,PlayerCrafts.IDS[i]).getInt("Rank")==i*4,"Legacy career mapping lost progress");
        var loaded=PlayerCrafts.load(migrated.save(new CompoundTag()));for(int i=0;i<3;i++)h.assertTrue(loaded.profession(id,PlayerCrafts.IDS[i]).getInt("Rank")==i*4,"Rank migrated twice");
        try(var p=player(h,6)){var crafts=PlayerCrafts.get(p.server);for(String career:PlayerCrafts.IDS){crafts.learnOrPromote(p,career);h.assertTrue(PlayerCrafts.bonus(p,career)==1,"Learned profession must start at first rank");var n=crafts.profession(p.getUUID(),career);n.putInt("Xp",100000);n.putInt("BestTier",4);var works=new CompoundTag();for(int j=0;j<12;j++)works.putInt("proof"+j,1);n.put("Works",works);
            if(career.equals("alchemy")){crafts.learnOrPromote(p,career);h.assertTrue(PlayerCrafts.rank(p,career)==0,"XP bypassed alchemy certification");continue;}for(int rank=1;rank<=8;rank++){crafts.learnOrPromote(p,career);h.assertTrue(PlayerCrafts.rank(p,career)==rank&&PlayerCrafts.bonus(p,career)==rank+1,"Career skipped or stalled a rank");}
            crafts.learnOrPromote(p,career);h.assertTrue(PlayerCrafts.rank(p,career)==8,"Rank exceeded ninth");}}
        h.succeed();
    }
    @GameTest(template="empty") public static void minorBreakthroughNeedsOnlyFullCultivation(GameTestHelper h){
        try(var p=player(h,2)){var d=GeneratedMod.data(p);d.wuxingxiantu$setAura(0);d.wuxingxiantu$setCultivationStage(0);long cap=cn.blockforge.generated.generatedmod.items.CultivationLogic.breakthroughRequirement(d);
            d.wuxingxiantu$setCultivation(cap-1);h.assertTrue(!cn.blockforge.generated.generatedmod.items.CultivationLogic.advanceMinorStage(p,d),"Incomplete cultivation advanced");
            for(int stage=1;stage<=3;stage++){d.wuxingxiantu$setCultivation(cn.blockforge.generated.generatedmod.items.CultivationLogic.breakthroughRequirement(d));h.assertTrue(cn.blockforge.generated.generatedmod.items.CultivationLogic.advanceMinorStage(p,d)&&d.wuxingxiantu$getCultivationStage()==stage,"Minor stage still gated by aura/pill/chance");h.assertTrue(d.wuxingxiantu$getAura()==0,"Minor stage consumed or created aura");}
            d.wuxingxiantu$setCultivation(cn.blockforge.generated.generatedmod.items.CultivationLogic.breakthroughRequirement(d));h.assertTrue(!cn.blockforge.generated.generatedmod.items.CultivationLogic.advanceMinorStage(p,d)&&d.wuxingxiantu$getRealm()==2,"Automatic advance bypassed major breakthrough");}
        h.succeed();
    }
    private static GameTestPlayer player(GameTestHelper h,int realm){var p=new GameTestPlayer(h.getLevel());p.join();p.setPos(h.absolutePos(BlockPos.ZERO).getX()+.5,210,h.absolutePos(BlockPos.ZERO).getZ()+.5);GeneratedMod.data(p).wuxingxiantu$setRealm(realm);return p;}
    private static void ticks(GameTestPlayer p,int count){for(int i=0;i<count;i++){p.tickCount++;SpiritualSense.tick(new TickEvent.PlayerTickEvent(TickEvent.Phase.END,p));}}
    @GameTest(template="empty") public static void allCraftsLearnIndependentlyAndRequirePromotionProof(GameTestHelper h){
        try(var p=player(h,4)){var d=PlayerCrafts.get(p.server);for(var id:PlayerCrafts.IDS)d.learnOrPromote(p,id);
            for(var id:PlayerCrafts.IDS){h.assertTrue(d.profession(p.getUUID(),id).getBoolean("Learned"),"Could not learn independently");d.learnOrPromote(p,id);h.assertTrue(PlayerCrafts.rank(p,id)==0,"Unproven promotion accepted");}
            var n=d.profession(p.getUUID(),"alchemy");n.putInt("Xp",100);d.receipt(p.getUUID(),"alchemy",new ItemStack(Items.APPLE),0);d.receipt(p.getUUID(),"alchemy",new ItemStack(Items.CARROT),0);d.learnOrPromote(p,"alchemy");h.assertTrue(PlayerCrafts.rank(p,"alchemy")==0,"Legacy item receipts bypassed independent certification");h.assertTrue(PlayerCrafts.rank(p,"refining")==0,"Cross profession contamination");
        }h.succeed();
    }
    @GameTest(template="empty") public static void offlineReceiptsPersistAndRepetitionDiminishes(GameTestHelper h){
        var d=new PlayerCrafts();var id=java.util.UUID.randomUUID();d.profession(id,"alchemy").putBoolean("Learned",true);var result=new ItemStack(Items.APPLE);
        d.receipt(id,"alchemy",result,0);int first=d.profession(id,"alchemy").getInt("Xp");for(int i=0;i<100;i++)d.receipt(id,"alchemy",result,0);
        var n=d.profession(id,"alchemy");int before=n.getInt("Xp");d.receipt(id,"alchemy",result,0);h.assertTrue(n.getInt("Xp")-before<first,"Repeat not diminished");
        var loaded=PlayerCrafts.load(d.save(new CompoundTag()));h.assertTrue(loaded.account(id).equals(d.account(id)),"Saved offline account lost data");
        n.putInt("Rank",4);before=n.getInt("Xp");d.receipt(id,"alchemy",result,0);h.assertTrue(before==n.getInt("Xp"),"Master gained XP from trivial work");h.succeed();
    }
    @GameTest(template="empty") public static void unlearnedAndEmptyProductionCannotGrantExperience(GameTestHelper h){var d=new PlayerCrafts();var id=java.util.UUID.randomUUID();d.receipt(id,"refining",new ItemStack(Items.DIAMOND),3);h.assertTrue(d.profession(id,"refining").getInt("Xp")==0,"Unlearned XP awarded");d.profession(id,"refining").putBoolean("Learned",true);d.receipt(id,"refining",ItemStack.EMPTY,4);h.assertTrue(d.profession(id,"refining").getInt("Xp")==0,"Empty output XP awarded");h.succeed();}
    @GameTest(template="empty") public static void openingIsRealmGatedAndMovementInterrupts(GameTestHelper h){try(var p=player(h,1)){var n=PlayerCrafts.get(p.server).account(p.getUUID());SpiritualSense.action(p,"open");ticks(p,110);h.assertTrue(!n.getBoolean("SenseOpen"),"Pre-golden core opened sense");}try(var p=player(h,2)){var n=PlayerCrafts.get(p.server).account(p.getUUID());SpiritualSense.action(p,"open");ticks(p,30);p.setPos(p.getX()+1,p.getY(),p.getZ());ticks(p,110);h.assertTrue(!n.getBoolean("SenseOpen"),"Moving opening succeeded");}h.succeed();}
    @GameTest(template="empty") public static void openingChargesCapacityWithoutChangingMana(GameTestHelper h){try(var p=player(h,2)){float mana=GeneratedMod.data(p).wuxingxiantu$getAura();SpiritualSense.action(p,"open");ticks(p,100);var n=PlayerCrafts.get(p.server).account(p.getUUID());h.assertTrue(n.getBoolean("SenseOpen")&&n.getFloat("Sense")==120,"Opening did not create correct independent resource");h.assertTrue(GeneratedMod.data(p).wuxingxiantu$getAura()==mana,"Sense changed mana");}h.succeed();}
    @GameTest(template="empty") public static void scanChargesOnceAndCannotSpamOrRunLocked(GameTestHelper h){try(var p=player(h,2)){var n=PlayerCrafts.get(p.server).account(p.getUUID());SpiritualSense.action(p,"scan");h.assertTrue(n.getFloat("Sense")==0,"Locked scan created resource");}try(var p=player(h,2)){var n=PlayerCrafts.get(p.server).account(p.getUUID());n.putBoolean("SenseOpen",true);n.putFloat("Sense",100);SpiritualSense.action(p,"scan");SpiritualSense.action(p,"scan");h.assertTrue(n.getFloat("Sense")==92,"Scan replay double charged or wrong cost");}h.succeed();}
    @GameTest(template="empty") public static void wallsAndRangeRejectLockVisibility(GameTestHelper h){try(var p=player(h,2)){var cow=EntityType.COW.create(h.getLevel());cow.setPos(p.getX(),p.getY(),p.getZ()+6);h.getLevel().addFreshEntity(cow);h.assertTrue(SpiritualSense.visible(p,cow,24),"Clear target not visible");var wall=BlockPos.containing(p.getX(),p.getEyeY(),p.getZ()+3);h.getLevel().setBlockAndUpdate(wall,Blocks.STONE.defaultBlockState());h.getLevel().setBlockAndUpdate(wall.below(),Blocks.STONE.defaultBlockState());h.assertTrue(!SpiritualSense.visible(p,cow,24),"Wall did not block lock");h.getLevel().setBlockAndUpdate(wall,Blocks.AIR.defaultBlockState());h.getLevel().setBlockAndUpdate(wall.below(),Blocks.AIR.defaultBlockState());cow.setPos(p.getX(),p.getY(),p.getZ()+25);h.assertTrue(!SpiritualSense.visible(p,cow,24),"Out-of-range lock allowed");cow.discard();}h.succeed();}
    @GameTest(template="empty") public static void actualLockConsumesUpkeepAndObstructionReleases(GameTestHelper h){try(var p=player(h,2)){
        p.setYRot(0);p.setXRot(0);var n=PlayerCrafts.get(p.server).account(p.getUUID());n.putBoolean("SenseOpen",true);n.putFloat("Sense",100);
        var cow=EntityType.COW.create(h.getLevel());cow.setPos(p.getX(),p.getY(),p.getZ()+6);h.getLevel().addFreshEntity(cow);SpiritualSense.action(p,"lock");h.assertTrue(n.getFloat("Sense")==96,"Actual ray lock failed");ticks(p,20);h.assertTrue(n.getFloat("Sense")==95,"Lock upkeep incorrect");
        var wall=BlockPos.containing(p.getX(),p.getEyeY(),p.getZ()+3);h.getLevel().setBlockAndUpdate(wall,Blocks.STONE.defaultBlockState());h.getLevel().setBlockAndUpdate(wall.below(),Blocks.STONE.defaultBlockState());ticks(p,20);h.assertTrue(n.getFloat("Sense")==95,"Occluded lock still charged upkeep");ticks(p,20);h.assertTrue(n.getFloat("Sense")>95,"Broken lock did not resume regeneration");cow.discard();h.getLevel().setBlockAndUpdate(wall,Blocks.AIR.defaultBlockState());h.getLevel().setBlockAndUpdate(wall.below(),Blocks.AIR.defaultBlockState());
    }h.succeed();}
    @GameTest(template="empty") public static void realmRequirementCannotBeBypassedWithForgedWorkStatistics(GameTestHelper h){try(var p=player(h,0)){
        var d=PlayerCrafts.get(p.server);var n=d.profession(p.getUUID(),"talisman");n.putBoolean("Learned",true);n.putInt("Rank",3);n.putInt("Xp",100000);n.putInt("BestTier",4);var works=new CompoundTag();for(int i=0;i<12;i++)works.putInt("test"+i,1);n.put("Works",works);d.learnOrPromote(p,"talisman");h.assertTrue(PlayerCrafts.rank(p,"talisman")==3,"Master bypassed realm gate");
    }h.succeed();}
    @GameTest(template="empty") public static void interruptedIdentificationDoesNotRefundItsResource(GameTestHelper h){try(var p=player(h,2)){var n=PlayerCrafts.get(p.server).account(p.getUUID());n.putBoolean("SenseOpen",true);n.putFloat("Sense",100);SpiritualSense.action(p,"identify");h.assertTrue(n.getFloat("Sense")==94,"Identify did not charge");p.setYRot(p.getYRot()+90);ticks(p,1);h.assertTrue(n.getFloat("Sense")==94,"Interrupted identify refunded paid resource");}h.succeed();}
    @GameTest(template="empty") public static void refiningEfficiencyRoundsUpAndRetainsZeroCost(GameTestHelper h){
        h.assertTrue(PlayerCrafts.refiningCost(0,4)==0,"Free recipe became charged");h.assertTrue(PlayerCrafts.refiningCost(1,4)==1,"Rounding created free recipe");h.assertTrue(PlayerCrafts.refiningCost(100,4)==90,"Fourth rank discount not ten percent");h.assertTrue(PlayerCrafts.refiningCost(Integer.MAX_VALUE,0)==Integer.MAX_VALUE,"Cost overflow");h.assertTrue(PlayerCrafts.refiningCost(100,100)==78,"Corrupt rank bypassed ninth rank discount cap");h.succeed();
    }
    @GameTest(template="empty") public static void talismanScoreUsesHundredPointScaleAndKeepsPerfectGlyphs(GameTestHelper h){
        for(String id:TalismanCatalog.ids())for(int rank=0;rank<=9;rank++){
            float score=TalismanCatalog.score(id,TalismanCatalog.glyph(id));h.assertTrue(PlayerCrafts.talismanScore(score,rank)==100,"Perfect glyph was damaged by profession bonus");h.assertTrue(TalismanCatalog.qualityOf(PlayerCrafts.talismanScore(score,rank))==TalismanCatalog.Quality.PERFECT,"Complete glyph became waste");
        }
        h.assertTrue(PlayerCrafts.talismanScore(0,4)==0&&PlayerCrafts.talismanScore(49,4)==49,"Bonus repaired empty/invalid strokes");h.assertTrue(PlayerCrafts.talismanScore(56,4)==60,"Four point bonus at threshold wrong");h.assertTrue(PlayerCrafts.talismanScore(76,4)==80&&PlayerCrafts.talismanScore(91,4)==95,"Quality thresholds not preserved");h.succeed();
    }
    @GameTest(template="empty") public static void talismanExperienceRequiresActualMaterialAndCommittedOutput(GameTestHelper h){try(var p=player(h,2)){
        var n=PlayerCrafts.get(p.server).profession(p.getUUID(),"talisman");n.putBoolean("Learned",true);
        try{var method=cn.blockforge.generated.generatedmod.network.service.TalismanService.class.getDeclaredMethod("handlePlatformSubmit",net.minecraft.server.level.ServerLevel.class,TalismanPlatformBlockEntity.class,String.class,TalismanCatalog.Quality.class,boolean.class,net.minecraft.server.level.ServerPlayer.class);method.setAccessible(true);
            String id=TalismanCatalog.ids().get(0);var rub=new TalismanPlatformBlockEntity(BlockPos.ZERO,WuXingBlocks.RUBBING_PLATFORM.get().defaultBlockState(),TalismanPlatformBlockEntity.Kind.RUB);method.invoke(null,h.getLevel(),rub,id,TalismanCatalog.Quality.PERFECT,true,p);h.assertTrue(n.getInt("Xp")==0,"Forged draw on rubbing table farmed XP");
            var draw=new TalismanPlatformBlockEntity(BlockPos.ZERO,WuXingBlocks.DRAWING_PLATFORM.get().defaultBlockState(),TalismanPlatformBlockEntity.Kind.DRAW);method.invoke(null,h.getLevel(),draw,id,TalismanCatalog.Quality.PERFECT,true,p);h.assertTrue(n.getInt("Xp")==0,"Missing materials awarded XP");
            draw.setItem(TalismanPlatformBlockEntity.DRAW_PAPER,new ItemStack(WuXingItems.TALISMAN_PAPER.get()));draw.setItem(1,new ItemStack(WuXingItems.SPIRIT_INK.get()));method.invoke(null,h.getLevel(),draw,id,TalismanCatalog.Quality.PERFECT,true,p);h.assertTrue(n.getInt("Xp")>0&&!draw.getItem(TalismanPlatformBlockEntity.OUTPUT).isEmpty()&&draw.getItem(TalismanPlatformBlockEntity.DRAW_PAPER).isEmpty(),"Actual successful draw did not settle material and XP");
            int xp=n.getInt("Xp");draw.setItem(TalismanPlatformBlockEntity.DRAW_PAPER,new ItemStack(WuXingItems.TALISMAN_PAPER.get()));draw.setItem(TalismanPlatformBlockEntity.OUTPUT,new ItemStack(Items.STONE,64));method.invoke(null,h.getLevel(),draw,id,TalismanCatalog.Quality.PERFECT,true,p);h.assertTrue(n.getInt("Xp")==xp&&draw.getItem(TalismanPlatformBlockEntity.DRAW_PAPER).getCount()==1,"Blocked output awarded XP or consumed material");
        }catch(ReflectiveOperationException ex){throw new AssertionError(ex);}
    }h.succeed();}
    @SuppressWarnings("unchecked") @GameTest(template="empty") public static void hiddenScanNeverSendsExactTargetOrWorkHistory(GameTestHelper h){try(var p=player(h,2)){
        var n=PlayerCrafts.get(p.server).account(p.getUUID());n.putBoolean("SenseOpen",true);n.putFloat("Sense",100);var craft=PlayerCrafts.get(p.server).profession(p.getUUID(),"alchemy");craft.putBoolean("Learned",true);PlayerCrafts.get(p.server).receipt(p.getUUID(),"alchemy",new ItemStack(Items.APPLE),0);
        var cow=EntityType.COW.create(h.getLevel());cow.setPos(p.getX(),p.getY(),p.getZ()+6);h.getLevel().addFreshEntity(cow);var wall=BlockPos.containing(p.getX(),p.getEyeY(),p.getZ()+3);h.getLevel().setBlockAndUpdate(wall,Blocks.STONE.defaultBlockState());h.getLevel().setBlockAndUpdate(wall.below(),Blocks.STONE.defaultBlockState());SpiritualSense.action(p,"scan");
        try{var active=SpiritualSense.class.getDeclaredField("ACTIVE");active.setAccessible(true);Object s=((java.util.Map<java.util.UUID,?>)active.get(null)).get(p.getUUID());var f=s.getClass().getDeclaredField("lastState");f.setAccessible(true);var state=(CompoundTag)f.get(s);for(int id:state.getIntArray("ScanTargets"))h.assertTrue(id!=cow.getId(),"Exact hidden entity leaked");h.assertTrue(!state.getCompound("alchemy").contains("Works")&&state.getCompound("alchemy").getInt("WorksCount")==1,"Full work ledger sent to client");}catch(ReflectiveOperationException ex){throw new AssertionError(ex);}
        h.getLevel().setBlockAndUpdate(wall,Blocks.AIR.defaultBlockState());h.getLevel().setBlockAndUpdate(wall.below(),Blocks.AIR.defaultBlockState());cow.discard();
    }h.succeed();}
}
