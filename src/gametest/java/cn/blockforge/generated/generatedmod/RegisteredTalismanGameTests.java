package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.fixtures.GameTestPlayer;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.gametest.*;

@GameTestHolder("wuxingxiantu_talismans")
@PrefixGameTestTemplate(false)
public final class RegisteredTalismanGameTests {
    @GameTest(template="empty") public static void allRegisteredKindsSurviveSerializationWithoutKindNbt(GameTestHelper h){
        var unique=new java.util.HashSet<net.minecraft.world.item.Item>();
        for(String id:TalismanCatalog.ids()){
            var item=WuXingItems.talismanItem(id);unique.add(item);
            h.assertTrue(net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(item).getPath().equals("talisman_"+id),"Wrong registered ID");
            var plain=new ItemStack(item);
            h.assertTrue(TalismanData.getTalisman(plain).equals(id)&&TalismanData.quality(plain)==TalismanCatalog.Quality.PERFECT,"Plain /give item did not default to perfect grade");
            for(var q:TalismanCatalog.Quality.values()){
                var stack=TalismanData.create(id,16,q);
                h.assertTrue(!stack.getTag().contains(TalismanData.KEY),"New item still depends on kind NBT");
                var restored=ItemStack.of(stack.save(new net.minecraft.nbt.CompoundTag()));
                h.assertTrue(restored.is(item)&&restored.getCount()==16&&TalismanData.quality(restored)==q,"Serialization changed kind/count/grade");
                TalismanData.setTalisman(restored,id.equals("fire")?"gather":"fire");
                h.assertTrue(TalismanData.getTalisman(restored).equals(id),"NBT changed registered item effect");
            }
        }
        h.assertTrue(unique.size()==7,"Kinds share a registered item");h.succeed();
    }
    @GameTest(template="empty") public static void platformOutputTradeAndLegacyKindsRemainUsable(GameTestHelper h)throws Exception{
        var method=cn.blockforge.generated.generatedmod.network.service.TalismanService.class.getDeclaredMethod("talismanStack",String.class,int.class,TalismanCatalog.Quality.class);
        method.setAccessible(true);
        for(String id:TalismanCatalog.ids()){
            var output=(ItemStack)method.invoke(null,id,16,TalismanCatalog.Quality.PERFECT);
            h.assertTrue(output.is(WuXingItems.talismanItem(id))&&output.getCount()==16&&TalismanData.quality(output)==TalismanCatalog.Quality.PERFECT,"Platform outputs legacy or loses grade/count");
            var legacy=new ItemStack(WuXingItems.TALISMAN.get());TalismanData.setTalisman(legacy,id);
            h.assertTrue(TalismanData.getTalisman(legacy).equals(id),"Old saved kind lost");
        }
        for(String id:java.util.List.of("shield","heal")){
            var output=(ItemStack)method.invoke(null,id,2,TalismanCatalog.Quality.MID);
            h.assertTrue(output.is(WuXingItems.TALISMAN.get())&&TalismanData.getTalisman(output).equals(id),"Legacy engraved seal cannot produce its kind");
        }
        var random=net.minecraft.util.RandomSource.create(17);
        for(int i=0;i<100;i++){
            var trade=TaoistEvents.randomTalisman(random,8);
            h.assertTrue(!trade.is(WuXingItems.TALISMAN.get())&&trade.getCount()==8&&TalismanCatalog.has(TalismanData.getTalisman(trade)),"Trade still sells generic talismans");
        }h.succeed();
    }
    @GameTest(template="empty") public static void differentKindsShareCooldownAndConsumeOnlyOnSuccess(GameTestHelper h){
        try(var player=new GameTestPlayer(h.getLevel())){
            player.join();GeneratedMod.data(player).wuxingxiantu$setRealm(8);
            var stack=TalismanData.create("gather",2,TalismanCatalog.Quality.LOW);
            player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,stack);
            stack.use(h.getLevel(),player,net.minecraft.world.InteractionHand.MAIN_HAND);
            h.assertTrue(stack.getCount()==1,"Successful use did not consume one");
            var speed=TalismanData.create("speed",2,TalismanCatalog.Quality.LOW);
            player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,speed);
            speed.use(h.getLevel(),player,net.minecraft.world.InteractionHand.MAIN_HAND);
            h.assertTrue(speed.getCount()==2,"Switching item kind bypassed shared cooldown");
            h.assertTrue(player.getCooldowns().isOnCooldown(WuXingItems.TALISMAN.get()),"Legacy item bypasses cooldown");
        }h.succeed();
    }
}
