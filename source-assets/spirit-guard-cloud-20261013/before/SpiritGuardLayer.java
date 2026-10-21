package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import cn.blockforge.generated.generatedmod.SpiritEnchantment;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.io.IOException;

/** Constant-cost passive shell; uses the live model, including custom meditation limbs. */
public final class SpiritGuardLayer extends RenderLayer<AbstractClientPlayer,PlayerModel<AbstractClientPlayer>> {
    private static final EquipmentSlot[] ARMOR={EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET};
    private static final java.util.Map<AbstractClientPlayer,Double> HIT_TIMES=new java.util.WeakHashMap<>();
    public static void impact(int entityId){var level=Minecraft.getInstance().level;if(level!=null&&level.getEntity(entityId) instanceof AbstractClientPlayer p)HIT_TIMES.put(p,(double)level.getGameTime());}
    public SpiritGuardLayer(RenderLayerParent<AbstractClientPlayer,PlayerModel<AbstractClientPlayer>> parent){super(parent);}
    @Override public void render(PoseStack pose,MultiBufferSource buffers,int light,AbstractClientPlayer player,
            float swing,float amount,float partial,float age,float yaw,float pitch){
        var camera=Minecraft.getInstance().getCameraEntity();
        if(!cn.blockforge.generated.generatedmod.HudConfig.SPIRIT_GUARD_VISUAL.get())return;
        if(Material.shader==null||player.isInvisible()||player.isSpectator()||camera==null||camera.distanceToSqr(player)>48*48)return;
        int tier=0;
        for(var slot:ARMOR)tier=Math.max(tier,EnchantmentHelper.getItemEnchantmentLevel(SpiritEnchantment.SPIRIT_GUARD.get(),player.getItemBySlot(slot)));
        var data=GeneratedMod.data(player);
        float hitAge=(float)Math.max(0,Math.min(1,(player.level().getGameTime()+partial-HIT_TIMES.getOrDefault(player,-1000D))/24));
        if(tier==0||data==null||hitAge>=1)return;
        float hit=1-hitAge;
        float strength=(.30F+Math.min(5,tier)*.04F+hit*.2F)*(data.wuxingxiantu$getAura()>0?1:hit);
        // One additional model submission; hit age is carried in vertex UV data.
        getParentModel().renderToBuffer(pose,new ShellVertices(buffers.getBuffer(Material.TYPE),pose.last().pose().transformPosition(new org.joml.Vector3f(0,.5F,0)),hitAge),15728880,
                OverlayTexture.NO_OVERLAY,.48F,.78F,1F,strength);
    }
    private static final class ShellVertices implements VertexConsumer {
        private final VertexConsumer target;
        private final org.joml.Vector3f center;private final float hitAge;
        private double x,y,z;private int r,g,b,a,uLight,vLight,uOverlay,vOverlay;
        private float u,v,nx,ny,nz;
        ShellVertices(VertexConsumer target,org.joml.Vector3f center,float hitAge){this.target=target;this.center=center;this.hitAge=hitAge;}
        public VertexConsumer vertex(double x,double y,double z){this.x=x;this.y=y;this.z=z;return this;}
        public VertexConsumer color(int r,int g,int b,int a){this.r=r;this.g=g;this.b=b;this.a=a;return this;}
        public VertexConsumer uv(float u,float v){this.u=u;this.v=v;return this;}
        public VertexConsumer overlayCoords(int u,int v){uOverlay=u;vOverlay=v;return this;}
        public VertexConsumer uv2(int u,int v){uLight=u;vLight=v;return this;}
        public VertexConsumer normal(float x,float y,float z){nx=x;ny=y;nz=z;return this;}
        public void endVertex(){target.vertex(x+nx*.025,y+ny*.025,z+nz*.025).color(r,g,b,a).uv((float)Math.sqrt((x-center.x)*(x-center.x)+(y-center.y)*(y-center.y)+(z-center.z)*(z-center.z)),hitAge)
                .overlayCoords(uOverlay,vOverlay).uv2(uLight,vLight).normal(nx,ny,nz).endVertex();}
        public void defaultColor(int r,int g,int b,int a){target.defaultColor(r,g,b,a);}
        public void unsetDefaultColor(){target.unsetDefaultColor();}
    }
    @Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT,bus=Mod.EventBusSubscriber.Bus.MOD)
    public static final class Material extends RenderType {
        static ShaderInstance shader;
        static final RenderType TYPE=create("spirit_guard_shell",DefaultVertexFormat.NEW_ENTITY,VertexFormat.Mode.QUADS,
                4096,false,true,CompositeState.builder().setShaderState(new ShaderStateShard(()->shader))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY).setDepthTestState(LEQUAL_DEPTH_TEST)
                .setWriteMaskState(COLOR_WRITE).setCullState(CULL).createCompositeState(false));
        private Material(String n,VertexFormat f,VertexFormat.Mode m,int size,boolean c,boolean s,Runnable a,Runnable b){super(n,f,m,size,c,s,a,b);}
        @SubscribeEvent public static void shaders(RegisterShadersEvent e)throws IOException{
            e.registerShader(new ShaderInstance(e.getResourceProvider(),new ResourceLocation(GeneratedMod.MOD_ID,"spirit_guard_shell"),DefaultVertexFormat.NEW_ENTITY),s->shader=s);
        }
        @SubscribeEvent public static void layers(EntityRenderersEvent.AddLayers e){
            for(String skin:e.getSkins()){
                net.minecraft.client.renderer.entity.player.PlayerRenderer renderer=e.getSkin(skin);
                if(renderer!=null)renderer.addLayer(new SpiritGuardLayer(renderer));
            }
        }
    }
}
