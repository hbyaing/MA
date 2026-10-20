package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.arts.ImportedSwordArts;
import cn.blockforge.generated.generatedmod.arts.SustainedSwordArts;
import cn.blockforge.generated.generatedmod.fixtures.GameTestPlayer;
import cn.blockforge.generated.generatedmod.items.HeartSwordItem;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("wuxingxiantu_entry")
@PrefixGameTestTemplate(false)
public final class SustainedSwordArtsGameTests {
    private static GameTestPlayer player(GameTestHelper helper,int offset) {
        var player = new GameTestPlayer(helper.getLevel());
        var pos = helper.absolutePos(new BlockPos(2 + offset,2,2));
        // 不加入自动 tick 列表；下方真实服务端时间回调仅调用一次生产入口，准确核对供灵。
        player.setPos(pos.getX()+.5D,210,pos.getZ()+.5D);
        player.setNoGravity(true); player.keepTicking(helper.getLevel(),player.blockPosition());
        var data = GeneratedMod.data(player); data.wuxingxiantu$setRealm(ProgressionGate.GOLDEN_CORE);
        data.wuxingxiantu$setAura(data.wuxingxiantu$getMaxMana());
        return player;
    }
    private static void prepare(GameTestPlayer player,String art) {
        var data = GeneratedMod.data(player); data.wuxingxiantu$learnArt(art); data.wuxingxiantu$equipArt(0,art);
    }
    private static String pulseState(GameTestPlayer player,net.minecraft.world.entity.LivingEntity target) {
        return " active="+SustainedSwordArts.activeArt(player)+" alive="+target.isAlive()+" removed="+target.isRemoved()
                +" added="+target.isAddedToWorld()+" health="+target.getHealth()+" allowed="+SustainedSwordArts.allowedTarget(player,target)
                +" los="+player.hasLineOfSight(target)+" distance="+player.distanceTo(target)
                +" aura="+GeneratedMod.data(player).wuxingxiantu$getAura()+" pos="+target.position();
    }

    @GameTest(template="empty")
    public static void manualsUseExistingLearningAndSlotsAndRespectRealm(GameTestHelper helper) {
        try (var player=player(helper,0)) {
            var data=GeneratedMod.data(player);
            var book=new ItemStack(ImportedSwordArts.WANJIAN_GUIZONG.get());
            player.setItemInHand(InteractionHand.MAIN_HAND,book);
            data.wuxingxiantu$setRealm(ProgressionGate.FOUNDATION);
            book.getItem().use(helper.getLevel(),player,InteractionHand.MAIN_HAND);
            helper.assertTrue(book.getCount()==1 && !data.wuxingxiantu$hasLearnedArt(SustainedSwordArts.WANJIAN),
                    "A below-gate player consumed/learned the source manual");
            data.wuxingxiantu$setRealm(ProgressionGate.GOLDEN_CORE);
            book.getItem().use(helper.getLevel(),player,InteractionHand.MAIN_HAND);
            helper.assertTrue(book.isEmpty() && SustainedSwordArts.WANJIAN.equals(data.wuxingxiantu$getEquippedArts()[0]),
                    "Wanjian manual did not learn and populate the old art slot");
            var rain=new ItemStack(ImportedSwordArts.JIUXIAO_SWORD_RAIN.get());
            player.setItemInHand(InteractionHand.MAIN_HAND,rain);
            rain.getItem().use(helper.getLevel(),player,InteractionHand.MAIN_HAND);
            helper.assertTrue(rain.isEmpty() && SustainedSwordArts.JIUXIAO.equals(data.wuxingxiantu$getEquippedArts()[1]),
                    "Jiuxiao manual did not learn independently in the old UI data");
            helper.assertTrue(ArtCatalog.isValid("art_myriad_swords") && ArtCatalog.isValid("art_sword_array"),
                    "Source spells replaced an existing target spell");
        }
        helper.succeed();
    }

