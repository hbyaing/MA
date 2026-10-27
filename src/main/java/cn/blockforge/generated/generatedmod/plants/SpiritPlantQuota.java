package cn.blockforge.generated.generatedmod.plants;

import java.util.*;

/** Pure allocation policy, independent of chunks, load order and Minecraft registries. */
public final class SpiritPlantQuota {
    public record Plan(int target, Map<String,Integer> limits) {}
    public static Plan choose(long seed, boolean end, boolean restricted, Map<String,WildRarity> eligible) {
        Random random = new Random(seed);
        if(random.nextDouble() >= (end ? .08 : restricted ? .35 : .60)) return new Plan(0,Map.of());
        double roll=random.nextDouble(); int target=roll<.70?1:roll<.95?2:3;
        List<String> rare=new ArrayList<>(),ordinary=new ArrayList<>();
        for(var entry:new TreeMap<>(eligible).entrySet()) {
            WildRarity rarity=entry.getValue();
            // A special plant never leaks into an overworld region, even with a custom feature.
            if(end != (rarity==WildRarity.SPECIAL))continue;
            double chance=SpiritPlantEcology.chance(entry.getKey(),rarity);
            if(random.nextDouble()>=chance)continue;
            if(rarity==WildRarity.RARE||rarity==WildRarity.VERY_RARE)rare.add(entry.getKey());
            else ordinary.add(entry.getKey());
        }
        Collections.shuffle(rare,random);Collections.shuffle(ordinary,random);
        Map<String,Integer> limits=new HashMap<>();int slots=target;
        if(!rare.isEmpty()){limits.put(rare.get(0),1);slots--;}
        for(String species:ordinary) {
            if(slots==0)break;
            int limit=Math.min(slots,eligible.get(species)==WildRarity.COMMON?2:1);
            limits.put(species,limit);slots-=limit;
        }
        return new Plan(target,Map.copyOf(limits));
    }
    private SpiritPlantQuota() {}
}
