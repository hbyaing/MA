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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.gametest.*;
import java.util.*;

@GameTestHolder("wuxingxiantu_entry") @PrefixGameTestTemplate(false)
public final class ArtifactGameTests {
    private static ItemStack artifact(ArtifactKind kind){return new ItemStack(ArtifactBootstrap.ARTIFACTS.get(kind).get());}
    private static BlockPos forge(GameTestHelper h){
        var pos=h.absolutePos(new BlockPos(2,1,2));
        for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)if(x!=0||z!=0){
            h.getLevel().setBlock(pos.offset(x,0,z),Math.abs(x)==1&&Math.abs(z)==1?Blocks.POLISHED_BLACKSTONE_BRICK_SLAB.defaultBlockState():WuXingBlocks.RUNE_STONE.get().defaultBlockState(),3);
            h.getLevel().setBlock(pos.offset(x,1,z),WuXingBlocks.RUNE_STONE.get().defaultBlockState(),3);
        }
        h.getLevel().setBlock(pos,WuXingBlocks.REFINING_FORGE_CORE.get().defaultBlockState(),3);
        h.getLevel().setBlock(pos.above(),WuXingBlocks.ETERNAL_SPIRIT_COAL_BLOCK.get().defaultBlockState(),3);
        h.getLevel().setBlock(pos.above(2),ExoticFireState.stateForTier(9,Blocks.FIRE.defaultBlockState()),3);
        h.assertTrue(RefiningForgeBlock.isStructureComplete(h.getLevel(),pos),"Incomplete artifact forge fixture");return pos;
    }
    private static GameTestPlayer player(GameTestHelper h,BlockPos pos){
        var p=new GameTestPlayer(h.getLevel());p.join();p.setPos(pos.getX()+.5,pos.getY()+1,pos.getZ()+.5);p.setShiftKeyDown(true);
        var data=GeneratedMod.data(p);data.wuxingxiantu$setRealm(8);data.wuxingxiantu$setLearnedFires(new int[]{9});data.wuxingxiantu$setAura(1000);
        var profession=PlayerCrafts.get(p.server).profession(p.getUUID(),"refining");profession.putBoolean("Learned",true);profession.putInt("Rank",9);
        p.getInventory().setItem(20,new ItemStack(SpiritStoneGrade.values()[3].item(),2));return p;
    }
    @GameTest(template="empty") public static void auxiliaryEmbryosRequireTargetChoiceAndNeverDoubleStack(GameTestHelper h){
        try(var p=new GameTestPlayer(h.getLevel())){
            for(var aux:List.of(ArtifactAux.XUANTIE,ArtifactAux.XINGTIE,ArtifactAux.YAOYANG)){
                var sword=artifact(ArtifactKind.SWORD);float base=ArtifactData.attack(sword);ArtifactData.infuse(sword,aux);
                var pending=EquipmentCrafting.read(sword).getCompound("Pending");int bp=aux.fire()>=5?10000:aux.fire()>=3?8500:6000;
                h.assertTrue(pending.getInt("Type")==0&&pending.getInt("Bp")==bp&&ArtifactData.attack(sword)==base&&EquipmentCrafting.temper(sword,0)==0,"Sharp was applied outside the target pending embryo");
                var first=pending.copy();ArtifactData.infuse(sword,ArtifactAux.YAOYANG);h.assertTrue(EquipmentCrafting.read(sword).getCompound("Pending").equals(first),"An unresolved candidate was overwritten");
                h.assertTrue(EquipmentCrafting.choose(p,sword,true)&&EquipmentCrafting.temper(sword,0)==bp/10000D&&ArtifactData.attack(sword)==base,"Target adoption failed or source attack was also added");
            }
            var armor=artifact(ArtifactKind.ARMOR);int base=armor.getMaxDamage();ArtifactData.infuse(armor,ArtifactAux.XUANGUIJIA);
            h.assertTrue(armor.getMaxDamage()==base&&EquipmentCrafting.read(armor).getCompound("Pending").getInt("Type")==1,"Tenacity altered maximum durability before adoption");
            h.assertTrue(EquipmentCrafting.choose(p,armor,true)&&armor.getMaxDamage()==Math.round(base*1.072),"ItemStack durability mixin did not apply the single target Sturdy bonus");
            int adopted=armor.getMaxDamage();ArtifactData.infuse(armor,ArtifactAux.QINGMU);h.assertTrue(armor.getMaxDamage()==adopted,"A new candidate immediately stacked durability");
            h.assertTrue(EquipmentCrafting.choose(p,armor,false)&&armor.getMaxDamage()==adopted,"Discarding a candidate changed accepted durability");
            double toughness=armor.getAttributeModifiers(EquipmentSlot.CHEST).get(Attributes.ARMOR_TOUGHNESS).stream().mapToDouble(v->v.getAmount()).sum();
            h.assertTrue(toughness==2&&EquipmentCrafting.level(armor,"tough")==0,"Unique armor toughness stacked or was confused with the wear-reduction rune");
            EquipmentCrafting.setRune(armor,"tough",3,0);h.assertTrue(armor.getMaxDamage()==adopted,"The independent Tough wear-reduction rune increased maximum durability");
            var baseline=artifact(ArtifactKind.SWORD);float attack=ArtifactData.attack(baseline);ArtifactData.infuse(baseline,ArtifactAux.LONGXUE);
            h.assertTrue(ArtifactData.attack(baseline)==attack&&ArtifactData.has(baseline,ArtifactAux.Trait.BLOODTHIRST),"Non-sharp auxiliary retained a second flat attack bonus or lost its unique trait");
        }h.succeed();
    }
    @GameTest(template="empty") public static void infusionUsesRealStationCostsAndRejectsParalysisAndForeignOwners(GameTestHelper h){
        var pos=forge(h);try(var p=player(h,pos);var other=player(h,pos)){
            p.setItemInHand(InteractionHand.MAIN_HAND,artifact(ArtifactKind.SWORD));p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ArtifactBootstrap.AUXILIARIES.get(ArtifactAux.XUANTIE).get(),2));
            long stones=SpiritCurrency.value(p.getInventory().items);float aura=GeneratedMod.data(p).wuxingxiantu$getAura();
            p.addEffect(new MobEffectInstance(ModEffects.PARALYSIS.get(),100,0));h.assertTrue(!ArtifactInfusion.apply(p,pos)&&p.getOffhandItem().getCount()==2&&SpiritCurrency.value(p.getInventory().items)==stones,"Paralysis consumed auxiliary or payment");p.removeEffect(ModEffects.PARALYSIS.get());
            h.assertTrue(ArtifactInfusion.apply(p,pos)&&p.getOffhandItem().getCount()==1&&SpiritCurrency.value(p.getInventory().items)==stones-4&&GeneratedMod.data(p).wuxingxiantu$getAura()==aura-12,"Real infusion costs or held inventory replacement failed");
            h.assertTrue(EquipmentCrafting.read(p.getMainHandItem()).contains("Pending")&&!ArtifactInfusion.apply(p,pos)&&p.getOffhandItem().getCount()==1,"Pending candidate could be replayed");
            var foreign=p.getMainHandItem().copy();foreign.getOrCreateTag().putUUID(HeartSwordItem.OWNER_TAG,p.getUUID());other.setItemInHand(InteractionHand.MAIN_HAND,foreign);other.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ArtifactBootstrap.AUXILIARIES.get(ArtifactAux.XUANTIE).get()));
            h.assertTrue(!ArtifactInfusion.apply(other,pos)&&!EquipmentCrafting.choose(other,foreign,true)&&other.getOffhandItem().getCount()==1,"A foreign owner could infuse or adopt");
        }h.succeed();
    }
    @GameTest(template="empty") public static void sourceArtifactsUseOneOwnerNurtureAndLifeBond(GameTestHelper h){
        var pos=forge(h);try(var p=player(h,pos);var other=player(h,pos)){
            p.setItemInHand(InteractionHand.MAIN_HAND,artifact(ArtifactKind.HAMMER));EquipmentBond.ritual(p,pos,p.getMainHandItem());
            h.assertTrue(p.getUUID().equals(EquipmentBond.owner(p.getMainHandItem()))&&!ArtifactData.life(p.getMainHandItem()),"Claim created an independent owner or premature life bond");
            var craft=EquipmentCrafting.write(p.getMainHandItem());craft.putLong("BondLastTick",h.getLevel().getGameTime()-60);float attack=ArtifactData.attack(p.getMainHandItem());EquipmentBond.ritual(p,pos,p.getMainHandItem());
            h.assertTrue(EquipmentCrafting.read(p.getMainHandItem()).getInt("BondNurture")==5&&ArtifactData.attack(p.getMainHandItem())>attack,"Target nurture did not drive source growth");
            var held=p.getMainHandItem();other.setItemInHand(InteractionHand.MAIN_HAND,held.copy());EquipmentBond.ritual(other,pos,other.getMainHandItem());h.assertTrue(p.getUUID().equals(EquipmentBond.owner(other.getMainHandItem())),"A second player rebound the artifact");
            craft=EquipmentCrafting.write(held);craft.putInt("BondNurture",100);craft.putLong("BondLastTick",h.getLevel().getGameTime()-60);EquipmentBond.ritual(p,pos,held);
            h.assertTrue(ArtifactData.life(p.getMainHandItem())&&p.getMainHandItem().getMaxDamage()==0&&EquipmentCrafting.refine(p.getMainHandItem())==1,"Life bond did not use existing nurture/refinement state");
            p.getMainHandItem().setDamageValue(99);h.assertTrue(p.getMainHandItem().getDamageValue()==0,"Life-bound source artifact still accumulated damage");
        }h.succeed();
    }
    @GameTest(template="empty") public static void quenchUsesSinglePersistentJobEscrowAndRefundsOnlyOnInterruption(GameTestHelper h){
        var pos=forge(h);var jobs=CraftingJobs.get(h.getLevel().getServer());var key=new CraftingJobs.JobKey(h.getLevel().dimension(),pos);
        try(var p=player(h,pos)){
            var forge=(RefiningForgeBlockEntity)h.getLevel().getBlockEntity(pos);long now=h.getLevel().getGameTime();
            var result=artifact(ArtifactKind.SWORD);var inputs=new ArrayList<ItemStack>();inputs.add(new ItemStack(Items.IRON_INGOT,2));
            jobs.add(new CraftingJobs.PendingRefining(p.getUUID(),h.getLevel().dimension(),pos,now+30,result,inputs,10));forge.startCrafting(now-70,now+30,"wuxingxiantu:refining_dark_iron_sword",p.getUUID());
            p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(WuXingItems.QUENCH_ESSENCE.get(),2));
            h.assertTrue(forge.operate(p)&&p.getMainHandItem().getCount()==1&&!forge.operate(p),"Quench failed or consumed twice in one stage");
            var job=jobs.refining(key);int conduct=ArtifactEffects.conduct(job.result());h.assertTrue(conduct==5&&ArtifactData.quenchConduct(job.result())==3&&job.materials().stream().filter(v->v.is(WuXingItems.QUENCH_ESSENCE.get())).mapToInt(ItemStack::getCount).sum()==1,"Coolant did not enter the same job escrow or conduct authority");
            var restored=CraftingJobs.load(jobs.save(new CompoundTag())).refining(key);h.assertTrue(ArtifactEffects.conduct(restored.result())==conduct&&restored.materials().stream().filter(v->v.is(WuXingItems.QUENCH_ESSENCE.get())).mapToInt(ItemStack::getCount).sum()==1,"Persistent quench escrow lost state");
            h.getLevel().setBlock(pos.offset(1,1,0),Blocks.AIR.defaultBlockState(),3);jobs.schedule(CraftingJobs.Kind.REFINING,key,p.server.overworld().getGameTime());AlchemyService.tickAlchemy(p.server);
            var drops=h.getLevel().getEntitiesOfClass(ItemEntity.class,new AABB(pos).inflate(4));
            try{h.assertTrue(jobs.refining(key)==null&&!forge.hasPendingReward()&&drops.stream().filter(v->v.getItem().is(WuXingItems.QUENCH_ESSENCE.get())).mapToInt(v->v.getItem().getCount()).sum()==1,"Interruption lost coolant or produced both refund and artifact");}finally{drops.forEach(ItemEntity::discard);}
        }finally{jobs.remove(CraftingJobs.Kind.REFINING,key);}h.succeed();
    }
    @GameTest(template="empty") public static void mixedBaseMaterialsCannotBorrowTheHighestTier(GameTestHelper h){
        var baseTag=net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM,new net.minecraft.resources.ResourceLocation(GeneratedMod.MOD_ID,"artifact_base_materials"));
        h.assertTrue(new ItemStack(Items.COPPER_INGOT).is(baseTag),"Copper cannot enter the real artifact base-material recipes");
        var iron=new ItemStack(Items.IRON_INGOT,7);var high=new ItemStack(Items.NETHERITE_INGOT);
        var mixed=ArtifactData.infuseRefining(artifact(ArtifactKind.SWORD),List.of(high,iron,new ItemStack(Items.DIAMOND)));
        h.assertTrue(ArtifactData.material(mixed)==1,"One high ingot promoted the whole mixed-material artifact");
        var crystal=ArtifactData.infuseRefining(artifact(ArtifactKind.SWORD),List.of(new ItemStack(Items.AMETHYST_SHARD,7),high,new ItemStack(Items.REDSTONE)));
        h.assertTrue(ArtifactData.material(crystal)==2,"Unrelated recipe inputs participated in base-material ranking");
        var pure=ArtifactData.infuseRefining(artifact(ArtifactKind.SWORD),List.of(new ItemStack(Items.NETHERITE_INGOT,8),new ItemStack(Items.DIAMOND)));
        h.assertTrue(ArtifactData.material(pure)==3,"A pure high-tier base was downgraded by an unrelated ingredient");
        var copper=new ItemStack(Items.COPPER_INGOT,8);
        var conductive=ArtifactData.infuseRefining(artifact(ArtifactKind.SWORD),List.of(copper,new ItemStack(Items.DIAMOND)));
        h.assertTrue(ArtifactData.material(conductive)==0&&ArtifactData.conduct(conductive)==2,"Copper lost its source material tier or innate conduct");
        for(var inputs:List.of(List.of(high,copper),List.of(copper,high))){
            var copperMix=ArtifactData.infuseRefining(artifact(ArtifactKind.SWORD),inputs);
            h.assertTrue(ArtifactData.material(copperMix)==0,"Mixed copper was treated as an unset tier and promoted by a high-tier ingredient");
        }
        var empty=ArtifactData.infuseRefining(artifact(ArtifactKind.SWORD),List.of(new ItemStack(Items.DIAMOND)));
        h.assertTrue(ArtifactData.material(empty)==0,"No matching base material invented a higher tier");h.succeed();
    }

}
