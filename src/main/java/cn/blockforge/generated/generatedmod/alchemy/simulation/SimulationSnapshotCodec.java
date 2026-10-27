package cn.blockforge.generated.generatedmod.alchemy.simulation;

import java.io.*;
import java.util.*;
import static cn.blockforge.generated.generatedmod.alchemy.simulation.AlchemySimulation.*;
import static cn.blockforge.generated.generatedmod.alchemy.simulation.SimulationRules.*;

/** Server-only, bounded binary persistence. Contains seed: NEVER use for player sync or shared notes. */
public final class SimulationSnapshotCodec {
    public static final int SCHEMA=1,MAX_BYTES=65536;
    public static byte[] encodeServerSnapshot(SimulationRules rules,Snapshot s){
        AlchemySimulation.validate(rules,s);
        try{var bytes=new ByteArrayOutputStream();var out=new DataOutputStream(bytes);
            out.writeInt(0x414c5635);out.writeInt(SCHEMA);out.writeUTF(ENGINE);out.writeUTF(RANDOM);out.writeUTF(s.batchId());out.writeUTF(s.rulesHash());
            out.writeLong(s.tick());out.writeByte(s.phase().ordinal());out.writeByte(s.status().ordinal());out.writeUTF(s.reason());map(out,s.stocks());
            out.writeLong(s.heat());out.writeLong(s.damage());out.writeLong(s.referenceMass());out.writeLong(s.seed());map(out,s.environment());map(out,s.draws());map(out,s.holds());out.writeLong(s.lastSequence());
            out.writeInt(s.operations().size());for(var op:s.operations()){out.writeLong(op.sequence());out.writeLong(op.tick());out.writeByte(op.action().ordinal());out.writeUTF(op.resource());out.writeLong(op.amount());}
            var l=s.ledger();for(long v:new long[]{l.initialMass(),l.residualMass(),l.ashMass(),l.ventedMass(),l.initialPotential(),l.authorizedPotential(),l.lostPotential(),l.ventedPotential(),l.initialEnergy(),l.externalEnergy(),l.boundEnergy(),l.lostEnergy(),l.ventedEnergy()})out.writeLong(v);
            out.flush();if(bytes.size()>MAX_BYTES)throw new IllegalArgumentException("Snapshot exceeds byte budget");return bytes.toByteArray();
        }catch(IOException e){throw new IllegalStateException(e);}
    }
    public static Snapshot decodeServerSnapshot(SimulationRules rules,byte[] bytes){
        if(bytes==null||bytes.length>MAX_BYTES)throw new IllegalArgumentException("Invalid snapshot bytes");
        try(var in=new DataInputStream(new ByteArrayInputStream(bytes))){
            if(in.readInt()!=0x414c5635||in.readInt()!=SCHEMA||!in.readUTF().equals(ENGINE)||!in.readUTF().equals(RANDOM))throw new IllegalArgumentException("Unknown snapshot/engine/random version");
            String batch=in.readUTF(),hash=in.readUTF();if(!hash.equals(rules.hash))throw new IllegalArgumentException("Rules hash mismatch; do not rewrite the old batch");
            long tick=in.readLong();Phase phase=enumeration(Phase.values(),in.readUnsignedByte());Status status=enumeration(Status.values(),in.readUnsignedByte());String reason=in.readUTF();var stocks=map(in,128);
            long heat=in.readLong(),damage=in.readLong(),reference=in.readLong(),seed=in.readLong();var environment=map(in,32);var draws=map(in,8);var holds=map(in,128);long sequence=in.readLong();
            int count=in.readInt();if(count<0||count>rules.limits.maxEvents())throw new IllegalArgumentException("Operation history exceeds budget");var ops=new ArrayList<Operation>();
            for(int i=0;i<count;i++)ops.add(new Operation(in.readLong(),in.readLong(),enumeration(Action.values(),in.readUnsignedByte()),in.readUTF(),in.readLong()));
            var l=new Ledger(in.readLong(),in.readLong(),in.readLong(),in.readLong(),in.readLong(),in.readLong(),in.readLong(),in.readLong(),in.readLong(),in.readLong(),in.readLong(),in.readLong(),in.readLong());
            if(in.available()!=0)throw new IllegalArgumentException("Trailing snapshot data");
            var snapshot=new Snapshot(batch,hash,tick,phase,status,reason,stocks,heat,damage,reference,seed,environment,draws,holds,sequence,ops,l);AlchemySimulation.validate(rules,snapshot);return snapshot;
        }catch(IOException|IndexOutOfBoundsException e){throw new IllegalArgumentException("Invalid/truncated server snapshot",e);}
    }
    private static void map(DataOutputStream out,Map<String,Long> map)throws IOException{out.writeInt(map.size());for(var e:new TreeMap<>(map).entrySet()){out.writeUTF(e.getKey());out.writeLong(e.getValue());}}
    private static Map<String,Long> map(DataInputStream in,int max)throws IOException{int count=in.readInt();if(count<0||count>max)throw new IllegalArgumentException("Map budget exceeded");var map=new TreeMap<String,Long>();for(int i=0;i<count;i++){String key=in.readUTF();if(map.putIfAbsent(key,in.readLong())!=null)throw new IllegalArgumentException("Duplicate snapshot key");}return map;}
    private static <T>T enumeration(T[] values,int index){if(index>=values.length)throw new IllegalArgumentException("Unknown enum version");return values[index];}
    private SimulationSnapshotCodec(){}
}
