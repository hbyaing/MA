package cn.blockforge.generated.generatedmod;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("wuxingxiantu")
@PrefixGameTestTemplate(false)
public final class LocateIslandGameTests {
    @GameTest(template="furnace_empty",timeoutTicks=100)
    public static void locateUnexploredCandidateAndCommand(GameTestHelper h) {
        var level=h.getLevel();
        BlockPos from=new BlockPos(-123456,100,234567);
        int chunks=level.getChunkSource().getLoadedChunksCount();
        var result=SpiritShrineManager.locate(level,from);
        h.assertTrue(result!=null && !result.generated(),"No unexplored island candidate found");
        h.assertTrue(result.position().getY()==264,"Wrong entrance height");
        BlockPos origin=result.position().subtract(SpiritShrineManager.ENTRANCE);
        ChunkPos center=new ChunkPos(BlockPos.containing(origin.getX()+51,220,origin.getZ()+43));
        h.assertTrue(SpiritShrineManager.isCandidate(level.getSeed(),center),"Locate disagrees with placement seed");
        h.assertTrue(level.getChunkSource().getLoadedChunksCount()==chunks,"Locate loaded new chunks");
        var source=level.getServer().createCommandSourceStack().withLevel(level)
                .withPosition(net.minecraft.world.phys.Vec3.atCenterOf(from)).withPermission(4).withSuppressedOutput();
        var dispatcher=level.getServer().getCommands().getDispatcher();
        try {
            for(String id:new String[]{"wuxingxiantu:spirit_shrine_island","spirit_shrine_island"})
                h.assertTrue(dispatcher.execute("locate structure "+id,source)==1,"Locate alias failed: "+id);
            var suggestions=dispatcher.getCompletionSuggestions(dispatcher.parse("locate structure wuxingxiantu:",source)).join();
            h.assertTrue(suggestions.getList().stream().anyMatch(s->s.getText().equals("wuxingxiantu:spirit_shrine_island")),"Structure autocomplete missing");
            var locateNode=dispatcher.getRoot().getChild("locate");
            h.assertTrue(locateNode!=null,"locate command missing");
            var structureNode=locateNode.getChild("structure");
            h.assertTrue(structureNode!=null,"locate structure command missing");
            h.assertTrue(locateNode.getChildren().stream().noneMatch(n->n.getName().equals("spirit_shrine")),"Removed top-level placeholder still registered");
            h.assertTrue(structureNode.getChildren().stream().noneMatch(n->n.getName().equals("wuxingxiantu:spirit_shrine")),"Removed placeholder structure still registered");
            h.assertTrue(structureNode.getChildren().stream().noneMatch(n->n.getName().equals("spirit_shrine")),"Removed bare placeholder structure still registered");
        } catch(com.mojang.brigadier.exceptions.CommandSyntaxException ex) { throw new RuntimeException(ex); }
        h.succeed();
    }
}
