package cn.blockforge.generated.generatedmod.client.firecoil;

import cn.blockforge.generated.generatedmod.*;
import cn.blockforge.generated.generatedmod.client.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.*;
import net.minecraftforge.registries.ForgeRegistries;
import java.nio.file.*;
import java.lang.reflect.*;
import java.util.*;

/** Opt-in checks inside the disposable VFX world, excluded from the release. */
final class SettingsAndInfuserChecks {
 private static final String[] SWORDS={"dark_iron_sword","mingjue_sword","qingming_sword","jixiao_sword","wuxiang_sword","guiyuan_sword","tianque_sword"};
 static void infuser(ServerPlayer player,Path out)throws Exception{
  if(!net.minecraftforge.fml.ModList.get().isLoaded("enchantinginfuser"))return;
  Class<?> type=Class.forName("fuzs.enchantinginfuser.world.level.block.InfuserBlock$InfuserType"),menus=Class.forName("fuzs.enchantinginfuser.world.inventory.InfuserMenu");
  Method create=menus.getMethod("create",type,int.class,Inventory.class,net.minecraft.world.Container.class,ContainerLevelAccess.class);
  Method mayEnchant=menus.getDeclaredMethod("mayEnchantStack",ItemStack.class);mayEnchant.setAccessible(true);
  List<Map<String,Object>> results=new ArrayList<>();
  for(String sword:SWORDS)for(Object kind:type.getEnumConstants()){
   ItemStack stack=new ItemStack(Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(new ResourceLocation("wuxingxiantu",sword))));
   if(!stack.isEnchantable())throw new AssertionError("Fresh sword rejected "+sword);
   stack.enchant(SpiritEnchantment.EXOTIC_FIRE_WRAP.get(),5);
   if(stack.isEnchantable())throw new AssertionError("Vanilla reroll became allowed "+sword);
   if(!stack.getItem().isEnchantable(stack))throw new AssertionError("Item capability rejects enchanted sword "+sword);
   var container=new SimpleContainer(1);
   var menu=(AbstractContainerMenu)create.invoke(null,kind,99,player.getInventory(),container,ContainerLevelAccess.create(player.serverLevel(),new BlockPos(0,128,0)));
   Object config=menus.getField("config").get(menu);Field modify=config.getClass().getField("allowModifyingEnchantments");Object original=modify.get(config);
   Object allowAll=Arrays.stream(modify.getType().getEnumConstants()).filter(v->v.toString().equals("ALL")).findFirst().orElseThrow();modify.set(config,allowAll);
   if(!(Boolean)mayEnchant.invoke(menu,stack))throw new AssertionError("Infuser gate rejected "+kind+"/"+sword);
   container.setItem(0,stack);menu.slotsChanged(container);
   var available=(Map<?,?>)menus.getMethod("getValidEnchantments").invoke(menu);
   if(!available.containsKey(Enchantments.SHARPNESS)||!available.containsKey(SpiritEnchantment.EXOTIC_FIRE_WRAP.get()))throw new AssertionError("Missing actual infuser choices "+kind+"/"+sword+": "+available);
   if(EnchantmentHelper.getItemEnchantmentLevel(SpiritEnchantment.EXOTIC_FIRE_WRAP.get(),stack)!=5)throw new AssertionError("Existing enchant lost");
   results.add(Map.of("sword",sword,"infuser",kind.toString(),"accepted",true,"choices",available.size(),"existingWrapLevel",5,"vanillaRerollBlocked",true));
   modify.set(config,original);
  }
  Files.writeString(out.resolve("infuser-compatibility.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(Map.of("passed",true,"cases",results,"scope","Actual loaded Enchanting Infuser menu eligibility and options on disposable stacks; does not spend player XP or edit the user's items")));
 }
 private static Field field(Class<?> owner,String name)throws Exception{Field f=owner.getDeclaredField(name);f.setAccessible(true);return f;}
 static void settings(Path out)throws Exception{
  var mc=Minecraft.getInstance();var screen=new CultivationMainScreen(mc.player,"settings");mc.setScreen(screen);
  var projection=new org.joml.Matrix4f(com.mojang.blaze3d.systems.RenderSystem.getProjectionMatrix());var sorting=com.mojang.blaze3d.systems.RenderSystem.getVertexSorting();
  var model=com.mojang.blaze3d.systems.RenderSystem.getModelViewStack();model.pushPose();model.setIdentity();model.translate(0,0,-11000);com.mojang.blaze3d.systems.RenderSystem.applyModelViewMatrix();
  com.mojang.blaze3d.systems.RenderSystem.setProjectionMatrix(new org.joml.Matrix4f().setOrtho(0,mc.getWindow().getGuiScaledWidth(),mc.getWindow().getGuiScaledHeight(),0,1000,21000),com.mojang.blaze3d.vertex.VertexSorting.ORTHOGRAPHIC_Z);
  mc.getMainRenderTarget().bindWrite(true);org.lwjgl.opengl.GL11.glDisable(org.lwjgl.opengl.GL11.GL_SCISSOR_TEST);com.mojang.blaze3d.systems.RenderSystem.disableDepthTest();com.mojang.blaze3d.systems.RenderSystem.depthMask(false);
  String[] categories={"界面布局","御剑飞行","遁光","战斗特效","灵力损耗"};
  List<Map<String,Object>> results=new ArrayList<>();
  for(String category:categories){
   var page=(CultivationSettingsPage)field(CultivationMainScreen.class,"page").get(screen);
   for(var child:List.copyOf(page.children()))if(child instanceof AbstractWidget w&&w.getMessage().getString().equals(category)&&w instanceof CultivationUi.Tab){((CultivationUi.Tab)w).onPress();break;}
   page=(CultivationSettingsPage)field(CultivationMainScreen.class,"page").get(screen);
   List<AbstractWidget> controls=new ArrayList<>();List<String> labels=new ArrayList<>();
   for(var child:page.children())if(child instanceof AbstractWidget w&&!(w instanceof CultivationUi.Tab)){controls.add(w);labels.add(w.getMessage().getString());}
   controls.sort(Comparator.comparingInt(AbstractWidget::getY));
   for(int i=1;i<controls.size();i++)if(controls.get(i-1).getY()+controls.get(i-1).getHeight()>controls.get(i).getY())throw new AssertionError("Overlapping settings "+category);
   if(category.equals("遁光")&&labels.stream().noneMatch(s->s.contains("遁光第一人称特效")))throw new AssertionError("Missing moved flight control");
   boolean firstPersonControl=labels.stream().anyMatch(s->s.contains("第一人称显示异火缠绕"));
   if(firstPersonControl!=category.equals("战斗特效"))throw new AssertionError("First-person fire control in wrong category: "+category);
   boolean thirdPersonControl=labels.stream().anyMatch(s->s.contains("异火缠绕第三人称近景优化"));
   if(thirdPersonControl!=category.equals("战斗特效"))throw new AssertionError("Third-person optimization in wrong category: "+category);
   boolean damageControl=labels.stream().anyMatch(s->s.startsWith("伤害非敌对生物："));
   if(damageControl!=category.equals("战斗特效"))throw new AssertionError("Fire damage preference in wrong category: "+category);
   if(category.equals("战斗特效")){
    if(page.contentHeight(300,100)!=274)throw new AssertionError("Combat sections do not reserve their expected content height");
    Map<String,Integer> positions=Map.of("灵力御体特效",28,"异火缠绕特效",78,"第一人称显示异火缠绕",106,"异火缠绕第一人称优化",134,"异火缠绕第三人称近景优化",162);
    for(var expected:positions.entrySet()){
     var widget=controls.stream().filter(v->v.getMessage().getString().equals(expected.getKey())).findFirst().orElseThrow(()->new AssertionError("Missing combat setting "+expected.getKey()));
     if(widget.getY()!=expected.getValue())throw new AssertionError("Combat section position mismatch "+expected.getKey()+": "+widget.getY());
    }
    if(!HudConfig.EXOTIC_FIRE_THIRD_PERSON_OPTIMIZATION.getDefault())throw new AssertionError("Third-person close range optimization should default on");
    var optimization=(CultivationUi.Toggle)controls.stream().filter(v->v.getMessage().getString().equals("异火缠绕第三人称近景优化")).findFirst().orElseThrow();
    boolean original=HudConfig.EXOTIC_FIRE_THIRD_PERSON_OPTIMIZATION.get();
    optimization.onPress();if(HudConfig.EXOTIC_FIRE_THIRD_PERSON_OPTIMIZATION.get()==original)throw new AssertionError("Third-person toggle action did not change config");
    optimization.onPress();if(HudConfig.EXOTIC_FIRE_THIRD_PERSON_OPTIMIZATION.get()!=original)throw new AssertionError("Third-person toggle action did not restore config");
    var targeting=(CultivationUi.Action)controls.stream().filter(v->v.getMessage().getString().startsWith("伤害非敌对生物：")).findFirst().orElseThrow();
    if(targeting.getY()!=216)throw new AssertionError("Fire damage preference is not in its separate section");
    var combatData=(CultivationDataImpl)GeneratedMod.data(mc.player);boolean originalTargeting=combatData.exoticFireHurtsNonHostile();
    targeting.onPress();if(combatData.exoticFireHurtsNonHostile()==originalTargeting)throw new AssertionError("Damage preference click did not update local state");
    if(!targeting.getMessage().getString().equals("伤害非敌对生物："+(!originalTargeting?"开启":"关闭")))throw new AssertionError("Damage preference label did not follow local state");
    targeting.onPress();if(combatData.exoticFireHurtsNonHostile()!=originalTargeting)throw new AssertionError("Damage preference click did not restore original state");
    if(!targeting.getMessage().getString().equals("伤害非敌对生物："+(originalTargeting?"开启":"关闭")))throw new AssertionError("Damage preference label did not restore");
   }
   if(!category.equals("遁光")&&labels.stream().anyMatch(s->s.contains("遁光")))throw new AssertionError("Flight setting leaked "+category);
   if(!category.equals("御剑飞行")&&labels.stream().anyMatch(s->s.contains("御剑")||s.contains("剑迹")||s.contains("横滚")))throw new AssertionError("Sword setting leaked "+category);
   var g=new GuiGraphics(mc,mc.renderBuffers().bufferSource());screen.render(g,0,0,0);g.flush();
   String filename="settings-"+results.size()+".png";
   try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(out.resolve(filename));}
   Map<String,Object> rendered=new LinkedHashMap<>();rendered.put("category",category);rendered.put("labels",labels);rendered.put("noOverlap",true);rendered.put("screenshot",filename);
   if(category.equals("战斗特效")){
    int cx=field(CultivationMainScreen.class,"contentX").getInt(screen),cy=field(CultivationMainScreen.class,"contentY").getInt(screen);
    int cw=field(CultivationMainScreen.class,"contentW").getInt(screen),ch=field(CultivationMainScreen.class,"contentH").getInt(screen);
    Field scrollField=field(CultivationMainScreen.class,"scroll");int previousScroll=scrollField.getInt(screen);
    if(!screen.mouseScrolled(cx+cw*.5,cy+ch*.5,-100))throw new AssertionError("Combat page rejected real scroll input");
    int scroll=scrollField.getInt(screen);float scale=field(CultivationMainScreen.class,"pageScale").getFloat(screen);
    var targeting=controls.stream().filter(v->v.getMessage().getString().startsWith("伤害非敌对生物：")).findFirst().orElseThrow();
    double top=cy+(targeting.getY()-scroll)*scale,bottom=top+targeting.getHeight()*scale;
    if(top<cy-.01||bottom>cy+ch+.01)throw new AssertionError("Damage preference is outside scrolled viewport: "+top+".."+bottom+" versus "+cy+".."+(cy+ch));
    var bottomGraphics=new GuiGraphics(mc,mc.renderBuffers().bufferSource());screen.render(bottomGraphics,0,0,0);bottomGraphics.flush();
    String damageShot="settings-combat-damage.png";
    try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(out.resolve(damageShot));}
    rendered.put("damagePreferenceScreenshot",damageShot);rendered.put("damagePreferenceVisible",true);rendered.put("scrolledByActualInput",true);rendered.put("scrollAtDamageCapture",scroll);
    scrollField.setInt(screen,previousScroll);
   }
   results.add(rendered);
  }
  var data=(CultivationDataImpl)GeneratedMod.data(mc.player);var original=data.manaLossRows();boolean enabled=data.manaLossEnabled();
  var seeded=new net.minecraft.nbt.ListTag();for(int i=0;i<3;i++){var row=new net.minecraft.nbt.CompoundTag();row.putString("Source","境界御空");row.putLong("At",System.currentTimeMillis()-5000);row.putFloat("Amount",303);seeded.add(row);}
  data.receiveManaLossRows(seeded);data.setManaLossEnabled(true);screen.selectPage("settings");
  var onGraphics=new GuiGraphics(mc,mc.renderBuffers().bufferSource());screen.render(onGraphics,0,0,0);onGraphics.flush();
  try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(out.resolve("settings-ledger-on-legacy.png"));}
  data.setManaLossEnabled(false);screen.selectPage("settings");
  var offGraphics=new GuiGraphics(mc,mc.renderBuffers().bufferSource());screen.render(offGraphics,0,0,0);offGraphics.flush();
  try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(out.resolve("settings-ledger-off-hidden.png"));}
  data.receiveManaLossRows(original);data.setManaLossEnabled(enabled);
  mc.setScreen(null);model.popPose();com.mojang.blaze3d.systems.RenderSystem.applyModelViewMatrix();com.mojang.blaze3d.systems.RenderSystem.setProjectionMatrix(projection,sorting);
  Files.writeString(out.resolve("settings-layout.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(Map.of("passed",true,"categories",results,"scope","Actual production widgets, tab action handlers and render method; isolated client")));
 }
}
