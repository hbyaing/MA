package cn.blockforge.generated.generatedmod.alchemy.simulation;

import com.google.gson.*;
import java.util.*;
import static cn.blockforge.generated.generatedmod.alchemy.simulation.SimulationRules.*;

/** Strict data decoder, with no scripts, reflection, arbitrary expressions or default high-power materials. */
public final class SimulationRuleLoader {
    // Gson's tree parser silently keeps the last duplicate key. Reject duplicates before decoding.
    private static JsonElement tree(com.google.gson.stream.JsonReader in,int depth)throws java.io.IOException{
        if(depth>16)throw new IllegalArgumentException("Rules nesting budget exceeded");
        return switch(in.peek()){
            case BEGIN_OBJECT -> {in.beginObject();var out=new JsonObject();while(in.hasNext()){String key=in.nextName();if(out.has(key))throw new IllegalArgumentException("Duplicate field "+key);out.add(key,tree(in,depth+1));}in.endObject();yield out;}
            case BEGIN_ARRAY -> {in.beginArray();var out=new JsonArray();while(in.hasNext())out.add(tree(in,depth+1));in.endArray();yield out;}
            case STRING -> new JsonPrimitive(in.nextString());
            case NUMBER -> new JsonPrimitive(new java.math.BigDecimal(in.nextString()));
            case BOOLEAN -> new JsonPrimitive(in.nextBoolean());
            case NULL -> {in.nextNull();yield JsonNull.INSTANCE;}
            default -> throw new IllegalArgumentException("Invalid JSON token");
        };
    }
    private static String checked(String json){if(json==null||json.length()>262144)throw new IllegalArgumentException("Document budget exceeded");return json;}
    public static JsonObject document(String json){try(var in=new com.google.gson.stream.JsonReader(new java.io.StringReader(checked(json)))){in.setLenient(false);var value=tree(in,0);if(in.peek()!=com.google.gson.stream.JsonToken.END_DOCUMENT)throw new IllegalArgumentException("Trailing rules");return value.getAsJsonObject();}catch(java.io.IOException e){throw new IllegalArgumentException("Malformed rules",e);}}
    private static void keys(JsonObject o,String... allowed){var set=Set.of(allowed);for(String key:o.keySet())if(!set.contains(key))throw new IllegalArgumentException("Unknown field "+key);for(String key:allowed)if(!o.has(key))throw new IllegalArgumentException("Missing field "+key);}
    private static long number(JsonObject o,String key){var v=o.get(key);if(v==null||!v.isJsonPrimitive()||!v.getAsJsonPrimitive().isNumber())throw new IllegalArgumentException("Expected integer "+key);return v.getAsBigDecimal().longValueExact();}
    private static int integer(JsonObject o,String key){return Math.toIntExact(number(o,key));}
    private static String string(JsonObject o,String key){var v=o.get(key);if(v==null||!v.isJsonPrimitive()||!v.getAsJsonPrimitive().isString())throw new IllegalArgumentException("Expected string "+key);return v.getAsString();}
    private static Map<String,Long> quantities(JsonObject o){var result=new TreeMap<String,Long>();for(String k:o.keySet())result.put(k,number(o,k));return SimulationRules.quantities(result);}
    public static SimulationRules parse(String json){
        if(json==null||json.length()>262144)throw new IllegalArgumentException("Rules byte/character budget exceeded");
        var root=document(json);keys(root,"schema","version","limits","resources","reactions");if(integer(root,"schema")!=1)throw new IllegalArgumentException("Unknown rules schema");
        var l=root.getAsJsonObject("limits");keys(l,"maxRules","maxExtent","maxSteps","maxEvents","maxStockUnits","maxExternalPerEvent","heatCapacity","coolingDivisor","damageTemperature","maximumDamage","burstPressure","controlNoise");
        var limits=new Limits(integer(l,"maxRules"),integer(l,"maxExtent"),integer(l,"maxSteps"),integer(l,"maxEvents"),number(l,"maxStockUnits"),number(l,"maxExternalPerEvent"),number(l,"heatCapacity"),number(l,"coolingDivisor"),number(l,"damageTemperature"),number(l,"maximumDamage"),number(l,"burstPressure"),integer(l,"controlNoise"));
        var resources=new ArrayList<Resource>();var resourceArray=root.getAsJsonArray("resources");if(resourceArray.size()>128)throw new IllegalArgumentException("Too many resources");
        for(var value:resourceArray){var o=value.getAsJsonObject();keys(o,"id","mass","potential","gas","destination");resources.add(new Resource(string(o,"id"),number(o,"mass"),number(o,"potential"),number(o,"gas"),Destination.valueOf(string(o,"destination"))));}
        var reactions=new ArrayList<Reaction>();var ruleArray=root.getAsJsonArray("reactions");if(ruleArray.size()>limits.maxRules())throw new IllegalArgumentException("Too many reactions");
        for(var value:ruleArray){var o=value.getAsJsonObject();keys(o,"id","priority","phase","consume","produce","maximumExtent","energyCost","authorizedPotential","holdSteps","windows");var windows=new ArrayList<Window>();var array=o.getAsJsonArray("windows");if(array.size()>32)throw new IllegalArgumentException("Too many windows");
            for(var item:array){var w=item.getAsJsonObject();keys(w,"measure","key","relativeTo","minimum","maximum");windows.add(new Window(Measure.valueOf(string(w,"measure")),string(w,"key"),string(w,"relativeTo"),number(w,"minimum"),number(w,"maximum")));}
            reactions.add(new Reaction(string(o,"id"),integer(o,"priority"),Phase.valueOf(string(o,"phase")),quantities(o.getAsJsonObject("consume")),quantities(o.getAsJsonObject("produce")),number(o,"maximumExtent"),number(o,"energyCost"),number(o,"authorizedPotential"),integer(o,"holdSteps"),windows));
        }return new SimulationRules(string(root,"version"),resources,reactions,limits);
    }
    private SimulationRuleLoader(){}
}
