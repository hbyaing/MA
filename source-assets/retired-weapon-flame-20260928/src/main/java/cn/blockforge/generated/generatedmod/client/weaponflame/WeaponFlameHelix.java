package cn.blockforge.generated.generatedmod.client.weaponflame;

/** Reusable local-space helix. Side vectors are perpendicular to its tangent, not to world Y. */
public final class WeaponFlameHelix {
    public static final int SEGMENTS=48;
    public record Point(double x,double y,double z) {
        public Point add(Point p){return new Point(x+p.x,y+p.y,z+p.z);}
        public Point subtract(Point p){return new Point(x-p.x,y-p.y,z-p.z);}
        public Point scale(double s){return new Point(x*s,y*s,z*s);}
        public double dot(Point p){return x*p.x+y*p.y+z*p.z;}
        public Point cross(Point p){return new Point(y*p.z-z*p.y,z*p.x-x*p.z,x*p.y-y*p.x);}
        public double length(){return Math.sqrt(dot(this));}
        public Point normalize(){return scale(1/length());}
    }
    public final float[] positions=new float[(SEGMENTS+1)*3];
    public final float[] sides=new float[(SEGMENTS+1)*3];
    public void update(Point hilt,Point tip,Point widthHint,double radius,double turns,double phase) {
        Point delta=tip.subtract(hilt),axis=delta.normalize();
        Point right=widthHint.subtract(axis.scale(widthHint.dot(axis)));
        if(right.length()<1e-6) {
            Point fallback=Math.abs(axis.x)<.8?new Point(1,0,0):new Point(0,0,1);
            right=fallback.subtract(axis.scale(fallback.dot(axis)));
        }
        right=right.normalize();Point up=axis.cross(right).normalize();
        double angular=turns*Math.PI*2;
        for(int i=0;i<=SEGMENTS;i++) {
            double t=i/(double)SEGMENTS,theta=phase+t*angular,c=Math.cos(theta),s=Math.sin(theta);
            double rx=right.x*c+up.x*s,ry=right.y*c+up.y*s,rz=right.z*c+up.z*s;
            int j=i*3;
            positions[j]=(float)(hilt.x+delta.x*t+rx*radius);
            positions[j+1]=(float)(hilt.y+delta.y*t+ry*radius);
            positions[j+2]=(float)(hilt.z+delta.z*t+rz*radius);
            double tx=delta.x+radius*angular*(-right.x*s+up.x*c);
            double ty=delta.y+radius*angular*(-right.y*s+up.y*c);
            double tz=delta.z+radius*angular*(-right.z*s+up.z*c);
            double sx=ry*tz-rz*ty,sy=rz*tx-rx*tz,sz=rx*ty-ry*tx;
            double length=Math.sqrt(sx*sx+sy*sy+sz*sz);
            sides[j]=(float)(sx/length);sides[j+1]=(float)(sy/length);sides[j+2]=(float)(sz/length);
        }
    }
}
