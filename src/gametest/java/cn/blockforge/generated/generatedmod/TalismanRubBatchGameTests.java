package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.fixtures.GameTestPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.gametest.*;

/** Batch-copy material transactions used by the compact rubbing UI. */
@GameTestHolder("wuxingxiantu_talismans")
@PrefixGameTestTemplate(false)
public final class TalismanRubBatchGameTests {
    @GameTest(template="empty")
    public static void selectedInkLimitsBatchAndPreservesSealGrade(GameTestHelper h)throws Exception {
        try(var player=new GameTestPlayer(h.getLevel())) {
            var be=table();var seal=new ItemStack(WuXingItems.SEAL.get());
            TalismanData.setTalisman(seal,"thunder");TalismanData.setQuality(seal,TalismanCatalog.Quality.MID);
            be.setItem(TalismanPlatformBlockEntity.RUB_SEAL,seal);
            be.setItem(TalismanPlatformBlockEntity.RUB_PAPER,new ItemStack(WuXingItems.TALISMAN_PAPER.get(),32));
            var untouched=new ItemStack(WuXingItems.SPIRIT_INK.get());
            var selected=new ItemStack(WuXingItems.CINNABAR_INK.get());int cost=TalismanCatalog.drawCost("thunder");
            selected.setDamageValue(selected.getMaxDamage()-cost*3);
            be.setItem(2,untouched);be.setItem(3,selected);be.selectInk(1);
            rub(h,be,player);
            var result=be.getItem(TalismanPlatformBlockEntity.OUTPUT);
            h.assertTrue(result.is(WuXingItems.talismanItem("thunder"))&&result.getCount()==3,"Batch ignored selected ink limit");
            h.assertTrue(TalismanData.quality(result)==TalismanCatalog.Quality.MID,"Rubbing changed grade");
            h.assertTrue(be.getItem(TalismanPlatformBlockEntity.RUB_PAPER).getCount()==29&&be.getItem(3).isEmpty(),"Batch charged incorrect materials");
            h.assertTrue(untouched.getDamageValue()==0&&TalismanData.getTalisman(seal).equals("thunder"),"Other ink or reusable seal was consumed");
        }
        h.succeed();
    }

    @GameTest(template="empty")
    public static void blockedOutputDoesNotConsumeMaterials(GameTestHelper h)throws Exception {
        try(var player=new GameTestPlayer(h.getLevel())) {
            var be=table();var seal=new ItemStack(WuXingItems.SEAL.get());TalismanData.setTalisman(seal,"thunder");
            be.setItem(0,seal);be.setItem(1,new ItemStack(WuXingItems.TALISMAN_PAPER.get(),32));
            var ink=new ItemStack(WuXingItems.SPIRIT_INK.get());be.setItem(2,ink);
            be.setItem(TalismanPlatformBlockEntity.OUTPUT,TalismanData.create("fire",64,TalismanCatalog.Quality.LOW));
            rub(h,be,player);
            h.assertTrue(be.getItem(1).getCount()==32&&ink.getDamageValue()==0,"Blocked output charged input");
            h.assertTrue(be.getItem(TalismanPlatformBlockEntity.OUTPUT).getCount()==64,"Blocked output was overwritten");
        }
        h.succeed();
    }

    private static TalismanPlatformBlockEntity table(){return new TalismanPlatformBlockEntity(BlockPos.ZERO,WuXingBlocks.RUBBING_PLATFORM.get().defaultBlockState(),TalismanPlatformBlockEntity.Kind.RUB);}
    private static void rub(GameTestHelper h,TalismanPlatformBlockEntity be,GameTestPlayer player)throws Exception {
        var method=cn.blockforge.generated.generatedmod.network.service.TalismanService.class.getDeclaredMethod("handleRub",net.minecraft.server.level.ServerLevel.class,TalismanPlatformBlockEntity.class,int.class,net.minecraft.server.level.ServerPlayer.class);
        method.setAccessible(true);method.invoke(null,h.getLevel(),be,16,player);
    }
}
