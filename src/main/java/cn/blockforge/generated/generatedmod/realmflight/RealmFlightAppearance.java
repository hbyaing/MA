package cn.blockforge.generated.generatedmod.realmflight;

/** Cosmetic tiers and a bounded turn filter; never grants or changes flight movement. */
public final class RealmFlightAppearance {
    public record Profile(int style,int color,double length,double boostLength,double width,double radius,double headLength,int sparkStride) {}
    private static final Profile[] PROFILES={
        new Profile(0,0x59DCFF,18,30,0,.85,.94,7),
        new Profile(1,0x65D9F2,28,44,1.42,1.38,1.38,12),
        new Profile(2,0xA78BFA,32,50,1.60,1.43,1.43,10),
        new Profile(3,0xFFD278,36,56,1.78,1.48,1.48,8),
        new Profile(4,0xDC9CFF,40,62,1.96,1.53,1.53,7),
        new Profile(5,0x7CF4DE,44,68,2.14,1.58,1.58,6)
    };
    public static Profile profile(int realm){return PROFILES[Math.max(0,Math.min(5,realm-3))];}
    public static final class Orientation {
        public RealmMeteorPath.Vec heading=new RealmMeteorPath.Vec(0,1,0),previousHeading=heading;
        public RealmMeteorPath.Vec side=new RealmMeteorPath.Vec(1,0,0),previousSide=side;
        public void tick(RealmMeteorPath.Vec desired,boolean moving){
            previousHeading=heading;previousSide=side;
            if(!moving||desired==null||!desired.finite()||desired.lengthSquared()<1e-12)return;
            desired=desired.normalize();double cosine=Math.max(-1,Math.min(1,heading.dot(desired))),angle=Math.acos(cosine);
            if(angle<1e-7)return;
            RealmMeteorPath.Vec axis=heading.cross(desired);
            // A true 180 degree reversal needs a stable axis: normalized lerp
            // with a weight below .5 would otherwise remain stuck forever.
            axis=axis.lengthSquared()>1e-12?axis.normalize():side;
            double turn=Math.min(angle*.42,.61);
            heading=rotate(heading,axis,turn).normalize();
            side=rotate(side,axis,turn);
            side=side.subtract(heading.scale(side.dot(heading))).normalize();
        }
        private static RealmMeteorPath.Vec rotate(RealmMeteorPath.Vec p,RealmMeteorPath.Vec axis,double angle){
            double c=Math.cos(angle),s=Math.sin(angle);
            return p.scale(c).add(axis.cross(p).scale(s)).add(axis.scale(axis.dot(p)*(1-c)));
        }
    }
    private RealmFlightAppearance(){}
}
