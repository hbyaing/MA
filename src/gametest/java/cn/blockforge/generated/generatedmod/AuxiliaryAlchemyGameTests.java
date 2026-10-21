package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.alchemy.*;
import cn.blockforge.generated.generatedmod.alchemy.auxiliary.*;
import cn.blockforge.generated.generatedmod.fixtures.GameTestPlayer;
import cn.blockforge.generated.generatedmod.plants.HerbAge;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.gametest.*;
import java.util.*;

@GameTestHolder("wuxingxiantu_entry") @PrefixGameTestTemplate(false)
public final class AuxiliaryAlchemyGameTests {
    private static ItemStack herb(String id,int years,int count){var stack=new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation("wuxingxiantu:herb_"+id)),count);HerbAge.setYears(stack,years);return stack;}
    private static ItemStack pill(){return PillData.create(PillCatalog.AURA,PillData.Grade.LOW,PillData.Element.METAL,0);}
    private static AuxiliaryAlchemy.Plan plan(ItemStack chance,ItemStack effect,ItemStack output){
        var in=new SimpleContainer(3);in.setItem(0,chance);in.setItem(1,effect);in.setItem(2,output);
        return AuxiliaryAlchemy.allocate(in,new int[3],new int[]{chance.isEmpty()?-1:0,effect.isEmpty()?-1:1,output.isEmpty()?-1:2}).plan();
    }
    private static GameTestPlayer player(GameTestHelper h){var p=new GameTestPlayer(h.getLevel());var at=h.absolutePos(new BlockPos(2,4,2));p.setPos(at.getX()+.5,at.getY(),at.getZ()+.5);var d=GeneratedMod.data(p);d.wuxingxiantu$setRealm(4);d.wuxingxiantu$setAura(0);return p;}
    private static <T extends Mob>T target(GameTestHelper h,GameTestPlayer p,EntityType<T> type){var entity=type.create(h.getLevel());if(entity==null)throw new AssertionError("No target");entity.setPos(p.getX()+1,p.getY(),p.getZ());entity.setNoAi(true);entity.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200);entity.setHealth(200);return entity;}
    private static void hit(GameTestPlayer p,LivingEntity target){AuxiliaryAlchemyEffects.hurt(new LivingHurtEvent(target,p.damageSources().playerAttack(p),5));}

    @GameTest(template="empty") public static void realFurnaceReservationPaysSecondaryHerbsAndSurvivesCancellationAndReload(GameTestHelper h){
        var pos=FurnaceGameTests.build(h);AlchemyFurnaceStructure.refreshAround(h.getLevel(),pos);
        var furnace=(AlchemyFurnaceBlockEntity)h.getLevel().getBlockEntity(pos);
        var recipe=(AlchemyRecipe)h.getLevel().getRecipeManager().byKey(new ResourceLocation("wuxingxiantu:alchemy_aura_pill_low")).orElseThrow();
        for(int i=0;i<recipe.getIngredients().size();i++)furnace.setItem(i,recipe.getIngredients().get(i).getItems()[0].copyWithCount(recipe.counts().get(i)));
        furnace.setItem(3,herb("xingchen",2500,2));furnace.setItem(4,herb("hanbing",2500,2));furnace.setItem(5,herb("lingdao",2500,2));
        var allocation=AuxiliaryAlchemy.allocate(furnace,recipe.reservation(furnace));
        furnace.reserve(recipe);
        h.assertTrue(furnace.reservedMaterials().stream().filter(s->AuxiliaryAlchemy.role(s)!=null).mapToInt(ItemStack::getCount).sum()==3,"Real furnace did not escrow all three paid secondary plants");
        furnace.startBrewing(1,21);var saved=furnace.saveWithoutMetadata();furnace.load(saved);furnace.finishBrewing(false);
        h.assertTrue(furnace.getItem(3).getCount()==2&&furnace.getItem(4).getCount()==2&&furnace.getItem(5).getCount()==2,"Cancellation consumed or duplicated secondary plants");
        furnace.reserve(recipe);furnace.startBrewing(1,21);var result=AuxiliaryAlchemy.finish(pill(),allocation.plan());
        h.assertTrue(result.getCount()==2&&furnace.complete(result),"Actual furnace could not settle the paid output bonus");
        h.assertTrue(furnace.getItem(3).getCount()==1&&furnace.getItem(4).getCount()==1&&furnace.getItem(5).getCount()==1,"Successful furnace did not consume exactly one secondary plant per role");
        h.assertTrue(AuxiliaryAlchemy.read(furnace.completedResult()).injections().size()==3&&!furnace.complete(result),"Result lost secondary NBT or completed twice");
        h.assertTrue(AuxiliaryAlchemy.finish(result,allocation.plan()).getCount()==2,"Secondary yield was granted twice");h.succeed();
    }

    @GameTest(template="empty") public static void explicitSelectionNeverConsumesUnchosenBackpackHerbsOrRequiredGuides(GameTestHelper h){
        var inventory=new SimpleContainer(36);inventory.setItem(0,herb("xingchen",2500,7));inventory.setItem(1,herb("hanbing",2500,7));inventory.setItem(35,herb("lingdao",2500,7));
        var none=AuxiliaryAlchemy.allocate(inventory,new int[36],new int[]{-1,-1,-1});h.assertTrue(none.plan().empty()&&Arrays.stream(none.counts()).sum()==0,"Unselected backpack herbs were treated as volunteered secondary inputs");
        var picked=AuxiliaryAlchemy.allocate(inventory,new int[36],new int[]{0,1,35});h.assertTrue(Arrays.stream(picked.counts()).sum()==3&&inventory.getItem(35).getCount()==7,"Preview charged secondary plants or boosted from the unconsumed stack size");
        int[] base=new int[36];base[0]=1;boolean rejected=false;try{AuxiliaryAlchemy.allocate(inventory,base,new int[]{0,1,35});}catch(IllegalArgumentException expected){rejected=true;}
        h.assertTrue(rejected,"A required guide was also spent as a free secondary effect");
        var reloaded=AuxiliaryAlchemy.Plan.load(picked.plan().save());h.assertTrue(reloaded.injections().equals(picked.plan().injections()),"Escrow snapshot lost source years or effects");h.succeed();
    }

    @GameTest(template="empty") public static void successfulEatingAppliesFrostButFailedBasePillGrantsNoFreeSecondaryPower(GameTestHelper h){
        try(var p=player(h)){
            var batch=AuxiliaryAlchemy.finish(pill(),plan(ItemStack.EMPTY,herb("hanbing",500,1),ItemStack.EMPTY));
            var remaining=batch.getItem().finishUsingItem(batch,p.level(),p);
            h.assertTrue(remaining.isEmpty()&&p.hasEffect(MobEffects.DAMAGE_RESISTANCE),"Actual BasePillItem did not apply its paid secondary effect");
            var enemy=target(h,p,EntityType.ZOMBIE);hit(p,enemy);h.assertTrue(enemy.hasEffect(MobEffects.MOVEMENT_SLOWDOWN),"Actual melee hurt entry did not apply frost");
            AuxiliaryAlchemyEffects.clear(p);p.removeEffect(MobEffects.DAMAGE_RESISTANCE);GeneratedMod.data(p).wuxingxiantu$setAura(GeneratedMod.data(p).wuxingxiantu$getMaxMana());
            var failed=AuxiliaryAlchemy.finish(pill(),plan(ItemStack.EMPTY,herb("hanbing",500,1),ItemStack.EMPTY));
            var kept=failed.getItem().finishUsingItem(failed,p.level(),p);h.assertTrue(kept.getCount()==1&&!p.hasEffect(MobEffects.DAMAGE_RESISTANCE),"A rejected full-mana base pill granted free secondary effects");
        }h.succeed();
    }

    @GameTest(template="empty") public static void uniqueMeleeEffectsProtectPetsServantsNeutralsAndRejectProjectilesAndExpiredState(GameTestHelper h){
        try(var p=player(h)){
            var poison=AuxiliaryAlchemy.finish(pill(),plan(herb("xingchen",500,1),herb("duzhen",500,1),ItemStack.EMPTY));
            AuxiliaryAlchemyEffects.applyPill(p,poison);h.assertTrue(!p.hasEffect(MobEffects.POISON),"Purity failed to balance the venom backlash");
            var enemy=target(h,p,EntityType.CREEPER);var wolf=target(h,p,EntityType.WOLF);wolf.setOwnerUUID(p.getUUID());wolf.setTame(true);
            var cow=target(h,p,EntityType.COW);var servant=target(h,p,EntityType.CREEPER);var n=servant.getPersistentData();n.putUUID("HumanEmperorBannerOwner",p.getUUID());n.putUUID("HumanEmperorBannerId",UUID.randomUUID());n.putString("HumanEmperorBannerType","minecraft:creeper");
            for(var protectedTarget:new LivingEntity[]{wolf,cow,servant}){hit(p,protectedTarget);h.assertTrue(!protectedTarget.hasEffect(MobEffects.POISON),"A protected creature received secondary poison");}
            var arrow=new net.minecraft.world.entity.projectile.Arrow(p.level(),p);AuxiliaryAlchemyEffects.hurt(new LivingHurtEvent(enemy,p.damageSources().arrow(arrow,p),5));h.assertTrue(!enemy.hasEffect(MobEffects.POISON),"A projectile received melee-only secondary effects");
            hit(p,enemy);h.assertTrue(enemy.hasEffect(MobEffects.POISON),"A valid enemy melee did not receive venom");enemy.removeEffect(MobEffects.POISON);
            // Preserve vanilla undead immunity rather than requiring poison on an immune zombie.
            var undead=target(h,p,EntityType.ZOMBIE);hit(p,undead);
            h.assertTrue(!undead.hasEffect(MobEffects.POISON),"Secondary venom bypassed vanilla undead immunity");
            var state=MedicineProgress.state(p).getCompound("AuxiliaryStrikes");state.putLong("Until",p.level().getGameTime());hit(p,enemy);
            h.assertTrue(!enemy.hasEffect(MobEffects.POISON),"Expired secondary power still triggered");
        }h.succeed();
    }

    @GameTest(template="empty") public static void repeatedMedicineUsesTargetRecoveryAuthorityAndNeverReplacesStrongerBuffs(GameTestHelper h){
        try(var p=player(h)){
            p.setHealth(5);var healing=AuxiliaryAlchemy.finish(pill(),plan(ItemStack.EMPTY,herb("lingzhi",500,1),ItemStack.EMPTY));
            AuxiliaryAlchemyEffects.applyPill(p,healing);var medicine=MedicineProgress.state(p);int seconds=medicine.getInt("RecoverySeconds");
            h.assertTrue(seconds>0&&p.getHealth()==5,"Wood created a second instant-heal settlement instead of target recovery");
            AuxiliaryAlchemyEffects.applyPill(p,healing);h.assertTrue(medicine.getInt("RecoverySeconds")==seconds,"Repeated wood medicine accumulated infinite duration");MedicineProgress.second(p);h.assertTrue(p.getHealth()>5,"Target medicine scheduler did not settle secondary recovery");
            p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,1000,2));
            AuxiliaryAlchemyEffects.applyPill(p,AuxiliaryAlchemy.finish(pill(),plan(ItemStack.EMPTY,herb("hanbing",500,1),ItemStack.EMPTY)));
            var resistance=p.getEffect(MobEffects.DAMAGE_RESISTANCE);h.assertTrue(resistance.getAmplifier()==2&&resistance.getDuration()==1000,"Weak secondary medicine replaced a stronger target buff");
        }h.succeed();
    }
}
