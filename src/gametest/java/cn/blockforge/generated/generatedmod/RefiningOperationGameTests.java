package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.crafting.*;
import cn.blockforge.generated.generatedmod.fixtures.GameTestPlayer;
import cn.blockforge.generated.generatedmod.items.HeartSwordItem;
import cn.blockforge.generated.generatedmod.economy.SpiritCurrency;
import cn.blockforge.generated.generatedmod.economy.SpiritStoneGrade;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.*;
import java.util.List;
import java.util.UUID;

@GameTestHolder("wuxingxiantu_entry") @PrefixGameTestTemplate(false)
public final class RefiningOperationGameTests {
    private static void check(GameTestHelper h, boolean condition, String message) { h.assertTrue(condition, message); }

    private static BlockPos forge(GameTestHelper h) {
        BlockPos pos = h.absolutePos(new BlockPos(2, 1, 2));
        for (int x=-1;x<=1;x++) for (int z=-1;z<=1;z++) {
            if (x==0 && z==0) continue;
            h.getLevel().setBlockAndUpdate(pos.offset(x,0,z), Math.abs(x)==1 && Math.abs(z)==1
                    ? Blocks.POLISHED_BLACKSTONE_BRICK_SLAB.defaultBlockState() : WuXingBlocks.RUNE_STONE.get().defaultBlockState());
            h.getLevel().setBlockAndUpdate(pos.offset(x,1,z), WuXingBlocks.RUNE_STONE.get().defaultBlockState());
        }
        h.getLevel().setBlockAndUpdate(pos, WuXingBlocks.REFINING_FORGE_CORE.get().defaultBlockState());
        h.getLevel().setBlockAndUpdate(pos.above(), WuXingBlocks.ETERNAL_SPIRIT_COAL_BLOCK.get().defaultBlockState());
        h.getLevel().setBlockAndUpdate(pos.above(2), ExoticFireState.stateForTier(9, Blocks.FIRE.defaultBlockState()));
        check(h, RefiningForgeBlock.isStructureComplete(h.getLevel(),pos), "Forge fixture incomplete");
        return pos;
    }
    private static GameTestPlayer player(GameTestHelper h, BlockPos pos) {
        var p = new GameTestPlayer(h.getLevel()); p.join();
        p.setPos(pos.getX()+.5,pos.getY()+1,pos.getZ()+.5); p.setShiftKeyDown(true);
        var d = GeneratedMod.data(p); d.wuxingxiantu$setRealm(8); d.wuxingxiantu$setLearnedFires(new int[]{9}); d.wuxingxiantu$setAura(1000);
        return p;
    }

    @GameTest(template="empty") public static void operationWindowsAndFinishingPreserveRecipeAuthority(GameTestHelper h) {
        check(h,RefiningOperation.stage(0,100,-1)==-1 && RefiningOperation.stage(0,100,100)==-1,"Outside time accepted");
        for(int i=0;i<5;i++) check(h,RefiningOperation.stage(0,100,i*20+10)==i && RefiningOperation.precise(0,100,i*20+10),"Wrong stage window");
        check(h,!RefiningOperation.precise(0,100,1),"Early strike treated as precise");
        var original = new ItemStack(Items.DIAMOND_SWORD); original.getOrCreateTag().putString("ThirdParty","keep");
        var result = RefiningOperation.finish(original,31,7);
        check(h,EquipmentCrafting.refine(result)==1 && result.getCount()==1 && EquipmentCrafting.refine(original)==0,"Finishing mutated source or quantity");
        check(h,result.getTag().getString("ThirdParty").equals("keep"),"Unrelated NBT lost");
        check(h,EquipmentCrafting.refine(RefiningOperation.finish(original,1,31))==0,"Unattempted stages counted");
        EquipmentCrafting.write(original).putInt("Refine",9);
        check(h,EquipmentCrafting.refine(RefiningOperation.finish(original,31,31))==9,"Refine cap bypassed");
        var heart = HeartSwordItem.create(UUID.randomUUID());
        check(h,ItemStack.isSameItemSameTags(heart,RefiningOperation.finish(heart,31,31)),"Heart sword acquired a second growth system");
        var stack = new ItemStack(Items.DIAMOND_SWORD,2);
        check(h,ItemStack.isSameItemSameTags(stack,RefiningOperation.finish(stack,31,31)),"Batch output received equipment bonus");
        h.succeed();
    }

