package cn.blockforge.generated.generatedmod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("wuxingxiantu")
@PrefixGameTestTemplate(false)
public final class FurnaceGameTests {
    // Independent transcription of the supplied document, bottom to top.
    private static final String[][] PLAN={
        {"GGGGG","GCCCG","GCKCG","GCCCG","GGGGG"},
        {"GRFRG","R...R","F...F","R...R","GRFRG"},
        {"GRGRG","R...R","G...G","R...R","GRGRG"},
        {"GGGGG","GGGGG","GG.GG","GGGGG","GGGGG"}};

    static BlockPos build(GameTestHelper h) {
        var level=h.getLevel();
        BlockPos core=h.absolutePos(new BlockPos(3,1,3));
        AlchemyFurnaceStructure.mutating=true;
        try {
            for(int y=0;y<4;y++) for(int z=0;z<5;z++) for(int x=0;x<5;x++) {
                char c=PLAN[y][z].charAt(x);
                BlockState s=switch(c) {
                    case 'G' -> Blocks.GOLD_BLOCK.defaultBlockState();
                    case 'C' -> WuXingBlocks.ETERNAL_SPIRIT_COAL_BLOCK.get().defaultBlockState();
                    case 'K' -> WuXingBlocks.ALCHEMY_FURNACE.get().defaultBlockState();
                    case 'R' -> WuXingBlocks.RUNE_STONE.get().defaultBlockState();
                    case 'F' -> Blocks.BLAST_FURNACE.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING,
                            x==0?Direction.WEST:x==4?Direction.EAST:z==0?Direction.NORTH:Direction.SOUTH);
                    default -> Blocks.AIR.defaultBlockState();
                };
                level.setBlock(core.offset(x-2,y,z-2),s,3);
            }
        } finally { AlchemyFurnaceStructure.mutating=false; }
        return core;
    }

    @GameTest(template="furnace_empty",timeoutTicks=100)
    public static void formedMouthKeepsOpeningAndRestoresAir(GameTestHelper h) {
        BlockPos core=build(h);var level=h.getLevel();BlockPos mouth=core.above(3);
        h.assertTrue(level.getBlockState(mouth).isAir(),"Unformed mouth must remain air");
        AlchemyFurnaceStructure.refreshAround(level,core);
        BlockState seal=level.getBlockState(mouth);
        h.assertTrue(seal.is(WuXingBlocks.ALCHEMY_FURNACE_PART.get()) && seal.getValue(AlchemyFurnacePartBlock.SEAL),"Mouth not sealed");
        h.assertTrue(seal.getRenderShape()==net.minecraft.world.level.block.RenderShape.INVISIBLE,"Seal changes appearance");
        var probe=net.minecraft.world.phys.shapes.Shapes.box(.4,0,.4,.6,1,.6);
        h.assertTrue(!net.minecraft.world.phys.shapes.Shapes.joinIsNotEmpty(seal.getCollisionShape(level,mouth),probe,
                net.minecraft.world.phys.shapes.BooleanOp.AND),"Invisible mouth collision blocks the open center");
        h.assertTrue(seal.getLightBlock(level,mouth)==0 && seal.getOcclusionShape(level,mouth).isEmpty(),"Seal blocks light");
        h.assertTrue(AlchemyFurnaceStructure.isComplete(level,core),"Seal invalidates air blueprint");
        var snapshot=(AlchemyFurnacePartEntity)level.getBlockEntity(mouth);
        var saved=(AlchemyFurnacePartEntity)BlockEntity.loadStatic(mouth,seal,snapshot.saveWithFullMetadata());
        h.assertTrue(saved.original.isAir() && saved.core.equals(core),"Seal snapshot lost on save");
        var player=net.minecraftforge.common.util.FakePlayerFactory.getMinecraft(level);
        player.setPos(core.getX()+0.5,core.getY()+5,core.getZ()+0.5);
        player.move(net.minecraft.world.entity.MoverType.SELF,new net.minecraft.world.phys.Vec3(0,-5,0));
        // The authored mouth is narrower than a player; test the central ray/probe, not player fit.
        h.assertTrue(player.getY()<core.getY()+4-.001,"Old full-cell seal remains above the rim");
        // Simulate an older formed save without the seal; periodic refresh must repair it.
        AlchemyFurnaceStructure.mutating=true;
        try { level.setBlock(mouth,Blocks.AIR.defaultBlockState(),3); }
        finally { AlchemyFurnaceStructure.mutating=false; }
        AlchemyFurnaceStructure.refreshAround(level,core);
        h.assertTrue(level.getBlockState(mouth).is(WuXingBlocks.ALCHEMY_FURNACE_PART.get()),"Existing formed furnace not upgraded");
        level.destroyBlock(core.offset(2,0,2),false);
        h.assertTrue(level.getBlockState(mouth).isAir(),"Seal remains after dismantling");
        player.setPos(core.getX()+0.5,core.getY()+5,core.getZ()+0.5);
        player.move(net.minecraft.world.entity.MoverType.SELF,new net.minecraft.world.phys.Vec3(0,-5,0));
        h.assertTrue(player.getY()<core.getY()+3,"Invisible collision remains after dismantling");
        h.succeed();
    }

    @GameTest(template="furnace_empty",timeoutTicks=100)
    public static void everyComponentFindsController(GameTestHelper h) {
        BlockPos core=build(h);var level=h.getLevel();
        h.assertTrue(AlchemyFurnaceStructure.findCore(level,core.east(2))==null,"Unformed component redirected");
        AlchemyFurnaceStructure.refreshAround(level,core);
        int components=0;
        for(int y=0;y<4;y++) for(int z=0;z<5;z++) for(int x=0;x<5;x++) {
            BlockPos pos=core.offset(x-2,y,z-2);
            BlockPos found=AlchemyFurnaceStructure.findCore(level,pos);
            if (PLAN[y][z].charAt(x)=='.') h.assertTrue(found==null,"Air slot counted as a component");
            else { h.assertTrue(core.equals(found),"Component does not resolve controller: "+pos); components++; }
        }
        h.assertTrue(components==81,"Wrong component count");
        h.assertTrue(AlchemyFurnaceStructure.findCore(level,core.east(3))==null,"Unrelated neighboring block redirected");
        level.destroyBlock(core.offset(2,0,2),false);
        h.assertTrue(AlchemyFurnaceStructure.findCore(level,core.east(2))==null,"Dismantled component still redirected");
        h.succeed();
    }

    @GameTest(template="furnace_empty",timeoutTicks=100)
    public static void blueprintAndAir(GameTestHelper h) {
        BlockPos core=build(h); var level=h.getLevel();
        h.assertTrue(AlchemyFurnaceStructure.isComplete(level,core),"Document blueprint rejected");
        BlockPos roof=core.above(3);
        level.setBlock(roof,Blocks.GOLD_BLOCK.defaultBlockState(),3);
        h.assertTrue(!AlchemyFurnaceStructure.isComplete(level,core),"Roof opening must remain air");
        level.setBlock(roof,Blocks.AIR.defaultBlockState(),3);
        level.setBlock(core.above(),Blocks.STONE.defaultBlockState(),3);
        h.assertTrue(!AlchemyFurnaceStructure.isComplete(level,core),"Interior obstruction accepted");
        h.succeed();
    }

    @GameTest(template="furnace_empty",timeoutTicks=100)
    public static void facing(GameTestHelper h) {
        BlockPos core=build(h); var level=h.getLevel();
        for(Direction d:Direction.Plane.HORIZONTAL) {
            BlockPos p=core.relative(d,2).above();
            AlchemyFurnaceStructure.mutating=true;
            level.setBlock(p,Blocks.BLAST_FURNACE.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING,d.getOpposite()),3);
            AlchemyFurnaceStructure.mutating=false;
            h.assertTrue(!AlchemyFurnaceStructure.isComplete(level,core),"Inward furnace accepted: "+d);
            AlchemyFurnaceStructure.mutating=true;
            level.setBlock(p,Blocks.BLAST_FURNACE.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING,d),3);
            AlchemyFurnaceStructure.mutating=false;
        }
        h.assertTrue(AlchemyFurnaceStructure.isComplete(level,core),"All outward furnaces rejected");
        h.succeed();
    }

    @GameTest(template="furnace_empty",timeoutTicks=100)
    public static void storageAndCoreRemoval(GameTestHelper h) {
        BlockPos core=build(h); var level=h.getLevel(); BlockPos furnace=core.north(2).above();
        ((Container)level.getBlockEntity(furnace)).setItem(0,new ItemStack(Items.RAW_IRON,7));
        AlchemyFurnaceStructure.refreshAround(level,core);
        h.assertTrue(AlchemyFurnaceBlock.isFormed(level.getBlockState(core)),"Did not form");
        h.assertTrue(level.getBlockEntity(furnace) instanceof AlchemyFurnacePartEntity,"No snapshot entity");
        AlchemyFurnacePartEntity part=(AlchemyFurnacePartEntity)level.getBlockEntity(furnace);
        BlockEntity copy=BlockEntity.loadStatic(furnace,part.getBlockState(),part.saveWithFullMetadata());
        h.assertTrue(copy instanceof AlchemyFurnacePartEntity,"Snapshot cannot deserialize");
        h.assertTrue(((Container)((AlchemyFurnacePartEntity)copy).originalEntity()).getItem(0).getCount()==7,"Snapshot inventory lost");
        BlockPos floor=core.east();
        h.assertTrue(level.getBlockState(floor).getLightBlock(level,floor)==0,"Shell blocks skylight");
        h.assertTrue(level.getBlockState(floor).getOcclusionShape(level,floor).isEmpty(),"Shell culls ground");
        level.destroyBlock(core,false);
        h.assertTrue(level.getBlockState(furnace).is(Blocks.BLAST_FURNACE),"Furnace not restored after core removal");
        h.assertTrue(((Container)level.getBlockEntity(furnace)).getItem(0).getCount()==7,"Inventory not restored");
        h.assertTrue(level.getBlockState(floor).is(WuXingBlocks.ETERNAL_SPIRIT_COAL_BLOCK.get()),"Floor not restored");
        h.succeed();
    }

    @GameTest(template="furnace_empty",timeoutTicks=100)
    public static void componentRemoval(GameTestHelper h) {
        BlockPos core=build(h); var level=h.getLevel();
        AlchemyFurnaceStructure.refreshAround(level,core);
        BlockPos broken=core.offset(2,0,2);
        // The persistent GameTest level can contain drops from earlier runs at this template position.
        // Count the immediate inventory delta so a previous gold block cannot masquerade as duplication.
        var dropArea=new net.minecraft.world.phys.AABB(broken).inflate(1);
        int goldBefore=level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,dropArea).stream()
                .filter(e->e.getItem().is(Items.GOLD_BLOCK)).mapToInt(e->e.getItem().getCount()).sum();
        level.destroyBlock(broken,true);
        h.assertTrue(!AlchemyFurnaceBlock.isFormed(level.getBlockState(core)),"Broken structure stayed formed");
        h.assertTrue(level.getBlockState(broken).isAir(),"Destroyed component reappeared");
        h.assertTrue(level.getBlockState(core.offset(-2,0,-2)).is(Blocks.GOLD_BLOCK),"Remaining components not restored");
        int gold=level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
                dropArea).stream()
                .filter(e->e.getItem().is(Items.GOLD_BLOCK)).mapToInt(e->e.getItem().getCount()).sum();
        h.assertTrue(gold-goldBefore==1,"Broken gold component did not drop exactly once: before="+goldBefore+", after="+gold);
        h.succeed();
    }

    @GameTest(template="furnace_empty",timeoutTicks=100)
    public static void furnaceInventoryDropsOnce(GameTestHelper h) {
        BlockPos core=build(h); var level=h.getLevel(); BlockPos furnace=core.north(2).above();
        // Reused GameTest worlds may retain item entities from a previous run.
        var dropArea=new net.minecraft.world.phys.AABB(furnace).inflate(1);
        var existingDrops=level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,dropArea)
                .stream().map(net.minecraft.world.entity.Entity::getUUID).collect(java.util.stream.Collectors.toSet());
        ((Container)level.getBlockEntity(furnace)).setItem(0,new ItemStack(Items.RAW_IRON,7));
        AlchemyFurnaceStructure.refreshAround(level,core);
        level.destroyBlock(furnace,true);
        var drops=level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,dropArea)
                .stream().filter(e->!existingDrops.contains(e.getUUID())).toList();
        int iron=drops.stream().filter(e->e.getItem().is(Items.RAW_IRON)).mapToInt(e->e.getItem().getCount()).sum();
        int blocks=drops.stream().filter(e->e.getItem().is(Items.BLAST_FURNACE)).mapToInt(e->e.getItem().getCount()).sum();
        h.assertTrue(iron==7 && blocks==1,"Furnace destruction duplicated or lost inventory: "+iron+" / "+blocks);
        h.succeed();
    }
}
