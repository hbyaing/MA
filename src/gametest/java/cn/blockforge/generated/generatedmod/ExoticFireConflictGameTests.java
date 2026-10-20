package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.fixtures.GameTestPlayer;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.*;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import java.util.*;

@GameTestHolder("wuxingxiantu_firetargets")
@PrefixGameTestTemplate(false)
public final class ExoticFireConflictGameTests {
    @GameTest(template="empty") public static void compatibilityIsSymmetricAndRetainsOtherEnchantments(GameTestHelper h){
        var wrap=SpiritEnchantment.EXOTIC_FIRE_WRAP.get();var fire=Enchantments.FIRE_ASPECT;
        h.assertTrue(!wrap.isCompatibleWith(fire)&&!fire.isCompatibleWith(wrap),"Fire enchantments are not mutually exclusive in both directions");
        h.assertTrue(wrap.isCompatibleWith(Enchantments.SHARPNESS)&&Enchantments.SHARPNESS.isCompatibleWith(wrap),"Unrelated enchantment compatibility changed");
        h.assertTrue(!wrap.isCompatibleWith(wrap),"Same enchantment became independently compatible");h.succeed();
    }
    @GameTest(template="empty") public static void bothAnvilsRejectBothConflictOrders(GameTestHelper h){
        try(var player=new GameTestPlayer(h.getLevel())){
            player.join();GeneratedMod.data(player).setCurrentMana(1000);
            for(boolean custom:new boolean[]{false,true})for(boolean wrapFirst:new boolean[]{false,true}){
                AnvilMenu menu=custom?new XuanlingAnvilMenu(19,player.getInventory(),ContainerLevelAccess.NULL):new AnvilMenu(19,player.getInventory(),ContainerLevelAccess.NULL);
                var sword=new ItemStack(Items.DIAMOND_SWORD);sword.enchant(wrapFirst?SpiritEnchantment.EXOTIC_FIRE_WRAP.get():Enchantments.FIRE_ASPECT,1);
                var book=EnchantedBookItem.createForEnchantment(new EnchantmentInstance(wrapFirst?Enchantments.FIRE_ASPECT:SpiritEnchantment.EXOTIC_FIRE_WRAP.get(),1));
                menu.getSlot(0).set(sword);menu.getSlot(1).set(book);menu.createResult();
                h.assertTrue(menu.getSlot(2).getItem().isEmpty(),"Anvil combined conflicting fire enchantments: "+custom+"/"+wrapFirst);
                menu.getSlot(1).set(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(Enchantments.SHARPNESS,1)));menu.createResult();
                h.assertTrue(!menu.getSlot(2).getItem().isEmpty(),"Anvil also rejected valid sharpness upgrade: "+custom+"/"+wrapFirst);
            }h.succeed();
        }
    }
    @GameTest(template="empty") public static void loadedInfusersFilterConflictingChoicesForAllSpiritSwords(GameTestHelper h)throws Exception{
        // Optional compatibility mod: the dedicated acceptance run loads it explicitly.
        if(!net.minecraftforge.fml.ModList.get().isLoaded("enchantinginfuser")){h.succeed();return;}
        try(var player=new GameTestPlayer(h.getLevel())){
            player.join();
            var type=Class.forName("fuzs.enchantinginfuser.world.level.block.InfuserBlock$InfuserType");
            var menus=Class.forName("fuzs.enchantinginfuser.world.inventory.InfuserMenu");
            var create=menus.getMethod("create",type,int.class,net.minecraft.world.entity.player.Inventory.class,net.minecraft.world.Container.class,ContainerLevelAccess.class);
            var pos=h.absolutePos(new net.minecraft.core.BlockPos(1,1,1));
            for(String id:List.of("dark_iron_sword","mingjue_sword","qingming_sword","jixiao_sword","wuxiang_sword","guiyuan_sword","tianque_sword"))for(var kind:type.getEnumConstants())for(boolean wrapFirst:new boolean[]{true,false}){
                var item=net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(new net.minecraft.resources.ResourceLocation(GeneratedMod.MOD_ID,id));
                var sword=new ItemStack(Objects.requireNonNull(item));var existing=wrapFirst?SpiritEnchantment.EXOTIC_FIRE_WRAP.get():Enchantments.FIRE_ASPECT;
                var forbidden=wrapFirst?Enchantments.FIRE_ASPECT:SpiritEnchantment.EXOTIC_FIRE_WRAP.get();sword.enchant(existing,1);
                var input=new net.minecraft.world.SimpleContainer(1);
                var menu=(net.minecraft.world.inventory.AbstractContainerMenu)create.invoke(null,kind,20,player.getInventory(),input,ContainerLevelAccess.create(h.getLevel(),pos));
                var config=menus.getField("config").get(menu);var field=config.getClass().getField("allowModifyingEnchantments");var previous=field.get(config);
                try{
                    field.set(config,Arrays.stream(field.getType().getEnumConstants()).filter(v->v.toString().equals("ALL")).findFirst().orElseThrow());
                    input.setItem(0,sword);menu.slotsChanged(input);
                    var available=(Map<?,?>)menus.getMethod("getValidEnchantments").invoke(menu);
                    var click=menus.getMethod("clickEnchantmentLevelButton",net.minecraft.world.entity.player.Player.class,Enchantment.class,boolean.class);
                    h.assertTrue(((Number)click.invoke(menu,player,forbidden,true)).intValue()==-1,"Infuser accepted conflicting enchantment: "+id+"/"+kind+"/"+wrapFirst);
                    var selected=(Map<?,?>)menus.getMethod("getValidEnchantments").invoke(menu);
                    h.assertTrue(((Number)selected.get(existing)).intValue()>0&&((Number)selected.get(forbidden)).intValue()==0,"Infuser changed conflicting selection: "+id+"/"+kind+"/"+wrapFirst);
                    h.assertTrue(available.containsKey(existing)&&available.containsKey(Enchantments.SHARPNESS),"Infuser lost valid upgrade choices: "+id);
                }finally{field.set(config,previous);}
            }com.mojang.logging.LogUtils.getLogger().info("Fire-conflict actual infuser acceptance: 28 combinations passed");h.succeed();
        }
    }
}