    @GameTest(template="empty") public static void originalOperatorFireAndSavedAttemptAreAuthoritative(GameTestHelper h) {
        BlockPos pos = forge(h);
        try(var owner=player(h,pos);var other=player(h,pos)) {
            var be=(RefiningForgeBlockEntity)h.getLevel().getBlockEntity(pos);
            String recipe="wuxingxiantu:refining_dark_iron_sword";
            check(h,h.getLevel().getRecipeManager().byKey(new ResourceLocation(recipe)).isPresent(),"Recipe fixture missing");
            long now=h.getLevel().getGameTime(); be.startCrafting(now-10,now+90,recipe,owner.getUUID());
            check(h,!be.operate(other) && be.operate(owner),"Wrong operator accepted or original denied");
            CompoundTag saved=be.getUpdateTag(); be.load(saved);
            check(h,!be.operate(owner),"Reload replayed a stage");
            saved=be.getUpdateTag(); saved.putLong("CraftStart",now-30);saved.putLong("CraftFinish",now+70);be.load(saved);
            check(h,be.operate(owner),"Second stage unavailable");
            saved=be.getUpdateTag();saved.putLong("CraftStart",now-50);saved.putLong("CraftFinish",now+50);be.load(saved);
            GeneratedMod.data(owner).wuxingxiantu$setLearnedFires(new int[0]);
            check(h,!be.operate(owner),"Unmastered fire accepted");
            GeneratedMod.data(owner).wuxingxiantu$setLearnedFires(new int[]{9});
            check(h,be.operate(owner),"Rejected attempt consumed the stage");
            var result=be.finishOperatedResult(new ItemStack(Items.DIAMOND_SWORD));
            check(h,EquipmentCrafting.refine(result)==1,"Three precise stages did not improve existing refinement");
            be.setPendingReward(result);check(h,!be.operate(owner),"Completed reward could be reworked");
            check(h,be.takePendingReward().getCount()==1 && be.takePendingReward().isEmpty(),"Output duplicated");
            be.startCrafting(now-10,now+90,recipe);
            check(h,!be.operate(owner),"Legacy ownerless job adopted by new operator");
        } h.succeed();
    }

    @GameTest(template="empty") public static void singleOwnerInheritanceAndForeignArmorAreLossless(GameTestHelper h) {
        try(var p=new GameTestPlayer(h.getLevel())) {
            p.join(); var stack=new ItemStack(Items.DIAMOND_CHESTPLATE);
            UUID owner=UUID.randomUUID();stack.getOrCreateTag().putUUID(HeartSwordItem.OWNER_TAG,owner);
            stack.getOrCreateTag().putString("ThirdParty","keep");
            check(h,!EquipmentBond.canBind(p,stack) && !EquipmentBond.canConsume(p,List.of(stack)),"Foreign owner overwritten");
            var converted=EquipmentBond.inheritOwner(new ItemStack(Items.NETHERITE_CHESTPLATE),List.of(stack));
            check(h,owner.equals(EquipmentBond.owner(converted)),"Recipe conversion lost owner");
            var conflicting=new ItemStack(Items.DIAMOND_SWORD);conflicting.getOrCreateTag().putUUID(HeartSwordItem.OWNER_TAG,UUID.randomUUID());
            check(h,EquipmentBond.inheritOwner(new ItemStack(Items.NETHERITE_SWORD),List.of(stack,conflicting)).isEmpty(),"Conflicting owners silently merged");
            p.setItemSlot(EquipmentSlot.CHEST,stack);
            EquipmentBondEvents.removeForeignArmor(p);
            check(h,p.getItemBySlot(EquipmentSlot.CHEST).isEmpty(),"Foreign armor remained equipped");
            var returned=p.getInventory().items.stream().filter(s->s.is(Items.DIAMOND_CHESTPLATE)).toList();
            check(h,returned.size()==1 && returned.get(0).getCount()==1 && owner.equals(EquipmentBond.owner(returned.get(0)))
                    && returned.get(0).getTag().getString("ThirdParty").equals("keep"),"Armor owner, NBT or quantity lost");
            EquipmentBondEvents.removeForeignArmor(p);
            check(h,p.getInventory().countItem(Items.DIAMOND_CHESTPLATE)==1,"Repeated armor check duplicated equipment");
        } h.succeed();
    }

    @GameTest(template="empty") public static void recognitionPaysFourTierCurrencyAndCannotBeStolen(GameTestHelper h) {
        var pos=forge(h);
        try(var p=player(h,pos);var other=player(h,pos)) {
            p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new ItemStack(Items.DIAMOND_SWORD));
            p.getInventory().setItem(20,new ItemStack(SpiritStoneGrade.values()[1].item()));
            long before=SpiritCurrency.value(p.getInventory().items);
            float aura=GeneratedMod.data(p).wuxingxiantu$getAura();
            check(h,EquipmentBond.ritual(p,pos,p.getMainHandItem()),"Recognition interaction unavailable");
            var bound=p.getMainHandItem();
            check(h,p.getUUID().equals(EquipmentBond.owner(bound)) && SpiritCurrency.value(p.getInventory().items)==before-1
                    && GeneratedMod.data(p).wuxingxiantu$getAura()==aura-20,"Payment change or held-item replacement lost binding");
            check(h,!EquipmentBond.canBind(other,bound),"Other player can rebind");
            EquipmentBond.ritual(p,pos,bound);
            check(h,GeneratedMod.data(p).wuxingxiantu$getAura()==aura-20,"Same-tick nurture bypassed cooldown");
            var n=EquipmentCrafting.write(bound);n.putLong("BondLastTick",h.getLevel().getGameTime()-60);n.putInt("BondNurture",100);
            check(h,EquipmentBond.ritual(p,pos,bound) && EquipmentCrafting.read(p.getMainHandItem()).getBoolean("BondLife"),"Life bond unavailable");
            check(h,EquipmentCrafting.refine(p.getMainHandItem())==1,"Life bond did not use existing refinement");
            float paid=GeneratedMod.data(p).wuxingxiantu$getAura();
            EquipmentCrafting.write(p.getMainHandItem()).putLong("BondLastTick",h.getLevel().getGameTime()-60);
            EquipmentBond.ritual(p,pos,p.getMainHandItem());
            check(h,GeneratedMod.data(p).wuxingxiantu$getAura()==paid && EquipmentCrafting.refine(p.getMainHandItem())==1,"Life bond reward replayed");
        } h.succeed();
    }
}
