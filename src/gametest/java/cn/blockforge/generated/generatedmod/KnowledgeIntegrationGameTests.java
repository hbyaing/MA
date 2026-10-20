package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.alchemy.FormulaKnowledge;
import cn.blockforge.generated.generatedmod.alchemy.PillCatalog;
import cn.blockforge.generated.generatedmod.knowledge.*;
import cn.blockforge.generated.generatedmod.progression.PlayerCrafts;
import com.mojang.authlib.GameProfile;
import java.util.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.gametest.*;

@GameTestHolder("wuxingxiantu_entry")
@PrefixGameTestTemplate(false)
public final class KnowledgeIntegrationGameTests {
    @GameTest(template="empty")
    public static void inheritanceIsFinitePersistentAndCannotPromoteWithoutWorks(GameTestHelper h) {
        var player=FakePlayerFactory.get(h.getLevel(),new GameProfile(UUID.randomUUID(),"InheritanceQA"));
        var crafts=new PlayerCrafts();
        h.assertTrue(crafts.studyAlchemyInheritance(player,0)==0,"Unlearned profession accepted inheritance");
        crafts.learnOrPromote(player,"alchemy");
        h.assertTrue(crafts.studyAlchemyInheritance(player,0)==50,"First rank did not cap at half its XP span");
        for(int tier=0;tier<4;tier++)h.assertTrue(crafts.studyAlchemyInheritance(player,tier)==0,"A different tier bypassed the shared allowance");
        var saved=PlayerCrafts.load(crafts.save(new CompoundTag()));
        h.assertTrue(saved.studyAlchemyInheritance(player,3)==0,"Reload reset the allowance");
        var row=saved.profession(player.getUUID(),"alchemy");
        h.assertTrue(row.getInt("Rank")==0&&row.getInt("BestTier")==0&&row.getCompound("Works").isEmpty(),"Inheritance manufactured progression evidence");
        row.putInt("Xp",PlayerCrafts.XP[1]);
        saved.learnOrPromote(player,"alchemy");
        h.assertTrue(row.getInt("Rank")==0,"XP bypassed representative works");
        var works=new CompoundTag();works.putInt("wuxingxiantu:aura_pill/0",1);works.putInt("wuxingxiantu:bigu_pill/0",1);row.put("Works",works);
        saved.learnOrPromote(player,"alchemy");
        h.assertTrue(row.getInt("Rank")==1,"Ordinary promotion was not available after meeting the real conditions");
        h.assertTrue(saved.studyAlchemyInheritance(player,1)==100,"The new rank did not receive its own finite allowance");
        row.putInt("Rank",8);
        h.assertTrue(saved.studyAlchemyInheritance(player,3)==0,"Master rank consumed a useless inheritance");
        h.succeed();
    }

    @GameTest(template="empty")
    public static void realScrollConsumesOnlyWhenItGrantsExperience(GameTestHelper h) {
        var player=FakePlayerFactory.get(h.getLevel(),new GameProfile(UUID.randomUUID(),"ScrollUseQA"));
        var stack=new ItemStack(KnowledgeItems.SCROLLS.get(3).get(),2);
        player.setItemInHand(InteractionHand.MAIN_HAND,stack);
        stack.getItem().use(h.getLevel(),player,InteractionHand.MAIN_HAND);
        h.assertTrue(stack.getCount()==2,"Blocked scroll was consumed");
        PlayerCrafts.get(player.server).learnOrPromote(player,"alchemy");
        stack.getItem().use(h.getLevel(),player,InteractionHand.MAIN_HAND);
        h.assertTrue(stack.getCount()==1,"Successful study did not consume exactly one scroll");
        stack.getItem().use(h.getLevel(),player,InteractionHand.MAIN_HAND);
        h.assertTrue(stack.getCount()==1,"Exhausted rank allowance consumed another scroll");
        h.succeed();
    }

