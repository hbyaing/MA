package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.crafting.*;
import cn.blockforge.generated.generatedmod.fixtures.GameTestPlayer;
import cn.blockforge.generated.generatedmod.items.HeartSwordItem;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.*;
import java.util.UUID;

@GameTestHolder("wuxingxiantu_entry") @PrefixGameTestTemplate(false)
public final class BoundEquipmentCraftingGameTests {
    private static CraftingMenu table(GameTestHelper h, GameTestPlayer player) {
        var pos=h.absolutePos(new BlockPos(1,1,1));h.getLevel().setBlockAndUpdate(pos,Blocks.CRAFTING_TABLE.defaultBlockState());
        player.setPos(pos.getX()+.5,pos.getY()+1,pos.getZ()+.5);
        var menu=new CraftingMenu(31,player.getInventory(),ContainerLevelAccess.create(h.getLevel(),pos));
        player.containerMenu=menu;return menu;
    }
    private static void mingjue(CraftingMenu menu, ItemStack sword) {
        for(int i=0;i<9;i++)menu.getSlot(i+1).set(i==4?sword:new ItemStack(i==1||i==7?WuXingItems.AURA_CRYSTAL.get():Items.IRON_INGOT));
    }
    private static void darkIron(CraftingMenu menu, ItemStack peach,ItemStack iron) {
        for(int i=0;i<9;i++)menu.getSlot(i+1).set(i==1?peach:i==4?new ItemStack(WuXingItems.AURA_CRYSTAL.get()):i==7?iron:new ItemStack(WuXingItems.DARK_IRON.get()));
    }
    private static CompoundTag inputs(CraftingMenu menu) {
        var snapshot=new CompoundTag();for(int i=1;i<=9;i++)snapshot.put("S"+i,menu.getSlot(i).getItem().save(new CompoundTag()));return snapshot;
    }

    @GameTest(template="empty") public static void actualTableUpgradePreservesOwnerBeforeAndAfterQuickPickup(GameTestHelper h) {
        try(var p=new GameTestPlayer(h.getLevel())) {
            p.join();var menu=table(h,p);var sword=new ItemStack(WuXingItems.DARK_IRON_SWORD.get());
            sword.getOrCreateTag().putUUID(HeartSwordItem.OWNER_TAG,p.getUUID());EquipmentCrafting.write(sword).putInt("Refine",4);
            mingjue(menu,sword);
            var preview=menu.getSlot(0).getItem();
            h.assertTrue(preview.is(WuXingItems.AZURE_STEEL_SWORD.get()) && p.getUUID().equals(EquipmentBond.owner(preview))
                    && EquipmentCrafting.refine(preview)==4,"Actual crafting preview lost owner or equipment growth");
            menu.clicked(0,0,ClickType.QUICK_MOVE,p);
            h.assertTrue(p.getInventory().items.stream().filter(s->s.is(WuXingItems.AZURE_STEEL_SWORD.get())).count()==1,"Quick pickup lost or duplicated upgrade");
            var result=p.getInventory().items.stream().filter(s->s.is(WuXingItems.AZURE_STEEL_SWORD.get())).findFirst().orElseThrow();
            h.assertTrue(p.getUUID().equals(EquipmentBond.owner(result)) && EquipmentCrafting.refine(result)==4,"Quick pickup washed ownership");
            for(int i=1;i<=9;i++)h.assertTrue(menu.getSlot(i).getItem().isEmpty(),"Recipe did not consume exactly one set");
            mingjue(menu,new ItemStack(WuXingItems.DARK_IRON_SWORD.get()));
            h.assertTrue(menu.getSlot(0).getItem().is(WuXingItems.AZURE_STEEL_SWORD.get()) && EquipmentBond.owner(menu.getSlot(0).getItem())==null,"Unbound recipe behavior changed");
            menu.removed(p);
        }h.succeed();
    }

    @GameTest(template="empty") public static void foreignAndMixedOwnersAreRejectedBeforeAnyTableIngredientConsumption(GameTestHelper h) {
        try(var p=new GameTestPlayer(h.getLevel())) {
            p.join();var menu=table(h,p);var foreign=new ItemStack(WuXingItems.DARK_IRON_SWORD.get());
            foreign.getOrCreateTag().putUUID(HeartSwordItem.OWNER_TAG,UUID.randomUUID());mingjue(menu,foreign);
            var before=inputs(menu);h.assertTrue(menu.getSlot(0).getItem().isEmpty(),"Foreign owner produced a takeable result");
            menu.clicked(0,0,ClickType.PICKUP,p);menu.clicked(0,0,ClickType.QUICK_MOVE,p);
            h.assertTrue(before.equals(inputs(menu)) && menu.getCarried().isEmpty(),"Denied pickup consumed ingredients");
            var own=new ItemStack(WuXingItems.PEACH_SWORD.get());own.getOrCreateTag().putUUID(HeartSwordItem.OWNER_TAG,p.getUUID());
            var other=new ItemStack(Items.IRON_SWORD);other.getOrCreateTag().putUUID(HeartSwordItem.OWNER_TAG,UUID.randomUUID());darkIron(menu,own,other);
            before=inputs(menu);h.assertTrue(menu.getSlot(0).getItem().isEmpty(),"Mixed owners merged into one unbound sword");
            // Even a stale result cannot be claimed through normal or direct quick-move entry points.
            menu.getSlot(0).set(new ItemStack(WuXingItems.DARK_IRON_SWORD.get()));
            h.assertTrue(!menu.getSlot(0).mayPickup(p) && menu.quickMoveStack(p,0).isEmpty(),"Stale preview bypassed owner gate");
            menu.clicked(0,0,ClickType.PICKUP,p);menu.clicked(0,0,ClickType.QUICK_MOVE,p);
            h.assertTrue(before.equals(inputs(menu)) && menu.getCarried().isEmpty(),"Mixed-owner denial consumed materials");
            menu.getSlot(0).set(ItemStack.EMPTY);menu.removed(p);
        }h.succeed();
    }
}
