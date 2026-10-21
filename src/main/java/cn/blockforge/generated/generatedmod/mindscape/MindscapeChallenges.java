package cn.blockforge.generated.generatedmod.mindscape;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import com.google.gson.*;
import java.util.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.*;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid=GeneratedMod.MOD_ID)
public final class MindscapeChallenges extends SimpleJsonResourceReloadListener {
    public record Definition(ResourceLocation id,ResourceLocation boss,int requiredRealm,String triggerStage,String arena,boolean isFinal,int progressIndex){}
    private static Map<ResourceLocation,Definition> definitions=Map.of();
    private static final org.slf4j.Logger LOGGER=com.mojang.logging.LogUtils.getLogger();
    private MindscapeChallenges(){super(new Gson(),"mindscape_challenges");}
    @SubscribeEvent public static void reload(AddReloadListenerEvent event){event.addListener(new MindscapeChallenges());}
    @Override protected void apply(Map<ResourceLocation,JsonElement> entries,ResourceManager manager,ProfilerFiller profiler){
        var next=new TreeMap<ResourceLocation,Definition>();
        entries.forEach((file,value)->{
            try{
                JsonObject json=value.getAsJsonObject();
                ResourceLocation id=new ResourceLocation(GsonHelper.getAsString(json,"challenge_id"));
                ResourceLocation boss=new ResourceLocation(GsonHelper.getAsString(json,"boss_entity_id"));
                int realm=GsonHelper.getAsInt(json,"required_realm",0);
                String trigger=GsonHelper.getAsString(json,"trigger_stage","manual_test");
                String arena=GsonHelper.getAsString(json,"arena_id");
                boolean last=GsonHelper.getAsBoolean(json,"is_final",false);
                int progress=GsonHelper.getAsInt(json,"progress_index",0);
                boolean knownBoss=boss.getNamespace().equals(GeneratedMod.MOD_ID)
                        &&(boss.getPath().equals("heart_demon")||cn.blockforge.generated.generatedmod.registry.ModEntities.VISUAL_BOSSES.containsKey(boss.getPath()));
                if(!file.equals(id)||id.toString().length()>256||!knownBoss||!ForgeRegistries.ENTITY_TYPES.containsKey(boss)||realm<0||realm>8
                        ||arena.isBlank()||(!arena.equals("test_platform")&&!arena.matches("(six_thieves|three_corps|heart_demon)_[0-9]+"))
                        ||progress<0||progress>9
                        ||(!trigger.equals("manual_test")&&!trigger.equals("post_tribulation_test")&&!trigger.equals("formal")))
                    throw new IllegalArgumentException("Invalid test challenge definition");
                if(progress>0&& !trigger.equals("formal")) throw new IllegalArgumentException("Formal progress requires formal trigger");
                next.put(id,new Definition(id,boss,realm,trigger,arena,last,progress));
            }catch(RuntimeException exception){LOGGER.error("Invalid mindscape challenge {}",file,exception);}
        });
        definitions=Map.copyOf(next);
    }
    public static Definition get(String id){ResourceLocation key=ResourceLocation.tryParse(id);return key==null?null:definitions.get(key);}
    public static Set<ResourceLocation> ids(){return definitions.keySet();}
    public static Definition forMeditation(int realm){return definitions.values().stream()
            .filter(d->d.requiredRealm()<=realm&&d.triggerStage().equals("post_tribulation_test"))
            .sorted(Comparator.comparing(d->d.id().toString())).findFirst().orElse(null);}
    public static Definition formalByIndex(int index,int realm){return definitions.values().stream()
            .filter(d->d.progressIndex()==index&&d.triggerStage().equals("formal")&&d.requiredRealm()<=realm)
            .findFirst().orElse(null);}
    public static Definition finalChallenge(int realm){return definitions.values().stream()
            .filter(d->d.isFinal()&&d.triggerStage().equals("formal")&&d.requiredRealm()<=realm)
            .findFirst().orElse(null);}
    public static boolean hasFormalChallenges(){return definitions.values().stream().anyMatch(d->d.triggerStage().equals("formal"));}
}
