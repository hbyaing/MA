package cn.blockforge.generated.generatedmod.alchemy.observation;

import com.google.gson.*;
import java.util.*;

/** Versioned presentation metadata. Providers are registered code, never expressions supplied by a pack. */
public record ObservationDefinitions(List<Parameter> parameters,Tags tags) {
    public record Parameter(String id,String source,String label,String unit){}
    public record Tags(String prefix,String translationPrefix,String category,String strengthLabel,String budgetLabel){}
    private static String string(JsonObject obj,String key){
        if(!obj.has(key)||!obj.get(key).isJsonPrimitive()||!obj.get(key).getAsJsonPrimitive().isString())throw new IllegalArgumentException("Missing string: "+key);
        String value=obj.get(key).getAsString();if(value.length()>96)throw new IllegalArgumentException("Field too long: "+key);return value;
    }
    public static ObservationDefinitions parse(String json,Set<String> providers){
        var root=JsonParser.parseString(json).getAsJsonObject();
        if(!root.has("schema")||!root.get("schema").isJsonPrimitive()||!root.get("schema").getAsJsonPrimitive().isNumber()||!root.get("schema").getAsString().equals("1"))throw new IllegalArgumentException("Unknown observation schema");
        var rows=root.getAsJsonArray("parameters");if(rows==null||rows.size()>32)throw new IllegalArgumentException("Parameter budget exceeded");
        var ids=new HashSet<String>();var parsed=new ArrayList<Parameter>();
        for(var row:rows){var o=row.getAsJsonObject();String id=string(o,"id"),source=string(o,"source");
            if(!id.matches("[a-z0-9_.:-]+")||!ids.add(id)||!providers.contains(source))throw new IllegalArgumentException("Duplicate id or unknown provider: "+id);
            parsed.add(new Parameter(id,source,string(o,"label"),string(o,"unit")));
        }
        var t=root.getAsJsonObject("tags");var tags=new Tags(string(t,"prefix"),string(t,"translationPrefix"),string(t,"category"),string(t,"strengthLabel"),string(t,"budgetLabel"));
        if(!tags.category.equals("material"))throw new IllegalArgumentException("Only publicly observed material tags have a live provider yet");
        return new ObservationDefinitions(List.copyOf(parsed),tags);
    }
}
