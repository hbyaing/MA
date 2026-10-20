package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.alchemy.*;
import cn.blockforge.generated.generatedmod.combat.HeartForgeContracts;
import cn.blockforge.generated.generatedmod.combat.HeartSwordCombatProfile;
import cn.blockforge.generated.generatedmod.fixtures.GameTestPlayer;
import cn.blockforge.generated.generatedmod.items.SpiritSwordItem;
import cn.blockforge.generated.generatedmod.mindscape.*;
import cn.blockforge.generated.generatedmod.network.packet.cultivation.SyncPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/** Required regressions for the September audit. Not executed in this source-only update. */
@GameTestHolder(GeneratedMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class SeptemberAuditGameTests {
    private static GameTestPlayer player(GameTestHelper h) {
        var p=new GameTestPlayer(h.getLevel());
        p.cleanupAfter(h,()->DivineArtLogic.stop(p,GeneratedMod.data(p),false));
        var pos=h.absolutePos(new BlockPos(2,1,2));
        p.setPos(pos.getX()+.5,pos.getY(),pos.getZ()+.5);
        p.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(100);
        p.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);p.setHealth(1000);
        GeneratedMod.data(p).wuxingxiantu$setRealm(8);
        p.join();return p;
    }
    @GameTest(template="empty") public static void invasionFreezesGrowthAndReplicatesExistingCultivation(GameTestHelper h) {
        try(var p=player(h)) {
            var data=GeneratedMod.data(p);data.wuxingxiantu$setCultivation(123456);
            MindscapeManager.markUnstable(p);
            data.wuxingxiantu$setCultivation(123457);
            h.assertTrue(data.wuxingxiantu$getCultivation()==123456,"Invasion changed existing cultivation or allowed growth");
            var buffer=new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
            try {
                SyncPacket.encode(new SyncPacket(data),buffer);
                var packet=SyncPacket.decode(buffer);var replica=new CultivationDataImpl();
                replica.wuxingxiantu$setRealm(packet.realm());
                replica.wuxingxiantu$setCultivationStage(packet.cultivationStage());
                replica.wuxingxiantu$setDaoXin(packet.daoXin());
                replica.wuxingxiantu$restoreCultivation(packet.cultivation());
                h.assertTrue(replica.wuxingxiantu$getCultivation()==123456,"Snapshot was blocked by the gameplay growth gate");
            } finally {buffer.release();}
        }
        h.succeed();
    }
    @GameTest(template="empty") public static void creationRepairClearsStateWithoutChangingCultivation(GameTestHelper h) {
        try(var p=player(h)) {
            var d=GeneratedMod.data(p);d.wuxingxiantu$setCultivation(123456);
            MindscapeManager.markUnstable(p);
            h.assertTrue(MindscapeManager.repairInvasion(p),"Repair rejected normal player");
            h.assertTrue(d.wuxingxiantu$getCultivation()==123456&&d.wuxingxiantu$getDaoXin()==1
                    &&MindscapeManager.state(p)==MindscapeState.NONE&&MindscapeProgression.pending(p)==null
                    &&d.wuxingxiantu$getHeartDemonInvasionCause().isEmpty(),"Repair left invasion state or erased cultivation");
        }
        h.succeed();
    }
    @GameTest(template="empty") public static void formulaUnlocksAndDuplicateIsNotConsumed(GameTestHelper h) {
        try(var p=player(h)) {
            FormulaKnowledge.reset(p);var stack=FormulaItem.create(PillCatalog.AURA);stack.setCount(2);
            p.setItemInHand(InteractionHand.MAIN_HAND,stack);
            stack.use(h.getLevel(),p,InteractionHand.MAIN_HAND);
            h.assertTrue(FormulaKnowledge.unlocked(p)&&stack.getCount()==1,"Single formula failed to unlock alchemy");
            stack.use(h.getLevel(),p,InteractionHand.MAIN_HAND);
            h.assertTrue(stack.getCount()==1,"Duplicate formula was consumed");
        }
        h.succeed();
    }
    @GameTest(template="empty") public static void historicalKnownFormulaRepairsLockedAlchemy(GameTestHelper h) {
        try(var p=player(h)) {
            FormulaKnowledge.reset(p);
            FormulaKnowledge.learn(p,ResourceLocation.fromNamespaceAndPath(GeneratedMod.MOD_ID,PillCatalog.AURA.id),false);
            var stack=FormulaItem.create(PillCatalog.AURA);p.setItemInHand(InteractionHand.MAIN_HAND,stack);
            stack.use(h.getLevel(),p,InteractionHand.MAIN_HAND);
            h.assertTrue(FormulaKnowledge.unlocked(p)&&stack.isEmpty(),"Historical known-but-locked formula did not unlock");
        }
        h.succeed();
    }
    @GameTest(template="empty") public static void invalidFormulaDoesNotUnlockOrConsume(GameTestHelper h) {
        try(var p=player(h)) {
            FormulaKnowledge.reset(p);var stack=FormulaItem.create(PillCatalog.AURA);
            stack.getOrCreateTag().putString("FormulaId","minecraft:stone");
            p.setItemInHand(InteractionHand.MAIN_HAND,stack);stack.use(h.getLevel(),p,InteractionHand.MAIN_HAND);
            h.assertTrue(!FormulaKnowledge.unlocked(p)&&stack.getCount()==1,"Invalid formula granted knowledge");
        }
        h.succeed();
    }
    @GameTest(template="empty") public static void everyOrdinaryBookUnlocksAndTeachesItsGroup(GameTestHelper h) {
        try(var p=player(h)) {
            for(var book:AlchemyCompendium.values()) {
                if(book==AlchemyCompendium.COMPLETE)continue;
                FormulaKnowledge.reset(p);
                var stack=new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(GeneratedMod.MOD_ID,book.id)),2);
                p.setItemInHand(InteractionHand.MAIN_HAND,stack);stack.use(h.getLevel(),p,InteractionHand.MAIN_HAND);
                h.assertTrue(FormulaKnowledge.unlocked(p)&&stack.getCount()==1,"Book did not unlock: "+book);
                for(var pill:book.formulas())h.assertTrue(FormulaKnowledge.knows(p,
                        ResourceLocation.fromNamespaceAndPath(GeneratedMod.MOD_ID,pill.id)),"Missing formula: "+pill);
                stack.use(h.getLevel(),p,InteractionHand.MAIN_HAND);
                h.assertTrue(stack.getCount()==1,"Duplicate book consumed: "+book);
            }
        }
        h.succeed();
    }
    @GameTest(template="empty") public static void newContractInvalidatesStoredSwordAndSurvivesSave(GameTestHelper h) {
        try(var p=player(h);var stranger=player(h)) {
            var old=new ItemStack(WuXingItems.PEACH_SWORD.get());var current=old.copy();
            HeartForgeContracts.bind(p,old);HeartForgeContracts.bind(p,current);
            h.assertTrue(!HeartForgeContracts.active(p,old)&&HeartForgeContracts.active(p,current)
                    &&!HeartForgeContracts.active(stranger,current),"Contract uniqueness or ownership failed");
            var restored=new CultivationDataImpl();restored.deserializeNBT(((CultivationDataImpl)GeneratedMod.data(p)).serializeNBT());
            h.assertTrue(HeartForgeContracts.contract(current).equals(restored.wuxingxiantu$getHeartForgeContract()),"Contract lost on save");
            var buffer=new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
            try {
                SyncPacket.encode(new SyncPacket(GeneratedMod.data(p)),buffer);
                h.assertTrue(HeartForgeContracts.contract(current).equals(SyncPacket.decode(buffer).heartForgeContract()),"Contract lost in sync");
            } finally {buffer.release();}
            HeartForgeContracts.revoke(p);
            h.assertTrue(!HeartForgeContracts.active(p,current),"Revoked contract remains active");
        }
        h.succeed();
    }
    @GameTest(template="empty") public static void upkeepIncludesYujianSlotAndStoredContract(GameTestHelper h) {
        try(var p=player(h)) {
            var d=GeneratedMod.data(p);d.wuxingxiantu$setMaxMana(10000);d.wuxingxiantu$setAura(10000);
            var sword=new ItemStack(WuXingItems.PEACH_SWORD.get());HeartForgeContracts.bind(p,sword);
            d.wuxingxiantu$setYujianStack(sword);p.tickCount=20;
            float before=d.wuxingxiantu$getAura();
            float upkeep=d.wuxingxiantu$getMaxMana()*.001F;
            HeartSwordSystem.playerTick(new TickEvent.PlayerTickEvent(TickEvent.Phase.END,p));
            h.assertTrue(Math.abs(d.wuxingxiantu$getAura()-(before-upkeep))<.01,"Yujian slot skipped upkeep");
            d.wuxingxiantu$setYujianStack(ItemStack.EMPTY);p.tickCount=40;
            before=d.wuxingxiantu$getAura();
            upkeep=d.wuxingxiantu$getMaxMana()*.001F;
            HeartSwordSystem.playerTick(new TickEvent.PlayerTickEvent(TickEvent.Phase.END,p));
            h.assertTrue(Math.abs(d.wuxingxiantu$getAura()-(before-upkeep))<.01,"Stored contract skipped upkeep");
            d.wuxingxiantu$setAura(0);p.tickCount=60;
            HeartSwordSystem.playerTick(new TickEvent.PlayerTickEvent(TickEvent.Phase.END,p));
            h.assertTrue(!HeartForgeContracts.active(p,sword)&&d.wuxingxiantu$getHeartForgeContract()==null,"Unpaid contract survived");
        }
        h.succeed();
    }
    @GameTest(template="empty") public static void forgedDirectSwordHitUsesOneUncappedTrueDamage(GameTestHelper h) {
        try(var p=player(h);var target=player(h)) {
            var sword=new ItemStack(WuXingItems.PEACH_SWORD.get());HeartForgeContracts.bind(p,sword);
            target.setAbsorptionAmount(200);target.getAttribute(Attributes.ARMOR).setBaseValue(30);
            var probe=new HeartSwordCombatGameTests.Probe(target);MinecraftForge.EVENT_BUS.register(probe);
            try {
                double expected=(HeartSwordCombatProfile.attackBaseline(p,sword)+3)*2;
                HeartSwordCombatProfile.swordHit(p,sword,target,8,3,2,28);
                h.assertTrue(Math.abs(1000-target.getHealth()-expected)<.02&&target.getAbsorptionAmount()==200
                        &&probe.heart&&probe.hurts==1&&probe.damages==1,"Direct sword path lost projection, enchantment or single-hit semantics");
            } finally {MinecraftForge.EVENT_BUS.unregister(probe);}
        }
        h.succeed();
    }
    @GameTest(template="empty") public static void ordinaryDirectSwordHitRetainsCapAndDefense(GameTestHelper h) {
        try(var p=player(h);var target=player(h)) {
            var sword=new ItemStack(WuXingItems.PEACH_SWORD.get());target.setAbsorptionAmount(50);
            HeartSwordCombatProfile.swordHit(p,sword,target,20,0,3,28);
            h.assertTrue(target.getHealth()==1000&&Math.abs(target.getAbsorptionAmount()-22)<.02,"Ordinary collision cap or absorption changed");
        }
        h.succeed();
    }
    @GameTest(template="empty") public static void completedMainlineRequiresExplicitRepeat(GameTestHelper h) {
        try(var p=player(h)) {
            var data=GeneratedMod.data(p);data.wuxingxiantu$getMindscapeData().putBoolean("final_completed",true);
            h.assertTrue(MindscapeProgression.chooseNext(p)==null,"Final challenge automatically replayed");
            MindscapeManager.markUnstable(p);
            h.assertTrue(!data.wuxingxiantu$isDaoHeartUnstable(),"Completed mainline forced a new invasion");
            h.assertTrue(MindscapeProgression.requestRepeat(p)&&MindscapeProgression.pending(p)!=null
                    &&"REPLAY".equals(data.wuxingxiantu$getHeartDemonInvasionCause()),"Explicit repeat unavailable");
            h.assertTrue(!MindscapeProgression.requestRepeat(p),"Duplicate repeat accepted");
        }
        h.succeed();
    }
    private SeptemberAuditGameTests() {}
}