    @GameTest(template="empty")
    public static void releaseRejectsForgedProgressAndForeignSwordAndPreventsStacking(GameTestHelper helper) {
        try (var player=player(helper,0)) {
            var data=GeneratedMod.data(player); prepare(player,SustainedSwordArts.WANJIAN);
            data.wuxingxiantu$setRealm(ProgressionGate.FOUNDATION);
            float before=data.wuxingxiantu$getAura();
            helper.assertTrue(!SustainedSwordArts.releaseSlot(player,0) && data.wuxingxiantu$getAura()==before
                    && data.wuxingxiantu$getArtCooldown(0)==0,"Forged learned record bypassed realm or charged a failed cast");
            data.wuxingxiantu$setRealm(ProgressionGate.GOLDEN_CORE); data.wuxingxiantu$setAura(0);
            helper.assertTrue(!SustainedSwordArts.releaseSlot(player,0) && !SustainedSwordArts.active(player),
                    "An unfunded cast became active");
            data.wuxingxiantu$setAura(data.wuxingxiantu$getMaxMana());
            var foreign=new ItemStack(WuXingItems.DARK_IRON_SWORD.get());
            foreign.getOrCreateTag().putUUID(HeartSwordItem.OWNER_TAG,java.util.UUID.randomUUID());
            player.setItemInHand(InteractionHand.MAIN_HAND,foreign);
            helper.assertTrue(!SustainedSwordArts.releaseSlot(player,0),"Foreign bound sword powered a source art");
            player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
            helper.assertTrue(SustainedSwordArts.releaseSlot(player,0),"Funded, learned source art failed to start");
            data.wuxingxiantu$learnArt(SustainedSwordArts.JIUXIAO); data.wuxingxiantu$equipArt(1,SustainedSwordArts.JIUXIAO);
            before=data.wuxingxiantu$getAura();
            helper.assertTrue(!SustainedSwordArts.releaseSlot(player,1) && data.wuxingxiantu$getAura()==before
                    && SustainedSwordArts.WANJIAN.equals(SustainedSwordArts.activeArt(player)),"Two source spells stacked or charged twice");
            helper.assertTrue(SustainedSwordArts.releaseSlot(player,0) && !SustainedSwordArts.active(player)
                    && data.wuxingxiantu$getAura()==before,"Toggling off charged mana or failed during cooldown");
            data.wuxingxiantu$equipArt(1,SustainedSwordArts.WANJIAN);
            helper.assertTrue(!SustainedSwordArts.releaseSlot(player,1),"Moving an art to another slot bypassed its cooldown");
        }
        helper.succeed();
    }

