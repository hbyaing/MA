package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.crafting.*;
import cn.blockforge.generated.generatedmod.fixtures.GameTestPlayer;
import cn.blockforge.generated.generatedmod.progression.PlayerCrafts;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.*;
import net.minecraftforge.gametest.*;
import java.util.*;

@GameTestHolder("wuxingxiantu_talismans") @PrefixGameTestTemplate(false)
public final class EquipmentCraftingGameTests {
    @GameTest(template="empty") public static void invalidBookActionsDoNotConvertOrReportSuccess(GameTestHelper h){try(var p=player(h)){
        var pos=h.absolutePos(BlockPos.ZERO);h.getLevel().setBlockAndUpdate(pos,WuXingBlocks.INSCRIPTION_TABLE.get().defaultBlockState());var menu=new EquipmentWorkstationMenu(9,p.getInventory(),pos,true);
        check(h,!menu.clickMenuButton(p,menu.command(103)),"Empty conversion must be rejected");
        var book=EnchantedBookItem.createForEnchantment(new EnchantmentInstance(SpiritEnchantment.SPIRIT_GUARD.get(),2));menu.slots.get(1).set(book);menu.slots.get(0).set(new ItemStack(WuXingItems.DARK_IRON_SWORD.get()));var before=book.save(new CompoundTag());
        check(h,!menu.clickMenuButton(p,menu.command(104))&&before.equals(book.save(new CompoundTag())),"Rejected application must not convert the book");
        menu.slots.get(0).set(new ItemStack(Items.DIAMOND_BOOTS));check(h,menu.clickMenuButton(p,menu.command(103)),"Valid book conversion");
        check(h,!menu.clickMenuButton(p,menu.command(103))&&EquipmentCrafting.read(book).getInt("GuardCredit")==2,"Repeated conversion must reject without changing credit");menu.removed(p);
    }h.succeed();}
    private static GameTestPlayer player(GameTestHelper h){var p=new GameTestPlayer(h.getLevel());p.join();var pos=h.absolutePos(BlockPos.ZERO);p.setPos(pos.getX()+.5,pos.getY()+1,pos.getZ()+.5);var d=GeneratedMod.data(p);d.wuxingxiantu$setRealm(8);d.wuxingxiantu$setLearnedFires(new int[]{9});d.wuxingxiantu$setEquippedFire(9);d.wuxingxiantu$setAura(1000000);var n=PlayerCrafts.get(p.server).profession(p.getUUID(),"refining");n.putBoolean("Learned",true);n.putInt("Rank",8);materials(p);return p;}
    private static void materials(GameTestPlayer p){Item[] items={WuXingItems.SPIRIT_STONE.get(),WuXingItems.DARK_IRON.get(),Items.LAPIS_LAZULI,WuXingItems.FIVE_ESSENCE.get(),WuXingItems.GLAZED_CRYSTAL.get(),WuXingItems.INSCRIPTION_CRYSTAL.get()};for(int i=0;i<items.length;i++)p.getInventory().setItem(20+i,new ItemStack(items[i],64));}
    private static void check(GameTestHelper h,boolean value,String message){h.assertTrue(value,message);}
    @GameTest(template="empty") public static void oldArmorMigratesExactlyOnceAndKeepsOtherData(GameTestHelper h){
        var s=new ItemStack(Items.DIAMOND_CHESTPLATE);s.enchant(SpiritEnchantment.SPIRIT_GUARD.get(),4);s.enchant(Enchantments.UNBREAKING,3);s.setDamageValue(42);s.getOrCreateTag().putString("ThirdParty","keep");
        check(h,EquipmentCrafting.migrate(s),"No migration");check(h,!EquipmentCrafting.migrate(s),"Migration repeated");check(h,EquipmentCrafting.guardLevel(s)==4&&EnchantmentHelper.getItemEnchantmentLevel(SpiritEnchantment.SPIRIT_GUARD.get(),s)==0,"Wrong guard conversion");check(h,EnchantmentHelper.getItemEnchantmentLevel(Enchantments.UNBREAKING,s)==3&&s.getDamageValue()==42&&s.getTag().getString("ThirdParty").equals("keep"),"Unrelated data lost");
        var restored=ItemStack.of(s.save(new CompoundTag()));check(h,EquipmentCrafting.level(restored,"guard")==4,"Migration not saved");h.succeed();
    }
    @GameTest(template="empty") public static void oldBookConversionPreservesOtherEnchantmentsAndCreditCannotReplay(GameTestHelper h){try(var p=player(h)){
        var pos=h.absolutePos(BlockPos.ZERO);h.getLevel().setBlockAndUpdate(pos,WuXingBlocks.INSCRIPTION_TABLE.get().defaultBlockState());var menu=new EquipmentWorkstationMenu(7,p.getInventory(),pos,true);
        var book=EnchantedBookItem.createForEnchantment(new EnchantmentInstance(SpiritEnchantment.SPIRIT_GUARD.get(),3));EnchantedBookItem.addEnchantment(book,new EnchantmentInstance(Enchantments.UNBREAKING,2));menu.slots.get(0).set(new ItemStack(Items.DIAMOND_BOOTS));menu.slots.get(1).set(book);
        check(h,menu.clickMenuButton(p,menu.command(103)),"Book conversion rejected");check(h,EquipmentCrafting.read(book).getInt("GuardCredit")==3&&EquipmentCrafting.legacyBookLevel(book)==0&&EnchantmentHelper.getEnchantments(book).get(Enchantments.UNBREAKING)==2,"Book data damaged");
        int command=menu.command(104);check(h,menu.clickMenuButton(p,command),"Credit apply rejected");check(h,!menu.clickMenuButton(p,command),"Stale command replayed");check(h,EquipmentCrafting.level(menu.equipment(),"guard")==3&&EquipmentCrafting.read(book).getInt("GuardCredit")==0,"Wrong credit result");menu.removed(p);
    }h.succeed();}
    @GameTest(template="empty") public static void inscriptionChargesMaterialsAndHonorsHardPity(GameTestHelper h){try(var p=player(h)){
        var s=new ItemStack(Items.DIAMOND_CHESTPLATE);float before=GeneratedMod.data(p).wuxingxiantu$getAura();EquipmentCrafting.attempt(p,s,0);
        check(h,EquipmentCrafting.level(s,"guard")==1&&p.getInventory().getItem(20).getCount()==60&&p.getInventory().getItem(22).getCount()==60&&p.getInventory().getItem(25).getCount()==63&&GeneratedMod.data(p).wuxingxiantu$getAura()==before,"Wrong first engraving transaction");
        var pity=new CompoundTag();pity.putInt("guard2",2);EquipmentCrafting.write(s).put("RunePity",pity);check(h,EquipmentCrafting.runeChance(s,"guard",0)==100,"Rune pity not applied");EquipmentCrafting.attempt(p,s,0);check(h,EquipmentCrafting.level(s,"guard")==2&&EquipmentCrafting.read(s).getCompound("RunePity").getInt("guard2")==0,"Guaranteed engraving failed");
        check(h,!EquipmentCrafting.runeGate(s,"tough").isEmpty(),"Vanilla slot cap ignored");
    }h.succeed();}
    @GameTest(template="empty") public static void missingMaterialsAndWrongRealmDoNotConsume(GameTestHelper h){try(var p=player(h)){
        var s=new ItemStack(WuXingItems.DARK_IRON_SWORD.get());p.getInventory().setItem(20,ItemStack.EMPTY);var n=s.save(new CompoundTag());float aura=GeneratedMod.data(p).wuxingxiantu$getAura();EquipmentCrafting.attempt(p,s,20);check(h,n.equals(s.save(new CompoundTag()))&&GeneratedMod.data(p).wuxingxiantu$getAura()==aura,"Missing materials mutated state");
        materials(p);EquipmentCrafting.write(s).putInt("Refine",6);GeneratedMod.data(p).wuxingxiantu$setRealm(0);int count=p.getInventory().getItem(20).getCount();EquipmentCrafting.attempt(p,s,21);check(h,EquipmentCrafting.refine(s)==6&&p.getInventory().getItem(20).getCount()==count,"Realm gate bypassed");
    }h.succeed();}
    @GameTest(template="empty") public static void riskyFailureCanDropOneButRetainsTargetPity(GameTestHelper h){try(var p=player(h)){
        var s=new ItemStack(WuXingItems.DARK_IRON_SWORD.get());EquipmentCrafting.write(s).putInt("Refine",6);long seed=0;for(;seed<10000;seed++){var r=net.minecraft.util.RandomSource.create(seed);if(r.nextInt(100)>=45&&r.nextInt(100)<30)break;}p.getRandom().setSeed(seed);
        EquipmentCrafting.attempt(p,s,21);check(h,EquipmentCrafting.refine(s)==5&&EquipmentCrafting.read(s).getCompound("RefinePity").getInt("L7")==1,"Risk failure/pity incorrect");
        EquipmentCrafting.write(s).putInt("Refine",6);var pity=EquipmentCrafting.read(s).getCompound("RefinePity");pity.putInt("L7",5);check(h,EquipmentCrafting.refineChance(s)==100,"Hard refine pity ignored");materials(p);EquipmentCrafting.attempt(p,s,22);check(h,EquipmentCrafting.refine(s)==7&&pity.getInt("L7")==0,"Guaranteed refine failed");
    }h.succeed();}
    @GameTest(template="empty") public static void reforgeCandidateSurvivesSerializationAndCannotReroll(GameTestHelper h){try(var p=player(h)){
        var s=new ItemStack(WuXingItems.DARK_IRON_SWORD.get());var n=EquipmentCrafting.write(s);n.putInt("ReforgeRare",10);n.putInt("ReforgeNatural",50);EquipmentCrafting.attempt(p,s,30);
        check(h,n.getCompound("Pending").getInt("Quality")==3&&n.getInt("ReforgeNatural")==0&&n.getInt("ReforgeRare")==0,"Natural pity priority failed");
        var copy=ItemStack.of(s.save(new CompoundTag()));int materials=p.getInventory().getItem(20).getCount();EquipmentCrafting.attempt(p,copy,30);check(h,p.getInventory().getItem(20).getCount()==materials&&EquipmentCrafting.read(copy).getCompound("Pending").equals(n.getCompound("Pending")),"Pending result rerolled");
        EquipmentCrafting.choose(p,copy,true);check(h,EquipmentCrafting.temper(copy,0)>=.98&&!EquipmentCrafting.read(copy).contains("Pending"),"Candidate acceptance failed");check(h,!EquipmentCrafting.choose(p,copy,true),"Candidate replay accepted");
        materials(p);EquipmentCrafting.attempt(p,copy,33);check(h,EquipmentCrafting.read(copy).contains("Pending"),"Second candidate missing");EquipmentCrafting.choose(p,copy,false);check(h,EquipmentCrafting.temper(copy,0)>=.98,"Reject lost original temper");
    }h.succeed();}
    @GameTest(template="empty") public static void actualDurabilityMixinAndStableAttributes(GameTestHelper h){
        var s=new ItemStack(Items.DIAMOND_CHESTPLATE);int base=s.getMaxDamage();EquipmentCrafting.write(s).putInt("Refine",9);check(h,s.getMaxDamage()==Math.round(base*1.18),"Durability hook inactive");
        int size=s.getAttributeModifiers(net.minecraft.world.entity.EquipmentSlot.CHEST).size();for(int i=0;i<100;i++)check(h,s.getAttributeModifiers(net.minecraft.world.entity.EquipmentSlot.CHEST).size()==size,"Attribute modifiers accumulated");
        var sword=new ItemStack(WuXingItems.DARK_IRON_SWORD.get());EquipmentCrafting.write(sword).putInt("Refine",9);check(h,sword.getMaxDamage()==0&&!sword.isDamageableItem(),"Indestructible sword made damageable");EquipmentCrafting.write(sword).putInt("Temper",0);EquipmentCrafting.write(sword).putInt("TemperBp",10000);check(h,Math.abs(EquipmentCrafting.upkeep(sword,100)-104)<.0001,"Indestructible sword got free reforge bonus");h.succeed();
    }
    @GameTest(template="empty") public static void menuCloseReturnsItemsAndStaleCommandsCannotRepeatWork(GameTestHelper h){try(var p=player(h)){
        var pos=h.absolutePos(BlockPos.ZERO);h.getLevel().setBlockAndUpdate(pos,WuXingBlocks.REFORGE_TABLE.get().defaultBlockState());var menu=new EquipmentWorkstationMenu(4,p.getInventory(),pos,false);menu.slots.get(0).set(new ItemStack(WuXingItems.DARK_IRON_SWORD.get()));int command=menu.command(100);check(h,menu.clickMenuButton(p,command)&&EquipmentCrafting.refine(menu.equipment())==1,"First refine failed");check(h,!menu.clickMenuButton(p,command)&&EquipmentCrafting.refine(menu.equipment())==1,"Duplicate command refined twice");menu.removed(p);check(h,p.getInventory().items.stream().filter(s->s.is(WuXingItems.DARK_IRON_SWORD.get())).mapToInt(ItemStack::getCount).sum()==1,"Menu close lost or duplicated equipment");menu.removed(p);check(h,p.getInventory().items.stream().filter(s->s.is(WuXingItems.DARK_IRON_SWORD.get())).mapToInt(ItemStack::getCount).sum()==1,"Repeated close duplicated");
    }h.succeed();}
    @GameTest(template="empty") public static void upkeepFloorAndWalkingBonusDoNotAffectAirborneMovement(GameTestHelper h){try(var p=player(h)){
        var s=new ItemStack(WuXingItems.DARK_IRON_SWORD.get());s.enchant(SpiritEnchantment.LIGHTNESS.get(),3);EquipmentCrafting.setRune(s,"control",3,2);EquipmentCrafting.write(s).putInt("Temper",2);EquipmentCrafting.write(s).putInt("TemperBp",10000);check(h,SpiritEnchantmentLogic.weaponAuraCost(100,s)==45,"Existing 45 percent cost floor bypassed");
        var boots=new ItemStack(Items.DIAMOND_BOOTS);EquipmentCrafting.setRune(boots,"light",3,2);p.setItemSlot(net.minecraft.world.entity.EquipmentSlot.FEET,boots);var attribute=p.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);double base=attribute.getValue();p.setOnGround(true);EquipmentCraftingEvents.tick(new net.minecraftforge.event.TickEvent.PlayerTickEvent(net.minecraftforge.event.TickEvent.Phase.END,p));check(h,Math.abs(attribute.getValue()-base-.0044)<.000001,"Walking bonus missing");p.setOnGround(false);EquipmentCraftingEvents.tick(new net.minecraftforge.event.TickEvent.PlayerTickEvent(net.minecraftforge.event.TickEvent.Phase.END,p));check(h,Math.abs(attribute.getValue()-base)<.000001,"Walking bonus affected flight");
    }h.succeed();}
    @GameTest(template="empty") public static void fullGuardQualityDoesNotIncreaseCostBeyondReductionCap(GameTestHelper h){try(var p=player(h)){
        Item[] armor={Items.DIAMOND_HELMET,Items.DIAMOND_CHESTPLATE,Items.DIAMOND_LEGGINGS,Items.DIAMOND_BOOTS};net.minecraft.world.entity.EquipmentSlot[] slots={net.minecraft.world.entity.EquipmentSlot.HEAD,net.minecraft.world.entity.EquipmentSlot.CHEST,net.minecraft.world.entity.EquipmentSlot.LEGS,net.minecraft.world.entity.EquipmentSlot.FEET};for(int i=0;i<4;i++){var s=new ItemStack(armor[i]);EquipmentCrafting.setRune(s,"guard",4,2);p.setItemSlot(slots[i],s);}var d=GeneratedMod.data(p);d.wuxingxiantu$setAura(10000);var event=new net.minecraftforge.event.entity.living.LivingDamageEvent(p,p.damageSources().playerAttack(p),100);SpiritEnchantmentLogic.applySpiritGuard(event);check(h,Math.abs(event.getAmount()-20)<.0001&&Math.abs(d.wuxingxiantu$getAura()-6800)<.001,"Guard cap/cost changed");
    }h.succeed();}
    @GameTest(template="empty") public static void swordUpgradeKeepsCraftingProgressWithoutSharingMutableTags(GameTestHelper h){
        var source=new ItemStack(WuXingItems.PURE_YANG_SWORD.get());var n=EquipmentCrafting.write(source);n.putInt("Refine",7);n.putUUID("Id",UUID.randomUUID());var pity=new CompoundTag();pity.putInt("L8",3);n.put("RefinePity",pity);EquipmentCrafting.setRune(source,"control",3,1);var result=EquipmentCrafting.inherit(new ItemStack(WuXingItems.XUANYUAN_SWORD.get()),List.of(source,new ItemStack(Items.NETHER_STAR)));
        check(h,EquipmentCrafting.read(result).equals(n)&&EquipmentCrafting.level(result,"control")==3,"Sword upgrade lost crafting progress");EquipmentCrafting.write(result).putInt("Refine",8);check(h,EquipmentCrafting.refine(source)==7,"Upgrade aliased input NBT");h.succeed();
    }
}
