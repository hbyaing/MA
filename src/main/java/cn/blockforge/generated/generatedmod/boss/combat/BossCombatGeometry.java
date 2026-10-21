package cn.blockforge.generated.generatedmod.boss.combat;

/** Pure geometry shared by server hit tests and regression fixtures. Units are blocks/degrees. */
public final class BossCombatGeometry {
    public static double segmentDistance(double px,double pz,double ax,double az,double bx,double bz) {
        double dx=bx-ax,dz=bz-az,len=dx*dx+dz*dz;
        double t=len<1.0E-9?0:Math.max(0,Math.min(1,((px-ax)*dx+(pz-az)*dz)/len));
        return Math.hypot(px-ax-t*dx,pz-az-t*dz);
    }
    public static boolean cone(double px,double pz,double ax,double az,double bx,double bz,double radius,double degrees) {
        double dx=px-ax,dz=pz-az,distance=Math.hypot(dx,dz),fx=bx-ax,fz=bz-az,length=Math.hypot(fx,fz);
        if(distance>radius)return false;
        if(distance<.01)return true;
        return length>.0001&&(dx*fx+dz*fz)/(distance*length)>=Math.cos(Math.toRadians(degrees*.5));
    }
    public static int phase(float ratio,int phases) {
        if(phases<=2)return ratio<=.60F?2:1;
        return ratio<=.30F?3:ratio<=.65F?2:1;
    }
    public static float budget(float requested,float already,float limit) {
        if(!Float.isFinite(requested)||!Float.isFinite(already)||!Float.isFinite(limit))return 0;
        return Math.max(0,Math.min(requested,limit-already));
    }
    private BossCombatGeometry(){}
}
