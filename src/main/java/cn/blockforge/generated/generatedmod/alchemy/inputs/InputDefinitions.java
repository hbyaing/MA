package cn.blockforge.generated.generatedmod.alchemy.inputs;

import cn.blockforge.generated.generatedmod.alchemy.simulation.SimulationRuleLoader;
import cn.blockforge.generated.generatedmod.plants.*;
import com.google.gson.*;
import java.util.*;
import java.nio.charset.StandardCharsets;

/** Bounded, immutable data snapshot. Batch owners retain it across resource reloads. */
@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid="wuxingxiantu")
public final class InputDefinitions {
    public final long massPerItem,baseActive,ageActive,ageScale,preparedActive,rainTransmission;
    public final List<Long> moonStrength;
    public final String hash;
    private final Map<String,Long> propertyWeights, fireMaximumRates;
    private final Map<String,Map<String,Long>> species;
    private static volatile InputDefinitions current;
    private static volatile String lastError="";
    private static void keys(JsonObject o,String... keys){
        var allowed=Set.of(keys);if(!o.keySet().equals(allowed))throw new IllegalArgumentException("Unknown/missing input definitions field: "+o.keySet());
    }
    private static long integer(JsonElement value){
        if(value==null||!value.isJsonPrimitive()||!value.getAsJsonPrimitive().isNumber())throw new IllegalArgumentException("Expected integer");
        return value.getAsBigDecimal().longValueExact();
    }
    private static long bounded(JsonObject o,String key,long min,long max){long n=integer(o.get(key));if(n<min||n>max)throw new IllegalArgumentException("Invalid "+key);return n;}
    private static Map<String,Long> weights(JsonObject o){
        var result=new TreeMap<String,Long>();if(o.size()>HerbProperty.values().length)throw new IllegalArgumentException("Property budget exceeded");
        for(var entry:o.entrySet()){if(!HerbProperty.byId(entry.getKey()).id().equals(entry.getKey()))throw new IllegalArgumentException("Noncanonical property ID");long value=integer(entry.getValue());if(value<1||value>10000)throw new IllegalArgumentException("Invalid property weight");result.put(entry.getKey(),value);}
        return Collections.unmodifiableMap(result);
    }
    public InputDefinitions(String json){
        var o=SimulationRuleLoader.document(json);
        keys(o,"schema","massPerItem","baseActive","ageActive","ageScale","preparedActive","rainTransmission","moonStrength","propertyWeights","species","fireMaximumRates");
        bounded(o,"schema",1,1);massPerItem=bounded(o,"massPerItem",1,100000);baseActive=bounded(o,"baseActive",0,massPerItem);
        ageActive=bounded(o,"ageActive",0,massPerItem);ageScale=bounded(o,"ageScale",1,1000000000);preparedActive=bounded(o,"preparedActive",0,massPerItem);
        if(baseActive+ageActive+preparedActive>massPerItem)throw new IllegalArgumentException("Active partition exceeds mass");
        rainTransmission=bounded(o,"rainTransmission",0,1000);
        var moon=new ArrayList<Long>();for(var v:o.getAsJsonArray("moonStrength")){long n=integer(v);if(n<0||n>1000000)throw new IllegalArgumentException("Invalid moon rate");moon.add(n);}
        if(moon.size()!=8)throw new IllegalArgumentException("Expected eight lunar phases");moonStrength=List.copyOf(moon);
        propertyWeights=weights(o.getAsJsonObject("propertyWeights"));
        for(var property:HerbProperty.values())if(!propertyWeights.containsKey(property.id()))throw new IllegalArgumentException("Missing property "+property.id());
        var speciesMap=new TreeMap<String,Map<String,Long>>();var overrides=o.getAsJsonObject("species");
        if(overrides.size()>256)throw new IllegalArgumentException("Species budget exceeded");
        for(var entry:overrides.entrySet()){
            if(!SpiritPlantCatalog.BY_ID.containsKey(entry.getKey()))throw new IllegalArgumentException("Unknown species "+entry.getKey());
            speciesMap.put(entry.getKey(),weights(entry.getValue().getAsJsonObject()));
        }
        species=Collections.unmodifiableMap(speciesMap);
        var rates=new TreeMap<String,Long>();var configuredRates=o.getAsJsonObject("fireMaximumRates");
        if(configuredRates.size()!=cn.blockforge.generated.generatedmod.fire.FireCatalog.ALL.length)throw new IllegalArgumentException("Incomplete fire input rates");
        for(var entry:configuredRates.entrySet()){
            if(cn.blockforge.generated.generatedmod.fire.FireCatalog.id(entry.getKey())==null)throw new IllegalArgumentException("Unknown fire");
            long n=integer(entry.getValue());if(n<1||n>1000000000)throw new IllegalArgumentException("Invalid fire rate");rates.put(entry.getKey(),n);
        }
        fireMaximumRates=Collections.unmodifiableMap(rates);
        try{String canonical=massPerItem+"|"+baseActive+"|"+ageActive+"|"+ageScale+"|"+preparedActive+"|"+rainTransmission+"|"+moonStrength+"|"+propertyWeights+"|"+species+"|"+fireMaximumRates;
            hash=HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(canonical.getBytes(StandardCharsets.UTF_8)));
        }catch(java.security.NoSuchAlgorithmException e){throw new IllegalStateException(e);}
    }
    public long fireMaximumRate(String fire){var rate=fireMaximumRates.get(fire);if(rate==null)throw new IllegalArgumentException("Unknown fire");return rate;}
    public Map<String,Long> weights(SpiritPlantDefinition herb){
        var override=species.get(herb.id);if(override!=null)return override;
        var result=new TreeMap<String,Long>();for(var p:herb.properties)result.put(p.id(),propertyWeights.get(p.id()));return Collections.unmodifiableMap(result);
    }
    public static synchronized InputDefinitions current(){
        if(current==null)try(var in=InputDefinitions.class.getResourceAsStream("/data/wuxingxiantu/alchemy/inputs.json")){
            if(in==null)throw new IllegalStateException("Missing input definitions");current=new InputDefinitions(new String(in.readAllBytes(),StandardCharsets.UTF_8));
        }catch(java.io.IOException e){throw new IllegalStateException(e);}return current;
    }
    public static String lastError(){return lastError;}
    @net.minecraftforge.eventbus.api.SubscribeEvent public static void reload(net.minecraftforge.event.AddReloadListenerEvent event){
        event.addListener((net.minecraft.server.packs.resources.ResourceManagerReloadListener)manager->{
            current();
            try(var reader=manager.openAsReader(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("wuxingxiantu","alchemy/inputs.json"))){
                var text=new StringBuilder();char[] buffer=new char[4096];int count;
                while((count=reader.read(buffer))!=-1){text.append(buffer,0,count);if(text.length()>262144)throw new IllegalArgumentException("Input definition budget exceeded");}
                current=new InputDefinitions(text.toString());lastError="";
            }catch(Exception e){lastError=e.toString();com.mojang.logging.LogUtils.getLogger().error("Retaining previous alchemy input definitions: {}",lastError);}
        });
    }
}
