package cn.blockforge.generated.generatedmod.client.tribulation;
import cn.blockforge.generated.generatedmod.ModEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.*;

@Mod.EventBusSubscriber(modid="wuxingxiantu",value=Dist.CLIENT)
public final class ParalysisEffects {
 private static final class Field {final LightningRecipe recipe;int tick=Integer.MIN_VALUE;List<OrbitField.Segment> segments=List.of();Field(LightningRecipe r){recipe=r;}LightningRecipe recipe(){return recipe;}List<OrbitField.Segment> sample(int t){if(tick!=t){tick=t;segments=body(recipe,t);}return segments;}}
 private static final Map<UUID,Field> FIELDS=new HashMap<>();private static Object world;private static Float lockedYaw,lockedPitch;
 private static Field create(UUID id){try(var stream=Minecraft.getInstance().getResourceManager().open(new net.minecraft.resources.ResourceLocation("wuxingxiantu","tribulation/paralysis.json"))){var r=LightningRecipe.parse(com.google.gson.JsonParser.parseReader(new java.io.InputStreamReader(stream,java.nio.charset.StandardCharsets.UTF_8)));r.seed=id.hashCode();return new Field(r);}catch(Exception ex){throw new IllegalStateException(ex);}}
 @SubscribeEvent public static void action(net.minecraftforge.client.event.InputEvent.InteractionKeyMappingTriggered e){var p=Minecraft.getInstance().player;if(p!=null&&p.hasEffect(ModEffects.PARALYSIS.get())){e.setCanceled(true);e.setSwingHand(false);}}
 @SubscribeEvent public static void tick(net.minecraftforge.event.TickEvent.ClientTickEvent e){var mc=Minecraft.getInstance();if(e.phase!=net.minecraftforge.event.TickEvent.Phase.START)return;ParalysisPose.cleanup();if(mc.player==null||!mc.player.hasEffect(ModEffects.PARALYSIS.get())){lockedYaw=null;lockedPitch=null;return;}if(lockedYaw==null){lockedYaw=mc.player.getYRot();lockedPitch=mc.player.getXRot();}mc.player.setYRot(lockedYaw);mc.player.setXRot(lockedPitch);if(mc.screen!=null&&!(mc.screen instanceof net.minecraft.client.gui.screens.PauseScreen)&&!(mc.screen instanceof net.minecraft.client.gui.screens.ChatScreen)&&!(mc.screen instanceof net.minecraft.client.gui.screens.DeathScreen))mc.setScreen(null);for(var key:mc.options.keyMappings){key.setDown(false);while(key.consumeClick()){} }mc.player.setNoGravity(false);mc.player.setDeltaMovement(0,Math.min(0,mc.player.getDeltaMovement().y),0);}
 @SubscribeEvent public static void screen(net.minecraftforge.client.event.ScreenEvent.Opening e){var p=Minecraft.getInstance().player;if(p!=null&&p.hasEffect(ModEffects.PARALYSIS.get())&&e.getNewScreen() instanceof net.minecraft.client.gui.screens.inventory.AbstractContainerScreen<?>)e.setCanceled(true);}
 private static List<OrbitField.Segment> body(LightningRecipe r,double t){var out=new ArrayList<OrbitField.Segment>();double[][] bases={{0,.42,.36},{0,.05,.35},{0,-.27,.30},{-.43,.14,.145},{.43,.14,.145},{-.16,-.62,.14},{.16,-.62,.14},{0,.68,.29}};
  for(int lane=0;lane<16;lane++){double period=5+LightningMath.unit(r.seed,lane+70)*3,raw=t+LightningMath.unit(r.seed,lane+80)*period;long cycle=(long)Math.floor(raw/period),seed=r.seed+lane*7919+cycle*104729;double age=raw-cycle*period,life=3.2+LightningMath.unit(seed,12)*1.2;if(age>life||LightningMath.unit(seed,13)>.92)continue;
   int region=(int)(LightningMath.unit(seed,14)*8);var base=bases[region];double height=region==7?.53+LightningMath.unit(seed,15)*.3:region<3?-.24+LightningMath.unit(seed,15)*.76:region<5?-.20+LightningMath.unit(seed,15)*.74:-.92+LightningMath.unit(seed,15)*.7,tilt=(LightningMath.unit(seed,16)-.5)*1.4;
   var local=new LightningRecipe();local.orbitAnimation="ring";local.currentWidth=r.currentWidth;local.seed=(int)seed;local.strands=1;local.samples=24;local.orbitRadius=base[2]*(.9+.2*LightningMath.unit(seed,17));local.arcLength=base[2]*(1.2+LightningMath.unit(seed,18)*2);local.orbitWobble=region<3?.08:.04;local.orbitNoiseFrequency=3.2;local.throttle=1;local.orbitExtendSpeed=120;local.pulseInterval=8;
   float env=(float)(Math.min(1,age/.18)*Math.max(0,1-age/life));var motion=ParalysisPose.transform(t,r.seed);
   for(var seg:new OrbitField(local).sample(Vec3.ZERO,2+age*.15)){java.util.function.Function<Vec3,Vec3> point=v->motion.apply(new Vec3(v.x+base[0],v.y+v.z*Math.sin(tilt)+height,v.z*Math.cos(tilt)),region);out.add(new OrbitField.Segment(point.apply(seg.a()),point.apply(seg.b()),seg.width(),seg.alpha()*env,lane,seg.alongA(),seg.alongB()));}
  }return out;
 }
 @SubscribeEvent public static void input(MovementInputUpdateEvent e){
  if(!e.getEntity().hasEffect(ModEffects.PARALYSIS.get()))return;
  var i=e.getInput();i.forwardImpulse=0;i.leftImpulse=0;i.jumping=false;i.up=false;i.down=false;i.left=false;i.right=false;
 }
 @SubscribeEvent public static void render(RenderLevelStageEvent e){
  if(e.getStage()!=RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS)return;
  var mc=Minecraft.getInstance();if(world!=mc.level){world=mc.level;FIELDS.clear();}if(mc.level==null)return;
  var draws=new ArrayList<Runnable>();var active=new HashSet<UUID>();var camera=e.getCamera().getPosition();var pose=new PoseStack();pose.last().pose().set(e.getPoseStack().last().pose());
  for(var entity:mc.level.entitiesForRendering()){
   if(!(entity instanceof net.minecraft.world.entity.LivingEntity player))continue;
   if(active.size()>=16)break;
   if(!player.hasEffect(ModEffects.PARALYSIS.get())||player.distanceToSqr(camera)>48*48)continue;
   active.add(player.getUUID());var f=FIELDS.computeIfAbsent(player.getUUID(),ParalysisEffects::create);
   double scale=player.getBbHeight()/1.8;var center=player.getPosition(e.getPartialTick()).add(0,player.getBbHeight()*.5,0);
   var local=f.sample((int)ParalysisPose.age(player,player.tickCount));var segments=new ArrayList<OrbitField.Segment>(local.size());
   for(var s:local)segments.add(new OrbitField.Segment(attach(s.a().yRot((float)Math.toRadians(-player.yBodyRot)),center,scale),attach(s.b().yRot((float)Math.toRadians(-player.yBodyRot)),center,scale),s.width()*(float)scale,s.alpha(),s.strand(),s.alongA(),s.alongB()));
   draws.add(()->LightningRender.drawOrbit(segments,f.recipe(),pose,camera));
  }
  FIELDS.keySet().retainAll(active);
  if(!draws.isEmpty()){Runnable draw=()->draws.forEach(Runnable::run);if(!LightningShaderBridge.active()||!LightningShaderBridge.prepare(draw))draw.run();}
 }
 private static Vec3 attach(Vec3 local,Vec3 center,double scale){return center.add(local.x*scale,local.y*scale,local.z*scale);}
}

