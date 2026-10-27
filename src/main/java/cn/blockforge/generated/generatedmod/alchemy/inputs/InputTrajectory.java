package cn.blockforge.generated.generatedmod.alchemy.inputs;

import cn.blockforge.generated.generatedmod.alchemy.simulation.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import java.util.*;

/** SERVER PRIVATE adapter session. Not a packet DTO or a substitute for the stage-05 item transaction. */
public final class InputTrajectory {
    private final InputDefinitions definitions;
    private final AlchemyInputs.Fire fire;
    public InputTrajectory(InputDefinitions definitions,AlchemyInputs.Fire fire){this.definitions=Objects.requireNonNull(definitions);this.fire=Objects.requireNonNull(fire);}
    public AlchemyEnvironment.Sample sample(ServerLevel level,BlockPos pos){
        var world=AlchemyEnvironment.sample(level,pos,definitions);var values=new TreeMap<>(world.values());
        values.put("fire_tier",(long)fire.tier());
        fire.essence().forEach((key,value)->values.put("fire_"+key,Math.round(value*1000)));
        return new AlchemyEnvironment.Sample(world.dimension(),world.gameTime(),world.dayTime(),values);
    }
    public record Control(AlchemySimulation.Action action,String resource,long amount){}
    /** Environment events precede this step's authorized controls; no historical sample is rewritten. */
    public AlchemySimulation.Step step(SimulationRules rules,AlchemySimulation.Snapshot state,ServerLevel level,BlockPos pos,List<Control> controls){return step(rules,state,level,pos,controls,false);}
    public AlchemySimulation.Step step(SimulationRules rules,AlchemySimulation.Snapshot state,ServerLevel level,BlockPos pos,List<Control> controls,boolean deviceFailure){
        if(controls.size()>16)throw new IllegalArgumentException("Control budget exceeded");
        var operations=new ArrayList<>(AlchemyEnvironment.changes(state,sample(level,pos)));
        long sequence=state.lastSequence()+operations.size()+1;long totalHeat=0;
        for(var command:controls){
            if(command.action()==AlchemySimulation.Action.ENVIRONMENT)throw new IllegalArgumentException("Environment comes only from server world");
            if(command.action()==AlchemySimulation.Action.HEAT){totalHeat=Math.addExact(totalHeat,command.amount());if(totalHeat>definitions.fireMaximumRate(fire.id()))throw new IllegalArgumentException("Fire input rate exceeded");}
            operations.add(new AlchemySimulation.Operation(sequence++,state.tick(),command.action(),command.resource(),command.amount()));
        }
        if(deviceFailure)operations.add(new AlchemySimulation.Operation(sequence,state.tick(),AlchemySimulation.Action.DEVICE_FAILURE,"",0));
        return AlchemySimulation.step(rules,state,operations);
    }
}
