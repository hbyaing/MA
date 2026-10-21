package cn.blockforge.generated.generatedmod;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.Map;
import java.util.WeakHashMap;

/** Horizontal lock preserves gravity and terrain collisions. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID)
public final class ParalysisControl {
 private record Anchor(net.minecraft.world.level.Level level,Vec3 position,float yaw,float pitch) {}
 private static final ThreadLocal<Boolean> TERRAIN_BREAK=ThreadLocal.withInitial(()->false);
 private static final Map<LivingEntity,Anchor> ANCHORS=new WeakHashMap<>();
 private static void lock(LivingEntity entity){
  if(entity.level().isClientSide)return;
  if(!entity.isAlive()||!entity.hasEffect(ModEffects.PARALYSIS.get())){ANCHORS.remove(entity);return;}
  var anchor=ANCHORS.computeIfAbsent(entity,e->anchor(e));
  if(anchor.level()!=entity.level()){anchor=anchor(entity);ANCHORS.put(entity,anchor);}
  entity.stopUsingItem();entity.setSprinting(false);entity.setNoGravity(false);
  entity.setDeltaMovement(0,Math.min(0,entity.getDeltaMovement().y),0);
  entity.setYRot(anchor.yaw());entity.setXRot(anchor.pitch());
  if(entity instanceof ServerPlayer player){player.getAbilities().flying=false;if(player.containerMenu!=player.inventoryMenu)player.closeContainer();}
  double dx=entity.getX()-anchor.position().x,dz=entity.getZ()-anchor.position().z;
  if(dx*dx+dz*dz>1e-8){
   var p=anchor.position();
   var falling=entity.getDeltaMovement();
   if(entity instanceof ServerPlayer player)player.connection.teleport(p.x,entity.getY(),p.z,anchor.yaw(),anchor.pitch());
   else entity.setPos(p.x,entity.getY(),p.z);
   entity.setDeltaMovement(falling);
  }
 }
 public static boolean blocked(LivingEntity e){return e!=null&&e.hasEffect(ModEffects.PARALYSIS.get());}
 public static boolean isTerrainBreak(){return TERRAIN_BREAK.get();}
 private static Anchor anchor(LivingEntity e){
  if(e instanceof ServerPlayer p){
   p.stopUsingItem();p.stopFallFlying();p.stopRiding();cn.blockforge.generated.generatedmod.yujian.YujianController.stop(p);
   var data=GeneratedMod.data(p);if(data!=null){DivineArtLogic.stop(p,data,false);data.wuxingxiantu$setGoldDomainEnabled(false);data.wuxingxiantu$setWoodDomainEnabled(false);data.wuxingxiantu$setWaterDomainEnabled(false);data.wuxingxiantu$setFireDomainEnabled(false);data.wuxingxiantu$setEarthDomainEnabled(false);cn.blockforge.generated.generatedmod.items.CultivationLogic.clearDomainEffects(p.serverLevel(),p);GeneratedMod.sync(p);}
   cn.blockforge.generated.generatedmod.realmflight.RealmFlightControl.release(p);
   p.getAbilities().flying=false;p.onUpdateAbilities();
  }
  return new Anchor(e.level(),e.position(),e.getYRot(),e.getXRot());
 }
 /** No damage-based stacking: refreshing a weaker hit never shortens the remaining lock. */
 public static int duration(int realm,double strength){return (int)Math.round((10+realm*3)*(.75+.5*Math.max(0,Math.min(1,strength))));}
 public static void apply(LivingEntity e,int ticks){if(!e.isAlive())return;var old=e.getEffect(ModEffects.PARALYSIS.get());e.addEffect(new net.minecraft.world.effect.MobEffectInstance(ModEffects.PARALYSIS.get(),Math.max(ticks,old==null?0:old.getDuration()),0,false,true,true));lock(e);}
 @SubscribeEvent public static void interact(net.minecraftforge.event.entity.player.PlayerInteractEvent e){if(blocked(e.getEntity())&&e.isCancelable())e.setCanceled(true);}
 @SubscribeEvent public static void outgoing(net.minecraftforge.event.entity.living.LivingAttackEvent e){if(e.getSource().getEntity() instanceof LivingEntity attacker&&blocked(attacker))e.setCanceled(true);}
 @SubscribeEvent public static void attack(net.minecraftforge.event.entity.player.AttackEntityEvent e){if(blocked(e.getEntity()))e.setCanceled(true);}
 public static boolean terrainBreak(net.minecraft.server.level.ServerLevel level,net.minecraft.core.BlockPos pos,net.minecraft.world.level.block.state.BlockState state,ServerPlayer owner){boolean previous=TERRAIN_BREAK.get();TERRAIN_BREAK.set(true);try{return net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.level.BlockEvent.BreakEvent(level,pos,state,owner));}finally{TERRAIN_BREAK.set(previous);}}
 @SubscribeEvent public static void breakBlock(net.minecraftforge.event.level.BlockEvent.BreakEvent e){if(blocked(e.getPlayer())&&!TERRAIN_BREAK.get())e.setCanceled(true);}
 @SubscribeEvent public static void use(net.minecraftforge.event.entity.living.LivingEntityUseItemEvent.Start e){if(blocked(e.getEntity()))e.setCanceled(true);}
 @SubscribeEvent public static void toss(net.minecraftforge.event.entity.item.ItemTossEvent e){if(blocked(e.getPlayer())){e.setCanceled(true);e.getPlayer().getInventory().placeItemBackInInventory(e.getEntity().getItem());}}
 @SubscribeEvent public static void living(LivingEvent.LivingTickEvent e){if(!(e.getEntity() instanceof ServerPlayer))lock(e.getEntity());}
 @SubscribeEvent public static void player(TickEvent.PlayerTickEvent e){if(e.phase==TickEvent.Phase.END)lock(e.player);}
 @SubscribeEvent public static void jump(LivingEvent.LivingJumpEvent e){if(e.getEntity().hasEffect(ModEffects.PARALYSIS.get()))e.getEntity().setDeltaMovement(Vec3.ZERO);}
}
