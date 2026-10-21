package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Vanilla particle rendering supplies the passive glow; the body layer handles impacts only. */
@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT)
public final class SpiritGuardGlow {
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e) {
        var mc=Minecraft.getInstance();
        if(e.phase!=TickEvent.Phase.END||mc.level==null||mc.player==null||!HudConfig.SPIRIT_GUARD_VISUAL.get())return;
        if(mc.level.getGameTime()%3!=0)return;
        for(var p:mc.level.players()) {
            if(p.isInvisible()||p.isSpectator()||p.distanceToSqr(mc.player)>32*32)continue;
            var data=GeneratedMod.data(p);if(data==null||data.wuxingxiantu$getAura()<=0)continue;
            boolean active=false;
            for(var slot:new EquipmentSlot[]{EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET})
                active|=EnchantmentHelper.getItemEnchantmentLevel(SpiritEnchantment.SPIRIT_GUARD.get(),p.getItemBySlot(slot))>0;
            if(!active)continue;
            var random=p.getRandom();
            for(int i=0;i<2;i++) {
                double angle=random.nextDouble()*Math.PI*2,radius=p.getBbWidth()*.56;
                mc.level.addParticle(ModParticles.SPIRIT_GUARD_GLOW.get(),p.getX()+Math.cos(angle)*radius,
                    p.getY()+.12+random.nextDouble()*(p.getBbHeight()-.2),p.getZ()+Math.sin(angle)*radius,
                    p.getDeltaMovement().x*.25,.018,p.getDeltaMovement().z*.25);
            }
        }
    }
    @Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT,bus=Mod.EventBusSubscriber.Bus.MOD)
    public static final class Providers {
        @SubscribeEvent public static void register(RegisterParticleProvidersEvent e) {
            e.registerSpriteSet(ModParticles.SPIRIT_GUARD_GLOW.get(),sprites->
                (SimpleParticleType type,ClientLevel level,double x,double y,double z,double xd,double yd,double zd)->
                    new GlowParticle(level,x,y,z,xd,yd,zd,sprites));
        }
    }
    private static final class GlowParticle extends TextureSheetParticle {
        GlowParticle(ClientLevel level,double x,double y,double z,double xd,double yd,double zd,SpriteSet sprites) {
            super(level,x,y,z);pickSprite(sprites);this.xd=xd;this.yd=yd;this.zd=zd;
            quadSize=.045F;lifetime=14+random.nextInt(7);hasPhysics=true;
            setColor(.25F,.68F,1F);alpha=.65F;
        }
        @Override public void tick(){super.tick();alpha=.65F*(1-age/(float)lifetime);}
        @Override public int getLightColor(float partial){return LightTexture.FULL_BRIGHT;}
        @Override public ParticleRenderType getRenderType(){return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;}
    }
}
