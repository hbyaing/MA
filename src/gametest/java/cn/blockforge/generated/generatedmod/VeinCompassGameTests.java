package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.fixtures.GameTestPlayer;
import cn.blockforge.generated.generatedmod.veincompass.*;
import cn.blockforge.generated.generatedmod.world.SpiritVeinData;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.gametest.*;

@GameTestHolder("wuxingxiantu_entry") @PrefixGameTestTemplate(false)
public final class VeinCompassGameTests {
    @GameTest(template="empty") public static void compassSnapshotUsesExistingDimensionDataAndRequiresRealHeldCompass(GameTestHelper helper){
        try(var player=new GameTestPlayer(helper.getLevel())){player.join();var pos=helper.absolutePos(new BlockPos(1,1,1));player.setPos(pos.getX()+.5,pos.getY(),pos.getZ()+.5);
            var compass=new ItemStack(WuXingItems.SPIRIT_VEIN_COMPASS.get());player.getInventory().setItem(9,compass);
            helper.assertTrue(!VeinCompassSync.permitted(player),"Inventory-only compass enabled HUD sync");
            player.getInventory().setItem(9,ItemStack.EMPTY);player.setItemInHand(InteractionHand.OFF_HAND,compass);
            helper.assertTrue(VeinCompassSync.permitted(player),"Real offhand compass did not enable sync");
            var data=SpiritVeinData.get(player.serverLevel());var expected=data.nearest(player.blockPosition(),384);var packet=VeinCompassSync.snapshot(player);
            helper.assertTrue(packet.dimension().equals(player.level().dimension().location().toString())&&packet.concentration()==data.auraAt(player.blockPosition()),"HUD used another dimension or aura calculation");
            helper.assertTrue(packet.hasVein()==expected.isPresent()&&(expected.isEmpty()||packet.center().equals(expected.get().center())&&packet.grade()==expected.get().grade()),"HUD detected phantom or differently ranked veins");
            player.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);helper.assertTrue(!VeinCompassSync.permitted(player),"Putting compass away left server updates permitted");
        }helper.succeed();
    }
    @GameTest(template="empty") public static void compassWirePreservesDimensionAndConcentrationAndCopiesPosition(GameTestHelper helper){
        var center=new BlockPos.MutableBlockPos(-23,-40,78);var packet=new VeinCompassStatePacket("minecraft:the_nether",85,true,center,120,4);center.set(0,0,0);
        helper.assertTrue(packet.center().equals(new BlockPos(-23,-40,78)),"Mutable caller moved the HUD vein");
        var buffer=new FriendlyByteBuf(Unpooled.buffer());try{packet.encode(buffer);helper.assertTrue(packet.equals(new VeinCompassStatePacket(buffer)),"Compass wire lost dimension or vein values");}finally{buffer.release();}
        boolean invalid=false;try{new VeinCompassStatePacket("minecraft:overworld",101,false,BlockPos.ZERO,0,0);}catch(IllegalArgumentException expected){invalid=true;}helper.assertTrue(invalid,"Invalid concentration packet accepted");helper.succeed();
    }
    @GameTest(template="empty") public static void compassDirectionAndConcentrationLabelsRespectSourceBoundaries(GameTestHelper helper){
        helper.assertTrue(CompassPresentation.direction(0,-1)==0&&CompassPresentation.direction(1,-1)==1&&CompassPresentation.direction(1,0)==2&&CompassPresentation.direction(0,1)==4&&CompassPresentation.direction(-1,0)==6,"Compass cardinals are mirrored or rotated");
        helper.assertTrue(CompassPresentation.quality(8)==0&&CompassPresentation.quality(9)==1&&CompassPresentation.quality(25)==1&&CompassPresentation.quality(26)==2&&CompassPresentation.quality(85)==4&&CompassPresentation.quality(86)==5,"Concentration label thresholds drifted");helper.succeed();
    }
}
