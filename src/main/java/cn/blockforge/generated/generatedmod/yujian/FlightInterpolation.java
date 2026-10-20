package cn.blockforge.generated.generatedmod.yujian;

import java.util.ArrayDeque;
import net.minecraft.util.Mth;

/** Bounded presentation buffer. Never extrapolates past a server-validated position. */
public final class FlightInterpolation {
    private final ArrayDeque<FlightSnapshot> samples=new ArrayDeque<>();
    private double offset;
    private double lastPlayback=Double.NEGATIVE_INFINITY;
    public void accept(FlightSnapshot snapshot,int clientTick){
        if(!samples.isEmpty()&&snapshot.tick()<=samples.getLast().tick())return;
        if(samples.isEmpty())offset=clientTick-snapshot.tick();
        else if(snapshot.tick()-samples.getLast().tick()>20){samples.clear();offset=clientTick-snapshot.tick();}
        // Server ticks remain consecutive across a server stall or delayed packet
        // delivery. Refill the buffer when the client clock has run beyond them.
        else if(clientTick-offset-3>snapshot.tick()+1)offset=clientTick-snapshot.tick();
        samples.addLast(snapshot);while(samples.size()>32)samples.removeFirst();
    }
    public FlightSnapshot sample(double clientTick){
        if(samples.isEmpty())return null;
        double time=Math.min(samples.getLast().tick(),Math.max(lastPlayback,clientTick-offset-3));
        lastPlayback=time;
        FlightSnapshot previous=samples.getFirst();
        if(time<=previous.tick())return previous;
        for(FlightSnapshot next:samples){
            if(next.tick()<=previous.tick())continue;
            if(time<=next.tick()){
                float t=(float)Mth.clamp((time-previous.tick())/(next.tick()-previous.tick()),0,1);
                return new FlightSnapshot(next.entityId(),(long)time,previous.position().lerp(next.position(),t),
                        Mth.rotLerp(t,previous.yaw(),next.yaw()),Mth.lerp(t,previous.pitch(),next.pitch()),
                        Mth.lerp(t,previous.roll(),next.roll()),Mth.lerp(t,previous.bank(),next.bank()),
                        Mth.lerp(t,previous.speed(),next.speed()),Mth.lerp(t,previous.angular(),next.angular()),
                        Mth.lerp(t,previous.jinghong(),next.jinghong()));
            }
            previous=next;
        }
        return samples.getLast();
    }
    public boolean active(){return !samples.isEmpty();}
}
