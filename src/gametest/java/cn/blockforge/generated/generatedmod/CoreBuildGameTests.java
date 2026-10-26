package cn.blockforge.generated.generatedmod;
import cn.blockforge.generated.generatedmod.fixtures.GameTestPlayer;
import net.minecraft.gametest.framework.*;
import net.minecraft.core.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.*;
@GameTestHolder("wuxingxiantu_talismans") @PrefixGameTestTemplate(false)
public final class CoreBuildGameTests {
    @GameTest(template="empty") public static void buildPacketsRoundTripAndSelectionUsesMenuValidation(GameTestHelper h){
        var pos=new BlockPos(-120,78,931);var buffer=new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
        try{
            var input=new cn.blockforge.generated.generatedmod.network.packet.CoreBuildInputPacket(pos,true);
            cn.blockforge.generated.generatedmod.network.packet.CoreBuildInputPacket.encode(input,buffer);
            h.assertTrue(input.equals(cn.blockforge.generated.generatedmod.network.packet.CoreBuildInputPacket.decode(buffer)),"Build input codec mismatch");
            buffer.clear();var state=new cn.blockforge.generated.generatedmod.network.packet.CoreBuildProgressPacket(pos,35,false);
            cn.blockforge.generated.generatedmod.network.packet.CoreBuildProgressPacket.encode(state,buffer);
            h.assertTrue(state.equals(cn.blockforge.generated.generatedmod.network.packet.CoreBuildProgressPacket.decode(buffer)),"Server progress codec mismatch");
        }finally{buffer.release();}
        try(var p=new GameTestPlayer(h.getLevel())){p.join();var at=site(h,300);h.getLevel().setBlock(at,WuXingBlocks.DRAWING_PLATFORM.get().defaultBlockState(),2);p.setPos(at.getX()+.5,at.getY()+1,at.getZ()+.5);
            var be=(TalismanPlatformBlockEntity)h.getLevel().getBlockEntity(at);var menu=new TalismanPlatformMenu(72,p.getInventory(),be);
            h.assertTrue(menu.clickMenuButton(p,101)&&be.selectedInk()==1,"Server menu did not persist ink selection");
            h.assertTrue(!menu.clickMenuButton(p,199)&&be.selectedInk()==1,"Invalid ink slot accepted");
            p.setPos(at.getX()+100,at.getY(),at.getZ());h.assertTrue(!menu.clickMenuButton(p,100)&&be.selectedInk()==1,"Remote ink mutation accepted");
        }h.succeed();
    }
    @GameTest(template="empty") public static void noSafeExitLeavesWorldAndInventoryUntouched(GameTestHelper h){
        try(var p=new GameTestPlayer(h.getLevel())){p.join();p.getAbilities().instabuild=false;
            var core=site(h,260);h.getLevel().setBlock(core,WuXingBlocks.REFINING_FORGE_CORE.get().defaultBlockState(),2);materials(p,core);p.setPos(core.getX()+.5,core.getY()+1,core.getZ()+.5);
            for(int x=-9;x<=9;x++)for(int z=-9;z<=9;z++)if(Math.abs(x)>=3||Math.abs(z)>=3)h.getLevel().setBlock(core.offset(x,-1,z),Blocks.AIR.defaultBlockState(),2);
            int count=p.getInventory().items.stream().mapToInt(ItemStack::getCount).sum();
            h.assertTrue(CoreStructureBuilder.build(p,core).contains("安全落脚点"),"Unsafe evacuation was accepted");
            h.assertTrue(p.getInventory().items.stream().mapToInt(ItemStack::getCount).sum()==count&&h.getLevel().getBlockState(core.east()).isAir(),"Unsafe exit changed materials or blocks");h.succeed();
        }
    }
    @GameTest(template="empty") public static void talismanDescriptionUsesPillColors(GameTestHelper h){
        var stack=TalismanData.create("gather",1,TalismanCatalog.Quality.MID);var lines=new java.util.ArrayList<net.minecraft.network.chat.Component>();
        stack.getItem().appendHoverText(stack,h.getLevel(),lines,net.minecraft.world.item.TooltipFlag.NORMAL);
        h.assertTrue(lines.get(0).getStyle().getColor().getValue()==net.minecraft.ChatFormatting.AQUA.getColor(),"Effect description is not pill-style aqua");
        h.assertTrue(lines.stream().anyMatch(t->t.getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents tr&&tr.getKey().equals("pill.wuxingxiantu.requirement")&&t.getStyle().getColor().getValue()==net.minecraft.ChatFormatting.GOLD.getColor()),"Realm requirement lacks pill-style gold");h.succeed();
    }
    @GameTest(template="empty") public static void forgeClickBuildsImmediatelyAndLegacyPacketsCannotBuild(GameTestHelper h){
        try(var p=new GameTestPlayer(h.getLevel())){p.join();
            var core=site(h,180);h.getLevel().setBlock(core,WuXingBlocks.REFINING_FORGE_CORE.get().defaultBlockState(),2);materials(p,core);
            p.setPos(core.getX()+.5,core.getY(),core.getZ()-3);
            for(int i=0;i<100;i++)CoreStructureBuilder.input(p,core,true);
            h.assertTrue(!CoreStructureBuilder.complete(h.getLevel(),core),"Legacy packet initiated construction");
            var hit=new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(core),Direction.NORTH,core,false);
            WuXingBlocks.REFINING_FORGE_CORE.get().use(h.getLevel().getBlockState(core),h.getLevel(),core,p,net.minecraft.world.InteractionHand.MAIN_HAND,hit);
            h.assertTrue(CoreStructureBuilder.complete(h.getLevel(),core),"Forge still required held input");
            h.assertTrue(p.getInventory().items.stream().allMatch(ItemStack::isEmpty),"Forge material count mismatch");h.succeed();
        }
    }
    @GameTest(template="empty") public static void deniedPlacementRollsBackAndKeepsInventory(GameTestHelper h){
        try(var p=new GameTestPlayer(h.getLevel())){p.join();p.getAbilities().instabuild=false;
            var core=site(h,220);h.getLevel().setBlock(core,WuXingBlocks.REFINING_FORGE_CORE.get().defaultBlockState(),2);materials(p,core);p.setPos(core.getX()+5,core.getY(),core.getZ());
            int count=p.getInventory().items.stream().mapToInt(ItemStack::getCount).sum();
            class Deny { @net.minecraftforge.eventbus.api.SubscribeEvent public void deny(net.minecraftforge.event.level.BlockEvent.EntityPlaceEvent e){if(e.getEntity()==p)e.setCanceled(true);} }
            var listener=new Deny();net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(listener);
            try{h.assertTrue(CoreStructureBuilder.build(p,core).contains("保护"),"Protection cancellation ignored");}
            finally{net.minecraftforge.common.MinecraftForge.EVENT_BUS.unregister(listener);}
            h.assertTrue(CoreStructureBuilder.blueprint(h.getLevel(),core).keySet().stream().allMatch(pos->h.getLevel().getBlockState(pos).isAir()),"Canceled placement left partial blocks");
            h.assertTrue(p.getInventory().items.stream().mapToInt(ItemStack::getCount).sum()==count,"Canceled placement spent materials");h.succeed();
        }
    }
    @GameTest(template="empty") public static void furnaceRightClickBuildsImmediatelyAndMissingMaterialsStayUntouched(GameTestHelper h){
        try(var p=new GameTestPlayer(h.getLevel())){p.join();
            var core=site(h,340);var block=WuXingBlocks.ALCHEMY_FURNACE_LOW.get();
            h.getLevel().setBlock(core,block.defaultBlockState(),2);p.setPos(core.getX()+.5,core.getY(),core.getZ()-3);
            var before=new java.util.HashMap<BlockPos,net.minecraft.world.level.block.state.BlockState>();
            CoreStructureBuilder.blueprint(h.getLevel(),core).keySet().forEach(pos->before.put(pos,h.getLevel().getBlockState(pos)));
            p.getInventory().add(new ItemStack(WuXingBlocks.RUNE_STONE.get(),1));
            var hit=new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(core),Direction.NORTH,core,false);
            block.use(h.getLevel().getBlockState(core),h.getLevel(),core,p,net.minecraft.world.InteractionHand.MAIN_HAND,hit);
            h.assertTrue(before.entrySet().stream().allMatch(e->h.getLevel().getBlockState(e.getKey()).equals(e.getValue())),"Missing materials partially built furnace");
            h.assertTrue(p.getInventory().countItem(WuXingBlocks.RUNE_STONE.get().asItem())==1,"Failed click spent materials");
            p.getInventory().clearContent();materials(p,core);
            block.use(h.getLevel().getBlockState(core),h.getLevel(),core,p,net.minecraft.world.InteractionHand.MAIN_HAND,hit);
            h.assertTrue(AlchemyFurnaceBlock.isFormed(h.getLevel().getBlockState(core)),"One right click did not immediately assemble furnace");
            h.assertTrue(p.getInventory().items.stream().allMatch(ItemStack::isEmpty),"Click consumed incorrect material count");
            h.succeed();
        }
    }
    @GameTest(template="empty") public static void furnaceMissingMaterialNoticeIsThrottledAndCreativeStillBuilds(GameTestHelper h){
        try(var p=new GameTestPlayer(h.getLevel())){p.join();
            var notices=new java.util.concurrent.atomic.AtomicInteger();
            p.connection=new net.minecraft.server.network.ServerGamePacketListenerImpl(h.getLevel().getServer(),new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND),p){
                @Override public void send(net.minecraft.network.protocol.Packet<?> packet){if(packet instanceof net.minecraft.network.protocol.game.ClientboundSystemChatPacket)notices.incrementAndGet();}
                @Override public void send(net.minecraft.network.protocol.Packet<?> packet,net.minecraft.network.PacketSendListener listener){send(packet);}
            };
            var core=site(h,380);var block=WuXingBlocks.ALCHEMY_FURNACE_LOW.get();
            h.getLevel().setBlock(core,block.defaultBlockState(),2);p.setPos(core.getX()+.5,core.getY(),core.getZ()-3);
            var hit=new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(core),Direction.NORTH,core,false);
            for(int i=0;i<10;i++)block.use(h.getLevel().getBlockState(core),h.getLevel(),core,p,net.minecraft.world.InteractionHand.MAIN_HAND,hit);
            h.assertTrue(notices.get()==1,"Repeated missing-material clicks spammed messages: "+notices.get());
            p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);
            block.use(h.getLevel().getBlockState(core),h.getLevel(),core,p,net.minecraft.world.InteractionHand.MAIN_HAND,hit);
            h.assertTrue(AlchemyFurnaceBlock.isFormed(h.getLevel().getBlockState(core)),"Creative right click still required materials");
            h.assertTrue(notices.get()==2,"Successful build feedback was suppressed by missing-material cooldown");h.succeed();
        }
    }
    private static BlockPos site(GameTestHelper h,int x){
        var core=h.absolutePos(new BlockPos(x,50,0));
        for(int dx=-10;dx<=10;dx++)for(int dz=-10;dz<=10;dz++){
            h.getLevel().getChunkAt(core.offset(dx,0,dz));h.getLevel().setBlock(core.offset(dx,-1,dz),Blocks.STONE.defaultBlockState(),2);
            for(int y=0;y<6;y++)h.getLevel().setBlock(core.offset(dx,y,dz),Blocks.AIR.defaultBlockState(),2);
        }return core;
    }
    private static void materials(GameTestPlayer p,BlockPos core){
        var costs=new java.util.HashMap<Item,Integer>();CoreStructureBuilder.blueprint(p.level(),core).values().stream().filter(s->!s.isAir()).forEach(s->costs.merge(s.getBlock().asItem(),1,Integer::sum));
        costs.forEach((item,count)->p.getInventory().add(new ItemStack(item,count)));
    }
    @GameTest(template="empty") public static void bothCoresConsumeExactMaterialsAndEvacuatePlayer(GameTestHelper h){
        try(var p=new GameTestPlayer(h.getLevel())){p.join();p.getAbilities().instabuild=false;
            for(boolean furnace:new boolean[]{false,true}){
                var core=site(h,furnace?90:50);h.getLevel().setBlock(core,(furnace?WuXingBlocks.ALCHEMY_FURNACE_LOW.get():WuXingBlocks.REFINING_FORGE_CORE.get()).defaultBlockState(),2);
                p.getInventory().clearContent();materials(p,core);p.setPos(core.getX()+.5,core.getY()+1,core.getZ()+.5);
                String result=CoreStructureBuilder.build(p,core);
                h.assertTrue(result.equals("结构搭建完成"),"Build failed: "+result);
                h.assertTrue(CoreStructureBuilder.complete(h.getLevel(),core),"Build did not assemble actual structure");
                h.assertTrue(p.getInventory().items.stream().allMatch(ItemStack::isEmpty),"Material cost mismatch");
                h.assertTrue(p.distanceToSqr(core.getX()+.5,core.getY(),core.getZ()+.5)>9,"Player still inside new structure");
                h.assertTrue(h.getLevel().noCollision(p,p.getBoundingBox()),"Evacuated player intersects blocks");
                h.assertTrue(!CoreStructureBuilder.build(p,core).equals("结构搭建完成"),"Repeated build succeeded");
            }h.succeed();
        }
    }
    @GameTest(template="empty") public static void failureDoesNotSpendOrReplaceObstruction(GameTestHelper h){
        try(var p=new GameTestPlayer(h.getLevel())){p.join();p.getAbilities().instabuild=false;
            var core=site(h,130);h.getLevel().setBlock(core,WuXingBlocks.REFINING_FORGE_CORE.get().defaultBlockState(),2);p.setPos(core.getX()+5,core.getY(),core.getZ());
            h.assertTrue(CoreStructureBuilder.build(p,core).startsWith("缺少材料"),"Missing inventory accepted");
            h.assertTrue(h.getLevel().getBlockState(core.east()).isAir(),"Missing-material build partially placed blocks");
            materials(p,core);int count=p.getInventory().items.stream().mapToInt(ItemStack::getCount).sum();
            h.getLevel().setBlock(core.east(),Blocks.CHEST.defaultBlockState(),2);
            h.assertTrue(CoreStructureBuilder.build(p,core).contains("阻挡"),"Chest obstacle replaced");
            h.assertTrue(h.getLevel().getBlockState(core.east()).is(Blocks.CHEST)&&p.getInventory().items.stream().mapToInt(ItemStack::getCount).sum()==count,"Failed build destroyed chest or spent material");
            h.succeed();
        }
    }
    @GameTest(template="empty") public static void selectedInkOnlyIsConsumedAndPersists(GameTestHelper h)throws Exception{
        var be=new TalismanPlatformBlockEntity(BlockPos.ZERO,WuXingBlocks.DRAWING_PLATFORM.get().defaultBlockState(),TalismanPlatformBlockEntity.Kind.DRAW);
        int start=TalismanPlatformBlockEntity.inkStart(be.kind());
        var black=new ItemStack(WuXingItems.SPIRIT_INK.get());var red=new ItemStack(WuXingItems.CINNABAR_INK.get());be.setItem(start,black);be.setItem(start+1,red);be.selectInk(1);
        var service=cn.blockforge.generated.generatedmod.network.service.TalismanService.class;
        var consume=service.getDeclaredMethod("consumeInk",net.minecraft.world.Container.class,TalismanPlatformBlockEntity.Kind.class,int.class);consume.setAccessible(true);consume.invoke(null,be,be.kind(),2);
        h.assertTrue(black.getDamageValue()==0&&red.getDamageValue()==2,"Selected red ink consumed other bottle");
        var tag=be.saveWithoutMetadata();var copy=new TalismanPlatformBlockEntity(BlockPos.ZERO,be.getBlockState(),be.kind());copy.load(tag);h.assertTrue(copy.selectedInk()==1,"Selected ink not persistent");
        be.selectInk(2);var total=service.getDeclaredMethod("inkTotal",net.minecraft.world.Container.class,TalismanPlatformBlockEntity.Kind.class);total.setAccessible(true);
        h.assertTrue(((int)total.invoke(null,be,be.kind()))==0,"Empty selected slot silently fell back to another ink");h.succeed();
    }
}

