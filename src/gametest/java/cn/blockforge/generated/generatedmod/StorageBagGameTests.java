package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.crafting.*;
import cn.blockforge.generated.generatedmod.fixtures.GameTestPlayer;
import cn.blockforge.generated.generatedmod.items.HeartSwordItem;
import cn.blockforge.generated.generatedmod.plants.*;
import cn.blockforge.generated.generatedmod.storage.*;
import cn.blockforge.generated.generatedmod.storage.menu.BagMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.gametest.*;
import java.util.*;

@GameTestHolder("wuxingxiantu_entry") @PrefixGameTestTemplate(false)
public final class StorageBagGameTests {
    private static GameTestPlayer player(GameTestHelper helper) {
        var player=new GameTestPlayer(helper.getLevel());player.join();
        var pos=helper.absolutePos(new BlockPos(1,1,1));player.setPos(pos.getX()+.5,pos.getY()+1,pos.getZ()+.5);
        var bag=new ItemStack(StorageRegistry.BAG_ITEM.get());bag.getOrCreateTag().putUUID("bag_id",UUID.randomUUID());
        player.setItemInHand(InteractionHand.MAIN_HAND,bag);return player;
    }
    private static BagMenu menu(GameTestPlayer player) {var menu=new BagMenu(42,player.getInventory(),player.getInventory().selected);player.containerMenu=menu;return menu;}
    private static ItemStack herb(int years,int count) {
        var stack=new ItemStack(SpiritPlantRegistry.SPIRIT_SPROUT_GRASS_ITEM.get(),count);
        HerbAge.setYears(stack,years);stack.getOrCreateTag().putString("ThirdParty","keep");return stack;
    }
    private static long total(BagInventory bag,Item item) {return bag.entries.stream().filter(entry->entry.display().is(item)).mapToLong(BagInventory.Entry::total).sum();}

    @GameTest(template="empty") public static void herbsAggregateWithoutAgingAndExtractYoungestWithExactNbt(GameTestHelper helper) {
        var inventory=new BagInventory();var young=herb(500,64);var old=herb(900,32);
        helper.assertTrue(inventory.insert(old)&&inventory.insert(young)&&old.isEmpty()&&young.isEmpty(),"Insert did not move exact quantities");
        helper.assertTrue(inventory.entries.size()==1&&inventory.entries.get(0).total()==96,"Same-tier herbs did not aggregate");
        var restored=BagInventory.load(inventory.save());var entry=restored.entries.get(0);
        var first=entry.take(64);var second=entry.take(64);
        helper.assertTrue(first.getCount()==64&&HerbAge.years(first)==500&&second.getCount()==32&&HerbAge.years(second)==900,"Medicinal ages changed or extraction order reversed");
        helper.assertTrue(first.getTag().getString("ThirdParty").equals("keep")&&second.getTag().getString("ThirdParty").equals("keep"),"Unrelated herb attributes lost");
        restored.insert(herb(700,4));var differentlyTagged=herb(700,2);differentlyTagged.getOrCreateTag().putString("ThirdParty","different");restored.insert(differentlyTagged);restored.compact();
        helper.assertTrue(restored.entries.size()==2,"Different non-age NBT merged");helper.succeed();
    }

    @GameTest(template="empty") public static void quantitiesExceedIntAndRejectOverflowWithoutLosingTheOfferedStack(GameTestHelper helper) {
        var list=new ListTag();var saved=new CompoundTag();saved.put("base",new ItemStack(Items.DIAMOND).save(new CompoundTag()));
        saved.putLong("count",(long)Integer.MAX_VALUE+50);list.add(saved);var inventory=BagInventory.load(list);
        helper.assertTrue(inventory.entries.get(0).total()==(long)Integer.MAX_VALUE+50&&inventory.entries.get(0).take(64).getCount()==64,"Bag quantity truncated to int or stack limit");
        saved.putLong("count",Long.MAX_VALUE-1);inventory=BagInventory.load(list);var offered=new ItemStack(Items.DIAMOND,2);
        helper.assertTrue(!inventory.insert(offered)&&offered.getCount()==2&&inventory.entries.get(0).total()==Long.MAX_VALUE-1,"Overflow consumed items or wrapped quantity");
        var nested=new ItemStack(StorageRegistry.BAG_ITEM.get());helper.assertTrue(!inventory.insert(nested)&&!nested.isEmpty()&&!nested.getItem().canFitInsideContainerItems(),"Bag nesting allowed");helper.succeed();
    }

