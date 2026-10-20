package cn.blockforge.generated.generatedmod.client;

/** Forge coordinates: blade along +Y, broad face in XY, grip at the origin. */
public final class HeartForgeLayout {
    public static final float THIRD_PERSON_SCALE=1.30F;
    public static final float ARM_HALF=6F/16;
    public static final float PALM_CLEARANCE=.065F;
    public record Point(float x,float y,float z) {
        public Point add(Point p){return new Point(x+p.x,y+p.y,z+p.z);}
        public Point subtract(Point p){return new Point(x-p.x,y-p.y,z-p.z);}
        public Point scale(float s){return new Point(x*s,y*s,z*s);}
        public Point handed(boolean leftHanded){return leftHanded?new Point(-x,y,z):this;}
        public float dot(Point p){return x*p.x+y*p.y+z*p.z;}
        public float length(){return (float)Math.sqrt(dot(this));}
    }
    public static Point firstPerson(float along,float face) {return new Point(.38F-along,-.30F,-.82F+face);}
    public static Point thirdPerson(float along,float face) {return new Point(-.28F+along,.28F,-.55F+face);}
    /** Fixed six-pixel bones, with the holding forearm level in front of the chest. */
    public static Point elbow(Point shoulder,Point wrist,boolean holding) {
        if(holding) {
            float dx=wrist.x-shoulder.x,dz=wrist.z-shoulder.z,dy=wrist.y-shoulder.y;
            float distance=(float)Math.sqrt(dx*dx+dz*dz);
            float upperSquared=ARM_HALF*ARM_HALF-dy*dy;
            float along=(upperSquared-ARM_HALF*ARM_HALF+distance*distance)/(2*distance);
            float bend=(float)Math.sqrt(Math.max(0,upperSquared-along*along));
            return new Point(shoulder.x+dx/distance*along+dz/distance*bend,wrist.y,
                    shoulder.z+dz/distance*along-dx/distance*bend);
        }
        Point delta=wrist.subtract(shoulder);
        float distance=delta.length();Point axis=delta.scale(1/distance);
        Point down=new Point(0,1,0);Point bend=down.subtract(axis.scale(down.dot(axis)));
        float height=(float)Math.sqrt(Math.max(0,ARM_HALF*ARM_HALF-distance*distance/4));
        return shoulder.add(delta.scale(.5F)).add(bend.scale(height/bend.length()));
    }
    private HeartForgeLayout() {}
}
