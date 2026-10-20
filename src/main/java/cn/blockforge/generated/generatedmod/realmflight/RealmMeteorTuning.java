package cn.blockforge.generated.generatedmod.realmflight;

/** Cosmetic units: blocks, seconds at 20 ticks/s, and straight alpha. */
public final class RealmMeteorTuning {
    public static final double TRAIL_LIFETIME_TICKS=60;
    public static final double MAX_TRAIL_LENGTH=68;
    public static final double ROOT_WIDTH_RATIO=.94;
    public static final int NEAR_RADIAL_SEGMENTS=8,MEDIUM_RADIAL_SEGMENTS=6,FAR_RADIAL_SEGMENTS=4;
    public static final double[] LAYER_WIDTH={1.08,1.0,.42};
    public static final float[] LAYER_ALPHA={.34F,.78F,.57F};
    public static final float ORB_ALPHA=.64F;
    public static final double TAKEOFF_SECONDS=.30,LANDING_SECONDS=.35,LENGTH_SECONDS=.25;
    public static final double ORB_STRETCH_MAX=1.48,ENERGY_PULSE_STRENGTH=.08,TRAIL_TURBULENCE_STRENGTH=.04;
    public static final double TRAIL_FLOW_SPEED=1;
    public static final double NEAR_DETAIL_DISTANCE=32,MEDIUM_DETAIL_DISTANCE=64,FAR_DETAIL_DISTANCE=128;
    /** Shared dimensions for both the leading volume and its trailing skin. */
    public static double headRadius(double motion){return RealmFlightAppearance.profile(4).radius()*(.90-.025*unit(motion));}
    public static double headLength(double motion){return RealmFlightAppearance.profile(4).radius()*(1.38+(ORB_STRETCH_MAX-1.38)*unit(motion));}
    public static double headWrapWidth(double behind,double nose,double radius){
        if(!Double.isFinite(behind)||!Double.isFinite(nose)||!Double.isFinite(radius)||nose<=0||radius<=0)return 0;
        double forward=unit(-behind/nose);
        return radius*Math.sqrt(Math.max(0,1-forward*forward));
    }
    public static double unit(double value){return Double.isFinite(value)?Math.max(0,Math.min(1,value)):0;}
    public static double smooth(double value){double t=unit(value);return t*t*(3-2*t);}
    public static double speedWeight(double speed,int realm,int stage){
        double cruise=RealmFlightBalance.speed(realm,stage,false)/20;
        double fast=RealmFlightBalance.speed(realm,stage,true)/20;
        return smooth((speed-cruise*.25)/Math.max(.001,fast-cruise*.25));
    }
    public static double targetLength(double speed,int realm,int stage){return targetLength(speed,realm,stage,realm);}
    public static double targetLength(double speed,int realm,int stage,int visualRealm){
        if(!Double.isFinite(speed)||speed<.015)return 0;
        var profile=RealmFlightAppearance.profile(visualRealm);
        double cruise=RealmFlightBalance.speed(realm,stage,false)/20;
        double fast=RealmFlightBalance.speed(realm,stage,true)/20;
        double activation=smooth(speed/Math.max(.30,Math.min(.55,cruise*.40)));
        double boost=smooth((speed-cruise)/Math.max(.001,fast-cruise));
        return Math.min(MAX_TRAIL_LENGTH,activation*(profile.length()+(profile.boostLength()-profile.length())*boost));
    }
    /** Largest at the body, always shrinking into a zero-width terminal. */
    public static double width(double behind,double length,double radius){
        double remaining=1-unit(behind/Math.max(.001,length));
        double shoulder=1-.20*smooth(behind/8);
        return radius*ROOT_WIDTH_RATIO*shoulder*Math.pow(remaining,.85);
    }
    /** Shared front/body/wake radiance envelope, in world blocks behind the body. */
    public static double bodyRadiance(double behind){
        double[] distance={-2,0,1.2,2.4,8,30},value={1,.70,.45,.25,.16,0};
        if(behind<=distance[0])return 1;
        for(int i=1;i<distance.length;i++)if(behind<=distance[i])return value[i-1]+(value[i]-value[i-1])*smooth((behind-distance[i-1])/(distance[i]-distance[i-1]));
        return 0;
    }
    /** Body and wake share full coverage; only the forward nose fades in. */
    public static double join(double behind){return smooth((behind+1.0)/.65);}
    /** Forward extension follows the orb, then meets the historical taper at its equator. */
    public static double wrapWidth(double behind,double length,double radius){
        if(behind>=0)return width(behind,length,radius);
        double front=unit(-behind/(radius*ORB_STRETCH_MAX));
        return radius*ROOT_WIDTH_RATIO*Math.sqrt(Math.max(0,1-front*front));
    }
    public static double filter(double current,double target,double seconds){
        return current+(target-current)*(1-Math.exp(-.05/seconds));
    }
    private RealmMeteorTuning(){}
}
