package cn.blockforge.generated.generatedmod.text;

import com.google.gson.JsonParser;
import cn.blockforge.generated.generatedmod.alchemy.PillData;
import cn.blockforge.generated.generatedmod.fire.FireBottleData;
import cn.blockforge.generated.generatedmod.fire.FireCatalog;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/** Stable editorial metadata; all player text uses language keys. No client API or stack mutation. */
public final class ItemTextCatalog {
    public record Entry(String id,int nameColor,int gradeColor,String visibility,int actions) {
        public Component text(String suffix) {return Component.translatable("tooltip.wuxingxiantu.intro."+id+"."+suffix);}
    }
    private static final Map<String,Entry> ENTRIES=load();
    private static Map<String,Entry> load() {
        var result=new java.util.HashMap<String,Entry>();
        try(var stream=ItemTextCatalog.class.getResourceAsStream("/assets/wuxingxiantu/item_text_catalog.json")) {
            if(stream==null)throw new IllegalStateException("Missing item text catalogue");
            var json=JsonParser.parseReader(new InputStreamReader(stream,StandardCharsets.UTF_8)).getAsJsonObject();
            json.entrySet().forEach(entry->{var value=entry.getValue().getAsJsonObject();
                result.put(entry.getKey(),new Entry(entry.getKey(),value.get("nameColor").getAsInt(),
                        value.get("gradeColor").getAsInt(),value.get("visibility").getAsString(),value.get("actions").getAsInt()));
            });
        }catch(java.io.IOException exception){throw new IllegalStateException("Invalid item text catalogue",exception);}
        return Map.copyOf(result);
    }
    public static Entry resolve(ItemStack stack) {
        var id=BuiltInRegistries.ITEM.getKey(stack.getItem());
        if(!id.getNamespace().equals("wuxingxiantu"))return null;
        var pill=PillData.read(stack);
        if(pill!=null)return ENTRIES.get(pill.pill().id);
        int fireTier=FireBottleData.tier(stack);
        if(fireTier>0)return ENTRIES.get("fire_seed_"+FireCatalog.tier(fireTier).id());
        return ENTRIES.get(id.getPath());
    }
    private ItemTextCatalog() {}
}
