package cn.blockforge.generated.generatedmod.realmflight;

/** Speeds are blocks per second at 20 TPS, never vanilla ability coefficients. */
public final class RealmFlightBalance {
    private static final double[] CRUISE={8,11,14,18,23,28}, BOOST={12,16,21,27,34,42}, VERTICAL={4,5,6,8,10,12};
    private static int index(int realm){return Math.max(0,Math.min(5,realm-3));}
    public static double speed(int realm,int stage,boolean boost){return (boost?BOOST:CRUISE)[index(realm)]*(1+.04*Math.max(0,Math.min(3,stage)));}
    public static double vertical(int realm,int stage){return VERTICAL[index(realm)]*(1+.04*Math.max(0,Math.min(3,stage)));}
    public static double cost(int realm,boolean boost,double maximumMana){return realm<5?(boost?50:25):(boost?Math.max(0,maximumMana)*.005:0);}
    public static double approach(double current,double target,double step){return current<target?Math.min(target,current+step):Math.max(target,current-step);}
    public record Velocity(double x,double y,double z){}
    public static boolean doubleJump(int now,int first){return now>=first&&now-first<=7;}
    public static double launchVertical(boolean grounded,double inputY,double velocityY){return grounded&&inputY>=0?Math.max(.16,velocityY):velocityY;}
    public static Velocity advance(double x,double y,double z,double tx,double ty,double tz,double step){
        double dx=tx-x,dy=ty-y,dz=tz-z,length=Math.sqrt(dx*dx+dy*dy+dz*dz);
        if(length<=step)return new Velocity(tx,ty,tz);
        return new Velocity(x+dx*step/length,y+dy*step/length,z+dz*step/length);
    }
    private RealmFlightBalance(){}
}
