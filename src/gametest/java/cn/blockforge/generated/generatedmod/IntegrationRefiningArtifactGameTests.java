package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.artifacts.*;
import cn.blockforge.generated.generatedmod.crafting.*;
import cn.blockforge.generated.generatedmod.economy.*;
import cn.blockforge.generated.generatedmod.fixtures.GameTestPlayer;
import cn.blockforge.generated.generatedmod.items.HeartSwordItem;
import cn.blockforge.generated.generatedmod.network.service.*;
import cn.blockforge.generated.generatedmod.progression.PlayerCrafts;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.gametest.*;
import java.util.*;

@GameTestHolder("wuxingxiantu_entry") @PrefixGameTestTemplate(false)
public final class IntegrationRefiningArtifactGameTests {
    private static BlockPos forge(GameTestHelper h){
        var p=h.absolutePos(new BlockPos(2,1,2));
        for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)if(x!=0||z!=0){h.getLevel().setBlockAndUpdate(p.offset(x,0,z),Math.abs(x)==1&&Math.abs(z)==1?Blocks.POLISHED_BLACKSTONE_BRICK_SLAB.defaultBlockState():WuXingBlocks.RUNE_STONE.get().defaultBlockState());h.getLevel().setBlockAndUpdate(p.offset(x,1,z),WuXingBlocks.RUNE_STONE.get().defaultBlockState());}
        h.getLevel().setBlockAndUpdate(p,WuXingBlocks.REFINING_FORGE_CORE.get().defaultBlockState());h.getLevel().setBlockAndUpdate(p.above(),WuXingBlocks.ETERNAL_SPIRIT_COAL_BLOCK.get().defaultBlockState());h.getLevel().setBlockAndUpdate(p.above(2),ExoticFireState.stateForTier(9,Blocks.FIRE.defaultBlockState()));
        h.assertTrue(RefiningForgeBlock.isStructureComplete(h.getLevel(),p),"Forge fixture incomplete");return p;
    }
    private static GameTestPlayer player(GameTestHelper h,BlockPos pos){var p=new GameTestPlayer(h.getLevel());p.join();p.setPos(pos.getX()+.5,pos.getY()+1,pos.getZ()+.5);p.setShiftKeyDown(true);var d=GeneratedMod.data(p);d.wuxingxiantu$setRealm(8);d.wuxingxiantu$setLearnedFires(new int[]{9});d.setCurrentMana(1000);var row=PlayerCrafts.get(p.server).profession(p.getUUID(),"refining");row.putBoolean("Learned",true);row.putInt("Rank",8);return p;}
    private static ItemStack artifact(ArtifactKind kind,UUID owner){var s=new ItemStack(ArtifactBootstrap.ARTIFACTS.get(kind).get());s.getOrCreateTag().putUUID(HeartSwordItem.OWNER_TAG,owner);return s;}

    @GameTest(template="empty") public static void infusionChangeFailureIsAtomicAndSavedCandidateCanBeAdoptedOnlyOnceByOwner(GameTestHelper h){
        var pos=forge(h);try(var p=player(h,pos);var other=player(h,pos)){
            for(int i=0;i<36;i++)p.getInventory().setItem(i,new ItemStack(Items.COBBLESTONE,64));
            p.setItemInHand(InteractionHand.MAIN_HAND,artifact(ArtifactKind.SWORD,p.getUUID()));p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ArtifactBootstrap.AUXILIARIES.get(ArtifactAux.XUANTIE).get(),2));p.getInventory().setItem(20,new ItemStack(SpiritStoneGrade.EXQUISITE.item(),2));
            var before=p.getInventory().items.stream().map(ItemStack::copy).toList();var off=p.getOffhandItem().copy();float aura=GeneratedMod.data(p).getCurrentMana();
            h.assertTrue(!ArtifactInfusion.apply(p,pos),"Full backpack accepted unplaceable change");
            for(int i=0;i<36;i++)h.assertTrue(ItemStack.matches(before.get(i),p.getInventory().getItem(i)),"Failed infusion changed slot "+i);
            h.assertTrue(ItemStack.matches(off,p.getOffhandItem())&&GeneratedMod.data(p).getCurrentMana()==aura,"Failed infusion spent auxiliary or mana");
            for(int i=21;i<=23;i++)p.getInventory().setItem(i,ItemStack.EMPTY);long value=SpiritCurrency.value(p.getInventory().items);
            h.assertTrue(ArtifactInfusion.apply(p,pos),"Valid auxiliary infusion failed");
            h.assertTrue(SpiritCurrency.value(p.getInventory().items)==value-4&&p.getOffhandItem().getCount()==1&&GeneratedMod.data(p).getCurrentMana()==aura-12,"Infusion charged incorrect resources");
            var saved=ItemStack.of(p.getMainHandItem().save(new CompoundTag()));h.assertTrue(EquipmentCrafting.read(saved).contains("Pending"),"Save lost pending auxiliary");
            h.assertTrue(!EquipmentCrafting.choose(other,saved,true)&&EquipmentCrafting.read(saved).contains("Pending"),"Foreign owner adopted candidate");
            p.setItemInHand(InteractionHand.MAIN_HAND,saved);h.assertTrue(EquipmentCrafting.choose(p,saved,true)&&!EquipmentCrafting.choose(p,saved,true),"Candidate not adopted exactly once");
            h.assertTrue(p.getUUID().equals(EquipmentBond.owner(saved))&&EquipmentCrafting.read(saved).getInt("TemperBp")==6000,"Candidate lost owner or target growth state");
        }h.succeed();
    }

    @GameTest(template="empty") public static void interruptedSavedRefiningReturnsTaggedMaterialsAndManaOnlyOnce(GameTestHelper h){
        var pos=forge(h);var jobs=CraftingJobs.get(h.getLevel().getServer());var key=new CraftingJobs.JobKey(h.getLevel().dimension(),pos);
        try(var p=player(h,pos)){try{
            String marker=UUID.randomUUID().toString();var sword=new ItemStack(Items.IRON_SWORD);sword.getOrCreateTag().putUUID(HeartSwordItem.OWNER_TAG,p.getUUID());sword.setHoverName(Component.literal("Escrow sword"));sword.setDamageValue(7);
            var parts=List.of(sword,new ItemStack(WuXingItems.DARK_IRON.get(),4),new ItemStack(WuXingItems.QUENCH_ESSENCE.get()));for(var part:parts)part.getOrCreateTag().putString("RefiningEscrow",marker);
            long now=p.server.overworld().getGameTime();var job=new CraftingJobs.PendingRefining(p.getUUID(),h.getLevel().dimension(),pos,now+100,artifact(ArtifactKind.SWORD,p.getUUID()),parts,17);
            jobs.add(job);var reloaded=CraftingJobs.load(jobs.save(new CompoundTag())).refining(key);jobs.remove(CraftingJobs.Kind.REFINING,key);jobs.add(reloaded);
            var be=(RefiningForgeBlockEntity)h.getLevel().getBlockEntity(pos);be.startCrafting(now,now+100,"wuxingxiantu:refining_dark_iron_sword",p.getUUID());
            h.getLevel().setBlockAndUpdate(pos.offset(1,1,0),Blocks.AIR.defaultBlockState());
            jobs.schedule(CraftingJobs.Kind.REFINING,key,now);AlchemyService.tickAlchemy(p.server);AlchemyService.tickAlchemy(p.server);
            var drops=h.getLevel().getEntitiesOfClass(ItemEntity.class,new AABB(pos).inflate(4),e->marker.equals(e.getItem().getOrCreateTag().getString("RefiningEscrow")));
            h.assertTrue(jobs.refining(key)==null&&!be.hasPendingReward()&&drops.stream().mapToInt(e->e.getItem().getCount()).sum()==6,"Interruption duplicated or lost escrow, or also made a reward");
            for(var expected:parts)h.assertTrue(drops.stream().filter(e->ItemStack.isSameItemSameTags(e.getItem(),expected)).mapToInt(e->e.getItem().getCount()).sum()==expected.getCount(),"Refund lost name, damage, owner or NBT");
            h.assertTrue(jobs.refunds.getOrDefault(p.getUUID(),0F)==17F,"Refund credit missing or duplicated");
            float mana=GeneratedMod.data(p).getCurrentMana();AlchemyService.deliverRefund(p);AlchemyService.deliverRefund(p);
            h.assertTrue(GeneratedMod.data(p).getCurrentMana()==mana+17&&!jobs.refunds.containsKey(p.getUUID()),"Refund paid twice or went to a second mana account");drops.forEach(ItemEntity::discard);
        }finally{jobs.remove(CraftingJobs.Kind.REFINING,key);jobs.refunds.remove(p.getUUID());}}h.succeed();
    }

    @GameTest(template="empty") public static void allSixPendingArtifactRewardsKeepOwnerAndGrowthAcrossSaveAndSingleTake(GameTestHelper h){
        var pos=forge(h);var be=(RefiningForgeBlockEntity)h.getLevel().getBlockEntity(pos);var owner=UUID.randomUUID();
        for(var kind:ArtifactKind.values()){
            var expected=artifact(kind,owner);expected.setHoverName(Component.literal("Named "+kind.id));var data=EquipmentCrafting.write(expected);data.putInt("Refine",7);data.putInt("BondNurture",65);data.putInt("ArtifactQuenchConduct",3);
            be.setPendingReward(expected);be.load(be.saveWithoutMetadata());var actual=be.takePendingReward();
            h.assertTrue(ItemStack.matches(expected,actual)&&owner.equals(EquipmentBond.owner(actual)),"Saved reward lost naming, owner or growth");
            h.assertTrue(be.takePendingReward().isEmpty(),"Second take duplicated reward");be.load(be.saveWithoutMetadata());h.assertTrue(!be.hasPendingReward(),"Consumed reward returned after save");
        }h.succeed();
    }
}
