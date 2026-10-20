package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.*;
import cn.blockforge.generated.generatedmod.client.realmflight.*;
import cn.blockforge.generated.generatedmod.realmflight.*;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

/** A bounded view-attached flow: no world-path or filtered turn direction in first person. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT)
public final class FirstPersonFlightFlow {
    public static int vertices;
    public static double strength;
    public static double advectionPhase;
    private static final BufferBuilder BUFFER=new BufferBuilder(65536);
    private static final SwordTrailPhase.TickClock CLOCK=new SwordTrailPhase.TickClock();
    private static final FlightFlowMotion MOTION=new FlightFlowMotion();
    private static Object motionPlayer;
    private static net.minecraft.world.phys.Vec3 previousPosition;
    private static boolean active;
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e){
        if(e.phase!=TickEvent.Phase.END)return;var mc=Minecraft.getInstance();if(CLOCK.setLevel(mc.level)){MOTION.reset();previousPosition=null;}CLOCK.advance(0,!mc.isPaused());
        if(motionPlayer!=mc.player){motionPlayer=mc.player;MOTION.reset();previousPosition=null;}
        active=mc.player!=null&&mc.player.getVehicle()==null&&HudConfig.REALM_FLIGHT_EFFECTS.get()
            &&HudConfig.REALM_FLIGHT_FIRST_PERSON_EFFECTS.get()&&RealmFlightEffects.transformed(mc.player);
        if(!active){MOTION.reset();previousPosition=null;return;}
        if(!mc.isPaused()){
            var position=mc.player.position();
            double moved=previousPosition==null?0:position.distanceTo(previousPosition);
            // Position is authoritative for client-controlled flight; velocity
            // can be cleared by prediction/network updates while still moving.
            if(moved>16)moved=0; // Teleports must not produce a sprint flash.
            MOTION.tick(Math.max(moved,mc.player.getDeltaMovement().length()),RealmFlightControl.boosting(mc.player));
            previousPosition=position;
        }
    }
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void gui(RenderGuiEvent.Pre e){e.getGuiGraphics().flush();draw();}
    @SubscribeEvent(priority=EventPriority.LOW) public static void frame(TickEvent.RenderTickEvent e){
        if(e.phase==TickEvent.Phase.START){vertices=0;strength=0;}
        else if(Minecraft.getInstance().options.hideGui)draw();
    }
    private static void draw(){
        var mc=Minecraft.getInstance();
        if(mc.player==null||mc.level==null||mc.screen!=null||!mc.options.getCameraType().isFirstPerson())return;
        // Realm flow never renders while riding a sword (or any other vehicle).
        if(mc.player.getVehicle()!=null||!active||!HudConfig.REALM_FLIGHT_EFFECTS.get()
            ||!HudConfig.REALM_FLIGHT_FIRST_PERSON_EFFECTS.get()||!RealmFlightEffects.transformed(mc.player))return;
            var data=GeneratedMod.data(mc.player);
            int realm=RealmFlightSelection.resolve(data.wuxingxiantu$getRealm(),HudConfig.REALM_FLIGHT_VISUAL_REALM.get());
        int color=RealmFlightAppearance.profile(realm).color();
        strength=MOTION.strength(mc.getFrameTime());
        advectionPhase=MOTION.phase(mc.getFrameTime());
        if(strength<=.001||FirstPersonFlowMaterial.shader==null)return;
        float w=mc.getWindow().getGuiScaledWidth(),h=mc.getWindow().getGuiScaledHeight();
        double time=(CLOCK.tick()+mc.getFrameTime())/20.;
        var previousProjection=new Matrix4f(RenderSystem.getProjectionMatrix());
        boolean stencil=org.lwjgl.opengl.GL11.glIsEnabled(org.lwjgl.opengl.GL11.GL_STENCIL_TEST);
        var sorting=RenderSystem.getVertexSorting();var model=RenderSystem.getModelViewStack();model.pushPose();
        try(var saved=new MeteorBloomRenderer.State()){
            mc.getMainRenderTarget().bindWrite(true);
            RenderSystem.colorMask(true,true,true,true);
            org.lwjgl.opengl.GL11.glDisable(org.lwjgl.opengl.GL11.GL_STENCIL_TEST);
            model.setIdentity();RenderSystem.applyModelViewMatrix();
            RenderSystem.setProjectionMatrix(new Matrix4f().setOrtho(0,w,h,0,-1,1),VertexSorting.ORTHOGRAPHIC_Z);
            RenderSystem.disableDepthTest();RenderSystem.depthMask(false);RenderSystem.disableCull();RenderSystem.disableScissor();
            RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();RenderSystem.setShaderColor(1,1,1,1);
            var shader=FirstPersonFlowMaterial.shader;
            shader.safeGetUniform("EffectTime").set((float)time);
            shader.safeGetUniform("AdvectionPhase").set((float)advectionPhase);
            shader.safeGetUniform("Strength").set((float)strength);
            RenderSystem.setShader(()->shader);
            BUFFER.begin(VertexFormat.Mode.QUADS,DefaultVertexFormat.POSITION_TEX_COLOR);
            // Four non-overlapping strips cover all edges; corners draw once.
            strip(0,0,w,h*.28F,w,h,color);
            strip(0,h*.78F,w,h,w,h,color);
            strip(0,h*.28F,w*.29F,h*.78F,w,h,color);
            strip(w*.71F,h*.28F,w,h*.78F,w,h,color);
            BufferUploader.drawWithShader(BUFFER.end());
        }finally{
            if(stencil)org.lwjgl.opengl.GL11.glEnable(org.lwjgl.opengl.GL11.GL_STENCIL_TEST);
            model.popPose();RenderSystem.applyModelViewMatrix();RenderSystem.setProjectionMatrix(previousProjection,sorting);
        }
    }
    private static void strip(float x0,float y0,float x1,float y1,float w,float h,int color){
        vertex(x0,y0,w,h,color);vertex(x0,y1,w,h,color);vertex(x1,y1,w,h,color);vertex(x1,y0,w,h,color);
    }
    private static void vertex(float x,float y,float w,float h,int color){
        BUFFER.vertex(x,y,0).uv(x/w,y/h).color((color>>16&255)/255F,(color>>8&255)/255F,(color&255)/255F,1).endVertex();vertices++;
    }
    private FirstPersonFlightFlow(){}
}
