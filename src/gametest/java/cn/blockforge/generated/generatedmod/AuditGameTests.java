package cn.blockforge.generated.generatedmod;

import com.mojang.authlib.GameProfile;
import cn.blockforge.generated.generatedmod.alchemy.FormulaKnowledge;
import cn.blockforge.generated.generatedmod.alchemy.PillCatalog;
import cn.blockforge.generated.generatedmod.mindscape.MindscapeExternalSnapshot;
import cn.blockforge.generated.generatedmod.mindscape.MindscapeInstances;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import java.util.UUID;

@GameTestHolder(GeneratedMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class AuditGameTests {
    @GameTest(template="empty") public static void recipeAndLootRegistryReferences(GameTestHelper helper) throws Exception {
        var manager=helper.getLevel().getServer().getResourceManager();
        int checked=0;
        for(String folder:java.util.List.of("recipes","loot_tables")) {
            for(var entry:manager.listResources(folder,id -> id.getNamespace().equals(GeneratedMod.MOD_ID)
                    && id.getPath().endsWith(".json")).entrySet()) {
                try(var reader=entry.getValue().openAsReader()) {
                    checkReferences(helper,com.google.gson.JsonParser.parseReader(reader),entry.getKey().toString());
                    checked++;
                }
            }
        }
        helper.assertTrue(checked>0,"No mod recipes or loot tables found");
        helper.succeed();
    }
    private static void checkReferences(GameTestHelper helper,com.google.gson.JsonElement value,String source) {
        if(value.isJsonArray()) {for(var child:value.getAsJsonArray())checkReferences(helper,child,source);return;}
        if(!value.isJsonObject())return;
        var object=value.getAsJsonObject();
        if(object.has("item")&&object.get("item").isJsonPrimitive())
            helper.assertTrue(net.minecraftforge.registries.ForgeRegistries.ITEMS.containsKey(ResourceLocation.parse(object.get("item").getAsString())),source+": unknown item "+object.get("item"));
        if(object.has("type")&&object.get("type").isJsonPrimitive()&&"minecraft:item".equals(object.get("type").getAsString())&&object.has("name"))
            helper.assertTrue(net.minecraftforge.registries.ForgeRegistries.ITEMS.containsKey(ResourceLocation.parse(object.get("name").getAsString())),source+": unknown loot item "+object.get("name"));
        if(object.has("block")&&object.get("block").isJsonPrimitive())
            helper.assertTrue(net.minecraftforge.registries.ForgeRegistries.BLOCKS.containsKey(ResourceLocation.parse(object.get("block").getAsString())),source+": unknown block "+object.get("block"));
        for(var entry:object.entrySet())checkReferences(helper,entry.getValue(),source);
    }
    private static FakePlayer player(GameTestHelper helper) {
        return FakePlayerFactory.get(helper.getLevel(),new GameProfile(UUID.randomUUID(),"Audit"));
    }
    @GameTest(template="empty") public static void invasionSurvivesOrdinaryChangesAndSave(GameTestHelper helper) {
        CultivationDataImpl data=new CultivationDataImpl();
        data.wuxingxiantu$setDaoXin(1);data.wuxingxiantu$setDaoXin(4);
        for(int ordinary:new int[]{0,2,3,5})data.wuxingxiantu$setDaoXin(ordinary);
        helper.assertTrue(data.wuxingxiantu$getDaoXin()==4,"ordinary events must not resolve invasion");
        CultivationDataImpl loaded=new CultivationDataImpl();loaded.deserializeNBT(data.serializeNBT());
        helper.assertTrue(loaded.wuxingxiantu$getDaoXin()==4,"invasion survives NBT roundtrip");
        loaded.wuxingxiantu$resolveHeartDemonInvasion();
        helper.assertTrue(loaded.wuxingxiantu$getDaoXin()==1,"explicit resolution restores previous state");
        helper.succeed();
    }
    @GameTest(template="empty") public static void highHealthUsesVanillaDamageAndHealing(GameTestHelper helper) {
        var cow=helper.spawn(EntityType.COW,1,1,1);
        cow.getAttribute(Attributes.MAX_HEALTH).setBaseValue(4000);
        cow.setHealth(3000);cow.heal(500);
        helper.assertTrue(cow.getMaxHealth()==4000&&cow.getHealth()==3500,"vanilla max health/heal exceeds 1024");
        cow.hurt(helper.getLevel().damageSources().generic(),100);
        helper.assertTrue(cow.getHealth()==3400,"vanilla damage operates on extended health");
        helper.succeed();
    }
    @GameTest(template="empty") public static void knowledgeIsExplicitEvenInCreative(GameTestHelper helper) {
        var player=player(helper);player.getAbilities().instabuild=true;
        var formula=ResourceLocation.fromNamespaceAndPath(GeneratedMod.MOD_ID,PillCatalog.values()[0].id);
        FormulaKnowledge.reset(player);
        helper.assertTrue(!FormulaKnowledge.knows(player,formula)&&!FormulaKnowledge.unlocked(player),"new/reset knowledge is empty even in creative");
        FormulaKnowledge.unlock(player);FormulaKnowledge.learn(player,formula);
        helper.assertTrue(FormulaKnowledge.knows(player,formula),"explicit learning works");
        helper.assertTrue(!FormulaKnowledge.learn(player,formula),"duplicate learning is idempotent");
        FormulaKnowledge.reset(player);
        helper.assertTrue(!FormulaKnowledge.knows(player,formula)&&!FormulaKnowledge.unlocked(player),"reset clears both states");
        helper.succeed();
    }
    @GameTest(template="empty") public static void externalSnapshotRestoresWithoutSession(GameTestHelper helper) {
        var player=player(helper);var data=GeneratedMod.data(player);
        player.getInventory().setItem(2,new ItemStack(Items.DIAMOND,7));
        player.getFoodData().setFoodLevel(13);
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE,600));
        data.wuxingxiantu$setRobeStack(new ItemStack(Items.LEATHER_CHESTPLATE));
        data.wuxingxiantu$setYujianStack(new ItemStack(Items.IRON_SWORD));
        helper.assertTrue(MindscapeExternalSnapshot.capture(player),"capture succeeds");
        helper.assertTrue(player.getInventory().getItem(2).isEmpty(),"capture seals inventory");
        CompoundTag persisted=data.serializeNBT();data.deserializeNBT(persisted);
        data.wuxingxiantu$getMindscapeData().remove("session");
        helper.assertTrue(MindscapeExternalSnapshot.restore(player),"restore does not depend on session");
        helper.assertTrue(player.getInventory().getItem(2).is(Items.DIAMOND)&&player.getInventory().getItem(2).getCount()==7,"items restored exactly");
        helper.assertTrue(player.getFoodData().getFoodLevel()==13&&player.hasEffect(MobEffects.FIRE_RESISTANCE),"food and effects restored");
        helper.assertTrue(data.wuxingxiantu$getRobeStack().is(Items.LEATHER_CHESTPLATE)&&data.wuxingxiantu$getYujianStack().is(Items.IRON_SWORD),"custom equipment slots restored");
        helper.assertTrue(!MindscapeExternalSnapshot.isValid(player),"snapshot cannot be used twice");
        helper.succeed();
    }
    @GameTest(template="empty") public static void instanceLeasesSurviveSave(GameTestHelper helper) {
        var instances=new MindscapeInstances();UUID owner=UUID.randomUUID(),session=UUID.randomUUID();
        int slot=instances.allocate(owner,session);instances.markInitialized(slot);
        var restored=MindscapeInstances.load(instances.save(new CompoundTag()));
        helper.assertTrue(restored.owns(owner,session,slot)&&restored.initialized(slot),"lease and arena initialization persist");
        restored.release(session);
        helper.assertTrue(!restored.owns(owner,session,slot),"released lease is no longer owned");
        helper.succeed();
    }
}
