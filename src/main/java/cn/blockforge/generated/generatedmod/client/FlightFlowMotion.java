package cn.blockforge.generated.generatedmod.client;

/** Tick snapshots: changing speed changes future advection, never its accumulated position. */
public final class FlightFlowMotion {
    private double previousPhase, phase, previousStrength, strength, speed, boost;
    private boolean moving;
    public void reset(){previousPhase=phase=previousStrength=strength=speed=boost=0;moving=false;}
    public void tick(double velocity, boolean sprint){
        previousPhase=phase;previousStrength=strength;
        // Hysteresis avoids toggling around the movement threshold on ascent and turns.
        moving=velocity>(moving?.015:.025);
        speed+=(Math.min(1,Math.max(0,velocity)/1.2)-speed)*.35;
        boost+=((sprint?1:0)-boost)*.35;
        double target=moving?.45+speed*.20+boost*.35:0;
        strength+=(target-strength)*.35;
        if(!moving&&strength<.005)strength=0;
        phase+=(.8+speed*1.4+boost*1.7)/20.;
    }
    public double phase(double partial){return previousPhase+(phase-previousPhase)*clamp(partial);}
    public double strength(double partial){return previousStrength+(strength-previousStrength)*clamp(partial);}
    private static double clamp(double value){return Math.max(0,Math.min(1,value));}
}
