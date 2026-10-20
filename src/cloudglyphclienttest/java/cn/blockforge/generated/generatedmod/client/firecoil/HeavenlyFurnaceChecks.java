package cn.blockforge.generated.generatedmod.client.firecoil;
import cn.blockforge.generated.generatedmod.*;
import cn.blockforge.generated.generatedmod.client.*;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.nbt.*;
import net.minecraft.world.item.*;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.*;
import java.nio.file.*;
import java.util.*;
final class HeavenlyFurnaceChecks {
    private static int phase=-1,ticks;
    private static java.util.concurrent.CompletableFuture<?> pending;
    private static final boolean FORGE=Boolean.getBoolean("wuxingxiantu.worldForgeQa");
    private static final int[] AGES=FORGE?new int[]{12,70,110,170,250,350,450,350}:new int[]{12,70,110,170,250,500,1380,500};
    private static Path output;
    private static boolean shot;
    private static float beforeMana;
    private static final List<Map<String,Object>> LIVE=new ArrayList<>();
    @SuppressWarnings("unchecked") static boolean advance(Path out)throws Exception{
        var mc=Minecraft.getInstance();output=out;mc.options.setCameraType(phase==2+AGES.length-1?CameraType.FIRST_PERSON:CameraType.THIRD_PERSON_BACK);mc.options.hideGui=false;
        if(phase==-1){
            run(out);phase=0;ticks=0;
            pending=mc.getSingleplayerServer().submit(()->{
                var p=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());p.getInventory().clearContent();p.teleportTo(.5,129,.5);p.setYRot(0);p.setXRot(-12);p.setNoGravity(true);p.getAbilities().flying=true;p.onUpdateAbilities();
                var slots=new net.minecraft.world.entity.EquipmentSlot[]{net.minecraft.world.entity.EquipmentSlot.HEAD,net.minecraft.world.entity.EquipmentSlot.CHEST,net.minecraft.world.entity.EquipmentSlot.LEGS,net.minecraft.world.entity.EquipmentSlot.FEET};
                var armor=new net.minecraft.world.item.Item[]{Items.DIAMOND_HELMET,Items.DIAMOND_CHESTPLATE,Items.DIAMOND_LEGGINGS,Items.DIAMOND_BOOTS};
                for(int i=0;i<4;i++){var stack=new ItemStack(armor[i]);stack.enchant(SpiritEnchantment.SPIRIT_GUARD.get(),4);p.setItemSlot(slots[i],stack);}
                if(Boolean.getBoolean("wuxingxiantu.heavenlySmallWorld")){
                    var key=net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION,new net.minecraft.resources.ResourceLocation("wuxingxiantu:world_1"));
                    var level=p.server.getLevel(key);if(level==null)throw new AssertionError("Real creation small-world dimension not loaded");
                    try{
                        var manager=Class.forName("cn.blockforge.wuxingchuangshi.creation.WorldManager");
                        var scroll=net.minecraft.core.registries.BuiltInRegistries.ITEM.get(new net.minecraft.resources.ResourceLocation("wuxingchuangshi:shanhe_sheji_tu"));
                        if(!(Boolean)manager.getMethod("unlockWorld",net.minecraft.server.level.ServerPlayer.class,ItemStack.class).invoke(null,p,new ItemStack(scroll)))throw new AssertionError("Cannot unlock actual small world");
                        manager.getMethod("enterWorld",net.minecraft.server.level.ServerPlayer.class,boolean.class).invoke(null,p,true);
                    }catch(Exception ex){throw new RuntimeException(ex);}
                    if(!p.level().dimension().equals(key))throw new AssertionError("Official creation travel did not enter world_1");p.connection.teleport(.5,60,.5,0,-20);
                }
                for(int x=-8;x<=8;x++)for(int z=-8;z<=8;z++)p.level().setBlock(new net.minecraft.core.BlockPos(x,(int)p.getY()-1,z),net.minecraft.world.level.block.Blocks.STONE_BRICKS.defaultBlockState(),3);
                var d=GeneratedMod.data(p);d.wuxingxiantu$setRealm(8);d.wuxingxiantu$learnDivineArt(cn.blockforge.generated.generatedmod.alchemy.HeavenlyFurnace.ID);d.wuxingxiantu$setEquippedFire(1);d.wuxingxiantu$setAura(1000000);beforeMana=d.wuxingxiantu$getAura();
                cn.blockforge.generated.generatedmod.alchemy.FormulaKnowledge.learnAll(p);
                var recipe=p.level().getRecipeManager().byKey(new net.minecraft.resources.ResourceLocation(FORGE?"wuxingxiantu:refining_dark_iron_sword":"wuxingxiantu:alchemy_aura_pill_low")).orElseThrow();
                var counts=recipe instanceof RefiningRecipe r?r.counts():((AlchemyRecipe)recipe).counts();
                for(int i=0;i<recipe.getIngredients().size();i++)p.getInventory().setItem(30+i,recipe.getIngredients().get(i).getItems()[0].copyWithCount(counts.get(i)));
                GeneratedMod.sync(p);if(!cn.blockforge.generated.generatedmod.alchemy.HeavenlyFurnace.open(p))throw new AssertionError("Real server cannot open selector");
            });return false;
        }
        mc.player.setYRot(0);mc.player.yRotO=0;mc.player.setXRot(-20);mc.player.xRotO=-20;mc.player.noPhysics=true;mc.player.setNoGravity(true);mc.player.getAbilities().flying=true;
        if(pending!=null){if(!pending.isDone())return false;pending.join();pending=null;}
        if(phase==0){
            if(Boolean.getBoolean("wuxingxiantu.heavenlySmallWorld")&&!mc.level.dimension().location().toString().equals("wuxingxiantu:world_1")){if(++ticks>120)throw new AssertionError("Client did not enter actual small world: "+mc.level.dimension());return false;}
            if(++ticks<30)return false;
            if(Boolean.getBoolean("wuxingxiantu.heavenlySmallWorld")&&ticks==30){
                pending=mc.getSingleplayerServer().submit(()->{
                    var p=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());p.connection.teleport(.5,70,.5,0,-20);p.setNoGravity(true);p.getAbilities().flying=true;
                    for(int x=-8;x<=8;x++)for(int z=-8;z<=8;z++)p.level().setBlock(new net.minecraft.core.BlockPos(x,69,z),net.minecraft.world.level.block.Blocks.STONE_BRICKS.defaultBlockState(),3);
                });return false;
            }
            if(ticks<45)return false;
            if(!(mc.screen instanceof HeavenlyFurnaceScreen)){
                // The outer capture harness closes test screens each tick; the packet was still processed.
                var p=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
                var offers=(Map<UUID,?>)field(cn.blockforge.generated.generatedmod.alchemy.HeavenlyFurnace.class,"OFFERS").get(null);Object offer=offers.get(p.getUUID());if(offer==null)return false;
                var accessor=offer.getClass().getDeclaredMethod("token");accessor.setAccessible(true);UUID token=(UUID)accessor.invoke(offer);
                Network.CHANNEL.sendToServer(new cn.blockforge.generated.generatedmod.network.packet.HeavenlyFurnaceStartPacket(token,new net.minecraft.resources.ResourceLocation(FORGE?"wuxingxiantu:refining_dark_iron_sword":"wuxingxiantu:alchemy_aura_pill_low")));
            }else{
                var screen=(HeavenlyFurnaceScreen)mc.screen;var filtered=(List<CompoundTag>)field(screen.getClass(),"filtered").get(screen);int index=0;while(!filtered.get(index).getString("Recipe").equals(FORGE?"wuxingxiantu:refining_dark_iron_sword":"wuxingxiantu:alchemy_aura_pill_low"))index++;
                Network.CHANNEL.sendToServer(new cn.blockforge.generated.generatedmod.network.packet.HeavenlyFurnaceStartPacket((UUID)field(screen.getClass(),"token").get(screen),new net.minecraft.resources.ResourceLocation(filtered.get(index).getString("Recipe"))));mc.setScreen(null);
            }
            phase=1;ticks=0;return false;
        }
        if(phase==1){
            var effects=(Map<UUID,CompoundTag>)field(HeavenlyFurnaceVisuals.class,"EFFECTS").get(null);if(!effects.containsKey(mc.player.getUUID())){if(++ticks>160){var p=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());throw new AssertionError("Actual start handshake failed: serverActive="+cn.blockforge.generated.generatedmod.alchemy.HeavenlyFurnace.active(p)+",serverDim="+p.level().dimension()+",pos="+p.position()+",clientDim="+mc.level.dimension()+",ingredients="+p.getInventory().items.stream().mapToInt(ItemStack::getCount).sum());}return false;}
            phase=2;ticks=0;return false;
        }
        int stage=phase-2;
        if(stage<AGES.length){
            if(ticks==0){shot=false;pending=mc.getSingleplayerServer().submit(()->{
                var p=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());try{
                    var running=(Map<UUID,CompoundTag>)field(cn.blockforge.generated.generatedmod.alchemy.HeavenlyFurnace.class,"RUNNING").get(null);var saved=running.get(p.getUUID());if(saved==null)throw new AssertionError("Live cast cancelled unexpectedly");saved.putLong("Start",p.level().getGameTime()-AGES[stage]);
                    Network.CHANNEL.send(net.minecraftforge.network.PacketDistributor.PLAYER.with(()->p),new cn.blockforge.generated.generatedmod.network.packet.HeavenlyFurnaceStatePacket(cn.blockforge.generated.generatedmod.alchemy.HeavenlyFurnace.visual(saved)));
                }catch(Exception ex){throw new RuntimeException(ex);}
            });ticks++;return false;}
            if(++ticks>=15&&shot){phase++;ticks=0;}return false;
        }
        if(stage==AGES.length){
            pending=mc.getSingleplayerServer().submit(()->{
                var p=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());try{
                    var running=(Map<UUID,CompoundTag>)field(cn.blockforge.generated.generatedmod.alchemy.HeavenlyFurnace.class,"RUNNING").get(null);var saved=running.get(p.getUUID());saved.putLong("Start",p.level().getGameTime()-saved.getInt("Duration"));
                    cn.blockforge.generated.generatedmod.alchemy.HeavenlyFurnace.tick(new net.minecraftforge.event.TickEvent.PlayerTickEvent(net.minecraftforge.event.TickEvent.Phase.END,p));
                    if(cn.blockforge.generated.generatedmod.alchemy.HeavenlyFurnace.active(p)||p.getInventory().items.stream().mapToInt(ItemStack::getCount).sum()!=1)throw new AssertionError("Real cast completion did not grant exactly one outcome");
                }catch(Exception ex){throw new RuntimeException(ex);}
            });phase++;return false;
        }
        var owned=(ReusableNativeMesh)field(SpiritGuardArmorRenderer.class,"POST_MESH").get(null);
        if(TrailShaderBridge.shaderPackDetected()&&(owned.uploads==0||owned.allocations>2))throw new AssertionError("Actual armor post mesh not reused: "+owned.allocations+" / "+owned.uploads);
        Files.writeString(out.resolve("heavenly-live.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(Map.of("passed",true,"actualNetworkStart",true,"actualOutcomeCount",1,"guardUploads",owned.uploads,"guardAllocations",owned.allocations,"guardNativeBytes",owned.bytes(),"shots",LIVE)));return true;
    }
    @net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=net.minecraftforge.api.distmarker.Dist.CLIENT)
    public static class Capture {
        @net.minecraftforge.eventbus.api.SubscribeEvent public static void frame(net.minecraftforge.event.TickEvent.RenderTickEvent e)throws Exception{
            if(!Boolean.getBoolean("wuxingxiantu.heavenlyFurnaceQa")||e.phase!=net.minecraftforge.event.TickEvent.Phase.END||phase<2||phase>=2+AGES.length||ticks<10||shot)return;
            var mc=Minecraft.getInstance();var file=output.resolve("heavenly-world-age"+AGES[phase-2]+(mc.options.getCameraType()==CameraType.FIRST_PERSON?"-fp":"")+".png");int firePixels=0;
            var palm=HeavenlyFurnacePose.palm(mc.player.getUUID(),net.minecraft.world.phys.Vec3.ZERO);
            if(palm.equals(net.minecraft.world.phys.Vec3.ZERO)||palm.distanceTo(mc.player.position())>2.5)throw new AssertionError("Rendered palm is missing or detached: "+palm+" player="+mc.player.position());
            var left=HeavenlyFurnacePose.palm(mc.player.getUUID(),true,net.minecraft.world.phys.Vec3.ZERO);
            if(left.equals(net.minecraft.world.phys.Vec3.ZERO)||left.distanceTo(mc.player.position())>2.5||left.distanceTo(palm)<.2)throw new AssertionError("Both palm anchors must remain distinct and attached");
            var tag=HeavenlyFurnaceVisuals.active(mc.player.getUUID());
            var camera=mc.gameRenderer.getMainCamera();var direction=cn.blockforge.generated.generatedmod.alchemy.HeavenlyFurnace.fireballCenter(tag).subtract(camera.getPosition()).normalize();
            var look=camera.getLookVector();double tracking=direction.dot(new net.minecraft.world.phys.Vec3(look.x(),look.y(),look.z()));
            if(tracking<.999)throw new AssertionError("Camera does not track the central flame: "+tracking);
            try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){
                image.writeToFile(file);for(int y=0;y<image.getHeight()*.55;y++)for(int x=image.getWidth()/4;x<image.getWidth()*3/4;x++){
                    int pixel=image.getPixelRGBA(x,y),r=pixel&255,g=pixel>>8&255,b=pixel>>16&255;if(r>60&&r>g*1.6&&r>b*1.4)firePixels++;
                }
            }
            if(AGES[phase-2]==500&&firePixels<200)throw new AssertionError("Fireball missing from final small-world composition: "+firePixels);
            int error=GL11.glGetError();if(error!=0)throw new AssertionError("Live GL error "+error);
            LIVE.add(Map.of("age",AGES[phase-2],"file",file.toString(),"gl",error,"firePixels",firePixels,"palmSeparation",left.distanceTo(palm),"cameraAlignment",tracking));shot=true;
        }
    }
    private static java.lang.reflect.Field field(Class<?> c,String n)throws Exception{var f=c.getDeclaredField(n);f.setAccessible(true);return f;}
    static void run(Path out)throws Exception{
        NativeMemoryChecks.run(out);
        if(Boolean.getBoolean("wuxingxiantu.equipmentCraftQa"))EquipmentWorkstationChecks.run(out);
        var mc=Minecraft.getInstance();var results=new ArrayList<Map<String,Object>>();
        var zero=net.minecraft.world.phys.Vec3.ZERO;var destination=cn.blockforge.generated.generatedmod.alchemy.HeavenlyFurnace.fireballCenter(zero,0);
        var sample=HeavenlyFurnaceVisuals.materialPosition(zero,destination,0,90,0,3);
        if(HeavenlyFurnaceVisuals.gatherProgress(90,0,3)<=0||HeavenlyFurnaceVisuals.gatherProgress(90,1,3)!=0||sample.y<=1.7)throw new AssertionError("Materials do not launch sequentially upwards");
        if(HeavenlyFurnaceVisuals.materialPosition(zero,destination,0,239,2,3).distanceTo(destination)>.001)throw new AssertionError("Final material does not reach forward fireball");
        if(HeavenlyFurnaceVisuals.materialPosition(zero,destination,0,12,1,3).distanceTo(HeavenlyFurnaceVisuals.materialPosition(zero,destination,0,35,1,3))<.5)throw new AssertionError("Orbit phase has no motion");
        Files.writeString(out.resolve("material-choreography.json"),"{\"passed\":true,\"orbitTicks\":60,\"gatherTicks\":240,\"sequentialLaunch\":true,\"forwardAnchor\":true}");
        var tag=new CompoundTag();tag.putString("Kind","open");tag.putUUID("Token",UUID.randomUUID());var rows=new ListTag();
        for(var r:mc.level.getRecipeManager().getAllRecipesFor(cn.blockforge.generated.generatedmod.registry.ModRecipes.ALCHEMY_TYPE.get())){
            var row=new CompoundTag();row.putString("Recipe",r.getId().toString());row.put("Result",r.getResultItem(mc.level.registryAccess()).save(new CompoundTag()));row.putString("Reason",rows.size()%2==0?"":"背包材料不足。");row.putInt("Cost",946);row.putInt("Duration",1405);row.putInt("Failure",10);
            var ingredients=new ListTag();for(int i=0;i<r.getIngredients().size();i++){var options=r.getIngredients().get(i).getItems();if(options.length>0)ingredients.add(options[0].copyWithCount(r.counts().get(i)).save(new CompoundTag()));}row.put("Ingredients",ingredients);rows.add(row);
        }
        var forgeRecipe=(RefiningRecipe)mc.level.getRecipeManager().byKey(new net.minecraft.resources.ResourceLocation("wuxingxiantu:refining_dark_iron_sword")).orElseThrow();
        var forgeRow=new CompoundTag();forgeRow.putString("Mode","refining");forgeRow.putString("Recipe",forgeRecipe.getId().toString());forgeRow.put("Result",forgeRecipe.getResultItem(mc.level.registryAccess()).save(new CompoundTag()));forgeRow.putInt("Cost",946);forgeRow.putInt("Duration",cn.blockforge.generated.generatedmod.alchemy.HeavenlyFurnace.duration(forgeRecipe));forgeRow.putInt("Failure",0);
        var forgeIngredients=new ListTag();for(int i=0;i<forgeRecipe.getIngredients().size();i++)forgeIngredients.add(forgeRecipe.getIngredients().get(i).getItems()[0].copyWithCount(forgeRecipe.counts().get(i)).save(new CompoundTag()));forgeRow.put("Ingredients",forgeIngredients);rows.add(forgeRow);tag.put("Rows",rows);
        for(int[] size:new int[][]{{1280,720,2},{1920,1080,3},{1280,960,4}}){
            mc.setScreen(null);mc.getWindow().setWindowed(size[0],size[1]);mc.options.guiScale().set(size[2]);mc.resizeDisplay();var screen=new HeavenlyFurnaceScreen(tag);mc.setScreen(screen);
            var choices=(List<Button>)field(screen.getClass(),"choices").get(screen);if(choices.isEmpty())throw new AssertionError("No recipe buttons");var first=choices.get(0);
            for(var child:screen.children())if(child instanceof Button&&!(child instanceof InkButton))throw new AssertionError("Vanilla button skin leaked into custom screen");
            float scale=field(screen.getClass(),"uiScale").getFloat(screen);screen.mouseClicked((first.getX()+2)*scale,(first.getY()+2)*scale,0);screen.mouseReleased((first.getX()+2)*scale,(first.getY()+2)*scale,0);
            int selected=field(screen.getClass(),"selected").getInt(screen);if(selected!=0)throw new AssertionError("Recipe mouse selection failed");
            saveUi(out.resolve("heavenly-ui-"+size[0]+"-gui"+size[2]+".png"),screen);
            var action=(Button)field(screen.getClass(),"start").get(screen);screen.setFocused(action);action.setFocused(true);saveUi(out.resolve("heavenly-ui-"+size[0]+"-gui"+size[2]+"-focus.png"),screen);action.setFocused(false);screen.setFocused(null);saveUi(out.resolve("heavenly-ui-"+size[0]+"-gui"+size[2]+"-hover.png"),screen);
            var search=(EditBox)field(screen.getClass(),"search").get(screen);search.setValue("不存在的丹药");
            if(!((List<?>)field(screen.getClass(),"filtered").get(screen)).isEmpty())throw new AssertionError("Search does not filter");
            var start=(Button)field(screen.getClass(),"start").get(screen);if(start.active)throw new AssertionError("Empty search can submit");
            search.setValue("");
            for(var child:List.copyOf(screen.children()))if(child instanceof Button button&&button.getMessage().getString().equals("炼器"))button.onPress();
            var forgeRows=(List<CompoundTag>)field(screen.getClass(),"filtered").get(screen);if(forgeRows.size()!=1||!forgeRows.get(0).getString("Mode").equals("refining"))throw new AssertionError("Refining category failed");
            ((List<Button>)field(screen.getClass(),"choices").get(screen)).get(0).onPress();
            saveUi(out.resolve("heavenly-ui-"+size[0]+"-gui"+size[2]+"-forge.png"),screen);search.setFocused(true);search.setValue("灵剑");search.setHighlightPos(0);saveUi(out.resolve("heavenly-ui-"+size[0]+"-gui"+size[2]+"-search-text.png"),screen);search.setValue("测试长文本搜索丹药与器方中文输入");search.setHighlightPos(0);saveUi(out.resolve("heavenly-ui-"+size[0]+"-gui"+size[2]+"-search-long.png"),screen);search.setValue("");search.setFocused(false);
            screen.resize(mc,screen.width,screen.height);screen.onClose();if(mc.screen!=null)throw new AssertionError("Close failed");
            results.add(Map.of("resolution",List.of(size[0],size[1]),"guiScale",size[2],"passed",true));
        }
        mc.setScreen(null);mc.getWindow().setWindowed(1280,720);mc.options.guiScale().set(2);mc.resizeDisplay();
        var sf=field(HeavenlyFurnaceVisuals.class,"shader");if(sf.get(null)==null)throw new AssertionError("Actual shader not registered");
        UUID owner=UUID.randomUUID();var visual=new CompoundTag();visual.putString("Kind","start");visual.putUUID("Owner",owner);visual.putString("Dimension",mc.level.dimension().location().toString());visual.putInt("Duration",1405);
        double ox=mc.player.getX(),oy=mc.player.getY(),oz=mc.player.getZ();visual.putDouble("X",ox);visual.putDouble("Y",oy);visual.putDouble("Z",oz);visual.put("Preview",new ItemStack(WuXingItems.AURA_PILL.get()).save(new CompoundTag()));
        var ingredients=new ListTag();for(var item:List.of(Items.REDSTONE,Items.APPLE,Items.AMETHYST_SHARD,Items.GOLD_NUGGET))ingredients.add(new ItemStack(item).save(new CompoundTag()));visual.put("Items",ingredients);
        boolean shaderQa=Boolean.getBoolean("wuxingxiantu.heavenlyShaderQa");
        var camera=new Camera();var position=Camera.class.getDeclaredMethod("setPosition",double.class,double.class,double.class);position.setAccessible(true);position.invoke(camera,ox+(shaderQa?.7:4),oy+(shaderQa?5.6:3.6),oz+(shaderQa?16:6));
        var projection=new Matrix4f().perspective((float)Math.toRadians(50),mc.getWindow().getWidth()/(float)mc.getWindow().getHeight(),.05f,64f);
        var pose=new PoseStack();pose.last().pose().set(new Matrix4f().lookAt(new Vector3f(0,0,0),shaderQa?new Vector3f(-.7f,-1.1f,-11.5f):new Vector3f(-4,-1.6f,-6),new Vector3f(0,1,0)));
        var frustum=new Frustum(pose.last().pose(),projection);frustum.prepare(ox+(shaderQa?.7:4),oy+(shaderQa?5.6:3.6),oz+(shaderQa?16:6));
        var oldProjection=new Matrix4f(RenderSystem.getProjectionMatrix());var oldSort=RenderSystem.getVertexSorting();var model=RenderSystem.getModelViewStack();model.pushPose();model.setIdentity();RenderSystem.applyModelViewMatrix();RenderSystem.setProjectionMatrix(projection,VertexSorting.DISTANCE_TO_ORIGIN);
        var shots=new ArrayList<Map<String,Object>>();
        try{
            for(int fire:new int[]{1,5,8,9})for(int age:new int[]{12,70,110,170,250,500,1380,1408}){
                visual.putInt("Fire",fire);visual.putLong("Start",mc.level.getGameTime()-age);HeavenlyFurnaceVisuals.receive(visual);
                mc.getMainRenderTarget().bindWrite(true);RenderSystem.clearColor(.012f,.024f,.022f,1);RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT,Minecraft.ON_OSX);
                var event=new RenderLevelStageEvent(RenderLevelStageEvent.Stage.AFTER_PARTICLES,mc.levelRenderer,pose,projection,0,0,camera,frustum);
                if(fire==1&&age==12)RealmFirstPersonToggleChecks.run(out,event);
                int query=age==350?GL15.glGenQueries():0;if(query!=0)GL15.glBeginQuery(GL33.GL_TIME_ELAPSED,query);
                HeavenlyFurnaceVisuals.render(event);
                HeavenlyFurnaceVisuals.post(new net.minecraftforge.client.event.RenderGuiEvent.Pre(mc.getWindow(),new GuiGraphics(mc,mc.renderBuffers().bufferSource()),0));
                if(shaderQa&&!HeavenlyFurnaceGpu.failure.isEmpty())throw new AssertionError(HeavenlyFurnaceGpu.failure);
                double gpuMs=0;if(query!=0){GL15.glEndQuery(GL33.GL_TIME_ELAPSED);gpuMs=GL33.glGetQueryObjectui64(query,GL15.GL_QUERY_RESULT)/1e6;GL15.glDeleteQueries(query);}
                var file=out.resolve("heavenly-fire"+fire+"-age"+age+".png");try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(file);}
                int error=GL11.glGetError();if(error!=0)throw new AssertionError("Heavenly shader GL error "+error);
                shots.add(Map.of("fire",fire,"age",age,"gl",error,"file",file.toString(),"gpuMs",gpuMs));
            }
        }finally{model.popPose();RenderSystem.applyModelViewMatrix();RenderSystem.setProjectionMatrix(oldProjection,oldSort);var end=new CompoundTag();end.putString("Kind","end");end.putUUID("Owner",owner);HeavenlyFurnaceVisuals.receive(end);}
        if(shaderQa)HeavenlyShaderChecks.run(out,ox,oy,oz);
        Files.writeString(out.resolve("heavenly-verification.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(Map.of("passed",true,"ui",results,"shaderShots",shots,"scope","Actual registered production shader and real item models in isolated client framebuffer; not a network multiplayer acceptance")));
    }
    private static void saveUi(Path file,HeavenlyFurnaceScreen screen)throws Exception{
        var mc=Minecraft.getInstance();var projection=new Matrix4f(RenderSystem.getProjectionMatrix());var sorting=RenderSystem.getVertexSorting();var model=RenderSystem.getModelViewStack();model.pushPose();model.setIdentity();model.translate(0,0,-11000);RenderSystem.applyModelViewMatrix();
        RenderSystem.setProjectionMatrix(new Matrix4f().setOrtho(0,mc.getWindow().getGuiScaledWidth(),mc.getWindow().getGuiScaledHeight(),0,1000,21000),VertexSorting.ORTHOGRAPHIC_Z);
        try{mc.getMainRenderTarget().bindWrite(true);RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT,Minecraft.ON_OSX);var g=new GuiGraphics(mc,mc.renderBuffers().bufferSource());int mx=0,my=0;if(file.toString().contains("-hover")){var button=(Button)field(screen.getClass(),"start").get(screen);float scale=field(screen.getClass(),"uiScale").getFloat(screen);mx=(int)((button.getX()+4)*scale);my=(int)((button.getY()+4)*scale);}screen.render(g,mx,my,0);g.flush();try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(file);}if(GL11.glGetError()!=0)throw new AssertionError("UI GL error");}
        finally{model.popPose();RenderSystem.applyModelViewMatrix();RenderSystem.setProjectionMatrix(projection,sorting);}
    }
}
