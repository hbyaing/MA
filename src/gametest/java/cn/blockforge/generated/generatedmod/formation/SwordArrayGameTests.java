package cn.blockforge.generated.generatedmod.formation;

import cn.blockforge.generated.generatedmod.WuXingItems;
import cn.blockforge.generated.generatedmod.economy.SpiritCurrency;
import cn.blockforge.generated.generatedmod.fixtures.GameTestPlayer;
import cn.blockforge.generated.generatedmod.items.HeartSwordItem;
import com.google.gson.JsonParser;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@GameTestHolder("wuxingxiantu_entry")
@PrefixGameTestTemplate(false)
public final class SwordArrayGameTests {
    private static BlockPos ground(GameTestHelper helper){
        helper.setBlock(new BlockPos(2,1,2),Blocks.STONE);return helper.absolutePos(new BlockPos(2,1,2));
    }
    private static GameTestPlayer player(GameTestHelper helper){
        var player=new GameTestPlayer(helper.getLevel());var ground=ground(helper);
        player.setPos(ground.getX()+.5D,ground.getY()+1,ground.getZ()+.5D);
        player.getInventory().setItem(0,new ItemStack(FormationBootstrap.ZHUXIAN_SWORD_CHART.get()));
        for(int i=0;i<4;i++)player.getInventory().setItem(i+1,new ItemStack(FormationBootstrap.swords().get(i).get()));
        player.getInventory().setItem(5,new ItemStack(WuXingItems.SPIRIT_STONE_EXQUISITE.get(),10));return player;
    }
    private static boolean use(GameTestHelper helper,GameTestPlayer player){
        var ground=ground(helper);var context=new UseOnContext(player,InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(ground).add(0,.5D,0),Direction.UP,ground,false));
        return player.getMainHandItem().getItem().useOn(context).consumesAction();
    }
    private static long value(GameTestPlayer player){return SpiritCurrency.value(player.getInventory().items);}
    private static void clear(GameTestPlayer player){SwordArrayServer.close(player.getUUID());player.close();}

    @GameTest(template="empty")
    public static void fourSwordsAndChartHaveRealRecipesModelsAndNativeDeployment(GameTestHelper helper){
        for(var item:FormationBootstrap.items()){
            var recipe=helper.getLevel().getRecipeManager().byKey(item.getId()).orElseThrow();
            helper.assertTrue(recipe instanceof CraftingRecipe&&recipe.getResultItem(helper.getLevel().registryAccess()).is(item.get()),
                    "Missing survival acquisition for "+item.getId());
            String root="assets/wuxingxiantu/";
            try(var input=SwordArrayGameTests.class.getClassLoader().getResourceAsStream(root+"models/item/"+item.getId().getPath()+".json")){
                helper.assertTrue(input!=null,"Missing authored item model: "+item.getId());
                var model=JsonParser.parseReader(new InputStreamReader(input,StandardCharsets.UTF_8)).getAsJsonObject();
                helper.assertTrue(model.get("loader").getAsString().equals("wuxingxiantu:formation_mesh"),"Authored model lost its mesh loader");
                String mesh=model.get("mesh").getAsString().split(":",2)[1];
                try(var geometry=SwordArrayGameTests.class.getClassLoader().getResourceAsStream(root+mesh)){
                    helper.assertTrue(geometry!=null,"Missing item mesh: "+mesh);
                    int count=JsonParser.parseReader(new InputStreamReader(geometry,StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonArray("quads").size();
                    helper.assertTrue(count>=32&&count%32==0,"Malformed authored item quad table: "+mesh);
                }
            }catch(java.io.IOException error){throw new AssertionError(error);}
        }
        var player=player(helper);
        try{
            long before=value(player);
            helper.assertTrue(use(helper,player)&&SwordArrayServer.active(player).isPresent(),"The real chart useOn entry failed to deploy");
            helper.assertTrue(value(player)==before&&player.getMainHandItem().getCount()==1,"Deployment consumed the chart or charged before formation");
            helper.assertTrue(use(helper,player)&&SwordArrayServer.active(player).isEmpty(),"Second chart use did not close the formation");
        }finally{clear(player);}
        helper.succeed();
    }

    @GameTest(template="empty")
    public static void firstPaymentIs240ThenEvery1200AndCannotDoubleCharge(GameTestHelper helper){
        helper.assertTrue(SwordArrayUpkeep.COST_LOW==3645&&!SwordArrayUpkeep.due(239)&&SwordArrayUpkeep.due(240)
                &&!SwordArrayUpkeep.due(1439)&&SwordArrayUpkeep.due(1440)&&SwordArrayUpkeep.due(2640),"Source fee cadence or grade value changed");
        var player=player(helper);
        try{
            helper.assertTrue(use(helper,player),"Funded source formation did not deploy");long before=value(player);
            SwordArrayServer.advance(player,239);helper.assertTrue(value(player)==before,"Payment before the 240th tick");
            SwordArrayServer.advance(player,240);helper.assertTrue(value(player)==before-3645,"First paid period did not debit 3645 low value");
            SwordArrayServer.advance(player,240);helper.assertTrue(value(player)==before-3645,"Replayed tick charged twice");
            SwordArrayServer.advance(player,1439);helper.assertTrue(value(player)==before-3645,"Second payment arrived before one full minute");
            SwordArrayServer.advance(player,1440);helper.assertTrue(value(player)==0&&SwordArrayServer.active(player).isPresent(),"Second exact payment failed or closed a paid interval");
            SwordArrayServer.advance(player,2640);helper.assertTrue(SwordArrayServer.active(player).isEmpty()&&value(player)==0,"Unfunded interval remained open or created negative balance");
        }finally{clear(player);}
        helper.succeed();
    }

    @GameTest(template="empty")
    public static void mixedGradesPayExactlyAndInsufficientUpkeepDoesNotMutateInventory(GameTestHelper helper){
        var player=player(helper);
        try{
            player.getInventory().setItem(5,new ItemStack(WuXingItems.SPIRIT_STONE_EXQUISITE.get(),4));
            player.getInventory().setItem(6,new ItemStack(WuXingItems.SPIRIT_STONE_SUPERIOR.get(),8));
            player.getInventory().setItem(7,new ItemStack(WuXingItems.SPIRIT_STONE_MIDDLE.get(),8));
            player.getInventory().setItem(8,new ItemStack(WuXingItems.SPIRIT_STONE.get(),8));
            helper.assertTrue(value(player)==3644&&!use(helper,player)&&value(player)==3644
                    &&SwordArrayServer.active(player).isEmpty(),"Insufficient deployment funds mutated inventory or opened a formation");
            player.getInventory().setItem(8,new ItemStack(WuXingItems.SPIRIT_STONE.get(),9));
            helper.assertTrue(value(player)==3645&&use(helper,player),"Equivalent mixed currency was rejected");
            SwordArrayServer.advance(player,240);helper.assertTrue(value(player)==0&&SwordArrayServer.active(player).isPresent(),"Mixed grades did not settle exactly");
            SwordArrayServer.close(player.getUUID());
            player.getInventory().setItem(5,new ItemStack(WuXingItems.SPIRIT_STONE_EXQUISITE.get(),5));
            player.getInventory().setItem(8,new ItemStack(WuXingItems.SPIRIT_STONE.get(),1));
            helper.assertTrue(use(helper,player),"Change-making fixture failed to open");SwordArrayServer.advance(player,240);
            helper.assertTrue(value(player)==1&&player.getInventory().countItem(WuXingItems.SPIRIT_STONE.get())==1,"Payment lost exact change");
            SwordArrayServer.close(player.getUUID());player.getInventory().setItem(5,new ItemStack(WuXingItems.SPIRIT_STONE_EXQUISITE.get(),5));
            player.getInventory().setItem(8,ItemStack.EMPTY);helper.assertTrue(use(helper,player),"Unfunded-upkeep fixture failed to open");
            player.getInventory().getItem(5).shrink(1);var before=player.getInventory().items.stream().map(ItemStack::copy).toList();
            SwordArrayServer.advance(player,240);
            helper.assertTrue(SwordArrayServer.active(player).isEmpty(),"Insufficient funds did not close the formation");
            for(int i=0;i<before.size();i++)helper.assertTrue(ItemStack.isSameItemSameTags(before.get(i),player.getInventory().items.get(i))
                    &&before.get(i).getCount()==player.getInventory().items.get(i).getCount(),"Failed payment mutated slot "+i);
        }finally{clear(player);}
        helper.succeed();
    }

    @GameTest(template="empty")
    public static void missingOrForeignSwordsCannotDeployAndLosingSwordsCloses(GameTestHelper helper){
        var player=player(helper);
        try{
            long before=value(player);var sword=player.getInventory().getItem(1);player.getInventory().setItem(1,ItemStack.EMPTY);
            helper.assertTrue(!use(helper,player)&&SwordArrayServer.active(player).isEmpty()&&value(player)==before,"Missing sword bypassed deployment requirements");
            player.getInventory().setItem(1,sword);sword.getOrCreateTag().putUUID(HeartSwordItem.OWNER_TAG,java.util.UUID.randomUUID());
            helper.assertTrue(!use(helper,player)&&value(player)==before,"A foreign bound sword powered the formation");
            sword.getOrCreateTag().putUUID(HeartSwordItem.OWNER_TAG,player.getUUID());helper.assertTrue(use(helper,player),"Owner's sword set was rejected");
            player.getInventory().setItem(1,ItemStack.EMPTY);SwordArrayServer.advance(player,1);
            helper.assertTrue(SwordArrayServer.active(player).isEmpty()&&value(player)==before,"Transferred sword left an unfunded detached formation");
        }finally{clear(player);}
        helper.succeed();
    }

    @GameTest(template="empty")
    public static void deathLogoutDimensionAndServerStopClearRuntimeState(GameTestHelper helper){
        var player=player(helper);
        try{
            helper.assertTrue(use(helper,player),"Death fixture failed to open");SwordArrayServer.death(new LivingDeathEvent(player,player.damageSources().generic()));
            helper.assertTrue(SwordArrayServer.active(player).isEmpty(),"Death retained render state");
            helper.assertTrue(use(helper,player),"Logout fixture failed to open");SwordArrayServer.logout(new PlayerEvent.PlayerLoggedOutEvent(player));
            helper.assertTrue(SwordArrayServer.active(player).isEmpty(),"Logout retained render state");
            helper.assertTrue(use(helper,player),"Dimension fixture failed to open");SwordArrayServer.dimension(new PlayerEvent.PlayerChangedDimensionEvent(player,Level.OVERWORLD,Level.NETHER));
            helper.assertTrue(SwordArrayServer.active(player).isEmpty(),"Dimension change retained render state");
            helper.assertTrue(use(helper,player),"Stop fixture failed to open");SwordArrayServer.stopped(new ServerStoppedEvent(helper.getLevel().getServer()));
            helper.assertTrue(SwordArrayServer.snapshot().isEmpty(),"Server stop persisted a runtime formation");
        }finally{clear(player);}
        helper.succeed();
    }

    @GameTest(template="empty")
    public static void lateObserverReceivesSameStateAndPacketFiltersDistanceAndDimension(GameTestHelper helper){
        var owner=player(helper);var observer=new GameTestPlayer(helper.getLevel());
        try{
            helper.assertTrue(use(helper,owner),"Synchronization fixture failed to open");var state=SwordArrayServer.active(owner).orElseThrow();
            var snapshot=SwordArrayServer.snapshotFor(observer);
            helper.assertTrue(snapshot.stream().anyMatch(packet->packet.cast().equals(state)&&!packet.stop()),"Late observer missed an existing formation");
            var buffer=new FriendlyByteBuf(Unpooled.buffer());
            try{
                var sent=new SwordArrayStatePacket(state,false);SwordArrayStatePacket.encode(sent,buffer);
                helper.assertTrue(SwordArrayStatePacket.decode(buffer).equals(sent)&&buffer.readableBytes()==0,"Formation packet changed origin, clock or seed");
            }finally{buffer.release();}
            helper.assertTrue(state.valid()&&state.visible(state.dimension(),state.origin().add(160,0,0))
                    &&!state.visible(state.dimension(),state.origin().add(160.01,0,0))
                    &&!state.visible(new ResourceLocation("minecraft:the_nether"),state.origin()),"Distance or dimension render filtering differs from source");
            SwordArrayServer.close(owner.getUUID());helper.assertTrue(SwordArrayServer.snapshotFor(observer).stream().noneMatch(packet->packet.cast().owner().equals(owner.getUUID())),"Closed formation appeared in late-join snapshot");
        }finally{clear(owner);observer.close();}
        helper.succeed();
    }
}
