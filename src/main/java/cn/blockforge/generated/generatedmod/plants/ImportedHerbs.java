package cn.blockforge.generated.generatedmod.plants;

import java.util.*;
import static cn.blockforge.generated.generatedmod.plants.HerbProperty.*;

/** The 18 r115 xiantu species, adapted to target habitats/properties; no second profession system. */
public final class ImportedHerbs {
    public static final List<SpiritPlantDefinition> DEFINITIONS=new ArrayList<>();
    static {
        add(SpiritPlantDefinition.builder("herb_juling",WildRarity.UNCOMMON).medicinalCap(3).substrate(Substrate.SOIL).growth(8,5).fertilizer(1).herbs(SPIRIT, WOOD, GATHER_QI).build());
        add(SpiritPlantDefinition.builder("herb_huayuan",WildRarity.RARE).medicinalCap(4).substrate(Substrate.SOIL).growth(8,5).fertilizer(1).herbs(SPIRIT, CULTIVATION, HARMONY).build());
        add(SpiritPlantDefinition.builder("herb_xisui",WildRarity.RARE).medicinalCap(4).substrate(Substrate.SOIL).growth(8,5).fertilizer(1).herbs(CLEANSE, MERIDIAN, WOOD).build());
        add(SpiritPlantDefinition.builder("herb_pozhang",WildRarity.VERY_RARE).medicinalCap(5).substrate(Substrate.SOIL).growth(8,5).fertilizer(1).herbs(SPIRIT, YANG, CULTIVATION).build());
        add(SpiritPlantDefinition.builder("herb_huichun",WildRarity.UNCOMMON).medicinalCap(3).substrate(Substrate.SOIL).growth(8,5).fertilizer(1).herbs(WOOD, RECOVERY, VITALITY).build());
        add(SpiritPlantDefinition.builder("herb_xingchen",WildRarity.VERY_RARE).medicinalCap(5).substrate(Substrate.SOIL).growth(8,5).fertilizer(1).prefers(SpiritPlantDefinition.Preference.NIGHT_BLOOM).herbs(STAR, CLEANSE, HARMONY).build());
        add(SpiritPlantDefinition.builder("herb_tianji",WildRarity.VERY_RARE).medicinalCap(5).substrate(Substrate.SOIL).growth(8,5).fertilizer(1).prefers(SpiritPlantDefinition.Preference.NIGHT_BLOOM).herbs(SOUL, SPIRIT, FORTUNE).build());
        add(SpiritPlantDefinition.builder("herb_huanling",WildRarity.RARE).medicinalCap(4).substrate(Substrate.SOIL).growth(8,5).fertilizer(1).prefers(SpiritPlantDefinition.Preference.NIGHT_BLOOM).herbs(YIN, SOUL, ILLUSION).build());
        add(SpiritPlantDefinition.builder("herb_fengling",WildRarity.UNCOMMON).medicinalCap(3).substrate(Substrate.SOIL).growth(8,5).fertilizer(1).herbs(WOOD, NOURISH, ABUNDANCE).build());
        add(SpiritPlantDefinition.builder("herb_hanbing",WildRarity.RARE).medicinalCap(4).substrate(Substrate.SOIL).growth(8,5).fertilizer(1).prefers(SpiritPlantDefinition.Preference.LOW_LIGHT).herbs(WATER, COLD, YIN).build());
        add(SpiritPlantDefinition.builder("herb_lieyang",WildRarity.RARE).medicinalCap(4).substrate(Substrate.SOIL).growth(8,5).fertilizer(1).prefers(SpiritPlantDefinition.Preference.SUN_LIGHT).herbs(FIRE, YANG, BODY_REFINE).build());
        add(SpiritPlantDefinition.builder("herb_jingang",WildRarity.RARE).medicinalCap(4).substrate(Substrate.SOIL).growth(8,5).fertilizer(1).herbs(METAL, TOUGHNESS, TEMPER).build());
        add(SpiritPlantDefinition.builder("herb_duzhen",WildRarity.RARE).medicinalCap(4).substrate(Substrate.SOIL).growth(8,5).fertilizer(1).herbs(WOOD, YIN, VENOM).build());
        add(SpiritPlantDefinition.builder("herb_jifeng",WildRarity.RARE).medicinalCap(4).substrate(Substrate.SOIL).growth(8,5).fertilizer(1).herbs(WIND, MOVEMENT, HARMONY).build());
        add(SpiritPlantDefinition.builder("herb_lingzhi",WildRarity.RARE).medicinalCap(4).substrate(Substrate.SOIL).growth(8,5).fertilizer(1).herbs(WOOD, VITALITY, NOURISH).build());
        add(SpiritPlantDefinition.builder("herb_leiteng",WildRarity.RARE).medicinalCap(4).substrate(Substrate.SOIL).growth(8,5).fertilizer(1).prefers(SpiritPlantDefinition.Preference.THUNDER).herbs(THUNDER, WOOD, TEMPER).build());
        add(SpiritPlantDefinition.builder("herb_yanjia",WildRarity.RARE).medicinalCap(4).substrate(Substrate.SOIL).growth(8,5).fertilizer(1).herbs(EARTH, TOUGHNESS, BODY_REFINE).build());
        add(SpiritPlantDefinition.builder("herb_lingdao",WildRarity.UNCOMMON).medicinalCap(3).substrate(Substrate.SOIL).growth(8,5).fertilizer(1).herbs(WOOD, NOURISH, ABUNDANCE).build());
    }
    private static void add(SpiritPlantDefinition def){DEFINITIONS.add(def);}
    public static boolean install(){
        for(var def:DEFINITIONS){
            if(SpiritPlantCatalog.BY_ID.putIfAbsent(def.id,def)!=null)throw new IllegalStateException("Duplicate herb: "+def.id);
            SpiritPlantCatalog.ALL.add(def);
        }
        return true;
    }
    private ImportedHerbs(){}
}