    @GameTest(template="empty",timeoutTicks=140)
    public static void bothArtsPayBeforePulseAttributeDamageAndProtectPetsAndNeutralAnimals(GameTestHelper helper) {
        var wanjian=player(helper,0); var jiuxiao=player(helper,40);
        jiuxiao.setXRot(45); jiuxiao.setYRot(0);
        prepare(wanjian,SustainedSwordArts.WANJIAN); prepare(jiuxiao,SustainedSwordArts.JIUXIAO);
        var victim=EntityType.ZOMBIE.create(helper.getLevel());
        victim.setPos(wanjian.getX(),wanjian.getY(),wanjian.getZ()+3); victim.setNoAi(true); victim.setNoGravity(true);
        // 未 join 的施法夹具不在原版最近玩家列表；NoAI 不阻止离所有玩家128格外的怪物自然消失。
        victim.setPersistenceRequired();
        victim.getAttribute(Attributes.MAX_HEALTH).setBaseValue(2000); victim.setHealth(2000);
        victim.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.FIRE_RESISTANCE,200));
        helper.getLevel().addFreshEntity(victim);
        var rainVictim=EntityType.ZOMBIE.create(helper.getLevel());
        var anchor=jiuxiao.getEyePosition().add(jiuxiao.getLookAngle().scale(24));
        rainVictim.setPos(anchor.x,anchor.y,anchor.z); rainVictim.setNoAi(true); rainVictim.setNoGravity(true);
        rainVictim.setPersistenceRequired();
        rainVictim.getAttribute(Attributes.MAX_HEALTH).setBaseValue(2000); rainVictim.setHealth(2000);
        rainVictim.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.FIRE_RESISTANCE,200));
        helper.getLevel().addFreshEntity(rainVictim); jiuxiao.keepTicking(helper.getLevel(),rainVictim.blockPosition());
        var pet=EntityType.WOLF.create(helper.getLevel()); pet.setOwnerUUID(wanjian.getUUID()); pet.setTame(true);
        pet.setPos(wanjian.getX()+1,wanjian.getY(),wanjian.getZ()+1); pet.setNoAi(true); pet.setNoGravity(true);
        helper.getLevel().addFreshEntity(pet);
        var cow=EntityType.COW.create(helper.getLevel()); cow.setPos(wanjian.getX()-1,wanjian.getY(),wanjian.getZ()+1);
        cow.setNoAi(true); cow.setNoGravity(true); helper.getLevel().addFreshEntity(cow);
        wanjian.cleanupAfter(helper,() -> {SustainedSwordArts.stop(wanjian);victim.discard();pet.discard();cow.discard();});
        jiuxiao.cleanupAfter(helper,() -> {SustainedSwordArts.stop(jiuxiao);rainVictim.discard();});
        float petHealth=pet.getHealth(),cowHealth=cow.getHealth();
        helper.assertTrue(SustainedSwordArts.releaseSlot(wanjian,0) && SustainedSwordArts.releaseSlot(jiuxiao,0),"Source arts failed to enter windup");
        float funded=GeneratedMod.data(wanjian).wuxingxiantu$getAura();
        helper.runAfterDelay(21,() -> {
            SustainedSwordArts.tick(wanjian); SustainedSwordArts.tick(jiuxiao);
            float expected=ArtCatalog.finalManaCost(SustainedSwordArts.WANJIAN,GeneratedMod.data(wanjian).wuxingxiantu$getMaxMana());
            helper.assertTrue(Math.abs(funded-GeneratedMod.data(wanjian).wuxingxiantu$getAura()-expected)<.05F,
                    "The per-second debit differs from the old UI cost catalogue");
            float once=GeneratedMod.data(wanjian).wuxingxiantu$getAura(); SustainedSwordArts.tick(wanjian);
            helper.assertTrue(once==GeneratedMod.data(wanjian).wuxingxiantu$getAura(),"Same-tick upkeep was duplicated");
            helper.assertTrue(victim.getLastHurtByMob()!=wanjian && rainVictim.getLastHurtByMob()!=jiuxiao,"A spell hit before completing windup");
        });
        helper.runAfterDelay(91,() -> {
            helper.assertTrue(victim.isAlive()&&!victim.isRemoved()&&victim.isAddedToWorld()
                            &&helper.getLevel().getEntity(victim.getUUID())==victim,
                    "Wanjian target disappeared before its paid pulse:"+pulseState(wanjian,victim));
            helper.assertTrue(rainVictim.isAlive()&&!rainVictim.isRemoved()&&rainVictim.isAddedToWorld()
                            &&helper.getLevel().getEntity(rainVictim.getUUID())==rainVictim,
                    "Jiuxiao target disappeared before its paid pulse:"+pulseState(jiuxiao,rainVictim));
            helper.assertTrue(SustainedSwordArts.active(wanjian)&&SustainedSwordArts.allowedTarget(wanjian,victim),
                    "Wanjian cannot acquire its live target:"+pulseState(wanjian,victim));
            helper.assertTrue(SustainedSwordArts.active(jiuxiao)&&SustainedSwordArts.allowedTarget(jiuxiao,rainVictim),
                    "Jiuxiao cannot acquire its live target:"+pulseState(jiuxiao,rainVictim));
            float beforeWanjian=victim.getHealth(),beforeJiuxiao=rainVictim.getHealth();
            SustainedSwordArts.tick(wanjian); SustainedSwordArts.tick(jiuxiao);
            helper.assertTrue(victim.getHealth()<beforeWanjian&&victim.getLastHurtByMob()==wanjian,
                    "Wanjian paid pulse failed to damage or credit its player:"+pulseState(wanjian,victim));
            helper.assertTrue(rainVictim.getHealth()<beforeJiuxiao&&rainVictim.getLastHurtByMob()==jiuxiao,
                    "Jiuxiao paid pulse failed to damage or credit its player:"+pulseState(jiuxiao,rainVictim));
            helper.assertTrue(rainVictim.hasEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN),"Jiuxiao did not suppress its domain");
            helper.assertTrue(pet.getHealth()==petHealth && cow.getHealth()==cowHealth,"Sword pulses damaged a protected pet or neutral animal");
            float health=victim.getHealth(); GeneratedMod.data(wanjian).wuxingxiantu$setAura(0);
            helper.runAfterDelay(21,() -> {
                SustainedSwordArts.tick(wanjian);
                helper.assertTrue(!SustainedSwordArts.active(wanjian) && victim.getHealth()==health,"Unfunded pulse damaged a target");
                SustainedSwordArts.stop(jiuxiao); wanjian.close();jiuxiao.close();victim.discard();rainVictim.discard();pet.discard();cow.discard();
                helper.succeed();
            });
        });
    }

    @GameTest(template="empty")
    public static void deathLogoutDimensionAndSneakTerminateAndCooldownSurvivesClone(GameTestHelper helper) {
        try (var dead=player(helper,0);var offline=player(helper,1);var moved=player(helper,2);var sneaking=player(helper,3);var clone=player(helper,4)) {
            for(var player:java.util.List.of(dead,offline,moved,sneaking)) {
                prepare(player,SustainedSwordArts.WANJIAN);
                helper.assertTrue(SustainedSwordArts.releaseSlot(player,0),"Lifecycle fixture failed to start");
            }
            SustainedSwordArts.death(new LivingDeathEvent(dead,dead.damageSources().generic()));
            SustainedSwordArts.logout(new PlayerEvent.PlayerLoggedOutEvent(offline));
            SustainedSwordArts.dimension(new PlayerEvent.PlayerChangedDimensionEvent(moved,Level.OVERWORLD,Level.NETHER));
            sneaking.setShiftKeyDown(true); SustainedSwordArts.tick(sneaking);
            for(var player:java.util.List.of(dead,offline,moved,sneaking))
                helper.assertTrue(!SustainedSwordArts.active(player),"A lifecycle transition left an active sword spell");
            prepare(clone,SustainedSwordArts.WANJIAN);
            SustainedSwordArts.clone(new PlayerEvent.Clone(clone,dead,true));
            helper.assertTrue(!SustainedSwordArts.releaseSlot(clone,0),"Death cleared an art cooldown and allowed immediate recast");
        }
        helper.succeed();
    }
}
