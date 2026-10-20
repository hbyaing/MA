package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.fire.*;
import cn.blockforge.generated.generatedmod.fixtures.GameTestPlayer;
import cn.blockforge.generated.generatedmod.network.packet.cultivation.WeaponFlameHitPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/** Required server regressions. They cannot validate a first/third-person picture. */
@GameTestHolder(GeneratedMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class WeaponFlameGameTests {
    private static GameTestPlayer player(GameTestHelper h) {
        var p=new GameTestPlayer(h.getLevel());p.cleanupAfter(h,()->ExoticBurnManager.clear(p));
        var pos=h.absolutePos(new BlockPos(2,1,2));p.setPos(pos.getX()+.5,pos.getY(),pos.getZ()+.5);p.join();
        var data=GeneratedMod.data(p);data.wuxingxiantu$setRealm(8);
        for(int i=1;i<=9;i++)data.wuxingxiantu$learnFire(i);
        data.wuxingxiantu$setEquippedFire(1);return p;
    }
    private static ItemStack sword(String id) {
        var stack=new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(GeneratedMod.MOD_ID,id)));
        stack.enchant(SpiritEnchantment.EXOTIC_FIRE_WRAP.get(),2);return stack;
    }
    @GameTest(template="empty") public static void sevenSwordsOnlyAndArmorUnchanged(GameTestHelper h) {
        var enchant=SpiritEnchantment.EXOTIC_FIRE_WRAP.get();
        for(String id:new String[]{"dark_iron_sword","mingjue_sword","qingming_sword","jixiao_sword","wuxiang_sword","guiyuan_sword","tianque_sword"}) {
            var stack=sword(id);
            h.assertTrue(enchant.canEnchant(stack)&&enchant.canApplyAtEnchantingTable(stack)&&enchant.category.canEnchant(stack.getItem()),"Allowed sword rejected: "+id);
        }
        for(var item:new net.minecraft.world.item.Item[]{Items.DIAMOND_SWORD,Items.NETHERITE_AXE,Items.BOW,Items.CROSSBOW,
                Items.TRIDENT,WuXingItems.PEACH_SWORD.get(),WuXingItems.HEART_SWORD.get()}) {
            var stack=new ItemStack(item);
            h.assertTrue(!enchant.canEnchant(stack)&&!enchant.canApplyAtEnchantingTable(stack)&&!enchant.category.canEnchant(item),"Disallowed weapon accepted: "+item);
        }
        h.assertTrue(enchant.canEnchant(new ItemStack(Items.DIAMOND_CHESTPLATE)),"Armor rule changed");h.succeed();
    }
    @GameTest(template="empty") public static void selectedFireUpdatesHeldStackAndClearsOnUnequip(GameTestHelper h) {
        try(var p=player(h)) {
            var stack=sword("dark_iron_sword");p.setItemInHand(InteractionHand.MAIN_HAND,stack);
            for(int tier:new int[]{1,5,6}) {
                GeneratedMod.data(p).wuxingxiantu$setEquippedFire(tier);WeaponFlameState.refresh(p);
                h.assertTrue(FireCatalog.tier(tier).key().equals(WeaponFlameState.fireId(stack,p)),"Weapon source failed to switch");
            }
            GeneratedMod.data(p).wuxingxiantu$setEquippedFire(0);WeaponFlameState.refresh(p);
            h.assertTrue(WeaponFlameState.fireId(stack)==null,"Unequipped fire still active");
        }
        h.succeed();
    }
    @GameTest(template="empty") public static void targetUsesWeaponSourceAndRetainsDamageFormula(GameTestHelper h) {
        try(var p=player(h)) {
            var target=h.spawn(net.minecraft.world.entity.EntityType.ZOMBIE,new BlockPos(3,1,3));
            var stack=sword("dark_iron_sword");p.setItemInHand(InteractionHand.MAIN_HAND,stack);WeaponFlameState.refresh(p);
            GeneratedMod.data(p).wuxingxiantu$setEquippedFire(6);
            var event=new LivingHurtEvent(target,p.damageSources().playerAttack(p),10);
            SpiritEnchantmentLogic.applyExoticFireStrike(event);
            h.assertTrue(FireCatalog.tier(1).key().equals(ExoticBurnManager.getFireId(target)),"Hit looked up player selection instead of weapon fireId");
            h.assertTrue(Math.abs(event.getAmount()-10*1.05*1.04)<.001,"Existing tier/enchantment formula changed");
        }
        h.succeed();
    }
    @GameTest(template="empty") public static void offhandCannotEnchantUnrelatedMainhandAttack(GameTestHelper h) {
        try(var p=player(h)) {
            var target=h.spawn(net.minecraft.world.entity.EntityType.ZOMBIE,new BlockPos(3,1,3));
            p.setItemInHand(InteractionHand.OFF_HAND,sword("dark_iron_sword"));
            p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.IRON_SWORD));WeaponFlameState.refresh(p);
            var event=new LivingHurtEvent(target,p.damageSources().playerAttack(p),10);SpiritEnchantmentLogic.applyExoticFireStrike(event);
            h.assertTrue(event.getAmount()==10&&!ExoticBurnManager.isBurning(target),"Offhand leaked wrap to mainhand");
        }
        h.succeed();
    }
    @GameTest(template="empty") public static void forcedInvalidEnchantmentHasNoWeaponEffect(GameTestHelper h) {
        try(var p=player(h)) {
            var target=h.spawn(net.minecraft.world.entity.EntityType.ZOMBIE,new BlockPos(3,1,3));
            var stack=new ItemStack(Items.DIAMOND_AXE);stack.enchant(SpiritEnchantment.EXOTIC_FIRE_WRAP.get(),5);
            p.setItemInHand(InteractionHand.MAIN_HAND,stack);WeaponFlameState.refresh(p);
            var event=new LivingHurtEvent(target,p.damageSources().playerAttack(p),10);SpiritEnchantmentLogic.applyExoticFireStrike(event);
            h.assertTrue(WeaponFlameState.fireId(stack)==null&&event.getAmount()==10&&!ExoticBurnManager.isBurning(target),"Forced illegal NBT activated wrap");
        }
        h.succeed();
    }
    @GameTest(template="empty") public static void yujianUsesBoundStackInsteadOfMainhand(GameTestHelper h) {
        try(var p=player(h)) {
            var target=h.spawn(net.minecraft.world.entity.EntityType.ZOMBIE,new BlockPos(3,1,3));
            var bound=sword("mingjue_sword");GeneratedMod.data(p).wuxingxiantu$setYujianStack(bound);WeaponFlameState.refresh(p);
            p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.STICK));
            WeaponFlameState.withWeapon(bound,()->{
                SpiritEnchantmentLogic.applyExoticFireStrike(new LivingHurtEvent(target,p.damageSources().playerAttack(p),10));return true;
            });
            h.assertTrue(FireCatalog.tier(1).key().equals(ExoticBurnManager.getFireId(target)),"Bound sword source was lost");
            h.assertTrue(WeaponFlameState.attackingStack(p)==p.getMainHandItem(),"Attack context leaked");
        }
        h.succeed();
    }
    @GameTest(template="empty") public static void hitPacketKeepsSourceTargetAndContactPoint(GameTestHelper h) {
        try(var p=player(h)) {
            var packet=new WeaponFlameHitPacket(p.level().dimension().location(),p.getId(),p.getUUID(),FireCatalog.tier(5).key(),p.getEyePosition());
            var buffer=new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
            try {WeaponFlameHitPacket.encode(packet,buffer);h.assertTrue(packet.equals(WeaponFlameHitPacket.decode(buffer)),"Hit packet lost source or location");}
            finally {buffer.release();}
        }
        h.succeed();
    }
}
