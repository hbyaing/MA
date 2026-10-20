package cn.blockforge.generated.generatedmod;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("wuxingxiantu")
@PrefixGameTestTemplate(false)
public final class SpiritIslandGameTests {
    @GameTest(template="furnace_empty",timeoutTicks=300)
    public static void importedIslandPreservesMerchants(GameTestHelper h) {
        var level=h.getLevel();
        var template=level.getStructureManager().get(SpiritShrineManager.TEMPLATE).orElseThrow();
        h.assertTrue(template.getSize().equals(new net.minecraft.core.Vec3i(103,74,86)),"Incorrect template dimensions");
        var saved=template.save(new net.minecraft.nbt.CompoundTag());
        var blocks=saved.getList("blocks",net.minecraft.nbt.Tag.TAG_COMPOUND);
        h.assertTrue(blocks.size()==136014,"Incomplete island template");
        var entities=saved.getList("entities",net.minecraft.nbt.Tag.TAG_COMPOUND);
        boolean protectedTaoist=false;
        for(int i=0;i<entities.size();i++) {
            var nbt=entities.getCompound(i).getCompound("nbt");
            if(!nbt.getCompound("VillagerData").getString("profession").equals("wuxingxiantu:taoist")) continue;
            protectedTaoist=true;
            h.assertTrue(nbt.getInt("Xp")>0,"Template Taoist can change profession");
            h.assertTrue(nbt.getCompound("ForgeData").getBoolean(TaoistEvents.SHRINE_MERCHANT_TAG),"Template Taoist identity missing");
            h.assertTrue(nbt.getCompound("Brain").getCompound("memories").isEmpty(),"Template retains absolute brain coordinates");
        }
        h.assertTrue(protectedTaoist,"Protected template Taoist missing");
        BlockPos origin=h.absolutePos(BlockPos.ZERO).offset(0,0,256);
        origin=new BlockPos(origin.getX(),SpiritShrineManager.GENERATION_Y,origin.getZ());
        boolean placed=false;
        // The integration-test world persists between runs; use another empty test area.
        for(int attempt=0;attempt<16;attempt++) {
            for(int x=Math.floorDiv(origin.getX(),16);x<=Math.floorDiv(origin.getX()+102,16);x++)
                for(int z=Math.floorDiv(origin.getZ(),16);z<=Math.floorDiv(origin.getZ()+85,16);z++) level.getChunk(x,z);
            if(SpiritShrineManager.placeIsland(level,origin)) { placed=true;break; }
            origin=origin.offset(256,0,0);
        }
        h.assertTrue(placed,"Island placement failed in all empty test areas");
        int blockEntities=0;
        for(int i=0;i<blocks.size();i++) {
            var entry=blocks.getCompound(i);
            if(!entry.contains("nbt")) continue;
            var pos=entry.getList("pos",net.minecraft.nbt.Tag.TAG_INT);
            h.assertTrue(level.getBlockEntity(origin.offset(pos.getInt(0),pos.getInt(1),pos.getInt(2)))!=null,"Imported block entity missing");
            blockEntities++;
        }
        h.assertTrue(blockEntities==26,"Expected 26 authored block entities");
        var villagers=level.getEntitiesOfClass(Villager.class,new AABB(origin,origin.offset(103,74,86)));
        h.assertTrue(villagers.size()==2,"Imported island must have exactly two villagers");
        h.assertTrue(villagers.stream().anyMatch(v->v.getVillagerData().getProfession()==WuXingProfessions.SPIRIT_CULTIVATOR.get()),"Spirit cultivator lost");
        h.assertTrue(villagers.stream().anyMatch(v->v.getVillagerData().getProfession()==WuXingProfessions.TAOIST.get()),"Taoist lost");
        for(var v:villagers) h.assertTrue(!v.getOffers().isEmpty(),"Saved merchant trades lost");
        h.assertTrue(!SpiritShrineManager.placeIsland(level,origin),"Existing island overwritten");
        h.assertTrue(!SpiritShrineManager.placeIsland(level,new BlockPos(origin.getX(),300,origin.getZ())),"Out-of-height placement accepted");
        h.succeed();
    }

    @GameTest(template="furnace_empty",timeoutTicks=100)
    public static void mansionSpacingAndFixedHeight(GameTestHelper h) {
        h.assertTrue(SpiritShrineManager.GENERATION_Y==220,"Island bottom must be Y=220");
        for(int rx=-1;rx<=1;rx++) for(int rz=-1;rz<=1;rz++) {
            int count=0;
            for(int x=0;x<80;x++) for(int z=0;z<80;z++) {
                var pos=new net.minecraft.world.level.ChunkPos(rx*80+x,rz*80+z);
                if(SpiritShrineManager.isCandidate(123456L,pos)) {
                    count++;
                    h.assertTrue(x<60 && z<60,"Mansion separation violated");
                }
            }
            h.assertTrue(count==1,"Expected one candidate per 80x80 chunk region");
        }
        h.succeed();
    }
}