    @GameTest(template="empty") public static void actualMenuDepositsWithdrawsFiltersAndLocksItsPhysicalBag(GameTestHelper helper) {
        try(var player=player(helper)) {
            var menu=menu(player);var data=BagData.get(player.serverLevel());var inventory=data.inventory(menu.bagId);
            player.getInventory().setItem(9,new ItemStack(Items.DIAMOND,64));
            helper.assertTrue(!menu.quickMoveStack(player,64).isEmpty()&&player.getInventory().getItem(9).isEmpty()&&total(inventory,Items.DIAMOND)==64,"Shift deposit lost items");
            menu.filter("minecraft:diamond",0);helper.assertTrue(menu.totals[0]==64,"Search did not use item ID");
            menu.clicked(1,0,ClickType.QUICK_MOVE,player);
            helper.assertTrue(player.getInventory().countItem(Items.DIAMOND)==64&&total(inventory,Items.DIAMOND)==0,"Shift withdrawal duplicated or lost items");
            var identity=player.getMainHandItem().copy();menu.clicked(92,player.getInventory().selected,ClickType.SWAP,player);
            helper.assertTrue(ItemStack.matches(identity,player.getMainHandItem()),"Hotbar swap displaced active bag");
            for(int i=0;i<70;i++){var stack=new ItemStack(Items.DIRT);stack.setHoverName(Component.literal("entry-"+i));inventory.insert(stack);}menu.storageChanged();menu.filter("",0);menu.scrollTo(1000);
            helper.assertTrue(menu.info.get(0)>0&&inventory.entries.size()==70,"Scrolling deleted or truncated hidden entries");
            menu.filter("does-not-exist",0);helper.assertTrue(inventory.entries.size()==70&&Arrays.stream(menu.totals).sum()==0,"Filtering modified backing contents");
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(ModEffects.PARALYSIS.get(),40));
            helper.assertTrue(!menu.clickMenuButton(player,3)&&menu.quickMoveStack(player,64).isEmpty(),"Paralysis bypassed bag mutations");
            player.removeEffect(ModEffects.PARALYSIS.get());
            player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);menu.setCarried(new ItemStack(Items.EMERALD,3));menu.clicked(1,0,ClickType.PICKUP,player);
            helper.assertTrue(menu.getCarried().getCount()==3&&!menu.clickMenuButton(player,3),"Stale menu mutated bag or cursor");menu.removed(player);
        }helper.succeed();
    }

    @GameTest(template="empty") public static void portableCraftingKeepsBoundOwnerAndRejectsForeignInputsBeforeConsumption(GameTestHelper helper) {
        try(var player=player(helper)) {
            var menu=menu(player);var sword=new ItemStack(WuXingItems.DARK_IRON_SWORD.get());sword.getOrCreateTag().putUUID(HeartSwordItem.OWNER_TAG,player.getUUID());EquipmentCrafting.write(sword).putInt("Refine",3);
            for(int i=0;i<9;i++)menu.getSlot(55+i).set(i==4?sword:new ItemStack(i==1||i==7?WuXingItems.AURA_CRYSTAL.get():Items.IRON_INGOT));
            var result=menu.getSlot(0).getItem();helper.assertTrue(result.is(WuXingItems.AZURE_STEEL_SWORD.get())&&player.getUUID().equals(EquipmentBond.owner(result))&&EquipmentCrafting.refine(result)==3,"Pocket workbench washed owner or growth");
            menu.clicked(0,0,ClickType.QUICK_MOVE,player);
            helper.assertTrue(player.getInventory().countItem(WuXingItems.AZURE_STEEL_SWORD.get())==1&&menu.getSlot(59).getItem().isEmpty(),"Pocket crafting did not consume exactly one recipe");
            var foreign=new ItemStack(WuXingItems.DARK_IRON_SWORD.get());foreign.getOrCreateTag().putUUID(HeartSwordItem.OWNER_TAG,UUID.randomUUID());
            for(int i=0;i<9;i++)menu.getSlot(55+i).set(i==4?foreign:new ItemStack(i==1||i==7?WuXingItems.AURA_CRYSTAL.get():Items.IRON_INGOT));
            helper.assertTrue(menu.getSlot(0).getItem().isEmpty()&&!menu.getSlot(0).mayPickup(player)&&menu.quickMoveStack(player,0).isEmpty(),"Foreign equipment bypassed pocket crafting ownership");
            helper.assertTrue(menu.getSlot(59).getItem().getCount()==1&&menu.getSlot(55).getItem().getCount()==1,"Rejected craft consumed material");
            menu.removed(player);
        }helper.succeed();
    }

    public static final class PickupProbe {
        @SubscribeEvent public void pickup(EntityItemPickupEvent event) {
            if(event.getItem().getItem().is(Items.APPLE))event.setCanceled(true);
            if(event.getItem().getItem().is(Items.IRON_INGOT))event.getItem().setTarget(UUID.randomUUID());
        }
    }
    private static ItemEntity drop(GameTestPlayer player,Item item,int count,double dx) {
        var entity=new ItemEntity(player.serverLevel(),player.getX()+dx,player.getY(),player.getZ(),new ItemStack(item,count));
        entity.setNoPickUpDelay();player.serverLevel().addFreshEntity(entity);return entity;
    }
    @GameTest(template="empty") public static void magnetDefaultsOffAndRespectsPickupOwnerDelayCancellationAndEventChanges(GameTestHelper helper) {
        try(var player=player(helper)) {
            var bag=player.getMainHandItem();var id=bag.getTag().getUUID("bag_id");var inventory=BagData.get(player.serverLevel()).inventory(id);
            var allowed=drop(player,Items.DIAMOND,5,2);var owned=drop(player,Items.EMERALD,3,2);owned.setTarget(UUID.randomUUID());
            var delayed=drop(player,Items.COBBLESTONE,4,2);delayed.setPickUpDelay(20);var cancelled=drop(player,Items.APPLE,2,2);
            var changed=drop(player,Items.IRON_INGOT,2,2);var far=drop(player,Items.DIRT,2,5);var nested=drop(player,StorageRegistry.BAG_ITEM.get(),1,2);
            List<ItemEntity> entities=List.of(allowed,owned,delayed,cancelled,changed,far,nested);var probe=new PickupProbe();MinecraftForge.EVENT_BUS.register(probe);
            try {
                player.tickCount=10;BagMagnetEvents.tick(new TickEvent.PlayerTickEvent(TickEvent.Phase.END,player));
                helper.assertTrue(allowed.isAlive()&&inventory.entries.isEmpty(),"Fresh bag magnet defaulted on");
                bag.getOrCreateTag().putBoolean("bag_magnet",true);BagMagnetEvents.tick(new TickEvent.PlayerTickEvent(TickEvent.Phase.END,player));
                helper.assertTrue(!allowed.isAlive()&&total(inventory,Items.DIAMOND)==5,"Magnet failed permitted pickup");
                helper.assertTrue(owned.isAlive()&&delayed.isAlive()&&cancelled.isAlive()&&changed.isAlive()&&far.isAlive()&&nested.isAlive(),"Magnet ignored ownership, delay, cancellation, changed event, range or nesting");
            }finally{MinecraftForge.EVENT_BUS.unregister(probe);entities.forEach(ItemEntity::discard);}
        }helper.succeed();
    }
}
