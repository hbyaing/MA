package cn.blockforge.generated.generatedmod.divinesurvey.client;
import java.util.Map;import net.minecraft.client.resources.language.I18n;
public final class StructureNames {
 private static final Map<String,String> NAMES=Map.ofEntries(
  Map.entry("village_plains","平原村庄"),Map.entry("village_desert","沙漠村庄"),Map.entry("village_savanna","热带草原村庄"),Map.entry("village_snowy","雪原村庄"),Map.entry("village_taiga","针叶林村庄"),
  Map.entry("mineshaft","废弃矿井"),Map.entry("mineshaft_mesa","恶地矿井"),Map.entry("stronghold","要塞"),Map.entry("fortress","下界要塞"),Map.entry("bastion_remnant","堡垒遗迹"),Map.entry("end_city","末地城"),Map.entry("ancient_city","远古城市"),Map.entry("monument","海底神殿"),Map.entry("mansion","林地府邸"),Map.entry("pillager_outpost","掠夺者前哨站"),Map.entry("desert_pyramid","沙漠神殿"),Map.entry("jungle_pyramid","丛林神庙"),Map.entry("swamp_hut","女巫小屋"),Map.entry("igloo","雪屋"),Map.entry("buried_treasure","埋藏的宝藏"),Map.entry("shipwreck","沉船"),Map.entry("shipwreck_beached","搁浅沉船"),Map.entry("ocean_ruin_cold","冷水海底废墟"),Map.entry("ocean_ruin_warm","暖水海底废墟"),Map.entry("nether_fossil","下界化石"));
 public static String display(String raw){String id=raw.replace("结构 · ","");String[] parts=id.split(":",2);if(parts.length!=2)return id;String path=parts[1];if(parts[0].equals("minecraft")){if(path.startsWith("ruined_portal"))return "废弃传送门";String name=NAMES.get(path);if(name!=null)return name;}String key="structure."+parts[0]+"."+path.replace('/','.');return I18n.exists(key)?I18n.get(key):id;}
}
