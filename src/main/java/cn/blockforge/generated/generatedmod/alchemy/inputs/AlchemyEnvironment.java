package cn.blockforge.generated.generatedmod.alchemy.inputs;

import cn.blockforge.generated.generatedmod.alchemy.simulation.AlchemySimulation;
import cn.blockforge.generated.generatedmod.world.SpiritVeinData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import java.util.*;

/** World facts are conditions/rates, never free stocks. Sample once per actual loaded logical step. */
public final class AlchemyEnvironment {
    public record Sample(String dimension,long gameTime,long dayTime,Map<String,Long> values){
        public Sample {values=Collections.unmodifiableMap(new TreeMap<>(values));}
    }
    public static Sample sample(ServerLevel level,BlockPos furnace){
        return sample(level,furnace,InputDefinitions.current());
    }
    public static Sample sample(ServerLevel level,BlockPos furnace,InputDefinitions definitions){
        if(!level.hasChunkAt(furnace))throw new IllegalArgumentException("Cannot sample unloaded furnace");
        var sky=furnace.above();boolean skyDimension=level.dimensionType().hasSkyLight()&&level.dimensionType().natural();
        boolean visible=skyDimension&&sky.getY()>=level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING,sky.getX(),sky.getZ());boolean night=skyDimension&&level.isNight();
        boolean raining=skyDimension&&visible&&level.isRaining()&&level.getBiome(sky).value().getPrecipitationAt(sky)!=net.minecraft.world.level.biome.Biome.Precipitation.NONE,thunder=skyDimension&&level.isThundering();
        int phase=level.getMoonPhase();long moon=definitions.moonStrength.get(phase);
        if(!night||!visible)moon=0;else if(raining)moon=moon*definitions.rainTransmission/1000;
        var values=new TreeMap<String,Long>();
        values.put("night",night?1L:0L);values.put("open_sky",visible?1L:0L);
        values.put("moon_phase",(long)phase);values.put("full_moon_night",night&&visible&&phase==0?1L:0L);
        values.put("new_moon_night",night&&visible&&phase==4?1L:0L);
        values.put("moon_input_rate",moon);values.put("rain",raining?1L:0L);values.put("thunder",thunder?1L:0L);
        values.put("spirit_aura",(long)SpiritVeinData.get(level).auraAt(furnace));
        values.put("biome_temperature_milli",Math.round(level.getBiome(furnace).value().getBaseTemperature()*1000D));
        return new Sample(level.dimension().location().toString(),level.getGameTime(),level.getDayTime(),values);
    }
    /** Changes take effect now; they do not rewrite prior history or replenish any inventory. */
    public static List<AlchemySimulation.Operation> changes(AlchemySimulation.Snapshot state,Sample next){
        if(!state.environment().keySet().equals(next.values().keySet()))throw new IllegalArgumentException("Environment schema mismatch");
        var events=new ArrayList<AlchemySimulation.Operation>();long sequence=state.lastSequence()+1;
        for(var entry:next.values().entrySet())if(!entry.getValue().equals(state.environment().get(entry.getKey())))
            events.add(new AlchemySimulation.Operation(sequence++,state.tick(),AlchemySimulation.Action.ENVIRONMENT,entry.getKey(),entry.getValue()));
        return List.copyOf(events);
    }
    private AlchemyEnvironment(){}
}
