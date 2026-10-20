package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.alchemy.*;
import cn.blockforge.generated.generatedmod.crafting.EquipmentCrafting;
import cn.blockforge.generated.generatedmod.fixtures.GameTestPlayer;
import cn.blockforge.generated.generatedmod.importedcreatures.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.*;
import java.util.*;

@GameTestHolder("wuxingxiantu_entry")
@PrefixGameTestTemplate(false)
public final class ImportedCreaturesGameTests {
    private static BlackBoarEntity boar(GameTestHelper helper,boolean king,int offset) {
        var entity=(king?ImportedCreatures.BLACK_BOAR_KING:ImportedCreatures.BLACK_BOAR).get().create(helper.getLevel());
        var pos=helper.absolutePos(new BlockPos(offset,0,0));entity.setPos(pos.getX()+.5,210,pos.getZ()+.5);
        entity.setNoAi(true);entity.setNoGravity(true);entity.setPersistenceRequired();helper.getLevel().addFreshEntity(entity);return entity;
    }
    @GameTest(template="empty") public static void kingAttributesAndRealMeleeUseTargetDamagePipeline(GameTestHelper helper) {
        var regular=boar(helper,false,0);var king=boar(helper,true,20);
        var target=EntityType.COW.create(helper.getLevel());target.setPos(regular.position().add(0,0,1));target.setNoAi(true);
        target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);target.setHealth(1000);helper.getLevel().addFreshEntity(target);
        try {
            helper.assertTrue(regular.getMaxHealth()==64&&king.getMaxHealth()==160&&king.getBbWidth()>regular.getBbWidth(),"Source king scaling or attributes lost");
            helper.assertTrue(regular.cultivationRealm()==ProgressionGate.QI_CONDENSATION&&king.cultivationRealm()==ProgressionGate.FOUNDATION,"Source fifteen-stage player scale leaked into target realms");
            float before=target.getHealth();helper.assertTrue(regular.doHurtTarget(target),"Normal melee failed");
            float ordinary=before-target.getHealth();helper.assertTrue(ordinary>0&&target.getLastHurtByMob()==regular,"Boar bypassed target damage or attribution");
            target.invulnerableTime=0;target.setHealth(1000);helper.assertTrue(king.doHurtTarget(target),"King melee failed");
            helper.assertTrue(1000-target.getHealth()>ordinary&&target.getLastHurtByMob()==king,"King lost its stronger normal melee");
        } finally {regular.discard();king.discard();target.discard();}
        helper.succeed();
    }
    @GameTest(template="empty") public static void realPlayerKillDropsVersionedBloodAndHideAndEnvironmentDoesNot(GameTestHelper helper) {
        try(var player=new GameTestPlayer(helper.getLevel())) {
            for(boolean king:List.of(false,true)) {
                var beast=boar(helper,king,king?48:0);var area=beast.getBoundingBox().inflate(2);
                beast.hurt(player.damageSources().playerAttack(player),10000);
                var drops=helper.getLevel().getEntitiesOfClass(ItemEntity.class,area,e->BeastMaterials.blood(e.getItem())||BeastMaterials.hide(e.getItem()));
                try {
                    int blood=0,hide=0;
                    for(var item:drops) {
                        var stack=item.getItem();if(BeastMaterials.blood(stack))blood+=stack.getCount();else hide+=stack.getCount();
                        helper.assertTrue(BeastMaterials.potency(stack)==(king?2:1),"Kill lost source potency");
                        var saved=ItemStack.of(stack.save(new CompoundTag()));
                        helper.assertTrue(ItemStack.isSameItemSameTags(saved,stack)&&saved.getTag().getInt("beast_schema")==2,"Material provenance did not survive save/load");
                    }
                    helper.assertTrue(blood==(king?2:1)&&hide==(king?2:1),"Source player-credit drop amounts changed");
                } finally {drops.forEach(ItemEntity::discard);beast.discard();}
            }
            var unattended=boar(helper,false,96);var area=unattended.getBoundingBox().inflate(2);
            unattended.hurt(unattended.damageSources().generic(),10000);
            helper.assertTrue(helper.getLevel().getEntitiesOfClass(ItemEntity.class,area,e->BeastMaterials.blood(e.getItem())||BeastMaterials.hide(e.getItem())).isEmpty(),"Environment farm produced player-credit materials");
            unattended.discard();
        }
        helper.succeed();
    }
    @GameTest(template="empty") public static void sourceSpawnPlacementRequiresLegalGrassHabitat(GameTestHelper helper) {
        var pos=helper.absolutePos(BlockPos.ZERO).atY(220);
        var before=helper.getLevel().getBlockState(pos.below());
        var previousDifficulty=helper.getLevel().getDifficulty();
        try {
            helper.getLevel().getServer().setDifficulty(net.minecraft.world.Difficulty.NORMAL,true);
            helper.getLevel().setBlock(pos.below(),Blocks.GRASS_BLOCK.defaultBlockState(),3);
            helper.assertTrue(SpawnPlacements.checkSpawnRules(ImportedCreatures.BLACK_BOAR.get(),helper.getLevel(),MobSpawnType.NATURAL,pos,helper.getLevel().random),"Registered daytime grass placement rejected source boar");
            helper.assertTrue(SpawnPlacements.checkSpawnRules(ImportedCreatures.BLACK_BOAR_KING.get(),helper.getLevel(),MobSpawnType.NATURAL,pos,helper.getLevel().random),"King has no real spawn placement");
            helper.getLevel().setBlock(pos.below(),Blocks.STONE.defaultBlockState(),3);
            helper.assertTrue(!SpawnPlacements.checkSpawnRules(ImportedCreatures.BLACK_BOAR.get(),helper.getLevel(),MobSpawnType.NATURAL,pos,helper.getLevel().random),"Source boar spawned on illegal stone floor");
        } finally {helper.getLevel().setBlock(pos.below(),before,3);helper.getLevel().getServer().setDifficulty(previousDifficulty,true);}
        helper.succeed();
    }
    @GameTest(template="empty") public static void realBloodRecipesConsumeProvenanceAndPillsDeliverStoneGuard(GameTestHelper helper) {
        var recipe=(AlchemyRecipe)helper.getLevel().getRecipeManager().byKey(new ResourceLocation("wuxingxiantu:alchemy_boar_blood_guard")).orElseThrow();
        var input=new SimpleContainer(8);
        for(int i=0;i<recipe.getIngredients().size();i++)input.setItem(i,recipe.getIngredients().get(i).getItems()[0].copyWithCount(recipe.counts().get(i)));
        input.setItem(2,BeastMaterials.create(true,true));
        helper.assertTrue(!recipe.matches(input,helper.getLevel()),"King provenance matched the ordinary blood recipe");
        input.setItem(2,BeastMaterials.create(true,false));
        helper.assertTrue(recipe.matches(input,helper.getLevel()),"Natural blood is unusable in the actual alchemy recipe");
        var consumed=recipe.consume(input);
        ItemStack pill=BeastMaterialEffects.infuseAlchemy(recipe.assemble(input,helper.getLevel().registryAccess()),consumed);
        helper.assertTrue(input.getItem(2).isEmpty()&&pill.hasTag()&&pill.getTag().contains(BeastMaterialEffects.STONE),"Blood was not consumed into the pill");
        try(var player=new GameTestPlayer(helper.getLevel())) {
            GeneratedMod.data(player).wuxingxiantu$setRealm(ProgressionGate.FOUNDATION);
            FormulaKnowledge.unlock(player);FormulaKnowledge.learn(player,recipe.formulaId(),false);
            pill.getItem().finishUsingItem(pill,helper.getLevel(),player);
            var resistance=player.getEffect(net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE);
            helper.assertTrue(resistance!=null&&resistance.getAmplifier()==1,"Actual pill use lost the source Stone Guard auxiliary");
            helper.assertTrue(!ImportedCreatures.BLOOD.get().isEdible(),"Raw blood became directly edible");
        }
        helper.succeed();
    }
    @GameTest(template="empty") public static void realHideRefiningAddsExistingToughRuneAndActualArmorWithoutLosingOwner(GameTestHelper helper) {
        var recipe=(RefiningRecipe)helper.getLevel().getRecipeManager().byKey(new ResourceLocation("wuxingxiantu:refining_boar_hide_armor")).orElseThrow();
        var input=new SimpleContainer(4);
        for(int i=0;i<4;i++)input.setItem(i,recipe.getIngredients().get(i).getItems()[0].copyWithCount(recipe.counts().get(i)));
        var hide=BeastMaterials.create(false,false);hide.setCount(2);input.setItem(1,hide);
        var owner=UUID.randomUUID();input.getItem(0).getOrCreateTag().putUUID(cn.blockforge.generated.generatedmod.items.HeartSwordItem.OWNER_TAG,owner);
        helper.assertTrue(recipe.matches(input,helper.getLevel()),"Natural hide unusable in actual refining recipe");
        var consumed=java.util.stream.IntStream.range(0,4).mapToObj(i->input.getItem(i).copy()).toList();
        recipe.consume(input);
        var result=BeastMaterialEffects.infuseRefining(EquipmentCrafting.inherit(recipe.assemble(input,helper.getLevel().registryAccess()),consumed),consumed);
        helper.assertTrue(input.isEmpty()&&EquipmentCrafting.level(result,"tough")==1&&BeastMaterialEffects.defense(result)==2,"Original tenacity/defense auxiliary purpose was lost");
        double actual=result.getAttributeModifiers(EquipmentSlot.CHEST).get(Attributes.ARMOR).stream().mapToDouble(modifier->modifier.getAmount()).sum();
        helper.assertTrue(actual>=8,"Hide defense exists only as tooltip NBT");
        var upgraded=EquipmentCrafting.inherit(new ItemStack(Items.DIAMOND_CHESTPLATE),List.of(result));
        helper.assertTrue(BeastMaterialEffects.defense(upgraded)==2&&owner.equals(cn.blockforge.generated.generatedmod.crafting.EquipmentBond.owner(upgraded)),"Target upgrade lost hide auxiliary or owner");
        helper.succeed();
    }
}
