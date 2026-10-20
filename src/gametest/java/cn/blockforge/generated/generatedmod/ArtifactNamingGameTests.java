package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.artifacts.*;
import cn.blockforge.generated.generatedmod.crafting.*;
import cn.blockforge.generated.generatedmod.fixtures.GameTestPlayer;
import cn.blockforge.generated.generatedmod.items.HeartSwordItem;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.*;

@GameTestHolder("wuxingxiantu_entry") @PrefixGameTestTemplate(false)
public final class ArtifactNamingGameTests {
    private static AnvilMenu menu(GameTestHelper h,GameTestPlayer player){
        var pos=h.absolutePos(new BlockPos(1,1,1));h.getLevel().setBlockAndUpdate(pos,Blocks.ANVIL.defaultBlockState());
        player.setPos(pos.getX()+.5,pos.getY()+1,pos.getZ()+.5);player.giveExperienceLevels(70-player.experienceLevel);
        var menu=new AnvilMenu(73,player.getInventory(),ContainerLevelAccess.create(h.getLevel(),pos));player.containerMenu=menu;return menu;
    }
    private static ItemStack artifact(ArtifactKind kind,UUID owner){
        var result=new ItemStack(ArtifactBootstrap.ARTIFACTS.get(kind).get());
        if(owner!=null)result.getOrCreateTag().putUUID(HeartSwordItem.OWNER_TAG,owner);
        var craft=EquipmentCrafting.write(result);craft.putInt("Refine",4);craft.putInt("BondNurture",65);craft.putInt("ArtifactQuenchConduct",3);
        craft.putInt("ArtifactMaterial",2);craft.putInt("ForgeFinishQuality",2);result.getOrCreateTag().putString("ThirdParty","preserve");
        return result;
    }
    @GameTest(template="empty") public static void vanillaAnvilRenameKeepsAllSixArtifactOwnersAndGrowth(GameTestHelper h){
        try(var player=new GameTestPlayer(h.getLevel())){
            player.join();String name="Myriad Ascension - ".repeat(2);
            h.assertTrue(name.length()>32&&name.length()<=AnvilMenu.MAX_NAME_LENGTH,"Fixture must exceed old dedicated naming limit");
            for(var kind:ArtifactKind.values()){
                var menu=menu(h,player);var source=artifact(kind,player.getUUID());source.setHoverName(Component.literal("Original custom name"));
                var growth=EquipmentCrafting.read(source).copy();menu.getSlot(0).set(source);menu.setItemName(name);
                var preview=menu.getSlot(2).getItem();
                h.assertTrue(!preview.isEmpty()&&preview.getHoverName().getString().equals(name)&&player.getUUID().equals(EquipmentBond.owner(preview))
                        &&EquipmentCrafting.read(preview).equals(growth),"Vanilla preview lost name, owner or growth for "+kind);
                int levels=player.experienceLevel,cost=menu.getCost();menu.clicked(2,0,ClickType.PICKUP,player);
                var claimed=menu.getCarried();
                h.assertTrue(!claimed.isEmpty()&&claimed.getCount()==1&&claimed.getHoverName().getString().equals(name)
                        &&player.getUUID().equals(EquipmentBond.owner(claimed))&&EquipmentCrafting.read(claimed).equals(growth)
                        &&claimed.getTag().getString("ThirdParty").equals("preserve")&&player.experienceLevel==levels-cost,"Actual anvil pickup lost data or vanilla payment for "+kind);
                var restored=ItemStack.of(claimed.save(new CompoundTag()));
                h.assertTrue(restored.getHoverName().getString().equals(name)&&player.getUUID().equals(EquipmentBond.owner(restored))
                        &&EquipmentCrafting.read(restored).equals(growth),"Named artifact did not persist for "+kind);
                menu.setCarried(ItemStack.EMPTY);menu.removed(player);
            }
        }h.succeed();
    }
    @GameTest(template="empty") public static void differentOwnersCannotMergeAndBoundSacrificeCannotBecomeUnbound(GameTestHelper h){
        try(var player=new GameTestPlayer(h.getLevel())){
            player.join();
            for(boolean boundLeft:new boolean[]{true,false}){
                var menu=menu(h,player);var left=artifact(ArtifactKind.SWORD,boundLeft?player.getUUID():null);
                var right=artifact(ArtifactKind.SWORD,boundLeft?UUID.randomUUID():player.getUUID());
                left.setDamageValue(300);right.setDamageValue(500);
                var beforeLeft=left.save(new CompoundTag());var beforeRight=right.save(new CompoundTag());int levels=player.experienceLevel;
                menu.getSlot(0).set(left);menu.getSlot(1).set(right);menu.setItemName("Merged artifact");
                h.assertTrue(menu.getSlot(2).getItem().isEmpty(),"Anvil owner washing produced a result: boundLeft="+boundLeft);
                menu.clicked(2,0,ClickType.PICKUP,player);menu.clicked(2,0,ClickType.QUICK_MOVE,player);
                h.assertTrue(menu.getCarried().isEmpty()&&beforeLeft.equals(menu.getSlot(0).getItem().save(new CompoundTag()))
                        &&beforeRight.equals(menu.getSlot(1).getItem().save(new CompoundTag()))&&player.experienceLevel==levels,"Denied ownership merge consumed inputs or experience");
                menu.removed(player);
            }
            var menu=menu(h,player);var foreign=artifact(ArtifactKind.HELMET,UUID.randomUUID());menu.getSlot(0).set(foreign);menu.setItemName("Stolen name");
            h.assertTrue(menu.getSlot(2).getItem().isEmpty(),"Foreign artifact could be renamed by another player");menu.removed(player);
        }h.succeed();
    }
}
