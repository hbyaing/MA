package cn.blockforge.generated.generatedmod.realmflight;

/** View-only selection of old world-space wake; never creates a screen effect or moves history. */
public final class FirstPersonRealmTrail {
    private FirstPersonRealmTrail() {}
    private static double smooth(double x){x=Math.max(0,Math.min(1,x));return x*x*(3-2*x);}
    /** Keep the central sight cone clear even while looking back into the old wake. */
    public static double centerClearWeight(double cosine){return 1-smooth((cosine-.985)/.012);}
    public static float viewWeight(RealmMeteorPath.Vec look,RealmMeteorPath.Vec travel) {
        if(look==null||travel==null||!look.finite()||!travel.finite()||travel.lengthSquared()<1e-10)return 0;
        double dot=look.normalize().dot(travel.normalize());
        // Looking along the flight direction retains exactly the existing front view.
        return (float)smooth((.995-dot)/.03);
    }
    public static double historyWeight(double behind,double distance,double scale) {
        scale=Math.max(1,scale);
        // Never restore the head/body field, or surround the camera with its nearest rings.
        return smooth((behind/scale-.05)/.35)*smooth((distance/scale-.15)/.65);
    }
}
