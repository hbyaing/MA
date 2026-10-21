package cn.blockforge.generated.generatedmod.realmflight;

/** Fixed-tick camera distance only; rendering reads two endpoints without advancing them. */
public final class RealmFlightCameraDynamics {
    public static final double NORMAL_DISTANCE=4, BOOST_DISTANCE=6.8;
    private static final double TICK_SECONDS=.05, PULL_SECONDS=.32, RETURN_SECONDS=.25;
    private double previous,current,target;

    public void tick(boolean active,boolean boosting,double horizontalSpeed,double cruiseSpeed,double boostedSpeed){
        if(!active){reset();return;}
        previous=current;
        double weight=boostWeight(boosting,horizontalSpeed,cruiseSpeed,boostedSpeed);
        target=(BOOST_DISTANCE-NORMAL_DISTANCE)*weight;
        double response=1-Math.exp(-TICK_SECONDS/(target>current?PULL_SECONDS:RETURN_SECONDS));
        current+=(target-current)*response;
        if(Math.abs(current-target)<.0001)current=target;
        current=Math.max(0,Math.min(BOOST_DISTANCE-NORMAL_DISTANCE,current));
    }

    /** Pressing sprint at rest, cruising or against a wall cannot pull the camera back. */
    public static double boostWeight(boolean boosting,double horizontalSpeed,double cruiseSpeed,double boostedSpeed){
        if(!boosting||!Double.isFinite(horizontalSpeed)||!Double.isFinite(cruiseSpeed)
                ||!Double.isFinite(boostedSpeed)||cruiseSpeed<=0||boostedSpeed<=cruiseSpeed)return 0;
        double threshold=cruiseSpeed*1.04;
        if(boostedSpeed<=threshold)return 0;
        double x=Math.max(0,Math.min(1,(horizontalSpeed-threshold)/(boostedSpeed-threshold)));
        return x*x*(3-2*x);
    }

    public double distance(float partial){
        double t=Float.isFinite(partial)?Math.max(0,Math.min(1,partial)):0;
        if(t==0)return previousDistance();
        if(t==1)return currentDistance();
        return NORMAL_DISTANCE+previous+(current-previous)*t;
    }
    public double previousDistance(){return NORMAL_DISTANCE+previous;}
    public double currentDistance(){return NORMAL_DISTANCE+current;}
    public double targetDistance(){return NORMAL_DISTANCE+target;}
    /** Exiting flight/world/camera entity restores both interpolation endpoints immediately. */
    public void reset(){previous=current=target=0;}
}
