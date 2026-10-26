package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.fixtures.GameTestPlayer;
import cn.blockforge.generated.generatedmod.items.SpiritSwordItem;
import cn.blockforge.generated.generatedmod.network.packet.yujian.SwordFlightInputPacket;
import cn.blockforge.generated.generatedmod.yujian.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.gametest.*;

@GameTestHolder("wuxingxiantu")
@PrefixGameTestTemplate(false)
public final class FlightNurtureGameTests {
    @GameTest(template="empty")
    public static void sprintAlwaysHasHeadroomAcrossSwordTiers(GameTestHelper h) {
        for(var sword:FlyingSwordRegistry.definitions()) {
            double normal=sword.advanced().maxSpeed(YujianPhase.ACCELERATING,sword);
            double sprint=sword.advanced().maxSpeed(YujianPhase.JIYU,sword);
            h.assertTrue(sprint>=normal*1.24,"No useful sprint headroom: "+sword.itemId());
            double multiplier=1+0.25*sword.advanced().lingkongLevel(sword);
            double expected=YujianTuning.BASE_FLIGHT_SPEED*multiplier*cn.blockforge.generated.generatedmod.yujian.FlyingSwordConfig.SPEED_MULTIPLIER.get();
            h.assertTrue(Math.abs(normal-expected)<1e-8,"Normal speed was capped: "+sword.itemId());
            h.assertTrue(Math.abs(sprint/normal-1.5)<1e-8,"Sprint speed was capped: "+sword.itemId());
        }
        var fast=new net.minecraft.world.phys.Vec3(10,20,30);
        h.assertTrue(YujianTuning.finiteVelocity(fast).equals(fast),"Finite flight velocity was still clamped");
        h.assertTrue(YujianTuning.finiteVelocity(new net.minecraft.world.phys.Vec3(Double.NaN,0,0)).equals(net.minecraft.world.phys.Vec3.ZERO),
                "Invalid numeric velocity was accepted");
        // Sprint and descent occupy independent fields even when both actions are held.
        for(boolean sprint:new boolean[]{false,true})for(boolean down:new boolean[]{false,true}) {
            var packet=new SwordFlightInputPacket(false,false,true,false,false,down,false,false,sprint,false,1,0,0,0);
            var buffer=new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
            try {
                SwordFlightInputPacket.encode(packet,buffer);
                h.assertTrue(packet.equals(SwordFlightInputPacket.decode(buffer)),"Sprint/descent input fields became mixed");
            } finally {buffer.release();}
        }
        h.succeed();
    }

    private static ItemStack readySword(GameTestPlayer player) {
        var sword=new ItemStack(WuXingItems.DARK_IRON_SWORD.get());
        player.getInventory().setItem(0,sword);
        var nurture=SpiritSwordItem.nurture(sword);
        nurture.nurtureExp=nurture.requirement(SwordProfile.of(sword));
        SpiritSwordItem.saveNurture(sword,nurture);
        return sword;
    }

