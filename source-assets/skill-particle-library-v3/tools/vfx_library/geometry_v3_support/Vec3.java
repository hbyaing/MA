package net.minecraft.world.phys;

/** Minimal numerical adapter for an isolated, source-timeline replay. */
public final class Vec3 {
    public static final Vec3 ZERO=new Vec3(0,0,0);
    public final double x,y,z;
    public Vec3(double x,double y,double z){this.x=x;this.y=y;this.z=z;}
    public Vec3 add(Vec3 v){return new Vec3(x+v.x,y+v.y,z+v.z);}
    public Vec3 add(double a,double b,double c){return new Vec3(x+a,y+b,z+c);}
    public Vec3 subtract(Vec3 v){return new Vec3(x-v.x,y-v.y,z-v.z);}
    public Vec3 scale(double v){return new Vec3(x*v,y*v,z*v);}
    public Vec3 lerp(Vec3 v,double t){return add(v.subtract(this).scale(t));}
    public double horizontalDistance(){return Math.hypot(x,z);}
    public double length(){return Math.sqrt(x*x+y*y+z*z);}
    public double dot(Vec3 v){return x*v.x+y*v.y+z*v.z;}
    public double distanceToSqr(Vec3 v){Vec3 d=subtract(v);return d.dot(d);}
    public double distanceTo(Vec3 v){return Math.sqrt(distanceToSqr(v));}
}
