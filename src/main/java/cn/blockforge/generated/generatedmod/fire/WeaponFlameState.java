package cn.blockforge.generated.generatedmod.fire;

import cn.blockforge.generated.generatedmod.*;
import java.util.Set;
import java.util.function.BooleanSupplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

/** Weapon eligibility and server-owned source, independent of the armor wrap system. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID)
public final class WeaponFlameState {
    private static final Set<String> SWORDS=Set.of("dark_iron_sword","mingjue_sword","qingming_sword",
            "jixiao_sword","wuxiang_sword","guiyuan_sword","tianque_sword");
    private static final String TAG="WeaponExoticFire",OWNER="WeaponExoticFireOwner";
    private static final ThreadLocal<ItemStack> ATTACK_STACK=new ThreadLocal<>();
    public static boolean eligible(Item item) {
        var id=ForgeRegistries.ITEMS.getKey(item);
        return id!=null&&id.getNamespace().equals(GeneratedMod.MOD_ID)&&SWORDS.contains(id.getPath());
    }
    public static int level(ItemStack stack) {
        return eligible(stack.getItem())?Math.min(5,EnchantmentHelper.getItemEnchantmentLevel(SpiritEnchantment.EXOTIC_FIRE_WRAP.get(),stack)):0;
    }
    public static ResourceLocation fireId(ItemStack stack) {
        if(level(stack)<=0||!stack.hasTag())return null;
        var id=ResourceLocation.tryParse(stack.getTag().getString(TAG));
        return id!=null&&FireCatalog.id(id.toString())!=null?id:null;
    }
    public static ResourceLocation fireId(ItemStack stack,Player owner) {
        var id=fireId(stack);
        return id!=null&&stack.getTag().hasUUID(OWNER)&&owner.getUUID().equals(stack.getTag().getUUID(OWNER))?id:null;
    }
    public static void refresh(ServerPlayer player) {
        var data=GeneratedMod.data(player);if(data==null)return;
        var fire=FireCatalog.tier(ProgressionGate.effectiveFire(data,data.wuxingxiantu$getEquippedFire()));
        refresh(player,player.getMainHandItem(),fire);
        refresh(player,player.getOffhandItem(),fire);
        refresh(player,data.wuxingxiantu$getYujianStack(),fire);
    }
    private static void refresh(ServerPlayer player,ItemStack stack,FireCatalog.Definition fire) {
        if(stack.isEmpty())return;
        if(fire==null||level(stack)<=0) {
            if(stack.hasTag()){stack.getTag().remove(TAG);stack.getTag().remove(OWNER);}return;
        }
        var tag=stack.getOrCreateTag();String id=fire.key().toString();
        if(!id.equals(tag.getString(TAG)))tag.putString(TAG,id);
        if(!tag.hasUUID(OWNER)||!player.getUUID().equals(tag.getUUID(OWNER)))tag.putUUID(OWNER,player.getUUID());
    }
    @SubscribeEvent public static void tick(TickEvent.PlayerTickEvent event) {
        if(event.phase==TickEvent.Phase.END&&event.player instanceof ServerPlayer player)refresh(player);
    }
    public static ItemStack attackingStack(Player player) {
        var stack=ATTACK_STACK.get();return stack==null?player.getMainHandItem():stack;
    }
    public static boolean withWeapon(ItemStack stack,BooleanSupplier action) {
        var previous=ATTACK_STACK.get();ATTACK_STACK.set(stack);
        try{return action.getAsBoolean();}
        finally {if(previous==null)ATTACK_STACK.remove();else ATTACK_STACK.set(previous);}
    }
    private WeaponFlameState() {}
}
