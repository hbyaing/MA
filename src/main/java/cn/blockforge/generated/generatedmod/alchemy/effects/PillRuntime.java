package cn.blockforge.generated.generatedmod.alchemy.effects;

import cn.blockforge.generated.generatedmod.*;
import cn.blockforge.generated.generatedmod.alchemy.batch.*;
import cn.blockforge.generated.generatedmod.items.CultivationLogic;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.nbt.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.common.Mod;
import java.util.*;
import static cn.blockforge.generated.generatedmod.alchemy.effects.AbilityCompiler.*;

/** Server-only dose intents. Effects and inventory are saved in the same player snapshot before ack. */
@Mod.EventBusSubscriber(modid="wuxingxiantu")
public final class PillRuntime {
    private static final String RECEIPTS="V5DoseReceipts",TIMED="V5TimedPill",COOLDOWN="V5PillCooldownUntil";
    public static final UUID SPEED=UUID.fromString("ff9b116e-1a1c-4ab9-93c0-f2ad49204007");
    private static long now(ServerPlayer p){return p.server.overworld().getGameTime();}
    public static boolean canUse(ServerPlayer p,ItemStack stack){
        try{
            if(!p.isAlive()||p.isSpectator()||!stack.hasTag()||!stack.getTag().hasUUID(V5Batch.KEY))return false;
            recover(p);var journal=new BatchJournal(p.server);if(!journal.pendingDoses(p.getUUID()).isEmpty())return false;
            var d=journal.read(stack.getTag().getUUID(V5Batch.KEY));if(d.isEmpty())return false;V5Batch.validate(d);
            if(!d.getString("State").equals("CLAIMED")||d.contains("DoseIntent")||!PillSnapshot.matches(stack,d))return false;
            var result=PillSnapshot.read(d.getCompound("PillResult"));
            return d.getInt("DosesUsed")<result.quantity()&&now(p)>=p.getPersistentData().getLong(COOLDOWN)&&ProgressionGate.canUseV5Pill(p,result)&&applicable(p,result);
        }catch(RuntimeException e){com.mojang.logging.LogUtils.getLogger().warn("V5 pill rejected; no new effects granted",e);return false;}
    }
    /** Called by the shared BasePillItem only at the end of a real, server-observed eating action. */
    public static ItemStack finish(ServerPlayer p,ItemStack stack){
        InteractionHand hand=p.getUsedItemHand();
        if(!p.isUsingItem()||p.getTicksUsingItem()<14||!ItemStack.isSameItemSameTags(p.getUseItem(),stack)||!ItemStack.isSameItemSameTags(p.getItemInHand(hand),stack)||!canUse(p,stack))return p.getItemInHand(hand).copy();
        try{
            var journal=new BatchJournal(p.server);UUID id=stack.getTag().getUUID(V5Batch.KEY);var doc=journal.read(id);var intent=new CompoundTag();
            intent.putUUID("Id",UUID.randomUUID());intent.putUUID("Recipient",p.getUUID());intent.putInt("Dose",doc.getInt("DosesUsed")+1);intent.putInt("Slot",hand==InteractionHand.MAIN_HAND?p.getInventory().selected:40);
            journal.pendingDose(p.getUUID(),id,true);doc.put("DoseIntent",intent);journal.write(doc);complete(p,journal,doc);
        }catch(RuntimeException e){com.mojang.logging.LogUtils.getLogger().error("V5 dose intent retained; retry cannot grant a second dose",e);}
        finally{p.stopUsingItem();Network.stopUsingItem(p,stack.getItem(),0);}
        return p.getItemInHand(hand).copy();
    }
    public static void recover(ServerPlayer p){
        var journal=new BatchJournal(p.server);
        for(UUID id:journal.pendingDoses(p.getUUID())){
            var doc=journal.read(id);if(doc.contains("DoseIntent")&&doc.getCompound("DoseIntent").hasUUID("Recipient")&&doc.getCompound("DoseIntent").getUUID("Recipient").equals(p.getUUID()))complete(p,journal,doc);
            else journal.pendingDose(p.getUUID(),id,false);
        }
    }
    private static void complete(ServerPlayer p,BatchJournal journal,CompoundTag doc){
        V5Batch.validate(doc);var intent=doc.getCompound("DoseIntent");UUID receipt=intent.getUUID("Id");String key=receipt.toString();
        var receipts=p.getPersistentData().getCompound(RECEIPTS);if(receipts.size()>256)throw new IllegalArgumentException("Dose receipt budget exceeded");
        if(!receipts.getBoolean(key)){
            if(!p.isAlive()||p.isSpectator())return;
            int slot=intent.getInt("Slot");if(slot<0||slot>=p.getInventory().getContainerSize()||!PillSnapshot.matches(p.getInventory().getItem(slot),doc)){
                slot=-1;for(int i=0;i<p.getInventory().getContainerSize();i++)if(PillSnapshot.matches(p.getInventory().getItem(i),doc)){slot=i;break;}
            }
            if(slot<0){doc=doc.copy();doc.remove("DoseIntent");journal.write(doc);journal.pendingDose(p.getUUID(),doc.getUUID("Id"),false);return;}
            var result=PillSnapshot.read(doc.getCompound("PillResult"));
            if(!ProgressionGate.canUseV5Pill(p,result)||!applicable(p,result)){var cancelled=doc.copy();cancelled.remove("DoseIntent");journal.write(cancelled);journal.pendingDose(p.getUUID(),doc.getUUID("Id"),false);return;}
            var remaining=p.getInventory().getItem(slot).copy();remaining.shrink(1);p.getInventory().setItem(slot,remaining);
            // Mark in memory before executing whitelist services; an exception cannot replay a partial application.
            receipts.putBoolean(key,true);p.getPersistentData().put(RECEIPTS,receipts);
            cn.blockforge.generated.generatedmod.alchemy.PillEffects.applyCompiledDose(p,result);p.getPersistentData().putLong(COOLDOWN,now(p)+result.effects().stream().mapToInt(Spec::cooldown).max().orElse(20));
        }
        p.server.getPlayerList().save(p);
        try{
            var file=p.server.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).resolve("playerdata").resolve(p.getUUID()+".dat").toFile();var saved=NbtIo.readCompressed(file);
            if(saved==null||!saved.getCompound("ForgeData").getCompound(RECEIPTS).getBoolean(key))throw new IllegalStateException("Dose receipt not durable; intent retained");
        }catch(java.io.IOException e){throw new IllegalStateException("Cannot verify saved dose",e);}
        var ack=doc.copy();ack.putInt("DosesUsed",intent.getInt("Dose"));ack.remove("DoseIntent");journal.write(ack);journal.pendingDose(p.getUUID(),doc.getUUID("Id"),false);
        receipts.remove(key);p.getPersistentData().put(RECEIPTS,receipts);p.getInventory().setChanged();p.containerMenu.broadcastChanges();GeneratedMod.sync(p);
    }
    private static boolean applicable(ServerPlayer p,Result r){
        var data=GeneratedMod.data(p);if(data==null)return false;boolean useful=false;long time=now(p);var timed=p.getPersistentData().getCompound(TIMED);
        for(var e:r.effects())switch(e.id()){
            case heal -> useful|=p.getHealth()<p.getMaxHealth();
            case mana -> useful|=data.wuxingxiantu$getAura()<CultivationLogic.maxAura(data);
            case regeneration -> {if(timed.getLong("RegenUntil")>time)return false;useful=true;}
            case speed -> {if(timed.getLong("SpeedUntil")>time)return false;useful=true;}
            case shield -> {if(p.hasEffect(MobEffects.ABSORPTION)||p.getAbsorptionAmount()>0)return false;useful=true;}
            case resistance -> {var current=p.getEffect(MobEffects.DAMAGE_RESISTANCE);useful|=current==null||!current.isInfiniteDuration()&&(current.getAmplifier()+1<e.magnitudeMilli()/1000||current.getDuration()<e.duration());}
            case cleanse -> useful|=p.hasEffect(MobEffects.POISON)||p.hasEffect(MobEffects.CONFUSION)||p.hasEffect(MobEffects.WEAKNESS);
            case cultivation -> {if(p.isCreative()||!cn.blockforge.generated.generatedmod.mindscape.MindscapeManager.canGain(p,data,false))return false;useful|=data.wuxingxiantu$getCultivation()<CultivationLogic.breakthroughRequirement(data);}
            case minor_breakthrough -> {if(data.wuxingxiantu$getCultivationStage()>=3||data.wuxingxiantu$getCultivation()<CultivationLogic.breakthroughRequirement(data))return false;useful=true;}
        }
        return useful;
    }
    private static final Set<UUID> APPLYING_NATIVE=new HashSet<>();
    public static void schedule(ServerPlayer p,Spec e){
        var timed=p.getPersistentData().getCompound(TIMED);long time=now(p);
        if(e.id()==Kind.regeneration){timed.putLong("RegenMilli",e.magnitudeMilli());timed.putLong("RegenUntil",time+e.duration());timed.putLong("NextPulse",time+20);}
        else if(e.id()==Kind.speed){timed.putLong("SpeedMilli",e.magnitudeMilli());timed.putLong("SpeedUntil",time+e.duration());installSpeed(p,e.magnitudeMilli()/1000D);}
        else throw new IllegalArgumentException("Unknown timed module");
        p.getPersistentData().put(TIMED,timed);
    }
    public static void finiteNative(ServerPlayer p,MobEffect effect,int duration,int amp){
        String prefix=effect==MobEffects.ABSORPTION?"Shield":effect==MobEffects.DAMAGE_RESISTANCE?"Resistance":null;if(prefix==null)throw new IllegalArgumentException("Unknown finite native effect");
        var current=p.getEffect(effect);if(current!=null&&(current.isInfiniteDuration()||current.getAmplifier()>amp||current.getAmplifier()==amp&&current.getDuration()>=duration))return;
        APPLYING_NATIVE.add(p.getUUID());try{p.addEffect(new MobEffectInstance(effect,duration,amp));}finally{APPLYING_NATIVE.remove(p.getUUID());}
        var timed=p.getPersistentData().getCompound(TIMED);timed.putLong(prefix+"Until",now(p)+duration);timed.putInt(prefix+"Amp",amp);p.getPersistentData().put(TIMED,timed);
    }
    @SubscribeEvent public static void externalEffect(net.minecraftforge.event.entity.living.MobEffectEvent.Added e){
        if(e.getEntity() instanceof ServerPlayer p&&!APPLYING_NATIVE.contains(p.getUUID())){
            var effect=e.getEffectInstance().getEffect();String prefix=effect==MobEffects.ABSORPTION?"Shield":effect==MobEffects.DAMAGE_RESISTANCE?"Resistance":null;
            if(prefix!=null){var timed=p.getPersistentData().getCompound(TIMED);timed.remove(prefix+"Until");timed.remove(prefix+"Amp");p.getPersistentData().put(TIMED,timed);}
        }
    }
    private static void nativeExpiry(ServerPlayer p,CompoundTag timed,String prefix,MobEffect effect,boolean login){
        if(!timed.contains(prefix+"Until"))return;var current=p.getEffect(effect);long until=timed.getLong(prefix+"Until"),time=now(p);int amp=timed.getInt(prefix+"Amp");
        if(current==null||current.getAmplifier()!=amp){timed.remove(prefix+"Until");timed.remove(prefix+"Amp");return;}
        if(until<=time||until>time+12000){APPLYING_NATIVE.add(p.getUUID());try{p.removeEffect(effect);}finally{APPLYING_NATIVE.remove(p.getUUID());}timed.remove(prefix+"Until");timed.remove(prefix+"Amp");}
        else if(login){float absorption=p.getAbsorptionAmount();APPLYING_NATIVE.add(p.getUUID());try{p.removeEffect(effect);p.addEffect(new MobEffectInstance(effect,(int)(until-time),amp));if(effect==MobEffects.ABSORPTION)p.setAbsorptionAmount(Math.min(absorption,p.getAbsorptionAmount()));}finally{APPLYING_NATIVE.remove(p.getUUID());}}
    }
    private static void installSpeed(ServerPlayer p,double amount){var attr=p.getAttribute(Attributes.MOVEMENT_SPEED);if(attr==null)return;attr.removeModifier(SPEED);if(amount>0&&amount<=.35)attr.addTransientModifier(new AttributeModifier(SPEED,"V5 finite pill speed",amount,AttributeModifier.Operation.MULTIPLY_BASE));}
    public static void reconcile(ServerPlayer p,boolean login){
        if(!p.getPersistentData().contains(TIMED)){if(login)installSpeed(p,0);return;}
        var timed=p.getPersistentData().getCompound(TIMED);long time=now(p);
        if(!p.isAlive()){p.getPersistentData().remove(TIMED);installSpeed(p,0);return;}
        long until=timed.getLong("SpeedUntil"),milli=timed.getLong("SpeedMilli");
        if(until<=time||until>time+12000||milli<10||milli>350){timed.remove("SpeedUntil");timed.remove("SpeedMilli");installSpeed(p,0);}else if(login)installSpeed(p,milli/1000D);
        long regen=timed.getLong("RegenMilli"),regenUntil=timed.getLong("RegenUntil");
        if(regenUntil<time||regenUntil>time+12000||regen<50||regen>4000){timed.remove("RegenMilli");timed.remove("RegenUntil");timed.remove("NextPulse");}
        else if(login)timed.putLong("NextPulse",time+20);
        else if(time>=timed.getLong("NextPulse")){p.heal(regen/1000F);timed.putLong("NextPulse",time+20);}
        nativeExpiry(p,timed,"Shield",MobEffects.ABSORPTION,login);nativeExpiry(p,timed,"Resistance",MobEffects.DAMAGE_RESISTANCE,login);
        p.getPersistentData().put(TIMED,timed);
    }
    @SubscribeEvent public static void tick(TickEvent.PlayerTickEvent e){if(e.phase==TickEvent.Phase.END&&e.player instanceof ServerPlayer p){reconcile(p,false);if(p.tickCount%20==0)safeRecover(p);}}
    @SubscribeEvent(priority=EventPriority.HIGHEST) public static void login(PlayerEvent.PlayerLoggedInEvent e){if(e.getEntity() instanceof ServerPlayer p){safeRecover(p);reconcile(p,true);}}
    @SubscribeEvent public static void death(LivingDeathEvent e){if(e.getEntity() instanceof ServerPlayer p){p.getPersistentData().remove(TIMED);installSpeed(p,0);}}
    @SubscribeEvent public static void clone(PlayerEvent.Clone e){if(e.getEntity() instanceof ServerPlayer p){p.getPersistentData().putLong(COOLDOWN,e.getOriginal().getPersistentData().getLong(COOLDOWN));p.getPersistentData().put(RECEIPTS,e.getOriginal().getPersistentData().getCompound(RECEIPTS).copy());p.getPersistentData().remove(TIMED);installSpeed(p,0);}}
    private static void safeRecover(ServerPlayer p){try{recover(p);}catch(RuntimeException e){com.mojang.logging.LogUtils.getLogger().error("V5 dose recovery retained",e);}}
    private PillRuntime(){}
}
