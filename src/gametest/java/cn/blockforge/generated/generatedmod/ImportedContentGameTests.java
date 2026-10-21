package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.fixtures.GameTestPlayer;
import cn.blockforge.generated.generatedmod.importedcontent.*;
import cn.blockforge.generated.generatedmod.items.CultivationLogic;
import cn.blockforge.generated.generatedmod.plants.HerbAge;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.*;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
import net.minecraftforge.gametest.*;
import java.util.*;

@GameTestHolder("wuxingxiantu_entry")
@PrefixGameTestTemplate(false)
public final class ImportedContentGameTests {
    private static ItemStack herb(String id) {var stack=new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation("wuxingxiantu",id)));HerbAge.setYears(stack,2500);return stack;}
    private static BlockHitResult hit(BlockPos pos) {return new BlockHitResult(Vec3.atCenterOf(pos),Direction.UP,pos,false);}
    private static BlockPos position(GameTestHelper helper,int height) {return helper.absolutePos(BlockPos.ZERO).atY(height);}
    private static void use(GameTestHelper helper,GameTestPlayer player,BlockPos pos) {
        var state=helper.getLevel().getBlockState(pos);state.getBlock().use(state,helper.getLevel(),pos,player,InteractionHand.MAIN_HAND,hit(pos));
    }
    @GameTest(template="empty") public static void bathRealInteractionPreservesHerbsRejectsDuplicatesAndReturnsPartialBatch(GameTestHelper helper) {
        var pos=position(helper,230);helper.getLevel().setBlock(pos,ImportedContent.SPIRIT_BATH.get().defaultBlockState(),3);
        var bath=(SpiritBathBlockEntity)helper.getLevel().getBlockEntity(pos);
        try(var player=new GameTestPlayer(helper.getLevel())) {
            var stack=herb(SpiritBathBlockEntity.HERBS.get(0));stack.setCount(3);stack.getOrCreateTag().putString("ThirdParty","keep");
            player.setItemInHand(InteractionHand.MAIN_HAND,stack);use(helper,player,pos);use(helper,player,pos);
            helper.assertTrue(stack.getCount()==2&&bath.depositCount()==1,"Duplicate herb was consumed or accepted");
            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.DIRT,3));use(helper,player,pos);
            helper.assertTrue(player.getMainHandItem().getCount()==3&&bath.depositCount()==1,"Wrong item entered the bath");
            var snapshot=bath.saveWithoutMetadata();var restored=new SpiritBathBlockEntity(pos,bath.getBlockState());restored.load(snapshot);
            helper.assertTrue(restored.depositCount()==1&&restored.herbs().get(0).getTag().getString("ThirdParty").equals("keep")&&HerbAge.years(restored.herbs().get(0))==2500,"Bath save lost herb provenance");
            restored.load(new CompoundTag());helper.assertTrue(restored.depositCount()==0,"Sparse load retained an old herb");
            helper.getLevel().setBlock(pos,Blocks.AIR.defaultBlockState(),3);
            var drops=helper.getLevel().getEntitiesOfClass(ItemEntity.class,new AABB(pos).inflate(2),entity->entity.getItem().is(stack.getItem()));
            try {helper.assertTrue(drops.size()==1&&drops.get(0).getItem().getCount()==1&&drops.get(0).getItem().getTag().getString("ThirdParty").equals("keep"),"Breaking a partial bath lost or duplicated its herb");}
            finally {drops.forEach(ItemEntity::discard);}
        } finally {helper.getLevel().setBlock(pos,Blocks.AIR.defaultBlockState(),3);}
        helper.succeed();
    }
    @GameTest(template="empty",timeoutTicks=260) public static void actualBathTickerCannotSkipMajorRealmGate(GameTestHelper helper) {
        actualBathTicker(helper,true);
    }
    @GameTest(template="empty",timeoutTicks=260) public static void actualBathTickerRetainsTargetAutomaticMinorProgression(GameTestHelper helper) {
        actualBathTicker(helper,false);
    }
    private static void actualBathTicker(GameTestHelper helper,boolean majorGate) {
        var pos=position(helper,230);helper.getLevel().setBlock(pos,ImportedContent.SPIRIT_BATH.get().defaultBlockState(),3);
        var player=new GameTestPlayer(helper.getLevel());player.join();player.setNoGravity(true);player.keepTicking(helper.getLevel(),pos);
        player.setPos(pos.getX()+.5,pos.getY()+.3,pos.getZ()+.5);
        player.cleanupAfter(helper,()->helper.getLevel().setBlock(pos,Blocks.AIR.defaultBlockState(),3));
        var data=GeneratedMod.data(player);PlayerInitialization.initializeIfNeeded(data,player.getRandom());
        helper.assertTrue(data.wuxingxiantu$getRealm()==0&&data.wuxingxiantu$getRootMask()!=0&&data.wuxingxiantu$isInitialized(),"Target new-player entry is not already initialized in qi condensation");
        // Only the final minor stage has a major-realm gate. Earlier full stages
        // legitimately advance through ServerTickEvents.advanceMinorStage.
        if(majorGate)data.wuxingxiantu$setCultivationStage(3);
        int roots=data.wuxingxiantu$getRootMask(),realm=data.wuxingxiantu$getRealm(),stage=data.wuxingxiantu$getCultivationStage();
        long cap=CultivationLogic.breakthroughRequirement(data);data.wuxingxiantu$setCultivation(cap-1);
        for(String id:SpiritBathBlockEntity.HERBS) {player.setItemInHand(InteractionHand.MAIN_HAND,herb(id));use(helper,player,pos);}
        var bath=(SpiritBathBlockEntity)helper.getLevel().getBlockEntity(pos);
        helper.assertTrue(bath.isReady()&&bath.getBlockState().getValue(SpiritBathBlock.FILLED),"Four source herbs did not make a live bath");
        // Repeated calls in the same service tick must never complete a timed bath.
        for(int i=0;i<210;i++)bath.serverTick();
        helper.assertTrue(data.wuxingxiantu$getCultivation()==cap-1&&bath.isReady(),"Bath could be accelerated by duplicate tick calls");
        long startedAt=helper.getLevel().getGameTime();
        helper.runAfterDelay(215,()->{
            String soak;
            try {
                var field=SpiritBathBlockEntity.class.getDeclaredField("soaking");field.setAccessible(true);
                soak=String.valueOf(field.get(bath));
            } catch(ReflectiveOperationException error) {throw new IllegalStateException(error);}
            String state=" [elapsed="+(helper.getLevel().getGameTime()-startedAt)+", pos="+player.position()
                    +", bath="+pos+", alive="+player.isAlive()+", health="+player.getHealth()
                    +", mode="+player.gameMode.getGameModeForPlayer()+", cultivation="+data.wuxingxiantu$getCultivation()+"/"+cap
                    +", daoXin="+data.wuxingxiantu$getDaoXin()+", ready="+bath.isReady()
                    +", filled="+bath.getBlockState().getValue(SpiritBathBlock.FILLED)+", removed="+bath.isRemoved()
                    +", sameEntity="+(helper.getLevel().getBlockEntity(pos)==bath)+", soak="+soak+"]";
            long expectedCultivation=majorGate?cap:0;
            int expectedStage=majorGate?stage:stage+1;
            helper.assertTrue(data.wuxingxiantu$getCultivation()==expectedCultivation&&!bath.isReady()&&!bath.getBlockState().getValue(SpiritBathBlock.FILLED),"Actual server block ticker did not nourish and consume exactly one batch"+state);
            helper.assertTrue(data.wuxingxiantu$getRealm()==realm&&data.wuxingxiantu$getCultivationStage()==expectedStage&&data.wuxingxiantu$getRootMask()==roots,"Bath bypassed or blocked the target progression rules"+state);
            helper.succeed();
        });
    }
    @GameTest(template="empty") public static void bathCannotConsumeBlockedOrAlreadyFullPlayersBatch(GameTestHelper helper) {
        var pos=position(helper,230);helper.getLevel().setBlock(pos,ImportedContent.SPIRIT_BATH.get().defaultBlockState(),3);
        try(var player=new GameTestPlayer(helper.getLevel())) {
            player.join();player.setPos(pos.getX()+.5,pos.getY()+.3,pos.getZ()+.5);player.setNoGravity(true);
            var data=GeneratedMod.data(player);data.wuxingxiantu$setCultivation(CultivationLogic.breakthroughRequirement(data));
            for(String id:SpiritBathBlockEntity.HERBS) {player.setItemInHand(InteractionHand.MAIN_HAND,herb(id));use(helper,player,pos);}
            var bath=(SpiritBathBlockEntity)helper.getLevel().getBlockEntity(pos);
            for(int i=0;i<210;i++)bath.serverTick();helper.assertTrue(bath.isReady(),"Full cultivation consumed the batch");
            data.wuxingxiantu$restoreCultivation(0);data.wuxingxiantu$setDaoXin(4);
            for(int i=0;i<210;i++)bath.serverTick();helper.assertTrue(bath.isReady()&&data.wuxingxiantu$getCultivation()==0,"Bath bypassed the target cultivation lock");
        } finally {helper.getLevel().setBlock(pos,Blocks.AIR.defaultBlockState(),3);helper.getLevel().getEntitiesOfClass(ItemEntity.class,new AABB(pos).inflate(2)).forEach(ItemEntity::discard);}
        helper.succeed();
    }
    @GameTest(template="empty",timeoutTicks=90) public static void dollsKeepDirectionDecorationAndTwoSecondPerPlayerRealDispensing(GameTestHelper helper) {
        var pos=position(helper,250);helper.getLevel().setBlock(pos,ImportedContent.LAOYANG_DOLL.get().defaultBlockState(),3);
        var player=new GameTestPlayer(helper.getLevel());player.setPos(pos.getX()+.5,pos.getY()+1,pos.getZ()+.5);
        player.cleanupAfter(helper,()->{helper.getLevel().setBlock(pos,Blocks.AIR.defaultBlockState(),3);helper.getLevel().getEntitiesOfClass(ItemEntity.class,new AABB(pos).inflate(3),entity->entity.getItem().is(ImportedContent.LAOYANG_PILL.get())).forEach(ItemEntity::discard);});
        var decorative=ImportedContent.CHENGRAN_DOLL.get().defaultBlockState();
        var result=decorative.getBlock().use(decorative,helper.getLevel(),pos,player,InteractionHand.MAIN_HAND,hit(pos));
        helper.assertTrue(result==InteractionResult.PASS&&!player.getPersistentData().contains(LaoyangDollBlock.NEXT),"Chengran decorative doll dispensed a pill");
        helper.assertTrue(decorative.rotate(Rotation.CLOCKWISE_90).getValue(LaoyangDollBlock.FACING)==Direction.EAST,"Doll facing/rotation lost");
        for(int i=0;i<player.getInventory().items.size();i++)player.getInventory().items.set(i,new ItemStack(Items.DIRT,64));
        use(helper,player,pos);use(helper,player,pos);
        var area=new AABB(pos).inflate(3);
        int first=helper.getLevel().getEntitiesOfClass(ItemEntity.class,area,entity->entity.getItem().is(ImportedContent.LAOYANG_PILL.get())).stream().mapToInt(entity->entity.getItem().getCount()).sum();
        helper.assertTrue(first==1,"Full inventory lost the pill or repeated click bypassed cooldown");
        // The airborne fixture has no floor; keep the already verified drop local while waiting for the cooldown.
        for(var item:helper.getLevel().getEntitiesOfClass(ItemEntity.class,area,entity->entity.getItem().is(ImportedContent.LAOYANG_PILL.get()))) {
            item.setNoGravity(true);item.setDeltaMovement(Vec3.ZERO);
        }
        helper.runAfterDelay(41,()->{
            use(helper,player,pos);
            int count=helper.getLevel().getEntitiesOfClass(ItemEntity.class,area,entity->entity.getItem().is(ImportedContent.LAOYANG_PILL.get())).stream().mapToInt(entity->entity.getItem().getCount()).sum();
            helper.assertTrue(count==2,"The doll failed to produce its next pill after forty real server ticks");helper.succeed();
        });
    }
    @GameTest(template="empty") public static void secretPillRealUseRetainsBothOriginalLevelTenOutcomes(GameTestHelper helper) {
        try(var player=new GameTestPlayer(helper.getLevel())) {
            for(boolean good:List.of(true,false)) {
                boolean found=false;
                for(long seed=0;seed<8192;seed++) {player.getRandom().setSeed(seed);if(player.getRandom().nextBoolean()==good) {player.getRandom().setSeed(seed);found=true;break;}}
                helper.assertTrue(found,"Could not select the deterministic outcome fixture");player.removeAllEffects();
                var pill=new ItemStack(ImportedContent.LAOYANG_PILL.get(),2);player.setItemInHand(InteractionHand.MAIN_HAND,pill);
                pill.getItem().use(helper.getLevel(),player,InteractionHand.MAIN_HAND);
                helper.assertTrue(player.isUsingItem()&&pill.getUseDuration()==32&&pill.getUseAnimation()==UseAnim.EAT,"Secret pill lost its original eating entry");
                pill.getItem().finishUsingItem(pill,helper.getLevel(),player);player.stopUsingItem();
                helper.assertTrue(pill.getCount()==1,"Secret pill did not consume exactly one item");
                var effect=player.getEffect(good?MobEffects.REGENERATION:MobEffects.POISON);
                helper.assertTrue(effect!=null&&effect.getAmplifier()==9&&effect.getDuration()==(good?600:200),"Secret pill changed its original X/30s or X/10s result");
                if(!good)helper.assertTrue(player.hasEffect(MobEffects.CONFUSION)&&player.getEffect(MobEffects.CONFUSION).getDuration()==200,"Negative branch lost its ten seconds of nausea");
            }
        }
        helper.succeed();
    }
}
