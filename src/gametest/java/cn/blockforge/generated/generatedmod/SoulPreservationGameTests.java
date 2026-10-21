package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.fixtures.GameTestPlayer;
import cn.blockforge.generated.generatedmod.progression.PlayerCrafts;
import cn.blockforge.generated.generatedmod.soulpreservation.SoulPreservation;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import java.util.UUID;

@GameTestHolder("wuxingxiantu_entry") @PrefixGameTestTemplate(false)
public final class SoulPreservationGameTests {
    private static void place(GameTestHelper h,GameTestPlayer player) {
        BlockPos feet=h.absolutePos(new BlockPos(1,3,1));
        player.keepTicking(h.getLevel(),feet);
        for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++) {
            h.getLevel().setBlockAndUpdate(feet.offset(x,-1,z),Blocks.STONE.defaultBlockState());
            for(int y=0;y<3;y++)h.getLevel().setBlockAndUpdate(feet.offset(x,y,z),Blocks.AIR.defaultBlockState());
        }
        player.setPos(Vec3.atBottomCenterOf(feet));
        GeneratedMod.data(player).wuxingxiantu$setRealm(3);
        player.setHealth(6);player.setAbsorptionAmount(0);
    }
    @GameTest(template="empty") public static void finalFatalExcessEscapesAfterFiniteTargetShield(GameTestHelper h) {
        h.getLevel().getServer().setDifficulty(net.minecraft.world.Difficulty.NORMAL,true);
        try(var player=new GameTestPlayer(h.getLevel())) {
            place(h,player);player.join();
            var zombie=EntityType.ZOMBIE.create(h.getLevel());
            zombie.setPos(player.position().add(3,0,0));h.getLevel().addFreshEntity(zombie);
            try {
                zombie.setTarget(player);zombie.setLastHurtByMob(player);
                player.addEffect(new MobEffectInstance(ModEffects.SPIRIT_SHIELD_VISUAL.get(),400));player.setAbsorptionAmount(8);
                player.hurt(player.damageSources().mobAttack(zombie),6);
                h.assertTrue(player.getHealth()==6&&player.getAbsorptionAmount()==2&&SoulPreservation.cooldown(player)==0,
                        "Nonfatal intercepted damage consumed the escape cooldown or bypassed the existing shield");
                player.invulnerableTime=0;player.hurt(player.damageSources().mobAttack(zombie),10);
                h.assertTrue(player.getDeltaMovement().lengthSqr()<1.0E-8,"The same fatal hurt replayed knockback after teleport");
                h.assertTrue(player.isAlive()&&player.getHealth()==6&&SoulPreservation.cooldown(player)==SoulPreservation.COOLDOWN_TICKS,
                        "Final fatal excess failed to escape or did not reserve exactly 25 minutes");
                h.assertTrue(SoulPreservation.safeLanding(player,player.position()),"Escape landed outside a supported collision-free loaded position");
                h.assertTrue(player.getEffect(MobEffects.INVISIBILITY).getDuration()==160
                        &&player.getEffect(MobEffects.MOVEMENT_SPEED).getAmplifier()==4
                        &&player.getEffect(MobEffects.DAMAGE_RESISTANCE).getAmplifier()==4
                        &&player.getEffect(MobEffects.ABSORPTION).getDuration()==200,
                        "Source short retreat protections were lost");
                h.assertTrue(zombie.getTarget()==null&&zombie.getLastHurtByMob()==null,"The source decoy did not clear pursuer aggro");
                h.assertTrue(GeneratedMod.data(player).wuxingxiantu$getRealm()==3,"Escape changed target cultivation rules");
            } finally {zombie.discard();}
        }h.succeed();
    }
    @GameTest(template="empty") public static void targetArmorPreventsRawFatalDamageFromSpendingEscape(GameTestHelper h) {
        try(var player=new GameTestPlayer(h.getLevel())) {
            place(h,player);player.join();player.setHealth(4);
            player.setItemSlot(EquipmentSlot.HEAD,new ItemStack(Items.DIAMOND_HELMET));
            player.setItemSlot(EquipmentSlot.CHEST,new ItemStack(Items.DIAMOND_CHESTPLATE));
            player.setItemSlot(EquipmentSlot.LEGS,new ItemStack(Items.DIAMOND_LEGGINGS));
            player.setItemSlot(EquipmentSlot.FEET,new ItemStack(Items.DIAMOND_BOOTS));
            // Vanilla installs equipment attribute modifiers during the equipment-change tick.
            player.tick();
            h.assertTrue(player.getArmorValue()==20,"Diamond equipment attributes were not applied by the fixture");
            player.setHealth(4);player.setAbsorptionAmount(0);
            var attacker=EntityType.ZOMBIE.create(h.getLevel());
            var armoredHit=player.damageSources().mobAttack(attacker);
            h.assertTrue(!armoredHit.is(net.minecraft.tags.DamageTypeTags.BYPASSES_ARMOR),"Armor regression must use an armor-mitigated source");
            player.hurt(armoredHit,6);
            h.assertTrue(player.isAlive()&&player.getHealth()>0&&player.getHealth()<4&&SoulPreservation.cooldown(player)==0,
                    "Raw pre-armor fatal amount incorrectly consumed the escape");
        }h.succeed();
    }
    @GameTest(template="empty") public static void forcedDeathVoidTribulationAndActiveProjectionRemainAuthoritative(GameTestHelper h) {
        try(var player=new GameTestPlayer(h.getLevel())) {
            place(h,player);player.join();var data=GeneratedMod.data(player);
            h.assertTrue(!SoulPreservation.eligible(player,player.damageSources().genericKill())
                    &&!SoulPreservation.eligible(player,player.damageSources().fellOutOfWorld()),"Forced death or void became escapable");
            data.wuxingxiantu$setRealm(2);h.assertTrue(!SoulPreservation.eligible(player,player.damageSources().generic()),"Golden core gained Nascent Soul escape");
            data.wuxingxiantu$setRealm(3);
            for(String active:new String[]{"yuan_shen_chu_qiao","yi_hun_ji_po"}) {
                data.wuxingxiantu$setActiveDivineArt(active);
                h.assertTrue(!SoulPreservation.eligible(player,player.damageSources().generic()),"Escape conflicts with the existing active soul art");
            }
            data.wuxingxiantu$setActiveDivineArt("");
            player.getPersistentData().putBoolean("wuxingxiantu_tribulation_strike",true);
            h.assertTrue(!SoulPreservation.eligible(player,player.damageSources().generic()),"Tribulation strike death was bypassed");
            player.getPersistentData().remove("wuxingxiantu_tribulation_strike");
            data.wuxingxiantu$getMindscapeData().putUUID("session",UUID.randomUUID());
            h.assertTrue(!SoulPreservation.eligible(player,player.damageSources().generic()),"Mindscape failure was bypassed");
            data.wuxingxiantu$getMindscapeData().remove("session");
            player.hurt(player.damageSources().genericKill(),Float.MAX_VALUE);
            h.assertTrue(!player.isAlive()&&SoulPreservation.cooldown(player)==0,"Vanilla /kill semantics were intercepted");
        }h.succeed();
    }
    @GameTest(template="empty") public static void cooldownLivesInExistingWorldAccountAcrossDeathReplacementAndSave(GameTestHelper h) {
        try(var player=new GameTestPlayer(h.getLevel())) {
            place(h,player);player.join();player.hurt(player.damageSources().generic(),12);
            int remaining=SoulPreservation.cooldown(player);
            h.assertTrue(remaining==30000,"Escape did not create the configured world-clock cooldown");
            var loaded=PlayerCrafts.load(PlayerCrafts.get(player.server).save(new CompoundTag()));
            h.assertTrue(loaded.account(player.getUUID()).getLong(SoulPreservation.COOLDOWN_KEY)
                    ==player.server.overworld().getGameTime()+remaining,"Save/reload erased the existing account cooldown");
            player.invulnerableTime=0;player.hurt(player.damageSources().genericKill(),Float.MAX_VALUE);
            try(var replacement=new GameTestPlayer(h.getLevel())) {
                replacement.setUUID(player.getUUID());GeneratedMod.data(replacement).wuxingxiantu$setRealm(3);
                h.assertTrue(SoulPreservation.cooldown(replacement)==remaining,"Death replacement or relog identity refreshed escape");
                h.assertTrue(!SoulPreservation.eligible(replacement,replacement.damageSources().generic()),"Cooldown can be bypassed by replacing the player entity");
            }
        }h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=40) public static void escapeHitSettlesOnceAndOrdinaryLaterKnockbackRemains(GameTestHelper h) {
        var player=new GameTestPlayer(h.getLevel());player.cleanupAfter(h,()->{});
        try {
            place(h,player);player.join();player.setHealth(4);
            try(var attacker=new GameTestPlayer(h.getLevel())) {
                attacker.join();attacker.setPos(player.position().add(2,0,0));
                attacker.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE).setBaseValue(100);
                // Run the real Player.attack continuation that restores this pre-hit velocity.
                player.setDeltaMovement(.8,-1.1,.6);attacker.attack(player);
                h.assertTrue(player.isAlive()&&SoulPreservation.cooldown(player)==30000,
                        "Real fatal player attack did not enter the escape settlement");
                Vec3 landing=player.position();
                h.runAfterDelay(1,()->{
                    h.assertTrue(player.position().distanceToSqr(landing)<.01
                            &&SoulPreservation.safeLanding(player,player.position()),"Escaped hit displaced the player from its safe landing by the next tick");
                    h.assertTrue(player.getDeltaMovement().lengthSqr()<.02,"Post-attack saved velocity survived escape settlement");
                    player.setDeltaMovement(Vec3.ZERO);player.knockback(.4,1,0);
                    h.assertTrue(player.getDeltaMovement().lengthSqr()>.01,"Escape marker granted later ordinary knockback immunity");
                    h.succeed();
                });
            }
        } catch(RuntimeException|Error error) {player.close();throw error;}
    }
    @GameTest(template="empty") public static void safeLandingRejectsObstructionHazardsBorderAndUnloadedChunks(GameTestHelper h) {
        try(var player=new GameTestPlayer(h.getLevel())) {
            place(h,player);Vec3 origin=player.position();BlockPos ground=player.blockPosition().below();
            h.assertTrue(SoulPreservation.safeLanding(player,origin),"Supported initial landing was rejected");
            h.getLevel().setBlockAndUpdate(player.blockPosition().above(),Blocks.STONE.defaultBlockState());
            h.assertTrue(!SoulPreservation.safeLanding(player,origin),"Escape accepted a head collision");
            h.getLevel().setBlockAndUpdate(player.blockPosition().above(),Blocks.AIR.defaultBlockState());
            h.getLevel().setBlockAndUpdate(ground,Blocks.MAGMA_BLOCK.defaultBlockState());
            h.assertTrue(!SoulPreservation.safeLanding(player,origin),"Escape accepted damaging ground");
            int chunk=600000;boolean before=h.getLevel().hasChunk(chunk,chunk);
            h.assertTrue(!SoulPreservation.safeLanding(player,new Vec3(chunk*16+.5,origin.y,chunk*16+.5)),"Escape accepted an unloaded distant chunk");
            h.assertTrue(h.getLevel().hasChunk(chunk,chunk)==before,"Safe position search generated a distant chunk");
            h.assertTrue(!SoulPreservation.safeLanding(player,new Vec3(30_000_100,origin.y,0))
                    &&!SoulPreservation.safeLanding(player,new Vec3(Double.NaN,origin.y,0)),"Escape accepted world border or nonfinite positions");
        }h.succeed();
    }
}
