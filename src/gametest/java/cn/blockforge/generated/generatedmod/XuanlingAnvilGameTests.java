package cn.blockforge.generated.generatedmod;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import java.util.UUID;

@GameTestHolder("wuxingxiantu_anvil")
@PrefixGameTestTemplate(false)
public final class XuanlingAnvilGameTests {
    @GameTest(template="empty")public static void separateForgeModeRestoresTemplate(GameTestHelper h){
        var pos=h.absolutePos(new BlockPos(1,1,1));h.getLevel().setBlock(pos,WuXingBlocks.XUANLING_ANVIL.get().defaultBlockState(),3);
        var p=player(h,"TemplateModes",pos);var access=ContainerLevelAccess.create(h.getLevel(),pos);
        var empty=new XuanlingAnvilMenu(6,p.getInventory(),access);h.assertTrue(!empty.clickMenuButton(p,1),"Empty library entered forge mode");
        var memory=AnvilTemplateMemory.get(h.getLevel());memory.learn(pos,new ItemStack(Items.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE));memory.learn(pos,new ItemStack(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE));
        var menu=new XuanlingAnvilMenu(7,p.getInventory(),access);h.assertTrue(menu.clickMenuButton(p,3)&&menu.selectedTemplate().stack().is(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE),"Second template was not selected");
        h.assertTrue(menu.clickMenuButton(p,0)&&menu.selectedTemplate().stack().isEmpty(),"Repair mode retained smithing");
        h.assertTrue(menu.clickMenuButton(p,1)&&menu.selectedTemplate().stack().is(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE),"Forge did not restore selected template");
        menu.clickMenuButton(p,1);h.assertTrue(menu.selectedTemplate().stack().is(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE),"Forge mode cycled templates");h.succeed();
    }
    @GameTest(template="empty")public static void templateHandsAndWidenedLedges(GameTestHelper h){
        var pos=h.absolutePos(new BlockPos(1,1,1));var block=(XuanlingAnvilBlock)WuXingBlocks.XUANLING_ANVIL.get();h.getLevel().setBlock(pos,block.defaultBlockState(),3);
        var p=player(h,"TemplateBothHands",pos);var memory=AnvilTemplateMemory.get(h.getLevel());
        var hit=new BlockHitResult(Vec3.atCenterOf(pos),Direction.UP,pos,false);
        // A real vanilla click starts with MAIN_HAND even when the template is
        // in OFF_HAND; the old callback never reached the offhand in this case.
        p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.IRON_SWORD));
        p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE));
        var mainAttempt=new net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock(p,InteractionHand.MAIN_HAND,pos,hit);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(mainAttempt);
        h.assertTrue(mainAttempt.isCanceled()&&memory.at(pos).size()==1,"Main-hand attempt opened a menu before registering offhand template");
        h.assertTrue(p.getOffhandItem().isEmpty()&&p.getMainHandItem().is(Items.IRON_SWORD),"Absorption did not consume exactly the template hand");
        p.setShiftKeyDown(true);p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE));
        var sneakAttempt=new net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock(p,InteractionHand.MAIN_HAND,pos,hit);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(sneakAttempt);
        h.assertTrue(sneakAttempt.isCanceled()&&memory.at(pos).size()==2,"Sneaking main-hand template not recorded");
        h.assertTrue(AnvilTemplateMemory.load(memory.save(new net.minecraft.nbt.CompoundTag())).at(pos).size()==2,"Both templates did not persist");
        h.assertTrue(!memory.learn(pos,new ItemStack(Items.DIRT)),"Non-template entered persistent template library");
        for(var direction:new Direction[]{Direction.NORTH,Direction.EAST,Direction.SOUTH,Direction.WEST}){
            var state=block.defaultBlockState().setValue(XuanlingAnvilBlock.FACING,direction);
            var shape=block.getShape(state,h.getLevel(),pos,net.minecraft.world.phys.shapes.CollisionContext.empty());
            double x=direction.getAxis()==Direction.Axis.Z?1.65:8,z=direction.getAxis()==Direction.Axis.Z?8:1.65;
            var start=new Vec3(pos.getX()+x/16,pos.getY()+.5,pos.getZ()+z/16);
            var end=new Vec3(start.x,pos.getY()+.01,start.z);var ledge=shape.clip(start,end,pos);
            h.assertTrue(ledge!=null&&ledge.getLocation().y>pos.getY()+3.6/16,"Visible widened ledge clicked through in "+direction);
        }
        h.succeed();
    }
    @GameTest(template="empty")public static void realTemplateRightClickAndDisabledLedger(GameTestHelper h){
        var pos=h.absolutePos(new BlockPos(1,1,1));var block=(XuanlingAnvilBlock)WuXingBlocks.XUANLING_ANVIL.get();h.getLevel().setBlock(pos,block.defaultBlockState(),3);var p=player(h,"TemplateClick",pos);
        p.setShiftKeyDown(true);p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE));
        var hit=new BlockHitResult(Vec3.atCenterOf(pos),Direction.UP,pos,false);
        var event=new net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock(p,InteractionHand.OFF_HAND,pos,hit);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event);
        h.assertTrue(event.isCanceled()&&AnvilTemplateMemory.get(h.getLevel()).at(pos).size()==1,"Sneak/offhand template event did not register");
        h.assertTrue(p.getOffhandItem().isEmpty(),"Absorption did not consume the template");
        var d=(CultivationDataImpl)GeneratedMod.data(p);d.setManaLossEnabled(false);long version=d.manaLossVersion();int size=d.manaLossRows().size();d.setCurrentMana(d.getCurrentMana()-20);
        h.assertTrue(d.manaLossVersion()==version&&d.manaLossRows().size()==size&&d.getCurrentMana()==980,"Disabled recorder still ran or mana was not spent");
        var copy=new CultivationDataImpl();copy.deserializeNBT(d.serializeNBT());h.assertTrue(!copy.manaLossEnabled(),"Tracking switch did not persist");d.setManaLossEnabled(true);d.setCurrentMana(970);h.assertTrue(d.manaLossRows().size()==size+1,"Tracking did not restart");
        var menu=new XuanlingAnvilMenu(4,p.getInventory(),ContainerLevelAccess.create(h.getLevel(),pos));h.assertTrue(menu.clickMenuButton(p,2)&&!menu.clickMenuButton(p,65),"Direct template index not validated");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=500) public static void weeklyWildGrassPopulation(GameTestHelper h){
        var end=h.getLevel().getServer().getLevel(net.minecraft.world.level.Level.END);
        h.assertTrue(end!=null,"End dimension missing");var cycle=new cn.blockforge.generated.generatedmod.plants.DragonGrassCycle();
        h.assertTrue(cycle.refresh(end,0),"Initial cycle did not run");var initial=cycle.save(new net.minecraft.nbt.CompoundTag());
        h.assertTrue(initial.getLongArray("Positions").length==3&&initial.getLong("Next")==168000,"Wild population or interval incorrect");
        for(long encoded:initial.getLongArray("Positions")){var p=BlockPos.of(encoded);h.assertTrue(cn.blockforge.generated.generatedmod.plants.DragonGrassCycle.mainIsland(p)&&end.getBlockState(p).is(cn.blockforge.generated.generatedmod.plants.SpiritPlantRegistry.DRAGON_SCALE_GRASS.get()),"Wild grass outside main island or not placed");}
        h.assertTrue(!cycle.refresh(end,167999),"Wild plants regenerated early");
        h.assertTrue(cycle.refresh(end,168000),"Seven-day refresh did not run");var next=cycle.save(new net.minecraft.nbt.CompoundTag());
        h.assertTrue(next.getLongArray("Positions").length==3&&next.getLong("Next")==336000,"Second population/interval incorrect");h.succeed();
    }
    @GameTest(template="empty") public static void reusableSmithingLibrary(GameTestHelper h){
        var pos=h.absolutePos(new BlockPos(1,1,1));h.getLevel().setBlock(pos,WuXingBlocks.XUANLING_ANVIL.get().defaultBlockState(),3);
        var p=player(h,"TemplateReuse",pos);var template=new ItemStack(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE);
        var memory=AnvilTemplateMemory.get(h.getLevel());h.assertTrue(memory.learn(pos,template),"First template not recorded");
        h.assertTrue(!memory.learn(pos,template),"Duplicate recorded");
        h.assertTrue(AnvilTemplateMemory.load(memory.save(new net.minecraft.nbt.CompoundTag())).at(pos).size()==1,"Template persistence lost");
        var menu=new XuanlingAnvilMenu(4,p.getInventory(),ContainerLevelAccess.create(h.getLevel(),pos));
        menu.clickMenuButton(p,1);menu.getSlot(0).set(new ItemStack(Items.DIAMOND_SWORD));menu.getSlot(1).set(new ItemStack(Items.NETHERITE_INGOT,2));menu.createResult();
        h.assertTrue(menu.getSlot(2).getItem().is(Items.NETHERITE_SWORD),"Template smithing did not assemble");
        var output=menu.getSlot(2).remove(1);menu.getSlot(2).onTake(p,output);
        h.assertTrue(menu.getSlot(1).getItem().getCount()==1,"Smithing did not consume exactly one addition");
        h.assertTrue(memory.at(pos).size()==1,"Reusable template consumed");
        h.assertTrue(GeneratedMod.data(p).getCurrentMana()==980,"Smithing fee mismatch");h.succeed();
    }
    @GameTest(template="empty") public static void boundedActualManaLedger(GameTestHelper h){
        var pos=h.absolutePos(new BlockPos(1,1,1));var p=player(h,"ManaLedger",pos);var d=(CultivationDataImpl)GeneratedMod.data(p);
        for(int i=0;i<40;i++)d.setCurrentMana(d.getCurrentMana()-1);
        h.assertTrue(d.manaLossRows().size()==32,"Ledger unbounded or missing debits");
        d.setCurrentMana(d.getCurrentMana()+10);h.assertTrue(d.manaLossRows().size()==32,"Recovery recorded as loss");
        var restored=new CultivationDataImpl();restored.deserializeNBT(d.serializeNBT());h.assertTrue(restored.manaLossRows().equals(d.manaLossRows()),"Ledger persistence mismatch");h.succeed();
    }
    @GameTest(template="empty") public static void dragonMainIslandBoundary(GameTestHelper h){
        h.assertTrue(cn.blockforge.generated.generatedmod.plants.DragonGrassCycle.mainIsland(new BlockPos(96,64,0)),"Main island excluded");
        h.assertTrue(!cn.blockforge.generated.generatedmod.plants.DragonGrassCycle.mainIsland(new BlockPos(1000,64,0)),"Outer island included");
        var data=new net.minecraft.nbt.CompoundTag();data.putLong("Next",168000);data.putLongArray("Positions",new long[]{new BlockPos(3,64,4).asLong()});
        var restored=cn.blockforge.generated.generatedmod.plants.DragonGrassCycle.load(data).save(new net.minecraft.nbt.CompoundTag());
        h.assertTrue(restored.getLong("Next")==168000&&java.util.Arrays.equals(restored.getLongArray("Positions"),data.getLongArray("Positions")),"Cycle did not persist next spawn and positions");h.succeed();
    }
    private static FakePlayer player(GameTestHelper h, String name, BlockPos pos) {
        FakePlayer p = new FakePlayer(h.getLevel(), new GameProfile(UUID.nameUUIDFromBytes(name.getBytes()),name));
        p.setPos(pos.getX()+.5,pos.getY()+1,pos.getZ()+.5);
        GeneratedMod.data(p).wuxingxiantu$setRealm(2);
        GeneratedMod.data(p).setMaxMana(1000);
        GeneratedMod.data(p).setCurrentMana(1000);
        return p;
    }
    @GameTest(template="empty")
    public static void renameSpendsManaAndNeverExperience(GameTestHelper h) {
        BlockPos pos=h.absolutePos(new BlockPos(1,1,1));
        h.getLevel().setBlock(pos,WuXingBlocks.XUANLING_ANVIL.get().defaultBlockState(),3);
        FakePlayer p=player(h,"AnvilRename",pos);
        p.experienceLevel=0;
        var menu=new XuanlingAnvilMenu(1,p.getInventory(),ContainerLevelAccess.create(h.getLevel(),pos));
        menu.getSlot(0).set(new ItemStack(Items.DIAMOND_SWORD));
        menu.setItemName("玄灵测试");
        h.assertTrue(menu.getCost()==1 && menu.manaCost()==20,"Rename quote must be 20 mana");
        GeneratedMod.data(p).setCurrentMana(19);
        menu.clicked(2,0,ClickType.PICKUP,p);
        h.assertTrue(menu.getCarried().isEmpty() && !menu.getSlot(0).getItem().isEmpty(),"Insufficient mana yielded an item");
        menu.quickMoveStack(p,2);
        h.assertTrue(!menu.getSlot(0).getItem().isEmpty() && GeneratedMod.data(p).getCurrentMana()==19,"Shift pickup bypassed payment");
        GeneratedMod.data(p).setCurrentMana(20);
        menu.clicked(2,0,ClickType.PICKUP,p);
        h.assertTrue(menu.getCarried().getHoverName().getString().equals("玄灵测试"),"Rename output lost");
        h.assertTrue(GeneratedMod.data(p).getCurrentMana()==0 && p.experienceLevel==0,"Wrong resource debited");
        h.assertTrue(menu.getSlot(0).getItem().isEmpty(),"Consumed input retained");
        h.succeed();
    }
    @GameTest(template="empty")
    public static void materialRepairKeepsUnusedMaterials(GameTestHelper h) {
        BlockPos pos=h.absolutePos(new BlockPos(1,1,1));
        h.getLevel().setBlock(pos,WuXingBlocks.XUANLING_ANVIL.get().defaultBlockState(),3);
        FakePlayer p=player(h,"AnvilMaterial",pos);
        var menu=new XuanlingAnvilMenu(1,p.getInventory(),ContainerLevelAccess.create(h.getLevel(),pos));
        ItemStack sword=new ItemStack(Items.DIAMOND_SWORD);sword.setDamageValue(100);
        menu.getSlot(0).set(sword);menu.getSlot(1).set(new ItemStack(Items.DIAMOND,3));
        h.assertTrue(menu.getSlot(2).getItem().getDamageValue()==0,"Vanilla repair result missing");
        int cost=menu.manaCost();
        menu.clicked(2,0,ClickType.QUICK_MOVE,p);
        h.assertTrue(menu.getSlot(1).getItem().getCount()==2,"Unused repair materials lost");
        h.assertTrue(GeneratedMod.data(p).getCurrentMana()==1000-cost,"Shift pickup payment incorrect");
        h.assertTrue(p.experienceLevel==0,"Repair charged XP");
        h.succeed();
    }
    @GameTest(template="empty")
    public static void enchantmentsAndVanillaLimitRemain(GameTestHelper h) {
        BlockPos pos=h.absolutePos(new BlockPos(1,1,1));
        h.getLevel().setBlock(pos,WuXingBlocks.XUANLING_ANVIL.get().defaultBlockState(),3);
        FakePlayer p=player(h,"AnvilEnchant",pos);
        var menu=new XuanlingAnvilMenu(1,p.getInventory(),ContainerLevelAccess.create(h.getLevel(),pos));
        ItemStack sword=new ItemStack(Items.DIAMOND_SWORD);sword.enchant(Enchantments.SHARPNESS,1);
        ItemStack sacrifice=sword.copy();
        menu.getSlot(0).set(sword);menu.getSlot(1).set(sacrifice);
        h.assertTrue(net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(
                Enchantments.SHARPNESS,menu.getSlot(2).getItem())==2,"Equal enchantments did not upgrade");
        sword.setRepairCost(40);menu.getSlot(0).set(sword);
        h.assertTrue(menu.getSlot(2).getItem().isEmpty(),"Vanilla 40-level limit bypassed");
        h.succeed();
    }
    @GameTest(template="empty")
    public static void exhaustedAnvilRestorationAndLootPreserveWear(GameTestHelper h) {
        BlockPos pos=h.absolutePos(new BlockPos(1,1,1));
        var block=(XuanlingAnvilBlock)WuXingBlocks.XUANLING_ANVIL.get();
        var state=block.defaultBlockState().setValue(XuanlingAnvilBlock.DAMAGE,3);
        h.getLevel().setBlock(pos,state,3);
        FakePlayer p=player(h,"AnvilRestore",pos);p.setShiftKeyDown(true);
        var hit=new BlockHitResult(Vec3.atCenterOf(pos),Direction.UP,pos,false);
        GeneratedMod.data(p).setCurrentMana(149);
        block.use(state,h.getLevel(),pos,p,InteractionHand.MAIN_HAND,hit);
        h.assertTrue(h.getLevel().getBlockState(pos).getValue(XuanlingAnvilBlock.DAMAGE)==3,"Unaffordable restoration succeeded");
        GeneratedMod.data(p).setCurrentMana(150);
        block.use(state,h.getLevel(),pos,p,InteractionHand.MAIN_HAND,hit);
        var restored=h.getLevel().getBlockState(pos);
        h.assertTrue(restored.getValue(XuanlingAnvilBlock.DAMAGE)==2 && GeneratedMod.data(p).getCurrentMana()==0,"Restoration incorrect");
        var drops=net.minecraft.world.level.block.Block.getDrops(restored,h.getLevel(),pos,null,p,new ItemStack(Items.DIAMOND_PICKAXE));
        h.assertTrue(drops.size()==1 && drops.get(0).getOrCreateTag().getCompound("BlockStateTag").getString("damage").equals("2"),"Mining reset saved wear");
        h.assertTrue(p.experienceLevel==0,"Restoration charged XP");
        h.succeed();
    }
}
