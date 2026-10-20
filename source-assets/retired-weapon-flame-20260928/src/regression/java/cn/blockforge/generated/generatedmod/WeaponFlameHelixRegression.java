package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.client.weaponflame.WeaponFlameHelix;
import cn.blockforge.generated.generatedmod.client.weaponflame.WeaponFlameHelix.Point;

/** Numeric geometry contracts, not a substitute for real-client visual acceptance. */
final class WeaponFlameHelixRegression {
    private static void near(double expected,double actual,String label) {
        if(Math.abs(expected-actual)>1e-5)throw new AssertionError(label+": "+actual+" != "+expected);
    }
    private static Point read(float[] a,int i){return new Point(a[i*3],a[i*3+1],a[i*3+2]);}
    private static Point rotate(Point p){return new Point(p.z(),p.x(),p.y());}
    static void run() {
        var h=new Point(.675,.5,.5);var tip=new Point(.012,.5,.5);var right=new Point(0,0,1);
        var axis=tip.subtract(h).normalize();var up=axis.cross(right);
        double radius=.071484375*.86*1.1,turns=1.05,phase=.7;
        var mesh=new WeaponFlameHelix();mesh.update(h,tip,right,radius,turns,phase);
        var rotated=new WeaponFlameHelix();var translation=new Point(2,-3,5);
        rotated.update(rotate(h).add(translation),rotate(tip).add(translation),rotate(right),radius,turns,phase);
        double previous=phase,totalAngle=0;
        for(int i=0;i<=WeaponFlameHelix.SEGMENTS;i++) {
            double t=i/(double)WeaponFlameHelix.SEGMENTS;
            var p=read(mesh.positions,i);var side=read(mesh.sides,i);
            var center=h.add(tip.subtract(h).scale(t));var radial=p.subtract(center);
            near(radius,radial.length(),"Circular clearance is independent of blade thickness");
            near(0,radial.dot(axis),"All centers remain on the anchor axis");
            double angle=Math.atan2(radial.dot(up),radial.dot(right));
            if(i>0)totalAngle+=Math.atan2(Math.sin(angle-previous),Math.cos(angle-previous));
            previous=angle;
            double theta=phase+t*turns*Math.PI*2;
            var tangent=tip.subtract(h).add(right.scale(-Math.sin(theta)*radius*turns*Math.PI*2))
                    .add(up.scale(Math.cos(theta)*radius*turns*Math.PI*2)).normalize();
            near(1,side.length(),"Ribbon width does not collapse");
            near(0,side.dot(tangent),"Width is perpendicular to the path tangent");
            near(0,side.dot(radial),"Width lies on the local cylindrical surface");
            near(0,read(rotated.positions,i).subtract(rotate(p).add(translation)).length(),"Sword transforms carry the entire helix");
            near(0,read(rotated.sides,i).subtract(rotate(side)).length(),"Width rotates with the sword");
        }
        near(turns*Math.PI*2,totalAngle,"Exactly 1.05 turns from hilt to tip");
        var fallback=new WeaponFlameHelix();fallback.update(h,tip,axis,radius,turns,phase);
        for(float v:fallback.positions)if(!Float.isFinite(v))throw new AssertionError("Parallel hint generated invalid coordinates");
        System.out.println("PASS: helix radius, anchor projection, tangent/width, rigid rotation, winding and fallback contracts (49 nodes)");
    }
}