    @GameTest(template="empty")
    public static void uncappedHighSpeedSweepStopsAtSolidWall(GameTestHelper h) {
        var saved=new java.util.HashMap<net.minecraft.core.BlockPos,net.minecraft.world.level.block.state.BlockState>();
        var blockData=new java.util.HashMap<net.minecraft.core.BlockPos,net.minecraft.nbt.CompoundTag>();
        var player=new GameTestPlayer(h.getLevel());
        player.cleanupAfter(h,()->{
            saved.forEach((pos,state)->h.getLevel().setBlockAndUpdate(pos,state));
            blockData.forEach((pos,tag)->{
                var entity=h.getLevel().getBlockEntity(pos);
                if(entity!=null){entity.load(tag);entity.setChanged();}
            });
        });
        // The ten-block path leaves the empty template. Prepare its own corridor instead
        // of relying on ambient terrain (including islands at Y=220) or chunk timing.
        var start=h.absolutePos(new net.minecraft.core.BlockPos(3,0,3))
                .atY(h.getLevel().getMaxBuildHeight()-8);
        player.setPos(start.getX()+0.5,start.getY(),start.getZ()+0.5);
        for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)
            player.keepTicking(h.getLevel(),start.offset(x*16,0,z*16));
        var velocity=new net.minecraft.world.phys.Vec3(10,0,0);
        var corridor=player.getBoundingBox().expandTowards(velocity).inflate(1);
        h.startSequence().thenWaitUntil(()->h.assertTrue(FlightCollision.loaded(player,corridor),
                "Flight fixture corridor chunks are not ready"))
        .thenExecute(()->{
            var positions=net.minecraft.core.BlockPos.betweenClosed(start.offset(-2,-1,-2),start.offset(12,3,2));
            for(var pos:positions) {
                var key=pos.immutable();saved.put(key,h.getLevel().getBlockState(pos));
                var entity=h.getLevel().getBlockEntity(pos);
                if(entity!=null)blockData.put(key,entity.saveWithFullMetadata());
            }
            // Deliberately seed an obstruction: skipping preparation must fail the open-path assertion.
            h.getLevel().setBlockAndUpdate(start.offset(2,0,0),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
            for(var pos:positions)h.getLevel().setBlockAndUpdate(pos,net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            player.join();
            var open=FlightCollision.sweep(player,player.getBoundingBox(),velocity,false);
            h.assertTrue(!open.blocked()&&open.loaded()&&open.movement().distanceTo(velocity)<1e-8,
                    "Clear flight path still limited movement to old speed cap: "+open+" at "+player.position());
            for(int y=0;y<3;y++)for(int z=-1;z<=1;z++) {
                var pos=start.offset(6,y,z);
                h.getLevel().setBlockAndUpdate(pos,net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
            }
            var blocked=FlightCollision.sweep(player,player.getBoundingBox(),velocity,false);
            h.assertTrue(blocked.blocked()&&blocked.loaded()&&blocked.movement().x>3&&blocked.movement().x<6,
                    "High-speed collision tunneled through wall or retained 3-block cap: "+blocked);
        }).thenSucceed();
    }

    @GameTest(template="empty")
    public static void nurtureConsumesManaWithoutCrystalAndRejectsReplay(GameTestHelper h) {
        try(var player=new GameTestPlayer(h.getLevel())) {
            var sword=readySword(player);var data=GeneratedMod.data(player);
            int cost=SwordNurture.breakthroughCost();data.wuxingxiantu$setAura(cost+5);
            h.assertTrue(SwordNurture.breakthrough(player,sword),"Mana-only breakthrough failed without crystal");
            var nurture=SpiritSwordItem.nurture(sword);
            h.assertTrue(nurture.nurtureStage==1&&nurture.nurtureExp==0&&nurture.insightPoints==1,"Sword progression was not updated once");
            h.assertTrue(data.wuxingxiantu$getAura()==5,"Wrong mana deduction");
            data.wuxingxiantu$setAura(cost+5);
            h.assertTrue(!SwordNurture.breakthrough(player,sword)&&data.wuxingxiantu$getAura()==cost+5,"Duplicate request charged or advanced again");
        }
        h.succeed();
    }

    @GameTest(template="empty")
    public static void nurtureInsufficientManaLeavesSwordAndCrystalIntact(GameTestHelper h) {
        try(var player=new GameTestPlayer(h.getLevel())) {
            var sword=readySword(player);var data=GeneratedMod.data(player);
            var crystal=new ItemStack(WuXingItems.AURA_CRYSTAL.get());player.getInventory().setItem(1,crystal);
            int cost=SwordNurture.breakthroughCost();data.wuxingxiantu$setAura(cost-1);
            var before=sword.copy();
            h.assertTrue(!SwordNurture.breakthrough(player,sword),"A crystal bypassed the mana requirement");
            h.assertTrue(ItemStack.matches(before,sword)&&data.wuxingxiantu$getAura()==cost-1&&crystal.getCount()==1,
                    "Failed breakthrough changed sword, mana or crystal");
            data.wuxingxiantu$setAura(cost);
            h.assertTrue(SwordNurture.breakthrough(player,sword)&&data.wuxingxiantu$getAura()==0&&crystal.getCount()==1,
                    "Exact mana payment consumed crystal or failed");
        }
        h.succeed();
    }
}
