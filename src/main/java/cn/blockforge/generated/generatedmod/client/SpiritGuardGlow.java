package cn.blockforge.generated.generatedmod.client;

import cn.blockforge.generated.generatedmod.*;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Registered particle stays available to explicit emitters; armor now supplies the passive visual. */
public final class SpiritGuardGlow {
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
