package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.fire.*;
import cn.blockforge.generated.generatedmod.fixtures.GameTestPlayer;
import cn.blockforge.generated.generatedmod.network.packet.cultivation.ExoticBurnPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/** Required independent-burn regressions. Written, not executed during source-only delivery. */
@GameTestHolder(GeneratedMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ExoticBurnGameTests {
    private static GameTestPlayer player(GameTestHelper h) {
        var p=new GameTestPlayer(h.getLevel());p.cleanupAfter(h,()->{});
        var pos=h.absolutePos(new BlockPos(2,1,2));p.setPos(pos.getX()+.5,pos.getY(),pos.getZ()+.5);
        p.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);p.setHealth(1000);p.join();return p;
    }
    private static void ignite(GameTestPlayer p,int tier,int ticks) {ExoticBurnManager.ignite(p,FireCatalog.tier(tier).key(),ticks,null);}
    private static void fire(GameTestHelper h,GameTestPlayer p,int tier) {
        h.assertTrue(FireCatalog.tier(tier).key().equals(ExoticBurnManager.getFireId(p)),"Wrong source fire ID");
    }
    @GameTest(template="empty") public static void wetContactCannotDealSameTickDamage(GameTestHelper h) {
        try(var p=player(h)) {
            ignite(p,2,80);float health=p.getHealth();p.isInPowderSnow=true;
            try {ExoticBurnManager.contact(p,2);}
            finally {p.isInPowderSnow=false;}
            h.assertTrue(p.getHealth()==health&&!ExoticBurnManager.isBurning(p),"Wet contact dealt damage before the next tick");
        }
        h.succeed();
    }
    @GameTest(template="empty") public static void ordinaryFireRemainsOrdinary(GameTestHelper h) {
        try(var p=player(h)) {p.setSecondsOnFire(8);h.assertTrue(p.isOnFire()&&!ExoticBurnManager.isBurning(p),"Vanilla ignition became exotic");h.succeed();}
    }
    @GameTest(template="empty") public static void blockIgnitesWithoutVanillaTimer(GameTestHelper h) {
        try(var p=player(h)) {
            p.clearFire();var block=(ExoticFireBlock)WuXingBlocks.EXOTIC_FIRES[2].get();
            block.entityInside(block.defaultBlockState(),h.getLevel(),p.blockPosition(),p);
            fire(h,p,3);h.assertTrue(p.getRemainingFireTicks()<=0&&!p.isOnFire(),"Exotic block invoked vanilla ignition");h.succeed();
        }
    }
    @GameTest(template="empty") public static void burnTicksAndDamagesWithoutVanillaFire(GameTestHelper h) {
        try(var p=player(h)) {
            p.setRemainingFireTicks(0);ignite(p,1,100);float health=p.getHealth();
            for(int i=0;i<20;i++)ExoticBurnManager.tick(p);
            h.assertTrue(ExoticBurnManager.getRemainingTicks(p)==80&&p.getHealth()<health&&p.getRemainingFireTicks()<=0,
                    "Independent timer/damage did not advance without vanilla fire");h.succeed();
        }
    }
    @GameTest(template="empty") public static void expiresCleanly(GameTestHelper h) {
        try(var p=player(h)) {ignite(p,1,1);ExoticBurnManager.tick(p);h.assertTrue(!ExoticBurnManager.isBurning(p)&&ExoticBurnManager.getFireId(p)==null,"Expired state remains");h.succeed();}
    }
    @GameTest(template="empty") public static void sourcesDoNotCrossEntities(GameTestHelper h) {
        try(var a=player(h);var b=player(h)) {ignite(a,1,80);ignite(b,2,80);fire(h,a,1);fire(h,b,2);h.succeed();}
    }
    @GameTest(template="empty") public static void equippedFireCannotReplaceSource(GameTestHelper h) {
        try(var p=player(h)) {
            var d=GeneratedMod.data(p);d.wuxingxiantu$setRealm(8);d.wuxingxiantu$learnFire(9);d.wuxingxiantu$setEquippedFire(9);
            ignite(p,2,80);ExoticBurnManager.tick(p);fire(h,p,2);h.succeed();
        }
    }
    @GameTest(template="empty") public static void sameFireRefreshesOneState(GameTestHelper h) {
        try(var p=player(h)) {ignite(p,3,40);ExoticBurnManager.tick(p);ignite(p,3,80);fire(h,p,3);h.assertTrue(ExoticBurnManager.getRemainingTicks(p)==80,"Refresh shortened or duplicated duration");h.succeed();}
    }
    @GameTest(template="empty") public static void latestValidFireReplacesSource(GameTestHelper h) {
        try(var p=player(h)) {ignite(p,9,80);ignite(p,1,40);fire(h,p,1);h.assertTrue(ExoticBurnManager.getRemainingTicks(p)==40,"New source inherited old duration");h.succeed();}
    }
    @GameTest(template="empty") public static void vanillaFireCannotChangeExoticSource(GameTestHelper h) {
        try(var p=player(h)) {
            p.setSecondsOnFire(8);ignite(p,3,80);
            h.assertTrue(p.getRemainingFireTicks()<=0,"Ignition retained ordinary fire");
            p.setSecondsOnFire(8);ExoticBurnManager.tick(p);fire(h,p,3);
            h.assertTrue(p.getRemainingFireTicks()<=0&&!p.isOnFire(),"External vanilla ignition survived an exotic tick");h.succeed();
        }
    }
    @GameTest(template="empty") public static void dimensionTravelPreservesIndependentState(GameTestHelper h) {
        try(var p=player(h)) {
            ignite(p,4,80);var level=p.server.getLevel(Level.NETHER);var pos=new BlockPos(0,120,0);p.keepTicking(level,pos);
            p.teleportTo(level,.5,120,.5,0,0);fire(h,p,4);
            h.assertTrue(ExoticBurnManager.getRemainingTicks(p)==80&&p.getRemainingFireTicks()<=0,"Dimension travel changed independent state");
            h.assertTrue(ExoticBurnManager.snapshot(p,false).dimension().equals(Level.NETHER.location()),"Resync used old dimension");h.succeed();
        }
    }
    @GameTest(template="empty") public static void deathAndRespawnClearBurn(GameTestHelper h) {
        try(var old=player(h);var replacement=player(h)) {
            ignite(old,4,80);old.kill();h.assertTrue(!ExoticBurnManager.isBurning(old),"Dead player retained burn");
            ExoticBurnManager.clone(new net.minecraftforge.event.entity.player.PlayerEvent.Clone(replacement,old,true));
            h.assertTrue(!ExoticBurnManager.isBurning(replacement)&&replacement.getRemainingFireTicks()<=0,"Respawn copied exotic to vanilla fire");h.succeed();
        }
    }
    @GameTest(template="empty") public static void unknownClientFireHasNoVanillaFallback(GameTestHelper h) {
        h.assertTrue(ExoticBurnSnapshots.visual("wuxingxiantu:missing_fire")==null
                &&ExoticBurnSnapshots.visual("minecraft:fire")==null,"Unknown ID returned a vanilla profile");h.succeed();
    }
    @GameTest(template="empty") public static void snapshotsOrderUuidAndDimension(GameTestHelper h) {
        try(var p=player(h)) {
            ignite(p,3,80);var cache=new ExoticBurnSnapshots();var full=ExoticBurnManager.snapshot(p,false);
            cache.accept(full,Level.NETHER.location(),0);h.assertTrue(cache.get(p.getId(),p.getUUID(),0)==null,"Wrong dimension accepted");
            cache.accept(full,p.level().dimension().location(),0);
            h.assertTrue(cache.get(p.getId(),java.util.UUID.randomUUID(),0)==null,"Recycled entity ID inherited fire");
            h.assertTrue(cache.remaining(p.getId(),p.getUUID(),5)==75,"Client prediction did not count down");
            var clear=ExoticBurnManager.snapshot(p,true);cache.accept(clear,p.level().dimension().location(),5);
            cache.accept(full,p.level().dimension().location(),6);
            h.assertTrue(cache.get(p.getId(),p.getUUID(),6)==null,"Late snapshot revived cleared state");h.succeed();
        }
    }
    @GameTest(template="empty") public static void trackingSnapshotAndPacketRoundtrip(GameTestHelper h) {
        try(var p=player(h)) {
            ignite(p,6,120);var full=ExoticBurnManager.snapshot(p,false);
            var buffer=new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
            try {ExoticBurnPacket.encode(full,buffer);h.assertTrue(full.equals(ExoticBurnPacket.decode(buffer)),"Full snapshot lost fields");}
            finally {buffer.release();}
            var lateTracker=new ExoticBurnSnapshots();lateTracker.accept(full,p.level().dimension().location(),10);
            h.assertTrue(lateTracker.get(p.getId(),p.getUUID(),10)!=null,"New tracker did not get complete state");
            lateTracker.remove(p.getId(),p.getUUID());h.assertTrue(lateTracker.get(p.getId(),p.getUUID(),10)==null,"Unload retained client state");h.succeed();
        }
    }
    @GameTest(template="empty") public static void persistenceAndCopyPreserveSource(GameTestHelper h) {
        try(var p=player(h);var replacement=player(h)) {
            ExoticBurnManager.ignite(p,FireCatalog.tier(5).key(),100,replacement);
            var original=ExoticBurnManager.state(p);h.assertTrue(original.equals(ExoticBurnState.load(original.save())),"Saved state lost timer or source");
            ExoticBurnManager.copyState(p,replacement);h.assertTrue(original.equals(ExoticBurnManager.state(replacement)),"Copy lost state");
            h.assertTrue(replacement.getRemainingFireTicks()<=0,"Copy enabled vanilla fire");h.succeed();
        }
    }
    @GameTest(template="empty") public static void contactFireResistanceAndPowderSnow(GameTestHelper h) {
        try(var p=player(h)) {
            p.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE,200));ignite(p,1,80);float health=p.getHealth();
            for(int i=0;i<20;i++)ExoticBurnManager.tick(p);
            h.assertTrue(ExoticBurnManager.isBurning(p)&&p.getHealth()==health,"Contact fire resistance changed");
            p.isInPowderSnow=true;ExoticBurnManager.tick(p);h.assertTrue(!ExoticBurnManager.isBurning(p),"Contact burn ignored powder snow");h.succeed();
        }
    }
    @GameTest(template="empty") public static void elementalBalanceAndDomainRefresh(GameTestHelper h) {
        try(var owner=player(h);var target=player(h)) {
            var id=FireCatalog.tier(1).key();target.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE,200));
            ExoticBurnManager.ignite(target,id,5,owner,true);ExoticBurnManager.ignite(target,id,5,owner,true);
            ExoticBurnManager.ignite(target,id,1,owner,false);
            var state=ExoticBurnManager.state(target);
            h.assertTrue(state.intensity()==2&&state.enchantmentLevel()==5&&state.remainingTicks()==160,"Domain refresh changed stacks or level");
            float before=target.getHealth();for(int i=0;i<20;i++)ExoticBurnManager.tick(target);
            h.assertTrue(Math.abs(before-target.getHealth()-2.4F)<.01,"Elemental magic damage or Fire Resistance behavior changed");h.succeed();
        }
    }
    private ExoticBurnGameTests() {}
}
