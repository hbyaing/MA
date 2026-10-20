package cn.blockforge.generated.generatedmod.client.weaponflame;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import com.google.gson.*;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID,value=Dist.CLIENT,bus=Mod.EventBusSubscriber.Bus.MOD)
public final class WeaponFlameResources extends SimpleJsonResourceReloadListener {
    private static Map<ResourceLocation,ExoticFlameVisualProfile> fires=Map.of();
    private static Map<ResourceLocation,WeaponFlameAnchorProfile> weapons=Map.of();
    private WeaponFlameResources(){super(new Gson(),"weapon_flames");}
    @SubscribeEvent public static void register(RegisterClientReloadListenersEvent event){event.registerReloadListener(new WeaponFlameResources());}
    public static ExoticFlameVisualProfile fire(ResourceLocation id){return fires.get(id);}
    public static WeaponFlameAnchorProfile anchor(ItemStack stack){return weapons.get(ForgeRegistries.ITEMS.getKey(stack.getItem()));}
    @Override protected void apply(Map<ResourceLocation,JsonElement> input,ResourceManager manager,ProfilerFiller profiler) {
        var nextFires=new HashMap<ResourceLocation,ExoticFlameVisualProfile>();
        var nextWeapons=new HashMap<ResourceLocation,WeaponFlameAnchorProfile>();
        input.forEach((id,element)->{
            try {
                var o=element.getAsJsonObject();
                if(GsonHelper.getAsString(o,"type").equals("weapon")) {
                    var h=vector(o,"hiltAnchor");var t=vector(o,"tipAnchor");var side=vector(o,"widthAxis").normalize();
                    if(h.distanceTo(t)<.1||Math.abs(t.subtract(h).normalize().dot(side))>.01||side.length()<.99)
                        throw new IllegalArgumentException("Invalid blade basis");
                    nextWeapons.put(id,new WeaponFlameAnchorProfile(h,t,side,number(o,"bladeWidth",.005F,2),number(o,"bladeThickness",.002F,1)));
                } else {
                    var c=o.getAsJsonObject("colors");var s=o.getAsJsonObject("shape");var m=o.getAsJsonObject("motion");var b=o.getAsJsonObject("combat");
                    boolean legacy=GsonHelper.getAsInt(o,"schemaVersion",1)==1;
                    // V1 radius used rectangular half-extents; V2 dimensions are full blade-width ratios.
                    var shape=legacy?new ExoticFlameVisualProfile.Shape(
                            Math.max(.24F,number(s,"flameWidth",.05F,.5F)),number(s,"flameHeight",.01F,.2F)*8,
                            Math.max(.75F,Math.min(.9F,number(s,"coilRadius",1,1.8F)*.67F)),number(s,"coilTurns",.8F,1.5F),
                            number(s,"attachmentSize",.01F,.12F)*2):new ExoticFlameVisualProfile.Shape(
                            number(s,"flameWidth",.2F,.35F),number(s,"flameHeight",.2F,1),number(s,"coilRadius",.65F,.9F),
                            number(s,"coilTurns",.8F,1.5F),number(s,"attachmentSize",.05F,.2F));
                    nextFires.put(id,new ExoticFlameVisualProfile(id,
                        new ExoticFlameVisualProfile.Colors(color(c,"coreColor"),color(c,"bodyColor"),color(c,"edgeColor"),color(c,"sparkColor"),color(c,"glowColor")),
                        shape,
                        new ExoticFlameVisualProfile.Motion(number(m,"flowSpeed",0,3),number(m,"coilSpeed",0,2),number(m,"flickerStrength",0,.5F),number(m,"turbulence",0,.4F)),
                        new ExoticFlameVisualProfile.Combat(number(b,"attackStretch",0,.5F),number(b,"trailLength",.02F,.3F),number(b,"trailFadeTime",2,5),
                            (int)number(b,"hitPeelAmount",2,4),(int)number(b,"scatterAmount",0,4))));
                }
            } catch(RuntimeException ex){com.mojang.logging.LogUtils.getLogger().error("Invalid weapon flame profile {}",id,ex);}
        });
        fires=Map.copyOf(nextFires);weapons=Map.copyOf(nextWeapons);
    }
    private static int color(JsonObject o,String key){return Integer.parseInt(GsonHelper.getAsString(o,key).replace("#",""),16)&0xffffff;}
    private static float number(JsonObject o,String key,float min,float max) {
        float value=GsonHelper.getAsFloat(o,key);
        if(!Float.isFinite(value)||value<min||value>max)throw new IllegalArgumentException(key+" outside bounds");
        return value;
    }
    private static Vec3 vector(JsonObject o,String key) {
        var a=GsonHelper.getAsJsonArray(o,key);if(a.size()!=3)throw new IllegalArgumentException(key);
        var v=new Vec3(a.get(0).getAsDouble(),a.get(1).getAsDouble(),a.get(2).getAsDouble());
        if(!Double.isFinite(v.lengthSqr())||v.length()>8)throw new IllegalArgumentException(key);return v;
    }
}
