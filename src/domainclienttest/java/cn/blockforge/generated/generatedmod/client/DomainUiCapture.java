package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.CultivationData;
import cn.blockforge.generated.generatedmod.items.CultivationLogic;
import com.google.gson.GsonBuilder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Real Screen.renderWithTooltip with a test pointer; not an OS mouse automation. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT)
public final class DomainUiCapture {
    private static final boolean ENABLED=Boolean.getBoolean("wuxingxiantu.domainUiCapture")&&!Boolean.getBoolean("wuxingxiantu.jeiCatalogCapture")&&!Boolean.getBoolean("wuxingxiantu.alchemyReservationCapture");
    private static final boolean ANVIL=Boolean.getBoolean("wuxingxiantu.anvilUiCapture");
    private static final boolean DETAILS=Boolean.getBoolean("wuxingxiantu.detailsUiCapture");
    private static final boolean FOLLOWUP=DETAILS&&Boolean.getBoolean("wuxingxiantu.alchemyFollowupCapture");
    private static final Path OUTPUT=Path.of(System.getProperty("wuxingxiantu.domainUiOutput",DETAILS?"../../reports/ui-details-20261014":ANVIL?"../../reports/anvil-blue-ui-client":"../../reports/domain-ui-client"));
    private static final String[] NAMES=DETAILS?new String[]{"settings-mana-loss","root-fire-details","mana-hover","meditation-rates","inventory-mana","mutated-roots","tracking-disabled","alchemy-preview-grey","alchemy-complete","alchemy-collected","inventory-physique","inventory-heart","inventory-domain","inventory-fire"}:ANVIL?new String[]{"anvil-empty","anvil-rename","anvil-repair","anvil-poor","anvil-template-smithing","anvil-night-north","anvil-night-east","anvil-night-south","anvil-night-west"}:new String[]{"gold","wood","water","fire","earth","thunder","exotic","fire-buffed","earth-narrow"};
    private static final int[] ROOTS={0,1,2,3,4,1,3,3,4};
    private static final List<Map<String,Object>> ROWS=new ArrayList<>();
    private static final List<String> FAILURES=new ArrayList<>();
    private static final long START=System.nanoTime();
    private static int captureTotal(){return NAMES.length+(FOLLOWUP?AlchemyFollowupUi.NAMES.length:0);}
    @SubscribeEvent public static void preserveCaptureSize(TickEvent.ClientTickEvent event){
        var mc=Minecraft.getInstance();if(ENABLED&&opened&&event.phase==TickEvent.Phase.START&&(mc.getMainRenderTarget().width!=1280||mc.getMainRenderTarget().height!=900)){
            mc.getWindow().setWidth(1280);mc.getWindow().setHeight(900);mc.resizeDisplay();
        }
    }
    private static boolean opened,prepared,finished,pending;
    private static int tick,stage;
    private static CompletableFuture<?> preparation;
    private static Object field(Object target,String name)throws Exception{for(Class<?> type=target.getClass();type!=null;type=type.getSuperclass())try{var f=type.getDeclaredField(name);f.setAccessible(true);return f.get(target);}catch(NoSuchFieldException ignored){}throw new NoSuchFieldException(name);}
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event){
        if(!ENABLED||finished||event.phase!=TickEvent.Phase.END)return;
        var mc=Minecraft.getInstance();
        try{
            Files.createDirectories(OUTPUT);mc.setWindowActive(true);mc.options.pauseOnLostFocus=false;
            if((System.nanoTime()-START)/1_000_000_000>300)throw new AssertionError("Domain UI timeout");
            if(!opened&&mc.player!=null)throw new AssertionError("Refuses to adopt existing save");
            if(!opened&&mc.getOverlay()==null&&(mc.screen instanceof net.minecraft.client.gui.screens.TitleScreen||mc.screen instanceof net.minecraft.client.gui.screens.AccessibilityOnboardingScreen)){
                opened=true;Files.deleteIfExists(OUTPUT.resolve("failure.txt"));Files.deleteIfExists(OUTPUT.resolve("complete.txt"));
                mc.getWindow().setWindowed(1280,900);mc.options.guiScale().set(2);mc.resizeDisplay();
                org.lwjgl.glfw.GLFW.glfwHideWindow(mc.getWindow().getWindow());
                mc.options.renderDistance().set(2);mc.options.simulationDistance().set(5);mc.options.framerateLimit().set(40);
                mc.getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);
                var rules=new GameRules();rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false,null);
                mc.createWorldOpenFlows().createFreshLevel("domain-ui-test-"+System.currentTimeMillis(),
                        new LevelSettings("Domain UI acceptance",GameType.SURVIVAL,false,net.minecraft.world.Difficulty.NORMAL,true,rules,WorldDataConfiguration.DEFAULT),
                        new WorldOptions(7215,false,false),net.minecraft.world.level.levelgen.presets.WorldPresets::createNormalWorldDimensions);
            }
            if(!opened||mc.player==null||mc.level==null||mc.getSingleplayerServer()==null||mc.getOverlay()!=null||mc.screen instanceof net.minecraft.client.gui.screens.ReceivingLevelScreen)return;
            if(!prepared){
                prepared=true;var id=mc.player.getUUID();preparation=mc.getSingleplayerServer().submitAsync(()->{
                    var p=mc.getSingleplayerServer().getPlayerList().getPlayer(id);p.setNoGravity(true);p.teleportTo(.5,180,.5);
                    var d=GeneratedMod.data(p);d.wuxingxiantu$setRealm(8);d.wuxingxiantu$setRootMask(31);
                    for(int root=0;root<5;root++)d.wuxingxiantu$setRootQuality(root,4);
                    d.wuxingxiantu$setThunderRoot(true);d.wuxingxiantu$setExoticRoot(true);d.wuxingxiantu$learnFire(6);d.wuxingxiantu$setEquippedFire(6);
                    d.wuxingxiantu$setMaxMana(1_000_000);d.wuxingxiantu$setAura(1_000_000);GeneratedMod.sync(p);
                });return;
            }
            if(preparation!=null){if(!preparation.isDone())return;preparation.join();preparation=null;}
            if(pending)return;tick++;
            if(tick==40)configureStage(mc);
            if(tick>=40&&(tick-40)%30==18)pending=true;
            if(tick>=40&&(tick-40)%30==0&&tick!=40)configureStage(mc);
            if(ANVIL&&stage==1&&tick==74&&mc.screen instanceof XuanlingAnvilScreen anvil)
                for(var child:anvil.children())if(child instanceof net.minecraft.client.gui.components.EditBox name&&name.getX()>(int)field(anvil,"leftPos"))name.setValue("玄灵剑");
            if(ANVIL&&stage==4&&tick==161&&mc.screen instanceof XuanlingAnvilScreen anvil){
                var search=(net.minecraft.client.gui.components.EditBox)field(anvil,"templateSearch");
                search.setValue("");anvil.mouseClicked(search.getX()+2,search.getY()+2,0);
                for(char letter:"__no_match__".toCharArray())anvil.charTyped(letter,0);
                if(!search.getValue().equals("__no_match__"))throw new AssertionError("Typing did not reach the focused template search");
                if(!((java.util.List<?>)field(anvil,"templateWidgets")).isEmpty())throw new AssertionError("Search did not remove unmatched template widgets");
                var template=((cn.blockforge.generated.generatedmod.XuanlingAnvilMenu)anvil.getMenu()).recordedTemplates().get(1);
                search.setValue(template.getTooltipLines(mc.player,net.minecraft.world.item.TooltipFlag.NORMAL).get(1).getString());
                var widgets=(java.util.List<?>)field(anvil,"templateWidgets");
                if(widgets.size()!=1)throw new AssertionError("Search did not isolate the requested template");
                ((net.minecraft.client.gui.components.Button)widgets.get(0)).onPress();
            }
            if(ANVIL&&stage==4&&tick==164&&mc.screen instanceof XuanlingAnvilScreen anvil){
                var menu=(cn.blockforge.generated.generatedmod.XuanlingAnvilMenu)anvil.getMenu();
                if(!menu.selectedTemplate().stack().is(net.minecraft.world.item.Items.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE))throw new AssertionError("Filtered template sent the wrong original index");
                ((net.minecraft.client.gui.components.EditBox)field(anvil,"templateSearch")).setValue("");
                mc.gameMode.handleInventoryButtonClick(menu.containerId,2);
            }
            if(DETAILS&&stage==6&&(tick-40)%30==10&&mc.screen instanceof CultivationMainScreen s){
                var page=(CultivationSettingsPage)field(s,"page");int bx=(int)field(page,"bodyX"),cx=(int)field(s,"contentX"),cy=(int)field(s,"contentY");float scale=(float)field(s,"pageScale");s.mouseClicked(cx+(bx+10)*scale,cy+37*scale,0);
            }
            if(ROWS.size()==captureTotal())finish();
        }catch(Throwable e){FAILURES.add(e.toString());com.mojang.logging.LogUtils.getLogger().error("DOMAIN_UI_CAPTURE",e);finish();}
    }
    private static void configureStage(Minecraft mc)throws Exception{
        if(FOLLOWUP&&stage>=NAMES.length){preparation=AlchemyFollowupUi.configure(mc,stage-NAMES.length);return;}
        if(DETAILS){
            if(stage>=7&&stage<=9){
                if(stage==9){if(mc.screen instanceof AlchemyScreen s)s.mouseClicked((int)field(s,"leftPos")+cn.blockforge.generated.generatedmod.AlchemyUiLayout.CENTER_X,(int)field(s,"topPos")+cn.blockforge.generated.generatedmod.AlchemyUiLayout.CENTER_Y,0);return;}
                var id=mc.player.getUUID();int current=stage;preparation=mc.getSingleplayerServer().submitAsync(()->{
                    var p=mc.getSingleplayerServer().getPlayerList().getPlayer(id);var pos=new net.minecraft.core.BlockPos(0,179,0);var level=p.serverLevel();
                    if(current==7){cn.blockforge.generated.generatedmod.CultivationBlock.stopMeditation(p,GeneratedMod.data(p),false);p.teleportTo(6.5,179,.5);cn.blockforge.generated.generatedmod.alchemy.FormulaKnowledge.learnAll(p);
                        for(int x=-2;x<=2;x++)for(int z=-2;z<=2;z++)for(int y=0;y<=3;y++){var state=cn.blockforge.generated.generatedmod.AlchemyFurnaceStructure.expected(x,z,y);level.setBlock(pos.offset(x,y,z),state==null?cn.blockforge.generated.generatedmod.WuXingBlocks.ALCHEMY_FURNACE_LOW.get().defaultBlockState():state,3);}
                        cn.blockforge.generated.generatedmod.AlchemyFurnaceStructure.refreshAround(level,pos);
                        var furnace=(cn.blockforge.generated.generatedmod.AlchemyFurnaceBlockEntity)level.getBlockEntity(pos);net.minecraftforge.network.NetworkHooks.openScreen(p,furnace.menuProvider(),pos);p.containerMenu.clickMenuButton(p,2);
                    }else{var furnace=(cn.blockforge.generated.generatedmod.AlchemyFurnaceBlockEntity)level.getBlockEntity(pos);furnace.startBrewing(0,1);if(!furnace.complete(cn.blockforge.generated.generatedmod.alchemy.PillData.create(cn.blockforge.generated.generatedmod.alchemy.PillCatalog.AURA,cn.blockforge.generated.generatedmod.alchemy.PillData.Grade.LOW,cn.blockforge.generated.generatedmod.alchemy.PillData.Element.WOOD,0).copyWithCount(3)))throw new AssertionError("UI result completion failed");}
                    GeneratedMod.sync(p);
                });return;
            }
            if(stage==4||stage>=10){mc.setScreen(new CultivationMainScreen(mc.player,"inventory"));var id=mc.player.getUUID();preparation=mc.getSingleplayerServer().submitAsync(()->{
                var p=mc.getSingleplayerServer().getPlayerList().getPlayer(id);var d=GeneratedMod.data(p);cn.blockforge.generated.generatedmod.CultivationBlock.stopMeditation(p,d,false);
                d.wuxingxiantu$setPhysique(cn.blockforge.generated.generatedmod.PhysiqueType.QINGLIAN);d.wuxingxiantu$setEquippedHeartMethod("yuan_hun");d.wuxingxiantu$setFireDomainEnabled(true);GeneratedMod.sync(p);
            });return;}
            if(stage==5){var id=mc.player.getUUID();preparation=mc.getSingleplayerServer().submitAsync(()->{var p=mc.getSingleplayerServer().getPlayerList().getPlayer(id);var d=GeneratedMod.data(p);d.wuxingxiantu$setThunderRootActive(true);d.wuxingxiantu$setExoticRootActive(true);GeneratedMod.sync(p);});}
            if(stage==6){var s=new CultivationMainScreen(mc.player,"settings");mc.setScreen(s);try{var page=(CultivationSettingsPage)field(s,"page");var category=CultivationSettingsPage.class.getDeclaredField("category");category.setAccessible(true);category.setInt(page,5);s.selectPage("settings");}catch(Exception e){throw new RuntimeException(e);}return;}
            if(stage==3){
                mc.setScreen(null);mc.options.hideGui=false;var id=mc.player.getUUID();
                preparation=mc.getSingleplayerServer().submitAsync(()->{
                    var p=mc.getSingleplayerServer().getPlayerList().getPlayer(id);var d=GeneratedMod.data(p);
                    var pos=new net.minecraft.core.BlockPos(0,179,0);p.serverLevel().setBlock(pos,cn.blockforge.generated.generatedmod.WuXingBlocks.MEDITATION_PLATFORM.get().defaultBlockState(),3);
                    d.wuxingxiantu$setCultivation(0);p.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
                    cn.blockforge.generated.generatedmod.CultivationBlock.startMeditation(p,d,pos,2);GeneratedMod.sync(p);
                });return;
            }
            mc.setScreen(new CultivationMainScreen(mc.player,stage==0?"settings":"vision"));
            if(stage==0){var id=mc.player.getUUID();preparation=mc.getSingleplayerServer().submitAsync(()->{
                var p=mc.getSingleplayerServer().getPlayerList().getPlayer(id);var mob=net.minecraft.world.entity.EntityType.HUSK.create(p.serverLevel());mob.setPos(p.getX()+1,p.getY(),p.getZ());mob.setNoAi(true);mob.setNoGravity(true);p.serverLevel().addFreshEntity(mob);
                try{var pulse=CultivationLogic.class.getDeclaredMethod("fireAura",net.minecraft.world.entity.player.Player.class,CultivationData.class,int.class);pulse.setAccessible(true);pulse.invoke(null,p,GeneratedMod.data(p),3);}catch(Exception e){throw new RuntimeException(e);}
                mob.discard();GeneratedMod.sync(p);
            });}return;
        }
        if(ANVIL){configureAnvil(mc);return;}
        if(stage==8){mc.options.guiScale().set(3);mc.resizeDisplay();}
        var id=mc.player.getUUID();int current=stage;
        preparation=mc.getSingleplayerServer().submitAsync(()->{
            var p=mc.getSingleplayerServer().getPlayerList().getPlayer(id);var d=GeneratedMod.data(p);
            d.wuxingxiantu$setThunderRootActive(current==5);d.wuxingxiantu$setExoticRootActive(current==6);
            if(current==7)p.getAttribute(Attributes.ATTACK_DAMAGE).addPermanentModifier(new AttributeModifier(UUID.fromString("e18c976c-7d73-4eb2-b1be-b79bdb85ae7a"),"UI live attack test",50,AttributeModifier.Operation.ADDITION));
            GeneratedMod.sync(p);
        });
        mc.setScreen(new CultivationMainScreen(mc.player,"vision"));
    }
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void frame(TickEvent.RenderTickEvent event){
        if(!ENABLED||finished||!pending||event.phase!=TickEvent.Phase.END)return;
        var mc=Minecraft.getInstance();if(mc.getOverlay()!=null)return;
        if(ANVIL){captureAnvil(mc);return;}
        if(DETAILS){captureDetails(mc);return;}
        if(!(mc.screen instanceof CultivationMainScreen screen))return;
        try{
            var page=(CultivationVisionPage)field(screen,"page");
            if(stage==8){var f=CultivationMainScreen.class.getDeclaredField("scroll");f.setAccessible(true);f.setInt(screen,250);}
            int cx=(int)field(screen,"contentX"),cy=(int)field(screen,"contentY"),scroll=(int)field(screen,"scroll");
            float scale=(float)field(screen,"pageScale");
            var layout=(CultivationVisionLayout)field(page,"layout");
            if(stage==8){scroll=Math.max(0,layout.domain().y()-110);var f=CultivationMainScreen.class.getDeclaredField("scroll");f.setAccessible(true);f.setInt(screen,scroll);}
            if((int)field(page,"popupMode")!=1)screen.mouseClicked(cx+(layout.domain().x()+12)*scale,cy+(layout.domain().y()+24-scroll)*scale,0);
            @SuppressWarnings("unchecked")var options=(List<AbstractWidget>)field(page,"options");
            var widget=options.get(ROOTS[stage]);int mx=(int)(cx+(widget.getX()+widget.getWidth()/2)*scale),my=(int)(cy+(widget.getY()+9-scroll)*scale);
            mc.getMainRenderTarget().bindWrite(true);
            com.mojang.blaze3d.systems.RenderSystem.setProjectionMatrix(new org.joml.Matrix4f().setOrtho(0,screen.width,screen.height,0,1000,21000),com.mojang.blaze3d.vertex.VertexSorting.ORTHOGRAPHIC_Z);
            var modelView=com.mojang.blaze3d.systems.RenderSystem.getModelViewStack();modelView.pushPose();modelView.setIdentity();modelView.translate(0,0,-11000);com.mojang.blaze3d.systems.RenderSystem.applyModelViewMatrix();
            com.mojang.blaze3d.systems.RenderSystem.clear(org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT,Minecraft.ON_OSX);
            var gui=new GuiGraphics(mc,mc.renderBuffers().bufferSource());screen.renderWithTooltip(gui,mx,my,mc.getFrameTime());gui.flush();
            int error=org.lwjgl.opengl.GL11.glGetError();if(error!=0)throw new AssertionError("GL error "+error);
            var data=GeneratedMod.data(mc.player);var text=DomainTooltips.describe(mc.player,data,ROOTS[stage]);
            String raw=text.getString();
            if(!widget.isHovered())throw new AssertionError("Real domain option not hovered");
            if(stage==6&&!raw.contains("异火领域"))throw new AssertionError("Exotic form not synchronized");
            if(stage==7&&mc.player.getAttributeValue(Attributes.ATTACK_DAMAGE)<300)throw new AssertionError("Attack modifier not synchronized");
            if(!raw.contains("下品")||!raw.contains("中品")||!raw.contains("上品")||!raw.contains("极品"))throw new AssertionError("Missing quality tiers");
            int lines=mc.font.split(text,Math.min(240,screen.width-16)).size();
            if(lines*mc.font.lineHeight+12>screen.height)throw new AssertionError("Tooltip too tall: "+lines+" lines / "+screen.height);
            try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(OUTPUT.resolve(NAMES[stage]+".png"));}
            modelView.popPose();com.mojang.blaze3d.systems.RenderSystem.applyModelViewMatrix();
            var row=new LinkedHashMap<String,Object>();row.put("name",NAMES[stage]);row.put("text",raw);row.put("attack",mc.player.getAttributeValue(Attributes.ATTACK_DAMAGE));
            row.put("manaMaximum",CultivationLogic.maxAura(data));row.put("guiWidth",screen.width);row.put("guiHeight",screen.height);row.put("hoverX",mx);row.put("hoverY",my);row.put("wrappedLines",lines);row.put("glError",error);ROWS.add(row);
            stage++;pending=false;writeReport();
        }catch(Throwable e){FAILURES.add(e.toString());com.mojang.logging.LogUtils.getLogger().error("DOMAIN_UI_FRAME",e);finish();}
    }
    private static void captureDetails(Minecraft mc){
        if(FOLLOWUP&&stage>=NAMES.length){try{ROWS.add(AlchemyFollowupUi.capture(mc,stage-NAMES.length,OUTPUT));stage++;pending=false;writeReport();}
            catch(Throwable e){FAILURES.add(e.toString());com.mojang.logging.LogUtils.getLogger().error("ALCHEMY_FOLLOWUP_UI",e);finish();}return;}
        if(stage>=7&&stage<=9){captureAlchemy(mc);return;}
        if(stage==3){try{
            var d=GeneratedMod.data(mc.player);if(mc.screen!=null||!d.wuxingxiantu$isMeditating()||!mc.player.isPassenger())throw new AssertionError("Actual meditation seat/HUD state absent");
            int error=org.lwjgl.opengl.GL11.glGetError();if(error!=0)throw new AssertionError("GL error "+error);
            try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(OUTPUT.resolve(NAMES[stage]+".png"));}
            var row=new LinkedHashMap<String,Object>();row.put("name",NAMES[stage]);row.put("detail","meditationCultivation="+CultivationLogic.meditationCultivationReward(d)+", manaRegeneration="+CultivationLogic.regeneration(d,true));row.put("glError",error);ROWS.add(row);stage++;pending=false;writeReport();
        }catch(Throwable e){FAILURES.add(e.toString());finish();}return;}
        if(!(mc.screen instanceof CultivationMainScreen screen))return;
        try{
            var page=field(screen,"page");int cx=(int)field(screen,"contentX"),cy=(int)field(screen,"contentY"),mx=0,my=0;
            var data=GeneratedMod.data(mc.player);String detail="";
            if(stage==4||stage>=10){
                int x=(int)field(screen,"leftPos")+(int)field(screen,"imageWidth")+8,y=cy+5,height=(int)field(screen,"contentH"),extra=height>=250?Math.max(0,height-246):0;
                int[] offsets={63,108,132,156,180};int row=stage==4?0:stage-9;mx=x+32;my=y+offsets[row]+(int)Math.round(extra*(row==0?2:row+3)/7.0)+8;detail=stage==4?CultivationUi.manaTooltip(data,data.getCurrentMana(),data.getMaxMana()).getString():NAMES[stage];
            }else if(stage==5){detail="both mutated roots";
                if(!CultivationLogic.isThunderForm(data)||!CultivationLogic.isExoticForm(data))throw new AssertionError("Mutated root forms not synchronized");
            }else if(stage==6){
                var playerId=mc.player.getUUID();
                boolean serverEnabled=mc.getSingleplayerServer().submit(()->((cn.blockforge.generated.generatedmod.CultivationDataImpl)GeneratedMod.data(mc.getSingleplayerServer().getPlayerList().getPlayer(playerId))).manaLossEnabled()).join();
                if(serverEnabled||((cn.blockforge.generated.generatedmod.CultivationDataImpl)data).manaLossEnabled())throw new AssertionError("Actual UI toggle did not disable server/client recorder");detail="recorder disabled on actual server/client";
            }else if(stage==0){
                var category=CultivationSettingsPage.class.getDeclaredField("category");category.setAccessible(true);category.setInt(page,5);screen.selectPage("settings");
                var rows=cn.blockforge.generated.generatedmod.balance.ManaLossLedger.read(data);
                if(rows.stream().noneMatch(row->row.getString("Source").equals("业火领域")))throw new AssertionError("Live debit source did not synchronize to settings UI");
                detail=rows.toString();
            }else{
                var vision=(CultivationVisionPage)page;var layout=(CultivationVisionLayout)field(vision,"layout");
                if(stage==1){
                    var box=layout.roots();var rootX=CultivationVisionPage.class.getDeclaredMethod("rootX",CultivationVisionLayout.Box.class,int.class);rootX.setAccessible(true);
                    var rootWidth=CultivationVisionPage.class.getDeclaredMethod("rootWidth",CultivationVisionLayout.Box.class,int.class);rootWidth.setAccessible(true);
                    mx=cx+(int)rootX.invoke(vision,box,3)+((int)rootWidth.invoke(vision,box,3)-28)/2+14;my=cy+box.y()+30;
                    var hint=CultivationVisionPage.class.getDeclaredMethod("rootHint",int.class);hint.setAccessible(true);detail=((net.minecraft.network.chat.Component)hint.invoke(vision,3)).getString();
                    if(!detail.contains("极品")||!detail.contains("灵力"))throw new AssertionError("Root details incomplete");
                }else{var box=layout.status();mx=cx+box.x()+48;my=cy+box.y()+74;detail="mana="+data.getCurrentMana()+", regeneration="+CultivationLogic.regeneration(data,data.wuxingxiantu$isMeditating());}
            }
            mc.getMainRenderTarget().bindWrite(true);com.mojang.blaze3d.systems.RenderSystem.setProjectionMatrix(new org.joml.Matrix4f().setOrtho(0,screen.width,screen.height,0,1000,21000),com.mojang.blaze3d.vertex.VertexSorting.ORTHOGRAPHIC_Z);
            var model=com.mojang.blaze3d.systems.RenderSystem.getModelViewStack();model.pushPose();model.setIdentity();model.translate(0,0,-11000);com.mojang.blaze3d.systems.RenderSystem.applyModelViewMatrix();
            com.mojang.blaze3d.systems.RenderSystem.clear(org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT,Minecraft.ON_OSX);
            var gui=new GuiGraphics(mc,mc.renderBuffers().bufferSource());screen.renderWithTooltip(gui,mx,my,mc.getFrameTime());gui.flush();int error=org.lwjgl.opengl.GL11.glGetError();if(error!=0)throw new AssertionError("GL error "+error);
            try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(OUTPUT.resolve(NAMES[stage]+".png"));}model.popPose();com.mojang.blaze3d.systems.RenderSystem.applyModelViewMatrix();
            var row=new LinkedHashMap<String,Object>();row.put("name",NAMES[stage]);row.put("detail",detail);row.put("glError",error);ROWS.add(row);stage++;pending=false;writeReport();
        }catch(Throwable e){FAILURES.add(e.toString());com.mojang.logging.LogUtils.getLogger().error("DETAILS_UI",e);finish();}
    }
    private static void captureAlchemy(Minecraft mc){
        if(!(mc.screen instanceof AlchemyScreen screen))return;
        try{
            var menu=(cn.blockforge.generated.generatedmod.AlchemyFurnaceMenu)mc.player.containerMenu;
            if(mc.level.getBlockEntity(menu.blockPos())!=menu.furnace())throw new AssertionError("UI is still reading a temporary furnace snapshot");
            if(stage==7&&menu.selectedRecipe()==null)throw new AssertionError("Selected preview recipe absent");
            if(stage==8&&!menu.furnace().hasResult())throw new AssertionError("Completed pill absent");
            if(stage==9&&menu.furnace().hasResult())throw new AssertionError("Center click did not collect result");
            mc.getMainRenderTarget().bindWrite(true);com.mojang.blaze3d.systems.RenderSystem.setProjectionMatrix(new org.joml.Matrix4f().setOrtho(0,screen.width,screen.height,0,1000,21000),com.mojang.blaze3d.vertex.VertexSorting.ORTHOGRAPHIC_Z);
            var model=com.mojang.blaze3d.systems.RenderSystem.getModelViewStack();model.pushPose();model.setIdentity();model.translate(0,0,-11000);com.mojang.blaze3d.systems.RenderSystem.applyModelViewMatrix();com.mojang.blaze3d.systems.RenderSystem.clear(org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT,Minecraft.ON_OSX);
            var gui=new GuiGraphics(mc,mc.renderBuffers().bufferSource());screen.renderWithTooltip(gui,0,0,mc.getFrameTime());gui.flush();int error=org.lwjgl.opengl.GL11.glGetError();if(error!=0)throw new AssertionError("GL error "+error);
            try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(OUTPUT.resolve(NAMES[stage]+".png"));}model.popPose();com.mojang.blaze3d.systems.RenderSystem.applyModelViewMatrix();
            var row=new LinkedHashMap<String,Object>();row.put("name",NAMES[stage]);row.put("glError",error);row.put("hasResult",menu.furnace().hasResult());row.put("capacity",menu.spiritCapacity());row.put("injectionPerSecond",cn.blockforge.generated.generatedmod.alchemy.AlchemyBalance.injectionPerSecond(menu.furnaceTier()));ROWS.add(row);stage++;pending=false;writeReport();
        }catch(Throwable e){FAILURES.add(e.toString());com.mojang.logging.LogUtils.getLogger().error("ALCHEMY_UI",e);finish();}
    }
    private static void configureAnvil(Minecraft mc){
        // Left sidebar and fullbright faces share the actual production menu/model.
        if(stage>=5){mc.setScreen(null);mc.options.hideGui=true;var id=mc.player.getUUID();float yaw=23+(stage-5)*90;
            preparation=mc.getSingleplayerServer().submitAsync(()->{
                var p=mc.getSingleplayerServer().getPlayerList().getPlayer(id);p.closeContainer();
                GeneratedMod.data(p).wuxingxiantu$setRealm(0);p.removeAllEffects();GeneratedMod.sync(p);
                p.serverLevel().setDayTime(18000);p.serverLevel().getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false,p.server);
                for(int x=-2;x<=4;x++)for(int z=-4;z<=4;z++)p.serverLevel().setBlock(new net.minecraft.core.BlockPos(x,178,z),net.minecraft.world.level.block.Blocks.DEEPSLATE.defaultBlockState(),3);
                p.teleportTo(1.5+Math.sin(Math.toRadians(yaw))*4,179.8,.5-Math.cos(Math.toRadians(yaw))*4);p.setYRot(yaw);p.setXRot(23);
            });mc.player.setYRot(yaw);mc.player.setXRot(23);return;}
        var id=mc.player.getUUID();int current=stage;
        preparation=mc.getSingleplayerServer().submitAsync(()->{
            var p=mc.getSingleplayerServer().getPlayerList().getPlayer(id);
            var pos=new net.minecraft.core.BlockPos(1,179,0);var level=p.serverLevel();
            level.setBlock(pos,cn.blockforge.generated.generatedmod.WuXingBlocks.XUANLING_ANVIL.get().defaultBlockState(),3);
            if(current==4){
                p.setShiftKeyDown(true);p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE));
                var hit=new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(pos),net.minecraft.core.Direction.UP,pos,false);p.gameMode.useItemOn(p,level,p.getMainHandItem(),net.minecraft.world.InteractionHand.MAIN_HAND,hit);
                if(cn.blockforge.generated.generatedmod.AnvilTemplateMemory.get(level).at(pos).size()!=1)throw new AssertionError("Real useItemOn did not register template");
                p.setShiftKeyDown(false);p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,net.minecraft.world.item.ItemStack.EMPTY);
                cn.blockforge.generated.generatedmod.AnvilTemplateMemory.get(level).learn(pos,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE));
                for(var template:new net.minecraft.world.item.Item[]{net.minecraft.world.item.Items.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE,net.minecraft.world.item.Items.COAST_ARMOR_TRIM_SMITHING_TEMPLATE,net.minecraft.world.item.Items.WILD_ARMOR_TRIM_SMITHING_TEMPLATE,net.minecraft.world.item.Items.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE})
                    cn.blockforge.generated.generatedmod.AnvilTemplateMemory.get(level).learn(pos,new net.minecraft.world.item.ItemStack(template));
            }
            p.closeContainer();
            net.minecraftforge.network.NetworkHooks.openScreen(p,new net.minecraft.world.SimpleMenuProvider((container,inventory,player)->
                    new cn.blockforge.generated.generatedmod.XuanlingAnvilMenu(container,inventory,net.minecraft.world.inventory.ContainerLevelAccess.create(level,pos)),
                    net.minecraft.network.chat.Component.literal("玄灵铁砧")),buffer->{buffer.writeBlockPos(pos);var templates=cn.blockforge.generated.generatedmod.AnvilTemplateMemory.get(level).at(pos);buffer.writeVarInt(templates.size());for(var item:templates)buffer.writeItem(item);});
            var menu=(cn.blockforge.generated.generatedmod.XuanlingAnvilMenu)p.containerMenu;
            if(current>0){var sword=new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_SWORD);sword.setDamageValue(160);menu.getSlot(0).set(sword);}
            if(current>=2)menu.getSlot(1).set(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_INGOT,2));
            if(current==4){menu.getSlot(0).set(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND_SWORD));menu.getSlot(1).set(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.NETHERITE_INGOT));}
            if(current==3)GeneratedMod.data(p).wuxingxiantu$setRealm(0); // Keep natural regen below the fee during this capture.
            GeneratedMod.data(p).wuxingxiantu$setAura(current==3?0:1_000_000);GeneratedMod.sync(p);menu.broadcastChanges();
        });
    }
    private static void captureAnvil(Minecraft mc){
        if(stage>=5){try{
            if(mc.screen!=null||!mc.level.getBlockState(new net.minecraft.core.BlockPos(1,179,0)).is(cn.blockforge.generated.generatedmod.WuXingBlocks.XUANLING_ANVIL.get()))throw new AssertionError("Night anvil fixture absent");
            int runeDraws=cn.blockforge.generated.generatedmod.client.anvillight.AnvilBlueLighting.runeDraws();
            if(runeDraws<1)throw new AssertionError("Night anvil rune geometry did not submit a real draw");
            int error=org.lwjgl.opengl.GL11.glGetError();if(error!=0)throw new AssertionError("GL error "+error);
            try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(OUTPUT.resolve(NAMES[stage]+".png"));}
            var row=new LinkedHashMap<String,Object>();row.put("name",NAMES[stage]);row.put("glError",error);row.put("runeDraws",runeDraws);ROWS.add(row);stage++;pending=false;writeReport();
        }catch(Throwable e){FAILURES.add(e.toString());finish();}return;}
        if(!(mc.screen instanceof XuanlingAnvilScreen screen))return;
        try{
            int panelX=(int)field(screen,"leftPos");
            for(var child:screen.children())if(child instanceof net.minecraft.client.gui.components.EditBox name&&name.getX()>panelX&&name.getX()!=panelX+62)throw new AssertionError("Rename field is not aligned with the main panel");
            mc.getMainRenderTarget().bindWrite(true);
            com.mojang.blaze3d.systems.RenderSystem.setProjectionMatrix(new org.joml.Matrix4f().setOrtho(0,screen.width,screen.height,0,1000,21000),com.mojang.blaze3d.vertex.VertexSorting.ORTHOGRAPHIC_Z);
            var model=com.mojang.blaze3d.systems.RenderSystem.getModelViewStack();model.pushPose();model.setIdentity();model.translate(0,0,-11000);com.mojang.blaze3d.systems.RenderSystem.applyModelViewMatrix();
            com.mojang.blaze3d.systems.RenderSystem.clear(org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT,Minecraft.ON_OSX);
            var gui=new GuiGraphics(mc,mc.renderBuffers().bufferSource());screen.renderWithTooltip(gui,0,0,mc.getFrameTime());gui.flush();
            var menu=(cn.blockforge.generated.generatedmod.XuanlingAnvilMenu)screen.getMenu();
            if(stage==1&&!menu.getSlot(2).getItem().getHoverName().getString().equals("玄灵剑"))throw new AssertionError("Native rename packet/result did not synchronize");
            if(stage>=2&&menu.getSlot(2).getItem().isEmpty())throw new AssertionError("Repair result absent");
            if(stage==4&&!menu.getSlot(2).getItem().is(net.minecraft.world.item.Items.NETHERITE_SWORD))throw new AssertionError("Reusable-template UI result absent");
            if(stage==3&&menu.getSlot(2).mayPickup(mc.player))throw new AssertionError("Mana-poor result was claimable");
            int error=org.lwjgl.opengl.GL11.glGetError();if(error!=0)throw new AssertionError("GL error "+error);
            try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(OUTPUT.resolve(NAMES[stage]+".png"));}
            model.popPose();com.mojang.blaze3d.systems.RenderSystem.applyModelViewMatrix();
            var row=new LinkedHashMap<String,Object>();row.put("name",NAMES[stage]);row.put("glError",error);row.put("manaCost",menu.manaCost());row.put("result",menu.getSlot(2).getItem().getHoverName().getString());row.put("mayPickup",menu.getSlot(2).mayPickup(mc.player));ROWS.add(row);
            stage++;pending=false;writeReport();
        }catch(Throwable e){FAILURES.add(e.toString());com.mojang.logging.LogUtils.getLogger().error("ANVIL_UI",e);finish();}
    }
    private static void writeReport()throws Exception{var report=new LinkedHashMap<String,Object>();report.put("test",ANVIL?"real-server-anvil-blue-ui":"real-domain-ui-selection-hover");report.put("input","Test pointer supplied to real Screen.renderWithTooltip, no OS cursor injection");report.put("captures",ROWS);report.put("failures",FAILURES);Files.writeString(OUTPUT.resolve("captures.json"),new GsonBuilder().setPrettyPrinting().create().toJson(report));}
    private static void finish(){if(finished)return;finished=true;try{Files.createDirectories(OUTPUT);writeReport();Files.writeString(OUTPUT.resolve(FAILURES.isEmpty()?"complete.txt":"failure.txt"),FAILURES.isEmpty()?"All quality hover captures passed\n":String.join("\n",FAILURES));}catch(Exception ignored){}Minecraft.getInstance().stop();}
}
