package cn.blockforge.generated.generatedmod.client.firecoil;

import cn.blockforge.generated.generatedmod.*;
import cn.blockforge.generated.generatedmod.client.ExoticFireOrbitRenderer;
import cn.blockforge.generated.generatedmod.fire.*;
import cn.blockforge.generated.generatedmod.client.weaponflame.*;
import com.google.gson.*;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.resources.model.BakedModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.client.event.*;
import java.util.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import org.joml.Vector3f;
import org.joml.Matrix4f;
import cn.blockforge.generated.generatedmod.client.firecoil.FireCore.V;

/** Consumes existing server-authoritative armor packets and weapon NBT. No gameplay mutation. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT)
public final class Client {
 public static boolean failed;public static String lastError="";
 private record Entry(UUID uuid,int tier,FireCore core){}
 private static final Map<Integer,Entry> entries=new HashMap<>();
 private static final Map<Integer,Long> claimed=new HashMap<>();
 private static final Map<Integer,Recipe> colors=new HashMap<>();
 private static final Set<Integer> weaponPlayers=new HashSet<>();
 private static final Map<Integer,Integer> weaponPasses=new HashMap<>();
 private static Recipe base;private static JsonArray palettes,levels;private static Object world;
 public static boolean bodyEligible(int id){return visualsEnabled()&&entries.containsKey(id);}
 private static final ThreadLocal<Player> weaponOwner=new ThreadLocal<>();
 public static int bodyDraws,weaponDraws;
 public static boolean visualsEnabled(){return HudConfig.EXOTIC_FIRE_WRAP_VISUAL.get()&&(!Minecraft.getInstance().options.getCameraType().isFirstPerson()||HudConfig.EXOTIC_FIRE_FIRST_PERSON_VISUAL.get());}
 public static double time(){var mc=Minecraft.getInstance();return mc.level==null?0:(mc.level.getGameTime()+mc.getFrameTime())*.05;}
 public static boolean owns(int entity){var mc=Minecraft.getInstance();return !failed&&visualsEnabled()&&mc.level!=null&&claimed.getOrDefault(entity,-100L)>=mc.level.getGameTime()-2;}
 private static String read(String name)throws Exception{try(var in=Client.class.getResourceAsStream("/assets/wuxingxiantu/firecoil/"+name)){if(in==null)throw new IllegalStateException(name);return new String(in.readAllBytes(),StandardCharsets.UTF_8);}}
 private static void load()throws Exception{
  Path path=net.minecraftforge.fml.loading.FMLPaths.CONFIGDIR.get().resolve("wuxingxiantu/firecoil.json");
  if(Files.isRegularFile(path)&&Files.size(path)>16384)throw new IllegalArgumentException("Fire coil recipe exceeds 16 KB");
  Recipe next=Recipe.parse(Files.isRegularFile(path)?Files.readString(path):read("recipe.json"));
  JsonArray nextPalettes=JsonParser.parseString(read("nine-flames.json")).getAsJsonObject().getAsJsonArray("palettes");
  JsonArray nextLevels=JsonParser.parseString(read("levels.json")).getAsJsonArray();
  if(nextPalettes==null||nextPalettes.size()!=9||nextLevels.size()!=5)throw new IllegalArgumentException("Invalid fire coil palettes/levels");
  palettes=nextPalettes;levels=nextLevels;base=next;colors.clear();
 }
 public static Recipe recipe(int tier){
  return recipe(tier,4);
 }
 public static Recipe recipe(int tier,int level){
  try {
  if(base==null)load();
  int safe=Math.max(1,Math.min(5,level));
  return colors.computeIfAbsent(tier*10+safe,k->{JsonObject j=base.json(),p=palettes.get(Math.max(1,Math.min(9,tier))-1).getAsJsonObject();j.addProperty("coreColor",p.get("fireCore").getAsString());j.addProperty("midColor",p.get("fireMid").getAsString());j.addProperty("edgeColor",p.get("fireEdge").getAsString());
   JsonObject l=levels.get(safe-1).getAsJsonObject();
   scale(j,l,"width","width",.04,.65);scale(j,l,"rotationSpeed","speed",-2,2);scale(j,l,"wispRate","wisps",0,80);scale(j,l,"emberRate","embers",0,160);scale(j,l,"intensity","intensity",.2,4);scale(j,l,"weaponWidth","weaponWidth",.02,.5);scale(j,l,"weaponStrength","weaponStrength",0,3);scale(j,l,"weaponTurns","turns",.25,5);
   j.addProperty("strands",Math.max(1,Math.min(8,(int)Math.floor(base.i("strands")*l.get("strands").getAsDouble()+.5))));var result=Recipe.parse(j.toString());return tier==9?result.withSpectrum():result;});
  }catch(Exception ex){fail(ex);return null;}
 }
 private static void scale(JsonObject j,JsonObject level,String key,String coefficient,double min,double max){j.addProperty(key,Math.max(min,Math.min(max,j.get(key).getAsDouble()*level.get(coefficient).getAsDouble())));}
 public static float[] color(Recipe recipe,String key){
  if(!recipe.dynamicSpectrum())return recipe.color(key);
  double ticks=time()*20;long tick=(long)Math.floor(ticks);float fraction=(float)(ticks-tick);
  int a=cn.blockforge.generated.generatedmod.SpiritFirePalette.spectrum(tick),b=cn.blockforge.generated.generatedmod.SpiritFirePalette.spectrum(tick+1);
  float[] result=new float[3];for(int i=0;i<3;i++){int shift=16-i*8;float c=(((a>>shift)&255)*(1-fraction)+((b>>shift)&255)*fraction)/255f;result[i]=key.equals("coreColor")?c*.2f+.8f:key.equals("edgeColor")?c*.35f:c;}
  return result;
 }
 public static void reset(){entries.clear();claimed.clear();colors.clear();weaponPlayers.clear();weaponPasses.clear();base=null;failed=false;FireRender.failed=false;VolumeGpu.release();}
 private static void fail(Exception ex){if(!failed)com.mojang.logging.LogUtils.getLogger().error("异火缠绕体积停用，保留原粒子回退",ex);failed=true;lastError=ex.toString();claimed.clear();}
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e){
  if(e.phase!=TickEvent.Phase.END)return;var mc=Minecraft.getInstance();
  if(world!=mc.level){world=mc.level;reset();}if(mc.level==null||mc.player==null||mc.isPaused())return;
  if(!visualsEnabled()||failed){entries.clear();claimed.clear();weaponPlayers.clear();return;}
  weaponPlayers.clear();
  var candidates=mc.level.players().stream().filter(p->p.isAlive()&&!p.isSpectator()&&p.distanceToSqr(mc.player)<48*48
   &&(ExoticFireOrbitRenderer.visualTier(p.getId())>0||WeaponFlameState.fireId(p.getMainHandItem(),p)!=null||WeaponFlameState.fireId(p.getOffhandItem(),p)!=null))
   .sorted(Comparator.comparingDouble(p->p.distanceToSqr(mc.player))).limit(4).toList();
  for(var p:candidates)weaponPlayers.add(p.getId());
  var selected=candidates.stream().filter(p->ExoticFireOrbitRenderer.visualTier(p.getId())>0).toList();
  Set<Integer> ids=new HashSet<>();for(var p:selected){int id=p.getId(),tier=ExoticFireOrbitRenderer.visualTier(id);ids.add(id);var entry=entries.get(id);
   if(entry==null||entry.tier!=tier||!entry.uuid.equals(p.getUUID())){entry=new Entry(p.getUUID(),tier,new FireCore());entries.put(id,entry);claimed.remove(id);}
   var r=recipe(tier,ExoticFireOrbitRenderer.visualLevel(id));if(r==null)return;entry.core.tick(r,new V(0,0,0),0,true,p==mc.player&&mc.options.getCameraType().isFirstPerson()?r.d("firstPerson"):1);
  }entries.keySet().retainAll(ids);claimed.keySet().retainAll(ids);
 }
 @SubscribeEvent public static void render(RenderLevelStageEvent e){
  if(e.getStage()!=RenderLevelStageEvent.Stage.AFTER_PARTICLES)return;bodyDraws=0;
  if(failed||!visualsEnabled())return;var mc=Minecraft.getInstance();if(mc.level==null)return;
  try{for(var item:entries.entrySet()){var raw=mc.level.getEntity(item.getKey());if(!(raw instanceof Player p)||!p.isAlive()||p.isSpectator()||ExoticFireOrbitRenderer.visualTier(p.getId())!=item.getValue().tier)continue;
   var r=recipe(item.getValue().tier,ExoticFireOrbitRenderer.visualLevel(p.getId()));if(r==null)return;double t=mc.isPaused()?0:e.getPartialTick();
   double strength=p==mc.player&&mc.options.getCameraType().isFirstPerson()?r.d("firstPerson"):1;
   Matrix4f attachment=BodyAttachment.matrix(e,p,(float)t);
   VolumeGpu.renderAttached(e,r,item.getValue().core,t,strength,attachment);
   if(VolumeGpu.draws>0){claimed.put(p.getId(),mc.level.getGameTime());bodyDraws++;}
   Matrix4f inverseAttachment=new Matrix4f(com.mojang.blaze3d.systems.RenderSystem.getModelViewMatrix()).mul(attachment).invert();
   Vector3f right=inverseAttachment.transformDirection(new Vector3f(1,0,0)).normalize(),up=inverseAttachment.transformDirection(new Vector3f(0,1,0)).normalize();
   var emberPose=new PoseStack();emberPose.last().pose().set(attachment);
   FireRender.draw(Geometry.embers(item.getValue().core,new V(0,0,0),new V(right.x,right.y,right.z),new V(up.x,up.y,up.z),t),r,emberPose,new V(0,0,0),item.getValue().core.time()+t*.05);
  }}catch(Exception ex){fail(ex);}
 }
 public static Player setWeaponOwner(Player p){Player old=weaponOwner.get();if(p==null)weaponOwner.remove();else weaponOwner.set(p);return old;}
 @SubscribeEvent(priority=EventPriority.HIGHEST) public static void frame(TickEvent.RenderTickEvent e){
  if(e.phase==TickEvent.Phase.START){weaponPasses.clear();bodyDraws=0;if(failed||!visualsEnabled()){VolumeGpu.skipFrame();return;}BodyAttachment.beginFrame();try{VolumeGpu.beginFrame();}catch(Exception ex){fail(ex);}}
  else if(e.phase==TickEvent.Phase.END&&!failed&&visualsEnabled())try{VolumeGpu.flushDeferred();}catch(Exception ex){fail(ex);}
 }
 @SubscribeEvent(priority=EventPriority.HIGHEST) public static void beforeGui(RenderGuiEvent.Pre e){
  if(failed||!visualsEnabled()||!VolumeGpu.hasDeferred())return;
  try{e.getGuiGraphics().flush();VolumeGpu.flushDeferred();}catch(Exception ex){fail(ex);}
 }
 public static void weapon(ItemStack stack,ItemDisplayContext view,PoseStack pose,MultiBufferSource buffers,BakedModel model){
  if(failed||!visualsEnabled()||model.isCustomRenderer())return;
  if(view!=ItemDisplayContext.FIRST_PERSON_LEFT_HAND&&view!=ItemDisplayContext.FIRST_PERSON_RIGHT_HAND&&view!=ItemDisplayContext.THIRD_PERSON_LEFT_HAND&&view!=ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)return;
  Player p=weaponOwner.get();var mc=Minecraft.getInstance();if(p==null&&mc.player!=null&&(stack==mc.player.getMainHandItem()||stack==mc.player.getOffhandItem()))p=mc.player;if(p==null||!p.isAlive()||p.isSpectator()||!weaponPlayers.contains(p.getId())||weaponPasses.getOrDefault(p.getId(),0)>=2)return;
  var id=WeaponFlameState.fireId(stack,p);var def=id==null?null:FireCatalog.id(id.toString());if(def==null)return;var a=WeaponFlameResources.anchor(stack);if(a==null)return;
  try{var r=recipe(def.tier(),WeaponFlameState.level(stack));if(r==null)return;if(buffers instanceof MultiBufferSource.BufferSource b)b.endBatch();var axis=a.tipAnchor().subtract(a.hiltAnchor()).normalize();var h=a.hiltAnchor();var side=a.widthAxis();if(VolumeGpu.renderWeapon(r,new Matrix4f(pose.last().pose()),new float[]{(float)h.x,(float)h.y,(float)h.z},new float[]{(float)axis.x,(float)axis.y,(float)axis.z},new float[]{(float)side.x,(float)side.y,(float)side.z},a.length(),Math.max(a.bladeWidth(),a.bladeThickness())*.5f)){weaponPasses.merge(p.getId(),1,Integer::sum);weaponDraws++;}}catch(Exception ex){fail(ex);}
 }
 @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut e){world=null;reset();}
 private Client(){}
}
