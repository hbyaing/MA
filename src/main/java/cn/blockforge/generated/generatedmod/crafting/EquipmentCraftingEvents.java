package cn.blockforge.generated.generatedmod.crafting;

import cn.blockforge.generated.generatedmod.*;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.item.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID)
public final class EquipmentCraftingEvents {
    private static final UUID WALK=UUID.nameUUIDFromBytes("wuxingcraft/walking".getBytes(StandardCharsets.UTF_8));
    private EquipmentCraftingEvents(){}
    @SubscribeEvent public static void attributes(ItemAttributeModifierEvent e){
        var s=e.getItemStack();if(!EquipmentCrafting.eligible(s))return;var slot=e.getSlotType();
        double weapon=EquipmentCrafting.refine(s)*.015+.04*EquipmentCrafting.temper(s,0)-.02*EquipmentCrafting.temper(s,2);
        double armor=EquipmentCrafting.refine(s)*.01;
        for(var attr:List.of(Attributes.ATTACK_DAMAGE,Attributes.ARMOR)){
            double base=0;for(var m:s.getItem().getDefaultAttributeModifiers(slot).get(attr))if(m.getOperation()==AttributeModifier.Operation.ADDITION)base+=m.getAmount();
            double amount=base*(attr==Attributes.ARMOR?armor:weapon);if(amount!=0)add(e,attr,amount);
        }
        if(slot==EquipmentSlot.MAINHAND&&s.getItem() instanceof SwordItem){double haste=.03*EquipmentCrafting.temper(s,3);if(haste>0)add(e,Attributes.ATTACK_SPEED,1.6*haste);}
    }
    private static void add(ItemAttributeModifierEvent e,Attribute attr,double value){var id=UUID.nameUUIDFromBytes(("wuxingcraft/"+e.getSlotType()+"/"+attr.getDescriptionId()).getBytes(StandardCharsets.UTF_8));e.addModifier(attr,new AttributeModifier(id,"wuxingcraft",value,AttributeModifier.Operation.ADDITION));}
    @SubscribeEvent public static void tick(TickEvent.PlayerTickEvent e){
        if(e.phase!=TickEvent.Phase.END||!(e.player instanceof ServerPlayer p))return;
        var boots=p.getItemBySlot(EquipmentSlot.FEET);int light=EquipmentCrafting.level(boots,"light");double walking=EquipmentCrafting.eligible(boots)?new double[]{0,.02,.03,.04}[light]*EquipmentCrafting.potency(boots,"light")+.03*EquipmentCrafting.temper(boots,3):0;
        if(!p.onGround()||p.isPassenger()||p.getAbilities().flying||p.isInWaterOrBubble())walking=0;
        var movement=p.getAttribute(Attributes.MOVEMENT_SPEED);if(movement!=null){var old=movement.getModifier(WALK);double amount=.1*walking;if(old!=null&&Math.abs(old.getAmount()-amount)>.0000001)movement.removeModifier(WALK);if(amount>0&&movement.getModifier(WALK)==null)movement.addTransientModifier(new AttributeModifier(WALK,"wuxingcraft walking",amount,AttributeModifier.Operation.ADDITION));}
        if(p.tickCount%20==0){boolean changed=false;for(var s:p.getInventory().items)changed|=EquipmentCrafting.migrate(s);for(var s:p.getInventory().armor)changed|=EquipmentCrafting.migrate(s);for(var s:p.getInventory().offhand)changed|=EquipmentCrafting.migrate(s);if(changed){p.getInventory().setChanged();p.containerMenu.broadcastChanges();}}
        var n=p.getPersistentData();int charge=EquipmentCrafting.level(p.getMainHandItem(),"charge");
        if(charge>0&&p.onGround()&&p.getDeltaMovement().horizontalDistanceSqr()<.0001&&!p.isPassenger()&&!p.getAbilities().flying){
            var current=p.getMainHandItem();if(!EquipmentCrafting.write(current).hasUUID("Id"))EquipmentCrafting.write(current).putUUID("Id",UUID.randomUUID());var id=EquipmentCrafting.read(current).getUUID("Id");
            if(!n.hasUUID("WuxingChargeItem")||!n.getUUID("WuxingChargeItem").equals(id)){n.putUUID("WuxingChargeItem",id);n.putInt("WuxingChargeTicks",0);}
            n.putInt("WuxingChargeTicks",Math.min(100,n.getInt("WuxingChargeTicks")+1));
        }else{n.putInt("WuxingChargeTicks",0);n.remove("WuxingChargeItem");}
    }
    @SubscribeEvent public static void chargedHit(LivingHurtEvent e){
        if(!(e.getSource().getDirectEntity() instanceof ServerPlayer p)||!e.getSource().is(net.minecraft.world.damagesource.DamageTypes.PLAYER_ATTACK))return;
        var n=p.getPersistentData();var s=p.getMainHandItem();var craft=EquipmentCrafting.read(s);int l=EquipmentCrafting.level(s,"charge");
        if(l<=0||n.getInt("WuxingChargeTicks")<100||!craft.hasUUID("Id")||!n.hasUUID("WuxingChargeItem")||!craft.getUUID("Id").equals(n.getUUID("WuxingChargeItem"))||n.getLong("WuxingChargeCooldown")>p.level().getGameTime())return;
        double base=0;for(var m:s.getItem().getDefaultAttributeModifiers(EquipmentSlot.MAINHAND).get(Attributes.ATTACK_DAMAGE))if(m.getOperation()==AttributeModifier.Operation.ADDITION)base+=m.getAmount();
        e.setAmount(e.getAmount()+(float)(base*.02*l*EquipmentCrafting.potency(s,"charge")));n.putInt("WuxingChargeTicks",0);n.putLong("WuxingChargeCooldown",p.level().getGameTime()+60);
    }
    @SubscribeEvent public static void tooltip(ItemTooltipEvent e){
        var s=e.getItemStack();var n=EquipmentCrafting.read(s);if(n.isEmpty()&&EquipmentCrafting.guardLevel(s)==0)return;
        for(int i=0;i<EquipmentCrafting.RUNES.length;i++){String rune=EquipmentCrafting.RUNES[i];int l=rune.equals("guard")?EquipmentCrafting.guardLevel(s):EquipmentCrafting.level(s,rune);if(l>0)e.getToolTip().add(Component.literal("铭纹 · "+EquipmentCrafting.NAMES[i]+" "+l+" · "+EquipmentCrafting.QUALITY[Math.max(0,Math.min(2,n.getCompound("Runes").getCompound(rune).getInt("Quality")))]).withStyle(ChatFormatting.AQUA));}
        if(EquipmentCrafting.refine(s)>0)e.getToolTip().add(Component.literal("精炼 +"+EquipmentCrafting.refine(s)+" · 基础属性成长").withStyle(ChatFormatting.GOLD));
        if(n.contains("TemperBp"))e.getToolTip().add(Component.literal("胚性 · "+EquipmentCrafting.TEMPER[Math.max(0,Math.min(3,n.getInt("Temper")))]+" · "+EquipmentCrafting.QUALITY[Math.max(0,Math.min(3,n.getInt("TemperQuality")))]).withStyle(ChatFormatting.LIGHT_PURPLE));
        if(n.contains("Pending"))e.getToolTip().add(Component.literal("有待选胚性：回到重铸精炼台采用或放弃").withStyle(ChatFormatting.YELLOW));
        if(n.getInt("GuardCredit")>0)e.getToolTip().add(Component.literal("旧书兑换 · 灵力御体 "+n.getInt("GuardCredit")+"阶刻印凭据").withStyle(ChatFormatting.AQUA));
    }
}