    @GameTest(template="empty")
    public static void transferredCompendiumRefreshesKnowledgeBeforeNativeOpen(GameTestHelper h) {
        var a=FakePlayerFactory.get(h.getLevel(),new GameProfile(UUID.randomUUID(),"ScholarA"));
        var b=FakePlayerFactory.get(h.getLevel(),new GameProfile(UUID.randomUUID(),"ScholarB"));
        FormulaKnowledge.unlock(a);
        FormulaKnowledge.learn(a,ResourceLocation.fromNamespaceAndPath(GeneratedMod.MOD_ID,PillCatalog.AURA.id),false);
        GeneratedMod.data(a).wuxingxiantu$learnArt("art_sword_aura");
        var token=new ItemStack(KnowledgeItems.MYRIAD_COMPENDIUM.get());
        a.setItemInHand(InteractionHand.MAIN_HAND,token);
        token.getItem().use(h.getLevel(),a,InteractionHand.MAIN_HAND);
        ItemStack book=a.getMainHandItem();
        h.assertTrue(MyriadCompendiumItem.isCompendium(book),"Craftable entry did not become a readable native book");
        String learned=book.getTag().getList("pages",Tag.TAG_STRING).toString();
        h.assertTrue(learned.contains("art.wuxingxiantu.function.art_sword_aura")&&learned.contains("knowledge.wuxingxiantu.formula_gates"),"Learned knowledge was not shown");
        b.setItemInHand(InteractionHand.MAIN_HAND,book.copy());
        var event=new PlayerInteractEvent.RightClickItem(b,InteractionHand.MAIN_HAND);
        MyriadCompendiumItem.read(event);
        var refreshed=b.getMainHandItem();
        String locked=refreshed.getTag().getList("pages",Tag.TAG_STRING).toString();
        h.assertTrue(event.isCanceled(),"Native use escaped the authoritative read hook");
        h.assertTrue(!locked.contains("art.wuxingxiantu.function.art_sword_aura")&&!locked.contains("knowledge.wuxingxiantu.formula_gates"),"Former reader's unlocked body leaked");
        h.assertTrue(refreshed.getTag().getUUID(CompendiumSnapshot.READER).equals(b.getUUID()),"Wrong reader snapshot");
        h.assertTrue(!FormulaKnowledge.unlocked(b)&&!GeneratedMod.data(b).wuxingxiantu$hasLearnedArt("art_sword_aura"),"Reading granted knowledge");
        var pages=refreshed.getTag().getList("pages",Tag.TAG_STRING);
        h.assertTrue(pages.size()<=CompendiumSnapshot.MAX_PAGES&&locked.length()<CompendiumSnapshot.MAX_TOTAL_JSON+1000,"Unbounded book NBT");
        for(String section:List.of("herbs","alchemy","refining","arts","treasures","swords"))
            h.assertTrue(locked.contains("knowledge.wuxingxiantu.section."+section),"Missing section "+section);
        CompendiumSnapshot.clear(refreshed);
        h.assertTrue(!refreshed.getTag().contains(CompendiumSnapshot.READER)&&refreshed.getTag().getList("pages",Tag.TAG_STRING).size()==1,"Dropped snapshot was not cleared");
        h.succeed();
    }

    @GameTest(template="empty")
    public static void inheritanceHasSelectiveRealRuinSources(GameTestHelper h) {
        String[] tables={"simple_dungeon","stronghold_library","ancient_city","end_city_treasure"};
        for(int tier=0;tier<4;tier++)h.assertTrue(KnowledgeLoot.scrollTier(ResourceLocation.fromNamespaceAndPath("minecraft","chests/"+tables[tier]))==tier,"Incorrect ruin tier");
        h.assertTrue(KnowledgeLoot.scrollTier(ResourceLocation.fromNamespaceAndPath("minecraft","chests/village/village_toolsmith"))<0,"Every village became a scroll source");
        var sources=new HashMap<ResourceLocation,Integer>();
        KnowledgeLoot.sources(new cn.blockforge.generated.generatedmod.acquisition.AcquisitionSourcesEvent((item,evidence)->sources.merge(item,1,Integer::sum)));
        for(var scroll:KnowledgeItems.SCROLLS)h.assertTrue(sources.containsKey(scroll.getId()),"Scroll missing from survival acquisition audit");
        h.succeed();
    }

    @GameTest(template="empty")
    public static void lecternCannotExposeAnotherReadersSnapshot(GameTestHelper h) {
        var player=FakePlayerFactory.get(h.getLevel(),new GameProfile(UUID.randomUUID(),"LecternQA"));
        var pos=h.absolutePos(net.minecraft.core.BlockPos.ZERO);
        var state=net.minecraft.world.level.block.Blocks.LECTERN.defaultBlockState();
        h.getLevel().setBlock(pos,state,3);
        var book=new ItemStack(Items.WRITTEN_BOOK);CompendiumSnapshot.refresh(book,player);
        player.setItemInHand(InteractionHand.MAIN_HAND,book);
        var hit=new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(pos),net.minecraft.core.Direction.UP,pos,false);
        var placing=new PlayerInteractEvent.RightClickBlock(player,InteractionHand.MAIN_HAND,pos,hit);
        MyriadCompendiumItem.lectern(placing);
        h.assertTrue(placing.isCanceled(),"Personal compendium could be placed on a shared lectern");
        var lectern=(net.minecraft.world.level.block.entity.LecternBlockEntity)h.getLevel().getBlockEntity(pos);
        lectern.setBook(book.copy());
        h.getLevel().setBlock(pos,state.setValue(net.minecraft.world.level.block.LecternBlock.HAS_BOOK,true),3);
        player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
        var reading=new PlayerInteractEvent.RightClickBlock(player,InteractionHand.MAIN_HAND,pos,hit);
        MyriadCompendiumItem.lectern(reading);
        h.assertTrue(reading.isCanceled()&&lectern.getBook().isEmpty(),"Existing lectern snapshot remained readable");
        boolean returned=false;
        for(int slot=0;slot<player.getInventory().getContainerSize();slot++) {
            var item=player.getInventory().getItem(slot);
            if(MyriadCompendiumItem.isCompendium(item)) {
                returned=true;
                h.assertTrue(!item.getTag().contains(CompendiumSnapshot.READER),"Returned book retained a reader's snapshot");
            }
        }
        h.assertTrue(returned,"Lectern protection lost the compendium");
        player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.WRITTEN_BOOK));
        var ordinary=new PlayerInteractEvent.RightClickBlock(player,InteractionHand.MAIN_HAND,pos,hit);
        MyriadCompendiumItem.lectern(ordinary);
        h.assertTrue(!ordinary.isCanceled(),"Ordinary books were blocked");
        h.succeed();
    }
}
