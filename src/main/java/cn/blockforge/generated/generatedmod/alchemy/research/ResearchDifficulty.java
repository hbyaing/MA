package cn.blockforge.generated.generatedmod.alchemy.research;

import cn.blockforge.generated.generatedmod.alchemy.simulation.*;
import net.minecraft.nbt.*;
import java.util.*;

/** Two independent profiles, never a grade multiplier or an invented success probability. */
public final class ResearchDifficulty {
    /** Paid warming can extend a phase; do not diagnose against nominal durations alone. */
    public static int[] stageBudgets(int[] nominal,List<AlchemySimulation.Operation> operations,long ticks){
        var result=nominal.clone();int phase=0;long start=0;
        for(var op:operations)if(op.action()==AlchemySimulation.Action.ADVANCE&&phase<5){result[phase]=Math.max(result[phase],Math.toIntExact(op.tick()-start));start=op.tick();phase++;}
        result[phase]=Math.max(result[phase],Math.toIntExact(ticks-start));return result;
    }
    public static CompoundTag operation(SimulationRules rules,Set<String> known,int[] phases,long heatQuantum,long capacity){
        if(phases.length!=6||heatQuantum<1||capacity<1)throw new IllegalArgumentException("Invalid apparatus projection");
        int windows=0,maxHold=1,unreachable=0,unknown=0;long smallest=Long.MAX_VALUE;boolean demanding=false;
        for(var r:rules.reactions){if(!known.contains(r.id())){unknown++;continue;}maxHold=Math.max(maxHold,r.holdSteps());if(r.holdSteps()>phases[r.phase().ordinal()])unreachable++;
            for(var w:r.windows()){windows++;long span=Math.subtractExact(w.maximum(),w.minimum());
                if(w.measure()==SimulationRules.Measure.TEMPERATURE){smallest=Math.min(smallest,span);long energyWidth=Math.multiplyExact(span,capacity);if(energyWidth<Math.addExact(heatQuantum,2L*rules.limits.controlNoise()))demanding=true;}
            }
        }
        var n=new CompoundTag();n.putInt("KnownWindows",windows);n.putInt("Hold",maxHold);n.putInt("UnreachableRules",unreachable);n.putInt("UnmappedRules",unknown);n.putLong("TemperatureSpan",smallest==Long.MAX_VALUE?-1:smallest);n.putLong("HeatQuantum",heatQuantum);n.putLong("Noise",rules.limits.controlNoise());n.putBoolean("Demanding",demanding);n.putBoolean("Complete",unknown==0);
        return n;
    }
    public static CompoundTag inference(int candidates,int conflicting,int controls,int unavailable){if(candidates<0||conflicting<0||controls<0||unavailable<0)throw new IllegalArgumentException("Negative inference count");var n=new CompoundTag();n.putInt("EquivalentRoutes",candidates);n.putInt("ConflictingRoutes",conflicting);n.putInt("SingleFactorPairs",controls);n.putInt("Unavailable",unavailable);n.putBoolean("Ambiguous",candidates!=1||conflicting>0||unavailable>0);return n;}
    public static String researchText(CompoundTag n){return "已知候选："+n.getInt("EquivalentRoutes")+"\n矛盾路线："+n.getInt("ConflictingRoutes")+"\n单因素对照："+n.getInt("SingleFactorPairs")+"\n缺失记录："+n.getInt("Unavailable")+"\n"+(n.getBoolean("Ambiguous")?"研究仍有歧义":"已知范围内路线较少")+"\n未知材料路线仍未排除";}
    public static String operationText(CompoundTag n){return "已知窗口："+n.getInt("KnownWindows")+"\n连续保持："+n.getInt("Hold")+"步\n"+(n.getLong("TemperatureSpan")<0?"未规定温度窄窗":"最小温窗跨度："+n.getLong("TemperatureSpan"))+"\n"+(n.getBoolean("Demanding")?"模型提示需精细控火":"当前已知控火约束较宽")+"\n"+(n.getInt("UnreachableRules")>0?"当前阶段计划时长不足":n.getBoolean("Complete")?"依公开模型评估":"未知工序仍待识别");}
    private ResearchDifficulty(){}
}
