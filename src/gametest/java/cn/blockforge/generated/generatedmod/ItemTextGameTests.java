package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.alchemy.PillCatalog;
import cn.blockforge.generated.generatedmod.alchemy.PillData;
import cn.blockforge.generated.generatedmod.fire.FireBottleData;
import cn.blockforge.generated.generatedmod.text.ItemGradeResolver;
import cn.blockforge.generated.generatedmod.text.ItemTextCatalog;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("wuxingxiantu")
@PrefixGameTestTemplate(false)
public final class ItemTextGameTests {
    @GameTest(template="furnace_empty",timeoutTicks=100)
    public static void samePillRegistryUsesCurrentQuality(GameTestHelper h) {
        var pill=PillData.create(PillCatalog.AURA,PillData.Grade.LOW,PillData.Element.METAL,0);
        var item=pill.getItem();
        var low=ItemGradeResolver.resolve(pill,0);
        pill.getOrCreateTag().putString("Grade","PERFECT");
        var before=pill.getTag().copy();
        var perfect=ItemGradeResolver.resolve(pill,0);
        h.assertTrue(pill.getItem()==item,"Changing quality replaced the registered item");
        h.assertTrue(low.nameColor()==0x79C58B&&perfect.nameColor()==0xFFC94A,"Name ignored current pill quality");
        h.assertTrue(pill.getTag().equals(before),"Tooltip resolution modified pill data");
        h.succeed();
    }

    @GameTest(template="furnace_empty",timeoutTicks=100)
    public static void evolvedBottleUsesCurrentFireAndRainbow(GameTestHelper h) {
        var bottle=FireBottleData.fresh(1);
        bottle.getOrCreateTag().getCompound(FireBottleData.KEY).putString("fire_id","wuxingxiantu:nirvana");
        var before=bottle.getTag().copy();
        h.assertTrue(ItemTextCatalog.resolve(bottle).id().equals("fire_seed_nirvana"),"Bottle lore follows old registered tier");
        var first=ItemGradeResolver.resolve(bottle,0);
        var later=ItemGradeResolver.resolve(bottle,32);
        h.assertTrue(!first.grade().getStyle().getColor().equals(later.grade().getStyle().getColor()),"Nirvana grade stopped cycling");
        h.assertTrue(bottle.getTag().equals(before),"Tooltip resolution modified fire data");
        h.succeed();
    }

    @GameTest(template="furnace_empty",timeoutTicks=100)
    public static void missingTalismanQualityAndCreativeRarity(GameTestHelper h) {
        var talisman=new ItemStack(WuXingItems.TALISMAN.get());
        talisman.getOrCreateTag().putString(TalismanData.KEY,"fire");
        h.assertTrue(TalismanData.quality(talisman)==TalismanCatalog.Quality.PERFECT,"Obtained talisman did not default to perfect grade");
        TalismanData.setQuality(talisman,TalismanCatalog.Quality.PERFECT);
        h.assertTrue(ItemGradeResolver.resolve(talisman,0).nameColor()==0xFFC94A,"Talisman quality color is static");
        var creative=ItemGradeResolver.resolve(new ItemStack(WuXingItems.CREATION_PILL.get()),0);
        h.assertTrue(creative.creative()&&creative.nameColor()==0x777777,"Creative pill is styled as a unique reward");
        h.succeed();
    }
}
